/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
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

import org.eclipse.linuxtools.tmf.Tracer;
import org.eclipse.linuxtools.tmf.event.TmfData;

/**
 * <b><u>TmfDataRequest</u></b>
 * <p>
 * TmfDataRequests are used to obtain blocks of contiguous data from a data
 * provider. Open ranges can be used, especially for continuous streaming.
 * <p>
 * The request is processed asynchronously by a TmfProvider and, as blocks
 * of data become available, handleData() is invoked synchronously for each
 * block. Upon return, the data instances go out of scope and become eligible
 * for gc. It is is thus the responsibility of the requester to either clone
 * or keep a reference to the data it wishes to track specifically.
 * <p>
 * This data block approach is used to avoid  busting the heap for very
 * large trace files. The block size is configurable. 
 * <p>
 * The TmfProvider indicates that the request is completed by calling done().
 * The request can be canceled at any time with cancel().
 * <p>
 * Typical usage:
 *<pre><code><i>TmfTimeWindow range = new TmfTimewindow(...);
 *TmfDataRequest&lt;DataType[]&gt; request = new TmfDataRequest&lt;DataType[]&gt;(DataType.class, 0, NB_EVENTS, BLOCK_SIZE) {
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
 * TODO: Consider decoupling from "time range", "rank", etc and for the more
 * generic notion of "criteria". This would allow to extend for "time range", etc
 * instead of providing specialized constructors. This also means removing the
 * criteria info from the data structure (with the possible exception of fNbRequested).
 * The nice thing about it is that it would prepare us well for the coming generation
 * of analysis tools.  
 * 
 * TODO: Implement request failures (codes, etc...)
 */
public abstract class TmfDataRequest<T extends TmfData> implements ITmfDataRequest<T> {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    // The default maximum number of events per chunk
    public static final int DEFAULT_BLOCK_SIZE = 1000;

    // The request count for all the events
    public static final int ALL_DATA = Integer.MAX_VALUE;
    
    private static int fRequestNumber = 0;
  
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final Class<T>      fDataType;
    private final ExecutionType fExecType;
    private final int      		fRequestId;  	// A unique request ID
    private final int      		fIndex;      	// The index (rank) of the requested event
    private final int      		fNbRequested;	// The number of requested events (ALL_DATA for all)
    private final int      		fBlockSize;     // The maximum number of events per chunk
    protected     int      		fNbRead;        // The number of reads so far

    private final   Object lock;
    private boolean fRequestRunning   = false;
    private boolean fRequestCompleted = false;
    private boolean fRequestFailed    = false;
    private boolean fRequestCanceled  = false;

    private T[] fData;	// Data object
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Resets the request counter (used for testing)
     */
    public static void reset() {
    	fRequestNumber = 0;
    }

    /**
     * Default constructor
	 *
     * @param dataType the requested data type
     */
    public TmfDataRequest(Class<T> dataType) {
        this(dataType, 0, ALL_DATA, DEFAULT_BLOCK_SIZE, ExecutionType.SHORT);
    }

    public TmfDataRequest(Class<T> dataType, ExecutionType execType) {
        this(dataType, 0, ALL_DATA, DEFAULT_BLOCK_SIZE, execType);
    }

    /**
     * @param dataType the requested data type
     * @param nbRequested the number of data items requested
     */
    public TmfDataRequest(Class<T> dataType, int index) {
        this(dataType, index, ALL_DATA, DEFAULT_BLOCK_SIZE, ExecutionType.SHORT);
    }

    public TmfDataRequest(Class<T> dataType, int index, ExecutionType execType) {
        this(dataType, index, ALL_DATA, DEFAULT_BLOCK_SIZE, execType);
    }

    /**
     * @param dataType the requested data type
     * @param index the index (rank) of the first event requested
     * @param blockSize the number of data items per block
     */
    public TmfDataRequest(Class<T> dataType, int index, int nbRequested) {
        this(dataType, index, nbRequested, DEFAULT_BLOCK_SIZE, ExecutionType.SHORT);
    }

    public TmfDataRequest(Class<T> dataType, int index, int nbRequested, ExecutionType execType) {
        this(dataType, index, nbRequested, DEFAULT_BLOCK_SIZE, execType);
    }

