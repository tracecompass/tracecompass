/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Jonathan Rajotte-Julien
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.ui.data;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.math.BigDecimal;

/**
 * BigDecimal based range representation. The chart range cannot be 0.
 *
 * TODO: See if this chart range is specific to the swtchart charts that are
 * implemented in this plugin or if they can be re-used in another other
 * charting scheme. We'll probably know when we actually have another
 * implementation. If swtchart specific, the name of the class and package
 * should make it clear.
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
        fMinimum = BigDecimal.ZERO;
        fMaximum = BigDecimal.ONE;
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
        BigDecimal subtract = maximum.subtract(minimum);
        if (minimum.compareTo(maximum) > 0) {
            throw new IllegalArgumentException("ChartRange: minimum should be lower than or equal to the maximum (min: " + minimum + ", max: " + maximum + ')'); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (BigDecimal.ZERO.equals(subtract)) {
            /*
             * Minimum and maximum values are all the same, so add 1 to the
             * minimum
             */
            fMinimum = minimum;
            fMaximum = minimum.add(BigDecimal.ONE);
            fRange = BigDecimal.ONE;
        } else {
            fMinimum = minimum;
            fMaximum = maximum;
            fRange = checkNotNull(subtract);
        }
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
        if (fMinimum.compareTo(BigDecimal.ZERO) > 0) {
            fMinimum = BigDecimal.ZERO;
            fRange = fMaximum;
        }

        return this;
    }

    @Override
    public String toString() {
        return "ChartRange: [" + fMinimum + ", " + fMaximum + "]"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
    }

}