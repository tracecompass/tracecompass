/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.handlers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.tracecompass.analysis.graph.core.building.AbstractTraceEventHandler;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.building.LttngKernelExecGraphProvider;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout.Lttng27EventLayout;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout.Lttng28EventLayout;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout.LttngEventLayout;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTraceWithPreDefinedEvents;
import org.eclipse.tracecompass.tmf.core.trace.TmfEventTypeCollectionHelper;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;

/**
 * Holds the fields commons to all handler classes.
 *
 * @author Francis Giraldeau <francis.giraldeau@gmail.com>
 *
 */
public class BaseHandler extends AbstractTraceEventHandler {

    private final LttngKernelExecGraphProvider fProvider;

    /**
     * The fWakeupEventMap is populated with the prefered wake-up event for a
     * given trace. Here is the possible values for LTTng, that are in priority
     * order:
     *
     * <pre>
     * - linux all versions and lttng all versions: sched_ttwu
     * - linux >= 4.3 and lttng >= 2.8: sched_waking + sched_wakeup_new
     * - linux < 3.8.0: sched_wakeup + sched_wakeup_new
     * </pre>
     *
     * The acronym ttwu stands for "Try To Wake Up". The sched_ttwu requires
     * lttng-modules addons and works with all versions of linux and LTTng. This
     * event alone is sufficient, because it a placeholder for both sched_wakeup
     * and sched_wakeup_new. The event sched_wakeup_new is a special case, and
     * is emitted when a process is created and is scheduled for the first time.
     * Therefore, we use in priority sched_ttwu if available, then sched_waking
     * and as a last resort, we use sched_wakup, but this last option will work
     * only for older kernels.
     *
     * If the trace has more than one type of wake-up event enabled, we ensure
     * that only one type will be processed.
     */
    private Map<ITmfTrace, String> fWakeupEventMap;

    BaseHandler(LttngKernelExecGraphProvider provider) {
        fProvider = provider;
        fWakeupEventMap = new HashMap<>();
        ITmfTrace trace = getProvider().getTrace();
        Collection<ITmfTrace> traceSet = TmfTraceManager.getTraceSet(trace);
        for (ITmfTrace traceItem : traceSet) {
            IKernelAnalysisEventLayout layout = getProvider().getEventLayout(traceItem);
            if (traceItem instanceof ITmfTraceWithPreDefinedEvents) {
                Set<? extends ITmfEventType> content = ((ITmfTraceWithPreDefinedEvents) traceItem).getContainedEventTypes();
                Set<String> traceEvents = TmfEventTypeCollectionHelper.getEventNames(content);

                /* default wake-up event */
                String wakeupEvent = layout.eventSchedProcessWakeup();

                /*
                 * FIXME: downcast in the client should be avoided
                 */
                if (layout instanceof LttngEventLayout) {
                    LttngEventLayout lttngLayout = (LttngEventLayout) layout;
                    if (traceEvents.contains(lttngLayout.eventSchedProcessTTWU())) {
                        /* use sched_ttwu if available */
                        wakeupEvent = lttngLayout.eventSchedProcessTTWU();
                    } else if (layout instanceof Lttng28EventLayout) {
                        /* use sched_waking if available */
                        Lttng28EventLayout layout28 = (Lttng28EventLayout) layout;
                        if (traceEvents.contains(layout28.eventSchedProcessWaking())) {
                            wakeupEvent = layout28.eventSchedProcessWaking();
                        }
                    }
                }
                fWakeupEventMap.put(traceItem, wakeupEvent);
            }
        }
    }

    /**
     * Returns the parent graph provider
     *
     * @return the graph provider
     */
    public LttngKernelExecGraphProvider getProvider() {
        return fProvider;
    }

    /**
     * Return if this event is a wake-up event for this trace
     *
     * @param event
     *            the trace to check
     * @return true if this is a wake-up event to process, false otherwise
     */
    public boolean isWakeupEvent(ITmfEvent event) {
        String eventName = event.getName();
        ITmfTrace trace = event.getTrace();
        IKernelAnalysisEventLayout eventLayout = getProvider().getEventLayout(event.getTrace());
        String wakeupEventName = NonNullUtils.nullToEmptyString(fWakeupEventMap.get(trace));

        /* First, check if sched_ttwu is the current wake-up event for this trace */
        if (eventLayout instanceof LttngEventLayout) {
            LttngEventLayout layoutDefault = (LttngEventLayout) eventLayout;
            if (wakeupEventName.equals(layoutDefault.eventSchedProcessTTWU())) {
                return eventName.equals(layoutDefault.eventSchedProcessTTWU());
            }
        }

        /* Fall back to built-in sched_waking and sched_wakeup_new */
        if (eventLayout instanceof Lttng28EventLayout) {
            Lttng28EventLayout layout28 = (Lttng28EventLayout) eventLayout;
            if (wakeupEventName.equals(layout28.eventSchedProcessWaking())) {
                return (eventName.equals(layout28.eventSchedProcessWaking()) ||
                        eventName.equals(layout28.eventSchedProcessWakeupNew()));
            }
        }

        /* Legacy support using built-in sched_wakeup and sched_wakeup_new */
        if (eventLayout instanceof LttngEventLayout) {
            LttngEventLayout layoutDefault = (LttngEventLayout) eventLayout;
            if (wakeupEventName.equals(layoutDefault.eventSchedProcessWakeup())) {
                return (eventName.equals(layoutDefault.eventSchedProcessWakeup()) ||
                        eventName.equals(layoutDefault.eventSchedProcessWakeupNew()));
            }
        }
        return false;
    }

    /**
     * Return true if this event is an IPI entry
     *
     * @param event
     *            the event
     * @return true of this is an IPI entry, false otherwise
     */
    public boolean isIpiEntry(ITmfEvent event) {
        return layoutContainsEvent(event, true);
    }

    /**
     * Return true if this event is an IPI exit
     *
     * @param event
     *            the event
     * @return true of this is an IPI exit, false otherwise
     */
    public boolean isIpiExit(ITmfEvent event) {
        return layoutContainsEvent(event, false);
    }

    private boolean layoutContainsEvent(ITmfEvent event, boolean entry) {
        String eventName = event.getName();
        ITmfTrace trace = event.getTrace();
        IKernelAnalysisEventLayout layout = getProvider().getEventLayout(trace);
        /* awkward downcast */
        if (layout instanceof Lttng27EventLayout) {
            Lttng27EventLayout layout27 = (Lttng27EventLayout) layout;
            if (entry) {
                return layout27.getX86IrqVectorsEntry().contains(eventName);
            }
            return layout27.getX86IrqVectorsExit().contains(eventName);
        }
        return false;
    }

    @Override
    public void handleEvent(ITmfEvent event) {
    }

}
