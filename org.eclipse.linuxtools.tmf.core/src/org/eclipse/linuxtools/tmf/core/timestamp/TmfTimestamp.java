/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Thomas Gatterweh	- Updated scaling / synchronization
 *   Francois Chouinard - Refactoring to align with TMF Event Model 1.0
 *   Francois Chouinard - Implement augmented interface
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.timestamp;

/**
 * A generic timestamp implementation. The timestamp is represented by the
 * tuple { value, scale, precision }. By default, timestamps are scaled in
 * seconds.
 *
 * @author Francois Chouinard
 * @version 1.1
 * @since 2.0
 */
public class TmfTimestamp implements ITmfTimestamp {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The beginning of time
     */
    public static final ITmfTimestamp BIG_BANG =
            new TmfTimestamp(Long.MIN_VALUE, Integer.MAX_VALUE, 0);

    /**
     * The end of time
     */
    public static final ITmfTimestamp BIG_CRUNCH =
            new TmfTimestamp(Long.MAX_VALUE, Integer.MAX_VALUE, 0);

    /**
     * A more practical definition of "beginning of time"
     */
    public static final ITmfTimestamp PROJECT_IS_FUNDED = BIG_BANG;

    /**
     * A more practical definition of "end of time"
     */
    public static final ITmfTimestamp PROJECT_IS_CANNED = BIG_CRUNCH;

    /**
     * Zero
     */
    public static final ITmfTimestamp ZERO =
            new TmfTimestamp(0, 0, 0);

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

    /**
     * The value precision (tolerance)
     */
    private final int fPrecision;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TmfTimestamp() {
        this(0, ITmfTimestamp.SECOND_SCALE, 0);
    }

    /**
     * Simple constructor (scale = precision = 0)
     *
     * @param value the timestamp value
     */
    public TmfTimestamp(final long value) {
        this(value, ITmfTimestamp.SECOND_SCALE, 0);
    }

    /**
     * Simple constructor (precision = 0)
     *
     * @param value the timestamp value
     * @param scale the timestamp scale
     */
    public TmfTimestamp(final long value, final int scale) {
        this(value, scale, 0);
    }

    /**
     * Full constructor
     *
     * @param value the timestamp value
     * @param scale the timestamp scale
     * @param precision the timestamp precision
     */
    public TmfTimestamp(final long value, final int scale, final int precision) {
        fValue = value;
        fScale = scale;
        fPrecision = Math.abs(precision);
    }

    /**
     * Copy constructor
     *
     * @param timestamp the timestamp to copy
     */
    public TmfTimestamp(final ITmfTimestamp timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException();
        }
        fValue = timestamp.getValue();
        fScale = timestamp.getScale();
        fPrecision = timestamp.getPrecision();
    }

    // ------------------------------------------------------------------------
    // ITmfTimestamp
    // ------------------------------------------------------------------------

    @Override
    public long getValue() {
        return fValue;
    }

    @Override
    public int getScale() {
        return fScale;
    }

    @Override
    public int getPrecision() {
        return fPrecision;
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
        int precision = fPrecision;

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
                precision *= scalingFactor;
            } else {
                value /= scalingFactor;
                precision /= scalingFactor;
            }
        }

        // Then, apply the offset
        if (offset < 0) {
            value = (value < Long.MIN_VALUE - offset) ? Long.MIN_VALUE : value + offset;
        } else {
            value = (value > Long.MAX_VALUE - offset) ? Long.MAX_VALUE : value + offset;
        }

        return new TmfTimestamp(value, scale, precision);
    }

    @Override
    public int compareTo(final ITmfTimestamp ts, final boolean withinPrecision) {

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
            if ((delta == 0) || (withinPrecision && (Math.abs(delta) <= (fPrecision + nts.getPrecision())))) {
                return 0;
            }
            return (delta > 0) ? 1 : -1;
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

    @Override
    public ITmfTimestamp getDelta(final ITmfTimestamp ts) {
        final ITmfTimestamp nts = ts.normalize(0, fScale);
        final long value = fValue - nts.getValue();
        return new TmfTimestampDelta(value, fScale, fPrecision + nts.getPrecision());
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
        return compareTo(ts, false);
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
        result = prime * result + fPrecision;
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
        if (!(other instanceof TmfTimestamp)) {
            return false;
        }
        final TmfTimestamp ts = (TmfTimestamp) other;
        return compareTo(ts, false) == 0;
    }

    @Override
    public String toString() {
        return toString(TmfTimestampFormat.getDefaulTimeFormat());
    }

    /**
     * @since 2.0
     */
    @Override
    public String toString(final TmfTimestampFormat format) {
        try {
            ITmfTimestamp ts = normalize(0, ITmfTimestamp.NANOSECOND_SCALE);
            return format.format(ts.getValue());
        }
        catch (ArithmeticException e) {
            return format.format(0);
        }
    }

}
