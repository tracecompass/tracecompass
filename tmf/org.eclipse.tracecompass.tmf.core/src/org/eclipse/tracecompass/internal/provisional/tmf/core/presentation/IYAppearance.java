/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.presentation;

/**
 * This represents the appearance of a Y series for a XY chart. It contains
 * information about the name, type (ex. Bar, Line, Scatter), the style (ex.
 * Dot, Dash, Solid) and the color of the Y Series. There is no information
 * about the data.
 *
 * @author Yonni Chen
 * @since 3.0
 */
public interface IYAppearance {

    /**
     * This contains strings defining a series type. It could be for example bar,
     * line, scatter, etc.
     *
     * @author Yonni Chen
     */
    public final class Type {

        /**
         * Line series
         */
        public static final String LINE = "line"; //$NON-NLS-1$

        /**
         * Area series
         */
        public static final String AREA = "area"; //$NON-NLS-1$

        /**
         * Scatter series
         */
        public static final String SCATTER = "scatter"; //$NON-NLS-1$

        /**
         * Bar series
         */
        public static final String BAR = "bar"; //$NON-NLS-1$

        /**
         * Constructor
         */
        private Type() {

        }
    }

    /**
     * This contains strings defining a series line style. It could be for example
     * solid, dash, dot, etc.
     *
     * @author Yonni Chen
     */
    public final class Style {

        /**
         * No line
         */
        public static final String NONE = "NONE"; //$NON-NLS-1$

        /**
         * Solid line
         */
        public static final String SOLID = "SOLID"; //$NON-NLS-1$

        /**
         * Dotted line
         */
        public static final String DOT = "DOT"; //$NON-NLS-1$

        /**
         * Dashed line
         */
        public static final String DASH = "DASH"; //$NON-NLS-1$

        /**
         * Dashed Dot (-.-.-.-) line
         */
        public static final String DASHDOT = "DASHDOT"; //$NON-NLS-1$

        /**
         * Dashed Dot Dot (-..-..-..) line
         */
        public static final String DASHDOTDOT = "DASHDOTDOT"; //$NON-NLS-1$

        /**
         * Constructor
         */
        private Style() {

        }
    }

    /**
     * Gets the Y series name
     *
     * @return the name of the Y serie
     */
    String getName();

    /**
     * Gets the Y series line style. Serie style can be DOT, DOTDOT, DASH, etc.
     *
     * @return the style of the Y serie
     */
    String getStyle();

    /**
     * Gets the Y serie color
     *
     * @return the color of the Y serie
     */
    RGBColor getColor();

    /**
     * Gets Y serie type. Serie type define the type of chart : Bar, Line, Scatter,
     * etc.
     *
     * @return the type of the Y serie
     */
    String getType();

    /**
     * Gets Y serie width.
     *
     * @return the width of the Y serie
     */
    int getWidth();
}
