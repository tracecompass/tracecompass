/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.trace;

import java.util.Collection;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Vector;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.internal.ctf.core.Activator;
import org.eclipse.linuxtools.internal.ctf.core.trace.Stream;
import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInput;
import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInputPacketIndexEntry;
import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInputReaderTimestampComparator;

/**
 * Reads the events of a trace.
 */

public class CTFTraceReader {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The trace to read from.
     */
    private final CTFTrace trace;

    /**
     * Vector of all the trace file readers.
     */
    private final Vector<StreamInputReader> streamInputReaders = new Vector<StreamInputReader>();

    /**
     * Priority queue to order the trace file readers by timestamp.
     */
    protected PriorityQueue<StreamInputReader> prio;

    /**
     * Array to count the number of event per trace file.
     */
    private int[] eventCountPerTraceFile;

    /**
     * Timestamp of the first event in the trace
     */
    private long startTime;

    /**
     * Timestamp of the last event read so far
     */
    private long endTime;

    /**
     * Current event index
     */
    private long fIndex;

    private final HashMap<Integer, Long> startIndex;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a TraceReader to read a trace.
     *
     * @param trace
     *            The trace to read from.
     * @throws CTFReaderException
     */
    public CTFTraceReader(CTFTrace trace) {
        this.trace = trace;

        /**
         * Create the trace file readers.
         */
        createStreamInputReaders();

        /**
         * Populate the timestamp-based priority queue.
         */
        populateStreamInputReaderHeap();

        /**
         * Get the start Time of this trace
         * bear in mind that the trace could be empty.
         */
        this.startTime = 0;// prio.peek().getPacketReader().getCurrentPacket().getTimestampBegin();
        if (hasMoreEvents()) {
            this.startTime = prio.peek().getCurrentEvent().getTimestamp();
            this.endTime = this.startTime;
            this.fIndex = 0;
        }
        startIndex = new HashMap<Integer, Long>();
    }

