/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
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
 * Intermediate class for {@link LamiNumber}s that contain Long values.
 *
 * @author Alexandre Montplaisir
 */
public class LamiLongNumber extends LamiNumber {

    /**
     * Constructor specifying only a value
     *
     * @param value
     *            The value
     */
    public LamiLongNumber(Long value) {
        super(value);
    }

    /**
     * Constructor specifying a nominal value, and higher/lower bounds
     *
     * @param lowLimit
     *            Lower limit
     * @param value
     *            Nominal value
     * @param highLimit
     *            Higher limit
     */
    public LamiLongNumber(@Nullable Long lowLimit, @Nullable Long value, @Nullable Long highLimit) {
        super(lowLimit, value, highLimit);
    }

    @Override
    public @Nullable Long getLowerLimit() {
        return (Long) super.getLowerLimit();
    }

    @Override
    public @Nullable Long getValue() {
        return (Long) super.getValue();
    }

    @Override
    public @Nullable Long getHigherLimit() {
        return (Long) super.getHigherLimit();
    }

}
