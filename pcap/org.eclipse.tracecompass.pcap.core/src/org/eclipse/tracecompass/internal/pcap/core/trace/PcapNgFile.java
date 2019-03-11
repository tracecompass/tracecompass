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

package org.eclipse.tracecompass.internal.pcap.core.trace;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.pcap.core.packet.BadPacketException;
import org.eclipse.tracecompass.internal.pcap.core.protocol.pcap.PcapNgBlock;
import org.eclipse.tracecompass.internal.pcap.core.protocol.pcap.PcapNgInterface;
import org.eclipse.tracecompass.internal.pcap.core.protocol.pcap.PcapNgPacket;
import org.eclipse.tracecompass.internal.pcap.core.util.ConversionHelper;
import org.eclipse.tracecompass.internal.pcap.core.util.PcapTimestampScale;

/**
 * Class that allows the interaction with a pcapng file.
 */
public class PcapNgFile extends PcapFile {

    private static final int MICRO = 6;
    private static final long MICRO_TO_NANO = 1000L;
    private static final long SEC_TO_NANO = 1000000000L;

    /** The list of interfaces. The index in the list is the Interface ID. */
    private List<PcapNgInterface> fInterfaceList = new ArrayList<>();

    /**
     * Constructor of the PcapNgFile class where the parent is PcapFile class.
     * This method allows to initialize the pcapNG configuration from the first
     * two consecutive blocks SHB and IDB
     *
     * @param filePath
     *            The path to the pcapng file.
     *
     * @throws BadPcapFileException
     *             Thrown if the Pcapng File is not valid.
     * @throws IOException
     *             Thrown if there is an IO error while reading the file.
     */
    public PcapNgFile(Path filePath) throws BadPcapFileException, IOException {
        // Instantiate the parent class
        super(filePath);

        // -------------------------SHB block-----------------------------
        // Using an unique buffer to read SHB section header block which
        // is containing:
        // - 4 bytes of Block Type = 0x0A0D0D0A
        // - 4 bytes of Block Total Length
        // - 4 bytes of Byte-Order Magic = 0x1A2B3C4D
        // - 2 bytes of Major Version
        // - 2 bytes of Minor Version
        // - 8 bytes of Section Length
        // ---------------------------------------------------------------
        ByteBuffer header = ByteBuffer.allocate(PcapNgFileValues.BLOCK_HEADER_SIZE + PcapNgFileValues.SHB_MIN_BODY_SIZE);
        getFileChannel().read(header);
        header.flip();
        // By default, try little-endian byte order
        ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
        header.order(byteOrder);
        // Block Type
        int blockType = header.getInt();
        if (blockType != PcapNgFileValues.SHB) {
            this.close();
            throw new BadPcapFileException(String.format("0x%08x", blockType) + " Missing Section Header Block."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        // Block Total Length
        int blockLength = header.getInt();
        // Byte-Order Magic
        int magicNumber = header.getInt();
        if (magicNumber == Integer.reverseBytes(PcapNgFileValues.BYTE_ORDER_MAGIC)) {
            // little-endian assumption was wrong, use big-endian instead
            byteOrder = ByteOrder.BIG_ENDIAN;
            header.order(byteOrder);
            blockLength = Integer.reverseBytes(blockLength);
        } else if (magicNumber != PcapNgFileValues.BYTE_ORDER_MAGIC) {
            this.close();
            throw new BadPcapFileException(String.format("%08x", magicNumber) + " is not a valid Byte-Order Magic value."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        // Major Version
        int majorVersion = ConversionHelper.unsignedShortToInt(header.getShort());
        // Minor Version
        int minorVersion = ConversionHelper.unsignedShortToInt(header.getShort());

        // Set position at end of SHB block
        getFileChannel().position(blockLength);
        // Set initial file position for seeking next packet
        getFileIndex().put(0L, getFileChannel().position());
        // initialize the pcapNG configuration
        init(byteOrder, majorVersion, minorVersion);
    }

    /**
     * Method that allows getting a next pcapNg block at the current position
     * until a packet data is found, then parsing it.
     *
     * @return The parsed PcapNg Packet or a last block of the pcapNg file
     * @throws IOException
     *             Thrown when there is an error while reading the file.
     * @throws BadPcapFileException
     *             Thrown when a packet header is invalid.
     * @throws BadPacketException
     *             Thrown when the packet is erroneous.
     */
    @Override
    public synchronized @Nullable PcapNgPacket parseNextPacket() throws IOException, BadPcapFileException, BadPacketException {
        PcapNgPacket packet = null;
        long position = 0;
        while (packet == null) {
            PcapNgBlock block = parseBlock();
            if (block == null) {
                setCurrentRank(getCurrentRank() + 1);
                return null; // End of file reached
            }
            position = block.getPosition();
            switch (block.getBlockType()) {
            case PcapNgFileValues.SHB:
                return null; // ignore other sections
            case PcapNgFileValues.IDB:
                parseIDB(block);
                break;
            case PcapNgFileValues.PB:
                //$FALL-THROUGH$
            case PcapNgFileValues.EPB:
                packet = parseEPB(block);
                break;
            case PcapNgFileValues.SPB:
                packet = parseSPB(block);
                break;
            default:
                // ignore other blocks
                break;
            }
        }
        /* update index */
        packet.setIndex(getCurrentRank());
        getFileIndex().put(getCurrentRank(), position);
        setCurrentRank(getCurrentRank() + 1);
        return packet;
    }

    /**
     * Parse an IDB block and add the interface description to the interface
     * list if it does not already exist
     */
    private void parseIDB(PcapNgBlock block) {
        long position = block.getPosition();
        for (PcapNgInterface id : fInterfaceList) {
            if (id.getPosition() == position) {
                // This IDB has already been parsed
                return;
            }
        }
        ByteBuffer body = block.getBlockBody();
        short linkType = body.getShort();
        body.getShort(); // Reserved
        int snapLen = body.getInt();
        ByteBuffer options = Objects.requireNonNull(body.slice());
        options.order(body.order());
        ByteBuffer tsResolValue = getOptionValue(options, PcapNgFileValues.IDB_IFTSRESOL_CODE);
        byte tsResol = tsResolValue == null ? PcapNgFileValues.IDB_IFTSRESOL_DEFAULT : tsResolValue.get();
        ByteBuffer tsOffsetValue = getOptionValue(options, PcapNgFileValues.IDB_IFTSOFFSET_CODE);
        long tsOffset = tsOffsetValue == null ? 0 : tsOffsetValue.getLong();
        PcapNgInterface id = new PcapNgInterface(position, linkType, snapLen, tsResol, tsOffset);
        fInterfaceList.add(id);
        fInterfaceList.sort(Comparator.comparingLong(i -> i.getPosition()));
    }

    /**
     * Parse an EPB (or PB) block and return a PcapNgPacket
     */
    private PcapNgPacket parseEPB(PcapNgBlock block) throws BadPacketException {
        ByteBuffer body = block.getBlockBody();
        int interfaceID;
        if (block.getBlockType() == PcapNgFileValues.EPB) {
            // EPB: Interface ID (4 octets)
            interfaceID = body.getInt();
        } else {
            // PB: Interface ID (2 octets)
            interfaceID = ConversionHelper.unsignedShortToInt(body.getShort());
            // PB: Drops Count (2 octets)
            body.getShort();
        }
        if (interfaceID >= fInterfaceList.size()) {
            throw new BadPacketException("Undefined Interface ID: " + interfaceID); //$NON-NLS-1$
        }
        PcapNgInterface interfaceDesc = fInterfaceList.get(interfaceID);
        long timestampHigh = ConversionHelper.unsignedIntToLong(body.getInt());
        long timestampLow = ConversionHelper.unsignedIntToLong(body.getInt());
        long timestamp = (timestampHigh << 32) + timestampLow;
        long timestampNs = timestampToNs(timestamp, interfaceDesc);
        int capturedLength = body.getInt();
        int originalLength = body.getInt();
        ByteBuffer packetData = body.slice();
        packetData.limit(capturedLength);
        packetData.order(ByteOrder.BIG_ENDIAN);
        return new PcapNgPacket(this, interfaceDesc, timestampNs, originalLength, packetData);
    }

    /**
     * Parse an SPB block and return a PcapNgPacket
     */
    private PcapNgPacket parseSPB(PcapNgBlock block) throws BadPacketException {
        ByteBuffer body = block.getBlockBody();
        int interfaceID = 0;
        if (interfaceID >= fInterfaceList.size()) {
            throw new BadPacketException("Undefined Interface ID: " + interfaceID); //$NON-NLS-1$
        }
        PcapNgInterface interfaceDesc = fInterfaceList.get(interfaceID);
        int originalLength = body.getInt();
        // Captured length is minimum of Original Packet Length and SnapLen
        int capturedLength = Math.min(originalLength, interfaceDesc.getSnapLen());
        ByteBuffer packetData = body.slice();
        packetData.limit(capturedLength);
        packetData.order(ByteOrder.BIG_ENDIAN);
        return new PcapNgPacket(this, interfaceDesc, 0L, originalLength, packetData);
    }

    /**
     * Get an option value for a given option code from an options list
     *
     * @param options
     *            options list byte buffer
     * @param code
     *            option code
     * @return a byte buffer containing the option value, or null if not found
     */
    private static @Nullable ByteBuffer getOptionValue(ByteBuffer options, int code) {
        options.rewind();
        while (options.remaining() > 0) {
            short optionCode = options.getShort();
            if (optionCode == PcapNgFileValues.ENDOFOPT_CODE) {
                break;
            }
            int length = ConversionHelper.unsignedShortToInt(options.getShort());
            if (optionCode == code) {
                ByteBuffer value = options.slice();
                value.limit(length);
                value.order(options.order());
                return value;
            }
            if (length % 4 != 0) {
                // pad to 32-bit boundary
                length += (4 - length % 4);
            }
            options.position(options.position() + length);
        }
        return null;
    }

    private static long timestampToNs(long timestamp, PcapNgInterface interfaceDesc) {
        byte tsResol = interfaceDesc.getTsResol();
        long tsOffset = interfaceDesc.getTsOffset();
        if (tsResol == MICRO) {
            // default resolution: microseconds
            return timestamp * MICRO_TO_NANO + tsOffset * SEC_TO_NANO;
        }
        BigDecimal divisor;
        if ((tsResol & PcapNgFileValues.IDB_IFTSRESOL_MSB) == 0) {
            // negative power of 10
            divisor = BigDecimal.TEN.pow(tsResol);
        } else {
            // negative power of 2
            tsResol &= ~PcapNgFileValues.IDB_IFTSRESOL_MSB;
            divisor = BigDecimal.valueOf(2).pow(tsResol);
        }
        return BigDecimal.valueOf(timestamp)
                .divide(divisor)
                .add(BigDecimal.valueOf(tsOffset))
                .multiply(BigDecimal.valueOf(SEC_TO_NANO))
                .longValue();
    }

    /**
     * Getter method that returns the default timestamp precision
     *
     * @return the timestamp precision of the file.
     */
    @Override
    public PcapTimestampScale getTimestampPrecision() {
        return PcapTimestampScale.NANOSECOND;
    }

    /**
     * Method that allows parsing a pcapNg block at the current position
     *
     * @param fileChannel
     *            The pcapNg file.
     * @param byteOrder
     *            The byte order
     *
     * @return A new object PcapNgBlock that contains the block type, block
     *         total length, and block body.
     * @throws IOException
     *             Thrown when there is an error while reading the file.
     * @throws BadPcapFileException
     *             Thrown when the block is erroneous.
     */
    private @Nullable PcapNgBlock parseBlock() throws IOException, BadPcapFileException {
        long position = getFileChannel().position();
        if (getFileChannel().size() - position < PcapNgFileValues.BLOCK_HEADER_SIZE) {
            return null; // End of file reached
        }
        ByteOrder byteOrder = getByteOrder();

        ByteBuffer blockHeader = ByteBuffer.allocate(PcapNgFileValues.BLOCK_HEADER_SIZE);
        blockHeader.order(byteOrder);
        // Read block header from the current position based on byte order
        getFileChannel().read(blockHeader);
        blockHeader.flip();
        // Get block type
        int blockType = blockHeader.getInt();
        // Get block length
        int blockLength = blockHeader.getInt();

        // Read the body of the block or packet data
        ByteBuffer blockBody = ByteBuffer.allocate(blockLength - PcapNgFileValues.BLOCK_HEADER_SIZE - PcapNgFileValues.BLOCK_FOOTER_SIZE);
        blockBody.order(byteOrder);
        // Read block body from the current position based on byte order
        getFileChannel().read(blockBody);
        blockBody.flip();

        ByteBuffer blockFooter = ByteBuffer.allocate(PcapNgFileValues.BLOCK_FOOTER_SIZE);
        blockFooter.order(byteOrder);
        getFileChannel().read(blockFooter);
        blockFooter.flip();
        int blockLengthFooter = blockFooter.getInt();
        if (blockLengthFooter != blockLength) {
            throw new BadPcapFileException("Inconsistent Block Total Length"); //$NON-NLS-1$
        }
        return new PcapNgBlock(position, blockType, blockLength, blockBody);
    }

    @Override
    public synchronized boolean skipNextPacket() throws IOException, BadPcapFileException {
        ByteOrder byteOrder = getByteOrder();
        long position = getFileChannel().position();
        while (getFileChannel().size() - position >= PcapNgFileValues.BLOCK_HEADER_SIZE) {
            ByteBuffer blockHeader = ByteBuffer.allocate(PcapNgFileValues.BLOCK_HEADER_SIZE);
            getFileChannel().read(blockHeader);
            blockHeader.flip();
            blockHeader.order(byteOrder);
            // Get block type
            int blockType = blockHeader.getInt();
            // Get block length
            int blockLength = blockHeader.getInt();

            if (blockType == PcapNgFileValues.IDB) {
                getFileChannel().position(position);
                PcapNgBlock block = parseBlock();
                if (block != null) {
                    parseIDB(block);
                }
            }

            getFileChannel().position(position + blockLength);
            if (blockType == PcapNgFileValues.SHB) {
                break; // ignore other sections
            }
            if (blockType == PcapNgFileValues.EPB ||
                    blockType == PcapNgFileValues.SPB ||
                    blockType == PcapNgFileValues.PB) {
                getFileIndex().put(getCurrentRank(), position);
                setCurrentRank(getCurrentRank() + 1);
                return true;
            }
            position += blockLength;
        }
        setCurrentRank(getCurrentRank() + 1);
        return false;
    }
}
