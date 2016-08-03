/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.ui.tests.view.controlflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.ControlFlowEntry;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.NaiveOptimizationAlgorithm;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ILinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeLinkEvent;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

/**
 * Series of tests to test the <strong>Optimize</strong> function of the control
 * flow view.
 * <p>
 * The <strong>Optimize</strong> function is a meta-heuristic function and thus
 * is not guaranteed to always give the perfect result, however, this method
 * should always give a better result than before. Moreover, the result should
 * be the same for the same dataset.
 *
 * @author Matthew Khouzam
 */
public class ControlFlowOptimizerTest {

    /**
     * Trace needed to satisfy nonnull requirements.
     */
    private static final @NonNull ITmfTrace TRACE = new CtfTmfTrace();

    /**
     * Overridable method to get the optimization method to test.
     *
     * @return the optimization method to test.
     */
    protected Function<Collection<ILinkEvent>, Map<Integer, Long>> getOptimizationMethod() {
        return new NaiveOptimizationAlgorithm();
    }

    /**
     * Test an empty vector.
     */
    @Test
    public void testEmpty() {
        Function<Collection<ILinkEvent>, Map<Integer, Long>> oa = getOptimizationMethod();
        Map<Integer, Long> result = oa.apply(Collections.EMPTY_LIST);
        assertNotNull(result);
        assertEquals(Collections.emptyMap(), result);
    }

    /**
     * Test a single invalid link.
     */
    @Test
    public void testSingleInvalid() {
        Function<Collection<ILinkEvent>, Map<Integer, Long>> oa = getOptimizationMethod();
        Map<Integer, Long> result = oa.apply(Collections.singleton(new TimeLinkEvent(null, null, 0, 0)));
        assertNotNull(result);
        assertEquals(Collections.emptyMap(), result);
    }

    /**
     * Test a single partially invalid link.
     */
    public void testSinglePartiallyInvalid() {
        Function<Collection<ILinkEvent>, Map<Integer, Long>> oa = getOptimizationMethod();
        Map<Integer, Long> result = oa.apply(Collections.singleton(new TimeLinkEvent(new TimeGraphEntry("Hi", 0, 1), generateCFVEntry(0), 0, 0)));
        assertNotNull(result);
        assertEquals(Collections.emptyMap(), result);
        result = oa.apply(Collections.singleton(new TimeLinkEvent(generateCFVEntry(0), new TimeGraphEntry("Hi", 0, 1), 0, 0)));
        assertNotNull(result);
        assertEquals(Collections.emptyMap(), result);
        result = oa.apply(Collections.singleton(new TimeLinkEvent(generateCFVEntry(0), null, 0, 0)));
        assertNotNull(result);
        assertEquals(Collections.emptyMap(), result);
    }

    /**
     * Test a single valid link.
     */
    @Test
    public void testSingle() {
        Function<Collection<ILinkEvent>, Map<Integer, Long>> oa = getOptimizationMethod();
        Map<Integer, Long> result = oa.apply(Collections.singleton(new TimeLinkEvent(generateCFVEntry(2), generateCFVEntry(0), 0, 0)));
        assertNotNull(result);
        assertEquals(ImmutableMap.of(0, 0L, 2, 1L), result);
    }

    /**
     * Test two valid links.
     */
    @Test
    public void testDouble() {
        Function<Collection<ILinkEvent>, Map<Integer, Long>> oa = getOptimizationMethod();

        List<ILinkEvent> links = new ArrayList<>();
        links.add(new TimeLinkEvent(generateCFVEntry(2), generateCFVEntry(0), 0, 0));
        links.add(new TimeLinkEvent(generateCFVEntry(1), generateCFVEntry(0), 0, 0));

        Map<Integer, Long> result = oa.apply(links);
        assertNotNull(result);
        assertEquals(ImmutableMap.of(0, 0L, 1, 1L, 2, 2L), result);
    }

    /**
     * Test the same links as {@link #testDouble()} but in a different order.
     */
    @Test
    public void testDoubleOtherOrder() {
        Function<Collection<ILinkEvent>, Map<Integer, Long>> oa = getOptimizationMethod();

        List<ILinkEvent> links = new ArrayList<>();
        links.add(new TimeLinkEvent(generateCFVEntry(1), generateCFVEntry(0), 0, 0));
        links.add(new TimeLinkEvent(generateCFVEntry(2), generateCFVEntry(0), 0, 0));

        Map<Integer, Long> result = oa.apply(links);
        assertNotNull(result);
        assertEquals(ImmutableMap.of(0, 0L, 1, 1L, 2, 2L), result);
    }

    /**
     * Test a pre-sorted small list. The results should be the same.
     */
    @Test
    public void test9InOrder() {

        Map<Integer, Long> expected = getExpected9();

        Function<Collection<ILinkEvent>, Map<Integer, Long>> oa = getOptimizationMethod();

        List<ILinkEvent> links = getTestVector();

        Map<Integer, Long> result = oa.apply(links);
        assertNotNull(result);
        assertEquals(expected, result);
    }

