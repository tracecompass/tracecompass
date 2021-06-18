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

package org.eclipse.tracecompass.tmf.core.tests.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.Annotation;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.AnnotationCategoriesModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.AnnotationModel;
import org.eclipse.tracecompass.internal.tmf.core.annotations.CustomAnnotationProvider;
import org.eclipse.tracecompass.internal.tmf.core.markers.Marker;
import org.eclipse.tracecompass.internal.tmf.core.markers.Marker.PeriodicMarker;
import org.eclipse.tracecompass.internal.tmf.core.markers.MarkerSegment;
import org.eclipse.tracecompass.internal.tmf.core.markers.MarkerSet;
import org.eclipse.tracecompass.internal.tmf.core.markers.SubMarker;
import org.eclipse.tracecompass.internal.tmf.core.markers.SubMarker.SplitMarker;
import org.eclipse.tracecompass.internal.tmf.core.markers.SubMarker.WeightedMarker;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.markers.ITimeReferenceProvider;
import org.eclipse.tracecompass.tmf.core.markers.TimeReference;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.AbstractTmfTraceAdapterFactory;
import org.eclipse.tracecompass.tmf.core.trace.ICyclesConverter;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceAdapterManager;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;

/**
 * Test the {@link CustomAnnotationProvider} class
 */
