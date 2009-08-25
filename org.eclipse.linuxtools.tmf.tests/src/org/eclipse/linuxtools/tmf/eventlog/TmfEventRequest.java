/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard (fchouinard@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.eventlog;

import java.util.Vector;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeWindow;

/**
 * <b><u>TmfEventRequest</u></b>
 * <p>
 * TmfEventRequests are used to obtain blocks of contiguous events from an
 * event stream, either all the events within a given time window or n events
 * starting a a specific timestamp. Open ranges can be used, especially for
 * continuous streaming.
 * <p>
 * The request is processed asynchronously by an ITmfRequestProcessor and,
 * as blocks of events become available, the callback function newEvents()
 * is invoked, synchronously, for each block. When returning from newEvents(),
 * the event instances go out of scope and become eligible for gc. It is
 * is thus the responsibility of the requester to either copy or keep a
 * reference to the events it wishes to track specifically.
 * <p>
 * This event block approach is necessary to avoid  busting the heap for very
 * large trace files. The block size is configurable.
 * <p>
 * The ITmfRequestProcessor indicates that the request is completed by
 * calling done(). The request can be canceled at any time with cancel().
 * <p>
 * Typical usage:
 *<pre><code><i>TmfTimeWindow range = new TmfTimewindow(...);
 *TmfEventRequest request = new TmfEventRequest(range, 0, NB_EVENTS, BLOCK_SIZE) {
 *    &#64;Override
 *    public void newEvents(Vector&lt;TmfEvent&gt; events) {
 *         for (TmfEvent e : events) {
 *             // do something
 *         }
 *    }
 *};
 *fProcessor.process(request, true);
 *</i></code></pre>
 *
 * TODO: Consider extending DataRequestMonitor from DSF concurrency plugin.
 * The main issue is the slicing of the result in blocks and continuous
 * streams.
 */
public class TmfEventRequest {

    // ========================================================================
    // Constants
    // ========================================================================

    // The default maximum number of events per chunk
    public static final int DEFAULT_BLOCK_SIZE = 1000;

    // ========================================================================
    // Attributes
    // ========================================================================

    private final TmfTimeWindow fRange;     // The requested events timestamp range
    private final long fOffset;             // The synchronization offset to apply
    private final int  fNbRequestedEvents;  // The number of events to read (-1 == the whole range)
    private final int  fBlockSize;          // The maximum number of events per chunk

    private Object lock = new Object();
    private boolean fRequestCompleted = false;
    private boolean fRequestCanceled  = false;

    // ========================================================================
    // Constructors
    // ========================================================================

    /**
     * @param range
     * @param offset
     * @param nbEvents
     */
    public TmfEventRequest(TmfTimeWindow range, long offset, int nbEvents) {
        this(range, offset, nbEvents, DEFAULT_BLOCK_SIZE);
    }

    /**
     * @param range
     * @param offset
     * @param nbEvents
     * @param maxBlockSize Size of the largest blocks expected
     */
    public TmfEventRequest(TmfTimeWindow range, long offset, int nbEvents, int maxBlockSize) {
        fRange = range;
        fOffset = offset;
        fNbRequestedEvents = nbEvents;
        fBlockSize = maxBlockSize;
    }

    // ========================================================================
    // Accessors
    // ========================================================================

    /**
     * @return the requested time range
     */
    public TmfTimeWindow getRange() {
        return fRange;
    }

    /**
     * @return the offset
     */
    public long getOffset() {
        return fOffset;
    }

    /**
     * @return the number of requested events (-1 = all)
     */
    public int getNbRequestedEvents() {
        return fNbRequestedEvents;
    }

    /**
     * @return the block size
     */
    public int getBlockize() {
        return fBlockSize;
    }

    /**
     * @return indicates if the request is completed
     */
    public boolean isCompleted() {
        return fRequestCompleted;
    }

    /**
     * @return indicates if the request is canceled
     */
    public boolean isCancelled() {
        return fRequestCanceled;
    }

    // ========================================================================
    // Operators
    // ========================================================================

    /**
     * newEvents()
     * 
     * - Events are received in the order they appear in the stream.
     * - Called by the request processor, in its execution thread, every time a
     *   block of events becomes available.
     * - Request processor performs a synchronous call to newEvents()
     *   i.e. its execution threads holds until newEvents() returns.
     * - Original events are disposed of on return i.e. keep a reference (or a 
     *   copy) if some persistence is needed between invocations.
     * - When there are no more events, 
     *
     * @param events - an array of events
     */
    public void newEvents(Vector<TmfEvent> events) {
    }

    /**
     * To suspend the client thread until the request completes (or is
     * canceled).
     * 
     * @throws InterruptedException 
     */
    public void waitForCompletion() {
        synchronized (lock) {
            while (!fRequestCompleted)
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
    }

    /**
     * Complete the request. Called by the request processor upon completion.
     */
    public void done() {
        synchronized(lock) {
            fRequestCompleted = true;
            lock.notify();
        }
    }

    /**
     * Cancel the request.
     */
    public void cancel() {
        synchronized(lock) {
            fRequestCanceled = true;
            fRequestCompleted = true;
            lock.notify();
        }
    }

}
