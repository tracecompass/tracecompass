/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.colors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tracecompass.tmf.core.dataprovider.X11ColorUtils;

/**
 * Color utility to manipulate {@link RGB} and Hex string
 *
 * @author Simon Delisle
 * @since 5.2
 */
public final class ColorUtils {

    private static final String HEX_COLOR_FORMAT = "#%02x%02x%02x"; //$NON-NLS-1$
    private static final String HEX_COLOR_REGEX = "#[A-Fa-f0-9]{6}"; //$NON-NLS-1$

    private ColorUtils() {
        // Do nothing
    }

    /**
     * Get {@link RGB} from hex string. The color string should match the
     * following regex: "#[A-Fa-f0-9]{6}"
     *
     * @param color
     *            Color in hex representation
     * @return RGB color
     */
    public static RGB fromHexColor(String color) {
        if (color != null && color.matches(HEX_COLOR_REGEX)) {
            return new RGB(Integer.valueOf(color.substring(1, 3), 16),
                    Integer.valueOf(color.substring(3, 5), 16),
                    Integer.valueOf(color.substring(5, 7), 16));
        }
        return null;
    }

    /**
     * Get an RGB hex string from a {@link RGB}.
     *
     * @param rgb
     *            RGB color
     * @return The hexadecimal string in format #rrggbb
     */
    public static @NonNull String toHexColor(RGB rgb) {
        int r = rgb.red;
        int g = rgb.green;
        int b = rgb.blue;
        return String.format(HEX_COLOR_FORMAT, r, g, b);
    }

    /**
     * Get an RGB hex string from the RGB values.
     *
     * @param red
     *            The red value, should be between 0 and 255
     * @param green
     *            The green value, should be between 0 and 255
     * @param blue
     *            The blue value, should be between 0 and 255
     * @return The hexadecimal string in format #rrggbb
     */
    public static @NonNull String toHexColor(int red, int green, int blue) {
        return String.format(HEX_COLOR_FORMAT, Math.abs(red % 256), Math.abs(green % 256), Math.abs(blue % 256));
    }

    /**
     * Get {@link RGB} from an X11 color name as described in
     * {@link X11ColorUtils}.
     *
     * @param x11ColorName
     *            Name of the color
     * @return RGB color
     */
    public static RGB fromX11Color(String x11ColorName) {
        return (x11ColorName) == null ? null : fromHexColor(X11ColorUtils.toHexColor(x11ColorName));
    }
}
