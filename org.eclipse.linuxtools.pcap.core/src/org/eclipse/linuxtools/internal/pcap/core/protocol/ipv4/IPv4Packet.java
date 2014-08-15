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

package org.eclipse.linuxtools.internal.pcap.core.protocol.ipv4;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.internal.pcap.core.packet.BadPacketException;
import org.eclipse.linuxtools.internal.pcap.core.packet.Packet;
import org.eclipse.linuxtools.internal.pcap.core.protocol.Protocol;
import org.eclipse.linuxtools.internal.pcap.core.protocol.tcp.TCPPacket;
import org.eclipse.linuxtools.internal.pcap.core.protocol.udp.UDPPacket;
import org.eclipse.linuxtools.internal.pcap.core.protocol.unknown.UnknownPacket;
import org.eclipse.linuxtools.internal.pcap.core.trace.PcapFile;
import org.eclipse.linuxtools.internal.pcap.core.util.ConversionHelper;
import org.eclipse.linuxtools.internal.pcap.core.util.IPProtocolNumberHelper;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * Class that represents an Ethernet II packet.
 *
 * @author Vincent Perot
 */
public class IPv4Packet extends Packet {

    private final @Nullable Packet fChildPacket;
    private final @Nullable ByteBuffer fPayload;

    private final int fVersion;
    private final int fInternetHeaderLength; // in 4 bytes blocks
    private final int fDSCP;
    private final int fExplicitCongestionNotification;
    private final int fTotalLength; // in bytes
    private final int fIdentification;
    private final boolean fReservedFlag;
    private final boolean fDontFragmentFlag;
    private final boolean fMoreFragmentFlag;
    private final int fFragmentOffset;
    private final int fTimeToLive;
    private final int fIpDatagramProtocol;
    private final int fHeaderChecksum;
    private final Inet4Address fSourceIpAddress;
    private final Inet4Address fDestinationIpAddress;
    private final @Nullable byte[] fOptions;

    private @Nullable IPv4Endpoint fSourceEndpoint;
    private @Nullable IPv4Endpoint fDestinationEndpoint;

    private @Nullable ImmutableMap<String, String> fFields;

    // TODO Interpret options. See
    // http://www.iana.org/assignments/ip-parameters/ip-parameters.xhtml

