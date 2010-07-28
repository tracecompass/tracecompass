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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.tmf.ui.views.project.ProjectView;
import org.eclipse.linuxtools.tmf.ui.views.project.model.ITmfProjectTreeNode;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfExperimentFolderNode;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfExperimentNode;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfTraceNode;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>DeleteTraceHandler</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class DeleteTraceHandler extends AbstractHandler {

	private TmfTraceNode fTrace = null;

	// ------------------------------------------------------------------------
	// Validation
	// ------------------------------------------------------------------------

	@Override
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
			fTrace = (element instanceof TmfTraceNode) ? (TmfTraceNode) element : null;
		}

		return (fTrace != null);
	}

	// ------------------------------------------------------------------------
	// Execution
	// ------------------------------------------------------------------------

	public Object execute(ExecutionEvent event) throws ExecutionException {

		IResource resource = fTrace.getResource();
		ITmfProjectTreeNode parent = fTrace.getParent();
		try {
		    if (!(parent instanceof TmfExperimentNode)) {
		        // Delete the trace from every experiment where it appears
		        TmfExperimentFolderNode experimentFolder = fTrace.getProject().getExperimentsFolder();
		        for (ITmfProjectTreeNode experiment : experimentFolder.getChildren()) {
		            for (int i = 0; i < experiment.getChildren().size(); i++) {
		                TmfTraceNode trace = (TmfTraceNode) experiment.getChildren().get(i);
		                if (trace.getResource().getLocation().equals(resource.getLocation())) {
		                    experiment.removeChild(trace);
		                    experiment.refresh();
		                    trace.getResource().delete(true, null);
		                }
		            }
		        }
		    }
			parent.removeChild(fTrace);
			parent.refresh();
			resource.delete(true, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return null;
	}

}
