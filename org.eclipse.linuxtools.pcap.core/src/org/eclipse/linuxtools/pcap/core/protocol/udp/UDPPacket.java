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

package org.eclipse.linuxtools.pcap.core.protocol.udp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.pcap.core.packet.BadPacketException;
import org.eclipse.linuxtools.pcap.core.packet.Packet;
import org.eclipse.linuxtools.pcap.core.protocol.Protocol;
import org.eclipse.linuxtools.pcap.core.protocol.unknown.UnknownPacket;
import org.eclipse.linuxtools.pcap.core.trace.PcapFile;
import org.eclipse.linuxtools.pcap.core.util.ConversionHelper;

import com.google.common.collect.ImmutableMap;

/**
 * Class that represents a UDP packet.
 *
 * @author Vincent Perot
 */
public class UDPPacket extends Packet {

    private final @Nullable Packet fChildPacket;
    private final @Nullable ByteBuffer fPayload;

    private final int fSourcePort;
    private final int fDestinationPort;
    private final int fTotalLength;
    private final int fChecksum;

    private @Nullable UDPEndpoint fSourceEndpoint;
    private @Nullable UDPEndpoint fDestinationEndpoint;

    private @Nullable ImmutableMap<String, String> fFields;

    /**
     * Constructor of the UDP Packet class.
     *
     * @param file
     *            The file that contains this packet.
     * @param parent
     *            The parent packet of this packet (the encapsulating packet).
     * @param packet
     *            The entire packet (header and payload).
     * @throws BadPacketException
     *             Thrown when the packet is erroneous.
     */
    public UDPPacket(PcapFile file, @Nullable Packet parent, ByteBuffer packet) throws BadPacketException {
        super(file, parent, Protocol.UDP);

        // The endpoints are lazy loaded. They are defined in the get*Endpoint()
        // methods.
        fSourceEndpoint = null;
        fDestinationEndpoint = null;

        fFields = null;

        packet.order(ByteOrder.BIG_ENDIAN);
        packet.position(0);

        fSourcePort = ConversionHelper.unsignedShortToInt(packet.getShort());
        fDestinationPort = ConversionHelper.unsignedShortToInt(packet.getShort());
        fTotalLength = ConversionHelper.unsignedShortToInt(packet.getShort());
        fChecksum = ConversionHelper.unsignedShortToInt(packet.getShort());

        if (packet.array().length - packet.position() > 0) {
            byte[] array = new byte[packet.array().length - packet.position()];
            packet.get(array);

            ByteBuffer payload = ByteBuffer.wrap(array);
            payload.order(ByteOrder.BIG_ENDIAN);
            payload.position(0);
            fPayload = payload;
        } else {
            fPayload = null;
        }

        // Find child
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
     * {@inheritDoc}
     *
     * See http://www.iana.org/assignments/service-names-port-numbers/service-
     * names-port-numbers.xhtml or
     * http://en.wikipedia.org/wiki/List_of_TCP_and_UDP_port_numbers
     */
    @Override
    protected @Nullable Packet findChildPacket() throws BadPacketException {
        // TODO implement further protocols and update this
        ByteBuffer payload = fPayload;
        if (payload == null) {
            return null;
        }

        return new UnknownPacket(getPcapFile(), this, payload);
    }

    @Override
    public String toString() {
        String string = getProtocol().getName() + ", Source Port: " + fSourcePort + ", Destination Port: " + fDestinationPort + //$NON-NLS-1$ //$NON-NLS-2$
                ", Length: " + fTotalLength + ", Checksum: " + fChecksum + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        final Packet child = fChildPacket;
        if (child != null) {
            return string + child.toString();
        }
        return string;
    }

    /**
     * Getter method that returns the UDP Source Port.
     *
     * @return The source Port.
     */
    public int getSourcePort() {
        return fSourcePort;
    }

    /**
     * Getter method that returns the UDP Destination Port.
     *
     * @return The destination Port.
     */
    public int getDestinationPort() {
        return fDestinationPort;
    }

    /**
     * Getter method that returns the total length of the packet in bytes. The
     * values it can take go from 8 to 65,515.
     *
     * @return The total length of the packet in bytes.
     */
    public int getTotalLength() {
        return fTotalLength;
    }

    /**
     * Getter method that returns the checksum (on header and payload). If the
     * transmitter does not use this field, it is set to zero. This checksum
     * might be wrong if the packet is erroneous.
     *
     * @return The checksum received from the packet.
     */
    public int getChecksum() {
        return fChecksum;
    }

    @Override
    public boolean validate() {
        // Not yet implemented. ATM, we consider that all packets are valid.
        // This is the case for all packets.
        // TODO Implement it.
        return true;
    }

    @Override
    public UDPEndpoint getSourceEndpoint() {
        @Nullable
        UDPEndpoint endpoint = fSourceEndpoint;
        if (endpoint == null) {
            endpoint = new UDPEndpoint(this, true);
        }
        fSourceEndpoint = endpoint;
        return fSourceEndpoint;
    }

    @Override
    public UDPEndpoint getDestinationEndpoint() {
        @Nullable UDPEndpoint endpoint = fDestinationEndpoint;
        if (endpoint == null) {
            endpoint = new UDPEndpoint(this, false);
        }
        fDestinationEndpoint = endpoint;
        return fDestinationEndpoint;
    }

    @Override
    public Map<String, String> getFields() {
        ImmutableMap<String, String> map = fFields;
        if (map == null) {
            @SuppressWarnings("null")
            @NonNull ImmutableMap<String, String> newMap = ImmutableMap.<String, String> builder()
                    .put("Source Port", String.valueOf(fSourcePort)) //$NON-NLS-1$
                    .put("Destination Port", String.valueOf(fDestinationPort)) //$NON-NLS-1$
                    .put("Length", String.valueOf(fTotalLength) + " bytes") //$NON-NLS-1$ //$NON-NLS-2$
                    .put("Checksum", String.format("%s%04x", "0x", fChecksum)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    .build();
            fFields = newMap;
            return newMap;
        }
        return map;
    }

    @Override
    public String getLocalSummaryString() {
        return "Src Port: " + fSourcePort + ", Dst Port: " + fDestinationPort; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected String getSignificationString() {
        return "Source Port: " + fSourcePort + ", Destination Port: " + fDestinationPort; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fChecksum;
        final Packet child = fChildPacket;
        if (child != null) {
            result = prime * result + child.hashCode();
        } else {
            result = prime * result;
        }
        result = prime * result + fDestinationPort;
        final ByteBuffer payload = fPayload;
        if (payload != null) {
            result = prime * result + payload.hashCode();
        } else {
            result = prime * result;
        }
        result = prime * result + fSourcePort;
        result = prime * result + fTotalLength;
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
        UDPPacket other = (UDPPacket) obj;
        if (fChecksum != other.fChecksum) {
            return false;
        }
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
        if (fDestinationPort != other.fDestinationPort) {
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
        if (fSourcePort != other.fSourcePort) {
            return false;
        }
        if (fTotalLength != other.fTotalLength) {
            return false;
        }
        return true;
    }

}
