/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.project.operations.TmfWorkspaceModifyOperation;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * An handler for removal of traces from under experiments.
 */
public class RemoveTraceFromExperimentHandler extends AbstractHandler {

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

        // Confirm the operation
        Shell shell = window.getShell();
        MessageDialog dialog = new MessageDialog(shell, Messages.RemoveDialog_Title, null,
                Messages.RemoveTraceFromExperimentHandler_Message, MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL,
                        IDialogConstants.NO_LABEL },
                1) {
            {
                setShellStyle(SWT.SHEET);
            }
        };
        if (dialog.open() != Window.OK) {
            return null;
        }

        final List<Object> list = ((IStructuredSelection) selection).toList();
        final int nbElements = list.size();

        TmfWorkspaceModifyOperation operation = new TmfWorkspaceModifyOperation() {
            @Override
            public void execute(IProgressMonitor monitor) throws CoreException {
                SubMonitor subMonitor = SubMonitor.convert(monitor, nbElements);

                for (Object element : list) {
                    IProgressMonitor elementSubMonitor = subMonitor.split(1);
                    if (element instanceof TmfTraceElement) {
                        final TmfTraceElement trace = (TmfTraceElement) element;
                        if (!trace.getResource().exists()) {
                            continue;
                        }
                        subMonitor.setTaskName(Messages.RemoveTraceFromExperimentHandler_TaskName + " " + trace.getElementPath()); //$NON-NLS-1$
                        try {
                            trace.delete(elementSubMonitor);
                        } catch (final CoreException e) {
                            Display.getDefault().asyncExec(() -> ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                                    null,
                                    null,
                                    new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, Messages.RemoveTraceFromExperimentHandler_Error + ' ' + trace.getName(), e)));
                            Activator.getDefault().logError(Messages.RemoveTraceFromExperimentHandler_Error + trace.getName(), e);
                        }
                    }
                    subMonitor.setTaskName(""); //$NON-NLS-1$
                }
           }
        };

        try {
            PlatformUI.getWorkbench().getProgressService().run(true, true, operation);
        } catch (InterruptedException e) {
            return null;
        } catch (InvocationTargetException e) {
            TraceUtils.displayErrorMsg(e.toString(), e.getTargetException().toString());
            return null;
        }
        return null;
    }

}
