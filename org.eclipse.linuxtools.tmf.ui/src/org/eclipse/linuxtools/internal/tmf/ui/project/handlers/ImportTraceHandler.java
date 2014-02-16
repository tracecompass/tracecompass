/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Update selection handling
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace.ImportTraceWizard;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * <b><u>ImportTraceHandler</u></b>
 * <p>
 * Starts an ImportTraceWizard that will handle the lowly details.
 */
public class ImportTraceHandler extends AbstractHandler {

    // ------------------------------------------------------------------------
    // Execution
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ImportTraceWizard w = new ImportTraceWizard();
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        if (window == null) {
            return false;
        }

        ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
        // Menu Selection is only not null for context-sensitive menu
        ISelection menuSelection  = HandlerUtil.getActiveMenuSelection(event);

        IStructuredSelection sec = StructuredSelection.EMPTY;

        // Only use the selection if handler is called from context-sensitive menu
        if ((menuSelection != null) && (currentSelection instanceof IStructuredSelection)) {
            sec = (IStructuredSelection) currentSelection;
        }

        w.init(PlatformUI.getWorkbench(), sec);
        WizardDialog dialog = new WizardDialog(window.getShell(), w);
        dialog.open();
        return null;
    }
}
