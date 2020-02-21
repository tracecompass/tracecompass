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
 * Intermediate class for {@link LamiNumber}s that contain Double values.
 *
 * @author Alexandre Montplaisir
 */
public class LamiDoubleNumber extends LamiNumber {

    /**
     * Constructor specifying only a value
     *
     * @param value
     *            The value
     */
    public LamiDoubleNumber(Double value) {
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
    protected LamiDoubleNumber(@Nullable Double lowLimit, @Nullable Double value, @Nullable Double highLimit) {
        super(lowLimit, value, highLimit);
    }

    @Override
    public @Nullable Double getLowerLimit() {
        return (Double) super.getLowerLimit();
    }

    @Override
    public @Nullable Double getValue() {
        return (Double) super.getValue();
    }

    @Override
    public @Nullable Double getHigherLimit() {
        return (Double) super.getHigherLimit();
    }

}
