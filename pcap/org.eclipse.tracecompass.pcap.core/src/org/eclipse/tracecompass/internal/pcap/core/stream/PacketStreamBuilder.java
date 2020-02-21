/*******************************************************************************
 * Copyright (c) 2014, 2019 Ericsson
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
 *   Viet-Hung Phan - Support pcapNg
 *******************************************************************************/

package org.eclipse.tracecompass.internal.pcap.core.stream;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.pcap.core.endpoint.ProtocolEndpoint;
import org.eclipse.tracecompass.internal.pcap.core.endpoint.ProtocolEndpointPair;
import org.eclipse.tracecompass.internal.pcap.core.filter.IPacketFilter;
import org.eclipse.tracecompass.internal.pcap.core.filter.PacketFilterByProtocol;
import org.eclipse.tracecompass.internal.pcap.core.packet.BadPacketException;
import org.eclipse.tracecompass.internal.pcap.core.packet.Packet;
import org.eclipse.tracecompass.internal.pcap.core.protocol.PcapProtocol;
import org.eclipse.tracecompass.internal.pcap.core.protocol.pcap.PcapPacket;
import org.eclipse.tracecompass.internal.pcap.core.trace.BadPcapFileException;
import org.eclipse.tracecompass.internal.pcap.core.trace.PcapFile;
import org.eclipse.tracecompass.internal.pcap.core.util.PcapHelper;

/**
 * Class that parse an entire pcap file to build the different streams.
 *
 * @author Vincent Perot
 */
public class PacketStreamBuilder {

    private final IPacketFilter fPacketFilter;
    private final PcapProtocol fProtocol;

    private final Map<Integer, PacketStream> fStreams;
    private final Map<ProtocolEndpointPair, Integer> fIDs;
    private int fCurrentId;

    /**
     * Main constructor.
     *
     * @param protocol
     *            The protocol of the builder.
     */
    public PacketStreamBuilder(PcapProtocol protocol) {
        fCurrentId = 0;
        fProtocol = protocol;
        fPacketFilter = new PacketFilterByProtocol(protocol);
        fStreams = new HashMap<>();
        fIDs = new HashMap<>();
    }

    /**
     * Method that returns a particular stream based on its ID.
     *
     * @param id
     *            The ID of the stream.
     * @return The stream that has the specified ID.
     */
    public synchronized @Nullable PacketStream getStream(int id) {
        return fStreams.get(id);
    }

    /**
     * Method that returns a particular stream based on its endpoints. It
     * returns null if no corresponding stream is found.
     *
     * @param endpointA
     *            The first endpoint of the stream.
     * @param endpointB
     *            The second endpoint of the stream.
     *
     * @return The stream that has the specified endpoints. Return Null if no
     *         stream is found between the two endpoints.
     */
    public synchronized @Nullable PacketStream getStream(ProtocolEndpoint endpointA, ProtocolEndpoint endpointB) {
        ProtocolEndpointPair set = new ProtocolEndpointPair(endpointA, endpointB);
        Integer id = checkNotNull(fIDs.get(set));
        return fStreams.get(id);
    }

    /**
     * Method that returns all the streams at the specified protocol level.
     *
     * @return The streams as a list.
     */
    public synchronized Iterable<PacketStream> getStreams() {
        Iterable<PacketStream> iterable = new LinkedList<>(fStreams.values());
        return iterable;
    }

    /**
     * Method that is called when the filter accepts a packet. This methods add
     * the packet to a stream based on its characteristics.
     *
     * @param packet
     *            The packet to be added.
     */
    public synchronized void addPacketToStream(PcapPacket packet) {
        if (fPacketFilter.accepts(packet)) {
            @Nullable Packet newPacket = packet.getPacket(fProtocol);
            if (newPacket == null) {
                return;
            }
            ProtocolEndpointPair endpointSet = new ProtocolEndpointPair(newPacket);
            if (!fIDs.containsKey(endpointSet)) {
                fIDs.put(endpointSet, fCurrentId);

                PacketStream stream = new PacketStream(fProtocol, fCurrentId, endpointSet);
                stream.add(packet);
                fStreams.put(fCurrentId, stream);
                fCurrentId++;
            } else {
                Integer id = fIDs.get(endpointSet);
                checkNotNull(fStreams.get(id)).add(packet);
            }
        }
        return;
    }

    /**
     * Getter method for the protocol of the stream builder.
     *
     * @return The protocol.
     */
    public PcapProtocol getProtocol() {
        return fProtocol;
    }

    /**
     * Method that clears the builder.
     */
    public void clear() {
        fStreams.clear();
        fIDs.clear();
        fCurrentId = 0;
    }

    /**
     * Method that returns the number of streams built.
     *
     * @return The number of streams built.
     */
    public synchronized int getNbStreams() {
        return fStreams.size();
    }

    /**
     * Method that parse an entire file and build the streams contained in the
     * file.
     *
     * @param filePath
     *            The file path.
     * @throws IOException
     *             When an IO error occurs.
     * @throws BadPcapFileException
     *             When the PcapFile is not valid.
     */
    public synchronized void parsePcapFile(Path filePath) throws IOException, BadPcapFileException {
        try (PcapFile pcapFile = PcapHelper.getPcapFile(filePath)) {
            while (true) {
                try {
                    PcapPacket packet = pcapFile.parseNextPacket();
                    if (packet == null) { // end-of-file
                        return;
                    }
                    addPacketToStream(packet);
                } catch (BadPacketException e) {
                    // Ignore packet. Do nothing.
                }
            }
        }
    }
}
