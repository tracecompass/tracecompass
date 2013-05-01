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

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.Attributes;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.Messages;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources.ResourcesEntry.Type;
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
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphRangeListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphTimeListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphRangeUpdateEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphTimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphViewer;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils.TimeFormat;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;

/**
 * Main implementation for the LTTng 2.0 kernel Resource view
 *
 * @author Patrick Tasse
 */
public class ResourcesView extends TmfView {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /** View ID. */
    public static final String ID = "org.eclipse.linuxtools.lttng2.kernel.ui.views.resources"; //$NON-NLS-1$

    /**
     * Redraw state enum
     */
    private enum State { IDLE, BUSY, PENDING }

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    // The time graph viewer
    TimeGraphViewer fTimeGraphViewer;

    // The selected trace
    private ITmfTrace fTrace;

    // The time graph entry list
    private ArrayList<TraceEntry> fEntryList;

    // The trace to entry list hash map
    final private HashMap<ITmfTrace, ArrayList<TraceEntry>> fEntryListMap = new HashMap<ITmfTrace, ArrayList<TraceEntry>>();

    // The trace to build thread hash map
    final private HashMap<ITmfTrace, BuildThread> fBuildThreadMap = new HashMap<ITmfTrace, BuildThread>();

    // The start time
    private long fStartTime;

    // The end time
    private long fEndTime;

    // The display width
    private final int fDisplayWidth;

    // The next resource action
    private Action fNextResourceAction;

    // The previous resource action
    private Action fPreviousResourceAction;

    // The zoom thread
    private ZoomThread fZoomThread;

    // The redraw state used to prevent unnecessary queuing of display runnables
    private State fRedrawState = State.IDLE;

    // The redraw synchronization object
    final private Object fSyncObj = new Object();

    // ------------------------------------------------------------------------
    // Classes
    // ------------------------------------------------------------------------

    private class TraceEntry implements ITimeGraphEntry {
        // The Trace
        private final LttngKernelTrace fKernelTrace;
        // The start time
        private final long fTraceStartTime;
        // The end time
        private final long fTraceEndTime;
        // The children of the entry
        private final ArrayList<ResourcesEntry> fChildren;
        // The name of entry
        private final String fName;

        public TraceEntry(LttngKernelTrace trace, String name, long startTime, long endTime) {
            fKernelTrace = trace;
            fChildren = new ArrayList<ResourcesEntry>();
            fName = name;
            fTraceStartTime = startTime;
            fTraceEndTime = endTime;
        }

        @Override
        public ITimeGraphEntry getParent() {
            return null;
        }

        @Override
        public boolean hasChildren() {
            return fChildren != null && fChildren.size() > 0;
        }

        @Override
        public List<ResourcesEntry> getChildren() {
            return fChildren;
        }

        @Override
        public String getName() {
            return fName;
        }

        @Override
        public long getStartTime() {
            return fTraceStartTime;
        }

        @Override
        public long getEndTime() {
            return fTraceEndTime;
        }

        @Override
        public boolean hasTimeEvents() {
            return false;
        }

        @Override
        public Iterator<ITimeEvent> getTimeEventsIterator() {
            return null;
        }

        @Override
        public <T extends ITimeEvent> Iterator<T> getTimeEventsIterator(long startTime, long stopTime, long visibleDuration) {
            return null;
        }

        public LttngKernelTrace getTrace() {
            return fKernelTrace;
        }

        public void addChild(ResourcesEntry entry) {
            int index;
            for (index = 0; index < fChildren.size(); index++) {
                ResourcesEntry other = fChildren.get(index);
                if (entry.getType().compareTo(other.getType()) < 0) {
                    break;
                } else if (entry.getType().equals(other.getType())) {
                    if (entry.getId() < other.getId()) {
                        break;
                    }
                }
            }
            entry.setParent(this);
            fChildren.add(index, entry);
        }
    }

