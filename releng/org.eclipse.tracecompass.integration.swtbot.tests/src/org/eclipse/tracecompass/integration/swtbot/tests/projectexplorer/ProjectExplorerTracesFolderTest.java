/******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.integration.swtbot.tests.projectexplorer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.ImportConfirmation;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.ImportTraceWizardPage;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ctf.ui.swtbot.tests.SWTBotImportWizardUtils;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.ui.IPageLayout;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * SWTBot test for testing Project Explorer trace folders (context-menu,
 * import, etc).
 */
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SuppressWarnings({"restriction", "javadoc"})
public class ProjectExplorerTracesFolderTest {

    private static final String PROP_LAST_MODIFIED_PROPERTY = "last modified";
    private static final String TEXT_EDITOR_ID = "org.eclipse.ui.DefaultTextEditor";

    private static final String GENERIC_CTF_TRACE_TYPE = "Common Trace Format : Generic CTF Trace";
    private static final String LTTNG_KERNEL_TRACE_TYPE = "Common Trace Format : Linux Kernel Trace";
    private static final String LTTNG_UST_TRACE_TYPE = "Common Trace Format : LTTng UST Trace";
    private static final String CUSTOM_TEXT_TRACE_TYPE = "Custom Text : TmfGeneric";
    private static final String CUSTOM_XML_TRACE_TYPE = "Custom XML : Custom XML Log";

    private static final @NonNull TestTraceInfo CUSTOM_TEXT_LOG = new TestTraceInfo("ExampleCustomTxt.log", CUSTOM_TEXT_TRACE_TYPE, 10, "29:52.034");
    private static final @NonNull TestTraceInfo CUSTOM_XML_LOG = new TestTraceInfo("ExampleCustomXml.xml", CUSTOM_XML_TRACE_TYPE, 6, "22:01:20");
    private static final @NonNull TestTraceInfo LTTNG_KERNEL_TRACE = new TestTraceInfo("kernel-overlap-testing", LTTNG_KERNEL_TRACE_TYPE, 1000, "04:32.650 993 664");
    private static final @NonNull TestTraceInfo SIMPLE_SERVER1_UST_TRACE = new TestTraceInfo("simple_server-thread1", LTTNG_UST_TRACE_TYPE, 1000, "04:32.650 993 664");
    private static final @NonNull TestTraceInfo SIMPLE_SERVER2_UST_TRACE = new TestTraceInfo("simple_server-thread2", LTTNG_UST_TRACE_TYPE, 1000, "04:32.650 993 664");
    private static final @NonNull TestTraceInfo UST_OVERLAP_TESTING_UST_TRACE = new TestTraceInfo("ust-overlap-testing", LTTNG_UST_TRACE_TYPE, 1000, "04:32.650 993 664");

    private static final String CLASHES_DIR_NAME = "z-clashes";
    private static final @NonNull TestTraceInfo CLASHES_CUSTOM_TEXT_LOG = new TestTraceInfo("ExampleCustomTxt.log", CLASHES_DIR_NAME + "/ExampleCustomTxt.log", CUSTOM_TEXT_TRACE_TYPE, 11, "29:52.034");
    private static final @NonNull TestTraceInfo CLASHES_CUSTOM_XML_LOG = new TestTraceInfo("ExampleCustomXml.xml", CLASHES_DIR_NAME + "/ExampleCustomXml.xml", CUSTOM_XML_TRACE_TYPE, 7, "22:01:20");
    private static final @NonNull TestTraceInfo CLASHES_LTTNG_KERNEL_TRACE = new TestTraceInfo("kernel-overlap-testing", CLASHES_DIR_NAME + "/kernel-overlap-testing", LTTNG_KERNEL_TRACE_TYPE, 1001, "04:32.650 993 664");
    private static final @NonNull TestTraceInfo CLASHES_SIMPLE_SERVER1_UST_TRACE = new TestTraceInfo("simple_server-thread1", CLASHES_DIR_NAME + "/simple_server-thread1", LTTNG_UST_TRACE_TYPE, 1001, "04:32.650 993 664");
    private static final @NonNull TestTraceInfo CLASHES_SIMPLE_SERVER2_UST_TRACE = new TestTraceInfo("simple_server-thread2", CLASHES_DIR_NAME + "/simple_server-thread2", LTTNG_UST_TRACE_TYPE, 1001, "04:32.650 993 664");
    private static final @NonNull TestTraceInfo CLASHES_UST_OVERLAP_TESTING_UST_TRACE = new TestTraceInfo("ust-overlap-testing", CLASHES_DIR_NAME + "/ust-overlap-testing", LTTNG_UST_TRACE_TYPE, 1001, "04:32.650 993 664");


    private static final @NonNull TestTraceInfo LTTNG_KERNEL_TRACE_METADATA = new TestTraceInfo(LTTNG_KERNEL_TRACE.getTraceName(), LTTNG_KERNEL_TRACE.getTraceName() + "/metadata", LTTNG_KERNEL_TRACE.getTraceType(), LTTNG_KERNEL_TRACE.getNbEvents(),
            LTTNG_KERNEL_TRACE.getFirstEventTimestamp());
    private static final @NonNull TestTraceInfo UNRECOGNIZED_LOG = new TestTraceInfo("unrecognized.log", "", 0, "");
    private static final @NonNull TestTraceInfo CUSTOM_XML_LOG_AS_TEXT = new TestTraceInfo("ExampleCustomXml.xml", CUSTOM_TEXT_TRACE_TYPE, 0, "");

