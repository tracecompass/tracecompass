/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
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

import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Interface for classes that can provide parameters to analysis when they are
 * not set manually
 *
 * @author Geneviève Bastien
 */
public interface IAnalysisParameterProvider {

    // --------------------------------------------------------
    // Getters and setters
    // --------------------------------------------------------

    /**
     * Gets the name of the parameter provider
     *
     * @return Name of the parameter provider
     */
    String getName();

    /**
     * Gets the value of a parameter
     *
     * @param name
     *            Name of the parameter
     * @return The value of a parameter
     */
    Object getParameter(String name);

    // --------------------------------------------------------
    // Functionalities
    // --------------------------------------------------------

    /**
     * Does this parameter provider apply to a given trace
     *
     * @param trace
     *            The trace to analyse
     * @return whether the parameter provider applies
     */
    boolean appliesToTrace(ITmfTrace trace);

    /**
     * Register an analysis module to be notified when a parameter value is
     * changed
     *
     * @param module
     *            The listening analysis module
     */
    void registerModule(IAnalysisModule module);

}
