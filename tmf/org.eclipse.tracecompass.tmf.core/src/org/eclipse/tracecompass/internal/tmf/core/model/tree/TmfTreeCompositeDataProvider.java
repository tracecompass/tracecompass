/**********************************************************************
 * Copyright (c) 2017, 2021 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.model.tree;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.AnnotationCategoriesModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.AnnotationModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.IOutputAnnotationProvider;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.ITableColumnDescriptor;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

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
public class TmfTreeCompositeDataProvider<M extends ITmfTreeDataModel, P extends ITmfTreeDataProvider<M>> implements ITmfTreeDataProvider<M>, IOutputAnnotationProvider {

    private final CopyOnWriteArrayList<P> fProviders = new CopyOnWriteArrayList<>();
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
        fProviders.addAll(providers);
        fId = id;
    }

    @Override
    public TmfModelResponse<TmfTreeModel<M>> fetchTree(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        boolean isComplete = true;
        List<Entry<M, Object>> entries = new ArrayList<>();
        List<ITableColumnDescriptor> columnDescriptor = null;

        Table<Object, Long, @NonNull M> scopedEntries = HashBasedTable.create();
        for (P dataProvider : fProviders) {
            Map<Long, AtomicInteger> indexMap = new HashMap<>();
            TmfModelResponse<TmfTreeModel<M>> response = dataProvider.fetchTree(fetchParameters, monitor);
            isComplete &= response.getStatus() == ITmfResponse.Status.COMPLETED;
            TmfTreeModel<M> model = response.getModel();
            if (model != null) {
                Object scope = (model.getScope() == null) ? dataProvider : model.getScope();
                Map<Long, @NonNull M> row = scopedEntries.row(scope);
                for (M entry : model.getEntries()) {
                    M previous = row.putIfAbsent(entry.getId(), entry);
                    // Ignore duplicate entries from different data providers
                    if (previous == null) {
                        if (entry.getParentId() == -1) {
                            entries.add(new SimpleEntry(entry, scope));
                        } else {
                            /*
                             * Insert new entries from subsequent data providers
                             * at the correct position in the entries list. New
                             * entries are inserted before sibling entries from
                             * previous data providers.
                             */
                            int index = indexMap.computeIfAbsent(entry.getParentId(), l -> new AtomicInteger()).getAndIncrement();
                            int pos = 0;
                            while (pos < entries.size()) {
                                Entry<M, Object> added = entries.get(pos);
                                if (added.getValue().equals(scope) && added.getKey().getParentId() == entry.getParentId()) {
                                    if (index == 0) {
                                        break;
                                    }
                                    index--;
                                }
                                pos++;
                            }
                            if (pos < entries.size()) {
                                entries.add(pos, new SimpleEntry(entry, scope));
                            } else {
                                entries.add(new SimpleEntry(entry, scope));
                            }
                        }
                    } else {
                        indexMap.computeIfAbsent(entry.getParentId(), l -> new AtomicInteger()).getAndIncrement();
                    }
                }
                // Use the column descriptor of the first model. All descriptors are supposed to be the same
                if (columnDescriptor == null) {
                    columnDescriptor = model.getColumnDescriptors();
                }
            }
            if (monitor != null && monitor.isCanceled()) {
                return new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
            }
        }

        TmfTreeModel.Builder<M> treeModelBuilder = new TmfTreeModel.Builder<>();
        if (columnDescriptor == null) {
            columnDescriptor = Collections.emptyList();
        }
        treeModelBuilder.setColumnDescriptors(columnDescriptor)
                        .setEntries(Lists.transform(entries, e -> e.getKey()));

        if (isComplete) {
            return new TmfModelResponse<>(treeModelBuilder.build(), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }
        return new TmfModelResponse<>(treeModelBuilder.build(), ITmfResponse.Status.RUNNING, CommonStatusMessage.RUNNING);
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

    /**
     * Adds a new data provider to the list of providers
     *
     * @param dataProvider
     *              a data provider to add
     */
    protected void addProvider(P dataProvider) {
        fProviders.add(dataProvider);
    }

    /**
     * Removes a give data provider from the list of providers.
     * It will dispose the data provider.
     *
     * @param dataProvider
     *            the data provider to remove
     */
    protected void removeProvider(P dataProvider) {
        fProviders.remove(dataProvider);
        dataProvider.dispose();
    }

    @Override
    public void dispose() {
        fProviders.forEach(ITmfTreeDataProvider::dispose);
        fProviders.clear();
    }

    @Override
    public TmfModelResponse<AnnotationCategoriesModel> fetchAnnotationCategories(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        AnnotationCategoriesModel model = new AnnotationCategoriesModel(Collections.emptyList());
        for (P dataProvider : getProviders()) {
            if (dataProvider instanceof IOutputAnnotationProvider) {
                TmfModelResponse<AnnotationCategoriesModel> response = ((IOutputAnnotationProvider) dataProvider).fetchAnnotationCategories(fetchParameters, monitor);
                model = AnnotationCategoriesModel.of(model, response.getModel());
            }
        }
        if (model.getAnnotationCategories().isEmpty()) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }
        return new TmfModelResponse<>(model, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    @Override
    public TmfModelResponse<AnnotationModel> fetchAnnotations(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        boolean isComplete = true;
        AnnotationModel model = new AnnotationModel(Collections.emptyMap());
        for (P dataProvider : getProviders()) {
            if (dataProvider instanceof IOutputAnnotationProvider) {
                TmfModelResponse<AnnotationModel> response = ((IOutputAnnotationProvider) dataProvider).fetchAnnotations(fetchParameters, monitor);
                isComplete &= response.getStatus() == ITmfResponse.Status.COMPLETED;
                model = AnnotationModel.of(model, response.getModel());
                if (monitor != null && monitor.isCanceled()) {
                    return new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
                }
            }
        }
        if (isComplete) {
            return new TmfModelResponse<>(model, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }
        return new TmfModelResponse<>(model, ITmfResponse.Status.RUNNING, CommonStatusMessage.RUNNING);
    }
}

