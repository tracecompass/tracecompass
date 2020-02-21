/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties.BorderStyle;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.ui.colors.ColorUtils;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEventStyleStrings;

/**
 * Temporary class to convert an old style map using deprecated
 * {@link ITimeEventStyleStrings} to the new style properties in
 * {@link StyleProperties}.
 *
 * @author Geneviève Bastien
 */
public class StylePropertiesUtils {

    /**
     * Transforms deprecated {@link ITimeEventStyleStrings} methods to the
     * proper styles. It will create the new properties out of the deprecated
     * ones only if such properties do not exist yet.
     *
     * @param styleMap
     *            The original style map.
     * @return A new map of style. This map is not immutable.
     */
    @SuppressWarnings("deprecation")
    public static @NonNull Map<@NonNull String, @NonNull Object> updateEventStyleProperties(@Nullable Map<String, Object> styleMap) {
        if (styleMap == null) {
            return new HashMap<>();
        }
        @NonNull Map<@NonNull String, @NonNull Object> updatedStyles = new HashMap<>(styleMap);
        Object object = styleMap.get(ITimeEventStyleStrings.label());
        if (object != null && !styleMap.containsKey(StyleProperties.STYLE_NAME)) {
            updatedStyles.put(StyleProperties.STYLE_NAME, object);
        }
        object = styleMap.get(ITimeEventStyleStrings.heightFactor());
        if (object != null && !styleMap.containsKey(StyleProperties.HEIGHT)) {
            updatedStyles.put(StyleProperties.HEIGHT, object);
        }
        object = styleMap.get(ITimeEventStyleStrings.fillStyle());
        if (object != null && !styleMap.containsKey(StyleProperties.LINEAR_GRADIENT)) {
            if (object.equals(ITimeEventStyleStrings.gradientColorFillStyle())) {
                updatedStyles.put(StyleProperties.LINEAR_GRADIENT, true);
            } else {
                updatedStyles.put(StyleProperties.LINEAR_GRADIENT, false);
            }
        }
        object = styleMap.get(ITimeEventStyleStrings.fillColor());
        if (object != null && (object instanceof Integer)) {
            RGBAColor rgba = new RGBAColor((int) object);
            String hexColor = ColorUtils.toHexColor(rgba.getRed(), rgba.getGreen(), rgba.getBlue());
            float opacity = (float) rgba.getAlpha() / 255;
            if (!styleMap.containsKey(StyleProperties.BACKGROUND_COLOR)) {
                updatedStyles.put(StyleProperties.BACKGROUND_COLOR, hexColor);
            }
            if (!styleMap.containsKey(StyleProperties.COLOR)) {
                updatedStyles.put(StyleProperties.COLOR, hexColor);
            }
            if (!styleMap.containsKey(StyleProperties.OPACITY)) {
                updatedStyles.put(StyleProperties.OPACITY, opacity);
            }
        }
        object = styleMap.get(ITimeEventStyleStrings.fillColorEnd());
        if (object != null && !styleMap.containsKey(StyleProperties.LINEAR_GRADIENT_COLOR_END)) {
            if (object instanceof Integer) {
                RGBAColor rgba = new RGBAColor((int) object);
                updatedStyles.put(StyleProperties.LINEAR_GRADIENT_COLOR_END, ColorUtils.toHexColor(rgba.getRed(), rgba.getGreen(), rgba.getBlue()));
            }
        }
        object = styleMap.get(ITimeEventStyleStrings.borderEnable());
        if (object != null && !styleMap.containsKey(StyleProperties.BORDER_STYLE)) {
            if (object instanceof Boolean && (Boolean) object) {
                updatedStyles.put(StyleProperties.BORDER_STYLE, BorderStyle.SOLID);
            } else {
                updatedStyles.put(StyleProperties.BORDER_STYLE, BorderStyle.NONE);
            }
        }
        object = styleMap.get(ITimeEventStyleStrings.borderColor());
        if (object != null && !styleMap.containsKey(StyleProperties.BORDER_COLOR)) {
            if (object instanceof Integer) {
                RGBAColor rgba = new RGBAColor((int) object);
                updatedStyles.put(StyleProperties.BORDER_COLOR, ColorUtils.toHexColor(rgba.getRed(), rgba.getGreen(), rgba.getBlue()));
            }
        }
        object = styleMap.get(ITimeEventStyleStrings.borderThickness());
        if (object != null && !styleMap.containsKey(StyleProperties.BORDER_WIDTH)) {
            updatedStyles.put(StyleProperties.BORDER_WIDTH, object);
        }
        object = styleMap.get(ITimeEventStyleStrings.symbolStyle());
        if (object != null && !styleMap.containsKey(StyleProperties.SYMBOL_TYPE)) {
            String symbolType = ITimeEventStyleStrings.SYMBOL_TYPES.get(object);
            if (symbolType != null) {
                updatedStyles.put(StyleProperties.SYMBOL_TYPE, symbolType);
            }
        }
        return updatedStyles;
    }

}
