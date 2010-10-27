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
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.lttng.ui.views.project.ProjectView;
import org.eclipse.linuxtools.lttng.ui.views.project.dialogs.NewExperimentDialog;
import org.eclipse.linuxtools.lttng.ui.views.project.model.ILTTngProjectTreeNode;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngExperimentFolderNode;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngProjectNode;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>NewExperimentHandler</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class NewExperimentHandler implements IHandler {

	private LTTngProjectNode fProject = null;

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
			if (element instanceof ILTTngProjectTreeNode) {
				ILTTngProjectTreeNode node = (ILTTngProjectTreeNode) element;
				while (node != null && !(node instanceof LTTngProjectNode)) {
					node = node.getParent();
				}
				fProject = (node instanceof LTTngProjectNode) ? (LTTngProjectNode) node : null;
			}
		}

		return (fProject != null && fProject.isOpen() && fProject.isLTTngProject());
	}

	// Handled if we are in the ProjectView
	@Override
	public boolean isHandled() {
		return true;
	}

	// ------------------------------------------------------------------------
	// Execution
	// ------------------------------------------------------------------------

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		// Open the experiment creation dialog
		LTTngExperimentFolderNode experimentFolder = fProject.getExperimentsFolder();
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		NewExperimentDialog dialog = new NewExperimentDialog(shell, experimentFolder);
		dialog.open();

		if (dialog.getReturnCode() == Window.OK) {
			Object[] experiments = dialog.getResult();
			if (experiments[0] instanceof IFolder) {
				experimentFolder.addExperiment((IFolder) experiments[0]);
			}
		}

		return null;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	// ------------------------------------------------------------------------
	// IHandlerListener
	// ------------------------------------------------------------------------

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub
	}

}
