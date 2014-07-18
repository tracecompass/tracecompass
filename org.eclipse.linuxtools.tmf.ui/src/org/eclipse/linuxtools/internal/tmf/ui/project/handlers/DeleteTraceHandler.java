/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Close editors to release resources
 *   Geneviève Bastien - Moved the delete code to element model's classes
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>DeleteTraceHandler</u></b>
 * <p>
 */
public class DeleteTraceHandler extends AbstractHandler {

    private TreeSelection fSelection = null;

    // ------------------------------------------------------------------------
    // Validation
    // ------------------------------------------------------------------------

    @Override
    public boolean isEnabled() {

        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return false;
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

        // Make sure selection contains only traces
        fSelection = null;
        if (selection instanceof TreeSelection) {
            fSelection = (TreeSelection) selection;
            Iterator<Object> iterator = fSelection.iterator();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (!(element instanceof TmfTraceElement)) {
                    return false;
                }
            }
        }

        // If we get here, either nothing is selected or everything is a trace
        return !selection.isEmpty();
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

        // Confirm the operation
        Shell shell = window.getShell();
        MessageBox confirmOperation = new MessageBox(shell, SWT.ICON_QUESTION | SWT.CANCEL | SWT.OK);
        confirmOperation.setText(Messages.DeleteDialog_Title);
        confirmOperation.setMessage(Messages.DeleteTraceHandler_Message);
        if (confirmOperation.open() != SWT.OK) {
            return null;
        }

        final Iterator<Object> iterator = fSelection.iterator();
        final int nbTraces = fSelection.size();

        SelectTraceOperation operation = new SelectTraceOperation() {
            @Override
            public void execute(IProgressMonitor monitor) throws CoreException {
                SubMonitor subMonitor = SubMonitor.convert(monitor, nbTraces);

                while (iterator.hasNext()) {
                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
                    Object element = iterator.next();
                    if (element instanceof TmfTraceElement) {
                        final TmfTraceElement trace = (TmfTraceElement) element;
                        subMonitor.setTaskName(Messages.DeleteTraceHandler_TaskName + " " + trace.getElementPath()); //$NON-NLS-1$
                        try {
                            trace.delete(null);
                        } catch (final CoreException e) {
                            Display.getDefault().asyncExec(new Runnable() {
                                @Override
                                public void run() {
                                    final MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
                                    mb.setText(Messages.DeleteTraceHandler_Error + ' ' + trace.getName());
                                    mb.setMessage(e.getMessage());
                                    mb.open();
                                }
                            });
                            Activator.getDefault().logError("Error deleting trace: " + trace.getName(), e); //$NON-NLS-1$
                        }
                    }
                    subMonitor.setTaskName(""); //$NON-NLS-1$
                    subMonitor.worked(1);
                }
           }
        };

        try {
            PlatformUI.getWorkbench().getProgressService().run(true, true, operation);
        } catch (InterruptedException e) {
            return null;
        } catch (InvocationTargetException e) {
            MessageDialog.openError(window.getShell(), e.toString(), e.getTargetException().toString());
            return null;
        }
        return null;
    }

    private abstract class SelectTraceOperation implements IRunnableWithProgress {
        @Override
        public synchronized final void run(IProgressMonitor monitor)
                throws InvocationTargetException, InterruptedException {
            final InvocationTargetException[] iteHolder = new InvocationTargetException[1];
            try {
                IWorkspaceRunnable workspaceRunnable = new IWorkspaceRunnable() {
                    @Override
                    public void run(IProgressMonitor pm) throws CoreException {
                        try {
                            execute(pm);
                        } catch (InvocationTargetException e) {
                            // Pass it outside the workspace runnable
                            iteHolder[0] = e;
                        } catch (InterruptedException e) {
                            // Re-throw as OperationCanceledException, which will be
                            // caught and re-thrown as InterruptedException below.
                            throw new OperationCanceledException(e.getMessage());
                        }
                        // CoreException and OperationCanceledException are propagated
                    }
                };

                IWorkspace workspace = ResourcesPlugin.getWorkspace();
                workspace.run(workspaceRunnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, monitor);
            } catch (CoreException e) {
                throw new InvocationTargetException(e);
            } catch (OperationCanceledException e) {
                throw new InterruptedException(e.getMessage());
            }
            // Re-throw the InvocationTargetException, if any occurred
            if (iteHolder[0] != null) {
                throw iteHolder[0];
            }
        }

        protected abstract void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException;
    }
}