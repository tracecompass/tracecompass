/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.graph.core.criticalpath;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex;

/**
 * Interface for all critical path algorithms
 *
 * @author Francis Giraldeau
 */
public interface ICriticalPathAlgorithm {

    /**
     * Computes the critical path
     *
     * @param start
     *            The starting vertex
     * @param end
     *            The end vertex
     * @return The graph of the critical path
     * @throws CriticalPathAlgorithmException
     *             an exception in the calculation occurred
     */
    public TmfGraph compute(TmfVertex start, @Nullable TmfVertex end) throws CriticalPathAlgorithmException;

    /**
     * Unique ID of this algorithm
     *
     * @return the ID string
     */
    public String getID();

    /**
     * Human readable display name
     *
     * @return display name
     */
    public String getDisplayName();

}
