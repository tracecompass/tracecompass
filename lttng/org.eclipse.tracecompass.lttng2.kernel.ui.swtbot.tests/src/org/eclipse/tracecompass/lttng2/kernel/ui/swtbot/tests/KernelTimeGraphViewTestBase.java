/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotTimeGraph;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotTimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;
import org.junit.Test;

/**
 * Kernel based time graph view test base. Used to test time graph views using
 * kernel traces
 *
 * @author Matthew Khouzam
 *
 */
public abstract class KernelTimeGraphViewTestBase extends KernelTestBase {

    /**
     * The vertical scrollbar is the first slider described in the view, so its
     * slider index is equal to 0
     */
    private static final int VERTICAL_SCROLLBAR_INDEX = 0;

    /**
     * The horizontal scrollbar is the second slider described in the view, so its
     * slider index is equal to 1
     */
    private static final int HORIZONTAL_SCROLLBAR_INDEX = 1;

    /**
     * Tooltip used for separator toolbar items
     */
    protected static final String SEPARATOR = "";

    /**
     * Get an SWTBotView of the view being tested
     *
     * @return a bot of the view being tested
     */
    protected abstract SWTBotView getViewBot();

    /**
     * Get the tool bar tool tip text values in order
     *
     * @return the tool bar tool tip text values in order
     */
    protected abstract List<String> getToolbarTooltips();

    /**
     * Get the legend text values in order
     *
     * @return the legend text values in order
     */
    protected abstract List<String> getLegendValues();

    /**
     * Opens the view
     *
     * @return the view bot
     */
    protected abstract SWTBotView openView();

    /**
     * Test toolbar button order and that all buttons are enabled and visible
     */
    @Test
    public void testToolbar() {
        List<SWTBotToolbarButton> buttons = getViewBot().getToolbarButtons();
        List<String> tooltipsExpected = getToolbarTooltips();
        List<String> tooltips = new ArrayList<>();
        for (SWTBotToolbarButton button : buttons) {
            tooltips.add(button.getToolTipText());
            assertTrue(button.getText() + " enabled", button.isEnabled());
            assertTrue(button.getText() + " visible", button.isVisible());
        }
        assertEquals(tooltipsExpected, tooltips);
    }

    /**
     * Test the legend content
     */
    @Test
    public void testLegend() {
        List<String> labelValues = getLegendValues();
        SWTBotToolbarButton legendButton = getViewBot().toolbarButton("Show Legend");
        legendButton.click();
        SWTBotShell shell = fBot.shell("States Transition Visualizer");
        shell.activate();
        SWTBot bot = shell.bot();
        for (int i = 1; i <= labelValues.size(); i++) {
            SWTBotLabel label = bot.label(i);
            assertNotNull(label);
            assertEquals(labelValues.get(i - 1), label.getText());
        }
        bot.button("OK").click();
    }

    /**
     * Test that re-opening a view repoopulates it properly.
     */
    @Test
    public void testOpenCloseOpen() {
        SWTBotView viewBot = openView();
        SWTBotTimeGraph tgBot = new SWTBotTimeGraph(viewBot.bot());
        Map<String, List<String>> before = getItemNames(tgBot);
        viewBot.close();
        viewBot = openView();
        tgBot = new SWTBotTimeGraph(viewBot.bot());
        Map<String, List<String>> after = getItemNames(tgBot);
        assertEquals(before, after);
    }

    /**
     * Test the vertical scroll bar position
     */
    @Test
    public void testVerticalScrollbar() {
        SWTBotView viewBot = openView();
        List<? extends @NonNull Slider> sliders = viewBot.bot().widgets(WidgetOfType.widgetOfType(Slider.class));
        assertTrue("The view has " + sliders.size() + " sliders", sliders.size() >= 2);
        Rectangle sliderRect = getBounds(sliders.get(VERTICAL_SCROLLBAR_INDEX));

        Rectangle timegraphRect = getBounds(viewBot.bot().widget(WidgetOfType.widgetOfType(TimeGraphControl.class)));
        assertEquals("Incorrect vertical slider start position", timegraphRect.width, sliderRect.x);
        assertEquals("Incorrect vertical slider height", timegraphRect.height, sliderRect.height);
    }

    /**
     * Test the horizontal scroll bar position
     */
    @Test
    public void testHorizontalScrollbar() {
        SWTBotView viewBot = openView();
        List<? extends @NonNull Slider> sliders = viewBot.bot().widgets(WidgetOfType.widgetOfType(Slider.class));
        assertTrue("The view has " + sliders.size() + " sliders", sliders.size() >= 2);
        Rectangle sliderRect = getBounds(sliders.get(HORIZONTAL_SCROLLBAR_INDEX));

        SWTBotTimeGraph tgBot = new SWTBotTimeGraph(viewBot.bot().widget(WidgetOfType.widgetOfType(TimeGraphControl.class)));
        Rectangle timegraphRect = getBounds(viewBot.bot().widget(WidgetOfType.widgetOfType(TimeGraphControl.class)));
        assertEquals("Incorrect horizontal slider start position", timegraphRect.x + tgBot.getNameSpace(), sliderRect.x);
    }

    private @NonNull static Map<String, List<String>> getItemNames(SWTBotTimeGraph tgBot) {
        Map<String, List<String>> returnStructure = new HashMap<>();
        for (SWTBotTimeGraphEntry element : tgBot.getEntries()) {
            returnStructure.put(element.getText(),
                    Arrays.stream(
                        element.getEntries())
                        .map(tgEntry -> tgEntry.getText())
                        .collect(Collectors.toList())
                    );
        }
        return returnStructure;
    }

    private static Rectangle getBounds(Control widget) {
        return UIThreadRunnable.syncExec(() -> widget.getBounds());
    }
}