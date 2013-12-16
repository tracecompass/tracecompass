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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace.BatchImportTraceWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Batch import handler, spawn a wizard
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class BatchImportTraceHandler extends AbstractHandler {

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

        return null;
    }

    /**
     * @return the trace folder or null
     */
    private static TmfTraceFolder getTraceFolder() {
        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }

        // Get the selection
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IWorkbenchPart part = page.getActivePart();
        if (part == null) {
            return null;
        }
        ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
        if (selectionProvider == null) {
            return null;
        }
        ISelection selection = selectionProvider.getSelection();

        TmfTraceFolder traceFolder = null;
        if (selection instanceof TreeSelection) {
            TreeSelection sel = (TreeSelection) selection;
            // There should be only one item selected as per the plugin.xml
            Object element = sel.getFirstElement();
            if (element instanceof TmfTraceFolder) {
                traceFolder = (TmfTraceFolder) element;
            }
        }
        return traceFolder;
    }

}
