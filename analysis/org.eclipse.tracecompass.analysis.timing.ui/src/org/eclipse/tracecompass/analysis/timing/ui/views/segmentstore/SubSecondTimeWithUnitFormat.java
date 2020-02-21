/**********************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

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
 * <li>314159264 -> "312.159 ms"</li>
 * <li>10000002000000 -> "1000.002 s"</li>
 * </ul>
 * @since 4.0
 * @deprecated Formatter relocated in {@link org.eclipse.tracecompass.common.core.format.SubSecondTimeWithUnitFormat}
 */
@Deprecated
public final class SubSecondTimeWithUnitFormat extends Format {

    private static final long serialVersionUID = -5147827135781459548L;

    private static final String SECONDS = "s"; //$NON-NLS-1$
    private static final String NANOSECONDS = "ns"; //$NON-NLS-1$
    private static final String MILLISECONDS = "ms"; //$NON-NLS-1$
    private static final String MICROSECONDS = "\u00B5" + SECONDS; //$NON-NLS-1$

    private static final int NANOS_PER_SEC = 1000000000;
    private static final int NANOS_PER_MILLI = 1000000;
    private static final int NANOS_PER_MICRO = 1000;

    private final DecimalFormat fDecimalFormat = new DecimalFormat("#.000"); //$NON-NLS-1$

    @Override
    public Object parseObject(@Nullable String source, @Nullable ParsePosition pos) {
        return source == null ? "" : source; //$NON-NLS-1$
    }

    @Override
    public StringBuffer format(@Nullable Object obj, @Nullable StringBuffer toAppendTo, @Nullable FieldPosition pos) {
        final @Nullable StringBuffer appender = toAppendTo;
        if ((obj != null) && (obj instanceof Double || obj instanceof Long)) {
            double formattedTime = obj instanceof Long ? ((Long) obj).doubleValue() : ((Double) obj).doubleValue();
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