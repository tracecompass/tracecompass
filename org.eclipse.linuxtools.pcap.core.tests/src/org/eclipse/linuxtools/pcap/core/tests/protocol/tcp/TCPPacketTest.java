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

package org.eclipse.linuxtools.pcap.core.tests.protocol.tcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.linuxtools.internal.pcap.core.packet.BadPacketException;
import org.eclipse.linuxtools.internal.pcap.core.protocol.Protocol;
import org.eclipse.linuxtools.internal.pcap.core.protocol.tcp.TCPEndpoint;
import org.eclipse.linuxtools.internal.pcap.core.protocol.tcp.TCPPacket;
import org.eclipse.linuxtools.internal.pcap.core.trace.BadPcapFileException;
import org.eclipse.linuxtools.internal.pcap.core.trace.PcapFile;
import org.eclipse.linuxtools.pcap.core.tests.shared.PcapTestTrace;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit Class that tests the TCPPacket class and its method.
 *
 * @author Vincent Perot
 */
public class TCPPacketTest {

    private static final Map<String, String> EXPECTED_FIELDS;
    static {
        EXPECTED_FIELDS = new LinkedHashMap<>();
        EXPECTED_FIELDS.put("Source Port", "18057");
        EXPECTED_FIELDS.put("Destination Port", "39611");
        EXPECTED_FIELDS.put("Sequence Number", "2575857510");
        EXPECTED_FIELDS.put("Acknowledgement Number", "1430532898");
        EXPECTED_FIELDS.put("Length", "24 bytes");
        EXPECTED_FIELDS.put("ECN-Nonce Flag", "true");
        EXPECTED_FIELDS.put("Congestion Window Reduced Flag", "false");
        EXPECTED_FIELDS.put("ECN-Echo Flag", "true");
        EXPECTED_FIELDS.put("Urgent Flag", "false");
        EXPECTED_FIELDS.put("ACK Flag", "true");
        EXPECTED_FIELDS.put("PSH Flag", "false");
        EXPECTED_FIELDS.put("RST Flag", "true");
        EXPECTED_FIELDS.put("SYN Flag", "false");
        EXPECTED_FIELDS.put("FIN Flag", "true");
        EXPECTED_FIELDS.put("Window Size Value", "4352");
        EXPECTED_FIELDS.put("Checksum", "0xffee");
        EXPECTED_FIELDS.put("Urgent Pointer", "0xddcc");
        EXPECTED_FIELDS.put("Options", "ad da bc cb");
    }

    private static final String EXPECTED_TOSTRING;
    static {
        StringBuilder sb = new StringBuilder();
        sb.append("Transmission Control Protocol, Source Port: 18057, Destination Port: 39611\n");
        sb.append("Sequence Number: 2575857510, Acknowledgment Number: 1430532898\n");
        sb.append("Header length: 24 bytes, Data length: 4\n");
        sb.append("Window size value: 4352, Urgent Pointer: 0xddcc\n");
        sb.append("Checksum: 0xffee\n");
        sb.append("Payload: 99 88 77 66");

        EXPECTED_TOSTRING = sb.toString();
    }

    private ByteBuffer fPacket;

    /**
     * Initialize the packet.
     */
    @Before
    public void initialize() {
        fPacket = ByteBuffer.allocate(28);
        fPacket.order(ByteOrder.BIG_ENDIAN);

        // Source Port
        fPacket.put((byte) 0x46);
        fPacket.put((byte) 0x89);

        // Destination Port
        fPacket.put((byte) 0x9A);
        fPacket.put((byte) 0xBB);

        // Sequence Number
        fPacket.put((byte) 0x99);
        fPacket.put((byte) 0x88);
        fPacket.put((byte) 0x77);
        fPacket.put((byte) 0x66);

        // Acknowledgment Number
        fPacket.put((byte) 0x55);
        fPacket.put((byte) 0x44);
        fPacket.put((byte) 0x33);
        fPacket.put((byte) 0x22);

        // Data Offset + Reserved + NS
        fPacket.put((byte) 0x61);

        // Other flags
        fPacket.put((byte) 0b01010101);

        // Window Size
        fPacket.put((byte) 0x11);
        fPacket.put((byte) 0x00);

        // Checksum
        fPacket.put((byte) 0xFF);
        fPacket.put((byte) 0xEE);

        // Urgent Pointer
        fPacket.put((byte) 0xDD);
        fPacket.put((byte) 0xCC);

        // Options - 4 bytes
        fPacket.put((byte) 0xAD);
        fPacket.put((byte) 0xDA);
        fPacket.put((byte) 0xBC);
        fPacket.put((byte) 0xCB);

        // Payload - 4 bytes
        fPacket.put((byte) 0x99);
        fPacket.put((byte) 0x88);
        fPacket.put((byte) 0x77);
        fPacket.put((byte) 0x66);

        fPacket.flip();
    }

