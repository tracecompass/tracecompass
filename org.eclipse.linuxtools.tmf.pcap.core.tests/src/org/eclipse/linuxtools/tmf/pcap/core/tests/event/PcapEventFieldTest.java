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
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.pcap.core.tests.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.pcap.core.packet.BadPacketException;
import org.eclipse.linuxtools.pcap.core.packet.Packet;
import org.eclipse.linuxtools.pcap.core.protocol.ipv4.IPv4Packet;
import org.eclipse.linuxtools.pcap.core.tests.shared.PcapTestTrace;
import org.eclipse.linuxtools.pcap.core.trace.BadPcapFileException;
import org.eclipse.linuxtools.pcap.core.trace.PcapFile;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.pcap.core.event.PcapEventField;
import org.eclipse.linuxtools.tmf.pcap.core.event.PcapRootEventField;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit that test the PcapEventField class.
 *
 * @author Vincent Perot
 */
public class PcapEventFieldTest {

    private static final @NonNull String EMPTY_STRING = "";
    private static PcapEventField fRegularField;
    private static PcapRootEventField fRootField;

    /**
     * Initialize the Packet and the Event.
     *
     * @throws BadPcapFileException
     *             Thrown when the pcap file is erroneous.
     * @throws IOException
     *             Thrown when an IO error occurs.
     * @throws BadPacketException
     *             Thrown when the packet is erroneous.
     */
    @BeforeClass
    public static void setUp() throws IOException, BadPcapFileException, BadPacketException {
        ByteBuffer bb = ByteBuffer.allocate(25);
        bb.order(ByteOrder.BIG_ENDIAN);

        // Version + IHL
        bb.put((byte) 0x46);

        // DSCP + ECN
        bb.put((byte) 0x9A);

        // Total length - this is randomly chosen so that we verify that the
        // packet handles wrong total length.
        bb.put((byte) 0x00);
        bb.put((byte) 0xFF);

        // Identification
        bb.put((byte) 0x0F);
        bb.put((byte) 0xF0);

        // Flags + Fragment Offset
        bb.put((byte) 0x1E);
        bb.put((byte) 0xE1);

        // Time to live
        bb.put((byte) 0xA0);

        // Protocol - Unknown
        bb.put((byte) 0xFE);

        // Header checksum - chosen randomly
        bb.put((byte) 0x33);
        bb.put((byte) 0x44);

        // Source IP - 4 bytes
        bb.put((byte) 192);
        bb.put((byte) 168);
        bb.put((byte) 1);
        bb.put((byte) 0);

        // Destination IP - 4 bytes
        bb.put((byte) 193);
        bb.put((byte) 169);
        bb.put((byte) 2);
        bb.put((byte) 1);

        // Options - 4 bytes
        bb.put((byte) 0xA2);
        bb.put((byte) 0x56);
        bb.put((byte) 0xA2);
        bb.put((byte) 0x56);

        // Payload - 1 byte
        bb.put((byte) 0xA6);

        bb.flip();

        PcapTestTrace trace = PcapTestTrace.MOSTLY_TCP;
        assumeTrue(trace.exists());
        try (PcapFile dummy = new PcapFile(trace.getPath())) {
            IPv4Packet packet = new IPv4Packet(dummy, null, bb);
            ITmfEventField[] fieldArray = generatePacketFields(packet);
            fRegularField = new PcapEventField("Regular Field", EMPTY_STRING, fieldArray, packet);
            fRootField = new PcapRootEventField(EMPTY_STRING, fieldArray, packet);
        }

    }

    /**
     * Method that tests the copy constructor.
     */
    @Test
    public void copyConstructorTest() {
        PcapEventField oldField = fRegularField;
        if (oldField == null) {
            fail("The field has not been initialized!");
            return;
        }
        PcapEventField newField = new PcapEventField(oldField);
        assertEquals(fRegularField.hashCode(), newField.hashCode());
        assertEquals(fRegularField, newField);
    }

    /**
     * Method that tests a standard field value request.
     */
    @Test
    public void regularFieldValueRequestTest() {
        ITmfEventField field = fRootField.getField("Internet Protocol Version 4");
        if (field == null) {
            fail("The field is null!");
            return;
        }

        ITmfEventField subfield = field.getField("Source IP Address");
        if (subfield == null) {
            fail("The subfield is null!");
            return;
        }

        String string = subfield.getValue().toString();
        assertEquals("192.168.1.0", string);
    }

    /**
     * Method that tests a custom field value request.
     */
    @Test
    public void customFieldValueRequestTest() {
        ITmfEventField field = fRootField.getField(":protocol:");
        if (field == null) {
            fail("The field is null!");
            return;
        }
        String string = field.getValue().toString();
        assertEquals("IPV4", string);

        field = fRootField.getField(":packetsource:");
        if (field == null) {
            fail("The field is null!");
            return;
        }
        string = field.getValue().toString();
        assertEquals("192.168.1.0", string);

        field = fRootField.getField(":packetdestination:");
        if (field == null) {
            fail("The field is null!");
            return;
        }
        string = field.getValue().toString();
        assertEquals("193.169.2.1", string);

    }

    /**
     * Method that teststhe toString() method for a non-root field.
     */
    @Test
    public void regularToStringTest() {
        assertEquals("Src: 192.168.1.0 , Dst: 193.169.2.1", fRegularField.toString());
    }

    /**
     * Method that teststhe toString() method for a root field.
     */
    @Test
    public void rootToStringTest() {
        assertEquals("192.168.1.0 > 193.169.2.1 Id=4080 Len=1", fRootField.toString());
    }

    // Convenience method
    private static ITmfEventField[] generatePacketFields(Packet packet) {
        List<ITmfEventField> fieldList = new ArrayList<>();
        List<ITmfEventField> subfieldList = new ArrayList<>();
        Packet localPacket = packet;

        while (localPacket != null) {
            subfieldList.clear();
            for (Map.Entry<String, String> entry : localPacket.getFields().entrySet()) {

                @SuppressWarnings("null")
                @NonNull
                String key = entry.getKey();

                @SuppressWarnings("null")
                @NonNull
                String value = entry.getValue();
                subfieldList.add(new TmfEventField(key, value, null));
            }
            ITmfEventField[] subfieldArray = subfieldList.toArray(new ITmfEventField[subfieldList.size()]);
            fieldList.add(new PcapEventField(localPacket.getProtocol().getName(), EMPTY_STRING, subfieldArray, localPacket));
            localPacket = localPacket.getChildPacket();
        }

        ITmfEventField[] fieldArray = fieldList.toArray(new ITmfEventField[fieldList.size()]);
        if (fieldArray == null) {
            return new ITmfEventField[0];
        }
        return fieldArray;
    }

}
