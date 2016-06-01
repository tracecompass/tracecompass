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
 * Duration data element
 *
 * @author Philippe Proulx
 */
public class LamiDuration extends LamiLongNumber {

    /**
     * Constructor
     *
     * @param value
     *            The duration value (as a long)
     */
    public LamiDuration(long value) {
        super(value);
    }

    /**
     * Constructor (with limits)
     *
     * @param low
     *            Lower bound of value (ns)
     * @param value
     *            Value (ns)
     * @param high
     *            Higher bound of value (ns)
     */
    public LamiDuration(@Nullable Long low, @Nullable Long value, @Nullable Long high) {
        super(low, value, high);
    }

}
