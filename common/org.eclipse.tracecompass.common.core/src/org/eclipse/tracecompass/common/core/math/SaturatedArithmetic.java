/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
     * result to {@link Integer#MAX_VALUE} and {@link Integer#MIN_VALUE}.
     *
     * @param left
     *            The left int to multiply
     * @param right
     *            The right int to multiply
     * @return The saturated multiplication result. The mathematical, not Java
     *         version of Min(Max(MIN_VALUE, left*right), MAX_VALUE).
     * @see <a href="http://en.wikipedia.org/wiki/Saturation_arithmetic">
     *      Saturation arithmetic</a>
     */
    public static int multiply(int left, int right) {
        int retVal = left * right;
        if ((left != 0) && ((retVal / left) != right)) {
            return (sameSign(left, right) ? Integer.MAX_VALUE : Integer.MIN_VALUE);
        }
        return retVal;
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
     * {@link Integer#MAX_VALUE} and {@link Integer#MIN_VALUE}.
     *
     * @param left
     *            The left int to add
     * @param right
     *            The right int to add
     * @return The saturated addition result. The mathematical, not Java version
     *         of Min(Max(MIN_VALUE, left+right), MAX_VALUE).
     * @see <a href="http://en.wikipedia.org/wiki/Saturation_arithmetic">
     *      Saturation arithmetic</a>
     */
    public static final int add(final int left, final int right) {
        int retVal = left + right;
        if (sameSign(left, right) && !sameSign(left, retVal)) {
            if (retVal > 0 || left == Integer.MIN_VALUE) {
                return Integer.MIN_VALUE;
            }
            return Integer.MAX_VALUE;
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
