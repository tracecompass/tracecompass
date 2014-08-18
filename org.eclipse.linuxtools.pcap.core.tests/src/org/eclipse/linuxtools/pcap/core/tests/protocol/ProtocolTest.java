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

package org.eclipse.linuxtools.pcap.core.tests.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.internal.pcap.core.protocol.PcapProtocol;
import org.eclipse.linuxtools.internal.pcap.core.protocol.PcapProtocolValues;
import org.junit.Test;

/**
 * JUnit Class that tests whether protocol operation are happening without
 * error.
 *
 * @author Vincent Perot
 */
public class ProtocolTest {

    /**
     * Test that verify if the protocol attributes are as expected.
     */
    @Test
    public void TestProtocolAttributes() {
        assertEquals(PcapProtocol.PCAP.getName(), "Packet Capture");
        assertEquals(PcapProtocol.PCAP.getShortName(), "pcap");
        assertEquals(PcapProtocol.PCAP.getLayer(), PcapProtocolValues.LAYER_0);
    }

    /**
     * Test that verify if the protocol getter methods are working properly.
     */
    @Test
    public void TestgetProtocols() {
        List<PcapProtocol> list = new ArrayList<>();
        List<PcapProtocol> manualListLayer = new ArrayList<>();
        for (int i = PcapProtocolValues.LAYER_0; i <= PcapProtocolValues.LAYER_7; i++) {
            List<PcapProtocol> listLayer = PcapProtocol.getProtocolsOnLayer(i);
            list.addAll(listLayer);

            manualListLayer.clear();
            switch (i) {
            case PcapProtocolValues.LAYER_0:
                manualListLayer.add(PcapProtocol.PCAP);
                break;
            case PcapProtocolValues.LAYER_1:
                break;
            case PcapProtocolValues.LAYER_2:
                manualListLayer.add(PcapProtocol.ETHERNET_II);
                break;
            case PcapProtocolValues.LAYER_3:
                manualListLayer.add(PcapProtocol.IPV4);
                break;
            case PcapProtocolValues.LAYER_4:
                manualListLayer.add(PcapProtocol.TCP);
                manualListLayer.add(PcapProtocol.UDP);
                break;
            case PcapProtocolValues.LAYER_5:
                break;
            case PcapProtocolValues.LAYER_6:
                break;
            case PcapProtocolValues.LAYER_7:
                manualListLayer.add(PcapProtocol.UNKNOWN);
                break;
            default:
                fail("Illegal layer value!");
            }
            assertEquals(manualListLayer, listLayer);
        }
        assertEquals(PcapProtocol.getAllProtocols(), list);

    }

}
