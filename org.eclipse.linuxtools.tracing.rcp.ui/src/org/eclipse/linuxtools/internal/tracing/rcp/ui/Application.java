/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.tracing.rcp.ui;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.tracing.rcp.ui.messages.Messages;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * This class controls all aspects of the application's execution
 * @author Bernd Hufmann
 */
public class Application implements IApplication {

    private Location fInstanceLoc = null;

    @Override
    public Object start(IApplicationContext context) throws Exception {
        Display display = PlatformUI.createDisplay();
        try {
            // fetch the Location that we will be modifying
            fInstanceLoc = Platform.getInstanceLocation();

            // -data @noDefault in <applName>.ini allows us to set the workspace here.
            // If the user wants to change the location then he has to change
            // @noDefault to a specific location or remove -data @noDefault for
            // default location
            if (!fInstanceLoc.allowsDefault() && !fInstanceLoc.isSet()) {
                File workspaceRoot = new File(TracingRcpPlugin.getWorkspaceRoot());

                if (!workspaceRoot.exists()) {
                    MessageDialog.openError(display.getActiveShell(),
                            Messages.Application_WorkspaceCreationError,
                            MessageFormat.format(Messages.Application_WorkspaceRootNotExistError, new Object[] { TracingRcpPlugin.getWorkspaceRoot() }));
                    return IApplication.EXIT_OK;
                }

                if (!workspaceRoot.canWrite()) {
                    MessageDialog.openError(display.getActiveShell(),
                            Messages.Application_WorkspaceCreationError,
                            MessageFormat.format(Messages.Application_WorkspaceRootPermissionError, new Object[] { TracingRcpPlugin.getWorkspaceRoot() }));
                    return IApplication.EXIT_OK;
                }

                String workspace = TracingRcpPlugin.getWorkspaceRoot() + File.separator + TracingRcpPlugin.WORKSPACE_NAME;
                // set location to workspace
                fInstanceLoc.set(new URL("file", null, workspace), false); //$NON-NLS-1$
            }

            if (!fInstanceLoc.lock()) {
                MessageDialog.openError(display.getActiveShell(),
                        Messages.Application_WorkspaceCreationError,
                        MessageFormat.format(Messages.Application_WorkspaceInUseError, new Object[] { fInstanceLoc.getURL().getPath() }));
                return IApplication.EXIT_OK;
            }

            int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
            if (returnCode == PlatformUI.RETURN_RESTART) {
                return IApplication.EXIT_RESTART;
            }
            return IApplication.EXIT_OK;
        } finally {
            display.dispose();
        }
    }

    @Override
    public void stop() {
        if (!PlatformUI.isWorkbenchRunning()) {
            return;
        }
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final Display display = workbench.getDisplay();
        fInstanceLoc.release();
        display.syncExec(new Runnable() {
            @Override
            public void run() {
                if (!display.isDisposed()) {
                    workbench.close();
                }
            }
        });
    }
}
