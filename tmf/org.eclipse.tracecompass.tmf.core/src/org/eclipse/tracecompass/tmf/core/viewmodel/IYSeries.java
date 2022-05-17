/*******************************************************************************
 * Copyright (c) 2017 Ericsson and others.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.viewmodel;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A y series, essentially a line
 *
 * @author Matthew Khouzam
 * @since 3.1
 * @deprecated As currently unused, at least in o.e.tracecompass.
 */
@Deprecated
public interface IYSeries {

    /**
     * Line series
     */
    String LINE = "line"; //$NON-NLS-1$
    /**
     * Area series
     */
    String AREA = "area"; //$NON-NLS-1$
    /**
     * Scatter series
     */
    String SCATTER = "scatter"; //$NON-NLS-1$
    /**
     * Bar series
     */
    String BAR = "bar"; //$NON-NLS-1$

    /**
     * No line
     */
    String NONE = "none"; //$NON-NLS-1$
    /**
     * Solid line
     */
    String SOLID = "solid"; //$NON-NLS-1$
    /**
     * Dotted line
     */
    String DOT = "dot"; //$NON-NLS-1$
    /**
     * Dashed line
     */
    String DASH = "dash"; //$NON-NLS-1$
    /**
     * Dashed Dot (-.-.-.-) line
     */
    String DASHDOT = "dashdot"; //$NON-NLS-1$
    /**
     * Dashed Dot Dot (-..-..-..) line
     */
    String DASHDOTDOT = "dashdotdot"; //$NON-NLS-1$

    /**
     * Get the label of the series, AKA, the name of that series to display
     *
     * @return the label
     */
    String getLabel();

    /**
     * Get the y series values
     *
     * @return the y series values
     */
    double[] getDatapoints();

    /**
     * Gets the series type. Can be a :
     * <ul>
     * <li>LINE</li>
     * <li>AREA</li>
     * <li>BAR</li>
     * <li>SCATTER</li>
     * </ul>
     *
     * @return the series type
     */
    String getSeriesType();

    /**
     * Gets the series style. Can be:
     * <ul>
     * <li>NONE</li>
     * <li>SOLID</li>
     * <li>DOT</li>
     * <li>DASH</li>
     * <li>DASHDOT</li>
     * <li>DASHDOTDOT</li>
     * </ul>
     *
     * @return the series style
     */
    String getSeriesStyle();

    /**
     * Get the color of the series
     *
     * @return the color of the series
     */
    @Nullable String getColor();

    /**
     * Get the thickness of the series.
     * <ul>
     * <li>If it is a {@link #LINE} chart, this is the width of the line</li>
     * <li>If it is an {@link #AREA} chart, this is the width of the envelope
     * line<</li>
     * <li>If it is a {@link #SCATTER} chart, this is the size of the
     * symbol<</li>
     * <li>If it is a {@link #BAR} chart, this is the width of the border<</li>
     * </ul>
     *
     * @return the width hint
     */
    int getWidth();
}
