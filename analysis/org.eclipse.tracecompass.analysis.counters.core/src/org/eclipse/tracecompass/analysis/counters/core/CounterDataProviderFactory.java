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
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.analysis.counters.core.Messages;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.TmfXYCompositeDataProvider;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * Factory to create instances of the {@link CompositeCounterDataProvider}. Uses the DataProviderFactory endpoint
 * @author Loic Prieur-Drevon
 */
public class CounterDataProviderFactory implements IDataProviderFactory {

    private static final String TITLE = Objects.requireNonNull(Messages.CounterDataProvider_ChartTitle);
    private static final String ID = "org.eclipse.tracecompass.analysis.counters.core.CounterDataProvider";

    @Override
    public @Nullable ITmfTreeDataProvider<ITmfTreeDataModel> createProvider(ITmfTrace trace) {
        List<CounterDataProvider> dataProviders = new ArrayList<>();
        Iterable<CounterAnalysis> modules = TmfTraceUtils.getAnalysisModulesOfClass(trace, CounterAnalysis.class);
        for (CounterAnalysis module : modules) {
            ITmfTrace subTrace = module.getTrace();
            CounterDataProvider provider = CounterDataProvider.create(Objects.requireNonNull(subTrace), module);
            if (provider != null) {
                dataProviders.add(provider);
            }
        }

        if (!dataProviders.isEmpty()) {
            return new TmfXYCompositeDataProvider(dataProviders, TITLE, ID);
        }
        return null;
    }

}
