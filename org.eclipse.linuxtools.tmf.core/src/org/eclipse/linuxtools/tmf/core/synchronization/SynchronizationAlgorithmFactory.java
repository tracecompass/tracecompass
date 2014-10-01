/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.synchronization;

import org.eclipse.linuxtools.internal.tmf.core.synchronization.SyncAlgorithmFullyIncremental;

/**
 * A factory to generate synchronization algorithm to synchronize traces
 *
 * @author Geneviève Bastien
 * @since 3.2
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
