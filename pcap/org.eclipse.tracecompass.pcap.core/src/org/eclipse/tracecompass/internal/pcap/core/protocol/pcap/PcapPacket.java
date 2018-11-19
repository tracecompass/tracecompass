/*******************************************************************************
 * Copyright (c) 2014, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *   Viet-Hung Phan - Support pcapNg
 *******************************************************************************/

package org.eclipse.tracecompass.internal.pcap.core.protocol.pcap;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.pcap.core.packet.BadPacketException;
import org.eclipse.tracecompass.internal.pcap.core.packet.Packet;
import org.eclipse.tracecompass.internal.pcap.core.protocol.PcapProtocol;
import org.eclipse.tracecompass.internal.pcap.core.protocol.ethernet2.EthernetIIPacket;
import org.eclipse.tracecompass.internal.pcap.core.protocol.unknown.UnknownPacket;
import org.eclipse.tracecompass.internal.pcap.core.trace.PcapFile;
import org.eclipse.tracecompass.internal.pcap.core.util.ConversionHelper;
import org.eclipse.tracecompass.internal.pcap.core.util.LinkTypeHelper;
import org.eclipse.tracecompass.internal.pcap.core.util.PcapTimestampScale;

import com.google.common.collect.ImmutableMap;

/**
 * Class that represents a Pcap/PcapNg packet. This is the highest level of
 * encapsulation. This class will serves both pcap and pcapNg packet
 *
 * @author Vincent Perot
 */

public abstract class PcapPacket extends Packet {

    private @Nullable Packet fChildPacket;
    private @Nullable ByteBuffer fPayload;

    private long fTimestamp;
    private long fIncludedLength;
    private long fOriginalLength;
    private long fPacketIndex;

    private @Nullable PcapEndpoint fSourceEndpoint;
    private @Nullable PcapEndpoint fDestinationEndpoint;

    private @Nullable Map<String, String> fFields;

    /**
     * Constructor of the Pcap and PcapNg Packet class.
     *
     * @param file
     *            The file that contains this packet.
     */
    public PcapPacket(PcapFile file) {
        super(file, null, PcapProtocol.PCAP);
    }

    @Override
    public @Nullable Packet getChildPacket() {
        return fChildPacket;
    }

    /**
     * Setter method that stores the child packet
     *
     * @param childPacket
     *            The child of packet
     */
    public void setChildPacket(@Nullable Packet childPacket) {
        fChildPacket = childPacket;
    }

    @Override
    public @Nullable ByteBuffer getPayload() {
        if (fPayload != null) {
            fPayload.position(0);
        }
        return fPayload;
    }

    /**
     * Setter method that stores the packet payload data
     *
     * @param payLoad
     *            The packet payload data
     */
    public void setPayload(ByteBuffer payLoad) {
        fPayload = payLoad;
    }

    /**
     * Getter method that returns the timestamp of this packet, in
     * microseconds/nanoseconds relative to epoch.
     *
     * @return The timestamp of the packet.
     */
    public long getTimestamp() {
        return fTimestamp;
    }

    /**
     * Setter method that stores the packet timestamp
     *
     * @param timeStamp
     *            The packet timestamp
     */
    public void setTimeStamp(long timeStamp) {
        fTimestamp = timeStamp;
    }

    /**
     * Getter method that returns the length in bytes of the packet that was
     * included in the {@link PcapFile}.
     *
     * @return The included length of the packet.
     */
    public long getIncludedLength() {
        return fIncludedLength;
    }

    /**
     * Setter method that stores the packet included length
     *
     * @param includedLength
     *            The packet included length
     */
    public void setIncludedLength(long includedLength) {
        fIncludedLength = includedLength;
    }

    /**
     * Getter method that returns the original length in bytes of the packet.
     *
     * @return The included length of the packet.
     */
    public long getOriginalLength() {
        return fOriginalLength;
    }

    /**
     * Setter method that stores the packet original length
     *
     * @param originalLength
     *            The packet original length
     */
    public void setOriginalLength(long originalLength) {
        fOriginalLength = originalLength;
    }

    /**
     * Method that indicates if this packet was truncated at capture time.
     *
     * @return Whether the packet is truncated or not.
     */
    public boolean isTruncated() {
        return fIncludedLength != fOriginalLength;
    }

    /**
     * Getter method that returns the index of the packet.
     *
     * @return The index of the packet.
     */
    public long getIndex() {
        return fPacketIndex;
    }

    /**
     * Setter method that stores the packet index
     *
     * @param packetIndex
     *            The packet index
     */
    public void setIndex(long packetIndex) {
        fPacketIndex = packetIndex;
    }

