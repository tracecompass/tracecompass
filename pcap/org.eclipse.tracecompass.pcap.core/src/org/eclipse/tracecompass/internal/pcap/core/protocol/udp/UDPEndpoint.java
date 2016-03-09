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

package org.eclipse.tracecompass.internal.pcap.core.protocol.udp;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.pcap.core.endpoint.ProtocolEndpoint;

/**
 * Class that extends the ProtocolEndpoint class. It represents the endpoint at
 * an UDP level.
 *
 * @author Vincent Perot
 */
public class UDPEndpoint extends ProtocolEndpoint {

    private final int fPort;

    /**
     * Constructor of the {@link UDPEndpoint} class. It takes a packet to get
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
    public UDPEndpoint(UDPPacket packet, boolean isSourceEndpoint) {
        super(packet, isSourceEndpoint);
        fPort = isSourceEndpoint ? packet.getSourcePort() : packet.getDestinationPort();
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
        result = prime * result + fPort;
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UDPEndpoint)) {
            return false;
        }

        UDPEndpoint other = (UDPEndpoint) obj;

        // Check on layer
        boolean localEquals = (fPort == other.fPort);
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
            return String.valueOf(fPort);
        }
        return endpoint.toString() + '/' + fPort;
    }

}
