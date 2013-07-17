/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
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
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;

/**
 * The CTF trace reader iterator.
 *
 * It doesn't reserve a file handle, so many iterators can be used without worries
 * of I/O errors or resource exhaustion.
 *
 * @version 1.0
 * @author Matthew Khouzam
 */
public class CtfIterator extends CTFTraceReader implements ITmfContext,
        Comparable<CtfIterator> {

    private final CtfTmfTrace ctfTmfTrace;

    /**
     * An invalid location
     */
    final public static CtfLocation NULL_LOCATION = new CtfLocation(CtfLocation.INVALID_LOCATION);

    private CtfLocation curLocation;
    private long curRank;

    /**
     * Create a new CTF trace iterator, which initially points at the first
     * event in the trace.
     *
     * @param trace
     *            the trace to iterate over
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

    private void setUnknownLocation() {
        this.curLocation = NULL_LOCATION;
        this.curRank = UNKNOWN_RANK;
    }

    /**
     * Constructor for CtfIterator.
     *
     * @param trace
     *            CtfTmfTrace the trace
     * @param ctfLocationData
     *            long the timestamp in ns of the trace for positioning
     * @param rank
     *            long the index of the trace for positioning
     * @since 2.0
     */
    public CtfIterator(final CtfTmfTrace trace,
            final CtfLocationInfo ctfLocationData, final long rank) {
        super(trace.getCTFTrace());

        this.ctfTmfTrace = trace;
        if (this.hasMoreEvents()) {
            this.curLocation = new CtfLocation(ctfLocationData);
            if (this.getCurrentEvent().getTimestamp().getValue() != ctfLocationData.getTimestamp()) {
                this.seek(ctfLocationData);
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
        final StreamInputReader top = super.getPrio().peek();
        if (top != null) {
            return CtfTmfEventFactory.createEvent(top.getCurrentEvent(),
                    top.getFilename(), ctfTmfTrace);
        }
        return null;
    }

    @Override
    public boolean seek(long timestamp) {
        return seek(new CtfLocationInfo(timestamp, 0));
    }

    /**
     * Seek this iterator to a given location.
     *
     * @param ctfLocationData
     *            The LocationData representing the position to seek to
     * @return boolean
     * @since 2.0
     */
    public synchronized boolean seek(final CtfLocationInfo ctfLocationData) {
        boolean ret = false;

        /* Adjust the timestamp depending on the trace's offset */
        long currTimestamp = ctfLocationData.getTimestamp();
        final long offsetTimestamp = this.getCtfTmfTrace().getCTFTrace().timestampNanoToCycles(currTimestamp);
        if (offsetTimestamp < 0) {
            ret = super.seek(0L);
        } else {
            ret = super.seek(offsetTimestamp);
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
            ret= false;
        }
        /* Seek the current location accordingly */
        if (ret) {
            curLocation = new CtfLocation(new CtfLocationInfo(getCurrentEvent().getTimestamp().getValue(), index));
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

    @Override
    public CtfIterator clone() {
        CtfIterator clone = null;
        clone = new CtfIterator(ctfTmfTrace, this.getLocation().getLocationInfo(), curRank);
        return clone;
    }

    /**
     * Method dispose.
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfContext#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
    }

    /**
     * Method setLocation.
     * @param location ITmfLocation<?>
     * @since 3.0
     */
    @Override
    public void setLocation(final ITmfLocation location) {
        // FIXME alex: isn't there a cleaner way than a cast here?
        this.curLocation = (CtfLocation) location;
        seek(((CtfLocation) location).getLocationInfo());
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
    public synchronized boolean advance() {
        long index = curLocation.getLocationInfo().getIndex();
        long timestamp = curLocation.getLocationInfo().getTimestamp();
        boolean ret = super.advance();

        if (ret) {
            final long timestampValue = getCurrentEvent().getTimestamp().getValue();
            if (timestamp == timestampValue) {
                curLocation = new CtfLocation(timestampValue, index + 1);
            } else {
                curLocation = new CtfLocation(timestampValue, 0L);
            }
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
