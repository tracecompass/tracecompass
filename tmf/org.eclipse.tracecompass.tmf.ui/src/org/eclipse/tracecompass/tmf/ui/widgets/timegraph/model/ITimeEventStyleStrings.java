/*******************************************************************************
 * Copyright (c) 2017, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties.BorderStyle;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties.SymbolType;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider;

import com.google.common.collect.ImmutableMap;

/**
 * <p>
 * <em>Time event styles</em>, this is for reference purposes. Many values will
 * be unsupported.
 * </p>
 * <p>
 * Special care is needed when populating the map as it is untyped. The API is
 * as follows
 * </p>
 * <ul>
 * <li>{@link #label()} a <em>String</em> to show in the legend</li>
 * <li>{@link #fillStyle()} can be {@link #solidColorFillStyle()},
 * {@link #gradientColorFillStyle()} or {@link #hatchPatternFillStyle()}.</li>
 * <li>{@link #heightFactor()} a <em>Float</em> between 0 and 1.0f</li>
 * <li>{@link #fillColor()} an <em>integer</em> encoding RGBA over 4 bytes (1
 * byte red, 1 byte green, 1 byte blue, 1 byte alpha)</li>
 * <li>{@link #fillColorEnd()} an <em>integer</em> encoding RGBA over 4 bytes (1
 * byte red, 1 byte green, 1 byte blue, 1 byte alpha)</li>
 * <li>{@link #borderColor()} an <em>integer</em> encoding RGBA over 4 bytes (1
 * byte red, 1 byte green, 1 byte blue, 1 byte alpha)</li>
 * <li>{@link #borderEnable()} a <em>boolean</em></li>
 * <li>{@link #borderThickness()} an <em>integer</em></li>
 * </ul>
 *
 * @author Matthew Khouzam
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 3.0
 */
@NonNullByDefault
public interface ITimeEventStyleStrings {

    /**
     * Mapping of {@link #symbolStyle()} to {@link StyleProperties#SYMBOL_TYPE}
     *
     * @since 5.2
     */
    Map<String, String> SYMBOL_TYPES = ImmutableMap.<String, String>builder()
            .put(StyleProperties.SymbolType.DIAMOND, SymbolType.DIAMOND)
            .put(StyleProperties.SymbolType.CIRCLE, SymbolType.CIRCLE)
            .put(StyleProperties.SymbolType.SQUARE, SymbolType.SQUARE)
            .put(StyleProperties.SymbolType.TRIANGLE, SymbolType.TRIANGLE)
            .put(StyleProperties.SymbolType.INVERTED_TRIANGLE, SymbolType.INVERTED_TRIANGLE)
            .put(StyleProperties.SymbolType.CROSS, SymbolType.CROSS)
            .put(StyleProperties.SymbolType.PLUS, SymbolType.PLUS)
            .build();

    /**
     * The label to display in the legend
     *
     * @return the key to get the value
     * @deprecated Use {@link StyleProperties#STYLE_NAME} instead
     */
    @Deprecated
    static String label() {
        return ".label"; //$NON-NLS-1$
    }

    /**
     * Height factor, can be between 0.0 and 1.0f.
     *
     * @return the key to get the value
     * @deprecated Use {@link StyleProperties#HEIGHT} instead
     */
    @Deprecated
    static String heightFactor() {
        return ".height.factor"; //$NON-NLS-1$
    }

    /**
     * Fill style, can be {@link #solidColorFillStyle()},
     * {@link #gradientColorFillStyle()} or {@link #hatchPatternFillStyle()}
     *
     * @return the key to get the value
     * @deprecated For {@link #solidColorFillStyle()}, set
     *             {@link StyleProperties#LINEAR_GRADIENT} to
     *             false, for {@link #gradientColorFillStyle()}, set
     *             {@link StyleProperties#LINEAR_GRADIENT} to true
     *             and hatch pattern is not supported.
     */
    @Deprecated
    static String fillStyle() {
        return ".fill";//$NON-NLS-1$
    }

    /**
     * Color fill style, this is a solid color, so it should make an event that
     * is uniformly filled with a color. The color is defined in the
     * {@link #fillColor()} parameter.
     *
     * @see #fillStyle()
     *
     * @return the color fill style
     * @deprecated For this style, set
     *             {@link StyleProperties#LINEAR_GRADIENT} to false
     */
    @Deprecated
    static String solidColorFillStyle() {
        return "color"; //$NON-NLS-1$
    }

