/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.datastore.core.encoding.HTVarInt;
import org.eclipse.tracecompass.datastore.core.interval.IHTIntervalReader;
import org.eclipse.tracecompass.datastore.core.serialization.ISafeByteBufferWriter;

/**
 * Basic implementation of {@link ISegment}.
 *
 * @author Alexandre Montplaisir
 */
public class BasicSegment implements ISegment {

    /**
     * The factory to read an object from a buffer
     * @since 2.0
     */
    public static final IHTIntervalReader<BasicSegment> BASIC_SEGMENT_READ_FACTORY = buffer -> {
            long start = buffer.getLong();
            return new BasicSegment(start, start + HTVarInt.readLong(buffer));
    };

    private static final long serialVersionUID = -3257452887960883177L;

    private final long fStart;
    private final long fDuration;

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
    public BasicSegment(long start, long end) {
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

    /**
     * @since 2.0
     */
    @Override
    public int getSizeOnDisk() {
        // Save the start and end time
        return HTVarInt.getEncodedLengthLong(fDuration) + Long.BYTES;
    }

    /**
     * @since 2.0
     */
    @Override
    public void writeSegment(@NonNull ISafeByteBufferWriter buffer) {
        buffer.putLong(getStart());
        HTVarInt.writeLong(buffer, fDuration);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append('[');
        sb.append(fStart);
        sb.append(", "); //$NON-NLS-1$
        sb.append(fStart + fDuration);
        sb.append(']');

        return sb.toString();
    }

}
