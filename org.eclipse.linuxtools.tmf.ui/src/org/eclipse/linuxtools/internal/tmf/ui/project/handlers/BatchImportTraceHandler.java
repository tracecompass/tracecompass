/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace.BatchImportTraceWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Batch import handler, spawn a wizard
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class BatchImportTraceHandler extends ImportTraceHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // Fire the Import Trace Wizard
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null) {
            return null;
        }

        final IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
        if (activeWorkbenchWindow == null) {
            return null;
        }

        IStructuredSelection selection = new StructuredSelection(HandlerUtil.getCurrentSelection(event));
        TmfTraceFolder traceFolder = getTraceFolder();
        if (traceFolder != null) {
            selection = new StructuredSelection(traceFolder);
        }

        BatchImportTraceWizard wizard = new BatchImportTraceWizard();
        wizard.init(PlatformUI.getWorkbench(), selection);
        WizardDialog dialog = new WizardDialog(activeWorkbenchWindow.getShell(), wizard);
        dialog.open();

        if (traceFolder != null) {
            traceFolder.refresh();
        }

        return null;
    }

}