    /**
     * Color fill style, this is a gradient color, it should make an event that
     * transitions from {@link #fillColor()} to {@link #fillColorEnd()}.
     *
     * @see #fillStyle()
     *
     * @return the color fill style
     * @deprecated For this style, set
     *             {@link StyleProperties#LINEAR_GRADIENT} to true
     */
    @Deprecated
    static String gradientColorFillStyle() {
        return "gradient"; //$NON-NLS-1$
    }

    /**
     * Color fill style, this is a hatch pattern, it should make an event that
     * has a hatch pattern with {@link #fillColor()} and
     * {@link #fillColorEnd()}.
     *
     * @see #fillStyle()
     * @return the color fill style
     * @deprecated not supported
     */
    @Deprecated
    static String hatchPatternFillStyle() {
        return "hatch"; //$NON-NLS-1$
    }

    /**
     * Fill color, used in all styles except for image.
     *
     * @return the key to get the value
     * @deprecated Use {@link StyleProperties#BACKGROUND_COLOR} instead
     */
    @Deprecated
    static String fillColor() {
        return ".fill.color";//$NON-NLS-1$
    }

    /**
     * Second fill color, used in gradients
     *
     * @return the key to get the value
     * @deprecated Use
     *             {@link StyleProperties#LINEAR_GRADIENT_COLOR_END}
     *             instead
     */
    @Deprecated
    static String fillColorEnd() {
        return ".fill.color_end";//$NON-NLS-1$
    }

    /**
     * Shadow the time event
     *
     * @return the key to get the value
     * @deprecated Not supported
     */
    @Deprecated
    static String shadowEnabled() {
        return ".shadow.enable";//$NON-NLS-1$
    }

    /**
     * Border
     *
     * @return the key to get the value
     * @deprecated To disable borders, set
     *             {@link StyleProperties#BORDER_STYLE} to
     *             {@link BorderStyle#NONE}, to enable, set it to any other
     *             value in {@link BorderStyle}
     */
    @Deprecated
    static String borderEnable() {
        return ".border.enable";//$NON-NLS-1$
    }

    /**
     * Border thickness
     *
     * @return the key to get the value
     * @deprecated Use {@link StyleProperties#BORDER_WIDTH} instead
     */
    @Deprecated
    static String borderThickness() {
        return ".border.weight";//$NON-NLS-1$
    }

    /**
     * Border color
     *
     * @return the key to get the value
     * @deprecated Use {@link StyleProperties#BORDER_COLOR} instead
     */
    @Deprecated
    static String borderColor() {
        return ".border.color";//$NON-NLS-1$
    }

    /**
     * Item property. Possible values are
     * {@link ITimeEventStyleStrings#stateType()} or
     * {@link ITimeEventStyleStrings#linkType()}
     *
     * @return The key to get the item property of a state item
     * @since 4.0
     */
    static String itemTypeProperty() {
        return ".type"; //$NON-NLS-1$
    }

    /**
     * Indicate that the item type is a STATE
     *
     * @return The state item type value
     * @since 4.0
     */
    static String stateType() {
        return ".type.state"; //$NON-NLS-1$
    }

    /**
     * Indicate that the item type is a LINK
     *
     * @return The link item type value
     * @since 4.0
     */
    static String linkType() {
        return ".type.link"; //$NON-NLS-1$
    }

    /**
     * The event is annotated. When this is set, the label will not be drawn and
     * {@link ITimeGraphPresentationProvider#postDrawEvent} will not be called
     *
     * @return the key to get the annotated value
     * @since 4.0
     */
    static String annotated() {
        return ".annotated"; //$NON-NLS-1$
    }

    /**
     * Indicate that the item has a symbol style associated to it. Values
     * associated to it are defined in
     * {@link org.eclipse.tracecompass.tmf.core.model.StyleProperties.SymbolType}
     *
     * @return the key associated to the symbol style.
     * @since 4.1
     * @deprecated Use {@link StyleProperties#SYMBOL_TYPE} instead
     */
    @Deprecated
    static String symbolStyle() {
        return ".symbol.style"; //$NON-NLS-1$
    }
}
