/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.profiling.core.callgraph;

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