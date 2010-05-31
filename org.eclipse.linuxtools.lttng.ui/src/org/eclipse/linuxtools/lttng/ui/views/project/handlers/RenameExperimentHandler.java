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
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngExperimentNode;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>RenameExperimentHandler</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class RenameExperimentHandler implements IHandler {

	private LTTngExperimentNode fExperiment = null;

	// ------------------------------------------------------------------------
	// Validation
	// ------------------------------------------------------------------------

	public boolean isEnabled() {
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

		MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		mb.setText("Rename Experiment");
		mb.setMessage("Not implemented yet");
		mb.open();

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
