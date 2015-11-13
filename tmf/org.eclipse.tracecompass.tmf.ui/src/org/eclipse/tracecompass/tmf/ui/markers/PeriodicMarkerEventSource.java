/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.markers;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.RGBA;
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
    public static class Reference {

        /** Reference marker index 0 at time 0 */
        public static final Reference ZERO = new Reference(0L, 0);

        private final long time;
        private final int index;

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
    }

    private final String fCategory;
    private final Reference fReference;
    private final double fPeriod;
    private final long fRollover;
    private final RGBA fColor;
    private final @Nullable RGBA fOddColor;
    private final boolean fForeground;

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
     * The markers will have the given category. Periods with even index will be
     * shaded with the even color. Periods with odd index will be shaded with
     * the odd color. The reference defines the marker with the given index to
     * be at the specified time.
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
     * @param evenColor
     *            the even marker color
     * @param oddColor
     *            the odd marker color
     * @param foreground
     *            true if the marker is drawn in foreground, and false otherwise
     */
    public PeriodicMarkerEventSource(String category, Reference reference, double period, long rollover, RGBA evenColor, RGBA oddColor, boolean foreground) {
        this(category, reference, period, rollover, foreground, evenColor, oddColor);
    }

    /* Private constructor. The order of parameters is changed to make it unique. */
    private PeriodicMarkerEventSource(String category, Reference reference, double period, long rollover, boolean foreground, RGBA evenColor, @Nullable RGBA oddColor) {
        if (period <= 0) {
            throw new IllegalArgumentException("period cannot be less than or equal to zero"); //$NON-NLS-1$
        }
        if (rollover < 0) {
            throw new IllegalArgumentException("rollover cannot be less than zero"); //$NON-NLS-1$
        }
        fCategory = category;
        fReference = reference;
        fPeriod = period;
        fRollover = rollover;
        fColor = evenColor;
        fOddColor = oddColor;
        fForeground = foreground;
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
        List<IMarkerEvent> markers = new ArrayList<>();
        long time = startTime;
        if (Math.round((Math.round((time - fReference.time) / fPeriod)) * fPeriod + fReference.time) >= time) {
            /* Subtract one period to ensure previous marker is included */
            time -= fPeriod;
        }
        while (true) {
            long index = Math.round((time - fReference.time) / fPeriod) + fReference.index;
            time = Math.round((index - fReference.index) * fPeriod) + fReference.time;
            long labelIndex = index;
            if (fRollover != 0) {
                labelIndex %= fRollover;
                if (labelIndex < 0) {
                    labelIndex += fRollover;
                }
            }
            if (fOddColor == null) {
                markers.add(new MarkerEvent(null, time, 0, fCategory, fColor, getMarkerLabel(labelIndex), fForeground));
            } else {
                RGBA color = index % 2 == 0 ? fColor : fOddColor;
                long duration = Math.round((index + 1 - fReference.index) * fPeriod + fReference.time) - time;
                markers.add(new MarkerEvent(null, time, duration, fCategory, color, getMarkerLabel(labelIndex), fForeground));
            }
            if (time > endTime) {
                /* The next marker out of range is included */
                break;
            }
            time += Math.max(fPeriod, resolution);
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
}
