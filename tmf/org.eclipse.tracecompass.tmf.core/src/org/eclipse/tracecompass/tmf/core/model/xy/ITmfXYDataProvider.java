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

package org.eclipse.tracecompass.tmf.core.model.xy;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.model.TmfXyResponseFactory;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;

/**
 * This interface represents an XY data provider. It returns a response that
 * will be used by viewers. Response encapsulates a status and an XY model.
 *
 * @author Yonni Chen
 * @since 4.0
 */
public interface ITmfXYDataProvider {

    /**
     * This methods computes a XY model. Then, it returns a
     * {@link TmfModelResponse} that contains the model. XY model will be used
     * to draw XY charts. See {@link TmfXyResponseFactory} methods for creating
     * {@link TmfModelResponse}.
     *
     * @param fetchParameters
     *            Query parameters that contains an array of time. Times are
     *            used for requesting data.
     * @param monitor
     *            A ProgressMonitor to cancel task
     * @return A {@link TmfModelResponse} instance
     * @since 5.0
     */
    TmfModelResponse<ITmfXyModel> fetchXY(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor);
}