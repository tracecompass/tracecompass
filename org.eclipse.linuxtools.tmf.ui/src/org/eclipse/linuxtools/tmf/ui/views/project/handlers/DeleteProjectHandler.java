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

package org.eclipse.linuxtools.tmf.ui.views.project.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.tmf.ui.views.project.ProjectView;
import org.eclipse.linuxtools.tmf.ui.views.project.model.ITmfProjectTreeNode;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfProjectNode;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>DeleteProjectHandler</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class DeleteProjectHandler extends AbstractHandler {

	private TmfProjectNode fProject = null;

	// ------------------------------------------------------------------------
	// Validation
	// ------------------------------------------------------------------------

	@Override
	public boolean isEnabled() {
		
		// Check if we are closing down
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
			return false;

		// Check if we are in the Project View
		IWorkbenchPage page = window.getActivePage();
		if (!(page.getActivePart() instanceof ProjectView))
			return false;

		// Check if a project is selected
		ISelection selection = page.getSelection(ProjectView.ID);
		if (selection instanceof StructuredSelection) {
			Object element = ((StructuredSelection) selection).getFirstElement();
			fProject = (element instanceof TmfProjectNode) ? (TmfProjectNode) element : null;
		}
		return (fProject != null);
	}

	// ------------------------------------------------------------------------
	// Execution
	// ------------------------------------------------------------------------

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IProject project = fProject.getProject();
		ITmfProjectTreeNode parent = fProject.getParent();
		try {
			parent.removeChild(fProject);
			parent.refresh();
			project.delete(true, true, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return null;
	}

}
