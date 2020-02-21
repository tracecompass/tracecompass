/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Modified from TmfSimpleTimestamp to use nanosecond scale
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.timestamp;

import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;

/**
 * A simplified timestamp where scale is nanoseconds and precision is set to 0.
 */
public final class TmfNanoTimestamp extends TmfTimestamp {

    private final long fValue;

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
     * @param value
     *            the timestamp value
     */
    public TmfNanoTimestamp(final long value) {
        fValue = value;
    }

    @Override
    public long getValue() {
        return fValue;
    }


    @Override
    public int getScale() {
        return ITmfTimestamp.NANOSECOND_SCALE;
    }

    /**
     * @since 2.0
     */
    @Override
    public long toNanos() {
        return getValue();
    }
}
