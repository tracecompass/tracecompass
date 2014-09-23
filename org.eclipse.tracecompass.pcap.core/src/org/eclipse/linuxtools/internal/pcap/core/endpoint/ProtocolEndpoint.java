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

package org.eclipse.linuxtools.internal.pcap.core.endpoint;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.internal.pcap.core.packet.Packet;

/**
 * Abstract class that represents an endpoint. An endpoint is an address where a
 * packet is received or sent. Therefore, it is protocol dependent. For
 * instance, an Ethernet II endpoint is the MAC address. An Ipv4 endpoint is the
 * combination of the MAC address and the IP address. This is useful for
 * building packet streams.
 *
 * @author Vincent Perot
 */
public abstract class ProtocolEndpoint {

    /**
     * Empty string for child classes.
     */
    protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

    /**
     * The encapsulating endpoint. Much like packets, endpoints are
     * encapsulated. The higher the layer of the packet protocol is, the more
     * parents an endpoint will have.
     */
    private final @Nullable ProtocolEndpoint fParentEndpoint;

    /**
     * Constructor of the {@link ProtocolEndpoint} class. It takes a packet to
     * get its endpoint. Since every packet has two endpoints (source and
     * destination), the isSourceEndpoint parameter is used to specify which
     * endpoint to take.
     *
     * @param packet
     *            The packet that contains the endpoints.
     * @param isSourceEndpoint
     *            Whether to take the source or the destination endpoint of the
     *            packet.
     */
    public ProtocolEndpoint(Packet packet, boolean isSourceEndpoint) {
        Packet parentPacket = packet.getParentPacket();
        if (parentPacket == null) {
            fParentEndpoint = null;
        } else {
            fParentEndpoint = isSourceEndpoint ?
                    parentPacket.getSourceEndpoint() :
                    parentPacket.getDestinationEndpoint();
        }
    }

    /**
     * Getter method that returns the parent endpoint.
     *
     * @return The parent endpoint.
     */
    public @Nullable ProtocolEndpoint getParentEndpoint() {
        return fParentEndpoint;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(@Nullable Object obj);

    @Override
    public abstract String toString();

}
