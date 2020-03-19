/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.timegraph;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
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
     * @param fetchParameters
     *            Time graph query parameters, specifies which IDs to return and
     *            the sampling rate.
     * @param monitor
     *            Progress monitor
     *
     * @return A {@link TmfModelResponse} that encapsulate a
     *         {@link TimeGraphModel}
     * @since 5.0
     */
    TmfModelResponse<TimeGraphModel> fetchRowModel(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor);

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
     * @since 5.0
     */
    TmfModelResponse<List<ITimeGraphArrow>> fetchArrows(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor);

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
     * @since 5.0
     */
    TmfModelResponse<Map<String, String>> fetchTooltip(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor);
}
