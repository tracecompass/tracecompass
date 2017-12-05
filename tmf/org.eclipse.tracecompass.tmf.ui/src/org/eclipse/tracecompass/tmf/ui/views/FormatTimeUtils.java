/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.tmf.ui.views;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.eclipse.tracecompass.common.core.format.DecimalUnitFormat;
import org.eclipse.tracecompass.common.core.format.LongToPercentFormat;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimePreferences;

/**
 * Format time to be used in different views.
 *
 * @author Simon Delisle
 * @since 3.3
 */
public final class FormatTimeUtils {
    private FormatTimeUtils() {
        // do nothing
    }

    /** Time format for dates and timestamp */
    public enum TimeFormat {
        /** Relative to the start of the trace */
        RELATIVE,

        /**
         * Absolute timestamp (ie, relative to the Unix epoch)
         */
        CALENDAR,

        /**
         * Timestamp displayed as a simple number
         */
        NUMBER,

        /**
         * Timestamp displayed as cycles
         */
        CYCLES,

        /**
         * Value displayed as percentage
         * @since 4.0
         */
        PERCENTAGE
    }

    /**
     * Timestamp resolution
     */
    public enum Resolution {
        /** seconds */
        SECONDS,

        /** milliseconds */
        MILLISEC,

        /** microseconds */
        MICROSEC,

        /** nanoseconds */
        NANOSEC
    }

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
    private static final long HOURS_PER_DAY = 24;
    private static final long MIN_PER_HOUR = 60;
    private static final long SEC_PER_MIN = 60;
    private static final long SEC_IN_NS = 1000000000;
    private static final long MILLISEC_IN_NS = 1000000;

    /**
     * Update the time and date formats to use the current time zone
     */
    public static void updateTimeZone() {
        TimeZone timeZone = TmfTimePreferences.getTimeZone();
        TIME_FORMAT.setTimeZone(timeZone);
        DATE_FORMAT.setTimeZone(timeZone);
    }

    /**
     * Formats time in format: MM:SS:NNN
     *
     * @param time
     *            time
     * @param format
     *            0: MMMM:ss:nnnnnnnnn, 1: HH:MM:ss MMM.mmmm.nnn
     * @param resolution
     *            the resolution
     * @return the formatted time
     */
    public static String formatTime(long time, TimeFormat format, Resolution resolution) {
        switch (format) {
        case CALENDAR:
            return formatTimeAbs(time, resolution);
        case NUMBER:
            return NumberFormat.getInstance().format(time);
        case CYCLES:
            return NumberFormat.getInstance().format(time) + Messages.Utils_ClockCyclesUnit;
        case RELATIVE:
            return formatTimeRelative(time, resolution);
        case PERCENTAGE:
            return LongToPercentFormat.getInstance().format(time);
        default:
        }

        StringBuilder str = new StringBuilder();
        long t = time;
        boolean neg = t < 0;
        if (neg) {
            t = -t;
            str.append('-');
        }

        long sec = t / SEC_IN_NS;
        str.append(sec);
        String ns = formatNs(t, resolution);
        if (!ns.isEmpty()) {
            str.append('.');
            str.append(ns);
        }

        return str.toString();
    }

    /**
     * From input time in nanoseconds, convert to Date format YYYY-MM-dd
     *
     * @param absTime
     *            The source time, in ns
     * @return the formatted date
     */
    public static String formatDate(long absTime) {
        return DATE_FORMAT.format(new Date(absTime / MILLISEC_IN_NS));
    }

    /**
     * Formats time in ns to Calendar format: HH:MM:SS MMM.mmm.nnn
     *
     * @param time
     *            The source time, in ns
     * @param res
     *            The resolution to use
     * @return the formatted time
     */
    public static String formatTimeAbs(long time, Resolution res) {
        // format time from nanoseconds to calendar time HH:MM:SS
        String stime = TIME_FORMAT.format(new Date(time / MILLISEC_IN_NS));
        StringBuilder str = new StringBuilder(stime);
        String ns = formatNs(time, res);
        if (!ns.isEmpty()) {
            str.append('.');
            /*
             * append the Milliseconds, MicroSeconds and NanoSeconds as specified in the
             * Resolution
             */
            str.append(ns);
        }
        return str.toString();
    }

