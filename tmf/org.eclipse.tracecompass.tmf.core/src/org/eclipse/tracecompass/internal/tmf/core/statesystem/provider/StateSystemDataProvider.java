/*******************************************************************************
 * Copyright (c) 2019, 2020 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.statesystem.provider;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.core.model.AbstractTmfTraceDataProvider;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphArrow;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.signal.TmfStartAnalysisSignal;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfAnalysisModuleWithStateSystems;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.util.Pair;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

/**
 * Gets the state system structure and data for all analyses. It only shows them
 * it will not run them.
 *
 * @author Benjamin Saint-Cyr
 * @author Florian Wininger
 * @author Alexandre Montplaisir
 * @author Loic Prieur-Drevon
 *
 */
public class StateSystemDataProvider extends AbstractTmfTraceDataProvider implements ITimeGraphDataProvider<@NonNull TimeGraphEntryModel> {

    /**
     * Extension point ID.
     */
    public static final String ID = "org.eclipse.tracecompass.internal.tmf.core.statesystem.provider.StateSystemDataProvider"; //$NON-NLS-1$
    /**
     * Suffix for dataprovider ID
     */
    public static final String SUFFIX = ".dataprovider"; //$NON-NLS-1$

    private static final String HT_EXTENSION = ".ht"; //$NON-NLS-1$

    private static final AtomicLong ENTRY_ID = new AtomicLong();

    private Map<Long, Pair<ITmfStateSystem, Integer>> fIDToDisplayQuark = new HashMap<>();

    // Use to add one event for every state system and module
    private final Map<ITmfStateSystem, Long> fSsToId = new HashMap<>();
    private final List<ModuleEntryModel> fModuleEntryModelList = new ArrayList<>();

    private final Map<ITmfAnalysisModuleWithStateSystems, Boolean> fModulesToStatus = new HashMap<>();

    /*
     * Entry Builder is a table to stash the entries so it won't duplicate the
     * entry when fetchTree is called again. Long represent the parentId. String
     * is the name of the entry. EntryModelBuilder is the builder of the entry.
     */
    private final Table<Long, String, EntryModelBuilder> fEntryBuilder = HashBasedTable.create();

    /**
     * Set of {@link ITmfAnalysisModuleWithStateSystems} that were received by
     * {@link TmfStateSystemExplorer#handleAnalysisStarted(TmfStartAnalysisSignal)}.
     * These are non automatic analysis that the build entry must join on.
     */
    private final Set<ITmfAnalysisModuleWithStateSystems> fStartedAnalysis = Objects.requireNonNull(ConcurrentHashMap.newKeySet());

    /**
     * Constructor
     *
     * @param trace
     *            The trace for which we will be providing the time graph models
     *
     */
    public StateSystemDataProvider(ITmfTrace trace) {
        super(trace);
        for (ITmfAnalysisModuleWithStateSystems module : Objects.requireNonNull(Iterables.filter(trace.getAnalysisModules(), ITmfAnalysisModuleWithStateSystems.class))) {
            fModulesToStatus.put(module, false);
        }
    }

    @Override
    public @NonNull String getId() {
        return ID;
    }