public class CustomAnnotationProviderTest {

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
            if (ITimeReferenceProvider.class.equals(adapterType)) {
                ITimeReferenceProvider adapter = new ITimeReferenceProvider() {
                    @Override
                    public TimeReference apply(String referenceId) {
                        if ("ref.c".equals(referenceId)) {
                            return new TimeReference(1000L, 0);
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
                    ITimeReferenceProvider.class
            };
        }
    }

    private static final int ALPHA = 10;
    private static final String COLOR_STR = "#010101";
    private static final RGBAColor COLOR = new RGBAColor(1, 1, 1, ALPHA);
    private static final RGBAColor ODD_COLOR = new RGBAColor(1, 1, 1, 0);
    private static final String RED_STR = "red";
    private static final RGBAColor RED = new RGBAColor(255, 0, 0, ALPHA);
    private static final String INVALID_STR = "invalid";
    private static final RGBAColor DEFAULT = new RGBAColor(0, 0, 0, ALPHA);

    private static ITmfTrace fTrace;
    private static AbstractTmfTraceAdapterFactory fFactory;
    private CustomAnnotationProvider fProvider;

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
     * Before instance
     */
    @Before
    public void before() {
        fProvider = new CustomAnnotationProvider(fTrace, null);
    }

    /**
     * Test simple
     */
    @Test
    public void testSimple() {
        List<Annotation> annotationList;
        MarkerSet set = new MarkerSet("name", "id");
        fProvider.configure(set);
        assertAnnotationCategoriesModelResponse(Collections.emptyList(), fProvider.fetchAnnotationCategories(Collections.emptyMap(), new NullProgressMonitor()));

        /*
         * period: 10 ms, offset: 20 ms, range: 0..4
         *
         * requested range: 100 ms-200 ms
         *
         * expected annotations: 90 ms[2] 100 ms[3] 110 ms[4] 120 ms[0] ... 200
         * ms[3] 210 ms[4]
         */
        Marker markerA = new PeriodicMarker("A", "A %d", "a", "ref.a", COLOR_STR, 10.0, "ms", Range.closed(0L, 4L), 20L, ImmutableRangeSet.of(Range.all()));
        set.addMarker(markerA);
        fProvider.configure(set);
        assertAnnotationCategoriesModelResponse(Arrays.asList("A"), fProvider.fetchAnnotationCategories(Collections.emptyMap(), new NullProgressMonitor()));
        annotationList = getAnnotationList("A", 100000000L, 200000000L, 1000L, new NullProgressMonitor());
        assertEquals(annotationList.toString(), 13, annotationList.size());
        for (int i = 0; i < annotationList.size(); i++) {
            long t = (i + 9) * 10000000L;
            int index = (i + 9) - 2;
            int labelIndex = index % 5;
            RGBAColor color = index % 2 == 0 ? COLOR : ODD_COLOR;
            validateAnnotation(annotationList.get(i), t, 10000000L, "A", String.format("A %d", labelIndex), color);
        }

    }

    /**
     * Test submarkers
     */
    @Test
    public void testSubmarkers() {

        List<Annotation> annotationList;
        MarkerSet set = new MarkerSet("name", "id");
        fProvider.configure(set);
        assertAnnotationCategoriesModelResponse(Collections.emptyList(), fProvider.fetchAnnotationCategories(Collections.emptyMap(), new NullProgressMonitor()));

        /*
         * period: 10 ms, offset: 20 ms, range: 0..4
         *
         * requested range: 100 ms-200 ms
         *
         * expected annotations: 90 ms[2] 100 ms[3] 110 ms[4] 120 ms[0] ... 200
         * ms[3] 210 ms[4]
         */
        Marker markerA = new PeriodicMarker("A", "A %d", "a", "ref.a", COLOR_STR, 10.0, "ms", Range.closed(0L, 4L), 20L, ImmutableRangeSet.of(Range.all()));
        set.addMarker(markerA);
        fProvider.configure(set);
        /*
         * period: 10 us, offset: 20 ms, range: 1..1000
         *
         * requested range: 100 ms-200 ms
         *
         * expected annotations: 99.99 ms[1000] 100.00 ms[1] 100.01 ms[2] 100.02
         * ms[2] ... 200.00 ms[1]
         */

        SubMarker markerB = new SplitMarker("B", "B %d", "b", COLOR_STR, Range.closed(1L, 1000L), ImmutableRangeSet.of(Range.all()));
        markerA.addSubMarker(markerB);
        fProvider.configure(set);
        assertAnnotationCategoriesModelResponse(Arrays.asList("A", "B"), fProvider.fetchAnnotationCategories(Collections.emptyMap(), new NullProgressMonitor()));
        annotationList = getAnnotationList("B", 100000000L, 200000000L, 10000L, new NullProgressMonitor());
        assertEquals(0, annotationList.size());
        annotationList = getAnnotationList("B", 100000000L, 200000000L, 1000L, new NullProgressMonitor());
        assertEquals(10002, annotationList.size());
        for (int i = 0; i < annotationList.size(); i++) {
            long t = (i + 9999) * 10000L;
            int index = (i + 9999) - 2000;
            int labelIndex = 1 + index % 1000;
            RGBAColor color = labelIndex % 2 == 0 ? COLOR : ODD_COLOR;
            validateAnnotation(annotationList.get(i), t, 10000L, "B", String.format("B %d", labelIndex), color);
        }
    }

    /**
     * Test offset
     */
    @Test
    public void testOffset() {
        List<Annotation> annotationList;
        MarkerSet set = new MarkerSet("name", "id");
        fProvider.configure(set);
        assertAnnotationCategoriesModelResponse(Collections.emptyList(), fProvider.fetchAnnotationCategories(Collections.emptyMap(), new NullProgressMonitor()));

        /*
         * period: 10 cycles (40ns), offset: -10 cycles (-40ns), reference:
         * 1000ns[0], range: 0..
         *
         * requested range: 1000 ns-2000 ns
         *
         * expected annotations: 960 ns[0] 1000 ns[1] 1040 ns[2] ... 2000 ns[26]
         * 2040 ns[27]
         */
        Marker markerC = new PeriodicMarker("C", "C %d", "c", "ref.c", COLOR_STR, 10.0, "cycles", Range.atLeast(0L), -10L, ImmutableRangeSet.of(Range.all()));
        set.addMarker(markerC);
        fProvider.configure(set);
        assertAnnotationCategoriesModelResponse(Arrays.asList("C"), fProvider.fetchAnnotationCategories(Collections.emptyMap(), new NullProgressMonitor()));
        annotationList = getAnnotationList("C", 1000L, 2000L, 1L, new NullProgressMonitor());
        assertEquals(28, annotationList.size());
        for (int i = 0; i < annotationList.size(); i++) {
            long t = (i + 24) * 40L;
            int index = i + 24 - 25 + 1; // -25 +1 for offset
            RGBAColor color = index % 2 == 0 ? COLOR : ODD_COLOR;
            validateAnnotation(annotationList.get(i), t, 40L, "C", String.format("C %d", index), color);
        }
    }

    /**
     * Test irregular
     */
    @Test
    public void testIrregular() {
        List<Annotation> annotationList;
        MarkerSet set = new MarkerSet("name", "id");
        fProvider.configure(set);
        assertAnnotationCategoriesModelResponse(Collections.emptyList(), fProvider.fetchAnnotationCategories(Collections.emptyMap(), new NullProgressMonitor()));

        /*
         * period: 40 ns, offset: 0 ns, range: 0..49, indexRange: 30..31,40
         *
         * requested range: 0 ns-4000 ns
         *
         * expected annotations: 1200 ns[30] 1240 ns[31] 1600 ns[40] 3200 ns[30]
         * 3240 ns[31] 3600 ns[40]
         */
        Marker markerD = new PeriodicMarker("D", "D %d", "d", "ref.d", COLOR_STR, 40.0, "ns", Range.closed(0L, 49L), 0L,
                ImmutableRangeSet.<Long> builder().add(Range.closed(30L, 31L)).add(Range.singleton(40L)).build());
        set.addMarker(markerD);
        fProvider.configure(set);
        assertAnnotationCategoriesModelResponse(Arrays.asList("D"), fProvider.fetchAnnotationCategories(Collections.emptyMap(), new NullProgressMonitor()));
        annotationList = getAnnotationList("D", 0L, 4000L, 1L, new NullProgressMonitor());
        assertEquals(6, annotationList.size());
        int i = 0;
        for (long t = 0L; t < 4000L; t += 40L) {
            int index = (int) (t / 40L) % 50;
            if (index == 30L || index == 31L || index == 40L) {
                Annotation annotation = annotationList.get(i++);
                RGBAColor color = index % 2 == 0 ? COLOR : ODD_COLOR;
                validateAnnotation(annotation, t, 40L, "D", String.format("D %d", index), color);
            }
        }
    }

    /**
     * Test weighted with submarkers
     */
    @Test
    public void testWeightedWithSubmarkers() {
        List<Annotation> annotationList;
        MarkerSet set = new MarkerSet("name", "id");
        fProvider.configure(set);
        assertAnnotationCategoriesModelResponse(Collections.emptyList(), fProvider.fetchAnnotationCategories(Collections.emptyMap(), new NullProgressMonitor()));
        /*
         * period: 40 ns, offset: 0 ns, range: 0..49, indexRange: 30..31,40
         *
         * requested range: 0 ns-4000 ns
         *
         * expected annotations: 1200 ns[30] 1240 ns[31] 1600 ns[40] 3200 ns[30]
         * 3240 ns[31] 3600 ns[40]
         */
        Marker markerD = new PeriodicMarker("D", "D %d", "d", "ref.d", COLOR_STR, 40.0, "ns", Range.closed(0L, 49L), 0L,
                ImmutableRangeSet.<Long> builder().add(Range.closed(30L, 31L)).add(Range.singleton(40L)).build());
        set.addMarker(markerD);
        /*
         * period: 40 ns with segment weights {2,1,3}, offset: 0 ns,
         * range:0..49, indexRange:30..31,40
         *
         * requested range: 0 ns-2000 ns
         *
         * expected annotations: 1200 ns[0] 1220[2] 1240 ns[0] 1260[2] 1600 ns[0]
         * 1620 ns[2]
         */
        WeightedMarker markerE = new WeightedMarker("E");
        markerD.addSubMarker(markerE);
        MarkerSegment segmentE1 = new MarkerSegment("E1 %d", "e1", RED_STR, 2);
        markerE.addSegment(segmentE1);
        MarkerSegment segmentE2 = new MarkerSegment("E2 %d", "e2", "", 1);
        markerE.addSegment(segmentE2);
        MarkerSegment segmentE3 = new MarkerSegment("E3 %d", "e3", INVALID_STR, 3);
        markerE.addSegment(segmentE3);
        fProvider.configure(set);
        assertAnnotationCategoriesModelResponse(Arrays.asList("D", "E"), fProvider.fetchAnnotationCategories(Collections.emptyMap(), new NullProgressMonitor()));
        annotationList = getAnnotationList("E", 0L, 2000L, 1L, new NullProgressMonitor());
        assertEquals(6, annotationList.size());
        int i = 0;
        for (long t = 0L; t < 2000L; t += 40L) {
            int index = (int) (t / 40L) % 50;
            if (index == 30L || index == 31L || index == 40L) {
                validateAnnotation(annotationList.get(i), t, 13L, "E", String.format("E1 %d", 0), RED);
                i++;
                /*
                 * segment 2 does not have visible annotation due to empty color
                 * name
                 */
                validateAnnotation(annotationList.get(i), t + 20L, 20L, "E", String.format("E3 %d", 2), DEFAULT);
                i++;
            }
        }
    }

    private List<@NonNull Annotation> getAnnotationList(String category, long startTime, long endTime, long resolution, IProgressMonitor monitor) {
        Map<String, Object> fetchParameters = ImmutableMap.of(
                DataProviderParameterUtils.REQUESTED_MARKER_CATEGORIES_KEY, Collections.singleton(category),
                DataProviderParameterUtils.REQUESTED_TIME_KEY, StateSystemUtils.getTimes(startTime, endTime, resolution));
        TmfModelResponse<@NonNull AnnotationModel> response = fProvider.fetchAnnotations(fetchParameters, monitor);
        assertEquals(Status.COMPLETED, response.getStatus());
        AnnotationModel model = response.getModel();
        assertNotNull(model);
        assertEquals(Collections.singleton(category), model.getAnnotations().keySet());
        return new ArrayList<>(model.getAnnotations().get(category));
    }

    private static void assertAnnotationCategoriesModelResponse(List<String> expectedCategories, TmfModelResponse<AnnotationCategoriesModel> response) {
        assertEquals(Status.COMPLETED, response.getStatus());
        AnnotationCategoriesModel model = response.getModel();
        assertNotNull(model);
        assertEquals(expectedCategories, model.getAnnotationCategories());
    }

    private static void validateAnnotation(Annotation annotation, long time, long duration, String category, String label, RGBAColor color) {
        String annotationText = annotation.toString();
        assertEquals(annotationText, time, annotation.getTime());
        assertEquals(annotationText, duration, annotation.getDuration());
        assertEquals(annotationText, -1, annotation.getEntryId());
        assertEquals(annotationText, label, annotation.getLabel());
        assertEquals(annotationText, createStyle(color), annotation.getStyle());
    }

    private static OutputElementStyle createStyle(RGBAColor color) {
        String colorString = color.toString().substring(0, 7);
        return new OutputElementStyle(null,
                ImmutableMap.of(StyleProperties.STYLE_NAME, colorString,
                        StyleProperties.COLOR, colorString,
                        StyleProperties.OPACITY, (float) (color.getAlpha() / 255.0)));
    }
}
