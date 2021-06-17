/**********************************************************************
 * Copyright (c) 2020, 2021 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.IAnnotation.AnnotationType;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties.SymbolType;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.Iterables;

/**
 * An annotation provider to adds annotations to a data provider of a given
 * trace, for events from another trace for the same host.
 *
 * The way it works, the data provider provides entries, that have a metadata
 * field whose value corresponds to the value of an event aspect, for example,
 * "TID" or "CPU". When the value match, an annotation for this event will be
 * added to the entry.
 *
 * @author Matthew Khouzam
 * @param <M>
 *            the model type to pass
 */
public class EventAnnotationProvider<@NonNull M extends TimeGraphEntryModel> implements IOutputAnnotationProvider {
    private static final TmfModelResponse<AnnotationModel> NO_DATA = new TmfModelResponse<>(new AnnotationModel(Collections.emptyMap()), Status.COMPLETED, ""); //$NON-NLS-1$
    private final String fMetadataKey;
    private final Map<String, String> fMarkerColorCache = new HashMap<>();
    private final Class<? extends ITmfEventAspect<?>> fAspect;
    private final Collection<@NonNull ITmfTrace> fMarkerTraces;
    private final BiFunction<Map<@NonNull String, @NonNull Object>, @Nullable IProgressMonitor, TmfModelResponse<@NonNull TmfTreeModel<M>>> fTreeResolver;
    private final Predicate<M> fAdditionalPredicate;
    private final Map<ITmfTrace, TmfEventRequest> fRunningRequests = new HashMap<>();

    /**
     * The constructor
     *
     * @param metadataKey
     *            the metadata string to query for a given model entry
     * @param additional
     *            additional predicate aside from metadata. Useful to resolve
     *            clashes. An example would be in a resources view where there
     *            are 4 entries with CPU==1 from the metadataKey, this can
     *            determine which of the four rows to select. If the metadata is
     *            unique, {@code (unused)->true} could be passed.
     * @param tracefilter
     *            The filter for the trace sources, e.g. can't be a trace of a
     *            certain type, or it needs a certain aspect type.
     * @param aspect
     *            the aspect to query, to determine if it matches the value of
     *            the metadata
     * @param trace
     *            the trace to analyze
     * @param treeResolver
     *            the tree resolver, to get the node to place the annotations at
     */
    public EventAnnotationProvider(String metadataKey, Predicate<M> additional, Predicate<ITmfTrace> tracefilter, Class<? extends ITmfEventAspect<?>> aspect, ITmfTrace trace,
            BiFunction<@NonNull Map<@NonNull String, @NonNull Object>, @Nullable IProgressMonitor, TmfModelResponse<TmfTreeModel<M>>> treeResolver) {
        fAspect = aspect;
        fMetadataKey = metadataKey;
        fTreeResolver = treeResolver;
        fAdditionalPredicate = additional;
        fMarkerTraces = TmfTraceManager.getInstance().getTracesForHost(trace.getHostId()).stream().filter(tracefilter).collect(Collectors.toSet());
    }

    @Override
    public TmfModelResponse<@NonNull AnnotationCategoriesModel> fetchAnnotationCategories(@NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        return new TmfModelResponse<>(new AnnotationCategoriesModel(fMarkerTraces.stream().map(ITmfTrace::getName).collect(Collectors.toList())), Status.COMPLETED, ""); //$NON-NLS-1$
    }

