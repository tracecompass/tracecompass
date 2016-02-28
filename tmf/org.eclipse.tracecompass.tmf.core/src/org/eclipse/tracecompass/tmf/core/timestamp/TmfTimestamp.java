/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation, refactoring and updates
 *   Thomas Gatterweh    - Updated scaling / synchronization
 *   Geneviève Bastien - Added copy constructor with new value
 *   Alexandre Montplaisir - Removed concept of precision
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.timestamp;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNull;

/**
 * A generic timestamp implementation. The timestamp is represented by the tuple
 * { value, scale, precision }.
 *
 * @author Francois Chouinard
 */
public abstract class TmfTimestamp implements ITmfTimestamp {
    /**
     * Create a timestamp.
     *
     * @param value
     *            the value in nanoseconds
     * @return the timestamp
     * @since 2.0
     */
    public static @NonNull ITmfTimestamp fromNanos(long value) {
        return new TmfNanoTimestamp(value);
    }

    /**
     * Create a timestamp.
     *
     * @param value
     *            the value in microseconds
     * @return the timestamp
     * @since 2.0
     */
    public static @NonNull ITmfTimestamp fromMicros(long value) {
        return create(value, ITmfTimestamp.MICROSECOND_SCALE);
    }

    /**
     * Create a timestamp.
     *
     * @param value
     *            the value in milliseconds
     * @return the timestamp
     * @since 2.0
     */
    public static @NonNull ITmfTimestamp fromMillis(long value) {
        return create(value, ITmfTimestamp.MILLISECOND_SCALE);
    }

    /**
     * Create a timestamp.
     *
     * @param value
     *            the value in seconds
     * @return the timestamp
     * @since 2.0
     */
    public static @NonNull ITmfTimestamp fromSeconds(long value) {
        return new TmfSecondTimestamp(value);
    }

    /**
     * Create a timestamp.
     *
     * @param bufferIn
     *            the byte buffer to read the timestamp from.
     * @return the timestamp
     * @since 2.0
     */
    public static @NonNull ITmfTimestamp create(ByteBuffer bufferIn) {
        return create(bufferIn.getLong(), bufferIn.getInt());
    }

    /**
     * Create a timestamp.
     *
     * @param value
     *            the value in time, the unit is specified by the scale
     * @param scale
     *            the scale of the timestamp with respect to seconds, so a
     *            nanosecond would be -9 (10e-9) and a megasecond would be 6
     *            (10e6)
     * @return the timestamp
     * @since 2.0
     */
    public static @NonNull ITmfTimestamp create(long value, int scale) {
        if (scale == ITmfTimestamp.NANOSECOND_SCALE) {
            return fromNanos(value);
        }
        if (scale == ITmfTimestamp.SECOND_SCALE) {
            return fromSeconds(value);
        }
        return createOther(value, scale);
    }

    /**
     * Write the time stamp to the ByteBuffer so that it can be saved to disk.
     *
     * @param bufferOut
     *            the buffer to write to
     * @param ts
     *            the timestamp to write
     * @since 2.0
     */
    public static void serialize(ByteBuffer bufferOut, ITmfTimestamp ts) {
        bufferOut.putLong(ts.getValue());
        bufferOut.putInt(ts.getScale());
    }

    private static @NonNull ITmfTimestamp createOther(long value, int scale) {
        return new TmfRealTimestamp(value, scale);
    }

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The beginning of time
     */
    public static final @NonNull ITmfTimestamp BIG_BANG = new TmfRealTimestamp(Long.MIN_VALUE, Integer.MAX_VALUE);

    /**
     * The end of time
     */
    public static final @NonNull ITmfTimestamp BIG_CRUNCH = new TmfRealTimestamp(Long.MAX_VALUE, Integer.MAX_VALUE);

    /**
     * Zero
     */
    public static final @NonNull ITmfTimestamp ZERO = new TmfRealTimestamp(0, 0);

    // ------------------------------------------------------------------------
    // ITmfTimestamp
    // ------------------------------------------------------------------------

    /**
     * Scaling factors to help scale
     *
     * @since 2.0
     */
    protected static final long SCALING_FACTORS[] = new long[] {
            1L,
            10L,
            100L,
            1000L,
            10000L,
            100000L,
            1000000L,
            10000000L,
            100000000L,
            1000000000L,
            10000000000L,
            100000000000L,
            1000000000000L,
            10000000000000L,
            100000000000000L,
            1000000000000000L,
            10000000000000000L,
            100000000000000000L,
            1000000000000000000L,
    };

