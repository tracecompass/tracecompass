/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.swtbot.tests.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRootMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimePreferencesConstants;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * SWTBot test for testing adding of tracing nature
 *
 * @author Bernd Hufmann
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SWTBotJunit4ClassRunner.class)

public class AddProjectNatureTest {

    private static final String SOME_PROJECT_NAME = "SomeProject";
    private static final String SOME_PROJECT_SHADOW_NAME = ".tracecompass-SomeProject";
    private static final String TRACE_NAME = "syslog_collapse";
    private static final String TRACE_PATH = "testfiles/" + TRACE_NAME;
    private static final String TRACE_TYPE = "org.eclipse.linuxtools.tmf.tests.stubs.trace.text.testsyslog";
    private static final String PROJECT_EXPLORER_TITLE = "Project Explorer";
    private static final String CONTEXT_MENU_CONFIGURE = "Configure";
    private static final String CONTEXT_MENU_CONFIGURE_TRACING_NATURE = "Configure or convert to Tracing Project";
    private static final String TRACING_PROJECT_ROOT_NAME = "Trace Compass";
    private static final String TRACES_FOLDER_NAME = "Traces";
    private static final String EXPERIMENTS_FOLDER_NAME = "Experiments";
    private static final String FIRST_EVENT_TIME = "Jan 1 01:01:01";

    private static final String CUSTOMIZE_VIEW_MENU_ITEM_4_6 = "Customize View...";
    private static final String CUSTOMIZE_VIEW_DIALOG_TITLE_4_6 = "Available Customizations";
    private static final String CUSTOMIZE_VIEW_MENU_ITEM_4_7 = "Filters and Customization...";
    private static final String CUSTOMIZE_VIEW_DIALOG_TITLE_4_7 = "Filters and Customization";
    private static final String CUSTOMIZE_VIEW_RESOUCES_FILTER = ".* resources";
    private static final String CUSTOMIZE_VIEW_SHADOW_FILTER = "Trace Compass Shadow Projects";
    private static final String OK_BUTTON = "OK";

    private static IWorkspaceRoot fWorkspaceRoot;
    private static IProject fSomeProject;
    private static File fTestFile = null;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    private static SWTWorkbenchBot fBot;

    /**
     * Test Class setup
     *
     * @throws Exception
     *             on error
     */
    @BeforeClass
    public static void init() throws Exception {
        IProgressMonitor progressMonitor = new NullProgressMonitor();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        SWTBotUtils.initialize();

        /* Set up for SWTBot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        fBot = new SWTWorkbenchBot();

        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();

        // Manually create C project
        fWorkspaceRoot = workspace.getRoot();
        fSomeProject = fWorkspaceRoot.getProject(SOME_PROJECT_NAME);
        fSomeProject.create(progressMonitor);
        fSomeProject.open(progressMonitor);
        IProjectDescription description = fSomeProject.getDescription();
        description.setNatureIds(new String[] { "org.eclipse.cdt.core.cnature" });
        fSomeProject.setDescription(description, null);
        fSomeProject.open(progressMonitor);

        /* set up test trace */
        URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(TRACE_PATH), null);
        URI uri;
        try {
            uri = FileLocator.toFileURL(location).toURI();
            fTestFile = new File(uri);
        } catch (URISyntaxException | IOException e) {
            fail(e.getMessage());
        }
        assumeTrue(fTestFile.exists());

