/*******************************************************************************
 * Copyright (c) 2014, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *   Viet-Hung Phan - Support pcapNg
 *******************************************************************************/

package org.eclipse.tracecompass.internal.pcap.core.trace;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.pcap.core.packet.BadPacketException;
import org.eclipse.tracecompass.internal.pcap.core.protocol.pcap.PcapPacket;
import org.eclipse.tracecompass.internal.pcap.core.util.PcapTimestampScale;

/**
 * Class that allows the interaction with a pcap file.
 *
 * @author Vincent Perot
 */
public abstract class PcapFile implements Closeable {

    private long fCurrentRank = 0;
    private long fTotalNumberPackets = -1;

    private FileChannel fFileChannel;
    private Path fPcapFilePath;
    private ByteOrder fByteOrder = ByteOrder.LITTLE_ENDIAN;
    private int fMajorVersion;
    private int fMinorVersion;

    private TreeMap<Long, Long> fFileIndex = new TreeMap<>();

    /**
     * Constructor of the PcapFile Class
     *
     * @param filePath
     *            The path file of the pcap file
     * @throws BadPcapFileException
     *             Thrown if it is not a pcap/pcapNg file
     * @throws IOException
     *             Thrown if there is an IO error while reading the file.
     *
     */
    public PcapFile(Path filePath) throws BadPcapFileException, IOException {
        fFileIndex = new TreeMap<>();
        fPcapFilePath = filePath;
        // Check file validity
        if (Files.notExists(fPcapFilePath) || !Files.isRegularFile(fPcapFilePath) ||
                Files.size(fPcapFilePath) < PcapFileValues.GLOBAL_HEADER_SIZE) {
            throw new BadPcapFileException("Bad Pcap File."); //$NON-NLS-1$
        }

        if (!Files.isReadable(fPcapFilePath)) {
            throw new BadPcapFileException("File is not readable."); //$NON-NLS-1$
        }

        // File is not empty. Try to open.
        fFileChannel = Objects.requireNonNull(FileChannel.open(fPcapFilePath));
    }

    /**
     * Method that allows the initialization of the parent data class for pcap
     * and pcapNg
     *
     * @param byteOrder
     *            byte order
     * @param majorVersion
     *            major version
     * @param minorVersion
     *            minor version
     */
    public void init(ByteOrder byteOrder,
            int majorVersion, int minorVersion) {
        fByteOrder = byteOrder;
        fMajorVersion = majorVersion;
        fMinorVersion = minorVersion;
    }

    /**
     * Method that allows the parsing of a pcap/pcapNg packet at the current
     * position.
     *
     * @return The parsed Pcap/PcapNg Packet.
     * @throws IOException
     *             Thrown when there is an error while reading the file.
     * @throws BadPcapFileException
     *             Thrown when a packet header is invalid.
     * @throws BadPacketException
     *             Thrown when the packet is erroneous.
     */
    public abstract @Nullable PcapPacket parseNextPacket() throws IOException, BadPcapFileException, BadPacketException;

    /**
     * Method that allows to skip a packet at the current position.
     *
     * @return true if a packet was skipped, false if end-of-file was reached
     *
     * @throws IOException
     *             Thrown when there is an error while reading the file.
     * @throws BadPcapFileException
     *             Thrown when a packet header is invalid.
     */
    public abstract boolean skipNextPacket() throws IOException, BadPcapFileException;

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
    public void seekPacket(long rank) throws IOException, BadPcapFileException {
        // Verify argument
        if (rank < 0) {
            throw new IllegalArgumentException();
        }

        TreeMap<Long, Long> fileIndex = getFileIndex();
        Long positionInBytes = fileIndex.get(rank);

        if (positionInBytes != null) {
            // Index is known. Move to position.
            getFileChannel().position(positionInBytes.longValue());
            setCurrentRank(rank);
        } else {
            // Index is unknown. Find the corresponding position.
            // Find closest index
            long floorRank = fileIndex.floorKey(rank);
            setCurrentRank(floorRank);
            positionInBytes = fileIndex.get(floorRank);
            if (positionInBytes != null) {
                getFileChannel().position(positionInBytes);
                // skip until wanted packet is found
                while (getCurrentRank() < rank && skipNextPacket()) {
                    // Do nothing
                }
            }
        }
    }

    /**
     * Method that returns the ts precision of a pcap/pcapNg packet data
     *
     * @return The ts precision of Pcap/PcapNg Packet.
     */
    public abstract PcapTimestampScale getTimestampPrecision();

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
            while (skipNextPacket()) {
                // Do nothing;
            }
            fTotalNumberPackets = fCurrentRank;
            fCurrentRank = rank;
            seekPacket(rank);
        }
        return fTotalNumberPackets;
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
     * Getter method for the path of the file.
     *
     * @return The path of the file.
     */
    public Path getPath() {
        return fPcapFilePath;
    }

    /**
     * Getter method that returns the current rank in the file (the packet
     * number).
     *
     * @return The current rank.
     */
    public long getCurrentRank() {
        return fCurrentRank;
    }

    /**
     * Setter method for the current rank value.
     *
     * @param currentRank
     *            The current packet number
     */
    public void setCurrentRank(long currentRank) {
        fCurrentRank = currentRank;
    }

    /**
     * Getter method that returns the current index of the file
     *
     * @return The current file index
     */
    public TreeMap<Long, Long> getFileIndex() {
        return fFileIndex;
    }

    /**
     * Method that closes the file.
     *
     */
    @Override
    public void close() throws IOException {
        getFileChannel().close();
    }

    /**
     * Getter method that returns the file channel of the file.
     *
     * @return the file channel
     */
    public FileChannel getFileChannel() {
        return fFileChannel;
    }
}
