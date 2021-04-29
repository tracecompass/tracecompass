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

package org.eclipse.tracecompass.tmf.ui.tests.markers;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.tracecompass.tmf.ui.markers.PeriodicMarkerEventSource;
import org.eclipse.tracecompass.tmf.ui.markers.PeriodicMarkerEventSource.Reference;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEventSource;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.MarkerEvent;
import org.junit.Test;

/**
 * Test the {@link PeriodicMarkerEventSource} class
 */
public class PeriodicMarkerEventSourceTest {

    private static final @NonNull String CATEGORY = "Category";
    private static final @NonNull RGBA COLOR = new RGBA(255, 0, 0, 64);
    private static final @NonNull RGBA ODD_COLOR = new RGBA(0, 255, 0, 64);
    private static final @NonNull RGBA EVEN_COLOR = new RGBA(0, 0, 255, 64);

    /**
     * Test constructor with invalid period.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPeriod() {
        new PeriodicMarkerEventSource(CATEGORY, Reference.ZERO, 0L, 0, COLOR, false);
    }

    /**
     * Test constructor with invalid roll-over.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidRollover() {
        new PeriodicMarkerEventSource(CATEGORY, Reference.ZERO, 100L, -1, COLOR, false);
    }

    /**
     * Test a marker event source with lines at every period.
     */
    @Test
    public void testLineMarkerEventSource() {
        IMarkerEventSource source = new PeriodicMarkerEventSource(CATEGORY, Reference.ZERO, 100L, 0, COLOR, false);
        assertEquals(Arrays.asList(CATEGORY), source.getMarkerCategories());
        List<IMarkerEvent> expected = Arrays.asList(
                new MarkerEvent(null, 0L, 0L, CATEGORY, COLOR, "0", false),
                new MarkerEvent(null, 100L, 0L, CATEGORY, COLOR, "1", false),
                new MarkerEvent(null, 200L, 0L, CATEGORY, COLOR, "2", false),
                new MarkerEvent(null, 300L, 0L, CATEGORY, COLOR, "3", false));
        assertMarkerListEquals(expected, source.getMarkerList(CATEGORY, 50L, 250L, 1, new NullProgressMonitor()));
    }

    /**
     * Test a marker event source with alternate shading at every period.
     */
    @Test
    public void testAlternateShadingMarkerEventSource() {
        IMarkerEventSource source = new PeriodicMarkerEventSource(CATEGORY, Reference.ZERO, 100L, 0, EVEN_COLOR, ODD_COLOR, false);
        assertEquals(Arrays.asList(CATEGORY), source.getMarkerCategories());
        List<IMarkerEvent> expected = Arrays.asList(
                new MarkerEvent(null, -100L, 100L, CATEGORY, ODD_COLOR, "-1", false),
                new MarkerEvent(null, 0L, 100L, CATEGORY, EVEN_COLOR, "0", false),
                new MarkerEvent(null, 100L, 100L, CATEGORY, ODD_COLOR, "1", false),
                new MarkerEvent(null, 200L, 100L, CATEGORY, EVEN_COLOR, "2", false),
                new MarkerEvent(null, 300L, 100L, CATEGORY, ODD_COLOR, "3", false));
        assertMarkerListEquals(expected, source.getMarkerList(CATEGORY, 50L, 250L, 1, new NullProgressMonitor()));
    }

    /**
     * Test that previous and next markers are always included.
     */
    @Test
    public void testNextPreviousIncluded() {
        IMarkerEventSource source = new PeriodicMarkerEventSource(CATEGORY, Reference.ZERO, 100L, 0, COLOR, false);
        List<IMarkerEvent> expected = Arrays.asList(
                new MarkerEvent(null, -100L, 0L, CATEGORY, COLOR, "-1", false),
                new MarkerEvent(null, 0L, 0L, CATEGORY, COLOR, "0", false),
                new MarkerEvent(null, 100L, 0L, CATEGORY, COLOR, "1", false),
                new MarkerEvent(null, 200L, 0L, CATEGORY, COLOR, "2", false),
                new MarkerEvent(null, 300L, 0L, CATEGORY, COLOR, "3", false),
                new MarkerEvent(null, 400L, 0L, CATEGORY, COLOR, "4", false));
        assertMarkerListEquals(expected, source.getMarkerList(CATEGORY, 0L, 300L, 1, new NullProgressMonitor()));
    }

