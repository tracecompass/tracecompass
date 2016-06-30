/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.core.callgraph;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.SegmentComparators;

import com.google.common.collect.Ordering;

/**
 * A Call stack function represented as an {@link ISegment}. It's used to build
 * a segments tree based on the state system. The parent represents the caller
 * of the function, and the children list represents its callees.
 *
 * @author Sonia Farrah
 */
public class CalledFunction implements ISegment {

    private static final long serialVersionUID = 7594768649825490010L;
    private static final Comparator<ISegment> COMPARATOR;
    static {
        /*
         * checkNotNull() has to be called separately, or else it breaks the
         * type inference.
         */
        Comparator<ISegment> comp = Ordering.from(SegmentComparators.INTERVAL_START_COMPARATOR).compound(SegmentComparators.INTERVAL_END_COMPARATOR);
        COMPARATOR = checkNotNull(comp);
    }

    private final long fStart;
    private final long fEnd;
    private final long fAddr;
    private final int fDepth;
    private final List<CalledFunction> fChildren = new ArrayList<>();
    @Nullable private CalledFunction fParent = null;
    private long fSelfTime = 0;

    /**
     * Create a new segment.
     *
     * The end position should be equal to or greater than the start position.
     *
     * @param start
     *            Start position of the segment
     * @param end
     *            End position of the segment
     * @param address
     *            The address of the call stack event
     * @param depth
     *            The depth in the call stack of a function
     */
    public CalledFunction(long start, long end, long address, int depth) {
        if (start > end) {
            throw new IllegalArgumentException(Messages.TimeError + "[" + start + "," + end + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        fStart = start;
        fEnd = end;
        fAddr = address;
        // It'll be modified once we add a child to it
        fSelfTime = fEnd - fStart;
        fDepth = depth;
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
    public int compareTo(@Nullable ISegment o) {
        if (o == null) {
            throw new IllegalArgumentException();
        }
        return COMPARATOR.compare(this, o);
    }

    @Override
    public String toString() {
        return new String("[" + String.valueOf(fStart) + ", " + String.valueOf(fEnd) + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * The address of the call stack event.
     *
     * @return The address
     *
     */
    public long getAddr() {
        return fAddr;
    }

    /**
     * The children of the segment
     *
     * @return The children
     *
     */
    public List<CalledFunction> getChildren() {
        return fChildren;
    }

    /**
     * The segment's parent
     *
     * @return The parent
     *
     */
    public @Nullable CalledFunction getParent() {
        return fParent;
    }

    /**
     * The segment's parent
     *
     * @param parent
     *            The parent of the segment
     *
     */
    private void setParent(CalledFunction parent) {
        fParent = parent;
    }

    /**
     * Add the child to the segment's children, and subtract the child's
     * duration to the duration of the segment so we can calculate its self
     * time.
     *
     * @param child
     *            The child to add to the segment's children
     */
    public void addChild(CalledFunction child) {
        child.setParent(this);
        fChildren.add(child);
        substractChildDuration(child.fEnd - child.fStart);
    }

    /**
     * Subtract the child's duration to the duration of the segment.
     *
     * @param childDuration
     *            The child's duration
     */
    private void substractChildDuration(long childDuration) {
        fSelfTime -= childDuration;
    }

    /**
     * The segment's self Time
     *
     * @return finalSelfTime The self time
     */
    public long getSelfTime() {
        return fSelfTime;
    }

    /**
     * The depth in the call stack of a function
     *
     * @return The depth of a function
     */
    public int getDepth() {
        return fDepth;
    }

}