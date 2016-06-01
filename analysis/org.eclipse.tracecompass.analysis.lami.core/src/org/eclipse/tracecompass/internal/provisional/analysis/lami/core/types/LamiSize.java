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
 * Class for LAMI 'size' types.
 *
 * @author Philippe Proulx
 */
public class LamiSize extends LamiLongNumber {

    /**
     * Constructor
     *
     * @param value
     *            The size value, in bytes
     */
    public LamiSize(long value) {
        super(value);
    }

    /**
     * Constructor (with limits)
     *
     * @param low
     *            Lower bound of value (bytes)
     * @param value
     *            Value (bytes)
     * @param high
     *            Higher bound of value (bytes)
     */
    public LamiSize(@Nullable Long low, @Nullable Long value, @Nullable Long high) {
        super(low, value, high);
    }
}
