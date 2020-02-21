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

package org.eclipse.tracecompass.internal.analysis.graph.core.criticalpath;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tracecompass.analysis.graph.core.criticalpath.ICriticalPathAlgorithm;

/**
 * A manager to provide a selection of critical path algorithms
 *
 * TODO: Investigate if there is already a facility in Eclipse to replace this
 * class?
 *
 * @author Francis Giraldeau
 *
 */
public final class AlgorithmManager {

    private static final AlgorithmManager INSTANCE = new AlgorithmManager();
    private final Map<String, Class<? extends ICriticalPathAlgorithm>> fMap;
    private final Map<String, Class<? extends ICriticalPathAlgorithm>> fPublicMap;


    static {
        INSTANCE.register(CriticalPathAlgorithmBounded.class);
    }

    private AlgorithmManager() {
        fMap = new HashMap<>();
        fPublicMap = Collections.unmodifiableMap(fMap);
    }

    /**
     * Get the singleton instance
     *
     * @return the instance
     */
    public static AlgorithmManager getInstance() {
        return INSTANCE;
    }

    /**
     * Register an algorithm in the manager
     *
     * @param type
     *            the class of the algorithm to register
     */
    public void register(Class<? extends ICriticalPathAlgorithm> type) {
        fMap.put(type.getSimpleName(), type);
    }

    /**
     * Return registered algorithms
     *
     * @return an unmodifiable map of the algorithms.
     */
    public Map<String, Class<? extends ICriticalPathAlgorithm>> registeredTypes() {
        return fPublicMap;
    }

}
