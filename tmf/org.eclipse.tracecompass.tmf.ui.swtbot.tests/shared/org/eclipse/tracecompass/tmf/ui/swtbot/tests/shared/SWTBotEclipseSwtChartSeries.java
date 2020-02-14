/*******************************************************************************
 * Copyright (c) 2019, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.ISeries;
import org.eclipse.swtchart.ISeries.SeriesType;

/**
 * SWTBot class representing a SwtChart series
 *
 * @author Patrick Tasse
 */
public class SWTBotEclipseSwtChartSeries extends AbstractSWTBotControl<Chart> {

    private final ISeries<?> fSeries;

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
    public SWTBotEclipseSwtChartSeries(Chart chart, ISeries<?> series) throws WidgetNotFoundException {
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
