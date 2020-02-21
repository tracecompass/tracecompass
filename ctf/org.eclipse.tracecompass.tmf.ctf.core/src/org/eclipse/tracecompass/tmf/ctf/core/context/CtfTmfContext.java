/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Simon Delisle - Remove the iterator in dispose()
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.context;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.ctf.core.trace.iterator.CtfIterator;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;

/**
 * Lightweight Context for CtfTmf traces. Should only use 3 references, 1 ref to
 * a boxed Long, a long and an int.
 *
 * @author Matthew Khouzam
 */
public class CtfTmfContext implements ITmfContext {

    // -------------------------------------------
    // Fields
    // -------------------------------------------

    private CtfLocation fCurLocation;
    private long fCurRank;

    private final CtfTmfTrace fTrace;

    // -------------------------------------------
    // Constructor
    // -------------------------------------------

    /**
     * Constructor
     *
     * @param ctfTmfTrace
     *            the parent trace
     */
    public CtfTmfContext(CtfTmfTrace ctfTmfTrace) {
        fTrace = ctfTmfTrace;
        fCurLocation = new CtfLocation(new CtfLocationInfo(0, 0));
    }

    // -------------------------------------------
    // TmfContext Overrides
    // -------------------------------------------

    @Override
    public long getRank() {
        return fCurRank;
    }

    @Override
    public synchronized ITmfLocation getLocation() {
        return fCurLocation;
    }

    @Override
    public boolean hasValidRank() {
        return fCurRank != CtfLocation.INVALID_LOCATION.getTimestamp();
    }

    @Override
    public synchronized void setLocation(ITmfLocation location) {
        if (location instanceof CtfLocation) {
            CtfLocation ctfLocation = (CtfLocation) location;
            if (location.getLocationInfo().equals(CtfLocation.INVALID_LOCATION)) {
                fCurLocation = ctfLocation;
            } else {
                CtfIterator iterator = getIterator();
                if(iterator == null) {
                    return;
                }
                iterator.seek(ctfLocation.getLocationInfo());
                fCurLocation = iterator.getLocation();
            }
        } else {
            fCurLocation = null;
        }
    }

    @Override
    public void setRank(long rank) {
        fCurRank = rank;

    }

    @Override
    public void increaseRank() {
        if (hasValidRank()) {
            fCurRank++;
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
        CtfIterator iterator = getIterator();
        return iterator == null ? null : iterator.getCurrentEvent();
    }

    /**
     * Advances to a the next event. Wrapper to help CtfTmfTrace
     *
     * @return success or not
     */
    public synchronized boolean advance() {
        final CtfLocationInfo curLocationData = fCurLocation.getLocationInfo();
        CtfIterator iterator = getIterator();
        if( iterator == null) {
            return false;
        }
        boolean retVal = iterator.advance();
        CtfTmfEvent currentEvent = iterator.getCurrentEvent();

        if (currentEvent != null) {
            final long timestampValue = iterator.getCurrentTimestamp();
            if (curLocationData.getTimestamp() == timestampValue) {
                fCurLocation = new CtfLocation(timestampValue, curLocationData.getIndex() + 1);
            } else {
                fCurLocation = new CtfLocation(timestampValue, 0L);
            }
        } else {
            fCurLocation = new CtfLocation(CtfLocation.INVALID_LOCATION);
        }

        return retVal;
    }

    @Override
    public void dispose() {
        fTrace.disposeContext(this);
    }

    /**
     * Seeks to a given timestamp. Wrapper to help CtfTmfTrace
     *
     * @param timestamp
     *            desired timestamp
     * @return success or not
     */
    public synchronized boolean seek(final long timestamp) {
        CtfIterator iterator = getIterator();
        if( iterator == null) {
            return false;
        }
        boolean ret = iterator.seek(timestamp);
        fCurLocation = iterator.getLocation();
        return ret;
    }

    /**
     * Seeks to a given location. Wrapper to help CtfTmfTrace
     * @param location
     *              unique location to find the event.
     *
     * @return success or not
     */
    public synchronized boolean seek(final CtfLocationInfo location) {
        fCurLocation = new CtfLocation(location);
        CtfIterator iterator = getIterator();
        return iterator == null ? false : iterator.seek(location);
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
    private @Nullable CtfIterator getIterator() {
        return (CtfIterator) fTrace.createIteratorFromContext(this);
    }
}
