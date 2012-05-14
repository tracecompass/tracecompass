/*******************************************************************************
 * Copyright (c) 2009, 2010, 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.internal.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>DeleteExperimentSupplementaryFilesHandler</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class DeleteExperimentSupplementaryFilesHandler extends AbstractHandler {

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
        ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
        if (selectionProvider == null) {
            return false;
        }

        ISelection selection = selectionProvider.getSelection();

        // Make sure there is only selection and that it is an experiment
        if (selection instanceof TreeSelection) {
            TreeSelection sel = (TreeSelection) selection;
            // There should be only one item selected as per the plugin.xml
            Object element = sel.getFirstElement();
            if (element instanceof TmfExperimentElement) {

                TmfExperimentElement trace = (TmfExperimentElement) element;

                for (TmfTraceElement aTrace : trace.getTraces()) {
                    
                    // If trace is under an experiment, use the original trace from the traces folder
                    aTrace = aTrace.getElementUnderTraceFolder();
                    
                    aTrace.deleteSupplementaryFiles();
                }

                IResource resource = trace.getProject().getResource();
                if (resource != null) {
                    try {
                        if (resource != null) {
                            resource.refreshLocal(IResource.DEPTH_INFINITE, null);
                        }
                    } catch (CoreException e) {
                        TmfUiPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, TmfUiPlugin.PLUGIN_ID, "Error refreshing resource " + resource, e)); //$NON-NLS-1$
                    }
                }
            }
        }
        return null;
    }
}
