/*******************************************************************************
 * Copyright (c) 2009, 2010, 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * <b><u>SelectTracesWizard</u></b>
 * <p>
 */
public class SelectTracesWizard extends Wizard implements IImportWizard {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final TmfProjectElement fProject;
    private final TmfExperimentElement fExperiment;
    private SelectTracesWizardPage fSelectTraceWizardPage;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    public SelectTracesWizard(TmfProjectElement project, TmfExperimentElement experiment) {
        fProject = project;
        fExperiment = experiment;
    }

    // ------------------------------------------------------------------------
    // Wizard
    // ------------------------------------------------------------------------

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setWindowTitle(Messages.SelectTracesWizard_WindowTitle);
    }

    @Override
    public void addPages() {
        super.addPages();
        fSelectTraceWizardPage = new SelectTracesWizardPage(fProject, fExperiment);
        addPage(fSelectTraceWizardPage);
    }

    @Override
    public boolean performFinish() {
        return fSelectTraceWizardPage.performFinish();
    }

}
