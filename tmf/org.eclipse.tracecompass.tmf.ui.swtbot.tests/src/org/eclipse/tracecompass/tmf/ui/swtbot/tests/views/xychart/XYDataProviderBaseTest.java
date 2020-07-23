/*******************************************************************************
 * Copyright (c) 2017, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.views.xychart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.utils.FileUtils;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.IBarSeries;
import org.eclipse.swtchart.ILineSeries;
import org.eclipse.swtchart.ISeries;
import org.eclipse.swtchart.LineStyle;
import org.eclipse.swtchart.model.CartesianSeriesModel;
import org.eclipse.tracecompass.tmf.core.model.TmfCommonXAxisModel;
import org.eclipse.tracecompass.tmf.core.model.YModel;
import org.eclipse.tracecompass.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.views.xychart.TmfChartView;
import org.eclipse.ui.IViewPart;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * SWTBot tests for viewers using XY data provider
 *
 * @author Yonni Chen
 */
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
     * Get the full path to a test file from this class's bundle.
     *
     * @param bundlePath
     *            path from the bundle
     * @return the absolute path
     */
    private String getFullPath(String bundlePath) {
        try {
            Bundle bundle = FrameworkUtil.getBundle(this.getClass());
            URL location = FileLocator.find(bundle, new Path(bundlePath), null);
            URI uri = FileLocator.toFileURL(location).toURI();
            return new File(uri).getAbsolutePath();
        } catch (Exception e) {
            fail(e.toString());
            return null;
        }
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
     *            The path of the JSON file relative to this class's bundle
     * @return True if the serialized chart data matches the JSON file content
     */
    protected boolean isChartDataValid(final Chart chart, String expectedJson, String... otherSeries) {
        /**
         * FIXME : Once CQ for Jackson is approved, use deserialization instead of
         * comparing strings
         */
        String expected = FileUtils.read(getFullPath(expectedJson));
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
        ISeries<Integer> mainSeries = chart.getSeriesSet().getSeries(mainSeriesName);
        if (mainSeries == null) {
            System.out.println("Main Series " + mainSeriesName + " not found in chart");
            return null;
        }

        /* X and Y Values shown in chart */
        double[] xMain = getXSeries(mainSeries);
        double[] yMain = getYSeries(mainSeries);

        Map<@NonNull String, @NonNull IYModel> yModels = new LinkedHashMap<>();
        yModels.put(mainSeriesName, new YModel(-1, mainSeriesName, Objects.requireNonNull(yMain)));

        for (String other : otherSeries) {
            if (other != null) {
                ISeries<Integer> series = chart.getSeriesSet().getSeries(other);
                if (series == null) {
                    System.out.println("Series " + other + " not found in chart");
                    return null;
                }

                /* X and Y Values shown in chart */
                double[] xSeries = getXSeries(series);
                double[] ySeries = getYSeries(series);

                /* Series should have the same x axis values, not finished updating all series*/
                if (!Arrays.equals(xMain, xSeries)) {
                    System.out.println("Series don't currently have the same x axis values");
                    return null;
                }
                yModels.put(other, new YModel(-1, other, Objects.requireNonNull(ySeries)));
            }
        }

        long[] x = Longs.toArray(Doubles.asList(xMain));
        assertNotNull(x);
        return new TmfCommonXAxisModel(getTitle(), x, yModels.values());
    }

    private static double[] getXSeries(ISeries<Integer> series) {
        CartesianSeriesModel<Integer> dataModel = series.getDataModel();
        if (dataModel == null) {
            return new double[0];
        }
        return StreamSupport.stream(dataModel.spliterator(), false).filter(t -> dataModel.getX(t) != null).mapToDouble(value -> dataModel.getX(value).doubleValue()).toArray();
    }

    private static double[] getYSeries(ISeries<Integer> series) {
        CartesianSeriesModel<Integer> dataModel = series.getDataModel();
        if (dataModel == null) {
            return new double[0];
        }
        return StreamSupport.stream(dataModel.spliterator(), false).filter(t -> dataModel.getY(t) != null).mapToDouble(value -> dataModel.getY(value).doubleValue()).toArray();
    }

    /**
     * Gets the ChartViewer from a TmfChartView
     *
     * @param viewPart
     *            The IViewPart
     * @return The ChartViewer from the IViewPart
     */
    protected static TmfXYChartViewer getChartViewer(IViewPart viewPart) {
        if (viewPart instanceof TmfChartView) {
            return ((TmfChartView) viewPart).getChartViewer();
        }
        return null;
    }

    /**
     * Verify the style of a series in the XY chart
     *
     * @param seriesName
     *            The name of the series
     * @param expectedType
     *            Expected type of the series
     * @param expectedColor
     *            Expected color of the series. If the color is arbitrary, a value
     *            of <code>null</code> will skip color check
     * @param expectedLineStyle
     *            Expected line style of the series
     * @param isArea
     *            Parameter should be true if expected series show area, false
     *            either
     */
    protected void verifySeriesStyle(String seriesName, ISeries.SeriesType expectedType, @Nullable RGB expectedColor, LineStyle expectedLineStyle, boolean isArea) {
        /* Make sure the UI update is complete */
        UIThreadRunnable.syncExec(() -> {});

        ISeries<?> series = fChart.getSeriesSet().getSeries(seriesName);
        assertNotNull(series);
        assertTrue(series.isVisible());

        /* Color, type and style */
        assertEquals(expectedType, series.getType());

        if (expectedType == ISeries.SeriesType.LINE) {
            ILineSeries<?> line = (ILineSeries<?>) series;
            if (expectedColor != null) {
                assertEquals(expectedColor, line.getLineColor().getRGB());
            }
            assertEquals(expectedLineStyle, line.getLineStyle());
            assertEquals(isArea, line.isAreaEnabled());
        } else if (expectedType == ISeries.SeriesType.BAR) {
            IBarSeries<?> bar = (IBarSeries<?>) series;
            if (expectedColor != null) {
                assertEquals(expectedColor, bar.getBarColor().getRGB());
            }
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
