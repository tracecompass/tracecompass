/*******************************************************************************
 * Copyright (c) 2012, 2016 Ericsson, École Polytechnique de Montréal and others.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Geneviève Bastien - Move code to provide base classes for time graph view
 *   Christian Mansky - Add check active / uncheck inactive buttons
 *   Mahdi Zolnouri & Samuel Gagnon - Add flat / hierarchical button
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelTidAspect;
import org.eclipse.tracecompass.common.core.StreamUtils.StreamFlattener;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Activator;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Messages;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.actions.FollowThreadAction;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.util.Pair;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractStateSystemTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ILinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeLinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils.Resolution;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils.TimeFormat;

import com.google.common.collect.ImmutableList;

/**
 * The Control Flow view main object
 *
 */
public class ControlFlowView extends AbstractStateSystemTimeGraphView {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * View ID.
     */
    public static final @NonNull String ID = "org.eclipse.tracecompass.analysis.os.linux.views.controlflow"; //$NON-NLS-1$

    private static final String ICONS_PATH = "icons/"; //$NON-NLS-1$
    private static final String OPTIMIZE_ICON = ICONS_PATH + "elcl16/Optimization.png"; //$NON-NLS-1$

    private static final String PROCESS_COLUMN = Messages.ControlFlowView_processColumn;
    private static final String TID_COLUMN = Messages.ControlFlowView_tidColumn;
    private static final String PTID_COLUMN = Messages.ControlFlowView_ptidColumn;
    private static final String BIRTH_TIME_COLUMN = Messages.ControlFlowView_birthTimeColumn;
    private static final String INVISIBLE_COLUMN = Messages.ControlFlowView_invisibleColumn;
    private Action fOptimizationAction;

    private static final String NEXT_EVENT_ICON_PATH = "icons/elcl16/shift_r_edit.gif"; //$NON-NLS-1$
    private static final String PREV_EVENT_ICON_PATH = "icons/elcl16/shift_l_edit.gif"; //$NON-NLS-1$

    private static final String[] COLUMN_NAMES = new String[] {
            PROCESS_COLUMN,
            TID_COLUMN,
            PTID_COLUMN,
            BIRTH_TIME_COLUMN
    };

    private static final String[] FILTER_COLUMN_NAMES = new String[] {
            PROCESS_COLUMN,
            TID_COLUMN
    };

    // Timeout between updates in the build thread in ms
    private static final long BUILD_UPDATE_TIMEOUT = 500;

    private static final Comparator<ITimeGraphEntry>[] COLUMN_COMPARATORS;

    private final Function<Collection<ILinkEvent>, Map<Integer, Long>> UPDATE_SCHEDULING_COLUMN_ALGO = new NaiveOptimizationAlgorithm();

    private static final int INITIAL_SORT_COLUMN_INDEX = 3;

    static {
        ImmutableList.Builder<Comparator<ITimeGraphEntry>> builder = ImmutableList.builder();
        builder.add(ControlFlowColumnComparators.PROCESS_NAME_COLUMN_COMPARATOR)
            .add(ControlFlowColumnComparators.TID_COLUMN_COMPARATOR)
            .add(ControlFlowColumnComparators.PTID_COLUMN_COMPARATOR)
            .add(ControlFlowColumnComparators.BIRTH_TIME_COLUMN_COMPARATOR);
        List<Comparator<ITimeGraphEntry>> l = builder.build();
        COLUMN_COMPARATORS = l.toArray(new Comparator[l.size()]);
    }

    /**
     * Mutex rule for search action jobs, making sure they execute sequentially
     */
    private final ISchedulingRule fSearchActionMutexRule = new ISchedulingRule() {
        @Override
        public boolean isConflicting(ISchedulingRule rule) {
            return (rule == this);
        }

        @Override
        public boolean contains(ISchedulingRule rule) {
            return (rule == this);
        }
    };

    private final Set<ITmfTrace> fFlatTraces = new HashSet<>();

    private IAction fFlatAction;

    private IAction fHierarchicalAction;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public ControlFlowView() {
        super(ID, new ControlFlowPresentationProvider());
        setTreeColumns(COLUMN_NAMES, COLUMN_COMPARATORS, INITIAL_SORT_COLUMN_INDEX);
        setTreeLabelProvider(new ControlFlowTreeLabelProvider());
        setFilterColumns(FILTER_COLUMN_NAMES);
        setFilterLabelProvider(new ControlFlowFilterLabelProvider());
        setEntryComparator(ControlFlowColumnComparators.BIRTH_TIME_COLUMN_COMPARATOR);
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        // add "Check active" Button to TimeGraphFilterDialog
        super.getTimeGraphCombo().addTimeGraphFilterCheckActiveButton(
                new ControlFlowCheckActiveProvider(Messages.ControlFlowView_checkActiveLabel, Messages.ControlFlowView_checkActiveToolTip));
        // add "Uncheck inactive" Button to TimeGraphFilterDialog
        super.getTimeGraphCombo().addTimeGraphFilterUncheckInactiveButton(
                new ControlFlowCheckActiveProvider(Messages.ControlFlowView_uncheckInactiveLabel, Messages.ControlFlowView_uncheckInactiveToolTip));
    }

