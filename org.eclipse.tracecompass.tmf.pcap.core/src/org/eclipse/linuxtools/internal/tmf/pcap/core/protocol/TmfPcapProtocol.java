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

package org.eclipse.linuxtools.internal.tmf.pcap.core.protocol;

import org.eclipse.linuxtools.internal.pcap.core.protocol.PcapProtocol;

/**
 * Enumeration as a TMF wrapper of the different Protocols. To register a
 * protocol in TMF, it must be present in
 * org.eclipse.linuxtools.pcap.core.protocol.Protocol and must have the same
 * name.
 *
 * @author Vincent Perot
 */
public enum TmfPcapProtocol {

    // Layer 0
    /**
     * The Pcap Protocol is not a real protocol but is used as an helper to
     * generate Pcap packets.
     */
    PCAP(PcapProtocol.PCAP),

    // Layer 1
    // Should always be empty.

    // Layer 2
    /**
     * The description of the Ethernet II Protocol.
     */
    ETHERNET_II(PcapProtocol.ETHERNET_II),

    // Layer 3
    /**
     * The description of the Internet Protocol Version 4.
     */
    IPV4(PcapProtocol.IPV4),

    // Layer 4
    /**
     * The description of the Transmission Control Protocol.
     */
    TCP(PcapProtocol.TCP),
    /**
     * The description of the User Datagram Protocol.
     */
    UDP(PcapProtocol.UDP),

    // Layer 5

    // Layer 6

    // Layer 7
    /**
     * This protocol is used as an helper if the protocol of a packet is not
     * recognized. Since all its data goes into payload, it can also be seen as
     * a "payload packet". This is considered to be on layer 7 since its always
     * the most encapsulated packet if present.
     */
    UNKNOWN(PcapProtocol.UNKNOWN);

    private final String fName;
    private final String fShortName;
    private final boolean fSupportsStream;

    private TmfPcapProtocol(PcapProtocol pcapProto) {
        fName = pcapProto.getName();
        fShortName = pcapProto.getShortName();
        fSupportsStream = pcapProto.supportsStream();
    }

    /**
     * Getter method for the long name of the protocol.
     *
     * @return The long name of the protocol, as a string.
     */
    public String getName() {
        return fName;
    }

    /**
     * Getter method for the short name of the protocol.
     *
     * @return The short name of the protocol, as a string.
     */
    public String getShortName() {
        return fShortName;
    }

    /**
     * Getter method that indicates if the protocol supports streams.
     *
     * @return Whether the protocol supports streams or not.
     */
    public boolean supportsStream() {
        return fSupportsStream;
    }

}
