/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader;
import org.eclipse.linuxtools.ctf.core.trace.StreamInputReader;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;

/**
 * The ctfIterator is the class that will act like a reader for the trace
 * it does not have a file handle, so many iterators can be used without worries
 * of io errors.
 */
public class CtfIterator extends CTFTraceReader implements ITmfContext, Comparable<CtfIterator>, Cloneable {

    private final CtfTmfTrace ctfTmfTrace;

    final public static CtfLocation NULL_LOCATION = new CtfLocation(
            CtfLocation.INVALID_LOCATION);
    private CtfLocation curLocation;
    private long curRank;

    /**
     * Create a new CTF trace iterator, which initially points at the first
     * event in the trace.
     *
     * @param trace the trace to iterate over
     */
    public CtfIterator(final CtfTmfTrace trace) {
        super(trace.getCTFTrace());
        this.ctfTmfTrace = trace;
        if (this.hasMoreEvents()) {

            this.curLocation = new CtfLocation(trace.getStartTime());
            this.curRank = 0;
        } else {
            setUnknownLocation();
        }
    }

    /**
     *
     */
    private void setUnknownLocation() {
        this.curLocation = NULL_LOCATION;
        this.curRank = UNKNOWN_RANK;
    }

    /**
     * Constructor for CtfIterator.
     * @param trace CtfTmfTrace the trace
     * @param timestampValue long the timestamp in ns of the trace for positioning
     * @param rank long the index of the trace for positioning
     */
    public CtfIterator(final CtfTmfTrace trace, final long timestampValue,
            final long rank) {
        super(trace.getCTFTrace());

        this.ctfTmfTrace = trace;
        if (this.hasMoreEvents()) {
            this.curLocation = (new CtfLocation(this.getCurrentEvent()
                    .getTimestampValue()));
            if (this.getCurrentEvent().getTimestampValue() != timestampValue) {
                this.seek(timestampValue);
                this.curRank = rank;
            }
        } else {
            setUnknownLocation();
        }

    }

    /**
     * Method getCtfTmfTrace. gets a CtfTmfTrace
     * @return CtfTmfTrace
     */
    public CtfTmfTrace getCtfTmfTrace() {
        return ctfTmfTrace;
    }

    /**
     * Method getCurrentEvent. gets the current event
     * @return CtfTmfEvent
     */
    public CtfTmfEvent getCurrentEvent() {
        final StreamInputReader top = super.prio.peek();
        if (top != null) {
            return new CtfTmfEvent(top.getCurrentEvent(), top.getFilename(),
                    ctfTmfTrace);
        }
        return null;
    }

    /**
     * Method seek. Seeks to a given timestamp
     * @param timestamp long the timestamp in ns (utc)
     * @return boolean
     */
    @Override
    public boolean seek(final long timestamp) {
        boolean ret = false;
        final long offsetTimestamp = timestamp
                - this.getTrace().getOffset();
        if (offsetTimestamp < 0) {
            ret = super.seek(timestamp);
        } else {
            ret = super.seek(offsetTimestamp);
        }

        if (ret) {
            curLocation.setLocation(getCurrentEvent().getTimestampValue());
        } else {
            curLocation = NULL_LOCATION;
        }
        return ret;
    }

    /**
     * Method getRank.
     * @return long
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfContext#getRank()
     */
    @Override
    public long getRank() {
        return curRank;
    }

    /**
     * Method setRank.
     * @param rank long
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfContext#setRank(long)
     */
    @Override
    public void setRank(final long rank) {
        curRank = rank;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.linuxtools.tmf.core.trace.TmfContext#clone()
     */
    @Override
    public CtfIterator clone() {
        CtfIterator clone = null;
        clone = new CtfIterator(ctfTmfTrace, this.getCurrentEvent().getTimestampValue(), curRank);
        return clone;
    }

    /**
     * Method dispose.
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfContext#dispose()
     */
    @Override
    public void dispose() {
        // FIXME add dispose() stuff to CTFTrace and call it here...

    }

    /**
     * Method setLocation.
     * @param location ITmfLocation<?>
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfContext#setLocation(ITmfLocation<?>)
     */
    @Override
    public void setLocation(final ITmfLocation<?> location) {
        // FIXME alex: isn't there a cleaner way than a cast here?
        this.curLocation = (CtfLocation) location;
        seek(((CtfLocation) location).getLocation());
    }

    /**
     * Method getLocation.
     * @return CtfLocation
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfContext#getLocation()
     */
    @Override
    public CtfLocation getLocation() {
        return curLocation;
    }

    /**
     * Method increaseRank.
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfContext#increaseRank()
     */
    @Override
    public void increaseRank() {
        /* Only increase the rank if it's valid */
        if(hasValidRank()) {
            curRank++;
        }
    }

    /**
     * Method hasValidRank, if the iterator is valid
     * @return boolean
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfContext#hasValidRank()
     */
    @Override
    public boolean hasValidRank() {
        return (getRank() >= 0);
    }

    /**
     * Method advance go to the next event
     * @return boolean successful or not
     */
    @Override
    public boolean advance() {
        boolean ret = super.advance();
        if (ret) {
            curLocation.setLocation(getCurrentEvent().getTimestampValue());
        } else {
            curLocation = NULL_LOCATION;
        }
        return ret;
    }

    /**
     * Method compareTo.
     * @param o CtfIterator
     * @return int -1, 0, 1
     */
    @Override
    public int compareTo(final CtfIterator o) {
        if (this.getRank() < o.getRank()) {
            return -1;
        } else if (this.getRank() > o.getRank()) {
            return 1;
        }
        return 0;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = (prime * result)
                + ((ctfTmfTrace == null) ? 0 : ctfTmfTrace.hashCode());
        result = (prime * result)
                + ((curLocation == null) ? 0 : curLocation.hashCode());
        result = (prime * result) + (int) (curRank ^ (curRank >>> 32));
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
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof CtfIterator)) {
            return false;
        }
        CtfIterator other = (CtfIterator) obj;
        if (ctfTmfTrace == null) {
            if (other.ctfTmfTrace != null) {
                return false;
            }
        } else if (!ctfTmfTrace.equals(other.ctfTmfTrace)) {
            return false;
        }
        if (curLocation == null) {
            if (other.curLocation != null) {
                return false;
            }
        } else if (!curLocation.equals(other.curLocation)) {
            return false;
        }
        if (curRank != other.curRank) {
            return false;
        }
        return true;
    }
}
