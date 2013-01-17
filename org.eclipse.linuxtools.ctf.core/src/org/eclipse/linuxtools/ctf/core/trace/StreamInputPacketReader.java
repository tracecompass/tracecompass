/*******************************************************************************
 * Copyright (c) 2011-2013 Ericsson, Ecole Polytechnique de Montreal and others
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

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.IEventDeclaration;
import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.SimpleDatatypeDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.VariantDefinition;
import org.eclipse.linuxtools.internal.ctf.core.event.EventDeclaration;
import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInputPacketIndexEntry;

/**
 * CTF trace packet reader. Reads the events of a packet of a trace file.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public class StreamInputPacketReader implements IDefinitionScope {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** BitBuffer used to read the trace file. */
    private final BitBuffer bitBuffer;

    /** StreamInputReader that uses this StreamInputPacketReader. */
    private final StreamInputReader streamInputReader;

    /** Trace packet header. */
    private final StructDefinition tracePacketHeaderDef;

    /** Stream packet context definition. */
    private final StructDefinition streamPacketContextDef;

    /** Stream event header definition. */
    private final StructDefinition streamEventHeaderDef;

    /** Stream event context definition.*/
    private final StructDefinition streamEventContextDef;

    /** Maps event ID to event definitions. */
    private final HashMap<Long, EventDefinition> events;

    /** Reference to the index entry of the current packet. */
    private StreamInputPacketIndexEntry currentPacket = null;

    /**
     * Last timestamp recorded.
     *
     * Needed to calculate the complete timestamp values for the events with
     * compact headers.
     */
    private long lastTimestamp = 0;

    /** CPU id of current packet. */
    private int currentCpu = 0;

    /** number of lost events in this packet */
    private int lostSoFar;

    private int lostEventsInThisPacket;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a StreamInputPacketReader.
     *
     * @param streamInputReader
     *            The StreamInputReader to which this packet reader belongs to.
     */
    public StreamInputPacketReader(StreamInputReader streamInputReader) {
        this.streamInputReader = streamInputReader;

        /* Set the BitBuffer's byte order. */
        bitBuffer = new BitBuffer();
        bitBuffer.setByteOrder(streamInputReader.getByteOrder());

        events = streamInputReader.getStreamInput().getStream().getTrace().getEventDefs(streamInputReader.getStreamInput());
        lostSoFar = 0;

        /* Create trace packet header definition. */
        final Stream currentStream = streamInputReader.getStreamInput().getStream();
        StructDeclaration tracePacketHeaderDecl = currentStream.getTrace().getPacketHeader();
        if (tracePacketHeaderDecl != null) {
            tracePacketHeaderDef = tracePacketHeaderDecl.createDefinition(this, "trace.packet.header"); //$NON-NLS-1$
        } else {
            tracePacketHeaderDef = null;
        }

        /* Create stream packet context definition. */
        StructDeclaration streamPacketContextDecl = currentStream.getPacketContextDecl();
        if (streamPacketContextDecl != null) {
            streamPacketContextDef = streamPacketContextDecl.createDefinition(this, "stream.packet.context"); //$NON-NLS-1$
        } else {
            streamPacketContextDef = null;
        }

        /* Create stream event header definition. */
        StructDeclaration streamEventHeaderDecl = currentStream.getEventHeaderDecl();
        if (streamEventHeaderDecl != null) {
            streamEventHeaderDef = streamEventHeaderDecl.createDefinition(this, "stream.event.header"); //$NON-NLS-1$
        } else {
            streamEventHeaderDef = null;
        }

        /* Create stream event context definition. */
        StructDeclaration streamEventContextDecl = currentStream.getEventContextDecl();
        if (streamEventContextDecl != null) {
            streamEventContextDef = streamEventContextDecl.createDefinition(this, "stream.event.context"); //$NON-NLS-1$
        } else {
            streamEventContextDef = null;
        }

        /* Create event definitions */
        Collection<IEventDeclaration> eventDecls = streamInputReader.getStreamInput().getStream().getEvents().values();

        for (IEventDeclaration event : eventDecls) {
            if (!events.containsKey(event.getId())) {
                EventDefinition eventDef = event.createDefinition(streamInputReader);
                events.put(event.getId(), eventDef);
            }
        }
    }

    /**
     * Dispose the StreamInputPacketReader
     * @since 2.0
     */
    public void dispose() {
        bitBuffer.setByteBuffer(null);
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Gets the current packet
     *
     * @return the current packet
     */
    StreamInputPacketIndexEntry getCurrentPacket() {
        return this.currentPacket;
    }

    /**
     * Gets the steamPacketContext Definition
     *
     * @return steamPacketContext Definition
     */
    public StructDefinition getStreamPacketContextDef() {
        return this.streamPacketContextDef;
    }

    /**
     * Gets the stream's event context definition.
     *
     * @return The streamEventContext definition
     */
    public StructDefinition getStreamEventContextDef() {
        return streamEventContextDef;
    }

    /**
     * Gets the CPU (core) number
     *
     * @return the CPU (core) number
     */
    public int getCPU() {
        return this.currentCpu;
    }

    @Override
    public String getPath() {
        return ""; //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Changes the current packet to the given one.
     *
     * @param currentPacket
     *            The index entry of the packet to switch to.
     */
    void setCurrentPacket(StreamInputPacketIndexEntry currentPacket) {
        this.currentPacket = currentPacket;

        if (this.currentPacket != null) {
            /*
             * Change the map of the BitBuffer.
             */
            MappedByteBuffer bb = null;
            try {
                bb = streamInputReader.getStreamInput().getFileChannel()
                        .map(MapMode.READ_ONLY,
                                this.currentPacket.getOffsetBytes(),
                                (this.currentPacket.getPacketSizeBits() + 7) / 8);
            } catch (IOException e) {
                /*
                 * The streamInputReader object is already allocated, so this
                 * shouldn't fail bar some very bad kernel or RAM errors...
                 */
                e.printStackTrace();
            }

            bitBuffer.setByteBuffer(bb);

            /*
             * Read trace packet header.
             */
            if (tracePacketHeaderDef != null) {
                tracePacketHeaderDef.read(bitBuffer);
            }

            /*
             * Read stream packet context.
             */
            if (getStreamPacketContextDef() != null) {
                getStreamPacketContextDef().read(bitBuffer);

                /* Read CPU ID */
                if (this.getCurrentPacket().getTarget() != null) {
                    this.currentCpu = (int) this.getCurrentPacket().getTargetId();
                }

                /* Read number of lost events */
                lostEventsInThisPacket = (int) this.getCurrentPacket().getLostEvents();
                lostSoFar = 0;

            }

            /*
             * Use the timestamp begin of the packet as the reference for the
             * timestamp reconstitution.
             */
            lastTimestamp = currentPacket.getTimestampBegin();
        } else {
            bitBuffer.setByteBuffer(null);

            lastTimestamp = 0;
        }
    }

    /**
     * Returns whether it is possible to read any more events from this packet.
     *
     * @return True if it is possible to read any more events from this packet.
     */
    public boolean hasMoreEvents() {
        if (currentPacket != null) {
            return bitBuffer.position() < currentPacket.getContentSizeBits();
        }
        return false;
    }

    /**
     * Reads the next event of the packet into the right event definition.
     *
     * @return The event definition containing the event data that was just
     *         read.
     * @throws CTFReaderException
     *             If there was a problem reading the trace
     */
    public EventDefinition readNextEvent() throws CTFReaderException {
        /* Default values for those fields */
        long eventID = 0;
        long timestamp = 0;

        if (lostEventsInThisPacket > lostSoFar) {
            EventDefinition eventDef = EventDeclaration.getLostEventDeclaration().createDefinition(streamInputReader);
            eventDef.setTimestamp(this.lastTimestamp);
            ++lostSoFar;
            return eventDef;
        }

        final StructDefinition sehd = streamEventHeaderDef;
        final BitBuffer currentBitBuffer = bitBuffer;

        /* Read the stream event header. */
        if (sehd != null) {
            sehd.read(currentBitBuffer);

            /* Check for the event id. */
            Definition idDef = sehd.lookupDefinition("id"); //$NON-NLS-1$
            if (idDef instanceof SimpleDatatypeDefinition) {
                eventID = ((SimpleDatatypeDefinition) idDef).getIntegerValue();
            } // else, eventID remains 0

            /* Get the timestamp from the event header (may be overridden later on) */
            Definition timestampDef = sehd.lookupInteger("timestamp"); //$NON-NLS-1$
            if (timestampDef instanceof IntegerDefinition) {
                timestamp = calculateTimestamp((IntegerDefinition) timestampDef);
            } // else timestamp remains 0

            /* Check for the variant v. */
            Definition variantDef = sehd.lookupDefinition("v"); //$NON-NLS-1$
            if (variantDef instanceof VariantDefinition) {

                /* Get the variant current field */
                StructDefinition variantCurrentField = (StructDefinition) ((VariantDefinition) variantDef).getCurrentField();

                /*
                 * Try to get the id field in the current field of the variant.
                 * If it is present, it overrides the previously read event id.
                 */
                Definition idIntegerDef = variantCurrentField.lookupDefinition("id"); //$NON-NLS-1$
                if (idIntegerDef instanceof IntegerDefinition) {
                    eventID = ((IntegerDefinition) idIntegerDef).getValue();
                }

                /* Get the timestamp. This would overwrite any previous timestamp definition */
                timestampDef = variantCurrentField.lookupDefinition("timestamp"); //$NON-NLS-1$
                if (timestampDef instanceof IntegerDefinition) {
                    timestamp = calculateTimestamp((IntegerDefinition) timestampDef);
                }
            }
        }

        /* Read the stream event context. */
        if (streamEventContextDef != null) {
            streamEventContextDef.read(currentBitBuffer);
        }

        /* Get the right event definition using the event id. */
        EventDefinition eventDef = events.get(eventID);
        if (eventDef == null) {
            throw new CTFReaderException("Incorrect event id : " + eventID); //$NON-NLS-1$
        }

        /* Read the event context. */
        if (eventDef.getEventContext() != null) {
            eventDef.getEventContext().read(currentBitBuffer);
        }

        /* Read the event fields. */
        if (eventDef.getFields() != null) {
            eventDef.getFields().read(currentBitBuffer);
        }

        /*
         * Set the event timestamp using the timestamp calculated by
         * updateTimestamp.
         */
        eventDef.setTimestamp(timestamp);

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
        long newval;
        long majorasbitmask;
        int len = timestampDef.getDeclaration().getLength();

        /*
         * If the timestamp length is 64 bits, it is a full timestamp.
         */
        if (timestampDef.getDeclaration().getLength() == 64) {
            lastTimestamp = timestampDef.getValue();
            return lastTimestamp;
        }

        /*
         * Bit mask to keep / remove all old / new bits.
         */
        majorasbitmask = (1L << len) - 1;

        /*
         * If the new value is smaller than the corresponding bits of the last
         * timestamp, we assume an overflow of the compact representation.
         */
        newval = timestampDef.getValue();
        if (newval < (lastTimestamp & majorasbitmask)) {
            newval = newval + (1L << len);
        }

        /* Keep only the high bits of the old value */
        lastTimestamp = lastTimestamp & ~majorasbitmask;

        /* Then add the low bits of the new value */
        lastTimestamp = lastTimestamp + newval;

        return lastTimestamp;
    }

    @Override
    public Definition lookupDefinition(String lookupPath) {
        return null;
    }
}
