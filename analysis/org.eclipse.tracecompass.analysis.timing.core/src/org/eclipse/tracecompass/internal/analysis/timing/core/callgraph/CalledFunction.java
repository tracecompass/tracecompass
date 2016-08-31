/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.core.callgraph;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.segmentstore.core.ISegment;

/**
 * A Call stack function represented as an {@link ISegment}. It's used to build
 * a segments tree based on the state system. The parent represents the caller
 * of the function, and the children list represents its callees.
 *
 * @author Sonia Farrah
 */
public class CalledFunction extends AbstractCalledFunction {

    private static final long serialVersionUID = 7594768649825490010L;

    private final Long fSymbol;

    /**
     * Create a new segment.
     *
     * The end position should be equal to or greater than the start position.
     *
     * @param start
     *            Start position of the segment
     * @param end
     *            End position of the segment
     * @param symbol
     *            The symbol of the call stack function
     * @param depth
     *            The depth in the call stack of a function
     * @param processId
     *            The process ID of the traced application
     * @param parent
     *            The caller, can be null for root elements
     */
    protected CalledFunction(long start, long end, long symbol, int depth, int processId, @Nullable ICalledFunction parent) {
        super(start, end, depth, processId, parent);
        fSymbol = symbol;
    }

    @Override
    public @NonNull Long getSymbol() {
        return fSymbol;
    }

}