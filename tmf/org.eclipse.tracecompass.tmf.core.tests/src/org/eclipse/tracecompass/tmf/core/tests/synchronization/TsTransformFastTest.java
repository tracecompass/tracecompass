/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francis Giraldeau - Initial implementation and API
 *   Geneviève Bastien - Fixes and improvements
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.synchronization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import org.eclipse.tracecompass.internal.tmf.core.synchronization.TmfTimestampTransformLinear;
import org.eclipse.tracecompass.internal.tmf.core.synchronization.TmfTimestampTransformLinearFast;
import org.eclipse.tracecompass.tmf.core.synchronization.ITmfTimestampTransform;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests for {@link TmfTimestampTransformLinearFast}
 *
 * @author Geneviève Bastien
 */
@RunWith(Parameterized.class)
public class TsTransformFastTest {

    private static final long ts = 1361657893526374091L;

    private static interface IFastTransformFactory {
        public TmfTimestampTransformLinearFast create(double alpha, double beta);
    }

    private static final IFastTransformFactory fNewObject = (a, b) -> {
        return new TmfTimestampTransformLinearFast(a, b);
    };

    private static final IFastTransformFactory fDeserialized = (a, b) -> {
        TmfTimestampTransformLinearFast tt = new TmfTimestampTransformLinearFast(a, b);
        /* Serialize the object */
        String filePath = null;
        try {
            File temp = File.createTempFile("serialSyncAlgo", ".tmp");
            filePath = temp.getAbsolutePath();
        } catch (IOException e) {
            fail("Could not create temporary file for serialization");
        }
        assertNotNull(filePath);

        try (FileOutputStream fileOut = new FileOutputStream(filePath);
                ObjectOutputStream out = new ObjectOutputStream(fileOut);) {
            out.writeObject(tt);
        } catch (IOException e) {
            fail("Error serializing the synchronization algorithm " + e.getMessage());
        }

        TmfTimestampTransformLinearFast deserialTt = null;
        /* De-Serialize the object */
        try (FileInputStream fileIn = new FileInputStream(filePath);
                ObjectInputStream in = new ObjectInputStream(fileIn);) {
            deserialTt = (TmfTimestampTransformLinearFast) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            fail("Error de-serializing the synchronization algorithm " + e.getMessage());
        }
        return deserialTt;
    };

    private final IFastTransformFactory fTransformFactory;

    /**
     * Constructor
     *
     * @param name
     *            The name of this parameterized test
     * @param factory
     *            Factory to create the timestamp transform
     */
    public TsTransformFastTest(String name, IFastTransformFactory factory) {
        fTransformFactory = factory;
    }

