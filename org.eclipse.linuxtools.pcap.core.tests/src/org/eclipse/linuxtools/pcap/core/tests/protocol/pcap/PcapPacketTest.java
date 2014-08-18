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

package org.eclipse.linuxtools.pcap.core.tests.protocol.pcap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

import org.eclipse.linuxtools.internal.pcap.core.packet.BadPacketException;
import org.eclipse.linuxtools.internal.pcap.core.protocol.PcapProtocol;
import org.eclipse.linuxtools.internal.pcap.core.protocol.pcap.PcapEndpoint;
import org.eclipse.linuxtools.internal.pcap.core.protocol.pcap.PcapPacket;
import org.eclipse.linuxtools.internal.pcap.core.trace.BadPcapFileException;
import org.eclipse.linuxtools.internal.pcap.core.trace.PcapFile;
import org.eclipse.linuxtools.pcap.core.tests.shared.PcapTestTrace;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

/**
 * JUnit Class that tests the PcapPacket class and its method.
 *
 * @author Vincent Perot
 */
public class PcapPacketTest {

    private static final Map<String, String> EXPECTED_FIELDS = ImmutableMap.of(
            "Frame", "36",
            "Frame Length", "75 bytes",
            "Capture Length", "75 bytes",
            "Capture Time", "2005-07-04 05:33:52.829.277.000"
            );

    private static final String EXPECTED_TOSTRING;
    static {
        StringBuilder sb = new StringBuilder();
        sb.append("Packet Capture 36: 75 bytes on wire, 75 bytes captured.\n");
        sb.append("Arrival time: 2005-07-04 05:33:52.829.277.000\n");
        sb.append("Ethernet II, Source: 00:e0:ed:01:6e:bd, Destination: 00:30:54:00:34:56, Type: Internet Protocol Version 4 (0x0800)\n");
        sb.append("Internet Protocol Version 4, Source: 192.168.1.2, Destination: 192.168.1.1\n");
        sb.append("Version: 4, Identification: 0x69aa, Header Length: 20 bytes, Total Length: 61 bytes\n");
        sb.append("Differentiated Services Code Point: 0x00; Explicit Congestion Notification: 0x00\n");
        sb.append("Flags: 0x00 (Don't have more fragments), Fragment Offset: 0\n");
        sb.append("Time to live: 128\n");
        sb.append("Protocol: 17\n");
        sb.append("Header Checksum: 0x4db2\n");
        sb.append("User Datagram Protocol, Source Port: 2719, Destination Port: 53, Length: 41, Checksum: 19038\n");
        sb.append("Payload: ed d4 01 00 00 01 00 00 00 00 00 00 03 66 74 70 07 65 63 69 74 65 6c 65 03 63 6f 6d 00 00 01 00 01");

        EXPECTED_TOSTRING = sb.toString();
    }

    private ByteBuffer fPayload;

