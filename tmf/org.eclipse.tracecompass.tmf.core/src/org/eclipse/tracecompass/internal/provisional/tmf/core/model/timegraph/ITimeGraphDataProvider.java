/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;

/**
 * Interface that provides data for states to time. Such a model has a
 * collection of {@link ITimeGraphEntryModel}s, which can be organized into a
 * tree. Each entry can have consecutive {@link ITimeGraphState}s associated to
 * it by its unique ID. The entries can also be connected by
 * {@link ITimeGraphArrow}s which go from a source entry at a start time to a
 * destination entry after a certain duration. These items can also display
 * additional information in tool tips.
 *
 * Typical usage is to build a tree of {@link ITimeGraphEntryModel}s, and
 * associate the relevant line of {@link ITimeGraphState}s to each.
 * {@link ITimeGraphArrow}s then describe additional relations from one state to
 * another. {@link ITimeGraphState#getValue()} can be used to assign a color to
 * a state.
 *
 * @param <M>
 *            the type of {@link ITimeGraphEntryModel} that the
 *            {@link ITimeGraphDataProvider} implementations will return
 * @author Simon Delisle
 */
public interface ITimeGraphDataProvider<M extends ITimeGraphEntryModel> extends ITmfTreeDataProvider<M> {

    /**
     * Computes a list of time graph row models, which associate an entry's ID to
     * sampled states.
     *
     * @param filter
     *            Time graph query filter, specifies which IDs to return and the
     *            sampling rate.
     * @param monitor
     *            Progress monitor
     *
     * @return A {@link TmfModelResponse} that encapsulate a
     *         {@link ITimeGraphRowModel}
     */
    TmfModelResponse<List<ITimeGraphRowModel>> fetchRowModel(SelectionTimeQueryFilter filter, @Nullable IProgressMonitor monitor);

    /**
     * Computes a list of time graph arrows.
     *
     * @param filter
     *            Time query filter, specifies the sampling rate.
     * @param monitor
     *            Progress monitor
     *
     * @return A {@link TmfModelResponse} that encapsulate a {@link ITimeGraphArrow}
     */
    TmfModelResponse<List<ITimeGraphArrow>> fetchArrows(TimeQueryFilter filter, @Nullable IProgressMonitor monitor);

    /**
     * Computes a tool tip for a time stamp and entry.
     *
     * @param filter
     *            Time query filter, specifies the time stamp, and item on which to
     *            give more information
     * @param monitor
     *            Progress monitor
     *
     * @return A {@link TmfModelResponse} that encapsulate a map of Tooltips
     */
    TmfModelResponse<Map<String, String>> fetchTooltip(SelectionTimeQueryFilter filter, @Nullable IProgressMonitor monitor);
}
