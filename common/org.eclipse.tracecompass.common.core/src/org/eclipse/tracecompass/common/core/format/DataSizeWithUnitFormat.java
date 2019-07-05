/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.format;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Provides a formatter for data sizes along with the unit of size (KB, MB, GB
 * ou TB). It receives a size in bytes and it formats a number in the closest
 * thousand's unit, with at most 3 decimals.
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class DataSizeWithUnitFormat extends Format {

    private static final @NonNull Format INSTANCE = new DataSizeWithUnitFormat();

    private static final long serialVersionUID = 3934127385682676804L;
    private static final String B = "B"; //$NON-NLS-1$
    private static final String K = "K"; //$NON-NLS-1$
    private static final String M = "M"; //$NON-NLS-1$
    private static final String G = "G"; //$NON-NLS-1$
    private static final String T = "T"; //$NON-NLS-1$
    private static final String SUFFIX_REPLACEMENT = "%suffix%"; //$NON-NLS-1$
    private static final long KILO = 1024;
    private static final Format FORMAT = new DecimalFormat("#.###"); //$NON-NLS-1$

    private static final String UNIT_PATTERN_STR = "^[\\s]*([" + //$NON-NLS-1$
            K + M + G + T + "]?)" + B + SUFFIX_REPLACEMENT; //$NON-NLS-1$
    private static final String SUFFIX = ""; //$NON-NLS-1$

    private final String fSuffix;
    private final Pattern fUnitPattern;

    /**
     * Protected constructor
     */
    protected DataSizeWithUnitFormat() {
        this(SUFFIX);
    }

    /**
     * Constructor with suffix
     *
     * @param suffix
     *            The suffix to append to the units for this formatter
     * @since 4.1
     */
    protected DataSizeWithUnitFormat(String suffix) {
        super();
        fSuffix = suffix;
        // Compile the unit pattern for this formatter
        fUnitPattern = Pattern.compile(UNIT_PATTERN_STR.replace(SUFFIX_REPLACEMENT, suffix.toUpperCase()));
    }

    /**
     * Returns the instance of this formatter
     *
     * @return The instance of this formatter
     */
    public static @NonNull Format getInstance() {
        return INSTANCE;
    }

    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        if (obj instanceof Number) {
            Number num = (Number) obj;
            double value = num.doubleValue();
            double abs = Math.abs(value);
            if (value == 0) {
                return toAppendTo.append("0").append(' ').append(B).append(fSuffix); //$NON-NLS-1$
            }
            if (abs >= KILO * KILO * KILO * KILO) {
                return toAppendTo.append(FORMAT.format(value / (KILO * KILO * KILO * KILO))).append(' ').append(T).append(B).append(fSuffix);
            }
            if (abs >= KILO * KILO * KILO) {
                return toAppendTo.append(FORMAT.format(value / (KILO * KILO * KILO))).append(' ').append(G).append(B).append(fSuffix);
            }
            if (abs >= KILO * KILO) {
                return toAppendTo.append(FORMAT.format(value / (KILO * KILO))).append(' ').append(M).append(B).append(fSuffix);
            }
            if (abs >= KILO) {
                return toAppendTo.append(FORMAT.format(value / (KILO))).append(' ').append(K).append(B).append(fSuffix);
            }
            return toAppendTo.append(FORMAT.format(value)).append(' ').append(B).append(fSuffix);
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
        String remaining = source.substring(pos.getIndex()).toUpperCase();
        Matcher matcher = fUnitPattern.matcher(remaining);
        long multiplier = 1;
        if (matcher.find()) {
            String unitString = matcher.group();
            String prefix = matcher.group(1);

            if (prefix.equals(K)) {
                multiplier = KILO;
            } else if (prefix.equals(M)) {
                multiplier = KILO * KILO;
            } else if (prefix.equals(G)) {
                multiplier = KILO * KILO * KILO;
            } else if (prefix.equals(T)) {
                multiplier = KILO * KILO * KILO * KILO;
            }
            pos.setIndex(pos.getIndex() + unitString.length());
        }

        if (multiplier != 1 && Double.isFinite(number.doubleValue())) {
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
