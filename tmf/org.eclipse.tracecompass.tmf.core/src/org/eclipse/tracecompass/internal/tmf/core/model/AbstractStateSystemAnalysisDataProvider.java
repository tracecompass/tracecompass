/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.model;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfXyModel;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Every data provider that used a TmfStateSystemAnalysisModule should extends
 * this class. The purpose of this class is to provide a common method to verify
 * parameters of fetch method before computing the model. It's intended only to
 * limit code duplication of verification in fetch method of concrete data
 * providers
 *
 * @author Yonni Chen
 * @since 4.0
 */
public abstract class AbstractStateSystemAnalysisDataProvider extends AbstractTmfTraceDataProvider {

    /**
     * Constructor
     *
     * @param trace
     *            A trace in which we are interested to compute a model
     */
    public AbstractStateSystemAnalysisDataProvider(ITmfTrace trace) {
        super(trace);
    }

    /**
     * This method is common to any data provider using a
     * TmfStateSystemAnalysisModule. We simply verify that parameters passed in
     * fetch method of data providers are correct before executing fetch method.
     * This method is intended to limit duplication accross all data providers using
     * TmfStateSystemAnalysisModule
     *
     * @param module
     *            A TmfStateSystemAnalysisModule. We are interested to know if
     *            initialization succeed
     * @param filter
     *            Contains the requested X values.
     * @param monitor
     *            A ProgressMonitor. We are interested if task was cancelled before
     *            proceding to request
     * @return A {@link TmfModelResponse} if something went wrong. Null if
     *         everything is fine
     */
    protected @Nullable TmfModelResponse<ITmfXyModel> verifyParameters(TmfStateSystemAnalysisModule module, TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        if (!module.waitForInitialization()) {
            return TmfXyResponseFactory.createFailedResponse(CommonStatusMessage.ANALYSIS_INITIALIZATION_FAILED);
        }
        if (monitor != null && monitor.isCanceled()) {
            return TmfXyResponseFactory.createCancelledResponse(CommonStatusMessage.TASK_CANCELLED);
        }
        ITmfStateSystem ss = module.getStateSystem();
        if (ss == null) {
            return TmfXyResponseFactory.createFailedResponse(CommonStatusMessage.STATE_SYSTEM_FAILED);
        }

        long realStart = Math.max(ss.getStartTime(), filter.getStart());
        if (realStart >= filter.getEnd()) {
            return TmfXyResponseFactory.createEmptyResponse(CommonStatusMessage.INCORRECT_QUERY_INTERVAL);
        }
        return null;
    }
}
