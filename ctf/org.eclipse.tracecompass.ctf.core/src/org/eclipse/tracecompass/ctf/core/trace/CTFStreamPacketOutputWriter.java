/*******************************************************************************
 * Copyright (c) 2015, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.ctf.core.trace;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.StandardOpenOption;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.CTFStrings;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.types.Definition;
import org.eclipse.tracecompass.ctf.core.event.types.FloatDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.FloatDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.ICompositeDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.ISimpleDatatypeDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.SimpleDatatypeDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StringDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;
import org.eclipse.tracecompass.internal.ctf.core.Activator;
import org.eclipse.tracecompass.internal.ctf.core.SafeMappedByteBuffer;
import org.eclipse.tracecompass.internal.ctf.core.trace.CTFPacketReader;
import org.eclipse.tracecompass.internal.ctf.core.trace.StreamInputPacketIndexEntry;

/**
 * CTF trace packet writer.
 *
 * @author Bernd Hufmann
 * @since 1.0
 */
public class CTFStreamPacketOutputWriter {

    private CTFStreamInput fStreamInput;

    /**
     * Default constructor
     */
    public CTFStreamPacketOutputWriter() {
        throw new UnsupportedOperationException();
    }

    /**
     * Writer output
     *
     * @param streamInput
     *            the reference input stream to copy
     * @since 3.0
     */
    public CTFStreamPacketOutputWriter(@NonNull CTFStreamInput streamInput) {
        fStreamInput = streamInput;
    }

    /**
     * Writes a stream packet to the output file channel based on the packet
     * descriptor information.
     *
     * @param byteBuffer
     *            a byte buffer with packet to write
     * @param output
     *            a file channel
     * @throws IOException
     *             if a reading or writing error occurs
     */
    public void writePacket(ByteBuffer byteBuffer, FileChannel output) throws IOException {
        output.write(byteBuffer);
    }

    /**
     * Writes a stream packet to the output file channel based on the packet
     * descriptor information.
     *
     * @param entry
     *            the packet descriptor
     * @param output
     *            the file channel to write to
     * @param initialLost
     *            the offset initial lost events as the field is cumulative
     *
     * @throws IOException
     *             if a reading or writing error occurs
     * @throws CTFException
     *             ctf reading error
     * @since 3.0
     */
    public void writePacket(ICTFPacketDescriptor entry, FileChannel output, long initialLost) throws IOException, CTFException {
        long startOffsetBits = entry.getPayloadStartBits();
        long endOffsetBits = entry.getContentSizeBits();
        StructDefinition context = null;
        File file = fStreamInput.getFile();
        try (FileChannel fc = FileChannel.open(file.toPath(), StandardOpenOption.READ);) {
            ByteBuffer bb = SafeMappedByteBuffer.map(fc, FileChannel.MapMode.READ_ONLY, entry.getOffsetBytes(), (long) Math.ceil(entry.getContentSizeBits() / (double) Byte.SIZE));
            BitBuffer bbInput = new BitBuffer(bb);
            bbInput.position(entry.getPayloadStartBits());
            ICTFStream stream = fStreamInput.getStream();
            CTFTrace trace = stream.getTrace();
            CTFPacketReader currentPacketReader = new CTFPacketReader(
                    bbInput,
                    entry,
                    stream.getEventDeclarations(),
                    stream.getEventHeaderDeclaration(),
                    stream.getEventContextDecl(),
                    trace.getPacketHeaderDef(),
                    trace);
            if (entry instanceof StreamInputPacketIndexEntry) {
                context = ((StreamInputPacketIndexEntry) entry).getStreamPacketContextDef();
            }
            if (context == null) {
                return;
            }
            ICompositeDefinition tracePacketHeader = currentPacketReader.getTracePacketHeader();
            writeCustomPacket(entry, fc, output, startOffsetBits, endOffsetBits, context, tracePacketHeader, entry.getTimestampBegin(), initialLost);
        }
    }

