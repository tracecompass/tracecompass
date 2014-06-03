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

package org.eclipse.linuxtools.pcap.core.stream;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.pcap.core.endpoint.ProtocolEndpointPair;
import org.eclipse.linuxtools.pcap.core.packet.PacketUniqueID;
import org.eclipse.linuxtools.pcap.core.protocol.Protocol;
import org.eclipse.linuxtools.pcap.core.protocol.pcap.PcapPacket;

// TODO decide if default modifier a good idea. This allows only the
// stream builder to call that method (and any class that is added to this
// package). This effectively makes the stream read-only.

/**
 * Class that represents a packet stream, which is a collection of packets that
 * share the same endpoints. The endpoints of a packet are protocol-dependent.
 * For example, a TCP stream is a collection of packets that share the same MAC
 * address, IP address, and Port couple.
 *
 * @author Vincent Perot
 */
public class PacketStream {

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$
    private final List<PacketUniqueID> fListIndex;
    private final Protocol fProtocol;
    private final int fId;
    private final ProtocolEndpointPair fEndpointPair;

    /**
     * Constructor of a packet stream.
     *
     * @param protocol
     *            The protocol of the packets of the stream. This is needed
     *            because the definition of a stream is protocol-dependent.
     * @param id
     *            The id of this stream.
     * @param endpointPair
     *            The common endpoints of the packets in this stream.
     */
    PacketStream(Protocol protocol, int id, ProtocolEndpointPair endpointPair) {
        fProtocol = protocol;
        fListIndex = new ArrayList<>();
        fId = id;
        fEndpointPair = endpointPair;
    }

    /**
     * Add a packet unique ID to the stream.
     *
     * @param packet
     *            The packet unique ID that must be added.
     */
    synchronized void add(PcapPacket packet) {
        fListIndex.add(new PacketUniqueID(packet));
    }

    /**
     * Get a packet unique ID in file from the stream.
     *
     * @param index
     *            The index in the stream of the packet to be retrieved.
     * @return The retrieved packet unique ID.
     */
    public synchronized PacketUniqueID get(int index) {
        PacketUniqueID id = fListIndex.get(index);
        if (id == null) {
            throw new IllegalStateException("PacketUniqueID is null!"); //$NON-NLS-1$
        }
        return id;
    }

    /**
     * Get the Protocol of this stream.
     *
     * @return The protocol of this stream.
     */
    public Protocol getProtocol() {
        return fProtocol;
    }

    /**
     * Method that returns the non-unique ID of this stream.
     *
     * @return the non-unique ID of this stream.
     */
    public int getID() {
        return fId;
    }

    /**
     * Method that returns the unique ID of this stream.
     *
     * @return the unique ID of this stream.
     */
    public String getUniqueID() {
        return fProtocol.getShortName() + '.' + fId;
    }

    /**
     * Method that returns the endpoint pair of the stream.
     *
     * @return The endpoint pair of the stream.
     */
    public ProtocolEndpointPair getEndpointPair() {
        return fEndpointPair;
    }

    // TODO return also the endpoint set.
    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Stream " + getUniqueID() + ", Number of Packets: " + fListIndex.size() + "\n"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

        for (int i = 0; i < fListIndex.size(); i++) {
            sb.append(fListIndex.get(i) + ", "); //$NON-NLS-1$
        }

        String string = sb.toString();
        if (string == null) {
            return EMPTY_STRING;
        }
        return string;

    }

    /**
     * Method that returns the number of packets in the stream.
     *
     * @return The number of packets in the stream.
     */
    public synchronized int size() {
        return fListIndex.size();
    }

}
