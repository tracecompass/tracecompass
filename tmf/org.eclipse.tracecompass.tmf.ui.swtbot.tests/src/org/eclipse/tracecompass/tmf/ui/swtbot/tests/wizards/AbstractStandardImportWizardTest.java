/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.swtbot.tests.wizards;

import static org.junit.Assert.assertNotNull;

import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.ImportTraceWizard;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Abstract SWTBot standard import test class
 *
 * @author Simon Delisle
 */
public abstract class AbstractStandardImportWizardTest {

    /**
     * Project name in the workspace
     */
    public static final String PROJECT_NAME = "Tracing";
    /**
     * Import wizard
     */
    private Wizard fWizard;
    /**
     * SWTBot
     */
    private static SWTWorkbenchBot fBot;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    /**
     * Class setup
     */
    @BeforeClass
    public static void init() {
        SWTBotUtils.initialize();
        fBot = new SWTWorkbenchBot();
        fLogger.removeAllAppenders();
        fLogger.addAppender(new NullAppender());
    }

    /**
     * Class teardown
     */
    @AfterClass
    public static void terminate() {
        fLogger.removeAllAppenders();
    }

    /**
     * Get the SWT Bot
     *
     * @return SWT Bot
     */
    public static SWTWorkbenchBot getSWTBot() {
        return fBot;
    }

    /**
     * Create the project
     */
    public void createProject() {
        /* Close welcome view */
        SWTBotUtils.closeView("Welcome", fBot);

        SWTBotUtils.createProject(PROJECT_NAME);
    }

    /**
     * Open the import wizard
     */
    public void openImportWizard() {
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

    /**
     * Click on the finish button to import
     */
    public void importFinish() {
        SWTBotShell shell = fBot.activeShell();
        final SWTBotButton finishButton = fBot.button("Finish");
        finishButton.click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        WaitUtils.waitForJobs();
    }
}
