/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Jonathan Rajotte-Julien
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.ui.data;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.math.BigDecimal;

/**
 * BigDecimal based range representation
 *
 * @author Jonathan Rajotte-Julien
 */
public class ChartRange {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private BigDecimal fMinimum;
    private BigDecimal fMaximum;
    private BigDecimal fRange;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     */
    public ChartRange() {
        fMinimum = checkNotNull(BigDecimal.ZERO);
        fMaximum = checkNotNull(BigDecimal.ONE);
        fRange = checkNotNull(getMaximum().subtract(getMinimum()));
    }

    /**
     * Constructor with minimum and maximum values supplied.
     *
     * @param minimum
     *            The minimum value of the range
     * @param maximum
     *            The maximum value of the range
     */
    public ChartRange(BigDecimal minimum, BigDecimal maximum) {
        fMinimum = minimum;
        fMaximum = maximum;
        fRange = checkNotNull(maximum.subtract(minimum));
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that returns the lower bound of the range.
     *
     * @return The minimum value of the range
     */
    public BigDecimal getMinimum() {
        return fMinimum;
    }

    /**
     * Accessor that returns the upper bound of the range.
     *
     * @return The maximum value of the range
     */
    public BigDecimal getMaximum() {
        return fMaximum;
    }

    /**
     * Accessor that returns the difference between the lower and the upper
     * bounds of the range.
     *
     * @return The range delta
     */
    public BigDecimal getDelta() {
        return fRange;
    }

    /**
     * Method that checks if the delta is equal to zero.
     *
     * @return {@code true} if the delta is null, else {@code false}
     */
    public boolean isDeltaNull() {
        return getDelta().compareTo(BigDecimal.ZERO) == 0;
    }

    // ------------------------------------------------------------------------
    // Mutators
    // ------------------------------------------------------------------------

    /**
     * Mutator that sets the minimum value of the range.
     *
     * @param minimum
     *            The new minimum value
     */
    public void setMinimum(BigDecimal minimum) {
        fMinimum = minimum;
        fRange = checkNotNull(getMaximum().subtract(getMinimum()));
    }

    /**
     * Mutator that sets the maximum value of the range.
     *
     * @param maximum
     *            The new maximum value
     */
    public void setMaximum(BigDecimal maximum) {
        fMaximum = maximum;
        fRange = checkNotNull(getMaximum().subtract(getMinimum()));
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * This method clamps the positive minimum value of this range down to zero.
     * It returns the current object back with the minimum value modified.
     *
     * @return The current range map
     */
    public ChartRange clamp() {
        fMinimum = fMinimum.min(BigDecimal.ZERO);

        return this;
    }

}