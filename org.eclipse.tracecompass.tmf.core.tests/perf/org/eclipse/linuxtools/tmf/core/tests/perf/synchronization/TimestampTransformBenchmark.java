/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francis Giraldeau - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.perf.synchronization;

import org.eclipse.linuxtools.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.linuxtools.tmf.core.synchronization.TimestampTransformFactory;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.junit.Test;

/**
 * Test the performance of linear transforms classes
 *
 * @author Francis Giraldeau
 */
public class TimestampTransformBenchmark {

    private static final String TEST_ID = "org.eclipse.linuxtools#Trace synchronization#";
    private static final String TEST_SUMMARY = "Timestamp Transform: ";

    /** Number of transformations done for each transform: 50 millions */
    private static final long NB_TRANSFORMATIONS = 50000000L;

    /**
     * Test the timestamp transform performances
     */
    @Test
    public void testTimestampTransformPerformance() {
        ITmfTimestampTransform transform = TimestampTransformFactory.getDefaultTransform();
        doTimestampTransformRun("Identity transform", transform, 10);

        transform = TimestampTransformFactory.createWithOffset(123456789);
        doTimestampTransformRun("Transform with offset", transform, 10);

        transform = TimestampTransformFactory.createLinear(Math.PI, 1234);
        doTimestampTransformRun("Linear transform", transform, 5);

        transform = TimestampTransformFactory.createLinear(10000.1234545565635, -4312278758437L);
        doTimestampTransformRun("Linear transform with larger slope and negative offset", transform, 5);
    }

    private static void doTimestampTransformRun(String testName, ITmfTimestampTransform xform, long loopCount) {
        Performance perf = Performance.getDefault();
        PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + testName);
        perf.tagAsSummary(pm, TEST_SUMMARY + testName, Dimension.CPU_TIME);

        for (int x = 0; x < loopCount; x++) {
            /**
             * We use timestamps with values in the order of 10^18, which about
             * corresponds to timestamps in nanoseconds since epoch
             */
            long time = (long) Math.pow(10, 18);
            pm.start();
            /**
             * Apply the timestamp transform NB_TRANSFORMATIONS times, with
             * timestamps incremented by 200 each time
             */
            for (long i = 0; i < NB_TRANSFORMATIONS; i++) {
                xform.transform(time);
                time += 200;
            }
            pm.stop();
        }
        pm.commit();
    }

}
