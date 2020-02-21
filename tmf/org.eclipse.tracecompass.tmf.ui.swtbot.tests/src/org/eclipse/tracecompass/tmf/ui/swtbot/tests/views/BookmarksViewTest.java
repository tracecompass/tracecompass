/**********************************************************************
 * Copyright (c) 2017, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.views;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.function.Predicate;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.utils.TableCollection;
import org.eclipse.swtbot.swt.finder.utils.TableRow;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.ui.internal.views.markers.BookmarksView;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.base.Objects;

/**
 * SWT bot test to test the {@link BookmarksView} in Trace Compass
 *
 * @author Loic Prieur-Drevon
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class BookmarksViewTest {

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    private static final String PROJECT_NAME = "TestBookmarks";
    private static final String TRACE_TYPE = "org.eclipse.linuxtools.tmf.core.tests.secondtt";
    private static final String BOOKMARK_NAME = "banana";

    private static final @NonNull String EXPERIMENT_NAME = "experiment";
    private static final Predicate<SWTBotTable> TABLE_NOT_EMPTY = tb -> tb.rowCount() > 2;

    private static SWTWorkbenchBot fBot;

    /**
     * Initialization
     */
    @BeforeClass
    public static void init() {
        SWTBotUtils.initialize();
        Thread.currentThread().setName("SWTBot Thread"); // for the debugger
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout()));
        fBot = new SWTWorkbenchBot();

        SWTBotUtils.switchToTracingPerspective();
        /* finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
    }

    /**
     * Open a trace and focus on the Bookmarks view
     */
    @Before
    public void beforeTest() {
        SWTBotUtils.createProject(PROJECT_NAME);
        SWTBotTreeItem treeItem = SWTBotUtils.selectTracesFolder(fBot, PROJECT_NAME);
        assertNotNull(treeItem);
    }

    /**
     * Test bookmarks on a trace.
     *
     * @throws IOException
     *             if an error occurs during the file URL -> path conversion
     */
    @Test
    public void testTrace() throws IOException {
        SWTBotUtils.openTrace(PROJECT_NAME, FileLocator.toFileURL(TmfTraceStub.class.getResource("/testfiles/A-Test-10K")).getPath(), TRACE_TYPE);

        bookmarkTest("A-Test-10K");
    }

    /**
     * Test bookmarking an experiment
     *
     * @throws IOException
     *             if an error occurs during the file URL -> path conversion
     */
    @Test
    public void testExperiment() throws IOException {
        /**
         * Create Experiment with 2 LTTng CTF Kernel traces in it and open
         * experiment. Verify that an Events editor is opened showing LTTng
         * Kernel specific columns.
         */
        SWTBotUtils.openTrace(PROJECT_NAME, FileLocator.toFileURL(TmfTraceStub.class.getResource("/testfiles/A-Test-10K")).getPath(), TRACE_TYPE);
        SWTBotUtils.openTrace(PROJECT_NAME, FileLocator.toFileURL(TmfTraceStub.class.getResource("/testfiles/A-Test-10K-2")).getPath(), TRACE_TYPE);
        WaitUtils.waitForJobs();
        SWTBotUtils.createExperiment(fBot, PROJECT_NAME, EXPERIMENT_NAME);
        SWTBotTreeItem project = SWTBotUtils.selectProject(fBot, PROJECT_NAME);
        SWTBotTreeItem experiment = SWTBotUtils.getTraceProjectItem(fBot, project, "Experiments", EXPERIMENT_NAME);
        experiment.contextMenu("Select Traces...").click();
        SWTBotShell selectTracesShell = fBot.shell("Select Traces");
        selectTracesShell.bot().button("Select All").click();
        selectTracesShell.bot().button("Finish").click();
        experiment.select();
        experiment.doubleClick();
        SWTBotEditor editor = SWTBotUtils.activeEventsEditor(fBot, EXPERIMENT_NAME);
        assertEquals("Event editor is displaying the wrong trace/experiment", EXPERIMENT_NAME, editor.getTitle());

        bookmarkTest(EXPERIMENT_NAME);
    }

    private static void bookmarkTest(String editorName) throws IOException {
        SWTBotView fViewBot = fBot.viewByPartName("Bookmarks");
        fViewBot.setFocus();
        WaitUtils.waitForJobs();
        assertEquals("Failed to show the Bookmarks View", "Bookmarks", fViewBot.getTitle());
        /**
         * Add a bookmark: a) Double click to select an event in the event
         * editor b) Go to the Edit > Add Bookmark... menu c) Enter the bookmark
         * description in dialog box
         */
        SWTBotEditor editorBot = SWTBotUtils.activateEditor(fBot, editorName);
        SWTBotTable tableBot = editorBot.bot().table();
        SWTBotTableItem tableItem = tableBot.getTableItem(7);
        String expectedTimeStamp = tableItem.getText(1);
        assertNull("The image should not be bookmarked yet", getBookmarkImage(tableItem));

        tableItem.select();
        tableItem.doubleClick();
        fBot.menu("Edit").menu("Add Bookmark...").click();
        WaitUtils.waitForJobs();
        SWTBotShell addBookmarkShell = fBot.shell("Add Bookmark");
        addBookmarkShell.bot().text().setText(BOOKMARK_NAME);
        addBookmarkShell.bot().button("OK").click();
        assertNotNull("Failed to add bookmark in event editor", getBookmarkImage(tableItem));

        fViewBot.setFocus();
        WaitUtils.waitForJobs();
        SWTBotTree bookmarkTree = fViewBot.bot().tree();
        WaitUtils.waitForJobs();
        /**
         * throws WidgetNotFoundException - if the node was not found, nothing
         * to assert
         */
        SWTBotTreeItem bookmark = bookmarkTree.getTreeItem(BOOKMARK_NAME);
        assertEquals(BOOKMARK_NAME, bookmark.cell(0));

        /**
         * Scroll within event table so that bookmark is not visible anymore and
         * then double-click on bookmark in Bookmarks View
         */
        UIThreadRunnable.syncExec(() -> TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(null, TmfTimestamp.fromMicros(22))));
        bookmark.doubleClick();
        WaitUtils.waitUntil(TABLE_NOT_EMPTY, tableBot, "Table is still empty");
        TableCollection selection = tableBot.selection();
        TableRow row = selection.get(0);
        assertNotNull(selection.toString(), row);
        assertEquals("Wrong event was selected " + selection, expectedTimeStamp, row.get(1));

        /**
         * Open another trace #2 and then double-click on bookmark in Bookmarks
         * view
         */
        SWTBotUtils.openTrace(PROJECT_NAME, FileLocator.toFileURL(TmfTraceStub.class.getResource("/testfiles/E-Test-10K")).getPath(), TRACE_TYPE);
        WaitUtils.waitForJobs();
        bookmark.doubleClick();
        editorBot = SWTBotUtils.activeEventsEditor(fBot, editorName);
        WaitUtils.waitUntil(TABLE_NOT_EMPTY, tableBot, "Table is still empty");
        selection = tableBot.selection();
        row = selection.get(0);
        assertNotNull(selection.toString(), row);
        assertEquals("Wrong event was selected " + selection, expectedTimeStamp, row.get(1));

        /**
         * Close the trace #1 and then double-click on bookmark in Bookmarks
         * view
         */
        editorBot.close();
        WaitUtils.waitUntil(eb -> !eb.isActive(), editorBot, "Waiting for the editor to close");
        bookmark.doubleClick();
        editorBot = SWTBotUtils.activeEventsEditor(fBot, editorName);
        WaitUtils.waitUntil(eb -> eb.bot().table().selection().rowCount() > 0, editorBot, "Selection is still empty");
        tableBot = editorBot.bot().table();
        WaitUtils.waitUntil(tb -> !Objects.equal(tb.selection().get(0).get(1), "<srch>"), tableBot, "Header is still selected");
        selection = tableBot.selection();
        row = selection.get(0);
        assertNotNull(selection.toString(), row);
        assertEquals("Wrong event was selected " + selection, expectedTimeStamp, row.get(1));

        /**
         * Select bookmarks icon in bookmark view right-click on icon and select
         * "Remove Bookmark"
         */
        bookmark.select();
        bookmark.contextMenu("Delete").click();
        SWTBotShell deleteBookmarkShell = fBot.shell("Delete Selected Entries");
        SWTBotUtils.anyButtonOf(deleteBookmarkShell.bot(), "Delete", "Yes").click();
        fBot.waitUntil(Conditions.treeHasRows(bookmarkTree, 0));
        tableItem = editorBot.bot().table().getTableItem(7);
        assertNull("Bookmark not deleted from event table", getBookmarkImage(tableItem));
    }

    private static Image getBookmarkImage(SWTBotTableItem tableItem) {
        return UIThreadRunnable.syncExec((Result<Image>) () -> tableItem.widget.getImage(0));
    }

    /**
     * Close the editor
     */
    @After
    public void tearDown() {
        fBot.closeAllEditors();
        SWTBotUtils.deleteProject(PROJECT_NAME, fBot);
    }

    /**
     * Delete the file
     */
    @AfterClass
    public static void cleanUp() {
        fLogger.removeAllAppenders();
    }

}
