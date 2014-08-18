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

import org.eclipse.linuxtools.internal.pcap.core.stream.PacketStream;
import org.eclipse.linuxtools.internal.tmf.pcap.core.protocol.TmfPcapProtocol;
import org.eclipse.linuxtools.internal.tmf.pcap.core.util.ProtocolConversion;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;

/**
 * Class that wraps a Packet Stream.
 *
 * @author Vincent Perot
 */
public class TmfPacketStream {

    private final PacketStream fPacketStream;

    /**
     * Class constructor.
     *
     * @param stream
     *            The stream to build the TmfPacketStream from.
     */
    public TmfPacketStream(PacketStream stream) {
        fPacketStream = stream;
    }

    /**
     * Method that returns the first endpoint of the packet stream.
     *
     * @return The first endpoint.
     */
    public String getFirstEndpoint() {
        return fPacketStream.getEndpointPair().getFirstEndpoint().toString();
    }

    /**
     * Method that returns the second endpoint of the packet stream.
     *
     * @return The second endpoint.
     */
    public String getSecondEndpoint() {
        return fPacketStream.getEndpointPair().getSecondEndpoint().toString();
    }

    /**
     * Method that returns the ID of the packet stream.
     *
     * @return The ID of the packet stream.
     */
    public int getID() {
        return fPacketStream.getID();
    }

    /**
     * Method that returns the TmfProtocol of the packet stream.
     *
     * @return The TmfProtocol of the packet stream.
     */
    public TmfPcapProtocol getProtocol() {
        return ProtocolConversion.wrap(fPacketStream.getProtocol());
    }

    /**
     * Get the number of packets going from the first endpoint to the second.
     *
     * @return The number of packets from A to B.
     */
    public synchronized long getNbPacketsAtoB() {
        return fPacketStream.getNbPacketsAtoB();
    }

    /**
     * Get the number of packets going from the second endpoint to the first.
     *
     * @return The number of packets from B to A.
     */
    public synchronized long getNbPacketsBtoA() {
        return fPacketStream.getNbPacketsBtoA();
    }

    /**
     * Get the total number of packets in this stream.
     *
     * @return The total number of packets.
     */
    public synchronized long getNbPackets() {
        return fPacketStream.getNbPackets();
    }

    /**
     * Get the number of bytes going from the first endpoint to the second.
     *
     * @return The number of bytes from A to B.
     */
    public synchronized long getNbBytesAtoB() {
        return fPacketStream.getNbBytesAtoB();
    }

    /**
     * Get the number of bytes going from the second endpoint to the first.
     *
     * @return The number of bytes from B to A.
     */
    public synchronized long getNbBytesBtoA() {
        return fPacketStream.getNbBytesBtoA();
    }

    /**
     * Get the total number of bytes in this stream.
     *
     * @return The total number of bytes.
     */
    public synchronized long getNbBytes() {
        return fPacketStream.getNbBytes();
    }

    /**
     * Get the start time of this stream.
     *
     * @return The start time.
     */
    public synchronized ITmfTimestamp getStartTime() {
        return new TmfTimestamp(fPacketStream.getStartTime(), ITmfTimestamp.NANOSECOND_SCALE);
    }

    /**
     * Get the stop time of this stream.
     *
     * @return The stop time.
     */
    public synchronized ITmfTimestamp getStopTime() {
        return new TmfTimestamp(fPacketStream.getStopTime(), ITmfTimestamp.NANOSECOND_SCALE);
    }

    /**
     * Get the duration of this stream, in seconds
     *
     * @return The duration of this stream.
     */
    public synchronized double getDuration() {
        return fPacketStream.getDuration();
    }

    /**
     * Get the the average byte per second from A to B.
     *
     * @return the average byte per second from A to B.
     */
    public synchronized double getBPSAtoB() {
        return fPacketStream.getBPSAtoB();
    }

    /**
     * Get the the average byte per second from B to A.
     *
     * @return the average byte per second from B to A.
     */
    public synchronized double getBPSBtoA() {
        return fPacketStream.getBPSBtoA();
    }

}
