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

import org.eclipse.linuxtools.internal.pcap.core.protocol.Protocol;
import org.eclipse.linuxtools.internal.pcap.core.protocol.ProtocolValues;
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
        assertEquals(Protocol.PCAP.getName(), "Packet Capture");
        assertEquals(Protocol.PCAP.getShortName(), "pcap");
        assertEquals(Protocol.PCAP.getLayer(), ProtocolValues.LAYER_0);
    }

    /**
     * Test that verify if the protocol getter methods are working properly.
     */
    @Test
    public void TestgetProtocols() {
        List<Protocol> list = new ArrayList<>();
        List<Protocol> manualListLayer = new ArrayList<>();
        for (int i = ProtocolValues.LAYER_0; i <= ProtocolValues.LAYER_7; i++) {
            List<Protocol> listLayer = Protocol.getProtocolsOnLayer(i);
            list.addAll(listLayer);

            manualListLayer.clear();
            switch (i) {
            case ProtocolValues.LAYER_0:
                manualListLayer.add(Protocol.PCAP);
                break;
            case ProtocolValues.LAYER_1:
                break;
            case ProtocolValues.LAYER_2:
                manualListLayer.add(Protocol.ETHERNET_II);
                break;
            case ProtocolValues.LAYER_3:
                manualListLayer.add(Protocol.IPV4);
                break;
            case ProtocolValues.LAYER_4:
                manualListLayer.add(Protocol.TCP);
                manualListLayer.add(Protocol.UDP);
                break;
            case ProtocolValues.LAYER_5:
                break;
            case ProtocolValues.LAYER_6:
                break;
            case ProtocolValues.LAYER_7:
                manualListLayer.add(Protocol.UNKNOWN);
                break;
            default:
                fail("Illegal layer value!");
            }
            assertEquals(manualListLayer, listLayer);
        }
        assertEquals(Protocol.getAllProtocols(), list);

    }

}
