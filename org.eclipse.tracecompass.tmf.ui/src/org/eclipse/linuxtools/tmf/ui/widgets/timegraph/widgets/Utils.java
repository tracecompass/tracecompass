/*****************************************************************************
 * Copyright (c) 2007, 2014 Intel Corporation, Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - Initial API and implementation
 *     Ruslan A. Scherbakov, Intel - Initial API and implementation
 *     Alvaro Sanchez-Leon - Udpated for TMF
 *     Patrick Tasse - Refactoring
 *     Marc-Andre Laperle - Add time zone preference
 *****************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimePreferences;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * General utilities and definitions used by the time graph widget
 *
 * @version 1.0
 * @author Alvaro Sanchez-Leon
 * @author Patrick Tasse
 */
public class Utils {

    private Utils() {
    }

    /** Time format for dates and timestamp */
    public enum TimeFormat {
        /** Relative to the start of the trace */
        RELATIVE,

        /**
         * Absolute timestamp (ie, relative to the Unix epoch)
         * @since 2.0
         */
        CALENDAR,

        /**
         * Timestamp displayed as a simple number
         * @since 2.0
         */
        NUMBER,

        /**
         * Timestamp displayed as cycles
         * @since 3.2
         */
        CYCLES
    }

    /**
     * Timestamp resolution
     */
    public static enum Resolution {
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
     *
     * @since 2.1
     */
    public static void updateTimeZone() {
        TimeZone timeZone = TmfTimePreferences.getInstance().getTimeZone();
        TIME_FORMAT.setTimeZone(timeZone);
        DATE_FORMAT.setTimeZone(timeZone);
    }

    static Rectangle clone(Rectangle source) {
        return new Rectangle(source.x, source.y, source.width, source.height);
    }

    /**
     * Initialize a Rectangle object to default values (all equal to 0)
     *
     * @param rect
     *            The Rectangle to initialize
     */
    public static void init(Rectangle rect) {
        rect.x = 0;
        rect.y = 0;
        rect.width = 0;
        rect.height = 0;
    }

    /**
     * Initialize a Rectangle object with all the given values
     *
     * @param rect
     *            The Rectangle object to initialize
     * @param x
     *            The X coordinate
     * @param y
     *            The Y coordinate
     * @param width
     *            The width of the rectangle
     * @param height
     *            The height of the rectangle
     */
    public static void init(Rectangle rect, int x, int y, int width, int height) {
        rect.x = x;
        rect.y = y;
        rect.width = width;
        rect.height = height;
    }

    /**
     * Initialize a Rectangle object to another existing Rectangle's values.
     *
     * @param rect
     *            The Rectangle to initialize
     * @param source
     *            The reference Rectangle to copy
     */
    public static void init(Rectangle rect, Rectangle source) {
        rect.x = source.x;
        rect.y = source.y;
        rect.width = source.width;
        rect.height = source.height;
    }

    /**
     * Reduce the size of a given rectangle by the given amounts.
     *
     * @param rect
     *            The rectangle to modify
     * @param x
     *            The reduction in width
     * @param y
     *            The reduction in height
     */
    public static void deflate(Rectangle rect, int x, int y) {
        rect.x += x;
        rect.y += y;
        rect.width -= x + x;
        rect.height -= y + y;
    }

    /**
     * Increase the size of a given rectangle by the given amounts.
     *
     * @param rect
     *            The rectangle to modify
     * @param x
     *            The augmentation in width
     * @param y
     *            The augmentation in height
     */
    public static void inflate(Rectangle rect, int x, int y) {
        rect.x -= x;
        rect.y -= y;
        rect.width += x + x;
        rect.height += y + y;
    }

    static void dispose(Color col) {
        if (null != col) {
            col.dispose();
        }
    }

    /**
     * Get the resulting color from a mix of two existing ones for a given
     * display.
     *
     * @param display
     *            The display device (which might affect the color conversion)
     * @param c1
     *            The first color
     * @param c2
     *            The second color
     * @param w1
     *            The gamma level for color 1
     * @param w2
     *            The gamma level for color 2
     * @return The resulting color
     */
    public static Color mixColors(Device display, Color c1, Color c2, int w1,
            int w2) {
        return new Color(display, (w1 * c1.getRed() + w2 * c2.getRed())
                / (w1 + w2), (w1 * c1.getGreen() + w2 * c2.getGreen())
                / (w1 + w2), (w1 * c1.getBlue() + w2 * c2.getBlue())
                / (w1 + w2));
    }

