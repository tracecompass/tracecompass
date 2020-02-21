/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.graph.core.tests.stubs;

import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;

/**
 * Base class for graph building graph test data
 *
 * @author Geneviève Bastien
 * @author Francis Giraldeau
 */
public abstract class GraphBuilder {
    private final String fName;

    /**
     * Constructor
     *
     * @param name
     *            Name of the graph builder
     */
    public GraphBuilder(String name) {
        this.fName = name;
    }

    /**
     * Get the graph builder name
     *
     * @return The graph builder name
     */
    public String getName() {
        return fName;
    }

    /**
     * Build a graph with the test data
     *
     * @return The full graph of the test case
     */
    public abstract TmfGraph build();

    /**
     * Computes the critical path with bounded algorithm
     *
     * @return The graph corresponding to the Bounded critical path algorithm
     */
    public abstract TmfGraph criticalPathBounded();

    /**
     * Computes the critical path with unbounded algorithm
     *
     * @return The graph corresponding to the result of the Unbounded critical
     *         path algorithm
     */
    public abstract TmfGraph criticalPathUnbounded();

}
