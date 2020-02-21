/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
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
import java.math.RoundingMode;

/**
 * Simple class that maps two {@link ChartRange} together. The first one is the
 * plotted range: it represents the range of the chart in which the data will be
 * plotted. The second one is the input data range: it represents the range of
 * the incoming data that will be plotted.
 * <p>
 * This map is used for mapping values that might be too big for a chart. Values
 * that are in the external range are mapped to fit the internal range.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ChartRangeMap {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final int BIG_DECIMAL_DIVISION_SCALE = 22;

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private ChartRange fPlottedRange;
    private ChartRange fInputDataRange;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     */
    public ChartRangeMap() {
        fPlottedRange = new ChartRange();
        fInputDataRange = new ChartRange();
    }

    /**
     * Surcharged constructor with the input data range.
     *
     * @param input
     *            The range of the input data
     */
    public ChartRangeMap(ChartRange input) {
        fPlottedRange = new ChartRange();
        fInputDataRange = input;
    }

    /**
     * Surcharged constructor with the plotted and input data ranges.
     *
     * @param plotted
     *            The plotted range
     * @param input
     *            The range of the input data
     */
    public ChartRangeMap(ChartRange plotted, ChartRange input) {
        fPlottedRange = plotted;
        fInputDataRange = input;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that returns the plotted range of the map.
     *
     * @return The plotted range
     */
    public ChartRange getPlottedRange() {
        return fPlottedRange;
    }

    /**
     * Accessor that returns the input data range of the map.
     *
     * @return The input data range
     */
    public ChartRange getInputDataRange() {
        return fInputDataRange;
    }

    // ------------------------------------------------------------------------
    // Mutators
    // ------------------------------------------------------------------------

    /**
     * Mutator that sets the plotted range of the map.
     *
     * @param plotted
     *            The new plotted range
     */
    public void setPlottedRange(ChartRange plotted) {
        fPlottedRange = plotted;
    }

    /**
     * Mutator that sets the input data range of the map.
     *
     * @param input
     *            The new input data range
     */
    public void setInputDataRange(ChartRange input) {
        fInputDataRange = input;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * This method transforms a number from the input data range into a number
     * that fits in the plotted range.
     * <p>
     * Incoming numbers from the data might be bigger than the numbers that the
     * chart library supports (e.g. SWT supports Double and we can pass Long).
     * While processing the numbers, a loss of precision might occur. In order
     * to minimize this, we transform the raw values to an internal
     * representation based on a linear transformation.
     * <p>
     * Let <i>e_val</i>, <i>e_min</i>, <i>e_Δ</i> be the external value,
     * external minimum and external delta respectively and <i>i_min</i>,
     * <i>i_Δ</i> be the internal minimum and the internal delta. The internal
     * value <i>i_val</i> is given by the formula:
     * <p>
     * i_val=(e_val-e_min)*(i_Δ/e_Δ)+i_min
     *
     * @param number
     *            A number to transform
     * @return The transformed value
     */
    public Number getInternalValue(Number number) {
        BigDecimal value = new BigDecimal(number.toString());
        ChartRange internal = getPlottedRange();
        ChartRange external = getInputDataRange();

        if (external.getDelta().compareTo(BigDecimal.ZERO) == 0) {
            return internal.getMinimum().doubleValue();
        }

        /* Apply the formula */
        BigDecimal internalValue = value
                .subtract(external.getMinimum())
                .multiply(internal.getDelta())
                .divide(external.getDelta(), BIG_DECIMAL_DIVISION_SCALE, RoundingMode.DOWN)
                .add(internal.getMinimum());

        return checkNotNull(internalValue);
    }

    /**
     * Util method that transforms an plotted value back into its original
     * range.
     * <p>
     * It is very similar to {@link #getInternalValue(Number)}, except that is
     * apply the formula in reverse in order to obtain the original value while
     * minimizing lost in precision.
     *
     * @param number
     *            A number to transform
     * @return A BigDecimal representation of the external value
     */
    public BigDecimal getExternalValue(Number number) {
        ChartRange internal = getPlottedRange();
        ChartRange external = getInputDataRange();

        if (internal.getDelta().compareTo(BigDecimal.ZERO) == 0) {
            return external.getMinimum();
        }

        /* Apply the formula in reverse */
        BigDecimal externalValue = (new BigDecimal(number.toString()))
                .subtract(internal.getMinimum())
                .multiply(external.getDelta())
                .divide(internal.getDelta(), BIG_DECIMAL_DIVISION_SCALE, RoundingMode.DOWN)
                .add(external.getMinimum());

        return checkNotNull(externalValue);
    }

    @Override
    public String toString() {
        return "ChartRangeMap: Input Data -> " + fInputDataRange + ", Plotted -> " + fPlottedRange + "]";  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
    }

}
