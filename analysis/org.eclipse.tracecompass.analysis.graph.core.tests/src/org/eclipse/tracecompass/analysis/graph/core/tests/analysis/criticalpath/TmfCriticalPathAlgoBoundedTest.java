/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.graph.core.tests.analysis.criticalpath;

import static org.junit.Assert.assertNotNull;

import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex;
import org.eclipse.tracecompass.analysis.graph.core.criticalpath.ICriticalPathAlgorithm;
import org.eclipse.tracecompass.analysis.graph.core.tests.stubs.GraphBuilder;
import org.eclipse.tracecompass.internal.analysis.graph.core.criticalpath.CriticalPathAlgorithmBounded;

/**
 * Test the {@link CriticalPathAlgorithmBounded} critical path algorithm
 *
 * @author Francis Giraldeau
 * @author Geneviève Bastien
 */
public class TmfCriticalPathAlgoBoundedTest extends TmfCriticalPathAlgorithmTest {

    @Override
    protected TmfGraph computeCriticalPath(TmfGraph graph, TmfVertex start) {
        assertNotNull(graph);
        ICriticalPathAlgorithm cp = new CriticalPathAlgorithmBounded(graph);
        TmfGraph bounded = cp.compute(start, null);
        return bounded;
    }

    @Override
    protected TmfGraph getExpectedCriticalPath(GraphBuilder builder) {
        return builder.criticalPathBounded();
    }

}
