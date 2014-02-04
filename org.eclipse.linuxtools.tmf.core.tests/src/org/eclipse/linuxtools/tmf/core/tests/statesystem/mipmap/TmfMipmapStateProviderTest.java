/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jean-Christian Kouamé - Initial API and implementation
 *     Patrick Tasse - Updates to mipmap feature
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.statesystem.mipmap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.StateSystem;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.IStateHistoryBackend;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.InMemoryBackend;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.mipmap.AbstractTmfMipmapStateProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemOperations;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue.Type;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Jean-Christian Kouamé
 *
 */
public class TmfMipmapStateProviderTest {

    @NonNull private static final String SSID = "mimap-test";
    private static final String TEST_ATTRIBUTE_NAME = TmfMipmapStateProviderStub.TEST_ATTRIBUTE_NAME;
    private static final int NB_LEVELS = 4;
    private static final long START_TIME = 1000L;
    private static final long END_TIME = 100000000L;
    private static final long INTERVAL = 1000L;
    private static final int RESOLUTION = 16;
    private static final double DELTA = 0.0001;
    private static final long TEST_TIMESTAMP = 12345000L;
    private static StateSystem ssq;

    /**
     * Startup code, build a state system with n attributes always going up
     * linearly
     */
    @BeforeClass
    public static void init() {
        TmfMipmapStateProviderStub mmp = new TmfMipmapStateProviderStub(RESOLUTION, Type.LONG);
        IStateHistoryBackend be = new InMemoryBackend(0);
        ssq = new StateSystem(SSID, be);
        mmp.assignTargetStateSystem(ssq);

        for (long time = START_TIME; time <= END_TIME; time += INTERVAL) {
            long value = time / INTERVAL;
            ITmfEvent event = mmp.createEvent(time, value);
            mmp.processEvent(event);
        }
        mmp.dispose();
        ssq.waitUntilBuilt();
    }

    /**
     * Test a single query to the state system.
     *
     * Make sure the state system has data.
     *
     * Hint: the value read should always be t / 1000
     *
     */
    @Test
    public void testQuery() {
        assertNotNull(ssq);
        try {
            Random rn = new Random();
            long time = Math.max(INTERVAL, rn.nextLong() % END_TIME);
            List<ITmfStateInterval> intervals = ssq.queryFullState(time);
            int mipmapQuark = ssq.getQuarkAbsolute(TEST_ATTRIBUTE_NAME);
            ITmfStateInterval interval = intervals.get(mipmapQuark);
            long valueLong = interval.getStateValue().unboxLong();
            assertEquals(time / INTERVAL, valueLong);

        } catch (TimeRangeException e) {
            fail(e.getMessage());
        } catch (StateSystemDisposedException e) {
            fail(e.getMessage());
        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        }
        assertTrue(true);
    }

    /**
     * Test a single query to the state system for the maxLevel.
     *
     * Make sure the state system has data.
     *
     */
    @Test
    public void testMaxLevel() {
        assertNotNull(ssq);
        try {
            Random rn = new Random();
            long time = Math.max(INTERVAL, rn.nextLong() % END_TIME);
            List<ITmfStateInterval> intervals = ssq.queryFullState(time);

            int maxMipmapQuark = ssq.getQuarkAbsolute(TEST_ATTRIBUTE_NAME, AbstractTmfMipmapStateProvider.MAX_STRING);
            int nbLevelMax = intervals.get(maxMipmapQuark).getStateValue().unboxInt();
            assertEquals(NB_LEVELS, nbLevelMax);

            int minMipmapQuark = ssq.getQuarkAbsolute(TEST_ATTRIBUTE_NAME, AbstractTmfMipmapStateProvider.MIN_STRING);
            int nbLevelMin = intervals.get(minMipmapQuark).getStateValue().unboxInt();
            assertEquals(NB_LEVELS, nbLevelMin);

            int avgMipmapQuark = ssq.getQuarkAbsolute(TEST_ATTRIBUTE_NAME, AbstractTmfMipmapStateProvider.AVG_STRING);
            int nbLevelAvg = intervals.get(avgMipmapQuark).getStateValue().unboxInt();
            assertEquals(NB_LEVELS, nbLevelAvg);

        } catch (TimeRangeException e) {
            fail(e.getMessage());
        } catch (StateSystemDisposedException e) {
            fail(e.getMessage());
        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        }
        assertTrue(true);

    }

