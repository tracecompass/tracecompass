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

package org.eclipse.linuxtools.pcap.core.tests.protocol.unknown;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

import org.eclipse.linuxtools.pcap.core.protocol.Protocol;
import org.eclipse.linuxtools.pcap.core.protocol.unknown.UnknownEndpoint;
import org.eclipse.linuxtools.pcap.core.protocol.unknown.UnknownPacket;
import org.eclipse.linuxtools.pcap.core.tests.shared.PcapTestTrace;
import org.eclipse.linuxtools.pcap.core.trace.BadPcapFileException;
import org.eclipse.linuxtools.pcap.core.trace.PcapFile;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

/**
 * JUnit Class that tests the UnknownPacket class and its method.
 *
 * @author Vincent Perot
 */
public class UnknownPacketTest {

    private static final Map<String, String> EXPECTED_FIELDS = ImmutableMap.of(
            "Binary", "61",
            "Character", "a"
            );

    private static final String fToString = "Payload: 61";

    private ByteBuffer fPacket;

    /**
     * Initialize the packet.
     */
    @Before
    public void initialize() {
        fPacket = ByteBuffer.allocate(1);
        fPacket.order(ByteOrder.BIG_ENDIAN);

        // Payload - 1 byte
        fPacket.put((byte) 97);

        fPacket.flip();
    }

    /**
     * Test that verify the correctness of the UnknownPacket's methods.
     * @throws BadPcapFileException
     *             Thrown when the file is erroneous. Fails the test.
     * @throws IOException
     *             Thrown when an IO error occurs. Fails the test.
     */
    @Test
    public void CompleteUnknownPacketTest() throws IOException, BadPcapFileException {
        PcapTestTrace trace = PcapTestTrace.MOSTLY_TCP;
        assumeTrue(trace.exists());
        try (PcapFile dummy = new PcapFile(trace.getPath())) {
            ByteBuffer byteBuffer = fPacket;
            if (byteBuffer == null) {
                fail("CompleteUnknownPacketTest has failed!");
                return;
            }
            UnknownPacket packet = new UnknownPacket(dummy, null, byteBuffer);

            // Protocol Testing
            assertEquals(Protocol.UNKNOWN, packet.getProtocol());
            assertTrue(packet.hasProtocol(Protocol.UNKNOWN));
            assertFalse(packet.hasProtocol(Protocol.UDP));

            // Abstract methods Testing
            assertTrue(packet.validate());
            assertEquals(1089, packet.hashCode());
            assertFalse(packet.equals(null));
            assertEquals(new UnknownPacket(dummy, null, byteBuffer), packet);

            assertEquals(EXPECTED_FIELDS, packet.getFields());
            assertEquals(fToString, packet.toString());
            assertEquals("Len: 1 bytes", packet.getLocalSummaryString());
            assertEquals("Data: 1 bytes", packet.getGlobalSummaryString());
            // TODO take care of plural form.

            // Unknown Endpoints are never equal!
            assertFalse(packet.getSourceEndpoint().equals(new UnknownEndpoint(packet, true)));
            assertFalse(packet.getDestinationEndpoint().equals(new UnknownEndpoint(packet, false)));

            fPacket.position(0);
            byte[] payload = new byte[1];
            fPacket.get(payload);
            ByteBuffer payloadBB = ByteBuffer.wrap(payload);
            payloadBB.flip();

            assertEquals(payloadBB, packet.getPayload());

            // Packet-specific methods Testing
            // None

        }
    }
}
