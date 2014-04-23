/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Patrick Tasse - Add support for folder elements
 **********************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
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
        final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        FileDialog fd = new FileDialog(shell);
        fd.setText(Messages.OpenFileHandler_SelectTraceFile);
        String filePath = fd.open();
        if (filePath == null) {
            return null;
        }

        try {
            TmfTraceFolder destinationFolder = TmfHandlerUtil.getTraceFolderFromSelection(HandlerUtil.getCurrentSelection(event));
            TmfOpenTraceHelper.openTraceFromPath(destinationFolder, filePath, shell);
        } catch (CoreException e) {
            Activator.getDefault().logError(e.getMessage(), e);
        }
        return null;
    }
}
