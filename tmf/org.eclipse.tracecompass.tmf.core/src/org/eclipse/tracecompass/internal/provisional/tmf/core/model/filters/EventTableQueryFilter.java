/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.ITmfFilterModel;

/**
 * This represents an event table query filter used by some data providers. It
 * encapsulates a list of desired columns, the number of events and top index.
 * It's the responsibility of viewers using data provider to create an event
 * table query filter and pass it to data providers if needed.
 *
 * @author Yonni Chen
 * @since 4.0
 */
public class EventTableQueryFilter extends VirtualTableQueryFilter {

    private final @Nullable Map<Long, String> fSearchFilter;
    private final @Nullable ITmfFilterModel fFilter;

    /**
     * Constructor
     *
     * @param desiredColumns
     *            A list of desired columns ID. An empty list will return all the
     *            columns
     * @param index
     *            The index of the first desired event
     * @param count
     *            The number of desired events
     * @param filter
     *            A filter that can be applied on multiple columns
     */
    public EventTableQueryFilter(List<Long> desiredColumns, long index, int count, @Nullable ITmfFilterModel filter) {
        this(desiredColumns, index, count, filter, null);
    }

    /**
     * Constructor
     *
     * @param desiredColumns
     *            A list of desired columns ID. An empty list will return all the
     *            columns
     * @param index
     *            The index of the first desired event
     * @param count
     *            The number of desired events
     * @param filter
     *            A filter that can be applied on multiple columns
     * @param searchFilter
     *            A search filter that can be applied on multiple columns
     */
    public EventTableQueryFilter(List<Long> desiredColumns, long index, int count, @Nullable ITmfFilterModel filter, @Nullable Map<Long, String> searchFilter) {
        super(desiredColumns, index, count);
        fFilter = filter;
        fSearchFilter = searchFilter;
    }

    /**
     * Gets the {@link ITmfFilterModel} to be applied on columns
     *
     * @return The filter model
     */
    public @Nullable ITmfFilterModel getFilters() {
        return fFilter;
    }

    /**
     * Gets the search filter to be applied on columns
     *
     * @return the filter instance
     */
    public @Nullable Map<Long, String> getSearchFilter() {
        return fSearchFilter;
    }
}