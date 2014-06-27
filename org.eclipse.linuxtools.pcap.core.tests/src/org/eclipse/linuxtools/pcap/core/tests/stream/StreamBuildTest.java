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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.linuxtools.pcap.core.protocol.Protocol;
import org.eclipse.linuxtools.pcap.core.stream.PacketStream;
import org.eclipse.linuxtools.pcap.core.stream.PacketStreamBuilder;
import org.eclipse.linuxtools.pcap.core.tests.shared.PcapTestTrace;
import org.eclipse.linuxtools.pcap.core.trace.BadPcapFileException;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

/**
 * JUnit Class that tests whether packet streams are built correctly.
 *
 * @author Vincent Perot
 */
public class StreamBuildTest {

    // Values taken from wireshark
    private static final Set<Long> TCP_INDEX_SET_STREAM_7_PACKETS = ImmutableSet.of(
            // This stream contains 7 packets.
            17L,
            23L,
            25L,
            26L,
            27L,
            35L,
            36L
            );

    private static final Set<Long> TCP_INDEX_SET_STREAM_34_PACKETS = new TreeSet<>();
    static {
        // This stream contains many packet (34). Some packets doesn't
        // belong to it like the 17 or the 23.
        for (Long i = new Long(0); i < 43; i++) {
            TCP_INDEX_SET_STREAM_34_PACKETS.add(i);
        }
        TCP_INDEX_SET_STREAM_34_PACKETS.removeAll(TCP_INDEX_SET_STREAM_7_PACKETS);
    }

    /**
     * Test that verify that stream building is done correctly.
     */
    @Test
    public void StreamBuildingTest() {
        PcapTestTrace trace = PcapTestTrace.MOSTLY_TCP;
        assumeTrue(trace.exists());

        try {
            String file = trace.getPath();
            // Test Ethernet II stream
            PacketStreamBuilder builder = new PacketStreamBuilder(Protocol.ETHERNET_II);
            builder.parsePcapFile(file);
            assertEquals(Protocol.ETHERNET_II, builder.getProtocol());
            // Should do one loop only, so hardcoded values are okay.
            for (PacketStream stream : builder.getStreams()) {
                assertTrue(stream.toString().contains("Stream eth.0, Number of Packets: 43"));
                for (int i = 0; i < stream.size(); i++) {
                    Long id = stream.get(i).getIndex();
                    String path = stream.get(i).getPath();
                    assertTrue(id >= 0 && id < 43);
                    assertEquals(file, path);
                }
            }

            // Test TCP streams and other constructor
            builder = new PacketStreamBuilder(Protocol.TCP);
            builder.parsePcapFile(file);
            assertEquals(Protocol.TCP, builder.getProtocol());

            PacketStream stream = builder.getStream(0);
            if (stream == null) {
                fail("StreamBuildingTest has failed!");
                return;
            }
            assertEquals(Protocol.TCP, stream.getProtocol());
            assertEquals(0, stream.getID());
            assertEquals("tcp.0", stream.getUniqueID());
            assertEquals(34, stream.size());
            for (int i = 0; i < stream.size(); i++) {
                Long id = stream.get(i).getIndex();
                String path = stream.get(i).getPath();
                assertTrue(TCP_INDEX_SET_STREAM_34_PACKETS.contains(id));
                assertEquals(file, path);
            }

            stream = builder.getStream(1);
            if (stream == null) {
                fail("StreamBuildingTest has failed!");
                return;
            }
            assertEquals(Protocol.TCP, stream.getProtocol());
            assertEquals(1, stream.getID());
            assertEquals("tcp.1", stream.getUniqueID());
            assertEquals(7, stream.size());
            for (int i = 0; i < stream.size(); i++) {
                Long id = stream.get(i).getIndex();
                String path = stream.get(i).getPath();
                assertTrue(TCP_INDEX_SET_STREAM_7_PACKETS.contains(id));
                assertEquals(file, path);
            }

            builder.clear();
            assertEquals(0, builder.getNbStreams());
        } catch (IOException | BadPcapFileException e) {
            fail("StreamBuildingTest has failed!");
        }

    }
}