    /**
     * @param dataType the requested data type
     * @param index the index (rank) of the first event requested
     * @param nbRequested the number of data items requested
     * @param blockSize the number of data items per block
     */
    public TmfDataRequest(Class<T> dataType, int index, int nbRequested, int blockSize) {
        this(dataType, index, nbRequested, blockSize, ExecutionType.SHORT);
    }

    public TmfDataRequest(Class<T> dataType, int index, int nbRequested, int blockSize, ExecutionType execType) {
    	fRequestId   = fRequestNumber++;
    	fDataType    = dataType;
    	fIndex       = index;
    	fNbRequested = nbRequested;
    	fBlockSize   = blockSize;
    	fExecType    = execType;
    	fNbRead      = 0;
        lock         = new Object();
        if (Tracer.isRequestTraced()) Tracer.traceRequest(this, "created");
    }

    /**
     * Copy constructor
     */
    @SuppressWarnings("unused")
	private TmfDataRequest(TmfDataRequest<T> other) {
    	this(null, 0, ALL_DATA, DEFAULT_BLOCK_SIZE);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

	/**
	 * @return the request ID
	 */
	public int getRequestId() {
		return fRequestId;
	}

	/**
	 * @return the index of the first event requested
	 */
	public int getIndex() {
		return fIndex;
	}

	/**
	 * @return the index of the first event requested
	 */
	public ExecutionType getExecType() {
		return fExecType;
	}

    /**
     * @return the number of requested events (ALL_DATA = all)
     */
    public int getNbRequested() {
        return fNbRequested;
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
    public synchronized int getNbRead() {
        return fNbRead;
    }

    /**
     * @return indicates if the request is completed
     */
    public boolean isRunning() {
        return fRequestRunning;
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

    /**
     * @return the requested data type
     */
    public Class<T> getDataType() {
        return fDataType;
    }

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

    /** 
     * Sets the data object to specified value. To be called by the 
     * asynchronous method implementor.
     * @param data Data value to set.
     */
    public synchronized void setData(T[] data) {
    	fNbRead += data.length;
    	fData = data;
    }
    
    /**
     * Returns the data value, null if not set.
     */
    public synchronized T[] getData() {
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
    public abstract void handleData();

    public void handleStarted() {
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
            if (Tracer.isRequestTraced()) Tracer.traceRequest(this, "failed");
    		handleFailure();
    	}
    	else if (fRequestCanceled) {
            if (Tracer.isRequestTraced()) Tracer.traceRequest(this, "cancelled");
    		handleCancel();
    	}
    	else {
            if (Tracer.isRequestTraced()) Tracer.traceRequest(this, "succeeded");
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
    public void waitForCompletion() throws InterruptedException {
        synchronized (lock) {
            while (!fRequestCompleted)
            	lock.wait();
        }
    }

    /**
     * Called by the request processor upon starting to service the request.
     */
    public void start() {
        if (Tracer.isRequestTraced()) Tracer.traceRequest(this, "starting");
        synchronized(lock) {
            fRequestRunning = true;
            lock.notify();
        }
        handleStarted();
        if (Tracer.isRequestTraced()) Tracer.traceRequest(this, "started");
    }

    /**
     * Called by the request processor upon completion.
     */
    public void done() {
        if (Tracer.isRequestTraced()) Tracer.traceRequest(this, "completing");
        synchronized(lock) {
        	if (!fRequestCompleted) {
            	fRequestRunning   = false;
                fRequestCompleted = true;
            }
            handleCompleted();
            lock.notify();
        }
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

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    // All requests have a unique id
    public int hashCode() {
    	return getRequestId();
    }

    @Override
    public boolean equals(Object other) {
    	if (other instanceof TmfDataRequest<?>) {
    		TmfDataRequest<?> request = (TmfDataRequest<?>) other;
    		return 	(request.fDataType    == fDataType) &&
    				(request.fIndex       == fIndex)    &&
    				(request.fNbRequested == fNbRequested);
    	}
    	return false;
    }

    @Override
    public String toString() {
		return "[TmfDataRequest(" + fRequestId + "," + fDataType.getSimpleName() 
			+ "," + fIndex + "," + fNbRequested + "," + fBlockSize + ")]";
    }

}
