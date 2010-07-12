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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.tmf.ui.views.project.ProjectView;
import org.eclipse.linuxtools.tmf.ui.views.project.model.ITmfProjectTreeNode;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfProjectRoot;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>RefreshHandler</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class RefreshHandler extends AbstractHandler {

	private TmfProjectRoot fProjectRoot = null;

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
			if (element instanceof ITmfProjectTreeNode) {
				ITmfProjectTreeNode node = (ITmfProjectTreeNode) element;
				while (node != null && !(node instanceof TmfProjectRoot)) {
					node = node.getParent();
				}
				fProjectRoot = (node instanceof TmfProjectRoot) ? (TmfProjectRoot) node : null;
			}
		}

		return (fProjectRoot != null);
	}

	// ------------------------------------------------------------------------
	// Execution
	// ------------------------------------------------------------------------

	public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        try {
            root.refreshLocal(IResource.DEPTH_INFINITE, null);
            fProjectRoot.refreshChildren();
        } catch (CoreException e) {
            throw new ExecutionException("CoreException", e);
        }

		return null;
	}

}
