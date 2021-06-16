/*******************************************************************************
 * Copyright (c) 2021 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.markers.IMarkerConstants;
import org.eclipse.tracecompass.internal.tmf.core.markers.Marker;
import org.eclipse.tracecompass.internal.tmf.core.markers.Marker.PeriodicMarker;
import org.eclipse.tracecompass.internal.tmf.core.markers.MarkerSegment;
import org.eclipse.tracecompass.internal.tmf.core.markers.MarkerSet;
import org.eclipse.tracecompass.internal.tmf.core.markers.SubMarker;
import org.eclipse.tracecompass.internal.tmf.core.markers.SubMarker.SplitMarker;
import org.eclipse.tracecompass.internal.tmf.core.markers.SubMarker.WeightedMarker;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.dataprovider.X11ColorUtils;
import org.eclipse.tracecompass.tmf.core.markers.ITimeReference;
import org.eclipse.tracecompass.tmf.core.markers.ITimeReferenceProvider;
import org.eclipse.tracecompass.tmf.core.markers.TimeReference;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Custom Annotation Provider
 *
 * @author Matthew Khouzam
 */
public class CustomAnnotationProvider implements IOutputAnnotationProvider {

    private static final String DEFAULT_COLOR = "blue"; //$NON-NLS-1$
    private static final long MIN_PERIOD = 5; // in units of resolution
                                              // intervals
    private static final int ALPHA = 10;
    private static final String COLOR_REGEX = "#[A-Fa-f0-9]{6}"; //$NON-NLS-1$

    private List<IOutputAnnotationProvider> fMarkerEventSources = new ArrayList<>();
    private Map<Marker, RGBAColor> fColors = new HashMap<>();
    private final @Nullable ITmfTrace fTrace;
    private @Nullable MarkerSet fMarkerSet;

    /**
     * Custom Annotation Provider for a given marker set
     *
     * @param trace
     *            the trace to make the annotations on. Used to get the start
     *            time, if null, the epoch is assumed to be the reference time.
     * @param markerSet
     *            the marker set, a definition of a group of configurable
     *            markers
     */
    public CustomAnnotationProvider(@Nullable ITmfTrace trace, @Nullable MarkerSet markerSet) {
        fTrace = trace;
        fMarkerSet = markerSet;
        fMarkerEventSources = configure(markerSet);
    }

    /**
     * Configure the marker source from the specified marker set
     *
     * @param markerSet
     *            the marker set, or null to clear the configuration
     * @return the list of {@link IOutputAnnotationProvider}s for this marker
     *         set
     */
    public List<IOutputAnnotationProvider> configure(@Nullable MarkerSet markerSet) {
        List<IOutputAnnotationProvider> markerEventSources = new ArrayList<>();
        if (markerSet != null) {
            for (Marker marker : markerSet.getMarkers()) {
                markerEventSources.add(configure(Objects.requireNonNull(marker)));
            }
        }
        fMarkerEventSources = markerEventSources;
        fMarkerSet = markerSet;
        return markerEventSources;
    }

    private IOutputAnnotationProvider configure(Marker marker) {
        if (marker instanceof PeriodicMarker) {
            PeriodicMarker periodicMarker = (PeriodicMarker) marker;
            long rollover = periodicMarker.getRange().hasUpperBound() ? (periodicMarker.getRange().upperEndpoint() - periodicMarker.getRange().lowerEndpoint() + 1) : 0;
            RGBAColor evenColor = getColor(marker);
            RGBAColor oddColor = getOddColor(evenColor);
            ITmfTrace trace = fTrace;
            double period = IMarkerConstants.convertToNanos(periodicMarker.getPeriod(), periodicMarker.getUnit(), trace);
            String referenceId = periodicMarker.getReferenceId();
            ITimeReference baseReference = null;
            if (trace instanceof IAdaptable && !referenceId.isEmpty()) {
                ITimeReferenceProvider adapter = ((IAdaptable) trace).getAdapter(ITimeReferenceProvider.class);
                if (adapter != null) {
                    baseReference = adapter.apply(referenceId);
                }
            }
            if (baseReference == null) {
                baseReference = ITimeReference.ZERO;
            }
            ITimeReference reference = new TimeReference(baseReference.getTime() + Math.round(IMarkerConstants.convertToNanos(periodicMarker.getOffset(), periodicMarker.getUnit(), trace)), baseReference.getIndex());
            return new PeriodicAnnotationSource(marker, reference.getIndex(), reference.getTime(), period, rollover, evenColor, oddColor);
        }
        throw new IllegalArgumentException("Marker must be of type PeriodicMarker or SubMarker"); //$NON-NLS-1$

    }

