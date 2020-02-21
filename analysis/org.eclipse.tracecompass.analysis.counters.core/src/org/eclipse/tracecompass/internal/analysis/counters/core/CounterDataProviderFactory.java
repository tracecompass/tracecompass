/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.counters.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.counters.core.CounterAnalysis;
import org.eclipse.tracecompass.internal.tmf.core.model.DataProviderDescriptor;
import org.eclipse.tracecompass.internal.tmf.core.model.xy.TmfTreeXYCompositeDataProvider;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor.ProviderType;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Factory to create instances of the {@link ITmfTreeXYDataProvider}.
 * Uses the DataProviderFactory endpoint.
 *
 * @author Loic Prieur-Drevon
 * @since 1.1
 */
public class CounterDataProviderFactory implements IDataProviderFactory {

    private static final String TITLE = Objects.requireNonNull(Messages.CounterDataProvider_ChartTitle);
    private static final Predicate<? super ITmfTrace> PREDICATE = t -> !Iterables.isEmpty(TmfTraceUtils.getAnalysisModulesOfClass(t, CounterAnalysis.class));
    private static final IDataProviderDescriptor DESCRIPTOR =
            new DataProviderDescriptor.Builder()
                        .setId(CounterDataProvider.ID)
                        .setName(TITLE)
                        .setDescription(Objects.requireNonNull(Messages.CounterDataProviderFactory_DescriptionText))
                        .setProviderType(ProviderType.TREE_TIME_XY)
                        .build();

    /**
     * @since 2.0
     */
    @Override
    public @Nullable ITmfTreeXYDataProvider<? extends ITmfTreeDataModel> createProvider(ITmfTrace trace) {
        Collection<ITmfTrace> traces = TmfTraceManager.getTraceSet(trace);
        if (traces.size() == 1) {
            List<ITmfTreeXYDataProvider<TmfTreeDataModel>> dataProviders = new ArrayList<>();
            for (CounterAnalysis module : TmfTraceUtils.getAnalysisModulesOfClass(trace, CounterAnalysis.class)) {
                ITmfTrace subTrace = module.getTrace();
                ITmfTreeXYDataProvider<TmfTreeDataModel> provider = CounterDataProvider.create(Objects.requireNonNull(subTrace), module);
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

    @Override
    public Collection<IDataProviderDescriptor> getDescriptors(@NonNull ITmfTrace trace) {
        Collection<ITmfTrace> traces = TmfTraceManager.getTraceSet(trace);
        return Iterables.any(traces, PREDICATE) ? Collections.singletonList(DESCRIPTOR) : Collections.emptyList();
    }

}
