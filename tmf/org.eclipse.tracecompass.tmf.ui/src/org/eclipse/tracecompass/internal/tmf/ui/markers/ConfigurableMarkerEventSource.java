/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.markers;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IllegalFormatException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.Annotation;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.AnnotationCategoriesModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.AnnotationModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.IOutputAnnotationProvider;
import org.eclipse.tracecompass.internal.tmf.core.annotations.CustomAnnotationProvider;
import org.eclipse.tracecompass.internal.tmf.core.annotations.PeriodicAnnotationProvider;
import org.eclipse.tracecompass.internal.tmf.core.markers.MarkerSegment;
import org.eclipse.tracecompass.internal.tmf.core.markers.MarkerSet;
import org.eclipse.tracecompass.internal.tmf.core.markers.SubMarker;
import org.eclipse.tracecompass.internal.tmf.core.markers.SubMarker.WeightedMarker;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.signal.TmfMarkerEventSourceUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.trace.AbstractTmfTraceAdapterFactory.IDisposableAdapter;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.colors.RGBAUtil;
import org.eclipse.tracecompass.tmf.ui.model.StyleManager;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEventSource;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.MarkerEvent;

import com.google.common.collect.Lists;

/**
 * Configurable marker event source.
 */
public class ConfigurableMarkerEventSource implements IMarkerEventSource, IDisposableAdapter {

    private static final int MIN_PERIOD = 5; // in units of resolution intervals
    private static final Pattern INDEX_EXTRACTOR = Pattern.compile("(%d+).*"); //$NON-NLS-1$

    private final ITmfTrace fTrace;
    private List<IConfigurableMarkerEventSource> fMarkerEventSources;
    private CustomAnnotationProvider fProvider;

    /**
     * Constructor
     *
     * @param trace
     *            the trace
     */
    public ConfigurableMarkerEventSource(ITmfTrace trace) {
        fTrace = trace;
        updateMarkerSet();
        TmfSignalManager.register(this);
    }

    @Override
    public void dispose() {
        TmfSignalManager.deregister(this);
    }

    /**
     * Configure the marker source from the specified marker set
     *
     * @param markerSet
     *            the marker set, or null to clear the configuration
     * @return The list of marker sources
     */
    public List<IConfigurableMarkerEventSource> configure(MarkerSet markerSet) {
        fProvider.configure(markerSet);
        List<@NonNull IConfigurableMarkerEventSource> singletonList = Objects.requireNonNull(Collections.singletonList(new ConfigurableMarkerSource(fProvider)));
        fMarkerEventSources = singletonList;
        return singletonList;
    }

    private class ConfigurableMarkerSource implements IConfigurableMarkerEventSource {

        private final IOutputAnnotationProvider fAnnotationProvider;
        private boolean fHasError = false;

        public ConfigurableMarkerSource(IOutputAnnotationProvider provider) {
            fAnnotationProvider = provider;
        }

        @Override
        public @NonNull List<@NonNull String> getMarkerCategories() {
            TimeQueryFilter timeQueryFilter = new TimeQueryFilter(0, Long.MAX_VALUE, 2);
            List<@NonNull String> categories = new ArrayList<>();
            Map<@NonNull String, @NonNull Object> fetchParameters = FetchParametersUtils.timeQueryToMap(timeQueryFilter);
            TmfModelResponse<@NonNull AnnotationCategoriesModel> response = fAnnotationProvider.fetchAnnotationCategories(fetchParameters, new NullProgressMonitor());
            AnnotationCategoriesModel model = response.getModel();
            if (model != null) {
                categories.addAll(model.getAnnotationCategories());
            }
            return categories;
        }

