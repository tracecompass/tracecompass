/*******************************************************************************
 * Copyright (c) 2018, 2021 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.timegraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.Annotation;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.AnnotationCategoriesModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.AnnotationModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.IAnnotation.AnnotationType;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.IOutputAnnotationProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.ui.widgets.ViewFilterDialog;
import org.eclipse.tracecompass.internal.provisional.tmf.ui.widgets.timegraph.BaseDataProviderTimeGraphPresentationProvider;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.views.timegraph.Messages;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.tmf.core.TmfStrings;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.event.lookup.TmfCallsite;
import org.eclipse.tracecompass.tmf.core.model.IOutputElement;
import org.eclipse.tracecompass.tmf.core.model.timegraph.IFilterProperty;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphArrow;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.actions.OpenSourceCodeAction;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ILinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.MarkerEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NamedTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry.Sampling;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeLinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;
import org.eclipse.ui.IWorkbenchActionConstants;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

/**
 * {@link AbstractTimeGraphView} for views with data providers.
 *
 * @author Loic Prieur-Drevon
 * @since 4.0
 */
public class BaseDataProviderTimeGraphView extends AbstractTimeGraphView {

    /**
     * Timeout between updates in the build thread in ms
     */
    protected static final long BUILD_UPDATE_TIMEOUT = 500;

    private static final Pattern SOURCE_REGEX = Pattern.compile("(.*):(\\d+)"); //$NON-NLS-1$

    /**
     * Table of (data provider, model id) to time graph entry. The table should be
     * filled by {@link #buildEntryList} and is read by {@link #zoomEntries} and
     * {@link #getLinkList}.
     */
    protected final Table<ITimeGraphDataProvider<? extends @NonNull TimeGraphEntryModel>, Long, @NonNull TimeGraphEntry> fEntries = HashBasedTable.create();

    /**
     * Table of (time graph entry, data provider) to model id. The table should
     * be filled by {@link #buildEntryList}.
     *
     * @since 5.2
     */
    protected final Table<@NonNull TimeGraphEntry, ITimeGraphDataProvider<? extends @NonNull TimeGraphEntryModel>, Long> fEntryIds = HashBasedTable.create();

    /**
     * Table of (scope, model id) to time graph entry. The table should be
     * filled by {@link #buildEntryList}.
     *
     * @since 5.2
     */
    protected final Table<Object, Long, @NonNull TimeGraphEntry> fScopedEntries = HashBasedTable.create();

    /** Map of parent trace to providers */
    private final Multimap<ITmfTrace, ITimeGraphDataProvider<? extends @NonNull TimeGraphEntryModel>> fProviders = HashMultimap.create();
    /** Map of parent trace to scopes, for cleanup */
    private final Multimap<ITmfTrace, Object> fScopes = HashMultimap.create();
    /** Map of provider to marker categories */
    private final Map<ITimeGraphDataProvider<? extends @NonNull TimeGraphEntryModel>, List<String>> fMarkerCategories = new HashMap<>();

    private final String fProviderId;

    /**
     * Constructs a time graph view that contains a time graph viewer.
     *
     * By default, the view uses a single default column in the name space that
     * shows the time graph entry name. To use multiple columns and/or customized
     * label texts, the subclass constructor must call
     * {@link #setTreeColumns(String[])} and/or
     * {@link #setTreeLabelProvider(TreeLabelProvider)}.
     *
     * @param id
     *            The id of the view
     * @param pres
     *            The presentation provider
     * @param providerId
     *            the ID for the {@link ITimeGraphDataProvider} to use to populate
     *            this view
     */
    public BaseDataProviderTimeGraphView(String id, TimeGraphPresentationProvider pres, String providerId) {
        super(id, pres);
        fProviderId = providerId;
    }

    /**
     * Get a data provider ID
     *
     * @return the data provider ID
     * @since 3.3
     */
    protected String getProviderId() {
        return fProviderId;
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        createTimeEventContextMenu();
    }

