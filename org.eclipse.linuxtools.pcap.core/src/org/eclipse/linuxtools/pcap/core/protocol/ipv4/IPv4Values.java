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

package org.eclipse.linuxtools.pcap.core.protocol.ipv4;

/**
 * Interface that lists constants related to Internet Protocol v4.
 *
 * See http://en.wikipedia.org/wiki/IPv4#Packet_structure.
 *
 * @author Vincent Perot
 */
public interface IPv4Values {

    /** Size in bytes of an IP address */
    int IP_ADDRESS_SIZE = 4;

    /** Size in bytes of a default IPv4 packet header */
    int DEFAULT_HEADER_LENGTH = 5;

    /** Size in bytes of a block of data. Used to convert data block to bytes */
    int BLOCK_SIZE = 4;

}
