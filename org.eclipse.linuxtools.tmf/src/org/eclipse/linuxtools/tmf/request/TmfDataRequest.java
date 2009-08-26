/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.request;

import org.eclipse.linuxtools.tmf.event.TmfTimeRange;

/**
 * <b><u>TmfDataRequest</u></b>
 * <p>
 * TmfDataRequests are used to obtain blocks of contiguous data from a data
 * provider, either all the data within a given time window or n elements
 * starting at a specific timestamp. Open ranges can be used, especially for
 * continuous streaming.
 * <p>
 * The request is processed asynchronously by an ITmfRequestProcessor and,
 * as blocks of data become available, the callback handlePartialData() is
 * invoked, synchronously, for each block. When returning from the callback,
 * the data instances go out of scope and become eligible for gc. It is
 * is thus the responsibility of the requester to either copy or keep a
 * reference to the data it wishes to track specifically.
 * <p>
 * This data block approach is necessary to avoid  busting the heap for very
 * large trace files. The block size is configurable.
 * <p>
 * The ITmfRequestProcessor indicates that the request is completed by
 * calling done(). The request can be canceled at any time with cancel().
 * <p>
 * Typical usage:
 *<pre><code><i>TmfTimeWindow range = new TmfTimewindow(...);
 *TmfDataRequest&lt;DataType[]&gt; request = new TmfDataRequest&lt;DataType[]&gt;(range, 0, NB_EVENTS, BLOCK_SIZE) {
 *    &#64;Override
 *    public void handlePartialResult() {
 *         DataType[] data = request.getData();
 *         for (DataType e : data) {
 *             // do something
 *         }
 *    }
 *};
 *fProcessor.process(request, true);
 *</i></code></pre>
 *
 * TODO: Consider extending DataRequestMonitor from DSF concurrency plugin.
 * The main issue is the slicing of the result in blocks and continuous
 * streams. This would require using a thread executor and to carefully
 * look at setData() and getData().
 */
public class TmfDataRequest<V> {

    // ========================================================================
    // Constants
    // ========================================================================

    // The default maximum number of events per chunk
    public static final int DEFAULT_BLOCK_SIZE = 1000;

    // The request count for all the events
    public static final int ALL_EVENTS = -1;
    
    // ========================================================================
    // Attributes
    // ========================================================================

    private final TmfTimeRange fRange;     // The requested events timestamp range
    private final int  fIndex;              // The event index to get
    private final long fOffset;             // The synchronization offset to apply
    private final int  fNbRequestedItems;   // The number of items to read (-1 == the whole range)
    private final int  fBlockSize;          // The maximum number of events per chunk

    private Object lock = new Object();
    private boolean fRequestCompleted = false;
    private boolean fRequestCanceled  = false;

    private V[] fData;	// Data object
    
    // ========================================================================
    // Constructors
    // ========================================================================

    /**
     * @param index
     * @param nbEvents
     */
    public TmfDataRequest(int index, long offset, int nbEvents) {
        this(null, index, offset, nbEvents, DEFAULT_BLOCK_SIZE);
    }

    /**
     * @param range
     * @param offset
     * @param nbEvents
     */
    public TmfDataRequest(TmfTimeRange range, long offset, int nbEvents) {
        this(range, 0, offset, nbEvents, DEFAULT_BLOCK_SIZE);
    }

    /**
     * @param range
     * @param offset
     * @param nbItems
     * @param maxBlockSize Size of the largest blocks expected
     */
    public TmfDataRequest(TmfTimeRange range, long offset, int nbEvents, int maxBlockSize) {
        this(range, 0, offset, nbEvents, maxBlockSize);
    }

    /**
     * @param range
     * @param index
     * @param offset
     * @param nbItems
     * @param maxBlockSize Size of the largest blocks expected
     */
    public TmfDataRequest(TmfTimeRange range, int index, long offset, int nbEvents, int maxBlockSize) {
        fRange = range;
        fIndex = index;
        fOffset = offset;
        fNbRequestedItems = nbEvents;
        fBlockSize = maxBlockSize;
    }

    // ========================================================================
    // Accessors
    // ========================================================================

    /**
     * @return the requested time range
     */
    public TmfTimeRange getRange() {
        return fRange;
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return fIndex;
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
    public int getNbRequestedItems() {
        return fNbRequestedItems;
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
     * Sets the data object to specified value.  To be called by the 
     * asynchronous method implementor.
     * @param data Data value to set.
     */
    public synchronized void setData(V[] data) {
    	fData = data;
    }
    
    /**
     * Returns the data value, null if not set.
     */
    public synchronized V[] getData() {
    	return fData;
    }
    
    /**
     * handlePartialResult()
     * 
     * - Data items are received in the order they appear in the stream.
     * - Called by the request processor, in its execution thread, every time a
     *   block of data becomes available.
     * - Request processor performs a synchronous call to handlePartialResult()
     *   i.e. its execution threads holds until handlePartialData() returns.
     * - Original data items are disposed of on return i.e. keep a reference
     *   (or a copy) if some persistence is needed between invocations.
     * - When there is no more data, done() is called. 
     *
     * @param events - an array of events
     */
    public void handlePartialResult() {
    }

    public void handleCompleted() {
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
        handleCompleted();
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