    /**
     * Constructor of the IPv4 Packet class.
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
    public IPv4Packet(PcapFile file, @Nullable Packet parent, ByteBuffer packet) throws BadPacketException {
        super(file, parent, Protocol.IPV4);

        // The endpoints are lazy loaded. They are defined in the get*Endpoint()
        // methods.
        fSourceEndpoint = null;
        fDestinationEndpoint = null;

        fFields = null;

        packet.order(ByteOrder.BIG_ENDIAN);
        packet.position(0);

        byte storage = packet.get();
        fVersion = ((storage & 0xF0) >> 4) & 0x000000FF;
        fInternetHeaderLength = storage & 0x0F;

        storage = packet.get();
        fDSCP = ((storage & 0b11111100) >> 2) & 0x000000FF;
        fExplicitCongestionNotification = storage & 0b00000011;

        fTotalLength = ConversionHelper.unsignedShortToInt(packet.getShort());
        fIdentification = ConversionHelper.unsignedShortToInt(packet.getShort());

        storage = packet.get();
        fReservedFlag = isBitSet(storage, 7);
        fDontFragmentFlag = isBitSet(storage, 6);
        fMoreFragmentFlag = isBitSet(storage, 5);
        int msb = ((storage & 0b00011111) << 8);
        int lsb = ConversionHelper.unsignedByteToInt(packet.get());
        fFragmentOffset = msb + lsb;

        fTimeToLive = ConversionHelper.unsignedByteToInt(packet.get());
        fIpDatagramProtocol = ConversionHelper.unsignedByteToInt(packet.get());
        fHeaderChecksum = ConversionHelper.unsignedShortToInt(packet.getShort());

        byte[] source = new byte[IPv4Values.IP_ADDRESS_SIZE];
        byte[] destination = new byte[IPv4Values.IP_ADDRESS_SIZE];
        packet.get(source);
        packet.get(destination);

        try {
            @SuppressWarnings("null")
            @NonNull Inet4Address sourceIP = (Inet4Address) InetAddress.getByAddress(source);
            @SuppressWarnings("null")
            @NonNull Inet4Address destinationIP = (Inet4Address) InetAddress.getByAddress(destination);
            fSourceIpAddress = sourceIP;
            fDestinationIpAddress = destinationIP;
        } catch (UnknownHostException e) {
            throw new BadPacketException("The IP Address size is not valid!"); //$NON-NLS-1$
        }

        // Get options if there are any
        if (fInternetHeaderLength > IPv4Values.DEFAULT_HEADER_LENGTH) {
            fOptions = new byte[(fInternetHeaderLength - IPv4Values.DEFAULT_HEADER_LENGTH) * IPv4Values.BLOCK_SIZE];
            packet.get(fOptions);
        } else {
            fOptions = null;
        }

        // Get payload if any.
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
     * See http://en.wikipedia.org/wiki/List_of_IP_protocol_numbers
     */
    @Override
    protected @Nullable Packet findChildPacket() throws BadPacketException {
        // TODO Implement more protocols
        ByteBuffer payload = fPayload;
        if (payload == null) {
            return null;
        }

        switch (fIpDatagramProtocol) {
        case IPProtocolNumberHelper.PROTOCOL_NUMBER_TCP:
            return new TCPPacket(getPcapFile(), this, payload);
        case IPProtocolNumberHelper.PROTOCOL_NUMBER_UDP:
            return new UDPPacket(getPcapFile(), this, payload);
        default:
            return new UnknownPacket(getPcapFile(), this, payload);
        }

    }

    @Override
    public String toString() {
        // Generate flagString
        // This is very ugly.
        String flagString = null;

        if (fReservedFlag && fDontFragmentFlag && fMoreFragmentFlag) { // 111
            flagString = "Flags: 0x07 (Invalid)"; //$NON-NLS-1$
        } else if (fReservedFlag && fDontFragmentFlag && !fMoreFragmentFlag) { // 110
            flagString = "Flags: 0x06 (Invalid)"; //$NON-NLS-1$
        } else if (fReservedFlag && !fDontFragmentFlag && fMoreFragmentFlag) { // 101
            flagString = "Flags: 0x05 (Invalid)"; //$NON-NLS-1$
        } else if (fReservedFlag && !fDontFragmentFlag && !fMoreFragmentFlag) { // 100
            flagString = "Flags: 0x04 (Invalid)"; //$NON-NLS-1$
        } else if (!fReservedFlag && fDontFragmentFlag && fMoreFragmentFlag) { // 011
            flagString = "Flags: 0x03 (Invalid)"; //$NON-NLS-1$
        } else if (!fReservedFlag && fDontFragmentFlag && !fMoreFragmentFlag) { // 010
            flagString = "Flags: 0x02 (Don't fragment)"; //$NON-NLS-1$
        } else if (!fReservedFlag && !fDontFragmentFlag && fMoreFragmentFlag) { // 001
            flagString = "Flags: 0x01 (More fragments)"; //$NON-NLS-1$
        } else if (!fReservedFlag && !fDontFragmentFlag && !fMoreFragmentFlag) { // 000
            flagString = "Flags: 0x00 (Don't have more fragments)"; //$NON-NLS-1$
        }

        flagString += ", Fragment Offset: " + fFragmentOffset; //$NON-NLS-1$

        // Generate checksum string
        // TODO calculate the expected checksum from packet
        String checksumString = "Header Checksum: " + String.format("%s%04x", "0x", fHeaderChecksum); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        String string = getProtocol().getName() + ", Source: " + fSourceIpAddress.getHostAddress() + ", Destination: " + fDestinationIpAddress.getHostAddress() + //$NON-NLS-1$ //$NON-NLS-2$
                "\nVersion: " + fVersion + ", Identification: " + String.format("%s%04x", "0x", fIdentification) + ", Header Length: " + getHeaderLength() + " bytes, Total Length: " + getTotalLength() + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                " bytes\nDifferentiated Services Code Point: " + String.format("%s%02x", "0x", fDSCP) + "; Explicit Congestion Notification: " + String.format("%s%02x", "0x", fExplicitCongestionNotification) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                + "\n" + flagString + "\nTime to live: " + fTimeToLive + //$NON-NLS-1$ //$NON-NLS-2$
                "\nProtocol: " + fIpDatagramProtocol + "\n" //$NON-NLS-1$ //$NON-NLS-2$
                + checksumString + "\n"; //$NON-NLS-1$
        final Packet child = fChildPacket;
        if (child != null) {
            return string + child.toString();
        }
        return string;
    }