    /**
     * Test a marker event source with roll-over.
     */
    @Test
    public void testRollover() {
        IMarkerEventSource source = new PeriodicMarkerEventSource(CATEGORY, Reference.ZERO, 100L, 4, COLOR, false);
        List<IMarkerEvent> expected = Arrays.asList(
                new MarkerEvent(null, -100L, 0L, CATEGORY, COLOR, "3", false),
                new MarkerEvent(null, 0L, 0L, CATEGORY, COLOR, "0", false),
                new MarkerEvent(null, 100L, 0L, CATEGORY, COLOR, "1", false),
                new MarkerEvent(null, 200L, 0L, CATEGORY, COLOR, "2", false),
                new MarkerEvent(null, 300L, 0L, CATEGORY, COLOR, "3", false),
                new MarkerEvent(null, 400L, 0L, CATEGORY, COLOR, "0", false));
        assertMarkerListEquals(expected, source.getMarkerList(CATEGORY, 0L, 300L, 1, new NullProgressMonitor()));
    }

    /**
     * Test a marker event source with a fractional period.
     */
    @Test
    public void testFractionalPeriod() {
        IMarkerEventSource source = new PeriodicMarkerEventSource(CATEGORY, Reference.ZERO, (100.0 / 3), 0, EVEN_COLOR, ODD_COLOR, false);
        List<IMarkerEvent> expected = Arrays.asList(
                new MarkerEvent(null, -33L, 33L, CATEGORY, ODD_COLOR, "-1", false),
                new MarkerEvent(null, 0L, 33L, CATEGORY, EVEN_COLOR, "0", false),
                new MarkerEvent(null, 33L, 34L, CATEGORY, ODD_COLOR, "1", false),
                new MarkerEvent(null, 67L, 33L, CATEGORY, EVEN_COLOR, "2", false),
                new MarkerEvent(null, 100L, 33L, CATEGORY, ODD_COLOR, "3", false),
                new MarkerEvent(null, 133L, 34L, CATEGORY, EVEN_COLOR, "4", false));
        assertMarkerListEquals(expected, source.getMarkerList(CATEGORY, 0L, 100L, 1, new NullProgressMonitor()));
    }

    /**
     * Test a marker event source with period smaller than one time unit.
     */
    @Test
    public void testSmallPeriod() {
        IMarkerEventSource source = new PeriodicMarkerEventSource(CATEGORY, Reference.ZERO, (1.0 / 3), 0, EVEN_COLOR, ODD_COLOR, false);
        List<IMarkerEvent> expected = Arrays.asList(
                new MarkerEvent(null, -1L, 0L, CATEGORY, ODD_COLOR, "-3", false),
                new MarkerEvent(null, 0L, 0L, CATEGORY, EVEN_COLOR, "0", false),
                new MarkerEvent(null, 1L, 0L, CATEGORY, ODD_COLOR, "3", false),
                new MarkerEvent(null, 2L, 0L, CATEGORY, EVEN_COLOR, "6", false),
                new MarkerEvent(null, 3L, 0L, CATEGORY, ODD_COLOR, "9", false));
        assertMarkerListEquals(expected, source.getMarkerList(CATEGORY, 0L, 2L, 1, new NullProgressMonitor()));
    }

