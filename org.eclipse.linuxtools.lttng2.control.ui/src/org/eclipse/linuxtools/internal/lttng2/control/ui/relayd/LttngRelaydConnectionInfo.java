/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc-Andre Laperle - Initial implementation
 **********************************************************************/

package org.eclipse.linuxtools.internal.lttng2.control.ui.relayd;

/**
 * A class that holds information about the relayd connection.
 *
 * @author Marc-Andre Laperle
 * @since 3.1
 */
public final class LttngRelaydConnectionInfo {

    private final String fHost;
    private final int fPort;
    private final String fSessionName;

    /**
     * Constructs a connection information.
     *
     * @param host
     *            the host string
     * @param port
     *            the port number
     * @param sessionName
     *            the session name
     */
    public LttngRelaydConnectionInfo(String host, int port, String sessionName) {
        fHost = host;
        fPort = port;
        fSessionName = sessionName;
    }

    /**
     * Get the host string.
     *
     * @return the host string
     */
    public String getHost() {
        return fHost;
    }

    /**
     * Get the port number.
     *
     * @return the port number
     */
    public int getPort() {
        return fPort;
    }

    /**
     * Get the session name.
     *
     * @return the session name
     */
    public String getSessionName() {
        return fSessionName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fHost == null) ? 0 : fHost.hashCode());
        result = prime * result + fPort;
        result = prime * result + ((fSessionName == null) ? 0 : fSessionName.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LttngRelaydConnectionInfo other = (LttngRelaydConnectionInfo) obj;
        if (fHost == null) {
            if (other.fHost != null) {
                return false;
            }
        } else if (!fHost.equals(other.fHost)) {
            return false;
        }
        if (fPort != other.fPort) {
            return false;
        }
        if (fSessionName == null) {
            if (other.fSessionName != null) {
                return false;
            }
        } else if (!fSessionName.equals(other.fSessionName)) {
            return false;
        }
        return true;
    }
}