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
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngProjectNode;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>RenameProjectHandler</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class RenameProjectHandler implements IHandler {

	private LTTngProjectNode fProject = null;

	// ------------------------------------------------------------------------
	// Validation
	// ------------------------------------------------------------------------

	@Override
	public boolean isEnabled() {

//		// Check if we are closing down
//		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//		if (window == null)
//			return false;
//
//		// Check if we are in the Project View
//		IWorkbenchPage page = window.getActivePage();
//		if (!(page.getActivePart() instanceof ProjectView))
//			return false;
//
//		// Check if a project is selected
//		ISelection selection = page.getSelection(ProjectView.ID);
//		if (selection instanceof StructuredSelection) {
//			Object element = ((StructuredSelection) selection).getFirstElement();
//			fProjectNode = (element instanceof LTTngProjectNode) ? (LTTngProjectNode) element : null;
//		}

		return (fProject != null);
	}

	// Handled if we are in the ProjectView
	@Override
	public boolean isHandled() {
		return false;
	}

	// ------------------------------------------------------------------------
	// Execution
	// ------------------------------------------------------------------------

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		mb.setText("Rename Project"); //$NON-NLS-1$
		mb.setMessage("Not implemented yet"); //$NON-NLS-1$
		mb.open();

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
