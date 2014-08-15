/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial implementation and API
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.core.synchronization;

import java.math.BigDecimal;

import org.eclipse.linuxtools.internal.tmf.core.synchronization.TmfConstantTransform;
import org.eclipse.linuxtools.internal.tmf.core.synchronization.TmfTimestampTransform;
import org.eclipse.linuxtools.internal.tmf.core.synchronization.TmfTimestampTransformLinear;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;

/**
 * A factory to generate timestamp tranforms
 *
 * @author Matthew Khouzam
 * @since 3.1
 */
public final class TimestampTransformFactory {

    private TimestampTransformFactory() {
    }

    /**
     * Creates the identity timestamp transform
     *
     * @return The identity timestamp transform
     */
    public static ITmfTimestampTransform getDefaultTransform() {
        return TmfTimestampTransform.IDENTITY;
    }

    /**
     * Create an offsetted transform
     *
     * @param offset
     *            the offset in long format, nanosecond scale
     * @return the offsetted transform
     */
    public static ITmfTimestampTransform createWithOffset(long offset) {
        if (offset == 0) {
            return TmfTimestampTransform.IDENTITY;
        }
        return new TmfConstantTransform(offset);
    }

    /**
     * Create an offsetted transform
     *
     * @param offset
     *            the offset in a timestamp with scale
     * @return the offsetted transform
     */
    public static ITmfTimestampTransform createWithOffset(ITmfTimestamp offset) {
        if (offset.getValue() == 0) {
            return TmfTimestampTransform.IDENTITY;
        }
        return new TmfConstantTransform(offset);
    }

    /**
     * Create a timestamp transform corresponding to a linear equation, with
     * slope and offset. The expected timestamp transform is such that f(t) =
     * m*x + b, where m is the slope and b the offset.
     *
     * @param factor
     *            the slope
     * @param offset
     *            the offset
     * @return the transform
     */
    public static ITmfTimestampTransform createLinear(double factor, ITmfTimestamp offset) {
        if (factor == 1.0) {
            return createWithOffset(offset);
        }
        return new TmfTimestampTransformLinear(factor, offset.normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue());
    }

    /**
     * Create a timestamp transform corresponding to a linear equation, with
     * slope and offset. The expected timestamp transform is such that f(t) =
     * m*x + b, where m is the slope and b the offset.
     *
     * @param factor
     *            the slope
     * @param offset
     *            the offset in nanoseconds
     * @return the transform
     */
    public static ITmfTimestampTransform createLinear(double factor, long offset) {
        if (factor == 1.0) {
            return createWithOffset(offset);
        }
        return new TmfTimestampTransformLinear(factor, offset);
    }

    /**
     * Create a timestamp transform corresponding to a linear equation, with
     * slope and offset expressed in BigDecimal. The expected timestamp
     * transform is such that f(t) = m*x + b, where m is the slope and b the
     * offset.
     *
     * @param factor
     *            the slope
     * @param offset
     *            the offset in nanoseconds
     * @return the transform
     */
    public static ITmfTimestampTransform createLinear(BigDecimal factor, BigDecimal offset) {
        if (factor.equals(BigDecimal.ONE)) {
            return createWithOffset(offset.longValueExact());
        }
        return new TmfTimestampTransformLinear(factor, offset);
    }

}
