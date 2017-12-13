/*******************************************************************************
 * Copyright (c) 2012, 2017 Ericsson, École Polytechnique de Montréal and others.
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

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Objects;
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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelTidAspect;
import org.eclipse.tracecompass.common.core.StreamUtils.StreamFlattener;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Activator;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Messages;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.actions.FollowThreadAction;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.filters.ActiveThreadsFilter;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.filters.DynamicFilterDialog;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.interval.TmfStateInterval;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.util.Pair;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ILinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry.Sampling;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeLinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils.Resolution;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils.TimeFormat;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;
import com.google.common.collect.TreeMultimap;

/**
 * The Control Flow view main object
 */
public class ControlFlowView extends AbstractTimeGraphView {

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

    private static final String WILDCARD = "*"; //$NON-NLS-1$

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

    private static final Function<Collection<ILinkEvent>, Map<Integer, Long>> UPDATE_SCHEDULING_COLUMN_ALGO = new NaiveOptimizationAlgorithm();

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

    /**
     * Cache trace and threadID to a {@link ControlFlowEntry} for faster lookups
     * when building link list
     */
    private final Map<ITmfTrace, TreeMultimap<Integer, ControlFlowEntry>> fEntryCache = new HashMap<>();

    private @NonNull ActiveThreadsFilter fActiveThreadsFilter = new ActiveThreadsFilter(null, false, null);

    private final ActiveThreadsFilterAction fActiveThreadsRapidToggle = new ActiveThreadsFilterAction();

