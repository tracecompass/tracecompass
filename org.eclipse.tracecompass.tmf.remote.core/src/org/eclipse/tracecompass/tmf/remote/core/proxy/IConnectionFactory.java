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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.exception.RemoteConnectionException;

/**
 *
 * Interface to create a {@link IRemoteConnection} identified by an URI.
 *
 * @author Bernd Hufmann
 *
 */
@NonNullByDefault
public interface IConnectionFactory {
    /**
     * Creates an {@link IRemoteConnection} for the given URI.
     * @param hostUri
     *                the host URI to connect to
     * @param hostName
     *                the host name
     * @return {@link IRemoteConnection} for given URI
     *
     * @throws RemoteConnectionException in case of an error
     */
    IRemoteConnection createConnection(URI hostUri, String hostName) throws RemoteConnectionException;
}
