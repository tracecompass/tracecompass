/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.scope.LexicalScope;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.EnumDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.FloatDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StringDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.internal.ctf.core.event.types.ArrayDefinition;
import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInputPacketIndex;
import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInputPacketIndexEntry;

/**
 * <b><u>StreamInput</u></b>
 * <p>
 * Represents a trace file that belongs to a certain stream.
 *
 * @since 3.0
 */
public class CTFStreamInput implements IDefinitionScope, AutoCloseable {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The associated Stream
     */
    private final CTFStream fStream;

    /**
     * FileChannel to the trace file
     */
    private final FileChannel fFileChannel;

    /**
     * Information on the file (used for debugging)
     */
    private final File fFile;

    /**
     * The packet index of this input
     */
    private final StreamInputPacketIndex fIndex;

    private long fTimestampEnd;

    /**
     * Definition of trace packet header
     */
    private StructDeclaration fTracePacketHeaderDecl = null;

    /**
     * Definition of trace stream packet context
     */
    private StructDeclaration fStreamPacketContextDecl = null;

    /**
     * Total number of lost events in this stream
     */
    private long fLostSoFar = 0;

    /**
     * File input stream, the parent input file
     */
    private final FileInputStream fFileInputStream;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a StreamInput.
     *
     * @param stream
     *            The stream to which this StreamInput belongs to.
     * @param file
     *            Information about the trace file (for debugging purposes).
     * @throws CTFReaderException
     *             The file must exist
     */
    public CTFStreamInput(CTFStream stream, File file) throws CTFReaderException {
        fStream = stream;
        fFile = file;
        try {
            fFileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new CTFReaderException(e);
        }

        fFileChannel = fFileInputStream.getChannel();
        fIndex = new StreamInputPacketIndex();
    }