    @Override
    public String toString() {
        // TODO Decide if first capture is 0 or 1. Right now, it is 0.
        String string = getProtocol().getName() + " " + fPacketIndex + //$NON-NLS-1$
                ": " + fOriginalLength + " bytes on wire, " + //$NON-NLS-1$ //$NON-NLS-2$
                fIncludedLength + " bytes captured.\nArrival time: " + //$NON-NLS-1$
                ConversionHelper.toGMTTime(fTimestamp, getTimestampScale()) + "\n"; //$NON-NLS-1$

        final Packet child = fChildPacket;
        if (child != null) {
            return string + child.toString();
        }
        return string;
    }

    /**
     * {@inheritDoc}
     *
     * See http://www.tcpdump.org/linktypes.html
     */
    @Override
    protected @Nullable Packet findChildPacket() throws BadPacketException {
        @Nullable ByteBuffer payload = fPayload;
        if (payload == null) {
            return null;
        }

        // The link type
        switch ((int) getDataLinkType()) {
        case LinkTypeHelper.LINKTYPE_ETHERNET:
            return new EthernetIIPacket(getPcapFile(), this, payload);
        default:
            return new UnknownPacket(getPcapFile(), this, payload);
        }
    }

    @Override
    public boolean validate() {
        // Not yet implemented. ATM, we consider that all packets are valid.
        // This is the case for all packets.
        // TODO Implement it.
        return true;
    }

    @Override
    public PcapEndpoint getSourceEndpoint() {
        PcapEndpoint endpoint = fSourceEndpoint;
        if (endpoint == null) {
            endpoint = new PcapEndpoint(this, true);
        }
        fSourceEndpoint = endpoint;
        return fSourceEndpoint;
    }

    @Override
    public PcapEndpoint getDestinationEndpoint() {
        PcapEndpoint endpoint = fDestinationEndpoint;

        if (endpoint == null) {
            endpoint = new PcapEndpoint(this, false);
        }
        fDestinationEndpoint = endpoint;
        return fDestinationEndpoint;
    }

    // TODO handle plural form correctly
    // TODO microsec
    @Override
    public Map<String, String> getFields() {
        Map<String, String> map = fFields;
        if (map == null) {
            ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
            builder.put("Frame", String.valueOf(fPacketIndex)); //$NON-NLS-1$
            builder.put("Frame Length", String.valueOf(fOriginalLength) + " bytes"); //$NON-NLS-1$ //$NON-NLS-2$
            builder.put("Capture Length", String.valueOf(fIncludedLength) + " bytes"); //$NON-NLS-1$ //$NON-NLS-2$
            builder.put("Capture Time", ConversionHelper.toGMTTime(fTimestamp, getTimestampScale())); //$NON-NLS-1$

            fFields = builder.build();
            return fFields;
        }
        return map;
    }

    @Override
    public String getLocalSummaryString() {
        return "Frame " + fPacketIndex + ": " + fOriginalLength + " bytes on wire, " + fIncludedLength + " bytes captured"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    @Override
    protected String getSignificationString() {
        return "New Frame: " + fOriginalLength + " bytes on wire"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        Packet child = fChildPacket;
        if (child == null) {
            result = prime * result;
        } else {
            result = prime * result + child.hashCode();
        }

        result = prime * result + (int) (fIncludedLength ^ (fIncludedLength >>> 32));
        result = prime * result + (int) (fOriginalLength ^ (fOriginalLength >>> 32));
        result = prime * result + (int) (fPacketIndex ^ (fPacketIndex >>> 32));

        if (child == null) {
            result = prime * result + payloadHashCode(fPayload);
        }

        result = prime * result + (int) (fTimestamp ^ (fTimestamp >>> 32));
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PcapPacket other = (PcapPacket) obj;
        if (!Objects.equals(fChildPacket, other.fChildPacket)) {
            return false;
        }
        if (fIncludedLength != other.fIncludedLength) {
            return false;
        }
        if (fOriginalLength != other.fOriginalLength) {
            return false;
        }
        if (fPacketIndex != other.fPacketIndex) {
            return false;
        }
        if (fChildPacket == null && !payloadEquals(fPayload, other.fPayload)) {
            return false;
        }
        return (fTimestamp == other.fTimestamp);
    }

    /**
     * Getter method that returns the Timestamp precision of the packet.
     *
     * @return the Timestamp precision.
     */
    public PcapTimestampScale getTimestampScale() {
        return getPcapFile().getTimestampPrecision();
    }

    /**
     * Getter method for the data link type of the packet. This parameter is
     * used to determine higher-level protocols (Ethernet, WLAN, SLL).
     *
     * @return The data link type of the packet.
     */
    public abstract long getDataLinkType();
}
