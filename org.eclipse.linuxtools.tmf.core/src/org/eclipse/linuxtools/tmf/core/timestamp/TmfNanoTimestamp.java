/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Modified from TmfSimpleTimestamp to use nanosecond scale
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.timestamp;

/**
 * A simplified timestamp where scale is nanoseconds and precision is set to 0.
 *
 * @since 2.1
 */
public class TmfNanoTimestamp extends TmfTimestamp {

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor (value = 0)
     */
    public TmfNanoTimestamp() {
        this(0);
    }

    /**
     * Full constructor
     *
     * @param value the timestamp value
     */
    public TmfNanoTimestamp(final long value) {
        super(value, ITmfTimestamp.NANOSECOND_SCALE, 0);
    }

    /**
     * Copy constructor.
     *
     * If the parameter is not a TmfNanoTimestamp, the timestamp will be
     * scaled to nanoseconds, and the precision will be discarded.
     *
     * @param timestamp
     *            The timestamp to copy
     */
    public TmfNanoTimestamp(final ITmfTimestamp timestamp) {
        super(timestamp.normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue(), ITmfTimestamp.NANOSECOND_SCALE, 0);
    }

    // ------------------------------------------------------------------------
    // ITmfTimestamp
    // ------------------------------------------------------------------------

    @Override
    public ITmfTimestamp normalize(final long offset, final int scale) {
        if (scale == ITmfTimestamp.NANOSECOND_SCALE) {
            return new TmfNanoTimestamp(getValue() + offset);
        }
        return super.normalize(offset, scale);
    }

    @Override
    public int compareTo(final ITmfTimestamp ts, final boolean withinPrecision) {
        if (ts instanceof TmfNanoTimestamp) {
            final long delta = getValue() - ts.getValue();
            return (delta == 0) ? 0 : (delta > 0) ? 1 : -1;
        }
        return super.compareTo(ts, withinPrecision);
    }

    @Override
    public ITmfTimestamp getDelta(final ITmfTimestamp ts) {
        if (ts instanceof TmfNanoTimestamp) {
            return new TmfTimestampDelta(getValue() - ts.getValue(), ITmfTimestamp.NANOSECOND_SCALE);
        }
        return super.getDelta(ts);
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof TmfNanoTimestamp)) {
            return super.equals(other);
        }
        final TmfNanoTimestamp ts = (TmfNanoTimestamp) other;

        return compareTo(ts, false) == 0;
    }

}
