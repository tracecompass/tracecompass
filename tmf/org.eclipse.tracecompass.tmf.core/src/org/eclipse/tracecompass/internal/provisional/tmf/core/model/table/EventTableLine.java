/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.table;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;

/**
 * Event table line model.
 *
 * @author Simon Delisle
 * @since 4.0
 */
public class EventTableLine extends VirtualTableLine {

    private final ITmfTimestamp fTimestamp;
    private final long fRank;
    private final long fRepeatCount;

    /**
     * Constructor.
     *
     * @param data
     *            The line data
     * @param index
     *            Index of this event
     * @param timestamp
     *            Timestamp associated to this line
     * @param rank
     *            Rank in the trace associated to this line
     * @param repeatCount
     *            Number of times this line is repeated
     */
    public EventTableLine(List<VirtualTableCell> data, long index, ITmfTimestamp timestamp, long rank, long repeatCount) {
        super(index, data);
        fTimestamp = timestamp;
        fRank = rank;
        fRepeatCount = repeatCount;
    }

    /**
     * Get the timestamp associated to this line.
     *
     * @return The timestamp
     */
    public ITmfTimestamp getTimestamp() {
        return fTimestamp;
    }

    /**
     * Get the rank in the trace associated to this line
     *
     * @return The rank in the trace
     */
    public long getRank() {
        return fRank;
    }

    /**
     * Number of times this line is repeated
     *
     * @return The repeat count
     */
    public long getRepeatCount() {
        return fRepeatCount;
    }

    @Override
    public String toString() {
        return "Index: " + getIndex() + //$NON-NLS-1$
                ", Line: " + getCells() + //$NON-NLS-1$
                ", Timestamp: " + fTimestamp + //$NON-NLS-1$
                ", Rank: " + fRank + //$NON-NLS-1$
                ", RepeatCount: " + fRepeatCount; //$NON-NLS-1$
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!super.equals(obj)) {
            return false;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        EventTableLine other = (EventTableLine) obj;
        return fTimestamp.equals(other.getTimestamp()) &&
                fRank == other.getRank() &&
                fRepeatCount == other.fRepeatCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fTimestamp, fRank, fRepeatCount, getIndex(), getCells());
    }
}