    private static class TraceEntryComparator implements Comparator<ITimeGraphEntry> {
        @Override
        public int compare(ITimeGraphEntry o1, ITimeGraphEntry o2) {
            int result = o1.getStartTime() < o2.getStartTime() ? -1 : o1.getStartTime() > o2.getStartTime() ? 1 : 0;
            if (result == 0) {
                result = o1.getName().compareTo(o2.getName());
            }
            return result;
        }
    }

    private class BuildThread extends Thread {
        private final ITmfTrace fBuildTrace;
        private final IProgressMonitor fMonitor;

        public BuildThread(ITmfTrace trace) {
            super("ResourcesView build"); //$NON-NLS-1$
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
        private final ArrayList<TraceEntry> fZoomEntryList;
        private final long fZoomStartTime;
        private final long fZoomEndTime;
        private final IProgressMonitor fMonitor;

        public ZoomThread(ArrayList<TraceEntry> entryList, long startTime, long endTime) {
            super("ResourcesView zoom"); //$NON-NLS-1$
            fZoomEntryList = entryList;
            fZoomStartTime = startTime;
            fZoomEndTime = endTime;
            fMonitor = new NullProgressMonitor();
        }

        @Override
        public void run() {
            if (fZoomEntryList == null) {
                return;
            }
            long resolution = Math.max(1, (fZoomEndTime - fZoomStartTime) / fDisplayWidth);
            for (TraceEntry traceEntry : fZoomEntryList) {
                if (!traceEntry.fKernelTrace.getStateSystems().get(LttngKernelTrace.STATE_ID).waitUntilBuilt()) {
                    return;
                }
                for (ITimeGraphEntry child : traceEntry.getChildren()) {
                    if (fMonitor.isCanceled()) {
                        break;
                    }
                    ResourcesEntry entry = (ResourcesEntry) child;
                    if (fZoomStartTime <= fStartTime && fZoomEndTime >= fEndTime) {
                        entry.setZoomedEventList(null);
                    } else {
                        List<ITimeEvent> zoomedEventList = getEventList(entry, fZoomStartTime, fZoomEndTime, resolution, true, fMonitor);
                        if (zoomedEventList != null) {
                            entry.setZoomedEventList(zoomedEventList);
                        }
                    }
                    redraw();
                }
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
     * Default constructor
     */
    public ResourcesView() {
        super(ID);
        fDisplayWidth = Display.getDefault().getBounds().width;
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

    @Override
    public void createPartControl(Composite parent) {
        fTimeGraphViewer = new TimeGraphViewer(parent, SWT.NONE);

        fTimeGraphViewer.setTimeGraphProvider(new ResourcesPresentationProvider(fTimeGraphViewer));

        fTimeGraphViewer.setTimeFormat(TimeFormat.CALENDAR);

        fTimeGraphViewer.addRangeListener(new ITimeGraphRangeListener() {
            @Override
            public void timeRangeUpdated(TimeGraphRangeUpdateEvent event) {
                long startTime = event.getStartTime();
                long endTime = event.getEndTime();
                TmfTimeRange range = new TmfTimeRange(new CtfTmfTimestamp(startTime), new CtfTmfTimestamp(endTime));
                TmfTimestamp time = new CtfTmfTimestamp(fTimeGraphViewer.getSelectedTime());
                broadcast(new TmfRangeSynchSignal(ResourcesView.this, range, time));
                startZoomThread(startTime, endTime);
            }
        });

        fTimeGraphViewer.addTimeListener(new ITimeGraphTimeListener() {
            @Override
            public void timeSelected(TimeGraphTimeEvent event) {
                long time = event.getTime();
                broadcast(new TmfTimeSynchSignal(ResourcesView.this, new CtfTmfTimestamp(time)));
            }
        });

        // View Action Handling
        makeActions();
        contributeToActionBars();

        ITmfTrace trace = getActiveTrace();
        if (trace != null) {
            traceSelected(new TmfTraceSelectedSignal(this, trace));
        }
    }

    @Override
    public void setFocus() {
        fTimeGraphViewer.setFocus();
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Handler for the trace selected signal
     *
     * @param signal
     *            The incoming signal
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
     * Trace is disposed: clear the data structures and the view
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
     * Handler for the TimeSynch signal
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public void synchToTime(final TmfTimeSynchSignal signal) {
        if (signal.getSource() == this || fTrace == null) {
            return;
        }
        final long time = signal.getCurrentTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphViewer.getControl().isDisposed()) {
                    return;
                }
                fTimeGraphViewer.setSelectedTime(time, true);
                startZoomThread(fTimeGraphViewer.getTime0(), fTimeGraphViewer.getTime1());
            }
        });
    }

    /**
     * Handler for the RangeSynch signal
     *
     * @param signal
     *            The incoming signal
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
                if (fTimeGraphViewer.getControl().isDisposed()) {
                    return;
                }
                fTimeGraphViewer.setStartFinishTime(startTime, endTime);
                fTimeGraphViewer.setSelectedTime(time, false);
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
        ArrayList<TraceEntry> entryList = new ArrayList<TraceEntry>();
        for (ITmfTrace aTrace : fTraceManager.getActiveTraceSet()) {
            if (monitor.isCanceled()) {
                return;
            }
            if (aTrace instanceof LttngKernelTrace) {
                LttngKernelTrace ctfKernelTrace = (LttngKernelTrace) aTrace;
                ITmfStateSystem ssq = ctfKernelTrace.getStateSystems().get(LttngKernelTrace.STATE_ID);
                if (!ssq.waitUntilBuilt()) {
                    return;
                }
                long startTime = ssq.getStartTime();
                long endTime = ssq.getCurrentEndTime() + 1;
                TraceEntry groupEntry = new TraceEntry(ctfKernelTrace, aTrace.getName(), startTime, endTime);
                entryList.add(groupEntry);
                fStartTime = Math.min(fStartTime, startTime);
                fEndTime = Math.max(fEndTime, endTime);
                List<Integer> cpuQuarks = ssq.getQuarks(Attributes.CPUS, "*"); //$NON-NLS-1$
                ResourcesEntry[] cpuEntries = new ResourcesEntry[cpuQuarks.size()];
                for (int i = 0; i < cpuQuarks.size(); i++) {
                    int cpuQuark = cpuQuarks.get(i);
                    int cpu = Integer.parseInt(ssq.getAttributeName(cpuQuark));
                    ResourcesEntry entry = new ResourcesEntry(cpuQuark, ctfKernelTrace, Type.CPU, cpu);
                    groupEntry.addChild(entry);
                    cpuEntries[i] = entry;
                }
                List<Integer> irqQuarks = ssq.getQuarks(Attributes.RESOURCES, Attributes.IRQS, "*"); //$NON-NLS-1$
                ResourcesEntry[] irqEntries = new ResourcesEntry[irqQuarks.size()];
                for (int i = 0; i < irqQuarks.size(); i++) {
                    int irqQuark = irqQuarks.get(i);
                    int irq = Integer.parseInt(ssq.getAttributeName(irqQuark));
                    ResourcesEntry entry = new ResourcesEntry(irqQuark, ctfKernelTrace, Type.IRQ, irq);
                    groupEntry.addChild(entry);
                    irqEntries[i] = entry;
                }
                List<Integer> softIrqQuarks = ssq.getQuarks(Attributes.RESOURCES, Attributes.SOFT_IRQS, "*"); //$NON-NLS-1$
                ResourcesEntry[] softIrqEntries = new ResourcesEntry[softIrqQuarks.size()];
                for (int i = 0; i < softIrqQuarks.size(); i++) {
                    int softIrqQuark = softIrqQuarks.get(i);
                    int softIrq = Integer.parseInt(ssq.getAttributeName(softIrqQuark));
                    ResourcesEntry entry = new ResourcesEntry(softIrqQuark, ctfKernelTrace, Type.SOFT_IRQ, softIrq);
                    groupEntry.addChild(entry);
                    softIrqEntries[i] = entry;
                }
            }
        }
        synchronized (fEntryListMap) {
            fEntryListMap.put(trace, (ArrayList<TraceEntry>) entryList.clone());
        }
        if (trace == fTrace) {
            refresh();
        }
        for (TraceEntry traceEntry : entryList) {
            if (monitor.isCanceled()) {
                return;
            }
            LttngKernelTrace ctfKernelTrace = traceEntry.getTrace();
            ITmfStateSystem ssq = ctfKernelTrace.getStateSystems().get(LttngKernelTrace.STATE_ID);
            long startTime = ssq.getStartTime();
            long endTime = ssq.getCurrentEndTime() + 1;
            long resolution = (endTime - startTime) / fDisplayWidth;
            for (ResourcesEntry entry : traceEntry.getChildren()) {
                List<ITimeEvent> eventList = getEventList(entry, startTime, endTime, resolution, false, monitor);
                entry.setEventList(eventList);
                redraw();
            }
        }
    }

    private static List<ITimeEvent> getEventList(ResourcesEntry entry,
            long startTime, long endTime, long resolution, boolean includeNull,
            IProgressMonitor monitor) {
        ITmfStateSystem ssq = entry.getTrace().getStateSystems().get(LttngKernelTrace.STATE_ID);
        final long realStart = Math.max(startTime, ssq.getStartTime());
        final long realEnd = Math.min(endTime, ssq.getCurrentEndTime() + 1);
        if (realEnd <= realStart) {
            return null;
        }
        List<ITimeEvent> eventList = null;
        int quark = entry.getQuark();
        try {
            if (entry.getType().equals(Type.CPU)) {
                int statusQuark = ssq.getQuarkRelative(quark, Attributes.STATUS);
                List<ITmfStateInterval> statusIntervals = ssq.queryHistoryRange(statusQuark, realStart, realEnd - 1, resolution, monitor);
                eventList = new ArrayList<ITimeEvent>(statusIntervals.size());
                long lastEndTime = -1;
                for (ITmfStateInterval statusInterval : statusIntervals) {
                    if (monitor.isCanceled()) {
                        return null;
                    }
                    int status = statusInterval.getStateValue().unboxInt();
                    long time = statusInterval.getStartTime();
                    long duration = statusInterval.getEndTime() - time + 1;
                    if (!statusInterval.getStateValue().isNull()) {
                        if (lastEndTime != time && lastEndTime != -1) {
                            eventList.add(new TimeEvent(entry, lastEndTime, time - lastEndTime));
                        }
                        eventList.add(new ResourcesEvent(entry, time, duration, status));
                        lastEndTime = time + duration;
                    } else {
                        if (includeNull) {
                            eventList.add(new ResourcesEvent(entry, time, duration));
                        }
                    }
                }
            } else if (entry.getType().equals(Type.IRQ)) {
                List<ITmfStateInterval> irqIntervals = ssq.queryHistoryRange(quark, realStart, realEnd - 1, resolution, monitor);
                eventList = new ArrayList<ITimeEvent>(irqIntervals.size());
                long lastEndTime = -1;
                boolean lastIsNull = true;
                for (ITmfStateInterval irqInterval : irqIntervals) {
                    if (monitor.isCanceled()) {
                        return null;
                    }
                    long time = irqInterval.getStartTime();
                    long duration = irqInterval.getEndTime() - time + 1;
                    if (!irqInterval.getStateValue().isNull()) {
                        int cpu = irqInterval.getStateValue().unboxInt();
                        eventList.add(new ResourcesEvent(entry, time, duration, cpu));
                        lastIsNull = false;
                    } else {
                        if (lastEndTime != time && lastEndTime != -1 && lastIsNull) {
                            eventList.add(new ResourcesEvent(entry, lastEndTime, time - lastEndTime, -1));
                        }
                        if (includeNull) {
                            eventList.add(new ResourcesEvent(entry, time, duration));
                        }
                        lastIsNull = true;
                    }
                    lastEndTime = time + duration;
                }
            } else if (entry.getType().equals(Type.SOFT_IRQ)) {
                List<ITmfStateInterval> softIrqIntervals = ssq.queryHistoryRange(quark, realStart, realEnd - 1, resolution, monitor);
                eventList = new ArrayList<ITimeEvent>(softIrqIntervals.size());
                long lastEndTime = -1;
                boolean lastIsNull = true;
                for (ITmfStateInterval softIrqInterval : softIrqIntervals) {
                    if (monitor.isCanceled()) {
                        return null;
                    }
                    long time = softIrqInterval.getStartTime();
                    long duration = softIrqInterval.getEndTime() - time + 1;
                    if (!softIrqInterval.getStateValue().isNull()) {
                        int cpu = softIrqInterval.getStateValue().unboxInt();
                        eventList.add(new ResourcesEvent(entry, time, duration, cpu));
                    } else {
                        if (lastEndTime != time && lastEndTime != -1 && lastIsNull) {
                            eventList.add(new ResourcesEvent(entry, lastEndTime, time - lastEndTime, -1));
                        }
                        if (includeNull) {
                            eventList.add(new ResourcesEvent(entry, time, duration));
                        }
                        lastIsNull = true;
                    }
                    lastEndTime = time + duration;
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
        return eventList;
    }

    private void refresh() {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphViewer.getControl().isDisposed()) {
                    return;
                }
                ITimeGraphEntry[] entries = null;
                synchronized (fEntryListMap) {
                    fEntryList = fEntryListMap.get(fTrace);
                    if (fEntryList == null) {
                        fEntryList = new ArrayList<TraceEntry>();
                    }
                    entries = fEntryList.toArray(new ITimeGraphEntry[0]);
                }
                if (entries != null) {
                    Arrays.sort(entries, new TraceEntryComparator());
                    fTimeGraphViewer.setInput(entries);
                    fTimeGraphViewer.setTimeBounds(fStartTime, fEndTime);

                    long timestamp = fTrace == null ? 0 : fTraceManager.getCurrentTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                    long startTime = fTrace == null ? 0 : fTraceManager.getCurrentRange().getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                    long endTime = fTrace == null ? 0 : fTraceManager.getCurrentRange().getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                    startTime = Math.max(startTime, fStartTime);
                    endTime = Math.min(endTime, fEndTime);
                    fTimeGraphViewer.setSelectedTime(timestamp, false);
                    fTimeGraphViewer.setStartFinishTime(startTime, endTime);

                    startZoomThread(startTime, endTime);
                }
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
                if (fTimeGraphViewer.getControl().isDisposed()) {
                    return;
                }
                fTimeGraphViewer.getControl().redraw();
                fTimeGraphViewer.getControl().update();
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
        fPreviousResourceAction = fTimeGraphViewer.getPreviousItemAction();
        fPreviousResourceAction.setText(Messages.ResourcesView_previousResourceActionNameText);
        fPreviousResourceAction.setToolTipText(Messages.ResourcesView_previousResourceActionToolTipText);
        fNextResourceAction = fTimeGraphViewer.getNextItemAction();
        fNextResourceAction.setText(Messages.ResourcesView_nextResourceActionNameText);
        fNextResourceAction.setToolTipText(Messages.ResourcesView_previousResourceActionToolTipText);
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(fTimeGraphViewer.getShowLegendAction());
        manager.add(new Separator());
        manager.add(fTimeGraphViewer.getResetScaleAction());
        manager.add(fTimeGraphViewer.getPreviousEventAction());
        manager.add(fTimeGraphViewer.getNextEventAction());
        manager.add(fPreviousResourceAction);
        manager.add(fNextResourceAction);
        manager.add(fTimeGraphViewer.getZoomInAction());
        manager.add(fTimeGraphViewer.getZoomOutAction());
        manager.add(new Separator());
    }
}
