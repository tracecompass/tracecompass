/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson, École Polytechnique de Montréal
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.Attributes;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.Messages;
import org.eclipse.linuxtools.lttng2.kernel.core.trace.LttngKernelTrace;
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

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    @Override
    protected void buildEventList(final ITmfTrace trace, IProgressMonitor monitor) {
        setStartTime(Long.MAX_VALUE);
        setEndTime(Long.MIN_VALUE);

        ArrayList<ControlFlowEntry> rootList = new ArrayList<>();
        for (ITmfTrace aTrace : TmfTraceManager.getTraceSet(trace)) {
            if (monitor.isCanceled()) {
                return;
            }
            if (aTrace instanceof LttngKernelTrace) {
                ArrayList<ControlFlowEntry> entryList = new ArrayList<>();
                LttngKernelTrace ctfKernelTrace = (LttngKernelTrace) aTrace;
                ITmfStateSystem ssq = ctfKernelTrace.getStateSystems().get(LttngKernelTrace.STATE_ID);
                if (ssq == null) {
                    return;
                }
                ssq.waitUntilBuilt();
                if (ssq.isCancelled()) {
                    return;
                }
                long start = ssq.getStartTime();
                long end = ssq.getCurrentEndTime() + 1;
                setStartTime(Math.min(getStartTime(), start));
                setEndTime(Math.max(getEndTime(), end));
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
                    if (threadId == 0) { // ignore the swapper thread
                        continue;
                    }
                    int execNameQuark = -1;
                    try {
                        try {
                            execNameQuark = ssq.getQuarkRelative(threadQuark, Attributes.EXEC_NAME);
                        } catch (AttributeNotFoundException e) {
                            continue;
                        }
                        int ppidQuark = ssq.getQuarkRelative(threadQuark, Attributes.PPID);
                        List<ITmfStateInterval> execNameIntervals = ssq.queryHistoryRange(execNameQuark, start, end - 1);
                        // use monitor when available in api
                        if (monitor.isCanceled()) {
                            return;
                        }
                        ControlFlowEntry entry = null;
                        for (ITmfStateInterval execNameInterval : execNameIntervals) {
                            if (monitor.isCanceled()) {
                                return;
                            }
                            if (!execNameInterval.getStateValue().isNull() &&
                                    execNameInterval.getStateValue().getType() == ITmfStateValue.Type.STRING) {
                                String execName = execNameInterval.getStateValue().unboxStr();
                                long startTime = execNameInterval.getStartTime();
                                long endTime = execNameInterval.getEndTime() + 1;
                                int ppid = -1;
                                if (ppidQuark != -1) {
                                    ITmfStateInterval ppidInterval = ssq.querySingleState(startTime, ppidQuark);
                                    ppid = ppidInterval.getStateValue().unboxInt();
                                }
                                if (entry == null) {
                                    entry = new ControlFlowEntry(threadQuark, ctfKernelTrace, execName, threadId, ppid, startTime, endTime);
                                    entryList.add(entry);
                                } else {
                                    // update the name of the entry to the
                                    // latest execName
                                    entry.setName(execName);
                                }
                                entry.addEvent(new TimeEvent(entry, startTime, endTime - startTime));
                            } else {
                                entry = null;
                            }
                        }
                    } catch (AttributeNotFoundException e) {
                        e.printStackTrace();
                    } catch (TimeRangeException e) {
                        e.printStackTrace();
                    } catch (StateValueTypeException e) {
                        e.printStackTrace();
                    } catch (StateSystemDisposedException e) {
                        /* Ignored */
                    }
                }
                buildTree(entryList, rootList);
            }
            Collections.sort(rootList, getEntryComparator());
            putEntryList(trace, new ArrayList<TimeGraphEntry>(rootList));

            if (trace.equals(getTrace())) {
                refresh();
            }
        }
        for (ControlFlowEntry entry : rootList) {
            if (monitor.isCanceled()) {
                return;
            }
            buildStatusEvents(entry.getTrace(), entry, monitor);
        }
    }

    private static void buildTree(ArrayList<ControlFlowEntry> entryList,
            ArrayList<ControlFlowEntry> rootList) {
        for (ControlFlowEntry entry : entryList) {
            boolean root = true;
            if (entry.getParentThreadId() > 0) {
                for (ControlFlowEntry parent : entryList) {
                    if (parent.getThreadId() == entry.getParentThreadId() &&
                            entry.getStartTime() >= parent.getStartTime() &&
                            entry.getStartTime() <= parent.getEndTime()) {
                        parent.addChild(entry);
                        root = false;
                        break;
                    }
                }
            }
            if (root) {
                rootList.add(entry);
            }
        }
    }

    private void buildStatusEvents(ITmfTrace trace, ControlFlowEntry entry, IProgressMonitor monitor) {
        ITmfStateSystem ssq = entry.getTrace().getStateSystems().get(LttngKernelTrace.STATE_ID);

        long start = ssq.getStartTime();
        long end = ssq.getCurrentEndTime() + 1;
        long resolution = Math.max(1, (end - start) / getDisplayWidth());
        List<ITimeEvent> eventList = getEventList(entry, entry.getStartTime(), entry.getEndTime(), resolution, monitor);
        if (monitor.isCanceled()) {
            return;
        }
        entry.setEventList(eventList);
        if (trace.equals(getTrace())) {
            redraw();
        }
        for (ITimeGraphEntry child : entry.getChildren()) {
            if (monitor.isCanceled()) {
                return;
            }
            buildStatusEvents(trace, (ControlFlowEntry) child, monitor);
        }
    }

    @Override
    protected List<ITimeEvent> getEventList(TimeGraphEntry tgentry, long startTime, long endTime, long resolution, IProgressMonitor monitor) {
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
        ITmfStateSystem ssq = entry.getTrace().getStateSystems().get(LttngKernelTrace.STATE_ID);
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
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (TimeRangeException e) {
            e.printStackTrace();
        } catch (StateSystemDisposedException e) {
            /* Ignored */
        }
        return eventList;
    }

    /**
     * Returns a value corresponding to the selected entry.
     *
     * Used in conjunction with selectEntry to change the selected entry. If one
     * of these methods is overridden in child class, then both should be.
     *
     * @param time
     *            The currently selected time
     * @return a value identifying the entry
     */
    private int getSelectionValue(long time) {
        int thread = -1;
        for (ITmfTrace trace : fTraceManager.getActiveTraceSet()) {
            if (thread > 0) {
                break;
            }
            if (trace instanceof LttngKernelTrace) {
                LttngKernelTrace ctfKernelTrace = (LttngKernelTrace) trace;
                ITmfStateSystem ssq = ctfKernelTrace.getStateSystems().get(LttngKernelTrace.STATE_ID);
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
                        } catch (AttributeNotFoundException e) {
                            e.printStackTrace();
                        } catch (TimeRangeException e) {
                            e.printStackTrace();
                        } catch (StateValueTypeException e) {
                            e.printStackTrace();
                        } catch (StateSystemDisposedException e) {
                            /* Ignored */
                        }
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
        List<TimeGraphEntry> entryList = getEntryListMap().get(getTrace());
        if (traces == null || entryList == null) {
            return list;
        }
        for (ITmfTrace trace : traces) {
            if (trace instanceof LttngKernelTrace) {
                ITmfStateSystem ssq = trace.getStateSystems().get(LttngKernelTrace.STATE_ID);
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
                } catch (TimeRangeException e) {
                    e.printStackTrace();
                } catch (AttributeNotFoundException e) {
                    e.printStackTrace();
                } catch (StateValueTypeException e) {
                    e.printStackTrace();
                } catch (StateSystemDisposedException e) {
                    /* Ignored */
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
