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

package org.eclipse.tracecompass.tmf.core.tests.markers;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.eclipse.tracecompass.internal.tmf.core.markers.Marker;
import org.eclipse.tracecompass.internal.tmf.core.markers.Marker.PeriodicMarker;
import org.eclipse.tracecompass.internal.tmf.core.markers.MarkerSet;
import org.junit.Test;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;

/**
 * Tests for class MarkerSet
 */
public class MarkerSetTest {

    /**
     * Test the constructor
     */
    @Test
    public void testConstructor() {
        MarkerSet markerSet = new MarkerSet("name", "id");
        assertEquals("name", markerSet.getName());
        assertEquals("id", markerSet.getId());
        assertEquals(0, markerSet.getMarkers().size());
    }

    /**
     * Test the method addMarker
     */
    @Test
    public void testAddMarker() {
        MarkerSet markerSet = new MarkerSet("name", "id");
        Marker markerA = new PeriodicMarker("A", "A %d", "a", "ref.a", "color1", 1.0, "ms", Range.atLeast(1L), 1L, ImmutableRangeSet.of(Range.atLeast(1L)));
        markerSet.addMarker(markerA);
        Marker markerB = new PeriodicMarker("B", "B %d", "b", "ref.b", "color2", 2.0, "ns", Range.atLeast(2L), 2L, ImmutableRangeSet.of(Range.atLeast(2L)));
        markerSet.addMarker(markerB);
        assertEquals(Arrays.asList(markerA, markerB), markerSet.getMarkers());
    }
}
