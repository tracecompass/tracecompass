/*******************************************************************************
 * Copyright (c) 2009, 2010, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Rebased on TmfRequest and deprecated
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.request;

import org.eclipse.linuxtools.internal.tmf.core.TmfCoreTracer;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;

/**
 * TmfDataRequests are used to obtain blocks of contiguous data from a data
 * provider. Open ranges can be used, especially for continuous streaming.
 * <p>
 * The request is processed asynchronously by a TmfProvider and, as blocks of
 * data become available, handleData() is invoked synchronously for each block.
 * Upon return, the data instances go out of scope and become eligible for gc.
 * It is is thus the responsibility of the requester to either clone or keep a
 * reference to the data it wishes to track specifically.
 * <p>
 * This data block approach is used to avoid busting the heap for very large
 * trace files. The block size is configurable.
 * <p>
 * The TmfProvider indicates that the request is completed by calling done().
 * The request can be canceled at any time with cancel().
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
 * TODO: Consider decoupling from "time range", "rank", etc and for the more
 * generic notion of "criteria". This would allow to extend for "time range",
 * etc instead of providing specialized constructors. This also means removing
 * the criteria info from the data structure (with the possible exception of
 * fNbRequested). The nice thing about it is that it would prepare us well for
 * the coming generation of analysis tools.
 *
 * TODO: Implement request failures (codes, etc...)
 *
 * @author Francois Chouinard
 * @version 1.1
 */
@Deprecated
@SuppressWarnings("deprecation")
public abstract class TmfDataRequest extends TmfRequest implements ITmfDataRequest {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /** The default maximum number of events per chunk */
    public static final int DEFAULT_BLOCK_SIZE = 1000;

    /** The request count for all the events */
    public static final int ALL_DATA = Integer.MAX_VALUE;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final TmfEventTypeFilter fEventTypeFilter;

    /** The block size (for BG requests) */
    private final int fBlockSize;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

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
        super(TmfTimeRange.ETERNITY, index, nbRequested,
              priority == ITmfDataRequest.ExecutionType.FOREGROUND ? ITmfRequest.TmfRequestPriority.HIGH : ITmfRequest.TmfRequestPriority.NORMAL);
        fEventTypeFilter = new TmfEventTypeFilter(dataType);
        addEventFilter(fEventTypeFilter);

        fBlockSize = blockSize;

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
     * @return the index of the first event requested
     */
    @Override
    public long getIndex() {
        return getStartIndex();
    }

    /**
     * @return the execution type (priority)
     */
    @Override
    public ExecutionType getExecType() {
        return getRequestPriority() == ITmfRequest.TmfRequestPriority.HIGH ?
                ITmfDataRequest.ExecutionType.FOREGROUND : ITmfDataRequest.ExecutionType.BACKGROUND;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#getNbRequested()
     */
    @Override
    public synchronized long getNbRequested() {
        long nbRequested = super.getNbRequested();
        return (nbRequested > 0 && nbRequested < Integer.MAX_VALUE) ? nbRequested : Integer.MAX_VALUE;
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
        return (int) getNbEventsRead();
    }

    /**
     * @return the requested data type
     */
    @Override
    public Class<? extends ITmfEvent> getDataType() {
        return fEventTypeFilter.getEventType();
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
        setStartIndex(index);
    }

    // ------------------------------------------------------------------------
    // Operations
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
        super.handleEvent(data);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.TmfRequest#handleEvent(org.eclipse.linuxtools.tmf.core.event.ITmfEvent)
     *
     * The TmfEventThread now calls handleEvent(). To ensure that handleData()
     * overrides are correctly handled, the method is overridden here.
     */
    @Override
    public synchronized void handleEvent(ITmfEvent data) {
        handleData(data);
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
        if (other == this) {
            return true;
        }
        if (other instanceof TmfDataRequest) {
            TmfDataRequest request = (TmfDataRequest) other;
            return (request.fEventTypeFilter.getEventType() == fEventTypeFilter.getEventType()) && (request.getStartIndex() == getStartIndex())
                    && (request.getNbRequested() == getNbRequested());
        }
        return false;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "[TmfDataRequest(" + getRequestId() + "," + fEventTypeFilter.getEventType().getSimpleName() + "," + getStartIndex() + "," + getNbRequested()
                + "," + getBlockSize() + ")]";
    }
}