    /**
     * Getter method that returns the version of the IP protocol used. This
     * should always be set to 4 as IPv6 has its own class.
     *
     * @return The version of the IP used.
     */
    public int getVersion() {
        return fVersion;
    }

    /**
     * Getter method that returns the header length in bytes. In the IPv4
     * packet, this is specified in 4-bytes data block. By default, this method
     * returns 20 if there are no options present. Otherwise, it will return a
     * higher number.
     *
     * @return The header length in bytes.
     */
    public int getHeaderLength() {
        return fInternetHeaderLength * IPv4Values.BLOCK_SIZE;
    }

    /**
     * Getter method that returns the Differentiated Services Code Point (a.k.a.
     * the Type of Service). This is useful for some technologies that require
     * real-time data exchange.
     *
     * @return The DSCP
     */
    public int getDSCP() {
        return fDSCP;
    }

    /**
     * Getter method that returns the Explicit Congestion Notification (ECN).
     * This allows end-to-end communication without dropping packets.
     *
     * @return The ECN.
     */
    public int getExplicitCongestionNotification() {
        return fExplicitCongestionNotification;
    }

    /**
     * Getter method to retrieve the length of the entire packet, in bytes. This
     * number is according to the packet, and might not be true if the packet is
     * erroneous.
     *
     * @return The total length (packet and payload) in bytes.
     */
    public int getTotalLength() {
        return fTotalLength;
    }

    /**
     * Getter method to retrieve the Identification. This is a field that is
     * used to uniquely identify the packets, thus allowing the reconstruction
     * of fragmented IP packets.
     *
     * @return The packet identification.
     */
    public int getIdentification() {
        return fIdentification;
    }

    /**
     * Getter method that returns the state of the Reserved flag. This must
     * always be zero.
     *
     * @return The state of the Reserved flag.
     */
    public boolean getReservedFlag() {
        return fReservedFlag;
    }

    /**
     * Getter method that indicates if the packet can be fragmented or not.
     *
     * @return Whether the packet can be fragmented or not.
     */
    public boolean getDontFragmentFlag() {
        return fDontFragmentFlag;
    }

    /**
     * Getter method that indicates if the packet has more fragments or not.
     *
     * @return Whether the packet has more fragments or not.
     */
    public boolean getHasMoreFragment() {
        return fMoreFragmentFlag;
    }

    /**
     * Getter method that specify the offset of a particular fragment relative
     * to the original unfragmented packet, in 8-bytes blocks. *
     *
     * @return The fragment offset.
     */
    public int getFragmentOffset() {
        return fFragmentOffset;
    }

    /**
     * Getter method that returns the time to live in seconds. In practice, this
     * is a hop count. This is used to prevent packets from persisting.
     *
     * @return The time left to live for the packet.
     */
    public int getTimeToLive() {
        return fTimeToLive;
    }

    /**
     * Getter method that returns the encapsulated protocol.
     *
     * See http://en.wikipedia.org/wiki/List_of_IP_protocol_numbers
     *
     * @return The encapsulated protocol.
     */
    public int getIpDatagramProtocol() {
        return fIpDatagramProtocol;
    }

