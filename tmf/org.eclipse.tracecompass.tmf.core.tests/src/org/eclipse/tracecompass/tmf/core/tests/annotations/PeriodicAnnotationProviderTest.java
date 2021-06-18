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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.Annotation;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.AnnotationCategoriesModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.AnnotationModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.annotations.IOutputAnnotationProvider;
import org.eclipse.tracecompass.internal.tmf.core.annotations.PeriodicAnnotationProvider;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.markers.ITimeReference;
import org.eclipse.tracecompass.tmf.core.markers.TimeReference;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

/**
 * Test the {@link PeriodicAnnotationProvider} class
 */
public class PeriodicAnnotationProviderTest {

    private static final @NonNull String CATEGORY = "Category";
    private static final @NonNull RGBAColor COLOR = new RGBAColor(255, 0, 0, 64);
    private static final @NonNull RGBAColor ODD_COLOR = new RGBAColor(0, 255, 0, 64);
    private static final @NonNull RGBAColor EVEN_COLOR = new RGBAColor(0, 0, 255, 64);
    private static final @NonNull OutputElementStyle COLOR_STYLE = createStyle(COLOR);
    private static final @NonNull OutputElementStyle ODD_COLOR_STYLE = createStyle(ODD_COLOR);
    private static final @NonNull OutputElementStyle EVEN_COLOR_STYLE = createStyle(EVEN_COLOR);

    /**
     * Test constructor with invalid period.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPeriod() {
        new PeriodicAnnotationProvider(CATEGORY, ITimeReference.ZERO, 0L, 0, COLOR, null);
    }

    /**
     * Test constructor with invalid roll-over.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidRollover() {
        new PeriodicAnnotationProvider(CATEGORY, ITimeReference.ZERO, 100L, -1, COLOR, null);
    }

    /**
     * Test fetching the annotation categories
     */
    @Test
    public void testCategories() {
        IOutputAnnotationProvider provider = new PeriodicAnnotationProvider(CATEGORY, ITimeReference.ZERO, 100L, 0, COLOR, null);
        TmfModelResponse<@NonNull AnnotationCategoriesModel> response = provider.fetchAnnotationCategories(Collections.emptyMap(), new NullProgressMonitor());
        assertEquals(Status.COMPLETED, response.getStatus());
        AnnotationCategoriesModel model = response.getModel();
        assertNotNull(model);
        assertEquals(Arrays.asList(CATEGORY), model.getAnnotationCategories());
    }

    /**
     * Test a periodic annotation provider with lines at every period.
     */
    @Test
    public void testLineAnnotationSource() {
        IOutputAnnotationProvider provider = new PeriodicAnnotationProvider(CATEGORY, ITimeReference.ZERO, 100L, 0, COLOR, null);
        Map<String, List<Annotation>> expected = ImmutableMap.of(CATEGORY, Arrays.asList(
                new Annotation(0L, 0L, -1, "0", COLOR_STYLE),
                new Annotation(100L, 0L, -1, "1", COLOR_STYLE),
                new Annotation(200L, 0L, -1, "2", COLOR_STYLE),
                new Annotation(300L, 0L, -1, "3", COLOR_STYLE)));
        Map<String, Object> fetchParameters = ImmutableMap.of(
                DataProviderParameterUtils.REQUESTED_TIME_KEY, StateSystemUtils.getTimes(50L, 250L, 1));
        assertAnnotationModelResponse(expected, provider.fetchAnnotations(fetchParameters, new NullProgressMonitor()));
    }

    /**
     * Test a periodic annotation provider with alternate shading at every period.
     */
    @Test
    public void testAlternateShadingAnnotationSource() {
        IOutputAnnotationProvider provider = new PeriodicAnnotationProvider(CATEGORY, ITimeReference.ZERO, 100L, 0, EVEN_COLOR, ODD_COLOR);
        Map<String, List<Annotation>> expected = ImmutableMap.of(CATEGORY, Arrays.asList(
                new Annotation(-100L, 100L, -1, "-1", ODD_COLOR_STYLE),
                new Annotation(0L, 100L, -1, "0", EVEN_COLOR_STYLE),
                new Annotation(100L, 100L, -1, "1", ODD_COLOR_STYLE),
                new Annotation(200L, 100L, -1, "2", EVEN_COLOR_STYLE),
                new Annotation(300L, 100L, -1, "3", ODD_COLOR_STYLE)));
        Map<String, Object> fetchParameters = ImmutableMap.of(
                DataProviderParameterUtils.REQUESTED_TIME_KEY, StateSystemUtils.getTimes(50L, 250L, 1));
        assertAnnotationModelResponse(expected, provider.fetchAnnotations(fetchParameters, new NullProgressMonitor()));
    }

