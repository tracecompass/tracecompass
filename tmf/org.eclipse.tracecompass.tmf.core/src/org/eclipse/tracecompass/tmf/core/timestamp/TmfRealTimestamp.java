/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.timestamp;

/**
 * A timestamp with a user provided scale and value
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class TmfRealTimestamp extends TmfTimestamp {

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
    public TmfRealTimestamp(final long value, final int scale) {
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
