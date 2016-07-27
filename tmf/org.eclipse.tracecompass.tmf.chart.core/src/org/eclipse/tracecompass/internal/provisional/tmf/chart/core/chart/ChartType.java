/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import org.eclipse.tracecompass.internal.tmf.chart.core.chart.Messages;

/**
 * Enumeration of the supported types of chart by the plugin. It also overrides
 * the default {@link #toString()} method that provides the name of the chart
 * type.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public enum ChartType {

    // ------------------------------------------------------------------------
    // Enum values
    // ------------------------------------------------------------------------

    /**
     * Defines a bar chart
     */
    BAR_CHART(nullToEmptyString(Messages.Chart_EnumBarChart)),
    /**
     * Defines a scatter chart
     */
    SCATTER_CHART(nullToEmptyString(Messages.Chart_EnumScatterChart)),
    /**
     * TODO: Defines a pie chart
     */
    PIE_CHART(nullToEmptyString(Messages.Chart_EnumPieChart));

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final String fName;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param name
     *            Name of the chart type
     */
    private ChartType(String name) {
        fName = name;
    }

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return fName;
    }

}
