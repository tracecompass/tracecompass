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

package org.eclipse.linuxtools.pcap.core.packet;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.pcap.core.packet.Packet;
import org.eclipse.linuxtools.pcap.core.protocol.Protocol;
import org.eclipse.linuxtools.pcap.core.protocol.pcap.PcapPacket;

/**
 * Class that represents a Packet ID. Using the information contained in this
 * class, it is possible to retrieve a packet. This allows to tremendously
 * reduce memory usage of packet streams while keeping good performance.
 *
 * @author Vincent Perot
 */
public class PacketUniqueID {

    private final String fPath;
    private final long fIndex;

    /**
     * Constructor. It builds the packet ID from a packet.
     *
     * @param packet
     *            The packet to build the ID from.
     */
    public PacketUniqueID(Packet packet) {
        fPath = packet.getPcapFile().getPath();
        PcapPacket pcapPacket = (PcapPacket) packet.getPacket(Protocol.PCAP);
        fIndex = (pcapPacket == null ? -1 : pcapPacket.getIndex());
    }

    /**
     * Getter method that returns the file path of the packet.
     *
     * @return The file path.
     */
    public String getPath() {
        return fPath;
    }

    /**
     * Getter method that returns the index within the file of the packet.
     *
     * @return The packet index.
     */
    public long getIndex() {
        return fIndex;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (fIndex ^ (fIndex >>> 32));
        result = prime * result + fPath.hashCode();
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        PacketUniqueID other = (PacketUniqueID) obj;
        if (fIndex != other.fIndex) {
            return false;
        }
        if (fPath != other.fPath) {
            return false;
        }
        return true;
    }

}
