/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.wizards;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard implementation to select traces for an experiment.
 * <p>
 * @version 1.0
 * @author Francois Chouinard
 */
public class SelectTracesWizard extends Wizard implements IImportWizard {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final TmfProjectElement fProject;
    private final @NonNull TmfExperimentElement fExperiment;
    private SelectTracesWizardPage fSelectTraceWizardPage;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param project The project model element
     * @param experiment The experiemnt model element
     */
    public SelectTracesWizard(TmfProjectElement project, @NonNull TmfExperimentElement experiment) {
        fProject = project;
        fExperiment = experiment;
    }

    // ------------------------------------------------------------------------
    // Wizard
    // ------------------------------------------------------------------------

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setWindowTitle(Messages.SelectTracesWizard_WindowTitle);
        setNeedsProgressMonitor(true);
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
