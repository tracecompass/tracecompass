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

package org.eclipse.linuxtools.internal.pcap.core.protocol.unknown;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.internal.pcap.core.endpoint.ProtocolEndpoint;

/**
 * Class that extends the {@link ProtocolEndpoint} class. It represents the
 * endpoint for a protocol that is unknown.
 *
 * @author Vincent Perot
 */
public class UnknownEndpoint extends ProtocolEndpoint {

    /**
     * Constructor of the {@link UnknownEndpoint} class. It takes a packet to
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
    public UnknownEndpoint(UnknownPacket packet, boolean isSourceEndpoint) {
        super(packet, isSourceEndpoint);
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return EMPTY_STRING;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        return false;
    }
}
