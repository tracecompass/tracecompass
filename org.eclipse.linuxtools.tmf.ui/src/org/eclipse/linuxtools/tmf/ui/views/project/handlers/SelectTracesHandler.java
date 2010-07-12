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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.tmf.ui.views.project.ProjectView;
import org.eclipse.linuxtools.tmf.ui.views.project.dialogs.AddTraceWizard;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfExperimentNode;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>SelectTracesHandler</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class SelectTracesHandler extends AbstractHandler {

	private TmfExperimentNode fExperiment = null;

	// ------------------------------------------------------------------------
	// Validation
	// ------------------------------------------------------------------------

	public boolean isEnabled() {

		// Check if we are closing down
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
			return false;

		// Check if we are in the Project View
		IWorkbenchPage page = window.getActivePage();
		if (!(page.getActivePart() instanceof ProjectView))
			return false;

		// Check that an experiment is selected
		ISelection selection = page.getSelection(ProjectView.ID);
		if (selection instanceof StructuredSelection) {
			Object element = ((StructuredSelection) selection).getFirstElement();
			fExperiment = (element instanceof TmfExperimentNode) ? (TmfExperimentNode) element : null;
		}

		return (fExperiment != null);
	}

	// ------------------------------------------------------------------------
	// Execution
	// ------------------------------------------------------------------------

	public Object execute(ExecutionEvent event) throws ExecutionException {

		// Open the trace addition wizard
		IWorkbench workbench = PlatformUI.getWorkbench();
		Shell shell = workbench.getActiveWorkbenchWindow().getShell();

		AddTraceWizard wizard = new AddTraceWizard(fExperiment.getProject(), fExperiment);
		wizard.init(PlatformUI.getWorkbench(), null);
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.open();

		return null;
	}

}