    /**
     * @since 2.0
     */
    @Override
    protected void fillTimeGraphEntryContextMenu(@NonNull IMenuManager menuManager) {
        ISelection selection = getSite().getSelectionProvider().getSelection();
        if (selection instanceof StructuredSelection) {
            StructuredSelection sSel = (StructuredSelection) selection;
            if (sSel.getFirstElement() instanceof ControlFlowEntry) {
                ControlFlowEntry entry = (ControlFlowEntry) sSel.getFirstElement();
                menuManager.add(new FollowThreadAction(ControlFlowView.this, entry.getName(), entry.getThreadId(), entry.getTrace()));
            }
        }
    }

    @Override
    protected void fillLocalToolBar(IToolBarManager manager) {
        // add "Optimization" Button to local tool bar of Controlflow
        IAction optimizationAction = getOptimizationAction();
        manager.add(optimizationAction);

        // add a separator to local tool bar
        manager.add(new Separator());

        super.fillLocalToolBar(manager);
        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(getClass().getName());
        if (section == null) {
            section = settings.addNewSection(getClass().getName());
        }

        IAction hideArrowsAction = getTimeGraphCombo().getTimeGraphViewer().getHideArrowsAction(section);
        manager.add(hideArrowsAction);

        IAction followArrowBwdAction = getTimeGraphCombo().getTimeGraphViewer().getFollowArrowBwdAction();
        followArrowBwdAction.setText(Messages.ControlFlowView_followCPUBwdText);
        followArrowBwdAction.setToolTipText(Messages.ControlFlowView_followCPUBwdText);
        manager.add(followArrowBwdAction);

        IAction followArrowFwdAction = getTimeGraphCombo().getTimeGraphViewer().getFollowArrowFwdAction();
        followArrowFwdAction.setText(Messages.ControlFlowView_followCPUFwdText);
        followArrowFwdAction.setToolTipText(Messages.ControlFlowView_followCPUFwdText);
        manager.add(followArrowFwdAction);

        IAction previousEventAction = new SearchEventAction(false, PackageMessages.ControlFlowView_PreviousEventJobName);
        previousEventAction.setText(PackageMessages.ControlFlowView_PreviousEventActionName);
        previousEventAction.setToolTipText(PackageMessages.ControlFlowView_PreviousEventActionTooltip);
        previousEventAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(PREV_EVENT_ICON_PATH));
        manager.add(previousEventAction);

