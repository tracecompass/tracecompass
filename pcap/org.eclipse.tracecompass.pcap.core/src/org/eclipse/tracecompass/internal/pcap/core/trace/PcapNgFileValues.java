/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.pcap.core.trace;

/**
 * Interface that lists constants related to a PcapNg File.
 *
 * See https://pcapng.github.io/pcapng/.
 */
public interface PcapNgFileValues {

    /** Section Header Block (SHB) block type */
    int SHB = 0x0A0D0D0A;

    /** Interface Description Block (IDB) block type */
    int IDB = 1;

    /** Packet Block (obsolete) (PB) block type */
    int PB = 2;

    /** Simple Packet Block (SPB) block type */
    int SPB = 3;

    /** Enhanced Packet Block (EPB) block type */
    int EPB = 6;

    /** Byte-Order Magic */
    int BYTE_ORDER_MAGIC = 0x1a2b3c4d;

    /** Block header size (Block Type + Block Total Length) */
    int BLOCK_HEADER_SIZE = 8;

    /** Block footer size (trailing Block Total Length) */
    int BLOCK_FOOTER_SIZE = 4;

    /** SHB minimum body size */
    int SHB_MIN_BODY_SIZE = 16;

    /** PCAPNG end-of-options code */
    int ENDOFOPT_CODE = 0;

    /** IDB if_tsresol option code */
    int IDB_IFTSRESOL_CODE = 9;

    /** IDB if_tsresol default value */
    int IDB_IFTSRESOL_DEFAULT = 6;

    /** IDB if_tsresol most-significant bit */
    byte IDB_IFTSRESOL_MSB = (byte) 0x80;

    /** IDB if_tsoffset option code */
    int IDB_IFTSOFFSET_CODE = 14;

}
