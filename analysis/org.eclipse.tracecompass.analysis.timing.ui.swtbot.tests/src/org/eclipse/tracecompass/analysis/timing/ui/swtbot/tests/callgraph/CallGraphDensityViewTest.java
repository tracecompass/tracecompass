/*******************************************************************************
 * Copyright (c) 2016, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.swtbot.tests.callgraph;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.core.tests.flamegraph.AggregationTreeTest;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density.AbstractSegmentStoreDensityViewer;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density.ISegmentStoreDensityViewerDataListener;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.internal.analysis.timing.ui.callgraph.CallGraphDensityView;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.ui.IViewPart;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.swtchart.ISeries;

/**
 * Test the call graph density view, known as the Function Density view. The
 * density view can change its data with respect to screen resolution, so
 * descriptive statistics are used to check validity. This is one of the rare
 * occasions that the error in the
 * {@link Assert#assertEquals(double, double, double)} is actually quite useful
 *
 * @author Matthew Khouzam
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class CallGraphDensityViewTest extends AggregationTreeTest {

    private static final String CALLGRAPHDENSITY_ID = CallGraphDensityView.ID;

    private final @NonNull ISegmentStoreDensityViewerDataListener fSyncListener = new ISegmentStoreDensityViewerDataListener() {
        @Override
        public void chartUpdated() {
            fLatch.countDown();
        }
    };
    private SWTWorkbenchBot fBot;
    private SWTBotView fView;
    private CallGraphDensityView fFuncDensityView;
    private SWTBotTable fTableBot;
    private AbstractSegmentStoreDensityViewer fDensityViewer;
    private AbstractSegmentStoreTableViewer fTableViewer;
    private CountDownLatch fLatch;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    /**
     * Initialization
     */
    @BeforeClass
    public static void beforeClass() {

        SWTBotUtils.initialize();
        Thread.currentThread().setName("SWTBotTest");
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        SWTBotUtils.closeView("welcome", bot);
        /* Switch perspectives */
        SWTBotUtils.switchToTracingPerspective();
        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
    }

    /**
     * Clean up
     */
    @AfterClass
    public static void afterClass() {
        fLogger.removeAllAppenders();
    }

    /**
     * Setup for the test
     */
    @Before
    public void before() {
        fBot = new SWTWorkbenchBot();
        SWTBotUtils.openView(CALLGRAPHDENSITY_ID);
        SWTBotView view = fBot.viewById(CALLGRAPHDENSITY_ID);
        assertNotNull(view);
        fView = view;
        CallGraphDensityView funcDensityView = UIThreadRunnable.syncExec((Result<CallGraphDensityView>) () -> {
            IViewPart viewRef = fView.getViewReference().getView(true);
            return (viewRef instanceof CallGraphDensityView) ? (CallGraphDensityView) viewRef : null;
        });
        assertNotNull(funcDensityView);
        fTableBot = fView.bot().table();
        assertNotNull(fTableBot);
        fDensityViewer = funcDensityView.getDensityViewer();
        assertNotNull(fDensityViewer);
        fLatch = new CountDownLatch(1);
        fDensityViewer.removeDataListener(fSyncListener);
        fDensityViewer.addDataListener(fSyncListener);
        fTableViewer = funcDensityView.getTableViewer();
        assertNotNull(fTableViewer);
        SWTBotUtils.maximize(funcDensityView);
        fFuncDensityView = funcDensityView;
        fDensityViewer.setNbPoints(100);
    }

    /**
     * Reset
     */
    @After
    public void after() {
        CallGraphDensityView funcDensityView = fFuncDensityView;
        assertNotNull(funcDensityView);
        SWTBotUtils.maximize(funcDensityView);
        setCga(null);

    }

    @Override
    public void emptyStateSystemTest() {
        super.emptyStateSystemTest();
        loadData();
        assertEquals(0, fTableBot.rowCount());
        ISeries series = getSeries();
        assertNotNull(series);
    }

    @Override
    public void cascadeTest() {
        super.cascadeTest();
        loadData();
        waitForTable(3);
        assertEquals(3, fTableBot.rowCount());
        double[] expected = { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 4.0 };
        waitForSeries(expected);
        assertArrayEquals(expected, getSeries().getYSeries(), 0.1);
    }

    @Override
    public void mergeFirstLevelCalleesTest() {
        super.mergeFirstLevelCalleesTest();
        loadData();
        waitForTable(5);
        assertEquals(5, fTableBot.rowCount());
        double[] expected = { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 3.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0 };
        waitForSeries(expected);
        assertArrayEquals(expected, getSeries().getYSeries(), 0.1);
    }

    @Override
    public void multiFunctionRootsSecondTest() {
        super.multiFunctionRootsSecondTest();
        loadData();
        waitForTable(4);
        assertEquals(4, fTableBot.rowCount());
        double[] expected = { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 3.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 3.0, 1.0, 1.0, 1.0, 1.0 };
        waitForSeries(expected);
        assertArrayEquals(expected, getSeries().getYSeries(), 0.1);
    }

    @Override
    public void mergeSecondLevelCalleesTest() {
        super.mergeSecondLevelCalleesTest();
        loadData();
        waitForTable(8);
        assertEquals(8, fTableBot.rowCount());
        double[] expected = { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 4.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0 };
        waitForSeries(expected);
        assertArrayEquals(expected, getSeries().getYSeries(), 0.1);
    }

    @Override
    public void multiFunctionRootsTest() {
        super.multiFunctionRootsTest();
        loadData();
        waitForTable(4);
        assertEquals(4, fTableBot.rowCount());
        double[] expected = { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 3.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 3.0, 1.0, 1.0, 1.0, 1.0 };
        waitForSeries(expected);
        assertArrayEquals(expected, getSeries().getYSeries(), 0.1);
    }

    @Override
    public void treeTest() {
        super.treeTest();
        loadData();
        waitForTable(4);
        assertEquals(4, fTableBot.rowCount());
        double[] expected = { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 3.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0 };
        waitForSeries(expected);
        assertArrayEquals(expected, getSeries().getYSeries(), 0.1);
    }

    @Override
    public void largeTest() {
        super.largeTest();
        loadData();
        waitForTable(1000);
        assertEquals(1000, fTableBot.rowCount());
        double[] expected = { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1001.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 };
        waitForSeries(expected);
        assertArrayEquals(expected, getSeries().getYSeries(), 0.1);
    }

    private ISeries getSeries() {
        AbstractSegmentStoreDensityViewer densityViewer = fDensityViewer;
        assertNotNull(densityViewer);
        ISeries[] serieses = densityViewer.getControl().getSeriesSet().getSeries();
        assertNotNull(serieses);
        assertTrue(serieses.length > 0);
        ISeries series = serieses[0];
        assertNotNull(series);
        return series;
    }

    private void loadData() {
        final ISegmentStoreProvider cga = getCga();

        UIThreadRunnable.syncExec(() -> {
            fTableViewer.setData(cga);
            fDensityViewer.setSegmentProvider(cga);
            fDensityViewer.updateWithRange(TmfTimeRange.ETERNITY);
            fDensityViewer.refresh();
            fTableViewer.refresh();
        });
        if (cga != null) {
            try {
                /*
                 * timeout of the test
                 */
                assertTrue(fLatch.await(20, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                fail(e.getMessage());
            }
        }

    }

    private void waitForSeries(double[] expected) {
        SWTBotUtils.waitUntil(arg -> {
            UIThreadRunnable.syncExec(() -> {
                fDensityViewer.refresh();
                fTableViewer.refresh();
            });
            double[] ySeries = getSeries().getYSeries();
            return Arrays.equals(expected, ySeries);
        }, null, "Unable to refresh viewer series");
    }

    private void waitForTable(int nbOfRows) {
        SWTBotUtils.waitUntil(tableBot -> {
            UIThreadRunnable.syncExec(() -> {
                fTableViewer.refresh();
            });
            return tableBot.rowCount() == nbOfRows;
        }, fTableBot, "Unable to refresh the table");
    }

}
