/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *   Patrick Tasse - Remove getSubField
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.pcap.core.event;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.pcap.core.packet.Packet;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;

/**
 * Class that represents the root node of Pcap Event Field.
 *
 * @author Vincent Perot
 */
public class PcapRootEventField extends TmfEventField {

    private final TmfEventField fPacketSourceField;
    private final TmfEventField fPacketDestinationField;
    private final TmfEventField fProtocolField;
    private final String fSummaryString;

    /**
     * Full constructor
     *
     * @param fields
     *            The list of subfields.
     * @param packet
     *            The packet from which to take the fields from.
     * @throws IllegalArgumentException
     *             If 'name' is null, or if 'fields' has duplicate field names.
     */
    public PcapRootEventField(ITmfEventField[] fields, Packet packet) {
        super(ITmfEventField.ROOT_FIELD_ID, null, fields);
        fPacketSourceField = new TmfEventField(PcapEvent.EVENT_FIELD_PACKET_SOURCE,
                packet.getMostEcapsulatedPacket().getSourceEndpoint().toString(), null);
        fPacketDestinationField = new TmfEventField(PcapEvent.EVENT_FIELD_PACKET_DESTINATION,
                packet.getMostEcapsulatedPacket().getDestinationEndpoint().toString(), null);
        fProtocolField = new TmfEventField(PcapEvent.EVENT_FIELD_PACKET_PROTOCOL,
                packet.getMostEcapsulatedPacket().getProtocol().getShortName().toUpperCase(), null);
        fSummaryString = packet.getGlobalSummaryString();
    }

    /**
     * Copy constructor
     *
     * @param field
     *            the other event field
     */
    public PcapRootEventField(final PcapRootEventField field) {
        super(field);
        fPacketSourceField = field.fPacketSourceField;
        fPacketDestinationField = field.fPacketDestinationField;
        fProtocolField = field.fProtocolField;
        fSummaryString = field.fSummaryString;
    }

    @Override
    public String toString() {
        return fSummaryString;
    }

    @Override
    public @Nullable ITmfEventField getField(String... path) {
        if (path.length != 1) {
            return super.getField(path);
        }
        switch (path[0]) {
        case PcapEvent.EVENT_FIELD_PACKET_SOURCE:
            return fPacketSourceField;
        case PcapEvent.EVENT_FIELD_PACKET_DESTINATION:
            return fPacketDestinationField;
        case PcapEvent.EVENT_FIELD_PACKET_PROTOCOL:
            return fProtocolField;
        default:
            return super.getField(path);
        }
    }
}
