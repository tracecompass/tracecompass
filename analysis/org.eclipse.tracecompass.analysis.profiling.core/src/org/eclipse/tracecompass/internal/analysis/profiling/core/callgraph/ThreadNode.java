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

/**
 * This class represents one thread. It's used as a root node for the aggregated
 * tree created in the CallGraphAnalysis.
 *
 * @author Sonia Farrah
 */
public class ThreadNode extends AggregatedCalledFunction {

    private final long fId;

    /**
     * @param calledFunction
     *            the called function
     * @param maxDepth
     *            The maximum depth
     * @param id
     *            The thread id
     */
    public ThreadNode(AbstractCalledFunction calledFunction, int maxDepth, long id) {
        super(calledFunction, maxDepth);
        fId = id;
    }

    /**
     * The thread id
     *
     * @return The thread id
     */
    public long getId() {
        return fId;
    }

}
