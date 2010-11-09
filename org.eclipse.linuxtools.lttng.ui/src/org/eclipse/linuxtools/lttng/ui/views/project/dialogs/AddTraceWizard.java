/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.views.project.dialogs;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngExperimentNode;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngProjectNode;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngTraceNode;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * <b><u>AddTraceWizard</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class AddTraceWizard extends Wizard implements IImportWizard {

    private LTTngProjectNode fProject;
    private LTTngExperimentNode fExperiment;
    private AddTraceWizardPage fMainPage;

    /**
     * @param project
     */
    public AddTraceWizard(LTTngProjectNode project, LTTngExperimentNode experiment) {
    	fProject = project;
    	fExperiment = experiment;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
    	setWindowTitle(Messages.AddTraceWizard_windowTitle);
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
	public void addPages() {
        super.addPages();
        fMainPage = new AddTraceWizardPage(fProject, "Some string"); //$NON-NLS-1$
        addPage(fMainPage);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IFolder experimentFolder = fExperiment.getFolder();

		LTTngTraceNode[] traces = fMainPage.getSelection();
		for (LTTngTraceNode trace : traces) {
			try {
				IFolder folder = experimentFolder.getFolder(trace.getName());
				IPath location = trace.getFolder().getLocation();
				if (workspace.validateLinkLocation(folder, location).isOK()) {
					folder.createLink(location, IResource.REPLACE, null);
					fExperiment.addTrace(folder);
				}
				else {
					System.out.println(Messages.AddTraceWizard_invalidTraceLocation);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		return true;
	}	

}
