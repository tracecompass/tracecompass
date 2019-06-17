/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Bernd Hufmann - Improved trace selection
 *******************************************************************************/

package org.eclipse.tracecompass.internal.gdbtrace.ui.views.project.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.gdbtrace.core.trace.GdbTrace;
import org.eclipse.tracecompass.internal.gdbtrace.ui.views.project.dialogs.SelectTraceExecutableDialog;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for the Select Trace Executable command
 * @author Patrick Tasse
 */
public class SelectTraceExecutableHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);

        // Get the selection before opening the dialog because otherwise the
        // getCurrentSelection() call will always return null
        ISelection selection = HandlerUtil.getCurrentSelection(event);

        SelectTraceExecutableDialog dialog = new SelectTraceExecutableDialog(shell);
        dialog.open();
        if (dialog.getReturnCode() != Window.OK) {
            return null;
        }
        IPath tracedExecutable = dialog.getExecutablePath();

        if (selection instanceof IStructuredSelection) {
            for (Object o : ((IStructuredSelection) selection).toList()) {
                TmfTraceElement traceElement = (TmfTraceElement) o;
                IResource resource = traceElement.getResource();
                try {
                    resource.setPersistentProperty(GdbTrace.EXEC_KEY, tracedExecutable.toString());
                } catch (CoreException e) {
                    TraceUtils.displayErrorMsg(e);
                }
            }
        }
        return null;
    }

}
