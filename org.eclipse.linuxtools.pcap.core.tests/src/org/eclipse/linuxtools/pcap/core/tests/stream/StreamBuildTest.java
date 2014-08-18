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

package org.eclipse.linuxtools.pcap.core.tests.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;

import org.eclipse.linuxtools.internal.pcap.core.protocol.PcapProtocol;
import org.eclipse.linuxtools.internal.pcap.core.stream.PacketStream;
import org.eclipse.linuxtools.internal.pcap.core.stream.PacketStreamBuilder;
import org.eclipse.linuxtools.internal.pcap.core.trace.BadPcapFileException;
import org.eclipse.linuxtools.pcap.core.tests.shared.PcapTestTrace;
import org.junit.Test;

/**
 * JUnit Class that tests whether packet streams are built correctly.
 *
 * @author Vincent Perot
 */
public class StreamBuildTest {

    private static final double DELTA = 0.001;

    /**
     * Test that verify that stream building is done correctly.
     */
    @Test
    public void StreamBuildingTest() {
        PcapTestTrace trace = PcapTestTrace.MOSTLY_TCP;
        assumeTrue(trace.exists());

        try {
            // Test Ethernet II stream
            PacketStreamBuilder builder = new PacketStreamBuilder(PcapProtocol.ETHERNET_II);
            builder.parsePcapFile(trace.getPath());
            assertEquals(PcapProtocol.ETHERNET_II, builder.getProtocol());
            // Should do one loop only, so hardcoded values are okay.
            for (PacketStream stream : builder.getStreams()) {
                assertEquals("Stream eth.0, Number of Packets: 43\n", stream.toString());
                assertEquals(43, stream.getNbPackets());
                assertEquals(25091, stream.getNbBytes());
                assertEquals(20, stream.getNbPacketsAtoB());
                assertEquals(2323, stream.getNbBytesAtoB());
                assertEquals(23, stream.getNbPacketsBtoA());
                assertEquals(22768, stream.getNbBytesBtoA());
                assertEquals(1084443427311224000L, stream.getStartTime());
                assertEquals(1084443457704928000L, stream.getStopTime());
                assertEquals(30.393704, stream.getDuration(), DELTA);
                assertEquals(76.43030280218561, stream.getBPSAtoB(), DELTA);
                assertEquals(749.1025114938278, stream.getBPSBtoA(), DELTA);
            }

            // Test TCP streams and other constructor
            builder = new PacketStreamBuilder(PcapProtocol.TCP);
            builder.parsePcapFile(trace.getPath());
            assertEquals(PcapProtocol.TCP, builder.getProtocol());

            PacketStream stream = builder.getStream(0);
            if (stream == null) {
                fail("StreamBuildingTest has failed!");
                return;
            }
            assertEquals(PcapProtocol.TCP, stream.getProtocol());
            assertEquals(0, stream.getID());
            assertEquals("tcp.0", stream.getUniqueID());
            assertEquals(34, stream.getNbPackets());
            assertEquals(20695, stream.getNbBytes());
            assertEquals(16, stream.getNbPacketsAtoB());
            assertEquals(1351, stream.getNbBytesAtoB());
            assertEquals(18, stream.getNbPacketsBtoA());
            assertEquals(19344, stream.getNbBytesBtoA());
            assertEquals(1084443427311224000L, stream.getStartTime());
            assertEquals(1084443457704928000L, stream.getStopTime());
            assertEquals(30.393704, stream.getDuration(), DELTA);
            assertEquals(44.449995301658525, stream.getBPSAtoB(), DELTA);
            assertEquals(636.4476011216008, stream.getBPSBtoA(), DELTA);

            stream = builder.getStream(1);
            if (stream == null) {
                fail("StreamBuildingTest has failed!");
                return;
            }
            assertEquals(PcapProtocol.TCP, stream.getProtocol());
            assertEquals(1, stream.getID());
            assertEquals("tcp.1", stream.getUniqueID());
            assertEquals(7, stream.getNbPackets());
            assertEquals(4119, stream.getNbBytes());
            assertEquals(3, stream.getNbPacketsAtoB());
            assertEquals(883, stream.getNbBytesAtoB());
            assertEquals(4, stream.getNbPacketsBtoA());
            assertEquals(3236, stream.getNbBytesBtoA());
            assertEquals(1084443430295515000L, stream.getStartTime());
            assertEquals(1084443432088092000L, stream.getStopTime());
            assertEquals(1.792577, stream.getDuration(), DELTA);
            assertEquals(492.58692932019096, stream.getBPSAtoB(), DELTA);
            assertEquals(1805.2223140205413, stream.getBPSBtoA(), DELTA);

            builder.clear();
            assertEquals(0, builder.getNbStreams());
        } catch (IOException | BadPcapFileException e) {
            fail("StreamBuildingTest has failed!");
        }

    }
}
