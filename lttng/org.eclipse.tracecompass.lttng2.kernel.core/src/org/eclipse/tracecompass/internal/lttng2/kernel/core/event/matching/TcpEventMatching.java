/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.event.matching;

import java.util.Set;

import org.eclipse.tracecompass.internal.lttng2.kernel.core.TcpEventStrings;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.matching.IEventMatchingKey;
import org.eclipse.tracecompass.tmf.core.event.matching.ITmfMatchEventDefinition;
import org.eclipse.tracecompass.tmf.core.event.matching.TcpEventKey;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventMatching.Direction;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTraceWithPreDefinedEvents;
import org.eclipse.tracecompass.tmf.core.trace.TmfEventTypeCollectionHelper;

import com.google.common.collect.ImmutableSet;

/**
 * Class to match tcp type events. This matching class applies to traces
 * obtained with the 'addons' lttng module. This module can be obtained with
 * lttng-modules to generate traces at
 * https://github.com/giraldeau/lttng-modules/tree/addons
 *
 * Note: this module only allows to generate traces to be read and analyzed by
 * TMF, no code from this module is being used here
 *
 * @author Geneviève Bastien
 */
public class TcpEventMatching implements ITmfMatchEventDefinition {

    private static final ImmutableSet<String> REQUIRED_EVENTS = ImmutableSet.of(
            TcpEventStrings.INET_SOCK_LOCAL_IN,
            TcpEventStrings.INET_SOCK_LOCAL_OUT);

    private static boolean canMatchPacket(final ITmfEvent event) {
        /* Make sure all required fields are present to match with this event */
        ITmfEventField content = event.getContent();
        return !((content.getFieldValue(Long.class, TcpEventStrings.SEQ) == null) ||
                (content.getFieldValue(Long.class, TcpEventStrings.ACKSEQ) == null) ||
                (content.getFieldValue(Long.class, TcpEventStrings.FLAGS) == null));
    }

    /**
     * @since 1.0
     */
    @Override
    public Direction getDirection(ITmfEvent event) {
        String evname = event.getName();

        if (!canMatchPacket(event)) {
            return null;
        }

        /* Is the event a tcp socket in or out event */
        if (evname.equals(TcpEventStrings.INET_SOCK_LOCAL_IN)) {
            return Direction.EFFECT;
        } else if (evname.equals(TcpEventStrings.INET_SOCK_LOCAL_OUT)) {
            return Direction.CAUSE;
        }
        return null;
    }

    @Override
    public IEventMatchingKey getEventKey(ITmfEvent event) {
        ITmfEventField content = event.getContent();
        Long sequence = content.getFieldValue(Long.class, TcpEventStrings.SEQ);
        Long ack = content.getFieldValue(Long.class, TcpEventStrings.ACKSEQ);
        Long flags = content.getFieldValue(Long.class, TcpEventStrings.FLAGS);

        if (sequence == null || ack == null || flags == null) {
            /* Should have been caught by canMatchPacket() above. */
            throw new IllegalArgumentException("Event does not have expected fields"); //$NON-NLS-1$
        }

        IEventMatchingKey key = new TcpEventKey(sequence, ack, flags);
        return key;
    }

    @Override
    public boolean canMatchTrace(ITmfTrace trace) {
        if (!(trace instanceof ITmfTraceWithPreDefinedEvents)) {
            return true;
        }
        ITmfTraceWithPreDefinedEvents ktrace = (ITmfTraceWithPreDefinedEvents) trace;

        Set<String> traceEvents = TmfEventTypeCollectionHelper.getEventNames(ktrace.getContainedEventTypes());
        traceEvents.retainAll(REQUIRED_EVENTS);
        return !traceEvents.isEmpty();
    }

}