    @Override
    public TmfModelResponse<List<ITimeGraphArrow>> fetchArrows(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        return new TmfModelResponse<>(null, Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    @Override
    public TmfModelResponse<Map<String, String>> fetchTooltip(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        Map<Long, Pair<ITmfStateSystem, Integer>> entries = getSelectedEntries(fetchParameters);
        if (entries.size() != 1) {
            // Not the expected size of tooltip, just return empty
            return new TmfModelResponse<>(null, Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }
        Entry<@NonNull Long, @NonNull Pair<ITmfStateSystem, Integer>> entry = entries.entrySet().iterator().next();
        Map<String, String> tooltip = new HashMap<>();
        Integer quark = Objects.requireNonNull(entry.getValue()).getSecond();
        ITmfStateSystem ss = Objects.requireNonNull(entry.getValue()).getFirst();
        tooltip.put(String.valueOf(Messages.QuarkColumnLabel), Integer.toString(quark));
        tooltip.put(String.valueOf(Messages.AttributePathColumnLabel), ss.getFullAttributePath(quark));
        tooltip.put(String.valueOf(Messages.ValueColumnLabel), getQuarkValue(fetchParameters, ss, quark));

        return new TmfModelResponse<>(tooltip, Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    private Map<Long, Pair<ITmfStateSystem, Integer>> getSelectedEntries(Map<String, Object> fetchParameters) {
        Collection<Long> selectedItems = DataProviderParameterUtils.extractSelectedItems(fetchParameters);
        if (selectedItems == null) {
            return Collections.emptyMap();
        }
        Map<Long, Pair<ITmfStateSystem, Integer>> idToQuark = new HashMap<>();
        synchronized (fEntryBuilder) {
            for (Long id : selectedItems) {
                Pair<ITmfStateSystem, Integer> pair = fIDToDisplayQuark.get(id);
                if (pair != null) {
                    idToQuark.put(id, pair);
                }
            }
        }
        return idToQuark;
    }

    private static String getQuarkValue(Map<String, Object> fetchParameters, ITmfStateSystem ss, Integer quark) {
        String valueString = null;
        try {
            Object actualTimeObj = fetchParameters.get(DataProviderParameterUtils.REQUESTED_TIME_KEY);
            if (actualTimeObj instanceof ArrayList) {
                ArrayList<?> actualList = (ArrayList<?>) actualTimeObj;
                Long actualTime = (Long) actualList.get(0);
                if (actualTime != null) {
                    ITmfStateInterval interval = ss.querySingleState(actualTime, quark);
                    valueString = String.valueOf(interval.getValue());
                }
            }
        } catch (StateSystemDisposedException e) {
            // Return empty string
        }
        return NonNullUtils.nullToEmptyString(valueString);
    }

    private abstract static  class EntryModelBuilder {
        private final long fId;
        private final long fParentId;
        private final String fName;
        private final long fStartTime;
        private long fEndTime;

        public EntryModelBuilder(long id, long parenId, String traceName, long startTime) {
            fId = id;
            fParentId = parenId;
            fName = traceName;
            fStartTime = startTime;
            fEndTime = startTime;
        }

        public long getId() {
            return fId;
        }

        public long getParentId() {
            return fParentId;
        }

        public String getName() {
            return fName;
        }

        public long getStartTime() {
            return fStartTime;
        }

        public long getEndTime() {
            return fEndTime;
        }

        public void setEndTime(long endTime) {
            fEndTime = endTime;
        }

        public abstract <T extends TimeGraphEntryModel> T build();
    }

    /**
     * Extension of {@link TimeGraphEntryModel} to keep a reference to the
     * corresponding {@link ITmfTrace}
     */
    public static class TraceEntryModel extends TimeGraphEntryModel {

        private static final class Builder extends EntryModelBuilder {
            private final ITmfTrace fTrace;

            public Builder(long id, long parentId, String traceName, long startTime, ITmfTrace trace) {
                super(id, parentId, traceName, startTime);
                fTrace = trace;
            }

            @Override
            public TraceEntryModel build() {
                return new TraceEntryModel(getId(), getParentId(), getName(), getStartTime(), getEndTime(), fTrace);
            }
        }

        private final ITmfTrace fEntryTrace;

        /**
         * Constructor
         *
         * @param trace
         *            Trace
         * @param id
         *            The ID of the trace
         * @param parentId
         *            Parent ID
         * @param traceName
         *            The name of the trace
         * @param startTime
         *            The start time
         * @param endTime
         *            The end time
         */
        protected TraceEntryModel(long id, long parentId, String traceName, long startTime, long endTime, ITmfTrace trace) {
            super(id, parentId, traceName, startTime, endTime);
            fEntryTrace = trace;
        }

        /**
         * Return the trace of the entry
         *
         * @return The trace
         */
        public ITmfTrace getTrace() {
            return fEntryTrace;
        }
    }

    /**
     * Extension of {@link TimeGraphEntryModel} to keep a reference to the
     * corresponding {@link ITmfAnalysisModuleWithStateSystems}
     */
    public static class ModuleEntryModel extends TimeGraphEntryModel {

        private static final class Builder extends EntryModelBuilder {
            private final ITmfAnalysisModuleWithStateSystems fModule;

            public Builder(long id, long parentId, String traceName, long startTime, ITmfAnalysisModuleWithStateSystems module) {
                super(id, parentId, traceName, startTime);
                fModule = module;
            }

            @Override
            public ModuleEntryModel build() {
                return new ModuleEntryModel(getId(), getParentId(), getName(), getStartTime(), getEndTime(), fModule);
            }
        }

        private final ITmfAnalysisModuleWithStateSystems fModule;

        /**
         * Constructor
         *
         * @param id
         *            Entry ID
         * @param parentId
         *            Parent ID
         * @param name
         *            Entry name
         * @param startTime
         *            Start time
         * @param endTime
         *            End time
         * @param module
         *            module
         */
        protected ModuleEntryModel(long id, long parentId, String name, long startTime, long endTime, ITmfAnalysisModuleWithStateSystems module) {
            super(id, parentId, name, startTime, endTime);
            fModule = module;
        }

        /**
         * Get the analysis module with stateSystems of this entry
         *
         * @return The module
         */
        public ITmfAnalysisModuleWithStateSystems getModule() {
            return fModule;
        }
    }

    /**
     * Extension of {@link TimeGraphEntryModel} to keep a reference to the
     * corresponding {@link ITmfStateSystem}
     */
    public static class StateSystemEntryModel extends TimeGraphEntryModel {
        private final ITmfStateSystem fSs;

        private static final class Builder extends EntryModelBuilder {
            private final ITmfStateSystem fSs;

            public Builder(long id, long parentId, String traceName, long startTime, long endTime, ITmfStateSystem ss) {
                super(id, parentId, traceName, startTime);
                setEndTime(endTime);
                fSs = ss;
            }

            @Override
            public StateSystemEntryModel build() {
                return new StateSystemEntryModel(getId(), getParentId(), getName(), getStartTime(), getEndTime(), fSs);
            }

        }

        /**
         * Constructor
         *
         * @param id
         *            Entry ID
         * @param parentId
         *            Parent ID
         * @param name
         *            The state system name
         * @param startTime
         *            The state system start time
         * @param endTime
         *            The state system end time
         * @param ss
         *            State System
         */
        protected StateSystemEntryModel(long id, long parentId, String name, long startTime, long endTime, ITmfStateSystem ss) {
            super(id, parentId, name, startTime, endTime);
            fSs = ss;
        }

        /**
         * Get the state sytem of this entry
         *
         * @return The State System
         */
        public ITmfStateSystem getStateSystem() {
            return fSs;
        }
    }

    /**
     * Extension of {@link TimeGraphEntryModel} to keep a reference to the
     * corresponding quark
     */
    public static class AttributeEntryModel extends TimeGraphEntryModel {
        private final int fQuark;

        private static final class Builder extends EntryModelBuilder {

            private final Integer fQuark;

            public Builder(long id, Long parentId, String traceName, long startTime, long endTime, Integer quark) {
                super(id, parentId, traceName, startTime);
                setEndTime(endTime);
                fQuark = quark;
            }

            public Integer getQuark() {
                return fQuark;
            }

            @Override
            public AttributeEntryModel build() {
                return new AttributeEntryModel(getId(), getParentId(), getName(), getStartTime(), getEndTime(), getQuark());
            }
        }

        /**
         * Constructor
         *
         * @param id
         *            Entry ID
         * @param parentId
         *            Entry ID
         * @param name
         *            name of the entry
         * @param start
         *            Start time
         * @param end
         *            End time
         * @param quark
         *            Quark
         */
        protected AttributeEntryModel(long id, long parentId, String name, long start, long end, int quark) {
            super(id, parentId, name, start, end, true);
            fQuark = quark;
        }

        /**
         * Get the quark of this entry
         *
         * @return Quark
         */
        public int getQuark() {
            return fQuark;
        }
    }

    @Override
    public @NonNull TmfModelResponse<TmfTreeModel<TimeGraphEntryModel>> fetchTree(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        // need to create the tree
        boolean fetchTreeIsComplete;
        synchronized (fEntryBuilder) {
            fModuleEntryModelList.clear();
            fetchTreeIsComplete = addTrace(monitor);
            if (monitor != null && monitor.isCanceled()) {
                return new TmfModelResponse<>(null, Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
            }
        }
        List<TimeGraphEntryModel> entryList = buildEntryList(monitor);

        Status status = fetchTreeIsComplete ? Status.COMPLETED : Status.RUNNING;
        String msg = fetchTreeIsComplete ? CommonStatusMessage.COMPLETED : CommonStatusMessage.RUNNING;
        return new TmfModelResponse<>(new TmfTreeModel<>(Collections.emptyList(), entryList), status, msg);
    }

    private boolean addTrace(@Nullable IProgressMonitor monitor) {
        boolean fetchTreeIsComplete = true;
        ITmfTrace trace = getTrace();

        // look if the trace entry already exist
        String traceName = Objects.requireNonNull(trace.getName());
        EntryModelBuilder entry = fEntryBuilder.get(-1L, traceName);
        TraceEntryModel.Builder traceEntry;
        if (entry instanceof TraceEntryModel.Builder) {
            traceEntry = (TraceEntryModel.Builder) entry;
        } else {
            long newId = ENTRY_ID.getAndIncrement();
            traceEntry = new TraceEntryModel.Builder(newId, -1, traceName, trace.getStartTime().toNanos(), trace);
            fEntryBuilder.put(-1L, traceName, traceEntry);
        }

        // add child entry
        long rootId = traceEntry.getId();
        for (Entry<ITmfAnalysisModuleWithStateSystems, Boolean> moduleWithStatus : fModulesToStatus.entrySet()) {
            if (monitor != null && monitor.isCanceled()) {
                return false;
            }
            ITmfAnalysisModuleWithStateSystems module = Objects.requireNonNull(moduleWithStatus.getKey());
            Boolean analysisIsDone = Objects.requireNonNull(moduleWithStatus.getValue());
            if (!analysisIsDone || fStartedAnalysis.contains(module)) {
                // Children entry of the trace are the modules
                fetchTreeIsComplete &= addModule(monitor, module, rootId, trace.getStartTime().toNanos());
                fStartedAnalysis.remove(module);
            }
        }

        // Update end Time
        long traceEnd = traceEntry.getEndTime();
        for (ModuleEntryModel moduleEntryModel : fModuleEntryModelList) {
            if (monitor != null && monitor.isCanceled()) {
                return false;
            }
            traceEnd = Long.max(traceEnd, moduleEntryModel.getEndTime());
        }

        traceEntry.setEndTime(traceEnd);

        return fetchTreeIsComplete;
    }

    private boolean addModule(@Nullable IProgressMonitor monitor, ITmfAnalysisModuleWithStateSystems module, Long parentId, Long startTime) {

        synchronized (fStartedAnalysis) {
            /*
             * Ensure that this is the only job running build entry list.
             */
            if (monitor != null && monitor.isCanceled()) {
                return false;
            }
        }
        waitForInitialization(getTrace(), module);

        // look if the module already exist
        String moduleName = Objects.requireNonNull(module.getName());
        EntryModelBuilder entry = fEntryBuilder.get(parentId, moduleName);
        ModuleEntryModel.Builder moduleEntry;

        if (entry instanceof ModuleEntryModel.Builder) {
            moduleEntry = (ModuleEntryModel.Builder) entry;
        } else {
            long newId = ENTRY_ID.getAndIncrement();
            moduleEntry = new ModuleEntryModel.Builder(newId, parentId, moduleName, startTime, module);
            fEntryBuilder.put(parentId, moduleName, moduleEntry);
        }
        long moduleId = moduleEntry.getId();

        // Add child entry
        boolean fetchTreeIsComplete = true;
        Long moduleEnd = startTime;
        boolean hasChildren = false;
        for (ITmfStateSystem ss : module.getStateSystems()) {
            if (monitor != null && monitor.isCanceled()) {
                return false;
            }

            // Children entry of the modules are state system
            fetchTreeIsComplete &= ss.waitUntilBuilt(0);
            if (!ss.isCancelled()) {
                addStateSystem(monitor, ss, moduleId);
                moduleEnd = Long.max(moduleEnd, ss.getCurrentEndTime());
                hasChildren = true;
            } else {
                /*
                 * Need to delete all the children of this State System because
                 * it might be incomplete
                 */
                EntryModelBuilder ssEntry = fEntryBuilder.get(moduleId, ss.getSSID());
                if (ssEntry != null) {
                    deleteElementFromBuildEntryList(ssEntry.getId());
                }
            }
        }

        // Update the entry
        moduleEntry.setEndTime(moduleEnd);
        fEntryBuilder.put(parentId, moduleName, moduleEntry);
        ModuleEntryModel finalModuleEntry = moduleEntry.build();
        fModuleEntryModelList.add(finalModuleEntry);
        if (fetchTreeIsComplete && hasChildren) {
            // Analysis is complete
            fModulesToStatus.put(module, true);
        }

        return fetchTreeIsComplete;
    }

    private void addStateSystem(@Nullable IProgressMonitor monitor, ITmfStateSystem ss, long parentId) {

        String ssName = ss.getSSID();
        EntryModelBuilder entry = fEntryBuilder.get(parentId, ssName);
        StateSystemEntryModel.Builder stateSystemEntryModel;

        // see if the entry already exist
        Long startTime = ss.getStartTime();
        Long endTime = ss.getCurrentEndTime();
        if (entry instanceof StateSystemEntryModel.Builder) {
            stateSystemEntryModel = (StateSystemEntryModel.Builder) entry;
        } else {
            long id = ENTRY_ID.getAndIncrement();
            stateSystemEntryModel = new StateSystemEntryModel.Builder(id, parentId, ssName, startTime, endTime, ss);
            fEntryBuilder.put(parentId, ssName, stateSystemEntryModel);
        }
        // Update entry
        long ssId = stateSystemEntryModel.getId();
        stateSystemEntryModel.setEndTime(endTime);
        fEntryBuilder.put(parentId, ssName, stateSystemEntryModel);
        fSsToId.put(ss, ssId);

        // Add child entry
        for (Integer attrib : ss.getSubAttributes(ITmfStateSystem.ROOT_ATTRIBUTE, false)) {
            if (monitor != null && monitor.isCanceled()) {
                return;
            }
            // Children of the state system are recursive hierarchy
            addSubAttributes(monitor, ssId, startTime, endTime, ss, attrib);
        }
    }

    private void addSubAttributes(@Nullable IProgressMonitor monitor, Long parentId, Long startTime, Long endTime, ITmfStateSystem ss, Integer attrib) {

        String name = ss.getAttributeName(attrib);
        // see if the entry already exist or not
        EntryModelBuilder entry = fEntryBuilder.get(parentId, name);
        AttributeEntryModel.Builder attributeEntry;
        if (entry instanceof AttributeEntryModel.Builder) {
            attributeEntry = (AttributeEntryModel.Builder) entry;
        } else {
            Long id = ENTRY_ID.getAndIncrement();
            attributeEntry = new AttributeEntryModel.Builder(id, parentId, name, startTime, endTime, attrib);
            fEntryBuilder.put(parentId, name, attributeEntry);
        }
        long id = attributeEntry.getId();

        Pair<ITmfStateSystem, Integer> displayQuark = new Pair<>(ss, attrib);
        fIDToDisplayQuark.put(id, displayQuark);

        // Add child entry
        for (Integer child : ss.getSubAttributes(attrib, false)) {
            if (monitor != null && monitor.isCanceled()) {
                return;
            }
            addSubAttributes(monitor, id, startTime, endTime, ss, child);
        }

        // Update entry
        attributeEntry.setEndTime(endTime);
        fEntryBuilder.put(parentId, name, attributeEntry);
    }

    private List<TimeGraphEntryModel> buildEntryList(@Nullable IProgressMonitor monitor) {
        List<TimeGraphEntryModel> entryList = new ArrayList<>();
        synchronized (fEntryBuilder) {
            for (EntryModelBuilder entry : fEntryBuilder.values()) {
                if (monitor != null && monitor.isCanceled()) {
                    return Collections.emptyList();
                }
                entryList.add(entry.build());
            }
        }
        return entryList;
    }

    private void deleteElementFromBuildEntryList(Long rowID) {
        for (EntryModelBuilder entry : fEntryBuilder.row(rowID).values()) {
            deleteElementFromBuildEntryList(entry.getId());
        }
        fEntryBuilder.row(rowID).clear();
    }

    private void waitForInitialization(ITmfTrace trace, ITmfAnalysisModuleWithStateSystems module) {
        if (module.isAutomatic() || fStartedAnalysis.remove(module)) {
            module.waitForInitialization();
            return;
        }
        /*
         * See if an analysis was already run by searching for its state history
         * tree. Scheduling the analysis will run it, and upon running, it will
         * just open the previous state system. We wait for the state system to
         * be initialized. FIXME if another naming convention is used, this
         * might fail.
         */
        String dir = TmfTraceManager.getSupplementaryFileDir(trace);
        boolean exists = Paths.get(dir, module.getId() + HT_EXTENSION).toFile().exists();
        if (exists) {
            module.schedule();
            module.waitForInitialization();
        }
    }

    @Override
    public @NonNull TmfModelResponse<@NonNull TimeGraphModel> fetchRowModel(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        Table<ITmfStateSystem, Integer, Long> table = HashBasedTable.create();
        // Get the quarks to display
        Collection<Long> selectedItems = DataProviderParameterUtils.extractSelectedItems(fetchParameters);
        synchronized (fEntryBuilder) {
            if (selectedItems == null) {
                // No selected items, take them all
                selectedItems = fIDToDisplayQuark.keySet();
            }
            for (Long id : selectedItems) {
                Pair<ITmfStateSystem, Integer> pair = fIDToDisplayQuark.get(id);
                if (pair != null) {
                    table.put(pair.getFirst(), pair.getSecond(), id);
                }
            }
        }
        List<@NonNull ITimeGraphRowModel> allRows = new ArrayList<>();
        try {
            List<Long> times = DataProviderParameterUtils.extractTimeRequested(fetchParameters);
            for (Entry<ITmfStateSystem, Map<Integer, Long>> ssEntry : table.rowMap().entrySet()) {
                ITmfStateSystem ss = Objects.requireNonNull(ssEntry.getKey());
                List<@NonNull ITimeGraphRowModel> rows = getRowModels(ss, ssEntry.getValue(), times, fetchParameters, monitor);
                if (monitor != null && monitor.isCanceled()) {
                    return new TmfModelResponse<>(null, Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
                }
                synchronized (fEntryBuilder) {
                    // Add the SS
                    Long ssId = fSsToId.get(ss);
                    if (ssId != null && selectedItems.contains(ssId)) {
                        TimeGraphRowModel ssRow = new TimeGraphRowModel(ssId, new ArrayList<>());
                        List<@NonNull ITimeGraphState> states = ssRow.getStates();
                        states.add(new TimeGraphState(ss.getStartTime(), ss.getCurrentEndTime() - ss.getStartTime(), Integer.MAX_VALUE));
                        rows.add(ssRow);
                    }
                }
                allRows.addAll(rows);
            }

            synchronized (fEntryBuilder) {
                for (ModuleEntryModel module : fModuleEntryModelList) {
                    if (selectedItems.contains(module.getId())) {
                        allRows.add(getModuleRowModels(module));
                    }
                }
            }
            if (monitor != null && monitor.isCanceled()) {
                return new TmfModelResponse<>(null, Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
            }
            return new TmfModelResponse<>(new TimeGraphModel(allRows), Status.COMPLETED, CommonStatusMessage.COMPLETED);
        } catch (IndexOutOfBoundsException | TimeRangeException | StateSystemDisposedException e) {
            return new TmfModelResponse<>(null, Status.FAILED, CommonStatusMessage.STATE_SYSTEM_FAILED);
        }
    }

    private static ITimeGraphRowModel getModuleRowModels(ModuleEntryModel module) {
        TimeGraphRowModel moduleRow = new TimeGraphRowModel(module.getId(), new ArrayList<>());
        List<@NonNull ITimeGraphState> states = moduleRow.getStates();
        states.add(new TimeGraphState(module.getStartTime(), module.getEndTime() - module.getStartTime(), Integer.MAX_VALUE));
        return moduleRow;
    }

    private List<@NonNull ITimeGraphRowModel> getRowModels(ITmfStateSystem ss, Map<Integer, Long> idToDisplayQuark,
            @Nullable List<Long> times, Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {
        // Create predicates
        Map<@NonNull Integer, @NonNull Predicate<@NonNull Multimap<@NonNull String, @NonNull Object>>> predicates = new HashMap<>();
        Multimap<@NonNull Integer, @NonNull String> regexesMap = DataProviderParameterUtils.extractRegexFilter(fetchParameters);
        if (regexesMap != null) {
            predicates.putAll(computeRegexPredicate(regexesMap));
        }
        // Create quark to row
        Map<Integer, ITimeGraphRowModel> quarkToRow = new HashMap<>(idToDisplayQuark.size());
        for (Entry<Integer, Long> entry : idToDisplayQuark.entrySet()) {
            quarkToRow.put(entry.getKey(), new TimeGraphRowModel(entry.getValue(), new ArrayList<>()));
        }

        for (ITmfStateInterval interval : ss.query2D(idToDisplayQuark.keySet(), getTimes(ss, times))) {
            if (monitor != null && monitor.isCanceled()) {
                return Collections.emptyList();
            }
            ITimeGraphRowModel row = quarkToRow.get(interval.getAttribute());
            if (row != null) {
                List<@NonNull ITimeGraphState> states = row.getStates();
                ITimeGraphState timeGraphState = getStateFromInterval(interval, ss.getCurrentEndTime());
                applyFilterAndAddState(states, timeGraphState, row.getEntryID(), predicates, monitor);
            }
        }

        // sort every row model so their states can be in chronological order
        for (ITimeGraphRowModel model : quarkToRow.values()) {
            model.getStates().sort(Comparator.comparingLong(ITimeGraphState::getStartTime));
        }
        return new ArrayList<>(quarkToRow.values());
    }

    private static Set<Long> getTimes(ITmfStateSystem key, @Nullable List<Long> timeRequested) {
        if (timeRequested == null) {
            return Collections.emptySet();
        }
        Set<@NonNull Long> times = new HashSet<>();
        for (long t : timeRequested) {
            if (key.getStartTime() <= t && t <= key.getCurrentEndTime()) {
                times.add(t);
            }
        }
        return times;
    }

    private static TimeGraphState getStateFromInterval(ITmfStateInterval statusInterval, long currentEndTime) {
        long time = statusInterval.getStartTime();
        long duration = Math.min(statusInterval.getEndTime(), currentEndTime - 1) + 1 - time;
        Object o = statusInterval.getValue();
        if (o instanceof Integer) {
            return new TimeGraphState(time, duration, ((Integer) o).intValue(), String.valueOf(o));
        } else if (o instanceof Long) {
            long l = (long) o;
            return new TimeGraphState(time, duration, (int) l, "0x" + Long.toHexString(l)); //$NON-NLS-1$
        } else if (o instanceof String) {
            return new TimeGraphState(time, duration, Integer.MIN_VALUE, (String) o);
        } else if (o instanceof Double) {
            return new TimeGraphState(time, duration, ((Double) o).intValue());
        }
        return new TimeGraphState(time, duration, Integer.MIN_VALUE);
    }

    /**
     *
     * Make sure that the module being analyzed will be rebuilt when fetchTree
     * is called.
     *
     * @param module
     *            The module that is being analyzed.
     */
    public void startedAnalysisSignalHandler(ITmfAnalysisModuleWithStateSystems module) {
        synchronized (fStartedAnalysis) {
            fStartedAnalysis.add(module);
        }
    }

}