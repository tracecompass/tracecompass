/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.tracecompass.tmf.ui.project.wizards.RenameFolderDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for the Rename Folder command.
 */
public class RenameFolderHandler extends AbstractHandler {

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

        ISelection selection = HandlerUtil.getCurrentSelection(event);
        TmfTraceFolder selectedFolder = null;
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof TmfTraceFolder) {
                selectedFolder = (TmfTraceFolder) element;
            }
        }
        if (selectedFolder == null) {
            return null;
        }
        final TmfTraceFolder oldFolder = selectedFolder;

        // Fire the Rename Folder dialog
        RenameFolderDialog dialog = new RenameFolderDialog(window.getShell(), oldFolder);
        dialog.open();

        if (dialog.getReturnCode() != Window.OK) {
            return null;
        }

        final String newName = (String) dialog.getFirstResult();

        IContainer parentFolder = oldFolder.getResource().getParent();
        final TmfTraceFolder tracesFolder = oldFolder.getProject().getTracesFolder();
        final IPath newFolderPath = parentFolder.getFullPath().append(newName);
        TmfExperimentFolder experimentFolder = oldFolder.getProject().getExperimentsFolder();
        // Not a standard tracing project
        if ((tracesFolder == null) || (experimentFolder == null)) {
            return null;
        }

        WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
            @Override
            public void execute(IProgressMonitor monitor) throws CoreException {
                try {
                    monitor.beginTask("", 1000); //$NON-NLS-1$
                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }

                    for (final TmfTraceElement traceElement : oldFolder.getTraces()) {
                        Display.getDefault().syncExec(traceElement::closeEditors);

                        IPath relativePath = traceElement.getPath().makeRelativeTo(oldFolder.getPath());
                        String newElementPath = newFolderPath.makeRelativeTo(tracesFolder.getPath()).append(relativePath).toString();
                        traceElement.renameSupplementaryFolder(newElementPath);
                    }

                    oldFolder.getResource().move(newFolderPath, IResource.FORCE | IResource.SHALLOW, monitor);
                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
                } finally {
                    monitor.done();
                }
            }
        };

        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(operation);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (InvocationTargetException e) {
            TraceUtils.displayErrorMsg(e.toString(), e.getTargetException().toString());
            return null;
        }

        /* We need to split the WorkspaceModifyOperation so that the new model
         * elements get created by the resource changed event */
        operation = new WorkspaceModifyOperation() {
            @Override
            protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
                IPath oldFolderElementPath = oldFolder.getPath().makeRelativeTo(tracesFolder.getPath());
                IPath newFolderElementPath = oldFolderElementPath.removeLastSegments(1).append(newName);
                Set<IFolder> affectedExperiments = new HashSet<>();
                for (TmfExperimentElement experiment : experimentFolder.getExperiments()) {
                    for (TmfTraceElement oldTrace : experiment.getTraces()) {
                        if (oldTrace.getElementPath().startsWith(oldFolderElementPath.toString())) {
                            experiment.removeTrace(oldTrace);
                            String relativePath = oldTrace.getElementPath().substring(oldFolderElementPath.toString().length() + 1);
                            String newTraceElementPath = newFolderElementPath.append(relativePath).toString();
                            for (TmfTraceElement newTrace : tracesFolder.getTraces()) {
                                if (newTrace.getElementPath().equals(newTraceElementPath)) {
                                    affectedExperiments.add(experiment.getResource());
                                    experiment.addTrace(newTrace);
                                    break;
                                }
                            }
                        }
                    }
                }

                // --- Begin workaround for platform bug 525833 ---
                // Delete directory under Traces folder created by platform
                IResource resourceToDelete = oldFolder.getResource();
                if (resourceToDelete.exists()) {
                    resourceToDelete.delete(true, monitor);
                }
                // Delete directories under Experiment folder created by platform
                for (IFolder experiment : affectedExperiments) {
                    IFolder expResourceToDelete = experiment.getFolder(oldFolderElementPath);
                    if (expResourceToDelete.exists()) {
                        expResourceToDelete.delete(true, monitor);
                    }
                }
                // --- End workaround for platform bug 525833 ---
            }
        };

        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(operation);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (InvocationTargetException e) {
            TraceUtils.displayErrorMsg(e.toString(), e.getTargetException().toString());
        }

        return null;
    }

}