    /**
     * Get the system color with the given ID.
     *
     * @param id
     *            The color ID
     * @return The resulting color
     */
    public static Color getSysColor(int id) {
        Color col = Display.getCurrent().getSystemColor(id);
        return new Color(col.getDevice(), col.getRGB());
    }

    /**
     * Get the resulting color from a mix of two existing ones for the current
     * display.
     *
     * @param col1
     *            The first color
     * @param col2
     *            The second color
     * @param w1
     *            The gamma level for color 1
     * @param w2
     *            The gamma level for color 2
     * @return The resulting color
     */
    public static Color mixColors(Color col1, Color col2, int w1, int w2) {
        return mixColors(Display.getCurrent(), col1, col2, w1, w2);
    }

    /**
     * Draw text in a rectangle.
     *
     * @param gc
     *            The SWT GC object
     * @param text
     *            The text to draw
     * @param rect
     *            The rectangle object which is being drawn
     * @param transp
     *            If true the background will be transparent
     * @return The width of the written text
     */
    public static int drawText(GC gc, String text, Rectangle rect, boolean transp) {
        Point size = gc.stringExtent(text);
        gc.drawText(text, rect.x, rect.y, transp);
        return size.x;
    }

    /**
     * Draw text at a given location.
     *
     * @param gc
     *            The SWT GC object
     * @param text
     *            The text to draw
     * @param x
     *            The X coordinate of the starting point
     * @param y
     *            the Y coordinate of the starting point
     * @param transp
     *            If true the background will be transparent
     * @return The width of the written text
     */
    public static int drawText(GC gc, String text, int x, int y, boolean transp) {
        Point size = gc.stringExtent(text);
        gc.drawText(text, x, y, transp);
        return size.x;
    }

    /**
     * Draw text in a rectangle, trimming the text to prevent exceeding the specified width.
     *
     * @param gc
     *            The SWT GC object
     * @param text
     *            The string to be drawn
     * @param x
     *            The x coordinate of the top left corner of the rectangular area where the text is to be drawn
     * @param y
     *            The y coordinate of the top left corner of the rectangular area where the text is to be drawn
     * @param width
     *            The width of the area to be drawn
     * @param isCentered
     *            If <code>true</code> the text will be centered in the available width if space permits
     * @param isTransparent
     *            If <code>true</code> the background will be transparent, otherwise it will be opaque
     * @return The number of characters written
     *
     * @since 2.0
     */
    public static int drawText(GC gc, String text, int x, int y, int width, boolean isCentered, boolean isTransparent) {
        if (width < 1) {
            return 0;
        }

        int len = text.length();
        int textWidth = 0;
        boolean isReallyCentered = isCentered;
        int realX = x;

        while (len > 0) {
            textWidth = gc.stringExtent(text.substring(0, len)).x;
            if (textWidth <= width) {
                break;
            }
            isReallyCentered = false;
            len--;
        }
        if (len > 0) {
            if (isReallyCentered) {
                realX += (width - textWidth) / 2;
            }
            gc.drawText(text.substring(0, len), realX, y, isTransparent);
        }
        return len;
    }

    /**
     * Formats time in format: MM:SS:NNN
     *
     * @param time time
     * @param format  0: MMMM:ss:nnnnnnnnn, 1: HH:MM:ss MMM.mmmm.nnn
     * @param resolution the resolution
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
        default:
        }

        StringBuffer str = new StringBuffer();
        long t = time;
        boolean neg = t < 0;
        if (neg) {
            t = -t;
            str.append('-');
        }

        long sec = t / SEC_IN_NS;
        str.append(sec);
        String ns = formatNs(t, resolution);
        if (!ns.equals("")) { //$NON-NLS-1$
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
        String sdate = DATE_FORMAT.format(new Date(absTime / MILLISEC_IN_NS));
        return sdate;
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
        StringBuffer str = new StringBuffer();

        // format time from nanoseconds to calendar time HH:MM:SS
        String stime = TIME_FORMAT.format(new Date(time / MILLISEC_IN_NS));
        str.append(stime);
        str.append('.');
        // append the Milliseconds, MicroSeconds and NanoSeconds as specified in
        // the Resolution
        str.append(formatNs(time, res));
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
     * @since 3.2
     * @return the formatted time delta
     */
    public static String formatDelta(long delta, TimeFormat format, Resolution resolution) {
        if (format == TimeFormat.CALENDAR) {
            return formatDeltaAbs(delta, resolution);
        }
        return formatTime(delta, format, resolution);
    }

