/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Standardize on the default toString()
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.timestamp;

/**
 * A simplified timestamp where scale and precision are set to 0.
 *
 * @author Francois Chouinard
 * @version 1.1
 * @since 2.0
 */
public class TmfSimpleTimestamp extends TmfTimestamp {

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor (value = 0)
     */
    public TmfSimpleTimestamp() {
        this(0);
    }

    /**
     * Full constructor
     *
     * @param value the timestamp value
     */
    public TmfSimpleTimestamp(final long value) {
        super(value, 0, 0);
    }

    /**
     * Copy constructor.
     *
     * If the parameter is not a TmfSimpleTimestamp, the timestamp will be
     * scaled to seconds, and the precision will be discarded.
     *
     * @param timestamp
     *            The timestamp to copy
     */
    public TmfSimpleTimestamp(final ITmfTimestamp timestamp) {
        super(timestamp.normalize(0, ITmfTimestamp.SECOND_SCALE).getValue(), 0, 0);
    }

    // ------------------------------------------------------------------------
    // ITmfTimestamp
    // ------------------------------------------------------------------------

    @Override
    public ITmfTimestamp normalize(final long offset, final int scale) {
        if (scale == 0) {
            return new TmfSimpleTimestamp(getValue() + offset);
        }
        return super.normalize(offset, scale);
    }

    @Override
    public int compareTo(final ITmfTimestamp ts, final boolean withinPrecision) {
        if (ts instanceof TmfSimpleTimestamp) {
            final long delta = getValue() - ts.getValue();
            return (delta == 0) ? 0 : (delta > 0) ? 1 : -1;
        }
        return super.compareTo(ts, withinPrecision);
    }

    @Override
    public ITmfTimestamp getDelta(final ITmfTimestamp ts) {
        if (ts instanceof TmfSimpleTimestamp) {
            return new TmfTimestampDelta(getValue() - ts.getValue());
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
        if (!(other instanceof TmfSimpleTimestamp)) {
            return super.equals(other);
        }
        final TmfSimpleTimestamp ts = (TmfSimpleTimestamp) other;

        return compareTo(ts, false) == 0;
    }

}
