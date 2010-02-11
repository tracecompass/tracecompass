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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>DeleteProjectActionDelegate</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class DeleteProjectActionDelegate implements IWorkbenchWindowActionDelegate {

	public static final String ID = "org.eclipse.linuxtools.lttng.ui.view.project.menu.deleteProject";

	/**
	 * 
	 */
	public DeleteProjectActionDelegate() {
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
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		// Open the project creation wizard
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

		String title = "Delete Project";
		String message = "Are you sure you want to delete TBD";
		String ok = "OK";
		String cancel = " No, thanks.";
		MessageDialog dialog = new MessageDialog(shell,	title, null, message,
				MessageDialog.QUESTION, new String[] { ok, cancel }, 1);
		int rc = dialog.open();
		if (rc == 0) {
			System.out.println("Delete project");
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
	}

}
