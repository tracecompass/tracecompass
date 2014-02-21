/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.importexport;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for importing a trace package
 *
 * @author Marc-Andre Laperle
 */
public class ImportTracePackageHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ImportTracePackageWizard w = new ImportTracePackageWizard();
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        if (window == null) {
            return Boolean.FALSE;
        }

        ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
        IStructuredSelection sec = StructuredSelection.EMPTY;
        if (currentSelection instanceof IStructuredSelection) {
            sec = (IStructuredSelection) currentSelection;
        }

        w.init(PlatformUI.getWorkbench(), sec);
        WizardDialog dialog = new WizardDialog(window.getShell(), w);
        dialog.open();
        return null;
    }
}