    @Override
    protected void buildEntryList(@NonNull ITmfTrace trace, @NonNull ITmfTrace parentTrace, @NonNull IProgressMonitor monitor) {
        ITimeGraphDataProvider<@NonNull TimeGraphEntryModel> dataProvider = DataProviderManager
                .getInstance().getDataProvider(trace, getProviderId(), ITimeGraphDataProvider.class);
        if (dataProvider == null) {
            return;
        }
        ITimeGraphPresentationProvider presentationProvider = getPresentationProvider();
        if (presentationProvider instanceof BaseDataProviderTimeGraphPresentationProvider) {
            ((BaseDataProviderTimeGraphPresentationProvider) presentationProvider).addProvider(dataProvider, getTooltipResolver(dataProvider));
        }
        boolean complete = false;
        while (!complete && !monitor.isCanceled()) {
            Map<@NonNull String, @NonNull Object> parameters = getFetchTreeParameters();
            TmfModelResponse<TmfTreeModel<@NonNull TimeGraphEntryModel>> response = dataProvider.fetchTree(parameters, monitor);
            if (response.getStatus() == ITmfResponse.Status.FAILED) {
                Activator.getDefault().logError(getClass().getSimpleName() + " Data Provider failed: " + response.getStatusMessage()); //$NON-NLS-1$
                return;
            } else if (response.getStatus() == ITmfResponse.Status.CANCELLED) {
                return;
            }
            complete = response.getStatus() == ITmfResponse.Status.COMPLETED;

            TmfTreeModel<@NonNull TimeGraphEntryModel> model = response.getModel();
            if (model != null) {
                synchronized (fEntries) {
                    Object scope = (model.getScope() == null) ? dataProvider : model.getScope();
                    fProviders.put(parentTrace, dataProvider);
                    fScopes.put(parentTrace, scope);
                    /*
                     * The provider may send entries unordered and parents may
                     * not exist when child is constructor, we'll re-unite
                     * families at the end
                     */
                    List<TimeGraphEntry> orphaned = new ArrayList<>();
                    Map<Long, AtomicInteger> indexMap = new HashMap<>();
                    for (TimeGraphEntryModel entry : model.getEntries()) {
                        TimeGraphEntry uiEntry = fScopedEntries.get(scope, entry.getId());
                        if (entry.getParentId() != -1) {
                            if (uiEntry == null) {
                                uiEntry = new TimeGraphEntry(entry);
                                TimeGraphEntry parent = fScopedEntries.get(scope, entry.getParentId());
                                if (parent != null) {
                                    // TODO: the order of children from different data providers is undefined
                                    int index = indexMap.computeIfAbsent(entry.getParentId(), l -> new AtomicInteger()).getAndIncrement();
                                    parent.addChild(index, uiEntry);
                                } else {
                                    orphaned.add(uiEntry);
                                }
                                fScopedEntries.put(scope, entry.getId(), uiEntry);
                            } else {
                                indexMap.computeIfAbsent(entry.getParentId(), l -> new AtomicInteger()).getAndIncrement();
                                uiEntry.updateModel(entry);
                            }
                        } else {
                            if (entry.getStartTime() != Long.MIN_VALUE) {
                                setStartTime(Long.min(getStartTime(), entry.getStartTime()));
                            }
                            setEndTime(Long.max(getEndTime(), entry.getEndTime() + 1));

                            if (uiEntry != null) {
                                uiEntry.updateModel(entry);
                            } else {
                                // Do not assume that parentless entries are
                                // trace entries
                                uiEntry = new TraceEntry(entry, trace, dataProvider);
                                fScopedEntries.put(scope, entry.getId(), uiEntry);
                                addToEntryList(parentTrace, Collections.singletonList(uiEntry));
                            }
                        }
                        fEntries.put(dataProvider, entry.getId(), uiEntry);
                        fEntryIds.put(uiEntry, dataProvider, entry.getId());
                    }
                    // Find missing parents
                    // Orphans should be inserted before non-orphans
                    indexMap.clear();
                    for (TimeGraphEntry orphanedEntry : orphaned) {
                        TimeGraphEntry parent = fScopedEntries.get(scope, orphanedEntry.getEntryModel().getParentId());
                        if (parent != null) {
                            int index = indexMap.computeIfAbsent(parent.getEntryModel().getId(), l -> new AtomicInteger()).getAndIncrement();
                            parent.addChild(index, orphanedEntry);
                        }
                    }
                }

                long start = getStartTime();
                long end = getEndTime();
                final long resolution = Long.max(1, (end - start) / getDisplayWidth());
                @NonNull Iterable<@NonNull TimeGraphEntry> entries;
                synchronized (fEntries) {
                    entries = ImmutableList.copyOf(fEntries.values());
                }
                zoomEntries(entries, start, end, resolution, monitor);
            }

            if (monitor.isCanceled()) {
                return;
            }

            if (parentTrace.equals(getTrace())) {
                synchingToTime(getTimeGraphViewer().getSelectionBegin());
                refresh();
            }
            monitor.worked(1);

            if (!complete && !monitor.isCanceled()) {
                try {
                    Thread.sleep(BUILD_UPDATE_TIMEOUT);
                } catch (InterruptedException e) {
                    Activator.getDefault().logError("Failed to wait for data provider", e); //$NON-NLS-1$
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Class to represent a parent entry, for which we keep a link to the trace
     * and data provider to avoid having to do so for its children. This type of
     * entry is otherwise not different from any other time graph entry.
     *
     * @author Loic Prieur-Drevon
     * @since 3.3
     */
    protected static class TraceEntry extends TimeGraphEntry {
        private final @NonNull ITmfTrace fTrace;
        private final @NonNull ITimeGraphDataProvider<? extends TimeGraphEntryModel> fProvider;

        /**
         * Constructor
         *
         * @param model
         *            trace level model
         * @param trace
         *            The trace corresponding to this trace entry.
         * @param provider
         *            reference to the provider for this trace and view
         */
        public TraceEntry(@NonNull TimeGraphEntryModel model, @NonNull ITmfTrace trace,
                @NonNull ITimeGraphDataProvider<? extends TimeGraphEntryModel> provider) {
            super(model);
            fTrace = trace;
            fProvider = provider;
        }

        /**
         * Getter for this trace entry's trace
         *
         * @return the trace for this trace entry and its children
         */
        public @NonNull ITmfTrace getTrace() {
            return fTrace;
        }

        /**
         * Getter for the data provider for this {@link TraceEntry}
         *
         * @return this entry's {@link ITimeGraphDataProvider}
         */
        public @NonNull ITimeGraphDataProvider<? extends TimeGraphEntryModel> getProvider() {
            return fProvider;
        }
    }

    /**
     * Get the {@link ITmfTrace} from a {@link TimeGraphEntry}'s parent.
     *
     * @param entry
     *            queried {@link TimeGraphEntry}.
     * @return the {@link ITmfTrace}
     * @since 3.3
     */
    public static @NonNull ITmfTrace getTrace(TimeGraphEntry entry) {
        return getTraceEntry(entry).getTrace();
    }

    /**
     * Get the {@link ITimeGraphDataProvider} from a {@link TimeGraphEntry}'s
     * parent.
     *
     * @param entry
     *            queried {@link TimeGraphEntry}.
     * @return the {@link ITimeGraphDataProvider}
     * @since 3.3
     */
    public static ITimeGraphDataProvider<? extends TimeGraphEntryModel> getProvider(TimeGraphEntry entry) {
        TraceEntry traceEntry = getTraceEntry(entry);
        // TODO: A trace can have many providers
        return traceEntry.getProvider();
    }

    private Collection<ITimeGraphDataProvider<? extends @NonNull TimeGraphEntryModel>> getProviders(ITmfTrace viewTrace) {
        Collection<ITimeGraphDataProvider<? extends @NonNull TimeGraphEntryModel>> providers;
        synchronized (fEntries) {
            providers = fProviders.get(viewTrace);
        }
        if (!providers.isEmpty()) {
            return providers;
        }
        List<@NonNull TimeGraphEntry> traceEntries = getEntryList(viewTrace);
        if (traceEntries == null) {
            return Collections.emptyList();
        }
        providers = new ArrayList<>();
        for (TraceEntry traceEntry : Iterables.filter(traceEntries, TraceEntry.class)) {
            providers.add(traceEntry.getProvider());
        }
        return providers;
    }

    private static TraceEntry getTraceEntry(TimeGraphEntry entry) {
        ITimeGraphEntry parent = entry;
        while (parent != null) {
            if (parent instanceof TraceEntry) {
                return ((TraceEntry) parent);
            }
            parent = parent.getParent();
        }
        throw new IllegalStateException(entry + " should have a TraceEntry parent"); //$NON-NLS-1$
    }

    @Override
    protected void zoomEntries(@NonNull Iterable<@NonNull TimeGraphEntry> entries, long zoomStartTime, long zoomEndTime, long resolution, boolean fullSearch, @NonNull IProgressMonitor monitor) {
        if (resolution < 0) {
            // StateSystemUtils.getTimes would throw an illegal argument exception.
            return;
        }

        long start = Long.min(zoomStartTime, zoomEndTime);
        long end = Long.max(zoomStartTime, zoomEndTime);
        Sampling sampling = new Sampling(start, end, resolution);
        Multimap<ITimeGraphDataProvider<? extends TimeGraphEntryModel>, Long> providersToModelIds = filterGroupEntries(entries, zoomStartTime, zoomEndTime);
        SubMonitor subMonitor = SubMonitor.convert(monitor, getClass().getSimpleName() + "#zoomEntries", providersToModelIds.size()); //$NON-NLS-1$

        for (Entry<ITimeGraphDataProvider<? extends TimeGraphEntryModel>, Collection<Long>> entry : providersToModelIds.asMap().entrySet()) {
            ITimeGraphDataProvider<? extends TimeGraphEntryModel> dataProvider = entry.getKey();
            Map<@NonNull String, @NonNull Object> parameters = getFetchRowModelParameters(start, end, resolution, fullSearch, entry.getValue());
            TmfModelResponse<TimeGraphModel> response = dataProvider.fetchRowModel(parameters, monitor);

            TimeGraphModel model = response.getModel();
            if (model != null) {
                zoomEntries(fEntries.row(dataProvider), model.getRows(), response.getStatus() == ITmfResponse.Status.COMPLETED, sampling);
            }
            subMonitor.worked(1);
        }
    }

    /**
     * Filter the entries to return only the Non Null {@link TimeGraphEntry} which
     * intersect the time range.
     *
     * @param visible
     *            the input list of visible entries
     * @param zoomStartTime
     *            the leftmost time bound of the view
     * @param zoomEndTime
     *            the rightmost time bound of the view
     * @return A Multimap of data providers to their visible entries' model IDs.
     */
    private Multimap<ITimeGraphDataProvider<? extends TimeGraphEntryModel>, Long> filterGroupEntries(Iterable<TimeGraphEntry> visible,
            long zoomStartTime, long zoomEndTime) {
        Multimap<ITimeGraphDataProvider<? extends TimeGraphEntryModel>, Long> providersToModelIds = HashMultimap.create();
        for (TimeGraphEntry entry : visible) {
            if (zoomStartTime <= entry.getEndTime() && zoomEndTime >= entry.getStartTime() && entry.hasTimeEvents()) {
                synchronized (fEntries) {
                    if (!fEntryIds.isEmpty()) {
                        fEntryIds.row(entry).forEach(providersToModelIds::put);
                    } else {
                        ITimeGraphDataProvider<? extends TimeGraphEntryModel> provider = getProvider(entry);
                        if (provider != null) {
                            providersToModelIds.put(provider, entry.getEntryModel().getId());
                        }
                    }
                }
            } else {
                entry.setZoomedEventList(Collections.emptyList());
            }
        }
        return providersToModelIds;
    }

    private void zoomEntries(Map<Long, TimeGraphEntry> map, List<ITimeGraphRowModel> model, boolean completed, Sampling sampling) {
        boolean isZoomThread = Thread.currentThread() instanceof ZoomThread;
        for (ITimeGraphRowModel rowModel : model) {
            TimeGraphEntry entry;
            synchronized (fEntries) {
                entry = map.get(rowModel.getEntryID());
            }

            if (entry != null) {
                List<ITimeEvent> events = createTimeEvents(entry, rowModel.getStates());
                if (isZoomThread) {
                    applyResults(() -> {
                        entry.setZoomedEventList(events);
                        if (completed) {
                            entry.setSampling(sampling);
                        }
                    });
                } else {
                    entry.setEventList(events);
                }
            }
        }
    }

    /**
     * Create {@link ITimeEvent}s for an entry from the list of
     * {@link ITimeGraphState}s, filling in the gaps.
     *
     * @param entry
     *            the {@link TimeGraphEntry} on which we are working
     * @param values
     *            the list of {@link ITimeGraphState}s from the
     *            {@link ITimeGraphDataProvider}.
     * @return a contiguous List of {@link ITimeEvent}s
     */
    protected List<ITimeEvent> createTimeEvents(TimeGraphEntry entry, List<ITimeGraphState> values) {
        List<ITimeEvent> events = new ArrayList<>(values.size());
        ITimeEvent prev = null;
        for (ITimeGraphState state : values) {
            ITimeEvent event = createTimeEvent(entry, state);
            if (prev != null) {
                long prevEnd = prev.getTime() + prev.getDuration();
                ViewFilterDialog viewFilterDialog = getViewFilterDialog();
                if (prevEnd < event.getTime() && (viewFilterDialog == null || !viewFilterDialog.hasActiveSavedFilters())) {
                    // fill in the gap.
                    TimeEvent timeEvent = new TimeEvent(entry, prevEnd, event.getTime() - prevEnd);
                    if (viewFilterDialog != null && viewFilterDialog.isFilterActive()) {
                        timeEvent.setProperty(IFilterProperty.DIMMED, true);
                    }
                    events.add(timeEvent);
                }
            }
            prev = event;
            events.add(event);
        }
        return events;
    }

    /**
     * Create a {@link TimeEvent} for a {@link TimeGraphEntry} and a
     * {@link TimeGraphState}
     *
     * @param entry
     *            {@link TimeGraphEntry} for which we create a state
     * @param state
     *            {@link ITimeGraphState} from the data provider
     * @return a new {@link TimeEvent} for these arguments
     *
     * @since 3.3
     */
    protected TimeEvent createTimeEvent(TimeGraphEntry entry, ITimeGraphState state) {
        String label = state.getLabel();
        if (state.getValue() == Integer.MIN_VALUE && label == null && state.getStyle() == null) {
            return new NullTimeEvent(entry, state.getStartTime(), state.getDuration());
        }
        if (label != null) {
            return new NamedTimeEvent(entry, label, state);
        }
        return new TimeEvent(entry, state);
    }

    @Override
    protected List<@NonNull ILinkEvent> getLinkList(long zoomStartTime, long zoomEndTime, long resolution,
            @NonNull IProgressMonitor monitor) {
        Collection<ITimeGraphDataProvider<? extends @NonNull TimeGraphEntryModel>> providers = getProviders(getTrace());
        if (providers.isEmpty()) {
            return Collections.emptyList();
        }
        List<@NonNull ILinkEvent> linkList = new ArrayList<>();
        List<@NonNull Long> times = StateSystemUtils.getTimes(zoomStartTime, zoomEndTime, resolution);
        Map<@NonNull String, @NonNull Object> parameters = getFetchArrowsParameters(times);

        for (ITimeGraphDataProvider<? extends TimeGraphEntryModel> provider : providers) {
            TmfModelResponse<List<ITimeGraphArrow>> response = provider.fetchArrows(parameters, monitor);
            List<ITimeGraphArrow> model = response.getModel();

            if (model != null) {
                for (ITimeGraphArrow arrow : model) {
                    ITimeGraphEntry prevEntry;
                    ITimeGraphEntry nextEntry;
                    synchronized (fEntries) {
                        prevEntry = fEntries.get(provider, arrow.getSourceId());
                        nextEntry = fEntries.get(provider, arrow.getDestinationId());
                    }
                    if (prevEntry != null && nextEntry != null) {
                        linkList.add(new TimeLinkEvent(arrow, prevEntry, nextEntry));
                    }
                }
            }
        }
        return linkList;
    }

    @Override
    protected @NonNull List<String> getViewMarkerCategories() {
        List<String> viewMarkerCategories = super.getViewMarkerCategories();
        Collection<ITimeGraphDataProvider<? extends @NonNull TimeGraphEntryModel>> providers = getProviders(getTrace());
        if (providers.isEmpty()) {
            return viewMarkerCategories;
        }
        for (ITimeGraphDataProvider<? extends TimeGraphEntryModel> provider : providers) {
            if (provider instanceof IOutputAnnotationProvider) {
                Map<@NonNull String, @NonNull Object> parameters = getFetchAnnotationCategoriesParameters();
                TmfModelResponse<@NonNull AnnotationCategoriesModel> response = ((IOutputAnnotationProvider) provider).fetchAnnotationCategories(parameters, new NullProgressMonitor());
                AnnotationCategoriesModel model = response.getModel();
                if (model != null) {
                    List<@NonNull String> categories = model.getAnnotationCategories();
                    viewMarkerCategories.addAll(categories);
                    fMarkerCategories.put(provider, categories);
                }
            }
        }
        return viewMarkerCategories;
    }

    @Override
    protected @NonNull List<IMarkerEvent> getViewMarkerList(Iterable<@NonNull TimeGraphEntry> entries, long startTime, long endTime, long resolution, @NonNull IProgressMonitor monitor) {
        List<IMarkerEvent> viewMarkerList = super.getViewMarkerList(startTime, endTime, resolution, monitor);
        List<@NonNull TimeGraphEntry> traceEntries = getEntryList(getTrace());
        if (traceEntries == null) {
            return viewMarkerList;
        }
        List<@NonNull Long> times = StateSystemUtils.getTimes(startTime, endTime, resolution);
        Multimap<ITimeGraphDataProvider<? extends TimeGraphEntryModel>, Long> providersToModelIds = filterGroupEntries(entries, startTime, endTime);
        for (ITimeGraphDataProvider<? extends TimeGraphEntryModel> provider : providersToModelIds.keySet()) {
            if (provider instanceof IOutputAnnotationProvider) {
                List<String> categories = new ArrayList<>(fMarkerCategories.get(provider));
                categories.removeIf(category -> !getTimeGraphViewer().isMarkerCategoryVisible(category));
                if (categories.isEmpty()) {
                    continue;
                }
                Map<@NonNull String, @NonNull Object> parameters = getFetchAnnotationsParameters(times, providersToModelIds.get(provider));
                parameters.put(DataProviderParameterUtils.REQUESTED_MARKER_CATEGORIES_KEY, categories);
                TmfModelResponse<@NonNull AnnotationModel> response = ((IOutputAnnotationProvider) provider).fetchAnnotations(parameters, new NullProgressMonitor());
                AnnotationModel model = response.getModel();
                if (model != null) {
                    for (Entry<String, Collection<Annotation>> entry : model.getAnnotations().entrySet()) {
                        String category = entry.getKey();
                        for (Annotation annotation : entry.getValue()) {
                            if (annotation.getType() == AnnotationType.CHART) {
                                // If the annotation entry ID is -1 we want the
                                // marker to span across all entries
                                ITimeGraphEntry markerEntry = null;
                                if (annotation.getEntryId() != -1) {
                                    synchronized (fEntries) {
                                        markerEntry = fEntries.get(provider, annotation.getEntryId());
                                    }
                                }
                                MarkerEvent markerEvent = new MarkerEvent(annotation, markerEntry, category, true);
                                viewMarkerList.add(markerEvent);
                            }
                        }
                    }
                }
            }
        }
        return viewMarkerList;
    }

    /**
     * Get the fetch parameters to pass to a fetchTree call
     *
     * @return Map of parameters for fetchTree
     * @since 5.2
     */
    protected @NonNull Map<@NonNull String, @NonNull Object> getFetchTreeParameters() {
        return new HashMap<>();
    }

    /**
     * Get the fetch parameters to pass to a fetchRowModel call
     *
     * @param start
     *            Start time of query
     * @param end
     *            End time of query
     * @param resolution
     *            Resolution of query
     * @param fullSearch
     *            True to perform a full search
     * @param items
     *            The unique keys of the entries to query.
     * @return Map of parameters for fetchRowModel
     * @since 5.2
     */
    protected @NonNull Map<@NonNull String, @NonNull Object> getFetchRowModelParameters(long start, long end, long resolution, boolean fullSearch, @NonNull Collection<Long> items) {
        @NonNull Map<@NonNull String, @NonNull Object> parameters = new HashMap<>();
        parameters.put(DataProviderParameterUtils.REQUESTED_TIME_KEY, StateSystemUtils.getTimes(start, end, resolution));
        parameters.put(DataProviderParameterUtils.REQUESTED_ITEMS_KEY, items);
        Multimap<@NonNull Integer, @NonNull String> regexesMap = getRegexes();
        if (!regexesMap.isEmpty()) {
            parameters.put(DataProviderParameterUtils.REGEX_MAP_FILTERS_KEY, regexesMap.asMap());
        }
        if (fullSearch) {
            parameters.put(DataProviderParameterUtils.FULL_SEARCH_KEY, Boolean.TRUE);
        }
        return parameters;
    }

    /**
     * Get the fetch parameters to pass to a fetchArrows call
     *
     * @param times
     *            Sorted list of times to query.
     * @return Map of parameters for fetchArrows
     * @since 5.2
     */
    protected @NonNull Map<@NonNull String, @NonNull Object> getFetchArrowsParameters(@NonNull List<@NonNull Long> times) {
        @NonNull Map<@NonNull String, @NonNull Object> parameters = new HashMap<>();
        parameters.put(DataProviderParameterUtils.REQUESTED_TIME_KEY, times);
        return parameters;
    }

    /**
     * Get the fetch parameters to pass to a fetchTooltip call
     *
     * @param time
     *            The time of the tooltip.
     * @param item
     *            The unique key of the tooltip entry.
     * @param element
     *            The model element of the tooltip.
     * @return Map of parameters for fetchTooltip
     * @since 5.2
     */
    protected @NonNull Map<@NonNull String, @NonNull Object> getFetchTooltipParameters(long time, long item, @Nullable IOutputElement element) {
        @NonNull Map<@NonNull String, @NonNull Object> parameters = new HashMap<>();
        parameters.put(DataProviderParameterUtils.REQUESTED_TIME_KEY, Collections.singletonList(time));
        parameters.put(DataProviderParameterUtils.REQUESTED_ITEMS_KEY, Collections.singletonList(item));
        if (element != null) {
            parameters.put(DataProviderParameterUtils.REQUESTED_ELEMENT_KEY, element);
        }
        return parameters;
    }

    /**
     * Get the fetch parameters to pass to a fetchAnnotationCategories call
     *
     * @return Map of parameters for fetchAnnotationCategories
     * @since 5.2
     */
    protected @NonNull Map<@NonNull String, @NonNull Object> getFetchAnnotationCategoriesParameters() {
        HashMap<@NonNull String, @NonNull Object> categoriesParameters = new HashMap<>();
        return categoriesParameters;
    }

    /**
     * Get the fetch parameters to pass to a fetchAnnotations call
     *
     * @param times
     *            Sorted list of times to query.
     * @param items
     *            The unique keys of the entries to query.
     * @return Map of parameters for fetchAnnotations
     * @since 5.2
     */
    protected @NonNull Map<@NonNull String, @NonNull Object> getFetchAnnotationsParameters(@NonNull List<Long> times, @NonNull Collection<Long> items) {
        Map<@NonNull String, @NonNull Object> parameters = new HashMap<>();
        parameters.put(DataProviderParameterUtils.REQUESTED_TIME_KEY, times);
        parameters.put(DataProviderParameterUtils.REQUESTED_ITEMS_KEY, items);
        return parameters;
    }

    private BiFunction<ITimeEvent, Long, Map<String, String>> getTooltipResolver(ITimeGraphDataProvider<? extends TimeGraphEntryModel> provider) {
        return (event, time) -> {
            Long entryId = null;
            synchronized (fEntries) {
                entryId = fEntryIds.get(event.getEntry(), provider);
            }
            if (entryId == null) {
                return Collections.emptyMap();
            }
            IOutputElement element = null;
            if (event instanceof TimeEvent) {
                element = ((TimeEvent) event).getModel();
            }
            Map<@NonNull String, @NonNull Object> parameters = getFetchTooltipParameters(time, entryId, element);
            TmfModelResponse<Map<String, String>> response = provider.fetchTooltip(parameters, new NullProgressMonitor());
            Map<String, String> tooltip = response.getModel();
            return (tooltip == null) ? Collections.emptyMap() : tooltip;
        };
    }

    @Override
    protected void resetView(ITmfTrace viewTrace) {
        List<@NonNull TimeGraphEntry> entryList = getEntryList(viewTrace);
        super.resetView(viewTrace);
        // Remove the entries for this trace
        if (entryList != null) {
            synchronized (fEntries) {
                if (!fProviders.isEmpty()) {
                    fProviders.removeAll(viewTrace).forEach(provider -> {
                        fEntries.row(provider).clear();
                        fEntryIds.column(provider).clear();
                        fMarkerCategories.remove(provider);
                    });
                } else {
                    for (TimeGraphEntry entry : entryList) {
                        if (entry instanceof TraceEntry) {
                            fEntries.row(((TraceEntry) entry).getProvider()).clear();
                        }
                    }
                }
                fScopes.removeAll(viewTrace).forEach(scope -> fScopedEntries.row(scope).clear());
            }
        }
    }

    private IContributionItem createOpenSourceCodeAction(Map<String, String> model) {
        if (model != null) {
            String callsite = model.get(TmfStrings.source());
            return OpenSourceCodeAction.create(Messages.BaseDataProviderTimeGraphView_OpenSourceActionName, () -> {
                if (callsite != null) {
                    Matcher matcher = SOURCE_REGEX.matcher(callsite);
                    if (matcher.matches()) {
                        return new TmfCallsite(Objects.requireNonNull(matcher.group(1)), Long.parseLong(matcher.group(2)));
                    }
                }
                return null;
            }, this.getTimeGraphViewer().getTimeGraphControl().getShell());
        }
        return null;
    }

    /**
     * Fill context menu
     *
     * @param menuManager
     *            a menuManager to fill
     * @since 5.2
     */
    protected void fillTimeEventContextMenu(@NonNull IMenuManager menuManager) {
        ISelection selection = getSite().getSelectionProvider().getSelection();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection sSel = (IStructuredSelection) selection;
            for (Object element : sSel.toArray()) {
                if (element instanceof TimeEvent) {
                    TimeEvent event = (TimeEvent) element;
                    IContributionItem contribItem = createOpenSourceCodeAction(getPresentationProvider().getEventHoverToolTipInfo(event, getTimeGraphViewer().getSelectionBegin()));
                    if (contribItem != null) {
                        menuManager.add(contribItem);
                        break;
                    }
                }
            }
        }
    }

    private void createTimeEventContextMenu() {
        MenuManager eventMenuManager = new MenuManager();
        eventMenuManager.setRemoveAllWhenShown(true);
        TimeGraphControl timeGraphControl = getTimeGraphViewer().getTimeGraphControl();
        final Menu timeEventMenu = eventMenuManager.createContextMenu(timeGraphControl);

        timeGraphControl.addTimeEventMenuListener(event -> {
            Menu menu = timeEventMenu;
            if (event.data instanceof TimeEvent) {
                timeGraphControl.setMenu(menu);
                return;
            }
            timeGraphControl.setMenu(null);
            event.doit = false;
        });

        eventMenuManager.addMenuListener(manager -> {
            fillTimeEventContextMenu(eventMenuManager);
            eventMenuManager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        });
        getSite().registerContextMenu(eventMenuManager, getTimeGraphViewer().getSelectionProvider());
    }

}
