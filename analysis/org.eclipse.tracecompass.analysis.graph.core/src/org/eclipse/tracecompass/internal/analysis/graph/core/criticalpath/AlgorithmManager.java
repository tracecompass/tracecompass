/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.graph.core.criticalpath;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.graph.core.criticalpath.ICriticalPathAlgorithm;

/**
 * Register algorithm
 *
 * FIXME: is there already a facility in Eclipse to replace this class?
 * @author Francis Giraldeau
 *
 */
public final class AlgorithmManager {

    private static @Nullable AlgorithmManager INSTANCE;
    private final Map<String, Class<? extends ICriticalPathAlgorithm>> map;

    private AlgorithmManager() {
        map = new HashMap<>();
    }

    /**
     * Get the singleton instance
     *
     * @return the instance
     */
    public static AlgorithmManager getInstance() {
        AlgorithmManager manager = INSTANCE;
        if (manager == null) {
            manager = new AlgorithmManager();
            manager.register(CriticalPathAlgorithmBounded.class);
            INSTANCE = manager;
        }
        return manager;
    }

    /**
     * Register a type in the manager
     *
     * @param type the class to register
     */
    public void register(Class<? extends ICriticalPathAlgorithm> type) {
        map.put(type.getSimpleName(), type);
    }

    /**
     * Return registered types
     * @return the types
     */
    public Map<String, Class<? extends ICriticalPathAlgorithm>> registeredTypes() {
        return map;
    }

}
