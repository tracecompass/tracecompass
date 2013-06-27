/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson, Ecole Polytechnique de Montreal and others
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

import java.nio.ByteOrder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInputPacketIndexEntry;

/**
 * A CTF trace event reader. Reads the events of a trace file.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public class StreamInputReader {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The StreamInput we are reading.
     */
    private final StreamInput streamInput;

    /**
     * The packet reader used to read packets from this trace file.
     */
    private final StreamInputPacketReader packetReader;

    /**
     * Iterator on the packet index
     */
    private int packetIndex;

    /**
     * Reference to the current event of this trace file (iow, the last on that
     * was read, the next one to be returned)
     */
    private EventDefinition currentEvent = null;

    private int name;

    private CTFTraceReader parent;

    /** Map of all the event types */
    private final Map<Long, EventDefinition> eventDefs = new HashMap<Long,EventDefinition>();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a StreamInputReader that reads a StreamInput.
     *
     * @param streamInput
     *            The StreamInput to read.
     * @since 2.0
     */
    public StreamInputReader(StreamInput streamInput) {
        this.streamInput = streamInput;
        this.packetReader = new StreamInputPacketReader(this);
        /*
         * Get the iterator on the packet index.
         */
        this.packetIndex = 0;
        /*
         * Make first packet the current one.
         */
        goToNextPacket();
    }

    /**
     * Dispose the StreamInputReader
     * @since 2.0
     */
    public void dispose() {
        packetReader.dispose();
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Gets the current event in this stream
     *
     * @return the current event in the stream, null if the stream is
     *         finished/empty/malformed
     */
    public EventDefinition getCurrentEvent() {
        return this.currentEvent;
    }

    /**
     * gets the current packet context
     *
     * @return the current packet context (size, lost events and such)
     */
    public StructDefinition getCurrentPacketContext() {
        return this.packetReader.getStreamPacketContextDef();
    }

    /**
     * Gets the byte order for a trace
     *
     * @return the trace byte order
     */
    public ByteOrder getByteOrder() {
        return streamInput.getStream().getTrace().getByteOrder();
    }

    /**
     * Gets the name of the stream (it's an id and a number)
     *
     * @return gets the stream name (it's a number)
     */
    public int getName() {
        return this.name;
    }

    /**
     * Sets the name of the stream
     *
     * @param name
     *            the name of the stream, (it's a number)
     */
    public void setName(int name) {
        this.name = name;
    }

    /**
     * Gets the CPU of a stream. It's the same as the one in /proc or running
     * the asm CPUID instruction
     *
     * @return The CPU id (a number)
     */
    public int getCPU() {
        return this.packetReader.getCPU();
    }

    /**
     * Gets the filename of the stream being read
     * @return The filename of the stream being read
     */
    public String getFilename() {
        return streamInput.getFilename();
    }

    /*
     * for internal use only
     */
    StreamInput getStreamInput() {
        return streamInput;
    }

    /**
     * Gets the event definition hashmap for this StreamInput
     *
     * @return Unmodifiable map with the event definitions
     * @since 2.1
     */
    public Map<Long, EventDefinition> getEventDefinitions() {
        return Collections.unmodifiableMap(eventDefs);
    }

    /**
     * Add an event definition to this stream input reader.
     *
     * @param id
     *            The id of the event definition. This will overwrite any
     *            existing definition with the same id.
     * @param def
     *            The matching event definition
     * @since 2.1
     */
    public void addEventDefinition(Long id, EventDefinition def) {
        eventDefs.put(id, def);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Reads the next event in the current event variable.
     *
     * @return If an event has been successfully read.
     */
    public boolean readNextEvent() {

        /*
         * Change packet if needed
         */
        if (!this.packetReader.hasMoreEvents()) {
            final StreamInputPacketIndexEntry prevPacket = this.packetReader
                    .getCurrentPacket();
            if (prevPacket != null) {
                goToNextPacket();
            }
        }

        /*
         * If an event is available, read it.
         */
        if (this.packetReader.hasMoreEvents()) {
            try {
                this.setCurrentEvent(this.packetReader.readNextEvent());
            } catch (CTFReaderException e) {
                /*
                 * Some problem happened, we'll assume that there are no more
                 * events
                 */
                return false;
            }
            return true;
        }
        this.setCurrentEvent(null);
        return false;
    }

    /**
     * Change the current packet of the packet reader to the next one.
     */
    private void goToNextPacket() {
        packetIndex++;
        if (getPacketSize() >= (packetIndex + 1)) {
            this.packetReader.setCurrentPacket(getPacket());
        } else {
            try {
                if (this.streamInput.addPacketHeaderIndex()) {
                    packetIndex = getPacketSize() - 1;
                    this.packetReader.setCurrentPacket(getPacket());

                } else {
                    this.packetReader.setCurrentPacket(null);
                }

            } catch (CTFReaderException e) {
                this.packetReader.setCurrentPacket(null);
            }
        }
    }

    /**
     * @return
     */
    private int getPacketSize() {
        return streamInput.getIndex().getEntries().size();
    }

    /**
     * Changes the location of the trace file reader so that the current event
     * is the first event with a timestamp greater than the given timestamp.
     *
     * @param timestamp
     *            The timestamp to seek to.
     * @return The offset compared to the current position
     */
    public long seek(long timestamp) {
        long offset = 0;

        gotoPacket(timestamp);

        /*
         * index up to the desired timestamp.
         */
        while ((this.packetReader.getCurrentPacket() != null)
                && (this.packetReader.getCurrentPacket().getTimestampEnd() < timestamp)) {
            try {
                this.streamInput.addPacketHeaderIndex();
                goToNextPacket();
            } catch (CTFReaderException e) {
                // do nothing here
            }
        }
        if (this.packetReader.getCurrentPacket() == null) {
            gotoPacket(timestamp);
        }

        /*
         * Advance until A. we reached the end of the trace file (which means
         * the given timestamp is after the last event), or B. we found the
         * first event with a timestamp greater than the given timestamp.
         */
        readNextEvent();
        boolean done = (this.getCurrentEvent() == null);
        while (!done && (this.getCurrentEvent().getTimestamp() < timestamp)) {
            readNextEvent();
            done = (this.getCurrentEvent() == null);
            offset++;
        }
        return offset;
    }

    /**
     * @param timestamp
     */
    private void gotoPacket(long timestamp) {
        this.packetIndex = this.streamInput.getIndex().search(timestamp)
                .previousIndex();
        /*
         * Switch to this packet.
         */
        goToNextPacket();
    }

    /**
     * Seeks the last event of a stream and returns it.
     */
    public void goToLastEvent() {
        /*
         * Search in the index for the packet to search in.
         */
        final int len = this.streamInput.getIndex().getEntries().size();

        /*
         * Go to beginning of trace.
         */
        seek(0);
        /*
         * if the trace is empty.
         */
        if ((len == 0) || (this.packetReader.hasMoreEvents() == false)) {
            /*
             * This means the trace is empty. abort.
             */
            return;
        }
        /*
         * Go to the last packet that contains events.
         */
        for (int pos = len - 1; pos > 0; pos--) {
            packetIndex = pos;
            this.packetReader.setCurrentPacket(getPacket());
            if (this.packetReader.hasMoreEvents()) {
                break;
            }
        }

        /*
         * Go until the end of that packet
         */
        EventDefinition prevEvent = null;
        while (this.currentEvent != null) {
            prevEvent = this.currentEvent;
            this.readNextEvent();
        }
        /*
         * Go back to the previous event
         */
        this.setCurrentEvent(prevEvent);
    }

    /**
     * @return the parent
     */
    public CTFTraceReader getParent() {
        return parent;
    }

    /**
     * @param parent
     *            the parent to set
     */
    public void setParent(CTFTraceReader parent) {
        this.parent = parent;
    }

    /**
     * Sets the current event in a stream input reader
     * @param currentEvent the event to set
     */
    public void setCurrentEvent(EventDefinition currentEvent) {
        this.currentEvent = currentEvent;
    }

    /**
     * @return the packetIndexIt
     */
    private int getPacketIndex() {
        return packetIndex;
    }

    private StreamInputPacketIndexEntry getPacket() {
        return streamInput.getIndex().getEntries().get(getPacketIndex());
    }

    /**
     * @return the packetReader
     */
    public StreamInputPacketReader getPacketReader() {
        return packetReader;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + name;
        result = (prime * result)
                + ((streamInput == null) ? 0 : streamInput.hashCode());
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
        if (!(obj instanceof StreamInputReader)) {
            return false;
        }
        StreamInputReader other = (StreamInputReader) obj;
        if (name != other.name) {
            return false;
        }
        if (streamInput == null) {
            if (other.streamInput != null) {
                return false;
            }
        } else if (!streamInput.equals(other.streamInput)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        // this helps debugging
        return this.name + ' ' + this.currentEvent.toString();
    }
}