        /* setup timestamp preference */
        IEclipsePreferences defaultPreferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        defaultPreferences.put(ITmfTimePreferencesConstants.DATIME, "MMM d HH:mm:ss");
        defaultPreferences.put(ITmfTimePreferencesConstants.SUBSEC, ITmfTimePreferencesConstants.SUBSEC_NO_FMT);
        TmfTimestampFormat.updateDefaultFormats();
    }

    /**
     * Test class tear down method.
     */
    @AfterClass
    public static void tearDown() {
        SWTBotUtils.deleteProject(SOME_PROJECT_NAME, fBot);
        fLogger.removeAllAppenders();

        /* Set timestamp defaults */
        IEclipsePreferences defaultPreferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        defaultPreferences.put(ITmfTimePreferencesConstants.DATIME, ITmfTimePreferencesConstants.TIME_HOUR_FMT);
        defaultPreferences.put(ITmfTimePreferencesConstants.SUBSEC, ITmfTimePreferencesConstants.SUBSEC_NANO_FMT);
        TmfTimestampFormat.updateDefaultFormats();
    }

    /**
     * Test tear down method.
     */
    @After
    public void afterTest() {
        SWTBotUtils.closeSecondaryShells(fBot);
    }

    /**
     * Test adding and configuring tracing nature on a C-Project.
     */
    @Test
    public void testConfigureTracingNature() {
        SWTBotTreeItem projectItem = SWTBotUtils.selectProject(fBot, SOME_PROJECT_NAME);
        projectItem.contextMenu().menu(CONTEXT_MENU_CONFIGURE, CONTEXT_MENU_CONFIGURE_TRACING_NATURE).click();
        WaitUtils.waitForJobs();

        SWTBotTreeItem projectRoot = SWTBotUtils.getTraceProjectItem(fBot, projectItem, TRACING_PROJECT_ROOT_NAME);
        assertEquals(TRACING_PROJECT_ROOT_NAME, projectRoot.getText());
        SWTBotUtils.getTraceProjectItem(fBot, projectRoot, TRACES_FOLDER_NAME);
        SWTBotUtils.getTraceProjectItem(fBot, projectRoot, EXPERIMENTS_FOLDER_NAME);

        SWTBotUtils.openTrace(SOME_PROJECT_NAME, fTestFile.getAbsolutePath(), TRACE_TYPE);
        SWTBotEditor editorBot = SWTBotUtils.activateEditor(fBot, fTestFile.getName());
        SWTBotTable tableBot = editorBot.bot().table();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, FIRST_EVENT_TIME, 1, 1));
        assertEquals("Timestamp", FIRST_EVENT_TIME, tableBot.cell(1, 1));
        fBot.closeAllEditors();
    }

    /**
     * Test viewer filter.
     */
    @Test
    public void testViewerFilter() {

        /* Check that shadow project is visible */
        toggleFilters();
        SWTBotTreeItem shadowProject = SWTBotUtils.selectProject(fBot, SOME_PROJECT_SHADOW_NAME);
        assertEquals(SOME_PROJECT_SHADOW_NAME, shadowProject.getText());
        SWTBotTreeItem tracesItem = SWTBotUtils.getTraceProjectItem(fBot, shadowProject, TRACES_FOLDER_NAME);
        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, tracesItem, TRACE_NAME);
        assertEquals(TRACE_NAME, traceItem.getText());
        SWTBotUtils.getTraceProjectItem(fBot, shadowProject, EXPERIMENTS_FOLDER_NAME);

        /* Check that shadow project is not visible */
        toggleFilters();

        SWTBotView viewBot = fBot.viewByTitle(PROJECT_EXPLORER_TITLE);
        viewBot.setFocus();
        SWTBot projectExplorerBot = viewBot.bot();

        final SWTBotTree tree = projectExplorerBot.tree();
        SWTBotTreeItem[] items = tree.getAllItems();
        for (SWTBotTreeItem swtBotTreeItem : items) {
            assertNotEquals(SOME_PROJECT_SHADOW_NAME, swtBotTreeItem.getText());
        }
    }

    private static void toggleFilters() {
        SWTBotView viewBot = fBot.viewByTitle(PROJECT_EXPLORER_TITLE);
        viewBot.setFocus();

        SWTBotRootMenu viewMenu = viewBot.viewMenu();
        String title = CUSTOMIZE_VIEW_DIALOG_TITLE_4_7;
        try {
            viewMenu.menu(CUSTOMIZE_VIEW_MENU_ITEM_4_7).click();
        } catch (WidgetNotFoundException e) {
            viewMenu.menu(CUSTOMIZE_VIEW_MENU_ITEM_4_6).click();
            title = CUSTOMIZE_VIEW_DIALOG_TITLE_4_6;
        }
        SWTBotShell shell = fBot.shell(title).activate();

        SWTBotTable table = shell.bot().table();
        SWTBotTableItem item = table.getTableItem(CUSTOMIZE_VIEW_RESOUCES_FILTER);
        item.select();
        item.toggleCheck();
        item = table.getTableItem(CUSTOMIZE_VIEW_SHADOW_FILTER);
        item.select();
        item.toggleCheck();

        shell.bot().button(OK_BUTTON).click();
    }
}
