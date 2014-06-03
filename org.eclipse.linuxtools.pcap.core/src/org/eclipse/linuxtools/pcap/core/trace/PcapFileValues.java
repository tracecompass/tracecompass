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

package org.eclipse.linuxtools.pcap.core.trace;

/**
 * Interface that lists constants related to a Pcap File.
 *
 * See http://wiki.wireshark.org/Development/LibpcapFileFormat.
 *
 * @author Vincent Perot
 */
public interface PcapFileValues {

    /** Number used to determine the endianness and precision of the file */
    int MAGIC_BIG_ENDIAN_MICRO = 0xa1b2c3d4;

    /** Number used to determine the endianness and precision of the file */
    int MAGIC_LITTLE_ENDIAN_MICRO = 0xd4c3b2a1;

    /** Number used to determine the endianness and precision of the file */
    int MAGIC_BIG_ENDIAN_NANO = 0xa1b23c4d;

    /** Number used to determine the endianness and precision of the file */
    int MAGIC_LITTLE_ENDIAN_NANO = 0x4d3cb2a1;

    /** Size in bytes of a Pcap file global header */
    int GLOBAL_HEADER_SIZE = 24;

    /** Size in bytes of a Pcap packet header */
    int PACKET_HEADER_SIZE = 16;

    /** Position in bytes in the packet header of the packet's length */
    int INCLUDED_LENGTH_POSITION = 8;

}
