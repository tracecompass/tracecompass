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

package org.eclipse.tracecompass.tmf.ui.markers;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.math.Fraction;
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
        public String toString() {
            return String.format("[%d, %d]", time, index); //$NON-NLS-1$
        }
    }

    private final String fCategory;
    private final Reference fReference;
    private final double fPeriod;
    private final long fPeriodInteger;
    private @Nullable Fraction fPeriodFraction;
    private final long fRollover;
    private final RGBA fColor1;
    private final @Nullable RGBA fColor2;
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
     * The markers will have the given category. Periods will be shaded with the
     * first and second colors alternatively. The reference defines the marker
     * with the given index to be at the specified time. The reference will be
     * shaded with the first color if its index is even, or the second color
     * if it is odd.
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

    /* Private constructor. The order of parameters is changed to make it unique. */
    private PeriodicMarkerEventSource(String category, Reference reference, double period, long rollover, boolean foreground, RGBA color1, @Nullable RGBA color2) {
        if (period <= 0) {
            throw new IllegalArgumentException("period cannot be less than or equal to zero"); //$NON-NLS-1$
        }
        if (rollover < 0) {
            throw new IllegalArgumentException("rollover cannot be less than zero"); //$NON-NLS-1$
        }
        fCategory = category;
        fReference = reference;
        fPeriod = period;
        fPeriodInteger = (long) period;
        try {
            fPeriodFraction = Fraction.getFraction(fPeriod - fPeriodInteger);
        } catch (ArithmeticException e) {
            /* can't convert to fraction, use floating-point arithmetic */
            fPeriodFraction = null;
        }
        fRollover = rollover;
        fColor1 = color1;
        fColor2 = color2;
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
        /* Subtract 1.5 periods to ensure previous marker is included */
        long time = startTime - Math.max(Math.round(1.5 * fPeriod), resolution);
        Reference reference = adjustReference(fReference, time);
        IMarkerEvent markerEvent = null;
        while (true) {
            long index = Math.round((time - reference.time) / fPeriod) + reference.index;
            time = Math.round((index - reference.index) * fPeriod) + reference.time;
            long duration = (fColor2 == null) ? 0 : Math.round((index + 1 - reference.index) * fPeriod) + reference.time - time;
            long labelIndex = index;
            if (fRollover != 0) {
                labelIndex %= fRollover;
                if (labelIndex < 0) {
                    labelIndex += fRollover;
                }
            }
            /* Add previous marker if current is visible */
            if ((time >= startTime || time + duration > startTime) && markerEvent != null) {
                markers.add(markerEvent);
            }
            if (isApplicable(labelIndex)) {
                RGBA color = (fColor2 == null) ? fColor1 : (index % 2 == 0) ? fColor1 : fColor2;
                markerEvent = new MarkerEvent(null, time, duration, fCategory, color, getMarkerLabel(labelIndex), fForeground);
            } else {
                markerEvent = null;
            }
            if (time > endTime) {
                if (markerEvent != null) {
                    /* The next marker out of range is included */
                    markers.add(markerEvent);
                }
                break;
            }
            time += Math.max(Math.round(fPeriod), resolution);
        }
        return markers;
    }

    /*
     * Adjust to a reference that is closer to the start time, to avoid rounding
     * errors in floating point calculations with large numbers.
     */
    private Reference adjustReference(Reference baseReference, long time) {
        long offsetIndex = (long) ((time - baseReference.time) / fPeriod);
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
        Reference reference = new Reference(baseReference.time + offsetTime, baseReference.index + offsetIndex);
        return reference;
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
     * @param index
     *            the marker index
     * @return true if the marker is applicable
     * @since 2.3
     */
    public boolean isApplicable(long index) {
        return true;
    }
}
