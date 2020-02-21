/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.pcap.core.event;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.tracecompass.internal.pcap.core.packet.Packet;
import org.eclipse.tracecompass.internal.pcap.core.protocol.PcapProtocol;
import org.eclipse.tracecompass.internal.pcap.core.protocol.pcap.PcapPacket;
import org.eclipse.tracecompass.internal.pcap.core.stream.PacketStreamBuilder;
import org.eclipse.tracecompass.internal.tmf.pcap.core.protocol.TmfPcapProtocol;
import org.eclipse.tracecompass.internal.tmf.pcap.core.util.ProtocolConversion;

/**
 * Class that wraps a PacketStreamBuilder.
 *
 * @author Vincent Perot
 */
public class TmfPacketStreamBuilder {

    private final PacketStreamBuilder fBuilder;

    /**
     * Constructor.
     *
     * @param protocol
     *            The protocol of the streams to build.
     */
    public TmfPacketStreamBuilder(TmfPcapProtocol protocol) {
        fBuilder = new PacketStreamBuilder(ProtocolConversion.unwrap(protocol));
    }

    /**
     * Method that adds an event to this builder.
     *
     * @param event
     *            The event to add.
     */
    public synchronized void addEventToStream(PcapEvent event) {
        Packet packet = event.getPacket().getPacket(PcapProtocol.PCAP);
        if (!(packet instanceof PcapPacket)) {
            return;
        }
        PcapPacket pcapPacket = (PcapPacket) packet;
        fBuilder.addPacketToStream(pcapPacket);
    }

    /**
     * Method that returns the number of streams built.
     *
     * @return The number of streams built.
     */
    public synchronized int getNbStreams() {
        return fBuilder.getNbStreams();
    }

    /**
     * Method that returns an iterable on the streams built so far.
     *
     * @return An iterable on the streams.
     */
    public synchronized Iterable<TmfPacketStream> getStreams() {
        // We can't store in immutable list since the stream number/content can
        // change dynamically.
        return StreamSupport.stream(fBuilder.getStreams().spliterator(), false)
                .map(e -> new TmfPacketStream(e))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
