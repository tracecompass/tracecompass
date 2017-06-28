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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.internal.tmf.core.model.TmfCommonXAxisModel;
import org.eclipse.tracecompass.internal.tmf.core.model.TmfCommonXAxisResponse;

/**
 * This class creates concrete instance of {@link ITmfCommonXAxisResponse}
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
     * Create a concrete {@link ITmfCommonXAxisResponse} with a either RUNNING or
     * COMPLETED status. Model is not null, it's either partial or full.
     *
     * @param title
     *            Chart title
     * @param xValues
     *            The x values requested by the viewer
     * @param yModels
     *            Collection of IYModel
     * @param currentEnd
     *            The current end that was being processed
     * @param isComplete
     *            Tells whether the computed model is complete or partial
     * @return A {@link ITmfCommonXAxisResponse} with either a running status or a
     *         completed status
     */
    public static ITmfCommonXAxisResponse create(String title, long[] xValues, Map<String, IYModel> yModels, long currentEnd, boolean isComplete) {
        ITmfCommonXAxisModel model = new TmfCommonXAxisModel(title, xValues, yModels);

        if (isComplete) {
            return new TmfCommonXAxisResponse(model, ITmfCommonXAxisResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED, currentEnd);
        }
        return new TmfCommonXAxisResponse(model, ITmfCommonXAxisResponse.Status.RUNNING, CommonStatusMessage.RUNNING, currentEnd);
    }

    /**
     * Create a concrete {@link ITmfCommonXAxisResponse} with a FAILED status. Model
     * inside of returned response is null.
     *
     * @param message
     *            A detailed message of why the response has a failed status
     * @return A {@link ITmfCommonXAxisResponse} with a failed status and null model
     */
    public static ITmfCommonXAxisResponse createFailedResponse(@Nullable String message) {
        return new TmfCommonXAxisResponse(null, ITmfCommonXAxisResponse.Status.FAILED, message, 0);
    }

    /**
     * Create a concrete {@link ITmfCommonXAxisResponse} with a CANCELLED status.
     * Model inside of returned response is null.
     *
     * @param message
     *            A detailed message of why the response has a cancelled status
     * @return A {@link ITmfCommonXAxisResponse} with a cancelled status and null
     *         model
     */
    public static ITmfCommonXAxisResponse createCancelledResponse(@Nullable String message) {
        return new TmfCommonXAxisResponse(null, ITmfCommonXAxisResponse.Status.CANCELLED, message, 0);
    }
}
