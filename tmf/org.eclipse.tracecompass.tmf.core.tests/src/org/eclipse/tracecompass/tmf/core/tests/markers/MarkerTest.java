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

package org.eclipse.tracecompass.tmf.core.tests.markers;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.eclipse.tracecompass.internal.tmf.core.markers.Marker.PeriodicMarker;
import org.eclipse.tracecompass.internal.tmf.core.markers.MarkerSegment;
import org.eclipse.tracecompass.internal.tmf.core.markers.SubMarker;
import org.eclipse.tracecompass.internal.tmf.core.markers.SubMarker.SplitMarker;
import org.eclipse.tracecompass.internal.tmf.core.markers.SubMarker.WeightedMarker;
import org.junit.Test;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;

/**
 * Tests for class Marker and its subclasses
 */
public class MarkerTest {

    /**
     * Test the PeriodicMarker constructor
     */
    @Test
    public void testConstructor() {
        PeriodicMarker marker = new PeriodicMarker("name", "label", "id", "referenceid", "color", 1.0, "ms", Range.atLeast(0L), 0L, ImmutableRangeSet.of(Range.all()));
        assertEquals("name", marker.getName());
        assertEquals("label", marker.getLabel());
        assertEquals("id", marker.getId());
        assertEquals("referenceid", marker.getReferenceId());
        assertEquals("color", marker.getColor());
        assertEquals(1.0, marker.getPeriod(), 0);
        assertEquals("ms", marker.getUnit());
        assertEquals(Range.atLeast(0L), marker.getRange());
        assertEquals(0L, marker.getOffset());
        assertEquals(ImmutableRangeSet.of(Range.all()), marker.getIndexRange());
        assertEquals(0, marker.getSubMarkers().size());
    }

    /**
     * Test the SplitMarker and WeightedMarker constructors and method addMarker
     */
    @Test
    public void testAddSubMarker() {
        PeriodicMarker marker = new PeriodicMarker("name", "label", "id", "referenceid", "color", 1.0, "ms", Range.atLeast(0L), 0L, ImmutableRangeSet.of(Range.all()));
        SubMarker subMarkerA = new SplitMarker("A", "a", "a", "color", Range.atLeast(0L), ImmutableRangeSet.of(Range.all()));
        marker.addSubMarker(subMarkerA);
        SubMarker subMarkerB = new WeightedMarker("B");
        marker.addSubMarker(subMarkerB);
        assertEquals(Arrays.asList(subMarkerA, subMarkerB), marker.getSubMarkers());
    }

    /**
     * Test the MarkerSegment constructor and method addSegment
     */
    @Test
    public void testAddSegment() {
        WeightedMarker subMarker = new WeightedMarker("name");
        MarkerSegment segmentA = new MarkerSegment("A", "a", "color1", 1);
        subMarker.addSegment(segmentA);
        MarkerSegment segmentB = new MarkerSegment("B", "b", "color2", 2);
        subMarker.addSegment(segmentB);
        assertEquals(Arrays.asList(segmentA, segmentB), subMarker.getSegments());
    }
}
