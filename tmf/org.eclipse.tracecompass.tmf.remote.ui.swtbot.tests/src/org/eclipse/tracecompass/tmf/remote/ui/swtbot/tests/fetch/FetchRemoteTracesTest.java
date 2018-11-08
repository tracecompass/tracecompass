/*******************************************************************************
 * Copyright (c) 2015, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.remote.ui.swtbot.tests.fetch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.ctf.core.tests.shared.LttngTraceGenerator;
import org.eclipse.tracecompass.tmf.remote.ui.swtbot.tests.TmfRemoteUISWTBotTestPlugin;
import org.eclipse.tracecompass.tmf.ui.dialog.TmfFileDialogFactory;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test the Fetch Remote Traces wizard.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class FetchRemoteTracesTest {

    private static final QualifiedName ORIGINAL = new QualifiedName("original", "original");
    private static final String AUTHENTICATION_SHELL_NAME = "Authentication Message";
    private static final String CONFIRMATION_SHELL_NAME = "Confirmation";
    private static final String CONNECTION_NODE1_NAME = "node1";
    private static final String CONNECTION_NODE2_NAME = "node2";
    private static final String CONNECTION_NODE_TEXT = CONNECTION_NODE1_NAME + " (file://)";
    private static final String FETCH_COMMAND_NAME = "Fetch Remote Traces...";
    private static final String FETCH_SHELL_NAME = "Fetch Remote Traces";
    private static final String LTTNG_TRACE_FILE_PATTERN = ".*synthetic.*";
    private static final String PASSWORD_SHELL_NAME = "Password Required";
    private static final String PROFILE_NAME = "new profile";
    private static final String PROFILES_LOCATION;
    private static final String PROJECT_EXPLORER = "Project Explorer";
    private static final String PROJECT_NAME = "Test";
    private static final String SYSLOG_FILE_PATTERN = ".*syslog";
    private static final String WILDCARD_PATTERN = ".*";
    private static final String TRACE_GROUP_NODE_TEXT;
    private static final String TRACE_LOCATION;
    private static final String TRACE_TYPE_KERNEL = "org.eclipse.linuxtools.lttng2.kernel.tracetype";
    private static final String TRACE_TYPE_SYSLOG = "org.eclipse.linuxtools.tmf.tests.stubs.trace.text.testsyslog";

    private static SWTWorkbenchBot fBot;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    static {
        String traceLocation = "";
        String profilesLocation = "";
        try {
            IPath resourcesPath = new Path("resources");
            File resourcesFile = getBundleFile(resourcesPath);
            // Create a sub directory to test the trace folders at the same time
            IPath subDirFullPath = new Path(resourcesFile.getAbsolutePath()).append("generated");
            File subDirFile = new File(subDirFullPath.toOSString());
            subDirFile.mkdir();

            IPath generatedTraceFullPath = subDirFullPath.append("synthetic-trace");
            File generatedTraceFile = new File(generatedTraceFullPath.toOSString());
            LttngTraceGenerator.generateLttngTrace(generatedTraceFile);
            traceLocation = new Path(resourcesFile.getAbsolutePath()).toString();
            profilesLocation = getBundleFile(new Path("profiles/test-profiles.xml")).getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        TRACE_LOCATION = traceLocation;
        TRACE_GROUP_NODE_TEXT = TRACE_LOCATION + " (recursive)";
        PROFILES_LOCATION = profilesLocation;
    }

    private static File getBundleFile(IPath relativePath) throws URISyntaxException, IOException {
        return new File(FileLocator.toFileURL(FileLocator.find(TmfRemoteUISWTBotTestPlugin.getDefault().getBundle(), relativePath, null)).toURI());
    }

    /** Test Class setup */
    @BeforeClass
    public static void init() {
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        SWTBotUtils.initialize();
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        fLogger.removeAllAppenders();
        fLogger.addAppender(new NullAppender());
        fBot = new SWTWorkbenchBot();

        /* finish waiting for eclipse to load */
        WaitUtils.waitForJobs();

        importProfiles();
    }

    /**
     * Before test
     */
    @Before
    public void before() {
        SWTBotUtils.createProject(PROJECT_NAME);
        WaitUtils.waitForJobs();
    }

    /**
     * After test
     */
    @After
    public void after() {
        fBot.closeAllEditors();
        SWTBotUtils.deleteProject(PROJECT_NAME, fBot);
    }

    /**
     * Clean up
     */
    @AfterClass
    public static void afterClass() {
        clearProfiles();
        fLogger.removeAllAppenders();
    }

    private static class TraceCountCondition extends DefaultCondition {

        private final TmfProjectElement fProject;
        private final int fExpectedCount;

        public TraceCountCondition(TmfProjectElement project, int expectedNumber) {
            fProject = project;
            fExpectedCount = expectedNumber;
        }

        @Override
        public boolean test() throws Exception {
            final TmfTraceFolder tracesFolder = fProject.getTracesFolder();
            return ((tracesFolder != null) && (tracesFolder.getTraces().size() == fExpectedCount));
        }

        @Override
        public String getFailureMessage() {
            return NLS.bind("The project {0} does not contain {1} traces.", fProject.getName(), fExpectedCount);
        }
    }

    /**
     * Test creating a profile, fetching all using the profile.
     */
    @Test
    public void testImportAll() {
        testImport(() -> {
        }, () -> {
            final TmfProjectElement project = TmfProjectRegistry.getProject(ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME), true);
            fBot.waitUntil(new TraceCountCondition(project, 3));
            final TmfTraceFolder tracesFolder = project.getTracesFolder();
            assertNotNull(tracesFolder);
            List<TmfTraceElement> traces = tracesFolder.getTraces();
            assertEquals(3, traces.size());
            testTrace(traces.get(0), CONNECTION_NODE1_NAME + "/resources/generated/synthetic-trace", TRACE_TYPE_KERNEL);
            testTrace(traces.get(1), CONNECTION_NODE1_NAME + "/resources/syslog", TRACE_TYPE_SYSLOG);
            testTrace(traces.get(2), CONNECTION_NODE1_NAME + "/resources/unrecognized", null);
        });
    }

    /**
     * Test creating a profile, fetching only one trace
     */
    @Test
    public void testImportOnlyOne() {
        testImport(() -> {
            SWTBotTree tree = fBot.tree();
            fBot.button("Deselect All").click();
            int length = tree.getAllItems().length;
            assertTrue(length > 0);
            // Selecting the second trace under node > traceGroup
            SWTBotTreeItem node = getTreeItem(fBot, tree, new String[] { CONNECTION_NODE_TEXT, TRACE_GROUP_NODE_TEXT }).getNode(1);
            assertEquals("syslog", node.getText());
            node.check();
        }, () -> {
            TmfProjectElement project = TmfProjectRegistry.getProject(ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME), true);
            fBot.waitUntil(new TraceCountCondition(project, 1));
            final TmfTraceFolder tracesFolder = project.getTracesFolder();
            assertNotNull(tracesFolder);
            List<TmfTraceElement> traces = tracesFolder.getTraces();
            assertEquals(1, traces.size());
            testTrace(traces.get(0), CONNECTION_NODE1_NAME + "/resources/syslog", TRACE_TYPE_SYSLOG);
        });
    }

    /**
     * Test creating a profile, fetching nothing
     */
    @Test
    public void testImportNothing() {
        testImport(() -> {
            fBot.button("Deselect All").click();
        }, () -> {
            TmfProjectElement project = TmfProjectRegistry.getProject(ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME), true);
            final TmfTraceFolder tracesFolder = project.getTracesFolder();
            assertNotNull(tracesFolder);
            List<TmfTraceElement> traces = tracesFolder.getTraces();
            assertEquals(0, traces.size());
        });
    }

    /**
     * Test to verify that empty files are omitted.
     */
    @Test
    public void testEmptyFile() {
        testImport(() -> {
            SWTBotTree tree = fBot.tree();
            fBot.button("Deselect All").click();
            int length = tree.getAllItems().length;
            assertTrue(length > 0);

            SWTBotTreeItem groupNode = getTreeItem(fBot, tree, new String[] { CONNECTION_NODE_TEXT, TRACE_GROUP_NODE_TEXT });
            /*
             *  Currently there are 4 items at the location where 1 file has 0 bytes.
             *  Verify that empty file is not shown.
             */
            assertEquals(3, groupNode.getItems().length);
        }, () -> {
            TmfProjectElement project = TmfProjectRegistry.getProject(ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME), true);
            final TmfTraceFolder tracesFolder = project.getTracesFolder();
            assertNotNull(tracesFolder);
            List<TmfTraceElement> traces = tracesFolder.getTraces();
            assertEquals(0, traces.size());
        });
    }

    /**
     * Test 7.3: Re-Import profiles
     */
    @Test
    public void test_7_03() {
        openRemoteProfilePreferences();
        TmfFileDialogFactory.setOverrideFiles(PROFILES_LOCATION);
        fBot.button("Import").click();

        String errorMessage = fBot.text(1).getText();
        assertTrue(errorMessage.endsWith("Duplicate profile names"));
        assertTrue(!SWTBotUtils.anyButtonOf(fBot, "Apply and Close", "OK").isEnabled());

        // Remove the imported duplicate profiles that are all selected
        fBot.button("Remove").click();

        SWTBotUtils.pressOKishButtonInPreferences(fBot);
    }

    /**
     * Test 8.4: Run Profile "TestAllRecursive"
     */
    @Test
    public void test_8_04() {
        SWTBotView projectExplorerBot = fBot.viewByTitle(PROJECT_EXPLORER);
        projectExplorerBot.show();
        SWTBotTreeItem tracesFolderItem = getTracesFolderTreeItem(projectExplorerBot);

        tracesFolderItem.contextMenu(FETCH_COMMAND_NAME).click();
        SWTBotShell shell = fBot.shell(FETCH_SHELL_NAME).activate();
        fBot.comboBox().setSelection("TestAllRecursive");
        fBot.button("Next >").click();
        fBot.button("Finish").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        WaitUtils.waitForJobs();

        TmfProjectElement project = TmfProjectRegistry.getProject(ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME), true);
        fBot.waitUntil(new TraceCountCondition(project, 3));
        final TmfTraceFolder tracesFolder = project.getTracesFolder();
        assertNotNull(tracesFolder);
        List<TmfTraceElement> traces = tracesFolder.getTraces();
        assertEquals(3, traces.size());
        testTrace(traces.get(0), CONNECTION_NODE1_NAME + "/resources/generated/synthetic-trace", TRACE_TYPE_KERNEL);
        testTrace(traces.get(1), CONNECTION_NODE1_NAME + "/resources/syslog", TRACE_TYPE_SYSLOG);
        testTrace(traces.get(2), CONNECTION_NODE1_NAME + "/resources/unrecognized", null);
    }

    /**
     * Test 8.5: Re-Run Profile "TestAllRecursive" (Rename)
     */
    @Test
    public void test_8_05() {
        SWTBotView projectExplorerBot = fBot.viewByTitle(PROJECT_EXPLORER);
        projectExplorerBot.show();
        SWTBotTreeItem tracesFolderItem = getTracesFolderTreeItem(projectExplorerBot);

        tracesFolderItem.contextMenu(FETCH_COMMAND_NAME).click();
        SWTBotShell shell = fBot.shell(FETCH_SHELL_NAME).activate();
        fBot.comboBox().setSelection("TestAllRecursive");
        fBot.button("Next >").click();
        fBot.button("Finish").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        WaitUtils.waitForJobs();

        tracesFolderItem.contextMenu(FETCH_COMMAND_NAME).click();
        shell = fBot.shell(FETCH_SHELL_NAME).activate();
        fBot.comboBox().setSelection("TestAllRecursive");
        fBot.button("Next >").click();
        fBot.button("Finish").click();
        fBot.shell(CONFIRMATION_SHELL_NAME).activate();
        fBot.button("Rename").click();
        fBot.shell(CONFIRMATION_SHELL_NAME).activate();
        fBot.button("Rename All").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        WaitUtils.waitForJobs();

        TmfProjectElement project = TmfProjectRegistry.getProject(ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME), true);
        fBot.waitUntil(new TraceCountCondition(project, 6));
        final TmfTraceFolder tracesFolder = project.getTracesFolder();
        assertNotNull(tracesFolder);
        List<TmfTraceElement> traces = tracesFolder.getTraces();
        assertEquals(6, traces.size());
        testTrace(traces.get(0), CONNECTION_NODE1_NAME + "/resources/generated/synthetic-trace", TRACE_TYPE_KERNEL);
        testTrace(traces.get(1), CONNECTION_NODE1_NAME + "/resources/generated/synthetic-trace(2)", TRACE_TYPE_KERNEL);
        testTrace(traces.get(2), CONNECTION_NODE1_NAME + "/resources/syslog", TRACE_TYPE_SYSLOG);
        testTrace(traces.get(3), CONNECTION_NODE1_NAME + "/resources/unrecognized", null);
        testTrace(traces.get(4), CONNECTION_NODE1_NAME + "/resources/syslog(2)", TRACE_TYPE_SYSLOG);
        testTrace(traces.get(5), CONNECTION_NODE1_NAME + "/resources/unrecognized(2)", null);
    }

    /**
     * Test 8.6: Re-Run Profile "TestAllRecursive" (Overwrite)
     *
     * @throws CoreException if an exception occurs
     */
    @Test
    public void test_8_06() throws CoreException {
        SWTBotView projectExplorerBot = fBot.viewByTitle(PROJECT_EXPLORER);
        projectExplorerBot.show();
        SWTBotTreeItem tracesFolderItem = getTracesFolderTreeItem(projectExplorerBot);

        tracesFolderItem.contextMenu(FETCH_COMMAND_NAME).click();
        SWTBotShell shell = fBot.shell(FETCH_SHELL_NAME).activate();
        fBot.comboBox().setSelection("TestAllRecursive");
        fBot.button("Next >").click();
        fBot.button("Finish").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        WaitUtils.waitForJobs();

        TmfProjectElement project = TmfProjectRegistry.getProject(ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME), true);
        fBot.waitUntil(new TraceCountCondition(project, 3));
        final TmfTraceFolder tracesFolder = project.getTracesFolder();
        assertNotNull(tracesFolder);
        for (TmfTraceElement traceElement : tracesFolder.getTraces()) {
            traceElement.getResource().setPersistentProperty(ORIGINAL, Boolean.TRUE.toString());
        }

        tracesFolderItem.contextMenu(FETCH_COMMAND_NAME).click();
        shell = fBot.shell(FETCH_SHELL_NAME).activate();
        fBot.comboBox().setSelection("TestAllRecursive");
        fBot.button("Next >").click();
        fBot.button("Finish").click();
        fBot.shell(CONFIRMATION_SHELL_NAME).activate();
        fBot.button("Overwrite").click();
        fBot.shell(CONFIRMATION_SHELL_NAME).activate();
        fBot.button("Overwrite All").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        WaitUtils.waitForJobs();

        fBot.waitUntil(new TraceCountCondition(project, 3));
        List<TmfTraceElement> traces = tracesFolder.getTraces();
        assertEquals(3, traces.size());
        testTrace(traces.get(0), CONNECTION_NODE1_NAME + "/resources/generated/synthetic-trace", TRACE_TYPE_KERNEL);
        testTrace(traces.get(1), CONNECTION_NODE1_NAME + "/resources/syslog", TRACE_TYPE_SYSLOG);
        testTrace(traces.get(2), CONNECTION_NODE1_NAME + "/resources/unrecognized", null);
        // Verify overwritten traces lost the original property
        for (TmfTraceElement traceElement : traces) {
            assertNull(traceElement.getResource().getPersistentProperty(ORIGINAL));
        }
    }

    /**
     * Test 8.7: Re-Run Profile "TestAllRecursive" (Skip)
     *
     * @throws CoreException if an exception occurs
     */
    @Test
    public void test_8_07() throws CoreException {
        SWTBotView projectExplorerBot = fBot.viewByTitle(PROJECT_EXPLORER);
        projectExplorerBot.show();
        SWTBotTreeItem tracesFolderItem = getTracesFolderTreeItem(projectExplorerBot);

        tracesFolderItem.contextMenu(FETCH_COMMAND_NAME).click();
        SWTBotShell shell = fBot.shell(FETCH_SHELL_NAME).activate();
        fBot.comboBox().setSelection("TestAllRecursive");
        fBot.button("Next >").click();
        fBot.button("Finish").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        WaitUtils.waitForJobs();

        TmfProjectElement project = TmfProjectRegistry.getProject(ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME), true);
        fBot.waitUntil(new TraceCountCondition(project, 3));
        final TmfTraceFolder tracesFolder = project.getTracesFolder();
        assertNotNull(tracesFolder);
        for (TmfTraceElement traceElement : tracesFolder.getTraces()) {
            traceElement.getResource().setPersistentProperty(ORIGINAL, Boolean.TRUE.toString());
        }

        tracesFolderItem.contextMenu(FETCH_COMMAND_NAME).click();
        shell = fBot.shell(FETCH_SHELL_NAME).activate();
        fBot.comboBox().setSelection("TestAllRecursive");
        fBot.button("Next >").click();
        fBot.button("Finish").click();
        fBot.shell(CONFIRMATION_SHELL_NAME).activate();
        fBot.button("Skip").click();
        fBot.shell(CONFIRMATION_SHELL_NAME).activate();
        fBot.button("Skip All").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        WaitUtils.waitForJobs();

        fBot.waitUntil(new TraceCountCondition(project, 3));
        assertNotNull(tracesFolder);
        List<TmfTraceElement> traces = tracesFolder.getTraces();
        assertEquals(3, traces.size());
        testTrace(traces.get(0), CONNECTION_NODE1_NAME + "/resources/generated/synthetic-trace", TRACE_TYPE_KERNEL);
        testTrace(traces.get(1), CONNECTION_NODE1_NAME + "/resources/syslog", TRACE_TYPE_SYSLOG);
        testTrace(traces.get(2), CONNECTION_NODE1_NAME + "/resources/unrecognized", null);
        // Verify skipped traces kept the original property
        for (TmfTraceElement traceElement : traces) {
            assertNotNull(traceElement.getResource().getPersistentProperty(ORIGINAL));
        }
    }

    /**
     * Test 8.8: Re-Run Profile "TestAllRecursive" (Overwrite without warning)
     *
     * @throws CoreException if an exception occurs
     */
    @Test
    public void test_8_08() throws CoreException {
        SWTBotView projectExplorerBot = fBot.viewByTitle(PROJECT_EXPLORER);
        projectExplorerBot.show();
        SWTBotTreeItem tracesFolderItem = getTracesFolderTreeItem(projectExplorerBot);

        tracesFolderItem.contextMenu(FETCH_COMMAND_NAME).click();
        SWTBotShell shell = fBot.shell(FETCH_SHELL_NAME).activate();
        fBot.comboBox().setSelection("TestAllRecursive");
        fBot.button("Next >").click();
        fBot.button("Finish").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        WaitUtils.waitForJobs();

        TmfProjectElement project = TmfProjectRegistry.getProject(ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME), true);
        fBot.waitUntil(new TraceCountCondition(project, 3));
        final TmfTraceFolder tracesFolder = project.getTracesFolder();
        assertNotNull(tracesFolder);
        for (TmfTraceElement traceElement : tracesFolder.getTraces()) {
            traceElement.getResource().setPersistentProperty(ORIGINAL, Boolean.TRUE.toString());
        }

        tracesFolderItem.contextMenu(FETCH_COMMAND_NAME).click();
        shell = fBot.shell(FETCH_SHELL_NAME).activate();
        fBot.comboBox().setSelection("TestAllRecursive");
        fBot.checkBox("Overwrite existing trace without warning").select();
        fBot.button("Next >").click();
        fBot.button("Finish").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        WaitUtils.waitForJobs();

        fBot.waitUntil(new TraceCountCondition(project, 3));
        assertNotNull(tracesFolder);
        List<TmfTraceElement> traces = tracesFolder.getTraces();
        assertEquals(3, traces.size());
        testTrace(traces.get(0), CONNECTION_NODE1_NAME + "/resources/generated/synthetic-trace", TRACE_TYPE_KERNEL);
        testTrace(traces.get(1), CONNECTION_NODE1_NAME + "/resources/syslog", TRACE_TYPE_SYSLOG);
        testTrace(traces.get(2), CONNECTION_NODE1_NAME + "/resources/unrecognized", null);
        // Verify overwritten traces lost the original property
        for (TmfTraceElement traceElement : traces) {
            assertNull(traceElement.getResource().getPersistentProperty(ORIGINAL));
        }
    }

    /**
     * Test 8.9: Run Profile "TestAllRecursive" (Skip next page)
     */
    @Test
    public void test_8_09() {
        SWTBotView projectExplorerBot = fBot.viewByTitle(PROJECT_EXPLORER);
        projectExplorerBot.show();
        SWTBotTreeItem tracesFolderItem = getTracesFolderTreeItem(projectExplorerBot);

        tracesFolderItem.contextMenu(FETCH_COMMAND_NAME).click();
        SWTBotShell shell = fBot.shell(FETCH_SHELL_NAME).activate();
        fBot.comboBox().setSelection("TestAllRecursive");
        fBot.button("Finish").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        WaitUtils.waitForJobs();

        TmfProjectElement project = TmfProjectRegistry.getProject(ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME), true);
        fBot.waitUntil(new TraceCountCondition(project, 3));
        final TmfTraceFolder tracesFolder = project.getTracesFolder();
        assertNotNull(tracesFolder);
        List<TmfTraceElement> traces = tracesFolder.getTraces();
        assertEquals(3, traces.size());
        testTrace(traces.get(0), CONNECTION_NODE1_NAME + "/resources/generated/synthetic-trace", TRACE_TYPE_KERNEL);
        testTrace(traces.get(1), CONNECTION_NODE1_NAME + "/resources/syslog", TRACE_TYPE_SYSLOG);
        testTrace(traces.get(2), CONNECTION_NODE1_NAME + "/resources/unrecognized", null);
    }

    /**
     * Test 8.10: Run Profile "TestAllNonRecursive"
     */
    @Test
    public void test_8_10() {
        SWTBotView projectExplorerBot = fBot.viewByTitle(PROJECT_EXPLORER);
        projectExplorerBot.show();
        SWTBotTreeItem tracesFolderItem = getTracesFolderTreeItem(projectExplorerBot);

        tracesFolderItem.contextMenu(FETCH_COMMAND_NAME).click();
        SWTBotShell shell = fBot.shell(FETCH_SHELL_NAME).activate();
        fBot.comboBox().setSelection("TestAllNonRecursive");
        fBot.button("Next >").click();
        fBot.button("Finish").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        WaitUtils.waitForJobs();

        TmfProjectElement project = TmfProjectRegistry.getProject(ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME), true);
        fBot.waitUntil(new TraceCountCondition(project, 2));
        final TmfTraceFolder tracesFolder = project.getTracesFolder();
        assertNotNull(tracesFolder);
        List<TmfTraceElement> traces = tracesFolder.getTraces();
        assertEquals(2, traces.size());
        testTrace(traces.get(0), CONNECTION_NODE1_NAME + "/resources/syslog", TRACE_TYPE_SYSLOG);
        testTrace(traces.get(1), CONNECTION_NODE1_NAME + "/resources/unrecognized", null);
    }

    /**
     * Test 8.11: Run Profile "TestSpecificRecursive"
     */
    @Test
    public void test_8_11() {
        SWTBotView projectExplorerBot = fBot.viewByTitle(PROJECT_EXPLORER);
        projectExplorerBot.show();
        SWTBotTreeItem tracesFolderItem = getTracesFolderTreeItem(projectExplorerBot);

        tracesFolderItem.contextMenu(FETCH_COMMAND_NAME).click();
        SWTBotShell shell = fBot.shell(FETCH_SHELL_NAME).activate();
        fBot.comboBox().setSelection("TestSpecificRecursive");
        fBot.button("Next >").click();
        fBot.button("Finish").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        WaitUtils.waitForJobs();

        TmfProjectElement project = TmfProjectRegistry.getProject(ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME), true);
        fBot.waitUntil(new TraceCountCondition(project, 2));
        final TmfTraceFolder tracesFolder = project.getTracesFolder();
        assertNotNull(tracesFolder);
        List<TmfTraceElement> traces = tracesFolder.getTraces();
        assertEquals(2, traces.size());
        testTrace(traces.get(0), CONNECTION_NODE1_NAME + "/resources/generated/synthetic-trace", TRACE_TYPE_KERNEL);
        testTrace(traces.get(1), CONNECTION_NODE1_NAME + "/resources/syslog", TRACE_TYPE_SYSLOG);
    }

    /**
     * Test 8.12: Run Profile "TestSpecificNonRecursive"
     */
    @Test
    public void test_8_12() {
        SWTBotView projectExplorerBot = fBot.viewByTitle(PROJECT_EXPLORER);
        projectExplorerBot.show();
        SWTBotTreeItem tracesFolderItem = getTracesFolderTreeItem(projectExplorerBot);

        tracesFolderItem.contextMenu(FETCH_COMMAND_NAME).click();
        SWTBotShell shell = fBot.shell(FETCH_SHELL_NAME).activate();
        fBot.comboBox().setSelection("TestSpecificNonRecursive");
        fBot.button("Next >").click();
        fBot.button("Finish").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        WaitUtils.waitForJobs();

        TmfProjectElement project = TmfProjectRegistry.getProject(ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME), true);
        fBot.waitUntil(new TraceCountCondition(project, 1));
        final TmfTraceFolder tracesFolder = project.getTracesFolder();
        assertNotNull(tracesFolder);
        List<TmfTraceElement> traces = tracesFolder.getTraces();
        assertEquals(1, traces.size());
        testTrace(traces.get(0), CONNECTION_NODE1_NAME + "/resources/syslog", TRACE_TYPE_SYSLOG);
    }

    /**
     * Test 8.13: Run Profile "TestSpecificMultiGroupRecursive"
     */
    @Test
    public void test_8_13() {
        SWTBotView projectExplorerBot = fBot.viewByTitle(PROJECT_EXPLORER);
        projectExplorerBot.show();
        SWTBotTreeItem tracesFolderItem = getTracesFolderTreeItem(projectExplorerBot);

        tracesFolderItem.contextMenu(FETCH_COMMAND_NAME).click();
        SWTBotShell shell = fBot.shell(FETCH_SHELL_NAME).activate();
        fBot.comboBox().setSelection("TestSpecificMultiGroupRecursive");
        fBot.button("Next >").click();
        fBot.button("Finish").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        WaitUtils.waitForJobs();

        TmfProjectElement project = TmfProjectRegistry.getProject(ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME), true);
        fBot.waitUntil(new TraceCountCondition(project, 2));
        final TmfTraceFolder tracesFolder = project.getTracesFolder();
        assertNotNull(tracesFolder);
        List<TmfTraceElement> traces = tracesFolder.getTraces();
        assertEquals(2, traces.size());
        testTrace(traces.get(0), CONNECTION_NODE1_NAME + "/resources/generated/synthetic-trace", TRACE_TYPE_KERNEL);
        testTrace(traces.get(1), CONNECTION_NODE1_NAME + "/resources/syslog", TRACE_TYPE_SYSLOG);
    }

    /**
     * Test 8.14: Cancel Import
     */
    @Test
    public void test_8_14() {
        SWTBotView projectExplorerBot = fBot.viewByTitle(PROJECT_EXPLORER);
        projectExplorerBot.show();
        SWTBotTreeItem tracesFolderItem = getTracesFolderTreeItem(projectExplorerBot);

        tracesFolderItem.contextMenu(FETCH_COMMAND_NAME).click();
        SWTBotShell shell = fBot.shell(FETCH_SHELL_NAME).activate();
        fBot.comboBox().setSelection("TestAllRecursive");
        fBot.button("Next >").click();
        fBot.button("Finish").click();
        fBot.button("Cancel").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        WaitUtils.waitForJobs();

        /* Can't verify cancelled import, it depends on timing */
    }

    /**
     * Test 8.15: Run Profile "TestMultiNodse"
     */
    @Test
    public void test_8_15() {
        SWTBotView projectExplorerBot = fBot.viewByTitle(PROJECT_EXPLORER);
        projectExplorerBot.show();
        SWTBotTreeItem tracesFolderItem = getTracesFolderTreeItem(projectExplorerBot);

        tracesFolderItem.contextMenu(FETCH_COMMAND_NAME).click();
        SWTBotShell shell = fBot.shell(FETCH_SHELL_NAME).activate();
        fBot.comboBox().setSelection("TestMultiNode");
        fBot.button("Next >").click();
        fBot.button("Finish").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        WaitUtils.waitForJobs();

        TmfProjectElement project = TmfProjectRegistry.getProject(ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME), true);
        fBot.waitUntil(new TraceCountCondition(project, 2));
        final TmfTraceFolder tracesFolder = project.getTracesFolder();
        assertNotNull(tracesFolder);
        List<TmfTraceElement> traces = tracesFolder.getTraces();
        assertEquals(2, traces.size());
        testTrace(traces.get(0), CONNECTION_NODE1_NAME + "/resources/generated/synthetic-trace", TRACE_TYPE_KERNEL);
        testTrace(traces.get(1), CONNECTION_NODE2_NAME + "/resources/syslog", TRACE_TYPE_SYSLOG);
    }

    /**
     * Test 9.1: Cannot connect to remote host (node doesn't exist)
     */
    @Test
    public void test_9_01() {
        SWTBotView projectExplorerBot = fBot.viewByTitle(PROJECT_EXPLORER);
        projectExplorerBot.show();
        SWTBotTreeItem tracesFolderItem = getTracesFolderTreeItem(projectExplorerBot);

        tracesFolderItem.contextMenu(FETCH_COMMAND_NAME).click();
        SWTBotShell shell = fBot.shell(FETCH_SHELL_NAME).activate();
        fBot.comboBox().setSelection("TestUnknown");
        fBot.button("Finish").click();
        /* ErrorDialog is inhibited by the platform when running tests */
        fBot.button("Cancel").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        WaitUtils.waitForJobs();
    }

    /**
     * Test 9.2: Cannot connect to remote host (wrong password)
     *
     * This test is ignored to avoid repeated authentication failures in the CI
     * infrastructure. The @Ignore tag can be removed to run the test locally.
     */
    @Test
    @Ignore
    public void test_9_02() {
        SWTBotView projectExplorerBot = fBot.viewByTitle(PROJECT_EXPLORER);
        projectExplorerBot.show();
        SWTBotTreeItem tracesFolderItem = getTracesFolderTreeItem(projectExplorerBot);

        tracesFolderItem.contextMenu(FETCH_COMMAND_NAME).click();
        SWTBotShell shell = fBot.shell(FETCH_SHELL_NAME).activate();
        fBot.comboBox().setSelection("TestLocal");
        fBot.button("Finish").click();
        SWTBotShell anyShell = SWTBotUtils.anyShellOf(fBot, AUTHENTICATION_SHELL_NAME, PASSWORD_SHELL_NAME).activate();
        if (anyShell.getText().equals(AUTHENTICATION_SHELL_NAME)) {
            fBot.button("Yes").click();
        }
        fBot.shell(PASSWORD_SHELL_NAME).activate();
        fBot.textWithLabel("Password:").setText("anonymous");
        fBot.button("OK").click();
        fBot.shell(PASSWORD_SHELL_NAME).activate();
        fBot.button("Cancel").click();
        /* ErrorDialog is inhibited by the platform when running tests */
        fBot.button("Cancel").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        WaitUtils.waitForJobs();
    }

    /**
     * Test editing a profile
     */
    @Test
    public void testEditProfile() {
        openRemoteProfilePreferences();
        createProfile();
        openRemoteProfilePreferences();

        // The first tree is the preference "categories" on the left side, we
        // need to skip it
        SWTBotTree tree = fBot.tree(1);

        final String[] traceGroupNodePath = new String[] { PROFILE_NAME, CONNECTION_NODE_TEXT, TRACE_GROUP_NODE_TEXT };

        // Initial order of traces
        SWTBotTreeItem traceGroupNode = getTreeItem(fBot, tree, traceGroupNodePath);
        SWTBotTreeItem[] traceNodes = traceGroupNode.getItems();
        assertEquals(3, traceNodes.length);
        assertEquals(LTTNG_TRACE_FILE_PATTERN, traceNodes[0].getText());
        assertEquals(SYSLOG_FILE_PATTERN, traceNodes[1].getText());
        assertEquals(WILDCARD_PATTERN, traceNodes[2].getText());

        // Test moving down a trace element
        SWTBotTreeItem traceNode = traceGroupNode.getNode(LTTNG_TRACE_FILE_PATTERN);
        traceNode.select();
        fBot.button("Move Down").click();
        traceGroupNode = getTreeItem(fBot, tree, traceGroupNodePath);
        traceNodes = traceGroupNode.getItems();
        assertEquals(3, traceNodes.length);
        assertEquals(SYSLOG_FILE_PATTERN, traceNodes[0].getText());
        assertEquals(LTTNG_TRACE_FILE_PATTERN, traceNodes[1].getText());
        assertEquals(WILDCARD_PATTERN, traceNodes[2].getText());

        // Test moving up a trace element
        traceNode = traceGroupNode.getNode(LTTNG_TRACE_FILE_PATTERN);
        traceNode.select();
        fBot.button("Move Up").click();
        traceGroupNode = getTreeItem(fBot, tree, traceGroupNodePath);
        traceNodes = traceGroupNode.getItems();
        assertEquals(3, traceNodes.length);
        assertEquals(LTTNG_TRACE_FILE_PATTERN, traceNodes[0].getText());
        assertEquals(SYSLOG_FILE_PATTERN, traceNodes[1].getText());
        assertEquals(WILDCARD_PATTERN, traceNodes[2].getText());

        // Test Copy/Paste
        traceNode = traceGroupNode.getNode(LTTNG_TRACE_FILE_PATTERN);
        traceNode.select().contextMenu("Copy").click();
        traceNode.contextMenu("Paste").click();
        traceNodes = traceGroupNode.getItems();
        assertEquals(4, traceNodes.length);
        assertEquals(LTTNG_TRACE_FILE_PATTERN, traceNodes[0].getText());
        assertEquals(LTTNG_TRACE_FILE_PATTERN, traceNodes[1].getText());
        assertEquals(SYSLOG_FILE_PATTERN, traceNodes[2].getText());
        assertEquals(WILDCARD_PATTERN, traceNodes[3].getText());

        // Test Cut/Paste
        traceNode = traceGroupNode.getNode(LTTNG_TRACE_FILE_PATTERN);
        traceNode.select().contextMenu("Cut").click();
        traceNode = traceGroupNode.getNode(SYSLOG_FILE_PATTERN);
        traceNode.select().contextMenu("Paste").click();
        traceNodes = traceGroupNode.getItems();
        assertEquals(4, traceNodes.length);
        assertEquals(LTTNG_TRACE_FILE_PATTERN, traceNodes[0].getText());
        assertEquals(SYSLOG_FILE_PATTERN, traceNodes[1].getText());
        assertEquals(LTTNG_TRACE_FILE_PATTERN, traceNodes[2].getText());
        assertEquals(WILDCARD_PATTERN, traceNodes[3].getText());

        // Test Delete
        traceNode = traceGroupNode.getNode(LTTNG_TRACE_FILE_PATTERN);
        traceNode.select().contextMenu("Delete").click();
        traceNodes = traceGroupNode.getItems();
        assertEquals(3, traceNodes.length);
        assertEquals(SYSLOG_FILE_PATTERN, traceNodes[0].getText());
        assertEquals(LTTNG_TRACE_FILE_PATTERN, traceNodes[1].getText());
        assertEquals(WILDCARD_PATTERN, traceNodes[2].getText());
        // Copy to test Paste after Delete
        traceNode = traceGroupNode.getNode(LTTNG_TRACE_FILE_PATTERN);
        traceNode.select().contextMenu("Copy").click();
        traceNode = traceGroupNode.select(SYSLOG_FILE_PATTERN, LTTNG_TRACE_FILE_PATTERN, WILDCARD_PATTERN);
        traceNode.pressShortcut(Keystrokes.DELETE);
        traceNodes = traceGroupNode.getItems();
        assertEquals(0, traceNodes.length);
        // Paste after Delete
        traceGroupNode.contextMenu("Paste").click();
        traceNodes = traceGroupNode.getItems();
        assertEquals(1, traceNodes.length);
        assertEquals(LTTNG_TRACE_FILE_PATTERN, traceNodes[0].getText());
        SWTBotUtils.pressOKishButtonInPreferences(fBot);
        deleteProfile();
    }

    private static void testImport(Runnable selectionFunctor, Runnable verifyTracesFunctor) {
        SWTBotView projectExplorerBot = fBot.viewByTitle(PROJECT_EXPLORER);
        assertNotNull("Cannot find " + PROJECT_EXPLORER, projectExplorerBot);
        projectExplorerBot.show();
        SWTBotTreeItem treeItem = getTracesFolderTreeItem(projectExplorerBot);

        treeItem.contextMenu(FETCH_COMMAND_NAME).click();
        SWTBotShell shell = fBot.shell(FETCH_SHELL_NAME).activate();

        fBot.button("Manage Profiles").click();
        fBot.shell("Preferences (Filtered)").activate();

        createProfile();

        assertEquals(PROFILE_NAME, fBot.comboBoxWithLabel("Profile name:").getText());
        assertEquals(CONNECTION_NODE_TEXT, fBot.textWithLabel("Nodes:").getText());

        // Make sure if we go to the next page and come back that the first page
        // still has valid values
        fBot.button("Next >").click();
        fBot.button("< Back").click();
        assertEquals(PROFILE_NAME, fBot.comboBoxWithLabel("Profile name:").getText());
        assertEquals(CONNECTION_NODE_TEXT, fBot.textWithLabel("Nodes:").getText());

        fBot.button("Next >").click();

        selectionFunctor.run();

        fBot.button("Finish").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        WaitUtils.waitForJobs();

        verifyTracesFunctor.run();

        deleteProfile();
    }

    private static void createProfile() {
        fBot.button("Add").click();

        // The first tree is the preference "categories" on the left side, we
        // need to skip it
        SWTBotTree tree = fBot.tree(1);

        SWTBotTreeItem treeNode = getTreeItem(fBot, tree, PROFILE_NAME, "name (ssh://userinfo@host:22)");
        treeNode.select();
        SWTBotText uriLabel = fBot.textWithLabel("URI:");
        uriLabel.setText("file://");
        SWTBotText nodeNameLabel = fBot.textWithLabel("Node name:");
        nodeNameLabel.setText(CONNECTION_NODE1_NAME);

        SWTBotTreeItem traceRootNode = treeNode.getNode("/rootpath");
        traceRootNode.select();
        SWTBotText pathLabel = fBot.textWithLabel("Root path:");
        pathLabel.setText(TRACE_LOCATION);
        fBot.checkBox("Recursive").select();

        // Add the ctf file pattern
        treeNode = traceRootNode.getNode(".*");
        treeNode.select();
        SWTBotText filePatternLabel = fBot.textWithLabel("File pattern:");
        filePatternLabel.setText(LTTNG_TRACE_FILE_PATTERN);

        // Add the syslog file pattern
        traceRootNode.contextMenu("New Trace").click();
        treeNode = traceRootNode.getNode(".*");
        treeNode.select();
        filePatternLabel = fBot.textWithLabel("File pattern:");
        filePatternLabel.setText(SYSLOG_FILE_PATTERN);
        SWTBotCombo combo = fBot.comboBoxWithLabel("Trace type:");
        combo.setSelection("Test trace : Test Syslog");

        // Add the wildcard file pattern
        traceRootNode.contextMenu("New Trace").click();

        SWTBotUtils.pressOKishButtonInPreferences(fBot);
    }

    private static void testTrace(TmfTraceElement tmfTraceElement, String expectedTracePath, String traceType) {
        assertEquals(traceType, tmfTraceElement.getTraceType());
        IPath tracePath = new Path(tmfTraceElement.getElementPath());
        assertEquals(expectedTracePath, tracePath.toString());
        if (traceType != null) {
            SWTBotUtils.openEditor(fBot, PROJECT_NAME, tracePath);
        }
    }

    private static void deleteProfile() {
        openRemoteProfilePreferences();

        // The second tree is the remote profiles tree on the right side
        SWTBotTree tree = fBot.tree(1);
        SWTBotTreeItem treeNode = tree.getTreeItem(PROFILE_NAME);
        treeNode.select();
        fBot.button("Remove").click();
        SWTBotUtils.pressOKishButtonInPreferences(fBot);
    }

    private static void importProfiles() {
        openRemoteProfilePreferences();
        TmfFileDialogFactory.setOverrideFiles(PROFILES_LOCATION);
        fBot.button("Import").click();

        // Change the root path of every profile
        SWTBotTree tree = fBot.tree(1);
        for (SWTBotTreeItem profile : tree.getAllItems()) {
            for (SWTBotTreeItem node : profile.getItems()) {
                for (SWTBotTreeItem traceGroup : node.getItems()) {
                    traceGroup.select();
                    fBot.textWithLabel("Root path:").setText(TRACE_LOCATION);
                }
            }
        }
        SWTBotUtils.pressOKishButtonInPreferences(fBot);
    }

    private static void clearProfiles() {
        openRemoteProfilePreferences();
        SWTBotTree tree = fBot.tree(1);
        for (SWTBotTreeItem profile : tree.getAllItems()) {
            profile.select();
            fBot.button("Remove").click();
        }
        SWTBotUtils.pressOKishButtonInPreferences(fBot);
    }

    private static SWTBotTreeItem getTreeItem(SWTWorkbenchBot bot, SWTBotTree tree, String... nodeNames) {
        if (nodeNames.length == 0) {
            return null;
        }

        SWTBotTreeItem currentNode = tree.getTreeItem(nodeNames[0]);
        for (int i = 1; i < nodeNames.length; i++) {
            String nodeName = nodeNames[i];
            bot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable(nodeName, currentNode));
            SWTBotTreeItem newNode = currentNode.getNode(nodeName);
            currentNode = newNode;
        }

        return currentNode;
    }

    private static void openRemoteProfilePreferences() {
        SWTBotShell preferencesShell = SWTBotUtils.openPreferences(fBot);

        // The first tree is the preference "categories" on the left side
        SWTBot bot = preferencesShell.bot();
        SWTBotTree tree = bot.tree(0);
        SWTBotTreeItem treeNode = tree.getTreeItem("Tracing");
        treeNode.select();
        treeNode.expand();
        bot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable("Remote Profiles", treeNode));
        treeNode = treeNode.getNode("Remote Profiles");
        treeNode.select();
    }

    private static SWTBotTreeItem getTracesFolderTreeItem(SWTBotView projectExplorerBot) {
        SWTBotTreeItem treeItem = projectExplorerBot.bot().tree().getTreeItem(PROJECT_NAME);
        treeItem.select();
        treeItem.expand();
        return treeItem.getNode("Traces [0]");
    }

}