    /**
     * Test a single query to the state system for a mip
     *
     * Make sure the state system has data.
     *
     */
    @Test
    public void testQueryEventField() {
        assertNotNull(ssq);
        try {
            List<ITmfStateInterval> intervals = ssq.queryFullState(TEST_TIMESTAMP);
            int eventFieldQuark = ssq.getQuarkAbsolute(TEST_ATTRIBUTE_NAME);
            ITmfStateInterval interval = intervals.get(eventFieldQuark);
            long valueLong = interval.getStateValue().unboxLong();
            assertEquals(TEST_TIMESTAMP / INTERVAL, valueLong);
        } catch (TimeRangeException e) {
            fail(e.getMessage());
        } catch (StateSystemDisposedException e) {
            fail(e.getMessage());
        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        }
        assertTrue(true);
    }

    /**
     * Test a single query to the state system for a max
     *
     * Make sure the state system has data.
     *
     * Hint: the value read should always be greater than(t / 1000)
     *
     */
    @Test
    public void testQueryMipMax() {
        assertNotNull(ssq);
        try {
            List<ITmfStateInterval> intervals = ssq.queryFullState(TEST_TIMESTAMP);
            int mipmapQuark = ssq.getQuarkAbsolute(TEST_ATTRIBUTE_NAME, AbstractTmfMipmapStateProvider.MAX_STRING);

            assertEquals("max nblevels", NB_LEVELS, intervals.get(mipmapQuark).getStateValue().unboxInt());
            for (int level = 1; level < NB_LEVELS; level++) {
                long width = (long) Math.pow(RESOLUTION, level);
                int levelQuark = ssq.getQuarkRelative(mipmapQuark, String.valueOf(level));
                ITmfStateInterval interval = intervals.get(levelQuark);
                long valueLong = interval.getStateValue().unboxLong();
                assertEquals("max value @ level " + level, width + (((TEST_TIMESTAMP - START_TIME) / INTERVAL) / width) * width, valueLong);
                assertEquals("max start time @ level " + level, START_TIME + (((TEST_TIMESTAMP - START_TIME) / INTERVAL) / width) * width * INTERVAL, interval.getStartTime());
                assertEquals("max end time @ level " + level, START_TIME + (INTERVAL * width) + (((TEST_TIMESTAMP - START_TIME) / INTERVAL) / width) * width * INTERVAL, interval.getEndTime() + 1);
            }

        } catch (TimeRangeException e) {
            fail(e.getMessage());
        } catch (StateSystemDisposedException e) {
            fail(e.getMessage());
        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        }
        assertTrue(true);
    }

    /**
     * Test a single query to the state system for a min
     *
     * Make sure the state system has data.
     *
     * Hint: the value read should always be less than(t / 1000)
     */
    @Test
    public void testQueryMipMin() {
        assertNotNull(ssq);
        try {
            List<ITmfStateInterval> intervals = ssq.queryFullState(TEST_TIMESTAMP);
            int mipmapQuark = ssq.getQuarkAbsolute(TEST_ATTRIBUTE_NAME, AbstractTmfMipmapStateProvider.MIN_STRING);

            assertEquals("min nblevels", NB_LEVELS, intervals.get(mipmapQuark).getStateValue().unboxInt());
            for (int level = 1; level < NB_LEVELS; level++) {
                long width = (long) Math.pow(RESOLUTION, level);
                int levelQuark = ssq.getQuarkRelative(mipmapQuark, String.valueOf(level));
                ITmfStateInterval interval = intervals.get(levelQuark);
                long valueLong = interval.getStateValue().unboxLong();
                assertEquals("min value @ level " + level, 1 + (((TEST_TIMESTAMP - START_TIME) / INTERVAL) / width) * width, valueLong);
                assertEquals("min start time @ level " + level, START_TIME + (((TEST_TIMESTAMP - START_TIME) / INTERVAL) / width) * width * INTERVAL, interval.getStartTime());
                assertEquals("min end time @ level " + level, START_TIME + (INTERVAL * width) + (((TEST_TIMESTAMP - START_TIME) / INTERVAL) / width) * width * INTERVAL, interval.getEndTime() + 1);
            }

        } catch (TimeRangeException e) {
            fail(e.getMessage());
        } catch (StateSystemDisposedException e) {
            fail(e.getMessage());
        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        }
        assertTrue(true);
    }

