/**********************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 *   Bernd Hufmann - Update to org.eclipse.remote API 2.0
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.remote;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteFileService;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteProcessService;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.RemoteConnectionChangeEvent;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

/**
 * <p>
 * RemoteSystemProxy implementation.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class RemoteSystemProxy implements IRemoteSystemProxy, IRemoteConnectionChangeListener {

    /** Name of a local connection */
    public static final String LOCAL_CONNECTION_NAME = "Local"; //$NON-NLS-1$

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

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public IRemoteFileService getRemoteFileService() {
        return fHost.getService(IRemoteFileService.class);
    }

    @Override
    public IRemoteProcessBuilder getProcessBuilder(String...command) {
        return fHost.getService(IRemoteProcessService.class).getProcessBuilder(command);
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
    public void connectionChanged(RemoteConnectionChangeEvent event) {
        int type = event.getType();
        if (type == RemoteConnectionChangeEvent.CONNECTION_ABORTED ||
                type == RemoteConnectionChangeEvent.CONNECTION_CLOSED) {
            fExplicitConnect = false;
        }
    }

    /**
     * Return a remote connection using OSGI service.
     *
     * @param remoteServicesId
     *            ID of remote service
     * @param name
     *            name of connection
     * @return the corresponding remote connection or null
     */
    public static @Nullable IRemoteConnection getRemoteConnection(final @NonNull String remoteServicesId, final @NonNull String name) {
        IRemoteServicesManager manager = Activator.getService(IRemoteServicesManager.class);
        if (manager == null) {
            return null;
        }
        FluentIterable<IRemoteConnection> connections = FluentIterable.from(manager.getAllRemoteConnections());
        Optional<IRemoteConnection> ret = connections.firstMatch(new Predicate<IRemoteConnection>() {
            @Override
            public boolean apply(IRemoteConnection input) {
                return (input.getConnectionType().getId().equals(remoteServicesId.toString()) && input.getName().equals(name.toString()));
            }
        });
        return ret.orNull();
    }

    /**
     * Return a Local connection.
     *
     * @return the local connection
     */
    public static @Nullable IRemoteConnection getLocalConnection() {
        IRemoteServicesManager manager = Activator.getService(IRemoteServicesManager.class);
        if (manager == null) {
            return null;
        }
        IRemoteConnectionType type = manager.getLocalConnectionType();
        return type.getConnection(LOCAL_CONNECTION_NAME);
    }
}
