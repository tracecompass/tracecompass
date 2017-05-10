/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.datastore.core.interval;

import java.util.Objects;
import java.util.StringJoiner;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.datastore.core.serialization.ISafeByteBufferWriter;

/**
 * Basic implementation of {@link IHTInterval}.
 *
 * @author Geneviève Bastien
 * @since 1.1
 */
public class HTInterval implements IHTInterval {

    private final long fStart;
    private final long fEnd;

    /**
     * The object to use to read a BaseHtObject from the disk
     */
    public static final IHTIntervalReader<HTInterval> INTERVAL_READER =
        (buffer) -> new HTInterval(buffer.getLong(), buffer.getLong());

    /**
     * Create a new segment.
     *
     * The end position should be equal to or greater than the start position.
     *
     * @param start
     *            Start position of the segment
     * @param end
     *            End position of the segment
     */
    public HTInterval(long start, long end) {
        if (end < start) {
            throw new IllegalArgumentException();
        }
        fStart = start;
        fEnd = end;
    }

    @Override
    public long getStart() {
        return fStart;
    }

    @Override
    public long getEnd() {
        return fEnd;
    }

    @Override
    public String toString() {
        return (new StringJoiner(", ", "[", "]")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                .add(String.valueOf(fStart))
                .add(String.valueOf(fEnd))
                .toString();
    }

    @Override
    public int getSizeOnDisk() {
        return 2 * Long.BYTES;
    }

    @Override
    public void writeSegment(@NonNull ISafeByteBufferWriter buffer) {
        buffer.putLong(fStart);
        buffer.putLong(fEnd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fStart, fEnd);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        HTInterval other = (HTInterval) obj;
        return (fStart == other.fStart
                && fEnd == other.fEnd);
    }

}
