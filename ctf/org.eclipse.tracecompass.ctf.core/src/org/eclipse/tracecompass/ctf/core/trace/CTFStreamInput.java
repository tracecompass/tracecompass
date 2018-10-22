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

package org.eclipse.tracecompass.ctf.core.trace;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.event.scope.ILexicalScope;
import org.eclipse.tracecompass.ctf.core.event.scope.LexicalScope;
import org.eclipse.tracecompass.ctf.core.event.types.AbstractArrayDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.Definition;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;
import org.eclipse.tracecompass.internal.ctf.core.Activator;
import org.eclipse.tracecompass.internal.ctf.core.SafeMappedByteBuffer;
import org.eclipse.tracecompass.internal.ctf.core.trace.StreamInputPacketIndex;
import org.eclipse.tracecompass.internal.ctf.core.trace.StreamInputPacketIndexEntry;
import org.eclipse.tracecompass.internal.ctf.core.trace.Utils;

/**
 * <b><u>StreamInput</u></b>
 * <p>
 * Represents a trace file that belongs to a certain stream.
 */
@NonNullByDefault
public class CTFStreamInput implements IDefinitionScope {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static final int MAP_SIZE = 4096;

    /**
     * The associated Stream
     */
    private final ICTFStream fStream;

    /**
     * Information on the file (used for debugging)
     */
    private final File fFile;

    /**
     * The file name
     */
    private final String fFileName;

    /**
     * The packet index of this input
     */
    private final StreamInputPacketIndex fIndex;

    private long fTimestampEnd;

    /**
     * Definition of trace packet header
     */
    private final StructDeclaration fTracePacketHeaderDecl;

    /**
     * Definition of trace stream packet context
     */
    private final StructDeclaration fStreamPacketContextDecl;

    /**
     * Total number of lost events in this stream
     */
    private long fLostSoFar = 0;