    /**
     * Test a single query to the state system for an average
     *
     * Make sure the state system has data.
     *
     * Hint: the value read should always be more or less(t / 1000)
     *
     */
    @Test
    public void testQueryMipAvg() {
        assertNotNull(ssq);
        try {
            List<ITmfStateInterval> intervals = ssq.queryFullState(TEST_TIMESTAMP);
            int mipmapQuark = ssq.getQuarkAbsolute(TEST_ATTRIBUTE_NAME, AbstractTmfMipmapStateProvider.AVG_STRING);

            assertEquals("avg nblevels", NB_LEVELS, intervals.get(mipmapQuark).getStateValue().unboxInt());
            for (int level = 1; level < NB_LEVELS; level++) {
                long width = (long) Math.pow(RESOLUTION, level);
                int levelQuark = ssq.getQuarkRelative(mipmapQuark, String.valueOf(level));
                ITmfStateInterval interval = intervals.get(levelQuark);
                double valueDouble = interval.getStateValue().unboxDouble();
                assertEquals("avg value @ level " + level, 0.5 + (width / 2) + (((TEST_TIMESTAMP - START_TIME) / INTERVAL) / width) * width, valueDouble, DELTA);
                assertEquals("avg start time @ level " + level, START_TIME + (((TEST_TIMESTAMP - START_TIME) / INTERVAL) / width) * width * INTERVAL, interval.getStartTime());
                assertEquals("avg end time @ level " + level, START_TIME + (INTERVAL * width) + (((TEST_TIMESTAMP - START_TIME) / INTERVAL) / width) * width * INTERVAL, interval.getEndTime() + 1);
            }

        } catch (TimeRangeException e) {
            fail(e.getMessage());
        } catch (StateSystemDisposedException e) {
            fail(e.getMessage());
        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        }
        assertTrue(true);
    }

