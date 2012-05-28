/*******************************************************************************
 * Copyright (c) 2012 Ericsson
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
import org.eclipse.linuxtools.lttng2.kernel.core.trace.CtfKernelTrace;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateSystemQuerier;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphRangeListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphTimeListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphRangeUpdateEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphTimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphViewer;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;

public class ResourcesView extends TmfView {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * View ID.
     */
    public static final String ID = "org.eclipse.linuxtools.lttng2.kernel.ui.views.resources"; //$NON-NLS-1$

    /**
     * Initial time range
     */
    private static final long INITIAL_WINDOW_OFFSET = (1L * 100  * 1000 * 1000); // .1sec

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    // The time graph viewer
    TimeGraphViewer fTimeGraphViewer;

    // The selected experiment
    private TmfExperiment<ITmfEvent> fSelectedExperiment;

    // The time graph entry list
    private ArrayList<TraceEntry> fEntryList;

    // The start time
    private long fStartTime;

    // The end time
    private long fEndTime;

    // The display width
    private int fDisplayWidth;

    // The next resource action
    private Action fNextResourceAction;

    // The previous resource action
    private Action fPreviousResourceAction;

    // The zoom thread
    private ZoomThread fZoomThread;

    // ------------------------------------------------------------------------
    // Classes
    // ------------------------------------------------------------------------

    private class TraceEntry implements ITimeGraphEntry {
        private CtfKernelTrace fTrace;
        public ArrayList<ResourcesEntry> fChildren;
        public String fName;

        public TraceEntry(CtfKernelTrace trace, String name) {
            fTrace = trace;
            fChildren = new ArrayList<ResourcesEntry>();
            fName = name;
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
        public ResourcesEntry[] getChildren() {
            return fChildren.toArray(new ResourcesEntry[0]);
        }

        @Override
        public String getName() {
            return fName;
        }

        @Override
        public long getStartTime() {
            return -1;
        }

        @Override
        public long getEndTime() {
            return -1;
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

        public CtfKernelTrace getTrace() {
            return fTrace;
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

    private class ZoomThread extends Thread {
        private long fZoomStartTime;
        private long fZoomEndTime;
        private IProgressMonitor fMonitor;

        public ZoomThread(long startTime, long endTime) {
            super("ResourcesView zoom"); //$NON-NLS-1$
            fZoomStartTime = startTime;
            fZoomEndTime = endTime;
            fMonitor = new NullProgressMonitor();
        }

        @Override
        public void run() {
            ArrayList<TraceEntry> entryList = fEntryList;
            if (entryList == null) {
                return;
            }
            long resolution = Math.max(1, (fZoomEndTime - fZoomStartTime) / fDisplayWidth);
            for (TraceEntry traceEntry : entryList) {
                for (ITimeGraphEntry child : traceEntry.getChildren()) {
                    ResourcesEntry entry = (ResourcesEntry) child;
                    if (fMonitor.isCanceled()) {
                        break;
                    }
                    List<ITimeEvent> zoomedEventList = getEventList(entry, fZoomStartTime, fZoomEndTime, resolution, true, fMonitor);
                    if (fMonitor.isCanceled()) {
                        break;
                    }
                    entry.setZoomedEventList(zoomedEventList);
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

    public ResourcesView() {
        super(ID);
        fDisplayWidth = Display.getDefault().getBounds().width;
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.TmfView#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        fTimeGraphViewer = new TimeGraphViewer(parent, SWT.NONE);

        fTimeGraphViewer.setTimeGraphProvider(new ResourcesPresentationProvider());

        fTimeGraphViewer.setTimeCalendarFormat(true);

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

        final Thread thread = new Thread("ResourcesView build") { //$NON-NLS-1$
            @Override
            public void run() {
                if (TmfExperiment.getCurrentExperiment() != null) {
                    selectExperiment(TmfExperiment.getCurrentExperiment());
                }
            }
        };
        thread.start();

        // View Action Handling
        makeActions();
        contributeToActionBars();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        fTimeGraphViewer.setFocus();
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    @TmfSignalHandler
    public void experimentSelected(final TmfExperimentSelectedSignal<? extends TmfEvent> signal) {
        if (signal.getExperiment().equals(fSelectedExperiment)) {
            return;
        }

        final Thread thread = new Thread("ResourcesView build") { //$NON-NLS-1$
            @Override
            public void run() {
                selectExperiment(signal.getExperiment());
            }
        };
        thread.start();
    }

    @TmfSignalHandler
    public void synchToTime(final TmfTimeSynchSignal signal) {
        if (signal.getSource() == this) {
            return;
        }
        final long time = signal.getCurrentTime().normalize(0, -9).getValue();
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphViewer.getControl().isDisposed()) {
                    return;
                }
                fTimeGraphViewer.setSelectedTime(time, true);
            }
        });
    }

    @TmfSignalHandler
    public void synchToRange(final TmfRangeSynchSignal signal) {
        if (signal.getSource() == this) {
            return;
        }
        final long startTime = signal.getCurrentRange().getStartTime().normalize(0, -9).getValue();
        final long endTime = signal.getCurrentRange().getEndTime().normalize(0, -9).getValue();
        final long time = signal.getCurrentTime().normalize(0, -9).getValue();
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

    @SuppressWarnings("unchecked")
    private void selectExperiment(TmfExperiment<?> experiment) {
        fStartTime = Long.MAX_VALUE;
        fEndTime = Long.MIN_VALUE;
        fSelectedExperiment = (TmfExperiment<ITmfEvent>) experiment;
        ArrayList<TraceEntry> entryList = new ArrayList<TraceEntry>();
        for (ITmfTrace<?> trace : experiment.getTraces()) {
            if (trace instanceof CtfKernelTrace) {
                CtfKernelTrace ctfKernelTrace = (CtfKernelTrace) trace;
                TraceEntry groupEntry = new TraceEntry(ctfKernelTrace, trace.getName());
                entryList.add(groupEntry);
                IStateSystemQuerier ssq = ctfKernelTrace.getStateSystem();
                long startTime = ssq.getStartTime();
                long endTime = ssq.getCurrentEndTime() + 1;
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
        fEntryList = entryList;
        refresh(INITIAL_WINDOW_OFFSET);
        for (TraceEntry traceEntry : fEntryList) {
            CtfKernelTrace ctfKernelTrace = ((TraceEntry) traceEntry).getTrace();
            IStateSystemQuerier ssq = ctfKernelTrace.getStateSystem();
            long startTime = ssq.getStartTime();
            long endTime = ssq.getCurrentEndTime() + 1;
            long resolution = (endTime - startTime) / fDisplayWidth;
            for (ResourcesEntry entry : traceEntry.getChildren()) {
                List<ITimeEvent> eventList = getEventList(entry, startTime, endTime, resolution, false, new NullProgressMonitor());
                entry.setEventList(eventList);
                redraw();
            }
        }
    }

    private List<ITimeEvent> getEventList(ResourcesEntry entry, long startTime, long endTime, long resolution, boolean includeNull, IProgressMonitor monitor) {
        if (endTime <= startTime) {
            return null;
        }
        IStateSystemQuerier ssq = entry.getTrace().getStateSystem();
        List<ITimeEvent> eventList = null;
        int quark = entry.getQuark();
        try {
            if (entry.getType().equals(Type.CPU)) {
                int statusQuark = ssq.getQuarkRelative(quark, Attributes.STATUS);
                List<ITmfStateInterval> statusIntervals = ssq.queryHistoryRange(statusQuark, startTime, endTime - 1, resolution);
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
                List<ITmfStateInterval> irqIntervals = ssq.queryHistoryRange(quark, startTime, endTime - 1, resolution);
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
                List<ITmfStateInterval> softIrqIntervals = ssq.queryHistoryRange(quark, startTime, endTime - 1, resolution);
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
        }
        return eventList;
    }

    private void refresh(final long windowRange) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphViewer.getControl().isDisposed()) {
                    return;
                }
                ITimeGraphEntry[] entries = fEntryList.toArray(new ITimeGraphEntry[0]);
                Arrays.sort(entries);
                fTimeGraphViewer.setInput(entries);
                fTimeGraphViewer.setTimeBounds(fStartTime, fEndTime);

                long endTime = fStartTime + windowRange;

                if (fEndTime < endTime) {
                    endTime = fEndTime;
                }
                fTimeGraphViewer.setStartFinishTime(fStartTime, endTime);

                startZoomThread(fStartTime, endTime);
            }
        });
    }


    private void redraw() {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphViewer.getControl().isDisposed()) {
                    return;
                }
                fTimeGraphViewer.getControl().redraw();
                fTimeGraphViewer.getControl().update();
            }
        });
    }

    private void startZoomThread(long startTime, long endTime) {
        if (fZoomThread != null) {
            fZoomThread.cancel();
        }
        fZoomThread = new ZoomThread(startTime, endTime);
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
