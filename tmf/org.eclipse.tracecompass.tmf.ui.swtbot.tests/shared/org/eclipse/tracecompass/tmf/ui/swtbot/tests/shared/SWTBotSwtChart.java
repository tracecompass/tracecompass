/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;
import org.swtchart.Chart;

/**
 * SWTBot class representing a SwtChart
 *
 * @author Bernd Hufmann
 */
public class SWTBotSwtChart extends AbstractSWTBotControl<Chart> {

    /**
     * Constructor
     *
     * @param w the widget
     * @throws WidgetNotFoundException if the widget is <code>null</code> or widget has been disposed.
     */
    public SWTBotSwtChart(Chart w) throws WidgetNotFoundException {
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
    public SWTBotSwtChart(SWTBot bot) throws WidgetNotFoundException {
        super(bot.widget(WidgetOfType.widgetOfType(Chart.class)));
    }

    @Override
    public AbstractSWTBotControl<Chart> moveMouseToWidget() {
        return super.moveMouseToWidget();
    }
}
