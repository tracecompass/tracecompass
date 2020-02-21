/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.datastore.core.interval;

import java.util.Objects;
import java.util.StringJoiner;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.datastore.core.encoding.HTVarInt;
import org.eclipse.tracecompass.datastore.core.serialization.ISafeByteBufferWriter;

/**
 * Basic implementation of {@link IHTInterval}.
 *
 * @author Geneviève Bastien
 * @since 1.1
 */
public class HTInterval implements IHTInterval {

    private final long fStart;
    private final long fDuration;

    /**
     * The object to use to read a BaseHtObject from the disk
     */
    public static final IHTIntervalReader<HTInterval> INTERVAL_READER =
        buffer -> {
            long start = buffer.getLong();
            return new HTInterval(start, start + HTVarInt.readLong(buffer));
        };


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
        fDuration = end - start;
    }

    @Override
    public long getStart() {
        return fStart;
    }

    @Override
    public long getEnd() {
        return fStart + fDuration;
    }

    @Override
    public String toString() {
        return (new StringJoiner(", ", "[", "]")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                .add(String.valueOf(fStart))
                .add(String.valueOf(fStart + fDuration))
                .toString();
    }

    @Override
    public int getSizeOnDisk() {
        return Long.BYTES + HTVarInt.getEncodedLengthLong(fDuration);
    }

    @Override
    public void writeSegment(@NonNull ISafeByteBufferWriter buffer) {
        buffer.putLong(fStart);
        HTVarInt.writeLong(buffer, fDuration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fStart, fStart + fDuration);
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
                && fDuration == other.fDuration);
    }

}
