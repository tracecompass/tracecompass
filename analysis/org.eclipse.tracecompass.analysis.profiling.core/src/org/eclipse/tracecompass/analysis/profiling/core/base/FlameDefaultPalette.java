/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.profiling.core.base;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.dataprovider.X11ColorUtils;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.presentation.IPaletteProvider;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.core.presentation.RotatingPaletteProvider;

import com.google.common.collect.ImmutableMap;

/**
 * Class to manage the colors of the flame chart and flame graph views
 *
 * @author Geneviève Bastien
 * @since 2.1
 */
public final class FlameDefaultPalette {

    /**
     * The state index for the multiple state
     */
    private static final int NUM_COLORS = 360;

    private static final @NonNull Map<@NonNull String, @NonNull OutputElementStyle> STYLES;

    static {
        IPaletteProvider palette = new RotatingPaletteProvider.Builder().setNbColors(NUM_COLORS).build();
        int i = 0;
        ImmutableMap.Builder<@NonNull String, @NonNull OutputElementStyle> builder = new ImmutableMap.Builder<>();
        for (RGBAColor color : palette.get()) {
            builder.put(String.valueOf(i), new OutputElementStyle(null, ImmutableMap.of(
                    StyleProperties.STYLE_NAME, String.valueOf(i),
                    StyleProperties.BACKGROUND_COLOR, X11ColorUtils.toHexColor(color.getRed(), color.getGreen(), color.getBlue()))));
            i++;
        }
        STYLES = builder.build();
    }

    private FlameDefaultPalette() {
        // Do nothing
    }

    /**
     * Get the map of all styles provided by this palette. These are the base
     * styles, mapping to the key for each style. Styles for object can then
     * refer to those base styles as parents.
     *
     * @return The map of style name to full style description.
     */
    public static @NonNull Map<@NonNull String, @NonNull OutputElementStyle> getStyles() {
        return STYLES;
    }

    /**
     * Get the style element for a given value
     *
     * @param callsite
     *            The value to get an element for
     * @return The output style
     */
    public static OutputElementStyle getStyleFor(Object callsite) {
        int index = Math.abs(31 * callsite.hashCode()) % NUM_COLORS;
        return Objects.requireNonNull(STYLES.get(String.valueOf(index)));
    }

}