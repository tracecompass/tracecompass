/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.remote.core.proxy;

import java.net.URI;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionHostService;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.internal.jsch.core.JSchConnection;
import org.eclipse.tracecompass.internal.tmf.remote.core.messages.Messages;

/**
 * RemoteSystemProxy factory.
 *
 * @author Bernd Hufmann
 */
@SuppressWarnings("restriction")
@NonNullByDefault
public class RemoteSystemProxyFactory {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private static final Map<String, IConnectionFactory> CONNECTION_FACTORIES = new HashMap<>();
    private static final DefaultConnectionFactory DEFAULT_CONNECTION_FACTORY = new DefaultConnectionFactory();

    static {
        // Add local services
        IRemoteServicesManager manager = RemoteSystemProxy.getService(IRemoteServicesManager.class);
        if (manager != null) {
            CONNECTION_FACTORIES.put(manager.getLocalConnectionType().getId(), new LocalConnectionFactory());
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
     * Creates a remote system proxy.
     *
     * @param hostUri
     *                The host URI
     * @param hostName
     *                The hostname
     * @return the remote system proxy
     *
     * @throws RemoteConnectionException
     *                In case of an error
     */
    public static RemoteSystemProxy createProxy(URI hostUri, String hostName) throws RemoteConnectionException {

        IRemoteConnection connection = null;
        IRemoteServicesManager manager = RemoteSystemProxy.getService(IRemoteServicesManager.class);
        if (manager == null) {
            throw new RemoteConnectionException(MessageFormat.format(Messages.RemoteConnection_ConnectionError, hostUri));
        }
        IRemoteConnectionType connectionType = manager.getConnectionType(hostUri);
        if (connectionType == null) {
            throw new RemoteConnectionException(MessageFormat.format(Messages.RemoteConnection_ConnectionError, hostUri));
        }

        IConnectionFactory connectionFactory = CONNECTION_FACTORIES.get(connectionType.getId());
        // Create a new connection
        if (connectionFactory != null) {
            connection = connectionFactory.createConnection(connectionType, hostUri, hostName);
        } else {
            connection = DEFAULT_CONNECTION_FACTORY.createConnection(connectionType, hostUri, hostName);
        }

        return new RemoteSystemProxy(connection);
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
        public IRemoteConnection createConnection(IRemoteConnectionType connectionType, URI hostUri, String hostName) throws RemoteConnectionException {
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
                try {
                    IRemoteConnectionWorkingCopy wc = null;
                    wc = connectionType.newConnection(hostName);
                    if (wc == null) {
                        throw new RemoteConnectionException(MessageFormat.format(Messages.RemoteConnection_ConnectionError, hostUri));
                    }
                    if (connectionType.getId().equals(JSchConnection.JSCH_ID)) {
                        wc.setAttribute(JSchConnection.ADDRESS_ATTR, hostUri.getHost());
                        wc.setAttribute(JSchConnection.PORT_ATTR, Integer.toString(hostUri.getPort()));
                        String user = hostUri.getUserInfo();
                        if (user == null) {
                            user = System.getProperty("user.name"); //$NON-NLS-1$
                        }
                        wc.setAttribute(JSchConnection.USERNAME_ATTR, user);
                        wc.setAttribute(JSchConnection.IS_PASSWORD_ATTR, Boolean.TRUE.toString());
                        connection = wc.save(); // Save the attributes
                    }
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
        public IRemoteConnection createConnection(IRemoteConnectionType connectionType, URI hostUri, String hostName) throws RemoteConnectionException {
            IRemoteConnection connection = RemoteSystemProxy.getLocalConnection();
            if (connection == null) {
                throw new RemoteConnectionException(MessageFormat.format(Messages.RemoteConnection_ConnectionError, hostUri));
            }
            return connection;
        }
    }

}
