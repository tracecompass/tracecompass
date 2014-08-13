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

package org.eclipse.linuxtools.internal.tmf.pcap.core.util;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.pcap.core.packet.Packet;
import org.eclipse.linuxtools.pcap.core.protocol.Protocol;
import org.eclipse.linuxtools.pcap.core.protocol.pcap.PcapPacket;
import org.eclipse.linuxtools.pcap.core.trace.PcapFile;
import org.eclipse.linuxtools.pcap.core.util.LinkTypeHelper;
import org.eclipse.linuxtools.pcap.core.util.PcapTimestampScale;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.pcap.core.event.PcapEvent;
import org.eclipse.linuxtools.tmf.pcap.core.event.PcapEventField;
import org.eclipse.linuxtools.tmf.pcap.core.event.PcapEventType;
import org.eclipse.linuxtools.tmf.pcap.core.event.PcapRootEventField;
import org.eclipse.linuxtools.tmf.pcap.core.trace.PcapTrace;

/**
 * Factory class that helps generating Pcap Events.
 *
 * @author Vincent Perot
 */
public class PcapEventFactory {

    private static final ITmfEventField[] EMPTY_FIELD_ARRAY = new ITmfEventField[0];
    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private static final Map<Protocol, TmfEventType> fEventTypes = new HashMap<>();

    private PcapEventFactory() {
    }

    /**
     * Method that create a PcapEvent from a packet.
     *
     * @param pcapPacket
     *            The packet to generate the event from.
     * @param pcap
     *            The pcap file to which the packet belongs.
     * @param trace
     *            The trace to which this packet belongs.
     * @return The generated PcapEvent.
     */
    public static @Nullable PcapEvent createEvent(PcapPacket pcapPacket, PcapFile pcap, PcapTrace trace) {
        long rank = pcapPacket.getIndex();
        long timestamp = pcapPacket.getTimestamp();
        PcapTimestampScale scale = pcapPacket.getTimestampScale();
        ITmfTimestamp tmfTimestamp;
        switch (scale) {
        case MICROSECOND:
            tmfTimestamp = new TmfTimestamp(timestamp, ITmfTimestamp.MICROSECOND_SCALE, (int) pcap.getTimeAccuracy());
            break;
        case NANOSECOND:
            tmfTimestamp = new TmfTimestamp(timestamp, ITmfTimestamp.NANOSECOND_SCALE, (int) pcap.getTimeAccuracy());
            break;
        default:
            throw new IllegalArgumentException("The timestamp precision is not valid!"); //$NON-NLS-1$
        }
        Path filePath = pcap.getPath().getFileName();
        @SuppressWarnings("null") // for .toString()
        @NonNull String fileName = (filePath == null ? EMPTY_STRING : filePath.toString());

        String dataLink = Messages.PcapEventFactory_LinkType + ':' + LinkTypeHelper.toString((int) pcapPacket.getPcapFile().getDataLinkType());

        ITmfEventField[] fields = generatePacketFields(pcapPacket);
        ITmfEventField field = new PcapRootEventField(EMPTY_STRING, fields, pcapPacket);
        Packet packet = pcapPacket.getMostEcapsulatedPacket();
        if (!fEventTypes.containsKey(packet.getProtocol())) {
            String typeIdString = PcapEventType.DEFAULT_PCAP_TYPE_ID + ':' + packet.getProtocol().getShortName();
            fEventTypes.put(packet.getProtocol(), new PcapEventType(typeIdString, null));
        }
        TmfEventType eventType = fEventTypes.get(packet.getProtocol());
        if (eventType == null) {
            eventType = new TmfEventType();
        }
        return new PcapEvent(trace, rank, tmfTimestamp, dataLink, eventType, field, fileName, packet);

    }

    private static ITmfEventField[] generatePacketFields(Packet packet) {
        // TODO This is SOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO slow. Must find a
        // way to use less intermediate data structures.
        List<ITmfEventField> fieldList = new ArrayList<>();
        List<ITmfEventField> subfieldList = new ArrayList<>();
        Packet localPacket = packet.getPacket(Protocol.PCAP);

        while (localPacket != null) {
            subfieldList.clear();
            for (Map.Entry<String, String> entry : localPacket.getFields().entrySet()) {

                @SuppressWarnings("null")
                @NonNull String key = entry.getKey();

                @SuppressWarnings("null")
                @NonNull String value = entry.getValue();
                subfieldList.add(new TmfEventField(key, value, null));
            }
            ITmfEventField[] subfieldArray = subfieldList.toArray(new ITmfEventField[subfieldList.size()]);
            fieldList.add(new PcapEventField(localPacket.getProtocol().getName(), EMPTY_STRING, subfieldArray, localPacket));
            localPacket = localPacket.getChildPacket();
        }

        ITmfEventField[] fieldArray = fieldList.toArray(new ITmfEventField[fieldList.size()]);
        if (fieldArray == null) {
            return EMPTY_FIELD_ARRAY;
        }
        return fieldArray;
    }
}
