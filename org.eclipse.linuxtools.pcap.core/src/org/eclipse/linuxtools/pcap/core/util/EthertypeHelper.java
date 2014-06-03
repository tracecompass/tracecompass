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

package org.eclipse.linuxtools.pcap.core.util;

// TODO finish this
// TODO maybe match it to protocol instead of string.

/**
 * Helper that is used to help mapping a certain ethertype to a particular
 * protocol (i.e. IPv4). This is used when finding the child packet of an
 * Ethernet packet, for instance.
 *
 * See http://en.wikipedia.org/wiki/EtherType
 *
 * @author Vincent Perot
 */
public final class EthertypeHelper {

    /** EtherType IPv4 */
    public static final int ETHERTYPE_IPV4 = 0x0800;

    /** EtherType ARP */
    public static final int ETHERTYPE_ARP = 0x0806;

    /** EtherType Wake-On-LAN */
    public static final int ETHERTYPE_WAKE_ON_LAN = 0x0842;

    /** EtherType TRILL */
    public static final int ETHERTYPE_TRILL = 0x22F3;

    /** EtherType DECnet Phase IV */
    public static final int ETHERTYPE_DECNET_PHASE_IV = 0x6003;

    private EthertypeHelper() {}

    /**
     * Method that matches the ethertype as a number, to a protocol as a string.
     *
     * @param ethertype
     *            The Ethertype as an int.
     * @return The protocol as a string.
     */
    public static String toString(int ethertype) {
        switch (ethertype) {
        case ETHERTYPE_IPV4:
            return "Internet Protocol Version 4"; //$NON-NLS-1$
        case ETHERTYPE_ARP:
            return "Address Resolution Protocol"; //$NON-NLS-1$
        case ETHERTYPE_WAKE_ON_LAN:
            return "Wake-on-LAN"; //$NON-NLS-1$
        case ETHERTYPE_TRILL:
            return "IETF TRILL Protocol"; //$NON-NLS-1$
        case ETHERTYPE_DECNET_PHASE_IV:
            return "DECnet Phase IV"; //$NON-NLS-1$
        default:
            return "Unknown"; //$NON-NLS-1$
        }
    }

    /**
     * Convert an ethertype (int) into its string representation. This allows
     * the mapping of ethertype to the real protocol name.
     *
     * @param type
     *            The Ethertype to convert.
     * @return The Ethertype as a string.
     */
    public static String toEtherType(int type) {
        return toString(type) + " (0x" + String.format("%04x", type) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}
