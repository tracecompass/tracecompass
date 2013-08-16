/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Adapt to junit.framework.TestCase
 *   Alexandre Montplaisir - Port to JUnit4
 *   Patrick Tasse - Support selection range
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.histogram;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.views.histogram.HistogramDataModel;
import org.eclipse.linuxtools.tmf.ui.views.histogram.HistogramScaledData;
import org.eclipse.linuxtools.tmf.ui.views.histogram.IHistogramModelListener;
import org.junit.Test;

/**
 * Unit tests for the HistogramDataModel class.
 */
public class HistogramDataModelTest {

    private static final double DELTA = 1e-15;

    /**
     * Test method for {@link HistogramDataModel#HistogramDataModel()}.
     */
    @Test
    public void testHistogramDataModel() {
        HistogramDataModel model = new HistogramDataModel();
        testModelConsistency(model, HistogramDataModel.DEFAULT_NUMBER_OF_BUCKETS,0, 1, 0 , 0 , 0 , HistogramDataModel.DEFAULT_NUMBER_OF_BUCKETS);
    }

    /**
     * Test method for {@link HistogramDataModel#HistogramDataModel(int)}.
     */
    @Test
    public void testHistogramDataModelInt() {
        final int nbBuckets = 5 * 1000;
        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        testModelConsistency(model, nbBuckets, 0, 1, 0, 0, 0, nbBuckets);
    }

    /**
     * Test methods for {@link HistogramDataModel#countEvent(long,long)}.
     */
    @Test
    public void testClear() {
        final int nbBuckets = 100;
        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        model.countEvent(0, -1);

        testModelConsistency(model, nbBuckets, 0, 1, 0, 0, 0, nbBuckets);
    }

    /**
     * Test methods for {@link HistogramDataModel#countEvent(long,long)}.
     */
    @Test
    public void testCountEvent_0() {
        final int nbBuckets = 100;
        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        model.countEvent(0, -1);

        testModelConsistency(model, nbBuckets, 0, 1, 0, 0, 0, nbBuckets);
    }

    /**
     * Test methods for {@link HistogramDataModel#countEvent(long,long)} and
     * {@link HistogramDataModel#scaleTo(int,int,int)}.
     */
    @Test
    public void testCountEvent_1() {
        final int nbBuckets = 100;
        final int maxHeight = 10;

        HistogramDataModel model = new HistogramDataModel(nbBuckets);

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight, 1);

        for (int i = 0; i < result.fData.length; i++) {
            assertEquals(0, result.fData[i]);
        }

