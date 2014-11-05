/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.remote;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionChangeEvent;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.IRemoteFileManager;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.exception.RemoteConnectionException;

/**
 * <p>
 * RemoteSystemProxy implementation.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class RemoteSystemProxy implements IRemoteSystemProxy, IRemoteConnectionChangeListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private IRemoteConnection fHost;
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

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public IRemoteFileManager getFileServiceSubSystem() {
        return fHost.getFileManager();
    }

    @Override
    public IRemoteProcessBuilder getProcessBuilder(String...command) {
        return fHost.getProcessBuilder(command);
    }

    @Override
    public void connect(IProgressMonitor monitor) throws ExecutionException {
        try {
            if (!fHost.isOpen()) {
                fExplicitConnect = true;
                fHost.open(monitor);
            }
        } catch (RemoteConnectionException e) {
            throw new ExecutionException("Cannot connect " + fHost.getName(), e); //$NON-NLS-1$
        }
    }

    @Override
    public void disconnect() throws ExecutionException {
        fHost.close();
    }

    @Override
    public void dispose() {
        fHost.removeConnectionChangeListener(this);
        if (fExplicitConnect) {
            fHost.close();
        }
    }

    @Override
    public ICommandShell createCommandShell() throws ExecutionException {
        ICommandShell shell = new CommandShell(fHost);
        shell.connect();
        return shell;
    }

    @Override
    public void addConnectionChangeListener(IRemoteConnectionChangeListener listener) {
        fHost.addConnectionChangeListener(listener);
    }

    @Override
    public void removeConnectionChangeListener(IRemoteConnectionChangeListener listener) {
        fHost.removeConnectionChangeListener(listener);
    }

    @Override
    public boolean isConnected() {
        return fHost.isOpen();
    }

    @Override
    public void connectionChanged(IRemoteConnectionChangeEvent event) {
        int type = event.getType();
        if (type == IRemoteConnectionChangeEvent.CONNECTION_ABORTED ||
                type == IRemoteConnectionChangeEvent.CONNECTION_CLOSED) {
            fExplicitConnect = false;
        }
    }
}
