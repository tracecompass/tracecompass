/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.utils.FileUtils;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.internal.tmf.core.model.TmfCommonXAxisModel;
import org.eclipse.tracecompass.internal.tmf.core.model.YModel;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;
import org.eclipse.ui.IViewPart;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.swtchart.Chart;
import org.swtchart.IBarSeries;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.LineStyle;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * SWTBot tests for viewers using XY data provider
 *
 * @author Yonni Chen
 */
@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class XYDataProviderBaseTest {

    /** The workbench bot */
    protected static SWTWorkbenchBot fBot;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    /** Default project name */
    protected static final String TRACE_PROJECT_NAME = "test";

    private static Gson fGson = new GsonBuilder().setPrettyPrinting().create();

    private SWTBotView fViewBot;
    private Chart fChart;

    /**
     * Before Class
     */
    @BeforeClass
    public static void beforeClass() {
        SWTBotUtils.initialize();

        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        fBot = new SWTWorkbenchBot();
        SWTBotUtils.closeView("welcome", fBot);
        /* Create the trace project */
        SWTBotUtils.createProject(TRACE_PROJECT_NAME);
        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
    }

    /**
     * Close the editor
     */
    @AfterClass
    public static void tearDown() {
        SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, fBot);
        fLogger.removeAllAppenders();
    }

    /**
     * Set up
     */
    @Before
    public void setup() {
        SWTBotUtils.openView(getViewID());
        fViewBot = fBot.viewById(getViewID());
        fViewBot.show();

        Matcher<Chart> widgetOfType = WidgetOfType.widgetOfType(Chart.class);
        fChart = fViewBot.bot().widget(widgetOfType);
        ITmfTrace trace = getTestTrace();

        File file = new File(trace.getPath());
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, file.getAbsolutePath(), trace.getTraceTypeId());
        SWTBotUtils.activateEditor(fBot, trace.getName());
    }

    /**
     * After Test
     */
    @After
    public void after() {
        fBot.closeAllEditors();
        SWTBotUtils.closeSecondaryShells(fBot);
        disposeTestTrace();
    }

    /**
     * Based on a SWT Chart, we check if data shown is valid with a JSON file.
     * Comparison with the main series and other series if exists
     *
     * @param chart
     *            A SWT Chart
     * @param otherSeries
     *            An array of other series name to check other than the main series
     * @param expectedJson
     *            The path of the JSON file
     * @return True if the serialized chart data matches the JSON file content
     */
    protected boolean isChartDataValid(final Chart chart, String expectedJson, String... otherSeries) {
        /**
         * FIXME : Once CQ for Jackson is approved, use deserialization instead of
         * comparing strings
         */
        String expected = FileUtils.read(expectedJson);
        TmfCommonXAxisModel model = extractModelFromChart(chart, otherSeries);
        String current = fGson.toJson(model);
        return expected.equals(current);
    }

    /**
     * From a SWT Chart, this method extract a {@link TmfCommonXAxisModel} that
     * represents the chart. Since, we unfortunately have no mecanism to deserialize
     * with GSON, we have to compare strings. So, once the model is extract from the
     * Chart, we serialize it and compare with a string
     *
     * @param chart
     *            A SWT Chart
     * @param otherSeries
     *            Name of other series to extract from Chart
     * @return A {@link TmfCommonXAxisModel}
     */
    protected TmfCommonXAxisModel extractModelFromChart(final Chart chart, String... otherSeries) {
        String mainSeriesName = getMainSeriesName();
        ISeries mainSeries = chart.getSeriesSet().getSeries(mainSeriesName);
        if (mainSeries == null) {
            System.out.println("Main Series is currently null");
            return null;
        }

        /* X and Y Values shown in chart */
        double[] xMain = mainSeries.getXSeries();
        double[] yMain = mainSeries.getYSeries();

        Map<@NonNull String, @NonNull IYModel> yModels = new LinkedHashMap<>();
        yModels.put(mainSeriesName, new YModel(mainSeriesName, Objects.requireNonNull(yMain)));

        for (String other : otherSeries) {
            if (other != null) {
                ISeries series = chart.getSeriesSet().getSeries(other);

                /* X and Y Values shown in chart */
                double[] xSeries = series.getXSeries();
                double[] ySeries = series.getYSeries();

                /* Series should have the same x axis values, not finished updating all series*/
                if (!Arrays.equals(xMain, xSeries)) {
                    System.out.println("Series don't currently have the same x axis values");
                    return null;
                }
                yModels.put(other, new YModel(other, Objects.requireNonNull(ySeries)));
            }
        }

        long[] x = Longs.toArray(Doubles.asList(xMain));
        assertNotNull(x);
        return new TmfCommonXAxisModel(getTitle(), x, yModels);
    }

    /**
     * Gets the ChartViewer from a IViewPart
     *
     * @param viewSite
     *            The IViewPart
     * @return The ChartViewer from the IViewPart
     *
     * @throws NoSuchMethodException
     *             Reflection exception should not happen
     * @throws SecurityException
     *             Reflection exception should not happen
     * @throws IllegalAccessException
     *             Reflection exception should not happen
     * @throws IllegalArgumentException
     *             Reflection exception should not happen
     * @throws InvocationTargetException
     *             Reflection exception should not happen
     */
    protected static TmfXYChartViewer getChartViewer(IViewPart viewSite)
            throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {

        TmfChartView chartView = (TmfChartView) viewSite;
        Method viewer = TmfChartView.class.getDeclaredMethod("getChartViewer");
        viewer.setAccessible(true);
        TmfXYChartViewer chartViewer = (TmfXYChartViewer) viewer.invoke(chartView);
        return chartViewer;
    }

    /**
     * Verify the style of a series in the XY chart
     *
     * @param seriesName
     *            The name of the series
     * @param expectedType
     *            Expected type of the series
     * @param expectedColor
     *            Expected color of the series
     * @param expectedLineStyle
     *            Expected line style of the series
     * @param isArea
     *            Parameter should be true if expected series show area, false
     *            either
     */
    protected void verifySeriesStyle(String seriesName, ISeries.SeriesType expectedType, RGB expectedColor, LineStyle expectedLineStyle, boolean isArea) {
        ISeries series = fChart.getSeriesSet().getSeries(seriesName);
        assertNotNull(series);
        assertTrue(series.isVisible());

        /* Color, type and style */
        assertEquals(expectedType, series.getType());

        if (expectedType == ISeries.SeriesType.LINE) {
            ILineSeries line = (ILineSeries) series;
            assertEquals(expectedColor, line.getLineColor().getRGB());
            assertEquals(expectedLineStyle, line.getLineStyle());
            assertEquals(isArea, line.isAreaEnabled());
        } else if (expectedType == ISeries.SeriesType.BAR) {
            IBarSeries bar = (IBarSeries) series;
            assertEquals(expectedColor, bar.getBarColor().getRGB());
            assertTrue(bar.isStackEnabled());
        }
    }

    /**
     * Gets the SWT Chart
     *
     * @return The chart
     *
     */
    protected Chart getChart() {
        return fChart;
    }

    /**
     * Gets the SWT Bot View
     *
     * @return The SWT Bot View
     *
     */
    protected SWTBotView getSWTBotView() {
        return fViewBot;
    }

    /**
     * Gets the main series name of the XY
     *
     * @return The main series name
     */
    protected abstract @NonNull String getMainSeriesName();

    /**
     * Gets the title of the XY
     *
     * @return The title
     */
    protected abstract @NonNull String getTitle();

    /**
     * Gets the view ID
     *
     * @return The view ID
     */
    protected abstract String getViewID();

    /**
     * Gets the trace on which the test will be run
     *
     * @return The trace
     */
    protected abstract ITmfTrace getTestTrace();

    /**
     * Disposes the trace on which the test was run
     */
    protected abstract void disposeTestTrace();
}
