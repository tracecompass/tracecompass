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

package org.eclipse.linuxtools.lttng.ui.views.project.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngExperimentEntry;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>DeleteExperimentActionDelegate</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class DeleteExperimentActionDelegate implements IWorkbenchWindowActionDelegate {

	public static final String ID = "org.eclipse.linuxtools.lttng.ui.view.project.menu.deleteExperiment";

	@SuppressWarnings("unused")
	private IWorkbenchWindow fWindow;
	private IStructuredSelection fSelection;
	private LTTngExperimentEntry fEntry;

	/**
	 * 
	 */
	public DeleteExperimentActionDelegate() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		fWindow = window;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		Shell shell = workbench.getActiveWorkbenchWindow().getShell();
		MessageDialog dialog = new MessageDialog(shell, "Delete Experiment", null,
				"Sorry, this feature is not implemented yet.\n\n" +
				"In the mean time, you can use the standard Eclipse Navigator View.",
				MessageDialog.INFORMATION, new String[] { "OK" }, 0);
		dialog.open();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			fSelection = (IStructuredSelection) selection;
			fEntry = null;
			Object sel = fSelection.getFirstElement();
			if (sel instanceof LTTngExperimentEntry) {
				fEntry = (LTTngExperimentEntry) sel;
			}
		}
	}

}
