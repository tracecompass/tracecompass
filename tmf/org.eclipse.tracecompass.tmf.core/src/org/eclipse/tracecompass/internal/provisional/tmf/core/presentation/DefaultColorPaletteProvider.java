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

import org.eclipse.jdt.annotation.NonNull;

/**
 * Regroups all available colors, the standard color scheme of Eclipse Trace Compass.
 *
 * @author Yonni Chen
 */
public final class DefaultColorPaletteProvider implements IPaletteProvider {

    /**
     * Gets an instance of {@link RGBAColor} that represents blue color
     */
    public static final RGBAColor BLUE = new RGBAColor(0, 0, 255);

    /**
     * Gets an instance of {@link RGBAColor} that represents red color
     */
    public static final RGBAColor RED = new RGBAColor(255, 0, 0);

    /**
     * Gets an instance of {@link RGBAColor} that represents gree color
     */
    public static final RGBAColor GREEN = new RGBAColor(0, 255, 0);

    /**
     * Gets an instance of {@link RGBAColor} that represents magenta color
     */
    public static final RGBAColor MAGENTA = new RGBAColor(255, 0, 255);

    /**
     * Gets an instance of {@link RGBAColor} that represents cyan color
     */
    public static final RGBAColor CYAN = new RGBAColor(0, 255, 255);

    /**
     * Gets an instance of {@link RGBAColor} that represents dark blue color
     */
    public static final RGBAColor DARK_BLUE = new RGBAColor(0, 0, 128);

    /**
     * Gets an instance of {@link RGBAColor} that represents dark red color
     */
    public static final RGBAColor DARK_RED = new RGBAColor(128, 0, 0);

    /**
     * Gets an instance of {@link RGBAColor} that represents dark green color
     */
    public static final RGBAColor DARK_GREEN = new RGBAColor(0, 128, 0);

    /**
     * Gets an instance of {@link RGBAColor} that represents dark magenta color
     */
    public static final RGBAColor DARK_MAGENTA = new RGBAColor(128, 0, 128);

    /**
     * Gets an instance of {@link RGBAColor} that represents dark cyan color
     */
    public static final RGBAColor DARK_CYAN = new RGBAColor(0, 128, 125);

    /**
     * Gets an instance of {@link RGBAColor} that represents dark yellow color
     */
    public static final RGBAColor DARK_YELLOW = new RGBAColor(128, 128, 0);

    /**
     * Gets an instance of {@link RGBAColor} that represents black color
     */
    public static final RGBAColor BLACK = new RGBAColor(0, 0, 0);

    /**
     * Gets an instance of {@link RGBAColor} that represents gray color
     */
    public static final RGBAColor GRAY = new RGBAColor(192, 192, 192);

    /**
     * Gets an instance of {@link RGBAColor} that represents yellow color
     */
    public static final RGBAColor YELLOW = new RGBAColor(255, 255, 0);

    private static final List<@NonNull RGBAColor> PALETTE = Arrays.asList(
            BLUE, RED, GREEN, MAGENTA, CYAN, DARK_BLUE, DARK_RED, DARK_GREEN,
            DARK_MAGENTA, DARK_CYAN, DARK_YELLOW, BLACK, GRAY, YELLOW);

    /**
     * Get the default default color palette provider
     */
    public static final IPaletteProvider INSTANCE = new DefaultColorPaletteProvider();

    private DefaultColorPaletteProvider() {
        // do nothing
    }

    @Override
    public List<@NonNull RGBAColor> get() {
        return PALETTE;
    }
}
