/**********************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 *   Bernd Hufmann - Update to org.eclipse.remote API 2.0
 **********************************************************************/
package org.eclipse.tracecompass.tmf.remote.core.proxy;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.RemoteConnectionChangeEvent;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.tracecompass.internal.tmf.remote.core.shell.CommandShell;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandShell;

/**
 * <p>
 * RemoteSystemProxy implementation.
 * </p>
 *
 * @author Bernd Hufmann
 */
@NonNullByDefault
public class RemoteSystemProxy implements IRemoteConnectionChangeListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private final IRemoteConnection fHost;
    private boolean fExplicitConnect;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param host
     *            The host of this proxy
     */
    public RemoteSystemProxy(IRemoteConnection host) {
        fHost = host;
        fHost.addConnectionChangeListener(this);
    }

    /**
     * Returns the connection instance.
     *
     * @return the @link{IRemoteConnection} instance
     */
    public IRemoteConnection getRemoteConnection() {
        return fHost;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Connects the remote connection.
     *
     * @param monitor
     *            a monitor to report progress.
     *
     * @throws ExecutionException
     *             If the connection fails
     */
    public void connect(IProgressMonitor monitor) throws ExecutionException {
        try {
            if (!fHost.isOpen()) {
                // Note that open() may trigger a RemoteConnectionChangeEvent
                fHost.open(monitor);
                fExplicitConnect = true;
            }
        } catch (RemoteConnectionException e) {
            throw new ExecutionException("Cannot connect " + fHost.getName(), e); //$NON-NLS-1$
        }
    }

    /**
     * Disconnects from the remote connection, may close the connection.
     */
    public void disconnect() {
        if (fExplicitConnect) {
            fHost.close();
            fExplicitConnect = false;
        }
    }

    /**
     * Disposes the proxy, may close the connection.
     */
    public void dispose() {
        fHost.removeConnectionChangeListener(this);
        disconnect();
    }

    /**
     * Creates a command shell.
     *
     * @return the command shell implementation
     */
    public ICommandShell createCommandShell() {
        return new CommandShell(fHost);
    }

    /**
     * Returns the connection state.
     *
     * @return whether the remote host is currently connected.
     */
    public boolean isConnected() {
        return fHost.isOpen();
    }

    @Override
    public void connectionChanged(@Nullable RemoteConnectionChangeEvent event) {
        if (event != null) {
            int type = event.getType();
            if (type == RemoteConnectionChangeEvent.CONNECTION_ABORTED ||
                    type == RemoteConnectionChangeEvent.CONNECTION_CLOSED) {
                fExplicitConnect = false;
            }
        }
    }

}
