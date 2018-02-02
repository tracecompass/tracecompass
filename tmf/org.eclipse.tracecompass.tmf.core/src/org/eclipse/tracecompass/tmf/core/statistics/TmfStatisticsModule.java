/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.statistics;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfAnalysisModuleWithStateSystems;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Analysis module to compute the statistics of a trace.
 *
 * @author Alexandre Montplaisir
 */
public class TmfStatisticsModule extends TmfAbstractAnalysisModule
        implements ITmfAnalysisModuleWithStateSystems {

    /** ID of this analysis module */
    public static final @NonNull String ID = "org.eclipse.linuxtools.tmf.core.statistics.analysis"; //$NON-NLS-1$

    /** The trace's statistics */
    private ITmfStatistics fStatistics = null;
    private boolean fInitializationSucceeded;

    private final TmfStateSystemAnalysisModule totalsModule = new TmfStatisticsTotalsModule();
    private final TmfStateSystemAnalysisModule eventTypesModule = new TmfStatisticsEventTypesModule();

    private final CountDownLatch fInitialized = new CountDownLatch(1);

    /**
     * Constructor
     */
    public TmfStatisticsModule() {
        super();
    }

    /**
     * Get the statistics object built by this analysis
     *
     * @return The ITmfStatistics object
     */
    @Nullable
    public ITmfStatistics getStatistics() {
        return fStatistics;
    }

    /**
     * Wait until the analyses/state systems underneath are ready to be queried.
     * @since 2.0
     */
    @Override
    public boolean waitForInitialization() {
        try {
            fInitialized.await();
        } catch (InterruptedException e) {
            return false;
        }
        return fInitializationSucceeded;
    }

    // ------------------------------------------------------------------------
    // TmfAbstractAnalysisModule
    // ------------------------------------------------------------------------

    @Override
    public void dispose() {
        /*
         * The sub-analyses are not registered to the trace directly, so we need
         * to tell them when the trace is disposed.
         */
        super.dispose();
        totalsModule.dispose();
        eventTypesModule.dispose();
    }

    @Override
    public boolean setTrace(ITmfTrace trace) throws TmfAnalysisException {
        if (!super.setTrace(trace)) {
            return false;
        }

        /*
         * Since these sub-analyzes are not built from an extension point, we
         * have to assign the trace ourselves. Very important to do so before
         * calling schedule()!
         */
        if (!totalsModule.setTrace(trace)) {
            return false;
        }
        return eventTypesModule.setTrace(trace);
    }

    @Override
    protected boolean executeAnalysis(IProgressMonitor monitor) throws TmfAnalysisException {
        ITmfTrace trace = getTrace();
        if (trace == null) {
            /* This analysis was cancelled in the meantime */
            analysisReady(false);
            return false;
        }

        IStatus status1 = totalsModule.schedule();
        IStatus status2 = eventTypesModule.schedule();
        if (!(status1.isOK() && status2.isOK())) {
            cancelSubAnalyses();
            analysisReady(false);
            return false;
        }

        /* Wait until the two modules are initialized */
        if (!totalsModule.waitForInitialization() || !eventTypesModule.waitForInitialization()) {
            analysisReady(false);
            return false;
        }

        ITmfStateSystem totalsSS = totalsModule.getStateSystem();
        ITmfStateSystem eventTypesSS = eventTypesModule.getStateSystem();

        if (totalsSS == null || eventTypesSS == null) {
            /* This analysis was cancelled in the meantime */
            analysisReady(false);
            throw new IllegalStateException("TmfStatisticsModule : Sub-modules initialization succeeded but there is a null state system."); //$NON-NLS-1$
        }

        fStatistics = new TmfStateStatistics(totalsSS, eventTypesSS);

        /* fStatistics is now set, consider this module initialized */
        analysisReady(true);

        /*
         * The rest of this "execute" will encompass the "execute" of the two
         * sub-analyzes.
         */
        return totalsModule.waitForCompletion(monitor) &&
                eventTypesModule.waitForCompletion(monitor);
    }

    /**
     * Make the module available and set whether the initialization went well or
     * not. If not, no state system is available and
     * {@link #waitForInitialization()} should return false.
     *
     * @param success
     *            True if the initialization went well, false otherwise
     */
    private void analysisReady(boolean succeeded) {
        fInitializationSucceeded = succeeded;
        fInitialized.countDown();
    }

    @Override
    protected void canceling() {
        /*
         * FIXME The "right" way to cancel state system construction is not
         * available yet...
         */
        cancelSubAnalyses();

        ITmfStatistics stats = fStatistics;
        if (stats != null) {
            stats.dispose();
        }
    }

    private void cancelSubAnalyses() {
        totalsModule.cancel();
        eventTypesModule.cancel();
    }

    // ------------------------------------------------------------------------
    // ITmfStateSystemAnalysisModule
    // ------------------------------------------------------------------------

    @Override
    public ITmfStateSystem getStateSystem(String id) {
        switch (id) {
        case TmfStatisticsTotalsModule.ID:
            return totalsModule.getStateSystem();
        case TmfStatisticsEventTypesModule.ID:
            return eventTypesModule.getStateSystem();
        default:
            return null;
        }
    }

    @Override
    public @NonNull Iterable<@NonNull ITmfStateSystem> getStateSystems() {
        List<@NonNull ITmfStateSystem> list = new LinkedList<>();
        ITmfStateSystem totalsStateSystem = totalsModule.getStateSystem();
        if (totalsStateSystem != null) {
            list.add(totalsStateSystem);
        }
        ITmfStateSystem eventTypesStateSystem = eventTypesModule.getStateSystem();
        if (eventTypesStateSystem != null) {
            list.add(eventTypesStateSystem);
        }
        return list;
    }
}
