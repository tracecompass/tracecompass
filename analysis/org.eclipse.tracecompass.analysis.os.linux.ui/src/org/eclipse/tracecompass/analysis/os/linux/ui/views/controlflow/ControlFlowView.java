/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson, École Polytechnique de Montréal and others.
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
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.ui.views.controlflow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.Attributes;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelAnalysisModule;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Activator;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Messages;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractStateSystemTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ILinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeLinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils.Resolution;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils.TimeFormat;

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
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.views.controlflow"; //$NON-NLS-1$

    private static final String PROCESS_COLUMN = Messages.ControlFlowView_processColumn;
    private static final String TID_COLUMN = Messages.ControlFlowView_tidColumn;
    private static final String PTID_COLUMN = Messages.ControlFlowView_ptidColumn;
    private static final String BIRTH_TIME_COLUMN = Messages.ControlFlowView_birthTimeColumn;
    private static final String TRACE_COLUMN = Messages.ControlFlowView_traceColumn;

    private static final String[] COLUMN_NAMES = new String[] {
            PROCESS_COLUMN,
            TID_COLUMN,
            PTID_COLUMN,
            BIRTH_TIME_COLUMN,
            TRACE_COLUMN
    };

    private static final String[] FILTER_COLUMN_NAMES = new String[] {
            PROCESS_COLUMN,
            TID_COLUMN
    };

    // Timeout between updates in the build thread in ms
    private static final long BUILD_UPDATE_TIMEOUT = 500;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public ControlFlowView() {
        super(ID, new ControlFlowPresentationProvider());
        setTreeColumns(COLUMN_NAMES);
        setTreeLabelProvider(new ControlFlowTreeLabelProvider());
        setFilterColumns(FILTER_COLUMN_NAMES);
        setFilterLabelProvider(new ControlFlowFilterLabelProvider());
        setEntryComparator(new ControlFlowEntryComparator());
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

    @Override
    protected void fillLocalToolBar(IToolBarManager manager) {
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

    private static class ControlFlowEntryComparator implements Comparator<ITimeGraphEntry> {

        @Override
        public int compare(ITimeGraphEntry o1, ITimeGraphEntry o2) {

            if (o1.getParent() != null || o2.getParent() != null) {
                /* Sort all child processes according to birth time. */
                return Long.compare(o1.getStartTime(), o2.getStartTime());
            }

            int result = 0;

            if ((o1 instanceof ControlFlowEntry) && (o2 instanceof ControlFlowEntry)) {
                /*
                 * Sort root processes according to their trace's start time,
                 * then by trace name, then by the process thread id.
                 */
                ControlFlowEntry entry1 = (ControlFlowEntry) o1;
                ControlFlowEntry entry2 = (ControlFlowEntry) o2;
                result = entry1.getTrace().getStartTime().compareTo(entry2.getTrace().getStartTime());
                if (result == 0) {
                    result = entry1.getTrace().getName().compareTo(entry2.getTrace().getName());
                }
                if (result == 0) {
                    result = Integer.compare(entry1.getThreadId(), entry2.getThreadId());
                }
            }

            if (result == 0) {
                /* Sort root processes with reused thread id by birth time. */
                result = Long.compare(o1.getStartTime(), o2.getStartTime());
            }

            return result;
        }
    }

    /**
     * @author gbastien
     *
     */
    protected static class ControlFlowTreeLabelProvider extends TreeLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
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
            }
            return ""; //$NON-NLS-1$
        }

    }

    private static class ControlFlowFilterLabelProvider extends TreeLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            ControlFlowEntry entry = (ControlFlowEntry) element;

            if (columnIndex == 0) {
                return entry.getName();
            } else if (columnIndex == 1) {
                return Integer.toString(entry.getThreadId());
            }
            return ""; //$NON-NLS-1$
        }

    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    @Override
    protected void buildEventList(final ITmfTrace trace, final ITmfTrace parentTrace, final IProgressMonitor monitor) {
        final ITmfStateSystem ssq = TmfStateSystemAnalysisModule.getStateSystem(trace, KernelAnalysisModule.ID);
        if (ssq == null) {
            return;
        }

        final List<ControlFlowEntry> entryList = new ArrayList<>();
        final Map<Integer, ControlFlowEntry> entryMap = new HashMap<>();

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
            final long resolution = Math.max(1, (end - ssq.getStartTime()) / getDisplayWidth());
            setEndTime(Math.max(getEndTime(), end + 1));
            final List<Integer> threadQuarks = ssq.getQuarks(Attributes.THREADS, "*"); //$NON-NLS-1$
            final long qStart = start;
            final long qEnd = end;
            queryFullStates(ssq, qStart, qEnd, resolution, monitor, new IQueryHandler() {
                @Override
                public void handle(List<List<ITmfStateInterval>> fullStates, List<ITmfStateInterval> prevFullState) {
                    for (int threadQuark : threadQuarks) {
                        String threadName = ssq.getAttributeName(threadQuark);
                        int threadId = -1;
                        try {
                            threadId = Integer.parseInt(threadName);
                        } catch (NumberFormatException e1) {
                            continue;
                        }
                        if (threadId <= 0) { // ignore the 'unknown' (-1) and swapper (0) threads
                            continue;
                        }

                        int execNameQuark;
                        int ppidQuark;
                        try {
                            execNameQuark = ssq.getQuarkRelative(threadQuark, Attributes.EXEC_NAME);
                            ppidQuark = ssq.getQuarkRelative(threadQuark, Attributes.PPID);
                        } catch (AttributeNotFoundException e) {
                            /* No information on this thread (yet?), skip it for now */
                            continue;
                        }
                        ITmfStateInterval lastExecNameInterval = prevFullState == null || execNameQuark >= prevFullState.size() ? null : prevFullState.get(execNameQuark);
                        long lastExecNameStartTime = lastExecNameInterval == null ? -1 : lastExecNameInterval.getStartTime();
                        long lastExecNameEndTime = lastExecNameInterval == null ? -1 : lastExecNameInterval.getEndTime() + 1;
                        long lastPpidStartTime = prevFullState == null || ppidQuark >= prevFullState.size() ? -1 : prevFullState.get(ppidQuark).getStartTime();
                        for (List<ITmfStateInterval> fullState : fullStates) {
                            if (monitor.isCanceled()) {
                                return;
                            }
                            if (execNameQuark >= fullState.size() || ppidQuark >= fullState.size()) {
                                /* No information on this thread (yet?), skip it for now */
                                continue;
                            }
                            ITmfStateInterval execNameInterval = fullState.get(execNameQuark);
                            ITmfStateInterval ppidInterval = fullState.get(ppidQuark);
                            long startTime = execNameInterval.getStartTime();
                            long endTime = execNameInterval.getEndTime() + 1;
                            if (startTime == lastExecNameStartTime && ppidInterval.getStartTime() == lastPpidStartTime) {
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
                                    ppidInterval = ssq.querySingleState(startTime - 1, ppidQuark);
                                    startTime = execNameInterval.getStartTime();
                                    endTime = execNameInterval.getEndTime() + 1;
                                } catch (AttributeNotFoundException e) {
                                    Activator.getDefault().logError(e.getMessage());
                                } catch (StateSystemDisposedException e) {
                                    /* ignored */
                                }
                            }
                            if (!execNameInterval.getStateValue().isNull() &&
                                    execNameInterval.getStateValue().getType() == ITmfStateValue.Type.STRING) {
                                String execName = execNameInterval.getStateValue().unboxStr();
                                int ppid = ppidInterval.getStateValue().unboxInt();
                                ControlFlowEntry entry = entryMap.get(threadId);
                                if (entry == null) {
                                    entry = new ControlFlowEntry(threadQuark, trace, execName, threadId, ppid, startTime, endTime);
                                    entryList.add(entry);
                                    entryMap.put(threadId, entry);
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
                                entryMap.remove(threadId);
                            }
                            lastExecNameStartTime = startTime;
                            lastExecNameEndTime = endTime;
                            lastPpidStartTime = ppidInterval.getStartTime();
                        }
                    }
                    updateTree(entryList, parentTrace, ssq);

                    for (final TimeGraphEntry entry : getEntryList(ssq)) {
                        if (monitor.isCanceled()) {
                            return;
                        }
                        buildStatusEvents(trace, parentTrace, ssq, fullStates, prevFullState, (ControlFlowEntry) entry, monitor, qStart, qEnd);
                    }
                }
            });

            if (parentTrace.equals(getTrace())) {
                refresh();
            }

            start = end;
        }
    }

    private void updateTree(List<ControlFlowEntry> entryList, ITmfTrace parentTrace, ITmfStateSystem ss) {
        List<TimeGraphEntry> rootListToAdd = new ArrayList<>();
        List<TimeGraphEntry> rootListToRemove = new ArrayList<>();
        List<TimeGraphEntry> rootList = getEntryList(ss);

        for (ControlFlowEntry entry : entryList) {
            boolean root = (entry.getParent() == null);
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
                        if (rootList != null && rootList.contains(entry)) {
                            rootListToRemove.add(entry);
                        }
                        break;
                    }
                }
            }
            if (root && (rootList == null || !rootList.contains(entry))) {
                rootListToAdd.add(entry);
            }
        }

        addToEntryList(parentTrace, ss, rootListToAdd);
        removeFromEntryList(parentTrace, ss, rootListToRemove);
    }

    private void buildStatusEvents(ITmfTrace trace, ITmfTrace parentTrace, ITmfStateSystem ss, @NonNull List<List<ITmfStateInterval>> fullStates,
            @Nullable List<ITmfStateInterval> prevFullState, ControlFlowEntry entry, @NonNull IProgressMonitor monitor, long start, long end) {
        if (start < entry.getEndTime() && end > entry.getStartTime()) {
            List<ITimeEvent> eventList = getEventList(entry, ss, fullStates, prevFullState, monitor);
            if (eventList == null) {
                return;
            }
            for (ITimeEvent event : eventList) {
                entry.addEvent(event);
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
            int threadQuark = entry.getThreadQuark();
            int statusQuark = ss.getQuarkRelative(threadQuark, Attributes.STATUS);
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
        } catch (AttributeNotFoundException | TimeRangeException e) {
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
                            int statusQuark = ssq.getQuarkAbsolute(Attributes.THREADS, Integer.toString(currentThread), Attributes.STATUS);
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
                } catch (AttributeNotFoundException e) {
                    Activator.getDefault().logError(e.getMessage());
                    return list;
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

    private ControlFlowEntry findEntry(List<? extends ITimeGraphEntry> entryList, ITmfTrace trace, int threadId) {
        for (ITimeGraphEntry entry : entryList) {
            if (entry instanceof ControlFlowEntry) {
                ControlFlowEntry controlFlowEntry = (ControlFlowEntry) entry;
                if (controlFlowEntry.getThreadId() == threadId && controlFlowEntry.getTrace() == trace) {
                    return controlFlowEntry;
                } else if (entry.hasChildren()) {
                    controlFlowEntry = findEntry(entry.getChildren(), trace, threadId);
                    if (controlFlowEntry != null) {
                        return controlFlowEntry;
                    }
                }
            }
        }
        return null;
    }
}
