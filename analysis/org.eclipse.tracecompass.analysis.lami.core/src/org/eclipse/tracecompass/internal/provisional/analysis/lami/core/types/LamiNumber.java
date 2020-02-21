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

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A LAMI number is a quantity of something with optional limits of
 * uncertainty or confidence.
 * <p>
 * The difference between a number and any other data object also
 * having an integer/real number property is that, since it represents
 * a quantity, a number always has an associated <em>unit</em>.
 *
 * @author Philippe Proulx
 */
public abstract class LamiNumber extends LamiData {

    private final @Nullable Number fLowLimit;
    private final @Nullable Number fValue;
    private final @Nullable Number fHighLimit;

    /**
     * Builds a new LAMI number data object with a single, precise value.
     *
     * @param value
     *            Value
     */
    public LamiNumber(Number value) {
        fValue = value;
        fLowLimit = null;
        fHighLimit = null;
    }

    /**
     * Builds a new LAMI number data object with a value and lower and higher
     * limits.
     *
     * @param lowLimit
     *            Lower limit
     * @param value
     *            Value
     * @param highLimit
     *            Higher limit
     */
    public LamiNumber(@Nullable Number lowLimit, @Nullable Number value, @Nullable Number highLimit) {
        fLowLimit = lowLimit;
        fValue = value;
        fHighLimit = highLimit;
    }

    /**
     * Returns the lower limit of this LAMI number data object.
     *
     * @return Lower limit or {@code null} if there's no lower limit
     */
    public @Nullable Number getLowerLimit() {
        return fLowLimit;
    }

    /**
     * Returns the value of this LAMI number data object.
     *
     * @return Value or {@code null} if there's no value
     */
    public @Nullable Number getValue() {
        return fValue;
    }

    /**
     * Returns the higher limit of this LAMI number data object.
     *
     * @return Higher limit or {@code null} if there's no higher limit
     */
    public @Nullable Number getHigherLimit() {
        return fHighLimit;
    }

    @Override
    public @Nullable String toString() {
        // TODO: The string should probably include the low and
        //       high limits here.
        if (fValue != null) {
            return fValue.toString();
        }

        return null;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (this == object) {
            return true;
        }

        if (object == null) {
            return false;
        }

        if (getClass() != object.getClass()) {
            return false;
        }

        LamiNumber number = (LamiNumber) object;

        return Objects.equals(fLowLimit, number.fLowLimit) &&
                Objects.equals(fValue, number.fValue) &&
                Objects.equals(fHighLimit, number.fHighLimit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fLowLimit, fValue, fHighLimit);
    }

}
