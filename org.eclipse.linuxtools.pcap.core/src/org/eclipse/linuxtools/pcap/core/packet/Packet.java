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

package org.eclipse.linuxtools.pcap.core.packet;

import java.nio.ByteBuffer;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.pcap.core.endpoint.ProtocolEndpoint;
import org.eclipse.linuxtools.pcap.core.protocol.Protocol;
import org.eclipse.linuxtools.pcap.core.protocol.ipv4.IPv4Packet;
import org.eclipse.linuxtools.pcap.core.protocol.unknown.UnknownPacket;
import org.eclipse.linuxtools.pcap.core.trace.PcapFile;

// TODO For all packets, make checks on dimension.
// TODO maybe add a invalid packet type?

/**
 * Abstract class that implements the methods that are common to every packets.
 *
 * @author Vincent Perot
 */
public abstract class Packet {

    /** Empty string */
    protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

    /** The Pcap File to which this packet belong */
    private final PcapFile fPcapFile;

    /** The parent packet of this packet */
    private final @Nullable Packet fParentPacket;

    /** The protocol that this packet uses */
    private final Protocol fProtocol;

    /**
     * Constructor of the Packet Class.
     *
     * @param file
     *            The file to which this packet belongs.
     * @param parent
     *            The parent packet of this packet.
     * @param protocol
     *            The protocol of the packet.
     */
    public Packet(PcapFile file, @Nullable Packet parent, Protocol protocol) {
        fPcapFile = file;
        fParentPacket = parent;
        fProtocol = protocol;
    }

    /**
     * Getter method for the Pcap File that contains this packet.
     *
     * @return The Pcap File.
     */
    public PcapFile getPcapFile() {
        return fPcapFile;
    }

    /**
     * Method that returns the parent (encapsulating) packet of this packet.
     * This method returns null if the packet is a Pcap Packet (highest level of
     * encapsulation).
     *
     * @return The parent packet.
     */
    public @Nullable Packet getParentPacket() {
        return fParentPacket;
    }

    /**
     * Method that returns the child (encapsulated) packet of this packet. This
     * method returns null if the packet is at the lowest level of
     * encapsulation.
     *
     * @return The child packet.
     */
    public abstract @Nullable Packet getChildPacket();

    /**
     * Getter method for the protocol of the packet.
     *
     * @return The protocol of the packet.
     */
    public Protocol getProtocol() {
        return fProtocol;
    }

    /**
     * Getter method for the payload of the packet. Returns null if there is no
     * payload.
     *
     * @return the payload of the packet.
     */
    public abstract @Nullable ByteBuffer getPayload();

    /**
     * Method that looks for the packet that respects the specified protocol. It
     * will go through all the layers of encapsulation and return the wanted
     * packet, or null if the protocol is not present.
     *
     * @param protocol
     *            The specified protocol.
     * @return The packet that respects the protocol.
     */
    public final @Nullable Packet getPacket(Protocol protocol) {

        Packet wantedPacket = this;

        while (wantedPacket != null) {
            if (wantedPacket.getProtocol() == protocol) {
                return wantedPacket;
            }
            wantedPacket = wantedPacket.getParentPacket();
        }
        wantedPacket = this.getChildPacket();

        while (wantedPacket != null) {
            if (wantedPacket.getProtocol() == protocol) {
                return wantedPacket;
            }
            wantedPacket = wantedPacket.getChildPacket();
        }

        return null;
    }

    /**
     * Method that looks if the protocol is contained in the packet, or in one
     * of the encapsulating/encapsulated packet. It will go through all the
     * layers of encapsulation and return true if it finds the specified
     * protocol, and false otherwise. *
     *
     * @param protocol
     *            The specified protocol.
     * @return The presence of the protocol.
     */
    public final boolean hasProtocol(Protocol protocol) {

        // TODO Verify inputs
        Packet wantedPacket = this;

        while (wantedPacket != null) {
            if (wantedPacket.getProtocol() == protocol) {
                return true;
            }
            wantedPacket = wantedPacket.getParentPacket();
        }
        wantedPacket = this.getChildPacket();

        while (wantedPacket != null) {
            if (wantedPacket.getProtocol() == protocol) {
                return true;
            }
            wantedPacket = wantedPacket.getChildPacket();
        }

        return false;
    }