    /**
     * Test a full query to the state system at the startTime
     *
     * Make sure the state system has data.
     *
     * Hint: the value read should always be more or less(t / 1000)
     *
     */
    @Test
    public void testQueryValuesOnStart() {
        assertNotNull(ssq);
        try {
            int quark;

            List<ITmfStateInterval> intervals = ssq.queryFullState(START_TIME);

            int baseQuark = ssq.getQuarkAbsolute(TEST_ATTRIBUTE_NAME);
            assertEquals(START_TIME / INTERVAL, intervals.get(baseQuark).getStateValue().unboxLong());

            int maxMipmapQuark = ssq.getQuarkRelative(baseQuark, AbstractTmfMipmapStateProvider.MAX_STRING);
            assertEquals("max nblevels", NB_LEVELS, intervals.get(maxMipmapQuark).getStateValue().unboxInt());
            quark = ssq.getQuarkRelative(maxMipmapQuark, String.valueOf(1));
            assertEquals("max value @ level 1", (long) Math.pow(RESOLUTION, 1), intervals.get(quark).getStateValue().unboxLong());
            quark = ssq.getQuarkRelative(maxMipmapQuark, String.valueOf(2));
            assertEquals("max value @ level 2", (long) Math.pow(RESOLUTION, 2), intervals.get(quark).getStateValue().unboxLong());
            quark = ssq.getQuarkRelative(maxMipmapQuark, String.valueOf(3));
            assertEquals("max value @ level 3", (long) Math.pow(RESOLUTION, 3), intervals.get(quark).getStateValue().unboxLong());
            quark = ssq.getQuarkRelative(maxMipmapQuark, String.valueOf(4));
            assertEquals("max value @ level 4", (long) Math.pow(RESOLUTION, 4), intervals.get(quark).getStateValue().unboxLong());

            int minMipmapQuark = ssq.getQuarkRelative(baseQuark, AbstractTmfMipmapStateProvider.MIN_STRING);
            assertEquals("min nblevels", NB_LEVELS, intervals.get(minMipmapQuark).getStateValue().unboxInt());
            quark = ssq.getQuarkRelative(minMipmapQuark, String.valueOf(1));
            assertEquals("min value @ level 1", START_TIME / INTERVAL, intervals.get(quark).getStateValue().unboxLong());
            quark = ssq.getQuarkRelative(minMipmapQuark, String.valueOf(2));
            assertEquals("min value @ level 2", START_TIME / INTERVAL, intervals.get(quark).getStateValue().unboxLong());
            quark = ssq.getQuarkRelative(minMipmapQuark, String.valueOf(3));
            assertEquals("min value @ level 3", START_TIME / INTERVAL, intervals.get(quark).getStateValue().unboxLong());
            quark = ssq.getQuarkRelative(minMipmapQuark, String.valueOf(4));
            assertEquals("min value @ level 4", START_TIME / INTERVAL, intervals.get(quark).getStateValue().unboxLong());

            int avgMipmapQuark = ssq.getQuarkRelative(baseQuark, AbstractTmfMipmapStateProvider.AVG_STRING);
            assertEquals("avg nblevels", NB_LEVELS, intervals.get(avgMipmapQuark).getStateValue().unboxInt());
            quark = ssq.getQuarkRelative(avgMipmapQuark, String.valueOf(1));
            assertEquals("avg value @ level 1", 0.5 + Math.pow(RESOLUTION, 1) / 2, intervals.get(quark).getStateValue().unboxDouble(), DELTA);
            quark = ssq.getQuarkRelative(avgMipmapQuark, String.valueOf(2));
            assertEquals("avg value @ level 2", 0.5 + Math.pow(RESOLUTION, 2) / 2, intervals.get(quark).getStateValue().unboxDouble(), DELTA);
            quark = ssq.getQuarkRelative(avgMipmapQuark, String.valueOf(3));
            assertEquals("avg value @ level 3", 0.5 + Math.pow(RESOLUTION, 3) / 2, intervals.get(quark).getStateValue().unboxDouble(), DELTA);
            quark = ssq.getQuarkRelative(avgMipmapQuark, String.valueOf(4));
            assertEquals("avg value @ level 4", 0.5 + Math.pow(RESOLUTION, 4) / 2, intervals.get(quark).getStateValue().unboxDouble(), DELTA);

        } catch (TimeRangeException e) {
            fail(e.getMessage());
        } catch (StateSystemDisposedException e) {
            fail(e.getMessage());
        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        }
        assertTrue(true);
    }

