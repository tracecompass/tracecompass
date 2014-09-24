/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>RefreshHandler</u></b>
 * <p>
 * TODO: Handle multiple selections
 */
public class RefreshHandler extends AbstractHandler {

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
        ISelection selection = part.getSite().getSelectionProvider().getSelection();
        if (selection instanceof TreeSelection) {
            TreeSelection treeSelection = (TreeSelection) selection;
            Object element = treeSelection.getFirstElement();
            IResource resource = null;
            if (element instanceof TmfTraceFolder) {
                TmfTraceFolder folder = (TmfTraceFolder) element;
                resource = folder.getResource();
            }
            else if (element instanceof TmfExperimentFolder) {
                TmfExperimentFolder folder = (TmfExperimentFolder) element;
                resource = folder.getResource();
            }
            else if (element instanceof TmfExperimentElement) {
                TmfExperimentElement folder = (TmfExperimentElement) element;
                resource = folder.getResource();
            }
            try {
                if (resource != null) {
                    resource.refreshLocal(IResource.DEPTH_INFINITE, null);
                }
            } catch (CoreException e) {
                Activator.getDefault().logError("Error refreshing projects", e); //$NON-NLS-1$
            }
        }


        return null;
    }

}