    private static final TestTraceInfo[] ALL_TRACEINFOS = new TestTraceInfo[] {
            CUSTOM_TEXT_LOG,
            CUSTOM_XML_LOG,
            LTTNG_KERNEL_TRACE,
            SIMPLE_SERVER1_UST_TRACE,
            SIMPLE_SERVER2_UST_TRACE,
            UST_OVERLAP_TESTING_UST_TRACE,

            CLASHES_CUSTOM_TEXT_LOG,
            CLASHES_CUSTOM_XML_LOG,
            CLASHES_LTTNG_KERNEL_TRACE,
            CLASHES_SIMPLE_SERVER1_UST_TRACE,
            CLASHES_SIMPLE_SERVER2_UST_TRACE,
            CLASHES_UST_OVERLAP_TESTING_UST_TRACE
    };

    private static final Set<TestTraceInfo> CLASHING_TRACEINFOS = ImmutableSet.of(
            CLASHES_CUSTOM_TEXT_LOG,
            CLASHES_CUSTOM_XML_LOG,
            CLASHES_LTTNG_KERNEL_TRACE,
            CLASHES_SIMPLE_SERVER1_UST_TRACE,
            CLASHES_SIMPLE_SERVER2_UST_TRACE,
            CLASHES_UST_OVERLAP_TESTING_UST_TRACE);

    // All normal traces plus the unrecognized trace
    private static final int NUM_UNIQUE_TRACES = CLASHING_TRACEINFOS.size() + 1;


    private static final File TEST_TRACES_PATH = new File(new Path(TmfTraceManager.getTemporaryDirPath()).append("testtraces").toOSString());
    private static final String TRACE_PROJECT_NAME = "test";
    private static final String MANAGE_CUSTOM_PARSERS_SHELL_TITLE = "Manage Custom Parsers";

    private static SWTWorkbenchBot fBot;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    private static String getPath(String relativePath) {
        return new Path(TEST_TRACES_PATH.getAbsolutePath()).append(relativePath).toOSString();
    }

    /**
     * Test Class setup
     *
     * @throws IOException
     */
    @BeforeClass
    public static void init() throws IOException {
        TestDirectoryStructureUtil.generateTraceStructure(TEST_TRACES_PATH);

        SWTBotUtils.initialize();

        /* Set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        fLogger.removeAllAppenders();
        fLogger.addAppender(new NullAppender());
        fBot = new SWTWorkbenchBot();

        SWTBotUtils.closeView("Welcome", fBot);

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

    private static void test3_01Preparation() {
        // FIXME: We can't use Manage Custom Parsers > Import because it uses a native dialog. We'll still check that they show up in the dialog
        CustomTxtTraceDefinition[] txtDefinitions = CustomTxtTraceDefinition.loadAll(getPath("customParsers/ExampleCustomTxtParser.xml"));
        txtDefinitions[0].save();
        CustomXmlTraceDefinition[] xmlDefinitions = CustomXmlTraceDefinition.loadAll(getPath("customParsers/ExampleCustomXmlParser.xml"));
        xmlDefinitions[0].save();

        SWTBotTreeItem traceFolder = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);
        traceFolder.contextMenu("Manage Custom Parsers...").click();
        fBot.waitUntil(Conditions.shellIsActive(MANAGE_CUSTOM_PARSERS_SHELL_TITLE));
        SWTBotShell shell = fBot.shell(MANAGE_CUSTOM_PARSERS_SHELL_TITLE);
        SWTBot shellBot = shell.bot();

        // Make sure the custom text trace type is imported
        shellBot.list().select(CUSTOM_TEXT_LOG.getTraceType());

        // Make sure the custom xml trace type is imported
        shellBot.radio("XML").click();
        shellBot.list().select(CUSTOM_XML_LOG.getTraceType());
        shellBot.button("Close").click();
        shellBot.waitUntil(Conditions.shellCloses(shell));
    }

    /**
     * Test that the expected context menu items are there
     * <p>
     * Action : Trace Folder menu
     * <p>
     * Procedure :Select the Traces folder and open its context menu
     * <p>
     * Expected Results: Correct menu opens (Import, Refresh, etc)
     */
    @Test
    public void test3_01ContextMenuPresence() {
        test3_01Preparation();

        SWTBotTreeItem traceItem = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);

        final List<String> EXPECTED_MENU_LABELS = ImmutableList.of(
                "Open Trace...",
                "",
                "Import...",
                "",
                "New Folder...",
                "Clear",
                "",
                "Import Trace Package...",
                "Fetch Remote Traces...",
                "",
                "Export Trace Package...",
                "",
                "Manage Custom Parsers...",
                "Manage XML analyses...",
                "",
                "Apply Time Offset...",
                "Clear Time Offset",
                "",
                "Refresh");

        List<String> menuLabels = traceItem.contextMenu().menuItems();
        for (int i = 0; i < menuLabels.size(); i++) {
            assertEquals(EXPECTED_MENU_LABELS.get(i), menuLabels.get(i));
        }

        fBot.closeAllEditors();
    }

