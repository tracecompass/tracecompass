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

package org.eclipse.linuxtools.tmf.ui.views.project.dialogs;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfExperimentNode;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfProjectNode;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfTraceNode;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * <b><u>AddTraceWizard</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class AddTraceWizard extends Wizard implements IImportWizard {

    private TmfProjectNode fProject;
    private TmfExperimentNode fExperiment;
    private AddTraceWizardPage fMainPage;

    /**
     * @param project
     */
    public AddTraceWizard(TmfProjectNode project, TmfExperimentNode experiment) {
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

		TmfTraceNode[] traces = fMainPage.getSelection();
		for (TmfTraceNode trace : traces) {
			try {
			    IResource resource = trace.getResource();
			    IPath location = resource.getLocation();
			    if (resource instanceof IFolder) {
			        IFolder folder = experimentFolder.getFolder(trace.getName());
			        if (workspace.validateLinkLocation(folder, location).isOK()) {
			            folder.createLink(location, IResource.REPLACE, null);
			        }
			        else {
			            System.out.println(Messages.AddTraceWizard_invalidTraceLocation);
			        }
			    } else {
                    IFile file = experimentFolder.getFile(trace.getName());
                    if (workspace.validateLinkLocation(file, location).isOK()) {
                        file.createLink(location, IResource.REPLACE, null);
                    }
                    else {
                        System.out.println(Messages.AddTraceWizard_invalidTraceLocation);
                    }
			    }
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		return true;
	}	

}
