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

package org.eclipse.linuxtools.tmf.core.analysis;

/**
 * This is the interface class must implement to listen to new analysis module
 * objects being instantiated.
 *
 * @author Geneviève Bastien
 * @since 3.0
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
