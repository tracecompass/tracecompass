/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *     Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.trace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.IEventDeclaration;
import org.eclipse.linuxtools.internal.ctf.core.Activator;
import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInputReaderTimestampComparator;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

/**
 * A CTF trace reader. Reads the events of a trace.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Alexandre Montplaisir
 */
public class CTFTraceReader implements AutoCloseable {

    private static final int MIN_PRIO_SIZE = 16;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The trace to read from.
     */
    private final CTFTrace fTrace;

    /**
     * Vector of all the trace file readers.
     */
    private final List<CTFStreamInputReader> fStreamInputReaders = new ArrayList<>();

    /**
     * Priority queue to order the trace file readers by timestamp.
     */
    private PriorityQueue<CTFStreamInputReader> fPrio;

    /**
     * Array to count the number of event per trace file.
     */
    private long[] fEventCountPerTraceFile;

    /**
     * Timestamp of the first event in the trace
     */
    private long fStartTime;

    /**
     * Timestamp of the last event read so far
     */
    private long fEndTime;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a TraceReader to read a trace.
     *
     * @param trace
     *            The trace to read from.
     * @throws CTFReaderException
     *             if an error occurs
     */
    public CTFTraceReader(CTFTrace trace) throws CTFReaderException {
        fTrace = trace;
        fStreamInputReaders.clear();

        /**
         * Create the trace file readers.
         */
        createStreamInputReaders();

        /**
         * Populate the timestamp-based priority queue.
         */
        populateStreamInputReaderHeap();

        /**
         * Get the start Time of this trace bear in mind that the trace could be
         * empty.
         */
        fStartTime = 0;
        if (hasMoreEvents()) {
            fStartTime = getTopStream().getCurrentEvent().getTimestamp();
            setEndTime(fStartTime);
        }
    }

    /**
     * Copy constructor
     *
     * @return The new CTFTraceReader
     * @throws CTFReaderException
     *             if an error occurs
     */
    public CTFTraceReader copyFrom() throws CTFReaderException {
        CTFTraceReader newReader = null;

        newReader = new CTFTraceReader(fTrace);
        newReader.fStartTime = fStartTime;
        newReader.setEndTime(fEndTime);
        return newReader;
    }

