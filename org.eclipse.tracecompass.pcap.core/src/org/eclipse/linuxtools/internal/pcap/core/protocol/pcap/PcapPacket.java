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

package org.eclipse.linuxtools.internal.pcap.core.protocol.pcap;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.internal.pcap.core.packet.BadPacketException;
import org.eclipse.linuxtools.internal.pcap.core.packet.Packet;
import org.eclipse.linuxtools.internal.pcap.core.protocol.PcapProtocol;
import org.eclipse.linuxtools.internal.pcap.core.protocol.ethernet2.EthernetIIPacket;
import org.eclipse.linuxtools.internal.pcap.core.protocol.unknown.UnknownPacket;
import org.eclipse.linuxtools.internal.pcap.core.trace.PcapFile;
import org.eclipse.linuxtools.internal.pcap.core.trace.PcapFileValues;
import org.eclipse.linuxtools.internal.pcap.core.util.ConversionHelper;
import org.eclipse.linuxtools.internal.pcap.core.util.LinkTypeHelper;
import org.eclipse.linuxtools.internal.pcap.core.util.PcapTimestampScale;

import com.google.common.collect.ImmutableMap;

/**
 * Class that represents a Pcap packet. This is the highest level of
 * encapsulation.
 *
 * @author Vincent Perot
 */
public class PcapPacket extends Packet {

    private static final int TIMESTAMP_MICROSECOND_MAX = 1000000;
    private static final int TIMESTAMP_NANOSECOND_MAX = 1000000000;

    private final @Nullable Packet fChildPacket;
    private final @Nullable ByteBuffer fPayload;

    private final long fTimestamp; // In microseconds
    private final long fIncludedLength;
    private final long fOriginalLength;
    private final long fPacketIndex;

    private @Nullable PcapEndpoint fSourceEndpoint;
    private @Nullable PcapEndpoint fDestinationEndpoint;

    private @Nullable ImmutableMap<String, String> fFields;

    /**
     * Constructor of the Pcap Packet class.
     *
     * @param file
     *            The file that contains this packet.
     * @param parent
     *            The parent packet of this packet (the encapsulating packet).
     * @param header
     *            The header of the packet.
     * @param payload
     *            The payload of this packet.
     * @param index
     *            The index of the packet in the file.
     * @throws BadPacketException
     *             Thrown when the Packet is erroneous.
     */
    public PcapPacket(PcapFile file, @Nullable Packet parent, ByteBuffer header, @Nullable ByteBuffer payload, long index) throws BadPacketException {
        super(file, parent, PcapProtocol.PCAP);

        if (header.array().length < PcapFileValues.PACKET_HEADER_SIZE) {
            fChildPacket = null;
            throw new BadPacketException("The Pcap packet header is too small."); //$NON-NLS-1$
        }

        // The endpoints are lazy loaded. They are defined in the get*Endpoint()
        // methods.
        fSourceEndpoint = null;
        fDestinationEndpoint = null;

        fFields = null;

        fPacketIndex = index;

        // PcapPacket header in File endian
        header.order(getPcapFile().getByteOrder());
        header.position(0);
        long timestampMostSignificant = ConversionHelper.unsignedIntToLong(header.getInt());
        long timestampLeastSignificant = ConversionHelper.unsignedIntToLong(header.getInt());

        switch (getTimestampScale()) {
        case MICROSECOND:
            if (timestampLeastSignificant > TIMESTAMP_MICROSECOND_MAX) {
                fChildPacket = null;
                throw new BadPacketException("The timestamp is erroneous."); //$NON-NLS-1$
            }
            fTimestamp = TIMESTAMP_MICROSECOND_MAX * timestampMostSignificant + timestampLeastSignificant;
            break;
        case NANOSECOND:
            if (timestampLeastSignificant > TIMESTAMP_NANOSECOND_MAX) {
                fChildPacket = null;
                throw new BadPacketException("The timestamp is erroneous."); //$NON-NLS-1$
            }
            fTimestamp = TIMESTAMP_NANOSECOND_MAX * timestampMostSignificant + timestampLeastSignificant;
            break;
        default:
            throw new IllegalArgumentException("The timestamp precision is not valid!"); //$NON-NLS-1$
        }

        fIncludedLength = ConversionHelper.unsignedIntToLong(header.getInt());
        fOriginalLength = ConversionHelper.unsignedIntToLong(header.getInt());

        // Set up payload
        final ByteBuffer pcapPacket = payload;
        if (pcapPacket == null) {
            fChildPacket = null;
            fPayload = null;
            return;
        }

        pcapPacket.order(ByteOrder.BIG_ENDIAN);
        pcapPacket.position(0);
        fPayload = pcapPacket;

        // Find Child Packet
        fChildPacket = findChildPacket();

    }

