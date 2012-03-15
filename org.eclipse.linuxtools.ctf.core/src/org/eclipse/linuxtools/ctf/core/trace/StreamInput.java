/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.trace;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.UUID;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.types.ArrayDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;

/**
 * <b><u>StreamInput</u></b>
 * <p>
 * Represents a trace file that belongs to a certain stream.
 */
public class StreamInput implements IDefinitionScope {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The associated Stream
     */
    private final Stream stream;

    /**
     * FileChannel to the trace file
     */
    private final FileChannel fileChannel;

    /**
     * Information on the file (used for debugging)
     */
    public final File file;

    /**
     * The packet index of this input
     */
    private final StreamInputPacketIndex index = new StreamInputPacketIndex();

    private long timestampEnd;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a StreamInput.
     *
     * @param stream
     *            The stream to which this StreamInput belongs to.
     * @param fileChannel
     *            The FileChannel to the trace file.
     * @param file
     *            Information about the trace file (for debugging purposes).
     */
    public StreamInput(Stream stream, FileChannel fileChannel, File file) {
        this.stream = stream;
        this.fileChannel = fileChannel;
        this.file = file;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    public Stream getStream() {
        return stream;
    }

    public StreamInputPacketIndex getIndex() {
        return index;
    }

    public FileChannel getFileChannel() {
        return fileChannel;
    }

    public String getFilename() {
        return file.getName();
    }

    public long getTimestampEnd() {
        return timestampEnd;
    }

    public void setTimestampEnd(long timestampEnd) {
        this.timestampEnd = timestampEnd;
    }

    @Override
    public String getPath() {
        return ""; //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Definition lookupDefinition(String lookupPath) {
        /* TODO: lookup in different dynamic scopes is not supported yet. */
        return null;
    }

    /**
     * Create the index for this trace file.
     *
     * @throws CTFReaderException
     */
    public void createIndex() throws CTFReaderException {
        /*
         * The size of the file in bytes
         */
        long fileSizeBytes = 0;
        try {
            fileSizeBytes = fileChannel.size();
        } catch (IOException e) {
            throw new CTFReaderException(e);
        }

        /*
         * Offset of the current packet in bytes
         */
        long packetOffsetBytes = 0;

        /*
         * Initial size, it should map at least the packet header + context
         * size.
         *
         * TODO: use a less arbitrary size.
         */
        long mapSize = 4096;

        /*
         * Definition of trace packet header
         */
        StructDefinition tracePacketHeaderDef = null;

        /*
         * Definition of trace stream packet context
         */
        StructDefinition streamPacketContextDef = null;

        /*
         * The BitBuffer to extract data from the StreamInput
         */
        BitBuffer bitBuffer = new BitBuffer();
        bitBuffer.order(this.getStream().getTrace().getByteOrder());

        /*
         * Create the definitions we need to read the packet headers + contexts
         */
        if (getStream().getTrace().getPacketHeader() != null) {
            tracePacketHeaderDef = getStream().getTrace().getPacketHeader().createDefinition(
                    this, "trace.packet.header"); //$NON-NLS-1$
        }

        if (getStream().getPacketContextDecl() != null) {
            streamPacketContextDef = getStream().getPacketContextDecl().createDefinition(
                    this, "stream.packet.context"); //$NON-NLS-1$
        }

        /*
         * Scan through the packets of the file.
         */
        while (packetOffsetBytes < fileSizeBytes) {
            /*
             * If there is less data remaining than what we want to map, reduce
             * the map size.
             */
            if ((fileSizeBytes - packetOffsetBytes) < mapSize) {
                mapSize = fileSizeBytes - packetOffsetBytes;
            }

            /*
             * Map the packet.
             */
            MappedByteBuffer bb;
            try {
                bb = fileChannel.map(MapMode.READ_ONLY, packetOffsetBytes,
                        mapSize);
            } catch (IOException e) {
                throw new CTFReaderException(e);
            }
            bitBuffer.setByteBuffer(bb);

            /*
             * Create the index entry
             */
            StreamInputPacketIndexEntry packetIndex = new StreamInputPacketIndexEntry(
                    packetOffsetBytes);

            /*
             * Read the trace packet header if it exists.
             */
            if (tracePacketHeaderDef != null) {
                tracePacketHeaderDef.read(bitBuffer);

                /*
                 * Check the CTF magic number
                 */
                IntegerDefinition magicDef = (IntegerDefinition) tracePacketHeaderDef.lookupDefinition("magic"); //$NON-NLS-1$
                if (magicDef != null) {
                    int magic = (int) magicDef.getValue();
                    if (magic != Utils.CTF_MAGIC) {
                        throw new CTFReaderException(
                                "CTF magic mismatch " + Integer.toHexString(magic) + " vs " + Integer.toHexString(Utils.CTF_MAGIC)); //$NON-NLS-1$//$NON-NLS-2$
                    }

                }

                /*
                 * Check the trace UUID
                 */
                ArrayDefinition uuidDef = (ArrayDefinition) tracePacketHeaderDef.lookupDefinition("uuid"); //$NON-NLS-1$
                if (uuidDef != null) {
                    byte[] uuidArray = new byte[16];

                    for (int i = 0; i < 16; i++) {
                        IntegerDefinition uuidByteDef = (IntegerDefinition) uuidDef.getElem(i);
                        uuidArray[i] = (byte) uuidByteDef.getValue();
                    }

                    UUID uuid = Utils.makeUUID(uuidArray);

                    if (!getStream().getTrace().getUUID().equals(uuid)) {
                        throw new CTFReaderException("UUID mismatch"); //$NON-NLS-1$
                    }
                }

                /*
                 * Check that the stream id did not change
                 */
                IntegerDefinition streamIDDef = (IntegerDefinition) tracePacketHeaderDef.lookupDefinition("stream_id"); //$NON-NLS-1$
                if (streamIDDef != null) {
                    long streamID = streamIDDef.getValue();

                    if (streamID != getStream().getId()) {
                        throw new CTFReaderException(
                                "Stream ID changing within a StreamInput"); //$NON-NLS-1$
                    }
                }
            }

            /*
             * Read the stream packet context if it exists.
             */
            if (streamPacketContextDef != null) {
                streamPacketContextDef.read(bitBuffer);

                /*
                 * Read the content size in bits
                 */
                IntegerDefinition contentSizeDef = (IntegerDefinition) streamPacketContextDef.lookupDefinition("content_size"); //$NON-NLS-1$
                if (contentSizeDef != null) {
                    packetIndex.contentSizeBits = (int) contentSizeDef.getValue();
                } else {
                    packetIndex.contentSizeBits = (int) (fileSizeBytes * 8);
                }

                /*
                 * Read the packet size in bits
                 */
                IntegerDefinition packetSizeDef = (IntegerDefinition) streamPacketContextDef.lookupDefinition("packet_size"); //$NON-NLS-1$
                if (packetSizeDef != null) {
                    packetIndex.packetSizeBits = (int) packetSizeDef.getValue();
                } else {
                    if (packetIndex.contentSizeBits != 0) {
                        packetIndex.packetSizeBits = packetIndex.contentSizeBits;
                    } else {
                        packetIndex.packetSizeBits = (int) (fileSizeBytes * 8);
                    }
                }

                /*
                 * Read the begin timestamp
                 */
                IntegerDefinition timestampBeginDef = (IntegerDefinition) streamPacketContextDef.lookupDefinition("timestamp_begin"); //$NON-NLS-1$
                if (timestampBeginDef != null) {
                    packetIndex.timestampBegin = timestampBeginDef.getValue();
                }

                /*
                 * Read the end timestamp
                 */
                IntegerDefinition timestampEndDef = (IntegerDefinition) streamPacketContextDef.lookupDefinition("timestamp_end"); //$NON-NLS-1$
                if (timestampEndDef != null) {
                    setTimestampEnd(packetIndex.timestampEnd = timestampEndDef.getValue());
                }
            } else {
                /*
                 * If there is no packet context, infer the content and packet
                 * size from the file size (assume that there is only one packet
                 * and no padding)
                 */
                packetIndex.contentSizeBits = (int) (fileSizeBytes * 8);
                packetIndex.packetSizeBits = (int) (fileSizeBytes * 8);
            }

            /* Basic validation */
            if (packetIndex.contentSizeBits > packetIndex.packetSizeBits) {
                throw new CTFReaderException("Content size > packet size"); //$NON-NLS-1$
            }

            if (packetIndex.packetSizeBits > ((fileSizeBytes - packetIndex.offsetBytes) * 8)) {
                throw new CTFReaderException(
                        "Not enough data remaining in the file for the size of this packet"); //$NON-NLS-1$
            }

            /*
             * Offset in the file, in bits
             */
            packetIndex.dataOffsetBits = bitBuffer.position();

            /*
             * Add the packet index entry to the index
             */
            index.addEntry(packetIndex);

            /*
             * Update the counting packet offset
             */
            packetOffsetBytes += (packetIndex.packetSizeBits + 7) / 8;

        }
    }

}
