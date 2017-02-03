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

package org.eclipse.tracecompass.tmf.ui.tests.markers;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.tracecompass.internal.tmf.core.markers.Marker;
import org.eclipse.tracecompass.internal.tmf.core.markers.Marker.PeriodicMarker;
import org.eclipse.tracecompass.internal.tmf.core.markers.MarkerSegment;
import org.eclipse.tracecompass.internal.tmf.core.markers.MarkerSet;
import org.eclipse.tracecompass.internal.tmf.core.markers.SubMarker;
import org.eclipse.tracecompass.internal.tmf.core.markers.SubMarker.SplitMarker;
import org.eclipse.tracecompass.internal.tmf.core.markers.SubMarker.WeightedMarker;
import org.eclipse.tracecompass.internal.tmf.ui.markers.ConfigurableMarkerEventSource;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.AbstractTmfTraceAdapterFactory;
import org.eclipse.tracecompass.tmf.core.trace.ICyclesConverter;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceAdapterManager;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.eclipse.tracecompass.tmf.ui.markers.IMarkerReferenceProvider;
import org.eclipse.tracecompass.tmf.ui.markers.PeriodicMarkerEventSource.Reference;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEvent;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;

/**
 * Tests for class FlexMarkerEventSource
 */
public class ConfigurableMarkerEventSourceTest {

    private static final class TmfTraceStubAdapterFactory extends AbstractTmfTraceAdapterFactory {
        @Override
        protected <T> @Nullable T getTraceAdapter(@NonNull ITmfTrace trace, Class<T> adapterType) {
            if (ICyclesConverter.class.equals(adapterType)) {
                ICyclesConverter adapter = new ICyclesConverter() {
                    @Override
                    public long cyclesToNanos(long cycles) {
                        return cycles * 4;
                    }
                    @Override
                    public long nanosToCycles(long nanos) {
                        return nanos / 4;
                    }
                };
                return adapterType.cast(adapter);
            }
            if (IMarkerReferenceProvider.class.equals(adapterType)) {
                IMarkerReferenceProvider adapter = new IMarkerReferenceProvider() {
                    @Override
                    public Reference getReference(String referenceId) {
                        if ("ref.c".equals(referenceId)) {
                            return new Reference(1000L, 0);
                        }
                        return null;
                    }
                };
                return adapterType.cast(adapter);
            }
            return null;
        }

        @Override
        public Class<?>[] getAdapterList() {
            return new Class[] {
                    ICyclesConverter.class,
                    IMarkerReferenceProvider.class
            };
        }
    }

    private static final String COLOR_STR = "#010101";
    private static final RGBA COLOR = new RGBA(1, 1, 1, 10);
    private static final RGBA ODD_COLOR = new RGBA(1, 1, 1, 0);

    private static ITmfTrace fTrace;
    private static AbstractTmfTraceAdapterFactory fFactory;

    /**
     * Before Class
     */
    @BeforeClass
    public static void beforeClass() {
        fFactory = new TmfTraceStubAdapterFactory();
        TmfTraceAdapterManager.registerFactory(fFactory, TmfTraceStub.class);
        fTrace = TmfTestTrace.A_TEST_10K.getTrace();
    }

    /**
     * After Class
     */
    @AfterClass
    public static void afterClass() {
        if (fTrace != null) {
            fTrace.dispose();
        }
        TmfTraceAdapterManager.unregisterFactory(fFactory);
        fFactory.dispose();
    }

