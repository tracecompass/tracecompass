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
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.SegmentComparators;

import com.google.common.collect.Ordering;

/**
 * Called Functuon common class, defines the start, end, depth, parent and
 * children. Does not define the symbol
 *
 * @author Matthew Khouzam
 * @author Sonia Farrah
 */
abstract class AbstractCalledFunction implements ICalledFunction {

    static final Comparator<ISegment> COMPARATOR;
    static {
        /*
         * checkNotNull() has to be called separately, or else it breaks the
         * type inference.
         */
        Comparator<ISegment> comp = Ordering.from(SegmentComparators.INTERVAL_START_COMPARATOR).compound(SegmentComparators.INTERVAL_END_COMPARATOR);
        COMPARATOR = checkNotNull(comp);
    }

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = 7992199223906717340L;

    protected final long fStart;
    protected final long fEnd;
    protected final int fDepth;
    private final List<ICalledFunction> fChildren = new ArrayList<>();
    private final @Nullable ICalledFunction fParent;
    protected long fSelfTime = 0;

    public AbstractCalledFunction(long start, long end, int depth, @Nullable ICalledFunction parent) {
        if (start > end) {
            throw new IllegalArgumentException(Messages.TimeError + "[" + start + "," + end + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        fStart = start;
        fEnd = end;
        fDepth = depth;
        fParent = parent;
        // It'll be modified once we add a child to it
        fSelfTime = fEnd - fStart;
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
    public List<ICalledFunction> getChildren() {
        return fChildren;
    }

    @Override
    public @Nullable ICalledFunction getParent() {
        return fParent;
    }

    /**
     * Add the child to the segment's children, and subtract the child's
     * duration to the duration of the segment so we can calculate its self
     * time.
     *
     * @param child
     *            The child to add to the segment's children
     */
    public void addChild(ICalledFunction child) {
        if (child.getParent() != this) {
            throw new IllegalArgumentException("Child parent not the same as child being added to."); //$NON-NLS-1$
        }
        fChildren.add(child);
        substractChildDuration(child.getEnd() - child.getStart());
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

    @Override
    public long getSelfTime() {
        return fSelfTime;
    }

    @Override
    public int getDepth() {
        return fDepth;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fDepth;
        result = prime * result + (int) (fEnd ^ (fEnd >>> 32));
        ICalledFunction parent = fParent;
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result + (int) (fSelfTime ^ (fSelfTime >>> 32));
        result = prime * result + (int) (fStart ^ (fStart >>> 32));
        result = prime * result + getSymbol().hashCode();
        return result;
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
        AbstractCalledFunction other = (AbstractCalledFunction) obj;
        if (fDepth != other.fDepth) {
            return false;
        }
        if (fEnd != other.fEnd) {
            return false;
        }
        if (fParent == null) {
            if (other.fParent != null) {
                return false;
            }
        } else if (!Objects.equals(fParent, other.fParent)) {
            return false;
        }
        if (fSelfTime != other.fSelfTime) {
            return false;
        }
        if (fStart != other.fStart) {
            return false;
        }
        if (!Objects.equals(getSymbol(), other.getSymbol())) {
            return false;
        }
        return true;
    }

}