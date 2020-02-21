/*******************************************************************************
 * Copyright (c) 2009, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Alexandre Montplaisir - Merge with ITmfDataRequest
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.request;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;

/**
 * The TMF event request
 *
 * @author Francois Chouinard
 */
public interface ITmfEventRequest {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The request count for all the events
     */
    static final int ALL_DATA = Integer.MAX_VALUE;

    /**
     * The request execution type/priority
     */
    enum ExecutionType {
        /**
         * Background, long-running, lower priority request.
         */
        BACKGROUND,
        /**
         * Foreground, short-running, high priority request.
         */
        FOREGROUND,
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return request data type (T)
     */
    Class<? extends ITmfEvent> getDataType();

    /**
     * @return request ID
     */
    int getRequestId();

    /**
     * @return request ID
     */
    ExecutionType getExecType();

    /**
     * Gets the dependency level. Use different dependency level for requests
     * that have a dependency with each other. They will be serviced separately.
     *
     * @return dependency
     * @since 2.0
     */
    default int getDependencyLevel() {
        return 0;
    }

    /**
     * @return the index of the first event requested
     */
    long getIndex();

    /**
     * @return the number of requested events
     */
    int getNbRequested();

    /**
     * @return the number of events read so far
     */
    int getNbRead();

    /**
     * @return the requested time range
     */
    TmfTimeRange getRange();

    /**
     * @return the event provider filter to verify if an event is provided by
     *         the relevant event provider.
     */
    ITmfFilter getProviderFilter();

    /**
     * Sets a provider filter to verify if an event is provided by the relevant
     * event provider.
     *
     * @param filter
     *            event provider filter to set
     */
    void setProviderFilter(ITmfFilter filter);

    // ------------------------------------------------------------------------
    // Request state predicates
    // ------------------------------------------------------------------------

    /**
     * @return true if the request is still active
     */
    boolean isRunning();

    /**
     * @return true if the request is completed
     */
    boolean isCompleted();

    /**
     * @return true if the request has failed
     */
    boolean isFailed();

    /**
     * @return true if the request was cancelled
     */
    boolean isCancelled();

    /**
     * @return get the cause of failure, or null if not applicable
     * @since 2.0
     */
    @Nullable Throwable getFailureCause();

    // ------------------------------------------------------------------------
    // Data handling
    // ------------------------------------------------------------------------

    /**
     * Process the piece of data
     *
     * @param event
     *            The trace event to process
     */
    void handleData(@NonNull ITmfEvent event);

    // ------------------------------------------------------------------------
    // Request notifications
    // ------------------------------------------------------------------------

    /**
     * Request processing start notification
     */
    void handleStarted();

    /**
     * Request processing completion notification
     */
    void handleCompleted();

    /**
     * Request successful completion notification
     */
    void handleSuccess();

    /**
     * Request failure notification
     */
    void handleFailure();

    /**
     * Request cancellation notification
     */
    void handleCancel();

    /**
     * To suspend the client thread until the request completes (or is
     * cancelled).
     *
     * @throws InterruptedException
     *             thrown if the request was cancelled
     */
    void waitForCompletion() throws InterruptedException;

    // ------------------------------------------------------------------------
    // Request state modifiers
    // ------------------------------------------------------------------------

    /**
     * Put the request in the running state
     */
    void start();

    /**
     * Put the request in the completed state
     */
    void done();

    /**
     * Put the request in the failed completed state
     *
     * @param e
     *            the exception causing the failure, can be null
     * @since 2.0
     */
    void fail(Exception e);

    /**
     * Put the request in the cancelled completed state
     */
    void cancel();

    // ------------------------------------------------------------------------
    // Others
    // ------------------------------------------------------------------------

    /**
     * This method is called by the event provider to set the index
     * corresponding to the time range start time
     *
     * @param index
     *            The start time index
     */
    void setStartIndex(int index);

}
