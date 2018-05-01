/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.model.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableList;

/**
 * Represents a base implementation of {@link ITmfTreeDataProvider} that
 * supports experiments. Clients of this data provider must provide a list of
 * {@link ITmfTreeDataProvider} for each trace in the experiment which supports
 * the provider. From the list of sub data provider, this data provider will
 * merge all responses into one.
 *
 * @param <M>
 *            The type of {@link ITmfTreeDataModel} that this composite's tree
 *            provider must return.
 * @param <P>
 *            The type of {@link ITmfTreeDataProvider} that this composite must
 *            encapsulate
 * @author Loic Prieur-Drevon
 * @since 4.0
 */
public class TmfTreeCompositeDataProvider<M extends ITmfTreeDataModel, P extends ITmfTreeDataProvider<M>> implements ITmfTreeDataProvider<M> {

    private final List<P> fProviders;
    private final String fId;

    /**
     * Return a composite {@link ITmfTreeDataProvider} from a list of traces.
     *
     * @param traces
     *            A list of traces from which to generate a provider.
     * @param id
     *            the provider's ID
     * @return null if the non of the traces returns a provider, the provider if the
     *         lists only return one, else a {@link TmfTreeCompositeDataProvider}
     *         encapsulating the providers
     */
    public static @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> create(Collection<ITmfTrace> traces, String id) {
        List<@NonNull ITmfTreeDataProvider<ITmfTreeDataModel>> providers = new ArrayList<>();
        for (ITmfTrace child : traces) {
            ITmfTreeDataProvider<ITmfTreeDataModel> provider = DataProviderManager.getInstance().getDataProvider(child, id, ITmfTreeDataProvider.class);
            if (provider != null) {
                providers.add(provider);
            }
        }
        if (providers.isEmpty()) {
            return null;
        } else if (providers.size() == 1) {
            return providers.get(0);
        }
        return new TmfTreeCompositeDataProvider<>(providers, id);
    }

    /**
     * Constructor
     *
     * @param providers
     *            A list of data providers. Each data provider should be associated
     *            to a different trace.
     * @param id
     *            the provider's ID
     */
    public TmfTreeCompositeDataProvider(List<P> providers, String id) {
        fProviders = providers;
        fId = id;
    }

    @Override
    public TmfModelResponse<List<M>> fetchTree(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        boolean isComplete = true;
        ImmutableList.Builder<M> series = ImmutableList.builder();

        for (P dataProvider : fProviders) {
            TmfModelResponse<List<M>> response = dataProvider.fetchTree(filter, monitor);
            isComplete &= response.getStatus() == ITmfResponse.Status.COMPLETED;
            List<M> model = response.getModel();
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
    public String getId() {
        return fId;
    }

    /**
     * Get the list of encapsulated providers
     *
     * @return the list of encapsulated providers
     */
    protected List<P> getProviders() {
        return fProviders;
    }

}
