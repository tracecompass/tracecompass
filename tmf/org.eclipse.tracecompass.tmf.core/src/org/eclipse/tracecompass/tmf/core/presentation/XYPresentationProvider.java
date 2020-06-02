/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.presentation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.presentation.YAppearance;
import org.eclipse.tracecompass.tmf.core.dataprovider.X11ColorUtils;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Iterables;

/**
 * This is a base implementation of {@link IXYPresentationProvider}
 *
 * @author Yonni Chen
 * @since 4.0
 */
public class XYPresentationProvider implements IXYPresentationProvider {

    private static final List<String> SUPPORTED_STYLES = ImmutableList.of(
            StyleProperties.SeriesStyle.SOLID,
            StyleProperties.SeriesStyle.DASH,
            StyleProperties.SeriesStyle.DOT,
            StyleProperties.SeriesStyle.DASHDOT,
            StyleProperties.SeriesStyle.DASHDOTDOT);

    private static final List<String> SUPPORTED_TYPES = ImmutableList.of(
            StyleProperties.SeriesType.AREA,
            StyleProperties.SeriesType.BAR,
            StyleProperties.SeriesType.LINE,
            StyleProperties.SeriesType.SCATTER);

    private static final List<String> SUPPORTED_TICKS = ImmutableList.of(
            StyleProperties.SymbolType.DIAMOND,
            StyleProperties.SymbolType.CIRCLE,
            StyleProperties.SymbolType.SQUARE,
            StyleProperties.SymbolType.TRIANGLE,
            StyleProperties.SymbolType.INVERTED_TRIANGLE,
            StyleProperties.SymbolType.PLUS,
            StyleProperties.SymbolType.CROSS);

    /* Gets the default palette for available colors for XY series */
    private static final IPaletteProvider COLOR_PALETTE = DefaultColorPaletteProvider.INSTANCE;

    /* This map a series name and an IYAppearance */
    private final Map<Long, OutputElementStyle> fYAppearances = new HashMap<>();
    /* Temporary map of string to ID for backward-compatibility */
    private final Map<String, Long> fStringToId = new HashMap<>();
    private final AtomicLong fIdGenerator = new AtomicLong();

    @Deprecated
    @Override
    public synchronized IYAppearance getAppearance(String serieName, String seriesType, int width) {
        OutputElementStyle seriesStyle = getSeriesStyle(fStringToId.computeIfAbsent(serieName, n -> fIdGenerator.getAndIncrement()), seriesType, width);
        return typeToYAppearance(seriesStyle);
    }

    private IYAppearance typeToYAppearance(OutputElementStyle seriesStyle) {
        Map<String, Object> styleValues = seriesStyle.getStyleValues();
        String color = (String) styleValues.get(StyleProperties.COLOR);
        int alpha = (int) ((Double) styleValues.getOrDefault(StyleProperties.OPACITY, 1.0) * 255);
        RGBAColor rgba = (color == null ? generateColor() : Objects.requireNonNull(RGBAColor.fromString(color, alpha)));
        return new YAppearance(String.valueOf(styleValues.getOrDefault(StyleProperties.STYLE_NAME, "style name")), //$NON-NLS-1$
                String.valueOf(styleValues.getOrDefault(StyleProperties.SERIES_TYPE, StyleProperties.SeriesType.LINE)),
                String.valueOf(styleValues.getOrDefault(StyleProperties.SERIES_STYLE, StyleProperties.SeriesStyle.SOLID)),
                rgba,
                (int) styleValues.getOrDefault(StyleProperties.WIDTH, 1));
    }

    @Override
    public @NonNull OutputElementStyle getSeriesStyle(Long seriesId, @NonNull String type, int width) {
        OutputElementStyle appearance = fYAppearances.get(seriesId);
        if (appearance != null) {
            return appearance;
        }

        if(!SUPPORTED_TYPES.contains(type)) {
            throw new UnsupportedOperationException("Series type: " + type + " is not supported."); //$NON-NLS-1$ //$NON-NLS-2$
        }

        appearance = StyleProperties.SeriesType.SCATTER.equals(type) ? createScatter(seriesId, type, width) : createAppearance(seriesId, type, width);
        fYAppearances.put(seriesId, appearance);
        return appearance;
    }

    @Override
    public void clear() {
        fYAppearances.clear();
    }

    private OutputElementStyle createAppearance(Long seriesId, String seriesType, int width) {
        RGBAColor color = generateColor();
        Builder<String, Object> builder = ImmutableMap.builder();
        builder.put(StyleProperties.STYLE_NAME, seriesId);
        builder.put(StyleProperties.SERIES_TYPE, seriesType);
        builder.put(StyleProperties.SERIES_STYLE, generateStyle(seriesType));
        builder.put(StyleProperties.COLOR, X11ColorUtils.toHexColor(color.getRed(), color.getGreen(), color.getBlue()));
        builder.put(StyleProperties.WIDTH, width);
        return new OutputElementStyle(null, builder.build());
    }

    private OutputElementStyle createScatter(Long seriesId, String seriesType, int width) {
        RGBAColor color = generateColor();
        Builder<String, Object> builder = ImmutableMap.builder();
        builder.put(StyleProperties.STYLE_NAME, seriesId);
        builder.put(StyleProperties.SERIES_TYPE, seriesType);
        builder.put(StyleProperties.SERIES_STYLE, StyleProperties.SeriesStyle.NONE);
        builder.put(StyleProperties.COLOR, X11ColorUtils.toHexColor(color.getRed(), color.getGreen(), color.getBlue()));
        builder.put(StyleProperties.WIDTH, width);
        builder.put(StyleProperties.SYMBOL_TYPE, generateTickStyle(seriesType));
        return new OutputElementStyle(null, builder.build());
    }

    /**
     * By using a Round Robin technique on all available colors supported, it will
     * return a color depending of the number of series already present
     *
     * @return An instance of {@link RGB} that represent the color
     */
    private RGBAColor generateColor() {
        List<RGBAColor> colors = COLOR_PALETTE.get();
        return Iterables.get(colors, fYAppearances.keySet().size() % colors.size());
    }

    /**
     * By using a Round Robin technique on all available styles supported, it will
     * return a style depending of the number of series already present and
     * depending on the series type.
     *
     * @param type
     *            Series type. Indeed, we want to apply style only on type different
     *            than scatter
     * @return A string defining the style. See {@link IYAppearance.Style}'s strings
     */
    private String generateStyle(String type) {
        if (!StyleProperties.SeriesType.SCATTER.equals(type)) {
            int nbColor = COLOR_PALETTE.get().size();
            return Iterables.get(SUPPORTED_STYLES, (fYAppearances.keySet().size() / nbColor) % SUPPORTED_STYLES.size());
        }
        return StyleProperties.SeriesStyle.NONE;
    }

    private String generateTickStyle(String type) {
        if (StyleProperties.SeriesType.SCATTER.equals(type)) {
            return Iterables.get(SUPPORTED_TICKS, (fYAppearances.keySet().size() / (COLOR_PALETTE.get().size())) % SUPPORTED_TICKS.size());
        }
        return StyleProperties.SeriesStyle.NONE;
    }

    /**
     * Associate a style with the ID of an element
     *
     * @param id
     *            The ID of the element
     * @param style
     *            The style
     * @since 6.0
     */
    public void setStyle(long id, OutputElementStyle style) {
        fYAppearances.put(id, style);
    }
}
