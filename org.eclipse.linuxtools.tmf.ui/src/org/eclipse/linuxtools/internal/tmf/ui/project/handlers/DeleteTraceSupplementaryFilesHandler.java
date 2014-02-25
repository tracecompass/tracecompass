/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Patrick Tasse - Close editors to release resources
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.project.dialogs.SelectSupplementaryResourcesDialog;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Handler for Delete Supplementary Files command on trace
 */
public class DeleteTraceSupplementaryFilesHandler extends AbstractHandler {

    // ------------------------------------------------------------------------
    // Execution
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }

        // Get the selection
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IWorkbenchPart part = page.getActivePart();
        if (part == null) {
            return Boolean.FALSE;
        }
        ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
        if (selectionProvider == null) {
            return Boolean.FALSE;
        }

        ISelection selection = selectionProvider.getSelection();

        // Make sure there is only selection and that it is an experiment
        if (selection instanceof TreeSelection) {
            TreeSelection sel = (TreeSelection) selection;
            // There should be only one item selected as per the plugin.xml
            Object element = sel.getFirstElement();
            if (element instanceof TmfTraceElement) {
                TmfTraceElement trace = (TmfTraceElement) element;
                // If trace is under an experiment, use the original trace from the traces folder
                trace = trace.getElementUnderTraceFolder();

                // Delete the selected resources
                IResource[] resources = trace.getSupplementaryResources();

                SelectSupplementaryResourcesDialog dialog = new SelectSupplementaryResourcesDialog(window.getShell(), resources);
                if (dialog.open() != Window.OK) {
                    return null;
                }

                trace.closeEditors();
                trace.deleteSupplementaryResources(dialog.getResources());

                // Refresh project
                IResource resource = trace.getProject().getResource();

                if (resource != null) {
                    try {
                        resource.refreshLocal(IResource.DEPTH_INFINITE, null);
                    } catch (CoreException e) {
                        Activator.getDefault().logError("Error refreshing resource " + resource, e); //$NON-NLS-1$
                    }
                }
            }
        }

        return null;
    }

}