    /**
     * Make a packet and write it to the fileChannel
     *
     * @param entry
     *            the reference packet descriptor
     * @param startTime
     *            startTime of the packet
     * @param endTime
     *            endTime of the packet
     * @param initialLost
     *            Initial quantity of lost events
     * @param output
     *            the file channel to write to
     * @throws IOException
     *             if a reading or writing error occurs
     * @throws CTFException
     *             ctf reading error
     * @since 3.0
     */
    public void writePacket(ICTFPacketDescriptor entry, long startTime, long endTime, long initialLost, FileChannel output) throws IOException, CTFException {
        if (entry.getTimestampBegin() > endTime || entry.getTimestampEnd() < startTime || startTime > endTime) {
            throw new IllegalStateException();
        }
        long startOffsetBits = entry.getPayloadStartBits();
        long endOffsetBits = entry.getContentSizeBits();
        StructDefinition context = null;
        File file = fStreamInput.getFile();
        try (FileChannel fc = FileChannel.open(file.toPath(), StandardOpenOption.READ);) {
            ByteBuffer bb = SafeMappedByteBuffer.map(fc, FileChannel.MapMode.READ_ONLY, entry.getOffsetBytes(), (long) Math.ceil(entry.getContentSizeBits() / (double) Byte.SIZE));
            BitBuffer bbInput = new BitBuffer(bb);
            bbInput.position(entry.getPayloadStartBits());
            ICTFStream stream = fStreamInput.getStream();
            CTFTrace trace = stream.getTrace();
            CTFPacketReader currentPacketReader = new CTFPacketReader(
                    bbInput,
                    entry,
                    stream.getEventDeclarations(),
                    stream.getEventHeaderDeclaration(),
                    stream.getEventContextDecl(),
                    trace.getPacketHeaderDef(),
                    trace);
            long lastLargeTimestamp = entry.getTimestampBegin();
            if (entry instanceof StreamInputPacketIndexEntry) {
                context = ((StreamInputPacketIndexEntry) entry).getStreamPacketContextDef();
            }
            if (context == null) {
                return;
            }
            boolean startIsSet = false;
            while (currentPacketReader.hasMoreEvents()) {

                IEventDefinition event = currentPacketReader.readNextEvent();
                long timestampInCycles = event.getTimestamp();
                if (!startIsSet) {
                    // handle "P" and "I" framed timestamps. overwrite the timestamp if there is one
                    // before the trim
                    ICompositeDefinition eventHeader = event.getEventHeader();
                    Definition def = null;
                    if(eventHeader != null) {
                        def = eventHeader.getDefinition(CTFStrings.TIMESTAMP);
                    }
                    if (def == null && eventHeader instanceof StructDefinition) {
                        StructDefinition structDefinition = (StructDefinition) eventHeader;
                        def = structDefinition.lookupDefinition(CTFStrings.TIMESTAMP);
                    }
                    Definition definition = ((StreamInputPacketIndexEntry) entry).getStreamPacketContextDef().getDefinition(CTFStrings.TIMESTAMP_BEGIN);
                    if (definition != null && def != null && def.size() == definition.size()) {
                        lastLargeTimestamp = timestampInCycles;
                    }
                }
                if (timestampInCycles >= startTime && !startIsSet) {
                    startOffsetBits = currentPacketReader.getLocation();
                    startIsSet = true;
                }
                if (timestampInCycles > endTime) {
                    endOffsetBits = currentPacketReader.getLocation();
                    break;
                }
            }
            if (startOffsetBits >= endOffsetBits) {
                return;
            }
            ICompositeDefinition tracePacketHeader = currentPacketReader.getTracePacketHeader();
            writeCustomPacket(entry, fc, output, startOffsetBits, endOffsetBits, context, tracePacketHeader, lastLargeTimestamp, initialLost);
        }
    }

