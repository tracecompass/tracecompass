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
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.histogram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
        assertTrue(model.getNbBuckets() == HistogramDataModel.DEFAULT_NUMBER_OF_BUCKETS);
        assertTrue(model.getNbEvents() == 0);
        assertTrue(model.getBucketDuration() == 1);
        assertTrue(model.getFirstBucketTime() == 0);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == 0);
        assertTrue(model.getTimeLimit() == HistogramDataModel.DEFAULT_NUMBER_OF_BUCKETS);
    }

    /**
     * Test method for {@link HistogramDataModel#HistogramDataModel(int)}.
     */
    @Test
    public void testHistogramDataModelInt() {
        final int nbBuckets = 5 * 1000;
        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        assertTrue(model.getNbEvents() == 0);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 1);
        assertTrue(model.getFirstBucketTime() == 0);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == 0);
        assertTrue(model.getTimeLimit() == nbBuckets);
    }

    /**
     * Test methods for {@link HistogramDataModel#countEvent(long,long)}.
     */
    @Test
    public void testClear() {
        final int nbBuckets = 100;
        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        model.countEvent(0, -1);

        assertTrue(model.getNbEvents() == 0);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 1);
        assertTrue(model.getFirstBucketTime() == 0);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == 0);
        assertTrue(model.getTimeLimit() == nbBuckets);
    }

    /**
     * Test methods for {@link HistogramDataModel#countEvent(long,long)}.
     */
    @Test
    public void testCountEvent_0() {
        final int nbBuckets = 100;
        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        model.countEvent(0, -1);

        assertTrue(model.getNbEvents() == 0);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 1);
        assertTrue(model.getFirstBucketTime() == 0);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == 0);
        assertTrue(model.getTimeLimit() == nbBuckets);
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
            assertTrue(result.fData[i] == 0);
        }

        assertTrue(model.getNbEvents() == 0);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 1);
        assertTrue(model.getFirstBucketTime() == 0);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == 0);
        assertTrue(model.getTimeLimit() == nbBuckets);
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
        assertTrue(result.fData[0] == 1);
        for (int i = 1; i < result.fData.length; i++) {
            assertTrue(result.fData[i] == 0);
        }

        assertTrue(model.getNbEvents() == 1);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 1);
        assertTrue(model.getFirstBucketTime() == 1);
        assertTrue(model.getStartTime() == 1);
        assertTrue(model.getEndTime() == 1);
        assertTrue(model.getTimeLimit() == nbBuckets + 1);
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
        for (int i = 0; i < nbBuckets; i++) {
            model.countEvent(i, i);
        }

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight, 1);
        for (int i = 0; i < result.fData.length; i++) {
            assertTrue(result.fData[i] == 1);
        }

        assertTrue(model.getNbEvents() == nbBuckets);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 1);
        assertTrue(model.getFirstBucketTime() == 0);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == nbBuckets - 1);
        assertTrue(model.getTimeLimit() == nbBuckets);
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
        for (int i = 0; i < nbBuckets; i++) {
            model.countEvent(i, i);
            model.countEvent(i+1, i);
        }

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight, 1);
        for (int i = 0; i < result.fData.length; i++) {
            assertTrue(result.fData[i] == 2);
        }

        assertTrue(model.getNbEvents() == 2 * nbBuckets);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 1);
        assertTrue(model.getFirstBucketTime() == 0);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == nbBuckets - 1);
        assertTrue(model.getTimeLimit() == nbBuckets);
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
        for (int i = 0; i < result.fData.length; i++) {
            assertTrue(result.fData[i] == 1);
        }

        assertTrue(model.getNbEvents() == nbBuckets);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 1);
        assertTrue(model.getFirstBucketTime() == startTime);
        assertTrue(model.getStartTime() == startTime);
        assertTrue(model.getEndTime() == startTime + nbBuckets - 1);
        assertTrue(model.getTimeLimit() == startTime + nbBuckets);
    }

    /**
     * Test method for {@link HistogramDataModel#scaleTo(int,int,int)}.
     */
    @Test
    public void testScaleTo_0() {
        HistogramDataModel model = new HistogramDataModel(10);
        try {
            model.scaleTo(10, 0, 1);
        }
        catch (AssertionError e1) {
            try {
                model.scaleTo(0, 10, 1);
            }
            catch (AssertionError e2) {
                try {
                    model.scaleTo(0, 0, 1);
                }
                catch (AssertionError e3) {
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
        for (int i = 0; i < nbEvents; i++) {
            model.countEvent(i, i);
        }

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight, 1);
        for (int i = 0; i < result.fData.length; i++) {
            assertTrue(result.fData[i] == expectedResult[i]);
        }

        assertTrue(model.getNbEvents() == nbEvents);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 1);
        assertTrue(model.getFirstBucketTime() == 0);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == nbEvents - 1);
        assertTrue(model.getTimeLimit() == nbBuckets);
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
        for (int i = 0; i < nbEvents; i++) {
            model.countEvent(i, i);
        }

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight, 1);
        for (int i = 0; i < result.fData.length; i++) {
            assertTrue(result.fData[i] == expectedResult[i]);
        }

        assertTrue(model.getNbEvents() == nbEvents);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 1);
        assertTrue(model.getFirstBucketTime() == 0);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == nbEvents - 1);
        assertTrue(model.getTimeLimit() == nbBuckets);
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
        for (int i = 0; i < nbEvents; i++) {
            model.countEvent(i, i);
        }

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight, 1);
        for (int i = 0; i < result.fData.length; i++) {
            assertTrue(result.fData[i] == expectedResult[i]);
        }

        assertTrue(model.getNbEvents() == nbEvents);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 2);
        assertTrue(model.getFirstBucketTime() == 0);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == nbEvents - 1);
        assertTrue(model.getTimeLimit() == 2 * nbBuckets);
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
        for (int i = 0; i < nbEvents; i++) {
            model.countEvent(i, i);
        }

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight, 1);
        for (int i = 0; i < result.fData.length; i++) {
            assertTrue(result.fData[i] == expectedResult[i]);
        }

        assertTrue(model.getNbEvents() == nbEvents);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 4);
        assertTrue(model.getFirstBucketTime() == 0);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == nbEvents - 1);
        assertTrue(model.getTimeLimit() == 4 * nbBuckets);
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
        for (int i = 0; i < nbEvents; i++) {
            model.countEvent(i, i);
        }

        HistogramScaledData result = model.scaleTo(10, maxHeight, 1);
        for (int i = 0; i < result.fData.length; i++) {
            assertTrue(result.fData[i] == expectedResult[i]);
        }

        assertTrue(model.getNbEvents() == nbEvents);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 2);
        assertTrue(model.getFirstBucketTime() == 0);
        assertTrue(model.getEndTime() == nbEvents - 1);
        assertTrue(model.getTimeLimit() == 2 * nbBuckets);
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
        for (int i = 0; i < nbEvents; i++) {
            model.countEvent(i, i);
        }

        HistogramScaledData result = model.scaleTo(10, maxHeight, 1);
        for (int i = 0; i < result.fData.length; i++) {
            assertTrue(result.fData[i] == expectedResult[i]);
        }

        assertTrue(model.getNbEvents() == nbEvents);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 4);
        assertTrue(model.getFirstBucketTime() == 0);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == nbEvents - 1);
        assertTrue(model.getTimeLimit() == 4 * nbBuckets);
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
        // buckets (in model) per bar = last bucket id / nbBars + 1 (plus 1 to cover all used buckets)
        // -> buckets per bar = 50 / 2 + 1 = 26
        // -> first entry in expected result is 26 * 4 = 104
        // -> second entry in expected result is 22 * 4 + 9 = 97
        final int[] expectedResult = new int[] { 104, 97 };

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        for (int i = 0; i < nbEvents; i++) {
            model.countEvent(i, i);
        }

        // verify scaled data
        HistogramScaledData result = model.scaleTo(width, maxHeight, barWidth);

        assertEquals(4 * 26, result.fBucketDuration);
        assertEquals(0, result.fCurrentBucket);
        assertEquals(0, result.fFirstBucketTime);
        assertEquals(0, result.fFirstEventTime);
        assertEquals(1, result.fLastBucket);
        assertEquals(104, result.fMaxValue);
        assertEquals((double)maxHeight/104, result.fScalingFactor, DELTA);
        assertEquals(maxHeight, result.fHeight);
        assertEquals(width, result.fWidth);
        assertEquals(barWidth, result.fBarWidth);

        for (int i = 0; i < result.fData.length; i++) {
            assertEquals(expectedResult[i], result.fData[i]);
        }

        // verify model
        assertEquals(nbEvents, model.getNbEvents());
        assertEquals(nbBuckets, model.getNbBuckets());
        assertEquals(4, model.getBucketDuration());
        assertEquals(0, model.getFirstBucketTime());
        assertEquals(0, model.getStartTime());
        assertEquals(nbEvents - 1, model.getEndTime());
        assertEquals(4 * nbBuckets, model.getTimeLimit());
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
        // buckets in (model) per bar = last bucket id / nbBars + 1 (plus 1 to cover all used buckets)
        // -> buckets per bar = 50 / 10 + 1 = 6
        final int[] expectedResult = new int[] { 21, 24, 24, 24, 24, 24, 24, 24, 12, 0 };

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        for (int i = nbEvents - 1; i >= 0; i--) {
            model.countEvent(i, i);
        }

        // verify scaled data
        HistogramScaledData result = model.scaleTo(width, maxHeight, barWidth);

        assertEquals(4 * 6, result.fBucketDuration);
        assertEquals(0, result.fCurrentBucket);
        assertEquals(-3, result.fFirstBucketTime); // negative is correct, can happen when reverse
        assertEquals(0, result.fFirstEventTime);
        assertEquals(9, result.fLastBucket);
        assertEquals(24, result.fMaxValue);
        assertEquals((double)maxHeight/24, result.fScalingFactor, DELTA);
        assertEquals(maxHeight, result.fHeight);
        assertEquals(width, result.fWidth);
        assertEquals(barWidth, result.fBarWidth);

        for (int i = 0; i < result.fData.length; i++) {
            assertEquals(expectedResult[i], result.fData[i]);
        }

        // verify model
        assertEquals(nbEvents, model.getNbEvents());
        assertEquals(nbBuckets, model.getNbBuckets());
        assertEquals(4, model.getBucketDuration());
        assertEquals(-3, model.getFirstBucketTime());
        assertEquals(0, model.getStartTime());
        assertEquals(nbEvents - 1, model.getEndTime());
        assertEquals(-3 + 4 * nbBuckets, model.getTimeLimit());
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
        for (int i = 0; i < nbEvents; i++) {
            model.countEvent(i, i);
        }

        HistogramScaledData result = model.scaleTo(width, maxHeight, barWidth);

        model.clear();

        for (int i = nbEvents -1; i >= 0; i--) {
            model.countEvent(i, i);
        }

        HistogramScaledData revResult = model.scaleTo(width, maxHeight, barWidth);

        assertEquals(nbEvents, model.getNbEvents());
        assertEquals(nbBuckets, model.getNbBuckets());
        assertEquals(2, model.getBucketDuration());
        assertEquals(0, model.getFirstBucketTime());
        assertEquals(0, model.getStartTime());
        assertEquals(nbEvents - 1, model.getEndTime());
        assertEquals(2 * nbBuckets, model.getTimeLimit());

        // For the above number of events, result and revResult are exactly the same.
        assertEquals(result.fBucketDuration, revResult.fBucketDuration);
        assertEquals(result.fCurrentBucket, revResult.fCurrentBucket);
        assertEquals(result.fFirstBucketTime, revResult.fFirstBucketTime);
        assertEquals(result.fMaxValue, revResult.fMaxValue);
        assertEquals(result.fScalingFactor, revResult.fScalingFactor, DELTA);
        assertEquals(result.fLastBucket, revResult.fLastBucket);
        assertEquals(result.getBucketEndTime(0), revResult.getBucketEndTime(0));
        assertEquals(result.getBucketStartTime(0), revResult.getBucketStartTime(0));

        for (int i = 0; i < result.fData.length; i++) {
            assertTrue(result.fData[i] == revResult.fData[i]);
        }
    }

    /**
     * Test method for testing model listener.
     */
    @Test
    public void testModelListener() {
        final int nbBuckets = 2000;
        final int nbEvents = 10 * nbBuckets + 256;
        final int[] count = new int[1];
        count [0] = 0;

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
        for (int i = 0; i < nbEvents; i++) {
            model.countEvent(i+1, i);
        }

        assertTrue(count[0] == 1);

        // Test that the listener interface is called when complete is called.
        model.complete();
        assertTrue(count[0] == 2);

        // Test that clear triggers call of listener interface
        model.clear();
        assertTrue(count[0] == 3);

        // Test remove listener
        count[0] = 0;
        model.removeHistogramListener(listener);

        for (int i = 0; i < nbEvents; i++) {
            model.countEvent(i, i);
        }
        model.complete();
        assertTrue(count[0] == 0);
    }
}
