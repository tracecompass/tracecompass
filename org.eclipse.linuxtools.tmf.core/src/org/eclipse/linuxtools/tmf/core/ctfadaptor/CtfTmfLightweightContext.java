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

import java.util.ArrayList;
import java.util.ListIterator;

import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;

/**
 * Lightweight Context for CtfTmf traces. Should only use 3 references, 1 ref to
 * a boxed Long, a long and an int.
 *
 * @versionj 1.0
 * @author Matthew Khouzam
 */
public class CtfTmfLightweightContext implements ITmfContext {

    // -------------------------------------------
    // Constants
    // -------------------------------------------
    private static final int MAX_COLLISIONS = 10;

    // -------------------------------------------
    // Fields
    // -------------------------------------------
    private CtfLocation curLocation;
    private long curRank;
    private int collisions;

    private CtfIterator fSeeker;
    final private ArrayList<CtfIterator> fIteratorPool;
    private ListIterator<CtfIterator> fCurrentIterator;

    // -------------------------------------------
    // Constructor
    // -------------------------------------------
    /**
     *
     * @param iters
     *            the shared iterator pool.
     * @param pos
     *            the iterator position.
     */
    public CtfTmfLightweightContext(ArrayList<CtfIterator> iters,
            ListIterator<CtfIterator> pos) {
        fIteratorPool = iters;
        fCurrentIterator = pos;
        fSeeker = getIterator();
        curLocation = new CtfLocation((Long)null);
        collisions = 0;
    }

    // -------------------------------------------
    // TmfContext Overrides
    // -------------------------------------------

    @Override
    public long getRank() {
        return curRank;
    }

    @Override
    public ITmfLocation<? extends Comparable<?>> getLocation() {
        return curLocation;
    }

    @Override
    public boolean hasValidRank() {
        return curRank != CtfLocation.INVALID_LOCATION;
    }

    @Override
    public void setLocation(ITmfLocation<? extends Comparable<?>> location) {
        curLocation = (CtfLocation) location;
        updateLocation();
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
     * Gets the current event. Wrapper to help CtfTmfTrace
     * @return The event or null
     */
    public synchronized CtfTmfEvent getCurrentEvent() {
        updateLocation();
        return fSeeker.getCurrentEvent();
    }

    /**
     * Advances to a the next event. Wrapper to help CtfTmfTrace
     * @return success or not
     */
    public synchronized boolean advance() {
        updateLocation();
        boolean retVal = fSeeker.advance();
        CtfTmfEvent currentEvent = fSeeker.getCurrentEvent();
        if (currentEvent != null) {
            curLocation.setLocation(currentEvent.getTimestampValue());
        } else {
            curLocation.setLocation(CtfLocation.INVALID_LOCATION);
        }

        return retVal;
    }

    @Override
    public void dispose() {
        // do nothing
    }

    /**
     * Seeks to a given timestamp. Wrapper to help CtfTmfTrace
     * @param timestamp desired timestamp
     * @return success or not
     */
    public synchronized boolean seek(final long timestamp) {
        curLocation.setLocation(timestamp);
        collisions = 0;
        fSeeker = getIterator();
        return updateLocation();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public CtfTmfLightweightContext clone() {
        CtfTmfLightweightContext ret = new CtfTmfLightweightContext(
                fIteratorPool, fCurrentIterator);
        ret.curLocation = curLocation.clone();
        ret.curRank = curRank;
        return ret;
    }

    // -------------------------------------------
    // Private helpers
    // -------------------------------------------
    /**
     * This updates the position of an iterator to the location(curLocation)
     * Since the iterators are in a pool to not exhaust the number of file
     * pointers some of them can be shared. This means there can be collisions
     * between contexts fighting over the same resource. A heuristic is applied
     * that if there are MAX_COLLISIONS collisions in a row, the iterator is
     * changed for the next one in the iterator pool.
     *
     * @return true if the location is correct.
     */
    private synchronized boolean updateLocation() {
        if (!curLocation.getLocation().equals(
                (fSeeker.getLocation().getLocation()))) {
            collisions++;
            if (collisions > MAX_COLLISIONS) {
                fSeeker = getIterator();
                collisions = 0;
            }
            fSeeker.setRank(curRank);
            return fSeeker.seek(curLocation.getLocation());
        }
        collisions = 0;
        return true;
    }

    /**
     * gets the next iterator in a pool.
     *
     * @return
     */
    private CtfIterator getIterator() {
        if (!fCurrentIterator.hasNext()) {
            fCurrentIterator = fIteratorPool.listIterator(0);
        }
        return fCurrentIterator.next();
    }

}
