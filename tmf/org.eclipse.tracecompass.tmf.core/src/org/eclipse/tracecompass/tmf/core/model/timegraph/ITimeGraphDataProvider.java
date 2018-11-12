/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.timegraph;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;

/**
 * Interface that provides data for states to time. Such a model has a
 * collection of {@link ITimeGraphEntryModel}s, which can be organized into a
 * tree. Each entry may have a {@link ITimeGraphRowModel} that contains
 * consecutive {@link ITimeGraphState}s associated to it by its unique ID. The
 * entries can also be connected by {@link ITimeGraphArrow}s which go from a
 * source entry at a start time to a destination entry after a certain duration.
 * These items can also display additional information in tool tips.
 *
 * Typical usage is to build a tree of {@link ITimeGraphEntryModel}s, and fetch
 * its {@link ITimeGraphRowModel} to associate the relevant line of
 * {@link ITimeGraphState}s to each entry. {@link ITimeGraphArrow}s then
 * describe additional relations from one state to another.
 * {@link ITimeGraphState#getValue()} can be used to assign a color to a state.
 *
 * @param <M>
 *            the type of {@link ITimeGraphEntryModel} that the
 *            {@link ITimeGraphDataProvider} implementations will return
 * @author Simon Delisle
 * @since 4.0
 */
public interface ITimeGraphDataProvider<M extends ITimeGraphEntryModel> extends ITmfTreeDataProvider<M>, ITimeGraphStateFilter {

    /**
     * Computes a list of time graph row models, which associate an entry's ID
     * to sampled states.
     *
     * @param filter
     *            Time graph query filter, specifies which IDs to return and the
     *            sampling rate.
     * @param monitor
     *            Progress monitor
     *
     * @return A {@link TmfModelResponse} that encapsulate a
     *         {@link ITimeGraphRowModel}
     *
     * @deprecated Use fetchRowModel with a map of parameters
     */
    @Deprecated
    TmfModelResponse<List<ITimeGraphRowModel>> fetchRowModel(SelectionTimeQueryFilter filter, @Nullable IProgressMonitor monitor);

    /**
     * Computes a list of time graph row models, which associate an entry's ID
     * to sampled states.
     *
     * @param fetchParameters
     *            Time graph query parameters, specifies which IDs to return and
     *            the sampling rate.
     * @param monitor
     *            Progress monitor
     *
     * @return A {@link TmfModelResponse} that encapsulate a
     *         {@link TimeGraphModel}
     * @since 4.3
     */
    default TmfModelResponse<TimeGraphModel> fetchRowModel(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        SelectionTimeQueryFilter filter = FetchParametersUtils.createSelectionTimeQuery(fetchParameters);
        if (filter != null) {
            TmfModelResponse<List<ITimeGraphRowModel>> response = fetchRowModel(filter, monitor);
            List<ITimeGraphRowModel> rows = response.getModel();
            if (rows != null) {
                return new TmfModelResponse<>(new TimeGraphModel(rows), response.getStatus(), response.getStatusMessage());
            }
            return new TmfModelResponse<>(null, response.getStatus(), response.getStatusMessage());
        }
        return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.INCORRECT_QUERY_PARAMETERS);
    }

    /**
     * Computes a list of time graph arrows.
     *
     * @param filter
     *            Time query filter, specifies the sampling rate.
     * @param monitor
     *            Progress monitor
     *
     * @return A {@link TmfModelResponse} that encapsulate a
     *         {@link ITimeGraphArrow}
     *
     * @deprecated Use fetchArrows with a map of parameters
     */
    @Deprecated
    TmfModelResponse<List<ITimeGraphArrow>> fetchArrows(TimeQueryFilter filter, @Nullable IProgressMonitor monitor);

    /**
     * Computes a list of time graph arrows.
     *
     * @param fetchParameters
     *            Query parameters, specifies the sampling rate.
     * @param monitor
     *            Progress monitor
     *
     * @return A {@link TmfModelResponse} that encapsulate a
     *         {@link ITimeGraphArrow}
     * @since 4.3
     */
    default TmfModelResponse<List<ITimeGraphArrow>> fetchArrows(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        TimeQueryFilter filter = FetchParametersUtils.createTimeQuery(fetchParameters);
        if (filter != null) {
            return fetchArrows(filter, monitor);
        }

        return new TmfModelResponse<>(Collections.emptyList(), ITmfResponse.Status.FAILED, CommonStatusMessage.INCORRECT_QUERY_PARAMETERS);
    }

    /**
     * Computes a tool tip for a time stamp and entry.
     *
     * @param filter
     *            Time query filter, specifies the time stamp, and item on which
     *            to give more information
     * @param monitor
     *            Progress monitor
     *
     * @return A {@link TmfModelResponse} that encapsulate a map of Tooltips
     *
     * @deprecated Use fetchTooltip with a map of parameters
     */
    @Deprecated
    TmfModelResponse<Map<String, String>> fetchTooltip(SelectionTimeQueryFilter filter, @Nullable IProgressMonitor monitor);

    /**
     * Computes a tool tip for a time stamp and entry.
     *
     * @param fetchParameters
     *            Query parameters, specifies the timestamp and item on which to
     *            give more information
     * @param monitor
     *            Progress monitor
     *
     * @return A {@link TmfModelResponse} that encapsulate a map of Tooltips
     * @since 4.3
     */
    default TmfModelResponse<Map<String, String>> fetchTooltip(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        SelectionTimeQueryFilter filter = FetchParametersUtils.createSelectionTimeQuery(fetchParameters);
        if (filter != null) {
            return fetchTooltip(filter, monitor);
        }

        return new TmfModelResponse<>(Collections.emptyMap(), ITmfResponse.Status.FAILED, CommonStatusMessage.INCORRECT_QUERY_PARAMETERS);
    }
}
