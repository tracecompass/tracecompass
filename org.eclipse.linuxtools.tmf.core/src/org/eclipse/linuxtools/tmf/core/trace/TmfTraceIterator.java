/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

import java.util.Iterator;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;

/**
 * A simple class to iterate over a TMF trace and return ITmfEvent:s. Its main
 * purpose is to encapsulate the ITmfContext.
 *
 * @author Francois Chouinard
 * @version 1.0
 * @since 2.0
 */
public class TmfTraceIterator implements Iterator<ITmfEvent> {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private ITmfTrace   fTrace;     // The trace to iterate over
    private ITmfContext fContext;   // The trace reading context
    private ITmfEvent   fNextEvent; // The buffered next event

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The standard constructor. Returns an iterator pointing to the start of
     * the trace.
     *
     * @param trace the trace to iterate over
     */
    public TmfTraceIterator(ITmfTrace trace) {
        this(trace, 0);
    }

    /**
     * The rank constructor. Returns an iterator pointing to the event
     * at the requested rank.
     *
     * @param trace the trace to iterate over
     * @param rank the starting event rank
     */
    public TmfTraceIterator(ITmfTrace trace, long rank) {
        fTrace = trace;
        fContext = fTrace.seekEvent(rank);
    }

    /**
     * The timestamp constructor. Returns an iterator pointing to the event
     * at the requested timestamp.
     *
     * @param trace the trace to iterate over
     * @param timestamp the starting event timestamp
     */
    public TmfTraceIterator(ITmfTrace trace, ITmfTimestamp timestamp) {
        fTrace = trace;
        fContext = fTrace.seekEvent(timestamp);
    }

    /**
     * The location constructor. Returns an iterator pointing to the event
     * at the requested location.
     *
     * @param trace the trace to iterate over
     * @param location the starting event location
     */
    public TmfTraceIterator(ITmfTrace trace, ITmfLocation location) {
        fTrace = trace;
        fContext = fTrace.seekEvent(location);
    }

    /**
     * The ratio constructor. Returns an iterator pointing to the event
     * at the requested ratio.
     *
     * @param trace the trace to iterate over
     * @param ratio the starting event ratio
     */
    public TmfTraceIterator(ITmfTrace trace, double ratio) {
        fTrace = trace;
        fContext = fTrace.seekEvent(ratio);
    }

    /**
     * Copy constructor
     *
     * @param other the other iterator
     */
    public TmfTraceIterator(TmfTraceIterator other) {
        fTrace = other.fTrace;
        fContext = other.fContext.clone();
    }

    // ------------------------------------------------------------------------
    // Iterator
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
        if (fNextEvent == null) {
            fNextEvent = fTrace.getNext(fContext);
        }
        return fNextEvent != null;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    @Override
    public ITmfEvent next() {
        ITmfEvent event;
        if (fNextEvent != null) {
            event = fNextEvent;
            fNextEvent = null;
        } else {
            event = fTrace.getNext(fContext);
        }
        return event;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
