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

package org.eclipse.linuxtools.pcap.core.protocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Enumeration used for describing the different known protocols.
 *
 * @author Vincent Perot
 */
public enum Protocol {

    // Layer 0
    /**
     * The Pcap Protocol is not a real protocol but is used as an helper to
     * generate Pcap packets.
     */
    PCAP("Packet Capture", "pcap", ProtocolValues.LAYER_0, false), //$NON-NLS-1$ //$NON-NLS-2$

    // Layer 1
    // Should always be empty.

    // Layer 2
    /**
     * The description of the Ethernet II Protocol.
     */
    ETHERNET_II("Ethernet II", "eth", ProtocolValues.LAYER_2, true), //$NON-NLS-1$ //$NON-NLS-2$

    // Layer 3
    /**
     * The description of the Internet Protocol Version 4.
     */
    IPV4("Internet Protocol Version 4", "ipv4", ProtocolValues.LAYER_3, true), //$NON-NLS-1$ //$NON-NLS-2$

    // Layer 4
    /**
     * The description of the Transmission Control Protocol.
     */
    TCP("Transmission Control Protocol", "tcp", ProtocolValues.LAYER_4, true), //$NON-NLS-1$ //$NON-NLS-2$
    /**
     * The description of the User Datagram Protocol.
     */
    UDP("User Datagram Protocol", "udp", ProtocolValues.LAYER_4, true), //$NON-NLS-1$ //$NON-NLS-2$

    // Layer 5

    // Layer 6

    // Layer 7
    /**
     * This protocol is used as an helper if the protocol of a packet is not
     * recognized. Since all its data goes into payload, it can also be seen as
     * a "payload packet". This is considered to be on layer 7 since its always
     * the most encapsulated packet if present.
     */
    UNKNOWN("Payload", "???", ProtocolValues.LAYER_7, false); //$NON-NLS-1$ //$NON-NLS-2$

    // Fields
    private final String fName;
    private final String fShortName;
    private final int fLayer;
    private final boolean fSupportsStream;

    private Protocol(String name, String shortName, int layer, boolean supportsStream) {
        fName = name;
        fShortName = shortName;
        fLayer = layer;
        fSupportsStream = supportsStream;
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
     * Getter method for the OSI layer of the protocol.
     *
     * @return The layer of the protocol.
     */
    public int getLayer() {
        return fLayer;
    }

    /**
     * Getter method that indicates if the protocol supports streams.
     *
     * @return Whether the protocol supports streams or not.
     */
    public boolean supportsStream() {
        return fSupportsStream;
    }

    // TODO make an immutable list that holds this data instead of computing it
    // everytime.

    /**
     * Method that returns a list of all the protocols included in a certain OSI
     * layer.
     *
     * @param layer
     *            The layer of the protocols.
     * @return The protocols on that layer.
     */
    public static List<Protocol> getProtocolsOnLayer(int layer) {

        if (layer > ProtocolValues.LAYER_7 || layer < ProtocolValues.LAYER_0) {
            throw new IllegalArgumentException("The layer is invalid."); //$NON-NLS-1$
        }

        List<Protocol> protocolsOnLayer = new ArrayList<>();
        for (Protocol p : Protocol.values()) {
            if (p.getLayer() == layer) {
                protocolsOnLayer.add(p);
            }
        }
        return protocolsOnLayer;
    }

    /**
     * Method that returns all the protocol defined.
     *
     * @return A list containing all the protocols.
     */
    public static List<Protocol> getAllProtocols() {
        return new ArrayList<>(Arrays.asList(Protocol.values()));
    }
}
