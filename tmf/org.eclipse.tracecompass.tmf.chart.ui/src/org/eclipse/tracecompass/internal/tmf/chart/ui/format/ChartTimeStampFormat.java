/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.ui.format;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.math.BigDecimal;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.chart.ui.data.ChartRangeMap;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;

/**
 * Formatter for timestamps.
 *
 * TODO: See if this formatter is specific to the swtchart charts that are
 * implemented in this plugin or if they can be re-used in another other
 * charting scheme. We'll probably know when we actually have another
 * implementation. If swtchart specific, the name of the class and package
 * should make it clear.
 *
 * @author Michael Jeanson
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ChartTimeStampFormat extends Format {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final long serialVersionUID = 8102026791684954897L;

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final TmfTimestampFormat fFormat;
    private @Nullable ChartRangeMap fRangeMap;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor with a range map supplied.
     *
     * @param map
     *            A internal and external ranges map
     */
    public ChartTimeStampFormat(@Nullable ChartRangeMap map) {
        fFormat = checkNotNull(TmfTimestampFormat.getDefaulTimeFormat());
        fRangeMap = map;
    }

    /**
     * Constructor with a pattern and a range map supplied.
     *
     * @param pattern
     *            The format pattern
     * @param map
     *            A chart range map for mapping values
     */
    public ChartTimeStampFormat(String pattern, @Nullable ChartRangeMap map) {
        fFormat = new TmfTimestampFormat(pattern);
        fRangeMap = map;
    }

    // ------------------------------------------------------------------------
    // Mutators
    // ------------------------------------------------------------------------

    /**
     * Mutators that sets the chart range map of this formatter.
     *
     * @param map
     *            The new chart range map
     */
    public void setRangeMap(ChartRangeMap map) {
        fRangeMap = map;
    }

    /**
     * Get the pattern string of the format.
     *
     * @return the pattern string.
     */
    public String getPattern() {
        return fFormat.toPattern();
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

        /* If no range was provided, format with the number unchanged */
        ChartRangeMap rangeMap = fRangeMap;
        if (rangeMap == null) {
            long time = ((Number) obj).longValue();
            return checkNotNull(toAppendTo.append(fFormat.format(time)));
        }

        /* Find external value before formatting */
        BigDecimal externalValue = checkNotNull(fRangeMap).getExternalValue(number);
        return checkNotNull(toAppendTo.append(fFormat.format(externalValue.longValue())));
    }

    @Override
    public @Nullable Object parseObject(@Nullable String source, @Nullable ParsePosition pos) {
        return null;
    }

}
