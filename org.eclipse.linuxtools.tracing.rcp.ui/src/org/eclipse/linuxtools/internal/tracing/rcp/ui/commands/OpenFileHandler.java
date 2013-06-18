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

package org.eclipse.linuxtools.internal.tracing.rcp.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.tracing.rcp.ui.TracingRcpPlugin;
import org.eclipse.linuxtools.internal.tracing.rcp.ui.messages.Messages;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Open file handler, used to open files (not directories)
 *
 * @author Matthew Khouzam
 */
public class OpenFileHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) {
        final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        FileDialog fd = new FileDialog(shell);
        fd.setText(Messages.OpenFileHandler_SelectTraceFile);
        String filePath = fd.open();
        if (filePath == null) {
            return null;
        }
        TmfOpenTraceHelper oth = new TmfOpenTraceHelper();
        try {
            oth.openTraceFromPath(Messages.ApplicationWorkbenchWindowAdvisor_DefaultProjectName,filePath, shell);
        } catch (CoreException e) {
            TracingRcpPlugin.getDefault().logError(e.getMessage(), e);
        }
        return null;
    }
}
