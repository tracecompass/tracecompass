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

package org.eclipse.tracecompass.internal.tmf.pcap.core.util;

import org.eclipse.tracecompass.internal.pcap.core.protocol.PcapProtocol;
import org.eclipse.tracecompass.internal.tmf.pcap.core.protocol.TmfPcapProtocol;

/**
 * Helper class that allows the conversion between Protocol and TmfProtocol.
 * This is only used by this project and thus is internal (not API).
 *
 * @author Vincent Perot
 */
public final class ProtocolConversion {

    private ProtocolConversion() {}

    /**
     * Wrap a {@link PcapProtocol} into a {@link TmfPcapProtocol}.
     *
     * @param protocol
     *            The {@link PcapProtocol} to match
     * @return The TmfProtocol
     */
    public static TmfPcapProtocol wrap(PcapProtocol protocol) {
        switch (protocol) {
        case ETHERNET_II:
            return TmfPcapProtocol.ETHERNET_II;
        case IPV4:
            return TmfPcapProtocol.IPV4;
        case PCAP:
            return TmfPcapProtocol.PCAP;
        case TCP:
            return TmfPcapProtocol.TCP;
        case UDP:
            return TmfPcapProtocol.UDP;
        case UNKNOWN:
            return TmfPcapProtocol.UNKNOWN;
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Unwrap a {@link TmfPcapProtocol} from a {@link PcapProtocol}.
     *
     * @param protocol
     *            The TmfProtocol
     * @return The Protocol
     */
    public static PcapProtocol unwrap(TmfPcapProtocol protocol) {
        switch (protocol) {
        case ETHERNET_II:
            return PcapProtocol.ETHERNET_II;
        case IPV4:
            return PcapProtocol.IPV4;
        case PCAP:
            return PcapProtocol.PCAP;
        case TCP:
            return PcapProtocol.TCP;
        case UDP:
            return PcapProtocol.UDP;
        case UNKNOWN:
            return PcapProtocol.UNKNOWN;
        default:
            throw new IllegalArgumentException();
        }
    }

}