    /**
     * Test that previous and next annotations are always included.
     */
    @Test
    public void testNextPreviousIncluded() {
        IOutputAnnotationProvider provider = new PeriodicAnnotationProvider(CATEGORY, ITimeReference.ZERO, 100L, 0, COLOR, null);
        Map<String, List<Annotation>> expected = ImmutableMap.of(CATEGORY, Arrays.asList(
                new Annotation(-100L, 0L, -1, "-1", COLOR_STYLE),
                new Annotation(0L, 0L, -1, "0", COLOR_STYLE),
                new Annotation(100L, 0L, -1, "1", COLOR_STYLE),
                new Annotation(200L, 0L, -1, "2", COLOR_STYLE),
                new Annotation(300L, 0L, -1, "3", COLOR_STYLE),
                new Annotation(400L, 0L, -1, "4", COLOR_STYLE)));
        Map<String, Object> fetchParameters = ImmutableMap.of(
                DataProviderParameterUtils.REQUESTED_TIME_KEY, StateSystemUtils.getTimes(0L, 300L, 1));
        assertAnnotationModelResponse(expected, provider.fetchAnnotations(fetchParameters, new NullProgressMonitor()));
    }

    /**
     * Test a periodic annotation provider with roll-over.
     */
    @Test
    public void testRollover() {
        IOutputAnnotationProvider provider = new PeriodicAnnotationProvider(CATEGORY, ITimeReference.ZERO, 100L, 4, COLOR, null);
        Map<String, List<Annotation>> expected = ImmutableMap.of(CATEGORY, Arrays.asList(
                new Annotation(-100L, 0L, -1, "3", COLOR_STYLE),
                new Annotation(0L, 0L, -1, "0", COLOR_STYLE),
                new Annotation(100L, 0L, -1, "1", COLOR_STYLE),
                new Annotation(200L, 0L, -1, "2", COLOR_STYLE),
                new Annotation(300L, 0L, -1, "3", COLOR_STYLE),
                new Annotation(400L, 0L, -1, "0", COLOR_STYLE)));
        Map<String, Object> fetchParameters = ImmutableMap.of(
                DataProviderParameterUtils.REQUESTED_TIME_KEY, StateSystemUtils.getTimes(0L, 300L, 1));
        assertAnnotationModelResponse(expected, provider.fetchAnnotations(fetchParameters, new NullProgressMonitor()));
    }

    /**
     * Test a periodic annotation provider with a fractional period.
     */
    @Test
    public void testFractionalPeriod() {
        IOutputAnnotationProvider provider = new PeriodicAnnotationProvider(CATEGORY, ITimeReference.ZERO, (100.0 / 3), 0, EVEN_COLOR, ODD_COLOR);
        Map<String, List<Annotation>> expected = ImmutableMap.of(CATEGORY, Arrays.asList(
                new Annotation(-33L, 33L, -1, "-1", ODD_COLOR_STYLE),
                new Annotation(0L, 33L, -1, "0", EVEN_COLOR_STYLE),
                new Annotation(33L, 34L, -1, "1", ODD_COLOR_STYLE),
                new Annotation(67L, 33L, -1, "2", EVEN_COLOR_STYLE),
                new Annotation(100L, 33L, -1, "3", ODD_COLOR_STYLE),
                new Annotation(133L, 34L, -1, "4", EVEN_COLOR_STYLE)));
        Map<String, Object> fetchParameters = ImmutableMap.of(
                DataProviderParameterUtils.REQUESTED_TIME_KEY, StateSystemUtils.getTimes(0L, 100L, 1));
        assertAnnotationModelResponse(expected, provider.fetchAnnotations(fetchParameters, new NullProgressMonitor()));
    }

    /**
     * Test a periodic annotation provider with period smaller than one time unit.
     */
    @Test
    public void testSmallPeriod() {
        IOutputAnnotationProvider provider = new PeriodicAnnotationProvider(CATEGORY, ITimeReference.ZERO, (1.0 / 3), 0, EVEN_COLOR, ODD_COLOR);
        Map<String, List<Annotation>> expected = ImmutableMap.of(CATEGORY, Arrays.asList(
                new Annotation(-1L, 0L, -1, "-3", ODD_COLOR_STYLE),
                new Annotation(0L, 0L, -1, "0", EVEN_COLOR_STYLE),
                new Annotation(1L, 0L, -1, "3", ODD_COLOR_STYLE),
                new Annotation(2L, 0L, -1, "6", EVEN_COLOR_STYLE),
                new Annotation(3L, 0L, -1, "9", ODD_COLOR_STYLE)));
        Map<String, Object> fetchParameters = ImmutableMap.of(
                DataProviderParameterUtils.REQUESTED_TIME_KEY, StateSystemUtils.getTimes(0L, 2L, 1));
        assertAnnotationModelResponse(expected, provider.fetchAnnotations(fetchParameters, new NullProgressMonitor()));
    }

