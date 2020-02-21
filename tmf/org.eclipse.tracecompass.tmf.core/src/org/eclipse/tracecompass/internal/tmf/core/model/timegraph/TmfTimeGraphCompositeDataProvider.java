/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.model.timegraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.internal.tmf.core.model.tree.TmfTreeCompositeDataProvider;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.IOutputStyleProvider;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.OutputStyleModel;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphArrow;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;

import com.google.common.collect.ImmutableList;

/**
 * Represents a base implementation of {@link ITimeGraphDataProvider} that
 * supports experiments. Clients of this data provider must provide a list of
 * {@link ITimeGraphDataProvider} for each trace in the experiment which
 * supports the provider. From the list of sub data provider, this data provider
 * will merge all responses into one.
 *
 * @param <M>
 *            The type of {@link ITimeGraphEntryModel} that this composite's
 *            tree provider must return.
 * @param <P>
 *            The type of {@link ITimeGraphDataProvider} that this composite
 *            must encapsulate
 * @author Loic Prieur-Drevon
 */
public class TmfTimeGraphCompositeDataProvider<M extends ITimeGraphEntryModel, P extends ITimeGraphDataProvider<M>>
extends TmfTreeCompositeDataProvider<M, P> implements ITimeGraphDataProvider<M>, IOutputStyleProvider {

    /**
     * Constructor
     *
     * @param providers
     *            a list of data providers. Each data provider should be
     *            associated to a different trace.
     * @param id
     *            the provider's ID
     */
    public TmfTimeGraphCompositeDataProvider(List<P> providers, String id) {
        super(providers, id);
    }

    @Override
    public TmfModelResponse<TimeGraphModel> fetchRowModel(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        boolean isComplete = true;
        ImmutableList.Builder<ITimeGraphRowModel> series = ImmutableList.builder();

        for (P dataProvider : getProviders()) {
            TmfModelResponse<TimeGraphModel> response = dataProvider.fetchRowModel(fetchParameters, monitor);
            isComplete &= response.getStatus() == ITmfResponse.Status.COMPLETED;
            TimeGraphModel model = response.getModel();
            if (model != null) {
                series.addAll(model.getRows());
            }

            if (monitor != null && monitor.isCanceled()) {
                return new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
            }
        }
        if (isComplete) {
            return new TmfModelResponse<>(new TimeGraphModel(series.build()), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }
        return new TmfModelResponse<>(new TimeGraphModel(series.build()), ITmfResponse.Status.RUNNING, CommonStatusMessage.RUNNING);
    }

    @Override
    public TmfModelResponse<List<ITimeGraphArrow>> fetchArrows(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        boolean isComplete = true;
        ImmutableList.Builder<ITimeGraphArrow> series = ImmutableList.builder();

        for (P dataProvider : getProviders()) {
            TmfModelResponse<List<ITimeGraphArrow>> response = dataProvider.fetchArrows(fetchParameters, monitor);
            isComplete &= response.getStatus() == ITmfResponse.Status.COMPLETED;
            List<ITimeGraphArrow> model = response.getModel();
            if (model != null) {
                series.addAll(model);
            }

            if (monitor != null && monitor.isCanceled()) {
                return new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
            }
        }
        if (isComplete) {
            return new TmfModelResponse<>(series.build(), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }
        return new TmfModelResponse<>(series.build(), ITmfResponse.Status.RUNNING, CommonStatusMessage.RUNNING);
    }

    @Override
    public TmfModelResponse<Map<String, String>> fetchTooltip(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        for (P dataProvider : getProviders()) {
            TmfModelResponse<Map<String, String>> response = dataProvider.fetchTooltip(fetchParameters, monitor);
            Map<String, String> tooltip = response.getModel();
            if (tooltip != null) {
                return response;
            }
        }
        return new TmfModelResponse<>(null, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
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

    @Deprecated
    @Override
    public TmfModelResponse<List<ITimeGraphArrow>> fetchArrows(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        Map<String, Object> parameters = FetchParametersUtils.timeQueryToMap(filter);
        return fetchArrows(parameters, monitor);
    }

    @Deprecated
    @Override
    public TmfModelResponse<Map<String, String>> fetchTooltip(SelectionTimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        Map<String, Object> parameters = FetchParametersUtils.selectionTimeQueryToMap(filter);
        return fetchTooltip(parameters, monitor);
    }

    @Override
    public TmfModelResponse<OutputStyleModel> fetchStyle(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        Map<String, OutputElementStyle> styles = new HashMap<>();
        for (P dataProvider : getProviders()) {
            if (dataProvider instanceof IOutputStyleProvider) {
                TmfModelResponse<OutputStyleModel> response = ((IOutputStyleProvider) dataProvider).fetchStyle(fetchParameters, monitor);
                OutputStyleModel model = response.getModel();
                if (model != null) {
                    styles.putAll(model.getStyles());
                }
            }
        }
        if (styles.isEmpty()) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }
        return new TmfModelResponse<>(new OutputStyleModel(styles), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

}