    private boolean fUUIDMismatchWarning = false;

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
     * @since 2.0
     */
    public CTFStreamInput(ICTFStream stream, File file) {
        fStream = stream;
        fFile = file;
        fFileName = fFile.getName();

        fIndex = new StreamInputPacketIndex();
        /*
         * Create the definitions we need to read the packet headers + contexts
         */
        StructDeclaration packetHeader = getStream().getTrace().getPacketHeader();
        if (packetHeader != null) {
            fTracePacketHeaderDecl = packetHeader;
        } else {
            fTracePacketHeaderDecl = new StructDeclaration(1);
        }
        StructDeclaration packetContextDecl = getStream().getPacketContextDecl();
        if (packetContextDecl != null) {
            fStreamPacketContextDecl = packetContextDecl;
        } else {
            fStreamPacketContextDecl = new StructDeclaration(1);
        }
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Gets the stream the streamInput wrapper is wrapping
     *
     * @return the stream the streamInput wrapper is wrapping
     * @since 2.0
     */
    public ICTFStream getStream() {
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
        return fFileName;
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
        return ILexicalScope.STREAM;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public @Nullable Definition lookupDefinition(@Nullable String lookupPath) {
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

    }

    /**
     * Adds the next packet header index entry to the index of a stream input.
     *
     * <strong>This method is slow and can corrupt data if not used
     * properly</strong>
     *
     * @return true if there are more packets to add
     * @throws CTFException
     *             If there was a problem reading the packed header
     */
    public boolean addPacketHeaderIndex() throws CTFException {
        long currentPosBits = 0L;
        if (!fIndex.isEmpty()) {
            ICTFPacketDescriptor pos = fIndex.lastElement();
            if (pos == null) {
                throw new IllegalStateException("Index contains null packet entries"); //$NON-NLS-1$
            }
            currentPosBits = pos.getOffsetBits() + pos.getPacketSizeBits();
        }
        if (currentPosBits < getStreamSizeBits()) {
            return fIndex.append(createPacketIndexEntry(currentPosBits));
        }
        return false;
    }

    private long getStreamSizeBits() {
        return fFile.length() * Byte.SIZE;
    }

    private ICTFPacketDescriptor createPacketIndexEntry(long dataOffsetbits)
            throws CTFException {

        try (FileChannel fc = FileChannel.open(fFile.toPath(), StandardOpenOption.READ)) {
            if (fc == null) {
                throw new IOException("Failed to create FileChannel"); //$NON-NLS-1$
            }
            BitBuffer bitBuffer = createBitBufferForPacketHeader(fc, dataOffsetbits);
            /*
             * Read the trace packet header if it exists.
             */
            parseTracePacketHeader(bitBuffer);

            /*
             * Read the stream packet context if it exists.
             */
            long size = fc.size();
            ICTFPacketDescriptor packetIndex = parsePacketContext(dataOffsetbits, size, bitBuffer);

            /* Basic validation */
            if (packetIndex.getContentSizeBits() > packetIndex.getPacketSizeBits()) {
                throw new CTFException("Content size (" + packetIndex.getContentSizeBits() + ") > packet size (" + packetIndex.getPacketSizeBits() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }

            if (packetIndex.getPacketSizeBits() > ((size * Byte.SIZE - packetIndex.getOffsetBits()))) {
                throw new CTFException("Not enough data remaining in the file for the size of this packet"); //$NON-NLS-1$
            }
            return packetIndex;
        } catch (IOException e) {
            throw new CTFException("Failed to create packet index entry", e); //$NON-NLS-1$
        } catch (CTFException e) {
            throw new CTFException("Cannot create packet entry at bit " + dataOffsetbits, e); //$NON-NLS-1$
        }
    }

    private BitBuffer createBitBufferForPacketHeader(FileChannel fc, long dataOffsetbits) throws CTFException, IOException {
        /*
         * create a packet bit buffer to read the packet header
         */
        int maximumSize = fStreamPacketContextDecl.getMaximumSize() + fTracePacketHeaderDecl.getMaximumSize();
        BitBuffer bitBuffer = new BitBuffer(createPacketBitBuffer(fc, dataOffsetbits/Byte.SIZE, maximumSize));
        bitBuffer.setByteOrder(getStream().getTrace().getByteOrder());
        return bitBuffer;
    }

    private static ByteBuffer getByteBufferAt(FileChannel fc, long position, long size) throws CTFException, IOException {
        return SafeMappedByteBuffer.map(fc, MapMode.READ_ONLY, position, size);
    }

    private static ByteBuffer createPacketBitBuffer(FileChannel fc,
            long packetOffsetBytes, long maxSize) throws CTFException, IOException {
        /*
         * If there is less data remaining than what we want to map, reduce the
         * map size.
         */
        long remain = fc.size() - packetOffsetBytes;
        /*
         * Initial size, it is the minimum of the the file size and the maximum
         * possible size of the
         */
        long mapSize = Math.min(remain, MAP_SIZE);
        if (maxSize < mapSize) {
            mapSize = maxSize;
        }

        /*
         * Map the packet.
         */
        try {
            return getByteBufferAt(fc, packetOffsetBytes, mapSize);
        } catch (IllegalArgumentException | IOException e) {
            throw new CTFException(e);
        }
    }

    private StructDefinition parseTracePacketHeader(
            BitBuffer bitBuffer) throws CTFException {

        StructDefinition tracePacketHeaderDef = fTracePacketHeaderDecl.createDefinition(fStream.getTrace(), ILexicalScope.TRACE_PACKET_HEADER, bitBuffer);

        /*
         * Check the CTF magic number
         */
        IntegerDefinition magicDef = (IntegerDefinition) tracePacketHeaderDef
                .lookupDefinition("magic"); //$NON-NLS-1$
        if (magicDef != null) {
            int magic = (int) magicDef.getValue();
            if (magic != Utils.CTF_MAGIC) {
                throw new CTFException(
                        "CTF magic mismatch " + Integer.toHexString(magic) + " vs " + Integer.toHexString(Utils.CTF_MAGIC)); //$NON-NLS-1$//$NON-NLS-2$
            }
        }

        /*
         * Check the trace UUID
         */
        AbstractArrayDefinition uuidDef =
                (AbstractArrayDefinition) tracePacketHeaderDef.lookupDefinition("uuid"); //$NON-NLS-1$
        if (uuidDef != null) {
            UUID uuid = Utils.getUUIDfromDefinition(uuidDef);

            if (!getStream().getTrace().getUUID().equals(uuid) && !fUUIDMismatchWarning ) {
                fUUIDMismatchWarning = true;
                Activator.log(IStatus.WARNING, "Reading CTF trace: UUID mismatch for trace " + getStream().getTrace()); //$NON-NLS-1$
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
                throw new CTFException("Stream ID changing within a StreamInput"); //$NON-NLS-1$
            }
        }
        return tracePacketHeaderDef;
    }

    private ICTFPacketDescriptor parsePacketContext(long dataOffsetBits, long fileSizeBytes,
            BitBuffer bitBuffer) throws CTFException {
        ICTFPacketDescriptor packetIndex;
        StructDefinition streamPacketContextDef = fStreamPacketContextDecl.createDefinition(this, ILexicalScope.STREAM_PACKET_CONTEXT, bitBuffer);
        packetIndex = new StreamInputPacketIndexEntry(dataOffsetBits, streamPacketContextDef, fileSizeBytes, fLostSoFar, bitBuffer.position());
        fLostSoFar = packetIndex.getLostEvents() + fLostSoFar;
        setTimestampEnd(packetIndex.getTimestampEnd());
        return packetIndex;
    }

    /**
     * Get the file
     *
     * @return the file
     * @since 1.0
     */
    public File getFile() {
        return fFile;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + fFile.hashCode();
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
        if (!(obj instanceof CTFStreamInput)) {
            return false;
        }
        CTFStreamInput other = (CTFStreamInput) obj;
        return (fFile.equals(other.fFile));
    }
}
