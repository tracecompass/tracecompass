/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.presentation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tracecompass.internal.tmf.core.presentation.YAppearance;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * This is a base implementation of {@link IXYPresentationProvider}
 *
 * @author Yonni Chen
 * @since 3.1
 */
public class XYPresentationProvider implements IXYPresentationProvider {

    private static final List<String> SUPPORTED_STYLES = ImmutableList.of(
            YAppearance.Style.SOLID,
            YAppearance.Style.DASH,
            YAppearance.Style.DOT,
            YAppearance.Style.DASHDOT,
            YAppearance.Style.DASHDOTDOT);

    private static final List<String> SUPPORTED_TYPES = ImmutableList.of(
            YAppearance.Type.AREA,
            YAppearance.Type.BAR,
            YAppearance.Type.LINE,
            YAppearance.Type.SCATTER);

    /* Gets the default palette for available colors for XY series */
    private static final List<RGBAColor> COLOR_PALETTE = ColorPalette.getDefaultPalette();

    /* This map a series name and an IYAppearance */
    private final Map<String, IYAppearance> fYAppearances = new HashMap<>();

    @Override
    public synchronized IYAppearance getAppearance(String serieName, String seriesType, int width) {
        IYAppearance appearance = fYAppearances.get(serieName);
        if (appearance != null) {
            return appearance;
        }

        if(!SUPPORTED_TYPES.contains(seriesType)) {
            throw new UnsupportedOperationException("Series type: " + seriesType + " is not supported."); //$NON-NLS-1$ //$NON-NLS-2$
        }

        appearance = createAppearance(serieName, seriesType, width);
        fYAppearances.put(serieName, appearance);
        return appearance;
    }

    @Override
    public void clear() {
        fYAppearances.clear();
    }

    private IYAppearance createAppearance(String seriesName, String seriesType, int width) {
        RGBAColor color = generateColor();
        String style = generateStyle(seriesType);
        return new YAppearance(seriesName, seriesType, style, color, width);
    }

    /**
     * By using a Round Robin technique on all available colors supported, it will
     * return a color depending of the number of series already present
     *
     * @return An instance of {@link RGB} that represent the color
     */
    private RGBAColor generateColor() {
        return Iterables.get(COLOR_PALETTE, fYAppearances.keySet().size() % COLOR_PALETTE.size());
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
        if (!IYAppearance.Type.SCATTER.equals(type)) {
            return Iterables.get(SUPPORTED_STYLES, (fYAppearances.keySet().size() / (COLOR_PALETTE.size())) % SUPPORTED_STYLES.size());
        }
        return IYAppearance.Style.NONE;
    }
}
