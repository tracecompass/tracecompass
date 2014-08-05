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

package org.eclipse.linuxtools.tmf.pcap.core.event;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.pcap.core.packet.Packet;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;

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
     * @param value
     *            The event field value.
     * @param fields
     *            The list of subfields.
     * @param packet
     *            The packet from which to take the fields from.
     * @throws IllegalArgumentException
     *             If 'name' is null, or if 'fields' has duplicate field names.
     */
    public PcapRootEventField(Object value, @Nullable ITmfEventField[] fields, Packet packet) {
        super(ITmfEventField.ROOT_FIELD_ID, value, fields);
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
    public @Nullable ITmfEventField getField(@Nullable String name) {
        if (name == null) {
            return null;
        }
        switch (name) {
        case PcapEvent.EVENT_FIELD_PACKET_SOURCE:
            return fPacketSourceField;
        case PcapEvent.EVENT_FIELD_PACKET_DESTINATION:
            return fPacketDestinationField;
        case PcapEvent.EVENT_FIELD_PACKET_PROTOCOL:
            return fProtocolField;
        default:
            return super.getField(name);
        }
    }
}
