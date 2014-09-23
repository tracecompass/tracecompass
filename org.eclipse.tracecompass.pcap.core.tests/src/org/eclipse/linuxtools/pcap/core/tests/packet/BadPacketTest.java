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

package org.eclipse.linuxtools.pcap.core.tests.packet;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.linuxtools.internal.pcap.core.packet.BadPacketException;
import org.eclipse.linuxtools.internal.pcap.core.protocol.ethernet2.EthernetIIPacket;
import org.eclipse.linuxtools.internal.pcap.core.trace.BadPcapFileException;
import org.eclipse.linuxtools.internal.pcap.core.trace.PcapFile;
import org.eclipse.linuxtools.pcap.core.tests.shared.PcapTestTrace;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit Class that tests if BadPacketExceptions are thrown correctly.
 *
 * @author Vincent Perot
 */
public class BadPacketTest {

    private ByteBuffer fEthernetPacket;

    /**
     * Initialize the packet.
     */
    @Before
    public void initialize() {
        fEthernetPacket = ByteBuffer.allocate(8);
        fEthernetPacket.order(ByteOrder.BIG_ENDIAN);

        // This packet is erroneous. It contains 8 bytes while the minimum is 14
        // bytes for an Ethernet II packet.

        // Destination MAC - 6 bytes
        fEthernetPacket.put((byte) 0x34);
        fEthernetPacket.put((byte) 0x67);
        fEthernetPacket.put((byte) 0x0C);
        fEthernetPacket.put((byte) 0xD2);
        fEthernetPacket.put((byte) 0x91);
        fEthernetPacket.put((byte) 0x51);

        // Source MAC - 2 bytes
        fEthernetPacket.put((byte) 0x10);
        fEthernetPacket.put((byte) 0xF8);

        fEthernetPacket.flip();

    }

    /**
     * Test that verify if a BadPacketException is correctly thrown (when a
     * packet is erroneous).
     *
     * @throws BadPcapFileException
     *             Thrown when the file is erroneous. Fails the test.
     * @throws IOException
     *             Thrown when an IO error occurs. Fails the test.
     * @throws BadPacketException
     *             Thrown when a packet is erroneous. Expected from the test.
     */
    @Test(expected = BadPacketException.class)
    public void PacketExceptionTest() throws BadPacketException, IOException, BadPcapFileException {
        PcapTestTrace trace = PcapTestTrace.MOSTLY_TCP;
        assumeTrue(trace.exists());
        try (PcapFile dummy = new PcapFile(trace.getPath())) {
            ByteBuffer packet = fEthernetPacket;
            if (packet != null) {
                new EthernetIIPacket(dummy, null, packet);
            }
            fail("PacketExceptionTest has failed!");
        }
    }
}
