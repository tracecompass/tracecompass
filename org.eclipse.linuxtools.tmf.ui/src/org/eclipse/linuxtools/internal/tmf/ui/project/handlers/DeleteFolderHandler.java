/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTracesFolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for the Delete Folder command.
 */
public class DeleteFolderHandler extends AbstractHandler {

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
        final boolean isTracesFolder = ((IStructuredSelection) selection).iterator().next() instanceof TmfTracesFolder;

        // Confirm the operation
        Shell shell = window.getShell();
        MessageBox confirmOperation = new MessageBox(shell, SWT.ICON_QUESTION | SWT.CANCEL | SWT.OK);
        confirmOperation.setText(isTracesFolder ? Messages.ClearDialog_Title : Messages.DeleteDialog_Title);
        confirmOperation.setMessage(isTracesFolder ? Messages.DeleteFolderHandlerClear_Message : Messages.DeleteFolderHandler_Message);
        if (confirmOperation.open() != SWT.OK) {
            return null;
        }

        final Iterator<Object> iterator = ((IStructuredSelection) selection).iterator();

        WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
            @Override
            public void execute(final IProgressMonitor monitor) throws CoreException {
                monitor.beginTask("", 1000); //$NON-NLS-1$
                while (iterator.hasNext()) {
                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
                    Object element = iterator.next();
                    if (element instanceof TmfTraceFolder) {
                        final TmfTraceFolder folder = (TmfTraceFolder) element;
                        final IResource resource = folder.getResource();

                        try {
                            // delete all traces under this folder
                            for (TmfTraceElement traceElement : folder.getTraces()) {
                                traceElement.delete(null);
                            }

                            // Finally, delete the folder. For the Traces
                            // folder, we only delete the children since the
                            // folder should always be there.
                            if (folder instanceof TmfTracesFolder) {
                                resource.accept(new IResourceVisitor() {
                                    @Override
                                    public boolean visit(IResource visitedResource) throws CoreException {
                                        if (visitedResource != resource) {
                                            visitedResource.delete(true, monitor);
                                        }
                                        return true;
                                    }
                                }, IResource.DEPTH_ONE, 0);
                            } else {
                                resource.delete(true, monitor);
                            }
                        } catch (final CoreException e) {
                            Display.getDefault().asyncExec(new Runnable() {
                                @Override
                                public void run() {
                                    final MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
                                    mb.setText(isTracesFolder ? Messages.DeleteFolderHandlerClear_Error : Messages.DeleteFolderHandler_Error + ' ' + folder.getName());
                                    mb.setMessage(e.getMessage());
                                    mb.open();
                                }
                            });
                            Activator.getDefault().logError("Error deleting folder: " + folder.getName(), e); //$NON-NLS-1$
                        }
                    }
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

        return null;
    }

}
