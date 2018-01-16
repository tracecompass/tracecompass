/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.TmfTreeCompositeDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;

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
 * @since 3.3
 */
public class TmfTimeGraphCompositeDataProvider<M extends ITimeGraphEntryModel, P extends ITimeGraphDataProvider<M>>
extends TmfTreeCompositeDataProvider<M, P> implements ITimeGraphDataProvider<M> {

    public TmfTimeGraphCompositeDataProvider(List<P> providers, String id) {
        super(providers, id);
    }

    @Override
    public TmfModelResponse<List<ITimeGraphRowModel>> fetchRowModel(SelectionTimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        boolean isComplete = true;
        ImmutableList.Builder<ITimeGraphRowModel> series = ImmutableList.builder();

        for (P dataProvider : getProviders()) {
            TmfModelResponse<List<ITimeGraphRowModel>> response = dataProvider.fetchRowModel(filter, monitor);
            isComplete &= response.getStatus() == ITmfResponse.Status.COMPLETED;
            List<ITimeGraphRowModel> model = response.getModel();
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
    public TmfModelResponse<List<ITimeGraphArrow>> fetchArrows(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        boolean isComplete = true;
        ImmutableList.Builder<ITimeGraphArrow> series = ImmutableList.builder();

        for (P dataProvider : getProviders()) {
            TmfModelResponse<List<ITimeGraphArrow>> response = dataProvider.fetchArrows(filter, monitor);
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
    public TmfModelResponse<Map<String, String>> fetchTooltip(SelectionTimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        for (P dataProvider : getProviders()) {
            TmfModelResponse<Map<String, String>> response = dataProvider.fetchTooltip(filter, monitor);
            Map<String, String> tooltip = response.getModel();
            if (tooltip != null) {
                return response;
            }
        }
        return new TmfModelResponse<>(null, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

}
