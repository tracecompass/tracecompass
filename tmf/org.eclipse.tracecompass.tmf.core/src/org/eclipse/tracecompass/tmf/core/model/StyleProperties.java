/**********************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model;

/**
 * Constants that defines different properties that can be used in
 * {@link OutputElementStyle}
 *
 * @author Simon Delisle
 * @since 5.1
 */
public final class StyleProperties {

    private StyleProperties() {
    }

    // Background

    /**
     * Background color, specified by an hex string (#rrggbb).
     * <p>
     * Default: "#000000"
     */
    public static final String BACKGROUND_COLOR = "background-color"; //$NON-NLS-1$

    /**
     * Linear gradient applied to the background color. Possible values: "true"
     * or "false".
     * <p>
     * Default: "false"
     */
    public static final String LINEAR_GRADIENT = "linear-gradient"; //$NON-NLS-1$

    /**
     * End color of the gradient, specified as an hex string (#rrggbb). The
     * start color is set with {@link #BACKGROUND_COLOR}. Used when the
     * {@link #LINEAR_GRADIENT} is true.
     * <p>
     * Default: "#ffffff"
     */
    public static final String LINEAR_GRADIENT_COLOR_END = "linear-gradient-color-end"; //$NON-NLS-1$

    /**
     * Direction of the linear gradient. Possible values:
     * {@link LinearGradientDirection}.
     * <p>
     * Default: {@link LinearGradientDirection#VERTICAL}
     */
    public static final String LINEAR_GRADIENT_DIRECTION = "linear-gradient-direction"; //$NON-NLS-1$

    /**
     * Linear gradient direction values.
     *
     * @noimplement This interface is not intended to be implemented by clients.
     */
    public interface LinearGradientDirection {
        /**
         * Horizontal (left to right)
         */
        String HORIZONTAL = "horizontal"; //$NON-NLS-1$
        /**
         * Vertical (top to bottom)
         */
        String VERTICAL = "vertical"; //$NON-NLS-1$
    }

    /**
     * Opacity level of an element between 0.0 and 1.0 (float) where 0.0 is
     * transparent and 1.0 is fully opaque.
     * <p>
     * Default: 1.0
     */
    public static final String OPACITY = "opacity"; //$NON-NLS-1$

    // Border

    /**
     * Border style as string. Possible values: {@link BorderStyle}.
     * <p>
     * Default: {@link BorderStyle#NONE}
     */
    public static final String BORDER_STYLE = "border-style"; //$NON-NLS-1$

    /**
     * Border style values.
     *
     * @noimplement This interface is not intended to be implemented by clients.
     */
    public interface BorderStyle {
        /**
         * No border
         */
        String NONE = "none"; //$NON-NLS-1$
        /**
         * Dotted border
         */
        String DOTTED = "dotted"; //$NON-NLS-1$
        /**
         * Dashed border
         */
        String DASHED = "dashed"; //$NON-NLS-1$
        /**
         * Solid border
         */
        String SOLID = "solid"; //$NON-NLS-1$
        /**
         * Double border
         */
        String DOUBLE = "double"; //$NON-NLS-1$
    }

    /**
     * Border color, specified as an hex string (#rrggbb). Used when
     * {@link #BORDER_STYLE} is other than {@link BorderStyle#NONE}.
     * <p>
     * Default: "#000000"
     */
    public static final String BORDER_COLOR = "border-color"; //$NON-NLS-1$

    /**
     * Border radius of rounded corners between 0.0 and 1.0 (float) where 0.0 is
     * not rounded and 1.0 is an arc radius equal to half the element height.
     * The radius applies to the background and to the border, if there is one.
     * <p>
     * Default: 0.5
     */
    public static final String BORDER_RADIUS = "border-radius"; //$NON-NLS-1$

    /**
     * Border width in pixels (int). Used when {@link #BORDER_STYLE} is other
     * than {@link BorderStyle#NONE}.
     * <p>
     * Default: 1
     */
    public static final String BORDER_WIDTH = "border-width"; //$NON-NLS-1$

    // Text

    /**
     * Foreground color, specified as an hex string (#rrggbb).
     * <p>
     * Default: "#000000" or, when the element has a background color, a
     * distinct color from this background color
     */
    public static final String COLOR = "color"; //$NON-NLS-1$

    /**
     * Text direction as a string. Possible values: {@link TextDirection}.
     * <p>
     * Default: @link {@link TextDirection#LTR}
     */
    public static final String TEXT_DIRECTION = "direction"; //$NON-NLS-1$

    /**
     * Text direction values.
     *
     * @noimplement This interface is not intended to be implemented by clients.
     */
    public interface TextDirection {
        /**
         * Left to right
         */
        String LTR = "ltr"; //$NON-NLS-1$
        /**
         * Right to left
         */
        String RTL = "rtl"; //$NON-NLS-1$
    }

    /**
     * Text alignment as a string. Possible values: {@link TextAlign}.
     * <p>
     * Default: {@link TextAlign#CENTER}
     */
    public static final String TEXT_ALIGN = "text-align"; //$NON-NLS-1$

