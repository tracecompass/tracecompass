/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.math;

/**
 * Saturated arithmetic. These are mathematical helper functions that are used
 * to clamp numbers to maximum and mimumum and avoid overflows.
 *
 * @author Matthew Khouzam
 * @since 2.1
 */
public final class SaturatedArithmetic {

    private SaturatedArithmetic() {
        // do nothing
    }

    /**
     * Saturated multiplication. It will not overflow but instead clamp the
     * result to {@link Long#MAX_VALUE} and {@link Long#MIN_VALUE}.
     *
     * @param left
     *            The left long to multiply
     * @param right
     *            The right long to multiply
     * @return The saturated multiplication result. The mathematical, not Java
     *         version of Min(Max(MIN_VALUE, left*right), MAX_VALUE).
     * @see <a href="http://en.wikipedia.org/wiki/Saturation_arithmetic">
     *      Saturation arithmetic</a>
     */
    public static long multiply(long left, long right) {
        long retVal = left * right;
        if ((left != 0) && ((retVal / left) != right)) {
            return (sameSign(left, right) ? Long.MAX_VALUE : Long.MIN_VALUE);
        }
        return retVal;
    }

    /**
     * Saturated addition. It will not overflow but instead clamp the result to
     * {@link Long#MAX_VALUE} and {@link Long#MIN_VALUE}.
     *
     * @param left
     *            The left long to add
     * @param right
     *            The right long to add
     * @return The saturated addition result. The mathematical, not Java version
     *         of Min(Max(MIN_VALUE, left+right), MAX_VALUE).
     * @see <a href="http://en.wikipedia.org/wiki/Saturation_arithmetic">
     *      Saturation arithmetic</a>
     * @since 2.0
     */
    public static final long add(final long left, final long right) {
        long retVal = left + right;
        if (sameSign(left, right) && !sameSign(left, retVal)) {
            if (retVal > 0 || left == Long.MIN_VALUE) {
                return Long.MIN_VALUE;
            }
            return Long.MAX_VALUE;
        }
        return retVal;
    }

    /**
     * Test if two numbers are the same sign or not
     *
     * @param left
     *            the left long
     * @param right
     *            the right long
     * @return true if both left and right are positive or both negative, false
     *         otherwise
     */
    public static boolean sameSign(final long left, final long right) {
        return (left ^ right) >= 0;
    }
}
