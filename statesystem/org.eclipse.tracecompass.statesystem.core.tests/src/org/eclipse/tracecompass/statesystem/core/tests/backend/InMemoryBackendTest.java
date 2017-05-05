/*******************************************************************************
 * Copyright (c) 2013, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests.backend;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.backend.StateHistoryBackendFactory;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.interval.TmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test cases for the in-memory backend
 *
 * @author Matthew Khouzam
 */
public class InMemoryBackendTest extends StateHistoryBackendTestBase {

    private static final String SSID = "test-ss";
    private static final int NUMBER_OF_ATTRIBUTES = 10;

    private static @Nullable IStateHistoryBackend fixture;

    /**
     * Test setup. make a state system that is moderately large
     */
    @BeforeClass
    public static void init() {
        IStateHistoryBackend backend = StateHistoryBackendFactory.createInMemoryBackend(SSID, 0);
        for (int attribute = 0; attribute < NUMBER_OF_ATTRIBUTES; attribute++) {
            for (int timeStart = 0; timeStart < 1000; timeStart++) {
                try {
                    final int stateEndTime = (timeStart * 100) + 90 + attribute;
                    final int stateStartTime = timeStart * 100 + attribute;
                    backend.insertPastState(stateStartTime, stateEndTime, attribute, timeStart % 100);
                    if (timeStart != 999) {
                        backend.insertPastState(stateEndTime + 1, stateEndTime + 9, attribute, (Object) null);
                    }
                } catch (TimeRangeException e) {
                    /* Should not happen here */
                    throw new IllegalStateException();
                }
            }
        }
        fixture = backend;
    }

    private static void testInterval(@Nullable ITmfStateInterval interval, int startTime,
            int endTime, int value) {
        assertNotNull(interval);
        assertEquals(startTime, interval.getStartTime());
        assertEquals(endTime, interval.getEndTime());
        try {
            assertEquals(value, interval.getStateValue().unboxInt());
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        }
    }

    @Override
    protected IStateHistoryBackend getBackendForBuilding(long startTime) {
        return StateHistoryBackendFactory.createInMemoryBackend(SSID, startTime);
    }

    /**
     * Test at start time
     */
    @Test
    public void testStartTime() {
        IStateHistoryBackend backend = fixture;
        assertNotNull(backend);
        assertEquals(0, backend.getStartTime());
    }

    /**
     * Test at end time
     */
    @Test
    public void testEndTime() {
        IStateHistoryBackend backend = fixture;
        assertNotNull(backend);
        assertEquals(99999, backend.getEndTime());
    }

    /**
     * Query the state system
     */
    @Test
    public void testDoQuery() {
        List<@Nullable ITmfStateInterval> interval = new ArrayList<>(NUMBER_OF_ATTRIBUTES);
        for (int i = 0; i < NUMBER_OF_ATTRIBUTES; i++) {
            interval.add(null);
        }
        IStateHistoryBackend backend = fixture;
        assertNotNull(backend);
        try {
            backend.doQuery(interval, 950);
        } catch (TimeRangeException | StateSystemDisposedException e) {
            fail(e.getMessage());
        }

        assertEquals(NUMBER_OF_ATTRIBUTES, interval.size());
        testInterval(interval.get(0), 900, 990, 9);
        testInterval(interval.get(1), 901, 991, 9);
        testInterval(interval.get(2), 902, 992, 9);
        testInterval(interval.get(3), 903, 993, 9);
        testInterval(interval.get(4), 904, 994, 9);
        testInterval(interval.get(5), 905, 995, 9);
        testInterval(interval.get(6), 906, 996, 9);
        testInterval(interval.get(7), 907, 997, 9);
        testInterval(interval.get(8), 908, 998, 9);
        testInterval(interval.get(9), 909, 999, 9);
    }


    /**
     * Test single attribute then compare it to a full query
     */
    @Test
    public void testQueryAttribute() {
        try {
            IStateHistoryBackend backend = fixture;
            assertNotNull(backend);
            ITmfStateInterval interval[] = new TmfStateInterval[10];
            for (int i = 0; i < 10; i++) {
                interval[i] = backend.doSingularQuery(950, i);
            }

            testInterval(interval[0], 900, 990, 9);
            testInterval(interval[1], 901, 991, 9);
            testInterval(interval[2], 902, 992, 9);
            testInterval(interval[3], 903, 993, 9);
            testInterval(interval[4], 904, 994, 9);
            testInterval(interval[5], 905, 995, 9);
            testInterval(interval[6], 906, 996, 9);
            testInterval(interval[7], 907, 997, 9);
            testInterval(interval[8], 908, 998, 9);
            testInterval(interval[9], 909, 999, 9);

            List<@Nullable ITmfStateInterval> intervalQuery = new ArrayList<>(NUMBER_OF_ATTRIBUTES);
            for (int i = 0; i < NUMBER_OF_ATTRIBUTES; i++) {
                intervalQuery.add(null);
            }

            backend.doQuery(intervalQuery, 950);
            ITmfStateInterval ref[] = intervalQuery.toArray(new ITmfStateInterval[0]);
            assertArrayEquals(ref, interval);

        } catch (TimeRangeException | StateSystemDisposedException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test single attribute that should not exist
     */
    @Test
    public void testQueryAttributeEmpty() {
        try {
            IStateHistoryBackend backend = fixture;
            assertNotNull(backend);
            ITmfStateInterval interval = backend.doSingularQuery(999, 0);
            assertEquals(TmfStateValue.nullValue(), interval.getStateValue());

        } catch (TimeRangeException | StateSystemDisposedException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test first element in ss
     */
    @Test
    public void testBegin() {
        try {
            IStateHistoryBackend backend = fixture;
            assertNotNull(backend);
            ITmfStateInterval interval = backend.doSingularQuery(0, 0);
            assertEquals(0, interval.getStartTime());
            assertEquals(90, interval.getEndTime());
            assertEquals(0, interval.getStateValue().unboxInt());

        } catch (TimeRangeException | StateSystemDisposedException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test last element in ss
     */
    @Test
    public void testEnd() {
        try {
            IStateHistoryBackend backend = fixture;
            assertNotNull(backend);
            ITmfStateInterval interval = backend.doSingularQuery(99998, 9);
            testInterval(interval, 99909, 99999, 99);

        } catch (TimeRangeException | StateSystemDisposedException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test out of range query
     *
     * @throws TimeRangeException
     *             Expected
     */
    @Test(expected = TimeRangeException.class)
    public void testOutOfRange_1() throws TimeRangeException {
        try {
            IStateHistoryBackend backend = fixture;
            assertNotNull(backend);
            ITmfStateInterval interval = backend.doSingularQuery(-1, 0);
            assertNull(interval);

        } catch (StateSystemDisposedException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test out of range query
     *
     * @throws TimeRangeException
     *             Expected
     */
    @Test(expected = TimeRangeException.class)
    public void testOutOfRange_2() throws TimeRangeException {
        try {
            IStateHistoryBackend backend = fixture;
            assertNotNull(backend);
            ITmfStateInterval interval = backend.doSingularQuery(100000, 0);
            assertNull(interval);

        } catch (StateSystemDisposedException e) {
            fail(e.getMessage());
        }
    }
}
