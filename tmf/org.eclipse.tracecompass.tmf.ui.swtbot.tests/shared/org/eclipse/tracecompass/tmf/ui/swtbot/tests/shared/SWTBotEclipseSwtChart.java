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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.ISeries;

/**
 * SWTBot class representing a SwtChart
 *
 * @author Bernd Hufmann
 */
public class SWTBotEclipseSwtChart extends AbstractSWTBotControl<Chart> {

    /**
     * Constructor
     *
     * @param w the widget
     * @throws WidgetNotFoundException if the widget is <code>null</code> or widget has been disposed.
     */
    public SWTBotEclipseSwtChart(Chart w) throws WidgetNotFoundException {
        super(w);
    }

    /**
     * Constructor
     *
     * @param bot
     *            a SWTBot instance with which to find a SwtChart
     * @throws WidgetNotFoundException
     *             if the widget is <code>null</code> or widget has been
     *             disposed.
     */
    public SWTBotEclipseSwtChart(SWTBot bot) throws WidgetNotFoundException {
        super(bot.widget(WidgetOfType.widgetOfType(Chart.class)));
    }

    @Override
    public AbstractSWTBotControl<Chart> moveMouseToWidget() {
        return super.moveMouseToWidget();
    }

    /**
     * Returns the list of series
     *
     * @return the list of series
     */
    public List<SWTBotEclipseSwtChartSeries> getSeries() {
        List<SWTBotEclipseSwtChartSeries> list = new ArrayList<>();
        for (ISeries<?> series : widget.getSeriesSet().getSeries()) {
            list.add(new SWTBotEclipseSwtChartSeries(widget, series));
        }
        return list;
    }

    /**
     * Returns the series with the specified id
     *
     * @param id
     *            the id
     * @return the series
     */
    public SWTBotEclipseSwtChartSeries getSeries(String id) {
        AtomicReference<ISeries<?>> series = new AtomicReference<>();
        SWTBotUtils.waitUntil(chart -> {
            series.set(widget.getSeriesSet().getSeries(id));
            return series.get() != null;
        }, widget, () -> "Timed out waiting for series " + id);
        return new SWTBotEclipseSwtChartSeries(widget, series.get());
    }
}
