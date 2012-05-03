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

import java.nio.ByteOrder;
import java.util.ListIterator;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInput;
import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInputPacketIndexEntry;

/**
 * <b><u>StreamInputReader</u></b>
 * <p>
 * Reads the events of a trace file.
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
    private ListIterator<StreamInputPacketIndexEntry> packetIndexIt;

    /**
     * Reference to the current event of this trace file (iow, the last on that
     * was read, the next one to be returned)
     */
    private EventDefinition currentEvent = null;

    private int name;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a StreamInputReader that reads a StreamInput.
     *
     * @param streamInput
     *            The StreamInput to read.
     */
    public StreamInputReader(StreamInput streamInput) {
        this.streamInput = streamInput;
        this.packetReader = new StreamInputPacketReader(this);

        /*
         * Get the iterator on the packet index.
         */
        this.packetIndexIt = streamInput.getIndex().listIterator();

        /*
         * Make first packet the current one.
         */
        goToNextPacket();
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    public EventDefinition getCurrentEvent() {
        return this.currentEvent;
    }

    public StructDefinition getCurrentPacketContext() {
        return this.packetReader.getStreamPacketContextDef();
    }

    public ByteOrder getByteOrder() {
        return streamInput.getStream().getTrace().getByteOrder();
    }

    public int getName() {
        return this.name;
    }

    public void setName(int name) {
        this.name = name;
    }

    public int getCPU() {
        return this.packetReader.getCPU();
    }

    public String getFilename() {
        return streamInput.getFilename();
    }

    /*
     * for internal use only
     */
    StreamInput getStreamInput() {
        return streamInput;
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
            goToNextPacket();
        }

        /*
         * If an event is available, read it.
         */
        if (this.packetReader.hasMoreEvents()) {
            try {
                this.setCurrentEvent(this.packetReader.readNextEvent());
            } catch (CTFReaderException e) {
                /* Some problem happened, we'll assume there is no more events */
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
        if (getPacketIndexIt().hasNext()) {
            StreamInputPacketIndexEntry nextPacket = getPacketIndexIt().next();
            this.packetReader.setCurrentPacket(nextPacket);
        } else {
            this.packetReader.setCurrentPacket(null);
        }
    }

    /**
     * Changes the location of the trace file reader so that the current event
     * is the first event with a timestamp greater than the given timestamp.
     *
     * @param timestamp
     *            The timestamp to seek to.
     */
    public long seek(long timestamp) {
        long offset = 0;
        /*
         * Search in the index for the packet to search in.
         */
        this.packetIndexIt = this.streamInput.getIndex().search(timestamp);

        /*
         * Switch to this packet.
         */
        goToNextPacket();

        /*
         * Advance until A. we reached the end of the trace file (which means
         * the given timestamp is after the last event), or B. we found the
         * first event with a timestamp greater than the given timestamp.
         */
        readNextEvent();
        boolean done = (this.getCurrentEvent() == null);
        while (!done && (this.getCurrentEvent().timestamp < timestamp)) {
            readNextEvent();
            done = (this.getCurrentEvent() == null);
            offset++;
        }
        return offset;
    }

    public long seekIndex(long index) throws CTFReaderException {
        /*
         * we need to check if a trace is empty too.
         */
        StreamInputPacketIndexEntry sipie = null;
        /*
         * Search in the index for the packet to search in.
         */
        this.packetIndexIt = this.streamInput.getIndex().searchIndex(index);
        /*
         * Switch to this packet.
         */
        goToNextPacket();

        sipie = this.packetReader.getCurrentPacket();
        /*
         * Read the first packet
         */
        readNextEvent();
        /*
         * get the current index
         */
        if (this.packetReader.getCurrentPacket() == null)  {
            if( !((sipie.getIndexBegin() == 0) && (sipie.getIndexEnd() == Long.MAX_VALUE))) {
                throw new CTFReaderException(
                        "Current packet null in index seek, did you index your trace yet?"); //$NON-NLS-1$
            }
            return 0;
        }
        return this.packetReader.getCurrentPacket().getIndexBegin();

    }

    public void goToLastEvent() throws CTFReaderException {
        /*
         * Search in the index for the packet to search in.
         */
        int len = this.streamInput.getIndex().getEntries().size();
        int back = 0;
        long desired_timestamp = -1;
        do {
            back++;
            StreamInputPacketIndexEntry entry = this.streamInput.getIndex()
                    .getEntries().get(len - back);
            desired_timestamp = entry.getTimestampBegin() + 1;
            seek(desired_timestamp);

        } while (!this.packetReader.hasMoreEvents());
        /*
         * Go until the end of that packet
         */

        int packet_size = 0;
        while (this.packetReader.hasMoreEvents()) {
            this.packetReader.readNextEvent();
            packet_size++;
        }
        seek(desired_timestamp);
        for (int i = 0; i < (packet_size - 1); i++) {
            this.packetReader.readNextEvent();
        }
    }

    public void setCurrentEvent(EventDefinition currentEvent) {
        this.currentEvent = currentEvent;
    }

    /**
     * @return the packetIndexIt
     */
    private ListIterator<StreamInputPacketIndexEntry> getPacketIndexIt() {
        return packetIndexIt;
    }

    /**
     * @return the packetReader
     */
    public StreamInputPacketReader getPacketReader() {
        return packetReader;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + name;
        result = (prime * result)
                + ((streamInput == null) ? 0 : streamInput.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
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

}
