/******************************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.integration.swtbot.tests.projectexplorer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.wizards.SWTBotImportWizardUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * SWTBot test for testing Project Explorer Refresh action
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SWTBotJunit4ClassRunner.class)
public class ProjectExplorerRefreshTest {
    private static final String TRACE_PROJECT_NAME = "test";
    private static final String C_PROJECT_NAME = "c-project";

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    private static final long DISK_ACCESS_TIMEOUT = 120000L;

    private static final File TEST_TRACES_PATH = new File(new Path(TmfTraceManager.getTemporaryDirPath()).append("testtraces").toOSString());

    private static final File TEST_FILE_KERNEL = new File(getPath("import/kernel-overlap-testing"));
    private static final File TEST_FILE_KERNEL_CLASH = new File(getPath("import/z-clashes/kernel-overlap-testing"));
    private static final File TEST_FILE_UST = new File(getPath("import/ust-overlap-testing"));

    private static SWTWorkbenchBot fBot;

    private static File fTracesFolder = null;
    private static File fCProjectFolder = null;

    private static String getPath(String relativePath) {
        return new Path(TEST_TRACES_PATH.getAbsolutePath()).append(relativePath).toOSString();
    }

    /**
     * Test Class setup
     *
     * @throws Exception
     *             on error
     */
    @BeforeClass
    public static void init() throws Exception {
        TestDirectoryStructureUtil.generateTraceStructure(TEST_TRACES_PATH);

        SWTBotUtils.initialize();

        /* Set up for SWTBot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        fBot = new SWTWorkbenchBot();

        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();

        SWTBotUtils.createProject(TRACE_PROJECT_NAME);
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(TRACE_PROJECT_NAME);
        fTracesFolder = new File(Objects.requireNonNull(TmfProjectRegistry.getProject(project, true).getTracesFolder()).getResource().getLocation().toOSString());

        createCProject(C_PROJECT_NAME);
        setTracingNature(C_PROJECT_NAME);
        IProject cProject = ResourcesPlugin.getWorkspace().getRoot().getProject(C_PROJECT_NAME);
        fCProjectFolder = cProject.getLocation().toFile();
    }

    /**
     * Test class tear down method.
     */
    @AfterClass
    public static void tearDown() {
        SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, fBot);
        SWTBotUtils.deleteProject(C_PROJECT_NAME, fBot);
        fLogger.removeAllAppenders();
    }

    /**
     * Test tear down method.
     */
    @After
    public void afterTest() {
        SWTBotUtils.closeSecondaryShells(fBot);
    }

    /**
     * Test Refresh after adding a trace on the file system.
     *
     * @throws IOException if an exception occurs
     */
    @Test
    public void test16_01RefreshTraceAdded() throws IOException {
        FileUtils.copyDirectory(TEST_FILE_KERNEL, FileUtils.getFile(fTracesFolder, TEST_FILE_KERNEL.getName()));
        FileUtils.copyDirectory(TEST_FILE_UST, FileUtils.getFile(fTracesFolder, TEST_FILE_UST.getName()));
        SWTBotTreeItem tracesFolder = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);
        assertEquals(0, tracesFolder.getItems().length);
        refresh(() -> tracesFolder.contextMenu().menu("Refresh").click());
        SWTBotTreeItem kernelTrace = SWTBotUtils.getTraceProjectItem(fBot, tracesFolder, TEST_FILE_KERNEL.getName());
        kernelTrace.contextMenu().menu("Select Trace Type...", "Common Trace Format", "Linux Kernel Trace").click();
        SWTBotTreeItem ustTrace = SWTBotUtils.getTraceProjectItem(fBot, tracesFolder, TEST_FILE_UST.getName());
        ustTrace.contextMenu().menu("Select Trace Type...", "Common Trace Format", "LTTng UST Trace").click();
    }

    /**
     * Test Refresh after modifying trace content while trace is opened.
     *
     * @throws IOException if an exception occurs
     */
    @Test
    public void test16_02RefreshOpenedTraceContentModified() throws IOException {
        SWTBotTreeItem tracesFolder = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);
        SWTBotTreeItem kernelTrace = SWTBotUtils.getTraceProjectItem(fBot, tracesFolder, TEST_FILE_KERNEL.getName());
        kernelTrace.contextMenu().menu("Open").click();
        SWTBotUtils.activateEditor(fBot, TEST_FILE_KERNEL.getName());
        tracesFolder.contextMenu().menu("Open As Experiment...", "Generic Experiment").click();
        SWTBotUtils.activateEditor(fBot, "Experiment");
        SWTBotTreeItem project = SWTBotUtils.selectProject(fBot, TRACE_PROJECT_NAME);
        SWTBotTreeItem experiment = SWTBotUtils.getTraceProjectItem(fBot, project, "Experiments", "Experiment");
        FileUtils.copyDirectory(TEST_FILE_KERNEL_CLASH, FileUtils.getFile(fTracesFolder, TEST_FILE_KERNEL.getName()), false);
        // false -> last modified times are copied file time stamps
        assertTrue(kernelTrace.contextMenu().menuItems().contains("Delete Supplementary Files..."));
        assertTrue(experiment.contextMenu().menuItems().contains("Delete Supplementary Files..."));
        refresh(() -> tracesFolder.contextMenu().menu("Refresh").click());
        SWTBotShell shell = fBot.shell("Trace Changed");
        shell.bot().button("No").click();
        assertTrue(kernelTrace.contextMenu().menuItems().contains("Delete Supplementary Files..."));
        assertTrue(experiment.contextMenu().menuItems().contains("Delete Supplementary Files..."));
        SWTBotUtils.activateEditor(fBot, TEST_FILE_KERNEL.getName());
        SWTBotUtils.activateEditor(fBot, "Experiment");
        FileUtils.copyDirectory(TEST_FILE_KERNEL, FileUtils.getFile(fTracesFolder, TEST_FILE_KERNEL.getName()), true);
        // true -> last modified times are original file time stamps
        assertTrue(kernelTrace.contextMenu().menuItems().contains("Delete Supplementary Files..."));
        refresh(() -> tracesFolder.contextMenu().menu("Refresh").click());
        shell = fBot.shell("Trace Changed");
        shell.bot().button("Yes").click();
        SWTBotUtils.waitUntil(treeItem -> !treeItem.contextMenu().menuItems().contains("Delete Supplementary Files..."),
                kernelTrace, "Supplementary Files did not get deleted");
        assertEquals(0, fBot.editors().size());
    }

    /**
     * Test Refresh after modifying trace content while trace is closed.
     *
     * @throws IOException if an exception occurs
     */
    @Test
    public void test16_03RefreshClosedTraceContentModified() throws IOException {
        SWTBotTreeItem tracesFolder = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);
        SWTBotTreeItem kernelTrace = SWTBotUtils.getTraceProjectItem(fBot, tracesFolder, TEST_FILE_KERNEL.getName());
        SWTBotTreeItem ustTrace = SWTBotUtils.getTraceProjectItem(fBot, tracesFolder, TEST_FILE_UST.getName());
        kernelTrace.contextMenu().menu("Select Trace Type...", "Common Trace Format", "Linux Kernel Trace").click();
        kernelTrace.contextMenu().menu("Open").click();
        SWTBotUtils.activateEditor(fBot, TEST_FILE_KERNEL.getName()).close();
        tracesFolder.contextMenu().menu("Open As Experiment...", "Generic Experiment").click();
        SWTBotUtils.activateEditor(fBot, "Experiment").close();
        SWTBotTreeItem project = SWTBotUtils.selectProject(fBot, TRACE_PROJECT_NAME);
        SWTBotTreeItem experiment = SWTBotUtils.getTraceProjectItem(fBot, project, "Experiments", "Experiment");
        FileUtils.touch(FileUtils.getFile(fTracesFolder, TEST_FILE_KERNEL.getName(), "channel1"));
        FileUtils.deleteQuietly(FileUtils.getFile(fTracesFolder, TEST_FILE_UST.getName(), "channel0"));
        assertTrue(kernelTrace.contextMenu().menuItems().contains("Delete Supplementary Files..."));
        assertTrue(ustTrace.contextMenu().menuItems().contains("Delete Supplementary Files..."));
        assertTrue(experiment.contextMenu().menuItems().contains("Delete Supplementary Files..."));
        refresh(() -> tracesFolder.contextMenu().menu("Refresh").click());
        SWTBotUtils.waitUntil(treeItem -> !treeItem.contextMenu().menuItems().contains("Delete Supplementary Files..."),
                kernelTrace, "Supplementary Files did not get deleted");
        SWTBotUtils.waitUntil(treeItem -> !treeItem.contextMenu().menuItems().contains("Delete Supplementary Files..."),
                ustTrace, "Supplementary Files did not get deleted");
        SWTBotUtils.waitUntil(treeItem -> !treeItem.contextMenu().menuItems().contains("Delete Supplementary Files..."),
                experiment, "Supplementary Files did not get deleted");
    }

    /**
     * Test Refresh after deleting a trace on the file system.
     *
     * @throws IOException if an exception occurs
     */
    @Test
    public void test16_04RefreshTraceDeleted() throws IOException {
        SWTBotTreeItem tracesFolder = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);
        assertEquals(2, tracesFolder.getItems().length);
        SWTBotUtils.getTraceProjectItem(fBot, tracesFolder, TEST_FILE_KERNEL.getName());
        SWTBotUtils.getTraceProjectItem(fBot, tracesFolder, TEST_FILE_UST.getName());
        FileUtils.deleteDirectory(FileUtils.getFile(fTracesFolder, TEST_FILE_UST.getName()));
        refresh(() -> fBot.menu().menu("File", "Refresh").click());
        assertEquals(1, tracesFolder.getItems().length);
        SWTBotUtils.getTraceProjectItem(fBot, tracesFolder, TEST_FILE_KERNEL.getName());
    }

    /**
     * Test refresh after deleting a trace in a c-project
     *
     * @throws IOException
     *             if an exception occurs
     */
    @Test
    public void test16_05CProjectRefreshTraceDeleted() throws IOException {
        // Copy traces inside the c-project
        FileUtils.copyDirectory(TEST_FILE_KERNEL, FileUtils.getFile(fCProjectFolder, TEST_FILE_KERNEL.getName()));
        FileUtils.copyDirectory(TEST_FILE_UST, FileUtils.getFile(fCProjectFolder, TEST_FILE_UST.getName()));
        SWTBotTreeItem project = SWTBotUtils.selectProject(fBot, C_PROJECT_NAME);
        refresh(() -> project.contextMenu().menu("Refresh").click());

        // Import traces in Trace Compass sub project as links
        SWTBotTreeItem tracesFolder = SWTBotUtils.getTraceProjectItem(fBot, project, "Trace Compass", "Traces");
        tracesFolder.contextMenu().menu("Import...").click();
        SWTBotImportWizardUtils.selectImportFromDirectory(fBot, fCProjectFolder.getAbsolutePath());
        SWTBotImportWizardUtils.selectFolder(fBot, true, C_PROJECT_NAME, TEST_FILE_KERNEL.getName());
        SWTBotImportWizardUtils.selectFolder(fBot, true, C_PROJECT_NAME, TEST_FILE_UST.getName());
        SWTBotShell shell = fBot.shell("Trace Import");
        shell.bot().button("Finish").click();
        fBot.waitUntil(Conditions.shellCloses(shell), DISK_ACCESS_TIMEOUT);
        WaitUtils.waitForJobs();

        // Be sure that the two traces are imported
        tracesFolder.expand();
        fBot.waitUntil(ConditionHelpers.treeItemCount(tracesFolder, 2));

        // Create an experiment
        tracesFolder.contextMenu().menu("Open As Experiment...", "Generic Experiment").click();
        SWTBotUtils.activateEditor(fBot, "Experiment").close();
        SWTBotTreeItem experiment = SWTBotUtils.getTraceProjectItem(fBot, project, "Trace Compass", "Experiments", "Experiment");
        experiment.expand();
        fBot.waitUntil(ConditionHelpers.treeItemCount(experiment, 5));

        // Delete one trace from the c-project and verify that the trace is not in Trace Compass anymore
        FileUtils.deleteDirectory(FileUtils.getFile(fCProjectFolder, TEST_FILE_UST.getName()));
        refresh(() -> project.contextMenu().menu("Refresh").click());
        fBot.waitUntil(ConditionHelpers.treeItemCount(tracesFolder, 1));

        // Make sure that the experiment is still there and the trace is not there
        fBot.waitUntil(ConditionHelpers.treeItemCount(experiment, 4));

        // Delete the remaining trace from the c-project and verify that the trace is not in Trace Compass anymore
        FileUtils.deleteDirectory(FileUtils.getFile(fCProjectFolder, TEST_FILE_KERNEL.getName()));
        refresh(() -> project.contextMenu().menu("Refresh").click());
        fBot.waitUntil(ConditionHelpers.treeItemCount(tracesFolder, 0));

        // Experiments folder should be empty at this point
        SWTBotTreeItem experimentsFolder = SWTBotUtils.getTraceProjectItem(fBot, project, "Trace Compass", "Experiments");
        experimentsFolder.expand();
        fBot.waitUntil(ConditionHelpers.treeItemCount(experimentsFolder, 0));
    }

    private static void createCProject(String projectName) throws CoreException {
        IProgressMonitor monitor = new NullProgressMonitor();
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        project.create(monitor);
        project.open(monitor);
        IProjectDescription description = project.getDescription();
        description.setNatureIds(new String[] { "org.eclipse.cdt.core.cnature" });
        project.setDescription(description, monitor);
        project.open(monitor);
    }

    private static void setTracingNature(String projectName) {
        SWTBotTreeItem projectItem = SWTBotUtils.selectProject(fBot, projectName);
        projectItem.contextMenu().menu("Configure", "Configure or convert to Tracing Project").click();
        WaitUtils.waitForJobs();
    }

    private static void refresh(Runnable runnable) {
        AtomicBoolean resourceChanged = new AtomicBoolean();
        IResourceChangeListener listener = event -> {
            resourceChanged.set(true);
        };
        try {
            ResourcesPlugin.getWorkspace().addResourceChangeListener(listener);
            SWTBotUtils.waitUntil(refresh -> {
                refresh.run();
                return resourceChanged.get();
            }, runnable, "Resource change event not received");
        } finally {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
        }
    }
}
