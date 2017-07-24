/*******************************************************************************
 * Copyright (c) 2017 Ericsson
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
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.utils.FileUtils;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.io.diskioactivity.DiskIOActivityView;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.internal.tmf.core.model.TmfCommonXAxisModel;
import org.eclipse.tracecompass.internal.tmf.core.model.YModel;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXLineChartViewer;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;
import org.eclipse.ui.IViewPart;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.LineStyle;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * SWTBot tests for Disks Activity view
 *
 * @author Yonni Chen
 */
@SuppressWarnings("restriction")
public class DisksIOViewTest extends KernelTestBase {

    private static final RGB RED = new RGB(255, 0, 0);
    private static final RGB BLUE = new RGB(0, 0, 255);

    private static final int NUMBER_OF_POINT = 50;
    private static final int MORE_POINTS = 100;

    private static final @NonNull String TITLE = "Disk I/O View";
    private static final @NonNull String READ_SERIES_NAME = "8,0 read";
    private static final @NonNull String WRITE_SERIES_NAME = "8,0 write";

    private static final @NonNull ITmfTimestamp START_TIME_WHERE_THERE_IS_DISK_ACTIVITY = TmfTimestamp.fromNanos(1332170686646030906L);
    private static final @NonNull ITmfTimestamp END_TIME_WHERE_THERE_IS_DISK_ACTIVITY = TmfTimestamp.fromNanos(1332170686658688158L);

    private static Gson fGson = new GsonBuilder().setPrettyPrinting().create();

    private SWTBotView fViewBotDisk;
    private Chart fChart;

    /**
     * Setup
     */
    @Override
    @Before
    public void before() {

        SWTBotUtils.openView(DiskIOActivityView.ID);
        fViewBotDisk = fBot.viewById(DiskIOActivityView.ID);
        fViewBotDisk.show();
        Matcher<Chart> widgetOfType = WidgetOfType.widgetOfType(Chart.class);
        fChart = fViewBotDisk.bot().widget(widgetOfType);

        try {
            SWTBotUtils.openTrace(TRACE_PROJECT_NAME, Paths.get(FileLocator.toFileURL(CtfTestTrace.KERNEL.getTraceURL()).toURI()).toString(), KERNEL_TRACE_TYPE);
        } catch (IOException | URISyntaxException e) {
            fail(e.getMessage());
        }
        SWTBotUtils.activateEditor(fBot, "kernel");
    }

    /**
     * Test to check the Disks IO Activity view. First, when trace opened, there
     * should not be any activity. Then, we move to a time range where there are
     * write activity. Afterward, we test the zoom
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
    @Test
    public void testDiskView() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        IViewPart viewSite = fViewBotDisk.getViewReference().getView(true);
        assertTrue(viewSite instanceof DiskIOActivityView);
        final TmfCommonXLineChartViewer chartViewer = getChartViewer(viewSite);
        assertNotNull(chartViewer);
        fBot.waitUntil(ConditionHelpers.xyViewerIsReadyCondition(chartViewer));

        final Chart chart = fChart;
        assertNotNull(chart);

        WaitUtils.waitUntil(c -> c.getSeriesSet().getSeries().length > 0, chart, "No data available");
        chartViewer.setNbPoints(NUMBER_OF_POINT);

        /* Initially, no disk activity */
        WaitUtils.waitUntil(json -> isChartDataValid(chart, json), "resources/disk0-res50.json", "Chart data is not valid");