        @SuppressWarnings({"restriction"})
        @Override
        public @NonNull List<@NonNull IMarkerEvent> getMarkerList(@NonNull String category, long startTime, long endTime, long resolution, @NonNull IProgressMonitor monitor) {
            if (startTime >= endTime) {
                return Collections.emptyList();
            }
            TimeQueryFilter filter = new TimeQueryFilter(StateSystemUtils.getTimes(startTime, endTime, resolution));
            TmfModelResponse<@NonNull AnnotationModel> response = fAnnotationProvider.fetchAnnotations(FetchParametersUtils.timeQueryToMap(filter), monitor);
            AnnotationModel model = response.getModel();
            if (model == null) {
                return Collections.emptyList();
            }
            Map<@NonNull String, @NonNull Collection<@NonNull Annotation>> annotationsMap = model.getAnnotations();
            List<@NonNull IMarkerEvent> markers = new ArrayList<>();
            Collection<Annotation> collection = annotationsMap.get(category);
            if (collection != null) {
                StyleManager sm = StyleManager.empty();
                for (Annotation annotation : collection) {
                    OutputElementStyle style = annotation.getStyle();
                    if (style != null) {
                        String indexStr = annotation.getLabel();
                        Matcher matcher = INDEX_EXTRACTOR.matcher(indexStr);
                        long index = annotation.getStartTime();
                        if (matcher.matches()) {
                            index = Long.parseLong(matcher.group(1));
                        }
                        boolean isApplicable = true;
                        String label = indexStr;
                        if (fAnnotationProvider instanceof PeriodicAnnotationProvider) {
                            PeriodicAnnotationProvider source = (PeriodicAnnotationProvider) fAnnotationProvider;
                            isApplicable = source.isApplicable(index);
                            if (!fHasError) {
                                try {
                                    label = String.format(indexStr, source.getBaseIndex() + index);
                                } catch (IllegalFormatException e) {
                                    Activator.getDefault().logError("Cannot format label for periodic marker ", e); //$NON-NLS-1$
                                    fHasError = true;
                                }
                            }
                        }
                        if (fAnnotationProvider instanceof CustomAnnotationProvider) {
                            CustomAnnotationProvider customAnnotationProvider = (CustomAnnotationProvider) fAnnotationProvider;
                            Map<String, String> formatters = customAnnotationProvider.getLabel();
                            String format = formatters.get(category);
                            if (!fHasError && format != null) {
                                try {
                                    long val = Long.decode(indexStr);
                                    label = String.format(format, val);
                                } catch (NumberFormatException | IllegalFormatException e) {
                                    Activator.getDefault().logError("Cannot format label for custom marker ", e); //$NON-NLS-1$
                                    fHasError = true;
                                }
                            }
                        }
                        if (isApplicable) {
                            annotation = new Annotation(annotation.getTime(), annotation.getDuration(), annotation.getEntryId(), label, style);
                            RGBAColor rgbaColor = sm.getColorStyle(style, StyleProperties.COLOR);
                            if (rgbaColor != null) {
                                RGBA color = RGBAUtil.fromRGBAColor(rgbaColor);
                                // set to false for now
                                MarkerEvent marker = new MarkerEvent(null, annotation.getTime(), annotation.getDuration(), category, color, label, false);
                                markers.add(marker);
                            }
                        }
                    }
                }
            }

            return markers;
        }

        @Override
        public List<SubMarker> getSubMarkers() {
            if (fAnnotationProvider instanceof PeriodicAnnotationProvider) {
                return ((PeriodicAnnotationProvider) fAnnotationProvider).getMarker().getSubMarkers();
            }
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getMarkerCategories() {
        Set<String> categories = new LinkedHashSet<>();
        for (IConfigurableMarkerEventSource source : fMarkerEventSources) {
            categories.addAll(source.getMarkerCategories());
            getSubMarkerCategories(categories, source.getSubMarkers());
        }
        return checkNotNull(Lists.newArrayList(categories));
    }

    private void getSubMarkerCategories(Set<String> categories, List<SubMarker> subMarkers) {
        for (SubMarker subMarker : subMarkers) {
            categories.add(subMarker.getName());
            getSubMarkerCategories(categories, subMarker.getSubMarkers());
            if (subMarker instanceof WeightedMarker) {
                for (MarkerSegment segment : ((WeightedMarker) subMarker).getSegments()) {
                    getSubMarkerCategories(categories, segment.getSubMarkers());
                }
            }
        }
    }

    @Override
    public List<IMarkerEvent> getMarkerList(String category, long startTime, long endTime, long resolution, IProgressMonitor monitor) {
        List<@NonNull IMarkerEvent> markerList = new ArrayList<>();
        for (IConfigurableMarkerEventSource source : fMarkerEventSources) {
            long minDuration = resolution * MIN_PERIOD;
            List<@NonNull IMarkerEvent> list = source.getMarkerList(category, startTime, endTime, resolution, monitor);
            for (IMarkerEvent markerEvent : list) {
                if (markerEvent.getDuration() > minDuration) {
                    markerList.add(markerEvent);
                }
            }
        }
        markerList.sort(Comparator.comparingLong(IMarkerEvent::getTime));
        return markerList;
    }

    @Override
    public List<IMarkerEvent> getMarkerList(long startTime, long endTime, long resolution, IProgressMonitor monitor) {
        List<@NonNull IMarkerEvent> markerList = new ArrayList<>();
        for (IConfigurableMarkerEventSource source : fMarkerEventSources) {
            long minDuration = resolution * MIN_PERIOD;
            List<@NonNull IMarkerEvent> list = source.getMarkerList(startTime, endTime, resolution, monitor);
            for (IMarkerEvent markerEvent : list) {
                if (markerEvent.getDuration() > minDuration) {
                    markerList.add(markerEvent);
                }
            }
        }
        markerList.sort(Comparator.comparingLong(IMarkerEvent::getTime));
        return markerList;
    }

    private static interface IConfigurableMarkerEventSource extends IMarkerEventSource {
        public List<SubMarker> getSubMarkers();
    }

    /**
     * A marker event source has been updated
     *
     * @param signal
     *            the signal
     */
    @TmfSignalHandler
    public void markerEventSourceUpdated(final TmfMarkerEventSourceUpdatedSignal signal) {
        updateMarkerSet();
    }

    private final void updateMarkerSet() {
        MarkerSet defaultMarkerSet = MarkerUtils.getDefaultMarkerSet();
        fProvider = new CustomAnnotationProvider(Objects.requireNonNull(fTrace), defaultMarkerSet);
        fMarkerEventSources = configure(defaultMarkerSet);
    }
}