    /**
     * Text alignment values.
     *
     * @noimplement This interface is not intended to be implemented by clients.
     */
    public interface TextAlign {
        /**
         * Center
         */
        String CENTER = "center"; //$NON-NLS-1$
        /**
         * Left
         */
        String LEFT = "left"; //$NON-NLS-1$
        /**
         * Right
         */
        String RIGHT = "right"; //$NON-NLS-1$
        /**
         * Justify
         */
        String JUSTIFY = "justify"; //$NON-NLS-1$
    }

    /**
     * Font family as string (ex. "sans-serif").
     * <p>
     * Default: system font
     */
    public static final String FONT_FAMILY = "font-family"; //$NON-NLS-1$

    /**
     * Font size in pixels (int), at normal zoom level.
     * <p>
     * Default: fit to element height
     */
    public static final String FONT_SIZE = "font-size"; //$NON-NLS-1$

    /**
     * Font style as a string. Possible values: {@link FontStyle}.
     * <p>
     * Default: {@link FontStyle#NORMAL}
     */
    public static final String FONT_STYLE = "font-style"; //$NON-NLS-1$

    /**
     * Font style values.
     *
     * @noimplement This interface is not intended to be implemented by clients.
     */
    public interface FontStyle {
        /**
         * Normal font
         */
        String NORMAL = "normal"; //$NON-NLS-1$
        /**
         * Italic font
         */
        String ITALIC = "italic"; //$NON-NLS-1$
        /**
         * Oblique font
         */
        String OBLIQUE = "oblique"; //$NON-NLS-1$
    }

    /**
     * Font weight as a string. Possible values: {@link FontWeight}.
     * <p>
     * Default: {@link FontWeight#NORMAL}
     */
    public static final String FONT_WEIGHT = "font-weight"; //$NON-NLS-1$

    /**
     * Font weight values.
     */
    public interface FontWeight {
        /**
         * Normal
         */
        String NORMAL = "normal"; //$NON-NLS-1$
        /**
         * Bold
         */
        String BOLD = "bold"; //$NON-NLS-1$
        /**
         * Bolder than the inherited font weight
         */
        String BOLDER = "bolder"; //$NON-NLS-1$
        /**
         * Lighter than the inherited font weight
         */
        String LIGHTER = "lighter"; //$NON-NLS-1$
    }

    // Annotation

    /**
     * Symbol type as a string. Possible values: {@link SymbolType}.
     * <p>
     * Default: @link {@link SymbolType#NONE}
     */
    public static final String SYMBOL_TYPE = "symbol-type"; //$NON-NLS-1$

    /**
     * Symbol type values.
     */
    public interface SymbolType {
        /**
         * None
         */
        String NONE = "none"; //$NON-NLS-1$
        /**
         * Diamond
         */
        String DIAMOND = "diamond"; //$NON-NLS-1$
        /**
         * Circle
         */
        String CIRCLE = "circle"; //$NON-NLS-1$
        /**
         * Square
         */
        String SQUARE = "square"; //$NON-NLS-1$
        /**
         * Triangle
         */
        String TRIANGLE = "triangle"; //$NON-NLS-1$
        /**
         * Inverted triangle
         */
        String INVERTED_TRIANGLE = "inverted-triangle"; //$NON-NLS-1$
        /**
         * Cross
         */
        String CROSS = "cross"; //$NON-NLS-1$
        /**
         * Plus
         */
        String PLUS = "plus"; //$NON-NLS-1$
    }

    // General properties

    /**
     * Height of an element, as a factor, between 0.0 and 1.0 (float), of the
     * normal element height.
     * <p>
     * Default: 1.0
     */
    public static final String HEIGHT = "height"; //$NON-NLS-1$

    /**
     * Width of an element in pixels. Not applicable to elements representing a
     * time duration.
     * <p>
     * Default: 1.0
     */
    public static final String WIDTH = "width"; //$NON-NLS-1$

    /**
     * Cursor style when pointing over an element, as a string equal to one of
     * the CSS 'cursor' property values.
     * <p>
     * Default: "auto"
     */
    public static final String CURSOR = "cursor"; //$NON-NLS-1$

    // modifiers

    /**
     * Suffix to be appended to a color style key to apply a blending color to
     * that style's color value. The value of the modifier style is a hex string
     * that includes the blending color's alpha between 0 and 255 (#rrggbbaa).
     */
    public static final String BLEND = "-blend"; //$NON-NLS-1$

    /**
     * Suffix to be appended to a numerical style key to apply a multiplication
     * factor to that style's numerical value. The value of the modifier style
     * is a positive float.
     */
    public static final String FACTOR = "-factor"; //$NON-NLS-1$

    // custom properties

    /**
     * Style group name. Can be used to indicate that some
     * {@link OutputElementStyle} are in a same group.
     * <p>
     * Default: ungrouped
     */
    public static final String STYLE_GROUP = "style-group"; //$NON-NLS-1$

    /**
     * Style name, the human-readable name of this style.
     * <p>
     * Default: style id
     */
    public static final String STYLE_NAME = "style-name"; //$NON-NLS-1$
}
