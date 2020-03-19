/**********************************************************************
 * Copyright (c) 2020 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.examples.core.data.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.examples.core.analysis.ExampleStateSystemAnalysisModule;
import org.eclipse.tracecompass.internal.tmf.core.model.AbstractTmfTraceDataProvider;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.dataprovider.X11ColorUtils;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.IOutputStyleProvider;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.OutputStyleModel;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphArrow;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;

/**
 * An example of a time graph data provider.
 *
 * This class is also in the developer documentation of Trace Compass. If it is
 * modified here, the doc should also be updated.
 *
 * @author Geneviève Bastien
 */
@SuppressWarnings("restriction")
@NonNullByDefault
public class ExampleTimeGraphDataProvider extends AbstractTmfTraceDataProvider implements ITimeGraphDataProvider<@NonNull ITimeGraphEntryModel>, IOutputStyleProvider {

    /**
     * Provider unique ID.
     */
    public static final String ID = "org.eclipse.tracecompass.examples.timegraph.dataprovider"; //$NON-NLS-1$
    private static final AtomicLong sfAtomicId = new AtomicLong();
    private static final String STYLE0_NAME = "style0"; //$NON-NLS-1$
    private static final String STYLE1_NAME = "style1"; //$NON-NLS-1$
    private static final String STYLE2_NAME = "style2"; //$NON-NLS-1$

    /* The map of basic styles */
    private static final Map<String, OutputElementStyle> STATE_MAP;
    /*
     * A map of styles names to a style that has the basic style as parent, to
     * avoid returning complete styles for each state
     */
    private static final Map<String, OutputElementStyle> STYLE_MAP;

    static {
        /* Build three different styles to use as examples */
        ImmutableMap.Builder<String, OutputElementStyle> builder = new ImmutableMap.Builder<>();

        builder.put(STYLE0_NAME, new OutputElementStyle(null, ImmutableMap.of(StyleProperties.STYLE_NAME, STYLE0_NAME,
                StyleProperties.BACKGROUND_COLOR, String.valueOf(X11ColorUtils.toHexColor("blue")), //$NON-NLS-1$
                StyleProperties.HEIGHT, 0.5f,
                StyleProperties.OPACITY, 0.75f)));
        builder.put(STYLE1_NAME, new OutputElementStyle(null, ImmutableMap.of(StyleProperties.STYLE_NAME, STYLE1_NAME,
                StyleProperties.BACKGROUND_COLOR, String.valueOf(X11ColorUtils.toHexColor("yellow")), //$NON-NLS-1$
                StyleProperties.HEIGHT, 1.0f,
                StyleProperties.OPACITY, 1.0f)));
        builder.put(STYLE2_NAME, new OutputElementStyle(null, ImmutableMap.of(StyleProperties.STYLE_NAME, STYLE2_NAME,
                StyleProperties.BACKGROUND_COLOR, String.valueOf(X11ColorUtils.toHexColor("green")), //$NON-NLS-1$
                StyleProperties.HEIGHT, 0.75f,
                StyleProperties.OPACITY, 0.5f)));
        STATE_MAP = builder.build();

        /* build the style map too */
        builder = new ImmutableMap.Builder<>();
        builder.put(STYLE0_NAME, new OutputElementStyle(STYLE0_NAME));
        builder.put(STYLE1_NAME, new OutputElementStyle(STYLE1_NAME));
        builder.put(STYLE2_NAME, new OutputElementStyle(STYLE2_NAME));
        STYLE_MAP = builder.build();
    }

    private final BiMap<Long, Integer> fIDToDisplayQuark = HashBiMap.create();
    private ExampleStateSystemAnalysisModule fModule;

    /**
     * Constructor
     *
     * @param trace
     *            The trace this analysis is for
     * @param module
     *            The scripted analysis for this data provider
     */
    public ExampleTimeGraphDataProvider(ITmfTrace trace, ExampleStateSystemAnalysisModule module) {
        super(trace);
        fModule = module;
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
        return module != null ? new ExampleTimeGraphDataProvider(trace, module) : null;
    }

    @Override
    public TmfModelResponse<TmfTreeModel<@NonNull ITimeGraphEntryModel>> fetchTree(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        fModule.waitForInitialization();
        ITmfStateSystem ss = fModule.getStateSystem();
        if (ss == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.ANALYSIS_INITIALIZATION_FAILED);
        }

        boolean isComplete = ss.waitUntilBuilt(0);
        long endTime = ss.getCurrentEndTime();

        // Make an entry for each base quark
        List<ITimeGraphEntryModel> entryList = new ArrayList<>();
        for (Integer quark : ss.getQuarks("CPUs", "*")) { //$NON-NLS-1$ //$NON-NLS-2$
            Long id = fIDToDisplayQuark.inverse().computeIfAbsent(quark, q -> sfAtomicId.getAndIncrement());
            entryList.add(new TimeGraphEntryModel(id, -1, ss.getAttributeName(quark), ss.getStartTime(), endTime));
        }

