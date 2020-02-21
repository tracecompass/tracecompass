/*******************************************************************************
 * Copyright (c) 2012, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Patrick Tasse - Close editors to release resources
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfUIPreferences;
import org.eclipse.tracecompass.internal.tmf.ui.project.dialogs.SelectSupplementaryResourcesDialog;
import org.eclipse.tracecompass.internal.tmf.ui.project.operations.TmfWorkspaceModifyOperation;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfCommonProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
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

    private static final Comparator<TmfCommonProjectElement> ELEMENT_COMPARATOR = Comparator.comparing(e -> e.getPath().toString());
    private static final Comparator<IResource> RESOURCE_COMPARATOR = Comparator.comparing(e -> e.getFullPath().toString());

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
        final Multimap<TmfCommonProjectElement, IResource> resourceMap = TreeMultimap.create(ELEMENT_COMPARATOR, RESOURCE_COMPARATOR);
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

        boolean confirm = Activator.getDefault().getPreferenceStore().getBoolean(ITmfUIPreferences.CONFIRM_DELETION_SUPPLEMENTARY_FILES);
        if (confirm) {
            final SelectSupplementaryResourcesDialog dialog = new SelectSupplementaryResourcesDialog(window.getShell(), resourceMap);
            if (dialog.open() != Window.OK) {
                return null;
            }
            // Only keep selected resource in the map
            resourceMap.values().retainAll(Arrays.asList(dialog.getResources()));
        }

        // Close editors for traces that have supplementary resources to delete
        resourceMap.keys().forEach(TmfCommonProjectElement::closeEditors);

        TmfWorkspaceModifyOperation operation = new TmfWorkspaceModifyOperation() {
            @Override
            public void execute(IProgressMonitor monitor) throws CoreException {

                Set<IProject> projectsToRefresh = new HashSet<>();

                // Initial work is number of resources to delete plus number of traces
                SubMonitor subMonitor = SubMonitor.convert(monitor, resourceMap.size() + resourceMap.keySet().size());

                for (Entry<TmfCommonProjectElement, Collection<IResource>> entry : resourceMap.asMap().entrySet()) {
                    TmfCommonProjectElement trace = entry.getKey();
                    Collection<IResource> resources = entry.getValue();
                    subMonitor.split(resources.size());
                    subMonitor.setTaskName(NLS.bind(Messages.DeleteSupplementaryFiles_DeletionTask, trace.getElementPath()));
                    trace.deleteSupplementaryResources(resources.toArray(new IResource[0]));
                    projectsToRefresh.add(trace.getProject().getResource());
                }

                // Redistribute work remaining from number of traces to number of projects
                subMonitor.setWorkRemaining(projectsToRefresh.size());

                // Refresh projects
                for (IProject project : projectsToRefresh) {
                    subMonitor.setTaskName(NLS.bind(Messages.DeleteSupplementaryFiles_ProjectRefreshTask, project.getName()));
                    try {
                        project.refreshLocal(IResource.DEPTH_INFINITE, subMonitor.split(1));
                    } catch (CoreException e) {
                        Activator.getDefault().logError("Error refreshing project " + project, e); //$NON-NLS-1$
                    }
                }
           }
        };

        try {
            PlatformUI.getWorkbench().getProgressService().run(true, true, operation);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (InvocationTargetException e) {
            TraceUtils.displayErrorMsg(e.toString(), e.getTargetException().toString());
        }
        return null;
    }

}
