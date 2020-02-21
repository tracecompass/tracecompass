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

package org.eclipse.tracecompass.analysis.graph.core.tests.analysis.criticalpath;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex;
import org.eclipse.tracecompass.analysis.graph.core.criticalpath.CriticalPathAlgorithmException;
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
        try {
            return cp.compute(start, null);
        } catch (CriticalPathAlgorithmException e) {
            fail(e.getMessage());
        }
        return null;
    }

    @Override
    protected TmfGraph getExpectedCriticalPath(GraphBuilder builder) {
        return builder.criticalPathBounded();
    }

}
