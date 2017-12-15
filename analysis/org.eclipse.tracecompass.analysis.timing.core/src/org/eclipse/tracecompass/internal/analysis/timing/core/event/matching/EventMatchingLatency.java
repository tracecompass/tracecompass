/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.core.event.matching;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.datastore.core.interval.IHTIntervalReader;
import org.eclipse.tracecompass.datastore.core.serialization.ISafeByteBufferWriter;
import org.eclipse.tracecompass.datastore.core.serialization.SafeByteBufferFactory;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.segment.interfaces.INamedSegment;
import org.eclipse.tracecompass.tmf.core.event.matching.IEventMatchingKey;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventDependency;

/**
 * A segment representing the latency between dependent events. This segment can
 * have negative values if the cause of a dependency happens after its effect,
 * for example in multi-trace scenarios.
 *
 * @author Geneviève Bastien
 */
public class EventMatchingLatency implements INamedSegment {

    /**
     * The factory to read an object from a buffer
     */
    public static final IHTIntervalReader<ISegment> MATCHING_LATENCY_READ_FACTORY = buffer -> {
        return new EventMatchingLatency(buffer.getLong(), buffer.getLong(), buffer.get() >= 0, buffer.getString());
    };
    private static final String DEFAULT_TYPE = ""; //$NON-NLS-1$
    private static final String TRACE_SEPARATOR = ", "; //$NON-NLS-1$

    private final long fStart;
    private final long fEnd;
    private final boolean fReverse;
    private final String fType;

    /**
     * Generated UID
     */
    private static final long serialVersionUID = 5048079637591761143L;

    /**
     * Constructor
     *
     * @param start
     *            The start of the latency
     * @param end
     *            The end of the latency
     * @param reverse
     *            If <code>true</code>, this latency is in the reverse direction, ie
     *            from end to start.
     * @param type
     *            The type of this matching
     */
    public EventMatchingLatency(long start, long end, boolean reverse, String type) {
        fStart = start;
        fEnd = end;
        fReverse = reverse;
        fType = type;
    }

    /**
     * Constructor
     *
     * @param eventKey
     *            The event key used for this match. The name of the class will be
     *            used as the type of match, to differentiate between match types
     *            where multiple are possible between traces
     * @param match
     *            An event dependency from which to create this latency
     */
    public EventMatchingLatency(@Nullable IEventMatchingKey eventKey, TmfEventDependency match) {
        long start = match.getSource().getTimestamp().toNanos();
        long end = match.getDestination().getTimestamp().toNanos();
        if (start <= end) {
            fStart = start;
            fEnd = end;
            fReverse = false;
        } else {
            fStart = end;
            fEnd = start;
            fReverse = true;
        }
        String srcTrace = match.getSource().getTrace().getName();
        String dstTrace = match.getDestination().getTrace().getName();
        String matching = (srcTrace.equals(dstTrace)) ? srcTrace : srcTrace + TRACE_SEPARATOR + dstTrace;
        String type = eventKey == null ? DEFAULT_TYPE : eventKey.getClass().getSimpleName() + ':';
        fType = type + matching;
    }

    @Override
    public long getStart() {
        return fReverse ? fEnd : fStart;
    }

    @Override
    public long getEnd() {
        return fReverse ? fStart : fEnd;
    }

    @Override
    public int getSizeOnDisk() {
        // Save the start and end time
        return Long.BYTES * 2 + Byte.BYTES + SafeByteBufferFactory.getStringSizeInBuffer(fType);
    }

    @Override
    public String getName() {
        return fType;
    }

    @Override
    public void writeSegment(@NonNull ISafeByteBufferWriter buffer) {
        buffer.putLong(fStart);
        buffer.putLong(fEnd);
        buffer.put(fReverse ? (byte) 1 : (byte) -1);
        buffer.putString(fType);
    }

    @Override
    public String toString() {
        return new String(fType + ':' + '[' + String.valueOf(fStart) + ", " + String.valueOf(fEnd) + ']'); //$NON-NLS-1$
    }

}
