/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Add support for folder elements
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.linuxtools.tmf.ui.project.wizards.RenameTraceDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for the Rename Trace command.
 */
public class RenameTraceHandler extends AbstractHandler {

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
        TmfTraceElement selectedTrace = null;
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof TmfTraceElement) {
                selectedTrace = (TmfTraceElement) element;
            }
        }
        if (selectedTrace == null) {
            return null;
        }

        // If trace is under an experiment, use the original trace from the traces folder
        final TmfTraceElement oldTrace = selectedTrace.getElementUnderTraceFolder();

        RenameTraceDialog dialog = new RenameTraceDialog(window.getShell(), oldTrace);
        if (dialog.open() != Window.OK) {
            return null;
        }

        final TmfTraceFolder traceFolder = (TmfTraceFolder) oldTrace.getParent();
        final String newName = (String) dialog.getFirstResult();

        IFolder parentFolder = (IFolder) oldTrace.getParent().getResource();
        final TmfTraceFolder tracesFolder = oldTrace.getProject().getTracesFolder();
        final IPath newPath = parentFolder.getFullPath().append(newName);

        WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
            @Override
            public void execute(IProgressMonitor monitor) throws CoreException {
                try {
                    monitor.beginTask("", 1000); //$NON-NLS-1$
                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
                    // Close the trace if open
                    oldTrace.closeEditors();

                    if (oldTrace.getResource() instanceof IFolder) {
                        IFolder folder = (IFolder) oldTrace.getResource();
                        IFile bookmarksFile = oldTrace.getBookmarksFile();
                        if (bookmarksFile.exists()) {
                            IFile newBookmarksFile = folder.getFile(bookmarksFile.getName().replace(oldTrace.getName(), newName));
                            if (!newBookmarksFile.exists()) {
                                IPath newBookmarksPath = newBookmarksFile.getFullPath();
                                bookmarksFile.move(newBookmarksPath, IResource.FORCE | IResource.SHALLOW, monitor);
                            }
                        }
                    }

                    String newElementPath = newPath.makeRelativeTo(tracesFolder.getPath()).toString();
                    oldTrace.renameSupplementaryFolder(newElementPath);
                    oldTrace.getResource().move(newPath, IResource.FORCE | IResource.SHALLOW, monitor);
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
            return null;
        } catch (InvocationTargetException e) {
            MessageDialog.openError(window.getShell(), e.toString(), e.getTargetException().toString());
            return null;
        }

        /* We need to split the WorkspaceModifyOperation so that the new model
         * elements get created by the resource changed event */
        operation = new WorkspaceModifyOperation() {
            @Override
            protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {

                // Locate the new trace object
                TmfTraceElement newTrace = null;
                String newElementPath = oldTrace.getParent().getPath().append(newName).makeRelativeTo(tracesFolder.getPath()).toString();
                for (TmfTraceElement element : traceFolder.getTraces()) {
                    if (element.getElementPath().equals(newElementPath)) {
                        newTrace = element;
                        break;
                    }
                }
                if (newTrace == null) {
                    return;
                }

                TmfExperimentFolder experimentFolder = newTrace.getProject().getExperimentsFolder();
                for (final TmfExperimentElement experiment : experimentFolder.getExperiments()) {
                    for (final TmfTraceElement expTrace : experiment.getTraces()) {
                        if (expTrace.getElementPath().equals(oldTrace.getElementPath())) {
                            experiment.removeTrace(expTrace);
                            experiment.addTrace(newTrace);
                        }
                    }
                }
            }
        };

        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(operation);
        } catch (InterruptedException e) {
        } catch (InvocationTargetException e) {
            MessageDialog.openError(window.getShell(), e.toString(), e.getTargetException().toString());
        }

        return null;
    }
}
