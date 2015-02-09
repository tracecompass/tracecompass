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
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.building.LttngKernelExecGraphProvider;
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
    private Map<ITmfTrace, Boolean> fHasEventSchedTTWU;

    BaseHandler(LttngKernelExecGraphProvider provider) {
        fProvider = provider;
        fHasEventSchedTTWU = new HashMap<>();
        ITmfTrace trace = getProvider().getTrace();
        LttngEventLayout layout = getProvider().getEventLayout();
        Collection<ITmfTrace> traceSet = TmfTraceManager.getTraceSet(trace);
        for (ITmfTrace traceItem : traceSet) {
            if (traceItem instanceof ITmfTraceWithPreDefinedEvents) {
                Set<? extends ITmfEventType> content = ((ITmfTraceWithPreDefinedEvents) traceItem).getContainedEventTypes();
                Set<String> traceEvents = TmfEventTypeCollectionHelper.getEventNames(content);
                boolean hasEvent = traceEvents.contains(layout.eventSchedProcessTTWU());
                fHasEventSchedTTWU.put(traceItem, hasEvent);
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
     * Return if the trace has the try to wake-up event
     *
     * @param trace
     *            the trace to check
     * @return if the trace has the try to wake-up event
     */
    public boolean traceHasEventSchedTTWU(ITmfTrace trace) {
        Boolean ret = fHasEventSchedTTWU.get(trace);
        if (ret == null) {
            return false;
        }
        return ret;
    }

    @Override
    public void handleEvent(ITmfEvent event) {
    }

}
