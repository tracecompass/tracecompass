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

package org.eclipse.tracecompass.tmf.core.model.tree;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;

/**
 * This interface represents a tree data provider. It returns a computed model
 * that will be used by tree viewers.
 *
 * @author Yonni Chen
 * @param <T>
 *            Tree model extending {@link ITmfTreeDataModel}
 * @since 4.0
 */
public interface ITmfTreeDataProvider<T extends ITmfTreeDataModel> {

    /**
     * This methods computes a tree model. Then, it returns a
     * {@link TmfModelResponse} that contains the model. Tree model will be used
     * by tree viewer to show entries as a tree or flat hierarchy
     *
     * @param filter
     *            A query filter that contains an array of time. Times are used
     *            for requesting data.
     * @param monitor
     *            A ProgressMonitor to cancel task
     * @return A {@link TmfModelResponse} instance
     *
     * @deprecated Use fetchTree with a map of parameters
     */
    @Deprecated
    TmfModelResponse<List<T>> fetchTree(TimeQueryFilter filter, @Nullable IProgressMonitor monitor);

    /**
     * This methods computes a tree model. Then, it returns a
     * {@link TmfModelResponse} that contains the model. Tree model will be used
     * by tree viewer to show entries as a tree or flat hierarchy
     *
     * @param fetchParameters
     *            A query filter that contains an array of time. Times are used
     *            for requesting data.
     * @param monitor
     *            A ProgressMonitor to cancel task
     * @return A {@link TmfModelResponse} instance
     * @since 5.0
     */
    default TmfModelResponse<TmfTreeModel<T>> fetchTree(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        TimeQueryFilter filter = FetchParametersUtils.createTimeQuery(fetchParameters);
        if (filter != null) {
            TmfModelResponse<List<T>> response = fetchTree(filter, monitor);
            TmfTreeModel<T> model = null;
            List<T> entryModel = response.getModel();
            if (entryModel != null) {
                model = new TmfTreeModel<>(Collections.emptyList(), entryModel);
            }
            return new TmfModelResponse<>(model, response.getStatus(), response.getStatusMessage());
        }

        return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.INCORRECT_QUERY_PARAMETERS);
    }

    /**
     * This method return the extension point ID of this provider
     *
     * @return The ID
     */
    String getId();

    /**
     * Dispose of the provider to avoid resource leakage.
     *
     * @since 4.0
     */
    public default void dispose() {
    }
}