    /**
     * Getter method that returns the checksum, according to the packet. This
     * checksum might be wrong if the packet is erroneous.
     *
     * @return The header checksum.
     */
    public int getHeaderChecksum() {
        return fHeaderChecksum;
    }

    /**
     * Getter method that returns the source IP address.
     *
     * @return The source IP address, as a byte array in big-endian.
     */
    public Inet4Address getSourceIpAddress() {
        return fSourceIpAddress;
    }

    /**
     * Getter method that returns the destination IP address.
     *
     * @return The destination IP address, as a byte array in big-endian.
     */
    public Inet4Address getDestinationIpAddress() {
        return fDestinationIpAddress;
    }

    /**
     * Getter method that returns the options. This method returns null if no
     * options are present.
     *
     * @return The options of the packet.
     */
    public @Nullable byte[] getOptions() {
        final byte[] options = fOptions;
        if (options == null) {
            return null;
        }
        return Arrays.copyOf(options, options.length);
    }

    @Override
    public boolean validate() {
        // Not yet implemented. ATM, we consider that all packets are valid.
        // This is the case for all packets.
        // TODO Implement it.
        return true;
    }

    @Override
    public IPv4Endpoint getSourceEndpoint() {
        @Nullable
        IPv4Endpoint endpoint = fSourceEndpoint;
        if (endpoint == null) {
            endpoint = new IPv4Endpoint(this, true);
        }
        fSourceEndpoint = endpoint;
        return fSourceEndpoint;
    }

    @Override
    public IPv4Endpoint getDestinationEndpoint() {
        @Nullable
        IPv4Endpoint endpoint = fDestinationEndpoint;

        if (endpoint == null) {
            endpoint = new IPv4Endpoint(this, false);
        }
        fDestinationEndpoint = endpoint;
        return fDestinationEndpoint;
    }

