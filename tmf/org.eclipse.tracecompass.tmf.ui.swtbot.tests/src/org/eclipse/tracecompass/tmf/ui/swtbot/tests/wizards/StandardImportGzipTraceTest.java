/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation.
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.swtbot.tests.wizards;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.ImportTraceWizard;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.Messages;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * Import operation for gz traces
 *
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class StandardImportGzipTraceTest {
    private static final String ROOT_FOLDER = "/";
    private static final String PROJECT_NAME = "Tracing";
    private static File fGzipTrace;
    private Wizard fWizard;
    private SWTWorkbenchBot fBot;

    /** The Log4j logger instance. */
    protected static final Logger fLogger = Logger.getRootLogger();

    /**
     * create a gzip file
     */
    @BeforeClass
    public static void init() {
        SWTBotUtils.initialize();
        zipTrace();
        fLogger.removeAllAppenders();
        fLogger.addAppender(new NullAppender());
    }

    /**
     * create the project
     */
    @Before
    public void setup() {
        createProject();
        SWTBotPreferences.TIMEOUT = 20000;
    }

    /**
     * cleanup
     */
    @AfterClass
    public static void destroy() {
        fLogger.removeAllAppenders();
    }

    /**
     * Tear down the test
     */
    @After
    public void tearDown() {
        SWTBotUtils.deleteProject(PROJECT_NAME, fBot);
    }

    private void createProject() {
        fBot = new SWTWorkbenchBot();
        /* Close welcome view */
        SWTBotUtils.closeView("Welcome", fBot);
        SWTBotUtils.switchToTracingPerspective();

        SWTBotUtils.createProject(PROJECT_NAME);
    }

    /**
     * Import a gzip trace
     */
    @Test
    public void testGzipImport() {
        final String traceType = "Test trace : TMF Tests";
        final String tracesNode = "Traces [1]";

        /*
         * Actual importing
         */
        openImportWizard();
        selectImportFromArchive(fGzipTrace.getAbsolutePath());
        selectFolder(ROOT_FOLDER);
        SWTBotCheckBox checkBox = fBot.checkBox(Messages.ImportTraceWizard_CreateLinksInWorkspace);
        assertFalse(checkBox.isEnabled());
        SWTBotCombo comboBox = fBot.comboBoxWithLabel(Messages.ImportTraceWizard_TraceType);
        comboBox.setSelection(traceType);
        importFinish();
        /*
         * Remove .gz extension
         */
        assertNotNull(fGzipTrace);
        String name = fGzipTrace.getName();
        assertNotNull(name);
        assertTrue(name.length() > 3);
        String traceName = name.substring(0, name.length() - 3);
        assertNotNull(traceName);
        assertFalse(traceName.isEmpty());

        /*
         * Open trace
         */
        SWTBotView projectExplorer = fBot.viewById(IPageLayout.ID_PROJECT_EXPLORER);
        projectExplorer.setFocus();
        final SWTBotTree tree = projectExplorer.bot().tree();
        /*
         * This appears to be problematic due to the length of the file name and
         * the resolution in our CI.
         */
        SWTBotTreeItem treeItem = SWTBotUtils.getTreeItem(projectExplorer.bot(), tree, PROJECT_NAME, tracesNode, traceName);
        treeItem.doubleClick();
        WaitUtils.waitForJobs();
        /*
         * Check results
         */
        SWTBot editorBot = SWTBotUtils.activeEventsEditor(fBot).bot();
        SWTBotTable editorTable = editorBot.table();
        final String expectedContent1 = "Type-1";
        final String expectedContent2 = "";
        editorBot.waitUntil(ConditionHelpers.isTableCellFilled(editorTable, expectedContent1, 2, 2));
        editorBot.waitUntil(ConditionHelpers.isTableCellFilled(editorTable, expectedContent2, 1, 0));
        String c22 = editorTable.cell(2, 2);
        String c10 = editorTable.cell(1, 0);
        assertEquals(expectedContent1, c22);
        assertEquals(expectedContent2, c10);
    }

    private static void zipTrace() {
        try {
            fGzipTrace = File.createTempFile("trace", ".gz");
            byte[] buffer = new byte[1024];

            try (GZIPOutputStream gzos = new GZIPOutputStream(new FileOutputStream(fGzipTrace));) {

                try (FileInputStream in = new FileInputStream(TmfTestTrace.A_TEST_10K2.getFullPath());) {

                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        gzos.write(buffer, 0, len);
                    }
                }
                gzos.finish();
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private void selectImportFromArchive(String archivePath) {
        SWTBotRadio button = fBot.radio("Select &archive file:");
        button.click();

        SWTBotCombo sourceCombo = fBot.comboBox(1);

        sourceCombo.setText(new File(archivePath).getAbsolutePath());

        SWTBotTree tree = fBot.tree();
        tree.setFocus();
    }

    private void openImportWizard() {
        fWizard = new ImportTraceWizard();

        UIThreadRunnable.asyncExec(new VoidResult() {
            @Override
            public void run() {
                final IWorkbench workbench = PlatformUI.getWorkbench();
                // Fire the Import Trace Wizard
                if (workbench != null) {
                    final IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
                    Shell shell = activeWorkbenchWindow.getShell();
                    assertNotNull(shell);
                    ((ImportTraceWizard) fWizard).init(PlatformUI.getWorkbench(), StructuredSelection.EMPTY);
                    WizardDialog dialog = new WizardDialog(shell, fWizard);
                    dialog.open();
                }
            }
        });

        fBot.waitUntil(ConditionHelpers.isWizardReady(fWizard));
    }

    private void selectFolder(String... treePath) {
        SWTBotTree tree = fBot.tree();
        fBot.waitUntil(Conditions.widgetIsEnabled(tree));
        SWTBotTreeItem folderNode = SWTBotUtils.getTreeItem(fBot, tree, treePath);
        folderNode.check();
    }

    private void importFinish() {
        SWTBotShell shell = fBot.activeShell();
        final SWTBotButton finishButton = fBot.button("Finish");
        finishButton.click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        WaitUtils.waitForJobs();
    }
}
