/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.handlers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Converts a project to a tracing project.
 *
 * @author Bernd Hufmann
 */
public class ConvertToTracingProjectHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        ISelection currentSelection = HandlerUtil.getCurrentSelectionChecked(event);
        if (currentSelection instanceof IStructuredSelection) {
            Object firstElement = ((IStructuredSelection) currentSelection).getFirstElement();
            if (firstElement instanceof IProject) {
                IProject project = (IProject) firstElement;
                try {
                    PlatformUI.getWorkbench().getProgressService().run(false, false, new IRunnableWithProgress() {
                        @Override
                        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                            TmfProjectRegistry.addTracingNature(project, monitor);
                        }
                    });
                } catch (InvocationTargetException e) {
                    Activator.getDefault().logError("Error adding tracing nature to project " + project.getName(), e); //$NON-NLS-1$
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return Status.OK_STATUS;
    }

}
