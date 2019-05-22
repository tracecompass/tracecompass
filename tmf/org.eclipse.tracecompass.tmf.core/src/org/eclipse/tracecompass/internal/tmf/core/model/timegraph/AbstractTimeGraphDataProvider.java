/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.model.timegraph;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLogBuilder;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.internal.tmf.core.model.tree.AbstractTreeDataProvider;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphStateFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.Multimap;

/**
 * Class to abstract {@link ITimeGraphDataProvider} methods and fields. Handles
 * the exceptions that can be thrown by the concrete classes, and logs the time
 * taken to build the time graph models.
 *
 * @param <A>
 *            Generic type for the encapsulated
 *            {@link TmfStateSystemAnalysisModule}
 * @param <M>
 *            Generic type for the returned {@link ITimeGraphEntryModel}.
 * @author Loic Prieur-Drevon
 * @since 4.0
 */
public abstract class AbstractTimeGraphDataProvider<A extends TmfStateSystemAnalysisModule, M extends ITimeGraphEntryModel>
    extends AbstractTreeDataProvider<A, M> implements ITimeGraphDataProvider<M> {

    /**
     * Constructor
     *
     * @param trace
     *            the trace this provider represents
     * @param analysisModule
     *            the analysis encapsulated by this provider
     */
    public AbstractTimeGraphDataProvider(ITmfTrace trace, A analysisModule) {
        super(trace, analysisModule);
    }

    @Deprecated
    @Override
    public @NonNull TmfModelResponse<List<ITimeGraphRowModel>> fetchRowModel(SelectionTimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        @NonNull Map<@NonNull String, @NonNull Object> parameters = FetchParametersUtils.selectionTimeQueryToMap(filter);
        TmfModelResponse<@NonNull TimeGraphModel> response = fetchRowModel(parameters, monitor);
        TimeGraphModel model = response.getModel();
        List<@NonNull ITimeGraphRowModel> rows = null;
        if (model != null) {
            rows = model.getRows();
        }
        return new TmfModelResponse<>(rows, response.getStatus(), response.getStatusMessage());
    }

    @Override
    public final TmfModelResponse<TimeGraphModel> fetchRowModel(Map<String, Object> parameters, @Nullable IProgressMonitor monitor) {
        A module = getAnalysisModule();
        if (!module.waitForInitialization()) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.ANALYSIS_INITIALIZATION_FAILED);
        }

        ITmfStateSystem ss = module.getStateSystem();
        if (ss == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.STATE_SYSTEM_FAILED);
        }

        long currentEnd = ss.getCurrentEndTime();
        //TODO Converting the map to a filter to see if mandatory parameters are there. This should be handle differently.
        SelectionTimeQueryFilter filter = FetchParametersUtils.createSelectionTimeQuery(parameters);
        if (filter == null) {
            return new TmfModelResponse<>(null, Status.FAILED, CommonStatusMessage.INCORRECT_QUERY_PARAMETERS);
        }
        boolean complete = ss.waitUntilBuilt(0) || filter.getEnd() <= currentEnd;

        try (FlowScopeLog scope = new FlowScopeLogBuilder(LOGGER, Level.FINE, "AbstractTimeGraphDataProvider#fetchRowModel") //$NON-NLS-1$
                .setCategory(getClass().getSimpleName()).build()) {

            TimeGraphModel models = getRowModel(ss, parameters, monitor);
            if (models == null) {
                // getRowModel returns null if the query was cancelled.
                return new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
            }
            return new TmfModelResponse<>(models, complete ? Status.COMPLETED : Status.RUNNING,
                    complete ? CommonStatusMessage.COMPLETED : CommonStatusMessage.RUNNING);
        } catch (StateSystemDisposedException | TimeRangeException | IndexOutOfBoundsException e) {
            return new TmfModelResponse<>(null, Status.FAILED, String.valueOf(e.getMessage()));
        }
    }

    @Override
    public @NonNull Multimap<@NonNull String, @NonNull Object> getFilterData(long entryId, long time, @Nullable IProgressMonitor monitor) {
        return ITimeGraphStateFilter.mergeMultimaps(ITimeGraphDataProvider.super.getFilterData(entryId, time, monitor),
                getEntryMetadata(entryId));
    }

    /**
     * Abstract method to be implemented by the providers to return rows. Lets the
     * abstract class handle waiting for {@link ITmfStateSystem} initialization and
     * progress, as well as error handling
     *
     * @param ss
     *            the {@link TmfStateSystemAnalysisModule}'s {@link ITmfStateSystem}
     * @param parameters
     *            the query's parameters
     * @param monitor
     *            progress monitor
     * @return the list of row models, null if the query was cancelled
     * @throws StateSystemDisposedException
     *             if the state system was closed during the query or could not be
     *             queried.
     */
    protected abstract @Nullable TimeGraphModel getRowModel(ITmfStateSystem ss,
            Map<String, Object> parameters, @Nullable IProgressMonitor monitor)
            throws StateSystemDisposedException;
}
