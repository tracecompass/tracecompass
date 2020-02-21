/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Philippe Proulx
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
