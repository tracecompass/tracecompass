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

package org.eclipse.linuxtools.lttng.ui.views.project.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.lttng.ui.views.project.ProjectView;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngExperimentNode;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>OpenExperimentHandler</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class OpenExperimentHandler implements IHandler {

	private LTTngExperimentNode fExperiment = null;

	// ------------------------------------------------------------------------
	// Validation
	// ------------------------------------------------------------------------

	public boolean isEnabled() {
		// Check if we are closing down
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
			return false;

		// Check if a trace is selected
		IWorkbenchPage page = window.getActivePage();
		if (!(page.getActivePart() instanceof ProjectView))
			return false;

		// Check if a trace is selected
		ISelection selection = page.getSelection(ProjectView.ID);
		if (selection instanceof StructuredSelection) {
			Object element = ((StructuredSelection) selection).getFirstElement();
			fExperiment = (element instanceof LTTngExperimentNode) ? (LTTngExperimentNode) element : null;
		}

		return (fExperiment != null);
	}

	// Handled if we are in the ProjectView
	public boolean isHandled() {
		return true;
	}

	// ------------------------------------------------------------------------
	// Execution
	// ------------------------------------------------------------------------

	public Object execute(ExecutionEvent event) throws ExecutionException {

		// Set the selection to the project
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		ProjectView projectView = (ProjectView) page.getActivePart();

		if (projectView != null) {
			projectView.selectExperiment(fExperiment);
		}

		return null;
	}

	// ------------------------------------------------------------------------
	// IHandlerListener
	// ------------------------------------------------------------------------

	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub
	}

	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
