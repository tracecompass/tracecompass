/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Jonathan Rajotte-Julien
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.ui.format;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.text.FieldPosition;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.format.DecimalUnitFormat;
import org.eclipse.tracecompass.internal.tmf.chart.ui.data.ChartRangeMap;

import com.google.common.annotations.VisibleForTesting;

/**
 * Decimal formatter for graph
 *
 * Since the graph use normalized internal value the initial (external)
 * representation needs to be obtained. Subsequent formatting is done based on a
 * Double. Loss of precision could occurs based on the size. For now, loss of
 * precision for decimal values is not a big concern. If it ever become one the
 * use of Long while formatting might come in handy.
 *
 * TODO: See if this formatter is specific to the swtchart charts that are
 * implemented in this plugin or if they can be re-used in another other
 * charting scheme. We'll probably know when we actually have another
 * implementation. If swtchart specific, the name of the class and package
 * should make it clear.
 *
 * @author Jonathan Rajotte-Julien
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ChartDecimalUnitFormat extends DecimalUnitFormat {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final long serialVersionUID = -4288059349658845257L;

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final @Nullable ChartRangeMap fRangeMap;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor with a range map supplied.
     *
     * @param map
     *            A chart range map for mapping values
     */
    public ChartDecimalUnitFormat(@Nullable ChartRangeMap map) {
        super();
        fRangeMap = map;
    }

    /**
     * Constructor with a multiplication factor and range map.
     *
     * @param factor
     *            A multiplication factor to apply to the value
     * @param map
     *            A chart range map for mapping values
     */
    public ChartDecimalUnitFormat(double factor, @Nullable ChartRangeMap map) {
        super(factor);
        fRangeMap = map;
    }

    /**
     * Get the range map for this formatter
     *
     * @return The range map
     */
    @VisibleForTesting
    public @Nullable ChartRangeMap getRangeMap() {
        return fRangeMap;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public StringBuffer format(@Nullable Object obj, @Nullable StringBuffer toAppendTo, @Nullable FieldPosition pos) {
        if (!(obj instanceof Number) || toAppendTo == null) {
            throw new IllegalArgumentException("Cannot format given Object as a Number: " + obj); //$NON-NLS-1$
        }

        Number number = (Number) obj;

        /* If no map was provided, format with the number unchanged */
        ChartRangeMap rangeMap = fRangeMap;
        if (rangeMap == null) {
            StringBuffer buffer = super.format(number, toAppendTo, pos);
            return (buffer == null ? new StringBuffer() : buffer);
        }

        /* Find external value before formatting */
        Double externalValue = checkNotNull(fRangeMap).getExternalValue(number).doubleValue();
        StringBuffer buffer = super.format(externalValue, toAppendTo, pos);
        return (buffer == null ? new StringBuffer() : buffer);
    }

}
