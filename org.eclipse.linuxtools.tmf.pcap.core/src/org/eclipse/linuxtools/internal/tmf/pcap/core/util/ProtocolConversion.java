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

package org.eclipse.linuxtools.internal.tmf.pcap.core.util;

import org.eclipse.linuxtools.pcap.core.protocol.Protocol;
import org.eclipse.linuxtools.tmf.pcap.core.protocol.TmfProtocol;

/**
 * Helper class that allows the conversion between Protocol and TmfProtocol.
 * This is only used by this project and thus is internal (not API).
 *
 * @author Vincent Perot
 */
public final class ProtocolConversion {

    private ProtocolConversion() {}

    /**
     * Wrap a {@link Protocol} into a {@link TmfProtocol}.
     *
     * @param protocol
     *            The {@link Protocol} to match
     * @return The TmfProtocol
     */
    public static TmfProtocol wrap(Protocol protocol) {
        switch (protocol) {
        case ETHERNET_II:
            return TmfProtocol.ETHERNET_II;
        case IPV4:
            return TmfProtocol.IPV4;
        case PCAP:
            return TmfProtocol.PCAP;
        case TCP:
            return TmfProtocol.TCP;
        case UDP:
            return TmfProtocol.UDP;
        case UNKNOWN:
            return TmfProtocol.UNKNOWN;
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Unwrap a {@link TmfProtocol} from a {@link Protocol}.
     *
     * @param protocol
     *            The TmfProtocol
     * @return The Protocol
     */
    public static Protocol unwrap(TmfProtocol protocol) {
        switch (protocol) {
        case ETHERNET_II:
            return Protocol.ETHERNET_II;
        case IPV4:
            return Protocol.IPV4;
        case PCAP:
            return Protocol.PCAP;
        case TCP:
            return Protocol.TCP;
        case UDP:
            return Protocol.UDP;
        case UNKNOWN:
            return Protocol.UNKNOWN;
        default:
            throw new IllegalArgumentException();
        }
    }

}
