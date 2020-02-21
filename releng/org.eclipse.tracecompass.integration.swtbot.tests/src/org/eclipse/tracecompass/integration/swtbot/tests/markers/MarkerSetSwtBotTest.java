/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.integration.swtbot.tests.markers;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRootMenu;
import org.eclipse.tracecompass.analysis.profiling.ui.views.flamechart.FlameChartView;
import org.eclipse.tracecompass.lttng2.ust.core.tests.shared.LttngUstTestTraceUtils;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for marker set in timegraph view. This test uses the Call Stack View.
 *
 * @author Jean-Christian Kouame
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class MarkerSetSwtBotTest {

    /**
     * Valid marker set content
     */
    private static final String XML_CONTENT =
            "<marker-sets>" +
            "  <marker-set name=\"Set A\" id=\"set.a\">" +
            "    <marker name=\"Marker A-1\" label=\"A-1 %d\" id=\"marker.a.1\" referenceid=\"ref.a.1\" color=\"blue\" period=\"10\" unit=\"ns\" range=\"0..4095\" offset=\"0\">" +
            "    </marker>" +
            "  </marker-set>" +
            "<marker-set name=\"Set B\" id=\"set.b\">" +
            "    <marker name=\"Marker B-1\" label=\"B-1 %d\" id=\"marker.b.1\" referenceid=\"ref.b.1\" color=\"#010203\" period=\"1000\" unit=\"cycles\" range=\"1..\" offset=\"5\">" +
            "    </marker>" +
            "  </marker-set>" +
            "</marker-sets>";

    private static final String EDITOR_TITLE = "markers.xml";
    /** The Log4j logger instance. */
    private static final String TRACE_PROJECT_NAME = "test_project";
    private static final String UST_ID = "org.eclipse.linuxtools.lttng2.ust.tracetype";
    private static SWTWorkbenchBot fBot;
    private static SWTBotView fViewBot;
    private static String[] MENU_ITEMS_NO_MARKERS_SET = new String[] { "None", "Example", "", "Edit..." };
    private static String[] MENU_ITEMS_2_MARKERS_SET = new String[] { "None", "Set A", "Set B","Example", "", "Edit..." };
    private static long fStart;
    private static TmfTimeRange fFullRange;

    /**
     * Set up the test context and environment
     */
    @BeforeClass
    public static void setUp() {
        SWTBotUtils.initialize();

        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 10000; /* 10 second timeout */
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        fBot = new SWTWorkbenchBot();

        SWTBotUtils.createProject(TRACE_PROJECT_NAME);
        WaitUtils.waitForJobs();

        final CtfTestTrace cygProfile = CtfTestTrace.CYG_PROFILE;
        LttngUstTrace trace = LttngUstTestTraceUtils.getTrace(cygProfile);
        fStart = ((CtfTmfTrace) trace).getStartTime().toNanos();
        fFullRange = new TmfTimeRange(TmfTimestamp.fromNanos(fStart), TmfTimestamp.fromNanos(fStart + 100l));
        final File file = new File(trace.getPath());
        LttngUstTestTraceUtils.dispose(cygProfile);
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, file.getAbsolutePath(), UST_ID);
        SWTBotUtils.openView(FlameChartView.ID);
        fViewBot = fBot.viewByTitle("Flame Chart");
        WaitUtils.waitForJobs();
    }

    /**
     * Test the marker set menu items when there is no marker set
     */
    @Test
    public void testMenuNoMarkerSet() {
        SWTBotRootMenu viewMenu = fViewBot.viewMenu();
        List<String> menuItems = viewMenu.menu("Marker Set").menuItems();
        assertArrayEquals("menu items, no marker set", MENU_ITEMS_NO_MARKERS_SET, menuItems.toArray());
    }

    /**
     * Test the marker set menu Edit... item
     */
    @Test
    public void testEditMarkerSet() {
        insertContent();
        SWTBotRootMenu viewMenu = fViewBot.viewMenu();
        List<String> menuItems = viewMenu.menu("Marker Set").menuItems();
        assertArrayEquals("menu items, two marker sets", MENU_ITEMS_2_MARKERS_SET, menuItems.toArray());
        assertTrue("None is checked", viewMenu.menu("Marker Set").menu(MENU_ITEMS_2_MARKERS_SET[0]).isChecked());

        // Select Set A
        viewMenu.menu("Marker Set", MENU_ITEMS_2_MARKERS_SET[1]).click();
        assertTrue("Set A is checked", viewMenu.menu("Marker Set").menu(MENU_ITEMS_2_MARKERS_SET[1]).isChecked());

        // Select Set B
        viewMenu.menu("Marker Set", MENU_ITEMS_2_MARKERS_SET[2]).click();
        assertTrue("set B is checked", viewMenu.menu("Marker Set").menu(MENU_ITEMS_2_MARKERS_SET[2]).isChecked());

        // Select None
        viewMenu.menu("Marker Set", MENU_ITEMS_2_MARKERS_SET[0]).click();
        assertTrue("None is checked", viewMenu.menu("Marker Set").menu(MENU_ITEMS_2_MARKERS_SET[0]).isChecked());

        // Remove all markers set
        removeContent();
        menuItems = viewMenu.menu("Marker Set").menuItems();
        assertArrayEquals("menu items, no marker set", MENU_ITEMS_NO_MARKERS_SET, menuItems.toArray());
    }

    /**
     * Test navigation between markers
     */
    @Test
    public void testNavigateBetweenMarkers() {
        AbstractTimeGraphView part = (AbstractTimeGraphView) fViewBot.getViewReference().getPart(false);
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, fFullRange));
        fBot.waitUntil(ConditionHelpers.windowRange(fFullRange));

        insertContent();
        fViewBot.setFocus();
        SWTBotRootMenu viewMenu = fViewBot.viewMenu();
        viewMenu.menu("Marker Set", "Set A").click();
        fBot.waitUntil(ConditionHelpers.timeGraphIsReadyCondition(part, new TmfTimeRange(TmfTimestamp.fromNanos(fStart), TmfTimestamp.fromNanos(fStart)), TmfTimestamp.fromNanos(fStart)));
        fViewBot.toolbarButton("Next Marker").click();

        //Marker A-1: period = 10, unit = ns, offset = 0
        long period = 10l;
        long nextStart = fStart + (period - (fStart % period));
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TmfTimestamp.fromNanos(nextStart), TmfTimestamp.fromNanos(nextStart + period))));
        viewMenu.menu("Show Markers", "Marker A-1").hide();
        fViewBot.toolbarButton("Previous Marker").click();
        fBot.waitUntil(ConditionHelpers.timeGraphIsReadyCondition(part, new TmfTimeRange(TmfTimestamp.fromNanos(fStart), TmfTimestamp.fromNanos(nextStart)), TmfTimestamp.fromNanos(fStart)));
        removeContent();
    }

    private static void insertContent() {
        SWTBotRootMenu viewMenu = fViewBot.viewMenu();
        viewMenu.menu("Marker Set").menu("Edit...").click();
        fBot.waitUntil(ConditionHelpers.isEditorOpened(fBot, EDITOR_TITLE));
        SWTBotEclipseEditor editor = fBot.editorByTitle(EDITOR_TITLE).toTextEditor();
        editor.setText(XML_CONTENT);
        editor.saveAndClose();
        WaitUtils.waitForJobs();
    }

    private static void removeContent() {
        SWTBotRootMenu viewMenu = fViewBot.viewMenu();
        viewMenu.menu("Marker Set").menu("Edit...").click();
        fBot.waitUntil(ConditionHelpers.isEditorOpened(fBot, EDITOR_TITLE));
        SWTBotEclipseEditor editor = fBot.editorByTitle(EDITOR_TITLE).toTextEditor();
        editor.setText("<marker-sets></marker-sets>");
        editor.saveAndClose();
        WaitUtils.waitForJobs();
    }

    /**
     * After class method
     */
    @AfterClass
    public static void tearDown() {
        fBot.closeAllEditors();
        SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, fBot);
    }
}