    /**
     * Test a marker event source with non-zero reference.
     */
    @Test
    public void testReference() {
        Reference reference = new Reference(250L, 10);
        IMarkerEventSource source = new PeriodicMarkerEventSource(CATEGORY, reference, 100L, 0, COLOR, false);
        List<IMarkerEvent> expected = Arrays.asList(
                new MarkerEvent(null, -50L, 0L, CATEGORY, COLOR, "7", false),
                new MarkerEvent(null, 50L, 0L, CATEGORY, COLOR, "8", false),
                new MarkerEvent(null, 150L, 0L, CATEGORY, COLOR, "9", false),
                new MarkerEvent(null, 250L, 0L, CATEGORY, COLOR, "10", false),
                new MarkerEvent(null, 350L, 0L, CATEGORY, COLOR, "11", false));
        assertMarkerListEquals(expected, source.getMarkerList(CATEGORY, 0L, 300L, 1, new NullProgressMonitor()));
    }

    /**
     * Test a query with a resolution.
     */
    @Test
    public void testResolution() {
        IMarkerEventSource source = new PeriodicMarkerEventSource(CATEGORY, Reference.ZERO, 10L, 0, EVEN_COLOR, ODD_COLOR, false);
        assertEquals(Arrays.asList(CATEGORY), source.getMarkerCategories());
        List<IMarkerEvent> expected = Arrays.asList(
                new MarkerEvent(null, -20L, 10L, CATEGORY, EVEN_COLOR, "-2", false),
                new MarkerEvent(null, 0L, 10L, CATEGORY, EVEN_COLOR, "0", false),
                new MarkerEvent(null, 30L, 10L, CATEGORY, ODD_COLOR, "3", false),
                new MarkerEvent(null, 50L, 10L, CATEGORY, ODD_COLOR, "5", false),
                new MarkerEvent(null, 80L, 10L, CATEGORY, EVEN_COLOR, "8", false),
                new MarkerEvent(null, 100L, 10L, CATEGORY, EVEN_COLOR, "10", false),
                new MarkerEvent(null, 130L, 10L, CATEGORY, ODD_COLOR, "13", false));
        assertMarkerListEquals(expected, source.getMarkerList(CATEGORY, 0L, 100L, 25, new NullProgressMonitor()));
    }

    /**
     * Test a marker event source with a filtering implementation.
     */
    @Test
    public void testIsApplicable() {
        IMarkerEventSource source = new PeriodicMarkerEventSource(CATEGORY, Reference.ZERO, 100L, 0, COLOR, false) {
            @Override
            public boolean isApplicable(long index) {
                return (index % 2 == 0);
            }
        };
        assertEquals(Arrays.asList(CATEGORY), source.getMarkerCategories());
        List<IMarkerEvent> expected = Arrays.asList(
                new MarkerEvent(null, 0L, 0L, CATEGORY, COLOR, "0", false),
                new MarkerEvent(null, 200L, 0L, CATEGORY, COLOR, "2", false),
                new MarkerEvent(null, 400L, 0L, CATEGORY, COLOR, "4", false),
                new MarkerEvent(null, 600L, 0L, CATEGORY, COLOR, "6", false),
                new MarkerEvent(null, 800L, 0L, CATEGORY, COLOR, "8", false),
                new MarkerEvent(null, 1000L, 0L, CATEGORY, COLOR, "10", false));
        assertMarkerListEquals(expected, source.getMarkerList(CATEGORY, 0L, 1000L, 1, new NullProgressMonitor()));
    }

    private static void assertMarkerListEquals(@NonNull List<IMarkerEvent> expectedList, @NonNull List<@NonNull IMarkerEvent> markerList) {
        assertEquals(expectedList.size(), markerList.size());
        for (int i = 0; i < expectedList.size(); i++) {
            IMarkerEvent expected = expectedList.get(i);
            IMarkerEvent marker = markerList.get(i);
            assertEquals(marker.toString(), expected.getEntry(), marker.getEntry());
            assertEquals(marker.toString(), expected.getTime(), marker.getTime());
            assertEquals(marker.toString(), expected.getDuration(), marker.getDuration());
            assertEquals(marker.toString(), expected.getCategory(), marker.getCategory());
            assertEquals(marker.toString(), expected.getLabel(), marker.getLabel());
            assertEquals(marker.toString(), expected.getColor(), marker.getColor());
        }
    }
}
