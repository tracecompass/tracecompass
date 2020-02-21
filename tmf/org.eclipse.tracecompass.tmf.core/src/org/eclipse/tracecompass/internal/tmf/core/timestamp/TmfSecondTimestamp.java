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
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Standardize on the default toString()
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.timestamp;

import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;

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
}
