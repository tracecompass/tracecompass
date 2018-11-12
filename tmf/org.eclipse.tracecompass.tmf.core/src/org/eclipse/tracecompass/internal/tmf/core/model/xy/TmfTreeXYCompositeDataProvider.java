/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.model.xy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.model.TmfXyResponseFactory;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.internal.tmf.core.model.tree.TmfTreeCompositeDataProvider;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ISeriesModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfCommonXAxisModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfXyModel;
import org.eclipse.tracecompass.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

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
 * @since 4.0
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
     * @param id
     *            the provider's ID
     */
    public TmfTreeXYCompositeDataProvider(List<P> providers, String title, String id) {
        super(providers, id);
        fTitle = title;
    }

    /**
     * Return a composite {@link ITmfTreeXYDataProvider} from a list of traces.
     *
     * @param traces
     *            A list of traces from which to generate a provider.
     * @param title
     *            Chart's title
     * @param id
     *            the provider's ID
     * @return null if the non of the traces returns a provider, the provider if the
     *         lists only return one, else a {@link TmfTreeXYCompositeDataProvider}
     *         encapsulating the providers
     */
    public static @Nullable ITmfTreeXYDataProvider<ITmfTreeDataModel> create(Collection<ITmfTrace> traces, String title, String id) {
        return create(traces, title, id, null);
    }

    /**
     * Return a composite {@link ITmfTreeXYDataProvider} from a list of traces.
     *
     * @param traces
     *            A list of traces from which to generate a provider.
     * @param title
     *            Chart's title
     * @param id
     *            the provider's ID
     * @param secondaryId
     *            The provider's secondaryId
     * @return null if the non of the traces returns a provider, the provider if the
     *         lists only return one, else a {@link TmfTreeXYCompositeDataProvider}
     *         encapsulating the providers
     */
    public static @Nullable ITmfTreeXYDataProvider<ITmfTreeDataModel> create(Collection<ITmfTrace> traces, String title, String id, @Nullable String secondaryId) {
        String providerId = secondaryId == null ? id : id + ':' + secondaryId;
        List<@NonNull ITmfTreeXYDataProvider<ITmfTreeDataModel>> providers = new ArrayList<>();
        for (ITmfTrace child : traces) {
            ITmfTreeXYDataProvider<ITmfTreeDataModel> provider = DataProviderManager.getInstance().getDataProvider(child, providerId, ITmfTreeXYDataProvider.class);
            if (provider != null) {
                providers.add(provider);
            }
        }
        if (providers.isEmpty()) {
            return null;
        } else if (providers.size() == 1) {
            return providers.get(0);
        }
        return new TmfTreeXYCompositeDataProvider<>(providers, title, providerId);
    }

    @Deprecated
    @Override
    public TmfModelResponse<ITmfXyModel> fetchXY(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        Map<String, Object> parameters = FetchParametersUtils.timeQueryToMap(filter);
        return fetchXY(parameters, monitor);
    }

    @Override
    public TmfModelResponse<ITmfXyModel> fetchXY(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        /**
         * <pre>
         * Response status according to the provider's reponse statuses:
         *
         * * Cancelled -> The monitor is cancelled
         * * Failed -> At least one provider has failed
         * * Running -> At least one of the providers is running
         * * Completed -> All providers have completed
         * </pre>
         */
        List<P> providers = getProviders();
        // Get all the responses
        Collection<TmfModelResponse<ITmfXyModel>> responses = getXyResponses(fetchParameters, monitor, providers);

        if (monitor != null && monitor.isCanceled()) {
            return TmfXyResponseFactory.createCancelledResponse(CommonStatusMessage.TASK_CANCELLED);
        }
        // If one response is failed, return a failed response with a concatenation of the messages
        String failedMsg = handleFailedStatus(responses);
        if (failedMsg != null) {
            return TmfXyResponseFactory.createFailedResponse(failedMsg);
        }

        boolean allCommon = Iterables.all(providers, ITmfCommonXAxisModel.class::isInstance);
        // The query is considered complete if all providers are completed
        boolean isComplete = Iterables.all(responses, response -> response.getStatus() == ITmfResponse.Status.COMPLETED);
        if (allCommon) {
            ImmutableMap.Builder<String, IYModel> series = ImmutableMap.builder();
            responses.forEach(response -> {
                ITmfCommonXAxisModel model = (ITmfCommonXAxisModel) response.getModel();
                if (model != null) {
                    series.putAll(model.getYData());
                }
            });
            TimeQueryFilter filter = FetchParametersUtils.createTimeQuery(fetchParameters);
            if (filter == null) {
                return TmfXyResponseFactory.createFailedResponse(CommonStatusMessage.INCORRECT_QUERY_PARAMETERS);
            }
            return TmfXyResponseFactory.create(fTitle, filter.getTimesRequested(), series.build(), isComplete);
        }
        ImmutableMap.Builder<String, ISeriesModel> series = ImmutableMap.builder();
        responses.forEach(response -> {
            ITmfXyModel model = response.getModel();
            if (model != null) {
                series.putAll(model.getData());
            }
        });
        return TmfXyResponseFactory.create(fTitle, series.build(), isComplete);
    }

    private static @Nullable String handleFailedStatus(Collection<TmfModelResponse<ITmfXyModel>> responses) {
        if (Iterables.any(responses, response -> response.getStatus() == ITmfResponse.Status.FAILED)) {
            // All requests have failed, return a concatenation of their errors
            return responses.stream().map(TmfModelResponse::getStatusMessage)
                    .collect(Collectors.joining("\n")); //$NON-NLS-1$
        }
        // At least one is good, return null
        return null;
    }

    private Collection<TmfModelResponse<ITmfXyModel>> getXyResponses(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor, List<P> providers) {
        List<TmfModelResponse<ITmfXyModel>> responses = new ArrayList<>();
        for (P dataProvider : providers) {
            TmfModelResponse<ITmfXyModel> response = dataProvider.fetchXY(fetchParameters, monitor);
            responses.add(response);

            if (monitor != null && monitor.isCanceled()) {
                return responses;
            }
        }
        return responses;
    }

}
