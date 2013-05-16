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

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;

/**
 * The TMF data request
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public interface ITmfDataRequest {

	// ------------------------------------------------------------------------
	// Constants
	// ------------------------------------------------------------------------

    /**
     * The request execution type/priority
     */
    public enum ExecutionType {
        /**
         * Backgroung, long-running, lower priority request
         */
        BACKGROUND,
        /**
         * Foreground, short-running, high priority request
         */
        FOREGROUND
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
	 * @return the index of the first event requested
     */
    long getIndex();

    /**
     * @return the number of requested events
     */
    int getNbRequested();

    /**
     * @return the block size (for BG requests)
     */
    int getBlockSize();

    /**
     * @return the number of events read so far
     */
    int getNbRead();

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

	// ------------------------------------------------------------------------
	// Data handling
	// ------------------------------------------------------------------------

    /**
     * Process the piece of data
     *
     * @param data the data to process
     */
    void handleData(ITmfEvent data);

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
     * To suspend the client thread until the request completes
     * (or is canceled).
     *
     * @throws InterruptedException thrown if the request was cancelled
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
     */
    void fail();

    /**
     * Put the request in the cancelled completed state
     */
    void cancel();
}
