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

package org.eclipse.tracecompass.internal.pcap.core.protocol.ethernet2;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.pcap.core.packet.BadPacketException;
import org.eclipse.tracecompass.internal.pcap.core.packet.Packet;
import org.eclipse.tracecompass.internal.pcap.core.protocol.PcapProtocol;
import org.eclipse.tracecompass.internal.pcap.core.protocol.ipv4.IPv4Packet;
import org.eclipse.tracecompass.internal.pcap.core.protocol.unknown.UnknownPacket;
import org.eclipse.tracecompass.internal.pcap.core.trace.PcapFile;
import org.eclipse.tracecompass.internal.pcap.core.util.ConversionHelper;
import org.eclipse.tracecompass.internal.pcap.core.util.EthertypeHelper;

import com.google.common.collect.ImmutableMap;

/**
 * Class that represents an Ethernet II packet. This should be called an
 * Ethernet frame, but in order to keep the nomenclature consistent, this is
 * called a packet.
 *
 * @author Vincent Perot
 */
public class EthernetIIPacket extends Packet {

    private final @Nullable Packet fChildPacket;
    private final @Nullable ByteBuffer fPayload;

    /* We store MAC addresses as byte arrays since
     * there is no standard java class to store them. */
    private final byte[] fSourceMacAddress;
    private final byte[] fDestinationMacAddress;

    private final int fType;

    private @Nullable EthernetIIEndpoint fSourceEndpoint;
    private @Nullable EthernetIIEndpoint fDestinationEndpoint;

    private @Nullable Map<String, String> fFields;

    /**
     * Constructor of the Ethernet Packet class.
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
    public EthernetIIPacket(PcapFile file, @Nullable Packet parent, ByteBuffer packet) throws BadPacketException {
        super(file, parent, PcapProtocol.ETHERNET_II);

        if (packet.array().length <= EthernetIIValues.ETHERNET_II_MIN_SIZE) {
            throw new BadPacketException("An Ethernet II packet can't be smaller than 14 bytes."); //$NON-NLS-1$
        }

        // The endpoints are lazy loaded. They are defined in the get*Endpoint()
        // methods.
        fSourceEndpoint = null;
        fDestinationEndpoint = null;

        fFields = null;

        fDestinationMacAddress = new byte[EthernetIIValues.MAC_ADDRESS_SIZE];
        fSourceMacAddress = new byte[EthernetIIValues.MAC_ADDRESS_SIZE];
        packet.order(ByteOrder.BIG_ENDIAN);
        packet.position(0);
        packet.get(fDestinationMacAddress);
        packet.get(fSourceMacAddress);
        fType = ConversionHelper.unsignedShortToInt(packet.getShort());

        // Get payload if it exists.
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
     * Getter method for the source MAC Address.
     *
     * @return The source MAC address.
     */
    public byte[] getSourceMacAddress() {
        return checkNotNull(Arrays.copyOf(fSourceMacAddress, fSourceMacAddress.length));
    }

    /**
     * Getter method for the destination MAC Address.
     *
     * @return The destination MAC address.
     */
    public byte[] getDestinationMacAddress() {
        return checkNotNull(Arrays.copyOf(fDestinationMacAddress, fDestinationMacAddress.length));
    }

    /**
     * Getter method for Ethertype. See
     * http://standards.ieee.org/develop/regauth/ethertype/eth.txt
     *
     * @return The Ethertype. This is used to determine the child packet..
     */
    public int getEthertype() {
        return fType;
    }

    @Override
    protected @Nullable Packet findChildPacket() throws BadPacketException {
        // TODO Add more protocols.
        ByteBuffer payload = fPayload;
        if (payload == null) {
            return null;
        }
        switch (fType) {
        case EthertypeHelper.ETHERTYPE_IPV4:
            return new IPv4Packet(getPcapFile(), this, payload);
        default:
            return new UnknownPacket(getPcapFile(), this, payload);
        }
    }

    @Override
    public String toString() {
        String string = getProtocol().getName() + ", Source: " + ConversionHelper.toMacAddress(fSourceMacAddress) + //$NON-NLS-1$
                ", Destination: " + ConversionHelper.toMacAddress(fDestinationMacAddress) + ", Type: " + //$NON-NLS-1$ //$NON-NLS-2$
                EthertypeHelper.toEtherType(fType) + "\n"; //$NON-NLS-1$
        final Packet child = fChildPacket;
        if (child != null) {
            return string + child.toString();
        }
        return string;
    }

    @Override
    public boolean validate() {
        // Not yet implemented. ATM, we consider that all packets are valid.
        // This is the case for all packets.
        // TODO Implement it.
        return true;
    }

    @Override
    public EthernetIIEndpoint getSourceEndpoint() {
        @Nullable EthernetIIEndpoint endpoint = fSourceEndpoint;
        if (endpoint == null) {
            endpoint = new EthernetIIEndpoint(this, true);
        }
        fSourceEndpoint = endpoint;
        return fSourceEndpoint;
    }

    @Override
    public EthernetIIEndpoint getDestinationEndpoint() {
        @Nullable EthernetIIEndpoint endpoint = fDestinationEndpoint;

        if (endpoint == null) {
            endpoint = new EthernetIIEndpoint(this, false);
        }
        fDestinationEndpoint = endpoint;
        return fDestinationEndpoint;
    }

    @Override
    public Map<String, String> getFields() {
        Map<String, String> map = fFields;
        if (map == null) {
            ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
            builder.put("Source MAC Address", ConversionHelper.toMacAddress(fSourceMacAddress)); //$NON-NLS-1$
            builder.put("Destination MAC Address", ConversionHelper.toMacAddress(fDestinationMacAddress)); //$NON-NLS-1$
            builder.put("Ethertype", String.valueOf(EthertypeHelper.toEtherType(fType))); //$NON-NLS-1$

            fFields = builder.build();
            return fFields;
        }
        return map;
    }

    @Override
    public String getLocalSummaryString() {
        return "Src: " + ConversionHelper.toMacAddress(fSourceMacAddress) + " , Dst: " + ConversionHelper.toMacAddress(fDestinationMacAddress); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected String getSignificationString() {
        return "Source MAC: " + ConversionHelper.toMacAddress(fSourceMacAddress) + " , Destination MAC: " + ConversionHelper.toMacAddress(fDestinationMacAddress); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        final Packet child = fChildPacket;
        if (child != null) {
            result = prime * result + child.hashCode();
        } else {
            result = prime * result;
        }
        result = prime * result + Arrays.hashCode(fDestinationMacAddress);
        final ByteBuffer payload = fPayload;
        if (payload != null) {
            result = prime * result + payload.hashCode();
        } else {
            result = prime * result;
        }
        result = prime * result + Arrays.hashCode(fSourceMacAddress);
        result = prime * result + fType;
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
        EthernetIIPacket other = (EthernetIIPacket) obj;
        if(!NonNullUtils.equalsNullable(fChildPacket, other.fChildPacket)) {
            return false;
        }
        if (!Arrays.equals(fDestinationMacAddress, other.fDestinationMacAddress)) {
            return false;
        }
        if(!NonNullUtils.equalsNullable(fPayload, other.fPayload)) {
            return false;
        }

        if (!Arrays.equals(fSourceMacAddress, other.fSourceMacAddress)) {
            return false;
        }
        if (fType != other.fType) {
            return false;
        }
        return true;
    }

}