    /**
     * Formats time delta in ns to Calendar format, only formatting the years,
     * days, hours or minutes if necessary.
     *
     * @param delta
     *            The time delta, in ns
     * @param resolution
     *            The resolution to use
     * @return the formatted time delta
     * @since 3.2
     */
    public static String formatDeltaAbs(long delta, Resolution resolution) {
        StringBuffer str = new StringBuffer();
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
        StringBuffer str = new StringBuffer();
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

    /**
     * FIXME Currently does nothing.
     *
     * @param opt
     *            The option name
     * @param def
     *            The option value
     * @param min
     *            The minimal accepted value
     * @param max
     *            The maximal accepted value
     * @return The value that was read
     */
    public static int loadIntOption(String opt, int def, int min, int max) {
        return def;
    }

    /**
     * FIXME currently does nothing
     *
     * @param opt
     *            The option name
     * @param val
     *            The option value
     */
    public static void saveIntOption(String opt, int val) {
    }

    static ITimeEvent getFirstEvent(ITimeGraphEntry entry) {
        if (null == entry || ! entry.hasTimeEvents()) {
            return null;
        }
        Iterator<ITimeEvent> iterator = entry.getTimeEventsIterator();
        if (iterator != null && iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    /**
     * Gets the {@link ITimeEvent} at the given time from the given
     * {@link ITimeGraphEntry}.
     *
     * @param entry
     *            a {@link ITimeGraphEntry}
     * @param time
     *            a timestamp
     * @param n
     *            this parameter means: <list> <li>-1: Previous Event</li> <li>
     *            0: Current Event</li> <li>
     *            1: Next Event</li> <li>2: Previous Event when located in a non
     *            Event Area </list>
     * @return a {@link ITimeEvent}, or <code>null</code>.
     * @since 3.0
     */
    public static ITimeEvent findEvent(ITimeGraphEntry entry, long time, int n) {
        if (null == entry || ! entry.hasTimeEvents()) {
            return null;
        }
        Iterator<ITimeEvent> iterator = entry.getTimeEventsIterator();
        if (iterator == null) {
            return null;
        }
        ITimeEvent nextEvent = null;
        ITimeEvent currEvent = null;
        ITimeEvent prevEvent = null;

        while (iterator.hasNext()) {
            nextEvent = iterator.next();
            long nextStartTime = nextEvent.getTime();

            if (nextStartTime > time) {
                break;
            }

            if (currEvent == null || currEvent.getTime() != nextStartTime ||
                    (nextStartTime != time && currEvent.getDuration() != nextEvent.getDuration())) {
                prevEvent = currEvent;
                currEvent = nextEvent;
            }
        }

        if (n == -1) { //previous
            if (currEvent != null && currEvent.getTime() + currEvent.getDuration() >= time) {
                return prevEvent;
            }
            return currEvent;
        } else if (n == 0) { //current
            if (currEvent != null && currEvent.getTime() + currEvent.getDuration() >= time) {
                return currEvent;
            }
            return null;
        } else if (n == 1) { //next
            if (nextEvent != null && nextEvent.getTime() > time) {
                return nextEvent;
            }
            return null;
        } else if (n == 2) { //current or previous when in empty space
            return currEvent;
        }

        return null;
    }

    /**
     * Pretty-print a method signature.
     *
     * @param origSig
     *            The original signature
     * @return The pretty signature
     */
    public static String fixMethodSignature(String origSig) {
        String sig = origSig;
        int pos = sig.indexOf('(');
        if (pos >= 0) {
            String ret = sig.substring(0, pos);
            sig = sig.substring(pos);
            sig = sig + " " + ret; //$NON-NLS-1$
        }
        return sig;
    }

    /**
     * Restore an original method signature from a pretty-printed one.
     *
     * @param ppSig
     *            The pretty-printed signature
     * @return The original method signature
     */
    public static String restoreMethodSignature(String ppSig) {
        String ret = ""; //$NON-NLS-1$
        String sig = ppSig;

        int pos = sig.indexOf('(');
        if (pos >= 0) {
            ret = sig.substring(0, pos);
            sig = sig.substring(pos + 1);
        }
        pos = sig.indexOf(')');
        if (pos >= 0) {
            sig = sig.substring(0, pos);
        }
        String args[] = sig.split(","); //$NON-NLS-1$
        StringBuffer result = new StringBuffer("("); //$NON-NLS-1$
        for (int i = 0; i < args.length; i++) {
            String arg = args[i].trim();
            if (arg.length() == 0 && args.length == 1) {
                break;
            }
            result.append(getTypeSignature(arg));
        }
        result.append(")").append(getTypeSignature(ret)); //$NON-NLS-1$
        return result.toString();
    }

    /**
     * Get the mangled type information from an array of types.
     *
     * @param typeStr
     *            The types to convert. See method implementation for what it
     *            expects.
     * @return The mangled string of types
     */
    public static String getTypeSignature(String typeStr) {
        int dim = 0;
        String type = typeStr;
        for (int j = 0; j < type.length(); j++) {
            if (type.charAt(j) == '[') {
                dim++;
            }
        }
        int pos = type.indexOf('[');
        if (pos >= 0) {
            type = type.substring(0, pos);
        }
        StringBuffer sig = new StringBuffer(""); //$NON-NLS-1$
        for (int j = 0; j < dim; j++)
         {
            sig.append("["); //$NON-NLS-1$
        }
        if (type.equals("boolean")) { //$NON-NLS-1$
            sig.append('Z');
        } else if (type.equals("byte")) { //$NON-NLS-1$
            sig.append('B');
        } else if (type.equals("char")) { //$NON-NLS-1$
            sig.append('C');
        } else if (type.equals("short")) { //$NON-NLS-1$
            sig.append('S');
        } else if (type.equals("int")) { //$NON-NLS-1$
            sig.append('I');
        } else if (type.equals("long")) { //$NON-NLS-1$
            sig.append('J');
        } else if (type.equals("float")) { //$NON-NLS-1$
            sig.append('F');
        } else if (type.equals("double")) { //$NON-NLS-1$
            sig.append('D');
        } else if (type.equals("void")) { //$NON-NLS-1$
            sig.append('V');
        }
        else {
            sig.append('L').append(type.replace('.', '/')).append(';');
        }
        return sig.toString();
    }

    /**
     * Compare two doubles together.
     *
     * @param d1
     *            First double
     * @param d2
     *            Second double
     * @return 1 if they are different, and 0 if they are *exactly* the same.
     *         Because of the way doubles are stored, it's possible for the
     *         same number obtained in two different ways to actually look
     *         different.
     */
    public static int compare(double d1, double d2) {
        if (d1 > d2) {
            return 1;
        }
        if (d1 < d2) {
            return 1;
        }
        return 0;
    }

    /**
     * Compare two character strings alphabetically. This is simply a wrapper
     * around String.compareToIgnoreCase but that will handle cases where
     * strings can be null
     *
     * @param s1
     *            The first string
     * @param s2
     *            The second string
     * @return A number below, equal, or greater than zero if the first string
     *         is smaller, equal, or bigger (alphabetically) than the second
     *         one.
     */
    public static int compare(String s1, String s2) {
        if (s1 != null && s2 != null) {
            return s1.compareToIgnoreCase(s2);
        }
        if (s1 != null) {
            return 1;
        }
        if (s2 != null) {
            return -1;
        }
        return 0;
    }

    /**
     * Calculates the square of the distance between two points.
     *
     * @param x1
     *            x-coordinate of point 1
     * @param y1
     *            y-coordinate of point 1
     * @param x2
     *            x-coordinate of point 2
     * @param y2
     *            y-coordinate of point 2
     *
     * @return the square of the distance in pixels^2
     * @since 3.2
     */
    public static double distance2(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        int d2 = dx * dx + dy * dy;
        return d2;
    }

    /**
     * Calculates the distance between a point and a line segment. If the point
     * is in the perpendicular region between the segment points, return the
     * distance from the point to its projection on the segment. Otherwise
     * return the distance from the point to its closest segment point.
     *
     * @param px
     *            x-coordinate of the point
     * @param py
     *            y-coordinate of the point
     * @param x1
     *            x-coordinate of segment point 1
     * @param y1
     *            y-coordinate of segment point 1
     * @param x2
     *            x-coordinate of segment point 2
     * @param y2
     *            y-coordinate of segment point 2
     *
     * @return the distance in pixels
     * @since 3.2
     */
    public static double distance(int px, int py, int x1, int y1, int x2, int y2) {
        double length2 = distance2(x1, y1, x2, y2);
        if (length2 == 0) {
            return Math.sqrt(distance2(px, py, x1, y1));
        }
        // 'r' is the ratio of the position, between segment point 1 and segment
        // point 2, of the projection of the point on the segment
        double r = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / length2;
        if (r <= 0.0) {
            // the projection is before segment point 1, return distance from
            // the point to segment point 1
            return Math.sqrt(distance2(px, py, x1, y1));
        }
        if (r >= 1.0) {
            // the projection is after segment point 2, return distance from
            // the point to segment point 2
            return Math.sqrt(distance2(px, py, x2, y2));
        }
        // the projection is between the segment points, return distance from
        // the point to its projection on the segment
        int x = (int) (x1 + r * (x2 - x1));
        int y = (int) (y1 + r * (y2 - y1));
        return Math.sqrt(distance2(px, py, x, y));
    }
}
