/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.IAnalysisRequirementProvider;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.osgi.framework.Bundle;

/**
 * Interface for modules helpers that provide basic module information and
 * creates module from a source when requested.
 *
 * @author Geneviève Bastien
 */
public interface IAnalysisModuleHelper extends IAnalysisRequirementProvider {

    // ------------------------------------
    // Getters
    // ------------------------------------

    /**
     * Gets the id of the analysis module
     *
     * @return The id of the module
     */
    @NonNull String getId();

    /**
     * Gets the name of the analysis module
     *
     * @return The id of the module
     */
    @NonNull String getName();

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
     * Gets a specific help message/documentation for this analysis module
     * applied on the given trace. This help message can add information on the
     * status of this analysis for a given trace, whether it can be executed or
     * not and why.
     *
     * This help text will be displayed to the user and may contain information
     * on what the module does, how to use it and how to correctly generate the
     * trace to make it available
     *
     * @param trace
     *            A trace for which to get specific help message
     * @return The generic help text
     */
    String getHelpText(@NonNull ITmfTrace trace);

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

    /**
     * Gets the list of valid trace types that the analysis can operate on.
     *
     * @return List of the trace type
     */
    Iterable<Class<? extends ITmfTrace>> getValidTraceTypes();

    /**
     * Determine if an analysis should be run on an experiment if it applies to
     * at least one of its traces. It means that an instance of the analysis
     * module will be created specifically for the experiment and the result
     * will be more than a simple aggregation of the results of each trace's
     * module. This method does not actually do the check, it just returns
     * whether it should apply.
     *
     * @return whether this analysis should be run on an experiment
     * @since 1.0
     */
    boolean appliesToExperiment();

    // ---------------------------------------
    // Functionalities
    // ---------------------------------------

    /**
     * Creates a new instance of the {@link IAnalysisModule} represented by this
     * helper and initializes it with the trace.
     *
     * After the module is fully created, this method should call
     * {@link TmfAnalysisManager#analysisModuleCreated(IAnalysisModule)} in
     * order for the new module listeners to be executed on this module.
     *
     * @param trace
     *            The trace to be linked to the module
     * @return A new {@link IAnalysisModule} instance initialized with the
     *         trace or {@code null} if the module couldn't be instantiated
     * @throws TmfAnalysisException
     *             Exceptions that occurred when setting trace
     */
    @Nullable IAnalysisModule newModule(@NonNull ITmfTrace trace) throws TmfAnalysisException;

}
