/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.analysis.counters.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.analysis.counters.core.Messages;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.TmfTreeXYCompositeDataProvider;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * Factory to create instances of the {@link ITmfTreeXYDataProvider}.
 * Uses the DataProviderFactory endpoint.
 *
 * @author Loic Prieur-Drevon
 * @since 1.1
 */
public class CounterDataProviderFactory implements IDataProviderFactory {

    private static final String TITLE = Objects.requireNonNull(Messages.CounterDataProvider_ChartTitle);

    @Override
    public @Nullable ITmfTreeXYDataProvider<? extends ITmfTreeDataModel> createProvider(ITmfTrace trace) {
        Collection<ITmfTrace> traces = TmfTraceManager.getTraceSet(trace);
        if (traces.size() == 1) {
            List<ITmfTreeXYDataProvider<CounterEntryModel>> dataProviders = new ArrayList<>();
            for (CounterAnalysis module : TmfTraceUtils.getAnalysisModulesOfClass(trace, CounterAnalysis.class)) {
                ITmfTrace subTrace = module.getTrace();
                ITmfTreeXYDataProvider<CounterEntryModel> provider = CounterDataProvider.create(Objects.requireNonNull(subTrace), module);
                if (provider != null) {
                    dataProviders.add(provider);
                }
            }
            if (dataProviders.isEmpty()) {
                return null;
            } else if (dataProviders.size() == 1) {
                return dataProviders.get(0);
            }
            return new TmfTreeXYCompositeDataProvider<>(dataProviders, TITLE, CounterDataProvider.ID);
        }

        return TmfTreeXYCompositeDataProvider.create(traces, TITLE, CounterDataProvider.ID);
    }

}
