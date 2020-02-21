/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
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
 * Lami timestamp data type
 *
 * @author Alexandre Montplaisir
 */
public class LamiTimestamp extends LamiLongNumber {

    /**
     * Construct a time stamp from a value in ns.
     *
     * @param value
     *            The value
     */
    public LamiTimestamp(long value) {
        super(value);
    }

    /**
     * Constructor (with limits)
     *
     * @param low
     *            Lower bound of value (ns since Unix epoch)
     * @param value
     *            Value (ns since Unix epoch)
     * @param high
     *            Higher bound of value (ns since Unix epoch)
     */
    public LamiTimestamp(@Nullable Long low, @Nullable Long value, @Nullable Long high) {
        super(low, value, high);
    }
}
