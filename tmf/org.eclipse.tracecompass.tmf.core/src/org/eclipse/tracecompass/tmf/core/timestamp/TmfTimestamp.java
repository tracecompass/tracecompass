/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.tracecompass.common.core.math.SaturatedArithmetic;
import org.eclipse.tracecompass.internal.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.tracecompass.internal.tmf.core.timestamp.TmfSecondTimestamp;

/**
 * A generic timestamp implementation. The timestamp is represented by the tuple
 * { value, scale, precision }.
 *
 * @author Francois Chouinard
 */
public abstract class TmfTimestamp implements ITmfTimestamp {

    /**
     * Default implementation of the tmf timestamp. We want this to be hidden.
     *
     * @author Matthew Khouzam
     *
     */
    private static final class Impl extends TmfTimestamp {

        // ------------------------------------------------------------------------
        // Attributes
        // ------------------------------------------------------------------------

        /**
         * The timestamp raw value (mantissa)
         */
        private final long fValue;

        /**
         * The timestamp scale (magnitude)
         */
        private final int fScale;

        // ------------------------------------------------------------------------
        // Constructors
        // ------------------------------------------------------------------------

        /**
         * Full constructor
         *
         * @param value
         *            the timestamp value
         * @param scale
         *            the timestamp scale
         */
        public Impl(final long value, final int scale) {
            fValue = value;
            fScale = scale;
        }

        @Override
        public long getValue() {
            return fValue;
        }

        @Override
        public int getScale() {
            return fScale;
        }
    }

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
        if (value == 0) {
            return ZERO;
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
        return new Impl(value, scale);
    }

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Zero - a zero time constant. The value is zero, so this allows some
     * interesting simplifications.
     */
    public static final @NonNull ITmfTimestamp ZERO = new Zero();
    private static final class Zero extends TmfTimestamp {
        @Override
        public long getValue() {
            return 0;
        }

        @Override
        public int getScale() {
            return 0;
        }

        @Override
        public @NonNull ITmfTimestamp normalize(long offset, int scale) {
            if (offset == 0) {
                return this;
            }
            return create(offset, scale);
        }

        @Override
        public int compareTo(ITmfTimestamp ts) {
            return Long.compare(0, ts.getValue());
        }
    }

    /**
     * The beginning of time will be lesser than any other timestamp
     */
    public static final @NonNull ITmfTimestamp BIG_BANG = new BigBang();
    private static final class BigBang extends TmfTimestamp {
        @Override
        public long getValue() {
            return Long.MIN_VALUE;
        }

        @Override
        public int getScale() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int compareTo(ITmfTimestamp other) {
            if (equals(other)) {
                return 0;
            }
            return -1;
        }

        @Override
        public ITmfTimestamp normalize(long offset, int scale) {
            return this;
        }

        @Override
        public boolean equals(Object other) {
            return this == other;
        }
    }

    /**
     * The end of time will be greater than any other timestamp
     */
    public static final @NonNull ITmfTimestamp BIG_CRUNCH = new BigCrunch();
    private static final class BigCrunch extends TmfTimestamp {
        @Override
        public long getValue() {
            return Long.MAX_VALUE;
        }

        @Override
        public int getScale() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int compareTo(ITmfTimestamp other) {
            if (equals(other) == true) {
                return 0;
            }
            return 1;
        }

        @Override
        public ITmfTimestamp normalize(long offset, int scale) {
            return this;
        }

        @Override
        public boolean equals(Object other) {
            return this == other;
        }
    }

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
                    value = SaturatedArithmetic.multiply(scalingFactor, value);
                }
            }
        }

        value = SaturatedArithmetic.add(value, offset);

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
        return ((this.compareTo(range.getStartTime()) >= 0 &&
                this.compareTo(range.getEndTime()) <= 0));
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
        if (BIG_BANG.equals(ts)) {
            return 1;
        }

        if (BIG_CRUNCH.equals(ts)) {
            return -1;
        }

        if (this == ts || isIdentical(this, ts)) {
            return 0;
        }

        if (scale == ts.getScale()) {
            if (ts.getValue() == Long.MIN_VALUE) {
                return 1;
            }
            final long delta = SaturatedArithmetic.add(getValue(), -ts.getValue());
            return Long.compare(delta, 0);
        }
        final ITmfTimestamp largerScale = (scale > ts.getScale()) ? this : ts;
        final ITmfTimestamp smallerScale = (scale < ts.getScale()) ? this : ts;

        final ITmfTimestamp nts = largerScale.normalize(0, smallerScale.getScale());
        if (hasSaturated(largerScale, nts)) {
            // We've saturated largerScale.
            if (smallerScale.getScale() == scale) {
                return Long.compare(0, nts.getValue());
            }
            return Long.compare(nts.getValue(), 0);
        }
        if (smallerScale.getScale() == scale) {
            return Long.compare(value, nts.getValue());
        }
        return Long.compare(nts.getValue(), smallerScale.getValue());
    }

    private static boolean hasSaturated(final ITmfTimestamp ts, final ITmfTimestamp nts) {
        return (nts.getValue() == 0 && ts.getValue() != 0) || !isIdentical(ts, nts) && ((nts.getValue() == Long.MAX_VALUE) || (nts.getValue() == Long.MIN_VALUE));
    }

    private static boolean isIdentical(final ITmfTimestamp ts, final ITmfTimestamp nts) {
        return ts.getValue() == nts.getValue() && ts.getScale() == nts.getScale();
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
        if (getScale() == ts.getScale()) {
            return getValue() == ts.getValue();
        }
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
