/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.colors;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.tracecompass.tmf.core.dataprovider.X11ColorUtils;

/**
 * Color utility to manipulate {@link RGB} and Hex string
 *
 * @author Simon Delisle
 * @since 5.1
 */
public final class ColorUtils {

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
     * Get an hex string from {@link RGB}.
     *
     * @param rgb
     *            RGB color
     * @return Hex string #rrggbb
     */
    public static String toHexColor(RGB rgb) {
        int r = rgb.red;
        int g = rgb.green;
        int b = rgb.blue;
        return String.format("#%02x%02x%02x", r, g, b); //$NON-NLS-1$
    }

    /**
     * Get {@link RGB} from an X11 color name as described in
     * {@link X11ColorUtils}.
     *
     * @param X11ColorName
     *            Name of the color
     * @return RGB color
     */
    public static RGB fromX11Color(String X11ColorName) {
        return fromHexColor(X11ColorUtils.toHexColor(X11ColorName));
    }
}
