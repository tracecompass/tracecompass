/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.markers;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.tracecompass.internal.tmf.core.markers.IMarkerConstants;
import org.eclipse.tracecompass.internal.tmf.core.markers.Marker;
import org.eclipse.tracecompass.internal.tmf.core.markers.Marker.PeriodicMarker;
import org.eclipse.tracecompass.internal.tmf.core.markers.MarkerSegment;
import org.eclipse.tracecompass.internal.tmf.core.markers.MarkerSet;
import org.eclipse.tracecompass.internal.tmf.core.markers.SubMarker;
import org.eclipse.tracecompass.internal.tmf.core.markers.SubMarker.SplitMarker;
import org.eclipse.tracecompass.internal.tmf.core.markers.SubMarker.WeightedMarker;
import org.eclipse.tracecompass.tmf.core.trace.ICyclesConverter;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.colors.X11Color;
import org.eclipse.tracecompass.tmf.ui.markers.IMarkerReferenceProvider;
import org.eclipse.tracecompass.tmf.ui.markers.PeriodicMarkerEventSource;
import org.eclipse.tracecompass.tmf.ui.markers.PeriodicMarkerEventSource.Reference;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEventSource;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.MarkerEvent;

import com.google.common.collect.Lists;
import com.google.common.collect.RangeSet;

/**
 * Configurable marker event source.
 */
public class ConfigurableMarkerEventSource implements IMarkerEventSource {

    private static final long NANO_PER_MILLI = 1000000L;
    private static final long NANO_PER_MICRO = 1000L;
    private static final int MIN_PERIOD = 5; // in units of resolution intervals
    private static final int ALPHA = 10;
    private static final String COLOR_REGEX = "#[A-Fa-f0-9]{6}"; //$NON-NLS-1$

    private List<IConfigurableMarkerEventSource> fMarkerEventSources;
    private Map<Marker, RGBA> fColors = new HashMap<>();
    private final ITmfTrace fTrace;

    /**
     * Constructor
     *
     * @param trace
     *            the trace
     */
    public ConfigurableMarkerEventSource(ITmfTrace trace) {
        fMarkerEventSources = new ArrayList<>();
        fTrace = trace;
    }

    /**
     * Configure the marker source from the specified marker set
     *
     * @param markerSet
     *            the marker set, or null to clear the configuration
     */
    public void configure(MarkerSet markerSet) {
        fMarkerEventSources.clear();
        if (markerSet != null) {
            for (Marker marker : markerSet.getMarkers()) {
                configure(marker);
            }
        }
    }

    private void configure(Marker marker) {
        if (marker instanceof PeriodicMarker) {
            PeriodicMarker periodicMarker = (PeriodicMarker) marker;
            String referenceId = periodicMarker.getReferenceId();
            Reference baseReference = null;
            if (fTrace instanceof IAdaptable && !referenceId.isEmpty()) {
                @Nullable IMarkerReferenceProvider adapter = ((IAdaptable) fTrace).getAdapter(IMarkerReferenceProvider.class);
                if (adapter != null) {
                    baseReference = adapter.getReference(referenceId);
                }
            }
            if (baseReference == null) {
                baseReference = Reference.ZERO;
            }
            long rollover = periodicMarker.getRange().hasUpperBound() ? (periodicMarker.getRange().upperEndpoint() - periodicMarker.getRange().lowerEndpoint() + 1) : 0;
            RGBA evenColor = getColor(periodicMarker);
            RGBA oddColor = getOddColor(evenColor);
            double period = convertToNanos(periodicMarker.getPeriod(), periodicMarker.getUnit());
            Reference reference = new Reference(baseReference.getTime() + Math.round(convertToNanos(periodicMarker.getOffset(), periodicMarker.getUnit())), baseReference.getIndex());
            ConfigurablePeriodicMarkerEventSource markerEventSource = new ConfigurablePeriodicMarkerEventSource(marker, checkNotNull(periodicMarker.getName()), reference, period, rollover, evenColor, oddColor, false, periodicMarker.getRange().lowerEndpoint(), checkNotNull(periodicMarker.getLabel()), periodicMarker.getIndexRange());
            fMarkerEventSources.add(markerEventSource);
        }
    }