    /**
     * Initialize the payload.
     */
    @Before
    public void initialize() {
        fPayload = ByteBuffer.allocate(75);
        fPayload.order(ByteOrder.BIG_ENDIAN);

        // Values copied from wireshark

        // Bytes 0x01-0x10
        fPayload.put((byte) 0x00);
        fPayload.put((byte) 0x30);
        fPayload.put((byte) 0x54);
        fPayload.put((byte) 0x00);
        fPayload.put((byte) 0x34);
        fPayload.put((byte) 0x56);
        fPayload.put((byte) 0x00);
        fPayload.put((byte) 0xE0);
        fPayload.put((byte) 0xED);
        fPayload.put((byte) 0x01);
        fPayload.put((byte) 0x6E);
        fPayload.put((byte) 0xBD);
        fPayload.put((byte) 0x08);
        fPayload.put((byte) 0x00);
        fPayload.put((byte) 0x45);
        fPayload.put((byte) 0x00);

        // Bytes 0x11-0x20
        fPayload.put((byte) 0x00);
        fPayload.put((byte) 0x3D);
        fPayload.put((byte) 0x69);
        fPayload.put((byte) 0xAA);
        fPayload.put((byte) 0x00);
        fPayload.put((byte) 0x00);
        fPayload.put((byte) 0x80);
        fPayload.put((byte) 0x11);
        fPayload.put((byte) 0x4D);
        fPayload.put((byte) 0xB2);
        fPayload.put((byte) 0xC0);
        fPayload.put((byte) 0xA8);
        fPayload.put((byte) 0x01);
        fPayload.put((byte) 0x02);
        fPayload.put((byte) 0xC0);
        fPayload.put((byte) 0xA8);

        // Bytes 0x21-0x30
        fPayload.put((byte) 0x01);
        fPayload.put((byte) 0x01);
        fPayload.put((byte) 0x0A);
        fPayload.put((byte) 0x9F);
        fPayload.put((byte) 0x00);
        fPayload.put((byte) 0x35);
        fPayload.put((byte) 0x00);
        fPayload.put((byte) 0x29);
        fPayload.put((byte) 0x4A);
        fPayload.put((byte) 0x5E);
        fPayload.put((byte) 0xED);
        fPayload.put((byte) 0xd4);
        fPayload.put((byte) 0x01);
        fPayload.put((byte) 0x00);
        fPayload.put((byte) 0x00);
        fPayload.put((byte) 0x01);

        // Bytes 0x31-0x40
        fPayload.put((byte) 0x00);
        fPayload.put((byte) 0x00);
        fPayload.put((byte) 0x00);
        fPayload.put((byte) 0x00);
        fPayload.put((byte) 0x00);
        fPayload.put((byte) 0x00);
        fPayload.put((byte) 0x03);
        fPayload.put((byte) 0x66);
        fPayload.put((byte) 0x74);
        fPayload.put((byte) 0x70);
        fPayload.put((byte) 0x07);
        fPayload.put((byte) 0x65);
        fPayload.put((byte) 0x63);
        fPayload.put((byte) 0x69);
        fPayload.put((byte) 0x74);
        fPayload.put((byte) 0x65);

        // Bytes 0x41-0x4B
        fPayload.put((byte) 0x6C);
        fPayload.put((byte) 0x65);
        fPayload.put((byte) 0x03);
        fPayload.put((byte) 0x63);
        fPayload.put((byte) 0x6F);
        fPayload.put((byte) 0x6D);
        fPayload.put((byte) 0x00);
        fPayload.put((byte) 0x00);
        fPayload.put((byte) 0x01);
        fPayload.put((byte) 0x00);
        fPayload.put((byte) 0x01);

        fPayload.flip();
    }

    /**
     * Test that verify the correctness of the PcapPacket's methods.
     * @throws BadPcapFileException
     *             Thrown when the file is erroneous. Fails the test.
     * @throws IOException
     *             Thrown when an IO error occurs. Fails the test.
     * @throws BadPacketException
     *             Thrown when a packet is erroneous. Fails the test.
     */
    @Test
    public void CompletePcapPacketTest() throws IOException, BadPcapFileException, BadPacketException {
        PcapTestTrace trace = PcapTestTrace.MOSTLY_UDP;
        assumeTrue(trace.exists());
        try (PcapFile file = new PcapFile(trace.getPath());) {

            file.seekPacket(36);
            PcapPacket packet = file.parseNextPacket();
            if (packet == null) {
                fail("CompletePcapPacketTest has failed!");
                return;
            }
            // Protocol Testing
            assertEquals(PcapProtocol.PCAP, packet.getProtocol());
            assertTrue(packet.hasProtocol(PcapProtocol.PCAP));
            assertTrue(packet.hasProtocol(PcapProtocol.UNKNOWN));
            assertFalse(packet.hasProtocol(PcapProtocol.TCP));

            // Abstract methods Testing
            assertTrue(packet.validate());
            assertEquals(86567859, packet.hashCode());
            assertFalse(packet.equals(null));
            assertFalse(packet.equals(file.parseNextPacket()));

            assertEquals(EXPECTED_FIELDS, packet.getFields());
            assertEquals(EXPECTED_TOSTRING, packet.toString());
            assertEquals("Frame 36: 75 bytes on wire, 75 bytes captured", packet.getLocalSummaryString());
            assertEquals("Source Port: 2719, Destination Port: 53", packet.getGlobalSummaryString());

            assertEquals(new PcapEndpoint(packet, true), packet.getSourceEndpoint());
            assertEquals(new PcapEndpoint(packet, false), packet.getDestinationEndpoint());

            ByteBuffer payload = packet.getPayload();
            if (payload == null) {
                fail("CompletePcapPacketTest has failed!");
                return;
            }
            assertEquals(fPayload, payload.flip());

            // Packet-specific methods Testing
            assertEquals(36, packet.getIndex());
            assertEquals(75, packet.getOriginalLength());
            assertEquals(75, packet.getIncludedLength());
            assertEquals(1120469632829277L, packet.getTimestamp());
            assertFalse(packet.isTruncated());
        }
    }
}
