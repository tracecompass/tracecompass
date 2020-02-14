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

package org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts;

/**
 * Contains UI settings about an XY chart. For instance, it contains its title,
 * x and y label and resolution.
 *
 * @author Yonni Chen
 * @since 3.2
 * @deprecated use {@link org.eclipse.tracecompass.tmf.ui.viewers.xychart.linechart.TmfXYChartSettings}
 */
@Deprecated
public class TmfXYChartSettings {

    private final String fTitle;
    private final String fXLabel;
    private final String fYLabel;
    private final double fResolution;

    /**
     * Constructor
     *
     * @param title
     *            Chart's title
     * @param xLabel
     *            Label describing x axis
     * @param yLabel
     *            Label describing y axis
     * @param resolution
     *            Chart's resolution. Used to calculate a sampling on x axis. It
     *            will be used to calculate number of nanoseconds between two points
     *            of the sampling
     */
    public TmfXYChartSettings(String title, String xLabel, String yLabel, double resolution) {
        fTitle = title;
        fXLabel = xLabel;
        fYLabel = yLabel;
        fResolution = resolution;
    }

    /**
     * Gets the chart's title
     *
     * @return The chart's title
     */
    public String getTitle() {
        return fTitle;
    }

    /**
     * Gets the label describing x axis
     *
     * @return The x label
     */
    public String getXLabel() {
        return fXLabel;
    }

    /**
     * Gets the label describing x axis
     *
     * @return The y label
     */
    public String getYLabel() {
        return fYLabel;
    }

    /**
     * Gets the chart's resolution
     *
     * @return The chart's resolution
     */
    public double getResolution() {
        return fResolution;
    }
}
