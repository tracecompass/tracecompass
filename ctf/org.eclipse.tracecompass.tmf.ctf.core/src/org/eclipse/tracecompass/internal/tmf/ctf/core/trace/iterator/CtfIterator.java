/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Florian Wininger - Performance improvements
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ctf.core.trace.iterator;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.trace.CTFStreamInputReader;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader;
import org.eclipse.tracecompass.internal.tmf.ctf.core.Activator;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;
import org.eclipse.tracecompass.tmf.ctf.core.context.CtfLocation;
import org.eclipse.tracecompass.tmf.ctf.core.context.CtfLocationInfo;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;

/**
 * The CTF trace reader iterator.
 *
 * It doesn't reserve a file handle, so many iterators can be used without
 * worries of I/O errors or resource exhaustion.
 *
 * @author Matthew Khouzam
 */
public class CtfIterator extends CTFTraceReader
        implements ITmfContext, Comparable<CtfIterator> {

    /** An invalid location */
    public static final CtfLocation NULL_LOCATION = new CtfLocation(CtfLocation.INVALID_LOCATION);

    private final @NonNull CtfTmfTrace fTrace;

    private CtfLocation fCurLocation;
    private long fCurRank;

    private CtfLocation fPreviousLocation;
    private CtfTmfEvent fPreviousEvent;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Create a new CTF trace iterator, which initially points at the first
     * event in the trace.
     *
     * @param ctfTrace
     *            The {@link CTFTrace} linked to the trace. It should be
     *            provided by the corresponding 'ctfTmfTrace'.
     *
     * @param ctfTmfTrace
     *            The {@link CtfTmfTrace} to iterate over
     * @throws CTFException
     *             If the iterator couldn't not be instantiated, probably due to
     *             a read error.
     */
    public CtfIterator(CTFTrace ctfTrace, @NonNull CtfTmfTrace ctfTmfTrace) throws CTFException {
        super(ctfTrace);
        fTrace = ctfTmfTrace;
        if (hasMoreEvents()) {
            fCurLocation = new CtfLocation(ctfTmfTrace.getStartTime());
            fCurRank = 0;
        } else {
            setUnknownLocation();
        }
    }

    /**
     * Create a new CTF trace iterator, which will initially point to the given
     * location/rank.
     *
     * @param ctfTrace
     *            The {@link CTFTrace} linked to the trace. It should be
     *            provided by the corresponding 'ctfTmfTrace'.
     * @param ctfTmfTrace
     *            The {@link CtfTmfTrace} to iterate over
     * @param ctfLocationData
     *            The initial timestamp the iterator will be pointing to
     * @param rank
     *            The initial rank
     * @throws CTFException
     *             If the iterator couldn't not be instantiated, probably due to
     *             a read error.
     */
    public CtfIterator(CTFTrace ctfTrace, @NonNull CtfTmfTrace ctfTmfTrace, CtfLocationInfo ctfLocationData, long rank)
            throws CTFException {
        super(ctfTrace);

        this.fTrace = ctfTmfTrace;
        if (this.hasMoreEvents()) {
            this.fCurLocation = new CtfLocation(ctfLocationData);
            if (this.getCurrentEvent().getTimestamp().getValue() != ctfLocationData.getTimestamp()) {
                this.seek(ctfLocationData);
                this.fCurRank = rank;
            }
        } else {
            setUnknownLocation();
        }
    }

    @Override
    public void dispose() {
        close();
    }

    private void setUnknownLocation() {
        fCurLocation = NULL_LOCATION;
        fCurRank = UNKNOWN_RANK;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Return this iterator's trace.
     *
     * @return CtfTmfTrace The iterator's trace
     */
    public CtfTmfTrace getCtfTmfTrace() {
        return fTrace;
    }

    /**
     * Return the current event pointed to by the iterator.
     *
     * @return CtfTmfEvent The current event
     */
    public synchronized CtfTmfEvent getCurrentEvent() {
        final CTFStreamInputReader top = super.getPrio().peek();
        if (top != null) {
            if (!fCurLocation.equals(fPreviousLocation)) {
                fPreviousLocation = fCurLocation;
                fPreviousEvent = fTrace.getEventFactory().createEvent(fTrace, checkNotNull(top.getCurrentEvent()), top.getFilename());
            }
            return fPreviousEvent;
        }
        return null;
    }

    /**
     * Return the current timestamp location pointed to by the iterator. This is
     * the timestamp for use in CtfLocation, not the event timestamp.
     *
     * @return long The current timestamp location
     */
    public synchronized long getCurrentTimestamp() {
        final CTFStreamInputReader top = super.getPrio().peek();
        if (top != null) {
            IEventDefinition currentEvent = top.getCurrentEvent();
            if (currentEvent != null) {
                long ts = currentEvent.getTimestamp();
                return fTrace.timestampCyclesToNanos(ts);
            }
        }
        return 0;
    }

    /**
     * Seek this iterator to a given location.
     *
     * @param ctfLocationData
     *            The LocationData representing the position to seek to
     * @return boolean True if the seek was successful, false if there was an
     *         error seeking.
     */
    public synchronized boolean seek(CtfLocationInfo ctfLocationData) {
        boolean ret = false;
        if (ctfLocationData.equals(CtfLocation.INVALID_LOCATION)) {
            fCurLocation = NULL_LOCATION;
            return false;
        }

        /* Avoid the cost of seeking at the current location. */
        if (fCurLocation.getLocationInfo().equals(ctfLocationData)) {
            return super.hasMoreEvents();
        }
        /* Update location to make sure the current event is updated */
        fCurLocation = new CtfLocation(ctfLocationData);

        /* Adjust the timestamp depending on the trace's offset */
        final long seekToTimestamp = ctfLocationData.getTimestamp();
        final long offsetTimestamp = this.getCtfTmfTrace().timestampNanoToCycles(seekToTimestamp);
        try {
            if (offsetTimestamp < 0) {
                ret = super.seek(0L);
            } else {
                ret = super.seek(offsetTimestamp);
            }
        } catch (CTFException e) {
            Activator.getDefault().logError(e.getMessage(), e);
            return false;
        }
        /*
         * Check if there is already one or more events for that timestamp, and
         * assign the location index correctly
         */
        long index = 0;
        ITmfEvent currentEvent = getCurrentEvent();
        ret &= (currentEvent != null);
        long offset = ctfLocationData.getIndex();
        while (currentEvent != null && index < offset) {
            if (seekToTimestamp >= Objects.requireNonNull(currentEvent).getTimestamp().getValue()) {
                index++;
            } else {
                index = 0;
                break;
            }
            ret = advance();
            currentEvent = getCurrentEvent();
        }
        /* Update the current location accordingly */
        if (ret) {
            long time = Objects.requireNonNull(currentEvent).getTimestamp().getValue();
            fCurLocation = new CtfLocation(new CtfLocationInfo(time, time != seekToTimestamp ? 0 : index));
        } else {
            fCurLocation = NULL_LOCATION;
        }
        return ret;
    }

    // ------------------------------------------------------------------------
    // CTFTraceReader
    // ------------------------------------------------------------------------

    @Override
    public boolean seek(long timestamp) {
        return seek(new CtfLocationInfo(timestamp, 0));
    }

    @Override
    public synchronized boolean advance() {
        boolean ret = false;
        try {
            ret = super.advance();
        } catch (CTFException e) {
            Activator.getDefault().logError(e.getMessage(), e);
        }

        if (ret) {
            long timestamp = fCurLocation.getLocationInfo().getTimestamp();
            final long timestampValue = getCurrentTimestamp();
            if (timestamp == timestampValue) {
                long index = fCurLocation.getLocationInfo().getIndex();
                fCurLocation = new CtfLocation(timestampValue, index + 1);
            } else {
                fCurLocation = new CtfLocation(timestampValue, 0L);
            }
        } else {
            fCurLocation = NULL_LOCATION;
        }
        return ret;
    }

    // ------------------------------------------------------------------------
    // ITmfContext
    // ------------------------------------------------------------------------

    @Override
    public long getRank() {
        return fCurRank;
    }

    @Override
    public void setRank(long rank) {
        fCurRank = rank;
    }

    @Override
    public void increaseRank() {
        /* Only increase the rank if it's valid */
        if (hasValidRank()) {
            fCurRank++;
        }
    }

    @Override
    public boolean hasValidRank() {
        return (getRank() >= 0);
    }

    @Override
    public void setLocation(ITmfLocation location) {
        // FIXME alex: isn't there a cleaner way than a cast here?
        fCurLocation = (CtfLocation) location;
        seek(((CtfLocation) location).getLocationInfo());
    }

    @Override
    public CtfLocation getLocation() {
        return fCurLocation;
    }

    // ------------------------------------------------------------------------
    // Comparable
    // ------------------------------------------------------------------------

    @Override
    public int compareTo(final CtfIterator o) {
        if (getRank() < o.getRank()) {
            return -1;
        } else if (getRank() > o.getRank()) {
            return 1;
        }
        return 0;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fTrace, fCurLocation, fCurRank);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof CtfIterator)) {
            return false;
        }
        CtfIterator other = (CtfIterator) obj;
        if (!Objects.equals(fTrace, other.fTrace)) {
            return false;
        }
        if (!Objects.equals(fCurLocation, other.fCurLocation)) {
            return false;
        }
        return (fCurRank == other.fCurRank);
    }
}
