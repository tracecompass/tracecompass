/**********************************************************************
 * Copyright (c) 2012, 2015 Ericsson and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann               - Initial API and implementation
 *   Anna Dushistova(Montavista) - [382684] Allow reusing already defined connections that have Files and Shells subsystems
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.handlers;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.INewConnectionDialog;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.TraceControlDialogFactory;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.tracecompass.tmf.remote.core.proxy.TmfRemoteConnectionFactory;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <p>
 * Command handler for creation of a new connection for trace control.
 * <br> By supplying arguments for the parameters with id {@link #PARAMETER_REMOTE_SERVICES_ID} and
 * {@link #PARAMETER_CONNECTION_NAME}, the caller can specify the remote connection that will
 * be added to the trace control. In case one of the optional arguments is not supplied, the handler
 * opens a dialog for selecting a remote connection.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class NewConnectionHandler extends BaseControlViewHandler {

    /**
     * Id of the parameter for the remote services id.
     * @see NewConnectionHandler
     * @see IRemoteConnectionType#getId()
     */
    public static final String PARAMETER_REMOTE_SERVICES_ID = "org.eclipse.linuxtools.lttng2.control.ui.remoteServicesIdParameter"; //$NON-NLS-1$
    /**
     * Id of the parameter for the name of the remote connection.
     * @see NewConnectionHandler
     * @see IRemoteConnection#getName()
     */
    public static final String PARAMETER_CONNECTION_NAME = "org.eclipse.linuxtools.lttng2.control.ui.connectionNameParameter"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The parent trace control component the new node will be added to.
     */
    private ITraceControlComponent fRoot = null;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (fRoot == null) {
            return false;
        }

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return false;
        }

        IRemoteConnection connection = getConnection(event.getParameters());
        if (connection != null) {
            fLock.lock();
            try {
                if (fRoot == null) {
                    return null;
                }
                // successful creation of host
                TargetNodeComponent node = null;
                if (!fRoot.containsChild(connection.getName())) {
                    node = new TargetNodeComponent(connection.getName(), fRoot, connection);
                    fRoot.addChild(node);
                } else {
                    node = (TargetNodeComponent)fRoot.getChild(connection.getName());
                }

                node.connect();
            } finally {
                fLock.unlock();
            }
        }
        return null;
    }

    private static IRemoteConnection getConnection(Map<?,?> parameters) {
        // First check whether arguments have been supplied
        Object remoteServicesId = parameters.get(PARAMETER_REMOTE_SERVICES_ID);
        Object connectionName = parameters.get(PARAMETER_CONNECTION_NAME);
        if ((remoteServicesId != null) && (connectionName != null)) {
            return TmfRemoteConnectionFactory.getRemoteConnection(
                    checkNotNull(remoteServicesId.toString()),
                    checkNotNull(connectionName.toString()));
        }

        // Without the arguments, open dialog box for the node name and address
        final INewConnectionDialog dialog = TraceControlDialogFactory.getInstance().getNewConnectionDialog();
        if (dialog.open() == Window.OK) {
            return dialog.getConnection();
        }

        return null;
    }

    @Override
    public boolean isEnabled() {

        // Get workbench page for the Control View
        IWorkbenchPage page = getWorkbenchPage();
        if (page == null) {
            return false;
        }

        ITraceControlComponent root = null;

        // no need to verify part because it has been already done in getWorkbenchPage()
        IWorkbenchPart part = page.getActivePart();
        root = ((ControlView) part).getTraceControlRoot();

        boolean isEnabled = root != null;

        fLock.lock();
        try {
            fRoot = null;
            if (isEnabled) {
                fRoot = root;
            }
        } finally {
            fLock.unlock();
        }

        return isEnabled;
    }
}
