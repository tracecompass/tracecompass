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
 *   Geneviève Bastien - Move code to provide base classes for time graph views
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.Attributes;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.Messages;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources.ResourcesEntry.Type;
import org.eclipse.linuxtools.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.linuxtools.lttng2.kernel.ui.analysis.LttngKernelAnalysisModule;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
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

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    @Override
    protected void buildEventList(ITmfTrace trace, IProgressMonitor monitor) {
        setStartTime(Long.MAX_VALUE);
        setEndTime(Long.MIN_VALUE);

        ArrayList<ResourcesEntry> entryList = new ArrayList<>();
        for (ITmfTrace aTrace : TmfTraceManager.getTraceSet(trace)) {
            if (monitor.isCanceled()) {
                return;
            }
            if (aTrace instanceof LttngKernelTrace) {
                LttngKernelTrace lttngKernelTrace = (LttngKernelTrace) aTrace;
                LttngKernelAnalysisModule module = lttngKernelTrace.getAnalysisModules(LttngKernelAnalysisModule.class).get(LttngKernelAnalysisModule.ID);
                module.schedule();
                if (!module.waitForCompletion(new NullProgressMonitor())) {
                    continue;
                }
                ITmfStateSystem ssq = module.getStateSystem();
                if (ssq == null) {
                    continue;
                }
                ssq.waitUntilBuilt();
                if (ssq.isCancelled()) {
                    continue;
                }
                long startTime = ssq.getStartTime();
                long endTime = ssq.getCurrentEndTime() + 1;
                ResourcesEntry groupEntry = new ResourcesEntry(lttngKernelTrace, aTrace.getName(), startTime, endTime, 0);
                entryList.add(groupEntry);
                setStartTime(Math.min(getStartTime(), startTime));
                setEndTime(Math.max(getEndTime(), endTime));
                List<Integer> cpuQuarks = ssq.getQuarks(Attributes.CPUS, "*"); //$NON-NLS-1$
                ResourcesEntry[] cpuEntries = new ResourcesEntry[cpuQuarks.size()];
                for (int i = 0; i < cpuQuarks.size(); i++) {
                    int cpuQuark = cpuQuarks.get(i);
                    int cpu = Integer.parseInt(ssq.getAttributeName(cpuQuark));
                    ResourcesEntry entry = new ResourcesEntry(cpuQuark, lttngKernelTrace, getStartTime(), getEndTime(), Type.CPU, cpu);
                    groupEntry.addChild(entry);
                    cpuEntries[i] = entry;
                }
                List<Integer> irqQuarks = ssq.getQuarks(Attributes.RESOURCES, Attributes.IRQS, "*"); //$NON-NLS-1$
                ResourcesEntry[] irqEntries = new ResourcesEntry[irqQuarks.size()];
                for (int i = 0; i < irqQuarks.size(); i++) {
                    int irqQuark = irqQuarks.get(i);
                    int irq = Integer.parseInt(ssq.getAttributeName(irqQuark));
                    ResourcesEntry entry = new ResourcesEntry(irqQuark, lttngKernelTrace, getStartTime(), getEndTime(), Type.IRQ, irq);
                    groupEntry.addChild(entry);
                    irqEntries[i] = entry;
                }
                List<Integer> softIrqQuarks = ssq.getQuarks(Attributes.RESOURCES, Attributes.SOFT_IRQS, "*"); //$NON-NLS-1$
                ResourcesEntry[] softIrqEntries = new ResourcesEntry[softIrqQuarks.size()];
                for (int i = 0; i < softIrqQuarks.size(); i++) {
                    int softIrqQuark = softIrqQuarks.get(i);
                    int softIrq = Integer.parseInt(ssq.getAttributeName(softIrqQuark));
                    ResourcesEntry entry = new ResourcesEntry(softIrqQuark, lttngKernelTrace, getStartTime(), getEndTime(), Type.SOFT_IRQ, softIrq);
                    groupEntry.addChild(entry);
                    softIrqEntries[i] = entry;
                }
            }
        }
        putEntryList(trace, new ArrayList<TimeGraphEntry>(entryList));

        if (trace.equals(getTrace())) {
            refresh();
        }
        for (ResourcesEntry traceEntry : entryList) {
            if (monitor.isCanceled()) {
                return;
            }
            LttngKernelTrace lttngKernelTrace = traceEntry.getTrace();
            LttngKernelAnalysisModule module = lttngKernelTrace.getAnalysisModules(LttngKernelAnalysisModule.class).get(LttngKernelAnalysisModule.ID);
            ITmfStateSystem ssq = module.getStateSystem();
            if (ssq == null) {
                continue;
            }
            long startTime = ssq.getStartTime();
            long endTime = ssq.getCurrentEndTime() + 1;
            long resolution = (endTime - startTime) / getDisplayWidth();
            for (TimeGraphEntry entry : traceEntry.getChildren()) {
                List<ITimeEvent> eventList = getEventList(entry, startTime, endTime, resolution, monitor);
                entry.setEventList(eventList);
                redraw();
            }
        }
    }

    @Override
    protected List<ITimeEvent> getEventList(TimeGraphEntry entry,
            long startTime, long endTime, long resolution,
            IProgressMonitor monitor) {
        ResourcesEntry resourcesEntry = (ResourcesEntry) entry;
        LttngKernelAnalysisModule module = resourcesEntry.getTrace().getAnalysisModules(LttngKernelAnalysisModule.class).get(LttngKernelAnalysisModule.ID);
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

}
