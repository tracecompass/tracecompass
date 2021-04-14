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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.Fraction;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.markers.Marker;
import org.eclipse.tracecompass.internal.tmf.core.markers.Marker.PeriodicMarker;
import org.eclipse.tracecompass.internal.tmf.core.markers.SubMarker;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.markers.ITimeReference;
import org.eclipse.tracecompass.tmf.core.markers.TimeReference;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.RangeSet;

/**
 * Data provider for Periodic Markers (frames)
 *
 * @author Matthew Khouzam
 * @since 6.3
 */
public class PeriodicAnnotationSource implements IOutputAnnotationProvider {
    private static final TmfModelResponse<AnnotationModel> EMPTY_MODEL_RESPONSE = new TmfModelResponse<>(new AnnotationModel(Collections.emptyMap()), Status.COMPLETED, ""); //$NON-NLS-1$
    private final String fCategory;
    private final double fPeriod;
    private final long fPeriodInteger;
    private @Nullable Fraction fPeriodFraction;
    private final long fRollover;
    private final RGBAColor fColor1;
    private final @Nullable RGBAColor fColor2;
    private ITimeReference fReference;
    /**
     * TODO: remove later
     */
    private final Marker fMarker;

    /**
     * Creates a data provider for periodic markers. They alternate in color and
     * can have sub markers.
     *
     * @param category
     *            the category marker, to reference submarkers and such
     * @param index
     *            the index (number) of the reference frame
     * @param time
     *            the time of the reference frame frame - the time of the frame refered by index.
     * @param period
     *            the period of the markers, in ns
     * @param rollover
     *            the rollover for the index. If it is 100, the indexes will
     *            increment 98->99->0-> ...
     * @param color1
     *            the even color
     * @param color2
     *            the odd color
     */
    public PeriodicAnnotationSource(Marker category, long index, long time, double period, long rollover, RGBAColor color1, @Nullable RGBAColor color2) {
        fCategory = String.valueOf(category.getName());
        fMarker = category;
        fReference = new TimeReference(time, index);
        fColor1 = color1;
        fColor2 = color2;
        fPeriod = period;
        fPeriodInteger = (long) period;
        try {
            fPeriodFraction = Fraction.getFraction(fPeriod - fPeriodInteger);
        } catch (ArithmeticException e) {
            /* can't convert to fraction, use floating-point arithmetic */
            fPeriodFraction = null;
        }
        fRollover = rollover;
    }

    /**
     * Creates a data provider for periodic markers. They alternate in color and
     * can have sub markers.
     *
     * @param category
     *            the category Name
     * @param index
     *            the index (number) of the starting frame
     * @param time
     *            the time of the starting frame
     * @param period
     *            the period of the markers, in ns
     * @param rollover
     *            the rollover for the index. If it is 100, the indexes will
     *            increment 98->99->0-> ...
     * @param color1
     *            the even color
     * @param color2
     *            the odd color
     */
    public PeriodicAnnotationSource(String category, long index, long time, double period, long rollover, RGBAColor color1, @Nullable RGBAColor color2) {
        this(new Marker(category, "black") {}, index, time, period, rollover, color1, color2); //$NON-NLS-1$
    }

    /*
     * Adjust to a reference that is closer to the start time, to avoid rounding
     * errors in floating point calculations with large numbers.
     */
    private ITimeReference adjustReference(ITimeReference baseReference, long time) {
        long offsetIndex = (long) ((time - baseReference.getTime()) / fPeriod);
        long offsetTime = 0;
        Fraction fraction = fPeriodFraction;
        if (fraction != null) {
            /*
             * If period = int num/den, find an offset index that is an exact
             * multiple of den and calculate index * period = (index * int) +
             * (index / den * num), all exact calculations.
             */
            offsetIndex = offsetIndex - offsetIndex % fraction.getDenominator();
            offsetTime = offsetIndex * fPeriodInteger + offsetIndex / fraction.getDenominator() * fraction.getNumerator();
        } else {
            /*
             * Couldn't compute fractional part as fraction, use simple
             * multiplication but with possible rounding error.
             */
            offsetTime = Math.round(offsetIndex * fPeriod);
        }
        return new TimeReference(baseReference.getTime() + offsetTime, baseReference.getIndex() + offsetIndex);
    }

    @Override
    public @NonNull TmfModelResponse<@NonNull AnnotationCategoriesModel> fetchAnnotationCategories(@NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        List<@NonNull String> categories = new ArrayList<>();
        categories.add(fCategory);
        getCategories(categories, getMarker());
        /*
         * We can have different sub-markers of the same category, we need them
         * to be distinct
         */
        categories = categories.stream().distinct().collect(Collectors.toList());
        return new TmfModelResponse<>(
                new AnnotationCategoriesModel(categories), Status.COMPLETED, ""); //$NON-NLS-1$
    }

