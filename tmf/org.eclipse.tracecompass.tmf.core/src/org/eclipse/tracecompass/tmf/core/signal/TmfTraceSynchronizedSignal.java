/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.signal;

import org.eclipse.tracecompass.tmf.core.synchronization.SynchronizationAlgorithm;

/**
 * Signal indicating a trace synchronization has been done
 *
 * @author Geneviève Bastien
 */
public class TmfTraceSynchronizedSignal extends TmfSignal {

    private final SynchronizationAlgorithm fAlgoSync;

    /**
     * Constructor
     *
     * @param source
     *            Object sending this signal
     * @param algoSync
     *            The synchronization algorithm used
     */
    public TmfTraceSynchronizedSignal(Object source, SynchronizationAlgorithm algoSync) {
        super(source);
        fAlgoSync = algoSync;
    }

    /**
     * Synchronization algorithm getter
     *
     * @return The algorithm object
     */
    public SynchronizationAlgorithm getSyncAlgo() {
        return fAlgoSync;
    }

    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + " (" + fAlgoSync.toString() + ")]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