    /**
     * Test a pre-sorted small list several times. The results should be the
     * same. This tests users clicking optimize many times hoping it would
     * optimize harder.
     */
    @Test
    public void test9InOrder10Times() {
        Map<Integer, Long> expected = getExpected9();

        Function<Collection<ILinkEvent>, Map<Integer, Long>> oa = getOptimizationMethod();

        List<ILinkEvent> links = getTestVector();

        List<Map<Integer, Long>> results = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            results.add(oa.apply(links));
        }
        for (Map<Integer, Long> result : results) {
            assertEquals(expected, result);
        }
    }

    /**
     * A typical use case, the links are out of order, this makes sure they
     * still are
     */
    @Test
    public void test9OutOfOrder() {
        Map<Integer, Long> expected = getExpected9();

        Random rnd = new Random();
        rnd.setSeed(0);

        Function<Collection<ILinkEvent>, Map<Integer, Long>> oa = getOptimizationMethod();

        List<ILinkEvent> links = getTestVector();

        Collections.shuffle(links, rnd);

        Map<Integer, Long> result = oa.apply(links);
        assertNotNull(result);
        assertEquals(expected, result);
    }

    /**
     * This is a smoke test, it makes sure that the algorithm scales moderately
     * well. The typical dataset is about 100-500 elements to sort, so we add a
     * factor of safety here.
     */
    @Test
    public void testMany() {
        Random rnd = new Random();
        rnd.setSeed(12345); // The same combination as my luggage

        Function<Collection<ILinkEvent>, Map<Integer, Long>> oa = getOptimizationMethod();

        List<ILinkEvent> links = new ArrayList<>();
        int count = 25000;
        for (int i = 0; i < count; i++) {
            int src = rnd.nextInt(10000);
            int dst = rnd.nextInt(1000);
            links.add(new TimeLinkEvent(generateCFVEntry(src), generateCFVEntry(dst), src, dst));
        }

        Map<Integer, Long> result = oa.apply(links);
        assertNotNull(result);
        // calculate weight
        long initialWeight = 0;
        long optimalWeight = 0;
        for (ILinkEvent link : links) {
            long src = link.getTime();
            long dst = link.getDuration();
            initialWeight += Math.abs(src - dst);
            Long newSrc = result.get((int) src);
            Long newDst = result.get((int) dst);
            assertNotNull(newSrc);
            assertNotNull(newDst);
            optimalWeight += Math.abs(newSrc - newDst);
        }
        assertTrue(optimalWeight <= initialWeight);
    }

    /**
     * Helper function to make entries to test. As they are only looking up the
     * tid, that is the only parameter that needs to be passed.
     *
     * @param tid
     *            the TID to lookup
     * @return a {@link ControlFlowEntry} with the correct tid.
     */
    private static ControlFlowEntry generateCFVEntry(int tid) {
        return new ControlFlowEntry(0, TRACE, "exec", tid, 0, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    // -------------------------------------------------------------------------
    // Small test vector
    // -------------------------------------------------------------------------

    private static List<ILinkEvent> getTestVector() {
        List<ILinkEvent> links = new ArrayList<>();
        links.add(new TimeLinkEvent(generateCFVEntry(1), generateCFVEntry(2), 0, 0));
        links.add(new TimeLinkEvent(generateCFVEntry(1), generateCFVEntry(2), 0, 0));
        links.add(new TimeLinkEvent(generateCFVEntry(1), generateCFVEntry(2), 0, 0));
        links.add(new TimeLinkEvent(generateCFVEntry(1), generateCFVEntry(2), 0, 0));
        links.add(new TimeLinkEvent(generateCFVEntry(1), generateCFVEntry(3), 0, 0));
        links.add(new TimeLinkEvent(generateCFVEntry(1), generateCFVEntry(3), 0, 0));
        links.add(new TimeLinkEvent(generateCFVEntry(1), generateCFVEntry(3), 0, 0));
        links.add(new TimeLinkEvent(generateCFVEntry(1), generateCFVEntry(4), 0, 0));
        links.add(new TimeLinkEvent(generateCFVEntry(1), generateCFVEntry(4), 0, 0));
        links.add(new TimeLinkEvent(generateCFVEntry(1), generateCFVEntry(5), 0, 0));
        links.add(new TimeLinkEvent(generateCFVEntry(2), generateCFVEntry(4), 0, 0));
        links.add(new TimeLinkEvent(generateCFVEntry(2), generateCFVEntry(5), 0, 0));
        links.add(new TimeLinkEvent(generateCFVEntry(2), generateCFVEntry(6), 0, 0));
        links.add(new TimeLinkEvent(generateCFVEntry(2), generateCFVEntry(7), 0, 0));
        links.add(new TimeLinkEvent(generateCFVEntry(2), generateCFVEntry(8), 0, 0));
        links.add(new TimeLinkEvent(generateCFVEntry(2), generateCFVEntry(9), 0, 0));
        links.add(new TimeLinkEvent(generateCFVEntry(2), generateCFVEntry(10), 0, 0));
        return links;
    }

    private static Map<Integer, Long> getExpected9() {
        Map<Integer, Long> expected = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            expected.put(i + 1, (long) i);
        }
        return expected;
    }

}
