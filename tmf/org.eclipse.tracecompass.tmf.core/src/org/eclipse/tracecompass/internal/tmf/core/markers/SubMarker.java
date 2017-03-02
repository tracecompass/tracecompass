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

package org.eclipse.tracecompass.internal.tmf.core.markers;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

/**
 * Model element for configurable sub-marker.
 */
public abstract class SubMarker extends Marker {

    /**
     * Private constructor.
     *
     * @param name
     *            the name
     * @param color
     *            the color
     */
    private SubMarker(String name, String color) {
        super(name, color);
    }

    /**
     * Subclass for sub-marker evenly split into segments of equal length.
     *
     */
    public static class SplitMarker extends SubMarker {

        private final String fLabel;
        private final String fId;
        private final Range<Long> fRange;
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
         * @param color
         *            the color
         * @param range
         *            the range
         * @param indexRange
         *            the index range
         */
        public SplitMarker(String name, String label, String id, String color, Range<Long> range, RangeSet<Long> indexRange) {
            super(name, color);
            fLabel = label;
            fId = id;
            fRange = range;
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
         * @return the range
         */
        public Range<Long> getRange() {
            return fRange;
        }

        /**
         * @return the index range
         */
        public RangeSet<Long> getIndexRange() {
            return fIndexRange;
        }
    }

    /**
     * Subclass for a sub-marker divided into segments of specified weighted lengths.
     *
     */
    public static class WeightedMarker extends SubMarker {

        private final List<MarkerSegment> fSegments;
        private long fTotalLength = 0;

        /**
         * Constructor
         *
         * @param name
         *            the name
         */
        public WeightedMarker(String name) {
            super(name, null);
            fSegments = new ArrayList<>();
        }

        /**
         * @return the segments
         */
        public List<MarkerSegment> getSegments() {
            return fSegments;
        }

        /**
         * Add a segment.
         *
         * @param segment
         *            the segment
         */
        public void addSegment(MarkerSegment segment) {
            fSegments.add(segment);
            fTotalLength += segment.getLength();
        }

        /**
         * Get the total length of all segments
         *
         * @return the total length
         */
        public long getTotalLength() {
            return fTotalLength;
        }
    }
}
