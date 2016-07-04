/*******************************************************************************
 * Copyright (c) 2009, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Alexandre Montplaisir - Consolidate constructors, merge with TmfDataRequest
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.request;

import java.util.concurrent.CountDownLatch;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.TmfCoreTracer;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;

/**
 * TmfEventRequest's are used to obtain series of events from an event provider.
 * Open ranges can be used, especially for continuous streaming.
 * <p>
 * The request is processed asynchronously by a TmfEventProvider and, as events
 * become available, handleData() is invoked synchronously for each one.
 * <p>
 * The TmfEventProvider indicates that the request is completed by calling
 * done(). The request can be cancelled at any time with cancel().
 * <p>
 * Typical usage:
 *
 * <pre>
 * <code>
 * TmfEventRequest request = new TmfEventRequest(DataType.class, range, startIndex, nbEvents, priority) {
 *
 *     public void handleData(ITmfEvent event) {
 *         // do something with the event
 *     }
 *
 *     public void handleSuccess() {
 *         // callback for when the request completes successfully
 *     }
 *
 *     public void handleFailure() {
 *         // callback for when the request fails due to an error
 *     }
 *
 *     public void handleCancel() {
 *         // callback for when the request is cancelled via .cancel()
 *     }
 *
 * };
 *
 * eventProvider.sendRequest(request);
 * </code>
 * </pre>
 *
 *
 * TODO: Implement request failures (codes, etc...)
 *
 * @author Francois Chouinard
 */
public abstract class TmfEventRequest implements ITmfEventRequest {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static int fRequestNumber = 0;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final Class<? extends ITmfEvent> fDataType;
    private final ExecutionType fExecType;

    /** A unique request ID */
    private final int fRequestId;

    /** The requested events time range */
    private final TmfTimeRange fRange;

    /** The index (rank) of the requested event */
    protected long fIndex;

    /** The number of requested events (ALL_DATA for all) */
    protected int fNbRequested;

    /** The number of reads so far */
    private int fNbRead;

    private final CountDownLatch fStartedLatch = new CountDownLatch(1);
    private final CountDownLatch fCompletedLatch = new CountDownLatch(1);

    private volatile boolean fRequestFailed = false;
    private volatile boolean fRequestCanceled = false;

    private ITmfFilter fEventFilter;

    private int fDependencyLevel;

