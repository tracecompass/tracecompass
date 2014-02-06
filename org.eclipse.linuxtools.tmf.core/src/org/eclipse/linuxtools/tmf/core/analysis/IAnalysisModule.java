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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.linuxtools.tmf.core.component.ITmfComponent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Interface that hooks analysis modules to the rest of TMF. Analysis modules
 * are a set of operations to be run on a trace (or experiment). They will
 * typically either provide outputs to the end user, or feed other analysis.
 *
 * An analysis module must tell what trace type it applies to and if it can be
 * executed on a given trace of the right type.
 *
 * Implementations of this interface must define how an analysis will be
 * executed once scheduled and provide help texts to describe how to use the
 * analysis.
 *
 * Analysis can also take parameters, manually set, through default values or
 * using an {@link IAnalysisParameterProvider}. {@link IAnalysisOutput} can also
 * be registered to an analysis modules to display the results of the analysis.
 *
 * This interface just allows to hook the analysis to the TMF framework, but the
 * developer is free to implement the internals of its operations the way he
 * wishes.
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public interface IAnalysisModule extends ITmfComponent {

    // --------------------------------------------------------
    // Getters and setters
    // --------------------------------------------------------

    /**
     * Sets the name of the analysis module
     *
     * @param name
     *            name of the module
     */
    void setName(String name);

    /**
     * Sets the id of the module
     *
     * @param id
     *            id of the module
     */
    void setId(String id);

    /**
     * Gets the id of the analysis module
     *
     * @return The id of the module
     */
    String getId();

    /**
     * Sets whether this analysis should be run automatically at trace opening
     *
     * @param auto
     *            True if analysis should be run automatically for a trace
     */
    void setAutomatic(boolean auto);

    /**
     * Gets whether the analysis should be run automatically at trace opening
     *
     * @return true if analysis is to be run automatically
     */
    boolean isAutomatic();

    /**
     * Sets the trace on which to run the analysis
     *
     * Note: The trace cannot be final since most modules are instantiated in a
     * way that does not know about the trace, but it shouldn't be set more than
     * once since an instance of a module belongs to a trace. It is up to each
     * implementation to make sure the trace is set only once.
     *
     * @param trace
     *            The trace to run the analysis on
     * @throws TmfAnalysisException
     */
    void setTrace(ITmfTrace trace) throws TmfAnalysisException;

    /**
     * Add a parameter to this module
     *
     * @param name
     *            Name of the parameter
     */
    void addParameter(String name);

    /**
     * Sets the value of a parameter
     *
     * @param name
     *            The name of the parameter
     * @param value
     *            The value (subclasses may type-check it)
     * @throws RuntimeException
     */
    void setParameter(String name, Object value);

    /**
     * Gets the value of a parameter
     *
     * @param name
     *            Name of the parameter
     * @return The value of a parameter
     */
    Object getParameter(String name);

    // -----------------------------------------------------
    // Functionalities
    // -----------------------------------------------------

    /**
     * Can an analysis be executed on a given trace (otherwise, it is shown
     * grayed out and a help message is available to see why it is not
     * applicable)
     *
     * @param trace
     *            The trace to analyze
     * @return Whether the analysis can be executed
     */
    boolean canExecute(ITmfTrace trace);

    /**
     * Schedule the execution of the analysis. If the trace has been set and is
     * opened, the analysis will be executed right away, otherwise it should
     * scheduled for execution once all pre-conditions are satisfied.
     *
     * @return An IStatus indicating if the execution of the analysis could be
     *         scheduled successfully or not.
     */
    IStatus schedule();

    /**
     * Gets a list of outputs
     *
     * @return The list of {@link IAnalysisOutput}
     */
    Iterable<IAnalysisOutput> getOutputs();

    /**
     * Registers an output for this analysis
     *
     * @param output
     *            The {@link IAnalysisOutput} object
     */
    void registerOutput(IAnalysisOutput output);

    /**
     * Block the calling thread until this analysis has completed (or has been
     * cancelled).
     *
     * @return True if the analysis finished successfully, false if it was
     *         cancelled.
     */
    boolean waitForCompletion();

    /**
     * Typically the output of an analysis will be available only after it is
     * completed. This method allows to wait until an analysis has been
     * completed or the analysis has been cancelled
     *
     * To avoid UI freezes, it should not be called from the main thread of the
     * application
     *
     * @param monitor
     *            The progress monitor to check for cancellation
     * @return If the analysis was successfully completed. If false is returned,
     *         this either means there was a problem during the analysis, or it
     *         got cancelled before it could finished or it has not been
     *         scheduled to run at all. In all cases, the quality or
     *         availability of the output(s) and results is not guaranteed.
     */
    boolean waitForCompletion(IProgressMonitor monitor);

    /**
     * Cancels the current analysis
     */
    void cancel();

    // -----------------------------------------------------
    // Utilities
    // -----------------------------------------------------

    /**
     * Gets a generic help message/documentation for this analysis module
     *
     * This help text will be displayed to the user and may contain information
     * on what the module does, how to use it and how to correctly generate the
     * trace to make it available
     *
     * TODO: Help texts could be quite long. They should reside in their own
     * file and be accessed either with text, for a command line man page, or
     * through the eclipse help context.
     *
     * @return The generic help text
     */
    String getHelpText();

    /**
     * Gets a help text specific for a given trace
     *
     * For instance, it may explain why the analysis module cannot be executed
     * on a trace and how to correct this
     *
     * @param trace
     *            The trace to analyze
     * @return A help text with information on a specific trace
     */
    String getHelpText(ITmfTrace trace);

    /**
     * Notify the module that the value of a parameter has changed
     *
     * @param name
     *            The of the parameter that changed
     */
    void notifyParameterChanged(String name);

}
