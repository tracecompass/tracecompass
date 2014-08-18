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

package org.eclipse.linuxtools.pcap.core.tests.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;

import org.eclipse.linuxtools.internal.pcap.core.packet.BadPacketException;
import org.eclipse.linuxtools.internal.pcap.core.protocol.PcapProtocol;
import org.eclipse.linuxtools.internal.pcap.core.protocol.pcap.PcapPacket;
import org.eclipse.linuxtools.internal.pcap.core.trace.BadPcapFileException;
import org.eclipse.linuxtools.internal.pcap.core.trace.PcapFile;
import org.eclipse.linuxtools.pcap.core.tests.shared.PcapTestTrace;
import org.junit.Test;

/**
 * JUnit Class that tests if packets are read without error.
 *
 * @author Vincent Perot
 */
public class PcapFileReadTest {

    /**
     * Test that verify that packets are well read and that no error happens in
     * file index.
     *
     * @throws BadPcapFileException
     *             Thrown when the file is erroneous. Fails the test.
     * @throws IOException
     *             Thrown when an IO error occurs. Fails the test.
     * @throws BadPacketException
     *             Thrown when a packet is erroneous. Fails the test.
     */
    @Test
    public void FileReadTest() throws IOException, BadPcapFileException, BadPacketException {

        PcapTestTrace trace = PcapTestTrace.MOSTLY_UDP;
        assumeTrue(trace.exists());

        try (PcapFile file = new PcapFile(trace.getPath());) {

            PcapPacket packet = file.parseNextPacket();
            if (packet == null) {
                fail("FileReadTest() failed!");
                return;
            }

            assertEquals(1, file.getCurrentRank());
            // Verify Pcap packet.
            assertEquals(file, packet.getPcapFile());
            assertEquals(PcapProtocol.PCAP, packet.getProtocol());
            assertEquals(0, packet.getIndex());
            assertEquals(1120469540839312L, packet.getTimestamp());
            assertEquals(92, packet.getOriginalLength());
            assertEquals(92, packet.getIncludedLength());
            assertEquals(false, packet.isTruncated());
            assertEquals(true, packet.validate());
            // Verify Ethernet Packet
            if (!packet.hasProtocol(PcapProtocol.ETHERNET_II)) {
                fail("Packet doesn't have ethernet!");
            }
            // Verify IPv4 Packet
            if (!packet.hasProtocol(PcapProtocol.IPV4)) {
                fail("Packet doesn't have IPv4!");
            }
            // Verify UDP Packet
            if (!packet.hasProtocol(PcapProtocol.UDP)) {
                fail("Packet doesn't have UDP!");
            }
            // Verify Unknown Packet
            if (!packet.hasProtocol(PcapProtocol.UNKNOWN)) {
                fail("Packet doesn't have payload!");
            }

            // Parse a "random" packet
            file.seekPacket(58);
            packet = file.parseNextPacket();
            if (packet == null) {
                fail("FileReadTest() failed!");
                return;
            }

            // Verify Pcap packet.
            assertEquals(file, packet.getPcapFile());
            assertEquals(PcapProtocol.PCAP, packet.getProtocol());
            assertEquals(58, packet.getIndex());
            assertEquals(1120469635045415L, packet.getTimestamp());
            assertEquals(113, packet.getOriginalLength());
            assertEquals(113, packet.getIncludedLength());
            assertEquals(false, packet.isTruncated());
            assertEquals(true, packet.validate());
            // Verify Ethernet Packet
            if (!packet.hasProtocol(PcapProtocol.ETHERNET_II)) {
                fail("Packet doesn't have ethernet!");
            }
            // Verify IPv4 Packet
            if (!packet.hasProtocol(PcapProtocol.IPV4)) {
                fail("Packet doesn't have IPv4!");
            }
            // Verify TCP Packet
            if (!packet.hasProtocol(PcapProtocol.TCP)) {
                fail("Packet doesn't have TCP!");
            }
            // Verify Unknown Packet
            if (!packet.hasProtocol(PcapProtocol.UNKNOWN)) {
                fail("Packet doesn't have payload!");
            }

            // Skip packet
            file.skipNextPacket();
            assertEquals(60, file.getCurrentRank());

            // Parse outside of file.
            file.seekPacket(99999999);
            assertEquals(file.getTotalNbPackets(), file.getCurrentRank());
            file.skipNextPacket(); // Should be a no-op
            assertEquals(file.getTotalNbPackets(), file.getCurrentRank());
            packet = file.parseNextPacket();
            assertNull(packet);
        }
    }
}
