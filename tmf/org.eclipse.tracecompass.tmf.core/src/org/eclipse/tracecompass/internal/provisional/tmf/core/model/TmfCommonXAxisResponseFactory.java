/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model;

import java.util.Map;
import java.util.Objects;

import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.internal.tmf.core.model.TmfCommonXAxisModel;

/**
 * This class creates instance of {@link TmfModelResponse}
 *
 * @author Yonni Chen
 */
public final class TmfCommonXAxisResponseFactory {

    /**
     * Constructor
     */
    private TmfCommonXAxisResponseFactory() {

    }

    /**
     * Create a {@link TmfModelResponse} with a either RUNNING or COMPLETED status.
     * Model is not null, it's either partial or full.
     *
     * @param title
     *            Chart title
     * @param xValues
     *            The x values requested by the viewer
     * @param yModels
     *            Collection of IYModel
     * @param isComplete
     *            Tells whether the computed model is complete or partial
     * @return A {@link TmfModelResponse} with either a running status or a
     *         completed status
     */
    public static TmfModelResponse<ITmfCommonXAxisModel> create(String title, long[] xValues, Map<String, IYModel> yModels, boolean isComplete) {
        ITmfCommonXAxisModel model = new TmfCommonXAxisModel(title, xValues, yModels);

        if (isComplete) {
            return new TmfModelResponse<>(model, ITmfResponse.Status.COMPLETED, Objects.requireNonNull(CommonStatusMessage.COMPLETED));
        }
        return new TmfModelResponse<>(model, ITmfResponse.Status.RUNNING, Objects.requireNonNull(CommonStatusMessage.RUNNING));
    }

    /**
     * Create a {@link TmfModelResponse} with a FAILED status. Model inside of
     * returned response is null.
     *
     * @param message
     *            A detailed message of why the response has a failed status
     * @return A {@link TmfModelResponse} with a failed status and null model
     */
    public static TmfModelResponse<ITmfCommonXAxisModel> createFailedResponse(String message) {
        return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, message);
    }

    /**
     * Create a {@link TmfModelResponse} with a CANCELLED status. Model inside of
     * returned response is null.
     *
     * @param message
     *            A detailed message of why the response has a cancelled status
     * @return A {@link TmfModelResponse} with a cancelled status and null model
     */
    public static TmfModelResponse<ITmfCommonXAxisModel> createCancelledResponse(String message) {
        return new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, message);
    }
}
