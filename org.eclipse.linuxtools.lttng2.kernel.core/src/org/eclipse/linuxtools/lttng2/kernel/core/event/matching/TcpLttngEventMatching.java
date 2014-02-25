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

package org.eclipse.linuxtools.lttng2.kernel.core.event.matching;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.TcpEventStrings;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.matching.TmfEventMatching.MatchingType;
import org.eclipse.linuxtools.tmf.core.event.matching.TmfNetworkEventMatching.Direction;
import org.eclipse.linuxtools.tmf.core.event.matching.ITmfNetworkMatchDefinition;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Class to match tcp type events. This class applies to traces obtained with
 * the full network tracepoint data available from an experimental branch of
 * lttng-modules. This branch is often rebased on lttng-modules master and is
 * available at
 * http://git.dorsal.polymtl.ca/~gbastien?p=lttng-modules.git;a=summary
 * net_data_experimental branch.
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class TcpLttngEventMatching implements ITmfNetworkMatchDefinition {

    private static final String[] key_seq = { TcpEventStrings.TRANSPORT_FIELDS, TcpEventStrings.TYPE_TCP, TcpEventStrings.SEQ };
    private static final String[] key_ackseq = { TcpEventStrings.TRANSPORT_FIELDS, TcpEventStrings.TYPE_TCP, TcpEventStrings.ACKSEQ };
    private static final String[] key_flags = { TcpEventStrings.TRANSPORT_FIELDS, TcpEventStrings.TYPE_TCP, TcpEventStrings.FLAGS };

    private static boolean canMatchPacket(final ITmfEvent event) {
        TmfEventField field = (TmfEventField) event.getContent();

        String[] tcp_data = { TcpEventStrings.TRANSPORT_FIELDS, TcpEventStrings.TYPE_TCP };
        ITmfEventField data = field.getSubField(tcp_data);
        if (data != null) {
            return (data.getValue() != null);
        }
        return false;
    }

    /**
     * The key to uniquely identify a TCP packet depends on many fields. This
     * method computes the key for a given event.
     *
     * @param event
     *            The event for which to compute the key
     * @return the unique key for this event
     */
    @Override
    public List<Object> getUniqueField(ITmfEvent event) {
        List<Object> keys = new ArrayList<>();

        TmfEventField field = (TmfEventField) event.getContent();
        ITmfEventField data;

        data = field.getSubField(key_seq);
        if (data != null) {
            keys.add(data.getValue());
        }
        data = field.getSubField(key_ackseq);
        if (data != null) {
            keys.add(data.getValue());
        }
        data = field.getSubField(key_flags);
        if (data != null) {
            keys.add(data.getValue());
        }

        return keys;
    }

    @Override
    public boolean canMatchTrace(ITmfTrace trace) {
        if (!(trace instanceof CtfTmfTrace)) {
            return false;
        }
        CtfTmfTrace ktrace = (CtfTmfTrace) trace;
        String[] events = { TcpEventStrings.NET_DEV_QUEUE, TcpEventStrings.NETIF_RECEIVE_SKB };
        return (ktrace.hasAtLeastOneOfEvents(events));
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

}
