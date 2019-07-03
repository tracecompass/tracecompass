/**********************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

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
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;

/**
 * Time format, it will take a time in nano seconds and convert it to a string
 * with 3 decimals max.
 *
 * examples:
 * <ul>
 * <li>100 -> "100 ns"</li>
 * <li>1001 -> "1.001 us" (mu)</li>
 * <li>314159264 -> "314.159 ms"</li>
 * <li>10000002000000 -> "1000.002 s"</li>
 * </ul>
 * @since 4.1
 */
public final class SubSecondTimeWithUnitFormat extends Format {

    private static final @NonNull Format INSTANCE = new SubSecondTimeWithUnitFormat();

    private static final long serialVersionUID = -5147827135781459548L;

    private static final Pattern UNIT_PATTERN = Pattern.compile("^[\\s]*([nmu\u00B5]?s)"); //$NON-NLS-1$

    private static final String SECONDS = "s"; //$NON-NLS-1$
    private static final String NANOSECONDS = "ns"; //$NON-NLS-1$
    private static final String MILLISECONDS = "ms"; //$NON-NLS-1$
    private static final String MICROSECONDS = "\u00B5" + SECONDS; //$NON-NLS-1$
    private static final String MICROSECONDS_FORMAT = "us"; //$NON-NLS-1$

    private static final int NANOS_PER_SEC = 1000000000;
    private static final int NANOS_PER_MILLI = 1000000;
    private static final int NANOS_PER_MICRO = 1000;

    private final DecimalFormat fDecimalFormat = new DecimalFormat("#.###"); //$NON-NLS-1$

    /**
     * Protected constructor
     */
    protected SubSecondTimeWithUnitFormat() {
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
    public Object parseObject(String source, ParsePosition pos) {
        Number number = NumberFormat.getInstance().parse(source, pos);
        if (number == null) {
            return null;
        }
        // Try to match the unit, if no match, assume nanoseconds and don't
        // update the position
        String remaining = source.substring(pos.getIndex()).toLowerCase();
        Matcher matcher = UNIT_PATTERN.matcher(remaining);
        long multiplier = 1;
        if (matcher.find()) {
            String unitString = matcher.group();
            String prefix = matcher.group(1);
            if (prefix.equals(SECONDS)) {
                multiplier = NANOS_PER_SEC;
            } else if (prefix.equals(MILLISECONDS)) {
                multiplier = NANOS_PER_MILLI;
            } else if (prefix.equals(MICROSECONDS)) {
                multiplier = NANOS_PER_MICRO;
            } else if (prefix.equals(MICROSECONDS_FORMAT)) {
                multiplier = NANOS_PER_MICRO;
            }

            // Update the position given the unit string
            pos.setIndex(pos.getIndex() + unitString.length());
        }
        // Get the number given the multiplier
        if (multiplier != 1 && Double.isFinite(number.doubleValue())) {
            BigDecimal bd = new BigDecimal(number.toString());
            bd = bd.multiply(BigDecimal.valueOf(multiplier));
            // This parses in ns, so just convert to the long value if within long range
            if (bd.abs().compareTo(new BigDecimal(Long.MAX_VALUE)) < 0) {
                return bd.longValue();
            }
            return bd.doubleValue();
        }
        return number;
    }

    @Override
    public StringBuffer format(@Nullable Object obj, @Nullable StringBuffer toAppendTo, @Nullable FieldPosition pos) {
        final @Nullable StringBuffer appender = toAppendTo;
        if ((obj != null) && (obj instanceof Number)) {
            double formattedTime =((Number) obj).doubleValue();
            if (Double.isNaN(formattedTime)) {
                return appender == null ? new StringBuffer() : NonNullUtils.checkNotNull(appender.append("---")); //$NON-NLS-1$
            }
            String unit = NANOSECONDS;
            double absFormattedTime = Math.abs(formattedTime);
            if (absFormattedTime >= NANOS_PER_SEC) {
                unit = SECONDS;
                formattedTime /= NANOS_PER_SEC;
            } else if (absFormattedTime >= NANOS_PER_MILLI) {
                unit = MILLISECONDS;
                formattedTime /= NANOS_PER_MILLI;
            } else if (absFormattedTime >= NANOS_PER_MICRO) {
                unit = MICROSECONDS;
                formattedTime /= NANOS_PER_MICRO;
            }
            if (formattedTime == 0) {
                return appender == null ? new StringBuffer() : NonNullUtils.checkNotNull(appender.append(0));
            }
            String timeString = unit.equals(NANOSECONDS) ? Long.toString((long) formattedTime) : fDecimalFormat.format(formattedTime);
            return appender == null ? new StringBuffer() : NonNullUtils.checkNotNull(appender.append(timeString).append(' ').append(unit));
        }
        return new StringBuffer();
    }
}