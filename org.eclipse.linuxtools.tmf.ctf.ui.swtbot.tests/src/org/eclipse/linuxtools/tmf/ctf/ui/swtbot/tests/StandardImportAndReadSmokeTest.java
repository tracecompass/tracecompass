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

import static org.junit.Assert.assertNotNull;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.importtrace.ImportTraceWizard;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.linuxtools.tmf.ui.swtbot.tests.conditions.ConditionHelpers;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
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

    private static final String TRACE_PROJECT_NAME = "Tracing";

    /**
     * Main test case
     */
    @Test
    public void test() {
        createProject();

        importOpenWizard();
        importAddDirectory();
        importFinish();

        TmfEventsEditor tmfEd = openEditor();

        testHistogramView(getViewPart("Histogram"), tmfEd);
        testPropertyView(getViewPart("Properties"));
        testStatisticsView(getViewPart("Statistics"));

        deleteProject();
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
        SWTBotCombo sourceCombo = fBot.comboBox();
        sourceCombo.setText(fTrace.getPath());

        SWTBotText text = fBot.text();
        text.setFocus();

        fBot.activeShell();
        SWTBotTree tree = fBot.tree();
        fBot.waitUntil(Conditions.widgetIsEnabled(tree));
        final SWTBotTreeItem genericCtfTreeItem = tree.getTreeItem(TRACE_NAME);
        fBot.waitUntil(Conditions.widgetIsEnabled(genericCtfTreeItem));
        genericCtfTreeItem.check();
    }

    @Override
    protected String getProjectName() {
        return TRACE_PROJECT_NAME;
    }

}