    private double convertToNanos(double number, String unit) {
        if (unit.equalsIgnoreCase(IMarkerConstants.MS)) {
            return number * NANO_PER_MILLI;
        } else if (unit.equalsIgnoreCase(IMarkerConstants.US)) {
            return number * NANO_PER_MICRO;
        } else if (unit.equalsIgnoreCase(IMarkerConstants.NS)) {
            return number;
        } else if (unit.equalsIgnoreCase(IMarkerConstants.CYCLES) &&
                fTrace instanceof IAdaptable) {
            ICyclesConverter adapter = ((IAdaptable) fTrace).getAdapter(ICyclesConverter.class);
            if (adapter != null) {
                return adapter.cyclesToNanos((long) number);
            }
        }
        return number;
    }

    private @NonNull RGBA getColor(Marker marker) {
        RGBA color = fColors.get(marker);
        if (color == null) {
            color = parseColor(marker.getColor());
            fColors.put(marker, color);
        }
        return color;
    }

    private static @NonNull RGBA getOddColor(RGBA color) {
        return new RGBA(color.rgb.red, color.rgb.green, color.rgb.blue, 0);
    }

    private static @NonNull RGBA parseColor(String color) {
        RGB rgb = null;
        if (color.matches(COLOR_REGEX)) {
            rgb = new RGB(Integer.valueOf(color.substring(1, 3), 16),
                    Integer.valueOf(color.substring(3, 5), 16),
                    Integer.valueOf(color.substring(5, 7), 16));
        } else {
            rgb = X11Color.toRGB(color);
            if (rgb == null) {
                rgb = new RGB(0, 0, 0);
            }
        }
        return new RGBA(rgb.red, rgb.green, rgb.blue, ALPHA);
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
        return getMarkerList(startTime, endTime, resolution, monitor).stream()
                .filter((marker) -> marker.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    @Override
    public List<IMarkerEvent> getMarkerList(long startTime, long endTime, long resolution, IProgressMonitor monitor) {
        @NonNull List<@NonNull IMarkerEvent> markerList = new ArrayList<>();
        for (IConfigurableMarkerEventSource source : fMarkerEventSources) {
            long minDuration = resolution * MIN_PERIOD;
            if (source.getMaxDuration() > minDuration) {
                @NonNull List<@NonNull IMarkerEvent> list = source.getMarkerList(startTime, endTime, resolution, monitor);
                for (IMarkerEvent markerEvent : list) {
                    for (SubMarker subMarker : source.getSubMarkers()) {
                        getSubMarkerList(subMarker, markerEvent, markerList, startTime, endTime, minDuration);
                    }
                    markerList.add(markerEvent);
                }
            }
        }
        markerList.sort(Comparator.comparingLong(marker -> marker.getTime()));
        return markerList;
    }

    private void getSubMarkerList(SubMarker subMarker, IMarkerEvent markerEvent, @NonNull List<@NonNull IMarkerEvent> markerList, long startTime, long endTime, long minDuration) {
        if (subMarker instanceof SplitMarker) {
            getSubMarkerList((SplitMarker) subMarker, markerEvent, markerList, startTime, endTime, minDuration);
        } else if (subMarker instanceof WeightedMarker) {
            getSubMarkerList((WeightedMarker) subMarker, markerEvent, markerList, startTime, endTime, minDuration);
        }
    }

    private void getSubMarkerList(SplitMarker splitMarker, IMarkerEvent markerEvent, @NonNull List<@NonNull IMarkerEvent> markerList, long startTime, long endTime, long minDuration) {
        if (markerEvent.getTime() > endTime || markerEvent.getTime() + markerEvent.getDuration() < startTime) {
            return;
        }
        long lower = splitMarker.getRange().lowerEndpoint();
        long upper = splitMarker.getRange().upperEndpoint();
        long segments = upper - lower + 1;
        long start = markerEvent.getTime();
        for (int i = 0; i < segments; i++) {
            long end = markerEvent.getTime() + Math.round((double) (i + 1) / segments * markerEvent.getDuration());
            long duration = end - start;
            long labelIndex = lower + i;
            if (end >= startTime && duration > minDuration && splitMarker.getIndexRange().contains(labelIndex)) {
                RGBA color = (labelIndex & 1) == 0 ? getColor(splitMarker) : getOddColor(getColor(splitMarker));
                IMarkerEvent subMarkerEvent = new MarkerEvent(null, start, end - start, splitMarker.getName(), color, String.format(splitMarker.getLabel(), labelIndex), false);
                for (SubMarker subMarker : splitMarker.getSubMarkers()) {
                    getSubMarkerList(subMarker, subMarkerEvent, markerList, startTime, endTime, minDuration);
                }
                markerList.add(subMarkerEvent);
            }
            if (start >= endTime) {
                break;
            }
            start = end;
        }
    }

    private void getSubMarkerList(WeightedMarker weightedMarker, IMarkerEvent markerEvent, @NonNull List<@NonNull IMarkerEvent> markerList, long startTime, long endTime, long minDuration) {
        if (markerEvent.getTime() > endTime || markerEvent.getTime() + markerEvent.getDuration() < startTime) {
            return;
        }
        long start = markerEvent.getTime();
        long length = 0;
        for (int i = 0; i < weightedMarker.getSegments().size(); i++) {
            MarkerSegment segment = weightedMarker.getSegments().get(i);
            length += segment.getLength();
            long end = markerEvent.getTime() + Math.round((length / (double) weightedMarker.getTotalLength()) * markerEvent.getDuration());
            long duration = end - start;
            if (end >= startTime && duration > minDuration && !segment.getColor().isEmpty()) {
                RGBA color = getColor(segment);
                IMarkerEvent subMarkerEvent = new MarkerEvent(null, start, end - start, weightedMarker.getName(), color, String.format(segment.getLabel(), i), false);
                for (SubMarker subMarker : segment.getSubMarkers()) {
                    getSubMarkerList(subMarker, subMarkerEvent, markerList, startTime, endTime, minDuration);
                }
                for (SubMarker subMarker : weightedMarker.getSubMarkers()) {
                    getSubMarkerList(subMarker, subMarkerEvent, markerList, startTime, endTime, minDuration);
                }
                markerList.add(subMarkerEvent);
            }
            if (start >= endTime) {
                break;
            }
            start = end;
        }
    }

    private static interface IConfigurableMarkerEventSource extends IMarkerEventSource {
        double getMaxDuration();

        public List<SubMarker> getSubMarkers();
    }

    private class ConfigurablePeriodicMarkerEventSource extends PeriodicMarkerEventSource implements IConfigurableMarkerEventSource {

        private final Marker fMarker;
        private final long fStartIndex;
        private final String fLabel;
        private final RangeSet<Long> fIndexRange;
        private final double fMaxDuration;

        public ConfigurablePeriodicMarkerEventSource(Marker marker, @NonNull String category, @NonNull Reference reference, double period, long rollover, @NonNull RGBA evenColor, @NonNull RGBA oddColor, boolean foreground, long startIndex, @NonNull String label, RangeSet<Long> indexRange) {
            super(category, reference, period, rollover, evenColor, oddColor, foreground);
            fMarker = marker;
            fStartIndex = startIndex;
            fLabel = label;
            fIndexRange = indexRange;
            fMaxDuration = period;
        }

        @Override
        public @NonNull String getMarkerLabel(long index) {
            return checkNotNull(String.format(fLabel, fStartIndex + index));
        }

        @Override
        public boolean isApplicable(long index) {
            if (fIndexRange != null) {
                return fIndexRange.contains(fStartIndex + index);
            }
            return true;
        }

        @Override
        public double getMaxDuration() {
            return fMaxDuration;
        }

        @Override
        public List<SubMarker> getSubMarkers() {
            return fMarker.getSubMarkers();
        }
    }
}
