/*******************************************************************************
 * Copyright (c) 2009, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Close editors to release resources
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map.Entry;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * <b><u>DeleteExperimentHandler</u></b>
 * <p>
 */
public class DeleteExperimentHandler extends AbstractHandler {

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
        confirmOperation.setMessage(Messages.DeleteExperimentHandler_Message);
        if (confirmOperation.open() != SWT.OK) {
            return null;
        }

        // Get the selection
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (!(selection instanceof IStructuredSelection)) {
            return null;
        }
        IRunnableWithProgress operation = monitor -> {
            Multimap<TmfProjectElement, TmfExperimentElement> experimentsToDelete = LinkedHashMultimap.create();
            for (Object element : ((IStructuredSelection) selection).toList()) {
                if (element instanceof TmfExperimentElement) {
                    TmfExperimentElement experimentElement = (TmfExperimentElement) element;
                    experimentsToDelete.put(experimentElement.getProject(), experimentElement);
                }
            }

            // Close the experiments to be deleted
            monitor.setTaskName(Messages.CloseTraces_TaskName);
            experimentsToDelete.values().forEach(experiment -> {
                Display.getDefault().syncExec(experiment::closeEditors);
            });

            SubMonitor subMon = SubMonitor.convert(monitor, experimentsToDelete.size());
            subMon.setTaskName(Messages.DeleteExperimentHandler_TaskName);
            for (Entry<TmfProjectElement, Collection<TmfExperimentElement>> entry : experimentsToDelete.asMap().entrySet()) {
                IProject project = entry.getKey().getResource();
                try {
                    ResourcesPlugin.getWorkspace().run(mon -> {
                        for (TmfExperimentElement experimentElement : entry.getValue()) {
                            IResource resource = experimentElement.getResource();
                            IPath path = resource.getLocation();
                            if (path != null) {
                                // Delete supplementary files
                                experimentElement.deleteSupplementaryFolder();
                            }

                            // Finally, delete the experiment
                            resource.delete(true, subMon.split(1));
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
            TraceUtils.displayErrorMsg(Messages.DeleteExperimentHandler_Error, e.getTargetException().toString(), e.getTargetException());
            return null;
        }
        return null;
    }
}
