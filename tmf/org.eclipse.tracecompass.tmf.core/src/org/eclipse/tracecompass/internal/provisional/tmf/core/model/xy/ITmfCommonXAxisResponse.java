/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy;

import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents a response from a XY data providers. Some analyses may
 * take too long so we return a partial model wrapped in a response. Depending
 * on the status, it's the responsability of the viewer to request again the XY
 * data provider for an updated model. Instances of this class should be
 * immutables.
 *
 * @author Yonni Chen
 */
public interface ITmfCommonXAxisResponse {

    /**
     * The status of the response can be either a running, completed, failed or
     * cancelled
     *
     * @author Yonni Chen
     */
    enum Status {
        /**
         * Model is partial, data provider is still computing. If this status is
         * returned, it's viewer responsability to request again the data provider after
         * waiting some time. Request data provider until COMPLETED status is received
         */
        RUNNING,
        /**
         * Model is complete, no need to request data provider again
         */
        COMPLETED,
        /**
         * Error happened. Please see logs or detailed message of status.
         */
        FAILED,
        /**
         * Task has been cancelled. Please see logs or detailed message of status.
         */
        CANCELLED
    }

    /**
     * Gets the model encapsulated by the response. This model should never be null
     * if the status is running or completed, but is expected to be null when failed
     * or cancelled.
     *
     * @return The XY model.
     */
    @Nullable ITmfCommonXAxisModel getModel();

    /**
     * Gets the status of the response.
     *
     * @return A {@link Status}
     */
    Status getStatus();

    /**
     * Gets the detailed status message
     *
     * @return An associated message with the status.
     */
    @Nullable String getStatusMessage();

    /**
     * Get current end of the state system
     *
     * @return the current end
     */
    long getCurrentEnd();
}
