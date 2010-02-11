/*******************************************************************************
 * Copyright (c) 2009 Ericsson
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
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngExperimentEntry;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngProject;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngTraceEntry;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * <b><u>AddTraceWizard</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class AddTraceWizard extends Wizard implements IImportWizard {

    @SuppressWarnings("unused")
	private IWorkbench fWorkbench;
    private LTTngProject fProject;
    private IStructuredSelection fSelection;
    private AddTraceWizardPage fMainPage;

    /**
     * @param project
     */
    public AddTraceWizard(LTTngProject project) {
    	fProject = project;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
    	fWorkbench = workbench;
    	fSelection = selection;
    	setWindowTitle("Adding traces to experiment");
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
	public void addPages() {
        super.addPages();
        fMainPage = new AddTraceWizardPage(fProject, "Some string");
        addPage(fMainPage);
        fMainPage.init(fSelection);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {

		Object selection = fSelection.getFirstElement();
		if (!(selection instanceof LTTngExperimentEntry)) {
			return true;
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		LTTngExperimentEntry experiment = (LTTngExperimentEntry) selection;
		IFolder experimentFolder = experiment.getFolder();

		LTTngTraceEntry[] traces = fMainPage.getSelection();
		for (LTTngTraceEntry trace : traces) {
			try {
				IFolder folder = experimentFolder.getFolder(trace.getName());
				IPath location = trace.getResource().getLocation();
				if (workspace.validateLinkLocation(folder, location).isOK()) {
					folder.createLink(location, IResource.REPLACE, null);
					experiment.addTrace(trace);
				}
				else {
					System.out.println("Problem");
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return true;
	}
	
	

}
