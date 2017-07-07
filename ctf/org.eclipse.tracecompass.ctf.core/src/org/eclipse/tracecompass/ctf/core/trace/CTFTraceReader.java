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

package org.eclipse.tracecompass.ctf.core.trace;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.IEventDeclaration;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.internal.ctf.core.Activator;
import org.eclipse.tracecompass.internal.ctf.core.trace.StreamInputReaderTimestampComparator;

/**
 * A CTF trace reader. Reads the events of a trace.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Alexandre Montplaisir
 */
public class CTFTraceReader implements AutoCloseable {

    private static final int LINE_LENGTH = 60;

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
    private final List<CTFStreamInputReader> fStreamInputReaders = Collections.synchronizedList(new ArrayList<CTFStreamInputReader>());

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

    /**
     * Boolean to indicate if the CTFTraceReader has been closed
     */
    private boolean fClosed = false;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a TraceReader to read a trace.
     *
     * @param trace
     *            The trace to read from.
     * @throws CTFException
     *             if an error occurs
     */
    public CTFTraceReader(CTFTrace trace) throws CTFException {
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
         * Get the start time of this trace bear in mind that the trace could be
         * empty. Set the start time should be in nanoseconds in UTC.
         */
        fStartTime = 0;
        if (hasMoreEvents()) {
            fStartTime = fTrace.timestampCyclesToNanos(checkNotNull(getTopStream().getCurrentEvent()).getTimestamp());
            setEndTime(fStartTime);
        }
    }

    /**
     * Copy constructor
     *
     * @return The new CTFTraceReader
     * @throws CTFException
     *             if an error occurs
     */
    public CTFTraceReader copyFrom() throws CTFException {
        CTFTraceReader newReader = null;

        newReader = new CTFTraceReader(fTrace);
        newReader.fStartTime = fStartTime;
        newReader.fEndTime = fEndTime;
        return newReader;
    }

