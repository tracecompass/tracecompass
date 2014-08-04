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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.pcap.core.endpoint.ProtocolEndpointPair;
import org.eclipse.linuxtools.pcap.core.packet.Packet;
import org.eclipse.linuxtools.pcap.core.protocol.Protocol;
import org.eclipse.linuxtools.pcap.core.protocol.pcap.PcapPacket;

import com.google.common.math.DoubleMath;

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

    private static final double SECOND_TO_NANOSECOND = 1000000000.0;
    private static final double DELTA = 0.000000001;
    private final Protocol fProtocol;
    private final int fId;
    private final ProtocolEndpointPair fEndpointPair;

    private long fNbPacketsAtoB;
    private long fNbPacketsBtoA;
    private long fNbBytesAtoB;
    private long fNbBytesBtoA;
    private long fStartTime;
    private long fEndTime;

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
        fId = id;
        fEndpointPair = endpointPair;
        fNbPacketsAtoB = 0;
        fNbPacketsBtoA = 0;
        fNbBytesAtoB = 0;
        fNbBytesBtoA = 0;
        fStartTime = Long.MAX_VALUE;
        fEndTime = Long.MIN_VALUE;
    }

    /**
     * Add a packet to the stream.
     *
     * @param packet
     *            The packet that must be added.
     */
    synchronized void add(PcapPacket packet) {

        Packet newPacket = packet.getPacket(fProtocol);
        if (newPacket == null) {
            return;
        }

        // Update packet and byte number
        if (fEndpointPair.getFirstEndpoint().equals(newPacket.getSourceEndpoint()) &&
                fEndpointPair.getSecondEndpoint().equals(newPacket.getDestinationEndpoint())) {
            fNbPacketsAtoB++;
            fNbBytesAtoB += packet.getOriginalLength();
        } else if (fEndpointPair.getFirstEndpoint().equals(newPacket.getDestinationEndpoint()) &&
                fEndpointPair.getSecondEndpoint().equals(newPacket.getSourceEndpoint())) {
            fNbPacketsBtoA++;
            fNbBytesBtoA += packet.getOriginalLength();
        } else {
            throw new IllegalStateException();
        }

        // Update start and stop time
        // Stream timestamp is ALWAYS in nanoseconds.
        long timestamp;
        switch (packet.getTimestampScale()) {
        case MICROSECOND:
            timestamp = packet.getTimestamp() * 1000;
            break;
        case NANOSECOND:
            timestamp = packet.getTimestamp();
            break;
        default:
            throw new IllegalArgumentException("The timestamp precision is not valid!"); //$NON-NLS-1$
        }
        fStartTime = Math.min(fStartTime, timestamp);
        fEndTime = Math.max(fEndTime, timestamp);
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
        sb.append("Stream " + getUniqueID() + ", Number of Packets: " + getNbPackets() + "\n"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

        @SuppressWarnings("null")
        @NonNull String string = sb.toString();
        return string;

    }

    /**
     * Get the number of packets going from the first endpoint to the second.
     *
     * @return The number of packets from A to B.
     */
    public synchronized long getNbPacketsAtoB() {
        return fNbPacketsAtoB;
    }

    /**
     * Get the number of packets going from the second endpoint to the first.
     *
     * @return The number of packets from B to A.
     */
    public synchronized long getNbPacketsBtoA() {
        return fNbPacketsBtoA;
    }

    /**
     * Get the total number of packets in this stream.
     *
     * @return The total number of packets.
     */
    public synchronized long getNbPackets() {
        return fNbPacketsAtoB + fNbPacketsBtoA;
    }

    /**
     * Get the number of bytes going from the first endpoint to the second.
     *
     * @return The number of bytes from A to B.
     */
    public synchronized long getNbBytesAtoB() {
        return fNbBytesAtoB;
    }

    /**
     * Get the number of bytes going from the second endpoint to the first.
     *
     * @return The number of bytes from B to A.
     */
    public synchronized long getNbBytesBtoA() {
        return fNbBytesBtoA;
    }

    /**
     * Get the total number of bytes in this stream.
     *
     * @return The total number of bytes.
     */
    public synchronized long getNbBytes() {
        return fNbBytesAtoB + fNbBytesBtoA;
    }

    /**
     * Get the start time of this stream, in nanoseconds relative to epoch.
     *
     * @return The start time.
     */
    public synchronized long getStartTime() {
        return fStartTime;
    }

    /**
     * Get the stop time of this stream, in nanoseconds relative to epoch.
     *
     * @return The stop time.
     */
    public synchronized long getStopTime() {
        return fEndTime;
    }

    /**
     * Get the duration of this stream, in seconds
     *
     * @return The duration of this stream.
     */
    public synchronized double getDuration() {
        return (fEndTime - fStartTime) / SECOND_TO_NANOSECOND;
    }

    /**
     * Get the the average byte per second from A to B.
     *
     * @return the average byte per second from A to B.
     */
    public synchronized double getBPSAtoB() {
        if (DoubleMath.fuzzyEquals(getDuration(), 0, DELTA)) {
            return 0;
        }
        return fNbBytesAtoB / getDuration();
    }

    /**
     * Get the the average byte per second from B to A.
     *
     * @return the average byte per second from B to A.
     */
    public synchronized double getBPSBtoA() {
        if (DoubleMath.fuzzyEquals(getDuration(), 0, DELTA)) {
            return 0;
        }
        return fNbBytesBtoA / getDuration();
    }

}
