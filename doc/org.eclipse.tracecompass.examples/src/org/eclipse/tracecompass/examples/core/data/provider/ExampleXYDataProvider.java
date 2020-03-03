/*******************************************************************************
 * Copyright (c) 2020 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.examples.core.data.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.examples.core.analysis.ExampleStateSystemAnalysisModule;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.internal.tmf.core.model.tree.AbstractTreeDataProvider;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.TmfCommonXAxisModel;
import org.eclipse.tracecompass.tmf.core.model.YModel;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfXyModel;
import org.eclipse.tracecompass.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * An example of an XY data provider.
 *
 * This class is also in the developer documentation of Trace Compass. If it is
 * modified here, the doc should also be updated.
 *
 * @author Geneviève Bastien
 */
@SuppressWarnings("restriction")
@NonNullByDefault
public class ExampleXYDataProvider extends AbstractTreeDataProvider<ExampleStateSystemAnalysisModule, TmfTreeDataModel> implements ITmfTreeXYDataProvider<TmfTreeDataModel> {

    /**
     * Provider unique ID.
     */
    public static final String ID = "org.eclipse.tracecompass.examples.xy.dataprovider"; //$NON-NLS-1$
    private static final AtomicLong sfAtomicId = new AtomicLong();

    private final BiMap<Long, Integer> fIDToDisplayQuark = HashBiMap.create();
    private final Map<Integer, String> fQuarkToString = new HashMap<>();

    /**
     * Constructor
     *
     * @param trace
     *            The trace this data provider is for
     * @param analysisModule
     *            The analysis module
     */
    public ExampleXYDataProvider(ITmfTrace trace, ExampleStateSystemAnalysisModule analysisModule) {
        super(trace, analysisModule);
    }

    /**
     * Create the time graph data provider
     *
     * @param trace
     *            The trace for which is the data provider
     * @return The data provider
     */
    public static @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> create(ITmfTrace trace) {
        ExampleStateSystemAnalysisModule module = TmfTraceUtils.getAnalysisModuleOfClass(trace, ExampleStateSystemAnalysisModule.class, ExampleStateSystemAnalysisModule.ID);
        return module != null ? new ExampleXYDataProvider(trace, module) : null;
    }


    @Override
    public String getId() {
        return ID;
    }

    @Override
    protected boolean isCacheable() {
        return true;
    }

    @Override
    protected TmfTreeModel<TmfTreeDataModel> getTree(ITmfStateSystem ss, Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {
        // Make an entry for each base quark
        List<TmfTreeDataModel> entryList = new ArrayList<>();
        for (Integer quark : ss.getQuarks("CPUs", "*")) { //$NON-NLS-1$ //$NON-NLS-2$
            int statusQuark = ss.optQuarkRelative(quark, "Status"); //$NON-NLS-1$
            if (statusQuark != ITmfStateSystem.INVALID_ATTRIBUTE) {
                Long id = fIDToDisplayQuark.inverse().computeIfAbsent(statusQuark, q -> sfAtomicId.getAndIncrement());
                entryList.add(new TmfTreeDataModel(id, -1, ss.getAttributeName(quark)));
                fQuarkToString.put(statusQuark, ss.getAttributeName(quark));
            }
        }
        return new TmfTreeModel<>(Collections.emptyList(), entryList);
    }

    @Override
    public TmfModelResponse<ITmfXyModel> fetchXY(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        ITmfStateSystem ss = getAnalysisModule().getStateSystem();
        if (ss == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.ANALYSIS_INITIALIZATION_FAILED);
        }

        Map<Integer, double[]> quarkToValues = new HashMap<>();
        // Prepare the quarks to display
        Collection<Long> selectedItems = DataProviderParameterUtils.extractSelectedItems(fetchParameters);
        if (selectedItems == null) {
            // No selected items, take them all
            selectedItems = fIDToDisplayQuark.keySet();
        }
        List<Long> times = getTimes(ss, DataProviderParameterUtils.extractTimeRequested(fetchParameters));
        for (Long id : selectedItems) {
            Integer quark = fIDToDisplayQuark.get(id);
            if (quark != null) {
                quarkToValues.put(quark, new double[times.size()]);
            }
        }
        long[] nativeTimes = new long[times.size()];
        for (int i = 0; i < times.size(); i++) {
            nativeTimes[i] = times.get(i);
        }

        // Query the state system to fill the array of values
        try {
            for (ITmfStateInterval interval : ss.query2D(quarkToValues.keySet(), times)) {
                if (monitor != null && monitor.isCanceled()) {
                    return new TmfModelResponse<>(null, Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
                }
                double[] row = quarkToValues.get(interval.getAttribute());
                Object value = interval.getValue();
                if (row != null && (value instanceof Number)) {
                    Double dblValue = ((Number) value).doubleValue();
                    for (int i = 0; i < times.size(); i++) {
                        Long time = times.get(i);
                        if (interval.getStartTime() <= time && interval.getEndTime() >= time) {
                            row[i] = dblValue;
                        }
                    }
                }
            }
        } catch (IndexOutOfBoundsException | TimeRangeException | StateSystemDisposedException e) {
            return new TmfModelResponse<>(null, Status.FAILED, CommonStatusMessage.STATE_SYSTEM_FAILED);
        }
        Map<String, IYModel> models = new HashMap<>();
        for (Entry<Integer, double[]> values : quarkToValues.entrySet()) {
            models.put(String.valueOf(fQuarkToString.get(values.getKey())), new YModel(fIDToDisplayQuark.inverse().getOrDefault(values.getKey(), -1L), String.valueOf(fQuarkToString.get(values.getKey())), values.getValue()));
        }

        return new TmfModelResponse<>(new TmfCommonXAxisModel("Example XY data provider", nativeTimes, models), Status.COMPLETED, CommonStatusMessage.COMPLETED); //$NON-NLS-1$
    }

    private static List<Long> getTimes(ITmfStateSystem key, @Nullable List<Long> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        List<@NonNull Long> times = new ArrayList<>();
        for (long t : list) {
            if (key.getStartTime() <= t && t <= key.getCurrentEndTime()) {
                times.add(t);
            }
        }
        Collections.sort(times);
        return times;
    }

    @Deprecated
    @Override
    public TmfModelResponse<ITmfXyModel> fetchXY(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        Map<String, Object> parameters = FetchParametersUtils.timeQueryToMap(filter);
        return fetchXY(parameters, monitor);
    }
}