    class ActiveThreadsFilterAction extends Action {
        public ActiveThreadsFilterAction() {
            super(PackageMessages.ControlFlowView_DynamicFiltersActiveThreadToggleLabel, IAction.AS_CHECK_BOX);
            setToolTipText(PackageMessages.ControlFlowView_DynamicFiltersActiveThreadToggleToolTip);
            addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    if (!(event.getNewValue() instanceof Boolean)) {
                        return;
                    }

                    Boolean enabled = (Boolean) event.getNewValue();

                    /* Always remove the previous Active Threads filter */
                    getTimeGraphViewer().removeFilter(fActiveThreadsFilter);

                    if (enabled) {
                        fActiveThreadsFilter.setEnabled(true);
                        getTimeGraphViewer().addFilter(fActiveThreadsFilter);

                        /* Use flat representation */
                        if (fFlatAction != null) {
                            applyFlatPresentation();
                            fFlatAction.setChecked(true);
                            fHierarchicalAction.setChecked(false);
                        }
                    } else {
                        fActiveThreadsFilter.setEnabled(false);
                    }
                    startZoomThread(getTimeGraphViewer().getTime0(), getTimeGraphViewer().getTime1());
                }
            });
        }
    }

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
        getTimeGraphViewer().getShowFilterDialogAction().getFilterDialog().addTimeGraphFilterCheckActiveButton(
                new ControlFlowCheckActiveProvider(Messages.ControlFlowView_checkActiveLabel, Messages.ControlFlowView_checkActiveToolTip));
        // add "Uncheck inactive" Button to TimeGraphFilterDialog
        getTimeGraphViewer().getShowFilterDialogAction().getFilterDialog().addTimeGraphFilterUncheckInactiveButton(
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
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, optimizationAction);

        // add a separator to local tool bar
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, new Separator());

        super.fillLocalToolBar(manager);
        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(getClass().getName());
        if (section == null) {
            section = settings.addNewSection(getClass().getName());
        }

        IAction hideArrowsAction = getTimeGraphViewer().getHideArrowsAction(section);
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, hideArrowsAction);

        IAction followArrowBwdAction = getTimeGraphViewer().getFollowArrowBwdAction();
        followArrowBwdAction.setText(Messages.ControlFlowView_followCPUBwdText);
        followArrowBwdAction.setToolTipText(Messages.ControlFlowView_followCPUBwdText);
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, followArrowBwdAction);

        IAction followArrowFwdAction = getTimeGraphViewer().getFollowArrowFwdAction();
        followArrowFwdAction.setText(Messages.ControlFlowView_followCPUFwdText);
        followArrowFwdAction.setToolTipText(Messages.ControlFlowView_followCPUFwdText);
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, followArrowFwdAction);

        IAction previousEventAction = new SearchEventAction(false, PackageMessages.ControlFlowView_PreviousEventJobName);
        previousEventAction.setText(PackageMessages.ControlFlowView_PreviousEventActionName);
        previousEventAction.setToolTipText(PackageMessages.ControlFlowView_PreviousEventActionTooltip);
        previousEventAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(PREV_EVENT_ICON_PATH));
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, previousEventAction);

        IAction nextEventAction = new SearchEventAction(true, PackageMessages.ControlFlowView_NextEventJobName);
        nextEventAction.setText(PackageMessages.ControlFlowView_NextEventActionName);
        nextEventAction.setToolTipText(PackageMessages.ControlFlowView_NextEventActionTooltip);
        nextEventAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(NEXT_EVENT_ICON_PATH));
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, nextEventAction);
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
        MenuManager item = new MenuManager(Messages.ControlFlowView_threadPresentation);
        fFlatAction = createFlatAction();
        item.add(fFlatAction);

        fHierarchicalAction = createHierarchicalAction();
        item.add(fHierarchicalAction);
        manager.add(item);

        item = new MenuManager(PackageMessages.ControlFlowView_DynamicFiltersMenuLabel);
        item.add(fActiveThreadsRapidToggle);
        item.add(new Separator());

        IAction dynamicFiltersConfigureAction = createDynamicFilterConfigureAction();
        item.add(dynamicFiltersConfigureAction);

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

                    /*
                     * TODO Specific to the Control Flow View and kernel traces for now. Could be
                     * eventually generalized to anything represented by the time graph row.
                     */
                    Predicate<@NonNull ITmfEvent> predicate = event -> Objects.equals(tid, KernelTidAspect.INSTANCE.resolve(event));

                    ITmfEvent event = (ifDirection ?
                            TmfTraceUtils.getNextEventMatching(cfEntry.getTrace(), rank, predicate, monitor) :
                            TmfTraceUtils.getPreviousEventMatching(cfEntry.getTrace(), rank, predicate, monitor));
                    if (event != null) {
                        TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(this, event.getTimestamp(), event.getTimestamp(), getTrace()));
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

    private IAction createDynamicFilterConfigureAction() {
        return new Action(PackageMessages.ControlFlowView_DynamicFiltersConfigureLabel, IAction.AS_PUSH_BUTTON) {
            @Override
            public void run() {
                DynamicFilterDialog dialog = new DynamicFilterDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), fActiveThreadsFilter, getTrace());
                if (dialog.open() == Window.OK) {
                    /* Remove the previous Active Threads filter */
                    checkNotNull(getTimeGraphViewer()).removeFilter(fActiveThreadsFilter);

                    ActiveThreadsFilter newFilter = dialog.getActiveThreadsResult();
                    ActiveThreadsFilter previousFilter = fActiveThreadsFilter;

                    /* Set the filter to the view */
                    fActiveThreadsFilter = newFilter;

                    boolean enabled = fActiveThreadsFilter.isEnabled();
                    if (enabled) {
                        checkNotNull(getTimeGraphViewer()).addFilter(newFilter);
                    }

                    /*
                     * Prevent double refresh from change state of setChecked
                     * and ensure that a refresh is done if the mode of the
                     * filter is changed or options are changed
                     */
                    if (previousFilter.isEnabled() && newFilter.isEnabled()) {
                        boolean changed = !Objects.equals(previousFilter.getCpuRanges(), newFilter.getCpuRanges()) || previousFilter.isCpuRangesBased() != newFilter.isCpuRangesBased();
                        if (changed) {
                            refresh();
                        }
                    } else {
                        fActiveThreadsRapidToggle.setChecked(enabled);
                    }
                }
            }
        };
    }

    private IAction createHierarchicalAction() {
        IAction action = new Action(Messages.ControlFlowView_hierarchicalViewLabel, IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                ITmfTrace parentTrace = getTrace();
                synchronized (fFlatTraces) {
                    fFlatTraces.remove(parentTrace);
                    List<@NonNull TimeGraphEntry> entryList = getEntryList(parentTrace);
                    if (entryList != null) {
                        for (TimeGraphEntry traceEntry : entryList) {
                            addEntriesToHierarchicalTree(Iterables.filter(traceEntry.getChildren(), ControlFlowEntry.class), traceEntry);
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
                applyFlatPresentation();
                refresh();
            }
        };
        action.setChecked(true);
        action.setToolTipText(Messages.ControlFlowView_flatViewToolTip);
        return action;
    }

    private void applyFlatPresentation() {
        ITmfTrace parentTrace = getTrace();
        synchronized (fFlatTraces) {
            fFlatTraces.add(parentTrace);
            List<@NonNull TimeGraphEntry> entryList = getEntryList(parentTrace);
            if (entryList != null) {
                for (TimeGraphEntry traceEntry : entryList) {
                    hierarchicalToFlatTree(traceEntry);
                }
            }
        }
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
            ITmfTrace parentTrace = getTrace();
            if (parentTrace == null) {
                return;
            }

            createFlatAction().run();

            /*
             * This method only returns the arrows in the current time interval
             * [a,b] of ControlFlowView. Thus, we only optimize for that time
             * interval
             */
            List<ILinkEvent> arrows = getTimeGraphViewer().getTimeGraphControl().getArrows();
            List<TimeGraphEntry> currentList = getEntryList(parentTrace);
            if (currentList == null) {
                return;
            }

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
                return FormatTimeUtils.formatTime(entry.getStartTime(), TimeFormat.CALENDAR, Resolution.NANOSEC);
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

        private final @NonNull ITmfTrace fTrace;
        private final @NonNull ITmfStateSystem fStateSystem;

        public TraceEntry(@NonNull ITmfTrace trace, ITmfStateSystem ssq, long endTime) {
            super(trace.getName(), ssq.getStartTime(), endTime);
            fTrace = trace;
            fStateSystem = ssq;
        }

        @Override
        public boolean hasTimeEvents() {
            return false;
        }

        public @NonNull ITmfTrace getTrace() {
            return fTrace;
        }

        public @NonNull ITmfStateSystem getStateSystem() {
            return fStateSystem;
        }
    }

    @TmfSignalHandler
    @Override
    public void traceClosed(TmfTraceClosedSignal signal) {
        super.traceClosed(signal);
        ITmfTrace parentTrace = signal.getTrace();
        synchronized (fFlatTraces) {
            fFlatTraces.remove(parentTrace);
        }
        synchronized (fEntryCache) {
            for (ITmfTrace trace : TmfTraceManager.getTraceSet(parentTrace)) {
                fEntryCache.remove(trace);
            }
        }
    }

    @TmfSignalHandler
    @Override
    public void traceSelected(TmfTraceSelectedSignal signal) {
        super.traceSelected(signal);

        /* Update the Flat and Hierarchical actions */
        synchronized (fFlatTraces) {
            if (fFlatTraces.contains(signal.getTrace())) {
                fHierarchicalAction.setChecked(false);
                fFlatAction.setChecked(true);
            } else {
                fFlatAction.setChecked(false);
                fHierarchicalAction.setChecked(true);
            }
        }

        /* Update the Dynamic Filters related actions */
        ViewerFilter activeThreadFilter = null;
        ViewerFilter[] traceFilters = getFiltersMap().get(signal.getTrace());
        if (traceFilters != null) {
            activeThreadFilter = getActiveThreadsFilter(traceFilters);
        }

        if (activeThreadFilter == null) {
            fActiveThreadsFilter = new ActiveThreadsFilter(null, false, getTrace());
        } else {
            fActiveThreadsFilter = (@NonNull ActiveThreadsFilter) checkNotNull(activeThreadFilter);
        }

        fActiveThreadsRapidToggle.setChecked(fActiveThreadsFilter.isEnabled());
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
        /** Map of control flow entries, key is a pair [threadId, cpuId] */
        final Map<Pair<Integer, Integer>, ControlFlowEntry> entryMap = new HashMap<>();
        final TreeMultimap<Integer, ITmfStateInterval> execNamesPPIDs = TreeMultimap.create(
                Comparator.naturalOrder(),
                Comparator.comparing(ITmfStateInterval::getStartTime));
        final TreeMultimap<Integer, ControlFlowEntry> pidMap = TreeMultimap.create(
                Comparator.naturalOrder(),
                Comparator.comparing(ControlFlowEntry::getStartTime));

        synchronized (fEntryCache) {
            fEntryCache.put(trace, pidMap);
        }

        long start = ssq.getStartTime();
        setStartTime(Long.min(getStartTime(), start));

        TraceEntry traceEntry = new TraceEntry(trace, ssq, ssq.getCurrentEndTime() + 1);
        addToEntryList(parentTrace, Collections.singletonList(traceEntry));

        boolean complete = false;
        while (!complete && !monitor.isCanceled() && !ssq.isCancelled()) {
            complete = ssq.waitUntilBuilt(BUILD_UPDATE_TIMEOUT);
            long end = ssq.getCurrentEndTime();
            /* When complete execute one last time regardless of end time. */
            if (start == end && !complete) {
                continue;
            }
            traceEntry.updateEndTime(end + 1);
            setEndTime(Long.max(getEndTime(), end + 1));

            /* Create a List with the threads' PPID and EXEC_NAME quarks for the 2D query .*/
            List<Integer> quarks = new ArrayList<>(ssq.getQuarks(Attributes.THREADS, WILDCARD, Attributes.EXEC_NAME));
            quarks.addAll(ssq.getQuarks(Attributes.THREADS, WILDCARD, Attributes.PPID));

            long queryStart = Long.max(start, ssq.getStartTime());
            long queryEnd = Long.min(end, ssq.getCurrentEndTime());
            execNamesPPIDs.clear();
            try {
                for (@NonNull ITmfStateInterval interval : ssq.query2D(quarks, queryStart, queryEnd)) {
                    if (monitor.isCanceled()) {
                        return;
                    }
                    execNamesPPIDs.put(interval.getAttribute(), interval);
                }
            } catch (TimeRangeException e) {
                Activator.getDefault().logError("CFV: incorrect query times for buildEntryList", e); //$NON-NLS-1$
                continue;
            } catch (StateSystemDisposedException e) {
                /* State System has been disposed, no need to try again. */
                return;
            }

            for (Integer threadQuark : ssq.getQuarks(Attributes.THREADS, WILDCARD)) {
                String threadAttributeName = ssq.getAttributeName(threadQuark);
                Pair<Integer, Integer> entryKey = Attributes.parseThreadAttributeName(threadAttributeName);
                int threadId = entryKey.getFirst();
                if (threadId < 0) {
                    // ignore the 'unknown' (-1) thread
                    continue;
                }

                int execNameQuark = ssq.optQuarkRelative(threadQuark, Attributes.EXEC_NAME);
                int ppidQuark = ssq.optQuarkRelative(threadQuark, Attributes.PPID);
                Collection<ITmfStateInterval> ppidIterator = execNamesPPIDs.get(ppidQuark);
                for (ITmfStateInterval execNameInterval : execNamesPPIDs.get(execNameQuark)) {
                    if (execNameInterval.getValue() == null) {
                        entryMap.remove(entryKey);
                        continue;
                    }

                    ControlFlowEntry entry = entryMap.get(entryKey);
                    long startTime = execNameInterval.getStartTime();
                    long endTime = execNameInterval.getEndTime() + 1;
                    String execName = String.valueOf(execNameInterval.getValue());
                    int ppid = getPpid((NavigableSet<ITmfStateInterval>) ppidIterator, endTime);

                    if (entry == null) {
                        entry = new ControlFlowEntry(threadQuark, trace, execName, threadId, ppid, startTime, endTime);
                        entryList.add(entry);
                        entryMap.put(entryKey, entry);
                    } else {
                        /*
                         * Update the name of the entry to the latest execName
                         * and the parent thread id to the latest ppid.
                         */
                        entry.setName(execName);
                        entry.setParentThreadId(ppid);
                        entry.updateEndTime(endTime);
                    }
                    synchronized (fEntryCache) {
                        pidMap.put(threadId, entry);
                    }
                }
            }

            synchronized (fFlatTraces) {
                if (fFlatTraces.contains(parentTrace)) {
                    addEntriesToFlatTree(entryList, traceEntry);
                } else {
                    addEntriesToHierarchicalTree(entryList, traceEntry);
                }
            }

            final long resolution = Long.max(1, (end - ssq.getStartTime()) / getDisplayWidth());
            /* Transform is just to change the type. */
            Iterable<TimeGraphEntry> entries = Iterables.transform(entryList, e -> (TimeGraphEntry) e);
            zoomEntries(entries, ssq.getStartTime(), end, resolution, monitor);

            if (parentTrace.equals(getTrace())) {
                refresh();
            }

            start = end;
        }
    }

    /**
     * Find the parent PID for a given time from a thread's sorted PPID intervals.
     *
     * @param ppidIterator
     *            a navigable set sorted by increasing start time
     * @param t
     *            the time stamp at which we want to know the PPID
     * @return the entry's PPID or -1 if we could not find it.
     */
    private static int getPpid(NavigableSet<ITmfStateInterval> ppidIterator, long t) {
        ITmfStateInterval ppidInterval = ppidIterator.lower(new TmfStateInterval(t, t + 1, 0, 0));
        if (ppidInterval != null) {
            Object o = ppidInterval.getValue();
            if (o instanceof Integer) {
                return (Integer) o;
            }
        }
        return -1;
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
        Stream<TimeGraphEntry> allEntries = rootList.stream().flatMap(sf::flatten);

        // We add every entry that is missing from the trace's entry list
        List<@NonNull TimeGraphEntry> rootListToAdd = allEntries
                .filter(entry -> !rootList.contains(entry))
                .collect(Collectors.toList());
        rootList.forEach(TimeGraphEntry::clearChildren);
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
    private static void addEntriesToHierarchicalTree(Iterable<ControlFlowEntry> entryList, TimeGraphEntry traceEntry) {
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
                        root = false;
                        if (rootList.contains(entry)) {
                            traceEntry.removeChild(entry);
                        }
                        parent.addChild(entry);
                        break;
                    }
                }
            }
            if (root && (!rootList.contains(entry))) {
                traceEntry.addChild(entry);
            }
        }
    }

    @Override
    protected void zoomEntries(@NonNull Iterable<@NonNull TimeGraphEntry> entries, long zoomStartTime, long zoomEndTime,
            long resolution, @NonNull IProgressMonitor monitor) {
        boolean isZoomThread = Thread.currentThread() instanceof ZoomThread;
        Table<ITmfStateSystem, Integer, ControlFlowEntry> table = filterGroupEntries(entries, zoomStartTime, zoomEndTime);
        TreeMultimap<Integer, ITmfStateInterval> intervals = TreeMultimap.create(Comparator.naturalOrder(),
                Comparator.comparingLong(ITmfStateInterval::getStartTime));

        for (Entry<ITmfStateSystem, Map<Integer, ControlFlowEntry>> ssEntries : table.rowMap().entrySet()) {
            ITmfStateSystem ss = ssEntries.getKey();
            /* Get the time stamps for the 2D query */
            long start = Long.max(zoomStartTime, ss.getStartTime());
            long end = Long.min(zoomEndTime, ss.getCurrentEndTime());
            if (start > end) {
                continue;
            }
            Sampling clampedSampling = new Sampling(start, end, resolution);
            List<Long> times = StateSystemUtils.getTimes(start, end, resolution);
            Map<Integer, ControlFlowEntry> quarksToEntries = ssEntries.getValue();
            /* Do the actual query */
            try {
                for (ITmfStateInterval interval : ss.query2D(quarksToEntries.keySet(), times)) {
                    if (monitor.isCanceled()) {
                        return;
                    }
                    intervals.put(interval.getAttribute(), interval);
                }
                for (Entry<Integer, Collection<ITmfStateInterval>> e : intervals.asMap().entrySet()) {
                    ControlFlowEntry controlFlowEntry = quarksToEntries.get(e.getKey());
                    if (controlFlowEntry == null) {
                        continue;
                    }
                    List<ITimeEvent> events = createTimeEvents(controlFlowEntry, e.getValue());
                    if (monitor.isCanceled()) {
                        return;
                    }
                    if (isZoomThread) {
                        applyResults(() -> {
                            controlFlowEntry.setZoomedEventList(events);
                            controlFlowEntry.setSampling(clampedSampling);
                        });
                    } else {
                        controlFlowEntry.setEventList(events);
                    }
                }
            } catch (TimeRangeException e) {
                Activator.getDefault().logError("CFV: incorrect query times for zoomEvent", e); //$NON-NLS-1$
            } catch (StateSystemDisposedException e) {
                // If the state system was disposed, the trace was closed, nothing to do here.
            } finally {
                intervals.clear();
            }
        }
        fActiveThreadsFilter.updateData(zoomStartTime, zoomEndTime);
    }

    /**
     * Filter the entries to return only ControlFlowEntries which intersect the time
     * range and group them by State System.
     *
     * @param visible
     *            the input list of visible entries
     * @param zoomStartTime
     *            the leftmost time bound of the view
     * @param zoomEndTime
     *            the rightmost time bound of the view
     * @return A Table of the visible entries keyed by their state system and status
     *         interval quark.
     */
    private static Table<ITmfStateSystem, Integer, ControlFlowEntry> filterGroupEntries(Iterable<TimeGraphEntry> visible,
            long zoomStartTime, long zoomEndTime) {
        Table<ITmfStateSystem, Integer, ControlFlowEntry> quarksToEntries = HashBasedTable.create();
        for (ControlFlowEntry entry : Iterables.filter(visible, ControlFlowEntry.class)) {
            if (zoomStartTime <= entry.getEndTime() && zoomEndTime >= entry.getStartTime()) {
                quarksToEntries.put(getStateSystem(entry), entry.getThreadQuark(), entry);
            }
        }
        return quarksToEntries;
    }

    /**
     * Get a {@link ControlFlowEntry}'s {@link ITmfStateSystem} from its
     * {@link TraceEntry} parent.
     *
     * @param controlFlowEntry
     *            {@link ControlFlowEntry}'s who's {@link ITmfStateSystem} we are
     *            looking up
     * @return the controlFlowEntry's state system if the entry has a TraceEntry
     *         parent, else null.
     */
    private static ITmfStateSystem getStateSystem(ITimeGraphEntry controlFlowEntry) {
        ITimeGraphEntry parent = controlFlowEntry.getParent();
        while (parent != null) {
            if (parent instanceof TraceEntry) {
                return ((TraceEntry) parent).getStateSystem();
            }
            parent = parent.getParent();
        }
        return null;
    }

    private static List<ITimeEvent> createTimeEvents(ControlFlowEntry controlFlowEntry, Collection<ITmfStateInterval> value) {
        List<ITimeEvent> events = new ArrayList<>(value.size());
        ITimeEvent prev = null;
        for (ITmfStateInterval interval : value) {
            ITimeEvent event = createTimeEvent(interval, controlFlowEntry);
            if (prev != null) {
                long prevEnd = prev.getTime() + prev.getDuration();
                if (prevEnd < event.getTime()) {
                    // fill in the gap.
                    events.add(new TimeEvent(controlFlowEntry, prevEnd, event.getTime() - prevEnd));
                }
            }
            prev = event;
            events.add(event);
        }
        return events;
    }

    /**
     * Create a {@link TimeEvent} from an {@link ITmfStateInterval} for a
     * {@link ControlFlowEntry}.
     *
     * @param controlFlowEntry
     *            control flow entry which receives the new entry.
     * @param interval
     *            state interval which will generate the new event
     */
    private static TimeEvent createTimeEvent(ITmfStateInterval interval, ControlFlowEntry controlFlowEntry) {
        long startTime = interval.getStartTime();
        long duration = interval.getEndTime() - startTime + 1;
        Object status = interval.getValue();
        if (status instanceof Integer) {
            return new TimeEvent(controlFlowEntry, startTime, duration, (int) status);
        }
        return new NullTimeEvent(controlFlowEntry, startTime, duration);
    }

    @Override
    protected List<@NonNull ILinkEvent> getLinkList(long zoomStartTime, long zoomEndTime, long resolution,
            @NonNull IProgressMonitor monitor) {
        ITmfTrace parentTrace = getTrace();
        if (parentTrace == null) {
            return Collections.emptyList();
        }

        List<TimeGraphEntry> traceEntries = getEntryList(parentTrace);
        if (traceEntries == null) {
            return Collections.emptyList();
        }
        List<@NonNull ILinkEvent> linkList = new ArrayList<>();
        /**
         * MultiMap of the current thread intervals, grouped by CPU, by increasing start
         * time.
         */
        TreeMultimap<Integer, ITmfStateInterval> currentThreadIntervalsMap = TreeMultimap.create(
                Comparator.naturalOrder(),
                Comparator.comparing(ITmfStateInterval::getStartTime));
        for (TimeGraphEntry entry : traceEntries) {
            TraceEntry traceEntry = (TraceEntry) entry;
            ITmfStateSystem ss = traceEntry.getStateSystem();
            /* Get the time stamps for the 2D query */
            long start = Long.max(zoomStartTime, ss.getStartTime());
            long end = Long.min(zoomEndTime, ss.getCurrentEndTime());
            if (start > end) {
                continue;
            }
            List<Integer> quarks = ss.getQuarks(Attributes.CPUS, WILDCARD, Attributes.CURRENT_THREAD);
            List<Long> times = StateSystemUtils.getTimes(start, end, resolution);
            try {
                /* Do the actual query */
                for (ITmfStateInterval interval : ss.query2D(quarks, times)) {
                    if (monitor.isCanceled()) {
                        return linkList;
                    }
                    currentThreadIntervalsMap.put(interval.getAttribute(), interval);
                }

                /* Get the arrows. */
                for (Collection<ITmfStateInterval> currentThreadIntervals : currentThreadIntervalsMap.asMap().values()) {
                    if (monitor.isCanceled()) {
                        return linkList;
                    }
                    linkList.addAll(createCpuArrows(traceEntry, (NavigableSet<ITmfStateInterval>) currentThreadIntervals));
                }
            } catch (TimeRangeException e) {
                Activator.getDefault().logError("CFV: incorrect query times for getLinkList", e); //$NON-NLS-1$
            } catch (StateSystemDisposedException e) {
                // If the state system was disposed, the trace was closed, nothing to do here.
            } finally {
                currentThreadIntervalsMap.clear();
            }
        }
        return linkList;
    }

    /**
     * Create the list of arrows to follow the current thread on a CPU
     *
     * @param trace
     *            trace displayed in the view
     * @param entryList
     *            entry list for this trace
     * @param intervals
     *            sorted collection of the current thread intervals for a CPU
     * @return the list of arrows to follow the current thread on a CPU
     * @throws StateSystemDisposedException
     *             If the query is sent after the state system has been disposed
     */
    private List<@NonNull ILinkEvent> createCpuArrows(TraceEntry entry, NavigableSet<ITmfStateInterval> intervals)
            throws StateSystemDisposedException {
        if (intervals.isEmpty()) {
            return Collections.emptyList();
        }
        /*
         * Add the previous interval if it is the first query iteration and the first
         * interval has currentThread=0. Add the following interval if the last interval
         * has currentThread=0. These are diagonal arrows crossing the query iteration
         * range.
         */
        ITmfStateSystem ss = entry.getStateSystem();
        ITmfStateInterval first = intervals.first();
        long start = first.getStartTime() - 1;
        if (start >= ss.getStartTime() && Objects.equals(first.getValue(), 0)) {
            intervals.add(ss.querySingleState(start, first.getAttribute()));
        }
        ITmfStateInterval last = intervals.last();
        long end = last.getEndTime() + 1;
        if (end <= ss.getCurrentEndTime() && Objects.equals(last.getValue(), 0)) {
            intervals.add(ss.querySingleState(end, last.getAttribute()));
        }

        List<@NonNull ILinkEvent> linkList = new ArrayList<>();
        long prevEnd = 0;
        long lastEnd = 0;
        ITimeGraphEntry prevEntry = null;
        for (ITmfStateInterval currentThreadInterval : intervals) {
            long time = currentThreadInterval.getStartTime();
            if (time != lastEnd) {
                /*
                 * Don't create links where there are gaps in intervals due to the resolution
                 */
                prevEntry = null;
                prevEnd = 0;
            }
            Integer tid = (Integer) currentThreadInterval.getValue();
            lastEnd = currentThreadInterval.getEndTime() + 1;
            ITimeGraphEntry nextEntry = null;
            if (tid != null && tid > 0) {
                nextEntry = findEntry(entry.getTrace(), tid, time);
                if (prevEntry != null) {
                    linkList.add(new TimeLinkEvent(prevEntry, nextEntry, prevEnd, time - prevEnd, 0));
                }
                prevEntry = nextEntry;
                prevEnd = lastEnd;
            }
        }
        return linkList;
    }

    private ControlFlowEntry findEntry(@NonNull ITmfTrace trace, int tid, long time) {
        synchronized (fEntryCache) {
            TreeMultimap<Integer, ControlFlowEntry> pidMap = fEntryCache.get(trace);
            if (pidMap == null) {
                return null;
            }
            /*
             * FIXME TreeMultimap values are Navigable Sets sorted by start time, find the
             * values using floor and the relevant anonymous class if ever the iteration
             * below slows down.
             */
            return Iterables.find(pidMap.get(tid), cfe -> cfe.getStartTime() <= time && time <= cfe.getEndTime(), null);
        }
    }

    /**
     * Find the thread which started running on a CPU at the queried time.
     *
     * @param time
     *            The currently selected time
     * @return the threadID or -1 if not found
     */
    private int getCurrentThread(long time) {
        List<@NonNull TimeGraphEntry> entryList = getEntryList(getTrace());
        if (entryList == null) {
            return -1;
        }
        for (TimeGraphEntry entry : entryList) {
            TraceEntry traceEntry = (TraceEntry) entry;
            ITmfStateSystem ssq = traceEntry.getStateSystem();
            if (time < ssq.getStartTime() || time > ssq.getCurrentEndTime()) {
                continue;
            }
            List<Integer> currentThreadQuarks = ssq.getQuarks(Attributes.CPUS, WILDCARD, Attributes.CURRENT_THREAD);
            try {
                for (int currentThreadQuark : currentThreadQuarks) {
                    ITmfStateInterval currentThreadInterval = ssq.querySingleState(time, currentThreadQuark);
                    Integer currentThread = (Integer) currentThreadInterval.getValue();
                    if (currentThread != null && currentThread > 0) {
                        int statusQuark = ssq.getQuarkAbsolute(Attributes.THREADS, Integer.toString(currentThread));
                        ITmfStateInterval statusInterval = ssq.querySingleState(time, statusQuark);
                        if (statusInterval.getStartTime() == time) {
                            return currentThread;
                        }
                    }
                }
            } catch (AttributeNotFoundException e) {
                Activator.getDefault().logError(e.getMessage());
            } catch (StateSystemDisposedException e) {
                /* State System has been disposed, no need to try again. */
            }
        }
        return -1;
    }

    @Override
    protected void synchingToTime(long time) {
        int currentThread = getCurrentThread(time);
        if (currentThread > 0) {
            for (ITimeGraphEntry element : getTimeGraphViewer().getExpandedElements()) {
                if (element instanceof ControlFlowEntry) {
                    ControlFlowEntry entry = (ControlFlowEntry) element;
                    if (entry.getThreadId() == currentThread) {
                        getTimeGraphViewer().setSelection(entry, true);
                        break;
                    }
                }
            }
        }
    }

    private static ActiveThreadsFilter getActiveThreadsFilter(ViewerFilter[] filters) {
        for (ViewerFilter viewerFilter : filters) {
            if ((viewerFilter instanceof ActiveThreadsFilter)) {
                return (ActiveThreadsFilter) viewerFilter;
            }
        }
        return null;
    }
}
