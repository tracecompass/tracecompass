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
    public Class<? extends ITmfEvent> getDataType();

    /**
     * @return request ID
     */
    public int getRequestId();

    /**
     * @return request ID
     */
    public ExecutionType getExecType();

    /**
	 * @return the index of the first event requested
     */
    public long getIndex();

    /**
     * @return the number of requested events
     */
    public int getNbRequested();

    /**
     * @return the block size (for BG requests)
     */
    public int getBlockSize();

    /**
     * @return the number of events read so far
     */
    public int getNbRead();

	// ------------------------------------------------------------------------
	// Request state predicates
	// ------------------------------------------------------------------------

    /**
     * @return true if the request is still active
     */
    public boolean isRunning();

    /**
     * @return true if the request is completed
     */
    public boolean isCompleted();

    /**
     * @return true if the request has failed
     */
    public boolean isFailed();

    /**
     * @return true if the request was cancelled
     */
    public boolean isCancelled();

	// ------------------------------------------------------------------------
	// Data handling
	// ------------------------------------------------------------------------

    /**
     * Process the piece of data
     *
     * @param data the data to process
     */
    public void handleData(ITmfEvent data);

	// ------------------------------------------------------------------------
	// Request notifications
	// ------------------------------------------------------------------------

    /**
     * Request processing start notification
     */
    public void handleStarted();

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

    /**
     * To suspend the client thread until the request completes
     * (or is canceled).
     *
     * @throws InterruptedException thrown if the request was cancelled
     */
    public void waitForCompletion() throws InterruptedException;

	// ------------------------------------------------------------------------
	// Request state modifiers
	// ------------------------------------------------------------------------

    /**
     * Put the request in the running state
     */
    public void start();

    /**
     * Put the request in the completed state
     */
    public void done();

    /**
     * Put the request in the failed completed state
     */
    public void fail();

    /**
     * Put the request in the cancelled completed state
     */
    public void cancel();
}
