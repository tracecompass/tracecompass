/******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.projectexplorer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.ContextMenuFinder;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.views.statistics.TmfStatisticsView;
import org.eclipse.ui.IEditorReference;
import org.hamcrest.core.IsAnything;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableList;

/**
 * SWTBot test for testing Project Explorer Trace actions (context-menus,
 * keyboard)
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class ProjectExplorerTraceActionsTest {
    private static final String TRACE_PROJECT_NAME = "test";
    private static final String TRACE_NAME = "syslog_collapse";
    private static final String RENAMED_TRACE_NAME = TRACE_NAME + 2;
    private static final String TRACE_PATH = "testfiles/" + TRACE_NAME;
    private static final String TRACE_TYPE = "org.eclipse.linuxtools.tmf.tests.stubs.trace.text.testsyslog";

    private static File fTestFile = null;

    private static SWTWorkbenchBot fBot;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private static final long NB_EVENTS = 22;

    /**
     * Test Class setup
     */
    @BeforeClass
    public static void init() {
        SWTBotUtils.initialize();

        /* set up test trace */
        URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(TRACE_PATH), null);
        URI uri;
        try {
            uri = FileLocator.toFileURL(location).toURI();
            fTestFile = new File(uri);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            fail();
        }

        assertTrue(fTestFile.exists());

        /* Set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        fBot = new SWTWorkbenchBot();

        /* Close welcome view */
        SWTBotUtils.closeView("Welcome", fBot);

        /* Switch perspectives */
        SWTBotUtils.switchToTracingPerspective();

        /* Finish waiting for eclipse to load */
        SWTBotUtils.waitForJobs();
        SWTBotUtils.createProject(TRACE_PROJECT_NAME);
    }

    /**
     * Test class tear down method.
     */
    @AfterClass
    public static void tearDown() {
        SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, fBot);
        fLogger.removeAllAppenders();
    }

    /**
     * Test that the expected context menu items are there
     * <p>
     * Action : Trace menu
     * <p>
     * Procedure :Select an LTTng trace and open its context menu
     * <p>
     * Expected Results: Correct menu opens (Open , Copy, Rename, …)
     *
     */
    @Test
    public void test4_01ContextMenuPresence() {
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fTestFile.getAbsolutePath(), TRACE_TYPE);
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), TRACE_NAME);

        final List<String> EXPECTED_MENU_LABELS = ImmutableList.of(
                "&Open\tShift+Ctrl+R", "Open With", "&Copy...\tCtrl+C", "Rena&me...\tF2", "&Delete\tDelete", "Delete &Supplementary Files...", "&Export Trace Package...", "Select &Trace Type...", "Apply Time Offset...", "Clear Time Offset",
                "Refresh\tF5");

        // TODO: SWTBot needs a better way to do this
        ContextMenuFinder finder = new ContextMenuFinder(fBot.tree().widget);
        List<MenuItem> menuItems = finder.findMenus(traceItem.contextMenu().widget, new IsAnything<>(), false);
        @NonNull
        List<String> menuLabels = menuItems.stream().map((item) -> {
            return UIThreadRunnable.syncExec(() -> item.getText());
        }).collect(Collectors.toList());
        assertEquals(EXPECTED_MENU_LABELS, menuLabels);

        fBot.closeAllEditors();
    }

    /**
     * Test that the trace opens with the context menu
     * <p>
     * Action : Open trace
     * <p>
     * Procedure :Select the Open menu
     * <p>
     * Expected Results: Trace is opened and views are populated
     *
     */
    @Test
    public void test4_02Open() {
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fTestFile.getAbsolutePath(), TRACE_TYPE);
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), TRACE_NAME);

        traceItem.contextMenu().menu("Open").click();
        testEventsTable(TRACE_NAME);
        testStatisticsView();
        fBot.closeAllEditors();
    }

    /**
     * Test that the trace can be copied with the context menu
     * <p>
     * Action : Copy trace
     * <p>
     * Procedure :Select the Copy menu and provide a new name. Open.
     * <p>
     * Expected Results: Trace is replicated under the new name
     *
     */
    @Test
    public void test4_03Copy() {
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fTestFile.getAbsolutePath(), TRACE_TYPE);
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), TRACE_NAME);

        createCopy(traceItem);

        fBot.closeAllEditors();
        SWTBotTreeItem copiedItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), RENAMED_TRACE_NAME);
        copiedItem.contextMenu().menu("Open").click();
        testEventsTable(RENAMED_TRACE_NAME);
        fBot.closeAllEditors();
        SWTBotUtils.clearTracesFolder(fBot, TRACE_PROJECT_NAME);
    }

    /**
     * Test that the trace can be renamed with the context menu
     * <p>
     * Action : Rename trace
     * <p>
     * Procedure :Select the Rename menu and provide a new name. Reopen.
     * <p>
     * Expected Results: Trace is renamed. The trace editor is closed.
     */
    @Test
    public void test4_04Rename() {
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fTestFile.getAbsolutePath(), TRACE_TYPE);
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), TRACE_NAME);

        traceItem.contextMenu().menu("Rename...").click();
        final String RENAME_TRACE_DIALOG_TITLE = "Rename Trace";
        fBot.waitUntil(Conditions.shellIsActive(RENAME_TRACE_DIALOG_TITLE));
        SWTBotShell shell = fBot.shell(RENAME_TRACE_DIALOG_TITLE);
        SWTBotText text = shell.bot().textWithLabel("New Trace name:");
        text.setText(RENAMED_TRACE_NAME);
        shell.bot().button("OK").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        fBot.waitWhile(new ConditionHelpers.ActiveEventsEditor(fBot, null));

        SWTBotTreeItem copiedItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), RENAMED_TRACE_NAME);
        copiedItem.contextMenu().menu("Open").click();
        testEventsTable(RENAMED_TRACE_NAME);
        fBot.closeAllEditors();
        SWTBotUtils.clearTracesFolder(fBot, TRACE_PROJECT_NAME);
    }

    /**
     * Test that the trace can be deleted with the context menu
     * <p>
     * Action : Delete trace
     * <p>
     * Procedure :Select the Delete menu and confirm deletion
     * <p>
     * Expected Results: Trace is deleted. The trace editor is closed.
     *
     */
    @Test
    public void test4_05Delete() {
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fTestFile.getAbsolutePath(), TRACE_TYPE);
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), TRACE_NAME);

        traceItem.contextMenu().menu("Delete").click();
        final String DELETE_TRACE_DIALOG_TITLE = "Confirm Delete";
        fBot.waitUntil(Conditions.shellIsActive(DELETE_TRACE_DIALOG_TITLE));
        SWTBotShell shell = fBot.shell(DELETE_TRACE_DIALOG_TITLE);
        shell.bot().button("Yes").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        fBot.waitWhile(new ConditionHelpers.ActiveEventsEditor(fBot, null));
    }

    /**
     * Test that the trace opens with the keyboard
     * <p>
     * Action : Open Trace (Accelerator)
     * <p>
     * Procedure :Select trace and press Enter
     * <p>
     * Expected Results: Trace is opened
     *
     *
     * @throws WidgetNotFoundException
     *             when a widget is not found
     */
    @Test
    public void test4_06OpenKeyboard() throws WidgetNotFoundException {
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fTestFile.getAbsolutePath(), TRACE_TYPE);
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), TRACE_NAME);
        traceItem.select();
        fBot.activeShell().pressShortcut(Keystrokes.CR);

        testEventsTable(TRACE_NAME);
        testStatisticsView();
        fBot.closeAllEditors();
    }

    /**
     * Test that the trace can be deleted with the keyboard
     * <p>
     * Action : Delete Trace (Accelerator)
     * <p>
     * Procedure :Select trace and press Delete and confirm deletion
     * <p>
     * Expected Results: Trace is deleted. The trace editor is closed.
     */
    @Test
    public void test4_07DeleteKeyboard() {
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fTestFile.getAbsolutePath(), TRACE_TYPE);
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), TRACE_NAME);
        traceItem.select();
        fBot.activeShell().pressShortcut(Keystrokes.DELETE);
        final String DELETE_TRACE_DIALOG_TITLE = "Confirm Delete";
        fBot.waitUntil(Conditions.shellIsActive(DELETE_TRACE_DIALOG_TITLE));
        SWTBotShell shell = fBot.shell(DELETE_TRACE_DIALOG_TITLE);
        shell.bot().button("Yes").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        fBot.waitWhile(new ConditionHelpers.ActiveEventsEditor(fBot, null));
    }

    /**
     * Test that the trace opens with double-click
     * <p>
     * Action : Open Trace (double click)
     * <p>
     * Procedure :Double-click a trace
     * <p>
     * Expected Results: Trace is opened
     *
     * @throws WidgetNotFoundException
     *             when a widget is not found
     */
    @Test
    public void test4_08OpenDoubleClick() throws WidgetNotFoundException {
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fTestFile.getAbsolutePath(), TRACE_TYPE);
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), TRACE_NAME);
        traceItem.select();
        traceItem.doubleClick();

        testEventsTable(TRACE_NAME);
        testStatisticsView();
        fBot.closeAllEditors();
    }

    /**
     * Test that the trace is brought to top if already opened
     * <p>
     * Action : Open Trace (already open)
     * <p>
     * Procedure :Open two traces. Open the first trace again.
     * <p>
     * Expected Results: The first trace editor is simply brought to front.
     */
    @Test
    public void test4_09BringToTop() {
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fTestFile.getAbsolutePath(), TRACE_TYPE);
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), TRACE_NAME);
        traceItem.select();
        traceItem.doubleClick();
        fBot.waitUntil(new ConditionHelpers.ActiveEventsEditor(fBot, TRACE_NAME));
        IEditorReference originalEditor = fBot.activeEditor().getReference();

        createCopy(traceItem);

        SWTBotTreeItem copiedItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), RENAMED_TRACE_NAME);
        copiedItem.select();
        copiedItem.doubleClick();
        copiedItem.doubleClick();
        fBot.waitUntil(new ConditionHelpers.ActiveEventsEditor(fBot, RENAMED_TRACE_NAME));
        SWTBotUtils.delay(1000);
        traceItem.select();
        traceItem.doubleClick();
        fBot.waitUntil(new ConditionHelpers.ActiveEventsEditor(fBot, TRACE_NAME));
        assertTrue(originalEditor == fBot.activeEditor().getReference());

        fBot.closeAllEditors();
        SWTBotUtils.clearTracesFolder(fBot, TRACE_PROJECT_NAME);
    }

    private static void createCopy(SWTBotTreeItem traceItem) {
        traceItem.contextMenu().menu("Copy...").click();
        final String COPY_TRACE_DIALOG_TITLE = "Copy Trace";
        fBot.waitUntil(Conditions.shellIsActive(COPY_TRACE_DIALOG_TITLE));
        SWTBotShell shell = fBot.shell(COPY_TRACE_DIALOG_TITLE);
        SWTBotText text = shell.bot().textWithLabel("New Trace name:");
        text.setText(RENAMED_TRACE_NAME);
        shell.bot().button("OK").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
    }

    private static void testEventsTable(String editorName) {
        SWTBotEditor editor = SWTBotUtils.activeEventsEditor(fBot, editorName);
        fBot.waitUntil(ConditionHelpers.numberOfEventsInTrace(TmfTraceManager.getInstance().getActiveTrace(), NB_EVENTS));

        SWTBotTable table = editor.bot().table();
        fBot.waitUntil(new DefaultCondition() {
            @Override
            public boolean test() throws Exception {
                return table.rowCount() > 1;
            }

            @Override
            public String getFailureMessage() {
                return "No items in table";
            }
        });
        // Select first event (skip filter/search row)
        table.getTableItem(1).select();

        editor.bot().waitUntil(new DefaultCondition() {
            @Override
            public boolean test() throws Exception {
                return table.selection().rowCount() == 1 && table.selection().get(0).toString().contains("01:01");
            }

            @Override
            public String getFailureMessage() {
                return "First event not selected";
            }
        });
    }

    private static void testStatisticsView() {
        SWTBotUtils.openView(TmfStatisticsView.ID);
        SWTBotView view = fBot.viewById(TmfStatisticsView.ID);
        assertTrue(view.bot().tree().hasItems());
        view.bot().tree().cell(0, 1).equals(Long.toString(NB_EVENTS));
    }
}
