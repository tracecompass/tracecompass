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

import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.osgi.framework.Bundle;

/**
 * Interface for modules helpers that provide basic module information and
 * creates module from a source when requested.
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public interface IAnalysisModuleHelper {

    // ------------------------------------
    // Getters
    // ------------------------------------

    /**
     * Gets the id of the analysis module
     *
     * @return The id of the module
     */
    String getId();

    /**
     * Gets the name of the analysis module
     *
     * @return The id of the module
     */
    String getName();

    /**
     * Gets whether the analysis should be run automatically at trace opening
     *
     * @return true if analysis is to be run automatically
     */
    boolean isAutomatic();

    /**
     * Gets a generic help message/documentation for this analysis module
     *
     * This help text will be displayed to the user and may contain information
     * on what the module does, how to use it and how to correctly generate the
     * trace to make it available
     *
     * TODO: Help texts could be quite long. They should reside in their own
     * file and be accessed either with text, for a command line man page, or
     * through the eclipse help context. There should be a custom way to make it
     * available through the helper, without instantiating the analysis, though
     * help text after analysis instantiation may be richer.
     *
     * @return The generic help text
     */
    String getHelpText();

    /**
     * Gets the icon for this module
     *
     * @return The icon path
     */
    String getIcon();

    /**
     * Gets the bundle this analysis module is part of
     *
     * @return The bundle
     */
    Bundle getBundle();

    /**
     * Does an analysis apply to a given trace type (otherwise, it is not shown)
     *
     * @param traceclass
     *            The trace to analyze
     * @return whether the analysis applies
     */
    boolean appliesToTraceType(Class<? extends ITmfTrace> traceclass);

    // ---------------------------------------
    // Functionalities
    // ---------------------------------------

    /**
     * Creates a new instance of the {@link IAnalysisModule} represented by this
     * helper and initializes it with the trace.
     *
     * @param trace
     *            The trace to be linked to the module
     * @return A new {@link IAnalysisModule} instance initialized with the
     *         trace.
     * @throws TmfAnalysisException
     *             Exceptions that occurred when setting trace
     */
    IAnalysisModule newModule(ITmfTrace trace) throws TmfAnalysisException;

}