    /**
     * Dispose the CTFTraceReader
     */
    @Override
    public void close() {
        synchronized (fStreamInputReaders) {
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
        fPrio.clear();
        fClosed = true;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Return the start time of this trace (== timestamp of the first event) in UTC
     * nanoseconds
     *
     * @return the trace start time
     */
    public long getStartTime() {
        return fStartTime;
    }

    /**
     * Set the trace's end time, must be in nanoseconds in UTC
     *
     * @param endTime
     *            The end time to use.
     */
    protected final void setEndTime(long endTime) {
        fEndTime = endTime;
    }

    /**
     * Get the priority queue of this trace reader.
     *
     * @return The priority queue of input readers
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
     * @throws CTFException
     *             if an error occurs
     */
    private void createStreamInputReaders() throws CTFException {
        /*
         * For each stream.
         */
        for (ICTFStream stream : fTrace.getStreams()) {
            Set<CTFStreamInput> streamInputs = stream.getStreamInputs();

            /*
             * For each trace file of the stream.
             */
            for (CTFStreamInput streamInput : streamInputs) {

                /*
                 * Create a reader and add it to the group.
                 */
                fStreamInputReaders.add(new CTFStreamInputReader(checkNotNull(streamInput)));
            }
        }

        /*
         * Create the array to count the number of event per trace file.
         */
        fEventCountPerTraceFile = new long[fStreamInputReaders.size()];
    }

    /**
     * Returns whether or not this CTFTraceReader has been closed
     *
     * @return true if it has been closed, false else
     * @since 1.1
     */
    public boolean isClosed() {
        return fClosed;
    }

    /**
     * Update the priority queue to make it match the parent trace
     *
     * @throws CTFException
     *             An error occured
     */
    public void update() throws CTFException {
        Set<CTFStreamInputReader> readers = new HashSet<>();
        for (ICTFStream stream : fTrace.getStreams()) {
            Set<CTFStreamInput> streamInputs = stream.getStreamInputs();
            for (CTFStreamInput streamInput : streamInputs) {
                /*
                 * Create a reader to check if it already exists in the list. If it doesn't, add
                 * it.
                 */
                try (CTFStreamInputReader streamInputReader = new CTFStreamInputReader(checkNotNull(streamInput))) {
                    if (!fStreamInputReaders.contains(streamInputReader)) {
                        CTFStreamInputReader streamInputReaderToAdd = new CTFStreamInputReader(checkNotNull(streamInput));
                        streamInputReaderToAdd.readNextEvent();
                        fStreamInputReaders.add(streamInputReaderToAdd);
                        readers.add(streamInputReaderToAdd);
                    }
                } catch (IOException e) {
                    Activator.logError(e.getMessage(), e);
                }
            }
        }
        long[] temp = fEventCountPerTraceFile;
        fEventCountPerTraceFile = new long[readers.size() + temp.length];
        for (CTFStreamInputReader reader : readers) {
            fPrio.add(reader);
        }
        System.arraycopy(temp, 0, fEventCountPerTraceFile, 0, temp.length);
    }

    /**
     * Gets an iterable of the stream input readers, useful for foreaches
     *
     * @return the iterable of the stream input readers
     */
    public Iterable<IEventDeclaration> getEventDeclarations() {
        Set<IEventDeclaration> retSet = new HashSet<>();
        for (CTFStreamInputReader sir : fStreamInputReaders) {
            retSet.addAll(sir.getEventDeclarations());
        }
        retSet.remove(null);
        return retSet;
    }

    /**
     * Initializes the priority queue used to choose the trace file with the lower
     * next event timestamp.
     *
     * @throws CTFException
     *             if an error occurs
     */
    private void populateStreamInputReaderHeap() throws CTFException {
        if (fStreamInputReaders.isEmpty()) {
            fPrio = new PriorityQueue<>(MIN_PRIO_SIZE,
                    new StreamInputReaderTimestampComparator());
            return;
        }

        /*
         * Create the priority queue with a size twice as bigger as the number of reader
         * in order to avoid constant resizing.
         */
        fPrio = new PriorityQueue<>(
                Math.max(fStreamInputReaders.size() * 2, MIN_PRIO_SIZE),
                new StreamInputReaderTimestampComparator());

        int pos = 0;

        for (CTFStreamInputReader reader : fStreamInputReaders) {
            /*
             * Add each trace file reader in the priority queue, if we are able to read an
             * event from it.
             */
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
     * Get the current event, which is the current event of the trace file reader
     * with the lowest timestamp.
     *
     * @return An event definition, or null of the trace reader reached the end of
     *         the trace.
     * @since 2.0
     */
    public IEventDefinition getCurrentEventDef() {
        CTFStreamInputReader top = getTopStream();
        return (top != null) ? top.getCurrentEvent() : null;
    }

    /**
     * Go to the next event.
     *
     * @return True if an event was read.
     * @throws CTFException
     *             if an error occurs
     */
    public boolean advance() throws CTFException {
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
            /*
             * We're in OK, there's a guaranteed top#getCurrentEvent() unless another thread
             * does something bad.
             */
            IEventDefinition currentEvent = checkNotNull(top.getCurrentEvent());
            final long topEnd = fTrace.timestampCyclesToNanos(currentEvent.getTimestamp());
            setEndTime(Math.max(topEnd, getEndTime()));
            fEventCountPerTraceFile[top.getName()]++;
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
         * If there is no reader in the queue, it means the trace reader reached the end
         * of the trace.
         */
        return hasMoreEvents();
    }

    /**
     * Go to the last event in the trace.
     *
     * @throws CTFException
     *             if an error occurs
     */
    public void goToLastEvent() throws CTFException {
        long endTime = Long.MIN_VALUE;
        for (CTFStreamInputReader sir : fPrio) {
            sir.goToLastEvent();
            IEventDefinition currentEvent = sir.getCurrentEvent();
            if (currentEvent != null) {
                endTime = Math.max(currentEvent.getTimestamp(), endTime);
            }
        }
        setEndTime(fTrace.timestampCyclesToNanos(endTime));
        // Go right before the end time, may be several events before the end but they
        // would all be the "last" event occurring at the same time
        seek(endTime);
    }

    /**
     * Seeks to a given timestamp. It will seek to the nearest event greater or
     * equal to timestamp. If a trace is [10 20 30 40] and you are looking for 19,
     * it will give you 20. If you want 20, you will get 20, if you want 21, you
     * will get 30. The value -inf will seek to the first element and the value +inf
     * will seek to the end of the file (past the last event). The seek method
     * requires relative time, so use {@link CTFTrace#timestampNanoToCycles(long)}
     * to convert the time if it is in UTC nanoseconds.
     *
     * @param timestamp
     *            the "timestamp" to seek to in ctf relative time (cycles)
     * @return true if there are events above or equal the seek timestamp, false if
     *         seek at the end of the trace (no valid event).
     * @throws CTFException
     *             if an error occurs
     */
    public boolean seek(long timestamp) throws CTFException {
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
        return !fPrio.isEmpty();
    }

    /**
     * Prints the event count stats.
     */
    public void printStats() {
        printStats(LINE_LENGTH);
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
     * Gets the last event timestamp that was read. This is NOT necessarily the last
     * event in a trace, just the last one read so far. Time is in nanoseconds in
     * UTC
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

    /**
     * This will read the entire trace and populate all the indexes. The reader will
     * then be reset to the first event in the trace.
     *
     * Do not call in the fast path.
     *
     * @throws CTFException
     *             A trace reading error occurred
     * @since 1.0
     */
    public void populateIndex() throws CTFException {
        for (CTFStreamInputReader sir : fPrio) {
            sir.goToLastEvent();
        }
        seek(0);
    }
}