    @Override
    public @Nullable Packet getChildPacket() {
        return fChildPacket;
    }

    @Override
    public @Nullable ByteBuffer getPayload() {
        return fPayload;
    }

    /**
     * Getter method that returns the timestamp of this packet, in microseconds/nanoseconds
     * relative to epoch.
     *
     * @return The timestamp of the packet.
     */
    public long getTimestamp() {
        return fTimestamp;
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
     * Getter method that returns the original length in bytes of the packet.
     *
     * @return The included length of the packet.
     */
    public long getOriginalLength() {
        return fOriginalLength;
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

    @Override
    public String toString() {
        // TODO Decide if first capture is 0 or 1. Right now, it is 0.
        String string = getProtocol().getName() + " " + fPacketIndex +  //$NON-NLS-1$
                ": " + fOriginalLength + " bytes on wire, " + //$NON-NLS-1$ //$NON-NLS-2$
                fIncludedLength + " bytes captured.\nArrival time: " +  //$NON-NLS-1$
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
        @Nullable
        ByteBuffer payload = fPayload;
        if (payload == null) {
            return null;
        }

        switch ((int) getPcapFile().getDataLinkType()) {
        case LinkTypeHelper.LINKTYPE_ETHERNET:
            return new EthernetIIPacket(getPcapFile(), this, payload);
        default: // TODO add more protocols
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
        @Nullable PcapEndpoint endpoint = fSourceEndpoint;
        if (endpoint == null) {
            endpoint = new PcapEndpoint(this, true);
        }
        fSourceEndpoint = endpoint;
        return fSourceEndpoint;
    }

    @Override
    public PcapEndpoint getDestinationEndpoint() {
        @Nullable
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
        ImmutableMap<String, String> map = fFields;
        if (map == null) {
            @SuppressWarnings("null")
            @NonNull ImmutableMap<String, String> newMap = ImmutableMap.<String, String> builder()
                    .put("Frame", String.valueOf(fPacketIndex)) //$NON-NLS-1$
                    .put("Frame Length", String.valueOf(fOriginalLength) + " bytes") //$NON-NLS-1$ //$NON-NLS-2$
                    .put("Capture Length", String.valueOf(fIncludedLength) + " bytes") //$NON-NLS-1$ //$NON-NLS-2$
                    .put("Capture Time", ConversionHelper.toGMTTime(fTimestamp, getTimestampScale())) //$NON-NLS-1$
                    .build();
            fFields = newMap;
            return newMap;
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

        ByteBuffer payload = fPayload;
        if (payload == null) {
            result = prime * result;
        } else {
            result = prime * result + payload.hashCode();
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
        final Packet child = fChildPacket;
        if (child != null) {
            if (!child.equals(other.fChildPacket)) {
                return false;
            }
        } else {
            if (other.fChildPacket != null) {
                return false;
            }
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
        final ByteBuffer payload = fPayload;
        if (payload != null) {
            if (!payload.equals(other.fPayload)) {
                return false;
            }
        } else {
            if (other.fPayload != null) {
                return false;
            }
        }

        if (fTimestamp != other.fTimestamp) {
            return false;
        }
        return true;
    }

    /**
     * Getter method that returns the Timestamp precision of the packet.
     *
     * @return the Timestamp precision.
     */
    public PcapTimestampScale getTimestampScale() {
        return getPcapFile().getTimestampPrecision();
    }
}