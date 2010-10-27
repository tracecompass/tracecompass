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
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfTraceNode;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>RenameTraceHandler</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class RenameTraceHandler extends AbstractHandler {

	private TmfTraceNode fTrace = null;

	// ------------------------------------------------------------------------
	// Validation
	// ------------------------------------------------------------------------

	@Override
	public boolean isEnabled() {
		return (fTrace != null);
	}

	// ------------------------------------------------------------------------
	// Execution
	// ------------------------------------------------------------------------

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		mb.setText("Rename Trace");
		mb.setMessage("Not implemented yet");
		mb.open();

		return null;
	}

}
