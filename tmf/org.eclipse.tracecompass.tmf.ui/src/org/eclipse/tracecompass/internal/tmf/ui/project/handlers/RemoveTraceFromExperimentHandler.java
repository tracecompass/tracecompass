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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

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
        MessageDialog dialog = new MessageDialog(
                shell,
                Messages.RemoveDialog_Title,
                null,
                Messages.RemoveTraceFromExperimentHandler_Message,
                MessageDialog.QUESTION,
                new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL },
                1);
        if (dialog.open() != Window.OK) {
            return null;
        }

        IRunnableWithProgress operation = monitor -> {
            Multimap<TmfProjectElement, TmfTraceElement> tracesToRemove = LinkedHashMultimap.create();
            Set<TmfExperimentElement> experiments = new LinkedHashSet<>();
            for (Object element : ((IStructuredSelection) selection).toList()) {
                if (element instanceof TmfTraceElement) {
                    TmfTraceElement traceElement = (TmfTraceElement) element;
                    tracesToRemove.put(traceElement.getProject(), traceElement);
                    if (traceElement.getParent() instanceof TmfExperimentElement) {
                        experiments.add((TmfExperimentElement) traceElement.getParent());
                    }
                }
            }

            // Close the experiments to be modified
            monitor.setTaskName(Messages.CloseTraces_TaskName);
            experiments.forEach(experiment -> {
                Display.getDefault().syncExec(experiment::closeEditors);
            });

            SubMonitor subMon = SubMonitor.convert(monitor, tracesToRemove.size());
            subMon.setTaskName(Messages.RemoveTraceFromExperimentHandler_TaskName);
            for (Entry<TmfProjectElement, Collection<TmfTraceElement>> entry : tracesToRemove.asMap().entrySet()) {
                IProject project = entry.getKey().getResource();
                try {
                    ResourcesPlugin.getWorkspace().run(mon -> {
                        for (TmfTraceElement traceElement : entry.getValue()) {
                            traceElement.delete(subMon.split(1), false, false);
                        }
                        subMon.subTask(""); //$NON-NLS-1$
                    }, project, IWorkspace.AVOID_UPDATE, null);
                } catch (CoreException e) {
                    throw new InvocationTargetException(e);
                }
            }
        };

        try {
            PlatformUI.getWorkbench().getProgressService().run(true, true, operation);
        } catch (InterruptedException e) {
            return null;
        } catch (InvocationTargetException e) {
            TraceUtils.displayErrorMsg(Messages.RemoveTraceFromExperimentHandler_Error, e.getTargetException().toString(), e.getTargetException());
            return null;
        }
        return null;
    }

}
