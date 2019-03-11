/*******************************************************************************
 * Copyright (c) 2018, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Viet-Hung Phan - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.pcap.core.protocol.pcap;

import java.nio.ByteBuffer;

import org.eclipse.tracecompass.internal.pcap.core.packet.BadPacketException;
import org.eclipse.tracecompass.internal.pcap.core.trace.PcapNgFile;

/**
 * Class that represents a PcapNg packet. This is the highest level of
 * encapsulation. This class will serve a pcapNg packet as receive and store the
 * necessary packet attributes
 */
public class PcapNgPacket extends PcapPacket {

    private final PcapNgInterface fInterface;

    /**
     * Constructor of the PcapNg Packet class.
     *
     * @param file
     *            The file that contains this packet.
     * @param interfaceDesc
     *            interface description
     * @param timestamp
     *            timestamp in nanoseconds
     * @param originalLength
     *            original length
     * @param packetData
     *            packet data
     * @throws BadPacketException
     *             Thrown when the Packet is erroneous.
     */
    public PcapNgPacket(PcapNgFile file, PcapNgInterface interfaceDesc, long timestamp, int originalLength, ByteBuffer packetData) throws BadPacketException {
        super(file);
        fInterface = interfaceDesc;
        setTimeStamp(timestamp);
        setIncludedLength(packetData.limit());
        setOriginalLength(originalLength);
        setPayload(packetData);
        setChildPacket(findChildPacket());
    }

    @Override
    public PcapNgFile getPcapFile() {
        return (PcapNgFile) super.getPcapFile();
    }

    @Override
    public long getDataLinkType() {
        return fInterface.getLinkType();
    }
}