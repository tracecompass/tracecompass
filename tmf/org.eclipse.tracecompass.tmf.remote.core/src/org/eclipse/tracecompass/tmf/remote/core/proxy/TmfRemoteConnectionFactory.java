/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.remote.core.proxy;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.net.URI;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionHostService;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.tracecompass.internal.tmf.remote.core.Activator;
import org.eclipse.tracecompass.internal.tmf.remote.core.messages.Messages;

/**
 * Factory for creation of remote connections programmatically.
 *
 * It creates {@link IRemoteConnection} instances base on host URI and name.
 *
 * @author Bernd Hufmann
 */
@NonNullByDefault
public class TmfRemoteConnectionFactory {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /** Name of a local connection */
    public static final String LOCAL_CONNECTION_NAME = "Local"; //$NON-NLS-1$

    private static final Map<String, IConnectionFactory> CONNECTION_FACTORIES = new HashMap<>();
    private static final DefaultConnectionFactory DEFAULT_CONNECTION_FACTORY = new DefaultConnectionFactory();

    static {
        // Add local services
        IRemoteServicesManager manager = getService(IRemoteServicesManager.class);
        if (manager != null) {
            IRemoteConnectionType type = manager.getLocalConnectionType();
            if (type != null) {
                CONNECTION_FACTORIES.put(checkNotNull(type.getId()), new LocalConnectionFactory());
            }
        }
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Registers a connection factory for a given {@link IRemoteConnectionType} ID.
     * Previously registered factories with same ID will be overwritten.
     *
     * @param connectionTypeId
     *                ID of remote connection type
     * @param factory
     *                the factory implementation
     */
    public static void registerConnectionFactory(String connectionTypeId, IConnectionFactory factory) {
        CONNECTION_FACTORIES.put(connectionTypeId, factory);
    }

    /**
     * Creates a remote connection instance.
     *
     * @param hostUri
     *                The host URI
     * @param hostName
     *                The hostname
     * @return the remote connection {@link IRemoteConnection}
     *
     * @throws RemoteConnectionException
     *                In case of an error
     */
    public static IRemoteConnection createConnection(URI hostUri, String hostName) throws RemoteConnectionException {

        IRemoteConnectionType connectionType = getConnectionType(hostUri);
        IConnectionFactory connectionFactory = CONNECTION_FACTORIES.get(connectionType.getId());
        if (connectionFactory == null) {
            connectionFactory = DEFAULT_CONNECTION_FACTORY;
        }
        // Create and return a new connection
        return connectionFactory.createConnection(hostUri, hostName);
    }

    // ------------------------------------------------------------------------
    // Helper classes
    // ------------------------------------------------------------------------
    /**
     * Default {@link IConnectionFactory} implementation. It uses the built-in
     * ssh implementation.
     */
    public static class DefaultConnectionFactory implements IConnectionFactory {

        @Override
        public IRemoteConnection createConnection(URI hostUri, String hostName) throws RemoteConnectionException {

            IRemoteConnectionType connectionType = getConnectionType(hostUri);

            IRemoteConnection connection = null;

            // Look for existing connections
            for (IRemoteConnection conn : connectionType.getConnections()) {
                if (conn.getName().equals(hostName)) {
                    IRemoteConnectionHostService hostService = conn.getService(IRemoteConnectionHostService.class);
                    if (hostService != null) {
                        if ((hostService.getHostname().equals(hostUri.getHost())) &&
                                (hostUri.getPort() == -1 || hostService.getPort() == hostUri.getPort())) {
                            connection = conn;
                            break;
                        }
                        throw new RemoteConnectionException(MessageFormat.format(Messages.RemoteConnection_DuplicateConnectionError, hostName, hostService.getHostname(), hostService.getPort()));
                    }
                }
            }

            if (connection == null) {
                // Create a new connection
                IRemoteConnectionWorkingCopy wc = null;
                wc = connectionType.newConnection(hostName);

                if (wc == null) {
                    throw new RemoteConnectionException(MessageFormat.format(Messages.RemoteConnection_ConnectionError, hostUri));
                }

                if (wc.hasService(IRemoteConnectionHostService.class)) {
                    IRemoteConnectionHostService hostService = checkNotNull(wc.getService(IRemoteConnectionHostService.class));
                    hostService.setHostname(hostUri.getHost());
                    hostService.setPort(hostUri.getPort());
                    String user = hostUri.getUserInfo();
                    if (user == null) {
                        user = System.getProperty("user.name"); //$NON-NLS-1$
                    }
                    hostService.setUsername(user);
                    hostService.setUsePassword(true);
                } else {
                    throw new RemoteConnectionException(MessageFormat.format(Messages.RemoteConnection_ConnectionError, hostUri));
                }

                try {
                    connection = wc.save(); // Save the attributes
                } catch (RemoteConnectionException e) {
                    throw new RemoteConnectionException(MessageFormat.format(Messages.RemoteConnection_ConnectionError, hostUri), e);
                }
            }

            if (connection == null) {
                throw new RemoteConnectionException(MessageFormat.format(Messages.RemoteConnection_ConnectionError, hostUri));
            }

            return connection;
        }
    }

    /**
     * Default Local Connection Factory
     */
    public static class LocalConnectionFactory implements IConnectionFactory {
        @Override
        public IRemoteConnection createConnection(URI hostUri, String hostName) throws RemoteConnectionException {
            IRemoteConnection connection = getLocalConnection();
            if (connection == null) {
                throw new RemoteConnectionException(MessageFormat.format(Messages.RemoteConnection_ConnectionError, hostUri));
            }
            return connection;
        }
    }

    // ------------------------------------------------------------------------
    // Helper method(s)
    // ------------------------------------------------------------------------
    private static IRemoteConnectionType getConnectionType(URI hostUri) throws RemoteConnectionException {
        IRemoteServicesManager manager = getService(IRemoteServicesManager.class);
        if (manager == null) {
            throw new RemoteConnectionException(MessageFormat.format(Messages.RemoteConnection_ConnectionError, hostUri));
        }
        IRemoteConnectionType connectionType = manager.getConnectionType(hostUri);
        if (connectionType == null) {
            throw new RemoteConnectionException(MessageFormat.format(Messages.RemoteConnection_ConnectionError, hostUri));
        }
        return connectionType;
    }

    // ------------------------------------------------------------------------
    // Helper methods using OSGI service
    // ------------------------------------------------------------------------
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
        return manager.getAllRemoteConnections().stream()
                .filter(Objects::nonNull)
                .filter(connection -> connection.getName().equals(name))
                .filter(connection -> connection.getConnectionType().getId().equals(remoteServicesId))
                .findFirst()
                .orElse(null);
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
