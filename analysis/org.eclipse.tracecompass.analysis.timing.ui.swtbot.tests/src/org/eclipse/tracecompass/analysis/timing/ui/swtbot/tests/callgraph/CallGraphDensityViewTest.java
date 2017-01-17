/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.swtbot.tests.callgraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCanvas;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.core.statistics.IStatistics;
import org.eclipse.tracecompass.analysis.timing.core.statistics.Statistics;
import org.eclipse.tracecompass.analysis.timing.core.tests.flamegraph.AggregationTreeTest;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density.AbstractSegmentStoreDensityViewer;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density.ISegmentStoreDensityViewerDataListener;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.internal.analysis.timing.ui.callgraph.CallGraphDensityView;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.ui.IViewPart;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
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
public class CallGraphDensityViewTest extends AggregationTreeTest {

    private static final String CALLGRAPHDENSITY_ID = CallGraphDensityView.ID;

    private final @NonNull ISegmentStoreDensityViewerDataListener fSyncListener = new ISegmentStoreDensityViewerDataListener() {
        @Override
        public void dataChanged(List<ISegment> newData) {
            fLatch.countDown();
        }

        @Override
        public void dataSelectionChanged(@Nullable List<@NonNull ISegment> newSelectionData) {
            // do nothing
        }

    };
    private SWTWorkbenchBot fBot;
    private SWTBotView fView;
    private CallGraphDensityView fFuncDensityView;
    private SWTBotTable fTableBot;
    private SWTBotCanvas fDensityBot;
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
        fDensityBot = fView.bot().canvas();
        assertNotNull(fDensityBot);
        fDensityViewer = funcDensityView.getDensityViewer();
        assertNotNull(fDensityViewer);
        fLatch = new CountDownLatch(1);
        fDensityViewer.removeDataListener(fSyncListener);
        fDensityViewer.addDataListener(fSyncListener);
        fTableViewer = funcDensityView.getTableViewer();
        assertNotNull(fTableViewer);
        SWTBotUtils.maximize(funcDensityView);
        fFuncDensityView = funcDensityView;
    }

    /**
     * Reset
     */
    @After
    public void after() {
        CallGraphDensityView funcDensityView = fFuncDensityView;
        assertNotNull(funcDensityView);
        SWTBotUtils.maximize(funcDensityView);

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
        assertEquals(3, fTableBot.rowCount());
        ISeries series = getSeries();
        IStatistics<@NonNull Long> sss = getDescriptiveStatistics(series);
        assertEquals(3.0, sss.getTotal(), 0.0);
        assertEquals(0.02, sss.getMean(), 0.02); // low mean
    }

    @Override
    public void mergeFirstLevelCalleesTest() {
        super.mergeFirstLevelCalleesTest();
        loadData();
        assertEquals(5, fTableBot.rowCount());
        ISeries series = getSeries();
        IStatistics<@NonNull Long> sss = getDescriptiveStatistics(series);
        assertEquals(5.0, sss.getTotal(), 0.0);
        assertEquals(0.02, sss.getMean(), 0.03); // low mean
    }

    @Override
    public void multiFunctionRootsSecondTest() {
        super.multiFunctionRootsSecondTest();
        loadData();
        assertEquals(4, fTableBot.rowCount());
        ISeries series = getSeries();
        IStatistics<@NonNull Long> sss = getDescriptiveStatistics(series);
        assertEquals(4.0, sss.getTotal(), 0.0);
        assertEquals(0.02, sss.getMean(), 0.02); // low mean
    }

    @Override
    public void mergeSecondLevelCalleesTest() {
        super.mergeSecondLevelCalleesTest();
        loadData();
        assertEquals(8, fTableBot.rowCount());
        ISeries series = getSeries();
        double[] ySeries = series.getYSeries();
        assertNotNull(ySeries);
        IStatistics<@NonNull Long> sss = getDescriptiveStatistics(series);
        assertEquals(8.0, sss.getTotal(), 0.0);
        assertEquals(0.06, sss.getMean(), 0.02); // average mean
    }

    @Override
    public void multiFunctionRootsTest() {
        super.multiFunctionRootsTest();
        loadData();
        assertEquals(4, fTableBot.rowCount());
        ISeries series = getSeries();
        double[] ySeries = series.getYSeries();
        assertNotNull(ySeries);
        IStatistics<@NonNull Long> sss = getDescriptiveStatistics(series);
        assertEquals(4.0, sss.getTotal(), 0.0);
        assertEquals(0.02, sss.getMean(), 0.02); // low mean
    }

    @Override
    public void treeTest() {
        super.treeTest();
        loadData();
        assertEquals(4, fTableBot.rowCount());
        ISeries series = getSeries();
        double[] ySeries = series.getYSeries();
        assertNotNull(ySeries);
        IStatistics<@NonNull Long> sss = getDescriptiveStatistics(series);
        assertEquals(4.0, sss.getTotal(), 0.0);
        assertEquals(0.02, sss.getMean(), 0.02); // low mean
    }

    @Override
    public void largeTest() {
        super.largeTest();
        loadData();
        assertEquals(1000, fTableBot.rowCount());
        ISeries series = getSeries();
        double[] ySeries = series.getYSeries();
        assertNotNull(ySeries);
        IStatistics<@NonNull Long> sss = getDescriptiveStatistics(series);
        assertEquals(1000.0, sss.getTotal(), 0.0);
        assertEquals(8, sss.getMean(), 1); // high mean
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

    private static IStatistics<@NonNull Long> getDescriptiveStatistics(ISeries series) {
        double[] ySeries = series.getYSeries();
        assertNotNull(ySeries);
        IStatistics<@NonNull Long> stats = new Statistics<>();
        for (double item : ySeries) {
            stats.update((long) (item - 1.0));
        }
        return stats;
    }

}
