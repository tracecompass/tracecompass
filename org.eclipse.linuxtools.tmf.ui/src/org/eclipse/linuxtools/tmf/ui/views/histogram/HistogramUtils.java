/*******************************************************************************
 * Copyright (c) 2009, 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   William Bourque - Initial API and implementation
 *   Francois Chouinard - Cleanup and refactoring
 *   Francois Chouinard - Moved from LTTng to TMF
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.histogram;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;

/**
 * Bunch of conversion utilities.
 *
 * @version 1.0
 * @author Francois Chouinard
 *         <p>
 */
public abstract class HistogramUtils {

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Format a long representing nanoseconds into a string of the form
     * "[seconds].[nanoseconds]" with the appropriate zero-padding.
     * <p>
     *
     * @param ns
     *            the timestamp in nanoseconds
     * @return the formatted string
     */
    public static String nanosecondsToString(long ns) {
        ns = Math.abs(ns);
        String time = Long.toString(ns);

        int length = time.length();
        if (time.length() > 9) {
            // Just insert the decimal dot
            time = time.substring(0, length - 9)
                    + "." + time.substring(length - 9); //$NON-NLS-1$
            return time;
        }

        // Zero-pad the value
        for (int i = length; i < 9; i++) {
            time = "0" + time; //$NON-NLS-1$
        }
        time = "0." + time; //$NON-NLS-1$
        return time;
    }

    /**
     * Convert a string representing a time to the corresponding long.
     * <p>
     *
     * @param time
     *            the string to convert
     * @return the corresponding nanoseconds value
     */
    public static long stringToNanoseconds(String time) {

        long result = 0L;
        StringBuffer buffer = new StringBuffer(time);

        try {
            int dot = buffer.indexOf("."); //$NON-NLS-1$

            // if no . was found, assume ns
            if (dot == -1) {
                // nanoseconds are the base unit.
                if (time.length() > 9) {
                    long nanos = Long
                            .parseLong(time.substring(time.length() - 9));
                    long secs = Long.parseLong(time.substring(0,
                            time.length() - 9));
                    result = (secs * 1000000000) + nanos;
                } else {
                    result = Long.parseLong(time);
                }

            } else {
                // Zero-pad the string for nanoseconds
                for (int i = buffer.length() - dot - 1; i < 9; i++) {
                    buffer.append("0"); //$NON-NLS-1$
                }

                // Remove the extra decimals if present
                int nbDecimals = buffer.substring(dot + 1).length();
                if (nbDecimals > 9) {
                    buffer.delete(buffer.substring(0, dot + 1 + 9).length(),
                            buffer.length());
                }

                // Do the conversion
                long seconds = (dot > 0) ? Long.parseLong(buffer.substring(0,
                        dot)) : 0;
                seconds = Math.abs(seconds);
                long nanosecs = Long.parseLong(buffer.substring(dot + 1));
                result = (seconds * 1000000000) + nanosecs;
            }
        } catch (NumberFormatException e) {
            // TODO: Find something interesting to say
        }

        return result;
    }

    /**
     * Calculate the width of a String.
     * <p>
     *
     * @param parent
     *            The control used as reference
     * @param text
     *            The Text to measure
     *
     * @return The result size
     */
    public static int getTextSizeInControl(Composite parent, String text) {

        GC controlGC = new GC(parent);

        int textSize = 0;
        for (int pos = 0; pos < text.length(); pos++) {
            textSize += controlGC.getAdvanceWidth(text.charAt(pos));
        }
        // Add an extra space
        textSize += controlGC.getAdvanceWidth(' ');

        controlGC.dispose();

        return textSize;
    }

}