        Status status = isComplete ? Status.COMPLETED : Status.RUNNING;
        String msg = isComplete ? CommonStatusMessage.COMPLETED : CommonStatusMessage.RUNNING;
        return new TmfModelResponse<>(new TmfTreeModel<>(Collections.emptyList(), entryList), status, msg);
    }

    @Override
    public @NonNull String getId() {
        return ID;
    }

    @Override
    public @NonNull TmfModelResponse<TimeGraphModel> fetchRowModel(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        ITmfStateSystem ss = fModule.getStateSystem();
        if (ss == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.ANALYSIS_INITIALIZATION_FAILED);
        }

        try {
            List<@NonNull ITimeGraphRowModel> rowModels = getDefaultRowModels(fetchParameters, ss, monitor);
            if (rowModels == null) {
                rowModels = Collections.emptyList();
            }
            return new TmfModelResponse<>(new TimeGraphModel(rowModels), Status.COMPLETED, CommonStatusMessage.COMPLETED);
        } catch (IndexOutOfBoundsException | TimeRangeException | StateSystemDisposedException e) {
            return new TmfModelResponse<>(null, Status.FAILED, CommonStatusMessage.STATE_SYSTEM_FAILED);
        }
    }

    private @Nullable List<ITimeGraphRowModel> getDefaultRowModels(Map<String, Object> fetchParameters, ITmfStateSystem ss, @Nullable IProgressMonitor monitor) throws IndexOutOfBoundsException, TimeRangeException, StateSystemDisposedException {
        Map<Integer, ITimeGraphRowModel> quarkToRow = new HashMap<>();
        // Prepare the quarks to display
        Collection<Long> selectedItems = DataProviderParameterUtils.extractSelectedItems(fetchParameters);
        if (selectedItems == null) {
            // No selected items, take them all
            selectedItems = fIDToDisplayQuark.keySet();
        }
        for (Long id : selectedItems) {
            Integer quark = fIDToDisplayQuark.get(id);
            if (quark != null) {
                quarkToRow.put(quark, new TimeGraphRowModel(id, new ArrayList<>()));
            }
        }

        // This regex map automatically filters or highlights the entry
        // according to the global filter entered by the user
        Map<@NonNull Integer, @NonNull Predicate<@NonNull Multimap<@NonNull String, @NonNull Object>>> predicates = new HashMap<>();
        Multimap<@NonNull Integer, @NonNull String> regexesMap = DataProviderParameterUtils.extractRegexFilter(fetchParameters);
        if (regexesMap != null) {
            predicates.putAll(computeRegexPredicate(regexesMap));
        }

        // Query the state system to fill the states
        long currentEndTime = ss.getCurrentEndTime();
        for (ITmfStateInterval interval : ss.query2D(quarkToRow.keySet(), getTimes(ss, DataProviderParameterUtils.extractTimeRequested(fetchParameters)))) {
            if (monitor != null && monitor.isCanceled()) {
                return Collections.emptyList();
            }
            ITimeGraphRowModel row = quarkToRow.get(interval.getAttribute());
            if (row != null) {
                List<@NonNull ITimeGraphState> states = row.getStates();
                ITimeGraphState timeGraphState = getStateFromInterval(interval, currentEndTime);
                // This call will compare the state with the filter predicate
                applyFilterAndAddState(states, timeGraphState, row.getEntryID(), predicates, monitor);
            }
        }
        for (ITimeGraphRowModel model : quarkToRow.values()) {
            model.getStates().sort(Comparator.comparingLong(ITimeGraphState::getStartTime));
        }

        return new ArrayList<>(quarkToRow.values());
    }

    private static TimeGraphState getStateFromInterval(ITmfStateInterval statusInterval, long currentEndTime) {
        long time = statusInterval.getStartTime();
        long duration = Math.min(currentEndTime, statusInterval.getEndTime() + 1) - time;
        Object o = statusInterval.getValue();
        if (!(o instanceof Long)) {
            // Add a null state
            return new TimeGraphState(time, duration, Integer.MIN_VALUE);
        }
        String styleName = "style" + ((Long) o) % 3; //$NON-NLS-1$
        return new TimeGraphState(time, duration, String.valueOf(o), STYLE_MAP.get(styleName));
    }

    private static Set<Long> getTimes(ITmfStateSystem key, @Nullable List<Long> list) {
        if (list == null) {
            return Collections.emptySet();
        }
        Set<@NonNull Long> times = new HashSet<>();
        for (long t : list) {
            if (key.getStartTime() <= t && t <= key.getCurrentEndTime()) {
                times.add(t);
            }
        }
        return times;
    }

    @Override
    public @NonNull TmfModelResponse<List<ITimeGraphArrow>> fetchArrows(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        /**
         * If there were arrows to be drawn, this is where they would be defined
         */
        return new TmfModelResponse<>(null, Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    @Override
    public @NonNull TmfModelResponse<Map<String, String>> fetchTooltip(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        /**
         * If there were tooltips to be drawn, this is where they would be
         * defined
         */
        return new TmfModelResponse<>(null, Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    @Override
    public TmfModelResponse<OutputStyleModel> fetchStyle(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        return new TmfModelResponse<>(new OutputStyleModel(STATE_MAP), Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

}
