/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
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

package org.eclipse.tracecompass.tmf.core.timestamp;

/**
 * A simplified timestamp where scale and precision are set to 0.
 *
 * @author Francois Chouinard
 * @since 2.0
 */
public class TmfSecondTimestamp extends TmfTimestamp {

    private final long fValue;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor (value = 0)
     */
    public TmfSecondTimestamp() {
        this(0);
    }

    /**
     * Full constructor
     *
     * @param value
     *            the timestamp value
     */
    public TmfSecondTimestamp(final long value) {
        fValue = value;
    }

    @Override
    public int getScale() {
        return ITmfTimestamp.SECOND_SCALE;
    }

    @Override
    public long getValue() {
        return fValue;
    }

    // ------------------------------------------------------------------------
    // ITmfTimestamp
    // ------------------------------------------------------------------------

    @Override
    public ITmfTimestamp normalize(final long offset, final int scale) {
        if (scale == ITmfTimestamp.SECOND_SCALE) {
            return TmfTimestamp.fromSeconds(saturatedAdd(getValue(), offset));
        }
        return super.normalize(offset, scale);
    }

    @Override
    public int compareTo(final ITmfTimestamp ts) {
        if (ts instanceof TmfSecondTimestamp) {
            final long delta = getValue() - ts.getValue();
            return (delta == 0) ? 0 : (delta > 0) ? 1 : -1;
        }
        return super.compareTo(ts);
    }

    @Override
    public ITmfTimestamp getDelta(final ITmfTimestamp ts) {
        if (ts instanceof TmfSecondTimestamp) {
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
        if (!(other instanceof TmfSecondTimestamp)) {
            return super.equals(other);
        }
        final TmfSecondTimestamp ts = (TmfSecondTimestamp) other;

        return compareTo(ts) == 0;
    }

}
