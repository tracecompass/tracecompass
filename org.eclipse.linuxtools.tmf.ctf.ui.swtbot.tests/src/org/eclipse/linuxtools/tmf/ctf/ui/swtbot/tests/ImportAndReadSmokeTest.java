/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Marc-Andre Laperle
 *   Bernd Hufmann - Extracted functionality to class AbstractImportAndReadSmokeTest
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ctf.ui.swtbot.tests;

import static org.junit.Assert.assertNotNull;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.importtrace.BatchImportTraceWizard;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.linuxtools.tmf.ui.swtbot.tests.SWTBotUtil;
import org.eclipse.linuxtools.tmf.ui.swtbot.tests.conditions.ConditionHelpers;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot Smoke test. base for other tests
 *
 * @author Matthew Khouzam
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class ImportAndReadSmokeTest extends AbstractImportAndReadSmokeTest {

    private static final String TRACE_PROJECT_NAME = "test";

    /**
     * Main test case
     */
    @Test
    public void test() {
        createProject();

        batchImportOpenWizard();
        batchImportSelecTraceType();
        batchImportAddDirectory();
        batchImportSelectTrace();
        importFinish();

        TmfEventsEditor tmfEd = openEditor();

        testHistogramView(getViewPart("Histogram"), tmfEd);
        testPropertyView(getViewPart("Properties"));
        testStatisticsView(getViewPart("Statistics"));
        fBot.closeAllEditors();

        SWTBotUtil.deleteProject(getProjectName(), fBot);
    }

    private static void batchImportOpenWizard() {
        fWizard = new BatchImportTraceWizard();

        UIThreadRunnable.asyncExec(new VoidResult() {
            @Override
            public void run() {
                final IWorkbench workbench = PlatformUI.getWorkbench();
                // Fire the Import Trace Wizard
                if (workbench != null) {
                    final IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
                    Shell shell = activeWorkbenchWindow.getShell();
                    assertNotNull(shell);
                    ((BatchImportTraceWizard) fWizard).init(PlatformUI.getWorkbench(), StructuredSelection.EMPTY);
                    WizardDialog dialog = new WizardDialog(shell, fWizard);
                    dialog.open();
                }
            }
        });

        fBot.waitUntil(ConditionHelpers.isWizardReady(fWizard));
    }

    private static void batchImportSelecTraceType() {
        final SWTBotTree tree = fBot.tree();
        final String ctfId = "Common Trace Format";
        fBot.waitUntil(ConditionHelpers.IsTreeNodeAvailable(ctfId, tree));
        fBot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable(TRACE_TYPE_NAME, tree.getTreeItem(ctfId)));
        tree.getTreeItem(ctfId).getNode(TRACE_TYPE_NAME).check();
        batchImportClickNext();
    }

    private static void batchImportAddDirectory() {
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                ((BatchImportTraceWizard) fWizard).addFileToScan(fTrace.getPath());
            }
        });
        final SWTBotButton removeButton = fBot.button("Remove");
        fBot.waitUntil(Conditions.widgetIsEnabled(removeButton));
        removeButton.click();
        fBot.waitUntil(Conditions.tableHasRows(fBot.table(), 1));

        batchImportClickNext();
    }

    private static void batchImportSelectTrace() {
        SWTBotTree tree = fBot.tree();
        fBot.waitUntil(Conditions.widgetIsEnabled(tree));
        final SWTBotTreeItem genericCtfTreeItem = tree.getTreeItem(TRACE_TYPE_NAME);
        fBot.waitUntil(Conditions.widgetIsEnabled(genericCtfTreeItem));
        genericCtfTreeItem.expand();
        genericCtfTreeItem.check();
        batchImportClickNext();
    }

    private static void batchImportClickNext() {
        IWizardPage currentPage = fWizard.getContainer().getCurrentPage();
        IWizardPage desiredPage = fWizard.getNextPage(currentPage);
        SWTBotButton nextButton = fBot.button("Next >");
        nextButton.click();
        fBot.waitUntil(ConditionHelpers.isWizardOnPage(fWizard, desiredPage));
    }

    @Override
    protected String getProjectName() {
        return TRACE_PROJECT_NAME;
    }

    @Override
    protected boolean supportsFolderStructure() {
        return false;
    }
}