    @Override
    public ITmfTimestamp normalize(final long offset, final int scale) {

        long value = getValue();

        // Handle the trivial case
        if (getScale() == scale && offset == 0) {
            return this;
        }

        // In case of big bang and big crunch just return this (no need to
        // normalize)
        if (this.equals(BIG_BANG) || this.equals(BIG_CRUNCH)) {
            return this;
        }

        if (value == 0) {
            return create(offset, scale);
        }

        // First, scale the timestamp
        if (getScale() != scale) {
            final int scaleDiff = Math.abs(getScale() - scale);
            if (scaleDiff >= SCALING_FACTORS.length) {
                if (getScale() < scale) {
                    value = 0;
                } else {
                    value = value > 0 ? Long.MAX_VALUE : Long.MIN_VALUE;
                }
            } else {
                final long scalingFactor = SCALING_FACTORS[scaleDiff];
                if (getScale() < scale) {
                    value /= scalingFactor;
                } else {
                    value = saturatedMult(scalingFactor, value);
                }
            }
        }

        value = saturatedAdd(value, offset);

        return create(value, scale);
    }

    @Override
    public ITmfTimestamp getDelta(final ITmfTimestamp ts) {
        final int scale = getScale();
        final ITmfTimestamp nts = ts.normalize(0, scale);
        final long value = getValue() - nts.getValue();
        return new TmfTimestampDelta(value, scale);
    }

    @Override
    public boolean intersects(TmfTimeRange range) {
        if (this.compareTo(range.getStartTime()) >= 0 &&
                this.compareTo(range.getEndTime()) <= 0) {
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------------------
    // Comparable
    // ------------------------------------------------------------------------

    @Override
    public int compareTo(final ITmfTimestamp ts) {
        long value = getValue();
        int scale = getScale();
        // Check the corner cases (we can't use equals() because it uses
        // compareTo()...)
        if (ts == null) {
            return 1;
        }
        if (this == ts || (value == ts.getValue() && scale == ts.getScale())) {
            return 0;
        }
        if ((value == BIG_BANG.getValue() && scale == BIG_BANG.getScale()) || (ts.getValue() == BIG_CRUNCH.getValue() && ts.getScale() == BIG_CRUNCH.getScale())) {
            return -1;
        }
        if ((value == BIG_CRUNCH.getValue() && scale == BIG_CRUNCH.getScale()) || (ts.getValue() == BIG_BANG.getValue() && ts.getScale() == BIG_BANG.getScale())) {
            return 1;
        }
        final ITmfTimestamp nts = ts.normalize(0, scale);
        if ((nts.getValue() == 0 && ts.getValue() != 0) || (ts.getValue() != Long.MAX_VALUE && nts.getValue() == Long.MAX_VALUE) || (ts.getValue() != Long.MIN_VALUE && nts.getValue() == Long.MIN_VALUE)) {
            // Scaling error. We can figure it out nonetheless.

            // First, look at the sign of the mantissa
            final long otherValue = ts.getValue();
            if (value == 0 && otherValue == 0) {
                return 0;
            }
            if (value < 0 && otherValue >= 0) {
                return -1;
            }
            if (value >= 0 && otherValue < 0) {
                return 1;
            }

            // Otherwise, just compare the scales
            final int otherScale = ts.getScale();
            return (scale > otherScale) ? (otherValue >= 0) ? 1 : -1 : (otherValue >= 0) ? -1 : 1;
        }
        final long delta = value - nts.getValue();
        return Long.compare(delta, 0);
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
    private static long saturatedMult(long left, long right) {
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
    protected static final long saturatedAdd(final long left, final long right) {
        long retVal = left + right;
        if (sameSign(left, right) && !sameSign(left, retVal)) {
            if (retVal > 0) {
                return Long.MIN_VALUE;
            }
            return Long.MAX_VALUE;
        }
        return retVal;
    }

    private static boolean sameSign(final long left, final long right) {
        return (left ^ right) >= 0;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        final long value = getValue();
        result = prime * result + (int) (value ^ (value >>> 32));
        result = prime * result + getScale();
        return result;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof ITmfTimestamp)) {
            return false;
        }
        /* We allow comparing with other types of *I*TmfTimestamp though */
        final ITmfTimestamp ts = (ITmfTimestamp) other;
        return (compareTo(ts) == 0);
    }

    @Override
    public String toString() {
        return toString(TmfTimestampFormat.getDefaulTimeFormat());
    }

    @Override
    public String toString(final TmfTimestampFormat format) {
        try {
            return format.format(toNanos());
        } catch (ArithmeticException e) {
            return format.format(0);
        }
    }
}
