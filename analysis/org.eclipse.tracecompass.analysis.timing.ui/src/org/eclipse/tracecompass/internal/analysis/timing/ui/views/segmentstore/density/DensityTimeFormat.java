/**********************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.density;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;

/**
 * Density format, it will take a time in NanoSeconds and convert it to a string
 * with 3 digits.
 *
 * @author Matthew Khouzam
 */
public final class DensityTimeFormat extends Format {


    private static final long serialVersionUID = -5147827135781459548L;

    private static final String SECONDS = "s"; //$NON-NLS-1$
    private static final String NANOSECONDS = "ns"; //$NON-NLS-1$
    private static final String MILLISECONDS = "ms"; //$NON-NLS-1$
    private static final String MICROSECONDS = "\u00B5" + SECONDS; //$NON-NLS-1$

    private static final int NANOS_PER_SEC = 1000000000;
    private static final int NANOS_PER_MILLI = 1000000;
    private static final int NANOS_PER_MICRO = 1000;

    private final DecimalFormat fDecimalFormat = new DecimalFormat("#.###"); //$NON-NLS-1$

    @Override
    public Object parseObject(@Nullable String source, @Nullable ParsePosition pos) {
        return source == null ? "" : source; //$NON-NLS-1$
    }

    @Override
    public StringBuffer format(@Nullable Object obj, @Nullable StringBuffer toAppendTo, @Nullable FieldPosition pos) {
        final @Nullable StringBuffer appender = toAppendTo;
        if ((obj != null) && (obj instanceof Double || obj instanceof Long)) {
            double formattedTime = obj instanceof Long ? ((Long) obj).doubleValue() : ((Double) obj).doubleValue();
            String unit = NANOSECONDS;
            if (formattedTime > NANOS_PER_SEC) {
                unit = SECONDS;
                formattedTime /= NANOS_PER_SEC;
            } else if (formattedTime > NANOS_PER_MILLI) {
                unit = MILLISECONDS;
                formattedTime /= NANOS_PER_MILLI;
            } else if (formattedTime > NANOS_PER_MICRO) {
                unit = MICROSECONDS;
                formattedTime /= NANOS_PER_MICRO;
            }
            String timeString = fDecimalFormat.format(formattedTime);
            return appender == null ? new StringBuffer() : NonNullUtils.checkNotNull(appender.append(timeString).append(' ').append(unit));
        }
        return new StringBuffer();
    }
}