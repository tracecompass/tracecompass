/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Patrick Tasse - Add support for folder elements
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.commands;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfUIPreferences;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.ui.dialog.TmfFileDialogFactory;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Open file handler, used to open files (not directories)
 *
 * @author Matthew Khouzam
 */
public class OpenFileHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) {

        ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
        // Menu Selection is not null for context-sensitive menu
        ISelection menuSelection  = HandlerUtil.getActiveMenuSelection(event);

        // Get trace path
        final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        FileDialog fd = TmfFileDialogFactory.create(shell);
        fd.setText(Messages.OpenFileHandler_SelectTraceFile);
        IEclipsePreferences defaultPreferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        String lastLocation = defaultPreferences.get(ITmfUIPreferences.PREF_SAVED_OPEN_FILE_LOCATION, null);
        if (lastLocation != null && !lastLocation.isEmpty()) {
            File parentFile = new File(lastLocation).getParentFile();
            if (parentFile != null && parentFile.exists()) {
                fd.setFilterPath(parentFile.toString());
            }
        }
        String filePath = fd.open();
        if (filePath == null) {
            return null;
        }

        try {

            TmfTraceFolder destinationFolder;

            if ((menuSelection != null) && (currentSelection instanceof IStructuredSelection)) {
                // If handler is called from the context sensitive menu of a tracing project import to
                // the traces folder from this project
                destinationFolder = TmfHandlerUtil.getTraceFolderFromSelection(currentSelection);
            } else {
                // If handler is called from file menu import into default tracing project
                IProject project = TmfProjectRegistry.createProject(
                        TmfCommonConstants.DEFAULT_TRACE_PROJECT_NAME, null, new NullProgressMonitor());
                TmfProjectElement projectElement = TmfProjectRegistry.getProject(project, true);
                destinationFolder = projectElement.getTracesFolder();
            }

            TmfOpenTraceHelper.openTraceFromPath(destinationFolder, filePath, shell);
        } catch (CoreException e) {
            Activator.getDefault().logError(e.getMessage(), e);
        }

        InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).put(ITmfUIPreferences.PREF_SAVED_OPEN_FILE_LOCATION, filePath);
        return null;
    }
}