    /**
     * Test
     */
    @Test
    public void test() {
        ConfigurableMarkerEventSource source = new ConfigurableMarkerEventSource(fTrace);
        MarkerSet set = new MarkerSet("name", "id");
        source.configure(set);
        assertEquals(0, source.getMarkerCategories().size());

        /*
         * period: 10 ms, offset: 20 ms, range: 0..4
         * requested range: 100 ms-200 ms
         * expected markers: 90 ms[2] 100 ms[3] 110 ms[4] 120 ms[0] ... 200 ms[3] 210 ms[4]
         */
        Marker markerA = new PeriodicMarker("A", "A %d", "a", "ref.a", COLOR_STR, 10.0, "ms", Range.closed(0L, 4L), 20L, ImmutableRangeSet.of(Range.all()));
        set.addMarker(markerA);
        source.configure(set);
        assertEquals(Arrays.asList("A"), source.getMarkerCategories());
        List<IMarkerEvent> markerList = source.getMarkerList("A", 100000000L, 200000000L, 1L, new NullProgressMonitor());
        assertEquals(13, markerList.size());
        for (int i = 0; i < markerList.size(); i++) {
            long t = (i + 9) * 10000000L;
            int index = (i + 9) - 2;
            int labelIndex = index % 5;
            RGBA color = index % 2 == 0 ? COLOR : ODD_COLOR;
            validateMarker(markerList.get(i), t, 10000000L, "A", String.format("A %d", labelIndex), color);
        }

        /*
         * period: 10 us, offset: 20 ms, range: 1..1000
         * requested range: 100 ms-200 ms
         * expected markers: 99.99 ms[1000] 100.00 ms[1] 100.01 ms[2] 100.02 ms[2] ... 200.00 ms[1]
         */
        SubMarker markerB = new SplitMarker("B", "B %d", "b", COLOR_STR, Range.closed(1L, 1000L), ImmutableRangeSet.of(Range.all()));
        markerA.addSubMarker(markerB);
        source.configure(set);
        assertEquals(Arrays.asList("A", "B"), source.getMarkerCategories());
        markerList = source.getMarkerList("B", 100000000L, 200000000L, 10000L, new NullProgressMonitor());
        assertEquals(0, markerList.size());
        markerList = source.getMarkerList("B", 100000000L, 200000000L, 1L, new NullProgressMonitor());
        assertEquals(10002, markerList.size());
        for (int i = 0; i < markerList.size(); i++) {
            long t = (i + 9999) * 10000L;
            int index = (i + 9999) - 2000;
            int labelIndex = 1 + index % 1000;
            RGBA color = labelIndex % 2 == 0 ? COLOR : ODD_COLOR;
            validateMarker(markerList.get(i), t, 10000L, "B", String.format("B %d", labelIndex), color);
        }

        /*
         * period: 10 cycles (40ns), offset: -10 cycles (-40ns), reference: 1000ns[0], range: 0..
         * requested range: 1000 ns-2000 ns
         * expected markers: 960 ns[0] 1000 ns[1] 1040 ns[2] ... 2000 ns[26] 2040 ns[27]
         */
        Marker markerC = new PeriodicMarker("C", "C %d", "c", "ref.c", COLOR_STR, 10.0, "cycles", Range.atLeast(0L), -10L, ImmutableRangeSet.of(Range.all()));
        set.addMarker(markerC);
        source.configure(set);
        assertEquals(Arrays.asList("A", "B", "C"), source.getMarkerCategories());
        markerList = source.getMarkerList("C", 1000L, 2000L, 1L, new NullProgressMonitor());
        assertEquals(28, markerList.size());
        for (int i = 0; i < markerList.size(); i++) {
            long t = (i + 24) * 40L;
            int index = i + 24 - 25 + 1; // -25 for reference, +1 for offset
            RGBA color = index % 2 == 0 ? COLOR : ODD_COLOR;
            validateMarker(markerList.get(i), t, 40L, "C", String.format("C %d", index), color);
        }

        /*
         * period: 40 ns, offset: 0 ns, range: 0..49, indexRange: 30..31,40
         * requested range: 0 ns-4000 ns
         * expected markers: 1200 ns[30] 1240 ns[31] 1600 ns[40] 3200 ns[30] 3240 ns[31] 3600 ns[40]
         */
        Marker markerD = new PeriodicMarker("D", "D %d", "d", "ref.d", COLOR_STR, 40.0, "ns", Range.closed(0L, 49L), 0L,
                ImmutableRangeSet.<Long>builder().add(Range.closed(30L, 31L)).add(Range.singleton(40L)).build());
        set.addMarker(markerD);
        source.configure(set);
        assertEquals(Arrays.asList("A", "B", "C", "D"), source.getMarkerCategories());
        markerList = source.getMarkerList("D", 0L, 4000L, 1L, new NullProgressMonitor());
        assertEquals(6, markerList.size());
        int i = 0;
        for (long t = 0L; t < 4000L; t += 40L) {
            int index = (int) (t / 40L) % 50;
            if (index == 30L || index == 31L || index == 40L) {
                IMarkerEvent marker = markerList.get(i++);
                RGBA color = index % 2 == 0 ? COLOR : ODD_COLOR;
                validateMarker(marker, t, 40L, "D", String.format("D %d", index), color);
            }
        }

        /*
         * period: 40 ns with segment weigths {2,1,3}, offset: 0 ns, range:0..49, indexRange:30..31,40
         * requested range: 0 ns-2000 ns
         * expected markers: 1200 ns[0] 1220[2] 1240 ns[0] 1260[2] 1600 ns[0] 1620 ns[2]
         */
        WeightedMarker markerE = new WeightedMarker("E");
        markerD.addSubMarker(markerE);
        MarkerSegment segmentE1 = new MarkerSegment("E1 %d", "e1", COLOR_STR, 2);
        markerE.addSegment(segmentE1);
        MarkerSegment segmentE2 = new MarkerSegment("E2 %d", "e2", "", 1);
        markerE.addSegment(segmentE2);
        MarkerSegment segmentE3 = new MarkerSegment("E3 %d", "e3", COLOR_STR, 3);
        markerE.addSegment(segmentE3);
        source.configure(set);
        assertEquals(Arrays.asList("A", "B", "C", "D", "E"), source.getMarkerCategories());
        markerList = source.getMarkerList("E", 0L, 2000L, 1L, new NullProgressMonitor());
        assertEquals(6, markerList.size());
        i = 0;
        for (long t = 0L; t < 2000L; t += 40L) {
            int index = (int) (t / 40L) % 50;
            if (index == 30L || index == 31L || index == 40L) {
                validateMarker(markerList.get(i++), t, 13L, "E", String.format("E1 %d", 0), COLOR);
                // segment 2 does not have visible marker due to empty color name
                validateMarker(markerList.get(i++), t + 20L, 20L, "E", String.format("E3 %d", 2), COLOR);
            }
        }
    }

    private static void validateMarker(IMarkerEvent marker, long time, long duration, String category, String label, RGBA color) {
        assertEquals(time, marker.getTime());
        assertEquals(duration, marker.getDuration());
        assertEquals(category, marker.getCategory());
        assertEquals(label, marker.getLabel());
        assertEquals(color, marker.getColor());
        assertEquals(false, marker.isForeground());
    }
}