    /**
     * Test that verify the correctness of the TCPPacket's methods.
     * @throws BadPcapFileException
     *             Thrown when the file is erroneous. Fails the test.
     * @throws IOException
     *             Thrown when an IO error occurs. Fails the test.
     * @throws BadPacketException
     *             Thrown when a packet is erroneous. Fails the test.
     */
    @Test
    public void CompleteTCPPacketTest() throws BadPacketException, IOException, BadPcapFileException {
        PcapTestTrace trace = PcapTestTrace.MOSTLY_TCP;
        assumeTrue(trace.exists());
        try (PcapFile dummy = new PcapFile(trace.getPath())) {
            ByteBuffer byteBuffer = fPacket;
            if (byteBuffer == null) {
                fail("CompleteTCPPacketTest has failed!");
                return;
            }
            TCPPacket packet = new TCPPacket(dummy, null, byteBuffer);

            // Protocol Testing
            assertEquals(Protocol.TCP, packet.getProtocol());
            assertTrue(packet.hasProtocol(Protocol.TCP));
            assertTrue(packet.hasProtocol(Protocol.UNKNOWN));
            assertFalse(packet.hasProtocol(Protocol.IPV4));

            // Abstract methods Testing
            assertTrue(packet.validate());
            assertEquals(-677046102, packet.hashCode());
            assertFalse(packet.equals(null));
            assertEquals(new TCPPacket(dummy, null, byteBuffer), packet);

            assertEquals(EXPECTED_FIELDS, packet.getFields());
            assertEquals(EXPECTED_TOSTRING, packet.toString());
            assertEquals("Src Port: 18057, Dst Port: 39611, Seq: 2575857510, Ack: 1430532898, Len: 24", packet.getLocalSummaryString());
            assertEquals("18057 > 39611 [ACK, FIN, RST, NS, ECE] Seq=2575857510 Ack=1430532898 Len=24", packet.getGlobalSummaryString());

            assertEquals(new TCPEndpoint(packet, true), packet.getSourceEndpoint());
            assertEquals(new TCPEndpoint(packet, false), packet.getDestinationEndpoint());

            fPacket.position(24);
            byte[] payload = new byte[4];
            fPacket.get(payload);
            assertEquals(ByteBuffer.wrap(payload), packet.getPayload());

            // Packet-specific methods Testing
            assertEquals(0x4689, packet.getSourcePort());
            assertEquals(0x9ABB, packet.getDestinationPort());
            assertEquals(2575857510L, packet.getSequenceNumber());
            assertEquals(1430532898L, packet.getAcknowledgmentNumber());
            assertEquals(6, packet.getDataOffset());
            assertEquals(0, packet.getReservedField());
            assertEquals(true, packet.isNSFlagSet());
            assertEquals(false, packet.isCongestionWindowReducedFlagSet());
            assertEquals(true, packet.isECNEchoFlagSet());
            assertEquals(false, packet.isUrgentFlagSet());
            assertEquals(true, packet.isAcknowledgeFlagSet());
            assertEquals(false, packet.isPushFlagSet());
            assertEquals(true, packet.isResetFlagSet());
            assertEquals(false, packet.isSynchronizationFlagSet());
            assertEquals(true, packet.isFinalFlagSet());
            assertEquals(4352, packet.getWindowSize());
            assertEquals(65518, packet.getChecksum());
            assertEquals(56780, packet.getUrgentPointer());
            assertTrue(Arrays.equals(packet.getOptions(), Arrays.copyOfRange(fPacket.array(), 20, 24)));

        }
    }
}
