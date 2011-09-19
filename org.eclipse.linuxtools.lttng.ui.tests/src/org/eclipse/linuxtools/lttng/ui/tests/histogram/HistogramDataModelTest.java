/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.tests.histogram;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.linuxtools.lttng.ui.views.histogram.HistogramDataModel;
import org.eclipse.linuxtools.lttng.ui.views.histogram.HistogramScaledData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * <b><u>HistogramDataModelTest</u></b>
 * <p>
 * Unit tests for the HistogramDataModel class.
 */
public class HistogramDataModelTest {

    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    /**
     * Test method for
     * {@link org.eclipse.linuxtools.tmf.HistogramDataModel.views.histogram.TmfHistogramDataModel#HistogramDataModel()}
     * .
     */
    @Test
    public void testHistogramDataModel() {
        HistogramDataModel model = new HistogramDataModel();
        assertTrue(model.getNbBuckets() == HistogramDataModel.DEFAULT_NUMBER_OF_BUCKETS);
        assertTrue(model.getNbEvents() == 0);
        assertTrue(model.getBucketDuration() == 1);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == 0);
        assertTrue(model.getTimeLimit() == HistogramDataModel.DEFAULT_NUMBER_OF_BUCKETS);
    }

    /**
     * Test method for
     * {@link org.eclipse.linuxtools.tmf.HistogramDataModel.views.histogram.TmfHistogramDataModel#HistogramDataModel(int)}
     * .
     */
    @Test
    public void testHistogramDataModelInt() {
        final int nbBuckets = 5 * 1000;
        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        assertTrue(model.getNbEvents() == 0);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 1);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == 0);
        assertTrue(model.getTimeLimit() == nbBuckets);
    }

    /**
     * Test methods for
     * {@link org.eclipse.linuxtools.tmf.HistogramDataModel.views.histogram.TmfHistogramDataModel#countEvent(long)}
     * .
     */
    @Test
    public void testClear() {
        final int nbBuckets = 100;
        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        model.countEvent(-1);

        assertTrue(model.getNbEvents() == 0);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 1);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == 0);
        assertTrue(model.getTimeLimit() == nbBuckets);
    }

    /**
     * Test methods for
     * {@link org.eclipse.linuxtools.tmf.HistogramDataModel.views.histogram.TmfHistogramDataModel#countEvent(long)}
     * .
     */
    @Test
    public void testCountEvent_0() {
        final int nbBuckets = 100;
        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        model.countEvent(-1);

        assertTrue(model.getNbEvents() == 0);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 1);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == 0);
        assertTrue(model.getTimeLimit() == nbBuckets);
    }

    @Test
    public void testCountEvent_1() {
        final int nbBuckets = 100;
        final int maxHeight = 10;

        HistogramDataModel model = new HistogramDataModel(nbBuckets);

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight);
        for (int i = 0; i < result.fData.length; i++) {
            assertTrue(result.fData[i] == 0);
        }

        assertTrue(model.getNbEvents() == 0);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 1);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == 0);
        assertTrue(model.getTimeLimit() == nbBuckets);
    }

    @Test
    public void testCountEvent_2() {
        final int nbBuckets = 100;
        final int maxHeight = 10;

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        model.countEvent(1);

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight);
        assertTrue(result.fData[0] == 1);
        for (int i = 1; i < result.fData.length; i++) {
            assertTrue(result.fData[i] == 0);
        }

        assertTrue(model.getNbEvents() == 1);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 1);
        assertTrue(model.getStartTime() == 1);
        assertTrue(model.getEndTime() == 1);
        assertTrue(model.getTimeLimit() == nbBuckets + 1);
    }

    @Test
    public void testCountEvent_3() {
        final int nbBuckets = 100;
        final int maxHeight = 10;

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        for (int i = 0; i < nbBuckets; i++) {
            model.countEvent(i);
        }

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight);
        for (int i = 0; i < result.fData.length; i++) {
            assertTrue(result.fData[i] == 1);
        }

        assertTrue(model.getNbEvents() == nbBuckets);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 1);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == nbBuckets - 1);
        assertTrue(model.getTimeLimit() == nbBuckets);
    }

    @Test
    public void testCountEvent_4() {
        final int nbBuckets = 100;
        final int maxHeight = 10;

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        for (int i = 0; i < nbBuckets; i++) {
            model.countEvent(i);
            model.countEvent(i);
        }

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight);
        for (int i = 0; i < result.fData.length; i++) {
            assertTrue(result.fData[i] == 2);
        }

        assertTrue(model.getNbEvents() == 2 * nbBuckets);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 1);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == nbBuckets - 1);
        assertTrue(model.getTimeLimit() == nbBuckets);
    }

    @Test
    public void testCountEvent_5() {
        final int nbBuckets = 100;
        final int startTime = 25;
        final int maxHeight = 10;

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        for (int i = startTime; i < startTime + nbBuckets; i++) {
            model.countEvent(i);
        }

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight);
        for (int i = 0; i < result.fData.length; i++) {
            assertTrue(result.fData[i] == 1);
        }

        assertTrue(model.getNbEvents() == nbBuckets);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 1);
        assertTrue(model.getStartTime() == startTime);
        assertTrue(model.getEndTime() == startTime + nbBuckets - 1);
        assertTrue(model.getTimeLimit() == startTime + nbBuckets);
    }

    /**
     * Test method for
     * {@link org.eclipse.linuxtools.tmf.HistogramDataModel.views.histogram.TmfHistogramDataModel#scaleTo(int,int)}
     * .
     */
    @Test
    public void testScaleTo_0() {
        HistogramDataModel model = new HistogramDataModel(10);
        try {
            model.scaleTo(10, 0);
        }
        catch (AssertionError e1) {
            try {
                model.scaleTo(0, 10);
            }
            catch (AssertionError e2) {
                try {
                    model.scaleTo(0, 0);
                }
                catch (AssertionError e3) {
                    return;
                }
            }
        }

        fail("Uncaught assertion error");
    }

    @Test
    public void testScaleTo_1() {
        final int nbBuckets = 10;
        final int maxHeight = 10;
        final int nbEvents = nbBuckets / 2;
        final int[] expectedResult = new int[] { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0 };

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        for (int i = 0; i < nbEvents; i++) {
            model.countEvent(i);
        }

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight);
        for (int i = 0; i < result.fData.length; i++) {
            assertTrue(result.fData[i] == expectedResult[i]);
        }

        assertTrue(model.getNbEvents() == nbEvents);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 1);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == nbEvents - 1);
        assertTrue(model.getTimeLimit() == nbBuckets);
    }

    @Test
    public void testScaleTo_2() {
        final int nbBuckets = 10;
        final int maxHeight = 10;
        final int nbEvents = nbBuckets;
        final int[] expectedResult = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        for (int i = 0; i < nbEvents; i++) {
            model.countEvent(i);
        }

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight);
        for (int i = 0; i < result.fData.length; i++) {
            assertTrue(result.fData[i] == expectedResult[i]);
        }

        assertTrue(model.getNbEvents() == nbEvents);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 1);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == nbEvents - 1);
        assertTrue(model.getTimeLimit() == nbBuckets);
    }

    @Test
    public void testScaleTo_3() {
        final int nbBuckets = 10;
        final int maxHeight = 10;
        final int nbEvents = 2 * nbBuckets;
        final int[] expectedResult = new int[] { 2, 2, 2, 2, 2, 2, 2, 2, 2, 2 };

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        for (int i = 0; i < nbEvents; i++) {
            model.countEvent(i);
        }

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight);
        for (int i = 0; i < result.fData.length; i++) {
            assertTrue(result.fData[i] == expectedResult[i]);
        }

        assertTrue(model.getNbEvents() == nbEvents);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 2);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == nbEvents - 1);
        assertTrue(model.getTimeLimit() == 2 * nbBuckets);
    }

    @Test
    public void testScaleTo_4() {
        final int nbBuckets = 10;
        final int maxHeight = 10;
        final int nbEvents = 3 * nbBuckets;
        final int[] expectedResult = new int[] { 4, 4, 4, 4, 4, 4, 4, 2, 0, 0 };

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        for (int i = 0; i < nbEvents; i++) {
            model.countEvent(i);
        }

        HistogramScaledData result = model.scaleTo(nbBuckets, maxHeight);
        for (int i = 0; i < result.fData.length; i++) {
            assertTrue(result.fData[i] == expectedResult[i]);
        }

        assertTrue(model.getNbEvents() == nbEvents);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 4);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == nbEvents - 1);
        assertTrue(model.getTimeLimit() == 4 * nbBuckets);
    }

    @Test
    public void testScaleTo_5() {
        final int nbBuckets = 100;
        final int maxHeight = 20;
        final int nbEvents = 2 * nbBuckets;
        final int[] expectedResult = new int[] { 20, 20, 20, 20, 20, 20, 20, 20, 20, 20 };

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        for (int i = 0; i < nbEvents; i++) {
            model.countEvent(i);
        }

        HistogramScaledData result = model.scaleTo(10, maxHeight);
        for (int i = 0; i < result.fData.length; i++) {
            assertTrue(result.fData[i] == expectedResult[i]);
        }

        assertTrue(model.getNbEvents() == nbEvents);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 2);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == nbEvents - 1);
        assertTrue(model.getTimeLimit() == 2 * nbBuckets);
    }

    @Test
    public void testScaleTo_6() {
        final int nbBuckets = 100;
        final int maxHeight = 24;
        final int nbEvents = 2 * nbBuckets + 1;
        final int[] expectedResult = new int[] { 24, 24, 24, 24, 24, 24, 24, 24, 9, 0 };

        HistogramDataModel model = new HistogramDataModel(nbBuckets);
        for (int i = 0; i < nbEvents; i++) {
            model.countEvent(i);
        }

        HistogramScaledData result = model.scaleTo(10, maxHeight);
        for (int i = 0; i < result.fData.length; i++) {
            assertTrue(result.fData[i] == expectedResult[i]);
        }

        assertTrue(model.getNbEvents() == nbEvents);
        assertTrue(model.getNbBuckets() == nbBuckets);
        assertTrue(model.getBucketDuration() == 4);
        assertTrue(model.getStartTime() == 0);
        assertTrue(model.getEndTime() == nbEvents - 1);
        assertTrue(model.getTimeLimit() == 4 * nbBuckets);
    }

}
