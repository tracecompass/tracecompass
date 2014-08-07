/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.synchronization;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.eclipse.linuxtools.internal.tmf.core.synchronization.TmfConstantTransform;
import org.eclipse.linuxtools.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.linuxtools.tmf.core.synchronization.TimestampTransformFactory;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.junit.Test;

/**
 * Timestamp transform tests
 *
 * @author Matthew Khouzam
 *
 */
public class TsTransformFactoryTest {
    private final ITmfTimestamp t0 = new TmfTimestamp(0);
    private final ITmfTimestamp t100 = new TmfTimestamp(100);
    private final ITmfTimestamp t1e2 = new TmfTimestamp(1, 2);
    private final ITmfTimestamp t1e3 = new TmfTimestamp(1, 3);
    private final ITmfTimestamp tn0 = new TmfNanoTimestamp(0);
    private final ITmfTimestamp tn100 = new TmfNanoTimestamp(100);
    private final ITmfTimestamp tn1 = new TmfNanoTimestamp(1);
    private final ITmfTimestampTransform identity1 = TimestampTransformFactory.create(1.0, new TmfNanoTimestamp(0));
    private final ITmfTimestampTransform offset1 = TimestampTransformFactory.create(100);
    private final ITmfTimestampTransform offset2 = TimestampTransformFactory.create(BigDecimal.ONE, new BigDecimal(100));
    private final ITmfTimestampTransform offset3 = TimestampTransformFactory.create(1.0, 100);
    private final ITmfTimestampTransform offset4 = TimestampTransformFactory.create(1.0, new TmfNanoTimestamp(100));

    /**
     * Test with identity
     */
    @Test
    public void transformIdenity() {
        final ITmfTimestampTransform identity = TimestampTransformFactory.create(0);
        final ITmfTimestampTransform innefficientIdentity = new TmfConstantTransform();
        final ITmfTimestampTransform compositeInnefficientIdentity = identity.composeWith(innefficientIdentity);
        final ITmfTimestampTransform compositeInnefficientIdentity2 = innefficientIdentity.composeWith(innefficientIdentity);
        final ITmfTimestampTransform compositeInnefficientIdentity3 = innefficientIdentity.composeWith(identity);
        assertEquals(t0, identity.transform(t0));
        assertEquals(tn0, identity.transform(tn0));
        assertEquals(t100, identity.transform(t100));
        assertEquals(t1e2, identity.transform(t100));
        assertEquals(t1e2, identity.transform(t1e2));
        assertEquals(t1e3, identity.transform(t1e3));
        assertEquals(tn100, identity.transform(tn100));
        assertEquals(t0, innefficientIdentity.transform(t0)); // bad practice
        assertEquals(t0, compositeInnefficientIdentity.transform(t0)); // bad
                                                                       // practice
        assertEquals(t0, compositeInnefficientIdentity2.transform(t0)); // bad
                                                                        // practice
        assertEquals(t0, compositeInnefficientIdentity3.transform(t0)); // bad
                                                                        // practice
    }

    /**
     * Test with an offset of 100
     */
    @Test
    public void transformOffset() {
        final ITmfTimestampTransform offset = offset1;
        final ITmfTimestampTransform compositeTransform = offset.composeWith(TimestampTransformFactory.create(new TmfNanoTimestamp(-100)));
        assertEquals(tn100, offset.transform(t0));
        assertEquals(tn100, offset.transform(tn0));
        assertEquals(tn0, compositeTransform.transform(tn0));
        assertEquals(t0, compositeTransform.transform(t0));
        assertEquals(200, offset1.transform(100));
        assertEquals(200, offset2.transform(100));
        assertEquals(200, offset3.transform(100));
        assertEquals(200, offset4.transform(100));
    }

    /**
     * Test with a slope
     */
    @Test
    public void transformSlope() {
        final ITmfTimestampTransform slope = TimestampTransformFactory.create(10, 0);
        final ITmfTimestampTransform slope1 = TimestampTransformFactory.create(10.0, new TmfNanoTimestamp(0));
        assertEquals(t1e3, slope.transform(t1e2));
        assertEquals(tn100, slope.transform(new TmfNanoTimestamp(10)));
        assertEquals(tn100, slope.transform(slope.transform(tn1)));
        assertEquals(tn100, slope.composeWith(slope).transform(tn1));
        assertEquals(tn100, slope1.transform(new TmfNanoTimestamp(10)));
    }

    /**
     * Test toStrings
     */
    @Test
    public void testToString() {
        final String expectedLinear = "TmfTimestampLinear [ alpha = 314.0, beta = 0.0 ]";
        final String expectedLinearBigDec = "TmfTimestampLinear [ alpha = 314, beta = 0 ]";
        final String expectedOffset = "TmfConstantTransform [ offset = 314 ]";
        final String expectedIdentity = "TmfTimestampTransform [ IDENTITY ]";
        final String expectedOffset100 = "TmfConstantTransform [ offset = 100 ]";
        assertEquals(expectedLinear, TimestampTransformFactory.create(314, 0).toString());
        assertEquals(expectedLinearBigDec, TimestampTransformFactory.create(BigDecimal.valueOf(314), BigDecimal.ZERO).toString());
        assertEquals(expectedOffset, TimestampTransformFactory.create(1, 314).toString());
        assertEquals(expectedOffset, TimestampTransformFactory.create(314).toString());
        assertEquals(expectedOffset, TimestampTransformFactory.create(14).composeWith(TimestampTransformFactory.create(300)).toString());
        assertEquals(expectedIdentity, TimestampTransformFactory.create(314).composeWith(TimestampTransformFactory.create(-314)).toString());
        assertEquals(expectedIdentity, TimestampTransformFactory.create(0).toString());
        assertEquals(expectedIdentity, identity1.toString());
        assertEquals(expectedOffset100, offset1.toString());
        assertEquals(expectedOffset100, offset2.toString());
        assertEquals(expectedOffset100, offset3.toString());
        assertEquals(expectedOffset100, offset4.toString());
    }
}
