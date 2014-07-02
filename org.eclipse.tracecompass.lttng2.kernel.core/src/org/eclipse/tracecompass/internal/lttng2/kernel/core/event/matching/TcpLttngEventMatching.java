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
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.matching.IEventMatchingKey;
import org.eclipse.tracecompass.tmf.core.event.matching.ITmfNetworkMatchDefinition;
import org.eclipse.tracecompass.tmf.core.event.matching.TcpEventKey;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventMatching.MatchingType;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfNetworkEventMatching.Direction;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTraceWithPreDefinedEvents;
import org.eclipse.tracecompass.tmf.core.trace.TmfEventTypeCollectionHelper;

import com.google.common.collect.ImmutableSet;

/**
 * Class to match tcp type events. This class applies to traces obtained with
 * the full network tracepoint data available from an experimental branch of
 * lttng-modules. This branch is often rebased on lttng-modules master and is
 * available at
 * http://git.dorsal.polymtl.ca/~gbastien?p=lttng-modules.git;a=summary
 * net_data_experimental branch.
 *
 * @author Geneviève Bastien
 */
public class TcpLttngEventMatching implements ITmfNetworkMatchDefinition {

    private static final String[] key_seq = { TcpEventStrings.TRANSPORT_FIELDS, TcpEventStrings.TYPE_TCP, TcpEventStrings.SEQ };
    private static final String[] key_ackseq = { TcpEventStrings.TRANSPORT_FIELDS, TcpEventStrings.TYPE_TCP, TcpEventStrings.ACKSEQ };
    private static final String[] key_flags = { TcpEventStrings.TRANSPORT_FIELDS, TcpEventStrings.TYPE_TCP, TcpEventStrings.FLAGS };

    private static final ImmutableSet<String> REQUIRED_EVENTS = ImmutableSet.of(
            TcpEventStrings.NET_DEV_QUEUE,
            TcpEventStrings.NETIF_RECEIVE_SKB);

    private static boolean canMatchPacket(final ITmfEvent event) {
        TmfEventField field = (TmfEventField) event.getContent();

        String[] tcp_data = { TcpEventStrings.TRANSPORT_FIELDS, TcpEventStrings.TYPE_TCP };
        ITmfEventField data = field.getSubField(tcp_data);
        if (data != null) {
            return (data.getValue() != null);
        }
        return false;
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

    @Override
    public Direction getDirection(ITmfEvent event) {
        String evname = event.getType().getName();

        /* Is the event a tcp socket in or out event */
        if (evname.equals(TcpEventStrings.NETIF_RECEIVE_SKB) && canMatchPacket(event)) {
            return Direction.IN;
        } else if (evname.equals(TcpEventStrings.NET_DEV_QUEUE) && canMatchPacket(event)) {
            return Direction.OUT;
        }
        return null;
    }

    @Override
    public MatchingType[] getApplicableMatchingTypes() {
        MatchingType[] types = { MatchingType.NETWORK };
        return types;
    }

    @Override
    public IEventMatchingKey getEventKey(ITmfEvent event) {
        TmfEventField field = (TmfEventField) event.getContent();
        ITmfEventField data;

        long seq = -1, ackseq = -1, flags = -1;
        data = field.getSubField(key_seq);
        if (data != null) {
            seq = (long) data.getValue();
        } else {
            return null;
        }
        data = field.getSubField(key_ackseq);
        if (data != null) {
            ackseq = (long) data.getValue();
        } else {
            return null;
        }
        data = field.getSubField(key_flags);
        if (data != null) {
            flags = (long) data.getValue();
        } else {
            return null;
        }

        IEventMatchingKey key = new TcpEventKey(seq, ackseq, flags);

        return key;
    }

}
