/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.memory;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.datastore.core.interval.IHTIntervalReader;
import org.eclipse.tracecompass.datastore.core.serialization.ISafeByteBufferWriter;
import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.segment.interfaces.INamedSegment;

/**
 * A segment representing an allocated memory pointer that was not deallocated
 * during the trace time. The start time would be the time of allocation and the
 * end time is typically the end time of the trace.
 *
 * @author Geneviève Bastien
 */
public class PotentialLeakSegment extends BasicSegment implements INamedSegment {

    /**
     * The factory to read this segment from a buffer
     */
    public static final @NonNull IHTIntervalReader<@NonNull ISegment> MEMORY_SEGMENT_READ_FACTORY = buffer -> {
        return new PotentialLeakSegment(buffer.getLong(), buffer.getLong(), buffer.getLong());
    };

    /**
     * Generated serial ID
     */
    private static final long serialVersionUID = 4906743387056014569L;

    private final Long fTid;

    /**
     * Constructor
     *
     * @param start
     *            The start time of the segment
     * @param end
     *            The end time
     * @param tid
     *            The ID of the thread doing the allocation
     */
    public PotentialLeakSegment(long start, long end, Long tid) {
        super(start, end);
        fTid = tid;
    }

    @Override
    public int getSizeOnDisk() {
        return Long.BYTES * 3;
    }

    @Override
    public void writeSegment(@NonNull ISafeByteBufferWriter buffer) {
        buffer.putLong(getStart());
        buffer.putLong(getEnd());
        buffer.putLong(fTid);
    }

    /**
     * Get the tid of this segment
     *
     * @return The thread ID
     */
    public @NonNull Integer getTid() {
        return fTid.intValue();
    }

    @Override
    public @NonNull String getName() {
        return String.valueOf(getTid());
    }

}
