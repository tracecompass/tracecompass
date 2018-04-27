/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests.shared.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import com.google.common.base.Objects;

/**
 * Contains utilities to test the content of state systems
 *
 * @author Geneviève Bastien
 */
public final class StateSystemTestUtils {

    private StateSystemTestUtils() {

    }

    /**
     * Test that the intervals for a given attribute correspond to what is
     * expected. If the attribute does not exist or if one of the interval does
     * not correspond, it will fail the test.
     *
     * @param ss
     *            The state system containing the attribute
     * @param path
     *            The path to the attribute
     * @param expected
     *            The list of intervals the attribute should have
     */
    public static void testIntervalForAttributes(ITmfStateSystem ss, List<ITmfStateInterval> expected, String... path) {
        try {
            int quark = ss.getQuarkAbsolute(path);
            List<ITmfStateInterval> actual = StateSystemUtils.queryHistoryRange(ss, quark, ss.getStartTime(), ss.getCurrentEndTime());
            /*
             * This unit test must help debug why a test fail, that is why we do
             * not test the number of intervals at this point. We make sure each
             * of the expected interval exists and has the same start and end
             * times and state value
             */
            for (int i = 0; i < expected.size(); i++) {
                if (i >= actual.size()) {
                    fail(Arrays.deepToString(path) + ": Missing interval " + i);
                }
                ITmfStateInterval act = actual.get(i);
                ITmfStateInterval exp = expected.get(i);
                if (!compareIntervalContent(exp, act)) {
                    fail(Arrays.deepToString(path) + ":Interval at position " + i + ": expected: " + displayInterval(exp) + " actual: " + displayInterval(act));
                }
            }
            /* Make sure there is no extra interval in the actual */
            assertEquals(Arrays.deepToString(path) + ":Number of intervals", expected.size(), actual.size());
        } catch (AttributeNotFoundException e) {
            fail("Attribute " + Arrays.deepToString(path) + " does not exist");
        } catch (StateSystemDisposedException e) {
            fail("State system was disposed");
        }
    }

    private static boolean compareIntervalContent(ITmfStateInterval expected, ITmfStateInterval actual) {
        Object expectedVal = expected.getValue();
        Object actualVal = actual.getValue();
        return (expected.getStartTime() == actual.getStartTime()) &&
                (expected.getEndTime() == actual.getEndTime()) &&
                (Objects.equal(expectedVal, actualVal));
    }

    private static String displayInterval(ITmfStateInterval interval) {
        return "[" + interval.getStartTime() + "," + interval.getEndTime() + "]:" + interval.getValue();
    }

    /**
     * Test that the values in the state system at time t correspond to what is
     * expected
     *
     * @param ss
     *            The state system containing the values
     * @param t
     *            The time at which to test the values
     * @param expected
     *            A mapping between the full path of an attribute and its
     *            expected value
     */
    public static void testValuesAtTime(ITmfStateSystem ss, long t, Map<String[], @Nullable Object> expected) {
        try {
            List<ITmfStateInterval> intervals = ss.queryFullState(t);
            for (Entry<String[], @Nullable Object> entry : expected.entrySet()) {
                String[] path = entry.getKey();
                try {
                    int quark = ss.getQuarkAbsolute(entry.getKey());
                    ITmfStateInterval interval = intervals.get(quark);
                    assertEquals(Arrays.deepToString(path) + " at time " + t, entry.getValue(), interval.getValue());
                } catch (AttributeNotFoundException e) {
                    fail("Attribute " + Arrays.deepToString(path) + " does not exist at time " + t);
                }

            }
        } catch (StateSystemDisposedException e) {
            fail("State system was disposed");
        }
    }

    /**
     * Utility method to return an attribute path as an array using varargs
     *
     * @param path
     *            The path of the attribute
     * @return The path as an array of String
     */
    public static @NonNull String[] makeAttribute(@NonNull String... path) {
        return path;
    }

}
