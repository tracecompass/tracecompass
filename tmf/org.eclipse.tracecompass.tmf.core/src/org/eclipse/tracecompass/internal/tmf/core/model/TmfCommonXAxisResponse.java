/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.model;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisResponse;

/**
 * This is a basic implementation of {@link ITmfCommonXAxisResponse}
 *
 * @author Yonni Chen
 * @since 3.0
 */
public class TmfCommonXAxisResponse implements ITmfCommonXAxisResponse {

    private final @Nullable ITmfCommonXAxisModel fModel;
    private final Status fStatus;
    private final @Nullable String fStatusMessage;
    private final long fCurrentEnd;

    /**
     * Constructor
     *
     * @param model
     *            The XY Model
     * @param status
     *            Status of the response. See documentation of
     *            {@link org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisResponse.Status}
     *            for supported status.
     * @param statusMessage
     *            Detailed message of the status. Useful when it's
     *            {@link org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisResponse.Status#FAILED}
     *            or
     *            {@link org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisResponse.Status#CANCELLED}
     * @param currentEnd
     *            Current end of the state system
     */
    public TmfCommonXAxisResponse(@Nullable ITmfCommonXAxisModel model, Status status, @Nullable String statusMessage, long currentEnd) {
        fModel = model;
        fStatus = status;
        fStatusMessage = statusMessage;
        fCurrentEnd = currentEnd;
    }

    @Override
    public @Nullable ITmfCommonXAxisModel getModel() {
        return fModel;
    }

    @Override
    public Status getStatus() {
        return fStatus;
    }

    @Override
    public @Nullable String getStatusMessage() {
        return fStatusMessage;
    }

    @Override
    public long getCurrentEnd() {
        return fCurrentEnd;
    }
}
