/*******************************************************************************
 * Copyright (c) 2015 Ericsson
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
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.tracecompass.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.tracecompass.tmf.ui.project.wizards.NewTmfProjectWizard;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotSash;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.views.callstack.CallStackView;
import org.eclipse.tracecompass.tmf.ui.views.histogram.HistogramView;
import org.eclipse.tracecompass.tmf.ui.views.timechart.TimeChartView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
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
    private static final BaseMatcher<Sash> SASH_MATCHER = new SashMatcher();

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
     * Test 3 views, none overlap, the histogram, the callstack and the
     * timechart are aligned, the histogram is moved, callstack and timechart
     * follow
     */
    @Test
    public void testMoveHistogramCallstackFollows() {
        fBot = new SWTWorkbenchBot();
        SWTBotUtils.switchToPerspective(AlignPerspectiveFactory1.ID);
        testOverlap(HistogramView.ID, CallStackView.ID);
    }

    /**
     * Test 3 views, none overlap, the histogram, the callstack and the
     * timechart are aligned, the callstack is moved, histogram and timechart
     * follow
     */
    @Test
    public void testMoveCallstackHistogramFollows() {
        fBot = new SWTWorkbenchBot();
        SWTBotUtils.switchToPerspective(AlignPerspectiveFactory1.ID);
        testOverlap(CallStackView.ID, HistogramView.ID);
    }

    /**
     * Test 3 views, overlap on the resizing view. The timechart and callstack
     * are overlapping. Test that when the histogram moves, the timechart
     * follows. Hidden views are not tested as their state does not matter until
     * they are displayed
     */
    @Test
    public void testOverlappingHistogramMove() {
        fBot = new SWTWorkbenchBot();
        SWTBotUtils.switchToPerspective(AlignPerspectiveFactory2.ID);
        testOverlap(HistogramView.ID, CallStackView.ID);
    }

    /**
     * Test 3 views, overlap on the resizing view. The timechart and callstack
     * are overlapping. Test that when the timechart moves, the histogram
     * follows. Hidden views are not tested as their state does not matter until
     * they are displayed
     */
    @Test
    public void testOverlappingTimechartMove() {
        fBot = new SWTWorkbenchBot();
        SWTBotUtils.switchToPerspective(AlignPerspectiveFactory2.ID);
        testOverlap(CallStackView.ID, HistogramView.ID);
    }

    /**
     * Test 3 views. No overlap. The callstack is not aligned with the
     * histogram, the histogram is moved, we check that the callstack does NOT
     * follow
     */
    @Test
    public void testNotOverlappingHistogramMove() {
        fBot = new SWTWorkbenchBot();
        testNonOverlap(HistogramView.ID, CallStackView.ID);
    }

    /**
     * Test 3 views. No overlap. The callstack is not aligned with the
     * histogram, the callstack is moved, we check that the histogram does NOT
     * follow
     */
    @Test
    public void testNotOverlappingCallstackMove() {
        fBot = new SWTWorkbenchBot();
        testNonOverlap(CallStackView.ID, HistogramView.ID);
    }

    private static void testNonOverlap(String vId1, String vId2) {
        final int offset = 100;
        // switch to the proper perspective and wait for views to align
        SWTBotUtils.switchToPerspective(AlignPerspectiveFactory3.ID);
        WaitUtils.waitForJobs();
        SWTBotUtils.delay(SYNC_DELAY);
        // get views
        SWTBotView masterView = fBot.viewById(vId1);
        SWTBotView slaveView = fBot.viewById(vId2);
        final Sash slaveSash = slaveView.bot().widget(SASH_MATCHER, 0);
        SWTBotSash slaveSashBot = new SWTBotSash(slaveSash, null);
        Point before = slaveSashBot.getPoint();
        // move master and wait for slaves to follow
        drag(masterView, offset);
        WaitUtils.waitForJobs();
        SWTBotUtils.delay(SYNC_DELAY);
        // verify that the slave did not follow
        assertEquals(before, slaveSashBot.getPoint());
        // put everything back the way it was
        drag(masterView, -offset);
        SWTBotUtils.delay(SYNC_DELAY);
    }

    private static final class SashMatcher extends BaseMatcher<Sash> {
        @Override
        public boolean matches(Object item) {
            return (item instanceof Sash);
        }

        @Override
        public void describeTo(Description description) {
        }
    }

    private static final class SashFormMatcher extends BaseMatcher<SashForm> {
        @Override
        public boolean matches(Object item) {
            return (item instanceof SashForm);
        }

        @Override
        public void describeTo(Description description) {
        }
    }

    /**
     * Simulate a drag operation using "setWeights"
     */
    private static void drag(final SWTBotView view, final int offset) {
        // this is the final sash form
        final SashForm sashForm = view.bot().widget(new SashFormMatcher(), 0);
        assertNotNull(sashForm);
        // resize widgets using sashform
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                int[] originalWeights = sashForm.getWeights();
                int[] newWeights = Arrays.copyOf(originalWeights, originalWeights.length);
                newWeights[0] += offset;
                newWeights[1] -= offset;
                sashForm.setWeights(newWeights);
                sashForm.getParent().layout();
            }
        });
        // send update signals
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                sashForm.getChildren()[0].notifyListeners(SWT.Resize, null);
                sashForm.getChildren()[1].notifyListeners(SWT.Resize, null);
                /*
                 * This one is the most important, the previous two are added to
                 * be a good citizen, this event (selection) is the one that
                 * triggers an alignment
                 */
                sashForm.getChildren()[2].notifyListeners(SWT.Selection, null);
            }
        });
    }

    private static void testOverlap(String masterView, String slaveView) {
        final int offset = 100;
        final int delta = offset / 2;
        // wait for the perspective switch to propagate alignments
        WaitUtils.waitForJobs();
        SWTBotUtils.delay(SYNC_DELAY);
        // select master and slave parts to observe
        SWTBotView masterViewBot = fBot.viewById(masterView);
        final Sash masterSash = masterViewBot.bot().widget(SASH_MATCHER, 0);
        SWTBotSash masterSashBot = new SWTBotSash(masterSash, null);

        SWTBotView slaveViewBot = fBot.viewById(slaveView);
        final Sash slaveSash = slaveViewBot.bot().widget(SASH_MATCHER, 0);
        SWTBotSash slaveSashBot = new SWTBotSash(slaveSash, null);

        double masterOriginalSashX = masterSashBot.getPoint().x;
        // check that the views are already aligned
        assertEquals("Approx align", masterOriginalSashX, slaveSashBot.getPoint().x, delta);
        // move sash and wait for alignment
        drag(masterViewBot, offset);
        WaitUtils.waitForJobs();
        SWTBotUtils.delay(SYNC_DELAY);
        // check results
        double masterNewSashX = masterSashBot.getPoint().x;
        assertEquals("Approx align", masterNewSashX, slaveSashBot.getPoint().x, delta);
        assertEquals(masterOriginalSashX, masterNewSashX - offset, delta);
        // put things back the way they were
        drag(masterViewBot, -offset);
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
            IFolderLayout topLeftFolder = layout.createFolder("topLeftFolder", IPageLayout.LEFT, 0.15f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
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
            IFolderLayout topLeftFolder = layout.createFolder("topLeftFolder", IPageLayout.LEFT, 0.15f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
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
            IFolderLayout topLeftFolder = layout.createFolder("topLeftFolder", IPageLayout.LEFT, 0.15f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
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