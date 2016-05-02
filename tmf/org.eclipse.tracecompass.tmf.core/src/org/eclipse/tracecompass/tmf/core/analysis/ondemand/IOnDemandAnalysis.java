/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis.ondemand;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * An on-demand analysis is an analysis run on a trace and started from an
 * explicit action from the user.
 *
 * The on-demand analysis object should be stateless, meaning every call of the
 * {@link #execute} method should be independant and not affect each other.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public interface IOnDemandAnalysis {

    /**
     * Get the displayed name of this analysis. It should usually be
     * externalized.
     *
     * @return The name of this analysis
     */
    String getName();

    /**
     * Returns whether or not this analysis is defined by a user, as opposed to
     * being built into Trace Compass (or an extension plugin).
     *
     * @return <code>true</code> if this analysis is defined by a user
     */
    boolean isUserDefined();

    /**
     * Determine if the current analysis can run on the given trace.
     *
     * If it does not apply, then it should not be suggested for the given trace
     * at all.
     *
     * This method should be a quick filter, and should not for instance call
     * external processes.
     *
     * @param trace
     *            The trace to check for
     * @return If this analysis applies to the trace
     */
    boolean appliesTo(ITmfTrace trace);

    /**
     * Second level of checking if an analysis can run on a trace.
     *
     * A analysis that {@link #appliesTo} a trace but whose {@link #canExecute}
     * returns false should be suggested to the user, albeit unavailable. For
     * example, striked-out in the UI.
     *
     * This will indicate to the user that in normal cases this analysis should
     * work, but something (trace contents, environment, etc.) is preventing it.
     *
     * @param trace
     *            The trace to check for
     * @return If this analysis can be run on this trace
     */
    boolean canExecute(ITmfTrace trace);

    /**
     * Execute the analysis on the given trace.
     *
     * It should have been ensured that the analysis can run first on this
     * trace, by calling both {@link #appliesTo} and {@link #canExecute}.
     *
     * @param trace
     *            The trace on which to execute the analysis
     * @param range
     *            The timerange on which to execute the analysis.
     *            {@link TmfTimeRange#ETERNITY} can be used to indicate the
     *            whole trace.
     * @param extraParams
     *            Extra user-defined parameters to add to the analysis's
     *            command.
     * @param monitor
     *            The progress monitor to use to display progress, if the
     *            analysis supports it. You can pass 'new NullProgressMonitor()'
     *            for a default monitor.
     * @return The results of this analysis. Exact object type is
     *         analysis-dependent, a more specific return type is encouraged.
     * @throws OnDemandAnalysisException
     *             If something went wrong with the execution, and expected
     *             results will not be returned
     */
    Object execute(ITmfTrace trace, TmfTimeRange range, String extraParams,
            IProgressMonitor monitor) throws OnDemandAnalysisException;
}