    @Override
    public void close() throws IOException {
        fFileChannel.close();
        fFileInputStream.close();
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Gets the stream the streamInput wrapper is wrapping
     *
     * @return the stream the streamInput wrapper is wrapping
     */
    public CTFStream getStream() {
        return fStream;
    }

    /**
     * The common streamInput Index
     *
     * @return the stream input Index
     */
    StreamInputPacketIndex getIndex() {
        return fIndex;
    }

    /**
     * Gets the filename of the streamInput file.
     *
     * @return the filename of the streaminput file.
     */
    public String getFilename() {
        return fFile.getName();
    }

    /**
     * Gets the last read timestamp of a stream. (this is not necessarily the
     * last time in the stream.)
     *
     * @return the last read timestamp
     */
    public long getTimestampEnd() {
        return fTimestampEnd;
    }

    /**
     * Sets the last read timestamp of a stream. (this is not necessarily the
     * last time in the stream.)
     *
     * @param timestampEnd
     *            the last read timestamp
     */
    public void setTimestampEnd(long timestampEnd) {
        fTimestampEnd = timestampEnd;
    }

    /**
     * Useless for streaminputs
     */
    @Override
    public LexicalScope getScopePath() {
        return LexicalScope.STREAM;
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
     */
    public void setupIndex() {

        /*
         * The BitBuffer to extract data from the StreamInput
         */
        BitBuffer bitBuffer = new BitBuffer();
        bitBuffer.setByteOrder(getStream().getTrace().getByteOrder());

        /*
         * Create the definitions we need to read the packet headers + contexts
         */
        if (getStream().getTrace().getPacketHeader() != null) {
            fTracePacketHeaderDecl = getStream().getTrace().getPacketHeader();
        }

        if (getStream().getPacketContextDecl() != null) {
            fStreamPacketContextDecl = getStream().getPacketContextDecl();
        }

    }

    /**
     * Adds the next packet header index entry to the index of a stream input.
     *
     * <strong>This method is slow and can corrupt data if not used
     * properly</strong>
     *
     * @return true if there are more packets to add
     * @throws CTFReaderException
     *             If there was a problem reading the packed header
     */
    public boolean addPacketHeaderIndex() throws CTFReaderException {
        long currentPos = 0L;
        if (!fIndex.getEntries().isEmpty()) {
            StreamInputPacketIndexEntry pos = fIndex.getEntries().lastElement();
            currentPos = computeNextOffset(pos);
        }
        long fileSize = getStreamSize();
        if (currentPos < fileSize) {

            StreamInputPacketIndexEntry packetIndex = new StreamInputPacketIndexEntry(
                    currentPos);
            createPacketIndexEntry(fileSize, currentPos, packetIndex,
                    fTracePacketHeaderDecl, fStreamPacketContextDecl);
            fIndex.addEntry(packetIndex);
            return true;
        }
        return false;
    }

    private long getStreamSize() {
        return fFile.length();
    }

    private long createPacketIndexEntry(long fileSizeBytes,
            long packetOffsetBytes, StreamInputPacketIndexEntry packetIndex,
            StructDeclaration tracePacketHeaderDecl,
            StructDeclaration streamPacketContextDecl)
            throws CTFReaderException {

        /*
         * create a packet bit buffer to read the packet header
         */
        BitBuffer bitBuffer = new BitBuffer(createPacketBitBuffer(fileSizeBytes, packetOffsetBytes, packetIndex));
        bitBuffer.setByteOrder(getStream().getTrace().getByteOrder());
        /*
         * Read the trace packet header if it exists.
         */
        if (tracePacketHeaderDecl != null) {
            parseTracePacketHeader(tracePacketHeaderDecl, bitBuffer);
        }

        /*
         * Read the stream packet context if it exists.
         */
        if (streamPacketContextDecl != null) {
            parsePacketContext(fileSizeBytes, streamPacketContextDecl,
                    bitBuffer, packetIndex);
        } else {
            setPacketContextNull(fileSizeBytes, packetIndex);
        }

        /* Basic validation */
        if (packetIndex.getContentSizeBits() > packetIndex.getPacketSizeBits()) {
            throw new CTFReaderException("Content size > packet size"); //$NON-NLS-1$
        }

        if (packetIndex.getPacketSizeBits() > ((fileSizeBytes - packetIndex
                .getOffsetBytes()) * 8)) {
            throw new CTFReaderException("Not enough data remaining in the file for the size of this packet"); //$NON-NLS-1$
        }

        /*
         * Offset in the file, in bits
         */
        packetIndex.setDataOffsetBits(bitBuffer.position());

        /*
         * Update the counting packet offset
         */
        return computeNextOffset(packetIndex);
    }

    /**
     * @param packetIndex
     * @return
     */
    private static long computeNextOffset(
            StreamInputPacketIndexEntry packetIndex) {
        return packetIndex.getOffsetBytes()
                + ((packetIndex.getPacketSizeBits() + 7) / 8);
    }

    @NonNull
    ByteBuffer getByteBufferAt(long position, long size) throws CTFReaderException, IOException {
        MappedByteBuffer map = fFileChannel.map(MapMode.READ_ONLY, position, size);
        if (map == null) {
            throw new CTFReaderException("Failed to allocate mapped byte buffer"); //$NON-NLS-1$
        }
        return map;
    }

    @NonNull
    private ByteBuffer createPacketBitBuffer(long fileSizeBytes,
            long packetOffsetBytes, StreamInputPacketIndexEntry packetIndex) throws CTFReaderException {
        /*
         * Initial size, it should map at least the packet header + context
         * size.
         *
         * TODO: use a less arbitrary size.
         */
        long mapSize = 4096;
        /*
         * If there is less data remaining than what we want to map, reduce the
         * map size.
         */
        if ((fileSizeBytes - packetIndex.getOffsetBytes()) < mapSize) {
            mapSize = fileSizeBytes - packetIndex.getOffsetBytes();
        }

        /*
         * Map the packet.
         */
        try {
            return getByteBufferAt(packetOffsetBytes, mapSize);
        } catch (IOException e) {
            throw new CTFReaderException(e);
        }
    }

    private void parseTracePacketHeader(StructDeclaration tracePacketHeaderDecl,
            @NonNull BitBuffer bitBuffer) throws CTFReaderException {
        StructDefinition tracePacketHeaderDef = tracePacketHeaderDecl.createDefinition(fStream.getTrace(), LexicalScope.TRACE_PACKET_HEADER, bitBuffer);

        /*
         * Check the CTF magic number
         */
        IntegerDefinition magicDef = (IntegerDefinition) tracePacketHeaderDef
                .lookupDefinition("magic"); //$NON-NLS-1$
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
        ArrayDefinition uuidDef =
                (ArrayDefinition) tracePacketHeaderDef.lookupDefinition("uuid"); //$NON-NLS-1$
        if (uuidDef != null) {
            UUID uuid = Utils.getUUIDfromDefinition(uuidDef);

            if (!getStream().getTrace().getUUID().equals(uuid)) {
                throw new CTFReaderException("UUID mismatch"); //$NON-NLS-1$
            }
        }

        /*
         * Check that the stream id did not change
         */
        IntegerDefinition streamIDDef = (IntegerDefinition) tracePacketHeaderDef
                .lookupDefinition("stream_id"); //$NON-NLS-1$
        if (streamIDDef != null) {
            long streamID = streamIDDef.getValue();

            if (streamID != getStream().getId()) {
                throw new CTFReaderException("Stream ID changing within a StreamInput"); //$NON-NLS-1$
            }
        }
    }

    private static void setPacketContextNull(long fileSizeBytes,
            StreamInputPacketIndexEntry packetIndex) {
        /*
         * If there is no packet context, infer the content and packet size from
         * the file size (assume that there is only one packet and no padding)
         */
        packetIndex.setContentSizeBits(fileSizeBytes * 8);
        packetIndex.setPacketSizeBits(fileSizeBytes * 8);
    }

    private void parsePacketContext(long fileSizeBytes,
            StructDeclaration streamPacketContextDecl, @NonNull BitBuffer bitBuffer,
            StreamInputPacketIndexEntry packetIndex) throws CTFReaderException {
        StructDefinition streamPacketContextDef = streamPacketContextDecl.createDefinition(this, LexicalScope.STREAM_PACKET_CONTEXT, bitBuffer);

        for (String field : streamPacketContextDef.getDeclaration()
                .getFieldsList()) {
            IDefinition id = streamPacketContextDef.lookupDefinition(field);
            if (id instanceof IntegerDefinition) {
                packetIndex.addAttribute(field,
                        ((IntegerDefinition) id).getValue());
            } else if (id instanceof FloatDefinition) {
                packetIndex.addAttribute(field,
                        ((FloatDefinition) id).getValue());
            } else if (id instanceof EnumDefinition) {
                packetIndex.addAttribute(field,
                        ((EnumDefinition) id).getValue());
            } else if (id instanceof StringDefinition) {
                packetIndex.addAttribute(field,
                        ((StringDefinition) id).getValue());
            }
        }

        Long contentSize = (Long) packetIndex.lookupAttribute("content_size"); //$NON-NLS-1$
        Long packetSize = (Long) packetIndex.lookupAttribute("packet_size"); //$NON-NLS-1$
        Long tsBegin = (Long) packetIndex.lookupAttribute("timestamp_begin"); //$NON-NLS-1$
        Long tsEnd = (Long) packetIndex.lookupAttribute("timestamp_end"); //$NON-NLS-1$
        String device = (String) packetIndex.lookupAttribute("device"); //$NON-NLS-1$
        // LTTng Specific
        Long cpuId = (Long) packetIndex.lookupAttribute("cpu_id"); //$NON-NLS-1$
        Long lostEvents = (Long) packetIndex.lookupAttribute("events_discarded"); //$NON-NLS-1$

        /* Read the content size in bits */
        if (contentSize != null) {
            packetIndex.setContentSizeBits(contentSize.intValue());
        } else if (packetSize != null) {
            packetIndex.setContentSizeBits(packetSize.longValue());
        } else {
            packetIndex.setContentSizeBits((int) (fileSizeBytes * 8));
        }

        /* Read the packet size in bits */
        if (packetSize != null) {
            packetIndex.setPacketSizeBits(packetSize.intValue());
        } else if (packetIndex.getContentSizeBits() != 0) {
            packetIndex.setPacketSizeBits(packetIndex.getContentSizeBits());
        } else {
            packetIndex.setPacketSizeBits((int) (fileSizeBytes * 8));
        }

        /* Read the begin timestamp */
        if (tsBegin != null) {
            packetIndex.setTimestampBegin(tsBegin.longValue());
        }

        /* Read the end timestamp */
        if (tsEnd != null) {
            if (tsEnd == -1) {
                tsEnd = Long.MAX_VALUE;
            }
            packetIndex.setTimestampEnd(tsEnd.longValue());
            setTimestampEnd(packetIndex.getTimestampEnd());
        }

        if (device != null) {
            packetIndex.setTarget(device);
        }

        if (cpuId != null) {
            packetIndex.setTarget("CPU" + cpuId.toString()); //$NON-NLS-1$
        }

        if (lostEvents != null) {
            packetIndex.setLostEvents(lostEvents - fLostSoFar);
            fLostSoFar = lostEvents;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((fFile == null) ? 0 : fFile.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CTFStreamInput)) {
            return false;
        }
        CTFStreamInput other = (CTFStreamInput) obj;
        if (fFile == null) {
            if (other.fFile != null) {
                return false;
            }
        } else if (!fFile.equals(other.fFile)) {
            return false;
        }
        return true;
    }

}
