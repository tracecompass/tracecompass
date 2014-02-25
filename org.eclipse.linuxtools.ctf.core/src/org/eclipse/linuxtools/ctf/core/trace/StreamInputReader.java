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
    private final StreamInput fStreamInput;

    /**
     * The packet reader used to read packets from this trace file.
     */
    private final StreamInputPacketReader fPacketReader;

    /**
     * Iterator on the packet index
     */
    private int fPacketIndex;

    /**
     * Reference to the current event of this trace file (iow, the last on that
     * was read, the next one to be returned)
     */
    private EventDefinition fCurrentEvent = null;

    private int fId;

    private CTFTraceReader fParent;

    /** Map of all the event types */
    private final Map<Long, EventDefinition> fEventDefs = new HashMap<>();

    /**
     * Live trace reading
     */
    private boolean fLive = false;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a StreamInputReader that reads a StreamInput.
     *
     * @param streamInput
     *            The StreamInput to read.
     * @throws CTFReaderException
     *             if an error occurs
     * @since 2.0
     */
    public StreamInputReader(StreamInput streamInput) throws CTFReaderException {
        fStreamInput = streamInput;
        fPacketReader = new StreamInputPacketReader(this);
        /*
         * Get the iterator on the packet index.
         */
        fPacketIndex = 0;
        /*
         * Make first packet the current one.
         */
        goToNextPacket();
    }

    /**
     * Dispose the StreamInputReader
     *
     * @since 2.0
     */
    public void dispose() {
        fPacketReader.dispose();
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
        return fCurrentEvent;
    }

    /**
     * Gets the current packet context
     *
     * @return the current packet context (size, lost events and such)
     */
    public StructDefinition getCurrentPacketContext() {
        return fPacketReader.getStreamPacketContextDef();
    }

    /**
     * Gets the byte order for a trace
     *
     * @return the trace byte order
     */
    public ByteOrder getByteOrder() {
        return fStreamInput.getStream().getTrace().getByteOrder();
    }

    /**
     * Gets the name of the stream (it's an id and a number)
     *
     * @return gets the stream name (it's a number)
     */
    public int getName() {
        return fId;
    }

    /**
     * Sets the name of the stream
     *
     * @param name
     *            the name of the stream, (it's a number)
     */
    public void setName(int name) {
        fId = name;
    }

    /**
     * Gets the CPU of a stream. It's the same as the one in /proc or running
     * the asm CPUID instruction
     *
     * @return The CPU id (a number)
     */
    public int getCPU() {
        return fPacketReader.getCPU();
    }

    /**
     * Gets the filename of the stream being read
     *
     * @return The filename of the stream being read
     */
    public String getFilename() {
        return fStreamInput.getFilename();
    }

    /*
     * for internal use only
     */
    StreamInput getStreamInput() {
        return fStreamInput;
    }

    /**
     * Gets the event definition hashmap for this StreamInput
     *
     * @return Unmodifiable map with the event definitions
     * @since 2.1
     */
    public Map<Long, EventDefinition> getEventDefinitions() {
        return Collections.unmodifiableMap(fEventDefs);
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
        fEventDefs.put(id, def);
    }

    /**
     * Set the trace to live mode
     *
     * @param live
     *            whether the trace is read live or not
     * @since 3.0
     */
    public void setLive(boolean live) {
        fLive = live;
    }

    /**
     * Get if the trace is to read live or not
     *
     * @return whether the trace is live or not
     * @since 3.0
     */
    public boolean isLive() {
        return fLive;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Reads the next event in the current event variable.
     *
     * @return If an event has been successfully read.
     * @throws CTFReaderException
     *             if an error occurs
     * @since 3.0
     */
    public CTFResponse readNextEvent() throws CTFReaderException {

        /*
         * Change packet if needed
         */
        if (!fPacketReader.hasMoreEvents()) {
            final StreamInputPacketIndexEntry prevPacket = fPacketReader
                    .getCurrentPacket();
            if (prevPacket != null || fLive ) {
                goToNextPacket();
            }

        }

        /*
         * If an event is available, read it.
         */
        if (fPacketReader.hasMoreEvents()) {
            setCurrentEvent(fPacketReader.readNextEvent());
            return CTFResponse.OK;
        }
        this.setCurrentEvent(null);
        return fLive ? CTFResponse.WAIT : CTFResponse.FINISH;
    }

    /**
     * Change the current packet of the packet reader to the next one.
     *
     * @throws CTFReaderException
     *             if an error occurs
     */
    private void goToNextPacket() throws CTFReaderException {
        fPacketIndex++;
        // did we already index the packet?
        if (getPacketSize() >= (fPacketIndex + 1)) {
            fPacketReader.setCurrentPacket(getPacket());
        } else {
            // go to the next packet if there is one, index it at the same time
            if (fStreamInput.addPacketHeaderIndex()) {
                fPacketIndex = getPacketSize() - 1;
                fPacketReader.setCurrentPacket(getPacket());
            } else {
                // out of packets
                fPacketReader.setCurrentPacket(null);
            }
        }
    }

    /**
     * @return
     */
    private int getPacketSize() {
        return fStreamInput.getIndex().getEntries().size();
    }

    /**
     * Changes the location of the trace file reader so that the current event
     * is the first event with a timestamp greater or equal the given timestamp.
     *
     * @param timestamp
     *            The timestamp to seek to.
     * @return The offset compared to the current position
     * @throws CTFReaderException
     *             if an error occurs
     */
    public long seek(long timestamp) throws CTFReaderException {
        long offset = 0;

        gotoPacket(timestamp);

        /*
         * index up to the desired timestamp.
         */
        while ((fPacketReader.getCurrentPacket() != null)
                && (fPacketReader.getCurrentPacket().getTimestampEnd() < timestamp)) {
            try {
                fStreamInput.addPacketHeaderIndex();
                goToNextPacket();
            } catch (CTFReaderException e) {
                // do nothing here
            }
        }
        if (fPacketReader.getCurrentPacket() == null) {
            gotoPacket(timestamp);
        }

        /*
         * Advance until either of these conditions are met:
         *
         * - reached the end of the trace file (the given timestamp is after the
         * last event)
         *
         * - found the first event with a timestamp greater or equal the given
         * timestamp.
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
     *            the time to seek
     * @throws CTFReaderException
     *             if an error occurs
     */
    private void gotoPacket(long timestamp) throws CTFReaderException {
        fPacketIndex = fStreamInput.getIndex().search(timestamp)
                .previousIndex();
        /*
         * Switch to this packet.
         */
        goToNextPacket();
    }

    /**
     * Seeks the last event of a stream and returns it.
     *
     * @throws CTFReaderException
     *             if an error occurs
     */
    public void goToLastEvent() throws CTFReaderException {
        /*
         * Search in the index for the packet to search in.
         */
        final int len = fStreamInput.getIndex().getEntries().size();

        /*
         * Go to beginning of trace.
         */
        seek(0);
        /*
         * if the trace is empty.
         */
        if ((len == 0) || (fPacketReader.hasMoreEvents() == false)) {
            /*
             * This means the trace is empty. abort.
             */
            return;
        }
        /*
         * Go to the last packet that contains events.
         */
        for (int pos = len - 1; pos > 0; pos--) {
            fPacketIndex = pos;
            fPacketReader.setCurrentPacket(getPacket());
            if (fPacketReader.hasMoreEvents()) {
                break;
            }
        }

        /*
         * Go until the end of that packet
         */
        EventDefinition prevEvent = null;
        while (fCurrentEvent != null) {
            prevEvent = fCurrentEvent;
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
        return fParent;
    }

    /**
     * @param parent
     *            the parent to set
     */
    public void setParent(CTFTraceReader parent) {
        fParent = parent;
    }

    /**
     * Sets the current event in a stream input reader
     *
     * @param currentEvent
     *            the event to set
     */
    public void setCurrentEvent(EventDefinition currentEvent) {
        fCurrentEvent = currentEvent;
    }

    /**
     * @return the packetIndexIt
     */
    private int getPacketIndex() {
        return fPacketIndex;
    }

    private StreamInputPacketIndexEntry getPacket() {
        return fStreamInput.getIndex().getEntries().get(getPacketIndex());
    }

    /**
     * @return the packetReader
     */
    public StreamInputPacketReader getPacketReader() {
        return fPacketReader;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + fId;
        result = (prime * result)
                + ((fStreamInput == null) ? 0 : fStreamInput.hashCode());
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
        if (fId != other.fId) {
            return false;
        }
        if (fStreamInput == null) {
            if (other.fStreamInput != null) {
                return false;
            }
        } else if (!fStreamInput.equals(other.fStreamInput)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        // this helps debugging
        return fId + ' ' + fCurrentEvent.toString();
    }
}