        /* Change time range where there is disks activity */
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, new TmfTimeRange(START_TIME_WHERE_THERE_IS_DISK_ACTIVITY, END_TIME_WHERE_THERE_IS_DISK_ACTIVITY)));
        fBot.waitUntil(ConditionHelpers.windowRange(new TmfTimeRange(START_TIME_WHERE_THERE_IS_DISK_ACTIVITY, END_TIME_WHERE_THERE_IS_DISK_ACTIVITY)));
        fBot.waitUntil(ConditionHelpers.xyViewerIsReadyCondition(chartViewer));

        /* Test type, style and color of series */
        verifyChartStyle(chart);

        /* Test data model */
        WaitUtils.waitUntil(json -> isChartDataValid(chart, json), "resources/disk1-res50.json", "Chart data is not valid");

        /* Change Zoom and number of points */
        chartViewer.setNbPoints(MORE_POINTS);

        /* Test type, style and color of series */
        verifyChartStyle(chart);

        /* Test data model */
        WaitUtils.waitUntil(json -> isChartDataValid(chart, json), "resources/disk2-res100.json", "Chart data is not valid");
    }

    private static boolean isChartDataValid(final Chart chart, String expectedJson) {
        /**
         * FIXME : Once CQ for Jackson is approved, use deserialization instead of
         * comparing strings
         */
        String expected = FileUtils.read(expectedJson);
        TmfCommonXAxisModel model = extractModelFromChart(chart);
        String current = fGson.toJson(model);
        return expected.equals(current);
    }

    /**
     * From a SWT Chart, this method extract a {@link TmfCommonXAxisModel} that
     * represents the chart. Since, we unfortunately have no mecanism to deserialize
     * with GSON, we have to compare strings. So, once the model is extract from the
     * Chart, we serialize it and compare with a string
     */
    private static TmfCommonXAxisModel extractModelFromChart(Chart chart) {

        ILineSeries readSeries = (ILineSeries) chart.getSeriesSet().getSeries(READ_SERIES_NAME);
        ILineSeries writeSeries = (ILineSeries) chart.getSeriesSet().getSeries(WRITE_SERIES_NAME);

        assertNotNull(readSeries);
        assertNotNull(writeSeries);

        /* X and Y Values shown in chart */
        double[] xRead = readSeries.getXSeries();
        double[] yRead = readSeries.getYSeries();

        /* X and Y Values shown in chart */
        double[] xWrite = writeSeries.getXSeries();
        double[] yWrite = writeSeries.getYSeries();

        assertTrue("Series should have the same x axis values", Arrays.equals(xRead, xWrite));

        /**
         * FIXME : Please change LinkedHashMap for HashMap when order is no more
         * important
         */
        @NonNull Map<@NonNull String, @NonNull IYModel> yModels = new LinkedHashMap<>();
        yModels.put(READ_SERIES_NAME, new YModel(READ_SERIES_NAME, Objects.requireNonNull(yRead)));
        yModels.put(WRITE_SERIES_NAME, new YModel(WRITE_SERIES_NAME, Objects.requireNonNull(yWrite)));

        long[] x = Longs.toArray(Doubles.asList(xRead));
        assertNotNull(x);
        return new TmfCommonXAxisModel(TITLE, x, yModels);
    }

    private static void verifyChartStyle(Chart chart) {

        ILineSeries readSeries = (ILineSeries) chart.getSeriesSet().getSeries(READ_SERIES_NAME);
        ILineSeries writeSeries = (ILineSeries) chart.getSeriesSet().getSeries(WRITE_SERIES_NAME);

        assertNotNull(readSeries);
        assertNotNull(writeSeries);

        /* Color, type and style */
        assertEquals(readSeries.getType(), ISeries.SeriesType.LINE);
        assertEquals(readSeries.getLineColor().getRGB(), BLUE);
        assertEquals(readSeries.getLineStyle(), LineStyle.SOLID);
        assertTrue(readSeries.isAreaEnabled());

        /* Color, type and style */
        assertEquals(writeSeries.getType(), ISeries.SeriesType.LINE);
        assertEquals(writeSeries.getLineColor().getRGB(), RED);
        assertEquals(writeSeries.getLineStyle(), LineStyle.SOLID);
        assertTrue(writeSeries.isAreaEnabled());
    }

    private static TmfCommonXLineChartViewer getChartViewer(IViewPart viewSite)
            throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {

        DiskIOActivityView diskView = (DiskIOActivityView) viewSite;
        Method viewer = TmfChartView.class.getDeclaredMethod("getChartViewer");
        viewer.setAccessible(true);
        TmfCommonXLineChartViewer chartViewer = (TmfCommonXLineChartViewer) viewer.invoke(diskView);
        return chartViewer;
    }
}