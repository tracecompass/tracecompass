/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.response;

/**
 * A response interface. It contains informations about the status and a
 * detailed message of it. Data providers that return partial models may
 * implements this interface.
 *
 * @author Yonni Chen
 * @since 4.0
 */
public interface ITmfResponse {

    /**
     * The status of the response can be either a runing, completed, failed or
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
     * Gets the status of the response
     *
     * @return A {@link Status}
     */
    Status getStatus();

    /**
     * Gets the detailed status message
     *
     * @return An associated message with the status.
     */
    String getStatusMessage();
}