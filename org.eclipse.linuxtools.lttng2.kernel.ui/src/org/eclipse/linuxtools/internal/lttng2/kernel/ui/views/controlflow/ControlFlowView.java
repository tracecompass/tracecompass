/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Geneviève Bastien - Move code to provide base classes for time graph view
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.controlflow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.Attributes;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.Messages;
import org.eclipse.linuxtools.lttng2.kernel.core.analysis.LttngKernelAnalysisModule;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ILinkEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeLinkEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils.Resolution;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils.TimeFormat;

/**
 * The Control Flow view main object
 *
 */
public class ControlFlowView extends AbstractTimeGraphView {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * View ID.
     */
    public static final String ID = "org.eclipse.linuxtools.lttng2.kernel.ui.views.controlflow"; //$NON-NLS-1$

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

            int result = 0;

            if ((o1 instanceof ControlFlowEntry) && (o2 instanceof ControlFlowEntry)) {
                ControlFlowEntry entry1 = (ControlFlowEntry) o1;
                ControlFlowEntry entry2 = (ControlFlowEntry) o2;
                result = entry1.getTrace().getStartTime().compareTo(entry2.getTrace().getStartTime());
                if (result == 0) {
                    result = entry1.getTrace().getName().compareTo(entry2.getTrace().getName());
                }
                if (result == 0) {
                    result = entry1.getThreadId() < entry2.getThreadId() ? -1 : entry1.getThreadId() > entry2.getThreadId() ? 1 : 0;
                }
            }

            if (result == 0) {
                result = o1.getStartTime() < o2.getStartTime() ? -1 : o1.getStartTime() > o2.getStartTime() ? 1 : 0;
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
    protected void buildEventList(final ITmfTrace trace, ITmfTrace parentTrace, IProgressMonitor monitor) {
        LttngKernelAnalysisModule module = trace.getAnalysisModuleOfClass(LttngKernelAnalysisModule.class, LttngKernelAnalysisModule.ID);
        if (module == null) {
            return;
        }
        module.schedule();
        module.waitForInitialization();
        ITmfStateSystem ssq = module.getStateSystem();
        if (ssq == null) {
            return;
        }

        List<ControlFlowEntry> entryList = new ArrayList<>();
        Map<Integer, ControlFlowEntry> entryMap = new HashMap<>();

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
            setEndTime(Math.max(getEndTime(), end + 1));
            List<Integer> threadQuarks = ssq.getQuarks(Attributes.THREADS, "*"); //$NON-NLS-1$
            for (int threadQuark : threadQuarks) {
                if (monitor.isCanceled()) {
                    return;
                }
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
                List<ITmfStateInterval> execNameIntervals;
                try {
                    execNameQuark = ssq.getQuarkRelative(threadQuark, Attributes.EXEC_NAME);
                    execNameIntervals = ssq.queryHistoryRange(execNameQuark, start, end);
                } catch (AttributeNotFoundException e) {
                    /* No information on this thread (yet?), skip it for now */
                    continue;
                } catch (StateSystemDisposedException e) {
                    /* State system is closing down, no point continuing */
                    break;
                }

                for (ITmfStateInterval execNameInterval : execNameIntervals) {
                    if (monitor.isCanceled()) {
                        return;
                    }
                    ControlFlowEntry entry = entryMap.get(threadId);
                    if (!execNameInterval.getStateValue().isNull() &&
                            execNameInterval.getStateValue().getType() == ITmfStateValue.Type.STRING) {
                        String execName = execNameInterval.getStateValue().unboxStr();
                        long startTime = execNameInterval.getStartTime();
                        long endTime = execNameInterval.getEndTime() + 1;
                        if (entry == null) {
                            ITmfStateInterval ppidInterval = null;
                            try {
                                int ppidQuark = ssq.getQuarkRelative(threadQuark, Attributes.PPID);
                                ppidInterval = ssq.querySingleState(startTime, ppidQuark);
                            } catch (AttributeNotFoundException e) {
                                /* No info, keep PPID at -1 */
                            } catch (StateSystemDisposedException e) {
                                /* SS is closing down, time to bail */
                                break;
                            }
                            int ppid = -1;
                            if (!(ppidInterval == null) && !ppidInterval.getStateValue().isNull()) {
                                ppid = ppidInterval.getStateValue().unboxInt();
                            }
                            entry = new ControlFlowEntry(threadQuark, trace, execName, threadId, ppid, startTime, endTime);
                            entryList.add(entry);
                            entryMap.put(threadId, entry);
                        } else {
                            // update the name of the entry to the latest
                            // execName
                            entry.setName(execName);
                            entry.updateEndTime(endTime);
                        }
                    } else {
                        entryMap.remove(threadId);
                    }
                }
            }

            updateTree(entryList, parentTrace);

            if (parentTrace.equals(getTrace())) {
                refresh();
            }

            for (ControlFlowEntry entry : entryList) {
                if (monitor.isCanceled()) {
                    return;
                }
                buildStatusEvents(entry.getTrace(), entry, monitor, start, end);
            }

            start = end;
        }
    }

