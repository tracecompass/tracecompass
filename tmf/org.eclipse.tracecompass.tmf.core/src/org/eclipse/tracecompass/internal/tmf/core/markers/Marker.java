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

package org.eclipse.tracecompass.internal.tmf.core.markers;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

/**
 * Model element for configurable marker.
 */
public class Marker {

    private final String fName;
    private final String fColor;
    private final List<SubMarker> fSubMarkers;

    /**
     * Private constructor.
     *
     * @param name
     *            the name
     * @param color
     *            the color
     */
    protected Marker(String name, String color) {
        super();
        fName = name;
        fColor = color;
        fSubMarkers = new ArrayList<>();
    }

    /**
     * Subclass for periodic marker evenly split into segments of equal length.
     *
     */
    public static class PeriodicMarker extends Marker {

        private final String fLabel;
        private final String fId;
        private final String fReferenceId;
        private final double fPeriod;
        private final String fUnit;
        private final Range<Long> fRange;
        private final long fOffset;
        private final RangeSet<Long> fIndexRange;

        /**
         * Constructor
         *
         * @param name
         *            the name
         * @param label
         *            the label
         * @param id
         *            the id
         * @param referenceId
         *            the reference id
         * @param color
         *            the color
         * @param period
         *            the period
         * @param unit
         *            the unit
         * @param range
         *            the range
         * @param offset
         *            the offset
         * @param indexRange
         *            the index range
         */
        public PeriodicMarker(String name, String label, String id, String referenceId, String color, double period, String unit, Range<Long> range, long offset, RangeSet<Long> indexRange) {
            super(name, color);
            fLabel = label;
            fId = id;
            fReferenceId = referenceId;
            fPeriod = period;
            fUnit = unit;
            fRange = range;
            fOffset = offset;
            fIndexRange = indexRange;
        }

        /**
         * @return the label
         */
        public String getLabel() {
            return fLabel;
        }

        /**
         * @return the id
         */
        public String getId() {
            return fId;
        }

        /**
         * @return the reference id
         */
        public String getReferenceId() {
            return fReferenceId;
        }

        /**
         * @return the period
         */
        public double getPeriod() {
            return fPeriod;
        }

        /**
         * @return the unit
         */
        public String getUnit() {
            return fUnit;
        }

        /**
         * @return the range
         */
        public Range<Long> getRange() {
            return fRange;
        }

        /**
         * @return the offset
         */
        public long getOffset() {
            return fOffset;
        }

        /**
         * @return the index range
         */
        public RangeSet<Long> getIndexRange() {
            return fIndexRange;
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return fName;
    }

    /**
     * @return the color
     */
    public String getColor() {
        return fColor;
    }

    /**
     * @return the sub-markers
     */
    public List<SubMarker> getSubMarkers() {
        return fSubMarkers;
    }

    /**
     * Add a sub-marker.
     *
     * @param subMarker the sub-marker
     */
    public void addSubMarker(SubMarker subMarker) {
        fSubMarkers.add(subMarker);
    }
}
