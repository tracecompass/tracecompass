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
        if ((content.getField(TcpEventStrings.SEQ) != null) &&
                (content.getField(TcpEventStrings.ACKSEQ) != null) && (content.getField(TcpEventStrings.FLAGS) != null)) {
            return true;
        }
        return false;
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
            return Direction.CAUSE;
        } else if (evname.equals(TcpEventStrings.INET_SOCK_LOCAL_OUT)) {
            return Direction.EFFECT;
        }
        return null;
    }

    @Override
    public IEventMatchingKey getEventKey(ITmfEvent event) {
        IEventMatchingKey key = new TcpEventKey((long) event.getContent().getField(TcpEventStrings.SEQ).getValue(),
                (long) event.getContent().getField(TcpEventStrings.ACKSEQ).getValue(),
                (long) event.getContent().getField(TcpEventStrings.FLAGS).getValue());
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
