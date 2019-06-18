/******************************************************************************
 * Copyright (c) 2016, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.integration.swtbot.tests.projectexplorer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.tmf.core.io.ResourceUtil;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceImportException;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceTypeUIUtils;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.wizards.SWTBotImportWizardUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.views.statistics.TmfStatisticsView;
import org.eclipse.ui.IEditorReference;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
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
    private static @NonNull TestTraceInfo CUSTOM_TEXT_LOG = new TestTraceInfo("ExampleCustomTxt.log", "Custom Text : TmfGeneric", 10, "29:52.034");
    private static final String TRACE_PROJECT_NAME = "test";
    private static final String TRACE_NAME = CUSTOM_TEXT_LOG.getTraceName();
    private static final String RENAMED_TRACE_NAME = TRACE_NAME + 2;
    private static final String RENAMED_AS_NEW_TRACE_NAME = TRACE_NAME + 3;
    private static final String COPY_AS_NEW_TRACE_OPTION = "Copy as a new trace";
    private static final String RESOURCE_PROPERTIES_ITEM_NAME = "Resource properties";
    private static final String LINKED_ITEM_NAME = "linked";
    private static final String COPY_TRACE_DIALOG_TITLE = "Copy Trace";
    private static final String PROJECT_EXPLORER_VIEW_NAME = "Project Explorer";
    private static final String PROPERTIES_VIEW_NAME = "Properties";
    private static final String COPY_EXPERIMENT_DIALOG_TITLE = "Copy Experiment";
    private static final String DEEP_COPY_OPTION = "Deep copy this experiment (each trace will be copied as a new trace)";
    private static final String RENAMED_EXP_DEEP_COPY = "expDeepCopy";
    private static final String RENAMED_EXP_NAME = "exp";
    private static final String SYMBOLIC_FOLDER_NAME = "symbolic-link";

    private static File fTestFile = null;

    private static SWTWorkbenchBot fBot;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    private static final long DISK_ACCESS_TIMEOUT = 120000L;

    private static final File TEST_TRACES_PATH = new File(new Path(TmfTraceManager.getTemporaryDirPath()).append("testtraces").toOSString());

    private static String getPath(String relativePath) {
        return new Path(TEST_TRACES_PATH.getAbsolutePath()).append(relativePath).toOSString();
    }

    /**
     * Test Class setup
     *
     * @throws IOException
     *             on error
     */
    @BeforeClass
    public static void init() throws IOException {
        TestDirectoryStructureUtil.generateTraceStructure(TEST_TRACES_PATH);

        SWTBotUtils.initialize();

        /*
         * FIXME: We can't use Manage Custom Parsers > Import because it uses a native
         * dialog. We'll still check that they show up in the dialog
         */
        CustomTxtTraceDefinition[] txtDefinitions = CustomTxtTraceDefinition.loadAll(getPath("customParsers/ExampleCustomTxtParser.xml"));
        txtDefinitions[0].save();
        /* set up test trace */
        fTestFile = new File(getPath(new Path("import").append(CUSTOM_TEXT_LOG.getTracePath()).toString()));

        assertTrue(fTestFile.exists());

        /* Set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        fBot = new SWTWorkbenchBot();

        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
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
     * Open the custom log trace (which all tests use), wait for the open operation
     * to complete and setFocus on the project explorer.
     */
    @Before
    public void openTrace() {
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fTestFile.getAbsolutePath(), CUSTOM_TEXT_LOG.getTraceType());
        fBot.waitUntil(new ConditionHelpers.ActiveEventsEditor(fBot, TRACE_NAME));
        fBot.viewByTitle(PROJECT_EXPLORER_VIEW_NAME).setFocus();
    }

    /**
     * Test tear down method.
     */
    @After
    public void afterTest() {
        fBot.closeAllEditors();
        SWTBotUtils.clearExperimentFolder(fBot, TRACE_PROJECT_NAME);
        SWTBotUtils.clearTracesFolderUI(fBot, TRACE_PROJECT_NAME);
        SWTBotUtils.closeSecondaryShells(fBot);
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
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), TRACE_NAME);

        final List<String> EXPECTED_MENU_LABELS = ImmutableList.of("Open",
                "Open As Experiment...",
                "Open With",
                "",
                "Copy...",
                "Rename...",
                "Delete", "",
                "Delete Supplementary Files...",
                "",
                "Export Trace Package...",
                "",
                "Select Trace Type...",
                "",
                "Apply Time Offset...",
                "Clear Time Offset",
                "",
                "Refresh");
        List<String> menuItems = traceItem.contextMenu().menuItems();
        assertEquals(EXPECTED_MENU_LABELS, menuItems);
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
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), TRACE_NAME);

        traceItem.contextMenu().menu("Open").click();
        SWTBotImportWizardUtils.testEventsTable(fBot, TRACE_NAME, CUSTOM_TEXT_LOG.getNbEvents(), CUSTOM_TEXT_LOG.getFirstEventTimestamp());
        testStatisticsView();
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
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), TRACE_NAME);

        // Copy the trace as link (default)
        createCopy(traceItem, true);
        // Copy the trace as a new trace
        createCopy(traceItem, false);

        fBot.closeAllEditors();
        SWTBotTreeItem copiedItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), RENAMED_TRACE_NAME);
        copiedItem.contextMenu().menu("Open").click();
        SWTBotImportWizardUtils.testEventsTable(fBot, RENAMED_TRACE_NAME, CUSTOM_TEXT_LOG.getNbEvents(), CUSTOM_TEXT_LOG.getFirstEventTimestamp());
        SWTBotTreeItem copiedAsNewItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), RENAMED_AS_NEW_TRACE_NAME);
        copiedAsNewItem.contextMenu().menu("Open").click();
        SWTBotImportWizardUtils.testEventsTable(fBot, RENAMED_AS_NEW_TRACE_NAME, CUSTOM_TEXT_LOG.getNbEvents(), CUSTOM_TEXT_LOG.getFirstEventTimestamp());

        // Make sure that the traces have the correct link status (linked or not)
        testLinkStatus(copiedItem, true);
        testLinkStatus(copiedAsNewItem, false);
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
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), TRACE_NAME);

        traceItem.contextMenu().menu("Rename...").click();
        final String RENAME_TRACE_DIALOG_TITLE = "Rename Trace";
        SWTBotShell shell = fBot.shell(RENAME_TRACE_DIALOG_TITLE).activate();
        SWTBotText text = shell.bot().textWithLabel("New Trace name:");
        text.setText(RENAMED_TRACE_NAME);
        shell.bot().button("OK").click();
        fBot.waitUntil(Conditions.shellCloses(shell), DISK_ACCESS_TIMEOUT);
        fBot.waitWhile(new ConditionHelpers.ActiveEventsEditor(fBot, null));

        SWTBotTreeItem copiedItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), RENAMED_TRACE_NAME);
        copiedItem.contextMenu().menu("Open").click();
        SWTBotImportWizardUtils.testEventsTable(fBot, RENAMED_TRACE_NAME, CUSTOM_TEXT_LOG.getNbEvents(), CUSTOM_TEXT_LOG.getFirstEventTimestamp());
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
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), TRACE_NAME);

        traceItem.contextMenu().menu("Delete").click();
        final String DELETE_TRACE_DIALOG_TITLE = "Confirm Delete";
        SWTBotShell shell = fBot.shell(DELETE_TRACE_DIALOG_TITLE).activate();
        shell.bot().button("Yes").click();
        fBot.waitUntil(Conditions.shellCloses(shell), DISK_ACCESS_TIMEOUT);
        fBot.waitWhile(new ConditionHelpers.ActiveEventsEditor(fBot, null));
        fBot.waitUntil(new TraceDeletedCondition());
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
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), TRACE_NAME);
        traceItem.select();
        traceItem.pressShortcut(Keystrokes.CR);

        SWTBotImportWizardUtils.testEventsTable(fBot, TRACE_NAME, CUSTOM_TEXT_LOG.getNbEvents(), CUSTOM_TEXT_LOG.getFirstEventTimestamp());
        testStatisticsView();
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
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), TRACE_NAME);
        traceItem.select();
        traceItem.pressShortcut(Keystrokes.DELETE);
        final String DELETE_TRACE_DIALOG_TITLE = "Confirm Delete";
        SWTBotShell shell = fBot.shell(DELETE_TRACE_DIALOG_TITLE).activate();
        shell.bot().button("Yes").click();
        fBot.waitUntil(Conditions.shellCloses(shell), DISK_ACCESS_TIMEOUT);
        fBot.waitWhile(new ConditionHelpers.ActiveEventsEditor(fBot, null));
        fBot.waitUntil(new TraceDeletedCondition());
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
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), TRACE_NAME);
        traceItem.doubleClick();

        SWTBotImportWizardUtils.testEventsTable(fBot, TRACE_NAME, CUSTOM_TEXT_LOG.getNbEvents(), CUSTOM_TEXT_LOG.getFirstEventTimestamp());
        testStatisticsView();
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
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), TRACE_NAME);
        traceItem.doubleClick();
        fBot.waitUntil(new ConditionHelpers.ActiveEventsEditor(fBot, TRACE_NAME));
        IEditorReference originalEditor = fBot.activeEditor().getReference();

        createCopy(traceItem, true);

        SWTBotTreeItem copiedItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), RENAMED_TRACE_NAME);
        fBot.viewByTitle(PROJECT_EXPLORER_VIEW_NAME).setFocus();
        copiedItem.doubleClick();
        fBot.waitUntil(new ConditionHelpers.ActiveEventsEditor(fBot, RENAMED_TRACE_NAME));
        SWTBotUtils.delay(1000);
        fBot.viewByTitle(PROJECT_EXPLORER_VIEW_NAME).setFocus();
        traceItem.doubleClick();
        fBot.waitUntil(new ConditionHelpers.ActiveEventsEditor(fBot, TRACE_NAME));
        assertTrue(originalEditor == fBot.activeEditor().getReference());
    }

    /**
     * Test that the experiment can be copied with the context menu
     * <p>
     * Action : Copy experiment
     * <p>
     * Procedure :Select the Copy menu and provide a new name. Open.
     * <p>
     * Expected Results: Experiment is replicated under the new name
     *
     */
    @Test
    public void test4_10CopyExperiment() {
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), TRACE_NAME);

        // Copy the trace as a new trace
        createCopy(traceItem, false);

        SWTBotTreeItem tracesFolder = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);
        tracesFolder.contextMenu().menu("Open As Experiment...", "Generic Experiment").click();
        fBot.waitUntil(new ConditionHelpers.ActiveEventsEditor(fBot, "Experiment"));
        SWTBotTreeItem experimentsItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectProject(fBot, TRACE_PROJECT_NAME), "Experiments");
        experimentsItem.expand();
        fBot.waitUntil(ConditionHelpers.isTreeChildNodeAvailable("Experiment [2]", experimentsItem));

        SWTBotTreeItem expItem = SWTBotUtils.getTraceProjectItem(fBot, experimentsItem, "Experiment");

        // Copy the experiment
        createExperimentCopy(expItem, false);
        SWTBotTreeItem copiedExpItem = SWTBotUtils.getTraceProjectItem(fBot, experimentsItem, RENAMED_EXP_NAME);
        verifyExperimentCopy(copiedExpItem, false);

        // Make a deep copy of the experiment
        createExperimentCopy(expItem, true);
        SWTBotTreeItem deepCopiedExpItem = SWTBotUtils.getTraceProjectItem(fBot, experimentsItem, RENAMED_EXP_DEEP_COPY);
        verifyExperimentCopy(deepCopiedExpItem, true);

        assertEquals(3, experimentsItem.getItems().length);
    }

    /**
     * Test that deleting a trace from an experiment: deletes the experiment if the
     * experiment is empty and deletes the trace from the Traces folder.
     */
    @Test
    public void test4_11DeleteTraceFromExperiment() {
        /*
         * close the editor for the trace to avoid name conflicts with the one for the
         * experiment
         */
        fBot.closeAllEditors();

        // create experiment
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), TRACE_NAME);
        traceItem.contextMenu().menu("Open As Experiment...", "Generic Experiment").click();
        fBot.waitUntil(new ConditionHelpers.ActiveEventsEditor(fBot, TRACE_NAME));

        // find the trace under the experiment
        fBot.viewByTitle(PROJECT_EXPLORER_VIEW_NAME).setFocus();
        SWTBotTreeItem experimentsItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectProject(fBot, TRACE_PROJECT_NAME), "Experiments");
        experimentsItem.expand();
        fBot.waitUntil(ConditionHelpers.isTreeChildNodeAvailable("ExampleCustomTxt.log [1]", experimentsItem));
        SWTBotTreeItem expItem = SWTBotUtils.getTraceProjectItem(fBot, experimentsItem, "ExampleCustomTxt.log [1]");
        expItem.expand();
        SWTBotTreeItem expTrace = expItem.getNode(TRACE_NAME);

        // delete it
        expTrace.contextMenu("Delete").click();
        SWTBotShell shell = fBot.shell("Confirm Delete").activate();
        shell.bot().button("Yes").click();
        fBot.waitUntil(Conditions.shellCloses(shell), DISK_ACCESS_TIMEOUT);
        fBot.waitWhile(new ConditionHelpers.ActiveEventsEditor(fBot, null));

        // ensure that it is properly deleted from places.
        SWTBotUtils.waitUntil(exp -> exp.getItems().length == 0, experimentsItem,
                "Failed to delete the trace from the experiment");
        fBot.waitUntil(new TraceDeletedCondition());
    }

    /**
     * Test that removing a trace from an experiment: removes it from the experiment
     * but does not delete the experiment if empty, keeps in in the Traces folders
     */
    @Test
    public void test4_12RemoveTraceFromExperiment() {
        /*
         * close the editor for the trace to avoid name conflicts with the one for the
         * experiment
         */
        fBot.closeAllEditors();

        // create experiment
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), TRACE_NAME);
        traceItem.contextMenu().menu("Open As Experiment...", "Generic Experiment").click();
        fBot.waitUntil(new ConditionHelpers.ActiveEventsEditor(fBot, TRACE_NAME));

        // find the trace under the experiment
        fBot.viewByTitle(PROJECT_EXPLORER_VIEW_NAME).setFocus();
        SWTBotTreeItem experimentsItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectProject(fBot, TRACE_PROJECT_NAME), "Experiments");
        experimentsItem.expand();
        fBot.waitUntil(ConditionHelpers.isTreeChildNodeAvailable("ExampleCustomTxt.log [1]", experimentsItem));
        SWTBotTreeItem expItem = SWTBotUtils.getTraceProjectItem(fBot, experimentsItem, "ExampleCustomTxt.log [1]");
        expItem.expand();
        SWTBotTreeItem expTrace = expItem.getNode(TRACE_NAME);

        // remove the trace from the experiment
        expTrace.contextMenu().menu("Remove").click();
        SWTBotShell shell = fBot.shell("Confirm Remove").activate();
        shell.bot().button("Yes").click();
        fBot.waitUntil(Conditions.shellCloses(shell), DISK_ACCESS_TIMEOUT);

        // ensure that it is properly removed from the experiment.
        SWTBotUtils.getTraceProjectItem(fBot, experimentsItem, "ExampleCustomTxt.log [0]");

        // ensure that the trace still exists in the Traces folder
        SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), TRACE_NAME);
    }

    /**
     * Test the backward compatibility
     */
    @Test
    public void testExperimentLinkBackwardCompatibility() {
        /*
         * close the editor for the trace to avoid name conflicts with the one for the
         * experiment
         */
        fBot.closeAllEditors();

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        final IProject project = root.getProject(TRACE_PROJECT_NAME);
        assertTrue(project.exists());
        TmfProjectElement projectElement = TmfProjectRegistry.getProject(project);

        // Get the experiment folder
        TmfExperimentFolder experimentsFolder = projectElement.getExperimentsFolder();
        assertNotNull("Experiment folder should exist", experimentsFolder);
        IFolder experimentsFolderResource = experimentsFolder.getResource();
        String experimentName = "exp";
        IFolder expFolder = experimentsFolderResource.getFolder(experimentName);
        assertFalse(expFolder.exists());

        // Create the experiment
        try {
            expFolder.create(true, true, null);
            IFile file = expFolder.getFile(TRACE_NAME);
            file.createLink(Path.fromOSString(fTestFile.getAbsolutePath()), IResource.REPLACE, new NullProgressMonitor());
        } catch (CoreException e) {
            fail("Failed to create the experiment");
        }

        fBot.viewByTitle(PROJECT_EXPLORER_VIEW_NAME).setFocus();
        SWTBotTreeItem experimentsItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectProject(fBot, TRACE_PROJECT_NAME), "Experiments");

        SWTBotTreeItem expItem = SWTBotUtils.getTraceProjectItem(fBot, experimentsItem, "exp");

        // find the trace under the experiment
        expItem.expand();
        expItem.getNode(TRACE_NAME);

        // Open the experiment
        expItem.contextMenu().menu("Open").click();
        fBot.waitUntil(new ConditionHelpers.ActiveEventsEditor(fBot, experimentName));
    }

    /**
     * Test copying of traces that are created with file system symbolic links.
     *
     * @throws CoreException
     *          If error happens
     * @throws TmfTraceImportException
     *          If error happens
     */
    @Test
    public void testCopySymbolicLinks() throws CoreException, TmfTraceImportException {
        // Close editor from @Before since not needed
        fBot.closeAllEditors();

        // Create File system symbolic link to traces
        importTraceAsSymlink();
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), SYMBOLIC_FOLDER_NAME, TRACE_NAME);

        // Copy the trace as link (default)
        createCopy(traceItem, true);
        // Copy the trace as a new trace
        createCopy(traceItem, false);

        fBot.closeAllEditors();
        SWTBotTreeItem copiedItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), SYMBOLIC_FOLDER_NAME, RENAMED_TRACE_NAME);
        copiedItem.contextMenu().menu("Open").click();
        SWTBotImportWizardUtils.testEventsTable(fBot, SYMBOLIC_FOLDER_NAME + '/' + RENAMED_TRACE_NAME, CUSTOM_TEXT_LOG.getNbEvents(), CUSTOM_TEXT_LOG.getFirstEventTimestamp());
        SWTBotTreeItem copiedAsNewItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), SYMBOLIC_FOLDER_NAME, RENAMED_AS_NEW_TRACE_NAME);
        copiedAsNewItem.contextMenu().menu("Open").click();
        SWTBotImportWizardUtils.testEventsTable(fBot, SYMBOLIC_FOLDER_NAME + '/' + RENAMED_AS_NEW_TRACE_NAME, CUSTOM_TEXT_LOG.getNbEvents(), CUSTOM_TEXT_LOG.getFirstEventTimestamp());

        // Make sure that the traces have the correct link status (linked or not)
        testLinkStatus(copiedItem, true);
        testLinkStatus(copiedAsNewItem, false);
    }

    /**
     * Test renaming of traces that are created with file system symbolic links.
     *
     * @throws CoreException
     *          If error happens
     * @throws TmfTraceImportException
     *          If error happens
     */
    @Test
    public void testRenameSymbolicLinks() throws CoreException, TmfTraceImportException {
        // Close editor from @Before since not needed
        fBot.closeAllEditors();

        // Create File system symbolic link to traces
        importTraceAsSymlink();
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), SYMBOLIC_FOLDER_NAME, TRACE_NAME);
        fBot.viewByTitle(PROJECT_EXPLORER_VIEW_NAME).setFocus();
        traceItem.doubleClick();
        fBot.waitUntil(new ConditionHelpers.ActiveEventsEditor(fBot, SYMBOLIC_FOLDER_NAME + '/' + TRACE_NAME));

        traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), SYMBOLIC_FOLDER_NAME, TRACE_NAME);
        traceItem.contextMenu().menu("Rename...").click();
        final String RENAME_TRACE_DIALOG_TITLE = "Rename Trace";
        SWTBotShell shell = fBot.shell(RENAME_TRACE_DIALOG_TITLE).activate();
        SWTBotText text = shell.bot().textWithLabel("New Trace name:");
        text.setText(RENAMED_TRACE_NAME);
        shell.bot().button("OK").click();
        fBot.waitUntil(Conditions.shellCloses(shell), DISK_ACCESS_TIMEOUT);
        fBot.waitWhile(new ConditionHelpers.ActiveEventsEditor(fBot, null));

        SWTBotTreeItem copiedItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), SYMBOLIC_FOLDER_NAME, RENAMED_TRACE_NAME);
        copiedItem.contextMenu().menu("Open").click();
        SWTBotImportWizardUtils.testEventsTable(fBot, SYMBOLIC_FOLDER_NAME + '/' + RENAMED_TRACE_NAME, CUSTOM_TEXT_LOG.getNbEvents(), CUSTOM_TEXT_LOG.getFirstEventTimestamp());

        // Make sure that the traces have the correct link status
        testLinkStatus(copiedItem, true);
    }

    private static void importTraceAsSymlink() throws TmfTraceImportException, CoreException {
        TraceTypeHelper helper = TmfTraceTypeUIUtils.selectTraceType(fTestFile.getAbsolutePath(), null, null);
        final boolean IS_WINDOWS = System.getProperty("os.name").contains("Windows"); //$NON-NLS-1$ //$NON-NLS-2$
        IProject project = TmfProjectRegistry.createProject(TRACE_PROJECT_NAME, null, new NullProgressMonitor());
        assertNotNull(project);
        TmfProjectElement projectElement = TmfProjectRegistry.getProject(project);
        TmfTraceFolder folder = projectElement.getTracesFolder();
        assertNotNull(folder);

        IFolder subFolder = folder.getResource().getFolder(SYMBOLIC_FOLDER_NAME);
        TraceUtils.createFolder(subFolder, new NullProgressMonitor());

        IFile file = subFolder.getFile(fTestFile.getName());
        assertNotNull(file);
        ResourceUtil.createSymbolicLink(file, new Path(fTestFile.getAbsolutePath()), !IS_WINDOWS, null);
        folder.getResource().refreshLocal(IResource.DEPTH_INFINITE, null);
        TmfTraceTypeUIUtils.setTraceType(file, helper, false);
    }

    private static void createCopy(SWTBotTreeItem traceItem, boolean copyAsLink) {
        fBot.viewByTitle(PROJECT_EXPLORER_VIEW_NAME).setFocus();
        traceItem.contextMenu().menu("Copy...").click();
        SWTBotShell shell = fBot.shell(COPY_TRACE_DIALOG_TITLE).activate();
        SWTBotText text = shell.bot().textWithLabel("New Trace name:");
        text.setText(RENAMED_TRACE_NAME);
        if (!copyAsLink) {
            shell.bot().radio(COPY_AS_NEW_TRACE_OPTION).click();
            text.setText(RENAMED_AS_NEW_TRACE_NAME);
        }
        shell.bot().button("OK").click();
        fBot.waitUntil(Conditions.shellCloses(shell), DISK_ACCESS_TIMEOUT);
    }

    private static void createExperimentCopy(SWTBotTreeItem expItem, boolean deepCopy) {
        fBot.viewByTitle(PROJECT_EXPLORER_VIEW_NAME).setFocus();
        expItem.contextMenu().menu("Copy...").click();
        SWTBotShell shell = fBot.shell(COPY_EXPERIMENT_DIALOG_TITLE).activate();
        SWTBotText text = shell.bot().textWithLabel("New Experiment name:");
        text.setText(RENAMED_EXP_NAME);
        if (deepCopy) {
            text.setText(RENAMED_EXP_DEEP_COPY);
            shell.bot().checkBox(DEEP_COPY_OPTION).click();
        }
        shell.bot().button("OK").click();
        fBot.waitUntil(Conditions.shellCloses(shell), DISK_ACCESS_TIMEOUT);
    }

    private static void testStatisticsView() {
        SWTBotUtils.openView(TmfStatisticsView.ID);
        SWTBotView view = fBot.viewById(TmfStatisticsView.ID);
        assertTrue(view.bot().tree().hasItems());
        view.bot().tree().cell(0, 1).equals(Long.toString(CUSTOM_TEXT_LOG.getNbEvents()));
    }

    private static void testLinkStatus(SWTBotTreeItem traceItem, boolean isLinked) {
        SWTBotView viewBot = fBot.viewByTitle(PROPERTIES_VIEW_NAME);
        viewBot.show();
        fBot.waitUntil(ConditionHelpers.viewIsActive(viewBot));
        traceItem.select();
        SWTBotTree tree = viewBot.bot().tree();
        SWTBotTreeItem resourcePropertiesItem = tree.getTreeItem(RESOURCE_PROPERTIES_ITEM_NAME);
        fBot.waitUntil(ConditionHelpers.isTreeChildNodeAvailable(LINKED_ITEM_NAME, resourcePropertiesItem));
        SWTBotTreeItem linkedNode = resourcePropertiesItem.getNode(LINKED_ITEM_NAME);
        String linkedValue = linkedNode.cell(1);
        assertEquals(Boolean.toString(isLinked), linkedValue);
    }

    private static void verifyExperimentCopy(SWTBotTreeItem copiedExpItem, boolean isDeepCopied) {
        SWTBotTreeItem tracesFolder = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);
        tracesFolder.expand();
        SWTBotTreeItem[] traceItems = tracesFolder.getItems();
        copiedExpItem.expand();
        if (isDeepCopied) {
            /*
             * Traces folder should contain the previous two traces and the new folder for
             * the copied traces
             */
            assertEquals(3, traceItems.length);
            copiedExpItem.getNode(RENAMED_EXP_DEEP_COPY + '/' + TRACE_NAME);
            copiedExpItem.getNode(RENAMED_EXP_DEEP_COPY + '/' + RENAMED_AS_NEW_TRACE_NAME);
            SWTBotTreeItem deepCopiedExpTracesFolder = SWTBotUtils.getTraceProjectItem(fBot, tracesFolder, RENAMED_EXP_DEEP_COPY);
            deepCopiedExpTracesFolder.expand();
            SWTBotTreeItem[] expTracesFolderItems = deepCopiedExpTracesFolder.getItems();
            assertEquals(2, expTracesFolderItems.length);
            for (SWTBotTreeItem traceItem : expTracesFolderItems) {
                testLinkStatus(traceItem, false);
            }
        } else {
            assertEquals(2, traceItems.length);
            copiedExpItem.getNode(TRACE_NAME);
            copiedExpItem.getNode(RENAMED_AS_NEW_TRACE_NAME);
        }
    }

    private final class TraceDeletedCondition extends DefaultCondition {
        @Override
        public boolean test() throws Exception {
            return ResourcesPlugin.getWorkspace().getRoot().getProject(TRACE_PROJECT_NAME).findMember(new Path("Traces/" + TRACE_NAME)) == null;
        }

        @Override
        public String getFailureMessage() {
            return TRACE_NAME + " was not deleted successfully.";
        }
    }
}
