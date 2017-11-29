/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;

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
 */
public class TmfTreeCompositeDataProvider<M extends ITmfTreeDataModel, P extends ITmfTreeDataProvider<M>> implements ITmfTreeDataProvider<M> {

    private final List<P> fProviders;
    private final String fId;

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
