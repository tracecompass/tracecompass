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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.tmf.ui.project.operations.TmfWorkspaceModifyOperation;
import org.eclipse.linuxtools.tmf.core.synchronization.TimestampTransformFactory;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Clear Trace Offset Handler
 *
 * @author Patrick Tasse
 */
public class ClearTraceOffsetHandler extends AbstractHandler {

    // ------------------------------------------------------------------------
    // Execution
    // ------------------------------------------------------------------------

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {

        ISelection selection = HandlerUtil.getCurrentSelection(event);

        // Get the set of selected trace elements
        final Set<TmfTraceElement> traceElements = new HashSet<>();
        if (selection instanceof StructuredSelection) {
            Iterator<Object> iterator = ((StructuredSelection) selection).iterator();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (element instanceof TmfTraceElement) {
                    TmfTraceElement trace = (TmfTraceElement) element;
                    traceElements.add(trace.getElementUnderTraceFolder());
                } else if (element instanceof TmfExperimentElement) {
                    TmfExperimentElement exp = (TmfExperimentElement) element;
                    for (TmfTraceElement trace : exp.getTraces()) {
                        traceElements.add(trace.getElementUnderTraceFolder());
                    }
                } else if (element instanceof TmfTraceFolder) {
                    TmfTraceFolder folder = (TmfTraceFolder) element;
                    traceElements.addAll(folder.getTraces());
                }
            }
        }

        Shell shell = HandlerUtil.getActiveShellChecked(event);
        MessageBox mb = new MessageBox(shell, SWT.ICON_QUESTION | SWT.CANCEL | SWT.OK);
        mb.setText(Messages.ClearTraceOffsetHandler_Title);
        mb.setMessage(Messages.ClearTraceOffsetHandler_ConfirmMessage);
        if (mb.open() != SWT.OK) {
            return null;
        }

        TmfWorkspaceModifyOperation operation = new TmfWorkspaceModifyOperation() {
            @Override
            public void execute(IProgressMonitor monitor) throws CoreException {
                for (final TmfTraceElement trace : traceElements) {
                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
                    if (!TimestampTransformFactory.getTimestampTransform(trace.getResource()).equals(TimestampTransformFactory.getDefaultTransform())) {
                        Display.getDefault().syncExec(new Runnable() {
                            @Override
                            public void run() {
                                trace.closeEditors();
                            }
                        });
                        trace.deleteSupplementaryResources();
                        TimestampTransformFactory.setTimestampTransform(trace.getResource(), null);
                        trace.refreshSupplementaryFolder();
                    }
                }
            }
        };
        try {
            PlatformUI.getWorkbench().getProgressService().run(true, true, operation);
        } catch (InterruptedException e) {
            return null;
        } catch (InvocationTargetException e) {
            MessageDialog.openError(shell, e.toString(), e.getTargetException().toString());
            return null;
        }

        return null;
    }
}