    /**
     * Copy constructor
     */
    public CTFTraceReader copyFrom() {
        CTFTraceReader newReader = null;

        newReader = new CTFTraceReader(this.trace);
        newReader.startTime = this.startTime;
        newReader.endTime = this.endTime;
        return newReader;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Return the start time of this trace (== timestamp of the first event)
     *
     * @return the trace start time
     */
    public long getStartTime() {
        return this.startTime;
    }

    /**
     * @return the index
     */
    public long getIndex() {
        return fIndex;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Creates one trace file reader per trace file contained in the trace.
     */
    private void createStreamInputReaders() {
        Collection<Stream> streams = this.trace.getStreams().values();

        /*
         * For each stream.
         */
        for (Stream stream : streams) {
            Set<StreamInput> streamInputs = stream.getStreamInputs();

            /*
             * For each trace file of the stream.
             */
            for (StreamInput streamInput : streamInputs) {
                /*
                 * Create a reader.
                 */
                StreamInputReader streamInputReader = new StreamInputReader(
                        streamInput);

                /*
                 * Add it to the group.
                 */
                this.streamInputReaders.add(streamInputReader);
            }
        }

        /*
         * Create the array to count the number of event per trace file.
         */
        this.eventCountPerTraceFile = new int[this.streamInputReaders.size()];
    }

    /**
     * Initializes the priority queue used to choose the trace file with the
     * lower next event timestamp.
     */
    private void populateStreamInputReaderHeap() {
        /*
         * Create the priority queue with a size twice as bigger as the number
         * of reader in order to avoid constant resizing.
         */
        this.prio = new PriorityQueue<StreamInputReader>(
                this.streamInputReaders.size() * 2,
                new StreamInputReaderTimestampComparator());

        int pos = 0;

        for (StreamInputReader reader : this.streamInputReaders) {
            /*
             * Add each trace file reader in the priority queue, if we are able
             * to read an event from it.
             */
            if (reader.readNextEvent()) {
                this.prio.add(reader);

                this.eventCountPerTraceFile[pos] = 0;
                reader.setName(pos);

                pos++;
            }
        }
    }

    /**
     * Get the current event, which is the current event of the trace file
     * reader with the lowest timestamp.
     *
     * @return An event definition, or null of the trace reader reached the end
     *         of the trace.
     */
    public EventDefinition getCurrentEventDef() {
        StreamInputReader top = getTopStream();

        return (top != null) ? top.getCurrentEvent() : null;
    }

    /**
     * Go to the next event.
     *
     * @return True if an event was read.
     */
    public boolean advance() {
        /*
         * Index the
         */
        /*
         * Remove the reader from the top of the priority queue.
         */
        StreamInputReader top = this.prio.poll();

        /*
         * If the queue was empty.
         */
        if (top == null) {
            return false;
        }
        /*
         * index if needed
         */
        if (hasMoreEvents()) {
            StreamInputPacketReader packetReader = top.getPacketReader();
            boolean packetHasMoreEvents = packetReader.hasMoreEvents();
            StreamInputPacketIndexEntry currentPacket = packetReader
                    .getCurrentPacket();
            if (!packetHasMoreEvents) {
                int n = this.streamInputReaders.indexOf(top);
                if (!startIndex.containsKey(n)) {
                    startIndex.put(n, 0L);
                }
                currentPacket.setIndexBegin(startIndex.get(n));
                currentPacket.setIndexEnd(fIndex);
                startIndex.put(n, fIndex + 1);
            }
        }
        /*
         * Read the next event of this reader.
         */
        if (top.readNextEvent()) {
            /*
             * Add it back in the queue.
             */
            this.prio.add(top);
            final long topEnd = top.getCurrentEvent().getTimestamp();
            this.endTime = Math.max(topEnd, this.endTime);
            this.eventCountPerTraceFile[top.getName()]++;
            /*
             * increment the index
             */
            fIndex++;
        }
        boolean hasMoreEvents = hasMoreEvents();

        /*
         * If there is no reader in the queue, it means the trace reader reached
         * the end of the trace.
         */
        return hasMoreEvents;
    }

    /**
     * Go to the last event in the trace.
     *
     * @throws CTFReaderException
     */
    public void goToLastEvent() throws CTFReaderException {

        for (StreamInputReader streamInputReader : this.streamInputReaders) {
            /*
             * Seek the trace reader.
             */
            streamInputReader.goToLastEvent();
        }
        int count = prio.size();
        for (int i = 0; i < (count - 1); i++) {
            advance();
        }
    }

    /**
     * Seeks to a given timestamp It will go to the event just after the
     * timestamp or the timestamp itself. if a if a trace is 10 20 30 40 and
     * you're looking for 19, it'll give you 20, it you want 20, you'll get 20,
     * if you want 21, you'll get 30. You want -inf, you'll get the first
     * element, you want +inf, you'll get the end of the file with no events.
     *
     * @param timestamp
     *            the timestamp to seek to
     * @return true if the trace has more events following the timestamp
     */
    public boolean seek(long timestamp) {
        /*
         * Remove all the trace readers from the priority queue
         */
        this.prio.clear();
        fIndex = 0;
        long offset = 0;
        for (StreamInputReader streamInputReader : this.streamInputReaders) {
            /*
             * Seek the trace reader.
             */
            offset += streamInputReader.seek(timestamp);

            /*
             * Add it to the priority queue if there is a current event.
             */

        }
        for (StreamInputReader streamInputReader : this.streamInputReaders) {
            if (streamInputReader.getCurrentEvent() != null) {
                this.prio.add(streamInputReader);
                fIndex = Math.max(fIndex, streamInputReader.getPacketReader()
                        .getCurrentPacket().getIndexBegin()
                        + offset);
            }
        }
        return hasMoreEvents();
    }

    public boolean seekIndex(long index) {
        this.prio.clear();

        long tempIndex = Long.MIN_VALUE;
        long tempTimestamp = Long.MIN_VALUE;
        try {
            for (StreamInputReader streamInputReader : this.streamInputReaders) {
                /*
                 * Seek the trace reader.
                 */
                final long streamIndex = streamInputReader.seekIndex(index);
                if (streamInputReader.getCurrentEvent() != null) {
                    tempIndex = Math.max(tempIndex, streamIndex);
                    EventDefinition currentEvent = streamInputReader
                            .getCurrentEvent();
                    /*
                     * Maybe we're at the beginning of a trace.
                     */
                    if (currentEvent == null) {
                        streamInputReader.readNextEvent();
                        currentEvent = streamInputReader.getCurrentEvent();
                    }
                    if (currentEvent != null) {
                        tempTimestamp = Math.max(tempTimestamp,
                                currentEvent.getTimestamp());
                    } else {
                        /*
                         * probably beyond the last event
                         */
                        tempIndex = goToZero();
                    }
                }

            }
        } catch (CTFReaderException e) {
            /*
             * Important, if it failed, it's because it's not yet indexed, so we
             * have to manually advance to the right value.
             */
            tempIndex = goToZero();
        }
        for (StreamInputReader streamInputReader : this.streamInputReaders) {
            /*
             * Add it to the priority queue if there is a current event.
             */

            if (streamInputReader.getCurrentEvent() != null) {
                this.prio.add(streamInputReader);
            }
        }
        if (tempIndex == Long.MAX_VALUE) {
            tempIndex = 0;
        }
        long pos = tempIndex;
        if (index > tempIndex) {
            /*
             * advance for offset
             */
            while ((prio.peek().getCurrentEvent().getTimestamp() < tempTimestamp)
                    && hasMoreEvents()) {
                this.advance();
            }

            for (pos = tempIndex; (pos < index) && hasMoreEvents(); pos++) {
                this.advance();
            }
        }
        this.fIndex = pos;
        return hasMoreEvents();
    }

    /**
     * Go to the first entry of a trace
     *
     * @return 0, the first index.
     */
    private long goToZero() {
        long tempIndex;
        for (StreamInputReader streamInputReader : this.streamInputReaders) {
            /*
             * Seek the trace reader.
             */
            streamInputReader.seek(0);
        }
        tempIndex = 0;
        return tempIndex;
    }

    public StreamInputReader getTopStream() {
        return this.prio.peek();
    }

    /**
     * Does the trace have more events?
     *
     * @return true if yes.
     */
    public boolean hasMoreEvents() {
        return this.prio.size() > 0;
    }

    /**
     * Prints the event count stats.
     */
    public void printStats() {
        printStats(60);
    }

    /**
     * Prints the event count stats.
     *
     * @param width
     *            Width of the display.
     */
    public void printStats(int width) {
        int numEvents = 0;
        if (width == 0) {
            return;
        }

        for (int i : this.eventCountPerTraceFile) {
            numEvents += i;
        }

        for (int j = 0; j < this.eventCountPerTraceFile.length; j++) {
            StreamInputReader se = this.streamInputReaders.get(j);

            int len = (width * this.eventCountPerTraceFile[se.getName()])
                    / numEvents;

            StringBuilder sb = new StringBuilder(se.getFilename() + "\t["); //$NON-NLS-1$

            for (int i = 0; i < len; i++) {
                sb.append('+');
            }

            for (int i = len; i < width; i++) {
                sb.append(' ');
            }

            sb.append("]\t" + this.eventCountPerTraceFile[se.getName()] + " Events"); //$NON-NLS-1$//$NON-NLS-2$
            Activator.getDefault().log(sb.toString());
        }
    }

    public long getEndTime() {
        return this.endTime;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (int) (endTime ^ (endTime >>> 32));
        result = (prime * result) + (int) (startTime ^ (startTime >>> 32));
        result = (prime * result)
                + ((streamInputReaders == null) ? 0 : streamInputReaders
                        .hashCode());
        result = (prime * result) + ((trace == null) ? 0 : trace.hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        CTFTraceReader other = (CTFTraceReader) obj;
        if (endTime != other.endTime) {
            return false;
        }
        if (startTime != other.startTime) {
            return false;
        }
        if (streamInputReaders == null) {
            if (other.streamInputReaders != null) {
                return false;
            }
        } else if (!streamInputReaders.equals(other.streamInputReaders)) {
            return false;
        }
        if (trace == null) {
            if (other.trace != null) {
                return false;
            }
        } else if (!trace.equals(other.trace)) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        /* Only for debugging, shouldn't be externalized */
        return "CTFTraceReader [trace=" + trace + ']'; //$NON-NLS-1$
    }

    public CTFTrace getTrace() {
        return trace;
    }
}
