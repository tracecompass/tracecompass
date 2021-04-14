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

package org.eclipse.tracecompass.tmf.ui.markers;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.Annotation;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.AnnotationModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.PeriodicAnnotationSource;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.markers.ITimeReference;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.ui.colors.RGBAUtil;
import org.eclipse.tracecompass.tmf.ui.model.StyleManager;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEventSource;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.MarkerEvent;

/**
 * Marker event source that produces periodic markers.
 *
 * @since 2.0
 */
@NonNullByDefault
public class PeriodicMarkerEventSource implements IMarkerEventSource {

    /**
     * Reference marker time and index
     */
    public static class Reference implements ITimeReference {

        /** Reference marker index 0 at time 0 */
        public static final Reference ZERO = new Reference(0L, 0);

        private final long time;
        private final long index;

        /**
         * Constructor
         *
         * @param time
         *            the reference marker time in time units
         * @param index
         *            the reference marker index
         */
        public Reference(long time, int index) {
            this.time = time;
            this.index = index;
        }

        /**
         * Constructor
         *
         * @param time
         *            the reference marker time in time units
         * @param index
         *            the reference marker index
         * @since 2.3
         */
        public Reference(long time, long index) {
            this.time = time;
            this.index = index;
        }

        @Override
        public long getTime() {
            return time;
        }

        @Override
        public long getIndex() {
            return index;
        }

        @Override
        public String toString() {
            return String.format("[%d, %d]", time, index); //$NON-NLS-1$
        }
    }

    private final String fCategory;
    private final boolean fForeground;
    private final PeriodicAnnotationSource fSource;

    /**
     * Constructs a periodic marker event source with line markers at period
     * boundaries.
     * <p>
     * The markers will have the given category and color. The reference defines
     * the marker with the given index to be at the specified time.
     *
     * @param category
     *            the marker category
     * @param reference
     *            the reference marker time and index
     * @param period
     *            the period in time units
     * @param rollover
     *            the number of periods before the index rolls-over to 0, or 0
     *            for no roll-over
     * @param color
     *            the marker color
     * @param foreground
     *            true if the marker is drawn in foreground, and false otherwise
     */
    public PeriodicMarkerEventSource(String category, Reference reference, double period, long rollover, RGBA color, boolean foreground) {
        this(category, reference, period, rollover, foreground, color, null);
    }

    /**
     * Constructs a periodic marker event source with alternating shading
     * markers.
     * <p>
     * The markers will have the given category. Periods will be shaded with the
     * first and second colors alternatively. The reference defines the marker
     * with the given index to be at the specified time. The reference will be
     * shaded with the first color if its index is even, or the second color if
     * it is odd.
     *
     * @param category
     *            the marker category
     * @param reference
     *            the reference marker time and index
     * @param period
     *            the period in time units
     * @param rollover
     *            the number of periods before the index rolls-over to 0, or 0
     *            for no roll-over
     * @param color1
     *            the first marker color
     * @param color2
     *            the second marker color
     * @param foreground
     *            true if the marker is drawn in foreground, and false otherwise
     */
    public PeriodicMarkerEventSource(String category, Reference reference, double period, long rollover, RGBA color1, RGBA color2, boolean foreground) {
        this(category, reference, period, rollover, foreground, color1, color2);
    }

    /*
     * Private constructor. The order of parameters is changed to make it
     * unique.
     */
    private PeriodicMarkerEventSource(String category, Reference reference, double period, long rollover, boolean foreground, RGBA color1, @Nullable RGBA color2) {
        if (period <= 0) {
            throw new IllegalArgumentException("period cannot be less than or equal to zero"); //$NON-NLS-1$
        }
        if (rollover < 0) {
            throw new IllegalArgumentException("rollover cannot be less than zero"); //$NON-NLS-1$
        }
        fSource = new PeriodicAnnotationSource(category, reference.getIndex(), reference.getTime(), period, rollover, Objects.requireNonNull(wrap(color1)), wrap(color2));
        fCategory = category;
        fForeground = foreground;
    }

    private static @Nullable RGBAColor wrap(@Nullable RGBA rgba) {
        if (rgba == null) {
            return null;
        }
        return new RGBAColor(rgba.rgb.red, rgba.rgb.green, rgba.rgb.blue, rgba.alpha);
    }

    @Override
    public List<String> getMarkerCategories() {
        return Arrays.asList(fCategory);
    }

    @Override
    public List<IMarkerEvent> getMarkerList(String category, long startTime, long endTime, long resolution, IProgressMonitor monitor) {
        if (startTime > endTime) {
            return Collections.emptyList();
        }
        StyleManager sm = StyleManager.empty();
        List<IMarkerEvent> markers = new ArrayList<>();
        if (resolution >= Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Cannot query " + resolution + " times"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        List<Long> times = StateSystemUtils.getTimes(startTime, endTime, resolution);
        Map<String, Object> query = new HashMap<>();
        query.put(DataProviderParameterUtils.REQUESTED_TIME_KEY, times);
        query.put(DataProviderParameterUtils.REQUESTED_MARKER_CATEGORIES_KEY, Collections.singleton(category));
        TmfModelResponse<AnnotationModel> annotations = fSource.fetchAnnotations(query, monitor);
        AnnotationModel model = annotations.getModel();
        if (model == null) {
            return markers;
        }
        Map<String, Collection<Annotation>> annotationsMap = model.getAnnotations();

        Collection<Annotation> collection = annotationsMap.get(category);
        if (collection != null) {
            for (Annotation annotation : collection) {
                OutputElementStyle style = annotation.getStyle();
                if (style != null) {
                    String indexStr = annotation.getLabel();
                    long index = Long.parseLong(indexStr);
                    if (isApplicable(index)) {
                        String label = getMarkerLabel(index);
                        annotation = new Annotation(annotation.getTime(), annotation.getDuration(), annotation.getEntryId(), label, style);
                        RGBAColor rgbaColor = sm.getColorStyle(style, StyleProperties.COLOR);
                        if (rgbaColor != null) {
                            RGBA color = RGBAUtil.fromRGBAColor(rgbaColor);
                            MarkerEvent marker = new MarkerEvent(null, annotation.getTime(), annotation.getDuration(), category, color, label, fForeground);
                            markers.add(marker);
                        }
                    }
                }
            }
        }
        return markers;
    }

    /**
     * Get the marker label for the given marker index.
     * <p>
     * This method can be overridden by clients.
     *
     * @param index
     *            the marker index
     * @return the marker label
     */
    public String getMarkerLabel(long index) {
        return checkNotNull(Long.toString(index));
    }

    /**
     * Returns true if the marker is applicable at the specified index.
     * <p>
     * This method can be overridden by clients. Returning false will
     * essentially filter-out the marker.
     *
     * @apiNote not used as far as we know, this API is not good for moving
     *          logic to core.
     *
     * @param index
     *            the marker index
     * @return true if the marker is applicable
     * @since 3.0
     */
    public boolean isApplicable(long index) {
        return true;
    }
}
