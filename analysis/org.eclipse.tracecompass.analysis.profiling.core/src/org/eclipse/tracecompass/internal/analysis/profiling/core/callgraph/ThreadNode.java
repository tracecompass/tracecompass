/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
