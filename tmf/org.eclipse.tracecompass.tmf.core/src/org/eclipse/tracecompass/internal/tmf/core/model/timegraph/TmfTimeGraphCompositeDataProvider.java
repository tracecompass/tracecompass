/**********************************************************************
 * Copyright (c) 2018, 2021 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.model.timegraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.model.tree.TmfTreeCompositeDataProvider;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.IOutputStyleProvider;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.OutputStyleModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphArrow;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

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

    /**
     * Return a composite {@link ITimeGraphDataProvider} from a list of traces.
     *
     * @param traces
     *            A list of traces from which to generate a provider.
     * @param id
     *            the provider's ID
     * @return null if the non of the traces returns a provider, the provider if the
     *         lists only return one, else a {@link TmfTimeGraphCompositeDataProvider}
     *         encapsulating the providers
     */
    public static @Nullable ITimeGraphDataProvider<ITimeGraphEntryModel> create(Collection<ITmfTrace> traces, String id) {
        return create(traces, id, null);
    }

    /**
     * Return a composite {@link ITimeGraphDataProvider} from a list of traces.
     *
     * @param traces
     *            A list of traces from which to generate a provider.
     * @param id
     *            the provider's ID
     * @param secondaryId
     *            The provider's secondaryId
     * @return null if the non of the traces returns a provider, the provider if the
     *         lists only return one, else a {@link TmfTimeGraphCompositeDataProvider}
     *         encapsulating the providers
     */
    public static @Nullable ITimeGraphDataProvider<ITimeGraphEntryModel> create(Collection<ITmfTrace> traces, String id, @Nullable String secondaryId) {
        String providerId = secondaryId == null ? id : id + ':' + secondaryId;
        List<@NonNull ITimeGraphDataProvider<ITimeGraphEntryModel>> providers = new ArrayList<>();
        for (ITmfTrace child : traces) {
            ITimeGraphDataProvider<ITimeGraphEntryModel> provider = DataProviderManager.getInstance().getDataProvider(child, providerId, ITimeGraphDataProvider.class);
            if (provider != null) {
                providers.add(provider);
            }
        }
        if (providers.isEmpty()) {
            return null;
        } else if (providers.size() == 1) {
            return providers.get(0);
        }
        return new TmfTimeGraphCompositeDataProvider<>(providers, providerId);
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
        Map<String, String> model = new LinkedHashMap<>();
        for (P dataProvider : getProviders()) {
            TmfModelResponse<Map<String, String>> response = dataProvider.fetchTooltip(fetchParameters, monitor);
            Map<String, String> tooltip = response.getModel();
            if (tooltip != null) {
                model.putAll(tooltip);
            }
        }
        return new TmfModelResponse<>(model, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
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
