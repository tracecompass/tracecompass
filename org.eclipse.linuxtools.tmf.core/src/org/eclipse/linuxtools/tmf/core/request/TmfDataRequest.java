/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.request;

import java.util.concurrent.CountDownLatch;

import org.eclipse.linuxtools.internal.tmf.core.TmfCoreTracer;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;

/**
 * TmfDataRequests are used to obtain blocks of contiguous data from a data provider. Open ranges can be used,
 * especially for continuous streaming.
 * <p>
 * The request is processed asynchronously by a TmfProvider and, as blocks of data become available, handleData() is
 * invoked synchronously for each block. Upon return, the data instances go out of scope and become eligible for gc. It
 * is is thus the responsibility of the requester to either clone or keep a reference to the data it wishes to track
 * specifically.
 * <p>
 * This data block approach is used to avoid busting the heap for very large trace files. The block size is
 * configurable.
 * <p>
 * The TmfProvider indicates that the request is completed by calling done(). The request can be canceled at any time
 * with cancel().
 * <p>
 * Typical usage:
 *
 * <pre>
 * <code><i>TmfTimeWindow range = new TmfTimewindow(...);
 * TmfDataRequest&lt;DataType[]&gt; request = new TmfDataRequest&lt;DataType[]&gt;(DataType.class, 0, NB_EVENTS, BLOCK_SIZE) {
 *     public void handleData() {
 *          DataType[] data = request.getData();
 *          for (DataType e : data) {
 *              // do something
 *          }
 *     }
 *     public void handleSuccess() {
 *          // do something
 *          }
 *     }
 *     public void handleFailure() {
 *          // do something
 *          }
 *     }
 *     public void handleCancel() {
 *          // do something
 *          }
 *     }
 * };
 * fProcessor.process(request, true);
 * </i></code>
 * </pre>
 *
 * TODO: Consider decoupling from "time range", "rank", etc and for the more generic notion of "criteria". This would
 * allow to extend for "time range", etc instead of providing specialized constructors. This also means removing the
 * criteria info from the data structure (with the possible exception of fNbRequested). The nice thing about it is that
 * it would prepare us well for the coming generation of analysis tools.
 *
 * TODO: Implement request failures (codes, etc...)
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public abstract class TmfDataRequest implements ITmfDataRequest {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /** The default maximum number of events per chunk */
    public static final int DEFAULT_BLOCK_SIZE = 1000;

    /** The request count for all the events */
    public static final int ALL_DATA = Integer.MAX_VALUE;

    private static int fRequestNumber = 0;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final Class<? extends ITmfEvent> fDataType;
    private final ExecutionType fExecType;

    /** A unique request ID */
    private final int fRequestId;

    /** The index (rank) of the requested event */
    protected long fIndex;

    /** The number of requested events (ALL_DATA for all) */
    protected int fNbRequested;

    /** The block size (for BG requests) */
    private final int fBlockSize;

    /** The number of reads so far */
    private int fNbRead;

    private final CountDownLatch startedLatch = new CountDownLatch(1);
    private final CountDownLatch completedLatch = new CountDownLatch(1);
    private boolean fRequestRunning;
    private boolean fRequestCompleted;
    private boolean fRequestFailed;
    private boolean fRequestCanceled;

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
     * Request all the events of a given type (high priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     *
     * @param dataType the requested data type
     */
    public TmfDataRequest(Class<? extends ITmfEvent> dataType) {
        this(dataType, 0, ALL_DATA, DEFAULT_BLOCK_SIZE, ExecutionType.FOREGROUND);
    }

    /**
     * Request all the events of a given type (given priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     *
     * @param dataType the requested data type
     * @param priority the requested execution priority
     */
    public TmfDataRequest(Class<? extends ITmfEvent> dataType, ExecutionType priority) {
        this(dataType, 0, ALL_DATA, DEFAULT_BLOCK_SIZE, priority);
    }

    /**
     * Request all the events of a given type from the given index (high priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     *
     * @param dataType the requested data type
     * @param index the index of the first event to retrieve
     */
    public TmfDataRequest(Class<? extends ITmfEvent> dataType, long index) {
        this(dataType, index, ALL_DATA, DEFAULT_BLOCK_SIZE, ExecutionType.FOREGROUND);
    }

    /**
     * Request all the events of a given type from the given index (given priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     *
     * @param dataType the requested data type
     * @param index the index of the first event to retrieve
     * @param priority the requested execution priority
     */
    public TmfDataRequest(Class<? extends ITmfEvent> dataType, long index, ExecutionType priority) {
        this(dataType, index, ALL_DATA, DEFAULT_BLOCK_SIZE, priority);
    }

    /**
     * Request 'n' events of a given type from the given index (high priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     *
     * @param dataType the requested data type
     * @param index the index of the first event to retrieve
     * @param nbRequested the number of events requested
     */
    public TmfDataRequest(Class<? extends ITmfEvent> dataType, long index, int nbRequested) {
        this(dataType, index, nbRequested, DEFAULT_BLOCK_SIZE, ExecutionType.FOREGROUND);
    }

    /**
     * Request 'n' events of a given type from the given index (given priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     *
     * @param dataType the requested data type
     * @param index the index of the first event to retrieve
     * @param nbRequested the number of events requested
     * @param priority the requested execution priority
     */
    public TmfDataRequest(Class<? extends ITmfEvent> dataType, long index, int nbRequested, ExecutionType priority) {
        this(dataType, index, nbRequested, DEFAULT_BLOCK_SIZE, priority);
    }

    /**
     * Request 'n' events of a given type from the given index (high priority).
     * Events are returned in blocks of the given size.
     *
     * @param dataType the requested data type
     * @param index the index of the first event to retrieve
     * @param nbRequested the number of events requested
     * @param blockSize the number of events per block
     */
    public TmfDataRequest(Class<? extends ITmfEvent> dataType, long index, int nbRequested, int blockSize) {
        this(dataType, index, nbRequested, blockSize, ExecutionType.FOREGROUND);
    }

    /**
     * Request 'n' events of a given type from the given index (given priority).
     * Events are returned in blocks of the given size.
     *
     * @param dataType the requested data type
     * @param index the index of the first event to retrieve
     * @param nbRequested the number of events requested
     * @param blockSize the number of events per block
     * @param priority the requested execution priority
     */
    public TmfDataRequest(Class<? extends ITmfEvent> dataType, long index, int nbRequested, int blockSize, ExecutionType priority) {
        fRequestId = fRequestNumber++;
        fDataType = dataType;
        fIndex = index;
        fNbRequested = nbRequested;
        fBlockSize = blockSize;
        fExecType = priority;
        fNbRead = 0;

        fRequestRunning = false;
        fRequestCompleted = false;
        fRequestFailed = false;
        fRequestCanceled = false;

        if (!(this instanceof ITmfEventRequest) && TmfCoreTracer.isRequestTraced()) {
            String type = getClass().getName();
            type = type.substring(type.lastIndexOf('.') + 1);
            @SuppressWarnings("nls")
            String message = "CREATED "
                    + (getExecType() == ITmfDataRequest.ExecutionType.BACKGROUND ? "(BG)" : "(FG)")
                    + " Type=" + type + " Index=" + getIndex() + " NbReq=" + getNbRequested()
                    + " DataType=" + getDataType().getSimpleName();
            TmfCoreTracer.traceRequest(this, message);
        }
    }

    /**
     * Copy constructor
     */
    @SuppressWarnings("unused")
    private TmfDataRequest(TmfDataRequest other) {
        this(null, 0, ALL_DATA, DEFAULT_BLOCK_SIZE);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the request ID
     */
    @Override
    public int getRequestId() {
        return fRequestId;
    }

    /**
     * @return the index of the first event requested
     */
    @Override
    public long getIndex() {
        return fIndex;
    }

    /**
     * @return the execution type (priority)
     */
    @Override
    public ExecutionType getExecType() {
        return fExecType;
    }

    /**
     * @return the number of requested events (ALL_DATA = all)
     */
    @Override
    public int getNbRequested() {
        return fNbRequested;
    }

    /**
     * @return the block size (for BG requests)
     */
    @Override
    public int getBlockSize() {
        return fBlockSize;
    }

    /**
     * @return the number of events read so far
     */
    @Override
    public synchronized int getNbRead() {
        return fNbRead;
    }

    /**
     * @return indicates if the request is currently running
     */
    @Override
    public synchronized boolean isRunning() {
        return fRequestRunning;
    }

    /**
     * @return indicates if the request is completed
     */
    @Override
    public synchronized boolean isCompleted() {
        return fRequestCompleted;
    }

    /**
     * @return indicates if the request has failed
     */
    @Override
    public synchronized boolean isFailed() {
        return fRequestFailed;
    }

    /**
     * @return indicates if the request is canceled
     */
    @Override
    public synchronized boolean isCancelled() {
        return fRequestCanceled;
    }

    /**
     * @return the requested data type
     */
    @Override
    public Class<? extends ITmfEvent> getDataType() {
        return fDataType;
    }

    // ------------------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------------------

    /**
     * this method is called by the event provider to set the index corresponding to the time range start time
     *
     * @param index
     *            the start time index
     */
    protected void setIndex(int index) {
        fIndex = index;
    }

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

    /**
     * Handle incoming data, one event at a time i.e. this method is invoked
     * for every data item obtained by the request.
     *
     * - Data items are received in the order they appear in the stream
     * - Called by the request processor, in its execution thread, every time
     *   a block of data becomes available.
     * - Request processor performs a synchronous call to handleData() i.e.
     *   its execution threads holds until handleData() returns.
     * - Original data items are disposed of on return i.e. keep a reference
     *   (or a copy) if some persistence is needed between invocations.
     * - When there is no more data, done() is called.
     *
     * @param data a piece of data
     */
    @Override
    public void handleData(ITmfEvent data) {
        if (data != null) {
            fNbRead++;
        }
    }

    @Override
    public void handleStarted() {
        if (TmfCoreTracer.isRequestTraced()) {
            TmfCoreTracer.traceRequest(this, "STARTED"); //$NON-NLS-1$
        }
    }

    /**
     * Handle the completion of the request. It is called when there is no
     * more data available either because:
     * - the request completed normally
     * - the request failed
     * - the request was canceled
     *
     * As a convenience, handleXXXX methods are provided. They are meant to be
     * overridden by the application if it needs to handle these conditions.
     */
    @Override
    public void handleCompleted() {
        boolean requestFailed = false;
        boolean requestCanceled = false;
        synchronized (this) {
            requestFailed = fRequestFailed;
            requestCanceled = fRequestCanceled;
        }

        if (requestFailed) {
            handleFailure();
        } else if (requestCanceled) {
            handleCancel();
        } else {
            handleSuccess();
        }
        if (TmfCoreTracer.isRequestTraced()) {
            TmfCoreTracer.traceRequest(this, "COMPLETED (" + fNbRead + " events read)"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @Override
    public void handleSuccess() {
        if (TmfCoreTracer.isRequestTraced()) {
            TmfCoreTracer.traceRequest(this, "SUCCEEDED"); //$NON-NLS-1$
        }
    }

    @Override
    public void handleFailure() {
        if (TmfCoreTracer.isRequestTraced()) {
            TmfCoreTracer.traceRequest(this, "FAILED"); //$NON-NLS-1$
        }
    }

    @Override
    public void handleCancel() {
        if (TmfCoreTracer.isRequestTraced()) {
            TmfCoreTracer.traceRequest(this, "CANCELLED"); //$NON-NLS-1$
        }
    }

    /**
     * To suspend the client thread until the request starts (or is canceled).
     *
     * @throws InterruptedException
     *             If the thread was interrupted while waiting
     */
    public void waitForStart() throws InterruptedException {
        while (!fRequestRunning) {
            startedLatch.await();
        }
    }

    /**
     * To suspend the client thread until the request completes (or is
     * canceled).
     *
     * @throws InterruptedException
     *             If the thread was interrupted while waiting
     */
    @Override
    public void waitForCompletion() throws InterruptedException {
        while (!fRequestCompleted) {
            completedLatch.await();
        }
    }

    /**
     * Called by the request processor upon starting to service the request.
     */
    @Override
    public void start() {
        synchronized (this) {
            fRequestRunning = true;
        }
        handleStarted();
        startedLatch.countDown();
    }

    /**
     * Called by the request processor upon completion.
     */
    @Override
    public void done() {
        synchronized (this) {
            if (!fRequestCompleted) {
                fRequestRunning = false;
                fRequestCompleted = true;
            } else {
                return;
            }
        }
        try {
            handleCompleted();
        } finally {
            completedLatch.countDown();
        }
    }

    /**
     * Called by the request processor upon failure.
     */
    @Override
    public void fail() {
        synchronized (this) {
            fRequestFailed = true;
        }
        done();
    }

    /**
     * Called by the request processor upon cancellation.
     */
    @Override
    public void cancel() {
        synchronized (this) {
            fRequestCanceled = true;
        }
        done();
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
        if (other instanceof TmfDataRequest) {
            TmfDataRequest request = (TmfDataRequest) other;
            return (request.fDataType == fDataType) && (request.fIndex == fIndex)
                    && (request.fNbRequested == fNbRequested);
        }
        return false;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "[TmfDataRequest(" + fRequestId + "," + fDataType.getSimpleName() + "," + fIndex + "," + fNbRequested
                + "," + getBlockSize() + ")]";
    }
}
