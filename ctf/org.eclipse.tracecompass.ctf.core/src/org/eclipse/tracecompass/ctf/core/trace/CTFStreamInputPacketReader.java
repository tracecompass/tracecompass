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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.CTFStrings;
import org.eclipse.tracecompass.ctf.core.event.EventDefinition;
import org.eclipse.tracecompass.ctf.core.event.IEventDeclaration;
import org.eclipse.tracecompass.ctf.core.event.LostEventDeclaration;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.event.scope.ILexicalScope;
import org.eclipse.tracecompass.ctf.core.event.scope.LexicalScope;
import org.eclipse.tracecompass.ctf.core.event.types.Definition;
import org.eclipse.tracecompass.ctf.core.event.types.ICompositeDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IEventHeaderDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.SimpleDatatypeDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.VariantDefinition;
import org.eclipse.tracecompass.internal.ctf.core.SafeMappedByteBuffer;
import org.eclipse.tracecompass.internal.ctf.core.event.types.composite.EventHeaderDefinition;

/**
 * CTF trace packet reader. Reads the events of a packet of a trace file.
 *
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public class CTFStreamInputPacketReader implements IDefinitionScope, AutoCloseable {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static final int BITS_PER_BYTE = Byte.SIZE;

    /** BitBuffer used to read the trace file. */
    @Nullable
    private BitBuffer fBitBuffer;

    /** StreamInputReader that uses this StreamInputPacketReader. */
    private final CTFStreamInputReader fStreamInputReader;

    /** Trace packet header. */
    private final StructDeclaration fTracePacketHeaderDecl;

    /** Stream packet context definition. */
    private final StructDeclaration fStreamPacketContextDecl;

    /** Stream event header definition. */
    private final IDeclaration fStreamEventHeaderDecl;

    /** Stream event context definition. */
    private final StructDeclaration fStreamEventContextDecl;

    private ICompositeDefinition fCurrentTracePacketHeaderDef;
    private ICompositeDefinition fCurrentStreamEventHeaderDef;
    private ICompositeDefinition fCurrentStreamPacketContextDef;
    /** Reference to the index entry of the current packet. */
    private ICTFPacketDescriptor fCurrentPacket = null;

    /**
     * Last timestamp recorded.
     *
     * Needed to calculate the complete timestamp values for the events with
     * compact headers.
     */
    private long fLastTimestamp = 0;

    /** CPU id of current packet. */
    private int fCurrentCpu = 0;

    private int fLostEventsInThisPacket;

    private long fLostEventsDuration;

    private boolean fHasLost = false;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a StreamInputPacketReader.
     *
     * @param streamInputReader
     *            The StreamInputReader to which this packet reader belongs to.
     */
    public CTFStreamInputPacketReader(CTFStreamInputReader streamInputReader) {
        fStreamInputReader = streamInputReader;

        /* Set the BitBuffer's byte order. */
        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(0);
        if (allocateDirect == null) {
            throw new IllegalStateException("Unable to allocate 0 bytes!"); //$NON-NLS-1$
        }
        fBitBuffer = new BitBuffer(allocateDirect);

        final CTFStream currentStream = streamInputReader.getStreamInput().getStream();
        fTracePacketHeaderDecl = currentStream.getTrace().getPacketHeader();
        fStreamPacketContextDecl = currentStream.getPacketContextDecl();
        fStreamEventHeaderDecl = currentStream.getEventHeaderDeclaration();
        fStreamEventContextDecl = currentStream.getEventContextDecl();
    }

    /**
     * Get the event context defintiion
     *
     * @param input
     *            the bitbuffer to read from
     * @return an context definition, can be null
     * @throws CTFException
     *             out of bounds exception or such
     */
    public StructDefinition getEventContextDefinition(@NonNull BitBuffer input) throws CTFException {
        return fStreamEventContextDecl.createDefinition(fStreamInputReader.getStreamInput(), ILexicalScope.STREAM_EVENT_CONTEXT, input);
    }

    /**
     * Get the packet context defintiion
     *
     * @param input
     *            the bitbuffer to read from
     * @return an context definition, can be null
     * @throws CTFException
     *             out of bounds exception or such
     */
    public StructDefinition getStreamPacketContextDefinition(@NonNull BitBuffer input) throws CTFException {
        return fStreamPacketContextDecl.createDefinition(fStreamInputReader.getStreamInput(), ILexicalScope.STREAM_PACKET_CONTEXT, input);
    }

    /**
     * Get the event header defintiion
     *
     * @param input
     *            the bitbuffer to read from
     * @return an header definition, can be null
     * @throws CTFException
     *             out of bounds exception or such
     */
    public StructDefinition getTracePacketHeaderDefinition(@NonNull BitBuffer input) throws CTFException {
        return fTracePacketHeaderDecl.createDefinition(fStreamInputReader.getStreamInput().getStream().getTrace(), ILexicalScope.TRACE_PACKET_HEADER, input);
    }

    /**
     * Dispose the StreamInputPacketReader
     */
    @Override
    public void close() {
        fBitBuffer = null;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Gets the current packet
     *
     * @return the current packet
     */
    ICTFPacketDescriptor getCurrentPacket() {
        return fCurrentPacket;
    }

    /**
     * Gets the CPU (core) number
     *
     * @return the CPU (core) number
     */
    public int getCPU() {
        return fCurrentCpu;
    }

    @Override
    public LexicalScope getScopePath() {
        return ILexicalScope.PACKET;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @NonNull
    private ByteBuffer getByteBufferAt(long position, long size) throws CTFException, IOException {
        ByteBuffer map = SafeMappedByteBuffer.map(fStreamInputReader.getFc(), MapMode.READ_ONLY, position, size);
        if (map == null) {
            throw new CTFIOException("Failed to allocate mapped byte buffer"); //$NON-NLS-1$
        }
        return map;
    }

    /**
     * Changes the current packet to the given one.
     *
     * @param currentPacket
     *            The index entry of the packet to switch to.
     * @throws CTFException
     *             If we get an error reading the packet
     * @since 1.0
     */
    public void setCurrentPacket(ICTFPacketDescriptor currentPacket) throws CTFException {
        ICTFPacketDescriptor prevPacket = null;
        fCurrentPacket = currentPacket;

        if (fCurrentPacket != null) {
            /*
             * Change the map of the BitBuffer.
             */
            ByteBuffer bb = null;
            try {
                bb = getByteBufferAt(fCurrentPacket.getOffsetBytes(), (fCurrentPacket.getPacketSizeBits() + BITS_PER_BYTE - 1) / BITS_PER_BYTE);
            } catch (IOException e) {
                throw new CTFIOException(e.getMessage(), e);
            }

            BitBuffer bitBuffer = new BitBuffer(bb);
            fBitBuffer = bitBuffer;
            /*
             * Read trace packet header.
             */
            if (fTracePacketHeaderDecl != null) {
                fCurrentTracePacketHeaderDef = getTracePacketHeaderDefinition(bitBuffer);
            }

            /*
             * Read stream packet context.
             */
            if (fStreamPacketContextDecl != null) {
                fCurrentStreamPacketContextDef = getStreamPacketContextDefinition(bitBuffer);

                /* Read CPU ID */
                if (getCurrentPacket().getTarget() != null) {
                    fCurrentCpu = (int) getCurrentPacket().getTargetId();
                }

                /* Read number of lost events */
                fLostEventsInThisPacket = (int) getCurrentPacket().getLostEvents();
                if (fLostEventsInThisPacket != 0) {
                    fHasLost = true;
                    /*
                     * Compute the duration of the lost event time range. If the
                     * current packet is the first packet, duration will be set
                     * to 1.
                     */
                    long lostEventsStartTime;
                    int index = fStreamInputReader.getStreamInput().getIndex().indexOf(currentPacket);
                    if (index == 0) {
                        lostEventsStartTime = currentPacket.getTimestampBegin() + 1;
                    } else {
                        prevPacket = fStreamInputReader.getStreamInput().getIndex().getElement(index - 1);
                        lostEventsStartTime = prevPacket.getTimestampEnd();
                    }
                    fLostEventsDuration = Math.abs(lostEventsStartTime - currentPacket.getTimestampBegin());
                }
            }

            /*
             * Use the timestamp begin of the packet as the reference for the
             * timestamp reconstitution.
             */
            fLastTimestamp = currentPacket.getTimestampBegin();
        } else {
            fBitBuffer = null;
            fLastTimestamp = 0;
        }
    }

    /**
     * Returns whether it is possible to read any more events from this packet.
     *
     * @return True if it is possible to read any more events from this packet.
     */
    public boolean hasMoreEvents() {
        BitBuffer bitBuffer = fBitBuffer;
        ICTFPacketDescriptor currentPacket = fCurrentPacket;
        if (currentPacket != null && bitBuffer != null) {
            return fHasLost || (bitBuffer.position() < currentPacket.getContentSizeBits());
        }
        return false;
    }

    /**
     * Reads the next event of the packet into the right event definition.
     *
     * @return The event definition containing the event data that was just
     *         read.
     * @throws CTFException
     *             If there was a problem reading the trace
     */
    public EventDefinition readNextEvent() throws CTFException {
        /* Default values for those fields */
        // compromise since we cannot have 64 bit addressing of arrays yet.
        int eventID = (int) IEventDeclaration.UNSET_EVENT_ID;
        long timestamp = 0;
        if (fHasLost) {
            fHasLost = false;
            IEventDeclaration lostEventDeclaration = LostEventDeclaration.INSTANCE;
            StructDeclaration lostFields = lostEventDeclaration.getFields();
            // this is a hard coded map, we know it's not null
            IntegerDeclaration lostFieldsDecl = (IntegerDeclaration) lostFields.getField(CTFStrings.LOST_EVENTS_FIELD);
            if (lostFieldsDecl == null)
            {
                throw new IllegalStateException("Lost events count not declared!"); //$NON-NLS-1$
            }
            IntegerDeclaration lostEventsDurationDecl = (IntegerDeclaration) lostFields.getField(CTFStrings.LOST_EVENTS_DURATION);
            if (lostEventsDurationDecl == null) {
                throw new IllegalStateException("Lost events duration not declared!"); //$NON-NLS-1$
            }
            IntegerDefinition lostDurationDef = new IntegerDefinition(lostFieldsDecl, null, CTFStrings.LOST_EVENTS_DURATION, fLostEventsDuration);
            IntegerDefinition lostCountDef = new IntegerDefinition(lostEventsDurationDecl, null, CTFStrings.LOST_EVENTS_FIELD, fLostEventsInThisPacket);
            IntegerDefinition[] fields = new IntegerDefinition[] { lostCountDef, lostDurationDef };
            return new EventDefinition(
                    lostEventDeclaration,
                    fStreamInputReader,
                    fLastTimestamp,
                    null,
                    null,
                    null,
                    new StructDefinition(
                            lostFields,
                            this, "fields", //$NON-NLS-1$
                            fields
                    ));

        }

        final BitBuffer currentBitBuffer = fBitBuffer;
        if (currentBitBuffer == null) {
            return null;
        }
        final long posStart = currentBitBuffer.position();
        /* Read the stream event header. */
        if (fStreamEventHeaderDecl != null) {
            if (fStreamEventHeaderDecl instanceof IEventHeaderDeclaration) {
                fCurrentStreamEventHeaderDef = (ICompositeDefinition) fStreamEventHeaderDecl.createDefinition(null, "", currentBitBuffer); //$NON-NLS-1$
                EventHeaderDefinition ehd = (EventHeaderDefinition) fCurrentStreamEventHeaderDef;
                eventID = ehd.getId();
                timestamp = calculateTimestamp(ehd.getTimestamp(), ehd.getTimestampLength());
            } else {
                fCurrentStreamEventHeaderDef = ((StructDeclaration) fStreamEventHeaderDecl).createDefinition(null, ILexicalScope.EVENT_HEADER, currentBitBuffer);
                StructDefinition StructEventHeaderDef = (StructDefinition) fCurrentStreamEventHeaderDef;
                /* Check for the event id. */
                IDefinition idDef = StructEventHeaderDef.lookupDefinition("id"); //$NON-NLS-1$
                SimpleDatatypeDefinition simpleIdDef = null;
                if (idDef instanceof SimpleDatatypeDefinition) {
                    simpleIdDef = ((SimpleDatatypeDefinition) idDef);
                } else if (idDef != null) {
                    throw new CTFIOException("Id defintion not an integer, enum or float definiton in event header."); //$NON-NLS-1$
                }

                /*
                 * Get the timestamp from the event header (may be overridden
                 * later on)
                 */
                IntegerDefinition timestampDef = StructEventHeaderDef.lookupInteger("timestamp"); //$NON-NLS-1$

                /* Check for the variant v. */
                IDefinition variantDef = StructEventHeaderDef.lookupDefinition("v"); //$NON-NLS-1$
                if (variantDef instanceof VariantDefinition) {

                    /* Get the variant current field */
                    StructDefinition variantCurrentField = (StructDefinition) ((VariantDefinition) variantDef).getCurrentField();

                    /*
                     * Try to get the id field in the current field of the
                     * variant. If it is present, it overrides the previously
                     * read event id.
                     */
                    IDefinition vIdDef = variantCurrentField.lookupDefinition("id"); //$NON-NLS-1$
                    if (vIdDef instanceof IntegerDefinition) {
                        simpleIdDef = (SimpleDatatypeDefinition) vIdDef;
                    }

                    /*
                     * Get the timestamp. This would overwrite any previous
                     * timestamp definition
                     */
                    timestampDef = variantCurrentField.lookupInteger("timestamp"); //$NON-NLS-1$
                }
                if (simpleIdDef != null) {
                    eventID = simpleIdDef.getIntegerValue().intValue();
                }
                if (timestampDef != null) {
                    timestamp = calculateTimestamp(timestampDef);
                } // else timestamp remains 0
            }
        }
        /* Get the right event definition using the event id. */
        IEventDeclaration eventDeclaration = fStreamInputReader.getStreamInput().getStream().getEventDeclaration(eventID);
        if (eventDeclaration == null) {
            throw new CTFIOException("Incorrect event id : " + eventID); //$NON-NLS-1$
        }
        EventDefinition eventDef = eventDeclaration.createDefinition(fStreamInputReader, currentBitBuffer, timestamp);

        /*
         * Set the event timestamp using the timestamp calculated by
         * updateTimestamp.
         */

        if (posStart == currentBitBuffer.position()) {
            throw new CTFIOException("Empty event not allowed, event: " + eventDef.getDeclaration().getName()); //$NON-NLS-1$
        }

        return eventDef;
    }

    /**
     * Calculates the timestamp value of the event, possibly using the timestamp
     * from the last event.
     *
     * @param timestampDef
     *            Integer definition of the timestamp.
     * @return The calculated timestamp value.
     */
    private long calculateTimestamp(IntegerDefinition timestampDef) {
        int len = timestampDef.getDeclaration().getLength();
        final long value = timestampDef.getValue();

        return calculateTimestamp(value, len);
    }

    private long calculateTimestamp(final long value, int len) {
        long newval;
        long majorasbitmask;
        /*
         * If the timestamp length is 64 bits, it is a full timestamp.
         */
        if (len == Long.SIZE) {
            fLastTimestamp = value;
            return fLastTimestamp;
        }

        /*
         * Bit mask to keep / remove all old / new bits.
         */
        majorasbitmask = (1L << len) - 1;

        /*
         * If the new value is smaller than the corresponding bits of the last
         * timestamp, we assume an overflow of the compact representation.
         */
        newval = value;
        if (newval < (fLastTimestamp & majorasbitmask)) {
            newval = newval + (1L << len);
        }

        /* Keep only the high bits of the old value */
        fLastTimestamp = fLastTimestamp & ~majorasbitmask;

        /* Then add the low bits of the new value */
        fLastTimestamp = fLastTimestamp + newval;

        return fLastTimestamp;
    }

    @Override
    public Definition lookupDefinition(String lookupPath) {
        if (lookupPath.equals(ILexicalScope.STREAM_PACKET_CONTEXT.getPath())) {
            return (Definition) fCurrentStreamPacketContextDef;
        }
        if (lookupPath.equals(ILexicalScope.TRACE_PACKET_HEADER.getPath())) {
            return (Definition) fCurrentTracePacketHeaderDef;
        }
        return null;
    }


    /**
     * Get stream event header
     *
     * @return the stream event header
     */
    public ICompositeDefinition getStreamEventHeaderDefinition() {
        return fCurrentStreamEventHeaderDef;
    }

    /**
     * Get the current packet event header
     *
     * @return the current packet event header
     */
    public StructDefinition getCurrentPacketEventHeader() {
        if (fCurrentTracePacketHeaderDef instanceof StructDefinition) {
            return (StructDefinition) fCurrentTracePacketHeaderDef;
        }
        return null;
    }
}
