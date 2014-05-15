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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.project.dialogs.SelectSupplementaryResourcesDialog;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfCommonProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

/**
 * Handler for Delete Supplementary Files command on trace
 */
public class DeleteTraceSupplementaryFilesHandler extends AbstractHandler {

    // ------------------------------------------------------------------------
    // Inner classes
    // ------------------------------------------------------------------------

    private class ElementComparator implements Comparator<TmfCommonProjectElement> {
        @Override
        public int compare(TmfCommonProjectElement e1, TmfCommonProjectElement e2) {
            return e1.getPath().toString().compareTo(e2.getPath().toString());
        }
    }

    private class ResourceComparator implements Comparator<IResource> {
        @Override
        public int compare(IResource r1, IResource r2) {
            return r1.getFullPath().toString().compareTo(r2.getFullPath().toString());
        }
    }

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
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (!(selection instanceof IStructuredSelection)) {
            return null;
        }
        final Multimap<TmfCommonProjectElement, IResource> resourceMap =
                TreeMultimap.create(new ElementComparator(), new ResourceComparator());
        final Iterator<Object> iterator = ((IStructuredSelection) selection).iterator();

        while (iterator.hasNext()) {
            Object element = iterator.next();
            if (element instanceof TmfTraceElement) {
                TmfTraceElement trace = (TmfTraceElement) element;
                // If trace is under an experiment, use the original trace from the traces folder
                trace = trace.getElementUnderTraceFolder();
                for (IResource resource : trace.getSupplementaryResources()) {
                    resourceMap.put(trace, resource);
                }

            } else if (element instanceof TmfExperimentElement) {
                TmfExperimentElement experiment = (TmfExperimentElement) element;
                for (IResource resource : experiment.getSupplementaryResources()) {
                    resourceMap.put(experiment, resource);
                }
                for (TmfTraceElement trace : experiment.getTraces()) {
                    // If trace is under an experiment, use the original trace from the traces folder
                    trace = trace.getElementUnderTraceFolder();
                    for (IResource resource : trace.getSupplementaryResources()) {
                        resourceMap.put(trace, resource);
                    }
                }
            }
        }

        SelectSupplementaryResourcesDialog dialog =
                new SelectSupplementaryResourcesDialog(window.getShell(), resourceMap);
        if (dialog.open() != Window.OK) {
            return null;
        }

        Set<IProject> projectsToRefresh = new HashSet<>();

        // Delete the resources that were selected
        List<IResource> allResourcesToDelete = Arrays.asList(dialog.getResources());
        for (TmfCommonProjectElement element : resourceMap.keySet()) {
            List<IResource> traceResourcesToDelete = new ArrayList<>(resourceMap.get(element));
            traceResourcesToDelete.retainAll(allResourcesToDelete);
            if (!traceResourcesToDelete.isEmpty()) {
                // Delete the selected resources
                element.closeEditors();
                element.deleteSupplementaryResources(traceResourcesToDelete.toArray(new IResource[0]));
                projectsToRefresh.add(element.getProject().getResource());
            }
        }

        // Refresh projects
        Iterator<IProject> projectIterator = projectsToRefresh.iterator();
        while (projectIterator.hasNext()) {
            IProject project = projectIterator.next();
            try {
                project.refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (CoreException e) {
                Activator.getDefault().logError("Error refreshing project " + project, e); //$NON-NLS-1$
            }
        }

        return null;
    }

}
