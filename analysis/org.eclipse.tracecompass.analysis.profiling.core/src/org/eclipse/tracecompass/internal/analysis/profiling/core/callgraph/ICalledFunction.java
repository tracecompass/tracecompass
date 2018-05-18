/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.profiling.core.callgraph;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.segment.interfaces.INamedSegment;

/**
 * CalledFunction Interface
 *
 * @author Matthew Khouzam
 * @author Sonia Farrah
 */
public interface ICalledFunction extends INamedSegment {

    /**
     * The symbol of the call stack function.
     *
     * @return The symbol of the called function
     *
     */
    Object getSymbol();

    /**
     * The functions called by this function
     *
     * @return The functions called by this function, in a {@link List} form.
     *
     */
    List<ICalledFunction> getChildren();

    /**
     * The segment's parent
     *
     * @return The parent, can be null
     *
     */
    @Nullable ICalledFunction getParent();

    /**
     * The segment's self Time
     *
     * @return The self time, should always be less than or equal to
     *         {@link ISegment#getLength()}
     */
    long getSelfTime();

    /**
     * The depth in the call stack of a function
     *
     * @return The depth of a function
     */
    int getDepth();

    /**
     * The process ID of the traced application
     *
     * @return The process ID
     */
    int getProcessId();

}