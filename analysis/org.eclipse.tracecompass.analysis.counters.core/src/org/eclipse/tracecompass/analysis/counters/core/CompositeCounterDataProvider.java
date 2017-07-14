/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.analysis.counters.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.analysis.counters.core.Messages;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.AbstractTmfTraceDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.TmfCommonXAxisResponseFactory;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfXYDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.ImmutableMap;

/**
 * This data provider supports experiments for Counters
 *
 * TODO : Please make a generic data provider that support experiments and
 * encapsulates a list of data providers
 *
 * @author Yonni Chen
 */
@SuppressWarnings("restriction")
public class CompositeCounterDataProvider extends AbstractTmfTraceDataProvider implements ITmfXYDataProvider {

    private static final String TITLE = Objects.requireNonNull(Messages.CounterDataProvider_ChartTitle);

    /* Each data provider is associated with a trace ID */
    private final Map<UUID, CounterDataProvider> fDataProviders;

    /**
     * Create an instance of {@link CompositeCounterDataProvider}. For each sub
     * trace, a data provider is created. A non null instance of
     * {@link CompositeCounterDataProvider} is created if it contains at least one
     * non null data provider. Either way, a null instance is returned
     *
     * @param trace
     *            A trace on which we are interested to fetch a model
     * @return A {@link CompositeCounterDataProvider} instance or null.
     */
    public static @Nullable CompositeCounterDataProvider create(ITmfTrace trace) {
        Map<UUID, CounterDataProvider> dataProviders = new HashMap<>();
        Iterable<CounterAnalysis> modules = TmfTraceUtils.getAnalysisModulesOfClass(trace, CounterAnalysis.class);
        for (CounterAnalysis module : modules) {
            ITmfTrace subTrace = module.getTrace();
            CounterDataProvider provider = CounterDataProvider.create(Objects.requireNonNull(subTrace), module);
            if (provider != null) {
                ITmfTrace t = Objects.requireNonNull(subTrace);
                dataProviders.put(Objects.requireNonNull(t.getUUID()), provider);
            }
        }

        if (!dataProviders.isEmpty()) {
            return new CompositeCounterDataProvider(trace, dataProviders);
        }
        return null;
    }

    /**
     * Constructor
     */
    private CompositeCounterDataProvider(ITmfTrace trace, Map<UUID, CounterDataProvider> providers) {
        super(trace);
        fDataProviders = ImmutableMap.copyOf(providers);
    }

    @Override
    public TmfModelResponse<ITmfCommonXAxisModel> fetchXY(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        boolean isComplete = true;
        ImmutableMap.Builder<String, IYModel> series = ImmutableMap.builder();

        for (CounterDataProvider dataProvider : fDataProviders.values()) {
            TmfModelResponse<ITmfCommonXAxisModel> response = dataProvider.fetchXY(filter, monitor);
            isComplete &= response.getStatus() == ITmfResponse.Status.COMPLETED;
            ITmfCommonXAxisModel model = response.getModel();
            if (model != null) {
                series.putAll(model.getYData());
            }
        }
        return TmfCommonXAxisResponseFactory.create(TITLE, filter.getTimesRequested(), series.build(), isComplete); // $NON-NLS-1$
    }
}
