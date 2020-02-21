/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.synchronization;

import org.eclipse.tracecompass.internal.tmf.core.synchronization.SyncAlgorithmFullyIncremental;

/**
 * A factory to generate synchronization algorithm to synchronize traces
 *
 * @author Geneviève Bastien
 */
public final class SynchronizationAlgorithmFactory {

    private SynchronizationAlgorithmFactory() {

    }

    /**
     * Returns the system's default trace synchronization algorithm, ie the
     * fully incremental convex hull synchronization algorithm.
     *
     * @return The default trace synchronization algorithm
     */
    public static SynchronizationAlgorithm getDefaultAlgorithm() {
        return new SyncAlgorithmFullyIncremental();
    }

    /**
     * Returns the class implementing the fully incremental convex hull trace
     * synchronization approach as described in
     *
     * Masoume Jabbarifar, Michel Dagenais and Alireza Shameli-Sendi,
     * "Streaming Mode Incremental Clock Synchronization"
     *
     * @return The {@link SynchronizationAlgorithm} implementing the fully
     *         incremental convex hull synchronization algorithm
     */
    public static SynchronizationAlgorithm getFullyIncrementalAlgorithm() {
        return new SyncAlgorithmFullyIncremental();
    }
}
