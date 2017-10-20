/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.TmfCommonXAxisResponseFactory;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.TmfTreeCompositeDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;

import com.google.common.collect.ImmutableMap;

/**
 * Represents a base implementation of {@link ITmfTreeXYDataProvider} that
 * supports experiments. Clients of this data provider must provide a list of
 * {@link ITmfTreeXYDataProvider} for each trace in the experiment which
 * supports the provider. From the list of sub data provider, this data provider
 * will merge all responses into one.
 *
 * @param <M>
 *            The type of {@link ITmfTreeDataModel} that this composite's tree
 *            provider must return.
 * @param <P>
 *            The type of {@link ITmfTreeXYDataProvider} that this composite
 *            must encapsulate
 * @author Yonni Chen
 */
public class TmfTreeXYCompositeDataProvider<M extends ITmfTreeDataModel, P extends ITmfTreeXYDataProvider<M>>
        extends TmfTreeCompositeDataProvider<M, P> implements ITmfTreeXYDataProvider<M> {
    private final String fTitle;

    /**
     * Constructor
     *
     * @param providers
     *            A factory that creates a list of data provider. Each data provider
     *            should be associated to a different trace.
     * @param title
     *            Chart's title
     * @param id the provider's ID
     */
    public TmfTreeXYCompositeDataProvider(List<P> providers, String title, String id) {
        super(providers, id);
        fTitle = title;
    }

    @Override
    public TmfModelResponse<ITmfCommonXAxisModel> fetchXY(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        boolean isComplete = true;
        ImmutableMap.Builder<String, IYModel> series = ImmutableMap.builder();

        for (P dataProvider : getProviders()) {
            TmfModelResponse<ITmfCommonXAxisModel> response = dataProvider.fetchXY(filter, monitor);
            isComplete &= response.getStatus() == ITmfResponse.Status.COMPLETED;
            ITmfCommonXAxisModel model = response.getModel();
            if (model != null) {
                series.putAll(model.getYData());
            }

            if (monitor != null && monitor.isCanceled()) {
                return TmfCommonXAxisResponseFactory.createCancelledResponse(CommonStatusMessage.TASK_CANCELLED);
            }
        }
        return TmfCommonXAxisResponseFactory.create(fTitle, filter.getTimesRequested(), series.build(), isComplete); // $NON-NLS-1$
    }
}
