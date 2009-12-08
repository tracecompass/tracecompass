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
 * as blocks of data become available, the callback handleData() is
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
 *    public void handleData() {
 *         DataType[] data = request.getData();
 *         for (DataType e : data) {
 *             // do something
 *         }
 *    }
 *    public void handleSuccess() {
 *         // do something
 *         }
 *    }
 *    public void handleFailure() {
 *         // do something
 *         }
 *    }
 *    public void handleCancel() {
 *         // do something
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
 * 
 * TODO: Implement request failures (codes, etc...)
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

    private final int  fIndex;              // The index (order) of the requested event
    private final TmfTimeRange fRange;      // The requested events time range
    private final int  fNbRequestedEvents;  // The number of events to read (-1 == all in the range)
    private final int  fBlockSize;          // The maximum number of events per chunk
    private       int  fNbEvents;           // The number of events read so far

    private Object lock = new Object();
    private boolean fRequestCompleted = false;
    private boolean fRequestFailed    = false;
    private boolean fRequestCanceled  = false;

    private V[] fData;	// Data object
    
    // ========================================================================
    // Constructors
    // ========================================================================

    /**
     * @param index
     * @param nbEvents
     */
    public TmfDataRequest(int index, int nbEvents) {
        this(null, index, nbEvents, DEFAULT_BLOCK_SIZE);
    }

    public TmfDataRequest(int index, int nbEvents, int blockSize) {
        this(null, index, nbEvents, blockSize);
    }
    
    /**
     * @param range
     */
    public TmfDataRequest(TmfTimeRange range) {
        this(range, 0, ALL_EVENTS, DEFAULT_BLOCK_SIZE);
    }

    /**
     * @param range
     * @param nbEvents
     */
    public TmfDataRequest(TmfTimeRange range, int nbEvents) {
        this(range, 0, nbEvents, DEFAULT_BLOCK_SIZE);
    }
    /**
     * @param range
     * @param nbEvents
     * @param blockSize Size of the largest blocks expected
     */
    public TmfDataRequest(TmfTimeRange range, int nbEvents, int blockSize) {
        this(range, 0, nbEvents, blockSize);
    }

    /**
     * @param range
     * @param index
     * @param nbEvents
     * @param blockSize Size of the largest blocks expected
     */
    private TmfDataRequest(TmfTimeRange range, int index, int nbEvents, int blockSize) {
    	fIndex             = index;
    	fRange             = range;
    	fNbRequestedEvents = nbEvents;
    	fBlockSize         = blockSize;
    	fNbEvents          = 0;
    }

    // ========================================================================
    // Accessors
    // ========================================================================

	/**
	 * @return the index
	 */
	public int getIndex() {
		return fIndex;
	}

    /**
     * @return the requested time range
     */
    public TmfTimeRange getRange() {
        return fRange;
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
     * @return the number of events read so far
     */
    public int getNbEvents() {
        return fNbEvents;
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
    public boolean isFailed() {
        return fRequestFailed;
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
     * Sets the data object to specified value. To be called by the 
     * asynchronous method implementor.
     * @param data Data value to set.
     */
    public synchronized void setData(V[] data) {
    	fNbEvents += data.length;
    	fData = data;
    }
    
    /**
     * Returns the data value, null if not set.
     */
    public synchronized V[] getData() {
    	return fData;
    }
    
    /**
     * Handle a block of incoming data. This method is called every time
     * a block of data becomes available.
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
    public void handleData() {
    }

    /**
     * Handle the completion of the request. It is called when there is no more
     * data available either because:
     * - the request completed normally
     * - the request failed
     * - the request was canceled
     * 
     * As a convenience, handleXXXX methods are provided. They are meant to be
     * overridden by the application if it needs to handle these conditions. 
     */
    public void handleCompleted() {
    	if (fRequestFailed) { 
    		handleFailure();
    	}
    	else if (fRequestCanceled) {
    		handleCancel();
    	}
    	else {
    		handleSuccess();
    	}
    }

    public void handleSuccess() {
    }

    public void handleFailure() {
    }

    public void handleCancel() {
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
     * Called by the request processor upon completion.
     */
    public void done() {
        synchronized(lock) {
            fRequestCompleted = true;
            lock.notify();
        }
        handleCompleted();
    }

    /**
     * Called by the request processor upon failure.
     */
    public void fail() {
        synchronized(lock) {
            fRequestFailed = true;
            done();
        }
    }

    /**
     * Called by the request processor upon cancellation.
     */
    public void cancel() {
        synchronized(lock) {
            fRequestCanceled = true;
            done();
        }
    }

}
