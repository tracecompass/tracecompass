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

package org.eclipse.linuxtools.pcap.core.trace;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.pcap.core.packet.BadPacketException;
import org.eclipse.linuxtools.pcap.core.protocol.pcap.PcapPacket;
import org.eclipse.linuxtools.pcap.core.util.ConversionHelper;
import org.eclipse.linuxtools.pcap.core.util.PcapTimestampScale;

/**
 * Class that allows the interaction with a pcap file.
 *
 * @author Vincent Perot
 */
public class PcapFile implements Closeable {

    // TODO add pcapng support.
    // TODO Make parsing faster by buffering the data.

    private final String fPcapFilePath;
    private final ByteOrder fByteOrder;
    private final FileChannel fFileChannel;
    private final FileInputStream fFileInputStream;
    private final PcapTimestampScale fTimestampPrecision;

    private final int fMajorVersion;
    private final int fMinorVersion;
    private final long fTimeAccuracy;
    private final long fTimeZoneCorrection;
    private final long fSnapshotLength;
    private final long fDataLinkType;

    private final TreeMap<Long, Long> fFileIndex;

    private long fCurrentRank;
    private long fTotalNumberPackets;

    /**
     * Constructor of the PcapFile Class.
     *
     * @param filePath
     *            The path to the pcap file.
     *
     * @throws BadPcapFileException
     *             Thrown if the Pcap File is not valid.
     * @throws IOException
     *             Thrown if there is an IO error while reading the file.
     */
    public PcapFile(String filePath) throws BadPcapFileException, IOException {

        fFileIndex = new TreeMap<>();
        fCurrentRank = 0;
        fTotalNumberPackets = -1;
        fPcapFilePath = filePath;

        // Check file validity
        File pcapFile = new File(fPcapFilePath);
        if ((!fPcapFilePath.endsWith(".cap") && !fPcapFilePath.endsWith(".pcap")) || //$NON-NLS-1$ //$NON-NLS-2$
                !pcapFile.exists() || !pcapFile.isFile() || pcapFile.length() < PcapFileValues.GLOBAL_HEADER_SIZE) {
            throw new BadPcapFileException("Bad Pcap File."); //$NON-NLS-1$
        }

        if (!pcapFile.canRead()) {
            throw new BadPcapFileException("File is not readable."); //$NON-NLS-1$
        }

        // File is not empty. Try to open.
        fFileInputStream = new FileInputStream(fPcapFilePath);

        @SuppressWarnings("null")
        @NonNull FileChannel fileChannel = fFileInputStream.getChannel();
        fFileChannel = fileChannel;

        // Parse the global header.
        // Read the magic number (4 bytes) from the input stream
        // and determine the mode (big endian or little endian)
        ByteBuffer globalHeader = ByteBuffer.allocate(PcapFileValues.GLOBAL_HEADER_SIZE);
        globalHeader.clear();
        fFileChannel.read(globalHeader);
        globalHeader.flip();
        int magicNumber = globalHeader.getInt();

        @SuppressWarnings("null")
        @NonNull ByteOrder be = ByteOrder.BIG_ENDIAN;
        @SuppressWarnings("null")
        @NonNull ByteOrder le = ByteOrder.LITTLE_ENDIAN;

        switch (magicNumber) {
        case PcapFileValues.MAGIC_BIG_ENDIAN_MICRO: // file is big endian
            fByteOrder = be;
            fTimestampPrecision = PcapTimestampScale.MICROSECOND;
            break;
        case PcapFileValues.MAGIC_LITTLE_ENDIAN_MICRO: // file is little endian
            fByteOrder = le;
            fTimestampPrecision = PcapTimestampScale.MICROSECOND;
            break;
        case PcapFileValues.MAGIC_BIG_ENDIAN_NANO: // file is big endian
            fByteOrder = be;
            fTimestampPrecision = PcapTimestampScale.NANOSECOND;
            break;
        case PcapFileValues.MAGIC_LITTLE_ENDIAN_NANO: // file is little endian
            fByteOrder = le;
            fTimestampPrecision = PcapTimestampScale.NANOSECOND;
            break;
        default:
            this.close();
            throw new BadPcapFileException(String.format("%08x", magicNumber) + " is not a known magic number."); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Put the rest of the buffer in file endian.
        globalHeader.order(fByteOrder);

        // Initialization of global header fields.
        fMajorVersion = ConversionHelper.unsignedShortToInt(globalHeader.getShort());
        fMinorVersion = ConversionHelper.unsignedShortToInt(globalHeader.getShort());
        fTimeAccuracy = ConversionHelper.unsignedIntToLong(globalHeader.getInt());
        fTimeZoneCorrection = ConversionHelper.unsignedIntToLong(globalHeader.getInt());
        fSnapshotLength = ConversionHelper.unsignedIntToLong(globalHeader.getInt());
        fDataLinkType = ConversionHelper.unsignedIntToLong(globalHeader.getInt());

        fFileIndex.put(fCurrentRank, fFileChannel.position());

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
    public synchronized @Nullable PcapPacket parseNextPacket() throws IOException, BadPcapFileException, BadPacketException {

        // Parse the packet header
        if (fFileChannel.size() - fFileChannel.position() == 0) {
            return null;
        }
        if (fFileChannel.size() - fFileChannel.position() < PcapFileValues.PACKET_HEADER_SIZE) {
            throw new BadPcapFileException("A pcap header is invalid."); //$NON-NLS-1$
        }

        ByteBuffer pcapPacketHeader = ByteBuffer.allocate(PcapFileValues.PACKET_HEADER_SIZE);
        pcapPacketHeader.clear();
        pcapPacketHeader.order(fByteOrder);

        fFileChannel.read(pcapPacketHeader);

        pcapPacketHeader.flip();
        pcapPacketHeader.position(PcapFileValues.INCLUDED_LENGTH_POSITION);
        long includedPacketLength = ConversionHelper.unsignedIntToLong(pcapPacketHeader.getInt());

        if (fFileChannel.size() - fFileChannel.position() < includedPacketLength) {
            throw new BadPcapFileException("A packet header is invalid."); //$NON-NLS-1$
        }

        if (includedPacketLength > Integer.MAX_VALUE) {
            throw new BadPacketException("Packets that are bigger than 2^31-1 bytes are not supported."); //$NON-NLS-1$
        }

        ByteBuffer pcapPacketData = ByteBuffer.allocate((int) includedPacketLength);
        pcapPacketData.clear();
        pcapPacketHeader.order(ByteOrder.BIG_ENDIAN); // Not really needed.
        fFileChannel.read(pcapPacketData);

        pcapPacketData.flip();

        fFileIndex.put(++fCurrentRank, fFileChannel.position());

        return new PcapPacket(this, null, pcapPacketHeader, pcapPacketData, fCurrentRank - 1);

    }

    /**
     * Method that allows to skip a packet at the current position.
     *
     * @throws IOException
     *             Thrown when there is an error while reading the file.
     * @throws BadPcapFileException
     *             Thrown when a packet header is invalid.
     */
    public synchronized void skipNextPacket() throws IOException, BadPcapFileException {

        // Parse the packet header
        if (fFileChannel.size() - fFileChannel.position() == 0) {
            return;
        }
        if (fFileChannel.size() - fFileChannel.position() < PcapFileValues.PACKET_HEADER_SIZE) {
            throw new BadPcapFileException("A pcap header is invalid."); //$NON-NLS-1$
        }

        ByteBuffer pcapPacketHeader = ByteBuffer.allocate(PcapFileValues.PACKET_HEADER_SIZE);
        pcapPacketHeader.clear();
        pcapPacketHeader.order(fByteOrder);

        fFileChannel.read(pcapPacketHeader);

        pcapPacketHeader.flip();
        pcapPacketHeader.position(PcapFileValues.INCLUDED_LENGTH_POSITION);
        long includedPacketLength = ConversionHelper.unsignedIntToLong(pcapPacketHeader.getInt());

        if (fFileChannel.size() - fFileChannel.position() < includedPacketLength) {
            throw new BadPcapFileException("A packet header is invalid."); //$NON-NLS-1$
        }

        fFileChannel.position(fFileChannel.position() + includedPacketLength);

        fFileIndex.put(++fCurrentRank, fFileChannel.position());

    }

    /**
     * Method that moves the position to the specified rank.
     *
     * @param rank
     *            The rank of the packet.
     *
     * @throws IOException
     *             Thrown when there is an error while reading the file.
     * @throws BadPcapFileException
     *             Thrown when a packet header is invalid.
     */
    public synchronized void seekPacket(long rank) throws IOException, BadPcapFileException {

        // Verify argument
        if (rank < 0) {
            throw new IllegalArgumentException();
        }

        Long positionInBytes = fFileIndex.get(rank);

        if (positionInBytes != null) {
            // Index is known. Move to position.
            fFileChannel.position(positionInBytes.longValue());
            fCurrentRank = rank;
        } else {
            // Index is unknown. Find the corresponding position.
            // Find closest index
            fCurrentRank = fFileIndex.floorKey(rank);
            // skip until wanted packet is found
            do {
                skipNextPacket();
            } while (fCurrentRank != rank && hasNextPacket());
        }
    }

    /**
     * Method that indicates if there are packets remaining to read. It is an
     * end of file indicator.
     *
     * @return Whether the pcap still has packets or not.
     * @throws IOException
     *             If some IO error occurs.
     */
    public synchronized boolean hasNextPacket() throws IOException {
        return ((fFileChannel.size() - fFileChannel.position()) > 0);
    }

    /**
     * Getter method for the Byte Order of the file.
     *
     * @return The byte Order of the file.
     */
    public ByteOrder getByteOrder() {
        return fByteOrder;
    }

    /**
     * Getter method for the Major Version of the file.
     *
     * @return The Major Version of the file.
     */
    public int getMajorVersion() {
        return fMajorVersion;
    }

    /**
     * Getter method for the Minor Version of the file.
     *
     * @return The Minor Version of the file.
     */
    public int getMinorVersion() {
        return fMinorVersion;
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
    public long getSnapLength() {
        return fSnapshotLength;
    }

    /**
     * Getter method for the datalink type of the file. This parameter is used
     * to determine higher-level protocols (Ethernet, WLAN, SLL).
     *
     * @return The datalink type of the file.
     */
    public long getDataLinkType() {
        return fDataLinkType;
    }

    /**
     * Getter method for the path of the file.
     *
     * @return The path of the file.
     */
    public String getPath() {
        return fPcapFilePath;
    }

    /**
     * Method that returns the total number of packets in the file.
     *
     * @return The total number of packets.
     * @throws IOException
     *             Thrown when some IO error occurs.
     * @throws BadPcapFileException
     *             Thrown when a packet header is invalid.
     */
    public synchronized long getTotalNbPackets() throws IOException, BadPcapFileException {
        if (fTotalNumberPackets == -1) {
            long rank = fCurrentRank;
            fCurrentRank = fFileIndex.floorKey(rank);

            // skip until end of file.
            while (hasNextPacket()) {
                skipNextPacket();
            }
            fTotalNumberPackets = fCurrentRank;
            fCurrentRank = rank;
            seekPacket(rank);
        }
        return fTotalNumberPackets;
    }

    /**
     * Getter method that returns the current rank in the file (the packet
     * number).
     *
     * @return The current rank.
     */
    public synchronized long getCurrentRank() {
        return fCurrentRank;
    }

    /**
     * Getter method that returns the timestamp precision of the file.
     *
     * @return The the timestamp precision of the file.
     */
    public PcapTimestampScale getTimestampPrecision() {
        return fTimestampPrecision;
    }

    @Override
    public void close() throws IOException {
        fFileChannel.close();
        fFileInputStream.close();
    }

}
