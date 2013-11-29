/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader;
import org.eclipse.linuxtools.ctf.core.trace.StreamInputReader;
import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;

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

    private final CtfTmfTrace fTrace;

    private CtfLocation fCurLocation;
    private long fCurRank;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Create a new CTF trace iterator, which initially points at the first
     * event in the trace.
     *
     * @param trace
     *            The trace to iterate over
     * @throws CTFReaderException
     *             If the iterator couldn't not be instantiated, probably due to
     *             a read error.
     */
    public CtfIterator(CtfTmfTrace trace) throws CTFReaderException {
        super(trace.getCTFTrace());
        fTrace = trace;
        if (this.hasMoreEvents()) {
            fCurLocation = new CtfLocation(trace.getStartTime());
            fCurRank = 0;
        } else {
            setUnknownLocation();
        }
    }

    /**
     * Create a new CTF trace iterator, which will initially point to the given
     * location/rank.
     *
     * @param trace
     *            The trace to iterate over
     * @param ctfLocationData
     *            The initial timestamp the iterator will be pointing to
     * @param rank
     *            The initial rank
     * @throws CTFReaderException
     *             If the iterator couldn't not be instantiated, probably due to
     *             a read error.
     * @since 2.0
     */
    public CtfIterator(CtfTmfTrace trace, CtfLocationInfo ctfLocationData, long rank)
            throws CTFReaderException {
        super(trace.getCTFTrace());

        this.fTrace = trace;
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
    public CtfTmfEvent getCurrentEvent() {
        final StreamInputReader top = super.getPrio().peek();
        if (top != null) {
            return CtfTmfEventFactory.createEvent(top.getCurrentEvent(),
                    top.getFilename(), fTrace);
        }
        return null;
    }

    /**
     * Seek this iterator to a given location.
     *
     * @param ctfLocationData
     *            The LocationData representing the position to seek to
     * @return boolean True if the seek was successful, false if there was an
     *         error seeking.
     * @since 2.0
     */
    public synchronized boolean seek(CtfLocationInfo ctfLocationData) {
        boolean ret = false;

        /* Avoid the cost of seeking at the current location. */
        if (fCurLocation.getLocationInfo().equals(ctfLocationData)) {
            return super.hasMoreEvents();
        }

        /* Adjust the timestamp depending on the trace's offset */
        long currTimestamp = ctfLocationData.getTimestamp();
        final long offsetTimestamp = this.getCtfTmfTrace().getCTFTrace().timestampNanoToCycles(currTimestamp);
        try {
            if (offsetTimestamp < 0) {
                ret = super.seek(0L);
            } else {
                ret = super.seek(offsetTimestamp);
            }
        } catch (CTFReaderException e) {
            Activator.logError(e.getMessage(), e);
            return false;
        }
        /*
         * Check if there is already one or more events for that timestamp, and
         * assign the location index correctly
         */
        long index = 0;
        final CtfTmfEvent currentEvent = this.getCurrentEvent();
        if (currentEvent != null) {
            currTimestamp = currentEvent.getTimestamp().getValue();

            for (long i = 0; i < ctfLocationData.getIndex(); i++) {
                if (currTimestamp == currentEvent.getTimestamp().getValue()) {
                    index++;
                } else {
                    index = 0;
                }
                this.advance();
            }
        } else {
            ret = false;
        }
        /* Seek the current location accordingly */
        if (ret) {
            fCurLocation = new CtfLocation(new CtfLocationInfo(getCurrentEvent().getTimestamp().getValue(), index));
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
        long index = fCurLocation.getLocationInfo().getIndex();
        long timestamp = fCurLocation.getLocationInfo().getTimestamp();
        boolean ret = false;
        try {
            ret = super.advance();
        } catch (CTFReaderException e) {
            Activator.logError(e.getMessage(), e);
        }

        if (ret) {
            final long timestampValue = getCurrentEvent().getTimestamp().getValue();
            if (timestamp == timestampValue) {
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
        if (this.getRank() < o.getRank()) {
            return -1;
        } else if (this.getRank() > o.getRank()) {
            return 1;
        }
        return 0;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = (prime * result)
                + ((fTrace == null) ? 0 : fTrace.hashCode());
        result = (prime * result)
                + ((fCurLocation == null) ? 0 : fCurLocation.hashCode());
        result = (prime * result) + (int) (fCurRank ^ (fCurRank >>> 32));
        return result;
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
        if (fTrace == null) {
            if (other.fTrace != null) {
                return false;
            }
        } else if (!fTrace.equals(other.fTrace)) {
            return false;
        }
        if (fCurLocation == null) {
            if (other.fCurLocation != null) {
                return false;
            }
        } else if (!fCurLocation.equals(other.fCurLocation)) {
            return false;
        }
        if (fCurRank != other.fCurRank) {
            return false;
        }
        return true;
    }
}
