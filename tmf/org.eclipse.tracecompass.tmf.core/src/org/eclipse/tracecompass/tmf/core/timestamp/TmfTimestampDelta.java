/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.core.timestamp;

import java.util.TimeZone;

/**
 * A generic timestamp implementation for delta between timestamps. The
 * toString() method takes negative values into consideration.
 *
 * @author Bernd Hufmann
 */
public class TmfTimestampDelta extends TmfTimestamp {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final long fValue;
    private final int fScale;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     */
    public TmfTimestampDelta() {
        this(0, ITmfTimestamp.SECOND_SCALE);
    }

    /**
     * Simple constructor (scale = precision = 0)
     *
     * @param value
     *            the timestamp value
     */

    public TmfTimestampDelta(long value) {
        this(value, ITmfTimestamp.SECOND_SCALE);
    }

    /**
     * Constructor
     *
     * @param value
     *            the timestamp value
     * @param scale
     *            the timestamp scale
     */
    public TmfTimestampDelta(long value, int scale) {
        fValue = value;
        fScale = scale;
    }

    /**
     * Copy constructor
     *
     * @param timestamp
     *            the timestamp to copy
     */
    public TmfTimestampDelta(ITmfTimestamp timestamp) {
        this(timestamp.getValue(), timestamp.getScale());
    }

    // ------------------------------------------------------------------------
    // Operations
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
    public ITmfTimestamp normalize(final long offset, final int scale) {
        ITmfTimestamp nts = super.normalize(offset, scale);
        return new TmfTimestampDelta(nts.getValue(), nts.getScale());
    }

    @Override
    public String toString() {
        return toString(TmfTimestampFormat.getDefaulIntervalFormat());
    }

    @Override
    public String toString(TmfTimestampFormat format) {
        if (getValue() < 0) {
            TmfTimestampDelta tmpTs = new TmfTimestampDelta(-getValue(), getScale());
            return "-" + tmpTs.toString(format); //$NON-NLS-1$
        }
        TmfTimestampFormat deltaFormat = new TmfTimestampFormat(format.toPattern());
        deltaFormat.setTimeZone(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$
        return super.toString(deltaFormat);
    }
}
