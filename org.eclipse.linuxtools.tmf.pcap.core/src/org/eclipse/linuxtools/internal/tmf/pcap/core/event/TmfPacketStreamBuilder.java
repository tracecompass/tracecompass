/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.pcap.core.event;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.internal.pcap.core.packet.Packet;
import org.eclipse.linuxtools.internal.pcap.core.protocol.PcapProtocol;
import org.eclipse.linuxtools.internal.pcap.core.protocol.pcap.PcapPacket;
import org.eclipse.linuxtools.internal.pcap.core.stream.PacketStream;
import org.eclipse.linuxtools.internal.pcap.core.stream.PacketStreamBuilder;
import org.eclipse.linuxtools.internal.tmf.pcap.core.protocol.TmfPcapProtocol;
import org.eclipse.linuxtools.internal.tmf.pcap.core.util.ProtocolConversion;

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
        if (packet == null || !(packet instanceof PcapPacket)) {
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
        List<TmfPacketStream> list = new ArrayList<>();
        for (PacketStream stream : fBuilder.getStreams()) {
            if (stream != null) {
                list.add(new TmfPacketStream(stream));
            }
        }
        return list;
    }

}