        IAction nextEventAction = new SearchEventAction(true, PackageMessages.ControlFlowView_NextEventJobName);
        nextEventAction.setText(PackageMessages.ControlFlowView_NextEventActionName);
        nextEventAction.setToolTipText(PackageMessages.ControlFlowView_NextEventActionTooltip);
        nextEventAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(NEXT_EVENT_ICON_PATH));
        manager.add(nextEventAction);
    }

    private IAction getOptimizationAction() {
        if (fOptimizationAction == null) {
            fOptimizationAction = new OptimizationAction();
            fOptimizationAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(OPTIMIZE_ICON));
            fOptimizationAction.setText(Messages.ControlFlowView_optimizeLabel);
            fOptimizationAction.setToolTipText(Messages.ControlFlowView_optimizeToolTip);
        }
        return fOptimizationAction;
    }

    @Override
    protected void fillLocalMenu(IMenuManager manager) {
        super.fillLocalMenu(manager);
        final MenuManager item = new MenuManager(Messages.ControlFlowView_threadPresentation);
        fFlatAction = createFlatAction();
        item.add(fFlatAction);

        fHierarchicalAction = createHierarchicalAction();
        item.add(fHierarchicalAction);
        manager.add(item);

    }

    /**
     * Base Action for the "Go to Next/Previous Event for thread" actions
     */
    private class SearchEventAction extends Action {

        private final boolean ifDirection;
        private final String ifJobName;

        /**
         * Constructor
         *
         * @param direction
         *            The direction of the search, "true" for forwards and
         *            "false" for backwards.
         * @param jobName
         *            The name of the job that will be spawned
         */
        public SearchEventAction(boolean direction, String jobName) {
            ifDirection = direction;
            ifJobName = jobName;
        }

        @Override
        public void run() {
            Job job = new Job(ifJobName) {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    TimeGraphControl ctrl = getTimeGraphViewer().getTimeGraphControl();
                    ITimeGraphEntry traceEntry = ctrl.getSelectedTrace();

                    long ts = getTimeGraphViewer().getSelectionBegin();
                    ITimeEvent selectedState = Utils.findEvent(traceEntry, ts, 0);

                    if (selectedState == null) {
                        /* No selection currently in the view, do nothing */
                        return Status.OK_STATUS;
                    }
                    ITimeGraphEntry entry = selectedState.getEntry();
                    if (!(entry instanceof ControlFlowEntry)) {
                        return Status.OK_STATUS;
                    }
                    ControlFlowEntry cfEntry = (ControlFlowEntry) entry;
                    int tid = cfEntry.getThreadId();

                    ITmfTrace trace = cfEntry.getTrace();
                    ITmfContext ctx = trace.seekEvent(TmfTimestamp.fromNanos(ts));
                    long rank = ctx.getRank();
                    ctx.dispose();

                    Predicate<@NonNull ITmfEvent> predicate = event -> {
                        /*
                         * TODO Specific to the Control Flow View and kernel
                         * traces for now. Could be eventually generalized to
                         * anything represented by the time graph row.
                         */
                        Integer eventTid = KernelTidAspect.INSTANCE.resolve(event);
                        return (eventTid != null && eventTid.intValue() == tid);
                    };

                    ITmfEvent event = (ifDirection ?
                            TmfTraceUtils.getNextEventMatching(cfEntry.getTrace(), rank, predicate, monitor) :
                            TmfTraceUtils.getPreviousEventMatching(cfEntry.getTrace(), rank, predicate, monitor));
                    if (event != null) {
                        TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(this, event.getTimestamp()));
                    }
                    return Status.OK_STATUS;

                }
            };
            /*
             * Make subsequent jobs not run concurrently, but wait after one
             * another.
             */
            job.setRule(fSearchActionMutexRule);
            job.schedule();
        }
    }

    private IAction createHierarchicalAction() {
        IAction action = new Action(Messages.ControlFlowView_hierarchicalViewLabel, IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                ITmfTrace parentTrace = getTrace();
                synchronized (fFlatTraces) {
                    fFlatTraces.remove(parentTrace);
                    for (ITmfTrace trace : TmfTraceManager.getTraceSet(parentTrace)) {
                        final ITmfStateSystem ss = TmfStateSystemAnalysisModule.getStateSystem(trace, KernelAnalysisModule.ID);
                        for (TimeGraphEntry traceEntry : getEntryList(ss)) {
                            List<ControlFlowEntry> currentRootList = traceEntry.getChildren().stream()
                                    .filter(e -> e instanceof ControlFlowEntry)
                                    .map(e -> (ControlFlowEntry) e)
                                    .collect(Collectors.toList());
                            addEntriesToHierarchicalTree(currentRootList, traceEntry);
                        }
                    }
                }
                refresh();
            }
        };
        action.setChecked(true);
        action.setToolTipText(Messages.ControlFlowView_hierarchicalViewToolTip);
        return action;
    }

    private IAction createFlatAction() {
        IAction action = new Action(Messages.ControlFlowView_flatViewLabel, IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                ITmfTrace parentTrace = getTrace();
                synchronized (fFlatTraces) {
                    fFlatTraces.add(parentTrace);
                    for (ITmfTrace trace : TmfTraceManager.getTraceSet(parentTrace)) {
                        final ITmfStateSystem ss = TmfStateSystemAnalysisModule.getStateSystem(trace, KernelAnalysisModule.ID);
                        List<@NonNull TimeGraphEntry> entryList = getEntryList(ss);
                        if (entryList != null) {
                            for (TimeGraphEntry traceEntry : entryList) {
                                hierarchicalToFlatTree(traceEntry);
                            }
                        }
                    }
                }
                refresh();
            }
        };
        action.setToolTipText(Messages.ControlFlowView_flatViewToolTip);
        return action;
    }

    @Override
    protected String getNextText() {
        return Messages.ControlFlowView_nextProcessActionNameText;
    }

    @Override
    protected String getNextTooltip() {
        return Messages.ControlFlowView_nextProcessActionToolTipText;
    }

    @Override
    protected String getPrevText() {
        return Messages.ControlFlowView_previousProcessActionNameText;
    }

    @Override
    protected String getPrevTooltip() {
        return Messages.ControlFlowView_previousProcessActionToolTipText;
    }

    /**
     * Get the optimization function for the scheduling column. In the base
     * implementation, this optimizes by Line arrows, but can be overidden.
     * <p>
     * It takes a collection of link events, looking at the entries being
     * linked, and returns a list of the proposed order. The list of indexes
     * should be in ascending order. There can be duplicates, but the values and
     * order should always be the same for the same input.
     *
     * @return the returned column order, where the integer is the tid of the
     *         entry, and the return value is the position, there can be
     *         duplicates.
     */
    public Function<Collection<ILinkEvent>, Map<Integer, Long>> getUpdatedSchedulingColumn() {
        return UPDATE_SCHEDULING_COLUMN_ALGO;
    }

    /**
     * This is an optimization action used to find cliques of entries due to
     * links and put them closer together
     *
     * @author Samuel Gagnon
     */
    private final class OptimizationAction extends Action {

        @Override
        public void runWithEvent(Event event) {
            ITmfTrace trace = getTrace();
            if (trace == null) {
                return;
            }

            createFlatAction().run();

            /*
             * This method only returns the arrows in the current time interval
             * [a,b] of ControlFlowView. Thus, we only optimize for that time
             * interval
             */
            List<ILinkEvent> arrows = getTimeGraphViewer().getTimeGraphControl().getArrows();
            final ITmfStateSystem ss = TmfStateSystemAnalysisModule.getStateSystem(trace, KernelAnalysisModule.ID);
            List<TimeGraphEntry> currentList = getEntryList(ss);

            Map<Integer, Long> orderedTidMap = getUpdatedSchedulingColumn().apply(arrows);

            /*
             * Now that we have our list of ordered tid, it's time to assign a
             * position for each threads in the view. For this, we assign a
             * value to an invisible column and sort according to the values in
             * this column.
             */
            for (TimeGraphEntry entry : currentList) {
                if (entry instanceof TraceEntry) {
                    for (TimeGraphEntry child : ((TraceEntry) entry).getChildren()) {
                        if (child instanceof ControlFlowEntry) {
                            ControlFlowEntry cEntry = (ControlFlowEntry) child;
                            /*
                             * If the thread is in our list, we give it a
                             * position. Otherwise, it means there's no activity
                             * in the current interval for that thread. We set
                             * its position to Long.MAX_VALUE so it goes to the
                             * bottom.
                             */
                            cEntry.setSchedulingPosition(orderedTidMap.getOrDefault(cEntry.getThreadId(), Long.MAX_VALUE));
                        }
                    }
                }
            }

            setEntryComparator(ControlFlowColumnComparators.SCHEDULING_COLUMN_COMPARATOR);
            refresh();
        }

    }

    /**
     * @author gbastien
     *
     */
    protected static class ControlFlowTreeLabelProvider extends TreeLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof TraceEntry) {
                if (columnIndex == 0) {
                    return ((TraceEntry) element).getName();
                }
                return ""; //$NON-NLS-1$
            }
            ControlFlowEntry entry = (ControlFlowEntry) element;

            if (COLUMN_NAMES[columnIndex].equals(Messages.ControlFlowView_processColumn)) {
                return entry.getName();
            } else if (COLUMN_NAMES[columnIndex].equals(Messages.ControlFlowView_tidColumn)) {
                return Integer.toString(entry.getThreadId());
            } else if (COLUMN_NAMES[columnIndex].equals(Messages.ControlFlowView_ptidColumn)) {
                if (entry.getParentThreadId() > 0) {
                    return Integer.toString(entry.getParentThreadId());
                }
            } else if (COLUMN_NAMES[columnIndex].equals(Messages.ControlFlowView_birthTimeColumn)) {
                return Utils.formatTime(entry.getStartTime(), TimeFormat.CALENDAR, Resolution.NANOSEC);
            } else if (COLUMN_NAMES[columnIndex].equals(Messages.ControlFlowView_traceColumn)) {
                return entry.getTrace().getName();
            } else if (COLUMN_NAMES[columnIndex].equals(INVISIBLE_COLUMN)) {
                return Long.toString(entry.getSchedulingPosition());
            }
            return ""; //$NON-NLS-1$
        }

    }

    private static class ControlFlowFilterLabelProvider extends TreeLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof TraceEntry) {
                if (columnIndex == 0) {
                    return ((TraceEntry) element).getName();
                }
                return ""; //$NON-NLS-1$
            }
            ControlFlowEntry entry = (ControlFlowEntry) element;

            if (columnIndex == 0) {
                return entry.getName();
            } else if (columnIndex == 1) {
                return Integer.toString(entry.getThreadId());
            }
            return ""; //$NON-NLS-1$
        }

    }

    private static class TraceEntry extends TimeGraphEntry {

        public TraceEntry(String name, long startTime, long endTime) {
            super(name, startTime, endTime);
        }

        @Override
        public boolean hasTimeEvents() {
            return false;
        }
    }

    @TmfSignalHandler
    @Override
    public void traceClosed(org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal signal) {
        super.traceClosed(signal);
        synchronized (fFlatTraces) {
            fFlatTraces.remove(signal.getTrace());
        }
    }

    @TmfSignalHandler
    @Override
    public void traceSelected(TmfTraceSelectedSignal signal) {
        super.traceSelected(signal);
        synchronized (fFlatTraces) {
            if (fFlatTraces.contains(signal.getTrace())) {
                fHierarchicalAction.setChecked(false);
                fFlatAction.setChecked(true);
            } else {
                fFlatAction.setChecked(false);
                fHierarchicalAction.setChecked(true);
            }
        }
    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    @Override
    protected void buildEntryList(final ITmfTrace trace, final ITmfTrace parentTrace, final IProgressMonitor monitor) {
        final ITmfStateSystem ssq = TmfStateSystemAnalysisModule.getStateSystem(trace, KernelAnalysisModule.ID);
        if (ssq == null) {
            return;
        }

        final List<ControlFlowEntry> entryList = new ArrayList<>();
        /** Map of trace entries */
        Map<ITmfTrace, TraceEntry> traceEntryMap = new HashMap<>();
        /** Map of control flow entries, key is a pair [threadId, cpuId] */
        final Map<Pair<Integer, Integer>, ControlFlowEntry> entryMap = new HashMap<>();

        long start = ssq.getStartTime();
        setStartTime(Math.min(getStartTime(), start));

        boolean complete = false;
        while (!complete) {
            if (monitor.isCanceled()) {
                return;
            }
            complete = ssq.waitUntilBuilt(BUILD_UPDATE_TIMEOUT);
            if (ssq.isCancelled()) {
                return;
            }
            long end = ssq.getCurrentEndTime();
            if (start == end && !complete) { // when complete execute one last time regardless of end time
                continue;
            }

            TraceEntry aTraceEntry = traceEntryMap.get(trace);
            if (aTraceEntry == null) {
                aTraceEntry = new TraceEntry(trace.getName(), start, end + 1);
                traceEntryMap.put(trace, aTraceEntry);
                addToEntryList(parentTrace, ssq, Collections.singletonList(aTraceEntry));
            } else {
                aTraceEntry.updateEndTime(end + 1);
            }
            final TraceEntry traceEntry = aTraceEntry;

            final long resolution = Math.max(1, (end - ssq.getStartTime()) / getDisplayWidth());
            setEndTime(Math.max(getEndTime(), end + 1));
            final List<Integer> threadQuarks = ssq.getQuarks(Attributes.THREADS, "*"); //$NON-NLS-1$
            queryFullStates(ssq, start, end, resolution, monitor, new IQueryHandler() {
                @Override
                public void handle(List<List<ITmfStateInterval>> fullStates, List<ITmfStateInterval> prevFullState) {
                    for (int threadQuark : threadQuarks) {
                        String threadAttributeName = ssq.getAttributeName(threadQuark);

                        Pair<Integer, Integer> entryKey = Attributes.parseThreadAttributeName(threadAttributeName);
                        int threadId = entryKey.getFirst();

                        if (threadId < 0) { // ignore the 'unknown' (-1) thread
                            continue;
                        }

                        int execNameQuark = ssq.optQuarkRelative(threadQuark, Attributes.EXEC_NAME);
                        int ppidQuark = ssq.optQuarkRelative(threadQuark, Attributes.PPID);
                        if (execNameQuark == ITmfStateSystem.INVALID_ATTRIBUTE) {
                            /* No information on this thread (yet?), skip it for now */
                            continue;
                        }
                        ITmfStateInterval lastExecNameInterval = prevFullState == null || execNameQuark >= prevFullState.size() ? null : prevFullState.get(execNameQuark);
                        long lastExecNameStartTime = lastExecNameInterval == null ? -1 : lastExecNameInterval.getStartTime();
                        long lastExecNameEndTime = lastExecNameInterval == null ? -1 : lastExecNameInterval.getEndTime() + 1;
                        long lastPpidStartTime = prevFullState == null || ppidQuark >= prevFullState.size() || ppidQuark == ITmfStateSystem.INVALID_ATTRIBUTE ? -1 : prevFullState.get(ppidQuark).getStartTime();
                        for (List<ITmfStateInterval> fullState : fullStates) {
                            if (monitor.isCanceled()) {
                                return;
                            }
                            if (execNameQuark >= fullState.size() || ppidQuark >= fullState.size()) {
                                /* No information on this thread (yet?), skip it for now */
                                continue;
                            }
                            ITmfStateInterval execNameInterval = fullState.get(execNameQuark);
                            ITmfStateInterval ppidInterval = ppidQuark == ITmfStateSystem.INVALID_ATTRIBUTE ? null : fullState.get(ppidQuark);
                            long startTime = execNameInterval.getStartTime();
                            long endTime = execNameInterval.getEndTime() + 1;
                            if (startTime == lastExecNameStartTime && ppidInterval != null && ppidInterval.getStartTime() == lastPpidStartTime) {
                                continue;
                            }
                            boolean isNull = execNameInterval.getStateValue().isNull();
                            if (isNull && lastExecNameEndTime < startTime && lastExecNameEndTime != -1) {
                                /*
                                 * There was a non-null interval in between the
                                 * full states, try to use it.
                                 */
                                try {
                                    execNameInterval = ssq.querySingleState(startTime - 1, execNameQuark);
                                    ppidInterval = ppidQuark == ITmfStateSystem.INVALID_ATTRIBUTE ? null : ssq.querySingleState(startTime - 1, ppidQuark);
                                    startTime = execNameInterval.getStartTime();
                                    endTime = execNameInterval.getEndTime() + 1;
                                } catch (StateSystemDisposedException e) {
                                    /* ignored */
                                }
                            }
                            if (!execNameInterval.getStateValue().isNull() &&
                                    execNameInterval.getStateValue().getType() == ITmfStateValue.Type.STRING) {
                                String execName = execNameInterval.getStateValue().unboxStr();
                                int ppid = ppidInterval == null ? -1 : ppidInterval.getStateValue().unboxInt();
                                ControlFlowEntry entry = entryMap.get(entryKey);
                                if (entry == null) {
                                    entry = new ControlFlowEntry(threadQuark, trace, execName, threadId, ppid, startTime, endTime);
                                    entryList.add(entry);
                                    entryMap.put(entryKey, entry);
                                } else {
                                    /*
                                     * Update the name of the entry to the
                                     * latest execName and the parent thread id
                                     * to the latest ppid.
                                     */
                                    entry.setName(execName);
                                    entry.setParentThreadId(ppid);
                                    entry.updateEndTime(endTime);
                                }
                            }
                            if (isNull) {
                                entryMap.remove(entryKey);
                            }
                            lastExecNameStartTime = startTime;
                            lastExecNameEndTime = endTime;
                            lastPpidStartTime = ppidInterval == null ? -1 : ppidInterval.getStartTime();
                        }
                    }
                    synchronized (fFlatTraces) {
                        if (fFlatTraces.contains(parentTrace)) {
                            addEntriesToFlatTree(entryList, traceEntry);
                        } else {
                            addEntriesToHierarchicalTree(entryList, traceEntry);
                        }
                    }
                }
            });

            queryFullStates(ssq, ssq.getStartTime(), end, resolution, monitor, new IQueryHandler() {
                @Override
                public void handle(@NonNull List<List<ITmfStateInterval>> fullStates, @Nullable List<ITmfStateInterval> prevFullState) {
                    for (final TimeGraphEntry entry : traceEntry.getChildren()) {
                        if (monitor.isCanceled()) {
                            return;
                        }
                        buildStatusEvents(trace, parentTrace, ssq, fullStates, prevFullState, (ControlFlowEntry) entry, monitor, ssq.getStartTime(), end);
                    }
                }
            });

            if (parentTrace.equals(getTrace())) {
                refresh();
            }

            start = end;
        }
    }

    /**
     * Add entries to the traces's child list in a flat fashion (no hierarchy).
     * If one entry has children, we do a depth first search to add each child
     * to the trace's child list and update the parent and child relations.
     */
    private static void hierarchicalToFlatTree(TimeGraphEntry traceEntry) {
        List<@NonNull TimeGraphEntry> rootList = traceEntry.getChildren();
        // We visit the children of every entry to add
        StreamFlattener<TimeGraphEntry> sf = new StreamFlattener<>(entry -> entry.getChildren().stream());
        Stream<TimeGraphEntry> allEntries = rootList.stream().flatMap(entry -> sf.flatten(entry));

        // We add every entry that is missing from the trace's entry list
        List<@NonNull TimeGraphEntry> rootListToAdd = allEntries
                .filter(entry -> !rootList.contains(entry))
                .collect(Collectors.toList());
        rootList.forEach(entry -> {
            entry.clearChildren();
        });
        rootListToAdd.forEach(entry -> {
            traceEntry.addChild(entry);
            entry.clearChildren();
        });
    }

    /**
     * Add entries to the traces's child list in a flat fashion (no hierarchy).
     */
    private static void addEntriesToFlatTree(List<@NonNull ControlFlowEntry> entryList, TimeGraphEntry traceEntry) {
        List<TimeGraphEntry> rootList = traceEntry.getChildren();
        for (ControlFlowEntry entry : entryList) {
            if (!rootList.contains(entry)) {
                traceEntry.addChild(entry);
            }
        }
    }

    /**
     * Add entries to the trace's child list in a hierarchical fashion.
     */
    private static void addEntriesToHierarchicalTree(List<ControlFlowEntry> entryList, TimeGraphEntry traceEntry) {
        List<TimeGraphEntry> rootList = traceEntry.getChildren();

        for (ControlFlowEntry entry : entryList) {
            boolean root = (entry.getParent() == null || entry.getParent() == traceEntry);
            if (root && entry.getParentThreadId() > 0) {
                for (ControlFlowEntry parent : entryList) {
                    /*
                     * Associate the parent entry only if their time overlap. A
                     * child entry may start before its parent, for example at
                     * the beginning of the trace if a parent has not yet
                     * appeared in the state system. We just want to make sure
                     * that the entry didn't start after the parent ended or
                     * ended before the parent started.
                     */
                    if (parent.getThreadId() == entry.getParentThreadId() &&
                            !(entry.getStartTime() > parent.getEndTime() ||
                            entry.getEndTime() < parent.getStartTime())) {
                        parent.addChild(entry);
                        root = false;
                        if (rootList.contains(entry)) {
                            traceEntry.removeChild(entry);
                        }
                        break;
                    }
                }
            }
            if (root && (!rootList.contains(entry))) {
                traceEntry.addChild(entry);
            }
        }
    }

    private void buildStatusEvents(ITmfTrace trace, ITmfTrace parentTrace, ITmfStateSystem ss, @NonNull List<List<ITmfStateInterval>> fullStates,
            @Nullable List<ITmfStateInterval> prevFullState, ControlFlowEntry entry, @NonNull IProgressMonitor monitor, long start, long end) {
        if (start < entry.getEndTime() && end > entry.getStartTime()) {
            List<ITimeEvent> eventList = getEventList(entry, ss, fullStates, prevFullState, monitor);
            if (eventList == null) {
                return;
            }
            /* Start a new event list on first iteration, then append to it */
            if (prevFullState == null) {
                entry.setEventList(eventList);
            } else {
                for (ITimeEvent event : eventList) {
                    entry.addEvent(event);
                }
            }
            if (parentTrace.equals(getTrace())) {
                redraw();
            }
        }
        for (ITimeGraphEntry child : entry.getChildren()) {
            if (monitor.isCanceled()) {
                return;
            }
            buildStatusEvents(trace, parentTrace, ss, fullStates, prevFullState, (ControlFlowEntry) child, monitor, start, end);
        }
    }

    @Override
    protected @Nullable List<ITimeEvent> getEventList(@NonNull TimeGraphEntry tgentry, ITmfStateSystem ss,
            @NonNull List<List<ITmfStateInterval>> fullStates, @Nullable List<ITmfStateInterval> prevFullState, @NonNull IProgressMonitor monitor) {
        List<ITimeEvent> eventList = null;
        if (!(tgentry instanceof ControlFlowEntry)) {
            return eventList;
        }
        ControlFlowEntry entry = (ControlFlowEntry) tgentry;
        try {
            int statusQuark = entry.getThreadQuark();
            eventList = new ArrayList<>(fullStates.size());
            ITmfStateInterval lastInterval = prevFullState == null || statusQuark >= prevFullState.size() ? null : prevFullState.get(statusQuark);
            long lastStartTime = lastInterval == null ? -1 : lastInterval.getStartTime();
            long lastEndTime = lastInterval == null ? -1 : lastInterval.getEndTime() + 1;
            for (List<ITmfStateInterval> fullState : fullStates) {
                if (monitor.isCanceled()) {
                    return null;
                }
                if (statusQuark >= fullState.size()) {
                    /* No information on this thread (yet?), skip it for now */
                    continue;
                }
                ITmfStateInterval statusInterval = fullState.get(statusQuark);
                long time = statusInterval.getStartTime();
                if (time == lastStartTime) {
                    continue;
                }
                long duration = statusInterval.getEndTime() - time + 1;
                int status = -1;
                try {
                    status = statusInterval.getStateValue().unboxInt();
                } catch (StateValueTypeException e) {
                    Activator.getDefault().logError(e.getMessage());
                }
                if (lastEndTime != time && lastEndTime != -1) {
                    eventList.add(new TimeEvent(entry, lastEndTime, time - lastEndTime));
                }
                if (!statusInterval.getStateValue().isNull()) {
                    eventList.add(new TimeEvent(entry, time, duration, status));
                } else {
                    eventList.add(new NullTimeEvent(entry, time, duration));
                }
                lastStartTime = time;
                lastEndTime = time + duration;
            }
        } catch (TimeRangeException e) {
            Activator.getDefault().logError(e.getMessage());
        }
        return eventList;
    }

    /**
     * Returns a value corresponding to the selected entry.
     *
     * Used in conjunction with synchingToTime to change the selected entry. If
     * one of these methods is overridden in child class, then both should be.
     *
     * @param time
     *            The currently selected time
     * @return a value identifying the entry
     */
    private int getSelectionValue(long time) {
        int thread = -1;
        for (ITmfTrace trace : TmfTraceManager.getTraceSet(getTrace())) {
            if (thread > 0) {
                break;
            }
            ITmfStateSystem ssq = TmfStateSystemAnalysisModule.getStateSystem(trace, KernelAnalysisModule.ID);
            if (ssq == null) {
                continue;
            }
            if (time >= ssq.getStartTime() && time <= ssq.getCurrentEndTime()) {
                List<Integer> currentThreadQuarks = ssq.getQuarks(Attributes.CPUS, "*", Attributes.CURRENT_THREAD); //$NON-NLS-1$
                for (int currentThreadQuark : currentThreadQuarks) {
                    try {
                        ITmfStateInterval currentThreadInterval = ssq.querySingleState(time, currentThreadQuark);
                        int currentThread = currentThreadInterval.getStateValue().unboxInt();
                        if (currentThread > 0) {
                            int statusQuark = ssq.getQuarkAbsolute(Attributes.THREADS, Integer.toString(currentThread));
                            ITmfStateInterval statusInterval = ssq.querySingleState(time, statusQuark);
                            if (statusInterval.getStartTime() == time) {
                                thread = currentThread;
                                break;
                            }
                        }
                    } catch (AttributeNotFoundException | TimeRangeException | StateValueTypeException e) {
                        Activator.getDefault().logError(e.getMessage());
                    } catch (StateSystemDisposedException e) {
                        /* Ignored */
                    }
                }
            }
        }
        return thread;
    }

    @Override
    protected void synchingToTime(long time) {
        int selected = getSelectionValue(time);
        if (selected > 0) {
            for (Object element : getTimeGraphViewer().getExpandedElements()) {
                if (element instanceof ControlFlowEntry) {
                    ControlFlowEntry entry = (ControlFlowEntry) element;
                    if (entry.getThreadId() == selected) {
                        getTimeGraphCombo().setSelection(entry);
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected @NonNull List<ILinkEvent> getLinkList(ITmfStateSystem ss,
            @NonNull List<List<ITmfStateInterval>> fullStates, @Nullable List<ITmfStateInterval> prevFullState, @NonNull IProgressMonitor monitor) {
        List<ILinkEvent> list = new ArrayList<>();
        List<TimeGraphEntry> entryList = getEntryList(ss);
        if (entryList == null) {
            return list;
        }
        for (ITmfTrace trace : TmfTraceManager.getTraceSet(getTrace())) {
            List<Integer> currentThreadQuarks = ss.getQuarks(Attributes.CPUS, "*", Attributes.CURRENT_THREAD); //$NON-NLS-1$
            for (int currentThreadQuark : currentThreadQuarks) {
                if (currentThreadQuark >= fullStates.get(0).size()) {
                    /* No information on this cpu (yet?), skip it for now */
                    continue;
                }
                List<ITmfStateInterval> currentThreadIntervals = new ArrayList<>(fullStates.size() + 2);
                try {
                    /*
                     * Add the previous interval if it is the first query
                     * iteration and the first interval has currentThread=0. Add
                     * the following interval if the last interval has
                     * currentThread=0. These are diagonal arrows crossing the
                     * query iteration range.
                     */
                    if (prevFullState == null) {
                        ITmfStateInterval currentThreadInterval = fullStates.get(0).get(currentThreadQuark);
                        if (currentThreadInterval.getStateValue().unboxInt() == 0) {
                            long start = Math.max(currentThreadInterval.getStartTime() - 1, ss.getStartTime());
                            currentThreadIntervals.add(ss.querySingleState(start, currentThreadQuark));
                        }
                    }
                    for (List<ITmfStateInterval> fullState : fullStates) {
                        currentThreadIntervals.add(fullState.get(currentThreadQuark));
                    }
                    ITmfStateInterval currentThreadInterval = fullStates.get(fullStates.size() - 1).get(currentThreadQuark);
                    if (currentThreadInterval.getStateValue().unboxInt() == 0) {
                        long end = Math.min(currentThreadInterval.getEndTime() + 1, ss.getCurrentEndTime());
                        currentThreadIntervals.add(ss.querySingleState(end, currentThreadQuark));
                    }
                } catch (StateSystemDisposedException e) {
                    /* Ignored */
                    return list;
                }
                int prevThread = 0;
                long prevEnd = 0;
                long lastEnd = 0;
                for (ITmfStateInterval currentThreadInterval : currentThreadIntervals) {
                    if (monitor.isCanceled()) {
                        return list;
                    }
                    if (currentThreadInterval.getEndTime() + 1 == lastEnd) {
                        continue;
                    }
                    long time = currentThreadInterval.getStartTime();
                    if (time != lastEnd) {
                        // don't create links where there are gaps in intervals due to the resolution
                        prevThread = 0;
                        prevEnd = 0;
                    }
                    int thread = currentThreadInterval.getStateValue().unboxInt();
                    if (thread > 0 && prevThread > 0) {
                        ITimeGraphEntry prevEntry = findEntry(entryList, trace, prevThread);
                        ITimeGraphEntry nextEntry = findEntry(entryList, trace, thread);
                        list.add(new TimeLinkEvent(prevEntry, nextEntry, prevEnd, time - prevEnd, 0));
                    }
                    lastEnd = currentThreadInterval.getEndTime() + 1;
                    if (thread != 0) {
                        prevThread = thread;
                        prevEnd = lastEnd;
                    }
                }
            }
        }
        return list;
    }

    private ControlFlowEntry findEntry(List<TimeGraphEntry> entryList, ITmfTrace trace, int threadId) {
        for (TimeGraphEntry entry : entryList) {
            if (entry instanceof ControlFlowEntry) {
                ControlFlowEntry controlFlowEntry = (ControlFlowEntry) entry;
                if (controlFlowEntry.getThreadId() == threadId && controlFlowEntry.getTrace() == trace) {
                    return controlFlowEntry;
                }
            }
            if (entry.hasChildren()) {
                ControlFlowEntry controlFlowEntry = findEntry(entry.getChildren(), trace, threadId);
                if (controlFlowEntry != null) {
                    return controlFlowEntry;
                }
            }
        }
        return null;
    }
}
