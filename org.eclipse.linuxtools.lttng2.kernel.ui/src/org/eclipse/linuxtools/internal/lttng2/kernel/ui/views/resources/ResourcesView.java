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
 *   Geneviève Bastien - Move code to provide base classes for time graph views
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.Attributes;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.Messages;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources.ResourcesEntry.Type;
import org.eclipse.linuxtools.lttng2.kernel.core.analysis.LttngKernelAnalysisModule;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

/**
 * Main implementation for the LTTng 2.0 kernel Resource view
 *
 * @author Patrick Tasse
 */
public class ResourcesView extends AbstractTimeGraphView {

    /** View ID. */
    public static final String ID = "org.eclipse.linuxtools.lttng2.kernel.ui.views.resources"; //$NON-NLS-1$

    private static final String[] FILTER_COLUMN_NAMES = new String[] {
            Messages.ResourcesView_stateTypeName
    };

    // Timeout between updates in the build thread in ms
    private static final long BUILD_UPDATE_TIMEOUT = 500;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public ResourcesView() {
        super(ID, new ResourcesPresentationProvider());
        setFilterColumns(FILTER_COLUMN_NAMES);
    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    @Override
    protected String getNextText() {
        return Messages.ResourcesView_nextResourceActionNameText;
    }

    @Override
    protected String getNextTooltip() {
        return Messages.ResourcesView_nextResourceActionToolTipText;
    }

    @Override
    protected String getPrevText() {
        return Messages.ResourcesView_previousResourceActionNameText;
    }

    @Override
    protected String getPrevTooltip() {
        return Messages.ResourcesView_previousResourceActionToolTipText;
    }

    @Override
    protected void buildEventList(ITmfTrace trace, ITmfTrace parentTrace, IProgressMonitor monitor) {
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

        Map<Integer, ResourcesEntry> entryMap = new HashMap<>();
        TimeGraphEntry traceEntry = null;

        long startTime = ssq.getStartTime();
        long start = startTime;
        setStartTime(Math.min(getStartTime(), startTime));
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
            long endTime = end + 1;
            setEndTime(Math.max(getEndTime(), endTime));

            if (traceEntry == null) {
                traceEntry = new ResourcesEntry(trace, trace.getName(), startTime, endTime, 0);
                List<TimeGraphEntry> entryList = Collections.singletonList(traceEntry);
                addToEntryList(parentTrace, entryList);
            } else {
                traceEntry.updateEndTime(endTime);
            }

            List<Integer> cpuQuarks = ssq.getQuarks(Attributes.CPUS, "*"); //$NON-NLS-1$
            for (Integer cpuQuark : cpuQuarks) {
                int cpu = Integer.parseInt(ssq.getAttributeName(cpuQuark));
                ResourcesEntry entry = entryMap.get(cpuQuark);
                if (entry == null) {
                    entry = new ResourcesEntry(cpuQuark, trace, startTime, endTime, Type.CPU, cpu);
                    entryMap.put(cpuQuark, entry);
                    traceEntry.addChild(entry);
                } else {
                    entry.updateEndTime(endTime);
                }
            }
            List<Integer> irqQuarks = ssq.getQuarks(Attributes.RESOURCES, Attributes.IRQS, "*"); //$NON-NLS-1$
            for (Integer irqQuark : irqQuarks) {
                int irq = Integer.parseInt(ssq.getAttributeName(irqQuark));
                ResourcesEntry entry = entryMap.get(irqQuark);
                if (entry == null) {
                    entry = new ResourcesEntry(irqQuark, trace, startTime, endTime, Type.IRQ, irq);
                    entryMap.put(irqQuark, entry);
                    traceEntry.addChild(entry);
                } else {
                    entry.updateEndTime(endTime);
                }
            }
            List<Integer> softIrqQuarks = ssq.getQuarks(Attributes.RESOURCES, Attributes.SOFT_IRQS, "*"); //$NON-NLS-1$
            for (Integer softIrqQuark : softIrqQuarks) {
                int softIrq = Integer.parseInt(ssq.getAttributeName(softIrqQuark));
                ResourcesEntry entry = entryMap.get(softIrqQuark);
                if (entry == null) {
                    entry = new ResourcesEntry(softIrqQuark, trace, startTime, endTime, Type.SOFT_IRQ, softIrq);
                    entryMap.put(softIrqQuark, entry);
                    traceEntry.addChild(entry);
                } else {
                    entry.updateEndTime(endTime);
                }
            }

            if (parentTrace.equals(getTrace())) {
                refresh();
            }
            long resolution = Math.max(1, (endTime - ssq.getStartTime()) / getDisplayWidth());
            for (TimeGraphEntry entry : traceEntry.getChildren()) {
                if (monitor.isCanceled()) {
                    return;
                }
                List<ITimeEvent> eventList = getEventList(entry, start, endTime, resolution, monitor);
                if (eventList != null) {
                    for (ITimeEvent event : eventList) {
                        entry.addEvent(event);
                    }
                }
                redraw();
            }

            start = end;
        }
    }

