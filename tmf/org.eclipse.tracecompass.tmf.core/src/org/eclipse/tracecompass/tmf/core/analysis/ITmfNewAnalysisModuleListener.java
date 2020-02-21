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

package org.eclipse.tracecompass.tmf.core.analysis;

/**
 * This is the interface class must implement to listen to new analysis module
 * objects being instantiated.
 *
 * @author Geneviève Bastien
 */
public interface ITmfNewAnalysisModuleListener {

    /**
     * Method called when an analysis module has just been instantiated.
     *
     * @param module
     *            The newly instantiated analysis module
     */
    public void moduleCreated(IAnalysisModule module);
}
