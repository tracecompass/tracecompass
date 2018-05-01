/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.response;

import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents a base response that data providers may return. Some
 * analyses may take too long so we return a partial model wrapped in a
 * response. Depending on the status, it's the responsibility of the viewer to
 * request again the data provider for an updated model. Instances of this class
 * should be immutable.
 *
 * @author Yonni Chen
 * @param <T>
 *            A specific model computed by a specific data provider. This object
 *            must be serializable.
 * @since 4.0
 */
public class TmfModelResponse<T> implements ITmfResponse {

    private final Status fStatus;
    private final String fStatusMessage;
    private final @Nullable T fModel;

    /**
     * Constructor
     *
     * @param model
     *            The Model of the response
     * @param status
     *            Status of the response. See documentation of
     *            {@link org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status}
     *            for supported status.
     * @param statusMessage
     *            Detailed message of the status. Useful when it's
     *            {@link org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status#FAILED}
     *            o
     *            {@link org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status#CANCELLED}
     */
    public TmfModelResponse(@Nullable T model, Status status, String statusMessage) {
        fModel = model;
        fStatus = status;
        fStatusMessage = statusMessage;
    }

    /**
     * Gets the model encapsulated by the response
     *
     * @return The model.
     */
    public @Nullable T getModel() {
        return fModel;
    }

    @Override
    public Status getStatus() {
        return fStatus;
    }

    @Override
    public String getStatusMessage() {
        return fStatusMessage;
    }
}
