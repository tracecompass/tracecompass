/*******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http:/www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.viewers.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRootMenu;
import org.eclipse.tracecompass.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.tracecompass.tmf.ui.project.wizards.NewTmfProjectWizard;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotSash;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotTimeGraph;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.views.callstack.CallStackView;
import org.eclipse.tracecompass.tmf.ui.views.histogram.HistogramView;
import org.eclipse.tracecompass.tmf.ui.views.timechart.TimeChartView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test common time axis for views
 *
 * @author Matthew Khouzam
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class TmfAlignTimeAxisTest {

    /**
     * wait for throttler (2x the throttler time)
     */
    private static final int SYNC_DELAY = 1000;
    private static final String TRACE_START = "<trace>";
    private static final String EVENT_BEGIN = "<event timestamp=\"";
    private static final String EVENT_MIDDLE = " \" name=\"event\"><field name=\"field\" value=\"";
    private static final String EVENT_END = "\" type=\"int\" />" + "</event>";
    private static final String TRACE_END = "</trace>";

    private static final String PROJET_NAME = "TestAxisAlignment";
    private static final int NUM_EVENTS = 100;

    private static final String ALIGN_VIEWS_ACTION_NAME = "Align Views";

    /**
     * Using a small ratio for the editor so that other views have enough space
     * to be drawn, even when a low screen resolution is used.
     */
    private static final float EDITOR_AREA_RATIO = 0.10f;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private static SWTWorkbenchBot fBot;

    private static String makeEvent(int ts, int val) {
        return EVENT_BEGIN + Integer.toString(ts) + EVENT_MIDDLE + Integer.toString(val) + EVENT_END + "\n";
    }

    private static File fLocation;

    /**
     * Initialization, creates a temp trace
     *
     * @throws IOException
     *             should not happen
     */
    @BeforeClass
    public static void init() throws IOException {
        SWTBotUtils.initialize();
        Thread.currentThread().setName("SWTBot Thread"); // for the debugger
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout()));
        SWTWorkbenchBot bot = new SWTWorkbenchBot();

        SWTBotUtils.closeView("welcome", bot);

        SWTBotUtils.switchToTracingPerspective();
        /* finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
        fLocation = File.createTempFile("sample", ".xml");
        try (BufferedRandomAccessFile braf = new BufferedRandomAccessFile(fLocation, "rw")) {
            braf.writeBytes(TRACE_START);
            for (int i = 0; i < NUM_EVENTS; i++) {
                braf.writeBytes(makeEvent(i * 100, i % 4));
            }
            braf.writeBytes(TRACE_END);
        }
        SWTBotUtils.createProject(PROJET_NAME);
        SWTBotUtils.selectTracesFolder(bot, PROJET_NAME);
    }

    /**
     * Delete file
     */
    @AfterClass
    public static void cleanup() {
        SWTBotUtils.deleteProject(PROJET_NAME, new SWTWorkbenchBot());
        fLocation.delete();
        fLogger.removeAllAppenders();
    }

    /**
     * Open the trace
     */
    @Before
    public void before() {
        SWTBotUtils.openTrace(PROJET_NAME, fLocation.getAbsolutePath(), "org.eclipse.linuxtools.tmf.core.tests.xmlstub");
    }

    /**
     * Close the trace
     */
    @After
    public void after() {
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        SWTBotUtils.activeEventsEditor(bot).close();
        /* Switch back to Tracing perspective since several tests change the perspective */
        SWTBotUtils.switchToTracingPerspective();
        SWTBotUtils.closeSecondaryShells(fBot);
    }

    /**
     * Test 3 views, none overlap, the histogram, callstack and timechart are
     * aligned. The histogram is moved, we check that the callstack and
     * timechart follow.
     */
    @Test
    public void testMoveHistogramOthersFollow() {
        fBot = new SWTWorkbenchBot();
        switchToPerspective(AlignPerspectiveFactory1.ID);
        testAligned(HistogramView.ID, CallStackView.ID, TimeChartView.ID);
    }

    /**
     * Test 3 views, none overlap, the histogram, callstack and timechart are
     * aligned. The callstack is moved, we check that the histogram and
     * timechart follow.
     */
    @Test
    public void testMoveCallstackOthersFollow() {
        fBot = new SWTWorkbenchBot();
        switchToPerspective(AlignPerspectiveFactory1.ID);
        testAligned(CallStackView.ID, HistogramView.ID, TimeChartView.ID);
    }

    /**
     * Test 3 views, overlap on the resizing view. The histogram and timechart
     * are overlapping, and the callstack is aligned. The histogram is moved, we
     * check that the callstack follows. The hidden timechart is not checked.
     */
    @Test
    public void testOverlappingHistogramMove() {
        fBot = new SWTWorkbenchBot();
        switchToPerspective(AlignPerspectiveFactory2.ID);
        testAligned(HistogramView.ID, CallStackView.ID);
    }

    /**
     * Test 3 views, overlap on the resizing view. The histogram and timechart
     * are overlapping, and the callstack is aligned. The callstack is moved, we
     * check that the histogram follows. The hidden timechart is not checked.
     */
    @Test
    public void testOverlappingCallstackMove() {
        fBot = new SWTWorkbenchBot();
        switchToPerspective(AlignPerspectiveFactory2.ID);
        testAligned(CallStackView.ID, HistogramView.ID);
    }

    /**
     * Test 3 views. No overlap. The histogram and timechart are aligned, but
     * the callstack is not aligned. The histogram is moved, we check that the
     * timechart follows and that the callstack does NOT follow.
     */
    @Test
    public void testNotOverlappingHistogramMove() {
        fBot = new SWTWorkbenchBot();
        switchToPerspective(AlignPerspectiveFactory3.ID);
        testAligned(HistogramView.ID, TimeChartView.ID);
        testNotAligned(HistogramView.ID, CallStackView.ID);
    }

    /**
     * Test 3 views. No overlap. The histogram and timechart are aligned, but
     * the callstack is not aligned. The callstack is moved, we check that the
     * histogram and timechart do NOT follow.
     */
    @Test
    public void testNotOverlappingCallstackMove() {
        fBot = new SWTWorkbenchBot();
        switchToPerspective(AlignPerspectiveFactory3.ID);
        testNotAligned(CallStackView.ID, HistogramView.ID, TimeChartView.ID);
    }

    /**
     * Test for the "Align Views" menu item
     */
    @Test
    public void testMenuItem() {
        fBot = new SWTWorkbenchBot();
        switchToPerspective(AlignPerspectiveFactory1.ID);
        SWTBotView viewBot = fBot.viewById(CallStackView.ID);
        SWTBotRootMenu viewMenu = viewBot.viewMenu();

        SWTBotMenu menuItems = viewMenu.menu(ALIGN_VIEWS_ACTION_NAME);
        assertTrue("Align views", menuItems.isChecked());
    }

    private static void switchToPerspective(String id) {
        // switch to the proper perspective and wait for views to align
        SWTBotUtils.switchToPerspective(id);
        WaitUtils.waitForJobs();
        SWTBotUtils.delay(SYNC_DELAY);
    }

    private static AbstractSWTBot<?> getAlignmentControl(String viewId) {
        SWTBotView viewBot = fBot.viewById(viewId);
        switch (viewId) {
        case HistogramView.ID:
            return new SWTBotSash(viewBot.bot().widget(WidgetOfType.widgetOfType(Sash.class)));
        case CallStackView.ID:
        case TimeChartView.ID:
            return new SWTBotTimeGraph(viewBot.bot().widget(WidgetOfType.widgetOfType(TimeGraphControl.class)));
        default:
            return null;
        }
    }

    private static void testAligned(String masterView, String... slaveViews) {
        final int offset = 50;

        // select alignment controls and get their original alignment positions
        AbstractSWTBot<?> master = getAlignmentControl(masterView);
        int masterOrigin = getAlignmentPosition(master);
        Map<AbstractSWTBot<?>, Integer> slaveMap = new HashMap<>();
        for (String slaveView : slaveViews) {
            AbstractSWTBot<?> slave = getAlignmentControl(slaveView);
            int slaveOrigin = getAlignmentPosition(slave);
            slaveMap.put(slave, slaveOrigin);
        }

        // change master position
        setAlignmentPosition(master, masterOrigin + offset);

        // check resulting alignment positions, slaves follow
        assertEquals(masterOrigin + offset, getAlignmentPosition(master), 2);
        for (Entry<AbstractSWTBot<?>, Integer> slave : slaveMap.entrySet()) {
            assertEquals(slave.getValue() + offset, getAlignmentPosition(slave.getKey()), 2);
        }

        // reset original alignment position
        setAlignmentPosition(master, masterOrigin);
    }

    private static void testNotAligned(String masterView, String... nonSlaveViews) {
        final int offset = 50;

        // select alignment controls and get their original alignment positions
        AbstractSWTBot<?> master = getAlignmentControl(masterView);
        int masterOrigin = getAlignmentPosition(master);
        Map<AbstractSWTBot<?>, Integer> nonSlaveMap = new HashMap<>();
        for (String nonSlaveView : nonSlaveViews) {
            AbstractSWTBot<?> nonSlave = getAlignmentControl(nonSlaveView);
            int nonSlaveOrigin = getAlignmentPosition(nonSlave);
            nonSlaveMap.put(nonSlave, nonSlaveOrigin);
        }

        // change master position
        setAlignmentPosition(master, masterOrigin + offset);

        // check resulting alignment positions, non-slaves do not follow
        assertEquals(masterOrigin + offset, getAlignmentPosition(master), 2);
        for (Entry<AbstractSWTBot<?>, Integer> nonSlave : nonSlaveMap.entrySet()) {
            assertEquals((int) nonSlave.getValue(), getAlignmentPosition(nonSlave.getKey()));
        }

        // reset original alignment position
        setAlignmentPosition(master, masterOrigin);
    }

    private static int getAlignmentPosition(AbstractSWTBot<?> control) {
        if (control instanceof SWTBotSash) {
            Rectangle bounds = ((SWTBotSash) control).getBounds();
            return bounds.x + bounds.width / 2;
        } else if (control instanceof SWTBotTimeGraph) {
            return ((SWTBotTimeGraph) control).getNameSpace();
        }
        return 0;
    }

    private static void setAlignmentPosition(AbstractSWTBot<?> control, int position) {
        if (control instanceof SWTBotSash) {
            SWTBotSash sash = (SWTBotSash) control;
            Rectangle bounds = sash.getBounds();
            Point dst = new Point(position, bounds.y + bounds.height / 2);
            sash.drag(dst);
        } else if (control instanceof SWTBotTimeGraph) {
            ((SWTBotTimeGraph) control).setNameSpace(position);
        }
        // wait for alignment
        WaitUtils.waitForJobs();
        SWTBotUtils.delay(SYNC_DELAY);
    }

    /**
     * Histogram, Callstack and timechart aligned but in different sites
     */
    public static class AlignPerspectiveFactory1 implements IPerspectiveFactory {

        /** The Perspective ID */
        public static final String ID = "org.eclipse.linuxtools.tmf.test.align.1"; //$NON-NLS-1$

        @Override
        public void createInitialLayout(IPageLayout layout) {
            if (layout == null) {
                return;
            }

            // Editor area
            layout.setEditorAreaVisible(true);

            // Editor area
            layout.setEditorAreaVisible(true);

            // Create the top left folder
            IFolderLayout topLeftFolder = layout.createFolder("topLeftFolder", IPageLayout.LEFT, 0.4f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
            topLeftFolder.addView(IPageLayout.ID_PROJECT_EXPLORER);

            // Create the top right folder
            IFolderLayout topRightFolder = layout.createFolder("topRightFolder", IPageLayout.BOTTOM, EDITOR_AREA_RATIO, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
            topRightFolder.addView(HistogramView.ID);

            // Create the middle right folder
            IFolderLayout middleRightFolder = layout.createFolder("middleRightFolder", IPageLayout.BOTTOM, 0.50f, "topRightFolder"); //$NON-NLS-1$
            middleRightFolder.addView(CallStackView.ID);

            // Create the bottom right folder
            IFolderLayout bottomRightFolder = layout.createFolder("bottomRightFolder", IPageLayout.BOTTOM, 0.65f, "middleRightFolder"); //$NON-NLS-1$ //$NON-NLS-2$
            bottomRightFolder.addView(TimeChartView.ID);

            // Populate menus, etc
            layout.addPerspectiveShortcut(ID);
            layout.addNewWizardShortcut(NewTmfProjectWizard.ID);
        }

    }

    /**
     * Timechart and Histogram share a site, all views aligned
     */
    public static class AlignPerspectiveFactory2 implements IPerspectiveFactory {

        /** The Perspective ID */
        public static final String ID = "org.eclipse.linuxtools.tmf.test.align.2"; //$NON-NLS-1$

        @Override
        public void createInitialLayout(IPageLayout layout) {
            if (layout == null) {
                return;
            }

            // Editor area
            layout.setEditorAreaVisible(true);

            // Editor area
            layout.setEditorAreaVisible(true);

            // Create the top left folder
            IFolderLayout topLeftFolder = layout.createFolder("topLeftFolder", IPageLayout.LEFT, 0.4f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
            topLeftFolder.addView(IPageLayout.ID_PROJECT_EXPLORER);

            // Create the middle right folder
            IFolderLayout middleRightFolder = layout.createFolder("middleRightFolder", IPageLayout.BOTTOM, EDITOR_AREA_RATIO, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
            middleRightFolder.addView(HistogramView.ID);
            middleRightFolder.addView(TimeChartView.ID);

            // Create the bottom right folder
            IFolderLayout bottomRightFolder = layout.createFolder("bottomRightFolder", IPageLayout.BOTTOM, 0.65f, "middleRightFolder"); //$NON-NLS-1$ //$NON-NLS-2$
            bottomRightFolder.addView(CallStackView.ID);

            // Populate menus, etc
            layout.addPerspectiveShortcut(ID);
            layout.addNewWizardShortcut(NewTmfProjectWizard.ID);
        }

    }

    /**
     * Histogram and timechart aligned, callstack not aligned
     */
    public static class AlignPerspectiveFactory3 implements IPerspectiveFactory {

        /** The Perspective ID */
        public static final String ID = "org.eclipse.linuxtools.tmf.test.align.3"; //$NON-NLS-1$

        @Override
        public void createInitialLayout(IPageLayout layout) {
            if (layout == null) {
                return;
            }

            // Editor area
            layout.setEditorAreaVisible(true);

            // Editor area
            layout.setEditorAreaVisible(true);

            // Create the top left folder
            IFolderLayout topLeftFolder = layout.createFolder("topLeftFolder", IPageLayout.LEFT, 0.4f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
            topLeftFolder.addView(IPageLayout.ID_PROJECT_EXPLORER);

            IFolderLayout bottomLeftFolder = layout.createFolder("bottomLeftFolder", IPageLayout.BOTTOM, 0.5f, "topLeftFolder"); //$NON-NLS-1$
            bottomLeftFolder.addView(CallStackView.ID);

            // Create the middle right folder
            IFolderLayout middleRightFolder = layout.createFolder("middleRightFolder", IPageLayout.BOTTOM, EDITOR_AREA_RATIO, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
            middleRightFolder.addView(HistogramView.ID);

            // Create the bottom right folder
            IFolderLayout bottomRightFolder = layout.createFolder("bottomRightFolder", IPageLayout.BOTTOM, 0.65f, "middleRightFolder"); //$NON-NLS-1$ //$NON-NLS-2$
            bottomRightFolder.addView(TimeChartView.ID);

            // Populate menus, etc
            layout.addPerspectiveShortcut(ID);
            layout.addNewWizardShortcut(NewTmfProjectWizard.ID);
        }

    }

}