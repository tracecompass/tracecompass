/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.presentation;

import java.util.Arrays;
import java.util.List;

/**
 * Regroups all available colors
 *
 * @author Yonni Chen
 */
public class ColorPalette {

    /**
     * Gets an instance of {@link RGBColor} that represents blue color
     */
    public static final RGBColor BLUE = new RGBColor(0, 0, 255);

    /**
     * Gets an instance of {@link RGBColor} that represents red color
     */
    public static final RGBColor RED = new RGBColor(255, 0, 0);

    /**
     * Gets an instance of {@link RGBColor} that represents gree color
     */
    public static final RGBColor GREEN = new RGBColor(0, 255, 0);

    /**
     * Gets an instance of {@link RGBColor} that represents magenta color
     */
    public static final RGBColor MAGENTA = new RGBColor(255, 0, 255);

    /**
     * Gets an instance of {@link RGBColor} that represents cyan color
     */
    public static final RGBColor CYAN = new RGBColor(0, 255, 255);

    /**
     * Gets an instance of {@link RGBColor} that represents dark blue color
     */
    public static final RGBColor DARK_BLUE = new RGBColor(0, 0, 128);

    /**
     * Gets an instance of {@link RGBColor} that represents dark red color
     */
    public static final RGBColor DARK_RED = new RGBColor(128, 0, 0);

    /**
     * Gets an instance of {@link RGBColor} that represents dark green color
     */
    public static final RGBColor DARK_GREEN = new RGBColor(0, 128, 0);

    /**
     * Gets an instance of {@link RGBColor} that represents dark magenta color
     */
    public static final RGBColor DARK_MAGENTA = new RGBColor(128, 0, 128);

    /**
     * Gets an instance of {@link RGBColor} that represents dark cyan color
     */
    public static final RGBColor DARK_CYAN = new RGBColor(0, 128, 125);

    /**
     * Gets an instance of {@link RGBColor} that represents dark yellow color
     */
    public static final RGBColor DARK_YELLOW = new RGBColor(128, 128, 0);

    /**
     * Gets an instance of {@link RGBColor} that represents black color
     */
    public static final RGBColor BLACK = new RGBColor(0, 0, 0);

    /**
     * Gets an instance of {@link RGBColor} that represents gray color
     */
    public static final RGBColor GRAY = new RGBColor(192, 192, 192);

    /**
     * Gets an instance of {@link RGBColor} that represents yellow color
     */
    public static final RGBColor YELLOW = new RGBColor(255, 255, 0);

    /**
     * Gets the list of default palette colors
     *
     * @return The list that contains all colors from this palette
     */
    public static List<RGBColor> getDefaultPalette() {
        return Arrays.asList(
                BLUE, RED, GREEN, MAGENTA, CYAN, DARK_BLUE, DARK_RED, DARK_GREEN,
                DARK_MAGENTA, DARK_CYAN, DARK_YELLOW, BLACK, GRAY, YELLOW);
    }

    private ColorPalette() {

    }
}