    private static void writeCustomPacket(ICTFPacketDescriptor entry, FileChannel source, FileChannel output, long startOffsetBits, long endOffsetBits, StructDefinition packetContext, ICompositeDefinition tracePacketHeader, long startTime,
            long initialLost)
            throws IOException, CTFException {
        ByteBuffer inBuffer = SafeMappedByteBuffer.map(source, MapMode.READ_ONLY, entry.getOffsetBytes(), (long) Math.ceil(entry.getContentSizeBits() / (double) Byte.SIZE));
        int headerSize = (int) (tracePacketHeader == null ? 0 : tracePacketHeader.size());
        int packetSize = (int) (headerSize + packetContext.size() + endOffsetBits - startOffsetBits);
        byte[] toWrite = new byte[packetSize / 8];
        ByteBuffer buffer = ByteBuffer.wrap(toWrite);
        byte[] header = new byte[headerSize / 8];
        byte[] body = new byte[(int) (endOffsetBits - startOffsetBits) / 8];
        inBuffer.get(header);
        inBuffer.position((int) (startOffsetBits / 8));
        inBuffer.get(body);
        buffer.put(header);
        writeContext(startTime, packetContext, packetSize, initialLost, buffer);
        buffer.position((int) (headerSize + packetContext.size()) / 8);
        buffer.put(body);
        output.write(ByteBuffer.wrap(toWrite));
    }

    private static void writeContext(long startTime, StructDefinition context, int newPacketSize, long initialLost, @NonNull ByteBuffer buffer) throws CTFException {
        BitBuffer bb = new BitBuffer(buffer);
        bb.position(buffer.position() * 8L);
        for (String field : context.getFieldNames()) {
            Definition def = context.getDefinition(field);
            IDeclaration declaration = def.getDeclaration();
            align(declaration.getAlignment(), bb);
            if (declaration instanceof ISimpleDatatypeDeclaration) {
                bb.setByteOrder(((ISimpleDatatypeDeclaration) declaration).getByteOrder());
            }
            if (def instanceof SimpleDatatypeDefinition) {
                SimpleDatatypeDefinition simpleDef = (SimpleDatatypeDefinition) def;
                int size = (int) simpleDef.size();
                if (field.equals(CTFStrings.PACKET_SIZE) || field.equals(CTFStrings.CONTENT_SIZE)) {
                    bb.putLong(size, newPacketSize);
                } else if (field.equals(CTFStrings.TIMESTAMP_BEGIN)) {
                    bb.putLong(size, startTime);
                } else if (field.equals(CTFStrings.EVENTS_DISCARDED)) {
                    bb.putLong(size, simpleDef.getIntegerValue() - initialLost);
                } else {
                    bb.putLong(size, simpleDef.getIntegerValue());
                }
            } else if (def instanceof StringDefinition) {
                writeString(bb, def);
            } else if (def instanceof FloatDefinition) {
                writeFloat(bb, def);
            } else {
                buffer.put(new byte[(int) (def.size() / 8)]);
                bb.position(buffer.position() * 8L);
            }
        }
        buffer.position((int) (bb.position() / 8));
    }

    private static void align(long align, BitBuffer input) throws CTFException {
        long mask = align - 1;
        /*
         * The alignment is a power of 2
         */
        long pos = input.position();
        if ((pos & mask) == 0) {
            return;
        }
        pos = (pos + mask) & ~mask;
        input.position(pos);
    }

    private static void writeFloat(BitBuffer bb, Definition def) throws CTFException {
        FloatDefinition floatDefinition = (FloatDefinition) def;
        double value = floatDefinition.getValue();
        FloatDeclaration declaration = floatDefinition.getDeclaration();
        int exponent = declaration.getExponent();
        int mantissa = declaration.getMantissa();
        if (mantissa == 23 && exponent == 8) {
            bb.putInt(Float.SIZE, Float.floatToIntBits((float) value));
        } else if (mantissa == 52 && exponent == 11) {
            bb.putLong(Double.SIZE, Double.doubleToLongBits(value));
        } else {
            // Inaccurate, but won't break the trace
            bb.putLong((int) floatDefinition.size(), Double.doubleToRawLongBits(value));
        }
    }

    private static void writeString(BitBuffer bb, Definition def) throws CTFException {
        StringDefinition stringDefinition = (StringDefinition) def;
        // may be encoded differently, but play it safe first.
        byte[] bytes = stringDefinition.toString().getBytes();
        try {
            bytes = stringDefinition.toString().getBytes(stringDefinition.getDeclaration().getEncoding().toString());
        } catch (UnsupportedEncodingException e) {
            Activator.log("Writing packet made an encoding error! " + e.getMessage()); //$NON-NLS-1$
        }
        bb.put(bytes);
        bb.put((byte) 0);
    }

}
