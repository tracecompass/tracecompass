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
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.pcap.core.packet.BadPacketException;
import org.eclipse.tracecompass.internal.pcap.core.trace.PcapFileValues;
import org.eclipse.tracecompass.internal.pcap.core.trace.PcapOldFile;
import org.eclipse.tracecompass.internal.pcap.core.util.ConversionHelper;

/**
 * Class that represents a Pcap packet. This is the highest level of
 * encapsulation.
 * This class will serve pcap packet
 */
public class PcapOldPacket extends PcapPacket {

    private static final long TIMESTAMP_MICROSECOND_MAX = 1000000L;
    private static final long TIMESTAMP_NANOSECOND_MAX = 1000000000L;

    /**
     * Constructor of the Pcap Packet class.
     *
     * @param file
     *            The file that contains this packet.
     * @param header
     *            The header of the packet.
     * @param payload
     *            The payload of this packet.
     * @param index
     *            The index of the packet in the file.
     * @throws BadPacketException
     *             Thrown when the Packet is erroneous.
     */
    public PcapOldPacket(PcapOldFile file, ByteBuffer header, @Nullable ByteBuffer payload, long index) throws BadPacketException {

        super(file);

        long timestampMostSignificant;
        long timestampLeastSignificant;

        if (header.limit() < PcapFileValues.PACKET_HEADER_SIZE) {
            throw new BadPacketException("The Pcap packet header is too small."); //$NON-NLS-1$
        }

        setIndex(index);

        // PcapPacket header in File endian
        header.order(getPcapFile().getByteOrder());
        header.position(0);

        // Get Timestamp MSB
        timestampMostSignificant = ConversionHelper.unsignedIntToLong(header.getInt());
        // Get Timestamp LSB
        timestampLeastSignificant = ConversionHelper.unsignedIntToLong(header.getInt());
        switch (getTimestampScale()) {
        case MICROSECOND:
            if (timestampLeastSignificant > TIMESTAMP_MICROSECOND_MAX) {
                throw new BadPacketException("The timestamp is erroneous."); //$NON-NLS-1$
            }
            setTimeStamp(TIMESTAMP_MICROSECOND_MAX * timestampMostSignificant + timestampLeastSignificant);
            break;
        case NANOSECOND:
            if (timestampMostSignificant > TIMESTAMP_NANOSECOND_MAX) {
                throw new BadPacketException("The timestamp is erroneous."); //$NON-NLS-1$
            }
            setTimeStamp(TIMESTAMP_NANOSECOND_MAX * timestampMostSignificant + timestampLeastSignificant);
            break;
        default:
            throw new IllegalArgumentException("The timestamp precision is not valid!"); //$NON-NLS-1$
        }

        // Get captured length from the packet
        setIncludedLength(ConversionHelper.unsignedIntToLong(header.getInt()));
        // Get packet data length (payload)
        setOriginalLength(ConversionHelper.unsignedIntToLong(header.getInt()));

        // Set up payload
        final ByteBuffer pcapPacket = payload;
        if (pcapPacket == null) {
            return;
        }

        pcapPacket.order(ByteOrder.BIG_ENDIAN);
        pcapPacket.position(0);
        setPayload(pcapPacket);

        // Find Child Packet
        setChildPacket(findChildPacket());

    }

    @Override
    public PcapOldFile getPcapFile() {
        return (PcapOldFile) super.getPcapFile();
    }

    @Override
    public long getDataLinkType() {
        return getPcapFile().getDataLinkType();
    }
}
