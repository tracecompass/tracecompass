/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.format;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableMap;

/**
 * Provides a formatter for numbers along with units. It receives a size and it
 * formats a number to the closest metric prefix above 1, with at most 3
 * decimals. Units will be appended to the data type.
 * <p>
 * For example, if the unit is 'rabbit', a value of 2 would be formatted as 2
 * rabbit and 1234 would be 1.234 krabbit.
 *
 * @author Matthew Khouzam
 * @since 4.3
 */
public class DecimalWithUnitPrefixFormat extends Format {

    private static final long serialVersionUID = 3934127385682676804L;
    private static final String METRIC_K = "k"; //$NON-NLS-1$
    private static final String BINARY_K = "K"; //$NON-NLS-1$
    private static final String M = "M"; //$NON-NLS-1$
    private static final String G = "G"; //$NON-NLS-1$
    private static final String T = "T"; //$NON-NLS-1$
    private static final String MILLI = "m"; //$NON-NLS-1$
    private static final String MICRO = "µ"; //$NON-NLS-1$
    private static final String MICRO2 = "u"; //$NON-NLS-1$
    private static final String NANO = "n"; //$NON-NLS-1$
    private static final String PICO = "p"; //$NON-NLS-1$
    private static final String SUFFIX_REPLACEMENT = "%suffix%"; //$NON-NLS-1$
    private static final long KILO = 1000;
    private static final Format FORMAT = new DecimalFormat("#.###"); //$NON-NLS-1$

    private static final String UNIT_PATTERN_STR = "^[\\s]*([" + //$NON-NLS-1$
            METRIC_K + BINARY_K + M + G + T + MILLI + MICRO + MICRO2 + NANO + PICO + "]?)" + SUFFIX_REPLACEMENT; //$NON-NLS-1$
    private static final String SUFFIX = ""; //$NON-NLS-1$
    /* Map of prefix to exponent */
    private static final Map<String, Integer> PREFIX_MAP = ImmutableMap.<String, Integer> builder()
            .put(METRIC_K, +1)
            .put(BINARY_K, +1)
            .put(M, +2)
            .put(G, +3)
            .put(T, +4)
            .put(MILLI, -1)
            .put(MICRO, -2)
            .put(MICRO2, -2)
            .put(NANO, -3)
            .put(PICO, -4)
            .build();

    private final String fSuffix;
    private final Pattern fUnitPattern;
    private final long fKilo;
    private final String fKiloPrefix;

    /**
     * Protected constructor
     */
    protected DecimalWithUnitPrefixFormat() {
        this(SUFFIX, KILO);
    }

    /**
     * Constructor with suffix and kilo value
     *
     * @param suffix
     *            The suffix to append to the units for this formatter
     * @param kiloValue
     *            The value of a 1000 divided. Defaults to <code>1000</code>.
     *            Binary values should put a value of <code>1024</code>
     * @since 4.1
     */
    protected DecimalWithUnitPrefixFormat(String suffix, long kiloValue) {
        super();
        fSuffix = suffix;
        fKilo = kiloValue;
        // Compile the unit pattern for this formatter
        fUnitPattern = Pattern.compile(UNIT_PATTERN_STR.replace(SUFFIX_REPLACEMENT, suffix));
        fKiloPrefix = fKilo == 1024 ? BINARY_K : METRIC_K;
    }

    /**
     * Constructor with unit suffix to append to the formatted string. For
     * example <code>m</code> to have 0.001 display 1mm and 1000 to display 1km
     *
     * @param suffix
     *            The unit suffix to append to the metric prefixed string
     * @since 4.1
     */
    public DecimalWithUnitPrefixFormat(@NonNull String suffix) {
        this(suffix, KILO);
    }

    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        if (obj instanceof Number) {
            Number num = (Number) obj;
            double value = num.doubleValue();
            double abs = Math.abs(value);
            if (value == 0) {
                return toAppendTo.append("0").append(' ').append(fSuffix); //$NON-NLS-1$
            }
            if (abs >= 1) {
                if (abs >= fKilo * fKilo * fKilo * fKilo) {
                    return toAppendTo.append(FORMAT.format(value / (fKilo * fKilo * fKilo * fKilo))).append(' ').append(T).append(fSuffix);
                }
                if (abs >= fKilo * fKilo * fKilo) {
                    return toAppendTo.append(FORMAT.format(value / (fKilo * fKilo * fKilo))).append(' ').append(G).append(fSuffix);
                }
                if (abs >= fKilo * fKilo) {
                    return toAppendTo.append(FORMAT.format(value / (fKilo * fKilo))).append(' ').append(M).append(fSuffix);
                }
                if (abs >= fKilo) {
                    return toAppendTo.append(FORMAT.format(value / (fKilo))).append(' ').append(fKiloPrefix).append(fSuffix);
                }
                return toAppendTo.append(FORMAT.format(value)).append(' ').append(fSuffix);
            }
            if (abs < (double) 1 / (fKilo * fKilo * fKilo)) {
                return toAppendTo.append(FORMAT.format(value * fKilo * fKilo * fKilo * fKilo)).append(' ').append(PICO).append(fSuffix);
            }
            if (abs < (double) 1 / (fKilo * fKilo)) {
                return toAppendTo.append(FORMAT.format(value * fKilo * fKilo * fKilo)).append(' ').append(NANO).append(fSuffix);
            }
            if (abs < (double) 1 / (fKilo)) {
                return toAppendTo.append(FORMAT.format(value * fKilo * fKilo)).append(' ').append(MICRO).append(fSuffix);
            }

            return toAppendTo.append(FORMAT.format(value * KILO)).append(' ').append(MILLI).append(fSuffix);
        }
        return toAppendTo;
    }

    /**
     * @since 2.2
     */
    @Override
    public Number parseObject(String source, ParsePosition pos) {
        Number number = NumberFormat.getInstance().parse(source, pos);
        if (number == null) {
            return null;
        }
        // Try to match the unit, if no match, assume no unit and don't
        // update the position
        String remaining = source.substring(pos.getIndex());
        Matcher matcher = fUnitPattern.matcher(remaining);
        Integer thousands = null;
        if (matcher.find()) {
            String unitString = matcher.group();
            String prefix = matcher.group(1);
            thousands = PREFIX_MAP.get(prefix);
            pos.setIndex(pos.getIndex() + unitString.length());
        }
        if (thousands != null && thousands < 0 && Double.isFinite(number.doubleValue())) {
            // Calculate the value with a multiplier < 0
            double multiplier = 1 / Math.pow(fKilo, Math.abs(thousands));
            BigDecimal bd = new BigDecimal(number.toString());
            bd = bd.multiply(BigDecimal.valueOf(multiplier));
            return bd.doubleValue();
        }
        if (thousands != null && Double.isFinite(number.doubleValue())) {
            // Calculate the value with a value > 0 to keep the long value when
            // possible
            long multiplier = (long) Math.pow(fKilo, thousands);
            BigDecimal bd = new BigDecimal(number.toString());
            bd = bd.multiply(BigDecimal.valueOf(multiplier));
            if (bd.remainder(BigDecimal.ONE).equals(BigDecimal.ZERO) &&
                    bd.abs().compareTo(new BigDecimal(Long.MAX_VALUE)) < 0) {
                return bd.longValue();
            }
            return bd.doubleValue();
        }
        return number;
    }
}
