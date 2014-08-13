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

package org.eclipse.linuxtools.pcap.core.tests.protocol.ipv4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.linuxtools.pcap.core.packet.BadPacketException;
import org.eclipse.linuxtools.pcap.core.protocol.Protocol;
import org.eclipse.linuxtools.pcap.core.protocol.ipv4.IPv4Endpoint;
import org.eclipse.linuxtools.pcap.core.protocol.ipv4.IPv4Packet;
import org.eclipse.linuxtools.pcap.core.tests.shared.PcapTestTrace;
import org.eclipse.linuxtools.pcap.core.trace.BadPcapFileException;
import org.eclipse.linuxtools.pcap.core.trace.PcapFile;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit Class that tests the IPv4Packet class and its method.
 *
 * @author Vincent Perot
 */
public class IPv4PacketTest {

    private static final Map<String, String> EXPECTED_FIELDS;
    static {
        EXPECTED_FIELDS = new LinkedHashMap<>();
        EXPECTED_FIELDS.put("Version", "4");
        EXPECTED_FIELDS.put("Header Length", "24 bytes");
        EXPECTED_FIELDS.put("Differentiated Services Field", "0x26");
        EXPECTED_FIELDS.put("Explicit Congestion Notification", "0x02");
        EXPECTED_FIELDS.put("Total Length", "255 bytes");
        EXPECTED_FIELDS.put("Identification", "0x0ff0");
        EXPECTED_FIELDS.put("Don't Fragment Flag", "false");
        EXPECTED_FIELDS.put("More Fragment Flag", "false");
        EXPECTED_FIELDS.put("Fragment Offset", "7905");
        EXPECTED_FIELDS.put("Time to live", "160");
        EXPECTED_FIELDS.put("Protocol", "Unknown (254)");
        EXPECTED_FIELDS.put("Checksum", "0x3344");
        EXPECTED_FIELDS.put("Source IP Address", "192.168.1.0");
        EXPECTED_FIELDS.put("Destination IP Address", "193.169.2.1");
        EXPECTED_FIELDS.put("Options", "a2 56 a2 56");
    }

    private static final String EXPECTED_TOSTRING;
    static {
        StringBuilder sb = new StringBuilder();
        sb.append("Internet Protocol Version 4, Source: 192.168.1.0, Destination: 193.169.2.1\n");
        sb.append("Version: 4, Identification: 0x0ff0, Header Length: 24 bytes, Total Length: 255 bytes\n");
        sb.append("Differentiated Services Code Point: 0x26; Explicit Congestion Notification: 0x02\n");
        sb.append("Flags: 0x00 (Don't have more fragments), Fragment Offset: 7905\n");
        sb.append("Time to live: 160\n");
        sb.append("Protocol: 254\n");
        sb.append("Header Checksum: 0x3344\n");
        sb.append("Payload: a6");

        EXPECTED_TOSTRING = sb.toString();
    }

    private ByteBuffer fPacket;

    /**
     * Initialize the packet.
     */
    @Before
    public void initialize() {
        fPacket = ByteBuffer.allocate(25);
        fPacket.order(ByteOrder.BIG_ENDIAN);

        // Version + IHL
        fPacket.put((byte) 0x46);

        // DSCP + ECN
        fPacket.put((byte) 0x9A);

        // Total length - this is randomly chosen so that we verify that the
        // packet handles wrong total length.
        fPacket.put((byte) 0x00);
        fPacket.put((byte) 0xFF);

        // Identification
        fPacket.put((byte) 0x0F);
        fPacket.put((byte) 0xF0);

        // Flags + Fragment Offset
        fPacket.put((byte) 0x1E);
        fPacket.put((byte) 0xE1);

        // Time to live
        fPacket.put((byte) 0xA0);

        // Protocol - Unknown
        fPacket.put((byte) 0xFE);

        // Header checksum - chosen randomly
        fPacket.put((byte) 0x33);
        fPacket.put((byte) 0x44);

        // Source IP - 4 bytes
        fPacket.put((byte) 192);
        fPacket.put((byte) 168);
        fPacket.put((byte) 1);
        fPacket.put((byte) 0);

        // Destination IP - 4 bytes
        fPacket.put((byte) 193);
        fPacket.put((byte) 169);
        fPacket.put((byte) 2);
        fPacket.put((byte) 1);

        // Options - 4 bytes
        fPacket.put((byte) 0xA2);
        fPacket.put((byte) 0x56);
        fPacket.put((byte) 0xA2);
        fPacket.put((byte) 0x56);

        // Payload - 1 byte
        fPacket.put((byte) 0xA6);

        fPacket.flip();
    }

