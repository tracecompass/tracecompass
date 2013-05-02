/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Non-modal wizard allows background jobs to work in tandem with modal jobs.
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class NonModalWizardDialog extends WizardDialog {

    private ProgressMonitorPart fProgressMonitor;
    private IWizardPage fWizardPage;
    private List<HelperThread> fHelpers = new ArrayList<HelperThread>();

    /**
     * Creates a new non modal wizard dialog for the given wizard.
     *
     * @param parentShell
     *            the parent shell
     * @param newWizard
     *            the wizard this dialog is working on
     */
    public NonModalWizardDialog(Shell parentShell, IWizard newWizard) {
        super(parentShell, newWizard);
    }

    /**
     * Copy constructor (be careful)
     *
     * @param wiz
     *            the Wizard to clone
     */
    public NonModalWizardDialog(WizardDialog wiz) {
        super(wiz.getShell(), wiz.getCurrentPage().getWizard());
    }

    @Override
    public boolean close() {
        for(HelperThread t : fHelpers){
            t.interrupt();
        }
        for(HelperThread t : fHelpers){
            try {
                t.join(100);
            } catch (InterruptedException e) {
            }
        }
        return super.close();
    }
    /**
     * Gets the progress monitor, can be null
     *
     * @return a progress monitor that can be null
     */
    public IProgressMonitor getMonitor() {
        return super.getProgressMonitor();
    }

    @Override
    public void updateButtons() {
        super.updateButtons();
        if (fProgressMonitor != null) {
            fProgressMonitor.setVisible(this.getCurrentPage().equals(fWizardPage));
            fProgressMonitor.getMonitor();
        }
    }

    /**
     * Background job to run
     *
     * @param pageForJob
     *            the page to display the job on
     * @param runnable
     *            the runnable that is a background job
     */
    public void backgroundRun(IWizardPage pageForJob, IRunnableWithProgress runnable) {
        // create progress monitor;
        fWizardPage = pageForJob;
        IProgressMonitor x = getProgressMonitor();
        SubMonitor y = SubMonitor.convert(x);
        // make a new thread with the runnable
        HelperThread h = new HelperThread(runnable, y);

        h.start();
        fHelpers.add(h);

    }

    @Override
    protected ProgressMonitorPart createProgressMonitorPart(Composite composite, GridLayout pmlayout) {
        fProgressMonitor = new ProgressMonitorPart(composite, pmlayout);
        return fProgressMonitor;
    }

    private class HelperThread extends Thread {
        private IRunnableWithProgress runnable;
        private IProgressMonitor monitor;

        public HelperThread(IRunnableWithProgress i, IProgressMonitor s) {
            runnable = i;
            monitor = s;
        }

        @Override
        public void run() {
            try {
                runnable.run(monitor);
            } catch (InvocationTargetException e) {
                // skip over me
            } catch (InterruptedException e) {
                // skip over me
            }
        }
    }

}
