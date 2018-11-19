/*******************************************************************************
 * Copyright (c) 2014, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *   Viet-Hung Phan - Support pcapNg
 *******************************************************************************/

package org.eclipse.tracecompass.pcap.core.tests.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.nio.ByteOrder;

import org.eclipse.tracecompass.internal.pcap.core.packet.BadPacketException;
import org.eclipse.tracecompass.internal.pcap.core.protocol.pcap.PcapPacket;
import org.eclipse.tracecompass.internal.pcap.core.trace.BadPcapFileException;
import org.eclipse.tracecompass.internal.pcap.core.trace.PcapFile;
import org.eclipse.tracecompass.pcap.core.tests.shared.PcapTestTrace;
import org.junit.Test;

/**
 * JUnit Class that tests whether the Pcap/PcapNg parser can read both big endian and
 * little endian files.
 *
 * @author Vincent Perot
 */

public class PcapFileEndiannessTest {

    /**
     * Test that verify that two files with different endianness contain the
     * same packets.
     *
     * @throws BadPcapFileException
     *             Thrown when the file is erroneous. Fails the test.
     * @throws IOException
     *             Thrown when an IO error occurs. Fails the test.
     * @throws BadPacketException
     *             Thrown when a packet is erroneous. Fails the test.
     */
    @Test
    public void EndiannessTest() throws IOException, BadPcapFileException, BadPacketException {
        PcapTestTrace trace = PcapTestTrace.SHORT_LITTLE_ENDIAN;
        assumeTrue(trace.exists());
        // Get a right pcap/pcapNg trace
        PcapFile littleEndian = trace.getTrace();

        trace = PcapTestTrace.SHORT_BIG_ENDIAN;
        assumeTrue(trace.exists());
        // Get a right pcap/pcapNg trace
        PcapFile bigEndian = trace.getTrace();

        assertEquals(ByteOrder.BIG_ENDIAN, bigEndian.getByteOrder());
        assertEquals(ByteOrder.LITTLE_ENDIAN, littleEndian.getByteOrder());
        while (true) {
            PcapPacket littleEndianPacket = littleEndian.parseNextPacket();
            PcapPacket bigEndianPacket = bigEndian.parseNextPacket();
            assertEquals(littleEndianPacket, bigEndianPacket);
            if (littleEndianPacket == null) {
                break;
            }
        }
    }
}