    /**
     * Formats time delta
     *
     * @param delta
     *            The time delta, in ns
     * @param format
     *            The time format to use
     * @param resolution
     *            The resolution to use
     * @return the formatted time delta
     */
    public static String formatDelta(long delta, TimeFormat format, Resolution resolution) {
        if (format == TimeFormat.CALENDAR) {
            return formatDeltaAbs(delta, resolution);
        }
        return formatTime(delta, format, resolution);
    }

    /**
     * Formats relative time to second
     *
     * @param time
     *            The relative time in ns
     * @param resolution
     *            The resolution to use
     * @return The formatted time in second
     */
    private static String formatTimeRelative(long time, Resolution resolution) {
        StringBuilder str = new StringBuilder();
        if (time < 0) {
            str.append('-');
        }

        long ns = Math.abs(time);
        long seconds = TimeUnit.NANOSECONDS.toSeconds(ns);
        str.append(seconds);
        str.append('.');
        // append the ms, us and ns as specified in the resolution
        str.append(formatNs(time, resolution));
        str.append('s');
        if (seconds == 0) {
            str.append(" ("); //$NON-NLS-1$
            str.append(new DecimalUnitFormat(1.0 / SEC_IN_NS).format(time));
            str.append("s)"); //$NON-NLS-1$
        }
        return str.toString();
    }

    /**
     * Formats time delta in ns to Calendar format, only formatting the years, days,
     * hours or minutes if necessary.
     *
     * @param delta
     *            The time delta, in ns
     * @param resolution
     *            The resolution to use
     * @return the formatted time delta
     */
    public static String formatDeltaAbs(long delta, Resolution resolution) {
        StringBuilder str = new StringBuilder();
        if (delta < 0) {
            str.append('-');
        }
        long ns = Math.abs(delta);
        long seconds = TimeUnit.NANOSECONDS.toSeconds(ns);
        long minutes = TimeUnit.NANOSECONDS.toMinutes(ns);
        long hours = TimeUnit.NANOSECONDS.toHours(ns);
        long days = TimeUnit.NANOSECONDS.toDays(ns);
        if (days > 0) {
            str.append(days);
            str.append("d "); //$NON-NLS-1$
        }
        if (hours > 0) {
            str.append(hours % HOURS_PER_DAY);
            str.append("h "); //$NON-NLS-1$
        }
        if (minutes > 0) {
            str.append(minutes % MIN_PER_HOUR);
            str.append("m "); //$NON-NLS-1$
        }
        str.append(seconds % SEC_PER_MIN);
        str.append('.');
        // append the ms, us and ns as specified in the resolution
        str.append(formatNs(delta, resolution));
        str.append("s"); //$NON-NLS-1$
        if (seconds == 0) {
            str.append(" ("); //$NON-NLS-1$
            str.append(new DecimalUnitFormat(1.0 / SEC_IN_NS).format(delta));
            str.append("s)"); //$NON-NLS-1$
        }
        return str.toString();
    }

    /**
     * Obtains the remainder fraction on unit Seconds of the entered value in
     * nanoseconds. e.g. input: 1241207054171080214 ns The number of fraction
     * seconds can be obtained by removing the last 9 digits: 1241207054 the
     * fractional portion of seconds, expressed in ns is: 171080214
     *
     * @param srcTime
     *            The source time in ns
     * @param res
     *            The Resolution to use
     * @return the formatted nanosec
     */
    public static String formatNs(long srcTime, Resolution res) {
        StringBuilder str = new StringBuilder();
        long ns = Math.abs(srcTime % SEC_IN_NS);
        String nanos = Long.toString(ns);
        str.append("000000000".substring(nanos.length())); //$NON-NLS-1$
        str.append(nanos);

        if (res == Resolution.MILLISEC) {
            return str.substring(0, 3);
        } else if (res == Resolution.MICROSEC) {
            return str.substring(0, 6);
        } else if (res == Resolution.NANOSEC) {
            return str.substring(0, 9);
        }
        return ""; //$NON-NLS-1$
    }

}