    @Override
    public TmfModelResponse<@NonNull AnnotationModel> fetchAnnotations(Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        List<Long> timeRequested = DataProviderParameterUtils.extractTimeRequested(fetchParameters);
        List<@NonNull Long> entries = DataProviderParameterUtils.extractSelectedItems(fetchParameters);
        @Nullable Set<@NonNull String> categories = DataProviderParameterUtils.extractSelectedCategories(fetchParameters);

        if (timeRequested == null || entries == null) {
            return NO_DATA;
        }

        TmfModelResponse<@NonNull TmfTreeModel<M>> tree = Objects.requireNonNull(fTreeResolver.apply(fetchParameters, monitor));

        TmfTreeModel<M> model = tree.getModel();
        if (model == null) {
            return NO_DATA;
        }

        Function<M, Integer> keyMapper = entry -> (Integer) Iterables.get(entry.getMetadata().get(fMetadataKey), 0);
        Predicate<M> predicate = entry -> {
            Collection<@NonNull Object> collection = entry.getMetadata().get(fMetadataKey);
            return !collection.isEmpty() && !Objects.equals(Iterables.get(collection, 0), -1);
        };

        Map<Integer, TimeGraphEntryModel> rowMap = new LinkedHashMap<>();
        List<@NonNull M> entries2 = model.getEntries();
        entries2.stream().filter(predicate).filter(fAdditionalPredicate).forEach(element -> rowMap.put(keyMapper.apply(element), element));
        Map<String, Collection<Annotation>> markers = new LinkedHashMap<>();
        TmfTimeRange tr = new TmfTimeRange(TmfTimestamp.fromNanos(timeRequested.get(0)), TmfTimestamp.fromNanos(timeRequested.get(timeRequested.size() - 1)));
        EventAnnotationProvider<@NonNull M> lock = this;
        synchronized (lock) {
            for (ITmfTrace source : fMarkerTraces) {
                TmfEventRequest old = fRunningRequests.remove(source);
                if (old != null && old.isRunning()) {
                    old.cancel();
                }
            }
            for (ITmfTrace source : fMarkerTraces) {
                if (categories != null && !categories.contains(source.getName())) {
                    // marker category is filtered out
                    continue;
                }

                TmfEventRequest req = new TmfEventRequest(ITmfEvent.class, tr, 0, Integer.MAX_VALUE, ExecutionType.FOREGROUND) {
                    private int timesliceIndex = 0;
                    private Set<Object> values = new HashSet<>();

                    private long next() {
                        if (timeRequested.size() > timesliceIndex + 1) {
                            return timeRequested.get(timesliceIndex + 1);
                        }
                        return Long.MAX_VALUE;
                    }

                    @Override
                    public void handleData(ITmfEvent event) {
                        super.handleData(event);
                        while (event.getTimestamp().toNanos() > next()) {
                            timesliceIndex++;
                            values.clear();
                            if (timesliceIndex >= timeRequested.size() - 1) {
                                done();
                                return;
                            }
                        }

                        Object value = TmfTraceUtils.resolveEventAspectOfClassForEvent(event.getTrace(), fAspect, event);
                        if (value != null && !values.contains(value)) {
                            values.add(value);
                            Collection<Annotation> markerList = markers.computeIfAbsent(String.valueOf(event.getTrace().getName()), string -> new ArrayList<>());
                            TimeGraphEntryModel entryModel = rowMap.get(value);
                            if (entryModel != null) {
                                String name = event.getName();
                                Map<String, Object> style = new HashMap<>();
                                style.put(StyleProperties.SYMBOL_TYPE, SymbolType.INVERTED_TRIANGLE);
                                style.put(StyleProperties.COLOR, getMarkerColor(name));
                                style.put(StyleProperties.HEIGHT, 0.3);
                                style.put(StyleProperties.VERTICAL_ALIGN, StyleProperties.VerticalAlign.TOP);
                                markerList.add(new Annotation(event.getTimestamp().toNanos(), 0L, entryModel.getId(), AnnotationType.CHART, name, new OutputElementStyle(name, style)));
                            }
                        }

                    }

                    private String getMarkerColor(String name) {
                        return Objects.requireNonNull(fMarkerColorCache.computeIfAbsent(name, label -> Objects.requireNonNull(String.format("#%6x", label.hashCode() & 0xffffff)))); //$NON-NLS-1$
                    }
                };
                fRunningRequests.put(source, req);
                source.sendRequest(req);
            }
            try {
                for (ITmfTrace source : fMarkerTraces) {
                    TmfEventRequest req = null;
                    synchronized (lock) {
                        req = fRunningRequests.get(source);
                    }
                    if (req != null) {
                        req.waitForCompletion();
                        fRunningRequests.remove(source);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return new TmfModelResponse<>(new AnnotationModel(markers), Status.COMPLETED, ""); //$NON-NLS-1$
    }
}