    @Override
    public Map<String, String> getFields() {
        ImmutableMap<String, String> map = fFields;
        if (map == null) {
            Builder<String, String> builder = ImmutableMap.<String, String> builder()
                    .put("Version", String.valueOf(fVersion)) //$NON-NLS-1$
                    .put("Header Length", String.valueOf(getHeaderLength()) + " bytes") //$NON-NLS-1$ //$NON-NLS-2$
                    .put("Differentiated Services Field", String.format("%s%02x", "0x", fDSCP)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    .put("Explicit Congestion Notification", String.format("%s%02x", "0x", fExplicitCongestionNotification)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    .put("Total Length", String.valueOf(fTotalLength) + " bytes") //$NON-NLS-1$ //$NON-NLS-2$
                    .put("Identification", String.format("%s%04x", "0x", fIdentification)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    .put("Don't Fragment Flag", String.valueOf(fDontFragmentFlag)) //$NON-NLS-1$
                    .put("More Fragment Flag", String.valueOf(fMoreFragmentFlag)) //$NON-NLS-1$
                    .put("Fragment Offset", String.valueOf(fFragmentOffset)) //$NON-NLS-1$
                    .put("Time to live", String.valueOf(fTimeToLive)) //$NON-NLS-1$
                    .put("Protocol", IPProtocolNumberHelper.toString(fIpDatagramProtocol) + " (" + String.valueOf(fIpDatagramProtocol) + ")") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    .put("Checksum", String.format("%s%04x", "0x", fHeaderChecksum)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    .put("Source IP Address", fSourceIpAddress.getHostAddress()) //$NON-NLS-1$
                    .put("Destination IP Address", fDestinationIpAddress.getHostAddress()); //$NON-NLS-1$
            byte[] options = fOptions;
            if (options == null) {
                builder.put("Options", EMPTY_STRING); //$NON-NLS-1$
            } else {
                builder.put("Options", ConversionHelper.bytesToHex(options, true)); //$NON-NLS-1$

            }
            @SuppressWarnings("null")
            @NonNull
            ImmutableMap<String, String> newMap = builder.build();
            fFields = newMap;
            return newMap;
        }
        return map;
    }

    @Override
    public String getLocalSummaryString() {
        return "Src: " + fSourceIpAddress.getHostAddress() + " , Dst: " + fDestinationIpAddress.getHostAddress(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected String getSignificationString() {
        StringBuilder sb = new StringBuilder();
        sb.append(fSourceIpAddress.getHostAddress())
                .append(" > ") //$NON-NLS-1$
                .append(fDestinationIpAddress.getHostAddress());

        String flags = generateFlagString();
        if (!(flags.equals(""))) { //$NON-NLS-1$
            sb.append(' ')
                    .append('[')
                    .append(flags)
                    .append(']');
        }
        sb.append(" Id=") //$NON-NLS-1$
        .append(fIdentification);

        final ByteBuffer payload = fPayload;
        if (payload != null) {
            sb.append(" Len=") //$NON-NLS-1$
            .append(payload.array().length);
        } else {
            sb.append(" Len=0"); //$NON-NLS-1$
        }
        String string = sb.toString();
        if (string == null) {
            return EMPTY_STRING;
        }
        return string;
    }

    private String generateFlagString() {
        StringBuilder sb = new StringBuilder();
        boolean start = true;

        if (fDontFragmentFlag) {
            if (!start) {
                sb.append(", "); //$NON-NLS-1$
            }
            sb.append("DF"); //$NON-NLS-1$
            start = false;
        }
        if (fMoreFragmentFlag) {
            if (!start) {
                sb.append(", "); //$NON-NLS-1$
            }
            sb.append("MF"); //$NON-NLS-1$
            start = false;
        }
        String string = sb.toString();
        if (string == null) {
            return EMPTY_STRING;
        }
        return string;
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
        result = prime * result + fDSCP;
        result = prime * result + fDestinationIpAddress.hashCode();
        result = prime * result + (fDontFragmentFlag ? 1231 : 1237);
        result = prime * result + fExplicitCongestionNotification;
        result = prime * result + fFragmentOffset;
        result = prime * result + fHeaderChecksum;
        result = prime * result + fIdentification;
        result = prime * result + fInternetHeaderLength;
        result = prime * result + fIpDatagramProtocol;
        result = prime * result + (fMoreFragmentFlag ? 1231 : 1237);
        result = prime * result + Arrays.hashCode(fOptions);
        final ByteBuffer payload = fPayload;
        if (payload != null) {
            result = prime * result + payload.hashCode();
        } else {
            result = prime * result;
        }
        result = prime * result + (fReservedFlag ? 1231 : 1237);
        result = prime * result + fSourceIpAddress.hashCode();
        result = prime * result + fTimeToLive;
        result = prime * result + fTotalLength;
        result = prime * result + fVersion;
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
        IPv4Packet other = (IPv4Packet) obj;
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

        if (fDSCP != other.fDSCP) {
            return false;
        }
        if (!(fDestinationIpAddress.equals(other.fDestinationIpAddress))) {
            return false;
        }
        if (fDontFragmentFlag != other.fDontFragmentFlag) {
            return false;
        }
        if (fExplicitCongestionNotification != other.fExplicitCongestionNotification) {
            return false;
        }
        if (fFragmentOffset != other.fFragmentOffset) {
            return false;
        }
        if (fHeaderChecksum != other.fHeaderChecksum) {
            return false;
        }
        if (fIdentification != other.fIdentification) {
            return false;
        }
        if (fInternetHeaderLength != other.fInternetHeaderLength) {
            return false;
        }
        if (fIpDatagramProtocol != other.fIpDatagramProtocol) {
            return false;
        }
        if (fMoreFragmentFlag != other.fMoreFragmentFlag) {
            return false;
        }
        if (!Arrays.equals(fOptions, other.fOptions)) {
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
        if (fReservedFlag != other.fReservedFlag) {
            return false;
        }
        if (!(fSourceIpAddress.equals(other.fSourceIpAddress))) {
            return false;
        }
        if (fTimeToLive != other.fTimeToLive) {
            return false;
        }
        if (fTotalLength != other.fTotalLength) {
            return false;
        }
        if (fVersion != other.fVersion) {
            return false;
        }
        return true;
    }

}
