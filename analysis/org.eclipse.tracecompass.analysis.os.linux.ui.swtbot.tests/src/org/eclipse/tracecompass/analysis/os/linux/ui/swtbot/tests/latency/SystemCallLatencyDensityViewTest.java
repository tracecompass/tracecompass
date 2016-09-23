/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.ui.swtbot.tests.latency;

import static org.eclipse.swtbot.swt.finder.SWTBotAssert.assertVisible;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density.AbstractSegmentStoreDensityView;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency.SystemCallLatencyDensityView;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.swtchart.Chart;
import org.swtchart.Range;

/**
 * Tests of the density view
 *
 * @author Matthew Khouzam
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class SystemCallLatencyDensityViewTest {

    private static final String TRACE_TYPE = "org.eclipse.linuxtools.lttng2.kernel.tracetype";
    private static final String PROJECT_NAME = "test";
    private static final String VIEW_ID = SystemCallLatencyDensityView.ID;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private AbstractSegmentStoreDensityView fDensityView;
    private AbstractSegmentStoreTableViewer fDensityViewer;
    private Chart fDensityChart;
    private static SWTWorkbenchBot fBot;

    /**
     * Things to setup
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
        fBot = new SWTWorkbenchBot();
        SWTBotUtils.closeView("welcome", fBot);
        /* Switch perspectives */
        SWTBotUtils.switchToTracingPerspective();
        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();

    }

    /**
     * Opens a latency table
     *
     * @throws SecurityException
     *             If a security manager is present and any the wrong class is
     *             loaded or the class loader is not the same as its ancestor's
     *             loader.
     *
     * @throws NoSuchFieldException
     *             Field not available
     * @throws IllegalAccessException
     *             Field is inaccessible
     * @throws IllegalArgumentException
     *             the object is not the correct class type
     *
     *
     */
    @Before
    public void createDensityViewer() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        /*
         * Open latency view
         */
        SWTBotUtils.openView(VIEW_ID);
        SWTBotView viewBot = fBot.viewById(VIEW_ID);
        final IViewReference viewReference = viewBot.getViewReference();
        IViewPart viewPart = UIThreadRunnable.syncExec(new Result<IViewPart>() {
            @Override
            public IViewPart run() {
                return viewReference.getView(true);
            }
        });
        assertNotNull(viewPart);
        if (!(viewPart instanceof SystemCallLatencyDensityView)) {
            fail("Could not instanciate view");
        }
        fDensityView = (SystemCallLatencyDensityView) viewPart;

        /*
         * Use reflection to access the table viewer
         */
        final Field field = AbstractSegmentStoreDensityView.class.getDeclaredField("fTableViewer");
        field.setAccessible(true);
        fDensityViewer = (AbstractSegmentStoreTableViewer) field.get(fDensityView);
        fDensityChart = viewBot.bot().widget(WidgetOfType.widgetOfType(Chart.class));
        assertNotNull(fDensityViewer);
    }

    /**
     * Closes the view
     */
    @After
    public void closeDensityViewer() {
        SWTBotUtils.closeViewById(VIEW_ID, fBot);
    }

    /**
     * Test with an actual trace, this is more of an integration test than a
     * unit test. This test is a slow one too. If some analyses are not well
     * configured, this test will also generates null pointer exceptions. These
     * will be logged.
     *
     * @throws IOException
     *             trace not found?
     * @throws SecurityException
     *             If a security manager is present and any the wrong class is
     *             loaded or the class loader is not the same as its ancestor's
     *             loader.
     *
     * @throws NoSuchFieldException
     *             Field not available
     * @throws IllegalAccessException
     *             Field is inaccessible
     * @throws IllegalArgumentException
     *             the object is not the correct class type
     *
     */
    @Test
    public void testWithTrace() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        String tracePath;
        tracePath = FileLocator.toFileURL(CtfTestTrace.ARM_64_BIT_HEADER.getTraceURL()).getPath();
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        SWTBotUtils.closeViewById(VIEW_ID, fBot);
        SWTBotUtils.createProject(PROJECT_NAME);
        SWTBotUtils.openTrace(PROJECT_NAME, tracePath, TRACE_TYPE);
        WaitUtils.waitForJobs();
        createDensityViewer();
        WaitUtils.waitForJobs();
        SWTBotTable tableBot = new SWTBotTable(fDensityViewer.getTableViewer().getTable());
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "1,600", 0, 2));
        tableBot.header("Duration").click();
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "1,600", 0, 2));
        tableBot.header("Duration").click();
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "1,001,046,400", 0, 2));
        final Chart densityChart = fDensityChart;
        assertNotNull(densityChart);
        bot.waitUntil(ConditionHelpers.numberOfSeries(densityChart, 1));

        SWTBotChart chartBot = new SWTBotChart(densityChart);
        assertVisible(chartBot);
        assertEquals("", chartBot.getToolTipText());
        final Range range = densityChart.getAxisSet().getXAxes()[0].getRange();
        assertTrue(0 > range.lower);
        assertTrue(1001046400 < range.upper);
        bot.closeAllEditors();
        SWTBotUtils.deleteProject(PROJECT_NAME, bot);
    }

    private static class SWTBotChart extends AbstractSWTBotControl<Chart> {
        public SWTBotChart(Chart w) throws WidgetNotFoundException {
            super(w);
        }
    }
}