    private void getCategories(List<@NonNull String> categories, Marker marker) {
        for (SubMarker sm : marker.getSubMarkers()) {
            categories.add(String.valueOf(sm.getName()));
            getCategories(categories, sm);
        }
    }

    @Override
    public @NonNull TmfModelResponse<@NonNull AnnotationModel> fetchAnnotations(@NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        List<Long> times = DataProviderParameterUtils.extractTimeRequested(fetchParameters);
        if (times == null) {
            return EMPTY_MODEL_RESPONSE;
        }
        int size = times.size() - 1;
        long startTime = times.get(0);
        long endTime = times.get(size);
        long resolution = (endTime - startTime) / (size);
        if (startTime > endTime) {
            return EMPTY_MODEL_RESPONSE;
        }
        long step = Math.max(Math.round(fPeriod), resolution);
        OutputElementStyle[] styles = new OutputElementStyle[2];
        styles[0] = generateOutputElementStyle(fColor1);
        RGBAColor color2 = fColor2;
        styles[1] = color2 == null ? styles[0] : generateOutputElementStyle(color2);

        Collection<Annotation> annotations = new ArrayList<>();
        /* Subtract 1.5 periods to ensure previous marker is included */
        long time = startTime - Math.max(Math.round(1.5 * fPeriod), resolution);
        ITimeReference reference = adjustReference(fReference, time);
        Annotation annotation = null;
        while (true) {
            long index = Math.round((time - reference.getTime()) / fPeriod) + reference.getIndex();
            long markerTime = Math.round((index - reference.getIndex()) * fPeriod) + reference.getTime();
            long duration = (fColor2 == null) ? 0 : Math.round((index + 1 - reference.getIndex()) * fPeriod) + reference.getTime() - markerTime;

            long labelIndex = index;
            if (fRollover != 0) {
                labelIndex %= fRollover;
                if (labelIndex < 0) {
                    labelIndex += fRollover;
                }
            }
            /* Add previous marker if current is visible */
            if ((markerTime >= startTime || markerTime + duration > startTime) && annotation != null) {
                annotations.add(annotation);
            }
            if (isApplicable(labelIndex)) {
                OutputElementStyle style = Objects.requireNonNull((index % 2) == 0 ? styles[0] : styles[1]);
                annotation = new Annotation(markerTime, duration, -1, String.valueOf(labelIndex), style);
            } else {
                annotation = null;
            }
            if (markerTime > endTime || (monitor != null && monitor.isCanceled())) {
                if (annotation != null && isApplicable(labelIndex)) {
                    /* The next marker out of range is included */
                    annotations.add(annotation);
                }
                break;
            }

            time += step;
        }
        Map<String, Collection<Annotation>> model = new HashMap<>();
        model.put(fCategory, annotations);
        return new TmfModelResponse<>(new AnnotationModel(model), Status.COMPLETED, ""); //$NON-NLS-1$
    }

    private static OutputElementStyle generateOutputElementStyle(RGBAColor color) {
        String colorString = color.toString().substring(0, 7);
        return new OutputElementStyle(null,
                ImmutableMap.of(StyleProperties.STYLE_NAME, colorString,
                        StyleProperties.COLOR, colorString,
                        StyleProperties.OPACITY, (float) (color.getAlpha() / 255.0)));
    }

    /**
     * Period
     *
     * @return period in ns
     */
    public double getPeriod() {
        return fPeriod;
    }

    /**
     * Get the marker model
     *
     * @return the marker that this annotation provider is modeled on
     */
    public Marker getMarker() {
        return fMarker;
    }

    /**
     * Get the base index (starting index) for a marker
     *
     * @return the base index
     */
    public long getBaseIndex() {
        Marker marker = getMarker();
        if (marker instanceof PeriodicMarker) {
            return ((PeriodicMarker) marker).getOffset();
        }
        return 0;
    }

    /**
     * Is this index applicable?
     *
     * @param index
     *            the index to look at
     * @return true if it applies
     */
    public boolean isApplicable(long index) {
        Marker marker = getMarker();
        if (marker instanceof PeriodicMarker) {
            PeriodicMarker periodicMarker = (PeriodicMarker) marker;
            RangeSet<@Nullable Long> indexRange = periodicMarker.getIndexRange();
            if (indexRange != null) {
                return indexRange.contains(periodicMarker.getOffset() + index);
            }
        }
        return true;
    }
}