    /**
     * @return the test parameters
     */
    @Parameters(name = "Factory={0}")
    public static Iterable<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
                { "Object", fNewObject },
                { "Deserialized", fDeserialized }
        });
    }

    /**
     * Test whether the fast linear transform always yields the same value for
     * the same timestamp
     */
    @Test
    public void testFLTRepeatability() {
        TmfTimestampTransformLinearFast fast = fTransformFactory.create(Math.PI, 0);
        // Access fDeltaMax to compute the cache range boundaries
        long deltaMax = fast.getDeltaMax();
        // Initialize the transform
        long timestamp = ts - (ts % deltaMax);
        fast.transform(timestamp);
        long tsMiss = timestamp + deltaMax;
        long tsNoMiss = timestamp + deltaMax - 1;

        // Get the transformed value to a timestamp without cache miss
        long tsTNoMiss = fast.transform(tsNoMiss);
        assertEquals(1, fast.getCacheMisses());

        // Cause a cache miss
        fast.transform(tsMiss);
        assertEquals(2, fast.getCacheMisses());

        /*
         * Get the transformed value of the same previous timestamp after the
         * miss
         */
        long tsTAfterMiss = fast.transform(tsNoMiss);
        assertEquals(tsTNoMiss, tsTAfterMiss);
    }

    /**
     * Test that 2 equal fast transform always give the same results for the
     * same values
     */
    @Test
    public void testFLTEquivalence() {
        TmfTimestampTransformLinearFast fast = fTransformFactory.create(Math.PI, 0);
        TmfTimestampTransformLinearFast fast2 = fTransformFactory.create(Math.PI, 0);

        long deltaMax = fast.getDeltaMax();

        long start = (ts - (ts % deltaMax) - 10);
        checkTime(fast, fast2, 20, start, 1);
    }

    /**
     * Test the precision of the fast timestamp transform compared to the
     * original transform.
     */
    @Test
    public void testFastTransformPrecision() {
        TmfTimestampTransformLinear precise = new TmfTimestampTransformLinear(Math.PI, 0);
        TmfTimestampTransformLinearFast fast = fTransformFactory.create(Math.PI, 0);
        int samples = 100;
        long start = (long) Math.pow(10, 18);
        long end = Long.MAX_VALUE;
        int step = (int) ((end - start) / (samples * Math.PI));
        checkTime(precise, fast, samples, start, step);
        assertEquals(samples, fast.getCacheMisses());

        // check that rescale is done only when required
        // assumes tsBitWidth == 30
        // test forward and backward timestamps
        samples = 1000;
        int[] directions = new int[] { 1, -1 };
        for (Integer direction : directions) {
            for (int i = 0; i <= 30; i++) {
                fast.resetScaleStats();
                step = (1 << i) * direction;
                checkTime(precise, fast, samples, start, step);
                assertTrue(String.format("samples: %d scale misses: %d",
                        samples, fast.getCacheMisses()), samples >= fast.getCacheMisses());
            }
        }
    }

    /**
     * Test that fast transform produces the same result for small and large
     * slopes.
     */
    @Test
    public void testFastTransformSlope() {
        int[] dir = new int[] { 1, -1 };
        long start = (1 << 30);
        for (int ex = -9; ex <= 9; ex++) {
            for (int d = 0; d < dir.length; d++) {
                double slope = Math.pow(10.0, ex);
                TmfTimestampTransformLinear precise = new TmfTimestampTransformLinear(slope, 0);
                TmfTimestampTransformLinearFast fast = fTransformFactory.create(slope, 0);
                checkTime(precise, fast, 1000, start, dir[d]);
            }
        }
    }

    /**
     * Test that fast transform produces the same result with a slope and
     * offset, for small and large values
     */
    @Test
    public void testFastTransformSlopeAndOffset() {
        double offset = 54321.0;
        double slope = Math.pow(10.0, 4);
        for (int ex = 0; ex <= Long.SIZE - 1; ex++) {
            long start = 1 << ex;
            TmfTimestampTransformLinear precise = new TmfTimestampTransformLinear(slope, offset);
            TmfTimestampTransformLinearFast fast = fTransformFactory.create(slope, offset);
            checkTime(precise, fast, 5, start, 1);
        }
    }

    /**
     * Check that the proper exception are raised for illegal slopes
     */
    @Test
    public void testFastTransformArguments() {
        double[] slopes = new double[] { -1.0, ((double) Integer.MAX_VALUE) + 1, 1e-10 };
        for (double slope : slopes) {
            Exception exception = null;
            try {
                fTransformFactory.create(slope, 0.0);
            } catch (IllegalArgumentException e) {
                exception = e;
            }
            assertNotNull(exception);
        }
    }

    private static void checkTime(ITmfTimestampTransform precise, ITmfTimestampTransform fast,
            int samples, long start, long step) {
        long prev = 0;
        for (int i = 0; i < samples; i++) {
            long time = start + i * step;
            long exp = precise.transform(time);
            long act = fast.transform(time);
            long err = act - exp;
            // allow only two ns of error
            assertTrue("start: " + start + " [" + err + "]", Math.abs(err) < 3);
            if (i > 0) {
                if (step > 0) {
                    assertTrue("monotonic error" + act + " " + prev, act >= prev);
                } else if (step < 0) {
                    assertTrue("monotonic ", act <= prev);
                }
            }
            prev = act;
        }
    }

}
