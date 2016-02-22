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
 * A generic timestamp implementation. The timestamp is represented by the
 * tuple { value, scale, precision }. By default, timestamps are scaled in
 * seconds.
 *
 * @author Francois Chouinard
 */
public class TmfTimestamp implements ITmfTimestamp {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The beginning of time
     */
    public static final @NonNull ITmfTimestamp BIG_BANG =
            new TmfTimestamp(Long.MIN_VALUE, Integer.MAX_VALUE);

    /**
     * The end of time
     */
    public static final @NonNull ITmfTimestamp BIG_CRUNCH =
            new TmfTimestamp(Long.MAX_VALUE, Integer.MAX_VALUE);

    /**
     * Zero
     */
    public static final @NonNull ITmfTimestamp ZERO =
            new TmfTimestamp(0, 0);

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
     * Default constructor
     */
    public TmfTimestamp() {
        this(0, ITmfTimestamp.SECOND_SCALE);
    }

    /**
     * Simple constructor (scale = 0)
     *
     * @param value
     *            the timestamp value
     */
    public TmfTimestamp(final long value) {
        this(value, ITmfTimestamp.SECOND_SCALE);
    }

    /**
     * Full constructor
     *
     * @param value
     *            the timestamp value
     * @param scale
     *            the timestamp scale
     */
    public TmfTimestamp(final long value, final int scale) {
        fValue = value;
        fScale = scale;
    }

    /**
     * Copy constructor
     *
     * @param timestamp
     *            the timestamp to copy
     */
    public TmfTimestamp(final ITmfTimestamp timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException();
        }
        fValue = timestamp.getValue();
        fScale = timestamp.getScale();
    }

    /**
     * Copies a timestamp but with a new time value
     *
     * @param timestamp
     *            The timestamp to copy
     * @param newvalue
     *            The value the new timestamp will have
     */
    public TmfTimestamp(ITmfTimestamp timestamp, long newvalue) {
        if (timestamp == null) {
            throw new IllegalArgumentException();
        }
        fValue = newvalue;
        fScale = timestamp.getScale();
    }

    // ------------------------------------------------------------------------
    // ITmfTimestamp
    // ------------------------------------------------------------------------

    /**
     * Construct the timestamp from the ByteBuffer.
     *
     * @param bufferIn
     *            the buffer to read from
     */
    public TmfTimestamp(ByteBuffer bufferIn) {
        this(bufferIn.getLong(), bufferIn.getInt());
    }

    @Override
    public long getValue() {
        return fValue;
    }

    @Override
    public int getScale() {
        return fScale;
    }

    private static final long scalingFactors[] = new long[] {
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

        long value = fValue;

        // Handle the trivial case
        if (fScale == scale && offset == 0) {
            return this;
        }

        // In case of big bang and big crunch just return this (no need to normalize)
        if (this.equals(BIG_BANG) || this.equals(BIG_CRUNCH)) {
            return this;
        }

        // First, scale the timestamp
        if (fScale != scale) {
            final int scaleDiff = Math.abs(fScale - scale);
            if (scaleDiff >= scalingFactors.length) {
                throw new ArithmeticException("Scaling exception"); //$NON-NLS-1$
            }

            final long scalingFactor = scalingFactors[scaleDiff];
            if (scale < fScale) {
                value *= scalingFactor;
            } else {
                value /= scalingFactor;
            }
        }

        // Then, apply the offset
        if (offset < 0) {
            value = (value < Long.MIN_VALUE - offset) ? Long.MIN_VALUE : value + offset;
        } else {
            value = (value > Long.MAX_VALUE - offset) ? Long.MAX_VALUE : value + offset;
        }

        return new TmfTimestamp(value, scale);
    }

    @Override
    public ITmfTimestamp getDelta(final ITmfTimestamp ts) {
        final ITmfTimestamp nts = ts.normalize(0, fScale);
        final long value = fValue - nts.getValue();
        return new TmfTimestampDelta(value, fScale);
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
        // Check the corner cases (we can't use equals() because it uses compareTo()...)
        if (ts == null) {
            return 1;
        }
        if (this == ts || (fValue == ts.getValue() && fScale == ts.getScale())) {
            return 0;
        }
        if ((fValue == BIG_BANG.getValue() && fScale == BIG_BANG.getScale()) || (ts.getValue() == BIG_CRUNCH.getValue() && ts.getScale() == BIG_CRUNCH.getScale())) {
            return -1;
        }
        if ((fValue == BIG_CRUNCH.getValue() && fScale == BIG_CRUNCH.getScale()) || (ts.getValue() == BIG_BANG.getValue() && ts.getScale() == BIG_BANG.getScale())) {
            return 1;
        }

        try {
            final ITmfTimestamp nts = ts.normalize(0, fScale);
            final long delta = fValue - nts.getValue();
            return Long.compare(delta, 0);
        }
        catch (final ArithmeticException e) {
            // Scaling error. We can figure it out nonetheless.

            // First, look at the sign of the mantissa
            final long value = ts.getValue();
            if (fValue == 0 && value == 0) {
                return 0;
            }
            if (fValue < 0 && value >= 0) {
                return -1;
            }
            if (fValue >= 0 && value < 0) {
                return 1;
            }

            // Otherwise, just compare the scales
            final int scale = ts.getScale();
            return (fScale > scale) ? (fValue >= 0) ? 1 : -1 : (fValue >= 0) ? -1 : 1;
        }
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (fValue ^ (fValue >>> 32));
        result = prime * result + fScale;
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
        }
        catch (ArithmeticException e) {
            return format.format(0);
        }
    }

    /**
     * Write the time stamp to the ByteBuffer so that it can be saved to disk.
     * @param bufferOut the buffer to write to
     */
    public void serialize(ByteBuffer bufferOut) {
        bufferOut.putLong(fValue);
        bufferOut.putInt(fScale);
    }
}
