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

import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * This represents the appearance of a Y series for a XY chart. It contains
 * information about the name, type (ex. Bar, Line, Scatter), the style (ex.
 * Dot, Dash, Solid) and the color of the Y Series. There is no information
 * about the data.
 *
 * NOTE: This interface is kept as an internal API, it is the equivalent of an
 * {@link OutputElementStyle} instead, where name maps to
 * {@link StyleProperties#STYLE_NAME}, type maps to
 * {@link StyleProperties#SERIES_TYPE}, style maps to
 * {@link StyleProperties#SERIES_STYLE}, color maps to
 * {@link StyleProperties#COLOR}, width maps to {@link StyleProperties#WIDTH},
 * symbolStyle maps to {@link StyleProperties#SYMBOL_TYPE}, symbolSize maps to
 * {@link StyleProperties#HEIGHT}
 *
 * @author Yonni Chen
 * @since 4.0
 */
public interface IYAppearance {

    /**
     * This contains strings defining a series type. It could be for example
     * bar, line, scatter, etc.
     *
     * @author Yonni Chen
     */
    public final class Type {

        /**
         * Line series
         */
        public static final String LINE = StyleProperties.SeriesType.LINE;

        /**
         * Area series
         */
        public static final String AREA = StyleProperties.SeriesType.AREA;

        /**
         * Scatter series
         */
        public static final String SCATTER = StyleProperties.SeriesType.SCATTER;

        /**
         * Bar series
         */
        public static final String BAR = StyleProperties.SeriesType.BAR;

        /**
         * Constructor
         */
        private Type() {
            // do nothing
        }
    }

    /**
     * This contains strings defining a series line style. It could be for
     * example solid, dash, dot, etc.
     *
     * @author Yonni Chen
     * @deprecated As currently unused, at least in o.e.tracecompass.
     */
    @Deprecated
    public final class Style {

        /**
         * No line
         */
        public static final String NONE = StyleProperties.SeriesStyle.NONE;

        /**
         * Solid line
         */
        public static final String SOLID = StyleProperties.SeriesStyle.SOLID;

        /**
         * Dotted line
         */
        public static final String DOT = StyleProperties.SeriesStyle.DOT;

        /**
         * Dashed line
         */
        public static final String DASH = StyleProperties.SeriesStyle.DASH;

        /**
         * Dashed Dot (-.-.-.-) line
         */
        public static final String DASHDOT = StyleProperties.SeriesStyle.DASHDOT;

        /**
         * Dashed Dot Dot (-..-..-..) line
         */
        public static final String DASHDOTDOT = StyleProperties.SeriesStyle.DASHDOTDOT;

        /**
         * Constructor
         */
        private Style() {
            // do nothing
        }
    }

    /**
     * Symbol styles, contains strings defining potential appearances of a dot
     * in a chart.
     *
     * @author Matthew Khouzam
     * @since 4.1
     */
    public final class SymbolStyle {

        /**
         * No tick
         *
         * @deprecated As currently unused, at least in o.e.tracecompass.
         */
        @Deprecated
        public static final String NONE = "NONE"; //$NON-NLS-1$

        /** Diamond tick */
        public static final String DIAMOND = StyleProperties.SymbolType.DIAMOND;

        /** Circle tick */
        public static final String CIRCLE = StyleProperties.SymbolType.CIRCLE;

        /** Square tick */
        public static final String SQUARE = StyleProperties.SymbolType.SQUARE;

        /** triangle tick */
        public static final String TRIANGLE = StyleProperties.SymbolType.TRIANGLE;

        /** inverted triangle */
        public static final String INVERTED_TRIANGLE = StyleProperties.SymbolType.INVERTED_TRIANGLE;

        /** st andrews cross, like an X */
        public static final String CROSS = StyleProperties.SymbolType.CROSS;

        /** Plus tick */
        public static final String PLUS = StyleProperties.SymbolType.PLUS;

        /** Constructor */
        private SymbolStyle() {
            // do nothing
        }
    }

    /**
     * Gets the Y series name
     *
     * @return the name of the Y serie
     * @deprecated As currently unused, at least in o.e.tracecompass.
     */
    @Deprecated
    String getName();

    /**
     * Gets the Y series line style. Serie style can be DOT, DOTDOT, DASH, etc.
     *
     * @return the style of the Y serie
     * @deprecated As currently unused, at least in o.e.tracecompass.
     */
    @Deprecated
    String getStyle();

    /**
     * Gets the Y serie color
     *
     * @return the color of the Y serie
     * @deprecated As currently unused, at least in o.e.tracecompass.
     */
    @Deprecated
    RGBAColor getColor();

    /**
     * Gets Y serie type. Serie type define the type of chart : Bar, Line,
     * Scatter, etc.
     *
     * @return the type of the Y serie
     * @deprecated As currently unused, at least in o.e.tracecompass.
     */
    @Deprecated
    String getType();

    /**
     * Gets Y serie width.
     *
     * @return the width of the Y serie
     * @deprecated As currently unused, at least in o.e.tracecompass.
     */
    @Deprecated
    int getWidth();

    /**
     * Symbol Style. Used for ticks. Styles type define the type of ticks for
     * actual points. Ticks can be:
     * <ul>
     * <li>{@link SymbolStyle#CIRCLE}</li>
     * <li>{@link SymbolStyle#CROSS}</li>
     * <li>{@link SymbolStyle#DIAMOND}</li>
     * <li>{@link SymbolStyle#INVERTED_TRIANGLE}</li>
     * <li>{@link SymbolStyle#TRIANGLE}</li>
     * <li>{@link SymbolStyle#PLUS}</li>
     * <li>{@link SymbolStyle#SQUARE}</li>
     * </ul>
     *
     * If the tick is {@link SymbolStyle#NONE}, no tick shall be displayed.
     *
     * @return the appearance
     * @since 4.1
     * @deprecated As currently unused, at least in o.e.tracecompass.
     */
    @Deprecated
    default String getSymbolStyle() {
        return SymbolStyle.NONE;
    }

    /**
     * Get the symbol size
     *
     * @return the size of the symbol in pixels
     * @since 4.1
     * @deprecated As currently unused, at least in o.e.tracecompass.
     */
    @Deprecated
    default int getSymbolSize() {
        return 3;
    }

    /**
     * Convert this IYAppearance to an OutputElementStyle
     *
     * @return The OutputElementStyle from this style
     * @since 6.0
     * @deprecated As currently unused, at least in o.e.tracecompass.
     */
    @Deprecated
    default OutputElementStyle toOutputElementStyle() {
        Builder<String, Object> builder = ImmutableMap.builder();
        builder.put(StyleProperties.STYLE_NAME, getName());
        builder.put(StyleProperties.SERIES_TYPE, getType());
        builder.put(StyleProperties.SERIES_STYLE, getStyle());
        builder.put(StyleProperties.COLOR, getColor());
        builder.put(StyleProperties.WIDTH, getWidth());
        builder.put(StyleProperties.SYMBOL_TYPE, getSymbolStyle());
        builder.put(StyleProperties.HEIGHT, getSymbolSize());
        return new OutputElementStyle(null, builder.build());
    }

}
