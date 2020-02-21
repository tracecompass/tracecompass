/*******************************************************************************
 * Copyright (c) 2018, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Viet-Hung Phan - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.pcap.core.trace;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.pcap.core.packet.BadPacketException;
import org.eclipse.tracecompass.internal.pcap.core.protocol.pcap.PcapOldPacket;
import org.eclipse.tracecompass.internal.pcap.core.util.ConversionHelper;
import org.eclipse.tracecompass.internal.pcap.core.util.PcapTimestampScale;

/**
 * Class that allows the interaction with a pcap file.
 *
 * @author Viet-Hung Phan
 */
public class PcapOldFile extends PcapFile {

    private long fTimeAccuracy;
    private long fTimeZoneCorrection;
    private long fSnapshotLength;
    private long fDataLinkType;

    PcapTimestampScale fTimestampPrecision;

    /**
     * Constructor of the PcapOldFile class where the parent is PcapFile class.
     *
     * @param filePath
     *            The path to the pcap file.
     *
     * @throws BadPcapFileException
     *             Thrown if the Pcap File is not valid.
     * @throws IOException
     *             Thrown if there is an IO error while reading the file.
     */
    public PcapOldFile(Path filePath) throws BadPcapFileException, IOException {
        // Instantiate the parent class
        super(filePath);

        ByteOrder byteOrder;

        TreeMap<Long, Long> fileIndex = getFileIndex();
        // Parse the global header.
        // Read the magic number (4 bytes) from the input stream
        // and determine the mode (big endian or little endian)
        ByteBuffer globalHeader = ByteBuffer.allocate(PcapFileValues.GLOBAL_HEADER_SIZE);
        getFileChannel().read(globalHeader);
        globalHeader.flip();
        int magicNumber = globalHeader.getInt();

        switch (magicNumber) {
        case PcapFileValues.MAGIC_BIG_ENDIAN_MICRO: // file is big endian
            byteOrder = ByteOrder.BIG_ENDIAN;
            fTimestampPrecision = PcapTimestampScale.MICROSECOND;
            break;
        case PcapFileValues.MAGIC_LITTLE_ENDIAN_MICRO: // file is little endian
            byteOrder = ByteOrder.LITTLE_ENDIAN;
            fTimestampPrecision = PcapTimestampScale.MICROSECOND;
            break;
        case PcapFileValues.MAGIC_BIG_ENDIAN_NANO: // file is big endian
            byteOrder = ByteOrder.BIG_ENDIAN;
            fTimestampPrecision = PcapTimestampScale.NANOSECOND;
            break;
        case PcapFileValues.MAGIC_LITTLE_ENDIAN_NANO: // file is little endian
            byteOrder = ByteOrder.LITTLE_ENDIAN;
            fTimestampPrecision = PcapTimestampScale.NANOSECOND;
            break;
        default:
            this.close();
            throw new BadPcapFileException(String.format("%08x", magicNumber) + " is not a known magic number."); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Put the rest of the buffer in file endian.
        globalHeader.order(byteOrder);

        // Initialization of global header fields.
        int fMajorVersion = ConversionHelper.unsignedShortToInt(globalHeader.getShort());
        int fMinorVersion = ConversionHelper.unsignedShortToInt(globalHeader.getShort());
        fTimeAccuracy = ConversionHelper.unsignedIntToLong(globalHeader.getInt());
        fTimeZoneCorrection = ConversionHelper.unsignedIntToLong(globalHeader.getInt());
        fSnapshotLength = ConversionHelper.unsignedIntToLong(globalHeader.getInt());
        fDataLinkType = ConversionHelper.unsignedIntToLong(globalHeader.getInt());

        fileIndex.put(getCurrentRank(), getFileChannel().position());
        // Data initialization
        init(byteOrder, fMajorVersion, fMinorVersion);
    }
    /**
     * Method that allows the parsing of a packet at the current position.
     *
     * @return The parsed Pcap Packet.
     * @throws IOException
     *             Thrown when there is an error while reading the file.
     * @throws BadPcapFileException
     *             Thrown when a packet header is invalid.
     * @throws BadPacketException
     *             Thrown when the packet is erroneous.
     */
    @Override
    public synchronized @Nullable PcapOldPacket parseNextPacket() throws IOException, BadPcapFileException, BadPacketException {

        // Parse the packet header
        if (getFileChannel().size() - getFileChannel().position() == 0) {
            return null;
        }
        if (getFileChannel().size() - getFileChannel().position() < PcapFileValues.PACKET_HEADER_SIZE) {
            throw new BadPcapFileException("A pcap header is invalid."); //$NON-NLS-1$
        }

        ByteBuffer pcapPacketHeader = ByteBuffer.allocate(PcapFileValues.PACKET_HEADER_SIZE);
        pcapPacketHeader.clear();
        pcapPacketHeader.order(getByteOrder());
        getFileChannel().read(pcapPacketHeader);
        pcapPacketHeader.flip();

        pcapPacketHeader.position(PcapFileValues.INCLUDED_LENGTH_POSITION);
        long includedPacketLength = ConversionHelper.unsignedIntToLong(pcapPacketHeader.getInt());

        if (getFileChannel().size() - getFileChannel().position() < includedPacketLength) {
            throw new BadPcapFileException("A packet header is invalid."); //$NON-NLS-1$
        }

        if (includedPacketLength > Integer.MAX_VALUE) {
            throw new BadPacketException("Packets that are bigger than 2^31-1 bytes are not supported."); //$NON-NLS-1$
        }

        ByteBuffer pcapPacketData = ByteBuffer.allocate((int) includedPacketLength);
        pcapPacketData.clear();
        pcapPacketData.order(getByteOrder());
        getFileChannel().read(pcapPacketData);

        pcapPacketData.flip();

        TreeMap<Long, Long> fFileIndex = getFileIndex();
        setCurrentRank(getCurrentRank()+1);
        fFileIndex.put(getCurrentRank(), getFileChannel().position());

        return new PcapOldPacket(this, pcapPacketHeader, pcapPacketData, getCurrentRank() - 1);

    }

    @Override
    public synchronized boolean skipNextPacket() throws IOException, BadPcapFileException {

        // Parse the packet header
        if (getFileChannel().size() - getFileChannel().position() == 0) {
            return false;
        }
        if (getFileChannel().size() - getFileChannel().position() < PcapFileValues.GLOBAL_HEADER_SIZE) {
            throw new BadPcapFileException("A pcap header is invalid."); //$NON-NLS-1$
        }

        ByteBuffer pcapPacketHeader = ByteBuffer.allocate(PcapFileValues.PACKET_HEADER_SIZE);
        pcapPacketHeader.clear();
        pcapPacketHeader.order(getByteOrder());
        getFileChannel().read(pcapPacketHeader);
        pcapPacketHeader.flip();
        pcapPacketHeader.position(PcapFileValues.INCLUDED_LENGTH_POSITION);
        long includedPacketLength = ConversionHelper.unsignedIntToLong(pcapPacketHeader.getInt());

        if (getFileChannel().size() - getFileChannel().position() < includedPacketLength) {
            throw new BadPcapFileException("A packet header is invalid."); //$NON-NLS-1$
        }

        getFileChannel().position(getFileChannel().position() + includedPacketLength);

        TreeMap<Long, Long> fFileIndex = getFileIndex();
        setCurrentRank(getCurrentRank() + 1);
        fFileIndex.put(getCurrentRank(), getFileChannel().position());
        return true;
    }

    /**
     * Getter method that returns the timestamp precision of the file.
     *
     * @return the timestamp precision of the file.
     */
    @Override
    public PcapTimestampScale getTimestampPrecision() {
        return fTimestampPrecision;
    }

    /**
     * Getter method for the time accuracy of the file.
     *
     * @return The time accuracy of the file.
     */
    public long getTimeAccuracy() {
        return fTimeAccuracy;
    }

    /**
     * Getter method for the time zone correction of the file.
     *
     * @return The time zone correction of the file.
     */
    public long getTimeZoneCorrection() {
        return fTimeZoneCorrection;
    }

    /**
     * Getter method for the snapshot length of the file.
     *
     * @return The snapshot length of the file.
     */
    public long getSnapShotLength() {
        return fSnapshotLength;
    }

    /**
     * Getter method for the data link type of the file. This parameter is used
     * to determine higher-level protocols (Ethernet, WLAN, SLL).
     *
     * @return The data link type of the file.
     */
    public long getDataLinkType() {
        return fDataLinkType;
    }

}