    private void getSubMarkerList(SubMarker subMarker, Annotation markerEvent, Map<String, Collection<@NonNull Annotation>> markerMap, long startTime, long endTime, long minDuration, Long[] times) {
        if (subMarker instanceof SplitMarker) {
            getSubMarkerList((SplitMarker) subMarker, markerEvent, markerMap, startTime, endTime, minDuration, times);
        } else if (subMarker instanceof WeightedMarker) {
            getSubMarkerList((WeightedMarker) subMarker, markerEvent, markerMap, startTime, endTime, minDuration, times);
        }
    }

    private void getSubMarkerList(SplitMarker splitMarker, Annotation markerEvent, Map<String, Collection<@NonNull Annotation>> annotationMap, long startTime, long endTime, long minDuration, Long[] times) {
        if (markerEvent.getTime() > endTime || markerEvent.getTime() + markerEvent.getDuration() < startTime) {
            return;
        }
        long lower = splitMarker.getRange().lowerEndpoint();
        long upper = splitMarker.getRange().upperEndpoint();
        long segments = upper - lower + 1;
        long start = markerEvent.getTime();
        List<@NonNull Annotation> annotationList = new ArrayList<>();
        for (int i = 0; i < segments; i++) {
            long end = markerEvent.getTime() + Math.round((double) (i + 1) / segments * markerEvent.getDuration());
            long duration = end - start;
            long labelIndex = lower + i;
            if (end >= startTime && duration > minDuration && splitMarker.getIndexRange().contains(labelIndex)) {
                RGBAColor color = (labelIndex & 1) == 0 ? getColor(splitMarker) : getOddColor(getColor(splitMarker));
                OutputElementStyle outputStyle = getOutputStyle(splitMarker, color);
                Annotation subAnnotation = new Annotation(start, end - start, -1, String.format(splitMarker.getLabel(), labelIndex), outputStyle);
                for (SubMarker subMarker : splitMarker.getSubMarkers()) {
                    getSubMarkerList(Objects.requireNonNull(subMarker), subAnnotation, annotationMap, startTime, endTime, minDuration, times);
                }
                annotationList.add(subAnnotation);
            }
            if (start >= endTime) {
                break;
            }
            start = end;
        }
        populateMap(annotationMap, annotationList, Objects.requireNonNull(splitMarker.getName()));
    }

