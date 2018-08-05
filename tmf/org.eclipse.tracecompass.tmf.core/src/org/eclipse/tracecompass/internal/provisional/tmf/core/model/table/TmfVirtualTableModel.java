/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.table;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.ImmutableList;

/**
 * Base implementation of {@link ITmfVirtualTableModel}.
 *
 * @author Yonni Chen
 * @param <L>
 *            the virtual table line type
 * @since 4.0
 */
public class TmfVirtualTableModel<L extends VirtualTableLine> implements ITmfVirtualTableModel<L> {

    private final List<Long> fColumnIds;
    private final List<L> fData;
    private final long fIndex;
    private final long fNbTotalEvents;

    /**
     * Constructor. The data passed in parameter is deep copied
     *
     * @param columnIds
     *            A list of columns ids
     *
     * @param data
     *            The data associated with the columns
     * @param index
     *            the rank of the first event
     * @param nbTotalEvents
     *            the total number of entries
     */
    public TmfVirtualTableModel(List<Long> columnIds, List<L> data, long index, long nbTotalEvents) {
        fIndex = index;
        fColumnIds = ImmutableList.copyOf(columnIds);

        fData = ImmutableList.copyOf(data);
        fNbTotalEvents = nbTotalEvents;
    }

    @Override
    public List<Long> getColumnIds() {
        return fColumnIds;
    }

    @Override
    public List<L> getData() {
        return fData;
    }

    @Override
    public long getIndex() {
        return fIndex;
    }

    @Override
    public long getNbTotalEntries() {
        return fNbTotalEvents;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fIndex, fNbTotalEvents, fColumnIds, fData);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TmfVirtualTableModel<?> other = (TmfVirtualTableModel<?>) obj;
        return fIndex == other.getIndex() &&
                fNbTotalEvents == other.getNbTotalEntries() &&
                fColumnIds.equals(other.fColumnIds) &&
                fData.equals(other.fData);
    }

    @Override
    public String toString() {
        return "Column Ids: " + fColumnIds + //$NON-NLS-1$
                ", Data: " + fData + //$NON-NLS-1$
                ", Index: " + fIndex + //$NON-NLS-1$
                ", Total nb of events: " + fNbTotalEvents; //$NON-NLS-1$
    }
}
