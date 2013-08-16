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
 *   Simon Delisle - Remove the iterator in dispose()
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;

/**
 * Lightweight Context for CtfTmf traces. Should only use 3 references, 1 ref to
 * a boxed Long, a long and an int.
 *
 * @author Matthew Khouzam
 * @version 1.0
 * @since 2.0
 */
public class CtfTmfContext implements ITmfContext {

    // -------------------------------------------
    // Fields
    // -------------------------------------------

    private CtfLocation curLocation;
    private long curRank;

    private final CtfTmfTrace fTrace;

    // -------------------------------------------
    // Constructor
    // -------------------------------------------

    /**
     * Constructor
     *
     * @param ctfTmfTrace
     *            the parent trace
     * @since 1.1
     */
    public CtfTmfContext(CtfTmfTrace ctfTmfTrace) {
        fTrace = ctfTmfTrace;
        curLocation = new CtfLocation(new CtfLocationInfo(0, 0));
    }

    // -------------------------------------------
    // TmfContext Overrides
    // -------------------------------------------

    @Override
    public long getRank() {
        return curRank;
    }

    /**
     * @since 3.0
     */
    @Override
    public ITmfLocation getLocation() {
        return curLocation;
    }

    @Override
    public boolean hasValidRank() {
        return curRank != CtfLocation.INVALID_LOCATION.getTimestamp();
    }

    /**
     * @since 3.0
     */
    @Override
    public void setLocation(ITmfLocation location) {
        curLocation = (CtfLocation) location;
        if (curLocation != null) {
            getIterator().seek(curLocation.getLocationInfo());
        }
    }

    @Override
    public void setRank(long rank) {
        curRank = rank;

    }

    @Override
    public void increaseRank() {
        if (hasValidRank()) {
            curRank++;
        }
    }

    // -------------------------------------------
    // CtfTmfTrace Helpers
    // -------------------------------------------

    /**
     * Gets the trace of this context.
     *
     * @return The trace of this context
     */
    public CtfTmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Gets the current event. Wrapper to help CtfTmfTrace
     *
     * @return The event or null
     */
    public synchronized CtfTmfEvent getCurrentEvent() {
        return getIterator().getCurrentEvent();
    }

    /**
     * Advances to a the next event. Wrapper to help CtfTmfTrace
     *
     * @return success or not
     */
    public synchronized boolean advance() {
        final CtfLocationInfo curLocationData = this.curLocation.getLocationInfo();
        boolean retVal = getIterator().advance();
        CtfTmfEvent currentEvent = getIterator().getCurrentEvent();

        if (currentEvent != null) {
            final long timestampValue = currentEvent.getTimestamp().getValue();
            if (curLocationData.getTimestamp() == timestampValue) {
                curLocation = new CtfLocation(timestampValue, curLocationData.getIndex() + 1);
            } else {
                curLocation = new CtfLocation(timestampValue, 0L);
            }
        } else {
            curLocation = new CtfLocation(CtfLocation.INVALID_LOCATION);
        }

        return retVal;
    }

    @Override
    public void dispose() {
        CtfIteratorManager.removeIterator(fTrace, this);
    }

    /**
     * Seeks to a given timestamp. Wrapper to help CtfTmfTrace
     *
     * @param timestamp
     *            desired timestamp
     * @return success or not
     */
    public synchronized boolean seek(final long timestamp) {
        curLocation = new CtfLocation(timestamp, 0);
        return getIterator().seek(timestamp);
    }

    /**
     * Seeks to a given location. Wrapper to help CtfTmfTrace
     * @param location
     *              unique location to find the event.
     *
     * @return success or not
     * @since 2.0
     */
    public synchronized boolean seek(final CtfLocationInfo location) {
        curLocation = new CtfLocation(location);
        return getIterator().seek(location);
    }

    @Override
    public CtfTmfContext clone() {
        CtfTmfContext ret = null;
        try {
            ret = (CtfTmfContext) super.clone();
            /* Fields are immutable, no need to deep-copy them */
        } catch (CloneNotSupportedException e) {
            /* Should not happen, we're calling Object.clone() */
        }
        return ret;
    }

    // -------------------------------------------
    // Private helpers
    // -------------------------------------------

    /**
     * Get iterator, called every time to get an iterator, no local copy is
     * stored so that there is no need to "update"
     *
     * @return an iterator
     */
    private CtfIterator getIterator() {
        return CtfIteratorManager.getIterator(fTrace, this);
    }
}