    private void updateTree(List<ControlFlowEntry> entryList, ITmfTrace parentTrace) {
        List<TimeGraphEntry> rootListToAdd = new ArrayList<>();
        List<TimeGraphEntry> rootListToRemove = new ArrayList<>();
        List<TimeGraphEntry> rootList = getEntryList(parentTrace);

        for (ControlFlowEntry entry : entryList) {
            boolean root = (entry.getParent() == null);
            if (root && entry.getParentThreadId() > 0) {
                for (ControlFlowEntry parent : entryList) {
                    if (parent.getThreadId() == entry.getParentThreadId() &&
                            entry.getStartTime() >= parent.getStartTime() &&
                            entry.getStartTime() <= parent.getEndTime()) {
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

        addToEntryList(parentTrace, rootListToAdd);
        removeFromEntryList(parentTrace, rootListToRemove);
    }

    private void buildStatusEvents(ITmfTrace trace, ControlFlowEntry entry, IProgressMonitor monitor, long start, long end) {
        if (start < entry.getEndTime() && end > entry.getStartTime()) {
            LttngKernelAnalysisModule module = entry.getTrace().getAnalysisModuleOfClass(LttngKernelAnalysisModule.class, LttngKernelAnalysisModule.ID);
            if (module == null) {
                return;
            }
            ITmfStateSystem ssq = module.getStateSystem();
            if (ssq == null) {
                return;
            }

            long startTime = Math.max(start, entry.getStartTime());
            long endTime = Math.min(end + 1, entry.getEndTime());
            long resolution = Math.max(1, (end - ssq.getStartTime()) / getDisplayWidth());
            List<ITimeEvent> eventList = getEventList(entry, startTime, endTime, resolution, monitor);
            if (eventList == null) {
                return;
            }
            for (ITimeEvent event : eventList) {
                entry.addEvent(event);
            }
            if (trace.equals(getTrace())) {
                redraw();
            }
        }
        for (ITimeGraphEntry child : entry.getChildren()) {
            if (monitor.isCanceled()) {
                return;
            }
            buildStatusEvents(trace, (ControlFlowEntry) child, monitor, start, end);
        }
    }

    @Override
    protected @Nullable List<ITimeEvent> getEventList(TimeGraphEntry tgentry, long startTime, long endTime, long resolution, IProgressMonitor monitor) {
        List<ITimeEvent> eventList = null;
        if (!(tgentry instanceof ControlFlowEntry)) {
            return eventList;
        }
        ControlFlowEntry entry = (ControlFlowEntry) tgentry;
        final long realStart = Math.max(startTime, entry.getStartTime());
        final long realEnd = Math.min(endTime, entry.getEndTime());
        if (realEnd <= realStart) {
            return null;
        }
        LttngKernelAnalysisModule module = entry.getTrace().getAnalysisModuleOfClass(LttngKernelAnalysisModule.class, LttngKernelAnalysisModule.ID);
        if (module == null) {
            return null;
        }
        ITmfStateSystem ssq = module.getStateSystem();
        if (ssq == null) {
            return null;
        }
        try {
            int statusQuark = ssq.getQuarkRelative(entry.getThreadQuark(), Attributes.STATUS);
            List<ITmfStateInterval> statusIntervals = ssq.queryHistoryRange(statusQuark, realStart, realEnd - 1, resolution, monitor);
            eventList = new ArrayList<>(statusIntervals.size());
            long lastEndTime = -1;
            for (ITmfStateInterval statusInterval : statusIntervals) {
                if (monitor.isCanceled()) {
                    return null;
                }
                long time = statusInterval.getStartTime();
                long duration = statusInterval.getEndTime() - time + 1;
                int status = -1;
                try {
                    status = statusInterval.getStateValue().unboxInt();
                } catch (StateValueTypeException e) {
                    e.printStackTrace();
                }
                if (lastEndTime != time && lastEndTime != -1) {
                    eventList.add(new TimeEvent(entry, lastEndTime, time - lastEndTime));
                }
                eventList.add(new TimeEvent(entry, time, duration, status));
                lastEndTime = time + duration;
            }
        } catch (AttributeNotFoundException | TimeRangeException e) {
            e.printStackTrace();
        } catch (StateSystemDisposedException e) {
            /* Ignored */
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
        ITmfTrace[] traces = TmfTraceManager.getTraceSet(getTrace());
        if (traces == null) {
            return thread;
        }
        for (ITmfTrace trace : traces) {
            if (thread > 0) {
                break;
            }
            LttngKernelAnalysisModule module = trace.getAnalysisModuleOfClass(LttngKernelAnalysisModule.class, LttngKernelAnalysisModule.ID);
            if (module == null) {
                continue;
            }
            ITmfStateSystem ssq = module.getStateSystem();
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
                        e.printStackTrace();
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
    protected List<ILinkEvent> getLinkList(long startTime, long endTime, long resolution, IProgressMonitor monitor) {
        List<ILinkEvent> list = new ArrayList<>();
        ITmfTrace[] traces = TmfTraceManager.getTraceSet(getTrace());
        List<TimeGraphEntry> entryList = getEntryList(getTrace());
        if (traces == null || entryList == null) {
            return list;
        }
        for (ITmfTrace trace : traces) {
            LttngKernelAnalysisModule module = trace.getAnalysisModuleOfClass(LttngKernelAnalysisModule.class, LttngKernelAnalysisModule.ID);
            if (module == null) {
                continue;
            }
            ITmfStateSystem ssq = module.getStateSystem();
            if (ssq == null) {
                continue;
            }
            try {
                long start = Math.max(startTime, ssq.getStartTime());
                long end = Math.min(endTime, ssq.getCurrentEndTime());
                if (end < start) {
                    continue;
                }
                List<Integer> currentThreadQuarks = ssq.getQuarks(Attributes.CPUS, "*", Attributes.CURRENT_THREAD); //$NON-NLS-1$
                for (int currentThreadQuark : currentThreadQuarks) {
                    // adjust the query range to include the previous and following intervals
                    long qstart = Math.max(ssq.querySingleState(start, currentThreadQuark).getStartTime() - 1, ssq.getStartTime());
                    long qend = Math.min(ssq.querySingleState(end, currentThreadQuark).getEndTime() + 1, ssq.getCurrentEndTime());
                    List<ITmfStateInterval> currentThreadIntervals = ssq.queryHistoryRange(currentThreadQuark, qstart, qend, resolution, monitor);
                    int prevThread = 0;
                    long prevEnd = 0;
                    long lastEnd = 0;
                    for (ITmfStateInterval currentThreadInterval : currentThreadIntervals) {
                        if (monitor.isCanceled()) {
                            return null;
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
            } catch (TimeRangeException | AttributeNotFoundException | StateValueTypeException e) {
                e.printStackTrace();
            } catch (StateSystemDisposedException e) {
                /* Ignored */
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