    /**
     * Test a periodic annotation provider with non-zero reference.
     */
    @Test
    public void testReference() {
        ITimeReference reference = new TimeReference(250L, 10);
        IOutputAnnotationProvider provider = new PeriodicAnnotationProvider(CATEGORY, reference, 100L, 0, COLOR, null);
        Map<String, List<Annotation>> expected = ImmutableMap.of(CATEGORY, Arrays.asList(
                new Annotation(-50L, 0L, -1, "7", COLOR_STYLE),
                new Annotation(50L, 0L, -1, "8", COLOR_STYLE),
                new Annotation(150L, 0L, -1, "9", COLOR_STYLE),
                new Annotation(250L, 0L, -1, "10", COLOR_STYLE),
                new Annotation(350L, 0L, -1, "11", COLOR_STYLE)));
        Map<String, Object> fetchParameters = ImmutableMap.of(
                DataProviderParameterUtils.REQUESTED_TIME_KEY, StateSystemUtils.getTimes(0L, 300L, 1));
        assertAnnotationModelResponse(expected, provider.fetchAnnotations(fetchParameters, new NullProgressMonitor()));
    }

    /**
     * Test a query with a resolution.
     */
    @Test
    public void testResolution() {
        IOutputAnnotationProvider provider = new PeriodicAnnotationProvider(CATEGORY, ITimeReference.ZERO, 10L, 0, EVEN_COLOR, ODD_COLOR);
        Map<String, List<Annotation>> expected = ImmutableMap.of(CATEGORY, Arrays.asList(
                new Annotation(-20L, 10L, -1, "-2", EVEN_COLOR_STYLE),
                new Annotation(0L, 10L, -1, "0", EVEN_COLOR_STYLE),
                new Annotation(30L, 10L, -1, "3", ODD_COLOR_STYLE),
                new Annotation(50L, 10L, -1, "5", ODD_COLOR_STYLE),
                new Annotation(80L, 10L, -1, "8", EVEN_COLOR_STYLE),
                new Annotation(100L, 10L, -1, "10", EVEN_COLOR_STYLE),
                new Annotation(130L, 10L, -1, "13", ODD_COLOR_STYLE)));
        Map<String, Object> fetchParameters = ImmutableMap.of(
                DataProviderParameterUtils.REQUESTED_TIME_KEY, StateSystemUtils.getTimes(0L, 100L, 25));
        assertAnnotationModelResponse(expected, provider.fetchAnnotations(fetchParameters, new NullProgressMonitor()));
    }

    /**
     * Test a periodic annotation provider with a filtering implementation.
     */
    @Test
    public void testIsApplicable() {
        IOutputAnnotationProvider provider = new PeriodicAnnotationProvider(CATEGORY, ITimeReference.ZERO, 100L, 0, COLOR, null) {
            @Override
            public boolean isApplicable(long index) {
                return (index % 2 == 0);
            }
        };
        Map<String, List<Annotation>> expected = ImmutableMap.of(CATEGORY, Arrays.asList(
                new Annotation(0L, 0L, -1, "0", COLOR_STYLE),
                new Annotation(200L, 0L, -1, "2", COLOR_STYLE),
                new Annotation(400L, 0L, -1, "4", COLOR_STYLE),
                new Annotation(600L, 0L, -1, "6", COLOR_STYLE),
                new Annotation(800L, 0L, -1, "8", COLOR_STYLE),
                new Annotation(1000L, 0L, -1, "10", COLOR_STYLE)));
        Map<String, Object> fetchParameters = ImmutableMap.of(
                DataProviderParameterUtils.REQUESTED_TIME_KEY, StateSystemUtils.getTimes(0L, 1000L, 1));
        assertAnnotationModelResponse(expected, provider.fetchAnnotations(fetchParameters, new NullProgressMonitor()));
    }

    private static void assertAnnotationModelResponse(Map<String, List<Annotation>> expectedMap, TmfModelResponse<AnnotationModel> response) {
        assertEquals(Status.COMPLETED, response.getStatus());
        AnnotationModel model = response.getModel();
        assertNotNull(model);
        for (Entry<String, List<Annotation>> entry : expectedMap.entrySet()) {
            String category = entry.getKey();
            List<Annotation> expectedList = entry.getValue();
            Collection<@NonNull Annotation> actualCollection = model.getAnnotations().get(category);
            assertNotNull(actualCollection);
            assertEquals(expectedList.size(), actualCollection.size());
            List<Annotation> actualList = new ArrayList<>(actualCollection);
            for (int i = 0; i < expectedList.size(); i++) {
                Annotation expected = expectedList.get(i);
                Annotation actual = actualList.get(i);
                assertEquals("Time comparison for index " + i + " " + actual.toString(), expected.getTime(), actual.getTime());
                assertEquals("Duration comparison for index " + i + " " + actual.toString(), expected.getDuration(), actual.getDuration());
                assertEquals("EntryId comparison for index " + i + " " + actual.toString(), expected.getEntryId(), actual.getEntryId());
                assertEquals("Label comparison for index " + i + " " + actual.toString(), expected.getLabel(), actual.getLabel());
                assertEquals("Style comparison for index " + i + " " + actual.toString(), expected.getStyle(), actual.getStyle());
            }
        }
    }

    private static OutputElementStyle createStyle(RGBAColor color) {
        String colorString = color.toString().substring(0, 7);
        return new OutputElementStyle(null,
                ImmutableMap.of(StyleProperties.STYLE_NAME, colorString,
                        StyleProperties.COLOR, colorString,
                        StyleProperties.OPACITY, (float) (color.getAlpha() / 255.0)));
    }
}