    /**
     * Method that returns the most encapsulated packet possible. If the global
     * packet contains the protocol Unknown, it will stop at the packet just
     * before this protocol. This is because the {@link UnknownPacket} can be
     * considered as plain payload.
     *
     * @return The most encapsulated packet.
     */
    public Packet getMostEcapsulatedPacket() {
        @NonNull
        Packet packet = this;
        while (packet.getProtocol() != Protocol.UNKNOWN) {
            Packet childPacket = packet.getChildPacket();
            if (childPacket == null || childPacket.getProtocol() == Protocol.UNKNOWN) {
                break;
            }
            packet = childPacket;
        }
        return packet;
    }

    /**
     * Method that look at the validity of the different fields (such as
     * checksum). This is protocol dependent and is used to identify bad
     * packets.
     *
     * @return The validity of the packet.
     */
    public abstract boolean validate();

    /**
     * Internal method that is used to find the child packet. This is protocol
     * dependent and must be implemented by each packet class.
     *
     * @return The child packet.
     * @throws BadPacketException
     *             Thrown when the packet is erroneous.
     */
    protected abstract @Nullable Packet findChildPacket() throws BadPacketException;

    /**
     * This method returns the source endpoint of this packet. The endpoint is
     * equivalent to the address of this packet, and is protocol dependent. For
     * instance, a UDP endpoint is the combination of the MAC address, the IP
     * address and the port number.
     *
     * @return The source endpoint of this packet.
     */
    public abstract ProtocolEndpoint getSourceEndpoint();

    /**
     * This method returns the destination endpoint of this packet. The endpoint
     * is equivalent to the address of this packet, and is protocol dependent.
     * For instance, a UDP endpoint is the combination of the MAC address, the
     * IP address and the port number.
     *
     * @return The destination endpoint of this packet.
     */
    public abstract ProtocolEndpoint getDestinationEndpoint();

    /**
     * Method that returns all the fields of the packet as a Map<Field ID, Field
     * Value>. All child classes of {@link Packet} must implement this method.
     *
     * @return All the packet fields as a map.
     */
    public abstract Map<String, String> getFields();

    /**
     * Method that returns a short summary of the local packet, such as the most
     * useful information.
     *
     * For instance, a possible summary string of an {@link IPv4Packet} can be:
     * "Src: 192.168.0.1, Dst: 192.168.1.12".
     *
     * @return A short summary of the local packet, as a string.
     */
    public abstract String getLocalSummaryString();

    /**
     * Method that returns the local meaning of a packet, based on its fields.
     *
     * For instance, a possible signification of an ARP packet can be:
     * "Who has 192.168.1.12? Tell 192.168.0.1".
     *
     * @return The local meaning of the packet, as a string.
     */
    protected abstract String getSignificationString();

    /**
     * Method that returns the global meaning of the packet. As such, it will
     * look for the most relevant packet and display its signification.
     *
     * For instance, a possible signification of an ARP packet can be:
     * "Who has 192.168.1.12? Tell 192.168.0.1".
     *
     * @return The meaning of the global packet, as a string.
     */
    public final String getGlobalSummaryString() {
        Packet packet = this.getMostEcapsulatedPacket();
        return packet.getSignificationString();
    }

    @Override
    public abstract boolean equals(@Nullable Object obj);

    @Override
    public abstract int hashCode();

    /**
     * Method that is used by child packet classes to verify if a bit is set.
     *
     * @param value
     *            the byte containing the flags.
     * @param bit
     *            the bit index.
     * @return Whether the bit is set or not.
     */
    protected static final boolean isBitSet(byte value, int bit) {
        if (bit < 0 || bit > 7) {
            throw new IllegalArgumentException("The byte index is not valid!"); //$NON-NLS-1$
        }
        return ((value >>> bit & 0b1) == 0b1);
    }
}