        testModelConsistency(model, nbBuckets, 0, 1, 0, 0, 0, nbBuckets);
    }

    /**
     * Test methods for {@link HistogramDataModel#countEvent(long,long)} and
     * {@link HistogramDataModel#scaleTo(int,int,int)}.
     */
    @Test
    public void testCountEvent_2() {
        final int nbBuckets = 100;
        final int maxHeight = 10;

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        model.countEvent(0, 1);

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight, 1);
        assertEquals(1, result.fData[0]);

        assertArrayEqualsInt(0, result.fData,1);

        testModelConsistency(model, nbBuckets, 1, 1, 1, 1, 1, nbBuckets + 1);
    }

    /**
     * Test methods for {@link HistogramDataModel#countEvent(long,long)} and
     * {@link HistogramDataModel#scaleTo(int,int,int)}.
     */
    @Test
    public void testCountEvent_3() {
        final int nbBuckets = 100;
        final int maxHeight = 10;

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        countEventsInModel(nbBuckets, model);

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight, 1);

        assertArrayEqualsInt(1, result.fData);

        testModelConsistency(model, nbBuckets, nbBuckets, 1, 0, 0, nbBuckets - 1, nbBuckets);
    }

    /**
     * Test methods for {@link HistogramDataModel#countEvent(long,long)} and
     * {@link HistogramDataModel#scaleTo(int,int,int)}.
     */
    @Test
    public void testCountEvent_4() {
        final int nbBuckets = 100;
        final int maxHeight = 10;

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        // to different to call elsewhere
        for (int i = 0; i < nbBuckets; i++) {
            model.countEvent(i, i);
            model.countEvent(i + 1, i);
        }

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight, 1);

        assertArrayEqualsInt(2, result.fData);

        testModelConsistency(model, nbBuckets, 2 * nbBuckets, 1, 0, 0, nbBuckets- 1, nbBuckets);
    }


    /**
     * Test methods for {@link HistogramDataModel#countEvent(long,long)} and
     * {@link HistogramDataModel#scaleTo(int,int,int)}.
     */
    @Test
    public void testCountEvent_5() {
        final int nbBuckets = 100;
        final int startTime = 25;
        final int maxHeight = 10;

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        for (int i = startTime; i < startTime + nbBuckets; i++) {
            model.countEvent(i, i);
        }

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight, 1);

        assertArrayEqualsInt(1, result.fData);

        testModelConsistency(model, nbBuckets, nbBuckets, 1, startTime, startTime, startTime + nbBuckets- 1, startTime + nbBuckets);
    }

    /**
     * Test method for {@link HistogramDataModel#scaleTo(int,int,int)}.
     */
    @Test
    public void testScaleTo_0() {
        HistogramDataModel model = new HistogramDataModel(10);
        try {
            model.scaleTo(10, 0, 1);
        } catch (AssertionError e1) {
            try {
                model.scaleTo(0, 10, 1);
            } catch (AssertionError e2) {
                try {
                    model.scaleTo(0, 0, 1);
                } catch (AssertionError e3) {
                    return;
                }
            }
        }
        fail("Uncaught assertion error");
    }

    /**
     * Test method for {@link HistogramDataModel#scaleTo(int,int,int)}.
     */
    @Test
    public void testScaleTo_1() {
        final int nbBuckets = 10;
        final int maxHeight = 10;
        final int nbEvents = nbBuckets / 2;
        final int[] expectedResult = new int[] { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0 };

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        countEventsInModel(nbEvents, model);

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight, 1);

        assertArrayEquals( expectedResult, result.fData);

        testModelConsistency(model, nbBuckets, nbEvents, 1, 0, 0, nbEvents - 1, nbBuckets);
    }

    /**
     * Test method for {@link HistogramDataModel#scaleTo(int,int,int)}.
     */
    @Test
    public void testScaleTo_2() {
        final int nbBuckets = 10;
        final int maxHeight = 10;
        final int nbEvents = nbBuckets;
        final int[] expectedResult = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        countEventsInModel(nbEvents, model);

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight, 1);

        assertArrayEquals(expectedResult, result.fData);

        testModelConsistency(model, nbBuckets, nbEvents, 1, 0, 0, nbEvents - 1, nbBuckets);
    }

    /**
     * Test method for {@link HistogramDataModel#scaleTo(int,int,int)}.
     */
    @Test
    public void testScaleTo_3() {
        final int nbBuckets = 10;
        final int maxHeight = 10;
        final int nbEvents = 2 * nbBuckets;
        final int[] expectedResult = new int[] { 2, 2, 2, 2, 2, 2, 2, 2, 2, 2 };

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        countEventsInModel(nbEvents, model);

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight, 1);

        assertArrayEquals(expectedResult, result.fData);

        testModelConsistency(model, nbBuckets, nbEvents, 2, 0, 0, nbEvents - 1, 2 * nbBuckets);
    }

    /**
     * Test method for {@link HistogramDataModel#scaleTo(int,int,int)}.
     */
    @Test
    public void testScaleTo_4() {
        final int nbBuckets = 10;
        final int maxHeight = 10;
        final int nbEvents = 3 * nbBuckets;
        final int[] expectedResult = new int[] { 4, 4, 4, 4, 4, 4, 4, 2, 0, 0 };

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        countEventsInModel(nbEvents, model);

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight, 1);

        assertArrayEquals(expectedResult, result.fData);

        testModelConsistency(model, nbBuckets, nbEvents, 4, 0, 0, nbEvents - 1, 4 * nbBuckets);
    }

    /**
     * Test method for {@link HistogramDataModel#scaleTo(int,int,int)}.
     */
    @Test
    public void testScaleTo_5() {
        final int nbBuckets = 100;
        final int maxHeight = 20;
        final int nbEvents = 2 * nbBuckets;
        final int[] expectedResult = new int[] { 20, 20, 20, 20, 20, 20, 20, 20, 20, 20 };

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        countEventsInModel(nbEvents, model);

        HistogramScaledData result = model.scaleTo(10, maxHeight, 1);

        assertArrayEquals(expectedResult, result.fData);

        testModelConsistency(model, nbBuckets, nbEvents, 2, 0, 0, nbEvents - 1, 2 * nbBuckets);
    }

    /**
     * Test method for {@link HistogramDataModel#scaleTo(int,int,int)}.
     */
    @Test
    public void testScaleTo_6() {
        final int nbBuckets = 100;
        final int maxHeight = 24;
        final int nbEvents = 2 * nbBuckets + 1;
        final int[] expectedResult = new int[] { 24, 24, 24, 24, 24, 24, 24, 24, 9, 0 };

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        countEventsInModel(nbEvents, model);

        HistogramScaledData result = model.scaleTo(10, maxHeight, 1);

        assertArrayEquals(expectedResult, result.fData);

        testModelConsistency(model, nbBuckets, nbEvents, 4, 0, 0, nbEvents - 1, 4 * nbBuckets);
    }

    /**
     * Test method for {@link HistogramDataModel#scaleTo(int,int,int)}.
     */
    @Test
    public void testScaleTo_7() {
        // verify scaleTo with barWidth > 1
        final int nbBuckets = 100;
        final int maxHeight = 24;
        final int width = 10;
        final int barWidth = 4;
        final int nbEvents = 2 * nbBuckets + 1;

        // (int)(width / barWith) = 2
        // -> 2 bars -> expected result needs two buckets (scaled data)
        //
        // buckets (in model) per bar = last bucket id / nbBars + 1 (plus 1 to
        // cover all used buckets)
        // -> buckets per bar = 50 / 2 + 1 = 26
        // -> first entry in expected result is 26 * 4 = 104
        // -> second entry in expected result is 22 * 4 + 9 = 97
        final int[] expectedResult = new int[] { 104, 97 };

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        countEventsInModel(nbEvents, model);

        // verify scaled data
        HistogramScaledData result = model.scaleTo(width, maxHeight, barWidth);

        assertEquals(4 * 26, result.fBucketDuration);
        assertEquals(0, result.fSelectionBeginBucket);
        assertEquals(0, result.fSelectionEndBucket);
        assertEquals(0, result.fFirstBucketTime);
        assertEquals(0, result.fFirstEventTime);
        assertEquals(1, result.fLastBucket);
        assertEquals(104, result.fMaxValue);
        assertEquals((double) maxHeight / 104, result.fScalingFactor, DELTA);
        assertEquals(maxHeight, result.fHeight);
        assertEquals(width, result.fWidth);
        assertEquals(barWidth, result.fBarWidth);

        assertArrayEquals(expectedResult, result.fData);

        // verify model
        testModelConsistency(model, nbBuckets, nbEvents, 4, 0, 0, nbEvents - 1, 4 * nbBuckets);
    }

    /**
     * Test method for {@link HistogramDataModel#scaleTo(int,int,int)}.
     */
    @Test
    public void testScaleToReverse_1() {
        final int nbBuckets = 100;
        final int maxHeight = 24;
        final int width = 10;
        final int barWidth = 1;
        final int nbEvents = 2 * nbBuckets + 1;

        // (int)(width / barWith) = 10
        // -> 10 bars -> expected result needs 10 buckets (scaled data)
        //
        // buckets in (model) per bar = last bucket id / nbBars + 1 (plus 1 to
        // cover all used buckets)
        // -> buckets per bar = 50 / 10 + 1 = 6
        final int[] expectedResult = new int[] { 21, 24, 24, 24, 24, 24, 24, 24, 12, 0 };

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        countInvertedEvents(nbEvents, model);

        // verify scaled data
        HistogramScaledData result = model.scaleTo(width, maxHeight, barWidth);

        assertEquals(4 * 6, result.fBucketDuration);
        assertEquals(0, result.fSelectionBeginBucket);
        assertEquals(0, result.fSelectionEndBucket);
        assertEquals(-3, result.fFirstBucketTime); // negative is correct, can
                                                   // happen when reverse
        assertEquals(0, result.fFirstEventTime);
        assertEquals(9, result.fLastBucket);
        assertEquals(24, result.fMaxValue);
        assertEquals((double) maxHeight / 24, result.fScalingFactor, DELTA);
        assertEquals(maxHeight, result.fHeight);
        assertEquals(width, result.fWidth);
        assertEquals(barWidth, result.fBarWidth);

        assertArrayEquals(expectedResult, result.fData);

        // verify model
        testModelConsistency(model, nbBuckets, nbEvents, 4, -3, 0, nbEvents - 1, -3 + 4 * nbBuckets);
    }

    private static void countInvertedEvents(final int nbEvents, HistogramDataModel model) {
        for (int i = nbEvents - 1; i >= 0; i--) {
            model.countEvent(i, i);
        }
    }

    /**
     * Test method for {@link HistogramDataModel#scaleTo(int,int,int)}.
     */
    @Test
    public void testScaleToReverse_2() {
        final int nbBuckets = 100;
        final int maxHeight = 24;
        final int width = 10;
        final int barWidth = 1;

        final int nbEvents = 2 * nbBuckets;

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        countEventsInModel(nbEvents, model);

        HistogramScaledData result = model.scaleTo(width, maxHeight, barWidth);

        model.clear();

        countInvertedEvents(nbEvents, model);

        HistogramScaledData revResult = model.scaleTo(width, maxHeight, barWidth);

        testModelConsistency(model, nbBuckets, nbEvents, 2, 0, 0, nbEvents - 1, 2 * nbBuckets);

        // For the above number of events, result and revResult are exactly the same.

        assertEquals(result.fBucketDuration, revResult.fBucketDuration);
        assertEquals(result.fSelectionBeginBucket, revResult.fSelectionBeginBucket);
        assertEquals(result.fSelectionEndBucket, revResult.fSelectionEndBucket);
        assertEquals(result.fFirstBucketTime, revResult.fFirstBucketTime);
        assertEquals(result.fMaxValue, revResult.fMaxValue);
        assertEquals(result.fScalingFactor, revResult.fScalingFactor, DELTA);
        assertEquals(result.fLastBucket, revResult.fLastBucket);
        assertEquals(result.getBucketEndTime(0), revResult.getBucketEndTime(0));
        assertEquals(result.getBucketStartTime(0), revResult.getBucketStartTime(0));

        assertArrayEquals(revResult.fData, result.fData);
    }

    /**
     * Test method for testing model listener.
     */
    @Test
    public void testModelListener() {
        final int nbBuckets = 2000;
        final int nbEvents = 10 * nbBuckets + 256;
        final int[] count = new int[1];
        count[0] = 0;

        // Test add listener and call of listener
        IHistogramModelListener listener = new IHistogramModelListener() {
            @Override
            public void modelUpdated() {
                count[0]++;
            }
        };

        // Test that the listener interface is called every 16000 events.
        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        model.addHistogramListener(listener);

        countEventsInModel(nbEvents, model, 1);

        assertEquals(1, count[0]);

        // Test that the listener interface is called when complete is called.
        model.complete();
        assertEquals(2, count[0]);

        // Test that clear triggers call of listener interface
        model.clear();
        assertEquals(3, count[0]);

        // Test remove listener
        count[0] = 0;
        model.removeHistogramListener(listener);

        countEventsInModel(nbEvents, model);
        model.complete();
        assertEquals(0, count[0]);
    }

    /**
     * Test method for {@link HistogramDataModel#scaleTo(int,int,int)}.
     */
    @Test
    public void testLostEventsScaleTo_0() {
        final int nbBuckets = 10;
        final int maxHeight = 10;
        final int nbEvents = 3 * nbBuckets;
        final int nbLostEvents_0 = 4;
        final int nbLostEvents_1 = 9;
        final int nbCombinedEvents = nbEvents + 2;
        final int[] expectedResult = new int[] { 4, 4, 4, 4, 4, 4, 4, 2, 0, 0 };
        final int[] expectedLostEventsResult = new int[] { 0, 2, 2, 0, 3, 3, 3, 0, 0, 0 };

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        countEventsInModel(nbEvents, model);

        final TmfTimeRange timeRange_0 = new TmfTimeRange(
                new TmfTimestamp(5L, ITmfTimestamp.NANOSECOND_SCALE),
                new TmfTimestamp(10L, ITmfTimestamp.NANOSECOND_SCALE));
        model.countLostEvent(timeRange_0, nbLostEvents_0, false);

        final TmfTimeRange timeRange_1 = new TmfTimeRange(
                new TmfTimestamp(18L, ITmfTimestamp.NANOSECOND_SCALE),
                new TmfTimestamp(27L, ITmfTimestamp.NANOSECOND_SCALE));
        model.countLostEvent(timeRange_1, nbLostEvents_1, false);

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight, 1);

        assertArrayEquals(expectedResult, result.fData);

        assertArrayEquals(expectedLostEventsResult, result.fLostEventsData);

        testModelConsistency(model, nbBuckets, nbCombinedEvents, 4, 0, 0, nbEvents - 1, 4 * nbBuckets);
        assertEquals(7, result.fMaxCombinedValue);
    }

    /**
     * Test method for {@link HistogramDataModel#scaleTo(int,int,int)}.
     */
    @Test
    public void testLostEventsScaleTo_1() {
        final int nbBuckets = 10;
        final int maxHeight = 10;
        final int nbEvents = 3 * nbBuckets;
        final int nbLostEvents_0 = 4;
        final int nbLostEvents_1 = 9;
        final int nbCombinedEvents = nbEvents + 2;
        final int[] expectedLostEventsResult = new int[] { 0, 2, 5, 3, 3, 0, 0, 0, 0, 0 };

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        countEventsInModel(nbEvents, model);

        final TmfTimeRange timeRange_0 = new TmfTimeRange(
                new TmfTimestamp(5L, ITmfTimestamp.NANOSECOND_SCALE),
                new TmfTimestamp(10L, ITmfTimestamp.NANOSECOND_SCALE));
        model.countLostEvent(timeRange_0, nbLostEvents_0, false);

        final TmfTimeRange timeRange_1 = new TmfTimeRange(
                new TmfTimestamp(11L, ITmfTimestamp.NANOSECOND_SCALE),
                new TmfTimestamp(18L, ITmfTimestamp.NANOSECOND_SCALE));
        model.countLostEvent(timeRange_1, nbLostEvents_1, false);

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight, 1);

        assertArrayEquals(expectedLostEventsResult, result.fLostEventsData);

        testModelConsistency(model, nbBuckets, nbCombinedEvents, 4, 0, 0, nbEvents - 1, 4 * nbBuckets);
        assertEquals(9, result.fMaxCombinedValue);
    }

    /**
     * Test method for {@link HistogramDataModel#scaleTo(int,int,int)}.
     */
    @Test
    public void testLostEventsScaleTo_2() {
        final int nbBuckets = 10;
        final int maxHeight = 10;
        final int nbEvents = 3 * nbBuckets;
        final int nbLostEvents_0 = 5;
        final int nbLostEvents_1 = 15;
        final int nbLostEvents_2 = 2;
        final int nbCombinedEvents = nbEvents + 3;
        final int[] expectedLostEventsResult = new int[] { 0, 0, 3, 3, 6, 5, 3, 2, 0, 0 };

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        countEventsInModel(nbEvents, model);

        final TmfTimeRange timeRange_0 = new TmfTimeRange(
                new TmfTimestamp(18L, ITmfTimestamp.NANOSECOND_SCALE),
                new TmfTimestamp(22L, ITmfTimestamp.NANOSECOND_SCALE));
        model.countLostEvent(timeRange_0, nbLostEvents_0, false);

        final TmfTimeRange timeRange_2 = new TmfTimeRange(
                new TmfTimestamp(28L, ITmfTimestamp.NANOSECOND_SCALE),
                new TmfTimestamp(29L, ITmfTimestamp.NANOSECOND_SCALE));
        model.countLostEvent(timeRange_2, nbLostEvents_2, false);

        final TmfTimeRange timeRange_1 = new TmfTimeRange(
                new TmfTimestamp(11L, ITmfTimestamp.NANOSECOND_SCALE),
                new TmfTimestamp(26L, ITmfTimestamp.NANOSECOND_SCALE));
        model.countLostEvent(timeRange_1, nbLostEvents_1, false);

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight, 1);

        assertArrayEquals(expectedLostEventsResult, result.fLostEventsData );

        testModelConsistency(model, nbBuckets, nbCombinedEvents, 4, 0, 0, nbEvents - 1, 4 * nbBuckets);
        assertEquals(10, result.fMaxCombinedValue);
    }

    /**
     * Test method for {@link HistogramDataModel#scaleTo(int,int,int)}.
     */
    @Test
    public void testLostEventsScaleTo_3() {
        final int nbBuckets = 10;
        final int maxHeight = 10;
        final int nbEvents = 3 * nbBuckets;
        final int nbLostEvents_0 = 23;
        final int nbCombinedEvents = nbEvents + 1;
        final int[] expectedLostEventsResult = new int[] { 0, 0, 5, 5, 5, 5, 3, 0, 0, 0 };

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        countEventsInModel(nbEvents, model);

        final TmfTimeRange timeRange_0 = new TmfTimeRange(
                new TmfTimestamp(11L, ITmfTimestamp.NANOSECOND_SCALE),
                new TmfTimestamp(26L, ITmfTimestamp.NANOSECOND_SCALE));
        model.countLostEvent(timeRange_0, nbLostEvents_0, false);

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight, 1);

        assertArrayEquals(expectedLostEventsResult, result.fLostEventsData );

        testModelConsistency(model, nbBuckets, nbCombinedEvents, 4, 0, 0, nbEvents - 1, 4 * nbBuckets);
        assertEquals(9, result.fMaxCombinedValue);
    }

    /*
     * helpers
     */

    private static void countEventsInModel(final int nbEvents, HistogramDataModel model) {
        countEventsInModel(nbEvents, model, 0);
    }

    private static void countEventsInModel(final int nbEvents, HistogramDataModel model, int offset) {
        countEventsInModel(nbEvents, model, offset, 0);
    }

    private static void countEventsInModel(final int nbEvents, HistogramDataModel model, int offset, int startTime) {
        for (int i = startTime; i < nbEvents + startTime; i++) {
            model.countEvent(i + offset, i);
        }
    }

    private static void testModelConsistency(HistogramDataModel model, int numberOfBuckets,int nbEvents, int bucketduration,int firstBucketTime, int startTime, int endTime, int timeLimit) {
        assertEquals(numberOfBuckets, model.getNbBuckets());
        assertEquals(nbEvents, model.getNbEvents());
        assertEquals(bucketduration, model.getBucketDuration());
        assertEquals(firstBucketTime, model.getFirstBucketTime());
        assertEquals(startTime, model.getStartTime());
        assertEquals(endTime, model.getEndTime());
        assertEquals(timeLimit, model.getTimeLimit());
    }

    private static void assertArrayEqualsInt(final int val , int[] result) {
        assertArrayEqualsInt(val, result, 0);
    }

    private static void assertArrayEqualsInt(final int val , int[] result, int startVal ) {
        for (int i = startVal; i < result.length; i++) {
            assertEquals(val, result[i]);
        }
    }

}
