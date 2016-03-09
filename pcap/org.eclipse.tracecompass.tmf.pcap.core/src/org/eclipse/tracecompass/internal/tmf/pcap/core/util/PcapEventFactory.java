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

package org.eclipse.tracecompass.internal.tmf.pcap.core.util;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.pcap.core.packet.Packet;
import org.eclipse.tracecompass.internal.pcap.core.protocol.PcapProtocol;
import org.eclipse.tracecompass.internal.pcap.core.protocol.pcap.PcapPacket;
import org.eclipse.tracecompass.internal.pcap.core.trace.PcapFile;
import org.eclipse.tracecompass.internal.pcap.core.util.LinkTypeHelper;
import org.eclipse.tracecompass.internal.pcap.core.util.PcapTimestampScale;
import org.eclipse.tracecompass.internal.tmf.pcap.core.event.PcapEvent;
import org.eclipse.tracecompass.internal.tmf.pcap.core.event.PcapEventField;
import org.eclipse.tracecompass.internal.tmf.pcap.core.event.PcapEventType;
import org.eclipse.tracecompass.internal.tmf.pcap.core.event.PcapRootEventField;
import org.eclipse.tracecompass.internal.tmf.pcap.core.trace.PcapTrace;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;

/**
 * Factory class that helps generating Pcap Events.
 *
 * @author Vincent Perot
 */
public class PcapEventFactory {

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private static final Map<PcapProtocol, TmfEventType> fEventTypes = new HashMap<>();

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
            long us = trace.getTimestampTransform().transform(timestamp * 1000) / 1000;
            tmfTimestamp = new TmfTimestamp(us, ITmfTimestamp.MICROSECOND_SCALE);
            break;
        case NANOSECOND:
            long ns = trace.getTimestampTransform().transform(timestamp);
            tmfTimestamp = new TmfTimestamp(ns, ITmfTimestamp.NANOSECOND_SCALE);
            break;
        default:
            throw new IllegalArgumentException("The timestamp precision is not valid!"); //$NON-NLS-1$
        }
        Path filePath = pcap.getPath().getFileName();
        @NonNull String fileName = (filePath == null ? EMPTY_STRING : checkNotNull(filePath.toString()));

        String dataLink = Messages.PcapEventFactory_LinkType + ':' + LinkTypeHelper.toString((int) pcapPacket.getPcapFile().getDataLinkType());

        ITmfEventField[] fields = generatePacketFields(pcapPacket);
        ITmfEventField field = new PcapRootEventField(fields, pcapPacket);
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
        Packet localPacket = packet.getPacket(PcapProtocol.PCAP);

        while (localPacket != null) {
            subfieldList.clear();
            for (Map.Entry<String, String> entry : localPacket.getFields().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                subfieldList.add(new TmfEventField(key, value, null));
            }
            ITmfEventField[] subfieldArray = subfieldList.toArray(new ITmfEventField[subfieldList.size()]);
            fieldList.add(new PcapEventField(localPacket.getProtocol().getName(), EMPTY_STRING, subfieldArray, localPacket));
            localPacket = localPacket.getChildPacket();
        }

        return fieldList.toArray(new ITmfEventField[fieldList.size()]);
    }
}
