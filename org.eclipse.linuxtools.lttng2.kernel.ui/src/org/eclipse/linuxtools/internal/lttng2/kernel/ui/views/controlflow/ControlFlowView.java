/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.controlflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.Attributes;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.Messages;
import org.eclipse.linuxtools.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTimestamp;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphRangeListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphSelectionListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphTimeListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphCombo;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphRangeUpdateEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphSelectionEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphTimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils.Resolution;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils.TimeFormat;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;

/**
 * The Control Flow view main object
 *
 */
public class ControlFlowView extends TmfView {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * View ID.
     */
    public static final String ID = "org.eclipse.linuxtools.lttng2.kernel.ui.views.controlflow"; //$NON-NLS-1$

    private static final String PROCESS_COLUMN    = Messages.ControlFlowView_processColumn;
    private static final String TID_COLUMN        = Messages.ControlFlowView_tidColumn;
    private static final String PTID_COLUMN       = Messages.ControlFlowView_ptidColumn;
    private static final String BIRTH_TIME_COLUMN = Messages.ControlFlowView_birthTimeColumn;
    private static final String TRACE_COLUMN      = Messages.ControlFlowView_traceColumn;

    private final String[] COLUMN_NAMES = new String[] {
            PROCESS_COLUMN,
            TID_COLUMN,
            PTID_COLUMN,
            BIRTH_TIME_COLUMN,
            TRACE_COLUMN
    };

    private final String[] FILTER_COLUMN_NAMES = new String[] {
            PROCESS_COLUMN,
            TID_COLUMN
    };

    /**
     * Redraw state enum
     */
    private enum State { IDLE, BUSY, PENDING }

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    // The timegraph combo
    private TimeGraphCombo fTimeGraphCombo;

    // The selected trace
    private ITmfTrace fTrace;

    // The timegraph entry list
    private ArrayList<ControlFlowEntry> fEntryList;

    // The trace to entry list hash map
    final private HashMap<ITmfTrace, ArrayList<ControlFlowEntry>> fEntryListMap = new HashMap<ITmfTrace, ArrayList<ControlFlowEntry>>();

    // The trace to build thread hash map
    final private HashMap<ITmfTrace, BuildThread> fBuildThreadMap = new HashMap<ITmfTrace, BuildThread>();

    // The start time
    private long fStartTime;

    // The end time
    private long fEndTime;

    // The display width
    private final int fDisplayWidth;

    // The zoom thread
    private ZoomThread fZoomThread;

    // The next resource action
    private Action fNextResourceAction;

    // The previous resource action
    private Action fPreviousResourceAction;

    // A comparator class
    private final ControlFlowEntryComparator fControlFlowEntryComparator = new ControlFlowEntryComparator();

    // The redraw state used to prevent unnecessary queuing of display runnables
    private State fRedrawState = State.IDLE;

    // The redraw synchronization object
    final private Object fSyncObj = new Object();

    // ------------------------------------------------------------------------
    // Classes
    // ------------------------------------------------------------------------

    private class TreeContentProvider implements ITreeContentProvider {

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return (ITimeGraphEntry[]) inputElement;
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            ITimeGraphEntry entry = (ITimeGraphEntry) parentElement;
            List<? extends ITimeGraphEntry> children = entry.getChildren();
            return children.toArray(new ITimeGraphEntry[children.size()]);
        }

        @Override
        public Object getParent(Object element) {
            ITimeGraphEntry entry = (ITimeGraphEntry) element;
            return entry.getParent();
        }

