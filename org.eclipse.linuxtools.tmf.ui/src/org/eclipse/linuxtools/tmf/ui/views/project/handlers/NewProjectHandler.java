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
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.TmfUiPreferenceInitializer;
import org.eclipse.linuxtools.tmf.ui.views.project.ProjectView;
import org.eclipse.linuxtools.tmf.ui.views.project.dialogs.NewProjectWizard;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfProjectRoot;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.prefs.BackingStoreException;

/**
 * <b><u>NewProjectHandler</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class NewProjectHandler extends AbstractHandler {

	private TmfProjectRoot fProjectRoot = null;

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
		IWorkbenchPart part = window.getActivePage().getActivePart();
		if (!(part instanceof ProjectView))
			return false;

		fProjectRoot = ((ProjectView) part).getRoot();

		return (fProjectRoot != null);
	}

	// ------------------------------------------------------------------------
	// Execution
	// ------------------------------------------------------------------------

	public Object execute(ExecutionEvent event) throws ExecutionException {

		// Fire the New Project Wizard
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		NewProjectWizard wizard = new NewProjectWizard();
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.open();

		// Update the project model
		if (dialog.getReturnCode() == Window.OK) {
			IProject project = wizard.getProject();
			if (project != null && fProjectRoot != null) {
                IEclipsePreferences node = new InstanceScope().getNode(TmfUiPlugin.PLUGIN_ID);
                node.put(TmfUiPreferenceInitializer.ACTIVE_PROJECT_PREFERENCE, project.getName());
                try {
                    node.flush();
                } catch (BackingStoreException e) {
                    e.printStackTrace();
                }
				fProjectRoot.refreshChildren();
				fProjectRoot.refresh();
			}
		}

		return null;
	}

}
