/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ctf.ui.swtbot.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.importtrace.ImportTraceWizard;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.importtrace.ImportTraceWizardPage;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.importtrace.Messages;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTracesFolder;
import org.eclipse.linuxtools.tmf.ui.swtbot.tests.SWTBotUtil;
import org.eclipse.linuxtools.tmf.ui.swtbot.tests.conditions.ConditionHelpers;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot Smoke test using ImportTraceWizard.
 *
 * @author Bernd Hufmann
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class StandardImportAndReadSmokeTest extends AbstractImportAndReadSmokeTest {

    private static final String TRACE_FOLDER_PARENT_PATH = fTrace.getPath() + File.separator + ".." + File.separator + ".." + File.separator;
    private static final String TRACE_ARCHIVE_PATH = TRACE_FOLDER_PARENT_PATH + "synctraces.tar.gz";
    private static final String TRACE_PROJECT_NAME = "Tracing";

    /**
     * Test import from directory
     */
    @Test
    public void testImportFromDirectory() {
        testImport(0, false, false);
    }

    /**
     * Test import from directory, create links
     */
    @Test
    public void testImportFromDirectoryLinks() {
        testImport(ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE, false, false);
    }

    /**
     * Test import from directory, preserve folder structure
     */
    @Test
    public void testImportFromDirectoryPreserveFolder() {
        testImport(ImportTraceWizardPage.OPTION_PRESERVE_FOLDER_STRUCTURE, false, false);
    }

    /**
     * Test import from directory, create links, preserve folder structure
     */
    @Test
    public void testImportFromDirectoryLinksPreserveFolder() {
        int options = ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE | ImportTraceWizardPage.OPTION_PRESERVE_FOLDER_STRUCTURE;
        testImport(options, false, false);
    }

    /**
     * Test import from directory, overwrite all
     */
    @Test
    public void testImportFromDirectoryOverwrite() {
        testImport(0, false, false);
        testImport(ImportTraceWizardPage.OPTION_OVERWRITE_EXISTING_RESOURCES, false, false);
    }

    /**
     * Test import from archive
     */
    @Test
    public void testImportFromArchive() {
        testImport(ImportTraceWizardPage.OPTION_PRESERVE_FOLDER_STRUCTURE, true, true);
    }

    /**
     * Test import from directory, preserve folder structure
     */
    @Test
    public void testImportFromArchivePreserveFolder() {
        testImport(ImportTraceWizardPage.OPTION_PRESERVE_FOLDER_STRUCTURE, false, true);
    }

    /**
     * Test import from directory, overwrite all
     */
    @Test
    public void testImportFromArchiveOverwrite() {
        testImport(0, false, true);
        testImport(ImportTraceWizardPage.OPTION_OVERWRITE_EXISTING_RESOURCES, false, true);
    }

    private void testImport(int options, boolean testViews, boolean fromArchive) {
        createProject();

        importOpenWizard();
        if (fromArchive) {
            importAddArchive();
        } else {
            importAddDirectory();
        }

        setOptions(options, ImportTraceWizardPage.TRACE_TYPE_AUTO_DETECT);
        importFinish();

        checkOptions(options);
        TmfEventsEditor tmfEd = openEditor(getTraceElementPath(options));
        if (testViews) {
            testViews(tmfEd);
        }

        fBot.closeAllEditors();

        SWTBotUtil.deleteProject(getProjectName(), fBot);
    }

    private void testViews(TmfEventsEditor editor) {
        testHistogramView(getViewPart("Histogram"), editor);
        testPropertyView(getViewPart("Properties"));
        testStatisticsView(getViewPart("Statistics"));
    }

    private static void importOpenWizard() {
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

    private static void importAddDirectory() {
        SWTBotRadio button = fBot.radio("Select roo&t directory:");
        button.click();

        SWTBotCombo sourceCombo = fBot.comboBox();
        File traceFolderParent = new File(TRACE_FOLDER_PARENT_PATH);
        sourceCombo.setText(traceFolderParent.getAbsolutePath());

        SWTBotText text = fBot.text();
        text.setFocus();

        fBot.activeShell();
        SWTBotTree tree = fBot.tree();
        fBot.waitUntil(Conditions.widgetIsEnabled(tree));

        final String traceFolderParentName = new Path(traceFolderParent.getAbsolutePath()).lastSegment();
        fBot.waitUntil(ConditionHelpers.IsTreeNodeAvailable(traceFolderParentName, tree));
        final SWTBotTreeItem folderParentNode = tree.getTreeItem(traceFolderParentName);
        folderParentNode.expand();

        fBot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable(TRACE_FOLDER, folderParentNode));
        final SWTBotTreeItem folderNode = folderParentNode.getNode(TRACE_FOLDER);
        folderNode.check();
    }

    private static void importAddArchive() {
        SWTBotRadio button = fBot.radio("Select &archive file:");
        button.click();

        SWTBotCombo sourceCombo = fBot.comboBox(1);

        sourceCombo.setText(new File(TRACE_ARCHIVE_PATH).getAbsolutePath());

        SWTBotText text = fBot.text();
        text.setFocus();

        SWTBotTree tree = fBot.tree();
        fBot.waitUntil(Conditions.widgetIsEnabled(tree));
        final SWTBotTreeItem genericCtfTreeItem = tree.getTreeItem("/");
        fBot.waitUntil(Conditions.widgetIsEnabled(genericCtfTreeItem));
        genericCtfTreeItem.check();

        SWTBotCheckBox checkBox = fBot.checkBox(Messages.ImportTraceWizard_CreateLinksInWorkspace);
        assertFalse(checkBox.isEnabled());
    }

    private static void setOptions(int optionFlags, String traceTypeName) {
        SWTBotCheckBox checkBox = fBot.checkBox(Messages.ImportTraceWizard_CreateLinksInWorkspace);
        if (checkBox.isEnabled()) {
            if ((optionFlags & ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE) != 0) {
                checkBox.select();
            } else {
                checkBox.deselect();
            }
        }

        checkBox = fBot.checkBox(Messages.ImportTraceWizard_PreserveFolderStructure);
        if ((optionFlags & ImportTraceWizardPage.OPTION_PRESERVE_FOLDER_STRUCTURE) != 0) {
            checkBox.select();
        } else {
            checkBox.deselect();
        }

        checkBox = fBot.checkBox(Messages.ImportTraceWizard_ImportUnrecognized);
        if ((optionFlags & ImportTraceWizardPage.OPTION_IMPORT_UNRECOGNIZED_TRACES) != 0) {
            checkBox.select();
        } else {
            checkBox.deselect();
        }

        checkBox = fBot.checkBox(Messages.ImportTraceWizard_OverwriteExistingTrace);
        if ((optionFlags & ImportTraceWizardPage.OPTION_OVERWRITE_EXISTING_RESOURCES) != 0) {
            checkBox.select();
        } else {
            checkBox.deselect();
        }

        SWTBotCombo comboBox = fBot.comboBoxWithLabel(Messages.ImportTraceWizard_TraceType);
        if (traceTypeName != null && !traceTypeName.isEmpty()) {
            comboBox.setSelection(traceTypeName);
        } else {
            comboBox.setSelection(ImportTraceWizardPage.TRACE_TYPE_AUTO_DETECT);
        }
    }

    private static void checkOptions(int optionFlags) {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(TRACE_PROJECT_NAME);
        assertTrue(project.exists());
        TmfProjectElement tmfProject = TmfProjectRegistry.getProject(project, true);
        assertNotNull(tmfProject);
        TmfTraceFolder tracesFolder = tmfProject.getTracesFolder();
        assertNotNull(tracesFolder);
        List<TmfTraceElement> traces = tracesFolder.getTraces();
        assertFalse(traces.isEmpty());
        Collections.sort(traces, new Comparator<TmfTraceElement>() {
            @Override
            public int compare(TmfTraceElement arg0, TmfTraceElement arg1) {
                return arg0.getElementPath().compareTo(arg1.getElementPath());
            }
        });

        TmfTraceElement tmfTraceElement = traces.get(0);
        IResource traceResource = tmfTraceElement.getResource();

        assertEquals((optionFlags & ImportTraceWizardPage.OPTION_CREATE_LINKS_IN_WORKSPACE) != 0, traceResource.isLinked());

        // i.e. /Tracing/Traces
        IPath expectedPath = Path.ROOT.append(new Path(TRACE_PROJECT_NAME)).append(TmfTracesFolder.TRACES_FOLDER_NAME);
        expectedPath = expectedPath.append(getTraceElementPath(optionFlags));
        assertEquals(expectedPath, traceResource.getFullPath());
    }

    private static IPath getTraceElementPath(int optionFlags) {
        IPath traceElementPath = new Path("");
        if ((optionFlags & ImportTraceWizardPage.OPTION_PRESERVE_FOLDER_STRUCTURE) != 0) {
            traceElementPath = traceElementPath.append(TRACE_FOLDER);
        }
        return traceElementPath.append(TRACE_NAME);
    }

    @Override
    protected String getProjectName() {
        return TRACE_PROJECT_NAME;
    }
}
