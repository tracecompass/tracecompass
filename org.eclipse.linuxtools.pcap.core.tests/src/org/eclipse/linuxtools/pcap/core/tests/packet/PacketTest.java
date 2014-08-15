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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.linuxtools.internal.pcap.core.packet.BadPacketException;
import org.eclipse.linuxtools.internal.pcap.core.packet.Packet;
import org.eclipse.linuxtools.internal.pcap.core.protocol.Protocol;
import org.eclipse.linuxtools.internal.pcap.core.protocol.ethernet2.EthernetIIPacket;
import org.eclipse.linuxtools.internal.pcap.core.trace.BadPcapFileException;
import org.eclipse.linuxtools.internal.pcap.core.trace.PcapFile;
import org.eclipse.linuxtools.pcap.core.tests.shared.PcapTestTrace;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit Class that tests the generic Packet class and its method.
 *
 * @author Vincent Perot
 */
public class PacketTest {

    private ByteBuffer fEthernetPacket;

    /**
     * Initialize the packet.
     */
    @Before
    public void initialize() {
        fEthernetPacket = ByteBuffer.allocate(15);
        fEthernetPacket.order(ByteOrder.BIG_ENDIAN);

        // Destination MAC - 6 bytes
        fEthernetPacket.put((byte) 0x34);
        fEthernetPacket.put((byte) 0x67);
        fEthernetPacket.put((byte) 0x0C);
        fEthernetPacket.put((byte) 0xD2);
        fEthernetPacket.put((byte) 0x91);
        fEthernetPacket.put((byte) 0x51);

        // Source MAC - 6 bytes
        fEthernetPacket.put((byte) 0x10);
        fEthernetPacket.put((byte) 0xF8);
        fEthernetPacket.put((byte) 0x82);
        fEthernetPacket.put((byte) 0xB3);
        fEthernetPacket.put((byte) 0x44);
        fEthernetPacket.put((byte) 0x78);

        // Ethertype - 2 bytes
        fEthernetPacket.put((byte) 0xA2);
        fEthernetPacket.put((byte) 0x56);

        // Payload - 1 byte
        fEthernetPacket.put((byte) 0xA6);

        fEthernetPacket.flip();

    }

    /**
     * Test that verify the correctness of the Packet's methods.
     * @throws BadPcapFileException
     *             Thrown when the file is erroneous. Fails the test.
     * @throws IOException
     *             Thrown when an IO error occurs. Fails the test.
     * @throws BadPacketException
     *             Thrown when a packet is erroneous. Fails the test.
     */
    @Test
    public void GenericPacketTest() throws BadPacketException, IOException, BadPcapFileException {
        PcapTestTrace trace = PcapTestTrace.MOSTLY_TCP;
        assumeTrue(trace.exists());
        try (PcapFile dummy = new PcapFile(trace.getPath())) {
            ByteBuffer byteBuffer = fEthernetPacket;
            if (byteBuffer == null) {
                fail("GenericPacketTest has failed!");
                return;
            }

            Packet packet = new EthernetIIPacket(dummy, null, byteBuffer);
            assertTrue(packet.hasProtocol(Protocol.ETHERNET_II));
            assertTrue(packet.hasProtocol(Protocol.UNKNOWN));
            assertFalse(packet.hasProtocol(Protocol.TCP));
            assertEquals(Protocol.ETHERNET_II, packet.getProtocol());

            assertEquals(packet, packet.getPacket(Protocol.ETHERNET_II));
            assertNull(packet.getPacket(Protocol.TCP));
            assertEquals(packet.getChildPacket(), packet.getPacket(Protocol.UNKNOWN));
            assertEquals(packet.getPacket(Protocol.ETHERNET_II), packet.getMostEcapsulatedPacket());

            assertNull(packet.getParentPacket());
            assertFalse(packet.getPcapFile().equals(null));

            Packet child = packet.getChildPacket();
            if (child == null) {
                fail("GenericPacketTest has failed!");
                return;
            }
            assertEquals(packet.getPayload(), child.getPayload());
            assertEquals(packet.getGlobalSummaryString(), "Source MAC: 10:f8:82:b3:44:78 , Destination MAC: 34:67:0c:d2:91:51");

        }
    }
}
