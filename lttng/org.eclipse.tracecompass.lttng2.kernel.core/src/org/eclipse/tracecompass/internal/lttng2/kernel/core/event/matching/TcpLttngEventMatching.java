/*******************************************************************************
 * Copyright (c) 2013, 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *   Patrick Tasse - Remove getSubField
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.event.matching;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.matching.IEventMatchingKey;
import org.eclipse.tracecompass.tmf.core.event.matching.ITmfMatchEventDefinition;
import org.eclipse.tracecompass.tmf.core.event.matching.TcpEventKey;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventMatching.Direction;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTraceWithPreDefinedEvents;
import org.eclipse.tracecompass.tmf.core.trace.TmfEventTypeCollectionHelper;

/**
 * Class to match tcp events. They use the main kernel's tracepoints
 * netif_receive_skb and net_dev_queue to check if they have a TCP header and
 * use the sequence, acknowledge and flags fields to match packets
 *
 * @author Geneviève Bastien
 */
public class TcpLttngEventMatching implements ITmfMatchEventDefinition {

    private static final Map<IKernelAnalysisEventLayout, Set<String>> REQUIRED_EVENTS = new HashMap<>();

    /** Use a weak hash map so that traces can be garbage collected */
    private static final Map<ITmfTrace, IKernelAnalysisEventLayout> TRACE_LAYOUTS = new WeakHashMap<>();

    @Override
    public boolean canMatchTrace(ITmfTrace trace) {
        // Get the events that this trace needs to have
        if (!(trace instanceof IKernelTrace)) {
            // Not a kernel trace, we cannot know what events to use, return
            // false
            return false;
        }
        IKernelAnalysisEventLayout layout = ((IKernelTrace) trace).getKernelEventLayout();
        TRACE_LAYOUTS.put(trace, layout);

        Set<String> events = REQUIRED_EVENTS.computeIfAbsent(layout, eventLayout -> {
            Set<String> eventsSet = new HashSet<>();
            eventsSet.addAll(eventLayout.eventsNetworkSend());
            eventsSet.addAll(eventLayout.eventsNetworkReceive());
            return eventsSet;
        });

        if (!(trace instanceof ITmfTraceWithPreDefinedEvents)) {
            // No predefined events, suppose events are present
            return true;
        }
        ITmfTraceWithPreDefinedEvents ktrace = (ITmfTraceWithPreDefinedEvents) trace;

        Set<String> traceEvents = TmfEventTypeCollectionHelper.getEventNames(ktrace.getContainedEventTypes());
        traceEvents.retainAll(events);
        return !traceEvents.isEmpty();
    }

    /**
     * @since 1.0
     */
    @Override
    public Direction getDirection(ITmfEvent event) {
        IKernelAnalysisEventLayout layout = TRACE_LAYOUTS.get(event.getTrace());
        if (layout == null) {
            return null;
        }
        String evname = event.getName();
        /* Is the event a tcp socket in or out event */
        if (layout.eventsNetworkReceive().contains(evname)) {
            return Direction.EFFECT;
        } else if (layout.eventsNetworkSend().contains(evname)) {
            return Direction.CAUSE;
        }
        return null;
    }

    @Override
    public IEventMatchingKey getEventKey(ITmfEvent event) {
        IKernelAnalysisEventLayout layout = TRACE_LAYOUTS.get(event.getTrace());
        if (layout == null) {
            return null;
        }

        TmfEventField content = (TmfEventField) event.getContent();

        Long sequence = content.getFieldValue(Long.class, layout.fieldPathTcpSeq());
        Long ack = content.getFieldValue(Long.class, layout.fieldPathTcpAckSeq());
        Long flags = content.getFieldValue(Long.class, layout.fieldPathTcpFlags());

        if (sequence == null || ack == null || flags == null) {
            return null;
        }

        IEventMatchingKey key = new TcpEventKey(sequence, ack, flags);
        return key;

    }

}
