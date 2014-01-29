/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.statistics;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfAnalysisModuleWithStateSystems;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.linuxtools.tmf.core.statistics.ITmfStatistics;
import org.eclipse.linuxtools.tmf.core.statistics.TmfStateStatistics;
import org.eclipse.linuxtools.tmf.core.statistics.TmfStatisticsEventTypesModule;
import org.eclipse.linuxtools.tmf.core.statistics.TmfStatisticsTotalsModule;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.analysis.TmfAnalysisViewOutput;

/**
 * Analysis module to compute the statistics of a trace.
 *
 * @author Alexandre Montplaisir
 * @since 3.0
 */
public class TmfStatisticsModule extends TmfAbstractAnalysisModule
        implements ITmfAnalysisModuleWithStateSystems {

    /** ID of this analysis module */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.statistics.analysis"; //$NON-NLS-1$

    /** The trace's statistics */
    private ITmfStatistics fStatistics = null;

    private final TmfStateSystemAnalysisModule totalsModule = new TmfStatisticsTotalsModule();
    private final TmfStateSystemAnalysisModule eventTypesModule = new TmfStatisticsEventTypesModule();

    private final CountDownLatch fInitialized = new CountDownLatch(1);

    /**
     * Constructor
     */
    public TmfStatisticsModule() {
        super();
        this.registerOutput(new TmfAnalysisViewOutput(TmfStatisticsView.ID));
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
     */
    public void waitForInitialization() {
        try {
            fInitialized.await();
        } catch (InterruptedException e) {}
    }

    // ------------------------------------------------------------------------
    // TmfAbstractAnalysisModule
    // ------------------------------------------------------------------------

    @Override
    protected boolean executeAnalysis(IProgressMonitor monitor) throws TmfAnalysisException {
        ITmfTrace trace = getTrace();
        if (trace == null) {
            /* This analysis's trace should not be null when this is called */
            throw new IllegalStateException();
        }

        /*
         * Since these sub-analyzes are not built from an extension point, we
         * have to assign the trace ourselves. Very important to do so before
         * calling schedule()!
         */
        totalsModule.setTrace(trace);
        eventTypesModule.setTrace(trace);

        IStatus status1 = totalsModule.schedule();
        IStatus status2 = eventTypesModule.schedule();
        if (!status1.isOK() || !status2.isOK()) {
            return false;
        }

        /* Wait until the two modules are initialized */
        totalsModule.waitForInitialization();
        eventTypesModule.waitForInitialization();

        ITmfStateSystem totalsSS = totalsModule.getStateSystem();
        ITmfStateSystem eventTypesSS = eventTypesModule.getStateSystem();

        if (totalsSS == null || eventTypesSS == null) {
            /* Better safe than sorry... */
            throw new IllegalStateException();
        }

        fStatistics = new TmfStateStatistics(totalsSS, eventTypesSS);

        /* fStatistics is now set, consider this module initialized */
        fInitialized.countDown();

        /*
         * The rest of this "execute" will encompass the "execute" of the two
         * sub-analyzes.
         */
        if (!totalsModule.waitForCompletion(monitor) ||
                !eventTypesModule.waitForCompletion(monitor)) {
            return false;
        }
        return true;
    }

    @Override
    protected void canceling() {
        /*
         * FIXME The "right" way to cancel state system construction is not
         * available yet...
         */
        totalsModule.cancel();
        eventTypesModule.cancel();

        ITmfStatistics stats = fStatistics;
        if (stats != null) {
            stats.dispose();
        }
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
    public String getStateSystemId(ITmfStateSystem ss) {
        if (ss.equals(totalsModule.getStateSystem())) {
            return TmfStatisticsTotalsModule.ID;
        } else if (ss.equals(eventTypesModule.getStateSystem())){
            return TmfStatisticsEventTypesModule.ID;
        }
        return null;
    }

    @Override
    public Iterable<ITmfStateSystem> getStateSystems() {
        List<ITmfStateSystem> list = new LinkedList<>();
        list.add(totalsModule.getStateSystem());
        list.add(eventTypesModule.getStateSystem());
        return list;
    }
}
