/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.profiling.core.callgraph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callstack.SymbolAspect;
import org.eclipse.tracecompass.segmentstore.core.ISegment;

/**
 * Called Function common class, defines the start, end, depth, parent and
 * children. Does not define the symbol
 *
 * @author Matthew Khouzam
 * @author Sonia Farrah
 */
abstract class AbstractCalledFunction implements ICalledFunction {

    static final Comparator<ISegment> COMPARATOR = Objects.requireNonNull(Comparator.comparingLong(ISegment::getStart).thenComparingLong(ISegment::getEnd));

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
    private final int fProcessId;

    public AbstractCalledFunction(long start, long end, int depth, int processId, @Nullable ICalledFunction parent) {
        if (start > end) {
            throw new IllegalArgumentException(Messages.TimeError + "[" + start + "," + end + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        fStart = start;
        fEnd = end;
        fDepth = depth;
        fParent = parent;
        // It'll be modified once we add a child to it
        fSelfTime = fEnd - fStart;
        fProcessId = processId;
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

    @Override
    public String getName() {
        return NonNullUtils.nullToEmptyString(SymbolAspect.SYMBOL_ASPECT.resolve(this));
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
        fSelfTime -= child.getLength();
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
    public int getProcessId() {
        return fProcessId;
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
        return "[" + fStart + ", " + fEnd + ']' + " Duration: " + getLength() + ", Self Time: " + fSelfTime; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    @Override
    public int hashCode() {
        return Objects.hash(fDepth, fEnd, fParent, fSelfTime, fStart, getSymbol());
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
        if (fSelfTime != other.fSelfTime) {
            return false;
        }
        if (fStart != other.fStart) {
            return false;
        }
        if (!Objects.equals(fParent, other.getParent())) {
            return false;
        }
        if (!Objects.equals(getSymbol(), other.getSymbol())) {
            return false;
        }
        return true;
    }

}