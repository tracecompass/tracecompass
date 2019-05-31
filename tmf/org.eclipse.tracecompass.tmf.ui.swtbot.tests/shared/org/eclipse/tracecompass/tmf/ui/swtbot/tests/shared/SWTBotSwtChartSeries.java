/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;
import org.swtchart.Chart;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;

/**
 * SWTBot class representing a SwtChart series
 *
 * @author Patrick Tasse
 */
public class SWTBotSwtChartSeries extends AbstractSWTBotControl<Chart> {

    private final ISeries fSeries;

    /**
     * Constructor
     *
     * @param chart
     *            the chart widget
     * @param series
     *            the series
     * @throws WidgetNotFoundException
     *             if the widget is <code>null</code> or widget has been
     *             disposed.
     */
    public SWTBotSwtChartSeries(Chart chart, ISeries series) throws WidgetNotFoundException {
        super(chart);
        fSeries = series;
    }

    /**
     * Returns the series type
     *
     * @return the series type
     */
    public SeriesType getType() {
        return fSeries.getType();
    }

    /**
     * Returns the series id
     *
     * @return the series id
     */
    public String getSeriesId() {
        return fSeries.getId();
    }
}