    /**
     * Test a full query to the state system when the end time
     *
     * Make sure the state system has data.
     *
     * Hint: the value read should always be more or less(t / 1000)
     *
     */
    @Test
    public void testQueryValuesOnClose() {
        assertNotNull(ssq);
        try {
            int quark;

            List<ITmfStateInterval> intervals = ssq.queryFullState(END_TIME);

            int baseQuark = ssq.getQuarkAbsolute(TEST_ATTRIBUTE_NAME);
            assertEquals(END_TIME / INTERVAL, intervals.get(baseQuark).getStateValue().unboxLong());

            int maxMipmapQuark = ssq.getQuarkRelative(baseQuark, AbstractTmfMipmapStateProvider.MAX_STRING);
            assertEquals("max nblevels", NB_LEVELS, intervals.get(maxMipmapQuark).getStateValue().unboxInt());
            quark = ssq.getQuarkRelative(maxMipmapQuark, String.valueOf(1));
            assertEquals("max value @ level 1", END_TIME / INTERVAL, intervals.get(quark).getStateValue().unboxLong());
            quark = ssq.getQuarkRelative(maxMipmapQuark, String.valueOf(2));
            assertEquals("max value @ level 2", END_TIME / INTERVAL, intervals.get(quark).getStateValue().unboxLong());
            quark = ssq.getQuarkRelative(maxMipmapQuark, String.valueOf(3));
            assertEquals("max value @ level 3", END_TIME / INTERVAL, intervals.get(quark).getStateValue().unboxLong());
            quark = ssq.getQuarkRelative(maxMipmapQuark, String.valueOf(4));
            assertEquals("max value @ level 4", END_TIME / INTERVAL, intervals.get(quark).getStateValue().unboxLong());

            int minMipmapQuark = ssq.getQuarkRelative(baseQuark, AbstractTmfMipmapStateProvider.MIN_STRING);
            assertEquals("min nblevels", NB_LEVELS, intervals.get(minMipmapQuark).getStateValue().unboxInt());
            quark = ssq.getQuarkRelative(minMipmapQuark, String.valueOf(1));
            assertEquals("min value @ level 1", END_TIME / INTERVAL - (END_TIME - START_TIME) / INTERVAL % (long) Math.pow(RESOLUTION, 1), intervals.get(quark).getStateValue().unboxLong());
            quark = ssq.getQuarkRelative(minMipmapQuark, String.valueOf(2));
            assertEquals("min value @ level 2", END_TIME / INTERVAL - (END_TIME - START_TIME) / INTERVAL % (long) Math.pow(RESOLUTION, 2), intervals.get(quark).getStateValue().unboxLong());
            quark = ssq.getQuarkRelative(minMipmapQuark, String.valueOf(3));
            assertEquals("min value @ level 3", END_TIME / INTERVAL - (END_TIME - START_TIME) / INTERVAL % (long) Math.pow(RESOLUTION, 3), intervals.get(quark).getStateValue().unboxLong());
            quark = ssq.getQuarkRelative(minMipmapQuark, String.valueOf(4));
            assertEquals("min value @ level 4", END_TIME / INTERVAL - (END_TIME - START_TIME) / INTERVAL % (long) Math.pow(RESOLUTION, 4), intervals.get(quark).getStateValue().unboxLong());

            int avgMipmapQuark = ssq.getQuarkRelative(baseQuark, AbstractTmfMipmapStateProvider.AVG_STRING);
            assertEquals("avg nblevels", NB_LEVELS, intervals.get(avgMipmapQuark).getStateValue().unboxInt());
            quark = ssq.getQuarkRelative(avgMipmapQuark, String.valueOf(1));
            assertEquals("avg value @ level 1", (long) (END_TIME / INTERVAL - (double) ((END_TIME - START_TIME) / INTERVAL % (long) Math.pow(RESOLUTION, 1)) / 2), intervals.get(quark).getStateValue().unboxDouble(), DELTA);
            quark = ssq.getQuarkRelative(avgMipmapQuark, String.valueOf(2));
            assertEquals("avg value @ level 2", (long) (END_TIME / INTERVAL - (double) ((END_TIME - START_TIME) / INTERVAL % (long) Math.pow(RESOLUTION, 2)) / 2), intervals.get(quark).getStateValue().unboxDouble(), DELTA);
            quark = ssq.getQuarkRelative(avgMipmapQuark, String.valueOf(3));
            assertEquals("avg value @ level 3", (long) (END_TIME / INTERVAL - (double) ((END_TIME - START_TIME) / INTERVAL % (long) Math.pow(RESOLUTION, 3)) / 2), intervals.get(quark).getStateValue().unboxDouble(), DELTA);
            quark = ssq.getQuarkRelative(avgMipmapQuark, String.valueOf(4));
            assertEquals("avg value @ level 4", (long) (END_TIME / INTERVAL - (double) ((END_TIME - START_TIME) / INTERVAL % (long) Math.pow(RESOLUTION, 4)) / 2), intervals.get(quark).getStateValue().unboxDouble(), DELTA);

        } catch (TimeRangeException e) {
            fail(e.getMessage());
        } catch (StateSystemDisposedException e) {
            fail(e.getMessage());
        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        }
        assertTrue(true);
    }

