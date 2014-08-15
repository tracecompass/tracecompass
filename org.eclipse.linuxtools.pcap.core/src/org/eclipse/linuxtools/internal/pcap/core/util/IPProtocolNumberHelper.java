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

package org.eclipse.linuxtools.internal.pcap.core.util;

import org.eclipse.linuxtools.internal.pcap.core.protocol.Protocol;

// TODO finish this
// TODO maybe match it to protocol instead of string.

/**
 * Helper that is used to help mapping a certain protocol number to a particular
 * protocol (i.e. TCP). This is used when finding the child packet of an IPv4
 * packet, for instance.
 *
 * See http://en.wikipedia.org/wiki/List_of_IP_protocol_numbers
 *
 * @author Vincent Perot
 */
public final class IPProtocolNumberHelper {

    /** Protocol Number ICMP */
    public static final int PROTOCOL_NUMBER_ICMP = 1;

    /** Protocol Number IGMP */
    public static final int PROTOCOL_NUMBER_IGMP = 2;

    /** Protocol Number TCP */
    public static final int PROTOCOL_NUMBER_TCP = 6;

    /** Protocol Number UDP */
    public static final int PROTOCOL_NUMBER_UDP = 17;

    /** Protocol Number Encapsulated IPv6 */
    public static final int PROTOCOL_NUMBER_ENCAP_IPV6 = 41;

    /** Protocol Number OSPF */
    public static final int PROTOCOL_NUMBER_OSPF = 89;

    /** Protocol Number SCTP */
    public static final int PROTOCOL_NUMBER_SCTP = 132;

    private IPProtocolNumberHelper() {}

    /**
     * Method that match the protocol number to a protocol as a string.
     *
     * @param protocolNumber
     *            The protocol number as an int.
     * @return The protocol as a string.
     */
    public static String toString(int protocolNumber) {
        switch (protocolNumber) {
        case PROTOCOL_NUMBER_ICMP:
            return "ICMP"; //$NON-NLS-1$
        case PROTOCOL_NUMBER_IGMP:
            return "IGMP"; //$NON-NLS-1$
        case PROTOCOL_NUMBER_TCP:
            return Protocol.TCP.getName();
        case PROTOCOL_NUMBER_UDP:
            return Protocol.UDP.getName();
        case PROTOCOL_NUMBER_ENCAP_IPV6:
            return "IPv6"; //$NON-NLS-1$
        case PROTOCOL_NUMBER_OSPF:
            return "OSPF"; //$NON-NLS-1$
        case PROTOCOL_NUMBER_SCTP:
            return "SCTP"; //$NON-NLS-1$
        default:
            return "Unknown"; //$NON-NLS-1$
        }
    }
}
