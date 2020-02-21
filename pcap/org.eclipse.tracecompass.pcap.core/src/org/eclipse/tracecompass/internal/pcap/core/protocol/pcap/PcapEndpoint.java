/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.pcap.core.protocol.pcap;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.pcap.core.endpoint.ProtocolEndpoint;

/**
 * Class that extends the {@link ProtocolEndpoint} class. It represents the endpoint at
 * a Pcap level.
 *
 * @author Vincent Perot
 */
public class PcapEndpoint extends ProtocolEndpoint {

    /**
     * Constructor of the {@link PcapEndpoint} class. It takes a packet to get
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
    public PcapEndpoint(PcapPacket packet, boolean isSourceEndpoint) {
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
        if (!(obj instanceof PcapEndpoint)) {
            return false;
        }

        PcapEndpoint other = (PcapEndpoint) obj;

        // Check above layers.
        ProtocolEndpoint endpoint = getParentEndpoint();
        if (endpoint != null) {
            return endpoint.equals(other.getParentEndpoint());
        }
        return true;
    }

}