    /**
     * Test a query range to the state system to get the maximum value in the
     * range
     *
     * Make sure the state system has data.
     *
     *
     */
    @Test
    public void testQueryMipmapRangeMax() {
        assertNotNull(ssq);
        try {
            long max;
            int quark = ssq.getQuarkAbsolute(TEST_ATTRIBUTE_NAME);

            max = TmfStateSystemOperations.queryRangeMax(ssq, 0, START_TIME, quark).unboxLong();
            assertEquals(START_TIME / INTERVAL, max);

            max = TmfStateSystemOperations.queryRangeMax(ssq, START_TIME, START_TIME, quark).unboxLong();
            assertEquals(START_TIME / INTERVAL, max);

            max = TmfStateSystemOperations.queryRangeMax(ssq, START_TIME, END_TIME / 2, quark).unboxLong();
            assertEquals((END_TIME / 2 / INTERVAL), max);

            max = TmfStateSystemOperations.queryRangeMax(ssq, 0, END_TIME, quark).unboxLong();
            assertEquals(END_TIME / INTERVAL, max);

            max = TmfStateSystemOperations.queryRangeMax(ssq, END_TIME / 2, END_TIME, quark).unboxLong();
            assertEquals(END_TIME / INTERVAL, max);

            max = TmfStateSystemOperations.queryRangeMax(ssq, START_TIME - INTERVAL / 2, END_TIME / 2 + INTERVAL / 2, quark).unboxLong();
            assertEquals(END_TIME / 2 / INTERVAL, max);

        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        } catch (TimeRangeException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test a query range to the state system to get the minimum value in the
     * range
     *
     * Make sure the state system has data.
     *
     *
     */
    @Test
    public void testQueryMipmapRangeMin() {
        assertNotNull(ssq);
        try {
            long min;
            int quark = ssq.getQuarkAbsolute(TEST_ATTRIBUTE_NAME);

            min = TmfStateSystemOperations.queryRangeMin(ssq, 0, START_TIME, quark).unboxLong();
            assertEquals(START_TIME / INTERVAL, min);

            min = TmfStateSystemOperations.queryRangeMin(ssq, START_TIME, START_TIME, quark).unboxLong();
            assertEquals(START_TIME / INTERVAL, min);

            min = TmfStateSystemOperations.queryRangeMin(ssq, START_TIME, END_TIME / 2, quark).unboxLong();
            assertEquals((START_TIME / INTERVAL), min);

            min = TmfStateSystemOperations.queryRangeMin(ssq, 0, END_TIME, quark).unboxLong();
            assertEquals(START_TIME / INTERVAL, min);

            min = TmfStateSystemOperations.queryRangeMin(ssq, END_TIME / 2, END_TIME, quark).unboxLong();
            assertEquals(END_TIME / 2 / INTERVAL, min);

            min = TmfStateSystemOperations.queryRangeMin(ssq, START_TIME - INTERVAL / 2, END_TIME / 2 + INTERVAL / 2, quark).unboxLong();
            assertEquals(START_TIME / INTERVAL, min);

        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        } catch (TimeRangeException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test a query range to the state system to get the average value in the
     * range
     *
     * Make sure the state system has data.
     *
     */
    @Test
    public void testQueryMipmapRangeAvg() {
        assertNotNull(ssq);
        try {
            double avg;
            int quark = ssq.getQuarkAbsolute(TEST_ATTRIBUTE_NAME);

            avg = TmfStateSystemOperations.queryRangeAverage(ssq, 0, START_TIME, quark);
            assertEquals((double) (START_TIME - INTERVAL) / INTERVAL, avg, DELTA);

            avg = TmfStateSystemOperations.queryRangeAverage(ssq, START_TIME, START_TIME, quark);
            assertEquals((double) START_TIME / INTERVAL, avg, DELTA);

            avg = TmfStateSystemOperations.queryRangeAverage(ssq, START_TIME, END_TIME / 2, quark);
            assertEquals((double) (START_TIME + (END_TIME / 2 - INTERVAL)) / 2 / INTERVAL, avg, DELTA);

            avg = TmfStateSystemOperations.queryRangeAverage(ssq, 0, END_TIME, quark);
            assertEquals((double) (END_TIME - INTERVAL) / 2 / INTERVAL, avg, DELTA);

            avg = TmfStateSystemOperations.queryRangeAverage(ssq, END_TIME / 2, END_TIME, quark);
            assertEquals((double) (END_TIME / 2 + (END_TIME - INTERVAL)) / 2 / INTERVAL, avg, DELTA);

            avg = TmfStateSystemOperations.queryRangeAverage(ssq, START_TIME - INTERVAL / 2, END_TIME / 2 + INTERVAL / 2, quark);
            assertEquals((double) (START_TIME + (END_TIME / 2 - INTERVAL)) / 2 / INTERVAL, avg, DELTA);

        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        } catch (TimeRangeException e) {
            fail(e.getMessage());
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        }
    }
}
