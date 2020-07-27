/*******************************************************************************
 * Copyright (c), 2020 2016 EfficiOS Inc., Michael Jeanson and others
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
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;

/**
 * Provides a formatter for decimal numbers with International System of Units
 * prefixes up to peta (quadrillion). It receives a number and formats it in the
 * closest thousand's unit, with at most 1 decimal.
 *
 * @author Michael Jeanson
 * @since 2.0
 */
public class DecimalUnitFormat extends Format {

    private static final long serialVersionUID = 3650332020346870384L;

    /* International System of Units prefixes */
    private static final String KILO_PREFIX = "k"; //$NON-NLS-1$
    private static final String MEGA_PREFIX = "M"; //$NON-NLS-1$
    private static final String GIGA_PREFIX = "G"; //$NON-NLS-1$
    private static final String TERA_PREFIX = "T"; //$NON-NLS-1$
    private static final String PETA_PREFIX = "P"; //$NON-NLS-1$

    private static final String MILLI_PREFIX = "m"; //$NON-NLS-1$
    private static final String MICRO_PREFIX = "µ"; //$NON-NLS-1$
    private static final String MICRO_PREFIX2 = "u"; //$NON-NLS-1$
    private static final String NANO_PREFIX = "n"; //$NON-NLS-1$
    private static final String PICO_PREFIX = "p"; //$NON-NLS-1$

    private static final Pattern UNIT_PATTERN = Pattern.compile("^[\\s]*([" + KILO_PREFIX + //$NON-NLS-1$
            MEGA_PREFIX + GIGA_PREFIX + TERA_PREFIX + PETA_PREFIX + MILLI_PREFIX +
            MICRO_PREFIX + MICRO_PREFIX2 + NANO_PREFIX + PICO_PREFIX + "]+)"); //$NON-NLS-1$

    private static final long KILO = 1000L;
    private static final long MEGA = 1000000L;
    private static final long GIGA = 1000000000L;
    private static final long TERA = 1000000000000L;
    private static final long PETA = 1000000000000000L;

    private static final double MILLI = 0.001;
    private static final double MICRO = 0.000001;
    private static final double NANO = 0.000000001;
    private static final double PICO = 0.000000000001;

    /* Map of prefix to exponent */
    private static final Map<String, Integer> PREFIX_MAP = ImmutableMap.<String, Integer> builder()
            .put(KILO_PREFIX, +3)
            .put(MEGA_PREFIX, +6)
            .put(GIGA_PREFIX, +9)
            .put(TERA_PREFIX, +12)
            .put(PETA_PREFIX, +15)
            .put(MILLI_PREFIX, -3)
            .put(MICRO_PREFIX, -6)
            .put(MICRO_PREFIX2, -6)
            .put(NANO_PREFIX, -9)
            .put(PICO_PREFIX, -12)
            .build();

    private static final String PATTERN = "#.#"; //$NON-NLS-1$
    private final Locale fLocale;
    private final Format fFormat;
    private final double fFactor;

    /**
     * Default constructor.
     */
    public DecimalUnitFormat() {
        this(1.0, Locale.getDefault());
    }

    /**
     * Constructor with multiplication factor.
     *
     * @param factor
     *            Multiplication factor to apply to the value
     */
    public DecimalUnitFormat(double factor) {
        this(factor, Locale.getDefault());
    }

    /**
     * Constructor with locale.
     *
     * @param locale
     *            Locale
     * @since 4.3
     */
    public DecimalUnitFormat(Locale locale) {
        this(1.0, locale);
    }

    /**
     * Constructor with multiplication factor and locale.
     *
     * @param factor
     *            Multiplication factor to apply to the value
     * @param locale
     *            Locale
     * @since 4.3
     */
    public DecimalUnitFormat(double factor, Locale locale) {
        fLocale = locale;
        fFormat = new DecimalFormat(PATTERN, DecimalFormatSymbols.getInstance(locale));
        fFactor = factor;
    }

    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        if (obj instanceof Number) {
            Number num = (Number) obj;

            /* Apply the multiplication factor before formatting */
            double value = num.doubleValue() * fFactor;

            double abs = Math.abs(value);

            if (Double.isInfinite(value) || Double.isNaN(value) || abs < PICO) {
                return toAppendTo.append(fFormat.format(value));
            }

            if (abs >= 1) {
                if (abs > Long.MAX_VALUE) {
                    return toAppendTo.append(num);
                }
                if (abs >= PETA) {
                    return toAppendTo.append(fFormat.format(value / PETA)).append(' ').append(PETA_PREFIX);
                }
                if (abs >= TERA) {
                    return toAppendTo.append(fFormat.format(value / TERA)).append(' ').append(TERA_PREFIX);
                }
                if (abs >= GIGA) {
                    return toAppendTo.append(fFormat.format(value / GIGA)).append(' ').append(GIGA_PREFIX);
                }
                if (abs >= MEGA) {
                    return toAppendTo.append(fFormat.format(value / MEGA)).append(' ').append(MEGA_PREFIX);
                }
                if (abs >= KILO) {
                    return toAppendTo.append(fFormat.format(value / KILO)).append(' ').append(KILO_PREFIX);
                }

                return toAppendTo.append(fFormat.format(value));
            }

            if (abs < NANO) {
                return toAppendTo.append(fFormat.format(value * TERA)).append(' ').append(PICO_PREFIX);
            }
            if (abs < MICRO) {
                return toAppendTo.append(fFormat.format(value * GIGA)).append(' ').append(NANO_PREFIX);
            }
            if (abs < MILLI) {
                return toAppendTo.append(fFormat.format(value * MEGA)).append(' ').append(MICRO_PREFIX);
            }

            return toAppendTo.append(fFormat.format(value * KILO)).append(' ').append(MILLI_PREFIX);
        }

        throw new IllegalArgumentException("Cannot format given Object as a Number: " + obj); //$NON-NLS-1$
    }

    /**
     * @since 2.2
     */
    @Override
    public Number parseObject(String source, ParsePosition pos) {
        Number number = NumberFormat.getInstance(fLocale).parse(source, pos);
        if (number == null) {
            return null;
        }
        // Try to match the unit, if no match, assume no unit and don't
        // update the position
        String remaining = source.substring(pos.getIndex());
        Matcher matcher = UNIT_PATTERN.matcher(remaining);
        Integer exponent = null;
        if (matcher.find()) {
            String unitString = matcher.group();
            String prefix = matcher.group(1);
            exponent = PREFIX_MAP.get(prefix);
            pos.setIndex(pos.getIndex() + unitString.length());
        }
        if (exponent != null && Double.isFinite(number.doubleValue())) {
            // Calculate the value with exponent
            BigDecimal bd = new BigDecimal(number.toString());
            bd = bd.movePointRight(exponent.intValue());
            if (bd.remainder(BigDecimal.ONE).equals(BigDecimal.ZERO) &&
                    bd.abs().compareTo(new BigDecimal(Long.MAX_VALUE)) < 0) {
                return bd.longValue();
            }
            return bd.doubleValue();
        }
        return number;
    }
}
