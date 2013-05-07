/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.project.dialogs.SelectSupplementaryResourcesDialog;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Handler for Delete Supplementary Files command on experiment
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
        if (part == null) {
            return false;
        }
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
            List<IResource> resourcesList = new ArrayList<IResource>();

            if (element instanceof TmfExperimentElement) {

                TmfExperimentElement experiment = (TmfExperimentElement) element;

                IResource[] resources = experiment.getSupplementaryResources();
                resourcesList.addAll(Arrays.asList(resources));

                // Map to know which trace to close for each resource
                HashMap<IResource, TmfTraceElement> traceMap = new HashMap<IResource, TmfTraceElement>();

                for (TmfTraceElement aTrace : experiment.getTraces()) {

                    // If trace is under an experiment, use the original trace from the traces folder
                    aTrace = aTrace.getElementUnderTraceFolder();

                    // Add the trace supplementary resources for the dialog
                    resources = aTrace.getSupplementaryResources();
                    resourcesList.addAll(Arrays.asList(resources));

                    for (IResource resource : resources) {
                        traceMap.put(resource, aTrace);
                    }
                }

                SelectSupplementaryResourcesDialog dialog = new SelectSupplementaryResourcesDialog(window.getShell(), resourcesList.toArray(new IResource[resourcesList.size()]));

                if (dialog.open() != Window.OK) {
                    return null;
                }

                IResource[] resourcesToDelete = dialog.getResources();

                // Delete the selected resources
                for (IResource resource : resourcesToDelete) {
                    traceMap.get(resource).closeEditors();
                    try {
                        resource.delete(true, new NullProgressMonitor());
                    } catch (CoreException e) {
                        Activator.getDefault().logError("Error deleting supplementary resource " + resource, e); //$NON-NLS-1$
                    }
                }

                IResource resource = experiment.getProject().getResource();
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