    /**
     * Test that verify the correctness of the IPv4Packet's methods.
     *
     * @throws BadPcapFileException
     *             Thrown when the file is erroneous. Fails the test.
     * @throws IOException
     *             Thrown when an IO error occurs. Fails the test.
     * @throws BadPacketException
     *             Thrown when a packet is erroneous. Fails the test.
     */
    @Test
    public void CompleteIPv4PacketTest() throws IOException, BadPcapFileException, BadPacketException {
        PcapTestTrace trace = PcapTestTrace.MOSTLY_TCP;
        assumeTrue(trace.exists());
        try (PcapFile dummy = new PcapFile(trace.getPath())) {
            ByteBuffer byteBuffer = fPacket;
            if (byteBuffer == null) {
                fail("CompleteIPv4PacketTest has failed!");
                return;
            }
            IPv4Packet packet = new IPv4Packet(dummy, null, byteBuffer);

            // Protocol Testing
            assertEquals(Protocol.IPV4, packet.getProtocol());
            assertTrue(packet.hasProtocol(Protocol.IPV4));
            assertTrue(packet.hasProtocol(Protocol.UNKNOWN));
            assertFalse(packet.hasProtocol(Protocol.TCP));

            // Abstract methods Testing
            assertTrue(packet.validate());
            assertEquals(-222021887, packet.hashCode());
            assertFalse(packet.equals(null));
            assertEquals(new IPv4Packet(dummy, null, byteBuffer), packet);

            assertEquals(EXPECTED_FIELDS, packet.getFields());
            assertEquals(EXPECTED_TOSTRING, packet.toString());
            assertEquals("Src: 192.168.1.0 , Dst: 193.169.2.1", packet.getLocalSummaryString());
            assertEquals("192.168.1.0 > 193.169.2.1 Id=4080 Len=1", packet.getGlobalSummaryString());

            assertEquals(new IPv4Endpoint(packet, true), packet.getSourceEndpoint());
            assertEquals(new IPv4Endpoint(packet, false), packet.getDestinationEndpoint());

            fPacket.position(24);
            byte[] payload = new byte[1];
            fPacket.get(payload);
            assertEquals(ByteBuffer.wrap(payload), packet.getPayload());

            // Packet-specific methods Testing
            assertEquals(InetAddress.getByAddress(Arrays.copyOfRange(fPacket.array(), 12, 16)), packet.getSourceIpAddress());
            assertEquals(InetAddress.getByAddress(Arrays.copyOfRange(fPacket.array(), 16, 20)), packet.getDestinationIpAddress());
            assertTrue(Arrays.equals(packet.getOptions(), Arrays.copyOfRange(fPacket.array(), 20, 24)));
            assertEquals(4, packet.getVersion());
            assertEquals(24, packet.getHeaderLength());
            assertEquals(0x26, packet.getDSCP());
            assertEquals(0x02, packet.getExplicitCongestionNotification());
            assertEquals(255, packet.getTotalLength());
            assertEquals(0x0FF0, packet.getIdentification());
            assertFalse(packet.getReservedFlag());
            assertFalse(packet.getDontFragmentFlag());
            assertFalse(packet.getHasMoreFragment());
            assertEquals(7905, packet.getFragmentOffset());
            assertEquals(160, packet.getTimeToLive());
            assertEquals(0xFE, packet.getIpDatagramProtocol());
            assertEquals(0x3344, packet.getHeaderChecksum());

        }
    }
}