    private void getSubMarkerList(WeightedMarker weightedMarker, Annotation markerEvent, Map<String, Collection<@NonNull Annotation>> annotationMap, long startTime, long endTime, long minDuration, Long[] times) {
        if (markerEvent.getTime() > endTime || markerEvent.getTime() + markerEvent.getDuration() < startTime) {
            return;
        }
        long start = markerEvent.getTime();
        long length = 0;
        List<@NonNull Annotation> annotationsList = new ArrayList<>();
        for (int i = 0; i < weightedMarker.getSegments().size(); i++) {
            MarkerSegment segment = weightedMarker.getSegments().get(i);
            length += segment.getLength();
            long end = markerEvent.getTime() + Math.round((length / (double) weightedMarker.getTotalLength()) * markerEvent.getDuration());
            long duration = end - start;
            if (end >= startTime && duration > minDuration && !segment.getColor().isEmpty()) {
                RGBAColor color = getColor(segment);
                Annotation subAnnotation = new Annotation(start, end - start, -1, String.format(segment.getLabel(), i), getOutputStyle(segment, color));
                for (SubMarker subMarker : segment.getSubMarkers()) {
                    getSubMarkerList(Objects.requireNonNull(subMarker), subAnnotation, annotationMap, startTime, endTime, minDuration, times);
                }
                for (SubMarker subMarker : weightedMarker.getSubMarkers()) {
                    getSubMarkerList(Objects.requireNonNull(subMarker), subAnnotation, annotationMap, startTime, endTime, minDuration, times);
                }
                annotationsList.add(subAnnotation);
            }
            if (start >= endTime) {
                break;
            }
            start = end;
        }
        populateMap(annotationMap, annotationsList, Objects.requireNonNull(weightedMarker.getName()));
    }

    private static void populateMap(Map<String, Collection<@NonNull Annotation>> annotationMap, List<@NonNull Annotation> annotationsList, String name) {
        Collection<@NonNull Annotation> annotations = annotationMap.get(name);
        if (annotations != null) {
            Set<@NonNull Annotation> annotationSet = new HashSet<>();
            annotationSet.addAll(annotationsList);
            annotationSet.addAll(annotations);
            annotationMap.put(name, annotationSet);
        } else {
            annotationMap.put(name, annotationsList);
        }
    }

    private RGBAColor getColor(Marker marker) {
        RGBAColor color = fColors.get(marker);
        if (color == null) {
            color = parseColor(marker.getColor());
            fColors.put(marker, color);
        }
        return color;
    }

    private static RGBAColor getOddColor(RGBAColor color) {
        return new RGBAColor(color.getRed(), color.getGreen(), color.getBlue(), 0);
    }

    private static RGBAColor fromHexColor(@Nullable String color) {
        if (color != null && color.matches(COLOR_REGEX)) {
            return new RGBAColor(Integer.valueOf(color.substring(1, 3), 16),
                    Integer.valueOf(color.substring(3, 5), 16),
                    Integer.valueOf(color.substring(5, 7), 16), ALPHA);
        }
        return new RGBAColor(0, 0, 0, ALPHA);
    }

    private static RGBAColor parseColor(@Nullable String colorString) {
        if (colorString == null) {
            return fromHexColor(DEFAULT_COLOR);
        }
        if (colorString.matches(COLOR_REGEX)) {
            return fromHexColor(colorString);
        }
        return fromHexColor(X11ColorUtils.toHexColor(colorString));
    }

