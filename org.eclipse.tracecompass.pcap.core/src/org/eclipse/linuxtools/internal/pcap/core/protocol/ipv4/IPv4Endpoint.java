/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.pcap.core.protocol.ipv4;

import java.net.Inet4Address;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.internal.pcap.core.endpoint.ProtocolEndpoint;

/**
 * Class that extends the {@link ProtocolEndpoint} class. It represents the
 * endpoint at an IPv4 level.
 *
 * @author Vincent Perot
 */
public class IPv4Endpoint extends ProtocolEndpoint {

    private final Inet4Address fIPAddress;

    /**
     * Constructor of the {@link IPv4Endpoint} class. It takes a packet to get
     * its endpoint. Since every packet has two endpoints (source and
     * destination), the isSourceEndpoint parameter is used to specify which
     * endpoint to take.
     *
     * @param packet
     *            The packet that contains the endpoints.
     * @param isSourceEndpoint
     *            Whether to take the source or the destination endpoint of the
     *            packet.
     */
    public IPv4Endpoint(IPv4Packet packet, boolean isSourceEndpoint) {
        super(packet, isSourceEndpoint);
        fIPAddress = isSourceEndpoint ? packet.getSourceIpAddress() : packet.getDestinationIpAddress();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        ProtocolEndpoint endpoint = getParentEndpoint();
        if (endpoint == null) {
            result = 0;
        } else {
            result = endpoint.hashCode();
        }

        result = prime * result + fIPAddress.hashCode();
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof IPv4Endpoint)) {
            return false;
        }

        IPv4Endpoint other = (IPv4Endpoint) obj;

        // Check on layer
        boolean localEquals = fIPAddress.equals(other.fIPAddress);
        if (!localEquals) {
            return false;
        }

        // Check above layers.
        ProtocolEndpoint endpoint = getParentEndpoint();
        if (endpoint != null) {
            return endpoint.equals(other.getParentEndpoint());
        }
        return true;
    }

    @Override
    public String toString() {
        ProtocolEndpoint endpoint = getParentEndpoint();
        if (endpoint == null) {
            @SuppressWarnings("null")
            @NonNull String ret = fIPAddress.getHostAddress();
            return ret;
        }
        return endpoint.toString() + '/' + fIPAddress.getHostAddress();
    }

}