    /**
     * Test that the trace import wizard appears
     * <p>
     * Action : Trace Import Wizard
     * <p>
     * Procedure : Select Import
     * <p>
     * Expected Results: Trace Import Wizard appears
     */
    @Test
    public void test3_02Import() {
        SWTBotTreeItem traceItem = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);

        SWTBotShell shell = openTraceFoldersImport(traceItem);
        shell.bot().button("Cancel").click();
    }

    /**
     * Test that the trace import wizard can import a single custom text trace
     * <p>
     * Action : Import single custom text trace (link to workspace)
     * <p>
     * <pre>
     * Procedure : 1) Browse to directory ${local}/traces/import/
     *             2) Select trace ExampleCustomTxt.log
     *             3) Keep <Auto Detection>, Select "Import unrecognized traces", unselect  "Overwrite existing without warning" and select "Create Links to workspace" and
     *             4) press Finish
     * </pre>
     * <p>
     * Expected Results: Imported trace appear in Traces Folder and the Trace Type Tmf Generic is set. Make sure trace can be opened
     */
    @Test
    public void test3_03SingleCustomTextTrace() {
        int optionFlags = ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES | ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE;
        testSingleTrace(CUSTOM_TEXT_LOG, optionFlags);
    }

    /**
     * <p>
     * Action : Import Single custom XML trace (link to workspace)
     * <p>
     *
     * <pre>
     * Procedure : redo 3.1-3.3 but this time select ExampleCustomXml.xml
     * </pre>
     * <p>
     * Expected Results: Imported trace appear in Traces Folder and the Trace
     * Type "Custom XML log" is set. Make sure that trace can be opened
     */
    @Test
    public void test3_04SingleCustomXmlTrace() {
        int optionFlags = ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES | ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE;
        testSingleTrace(CUSTOM_XML_LOG, optionFlags);
    }

    /**
     * <p>
     * Action : Import LTTng Kernel CTF trace (link to workspace)
     * <p>
     *
     * <pre>
     * Procedure : redo 3.1-3.3 but this time select directory kernel-overlap-testing/
     * </pre>
     * <p>
     * Expected Results: Imported trace appear in Traces Folder and the Trace
     * Type "LTTng Kernel" is set. Make sure that trace can be opened
     */
    @Test
    public void test3_05SingleCtfTrace() {
        int optionFlags = ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES | ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE;
        testSingleTrace(LTTNG_KERNEL_TRACE, optionFlags);
    }

    /**
     * <p>
     * Action : Rename + copy import
     * <p>
     *
     * <pre>
     * Procedure : 1) redo 3.3, 3.4, 3.5. However, Unselect "Create Links to workspace"
     *             2) When dialog box appear select Rename
     * </pre>
     * <p>
     * Expected Results: Traces are imported with new name that has a suffix (2)
     * at the end. Make sure that imported traces are copied to the project.
     */
    @Test
    public void test3_06RenameCopyImport() {
        testRenameCopyImport(CUSTOM_TEXT_LOG);
        testRenameCopyImport(CUSTOM_XML_LOG);
        testRenameCopyImport(LTTNG_KERNEL_TRACE);
    }

    private static void testRenameCopyImport(TestTraceInfo traceInfo) {
        int optionFlags = ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES;
        importTrace(optionFlags, ImportConfirmation.RENAME, traceInfo.getTraceName());
        String renamed = toRenamedName(traceInfo.getTraceName());
        verifyTrace(traceInfo, optionFlags, renamed);
    }

    /**
     * <p>
     * Action : Overwrite + copy import
     * <p>
     *
     * <pre>
     * Procedure : 1) redo 3.3, 3.4, 3.5. However, Unselect "Create Links to workspace"
     *             2) When dialog box appear select Overwrite
     * </pre>
     * <p>
     * Expected Results: Existing traces are deleted and new traces are
     * imported. Make sure that imported traces are copied to the project and
     * can be opened
     */
    @Test
    public void test3_07OverwriteCopyImport() {
        testOverwriteCopyImport(CUSTOM_TEXT_LOG);
        testOverwriteCopyImport(CUSTOM_XML_LOG);
        testOverwriteCopyImport(LTTNG_KERNEL_TRACE);
    }

    private static void testOverwriteCopyImport(TestTraceInfo traceInfo) {
        String traceName = traceInfo.getTraceName();
        SWTBotTreeItem traceItem = SWTBotUtils.getTreeItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), traceName);
        String lastModified = getTraceProperty(traceItem, PROP_LAST_MODIFIED_PROPERTY);
        int optionFlags = ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES;
        importTrace(optionFlags, ImportConfirmation.OVERWRITE, traceName);
        verifyTrace(traceInfo, optionFlags);

        assertNotEquals(lastModified, getTraceProperty(traceItem, PROP_LAST_MODIFIED_PROPERTY));
    }

    /**
     * <p>
     * Action : Skip
     * <p>
     *
     * <pre>
     * Procedure : 1) redo 3.3, 3.4, 3.5. However, Unselect "Create Links to workspace"
     *             2) When dialog box appear select Skip
     * </pre>
     * <p>
     * Expected Results: Make sure that no new trace is imported
     */
    @Test
    public void test3_08SkipImport() {
        testSkipImport(CUSTOM_TEXT_LOG);
        testSkipImport(CUSTOM_XML_LOG);
        testSkipImport(LTTNG_KERNEL_TRACE);
    }

    private static void testSkipImport(TestTraceInfo traceInfo) {
        String traceName = traceInfo.getTraceName();
        SWTBotTreeItem traceItem = SWTBotUtils.getTreeItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), traceName);
        String lastModified = getTraceProperty(traceItem, PROP_LAST_MODIFIED_PROPERTY);
        int optionFlags = ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES;
        importTrace(optionFlags, ImportConfirmation.SKIP, traceName);
        verifyTrace(traceInfo, optionFlags);

        assertEquals(lastModified, getTraceProperty(traceItem, PROP_LAST_MODIFIED_PROPERTY));
    }

    /**
     * <p>
     * Action : Default overwrite
     * <p>
     *
     * <pre>
     * Procedure : 1) redo 3.3, 3.4, 3.5. However, Unselect "Create Links to workspace" and select "Overwrite existing without warning"
     * </pre>
     * <p>
     * Expected Results: Make sure that no dialog box appears (for renaming,
     * overwriting, skipping) and existing traces are overwritten). Make sure
     * trace can be opened
     */
    @Test
    public void test3_09OverwriteOptionImport() {
        testOverwriteOptionImport(CUSTOM_TEXT_LOG);
        testOverwriteOptionImport(CUSTOM_XML_LOG);
        testOverwriteOptionImport(LTTNG_KERNEL_TRACE);
    }

    private static void testOverwriteOptionImport(TestTraceInfo traceInfo) {
        String traceName = traceInfo.getTraceName();
        SWTBotTreeItem traceItem = SWTBotUtils.getTreeItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), traceName);
        String lastModified = getTraceProperty(traceItem, PROP_LAST_MODIFIED_PROPERTY);

        int optionFlags = ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES | ImportTraceWizardPage.OPTION_OVERWRITE_EXISTING_RESOURCES;
        importTrace(optionFlags, ImportConfirmation.CONTINUE, traceName);
        verifyTrace(traceInfo, optionFlags);

        assertNotEquals(lastModified, getTraceProperty(traceItem, PROP_LAST_MODIFIED_PROPERTY));
    }

    /**
     * <p>
     * Action : Import unrecognized
     * <p>
     *
     * <pre>
     * Procedure : 1) Open Import wizard (see 3.1-3.2)
     *             2) Browse to directory ${local}/traces/import
     *             3) Select trace unrecognized.log
     *             4) Keep <Auto Detection>, Select "Import unrecognized traces", unselect  "Overwrite existing without warning" and select "Create Links to workspace" and
     *             5) press Finish
     * </pre>
     * <p>
     * Expected Results: unrecognized.log is imported with trace type unknown.
     * The default text file icon is displayed. The trace, when opened, is
     * displayed in the text editor.
     */
    @Test
    public void test3_10ImportUnrecognized() {
        String traceName = UNRECOGNIZED_LOG.getTraceName();
        int optionFlags = ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES | ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE;
        importTrace(optionFlags, traceName);
        verifyTrace(UNRECOGNIZED_LOG, optionFlags);
    }

    /**
     * <p>
     * Action : Import unrecognized (ignore)
     * <p>
     *
     * <pre>
     * Procedure : 1) redo 3.10, however unselect "Import unrecognized traces"
     * </pre>
     * <p>
     * Expected Results: unrecognized.log is not imported
     */
    @Test
    public void test3_11ImportUnrecognizedIgnore() {
        String traceName = UNRECOGNIZED_LOG.getTraceName();
        SWTBotTreeItem tracesFolderItem = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);
        int numTraces = tracesFolderItem.getItems().length;

        SWTBotTreeItem traceItem = SWTBotUtils.getTreeItem(fBot, tracesFolderItem, traceName);
        String lastModified = getTraceProperty(traceItem, PROP_LAST_MODIFIED_PROPERTY);

        int optionFlags = ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE;
        importTrace(optionFlags, traceName);
        verifyTrace(UNRECOGNIZED_LOG, optionFlags);

        assertEquals(lastModified, getTraceProperty(traceItem, PROP_LAST_MODIFIED_PROPERTY));
        assertEquals(numTraces, tracesFolderItem.getItems().length);
    }

    /**
     * <p>
     * Action : Import CTF trace by selection metadata file only
     * <p>
     *
     * <pre>
     * Procedure : 1) Redo 3.5, However only select metadata file instead of directory trace
     * </pre>
     * <p>
     * Expected Results: Imported trace appear in Traces Folder and the Trace
     * Type "LTTng Kernel" is set. Make sure that trace can be opened
     */
    @Test
    public void test3_12ImportCtfWithMetadataSelection() {
        SWTBotUtils.clearTracesFolderUI(fBot, TRACE_PROJECT_NAME);
        testSingleTrace(LTTNG_KERNEL_TRACE_METADATA, ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES | ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE);
    }

    /**
     * <p>
     * Action : Recursive import with auto-detection (Rename All)
     * <p>
     *
     * <pre>
     * Procedure : 1) Open Import wizard (see 3.1-3.2)
     *             2) Browse to directory ${local}/traces/import
     *             3) select directory import
     *             4) Keep <Auto Detection>, Select "Import unrecognized traces", unselect  "Overwrite existing without warning", select "Create Links to workspace" and unselect "Preserve Folder Structure"
     *             5) press Finish
     *             6) When dialog appears select "Rename All"
     * </pre>
     * <p>
     * Expected Results: All Traces are imported with respective trace type set.
     * Traces with name clashes are imported with suffix (2). 1 trace
     * (unrecognized.log) is imported with trace type unknown. Make sure that
     * traces can be opened which have a trace type set. The unknown trace type
     * should open with the text editor.
     */
    @Test
    public void test3_13ImportRecursiveAutoRenameAll() {
        SWTBotUtils.clearTracesFolderUI(fBot, TRACE_PROJECT_NAME);

        int optionFlags = ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES | ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE;
        importTrace(optionFlags, ImportConfirmation.RENAME_ALL, "");

        for (TestTraceInfo info : ALL_TRACEINFOS) {
            String traceName = info.getTraceName();
            if (CLASHING_TRACEINFOS.contains(info)) {
                traceName = toRenamedName(traceName);
            }
            verifyTrace(info, optionFlags, traceName);
        }

        // Also check unrecognized file
        verifyTrace(UNRECOGNIZED_LOG, optionFlags);
    }

    /**
     * <p>
     * Action : Recursive import with auto-detection (Overwrite All)
     * <p>
     *
     * <pre>
     * Procedure : 1) Open Import wizard (see 3.1-3.2)
     *             2) Browse to directory ${local}/traces/import/
     *             3) select directory import
     *             4) Keep <Auto Detection>, Select "Import unrecognized traces", unselect  "Overwrite existing without warning", select "Create Links to workspace" and unselect "Preserve Folder Structure"
     *             5) press Finish
     *             6) When dialog appears select Overwrite All"
     * </pre>
     * <p>
     * Expected Results: All Traces are imported with respective trace type set.
     * Traces with name clashes are overwritten . 1 trace (unrecognized.log) is
     * imported with trace type unknown. Make sure that traces can be opened
     * which have a trace type set. The unknown trace type should open with the
     * text editor.
     */
    @Test
    public void test3_14ImportRecursiveAutoOverwriteAll() {
        SWTBotUtils.clearTracesFolderUI(fBot, TRACE_PROJECT_NAME);

        int optionFlags = ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES | ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE;
        importTrace(optionFlags, ImportConfirmation.OVERWRITE_ALL, "");

        for (TestTraceInfo info : CLASHING_TRACEINFOS) {
            verifyTrace(info, optionFlags);
        }

        // All traces should have clashed/overwritten (plus the unrecognized trace)
        SWTBotTreeItem tracesFolderItem = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);
        assertEquals(NUM_UNIQUE_TRACES, tracesFolderItem.getItems().length);

        // Also check unrecognized file
        verifyTrace(UNRECOGNIZED_LOG, optionFlags);
    }

    /**
     * <p>
     * Action : Recursive import with auto-detection (Skip All)
     * <p>
     *
     * <pre>
     * Procedure : 1) Open Import wizard (see 3.1-3.2)
     *             2) Browse to directory ${local}/traces/import/
     *             3) select directory import
     *             4) Keep <Auto Detection>, Select "Import unrecognized traces", unselect  "Overwrite existing without warning" and select "Create Links to workspace" and uncheck "preserve folder structure"
     *             5) press Finish
     *             6) When dialog appears select Skip All"
     * </pre>
     * <p>
     * Expected Results: All Traces are imported with respective trace type set. Traces with name
     * clashes are not imported. 1 trace (unrecognized.log) is imported with
     * trace type unknown. The unknown trace type should open with the text
     * editor.
     */
    @Test
    public void test3_15ImportRecursiveAutoSkipAll() {
        SWTBotUtils.clearTracesFolderUI(fBot, TRACE_PROJECT_NAME);

        int optionFlags = ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES | ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE;
        importTrace(optionFlags, ImportConfirmation.SKIP_ALL, "");

        SWTBotTreeItem tracesFolderItem = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);
        for (TestTraceInfo info : ALL_TRACEINFOS) {
            if (!CLASHING_TRACEINFOS.contains(info)) {
                verifyTrace(info, optionFlags);
            }
        }

        // All traces should have skipped (plus the unrecognized trace)
        assertEquals(NUM_UNIQUE_TRACES, tracesFolderItem.getItems().length);

        // Also check unrecognized file
        verifyTrace(UNRECOGNIZED_LOG, optionFlags);
    }

    /**
     * <p>
     * Action : Recursive import with auto-detection (test rename, overwrite and
     * skip)
     * <p>
     *
     * <pre>
     * Procedure : 1) Open Import wizard (see 3.1-3.2)
     *             2) Browse to directory ${local}/traces/import/
     *             3) select directory import
     *             4) Keep <Auto Detection>, Select "Import unrecognized traces", unselect  "Overwrite existing without warning", select "Create Links to workspace" and unselect "Preserve Folder Structure"
     *             5) press Finish
     *             6) When dialog appears select "Rename"
     *             7) When dialog appears select "Overwrite"
     *             8) When dialog appears select "Skip"
     * </pre>
     * <p>
     * Expected Results: All Traces are imported with respective trace type set. Traces with name
     * clashes are either renamed, overwritten or skipped as per dialog action.
     * Make sure that traces can be opened which have trace type set. The
     * unknown trace type should open with the text editor.
     */
    @Test
    public void test3_16ImportRecursiveAutoRenameOverwriteSkip() {
        SWTBotUtils.clearTracesFolderUI(fBot, TRACE_PROJECT_NAME);

        Supplier<ImportConfirmation> confirmationSupplier = new Supplier<ImportConfirmation>() {
            final ImportConfirmation dialogConfirmationOrder[] = new ImportConfirmation[] { ImportConfirmation.RENAME, ImportConfirmation.OVERWRITE, ImportConfirmation.SKIP };
            int fRsponseNum = 0;

            @Override
            public ImportConfirmation get() {
                if (fRsponseNum >= dialogConfirmationOrder.length) {
                    return null;
                }

                ImportConfirmation confirmation = dialogConfirmationOrder[fRsponseNum];
                fRsponseNum++;
                return confirmation;
            }
        };
        int optionFlags = ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES | ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE;
        importTrace(optionFlags, confirmationSupplier, LTTNG_KERNEL_TRACE.getTracePath(), CLASHES_LTTNG_KERNEL_TRACE.getTracePath(), SIMPLE_SERVER1_UST_TRACE.getTracePath(), CLASHES_SIMPLE_SERVER1_UST_TRACE.getTracePath(),
                SIMPLE_SERVER2_UST_TRACE.getTracePath(), CLASHES_SIMPLE_SERVER2_UST_TRACE.getTracePath(), UNRECOGNIZED_LOG.getTracePath());

        verifyTrace(LTTNG_KERNEL_TRACE, optionFlags);

        // Renamed trace
        String renamed = toRenamedName(CLASHES_LTTNG_KERNEL_TRACE.getTraceName());
        verifyTrace(CLASHES_LTTNG_KERNEL_TRACE, optionFlags, renamed);

        // Overwritten trace
        verifyTrace(CLASHES_SIMPLE_SERVER1_UST_TRACE, optionFlags);

        // Skipped trace
        verifyTrace(SIMPLE_SERVER2_UST_TRACE, optionFlags);

        // Also check unrecognized file
        verifyTrace(UNRECOGNIZED_LOG, optionFlags);
    }

    /**
     * <p>
     * Action : Recursive import with specific trace type 1 (Skip All) skip)
     * <p>
     *
     * <pre>
     * Procedure : 1) Open Import wizard
     *             2) Browse to directory ${local}/traces/import/
     *             3) Select directory import
     *             4) Select trace type "Generic CTF Trace", unselect  "Overwrite existing without warning", select "Create Links to workspace" and unselect "Preserve Folder Structure"and
     *             5) press Finish
     *             6) When dialog appears select Skip All"
     * </pre>
     * <p>
     * Expected Results: After selecting trace type, verify that button "Import
     * unrecognized traces" is disabled. 4 CTF traces are imported with trace
     * type "Generic CTF Trace" . Make sure that these traces can be opened
     */
    @Test
    public void test3_17ImportRecursiveSpecityTraceTypeCTF() {
        SWTBotUtils.clearTracesFolderUI(fBot, TRACE_PROJECT_NAME);

        int optionFlags = ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE;
        importTrace(GENERIC_CTF_TRACE_TYPE, optionFlags, ImportConfirmation.SKIP_ALL, "");

        final TestTraceInfo[] CTF_TRACEINFOS = new TestTraceInfo[] {
                LTTNG_KERNEL_TRACE,
                SIMPLE_SERVER1_UST_TRACE,
                SIMPLE_SERVER2_UST_TRACE,
                UST_OVERLAP_TESTING_UST_TRACE
        };
        for (TestTraceInfo info : CTF_TRACEINFOS) {
            verifyTrace(info, optionFlags, info.getTraceName(), GENERIC_CTF_TRACE_TYPE);
        }

        SWTBotTreeItem tracesFolderItem = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);
        assertEquals(CTF_TRACEINFOS.length, tracesFolderItem.getItems().length);
    }

    /**
     * <p>
     * Action : Recursive import with specific trace type 2 (Skip All)
     * <p>
     *
     * <pre>
     * Procedure : 1) Open Import wizard (see 3.1-3.2)
     *             2) Browse to directory ${local}/traces/import/
     *             3) Select directory import
     *             4) Select trace type "LTTng Kernel Trace", unselect "Overwrite existing without warning", select "Create Links to workspace" and unselect "Preserve Folder Structure"
     *             5) Press Finish
     *             6) When dialog appears select Skip All"
     * </pre>
     * <p>
     * Expected Results: After selecting trace type, verify that button "Import
     * unrecognized traces" is disabled. One LTTng Kernel trace is imported with
     * trace type "LTTng Kernel Trace". Make sure that this trace can be opened.
     */
    @Test
    public void test3_18ImportRecursiveSpecityTraceTypeKernel() {
        SWTBotUtils.clearTracesFolderUI(fBot, TRACE_PROJECT_NAME);

        int optionFlags = ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE;
        importTrace(LTTNG_KERNEL_TRACE_TYPE, optionFlags, ImportConfirmation.SKIP_ALL, "");

        verifyTrace(LTTNG_KERNEL_TRACE, optionFlags);

        SWTBotTreeItem tracesFolderItem = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);
        assertEquals(1, tracesFolderItem.getItems().length);
    }

    /**
     * <p>
     * Action : Recursive import with specific trace type 3 (Skip All)
     * <p>
     *
     * <pre>
     * Procedure : 1) Open Import wizard
     *             2) Browse to directory ${local}/traces/import/
     *             3) Select directory import
     *             4) Select trace type "LTTng UST Trace", unselect "Overwrite existing without warning", select "Create Links to workspace" and unselect "Preserve Folder Structure"
     *             5) Press Finish
     *             6) When dialog appears select Skip All"
     * </pre>
     * <p>
     * Expected Results: After selecting trace type, verify that button "Import
     * unrecognized traces" is disabled. 3 LTTng UST traces are imported with
     * trace type "LTTng UST Trace". Make sure that these traces can be opened.
     */
    @Test
    public void test3_19ImportRecursiveSpecityTraceTypeUST() {
        SWTBotUtils.clearTracesFolderUI(fBot, TRACE_PROJECT_NAME);

        int optionFlags = ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE;
        importTrace(LTTNG_UST_TRACE_TYPE, optionFlags, ImportConfirmation.SKIP_ALL, "");

        final TestTraceInfo[] UST_TRACEINFOS = new TestTraceInfo[] {
                SIMPLE_SERVER1_UST_TRACE,
                SIMPLE_SERVER2_UST_TRACE,
                UST_OVERLAP_TESTING_UST_TRACE
        };
        for (TestTraceInfo info : UST_TRACEINFOS) {
            verifyTrace(info, optionFlags);
        }

        SWTBotTreeItem tracesFolderItem = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);
        assertEquals(UST_TRACEINFOS.length, tracesFolderItem.getItems().length);
    }

    /**
     * <p>
     * Action : Recursive import with specific trace type 4 (Skip All)
     * <p>
     *
     * <pre>
     * Procedure : 1) Open Import wizard (see 3.1-3.2)
     *             2) Browse to directory ${local}/traces/import/
     *             3) select directory import
     *             4) Select trace type "Tmf Generic", unselect  "Overwrite existing without warning", select "Create Links to workspace" and unselect "Preserve Folder Structure"
     *             5) press Finish
     *             6) When dialog appears select Skip All"
     * </pre>
     * <p>
     * Expected Results: All text files in directories are imported as trace and
     * trace type "Tmf Generic" is set. Note that trace type validation only
     * checks for file exists and that file is not a directory. Make sure that
     * these traces can be opened. However traces with wrong trace type won't
     * show any events in the table.
     */
    @Test
    public void test3_20ImportRecursiveSpecityTraceTypeCustomText() {
        SWTBotUtils.clearTracesFolderUI(fBot, TRACE_PROJECT_NAME);

        int optionFlags = ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE;
        importTrace(CUSTOM_TEXT_TRACE_TYPE, optionFlags, ImportConfirmation.SKIP_ALL, "");
        verifyTrace(CUSTOM_TEXT_LOG, optionFlags);

        final TestTraceInfo[] TEXT_BASED_TRACEINFOS = new TestTraceInfo[] {
                CUSTOM_TEXT_LOG,
                CUSTOM_XML_LOG_AS_TEXT,
                UNRECOGNIZED_LOG
        };
        for (TestTraceInfo info : TEXT_BASED_TRACEINFOS) {
            verifyTrace(info, optionFlags, info.getTraceName(), CUSTOM_TEXT_TRACE_TYPE);
        }

        SWTBotTreeItem tracesFolderItem = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);
        assertEquals(TEXT_BASED_TRACEINFOS.length, tracesFolderItem.getItems().length);
    }

    private static void verifyTrace(TestTraceInfo traceInfo, int importOptionFlags) {
        verifyTrace(traceInfo, importOptionFlags, traceInfo.getTraceName());
    }

    private static void verifyTrace(TestTraceInfo traceInfo, int importOptionFlags, String traceName) {
        verifyTrace(traceInfo, importOptionFlags, traceName, traceInfo.getTraceType());
    }

    private static void verifyTrace(TestTraceInfo traceInfo, int importOptionFlags, String traceName, String traceType) {
        SWTBotTreeItem traceItem = SWTBotUtils.getTreeItem(fBot, SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME), traceName);
        checkTraceType(traceItem, traceType);
        openTrace(traceItem);
        if (traceType != null && !traceType.isEmpty()) {
            SWTBotImportWizardUtils.testEventsTable(fBot, traceName, traceInfo.getNbEvents(), traceInfo.getFirstEventTimestamp());
        } else {
            // If there is no trace type, make sure it can be opened with the text editor
            fBot.waitUntil(ConditionHelpers.isEditorOpened(fBot, traceName));
            SWTBotEditor editor = fBot.editorByTitle(traceName);
            assertEquals(TEXT_EDITOR_ID, editor.getReference().getId());
        }
        checkTraceLinked(traceItem, (importOptionFlags & ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE) != 0);
    }

    private static String toRenamedName(String traceName) {
        return traceName + "(2)";
    }

    private static void openTrace(SWTBotTreeItem traceItem) {
        traceItem.select();
        traceItem.doubleClick();
    }

    private static void testSingleTrace(TestTraceInfo traceInfo, int optionFlags) {
        importTrace(optionFlags, traceInfo.getTracePath());
        verifyTrace(traceInfo, optionFlags);
    }

    private static void importTrace(int optionFlags, String ... tracePaths) {
        importTrace(optionFlags, ImportConfirmation.CONTINUE, tracePaths);
    }

    private static void importTrace(int optionFlags, ImportConfirmation confirmationMode, String ... tracePaths) {
        importTrace(null, optionFlags, confirmationMode, tracePaths);
    }

    private static void importTrace(String traceType, int optionFlags, ImportConfirmation confirmationMode, String ... tracePaths) {
        importTrace(traceType, optionFlags, new Supplier<ImportConfirmation>() {
            boolean fDone = false;
            @Override
            public ImportConfirmation get() {
                if (fDone) {
                    return null;
                }
                fDone = true;
                return confirmationMode;
            }
        }, tracePaths);
    }

    private static void importTrace(int optionFlags, Supplier<ImportConfirmation> confirmationSuplier, String ... tracePaths) {
        importTrace(null, optionFlags, confirmationSuplier, tracePaths);
    }

    /**
     * @param tracePath relative to parent test traces folder
     */
    private static void importTrace(String traceType, int optionFlags, Supplier<ImportConfirmation> confirmationSuplier, String ... tracePaths) {
        SWTBotTreeItem traceFolder = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);

        SWTBotShell shell = openTraceFoldersImport(traceFolder);
        SWTBot bot = shell.bot();
        final String importDirectoryRelativePath = "import";
        String importDirectoryFullPath = getPath(importDirectoryRelativePath);

        for (String tracePath : tracePaths) {
            IPath somePath = new Path(importDirectoryRelativePath).append(tracePath);
            IPath fullParentPath = somePath.removeLastSegments(1);
            boolean isDirectory = new Path(importDirectoryFullPath).append(tracePath).toFile().isDirectory();

            SWTBotImportWizardUtils.selectImportFromDirectory(bot, importDirectoryFullPath);
            if (isDirectory) {
                SWTBotImportWizardUtils.selectFolder(fBot, true, somePath.segments());
            } else {
                SWTBotImportWizardUtils.selectFile(bot, new Path(tracePath).lastSegment(), fullParentPath.segments());
            }
        }

        SWTBotImportWizardUtils.setOptions(bot, optionFlags, traceType);
        bot.button("Finish").click();

        ImportConfirmation importConfirmation = confirmationSuplier.get();
        while (importConfirmation != null) {
            if (importConfirmation != ImportConfirmation.CONTINUE) {
                fBot.waitUntil(Conditions.shellIsActive("Confirmation"));
                SWTBotShell shell2 = fBot.activeShell();
                SWTBotButton button = shell2.bot().button(importConfirmation.getInName());
                button.click();
            }
            importConfirmation = confirmationSuplier.get();
        }

        fBot.waitUntil(Conditions.shellCloses(shell));
    }

    private static void checkTraceType(SWTBotTreeItem traceItem, String traceType) {
        assertEquals(traceType, getTraceProperty(traceItem, "type"));
    }

    private static void checkTraceLinked(SWTBotTreeItem traceItem, boolean linked) {
        assertEquals(Boolean.toString(linked), getTraceProperty(traceItem, "linked"));
    }

    private static String getTraceProperty(SWTBotTreeItem traceItem, String property) {
        SWTBotUtils.openView(IPageLayout.ID_PROP_SHEET);
        SWTBotView view = fBot.viewById(IPageLayout.ID_PROP_SHEET);
        view.show();
        traceItem.select();
        SWTBotTreeItem traceTypeItem = SWTBotUtils.getTreeItem(view.bot(), view.bot().tree(), "Resource properties", property);
        return traceTypeItem.cell(1);
    }

    private static SWTBotShell openTraceFoldersImport(SWTBotTreeItem traceItem) {
        traceItem.contextMenu().menu("Import...").click();
        fBot.waitUntil(Conditions.shellIsActive("Trace Import"));

        SWTBotShell shell = fBot.shell("Trace Import");
        return shell;
    }
}
