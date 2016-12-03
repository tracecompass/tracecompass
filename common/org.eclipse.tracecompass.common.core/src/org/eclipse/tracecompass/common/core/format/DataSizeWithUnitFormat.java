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

import org.eclipse.jdt.annotation.NonNull;

/**
 * Provides a formatter for data sizes along with the unit of size (KG, MB, GB
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
    private static final long KILO = 1024;
    private static final Format FORMAT = new DecimalFormat("#.###"); //$NON-NLS-1$

    /**
     * Protected constructor
     */
    protected DataSizeWithUnitFormat() {
        super();
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
                return toAppendTo.append("0"); //$NON-NLS-1$
            }
            if (abs >= KILO * KILO * KILO * KILO) {
                return toAppendTo.append(FORMAT.format(value / (KILO * KILO * KILO * KILO))).append(' ').append(T).append(B);
            }
            if (abs >= KILO * KILO * KILO) {
                return toAppendTo.append(FORMAT.format(value / (KILO * KILO * KILO))).append(' ').append(G).append(B);
            }
            if (abs >= KILO * KILO) {
                return toAppendTo.append(FORMAT.format(value / (KILO * KILO))).append(' ').append(M).append(B);
            }
            if (abs >= KILO) {
                return toAppendTo.append(FORMAT.format(value / (KILO))).append(' ').append(K).append(B);
            }
            return toAppendTo.append(FORMAT.format(value)).append(' ').append(B);
        }
        return toAppendTo.append(obj);
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
        String unit = source.substring(pos.getIndex()).trim().toUpperCase();
        long multiplier = 1;
        if (!unit.isEmpty()) {
            if (unit.startsWith(K)) {
                multiplier = KILO;
            } else if (unit.startsWith(M)) {
                multiplier = KILO * KILO;
            } else if (unit.startsWith(G)) {
                multiplier = KILO * KILO * KILO;
            } else if (unit.startsWith(T)) {
                multiplier = KILO * KILO * KILO * KILO;
            }
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