    @Override
    protected List<ITimeEvent> getEventList(TimeGraphEntry entry,
            long startTime, long endTime, long resolution,
            IProgressMonitor monitor) {
        ResourcesEntry resourcesEntry = (ResourcesEntry) entry;
        LttngKernelAnalysisModule module = resourcesEntry.getTrace().getAnalysisModuleOfClass(LttngKernelAnalysisModule.class, LttngKernelAnalysisModule.ID);
        if (module == null) {
            return null;
        }
        ITmfStateSystem ssq = module.getStateSystem();
        if (ssq == null) {
            return null;
        }
        final long realStart = Math.max(startTime, ssq.getStartTime());
        final long realEnd = Math.min(endTime, ssq.getCurrentEndTime() + 1);
        if (realEnd <= realStart) {
            return null;
        }
        List<ITimeEvent> eventList = null;
        int quark = resourcesEntry.getQuark();

        try {
            if (resourcesEntry.getType().equals(Type.CPU)) {
                int statusQuark = ssq.getQuarkRelative(quark, Attributes.STATUS);
                List<ITmfStateInterval> statusIntervals = ssq.queryHistoryRange(statusQuark, realStart, realEnd - 1, resolution, monitor);
                eventList = new ArrayList<>(statusIntervals.size());
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
                        eventList.add(new TimeEvent(entry, time, duration, status));
                    } else if (lastEndTime == -1 || time + duration >= endTime) {
                        // add null event if it intersects the start or end time
                        eventList.add(new NullTimeEvent(entry, time, duration));
                    }
                    lastEndTime = time + duration;
                }
            } else if (resourcesEntry.getType().equals(Type.IRQ)) {
                List<ITmfStateInterval> irqIntervals = ssq.queryHistoryRange(quark, realStart, realEnd - 1, resolution, monitor);
                eventList = new ArrayList<>(irqIntervals.size());
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
                        eventList.add(new TimeEvent(entry, time, duration, cpu));
                        lastIsNull = false;
                    } else {
                        if (lastEndTime == -1) {
                            // add null event if it intersects the start time
                            eventList.add(new NullTimeEvent(entry, time, duration));
                        } else {
                            if (lastEndTime != time && lastIsNull) {
                                /* This is a special case where we want to show IRQ_ACTIVE state but we don't know the CPU (it is between two null samples) */
                                eventList.add(new TimeEvent(entry, lastEndTime, time - lastEndTime, -1));
                            }
                            if (time + duration >= endTime) {
                                // add null event if it intersects the end time
                                eventList.add(new NullTimeEvent(entry, time, duration));
                            }
                        }
                        lastIsNull = true;
                    }
                    lastEndTime = time + duration;
                }
            } else if (resourcesEntry.getType().equals(Type.SOFT_IRQ)) {
                List<ITmfStateInterval> softIrqIntervals = ssq.queryHistoryRange(quark, realStart, realEnd - 1, resolution, monitor);
                eventList = new ArrayList<>(softIrqIntervals.size());
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
                        eventList.add(new TimeEvent(entry, time, duration, cpu));
                    } else {
                        if (lastEndTime == -1) {
                            // add null event if it intersects the start time
                            eventList.add(new NullTimeEvent(entry, time, duration));
                        } else {
                            if (lastEndTime != time && lastIsNull) {
                                /* This is a special case where we want to show IRQ_ACTIVE state but we don't know the CPU (it is between two null samples) */
                                eventList.add(new TimeEvent(entry, lastEndTime, time - lastEndTime, -1));
                            }
                            if (time + duration >= endTime) {
                                // add null event if it intersects the end time
                                eventList.add(new NullTimeEvent(entry, time, duration));
                            }
                        }
                        lastIsNull = true;
                    }
                    lastEndTime = time + duration;
                }
            }

        } catch (AttributeNotFoundException | TimeRangeException | StateValueTypeException e) {
            e.printStackTrace();
        } catch (StateSystemDisposedException e) {
            /* Ignored */
        }
        return eventList;
    }

}