        @Override
        public boolean hasChildren(Object element) {
            ITimeGraphEntry entry = (ITimeGraphEntry) element;
            return entry.hasChildren();
        }

    }

    private class TreeLabelProvider implements ITableLabelProvider {

        @Override
        public void addListener(ILabelProviderListener listener) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            ControlFlowEntry entry = (ControlFlowEntry) element;
            if (columnIndex == 0) {
                return entry.getName();
            } else if (columnIndex == 1) {
                return Integer.toString(entry.getThreadId());
            } else if (columnIndex == 2) {
                if (entry.getParentThreadId() > 0) {
                    return Integer.toString(entry.getParentThreadId());
                }
            } else if (columnIndex == 3) {
                return Utils.formatTime(entry.getStartTime(), TimeFormat.CALENDAR, Resolution.NANOSEC);
            } else if (columnIndex == 4) {
                return entry.getTrace().getName();
            }
            return ""; //$NON-NLS-1$
        }

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

    private class BuildThread extends Thread {
        private final ITmfTrace fBuildTrace;
        private final IProgressMonitor fMonitor;

        public BuildThread(ITmfTrace trace) {
            super("ControlFlowView build"); //$NON-NLS-1$
            fBuildTrace = trace;
            fMonitor = new NullProgressMonitor();
        }

        @Override
        public void run() {
            buildEventList(fBuildTrace, fMonitor);
            synchronized (fBuildThreadMap) {
                fBuildThreadMap.remove(this);
            }
        }

        public void cancel() {
            fMonitor.setCanceled(true);
        }
    }

    private class ZoomThread extends Thread {
        private final ArrayList<ControlFlowEntry> fZoomEntryList;
        private final long fZoomStartTime;
        private final long fZoomEndTime;
        private final long fResolution;
        private final IProgressMonitor fMonitor;

        public ZoomThread(ArrayList<ControlFlowEntry> entryList, long startTime, long endTime) {
            super("ControlFlowView zoom"); //$NON-NLS-1$
            fZoomEntryList = entryList;
            fZoomStartTime = startTime;
            fZoomEndTime = endTime;
            fResolution = Math.max(1, (fZoomEndTime - fZoomStartTime) / fDisplayWidth);
            fMonitor = new NullProgressMonitor();
        }

        @Override
        public void run() {
            if (fZoomEntryList == null) {
                return;
            }
            for (ControlFlowEntry entry : fZoomEntryList) {
                if (fMonitor.isCanceled()) {
                    break;
                }
                zoom(entry, fMonitor);
            }
        }

        private void zoom(ControlFlowEntry entry, IProgressMonitor monitor) {
            if (fZoomStartTime <= fStartTime && fZoomEndTime >= fEndTime) {
                entry.setZoomedEventList(null);
            } else {
                List<ITimeEvent> zoomedEventList = getEventList(entry, fZoomStartTime, fZoomEndTime, fResolution, monitor);
                if (zoomedEventList != null) {
                    entry.setZoomedEventList(zoomedEventList);
                }
            }
            redraw();
            for (ControlFlowEntry child : entry.getChildren()) {
                if (fMonitor.isCanceled()) {
                    return;
                }
                zoom(child, monitor);
            }
        }

        public void cancel() {
            fMonitor.setCanceled(true);
        }
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public ControlFlowView() {
        super(ID);
        fDisplayWidth = Display.getDefault().getBounds().width;
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

    @Override
    public void createPartControl(Composite parent) {
        fTimeGraphCombo = new TimeGraphCombo(parent, SWT.NONE);

        fTimeGraphCombo.setTreeContentProvider(new TreeContentProvider());

        fTimeGraphCombo.setTreeLabelProvider(new TreeLabelProvider());

        fTimeGraphCombo.setTimeGraphProvider(new ControlFlowPresentationProvider());

        fTimeGraphCombo.setTreeColumns(COLUMN_NAMES);

        fTimeGraphCombo.setFilterContentProvider(new TreeContentProvider());

        fTimeGraphCombo.setFilterLabelProvider(new TreeLabelProvider());

        fTimeGraphCombo.setFilterColumns(FILTER_COLUMN_NAMES);

        fTimeGraphCombo.getTimeGraphViewer().addRangeListener(new ITimeGraphRangeListener() {
            @Override
            public void timeRangeUpdated(TimeGraphRangeUpdateEvent event) {
                final long startTime = event.getStartTime();
                final long endTime = event.getEndTime();
                TmfTimeRange range = new TmfTimeRange(new CtfTmfTimestamp(startTime), new CtfTmfTimestamp(endTime));
                TmfTimestamp time = new CtfTmfTimestamp(fTimeGraphCombo.getTimeGraphViewer().getSelectedTime());
                broadcast(new TmfRangeSynchSignal(ControlFlowView.this, range, time));
                if (fZoomThread != null) {
                    fZoomThread.cancel();
                }
                startZoomThread(startTime, endTime);
            }
        });

        fTimeGraphCombo.getTimeGraphViewer().addTimeListener(new ITimeGraphTimeListener() {
            @Override
            public void timeSelected(TimeGraphTimeEvent event) {
                long time = event.getTime();
                broadcast(new TmfTimeSynchSignal(ControlFlowView.this, new CtfTmfTimestamp(time)));
            }
        });

        fTimeGraphCombo.addSelectionListener(new ITimeGraphSelectionListener() {
            @Override
            public void selectionChanged(TimeGraphSelectionEvent event) {
                //ITimeGraphEntry selection = event.getSelection();
            }
        });

        fTimeGraphCombo.getTimeGraphViewer().setTimeFormat(TimeFormat.CALENDAR);

        // View Action Handling
        makeActions();
        contributeToActionBars();

        ITmfTrace trace = getActiveTrace();
        if (trace != null) {
            traceSelected(new TmfTraceSelectedSignal(this, trace));
        }

        // make selection available to other views
        getSite().setSelectionProvider(fTimeGraphCombo.getTreeViewer());
    }

    @Override
    public void setFocus() {
        fTimeGraphCombo.setFocus();
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Handler for the trace selected signal
     *
     * @param signal
     *            The signal that's received
     */
    @TmfSignalHandler
    public void traceSelected(final TmfTraceSelectedSignal signal) {
        if (signal.getTrace() == fTrace) {
            return;
        }
        fTrace = signal.getTrace();

        synchronized (fEntryListMap) {
            fEntryList = fEntryListMap.get(fTrace);
            if (fEntryList == null) {
                synchronized (fBuildThreadMap) {
                    BuildThread buildThread = new BuildThread(fTrace);
                    fBuildThreadMap.put(fTrace, buildThread);
                    buildThread.start();
                }
            } else {
                fStartTime = fTrace.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                fEndTime = fTrace.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                refresh();
            }
        }
    }

    /**
     * Trace is closed: clear the data structures and the view
     *
     * @param signal the signal received
     */
    @TmfSignalHandler
    public void traceClosed(final TmfTraceClosedSignal signal) {
        synchronized (fBuildThreadMap) {
            BuildThread buildThread = fBuildThreadMap.remove(signal.getTrace());
            if (buildThread != null) {
                buildThread.cancel();
            }
        }
        synchronized (fEntryListMap) {
            fEntryListMap.remove(signal.getTrace());
        }
        if (signal.getTrace() == fTrace) {
            fTrace = null;
            fStartTime = 0;
            fEndTime = 0;
            if (fZoomThread != null) {
                fZoomThread.cancel();
            }
            refresh();
        }
    }

    /**
     * Handler for the synch signal
     *
     * @param signal
     *            The signal that's received
     */
    @TmfSignalHandler
    public void synchToTime(final TmfTimeSynchSignal signal) {
        if (signal.getSource() == this || fTrace == null) {
            return;
        }
        final long time = signal.getCurrentTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();

        int thread = -1;
        for (ITmfTrace trace : fTraceManager.getActiveTraceSet()) {
            if (thread > 0) {
                break;
            }
            if (trace instanceof LttngKernelTrace) {
                LttngKernelTrace ctfKernelTrace = (LttngKernelTrace) trace;
                ITmfStateSystem ssq = ctfKernelTrace.getStateSystems().get(LttngKernelTrace.STATE_ID);
                if (time >= ssq.getStartTime() && time <= ssq.getCurrentEndTime()) {
                    List<Integer> currentThreadQuarks = ssq.getQuarks(Attributes.CPUS, "*", Attributes.CURRENT_THREAD);  //$NON-NLS-1$
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
        final int selectedThread = thread;

        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphCombo.isDisposed()) {
                    return;
                }
                fTimeGraphCombo.getTimeGraphViewer().setSelectedTime(time, true);
                startZoomThread(fTimeGraphCombo.getTimeGraphViewer().getTime0(), fTimeGraphCombo.getTimeGraphViewer().getTime1());

                if (selectedThread > 0) {
                    for (Object element : fTimeGraphCombo.getTimeGraphViewer().getExpandedElements()) {
                        if (element instanceof ControlFlowEntry) {
                            ControlFlowEntry entry = (ControlFlowEntry) element;
                            if (entry.getThreadId() == selectedThread) {
                                fTimeGraphCombo.setSelection(entry);
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Handler for the range sync signal
     *
     * @param signal
     *            The signal that's received
     */
    @TmfSignalHandler
    public void synchToRange(final TmfRangeSynchSignal signal) {
        if (signal.getSource() == this || fTrace == null) {
            return;
        }
        if (signal.getCurrentRange().getIntersection(fTrace.getTimeRange()) == null) {
            return;
        }
        final long startTime = signal.getCurrentRange().getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        final long endTime = signal.getCurrentRange().getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        final long time = signal.getCurrentTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphCombo.isDisposed()) {
                    return;
                }
                fTimeGraphCombo.getTimeGraphViewer().setStartFinishTime(startTime, endTime);
                fTimeGraphCombo.getTimeGraphViewer().setSelectedTime(time, false);
                startZoomThread(startTime, endTime);
            }
        });
    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    private void buildEventList(final ITmfTrace trace, IProgressMonitor monitor) {
        fStartTime = Long.MAX_VALUE;
        fEndTime = Long.MIN_VALUE;
        ArrayList<ControlFlowEntry> rootList = new ArrayList<ControlFlowEntry>();
        for (ITmfTrace aTrace : fTraceManager.getActiveTraceSet()) {
            if (monitor.isCanceled()) {
                return;
            }
            if (aTrace instanceof LttngKernelTrace) {
                ArrayList<ControlFlowEntry> entryList = new ArrayList<ControlFlowEntry>();
                LttngKernelTrace ctfKernelTrace = (LttngKernelTrace) aTrace;
                ITmfStateSystem ssq = ctfKernelTrace.getStateSystems().get(LttngKernelTrace.STATE_ID);
                if (!ssq.waitUntilBuilt()) {
                    return;
                }
                long start = ssq.getStartTime();
                long end = ssq.getCurrentEndTime() + 1;
                fStartTime = Math.min(fStartTime, start);
                fEndTime = Math.max(fEndTime, end);
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
                        List<ITmfStateInterval> execNameIntervals = ssq.queryHistoryRange(execNameQuark, start, end - 1); // use monitor when available in api
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
                                    // update the name of the entry to the latest execName
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
            Collections.sort(rootList, fControlFlowEntryComparator);
            synchronized (fEntryListMap) {
                fEntryListMap.put(trace, (ArrayList<ControlFlowEntry>) rootList.clone());
            }
            if (trace == fTrace) {
                refresh();
            }
        }
        for (ControlFlowEntry entry : rootList) {
            if (monitor.isCanceled()) {
                return;
            }
            buildStatusEvents(trace, entry, monitor);
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
        long resolution = Math.max(1, (end - start) / fDisplayWidth);
        List<ITimeEvent> eventList = getEventList(entry, entry.getStartTime(), entry.getEndTime(), resolution, monitor);
        if (monitor.isCanceled()) {
            return;
        }
        entry.setEventList(eventList);
        if (trace == fTrace) {
            redraw();
        }
        for (ITimeGraphEntry child : entry.getChildren()) {
            if (monitor.isCanceled()) {
                return;
            }
            buildStatusEvents(trace, (ControlFlowEntry) child, monitor);
        }
    }

    private static List<ITimeEvent> getEventList(ControlFlowEntry entry,
            long startTime, long endTime, long resolution,
            IProgressMonitor monitor) {
        final long realStart = Math.max(startTime, entry.getStartTime());
        final long realEnd = Math.min(endTime, entry.getEndTime());
        if (realEnd <= realStart) {
            return null;
        }
        ITmfStateSystem ssq = entry.getTrace().getStateSystems().get(LttngKernelTrace.STATE_ID);
        List<ITimeEvent> eventList = null;
        try {
            int statusQuark = ssq.getQuarkRelative(entry.getThreadQuark(), Attributes.STATUS);
            List<ITmfStateInterval> statusIntervals = ssq.queryHistoryRange(statusQuark, realStart, realEnd - 1, resolution, monitor);
            eventList = new ArrayList<ITimeEvent>(statusIntervals.size());
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
                eventList.add(new ControlFlowEvent(entry, time, duration, status));
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

    private void refresh() {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphCombo.isDisposed()) {
                    return;
                }
                ITimeGraphEntry[] entries = null;
                synchronized (fEntryListMap) {
                    fEntryList = fEntryListMap.get(fTrace);
                    if (fEntryList == null) {
                        fEntryList = new ArrayList<ControlFlowEntry>();
                    }
                    entries = fEntryList.toArray(new ITimeGraphEntry[0]);
                }
                Arrays.sort(entries, fControlFlowEntryComparator);
                fTimeGraphCombo.setInput(entries);
                fTimeGraphCombo.getTimeGraphViewer().setTimeBounds(fStartTime, fEndTime);

                long timestamp = fTrace == null ? 0 : fTraceManager.getCurrentTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                long startTime = fTrace == null ? 0 : fTraceManager.getCurrentRange().getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                long endTime = fTrace == null ? 0 : fTraceManager.getCurrentRange().getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                startTime = Math.max(startTime, fStartTime);
                endTime = Math.min(endTime, fEndTime);
                fTimeGraphCombo.getTimeGraphViewer().setSelectedTime(timestamp, false);
                fTimeGraphCombo.getTimeGraphViewer().setStartFinishTime(startTime, endTime);

                for (TreeColumn column : fTimeGraphCombo.getTreeViewer().getTree().getColumns()) {
                    column.pack();
                }

                startZoomThread(startTime, endTime);
            }
        });
    }

    private void redraw() {
        synchronized (fSyncObj) {
            if (fRedrawState == State.IDLE) {
                fRedrawState = State.BUSY;
            } else {
                fRedrawState = State.PENDING;
                return;
            }
        }
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphCombo.isDisposed()) {
                    return;
                }
                fTimeGraphCombo.redraw();
                fTimeGraphCombo.update();
                synchronized (fSyncObj) {
                    if (fRedrawState == State.PENDING) {
                        fRedrawState = State.IDLE;
                        redraw();
                    } else {
                        fRedrawState = State.IDLE;
                    }
                }
            }
        });
    }

    private void startZoomThread(long startTime, long endTime) {
        if (fZoomThread != null) {
            fZoomThread.cancel();
        }
        fZoomThread = new ZoomThread(fEntryList, startTime, endTime);
        fZoomThread.start();
    }

    private void makeActions() {
        fPreviousResourceAction = fTimeGraphCombo.getTimeGraphViewer().getPreviousItemAction();
        fPreviousResourceAction.setText(Messages.ControlFlowView_previousProcessActionNameText);
        fPreviousResourceAction.setToolTipText(Messages.ControlFlowView_previousProcessActionToolTipText);
        fNextResourceAction = fTimeGraphCombo.getTimeGraphViewer().getNextItemAction();
        fNextResourceAction.setText(Messages.ControlFlowView_nextProcessActionNameText);
        fNextResourceAction.setToolTipText(Messages.ControlFlowView_nextProcessActionToolTipText);
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(fTimeGraphCombo.getShowFilterAction());
        manager.add(fTimeGraphCombo.getTimeGraphViewer().getShowLegendAction());
        manager.add(new Separator());
        manager.add(fTimeGraphCombo.getTimeGraphViewer().getResetScaleAction());
        manager.add(fTimeGraphCombo.getTimeGraphViewer().getPreviousEventAction());
        manager.add(fTimeGraphCombo.getTimeGraphViewer().getNextEventAction());
        manager.add(fPreviousResourceAction);
        manager.add(fNextResourceAction);
        manager.add(fTimeGraphCombo.getTimeGraphViewer().getZoomInAction());
        manager.add(fTimeGraphCombo.getTimeGraphViewer().getZoomOutAction());
        manager.add(new Separator());
    }
}