    private @Nullable Throwable fFailureCause;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Request 'n' events of a given type, for the *whole* trace, at the given
     * priority.
     *
     * @param dataType
     *            The requested data type.
     * @param index
     *            The index of the first event to retrieve. You can use '0' to
     *            start at the beginning of the trace.
     * @param nbRequested
     *            The number of events requested. You can use
     *            {@link TmfEventRequest#ALL_DATA} to indicate you want all
     *            events in the trace.
     * @param priority
     *            The requested execution priority.
     */
    public TmfEventRequest(Class<? extends ITmfEvent> dataType,
            long index,
            int nbRequested,
            ExecutionType priority) {
        this(dataType, TmfTimeRange.ETERNITY, index, nbRequested, priority);
    }

    /**
     * Request 'n' events of a given type, for the given time range, at the
     * given priority.
     *
     * @param dataType
     *            The requested data type.
     * @param range
     *            The time range of the requested events. You can use
     *            {@link TmfTimeRange#ETERNITY} to indicate you want to cover
     *            the whole trace.
     * @param index
     *            The index of the first event to retrieve. You can use '0' to
     *            start at the beginning of the trace.
     * @param nbRequested
     *            The number of events requested. You can use
     *            {@link TmfEventRequest#ALL_DATA} to indicate you want all
     *            events in the time range.
     * @param priority
     *            The requested execution priority.
     */
    public TmfEventRequest(Class<? extends ITmfEvent> dataType,
            TmfTimeRange range,
            long index,
            int nbRequested,
            ExecutionType priority) {
        this(dataType, range, index, nbRequested, priority, 0);
    }

    /**
     * Request 'n' events of a given type, for the given time range, at the
     * given priority.
     *
     * @param dataType
     *            The requested data type.
     * @param range
     *            The time range of the requested events. You can use
     *            {@link TmfTimeRange#ETERNITY} to indicate you want to cover
     *            the whole trace.
     * @param index
     *            The index of the first event to retrieve. You can use '0' to
     *            start at the beginning of the trace.
     * @param nbRequested
     *            The number of events requested. You can use
     *            {@link TmfEventRequest#ALL_DATA} to indicate you want all
     *            events in the time range.
     * @param priority
     *            The requested execution priority.
     * @param dependencyLevel
     *            The dependency level. Use different dependency level for
     *            requests that have a dependency with each other. They will
     *            be serviced separately.
     * @since 2.0
     */
    public TmfEventRequest(Class<? extends ITmfEvent> dataType,
            TmfTimeRange range,
            long index,
            int nbRequested,
            ExecutionType priority,
            int dependencyLevel) {

        synchronized (TmfEventRequest.class) {
            fRequestId = fRequestNumber++;
        }
        fDataType = dataType;
        fIndex = index;
        fNbRequested = nbRequested;
        fExecType = priority;
        fRange = range;
        fNbRead = 0;
        fDependencyLevel = dependencyLevel;

        /* Setup the request tracing if it's enabled */
        if (TmfCoreTracer.isRequestTraced()) {
            String type = getClass().getName();
            type = type.substring(type.lastIndexOf('.') + 1);
            @SuppressWarnings("nls")
            String message = "CREATED "
                    + (getExecType() == ExecutionType.BACKGROUND ? "(BG)" : "(FG)")
                    + " Type=" + type + " Index=" + getIndex() + " NbReq=" + getNbRequested()
                    + " Range=" + getRange()
                    + " DataType=" + getDataType().getSimpleName()
                    + " DependencyLevel= " + fDependencyLevel;
            TmfCoreTracer.traceRequest(fRequestId, message);
        }
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public int getRequestId() {
        return fRequestId;
    }

    @Override
    public long getIndex() {
        return fIndex;
    }

    @Override
    public ExecutionType getExecType() {
        return fExecType;
    }

    @Override
    public int getNbRequested() {
        return fNbRequested;
    }

    @Override
    public synchronized int getNbRead() {
        return fNbRead;
    }

    @Override
    public synchronized boolean isRunning() {
        return (fStartedLatch.getCount() <= 0 && fCompletedLatch.getCount() > 0);
    }

    @Override
    public synchronized boolean isCompleted() {
        return (fCompletedLatch.getCount() <= 0);
    }

    @Override
    public synchronized boolean isFailed() {
        return fRequestFailed;
    }

    @Override
    public synchronized boolean isCancelled() {
        return fRequestCanceled;
    }

    @Override
    public Class<? extends ITmfEvent> getDataType() {
        return fDataType;
    }

    @Override
    public TmfTimeRange getRange() {
        return fRange;
    }

    @Override
    public ITmfFilter getProviderFilter() {
        return fEventFilter;
    }

    @Override
    public void setProviderFilter(ITmfFilter provider) {
        fEventFilter = provider;
    }

    /** @since 2.0 */
    @Override
    public int getDependencyLevel() {
        return fDependencyLevel;
    }

    /**
     * @since 2.0
     */
    @Override
    public @Nullable Throwable getFailureCause() {
        return fFailureCause;
    }

    // ------------------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------------------

    /**
     * This method is called by the event provider to set the index
     * corresponding to the time range start time
     *
     * @param index
     *            The start time index
     */
    protected void setIndex(int index) {
        fIndex = index;
    }

    @Override
    public void setStartIndex(int index) {
        setIndex(index);
    }

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

    @Override
    public void handleData(ITmfEvent event) {
        fNbRead++;
    }

    @Override
    public void handleStarted() {
        if (TmfCoreTracer.isRequestTraced()) {
            TmfCoreTracer.traceRequest(getRequestId(), "STARTED"); //$NON-NLS-1$
        }
    }

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
            TmfCoreTracer.traceRequest(getRequestId(), "COMPLETED (" + fNbRead + " events read)"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @Override
    public void handleSuccess() {
        if (TmfCoreTracer.isRequestTraced()) {
            TmfCoreTracer.traceRequest(getRequestId(), "SUCCEEDED"); //$NON-NLS-1$
        }
    }

    @Override
    public void handleFailure() {
        if (TmfCoreTracer.isRequestTraced()) {
            TmfCoreTracer.traceRequest(getRequestId(), "FAILED"); //$NON-NLS-1$
        }
    }

    @Override
    public void handleCancel() {
        if (TmfCoreTracer.isRequestTraced()) {
            TmfCoreTracer.traceRequest(getRequestId(), "CANCELLED"); //$NON-NLS-1$
        }
    }

    /**
     * To suspend the client thread until the request starts (or is canceled).
     *
     * @throws InterruptedException
     *             If the thread was interrupted while waiting
     */
    public void waitForStart() throws InterruptedException {
        fStartedLatch.await();
    }

    @Override
    public void waitForCompletion() throws InterruptedException {
        fCompletedLatch.await();
    }

    @Override
    public synchronized void start() {
        handleStarted();
        fStartedLatch.countDown();
    }

    @Override
    public synchronized void done() {
        handleCompleted();
        fCompletedLatch.countDown();
    }

    /**
     * @since 2.0
     */
    @Override
    public void fail(Exception e) {
        fRequestFailed = true;
        fFailureCause = e;
        done();
    }

    @Override
    public void cancel() {
        fRequestCanceled = true;
        done();
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        String name = getClass().getName();
        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            name = name.substring(dot + 1);
        }
        return '[' + name + '(' + getRequestId() + ',' + getDataType().getSimpleName() +
                ',' + getExecType() + ',' + getRange() + ',' + getIndex() +
                ',' + getNbRequested() + ','+ getDependencyLevel() + ")]"; //$NON-NLS-1$
    }

}
