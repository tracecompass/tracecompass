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

package org.eclipse.tracecompass.internal.tmf.pcap.core.event;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.pcap.core.packet.Packet;
import org.eclipse.tracecompass.internal.pcap.core.protocol.PcapProtocol;
import org.eclipse.tracecompass.internal.tmf.pcap.core.protocol.TmfPcapProtocol;
import org.eclipse.tracecompass.internal.tmf.pcap.core.util.ProtocolConversion;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableList;

/**
 * Class that extends TmfEvent to allow TMF to use the packets from the parser.
 * It is a simple TmfEvent that wraps a Packet.
 *
 * @author Vincent Perot
 */
public class PcapEvent extends TmfEvent {

    /** Packet Source Field ID */
    public static final String EVENT_FIELD_PACKET_SOURCE = ":packetsource:"; //$NON-NLS-1$
    /** Packet Destination Field ID */
    public static final String EVENT_FIELD_PACKET_DESTINATION = ":packetdestination:"; //$NON-NLS-1$
    /** Packet Protocol Field ID */
    public static final String EVENT_FIELD_PACKET_PROTOCOL = ":protocol:"; //$NON-NLS-1$

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private final Packet fPacket;
    private final String fSource;
    private final String fReference;

    /**
     * Lazy-loaded field representing all the protocols in this event
     */
    private transient @Nullable Collection<TmfPcapProtocol> fProtocols;

    /**
     * Full constructor.
     *
     * @param trace
     *            the parent trace
     * @param rank
     *            the event rank (in the trace)
     * @param timestamp
     *            the event timestamp
     * @param source
     *            the event source
     * @param type
     *            the event type
     * @param content
     *            the event content (payload)
     * @param reference
     *            the event reference
     * @param packet
     *            The packet contained in this event
     */
    public PcapEvent(ITmfTrace trace,
            long rank,
            ITmfTimestamp timestamp,
            String source,
            TmfEventType type,
            ITmfEventField content,
            String reference,
            Packet packet) {

        super(trace, rank, timestamp, type, content);
        fPacket = packet;
        fSource = source;
        fReference = reference;
    }

    /**
     * Return this event's source.
     *
     * @return The event's source
     */
    public String getSource() {
        return fSource;
    }

    /**
     * Return this event's reference
     *
     * @return The event's reference
     */
    public String getReference() {
        return fReference;
    }

    /**
     * Method that returns an immutable map containing all the fields of a
     * packet at a certain protocol. For instance, to get the Source IP Address,
     * use:
     * <code>event.getFields(TmfProtocol.IPV4).get("Source IP Address");</code>. <br>
     * It returns null if the protocol is inexistent in the PcapEvent.
     *
     * @param protocol
     *            The specified protocol
     * @return A map containing the fields.
     */
    public @Nullable Map<String, String> getFields(TmfPcapProtocol protocol) {
        PcapProtocol p = ProtocolConversion.unwrap(protocol);
        Packet packet = fPacket.getPacket(p);
        if (packet == null) {
            return null;
        }
        return packet.getFields();
    }

    /**
     * Method that returns the payload at a certain protocol level. It returns
     * null if the protocol is inexistent in the PcapEvent.
     *
     * @param protocol
     *            The specified protocol
     * @return The payload as a ByteBuffer.
     */
    public @Nullable ByteBuffer getPayload(TmfPcapProtocol protocol) {
        PcapProtocol p = ProtocolConversion.unwrap(protocol);
        Packet packet = fPacket.getPacket(p);
        if (packet == null) {
            return null;
        }
        return packet.getPayload();
    }

    /**
     * Method that returns the source endpoint at a certain protocol level. It
     * returns null if the protocol is inexistent in the PcapEvent.
     *
     * @param protocol
     *            The specified protocol
     * @return The source endpoint.
     */
    public @Nullable String getSourceEndpoint(TmfPcapProtocol protocol) {
        PcapProtocol p = ProtocolConversion.unwrap(protocol);
        Packet packet = fPacket.getPacket(p);
        if (packet == null) {
            return null;
        }
        return packet.getSourceEndpoint().toString();
    }

    /**
     * Method that returns the destination endpoint at a certain protocol level.
     * It returns null if the protocol is inexistent in the PcapEvent.
     *
     * @param protocol
     *            The specified protocol
     * @return The destination endpoint.
     */
    public @Nullable String getDestinationEndpoint(TmfPcapProtocol protocol) {
        PcapProtocol p = ProtocolConversion.unwrap(protocol);
        Packet packet = fPacket.getPacket(p);
        if (packet == null) {
            return null;
        }
        return packet.getDestinationEndpoint().toString();
    }

    /**
     * Method that returns the most encapsulated protocol in this PcapEvent. If
     * it is an unknown protocol, it returns the last known protocol.
     *
     * @return The most encapsulated TmfProtocol.
     */
    public TmfPcapProtocol getMostEncapsulatedProtocol() {
        return ProtocolConversion.wrap(fPacket.getMostEcapsulatedPacket().getProtocol());
    }

    /**
     * Method that returns all the protocols in this PcapEvent.
     *
     * @return A list containing all the TmfProtocol.
     */
    public Collection<TmfPcapProtocol> getProtocols() {
        if (fProtocols != null) {
            return fProtocols;
        }
        ImmutableList.Builder<TmfPcapProtocol> builder = new ImmutableList.Builder<>();
        Packet packet = fPacket;

        // Go to start.
        while (packet != null && packet.getParentPacket() != null) {
            packet = packet.getParentPacket();
        }

        if (packet == null) {
            fProtocols = Collections.EMPTY_LIST;
            return fProtocols;
        }
        // Go through all the packets and add them to list.
        builder.add(ProtocolConversion.wrap(packet.getProtocol()));
        while (packet != null && packet.getChildPacket() != null) {
            packet = packet.getChildPacket();
            if (packet != null) {
                builder.add(ProtocolConversion.wrap(packet.getProtocol()));
            }
        }

        fProtocols = builder.build();
        return fProtocols;
    }

    /**
     * Getter method that returns the packet. This is default visible since it
     * is only used by tmf.pcap.core and thus should not be visible to other
     * packages
     *
     * @return The packet.
     */
    Packet getPacket() {
        return fPacket;
    }

    @Override
    public String toString() {
        return fPacket.getGlobalSummaryString();
    }

    /**
     * Return the signification of the PcapEvent at a specific protocol level.
     *
     * @param protocol
     *            The specified protocol.
     * @return The signification as a String.
     */
    public String toString(TmfPcapProtocol protocol) {
        PcapProtocol p = ProtocolConversion.unwrap(protocol);
        Packet packet = fPacket.getPacket(p);
        if (packet == null) {
            return EMPTY_STRING;
        }
        return packet.getLocalSummaryString();
    }
}
