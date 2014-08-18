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

package org.eclipse.linuxtools.pcap.core.tests.protocol.ethernet2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.linuxtools.internal.pcap.core.packet.BadPacketException;
import org.eclipse.linuxtools.internal.pcap.core.protocol.PcapProtocol;
import org.eclipse.linuxtools.internal.pcap.core.protocol.ethernet2.EthernetIIEndpoint;
import org.eclipse.linuxtools.internal.pcap.core.protocol.ethernet2.EthernetIIPacket;
import org.eclipse.linuxtools.internal.pcap.core.protocol.ethernet2.EthernetIIValues;
import org.eclipse.linuxtools.internal.pcap.core.trace.BadPcapFileException;
import org.eclipse.linuxtools.internal.pcap.core.trace.PcapFile;
import org.eclipse.linuxtools.pcap.core.tests.shared.PcapTestTrace;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

/**
 * JUnit Class that tests the EthernetIIPacket class and its method.
 *
 * @author Vincent Perot
 */
public class EthernetIIPacketTest {

    private static final Map<String, String> EXPECTED_FIELDS = ImmutableMap.of(
            "Source MAC Address", "10:f8:82:b3:44:78",
            "Destination MAC Address", "34:67:0c:d2:91:51",
            "Ethertype", "Unknown (0xa256)"
            );

    private static final String EXPECTED_TOSTRING =
            "Ethernet II, Source: 10:f8:82:b3:44:78, Destination: 34:67:0c:d2:91:51, Type: Unknown (0xa256)\nPayload: a6";

    private ByteBuffer fPacket;

    /**
     * Initialize the packet.
     */
    @Before
    public void initialize() {
        fPacket = ByteBuffer.allocate(15);
        fPacket.order(ByteOrder.BIG_ENDIAN);

        // Destination MAC - 6 bytes
        fPacket.put((byte) 0x34);
        fPacket.put((byte) 0x67);
        fPacket.put((byte) 0x0C);
        fPacket.put((byte) 0xD2);
        fPacket.put((byte) 0x91);
        fPacket.put((byte) 0x51);

        // Source MAC - 6 bytes
        fPacket.put((byte) 0x10);
        fPacket.put((byte) 0xF8);
        fPacket.put((byte) 0x82);
        fPacket.put((byte) 0xB3);
        fPacket.put((byte) 0x44);
        fPacket.put((byte) 0x78);

        // Ethertype - 2 bytes
        fPacket.put((byte) 0xA2);
        fPacket.put((byte) 0x56);

        // Payload - 1 byte
        fPacket.put((byte) 0xA6);

        fPacket.flip();
    }

    /**
     * Test that verify the correctness of the EthernetIIPacket's methods.
     * @throws BadPcapFileException
     *             Thrown when the file is erroneous. Fails the test.
     * @throws IOException
     *             Thrown when an IO error occurs. Fails the test.
     * @throws BadPacketException
     *             Thrown when a packet is erroneous. Fails the test.
     */
    @Test
    public void CompleteEthernetIIPacketTest() throws IOException, BadPcapFileException, BadPacketException {
        PcapTestTrace trace = PcapTestTrace.MOSTLY_TCP;
        assumeTrue(trace.exists());
        try (PcapFile dummy = new PcapFile(trace.getPath())) {
            ByteBuffer byteBuffer = fPacket;
            if (byteBuffer == null) {
                fail("CompleteEthernetIIPacketTest has failed!");
                return;
            }
            EthernetIIPacket packet = new EthernetIIPacket(dummy, null, byteBuffer);

            // Protocol Testing
            assertEquals(PcapProtocol.ETHERNET_II, packet.getProtocol());
            assertTrue(packet.hasProtocol(PcapProtocol.ETHERNET_II));
            assertTrue(packet.hasProtocol(PcapProtocol.UNKNOWN));
            assertFalse(packet.hasProtocol(PcapProtocol.TCP));

            // Abstract methods Testing
            assertTrue(packet.validate());
            assertEquals(-653947816, packet.hashCode());
            assertFalse(packet.equals(null));
            assertEquals(new EthernetIIPacket(dummy, null, byteBuffer), packet);

            assertEquals(EXPECTED_FIELDS, packet.getFields());
            assertEquals(EXPECTED_TOSTRING, packet.toString());
            assertEquals("Src: 10:f8:82:b3:44:78 , Dst: 34:67:0c:d2:91:51", packet.getLocalSummaryString());
            assertEquals("Source MAC: 10:f8:82:b3:44:78 , Destination MAC: 34:67:0c:d2:91:51", packet.getGlobalSummaryString());

            assertEquals(new EthernetIIEndpoint(packet, true), packet.getSourceEndpoint());
            assertEquals(new EthernetIIEndpoint(packet, false), packet.getDestinationEndpoint());

            fPacket.position(14);
            byte[] payload = new byte[1];
            fPacket.get(payload);
            assertEquals(ByteBuffer.wrap(payload), packet.getPayload());

            // Packet-specific methods Testing
            assertTrue(Arrays.equals(packet.getSourceMacAddress(), Arrays.copyOfRange(fPacket.array(), EthernetIIValues.MAC_ADDRESS_SIZE, EthernetIIValues.MAC_ADDRESS_SIZE + EthernetIIValues.MAC_ADDRESS_SIZE)));
            assertTrue(Arrays.equals(packet.getDestinationMacAddress(), Arrays.copyOfRange(fPacket.array(), 0, 0 + EthernetIIValues.MAC_ADDRESS_SIZE)));
            assertEquals(0xA256, packet.getEthertype());

        }
    }
}
