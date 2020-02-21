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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.IAnalysisRequirementProvider;
import org.eclipse.tracecompass.tmf.core.component.ITmfComponent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

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
 */
public interface IAnalysisModule extends ITmfComponent, IAnalysisRequirementProvider {

    // --------------------------------------------------------
    // Getters and setters
    // --------------------------------------------------------

    /**
     * Sets the name of the analysis module
     *
     * @param name
     *            name of the module
     */
    void setName(@NonNull String name);

    /**
     * Sets the id of the module
     *
     * @param id
     *            id of the module
     */
    void setId(@NonNull String id);

    /**
     * Gets the id of the analysis module
     *
     * @return The id of the module
     */
    @NonNull String getId();

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
     * Sets the trace on which to run the analysis and return whether the trace
     * could be successfully set
     *
     * Note: The trace cannot be final since most modules are instantiated in a
     * way that does not know about the trace, but it shouldn't be set more than
     * once since an instance of a module belongs to a trace. It is up to each
     * implementation to make sure the trace is set only once.
     *
     * @param trace
     *            The trace to run the analysis on
     * @return {@code true} if the trace was successfully set on the module,
     *         {@code false} if the analysis cannot be applied to the trace, for
     *         instance if the trace does not have the right requirements
     * @throws TmfAnalysisException
     *             This exception should be thrown if the trace is set more than
     *             once
     * @since 1.0
     */
    boolean setTrace(@NonNull ITmfTrace trace) throws TmfAnalysisException;

    /**
     * Add a parameter to this module
     *
     * @param name
     *            Name of the parameter
     */
    void addParameter(@NonNull String name);

    /**
     * Sets the value of a parameter
     *
     * @param name
     *            The name of the parameter
     * @param value
     *            The value (subclasses may type-check it)
     * @throws RuntimeException
     */
    void setParameter(@NonNull String name, @Nullable Object value);

    /**
     * Gets the value of a parameter
     *
     * @param name
     *            Name of the parameter
     * @return The value of a parameter
     */
    @Nullable Object getParameter(@NonNull String name);

    /**
     * Get the level of dependencies on other analyses that this analysis has.
     * Typically, it would be equal to the number of dependent analyses. An
     * analysis with no dependence would have a level of 0. This value can be
     * used for the dependency level of event requests.
     *
     * @return The dependency level of this analysis
     * @since 2.0
     */
    default int getDependencyLevel() {
        return 0;
    }

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
    boolean canExecute(@NonNull ITmfTrace trace);

    /**
     * Schedule the execution of the analysis. If the trace has been set and is
     * opened, the analysis will be executed right away, otherwise it should
     * scheduled for execution once all pre-conditions are satisfied.
     *
     * @return An IStatus indicating if the execution of the analysis could be
     *         scheduled successfully or not.
     */
    @NonNull IStatus schedule();

    /**
     * Gets a list of outputs
     *
     * @return The list of {@link IAnalysisOutput}
     */
    @NonNull Iterable<IAnalysisOutput> getOutputs();

    /**
     * Registers an output for this analysis
     *
     * @param output
     *            The {@link IAnalysisOutput} object
     */
    void registerOutput(@NonNull IAnalysisOutput output);

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
    boolean waitForCompletion(@NonNull IProgressMonitor monitor);

    /**
     * Return whether the analysis is ready to be queried at a given time.
     *
     * A return value of <code>false</code> means that the caller can wait and
     * this will eventually return <code>true</code>.
     *
     * Note to implementers: If the analysis is not started or completed, even
     * though the timestamp was not part of it, or cancelled, this should return
     * <code>true</code> so the caller does not end up waiting for something
     * that will never happen. Calling this method can however trigger the
     * scheduling of the analysis. In this case, it may return
     * <code>false</code> until the timestamp is covered.
     *
     * @param ts
     *            The timestamp to validate
     * @return Whether the analysis is ready to be queried at the timestamp. A
     *         value of <code>false</code> means the caller may wait until the
     *         analysis has reached the desired time.
     * @since 2.0
     */
    default boolean isQueryable(long ts) {
        return true;
    }

    /**
     * Cancels the current analysis
     */
    void cancel();

    /**
     * Makes the analysis fail with a cause
     *
     * @param cause The cause of the failure
     * @since 2.3
     */
    default void fail(@NonNull Throwable cause) {
        // Do nothing by default.
    }

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
    @NonNull String getHelpText();

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
    @NonNull String getHelpText(@NonNull ITmfTrace trace);

    /**
     * Notify the module that the value of a parameter has changed
     *
     * @param name
     *            The of the parameter that changed
     */
    void notifyParameterChanged(@NonNull String name);
}
