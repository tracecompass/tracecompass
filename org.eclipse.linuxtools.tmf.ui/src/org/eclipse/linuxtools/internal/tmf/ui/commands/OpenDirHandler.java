/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 **********************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Open a directory, not a file
 *
 * @author Matthew Khouzam
 */
public class OpenDirHandler extends AbstractHandler{

    @Override
    public Object execute(ExecutionEvent event) {
        // Open a directory
        final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        DirectoryDialog dd = new DirectoryDialog(shell);
        dd.setText(Messages.OpenDirHandler_SelectTraceDirectory);
        String dir = dd.open();
        if (dir == null) {
            return null;
        }
        TmfOpenTraceHelper oth = new TmfOpenTraceHelper();
        try {
            IProject project = TmfHandlerUtil.getProjectFromSelection(HandlerUtil.getCurrentSelection(event));
            String projectName = project != null ? project.getName() : TmfCommonConstants.DEFAULT_TRACE_PROJECT_NAME;
            if (project == null) {
                project = TmfProjectRegistry.createProject(projectName, null, new NullProgressMonitor());
            }

            oth.openTraceFromPath(projectName, dir, shell);
        } catch (CoreException e) {
            Activator.getDefault().logError(e.getMessage(), e);
        }
        return null;
    }
}
