/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.core.timestamp;

import java.util.TimeZone;

/**
 * A generic timestamp implementation for delta between timestamps.
 * The toString() method takes negative values into consideration.
 *
 * @author Bernd Hufmann
 * @since 2.0
 */
public class TmfTimestampDelta extends TmfTimestamp {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     */
    public TmfTimestampDelta() {
        super();
    }

    /**
     * Simple constructor (scale = precision = 0)
     *
     * @param value the timestamp value
     */

    public TmfTimestampDelta(long value) {
        super(value);
    }

    /**
     * Simple constructor (precision = 0)
     *
     * @param value the timestamp value
     * @param scale the timestamp scale
     */
    public TmfTimestampDelta(long value, int scale) {
        super(value, scale);
    }


    /**
     * Copy constructor
     *
     * @param timestamp the timestamp to copy
     */
    public TmfTimestampDelta(ITmfTimestamp timestamp) {
        super(timestamp);
    }

    /**
     * Full constructor
     *
     * @param value the timestamp value
     * @param scale the timestamp scale
     * @param precision the timestamp precision
     */
    public TmfTimestampDelta(long value, int scale, int precision) {
        super(value, scale, precision);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public ITmfTimestamp normalize(final long offset, final int scale) {
        ITmfTimestamp nts = super.normalize(offset, scale);
        return new TmfTimestampDelta(nts.getValue(), nts.getScale(), nts.getPrecision());
    }

    @Override
    public String toString() {
        return toString(TmfTimestampFormat.getDefaulIntervalFormat());
    }

    @Override
    public String toString(TmfTimestampFormat format) {
        if (getValue() < 0) {
            TmfTimestampDelta tmpTs = new TmfTimestampDelta(-getValue(), getScale(), getPrecision());
            return "-" + tmpTs.toString(format); //$NON-NLS-1$
        }
        TmfTimestampFormat deltaFormat = new TmfTimestampFormat(format.toPattern());
        deltaFormat.setTimeZone(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$
        return super.toString(deltaFormat);
    }
}
