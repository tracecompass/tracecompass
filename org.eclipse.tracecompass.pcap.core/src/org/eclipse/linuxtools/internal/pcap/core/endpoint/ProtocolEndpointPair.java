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
 * Class that represents a pair of endpoints. This is used to find a packet
 * stream between to endpoints.
 *
 * @author Vincent Perot
 */
public class ProtocolEndpointPair {

    private final ProtocolEndpoint fEndpointA;
    private final ProtocolEndpoint fEndpointB;

    /**
     * Constructor of the class {@link ProtocolEndpointPair}. It constructs a
     * {@link ProtocolEndpointPair} object from a packet.
     *
     * @param packet
     *            The packet that contains the endpoints.
     */
    public ProtocolEndpointPair(Packet packet) {
        fEndpointA = packet.getSourceEndpoint();
        fEndpointB = packet.getDestinationEndpoint();
    }

    /**
     * Getter method that returns the first endpoint of the pair.
     *
     * @return The first endpoint.
     */
    public ProtocolEndpoint getFirstEndpoint() {
        return fEndpointA;
    }

    /**
     * Getter method that returns the second endpoint of the pair.
     *
     * @return The second endpoint.
     */
    public ProtocolEndpoint getSecondEndpoint() {
        return fEndpointB;
    }

    /**
     * Constructor of the class {@link ProtocolEndpointPair}. It constructs a
     * {@link ProtocolEndpointPair} object from two endpoints.
     *
     * @param endpointA
     *            The first endpoint.
     * @param endpointB
     *            The second endpoint.
     */
    public ProtocolEndpointPair(ProtocolEndpoint endpointA, ProtocolEndpoint endpointB) {
        fEndpointA = endpointA;
        fEndpointB = endpointB;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fEndpointA.hashCode() * fEndpointB.hashCode();
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {

        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProtocolEndpointPair)) {
            return false;
        }
        ProtocolEndpointPair other = (ProtocolEndpointPair) obj;

        return (this.fEndpointA.equals(other.fEndpointA) && this.fEndpointB.equals(other.fEndpointB)) ||
                (this.fEndpointA.equals(other.fEndpointB) && this.fEndpointB.equals(other.fEndpointA));
    }

}
