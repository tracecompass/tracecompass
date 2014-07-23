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

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.pcap.core.tests.shared.PcapTestTrace;
import org.eclipse.linuxtools.pcap.core.trace.BadPcapFileException;
import org.eclipse.linuxtools.pcap.core.trace.PcapFile;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.location.TmfLongLocation;
import org.eclipse.linuxtools.tmf.pcap.core.event.PcapEvent;
import org.eclipse.linuxtools.tmf.pcap.core.protocol.TmfProtocol;
import org.eclipse.linuxtools.tmf.pcap.core.trace.PcapTrace;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * JUnit that test the PcapEvent class.
 *
 * @author Vincent Perot
 */
public class PcapEventTest {

    private static PcapEvent fEvent;
    private static List<TmfProtocol> fProtocolList;

    /**
     * Initialize the Packet and the EventField.
     *
     * @throws BadPcapFileException
     *             Thrown when the pcap file is erroneous.
     * @throws IOException
     *             Thrown when an IO error occurs.
     * @throws TmfTraceException
     *             Thrown when the trace is not valid.
     */
    @BeforeClass
    public static void setUp() throws IOException, BadPcapFileException, TmfTraceException {

        PcapTestTrace trace = PcapTestTrace.MOSTLY_TCP;
        assumeTrue(trace.exists());
        String file = trace.getPath();
        try (PcapFile pcap = new PcapFile(file);
                PcapTrace pcapTrace = new PcapTrace();) {
            pcapTrace.initTrace(null, trace.getPath(), PcapEvent.class);
            fEvent = pcapTrace.parseEvent(new TmfContext(new TmfLongLocation(3), 3));
        }

        // Initialize protocol list.
        List<TmfProtocol> list = new ArrayList<>();
        list.add(TmfProtocol.PCAP);
        list.add(TmfProtocol.ETHERNET_II);
        list.add(TmfProtocol.IPV4);
        list.add(TmfProtocol.TCP);
        list.add(TmfProtocol.UNKNOWN);
        fProtocolList = ImmutableList.copyOf(list);
    }

    /**
     * Method that tests getProtocols of PcapEvent.
     */
    @Test
    public void getProtocolsTest() {
        assertEquals(fProtocolList, fEvent.getProtocols());
    }

    /**
     * Method that tests getMostEncapsulatedProtocol of PcapEvent.
     */
    @Test
    public void getMostEncapsulatedProtocolTest() {
        assertEquals(TmfProtocol.TCP, fEvent.getMostEncapsulatedProtocol());
    }

    /**
     * Method that tests getFields of PcapEvent.
     */
    @Test
    public void getFieldsTest() {
        Map<String, String> map = fEvent.getFields(TmfProtocol.IPV4);
        if (map == null) {
            fail("getFieldsTest() failed because map is null!");
            return;
        }
        assertEquals("145.254.160.237", map.get("Source IP Address"));
    }

    /**
     * Method that tests getPayload of PcapEvent.
     */
    @Test
    public void getPayloadTest() {
        ByteBuffer bb = fEvent.getPayload(TmfProtocol.TCP);
        if (bb == null) {
            fail("getPayloadTest() failed because bb is null!");
            return;
        }
        assertEquals((byte) 0x47, bb.get());
    }

    /**
     * Method that tests getSourceEndpoint of PcapEvent.
     */
    @Test
    public void getSourceEndpointTest() {
        assertEquals("00:00:01:00:00:00/145.254.160.237/3372", fEvent.getSourceEndpoint(TmfProtocol.TCP));
    }

    /**
     * Method that tests getDestinationEndpointTest of PcapEvent.
     */
    @Test
    public void getDestinationEndpointTest() {
        assertEquals("fe:ff:20:00:01:00", fEvent.getDestinationEndpoint(TmfProtocol.ETHERNET_II));
    }

    /**
     * Method that tests toString() of PcapEvent.
     */
    @Test
    public void toStringTest() {
        assertEquals("3372 > 80 [ACK, PSH] Seq=951057940 Ack=290218380 Len=20", fEvent.toString());
    }

    /**
     * Method that tests toString(protocol) of PcapEvent.
     */
    @Test
    public void toStringAtSpecificProtocolTest() {
        assertEquals("Src: 145.254.160.237 , Dst: 65.208.228.223", fEvent.toString(TmfProtocol.IPV4));
    }

}
