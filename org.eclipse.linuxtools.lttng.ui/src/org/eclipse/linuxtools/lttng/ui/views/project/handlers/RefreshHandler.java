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
import org.eclipse.linuxtools.lttng.ui.views.project.model.ILTTngProjectTreeNode;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngProjectRoot;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>RefreshHandler</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class RefreshHandler implements IHandler {

	private LTTngProjectRoot fProjectRoot = null;

	// ------------------------------------------------------------------------
	// Validation
	// ------------------------------------------------------------------------

	public boolean isEnabled() {
		
		// Check if we are closing down
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
			return false;

		// Check if we can find the project model root node
		ISelection selection = window.getActivePage().getSelection(ProjectView.ID);
		if (selection instanceof StructuredSelection) {
			Object element = ((StructuredSelection) selection).getFirstElement();
			if (element instanceof ILTTngProjectTreeNode) {
				ILTTngProjectTreeNode node = (ILTTngProjectTreeNode) element;
				while (node != null && !(node instanceof LTTngProjectRoot)) {
					node = node.getParent();
				}
				fProjectRoot = (node instanceof LTTngProjectRoot) ? (LTTngProjectRoot) node : null;
			}
		}

		return (fProjectRoot != null);
	}

	// Handled if we are in the ProjectView
	public boolean isHandled() {
		return true;
	}

	// ------------------------------------------------------------------------
	// Execution
	// ------------------------------------------------------------------------

	public Object execute(ExecutionEvent event) throws ExecutionException {

		if (fProjectRoot != null) {
			fProjectRoot.refreshChildren();
			fProjectRoot.refresh();
		}

		return null;
	}

	public void dispose() {
		// TODO Auto-generated method stub
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

}
