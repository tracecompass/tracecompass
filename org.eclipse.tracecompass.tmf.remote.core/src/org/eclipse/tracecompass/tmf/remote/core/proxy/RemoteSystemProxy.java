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
package org.eclipse.tracecompass.tmf.remote.core.proxy;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNullByDefault;
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
import org.eclipse.tracecompass.internal.tmf.remote.core.Activator;
import org.eclipse.tracecompass.tmf.remote.core.shell.CommandShell;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandShell;

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
@NonNullByDefault
public class RemoteSystemProxy implements IRemoteConnectionChangeListener {

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
     * Finds the remote file system service.
     *
     * @return file service subsystem, or <code>null</code> if not found.
     */
    public @Nullable IRemoteFileService getRemoteFileService() {
        return fHost.getService(IRemoteFileService.class);
    }

    /**
     * Returns a remote process builder for remote launching a process.
     *
     * @param command
     *            the command to be executed.
     * @return the builder, or <code>null</code> if not possible.
     */
    public @Nullable IRemoteProcessBuilder getProcessBuilder(String...command) {
        return fHost.getService(IRemoteProcessService.class).getProcessBuilder(command);
    }

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
                fExplicitConnect = true;
                fHost.open(monitor);
            }
        } catch (RemoteConnectionException e) {
            throw new ExecutionException("Cannot connect " + fHost.getName(), e); //$NON-NLS-1$
        }
    }

    /**
     * Disconnects from the remote connection.
     */
    public void disconnect() {
        fHost.close();
    }

    /**
     * Disposes the proxy, may close the connection.
     */
    public void dispose() {
        fHost.removeConnectionChangeListener(this);
        if (fExplicitConnect) {
            fHost.close();
        }
    }

    /**
     * Creates a command shell.
     *
     * @return the command shell implementation
     * @throws ExecutionException
     *             If the command fails
     */
    public ICommandShell createCommandShell() throws ExecutionException {
        ICommandShell shell = new CommandShell(fHost);
        shell.connect();
        return shell;
    }

    /**
     * Method to add a communication listener to the connector service defined
     * for the given connection.
     *
     * @param listener
     *            listener to add
     */
    public void addConnectionChangeListener(IRemoteConnectionChangeListener listener) {
        fHost.addConnectionChangeListener(listener);
    }

    /**
     * Method to remove a communication listener from the connector service
     * defined for the given connection.
     *
     * @param listener
     *            listener to remove
     */
    public void removeConnectionChangeListener(IRemoteConnectionChangeListener listener) {
        fHost.removeConnectionChangeListener(listener);
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

    /**
     * Return the OSGi service with the given service interface.
     *
     * @param service
     *            service interface
     * @return the specified service or null if it's not registered
     */
    public static @Nullable <T> T getService(Class<T> service) {
        return Activator.getService(service);
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
    public static @Nullable IRemoteConnection getRemoteConnection(final String remoteServicesId, final String name) {
        IRemoteServicesManager manager = Activator.getService(IRemoteServicesManager.class);
        if (manager == null) {
            return null;
        }
        FluentIterable<IRemoteConnection> connections = FluentIterable.from(manager.getAllRemoteConnections());
        Optional<IRemoteConnection> ret = connections.firstMatch(new Predicate<IRemoteConnection>() {
            @Override
            public boolean apply(@Nullable IRemoteConnection input) {
                return ((input != null) && input.getConnectionType().getId().equals(remoteServicesId.toString()) && input.getName().equals(name.toString()));
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
        if (manager != null) {
            IRemoteConnectionType type = manager.getLocalConnectionType();
            return type.getConnection(LOCAL_CONNECTION_NAME);
        }
        return null;
    }
}
