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

package org.eclipse.tracecompass.pcap.core.tests.protocol.pcap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.eclipse.tracecompass.internal.pcap.core.packet.BadPacketException;
import org.eclipse.tracecompass.internal.pcap.core.protocol.PcapProtocol;
import org.eclipse.tracecompass.internal.pcap.core.protocol.pcap.PcapEndpoint;
import org.eclipse.tracecompass.internal.pcap.core.protocol.pcap.PcapOldPacket;
import org.eclipse.tracecompass.internal.pcap.core.protocol.pcap.PcapPacket;
import org.eclipse.tracecompass.internal.pcap.core.trace.BadPcapFileException;
import org.eclipse.tracecompass.internal.pcap.core.trace.PcapFile;
import org.eclipse.tracecompass.internal.pcap.core.trace.PcapOldFile;
import org.eclipse.tracecompass.pcap.core.tests.shared.PcapTestTrace;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

/**
 * JUnit Class that tests the PcapPacket class and its method.
 *
 * @author Vincent Perot
 */
public class PcapPacketTest {

    private static final String EXPECTED_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String EXPECTED_GMT_TIME = "2005-07-04 09:33:52.829";

    private static final Map<String, String> EXPECTED_FIELDS;
    private static final String EXPECTED_TOSTRING;
    static {

        // Convert known GMT time to default (local) time zone. The local time
        // is the expected value.
        String captureTime = "";
        try {
            SimpleDateFormat gmtFormat = new SimpleDateFormat(EXPECTED_DATE_FORMAT);
            gmtFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date gmtDate = gmtFormat.parse(EXPECTED_GMT_TIME);

            SimpleDateFormat defaultFormat = new SimpleDateFormat(EXPECTED_DATE_FORMAT);
            captureTime = defaultFormat.format(gmtDate);
        } catch (ParseException e) {
            fail("failed to parse date");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Packet Capture 36: 75 bytes on wire, 75 bytes captured.\n");
        sb.append("Arrival time: " + captureTime + ".277.000\n");
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
        EXPECTED_FIELDS = ImmutableMap.of(
                "Frame", "36",
                "Frame Length", "75 bytes",
                "Capture Length", "75 bytes",
                "Capture Time", captureTime + ".277.000"
                );
    }

    private ByteBuffer fHeader;
    private ByteBuffer fPayload;

    /**
     * Initialize the payload.
     */
    @Before
    public void initialize() {
        // Values copied from wireshark

        fHeader = ByteBuffer.allocate(16);
        fHeader.put((byte) 0x80);
        fHeader.put((byte) 0x02);
        fHeader.put((byte) 0xc9);
        fHeader.put((byte) 0x42);
        fHeader.put((byte) 0x5d);
        fHeader.put((byte) 0xa7);
        fHeader.put((byte) 0x0c);
        fHeader.put((byte) 0x00);
        fHeader.put((byte) 0x4b);
        fHeader.put((byte) 0x00);
        fHeader.put((byte) 0x00);
        fHeader.put((byte) 0x00);
        fHeader.put((byte) 0x4b);
        fHeader.put((byte) 0x00);
        fHeader.put((byte) 0x00);
        fHeader.put((byte) 0x00);
        fHeader.flip();

        fPayload = ByteBuffer.allocate(75);
        fPayload.order(ByteOrder.BIG_ENDIAN);

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
        try (PcapFile file = trace.getTrace()) {
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
            PcapOldPacket expected = new PcapOldPacket((PcapOldFile) file, fHeader, fPayload, 36L);
            assertEquals(expected.hashCode(), packet.hashCode());
            assertEquals(expected, packet);

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

            // Packet-specific methods Testing
            assertEquals(36, packet.getIndex());
            assertEquals(75, packet.getOriginalLength());
            assertEquals(75, packet.getIncludedLength());
            assertEquals(1120469632829277L, packet.getTimestamp());
            assertFalse(packet.isTruncated());
        }
    }
}