    /**
     * Dispose the CTFTraceReader
     *
     * @since 3.0
     */
    @Override
    public void close() {
        for (CTFStreamInputReader reader : fStreamInputReaders) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Activator.logError(e.getMessage(), e);
                }
            }
        }
        fStreamInputReaders.clear();
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
        return fStartTime;
    }

    /**
     * Set the trace's end time
     *
     * @param endTime
     *            The end time to use
     */
    protected final void setEndTime(long endTime) {
        fEndTime = endTime;
    }

    /**
     * Get the priority queue of this trace reader.
     *
     * @return The priority queue of input readers
     * @since 2.0
     */
    protected PriorityQueue<CTFStreamInputReader> getPrio() {
        return fPrio;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Creates one trace file reader per trace file contained in the trace.
     *
     * @throws CTFReaderException
     *             if an error occurs
     */
    private void createStreamInputReaders() throws CTFReaderException {
        /*
         * For each stream.
         */
        for (CTFStream stream : fTrace.getStreams()) {
            Set<CTFStreamInput> streamInputs = stream.getStreamInputs();

            /*
             * For each trace file of the stream.
             */
            for (CTFStreamInput streamInput : streamInputs) {

                /*
                 * Create a reader and add it to the group.
                 */
                fStreamInputReaders.add(new CTFStreamInputReader(streamInput));
            }
        }

        /*
         * Create the array to count the number of event per trace file.
         */
        fEventCountPerTraceFile = new long[fStreamInputReaders.size()];
    }

    /**
     * Update the priority queue to make it match the parent trace
     *
     * @throws CTFReaderException
     *             An error occured
     *
     * @since 3.0
     */
    public void update() throws CTFReaderException {
        Set<CTFStreamInputReader> readers = new HashSet<>();
        for (CTFStream stream : fTrace.getStreams()) {
            Set<CTFStreamInput> streamInputs = stream.getStreamInputs();
            for (CTFStreamInput streamInput : streamInputs) {
                /*
                 * Create a reader.
                 */
                CTFStreamInputReader streamInputReader = new CTFStreamInputReader(
                        streamInput);

                /*
                 * Add it to the group.
                 */
                if (!fStreamInputReaders.contains(streamInputReader)) {
                    streamInputReader.readNextEvent();
                    fStreamInputReaders.add(streamInputReader);
                    readers.add(streamInputReader);
                }
            }
        }
        long[] temp = fEventCountPerTraceFile;
        fEventCountPerTraceFile = new long[readers.size() + temp.length];
        for (CTFStreamInputReader reader : readers) {
            fPrio.add(reader);
        }
        for (int i = 0; i < temp.length; i++) {
            fEventCountPerTraceFile[i] = temp[i];
        }
    }

    /**
     * Gets an iterable of the stream input readers, useful for foreaches
     *
     * @return the iterable of the stream input readers
     * @since 3.0
     */
    public Iterable<IEventDeclaration> getEventDeclarations() {
        ImmutableSet.Builder<IEventDeclaration> builder = new Builder<>();
        for (CTFStreamInputReader sir : fStreamInputReaders) {
            builder.addAll(sir.getEventDeclarations());
        }
        return builder.build();
    }

    /**
     * Initializes the priority queue used to choose the trace file with the
     * lower next event timestamp.
     *
     * @throws CTFReaderException
     *             if an error occurs
     */
    private void populateStreamInputReaderHeap() throws CTFReaderException {
        if (fStreamInputReaders.isEmpty()) {
            fPrio = new PriorityQueue<>(MIN_PRIO_SIZE,
                    new StreamInputReaderTimestampComparator());
            return;
        }

        /*
         * Create the priority queue with a size twice as bigger as the number
         * of reader in order to avoid constant resizing.
         */
        fPrio = new PriorityQueue<>(
                Math.max(fStreamInputReaders.size() * 2, MIN_PRIO_SIZE),
                new StreamInputReaderTimestampComparator());

        int pos = 0;

        for (CTFStreamInputReader reader : fStreamInputReaders) {
            /*
             * Add each trace file reader in the priority queue, if we are able
             * to read an event from it.
             */
            reader.setParent(this);
            CTFResponse readNextEvent = reader.readNextEvent();
            if (readNextEvent == CTFResponse.OK || readNextEvent == CTFResponse.WAIT) {
                fPrio.add(reader);

                fEventCountPerTraceFile[pos] = 0;
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
        CTFStreamInputReader top = getTopStream();
        return (top != null) ? top.getCurrentEvent() : null;
    }

    /**
     * Go to the next event.
     *
     * @return True if an event was read.
     * @throws CTFReaderException
     *             if an error occurs
     */
    public boolean advance() throws CTFReaderException {
        /*
         * Remove the reader from the top of the priority queue.
         */
        CTFStreamInputReader top = fPrio.poll();

        /*
         * If the queue was empty.
         */
        if (top == null) {
            return false;
        }
        /*
         * Read the next event of this reader.
         */
        switch (top.readNextEvent()) {
        case OK: {
            /*
             * Add it back in the queue.
             */
            fPrio.add(top);
            final long topEnd = fTrace.timestampCyclesToNanos(top.getCurrentEvent().getTimestamp());
            setEndTime(Math.max(topEnd, getEndTime()));
            fEventCountPerTraceFile[top.getName()]++;

            if (top.getCurrentEvent() != null) {
                fEndTime = Math.max(top.getCurrentEvent().getTimestamp(),
                        fEndTime);
            }
            break;
        }
        case WAIT: {
            fPrio.add(top);
            break;
        }
        case FINISH:
            break;
        case ERROR:
        default:
            // something bad happend
        }
        /*
         * If there is no reader in the queue, it means the trace reader reached
         * the end of the trace.
         */
        return hasMoreEvents();
    }

    /**
     * Go to the last event in the trace.
     *
     * @throws CTFReaderException
     *             if an error occurs
     */
    public void goToLastEvent() throws CTFReaderException {
        seek(getEndTime());
        while (fPrio.size() > 1) {
            advance();
        }
    }

    /**
     * Seeks to a given timestamp. It will seek to the nearest event greater or
     * equal to timestamp. If a trace is [10 20 30 40] and you are looking for
     * 19, it will give you 20. If you want 20, you will get 20, if you want 21,
     * you will get 30. The value -inf will seek to the first element and the
     * value +inf will seek to the end of the file (past the last event).
     *
     * @param timestamp
     *            the timestamp to seek to
     * @return true if there are events above or equal the seek timestamp, false
     *         if seek at the end of the trace (no valid event).
     * @throws CTFReaderException
     *             if an error occurs
     */
    public boolean seek(long timestamp) throws CTFReaderException {
        /*
         * Remove all the trace readers from the priority queue
         */
        fPrio.clear();
        for (CTFStreamInputReader streamInputReader : fStreamInputReaders) {
            /*
             * Seek the trace reader.
             */
            streamInputReader.seek(timestamp);

            /*
             * Add it to the priority queue if there is a current event.
             */
            if (streamInputReader.getCurrentEvent() != null) {
                fPrio.add(streamInputReader);
            }
        }
        return hasMoreEvents();
    }

    /**
     * Gets the stream with the oldest event
     *
     * @return the stream with the oldest event
     * @since 3.0
     */
    public CTFStreamInputReader getTopStream() {
        return fPrio.peek();
    }

    /**
     * Does the trace have more events?
     *
     * @return true if yes.
     */
    public final boolean hasMoreEvents() {
        return fPrio.size() > 0;
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

        for (long i : fEventCountPerTraceFile) {
            numEvents += i;
        }

        for (int j = 0; j < fEventCountPerTraceFile.length; j++) {
            CTFStreamInputReader se = fStreamInputReaders.get(j);

            long len = (width * fEventCountPerTraceFile[se.getName()])
                    / numEvents;

            StringBuilder sb = new StringBuilder(se.getFilename());
            sb.append("\t["); //$NON-NLS-1$

            for (int i = 0; i < len; i++) {
                sb.append('+');
            }

            for (long i = len; i < width; i++) {
                sb.append(' ');
            }

            sb.append("]\t" + fEventCountPerTraceFile[se.getName()] + " Events"); //$NON-NLS-1$//$NON-NLS-2$
            Activator.log(sb.toString());
        }
    }

    /**
     * Gets the last event timestamp that was read. This is NOT necessarily the
     * last event in a trace, just the last one read so far.
     *
     * @return the last event
     */
    public long getEndTime() {
        return fEndTime;
    }

    /**
     * Sets a trace to be live or not
     *
     * @param live
     *            whether the trace is live
     * @since 3.0
     */
    public void setLive(boolean live) {
        for (CTFStreamInputReader s : fPrio) {
            s.setLive(live);
        }
    }

    /**
     * Get if the trace is to read live or not
     *
     * @return whether the trace is live or not
     * @since 3.0
     *
     */
    public boolean isLive() {
        return getTopStream().isLive();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (int) (fStartTime ^ (fStartTime >>> 32));
        result = (prime * result) + fStreamInputReaders.hashCode();
        result = (prime * result) + ((fTrace == null) ? 0 : fTrace.hashCode());
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
        if (!(obj instanceof CTFTraceReader)) {
            return false;
        }
        CTFTraceReader other = (CTFTraceReader) obj;
        if (!fStreamInputReaders.equals(other.fStreamInputReaders)) {
            return false;
        }
        if (fTrace == null) {
            if (other.fTrace != null) {
                return false;
            }
        } else if (!fTrace.equals(other.fTrace)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        /* Only for debugging, shouldn't be externalized */
        return "CTFTraceReader [trace=" + fTrace + ']'; //$NON-NLS-1$
    }

    /**
     * Gets the parent trace
     *
     * @return the parent trace
     */
    public CTFTrace getTrace() {
        return fTrace;
    }
}
