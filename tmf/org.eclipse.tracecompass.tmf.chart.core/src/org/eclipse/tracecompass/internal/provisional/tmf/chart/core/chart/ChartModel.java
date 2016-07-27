/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart;

/**
 * This object should contain all the information needed to create a chart in
 * the GUI, independently of the actual chart implementation.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ChartModel {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final ChartType fType;
    private final String fTitle;
    private final boolean fXLogscale;
    private final boolean fYLogscale;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param type
     *            The type of the chart
     * @param title
     *            The title of the chart
     * @param xlog
     *            Whether X axis is logarithmic
     * @param ylog
     *            Whether Y axis is logarithmic
     */
    public ChartModel(ChartType type, String title, boolean xlog, boolean ylog) {
        fType = type;
        fTitle = title;
        fXLogscale = xlog;
        fYLogscale = ylog;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that returns the type of the chart.
     *
     * @return The chart type
     */
    public ChartType getChartType() {
        return fType;
    }

    /**
     * Accessor that returns the title of the chart.
     *
     * @return The title of the chart
     */
    public String getTitle() {
        return fTitle;
    }

    /**
     * Accessor that returns whether X axis is logarithmic.
     *
     * @return Whether X axis is logarithmic
     */
    public boolean isXLogscale() {
        return fXLogscale;
    }

    /**
     * Accessor that returns whether Y axis is logarithmic.
     *
     * @return Whether Y axis is logarithmic
     */
    public boolean isYLogscale() {
        return fYLogscale;
    }

}
