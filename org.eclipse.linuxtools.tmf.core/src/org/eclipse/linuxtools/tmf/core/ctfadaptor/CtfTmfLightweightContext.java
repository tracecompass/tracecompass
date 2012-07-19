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
    // Fields
    // -------------------------------------------
    private CtfLocation curLocation;
    private long curRank;

    private final CtfTmfTrace fTrace;

    // -------------------------------------------
    // Constructor
    // -------------------------------------------
    /**
     * Deprecated, use CtfTmfLightweightContext( CtfTmfTrace please )
     *
     * @param iters
     *            the shared iterator pool.
     * @param pos
     *            the iterator position.
     */
    @Deprecated
    public CtfTmfLightweightContext(ArrayList<CtfIterator> iters,
            ListIterator<CtfIterator> pos) {
        fTrace = iters.get(0).getCtfTmfTrace();
        curLocation = new CtfLocation((Long) null);
    }

    /**
     *
     * @param ctfTmfTrace
     *            the parent trace
     * @since 1.1
     */
    public CtfTmfLightweightContext(CtfTmfTrace ctfTmfTrace) {
        fTrace = ctfTmfTrace;
        curLocation = new CtfLocation((Long) null);
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
        getIterator().seek(curLocation.getLocation());
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
        boolean retVal = getIterator().advance();
        CtfTmfEvent currentEvent = getIterator().getCurrentEvent();
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
     *
     * @param timestamp
     *            desired timestamp
     * @return success or not
     */
    public synchronized boolean seek(final long timestamp) {
        curLocation.setLocation(timestamp);
        return getIterator().seek(timestamp);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public CtfTmfLightweightContext clone() {
        CtfTmfLightweightContext ret = new CtfTmfLightweightContext(fTrace);
        ret.curLocation = curLocation.clone();
        ret.curRank = curRank;
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
