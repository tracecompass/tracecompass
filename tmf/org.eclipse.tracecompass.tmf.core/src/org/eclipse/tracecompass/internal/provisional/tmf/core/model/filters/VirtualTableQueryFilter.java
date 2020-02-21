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

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.ImmutableList;

/**
 * This represents a virtual table query filter used by some data providers. It
 * encapsulates a list of desired columns, the number of events and top index.
 * It's the responsibility of viewers using data provider to create a virtual
 * table query filter and pass it to data providers if needed.
 *
 * @author Yonni Chen
 * @since 4.0
 */
@NonNullByDefault
public class VirtualTableQueryFilter {

    private final List<Long> fDesiredColumns;
    private final long fDesiredIndex;
    private final int fDesiredCount;

    /**
     * Constructor
     *
     * @param desiredColumns
     *            A list of desired columns id. An empty list will return all the
     *            columns
     * @param index
     *            The index of the first desired event
     * @param count
     *            The number of desired events
     */
    public VirtualTableQueryFilter(List<Long> desiredColumns, long index, int count) {
        if (index < 0) {
            throw new IllegalArgumentException("Index must be positive"); //$NON-NLS-1$
        }

        fDesiredColumns = ImmutableList.copyOf(desiredColumns);
        fDesiredIndex = index;
        fDesiredCount = count;
    }

    /**
     * Gets the list of desired columns id
     *
     * @return the list of desired columns id
     */
    public List<Long> getColumnsId() {
        return fDesiredColumns;
    }

    /**
     * Gets the count of desired events
     *
     * @return the count
     */
    public int getCount() {
        return fDesiredCount;
    }

    /**
     * Gets the rank of the first event desired
     *
     * @return the desired index
     */
    public long getIndex() {
        return fDesiredIndex;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        VirtualTableQueryFilter other = (VirtualTableQueryFilter) obj;
        return fDesiredColumns.equals(other.getColumnsId())
                && fDesiredIndex == other.getIndex()
                && fDesiredCount == other.getCount();
    }

    @Override
    public int hashCode() {
        return Objects.hash(fDesiredColumns, fDesiredIndex, fDesiredCount);
    }
}