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
 *   Francois Chouinard - Rebased on ITmfRequest, removed duplicates, deprecated
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.request;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;

/**
 * The TMF data request
 *
 * @version 1.0
 * @author Francois Chouinard
 */
@Deprecated
public interface ITmfDataRequest extends ITmfRequest {

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
    public ExecutionType getExecType();

    /**
	 * @return the index of the first event requested
     */
    public long getIndex();

    /**
     * @return the block size (for BG requests)
     */
    public int getBlockSize();

    /**
     * @return the number of events read so far
     */
    public int getNbRead();

	// ------------------------------------------------------------------------
	// Data handling
	// ------------------------------------------------------------------------

    /**
     * Process the piece of data
     *
     * @param data the data to process
     */
    public void handleData(ITmfEvent data);

}
