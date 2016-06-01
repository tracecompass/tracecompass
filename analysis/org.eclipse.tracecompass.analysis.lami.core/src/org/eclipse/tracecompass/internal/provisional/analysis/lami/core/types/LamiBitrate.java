/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Philippe Proulx
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Class for LAMI 'bitrate' types.
 *
 * @author Philippe Proulx
 */
public class LamiBitrate extends LamiDoubleNumber {

    /**
     * Constructor
     *
     * @param value
     *            The bitrate value, in bits per second (bps)
     */
    public LamiBitrate(double value) {
        super(value);
    }

    /**
     * Constructor (with limits)
     *
     * @param low
     *            Lower bound of value (bps)
     * @param value
     *            Value (bps)
     * @param high
     *            Higher bound of value (bps)
     */
    public LamiBitrate(@Nullable Double low, @Nullable Double value, @Nullable Double high) {
        super(low, value, high);
    }
}