    private Map<@NonNull String, @NonNull Collection<@NonNull Annotation>> getMarkers(Map<@NonNull String, @NonNull Object> fetchParams, @Nullable IProgressMonitor monitor) {
        List<@NonNull Long> timeRequested = DataProviderParameterUtils.extractTimeRequested(fetchParams);
        if (timeRequested == null || timeRequested.size() < 2) {
            return Collections.emptyMap();
        }
        Long[] times = timeRequested.toArray(new Long[0]);
        long starttime = timeRequested.get(0);
        long endtime = timeRequested.get(timeRequested.size() - 1);
        int resolution = (int) Math.min(Integer.MAX_VALUE, timeRequested.get(1) - timeRequested.get(0));
        Map<@NonNull String, @NonNull Collection<@NonNull Annotation>> markerMap = new LinkedHashMap<>();
        for (IOutputAnnotationProvider source : fMarkerEventSources) {
            long minDuration = resolution * MIN_PERIOD;
            if (source instanceof PeriodicAnnotationSource) {
                PeriodicAnnotationSource periodicAnnotationSource = (PeriodicAnnotationSource) source;
                long maxDuration = (long) periodicAnnotationSource.getPeriod();
                if (maxDuration > minDuration) {
                    AnnotationModel model = periodicAnnotationSource.fetchAnnotations(fetchParams, monitor).getModel();
                    if (model != null) {
                        Map<@NonNull String, @NonNull Collection<@NonNull Annotation>> annotations = model.getAnnotations();
                        List<Annotation> markerList = new ArrayList<>();
                        for (Entry<@NonNull String, @NonNull Collection<@NonNull Annotation>> entryAnnotation : annotations.entrySet()) {
                            String category = Objects.requireNonNull(entryAnnotation.getKey());
                            for (Annotation annotation : Objects.requireNonNull(entryAnnotation.getValue())) {
                                markerList.add(annotation);
                                for (SubMarker subMarker : periodicAnnotationSource.getMarker().getSubMarkers()) {
                                    getSubMarkerList(Objects.requireNonNull(subMarker), annotation, markerMap, starttime, endtime, minDuration, times);

                                }
                            }
                            markerMap.put(category, markerList);
                        }
                    }
                }
            }
        }

        @Nullable Set<@NonNull String> categoriesRequested = DataProviderParameterUtils.extractSelectedCategories(fetchParams);
        markerMap.keySet().removeIf(cat -> categoriesRequested != null && !categoriesRequested.contains(cat));

        return markerMap;
    }



    private static OutputElementStyle getOutputStyle(Marker marker, RGBAColor color) {
        Map<@NonNull String, @NonNull Object> style = new HashMap<>();
        String name = marker.getName();
        if (name != null) {
            style.put(StyleProperties.STYLE_NAME, name);
        }
        style.put(StyleProperties.COLOR, color.toString().substring(0, 7));
        style.put(StyleProperties.OPACITY, (float) (color.getAlpha() / 255.0));
        return new OutputElementStyle(null, style);
    }

    @Override
    public @NonNull TmfModelResponse<@NonNull AnnotationCategoriesModel> fetchAnnotationCategories(@NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        List<@NonNull String> categories = new ArrayList<>();
        for (IOutputAnnotationProvider source : fMarkerEventSources) {
            TmfModelResponse<@NonNull AnnotationCategoriesModel> response = source.fetchAnnotationCategories(fetchParameters, monitor);
            AnnotationCategoriesModel model = response.getModel();
            if (model != null) {
                categories.addAll(model.getAnnotationCategories());
            }
        }
        return new TmfModelResponse<>(new AnnotationCategoriesModel(categories), Status.COMPLETED, ""); //$NON-NLS-1$
    }

    @Override
    public @NonNull TmfModelResponse<@NonNull AnnotationModel> fetchAnnotations(@NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        Map<@NonNull String, @NonNull Collection<@NonNull Annotation>> markers = getMarkers(fetchParameters, monitor);
        return new TmfModelResponse<>(new AnnotationModel(markers), Status.COMPLETED, ""); //$NON-NLS-1$
    }

    /**
     * Get Max Duration
     *
     * @return the maximum duration of all event sources
     */
    public double getMaxDuration() {
        double duration = 0;
        for (IOutputAnnotationProvider es : fMarkerEventSources) {
            if (es instanceof PeriodicAnnotationSource) {
                duration = Math.max(duration, ((PeriodicAnnotationSource) es).getPeriod());
            }
        }
        return duration;
    }

    /**
     * Get the label of the marker, may be a format string
     *
     * @return the label of the marker
     */
    public Map<String, String> getLabel() {
        Map<String, String> labels = new HashMap<>();
        if (fMarkerSet != null) {
            for (Marker marker : fMarkerSet.getMarkers()) {
                if (marker instanceof PeriodicMarker) {
                    PeriodicMarker periodicMarker = (PeriodicMarker) marker;
                    labels.put(Objects.requireNonNull(periodicMarker.getName()), Objects.requireNonNull(periodicMarker.getLabel()));
                }
            }
        }
        return labels;
    }
}
