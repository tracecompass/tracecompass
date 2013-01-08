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

package org.eclipse.linuxtools.tmf.core.request;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.filter.ITmfFilter;

/**
 * The TMF request API.
 * <p>
 * ITmfRequest:s are used to obtain blocks of events from an event provider. Open
 * ranges can be used, typically for continuous streaming.
 * <p>
 * The request is processed asynchronously by a TmfRequestHandler which invokes
 * the request's handleEvent() synchronously for each event that matches the
 * request filter(s).
 * <p>
 * The TmfProvider indicates that the request is completed by calling done().
 * The request can be canceled at any time with cancel(). In case of unexpected
 * exception or error, fail() will be called.
 * <p>
 * Typical usage:
 * <pre>
 * <code>
 * ITmfRequest request = new TmfRequest(someParams) {
 *     &#64;Override
 *     public synchronized void handleEvent(ITmfEvent event) {
 *          super.handleEvent(event);
 *          // do something
 *     }
 *     &#64;Override
 *     public void handleCompleted() {
 *          // do something
 *     }
 *     &#64;Override
 *     public void handleCancel() {
 *          // do something
 *     }
 *     &#64;Override
 *     public void handleFailure() {
 *          // do something
 *     }
 * };
 * request.addEventFilter(myFilter);
 * fProcessor.process(request);
 * </code>
 * </pre>
 *
 * @author Francois Chouinard
 * @version 1.0
 * @since 2.0
 */
public interface ITmfRequest {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /** The request count for all the events */
    public static final long ALL_EVENTS = Long.MAX_VALUE;

    // ------------------------------------------------------------------------
    // Enumerated Types
    // ------------------------------------------------------------------------

    /**
     * The request execution type/priority
     * @author francois
     */
    public enum TmfRequestPriority {
        /**
         * Normal priority request (preemptible)
         */
        NORMAL,
        /**
         * High priority request (non-preemptible)
         */
        HIGH
    }

    /**
     * The request execution state
     * @author francois
     */
    public enum TmfRequestState {
        /**
         * The request is created but has not started being processed yet
         */
        PENDING,
        /**
         * The request is being handled but has not completed yet
         */
        RUNNING,
        /**
         * The request has completed
         */
        COMPLETED
    }

	// ------------------------------------------------------------------------
	// Getters
	// ------------------------------------------------------------------------

    /**
     * @return the request ID
     */
    public int getRequestId();

    /**
     * @return the request type
     */
    public TmfRequestPriority getRequestPriority();

    /**
     * @return the time range of interest
     */
    public TmfTimeRange getTimeRange();

    /**
     * @return the number of events requested
     */
    public long getNbRequested();

    /**
     * @return the index of the first event requested
     */
    public long getStartIndex();

    /**
     * @return the number of events read so far
     */
    public long getNbEventsRead();

    /**
     * @param filterType the filter type to retrieve
     *
     * @return the corresponding filter (if any)
     */
    public ITmfFilter getEventFilter(Class<?> filterType);

    /**
     * Add/replace a filter
     *
     * @param filter the new filter
     */
    public void addEventFilter(ITmfFilter filter);

    /**
     * Check if the event matches the request
     *
     * @param event the event to test
     *
     * @return true if the event matches the request, false otherwise
     */
    public boolean matches(ITmfEvent event);

    /**
     * Sets the request's parent in the hierarchy
     *
     * @param parent the parent request
     */
    public void setParent(ITmfRequest parent);

    /**
     * Gets the request's parent in the hierarchy
     *
     * @return the parent request
     */
    public ITmfRequest getParent();

    /**
     * Completion notification for teh parent request
     *
     * @param child the child request
     */
    public void notifyParent(ITmfRequest child);

	// ------------------------------------------------------------------------
	// Request execution state
	// ------------------------------------------------------------------------

    /**
     * @return the request execution state
     */
    public TmfRequestState getState();

    /**
     * @return true if the request is still active
     */
    public boolean isRunning();

    /**
     * @return true if the request is completed
     */
    public boolean isCompleted();

    // ------------------------------------------------------------------------
    // Request completion status
    // ------------------------------------------------------------------------

    /**
     * @return the request completion status
     */
    public IStatus getStatus();

    /**
     * @return true if the request completed successfully
     */
    public boolean isOK();

    /**

     * @return true if the request has failed */
    public boolean isFailed();

    /**
     * @return true if the request was cancelled
     */
    public boolean isCancelled();

    // ------------------------------------------------------------------------
    // Request operations
    // ------------------------------------------------------------------------

    /**
     * Put the request in the running state
     */
    public void start();

    /**
     * Put the request in the terminated state
     */
    public void done();

    /**
     * Put the request in the failed state
     */
    public void fail();

    /**
     * Put the request in the cancelled state
     */
    public void cancel();

    /**
     * To suspend the client thread until the request starts
     * (or is canceled).
     *
     * @throws InterruptedException thrown if the request was cancelled
     */
    public void waitForStart() throws InterruptedException;

    /**
     * To suspend the client thread until the request completes
     * (or is canceled).
     *
     * @throws InterruptedException thrown if the request was cancelled
     */
    public void waitForCompletion() throws InterruptedException;

	// ------------------------------------------------------------------------
	// Request processing hooks
	// ------------------------------------------------------------------------

    /**
     * Request processing start notification
     */
    public void handleStarted();

    /**
     * Process the piece of data
     *
     * @param event the data to process
     */
    public void handleEvent(ITmfEvent event);

    /**
     * Request processing completion notification
     */
    public void handleCompleted();

    /**
     * Request successful completion notification
     */
    public void handleSuccess();

    /**
     * Request failure notification
     */
    public void handleFailure();

    /**
     * Request cancellation notification
     */
    public void handleCancel();

}
