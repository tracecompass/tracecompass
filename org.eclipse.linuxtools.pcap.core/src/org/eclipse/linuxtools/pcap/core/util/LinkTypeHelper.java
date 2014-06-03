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
// TODO map to protocol instead of string? that would make more sense imo.

/**
 * Helper that is used to help mapping a certain linktype to a particular
 * protocol (i.e. ethernet).
 *
 * See http://www.tcpdump.org/linktypes.html
 *
 * @author Vincent Perot
 */
public final class LinkTypeHelper {

    /** Linktype Null */
    public static final int LINKTYPE_NULL = 0;

    /** Linktype Ethernet II */
    public static final int LINKTYPE_ETHERNET = 1;

    /** Linktype AX25 */
    public static final int LINKTYPE_AX25 = 3;

    /** Linktype IEEE802.5 */
    public static final int LINKTYPE_IEEE802_5 = 6;

    /** Linktype Raw */
    public static final int LINKTYPE_RAW = 101;

    /** Linktype IEEE802.11 */
    public static final int LINKTYPE_IEEE802_11 = 105;

    /** Linktype Linux SLL */
    public static final int LINKTYPE_LINUX_SLL = 113;

    private LinkTypeHelper() {}

    /**
     * Method that match the linktype as an int to a protocol as a string.
     *
     * @param linkType
     *            The linkType as an int.
     * @return The protocol as a string.
     */
    public static String toString(int linkType) {
        switch (linkType) {
        case LINKTYPE_NULL:
            return "null"; //$NON-NLS-1$
        case LINKTYPE_ETHERNET:
            return "ethernet"; //$NON-NLS-1$
        case LINKTYPE_AX25:
            return "ax25"; //$NON-NLS-1$
        case LINKTYPE_IEEE802_5:
            return "ieee802.5"; //$NON-NLS-1$
        case LINKTYPE_RAW:
            return "raw"; //$NON-NLS-1$
        case LINKTYPE_IEEE802_11:
            return "ieee802.11"; //$NON-NLS-1$
        case LINKTYPE_LINUX_SLL:
            return "linux_sll"; //$NON-NLS-1$
        default:
            return "unknown"; //$NON-NLS-1$
        }
    }

}
