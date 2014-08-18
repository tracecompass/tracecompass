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

package org.eclipse.linuxtools.internal.pcap.core.protocol.unknown;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.internal.pcap.core.packet.Packet;
import org.eclipse.linuxtools.internal.pcap.core.protocol.PcapProtocol;
import org.eclipse.linuxtools.internal.pcap.core.trace.PcapFile;
import org.eclipse.linuxtools.internal.pcap.core.util.ConversionHelper;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * Class that represents an Unknown packet. It is possible to get such a packet
 * if the protocol has not been implemented in this library or if the parent
 * packet was invalid (in certain cases only). The header of such a packet is
 * inexistent.
 *
 * @author Vincent Perot
 */
public class UnknownPacket extends Packet {

    private final @Nullable Packet fChildPacket;
    private final ByteBuffer fPayload;

    private @Nullable UnknownEndpoint fSourceEndpoint;
    private @Nullable UnknownEndpoint fDestinationEndpoint;

    private @Nullable ImmutableMap<String, String> fFields;

    /**
     * Constructor of an Unknown Packet object.
     *
     * @param file
     *            The file to which this packet belongs.
     * @param parent
     *            The parent packet of this packet.
     * @param packet
     *            The entire packet (header and payload).
     */
    public UnknownPacket(PcapFile file, @Nullable Packet parent, ByteBuffer packet) {
        super(file, parent, PcapProtocol.UNKNOWN);

        // The endpoints are lazy loaded. They are defined in the get*Endpoint()
        // methods.
        fSourceEndpoint = null;
        fDestinationEndpoint = null;

        fFields = null;

        // Header is not used. All data go into payload.
        fPayload = packet;

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

    @Override
    protected @Nullable Packet findChildPacket() {
        return null;
    }

    @Override
    public String toString() {
        @SuppressWarnings("null")
        @NonNull byte[] array = fPayload.array();
        String string = "Payload: " + ConversionHelper.bytesToHex(array, true); //$NON-NLS-1$
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
    public UnknownEndpoint getSourceEndpoint() {
        @Nullable UnknownEndpoint endpoint = fSourceEndpoint;
        if (endpoint == null) {
            endpoint = new UnknownEndpoint(this, true);
        }
        fSourceEndpoint = endpoint;
        return fSourceEndpoint;
    }

    @Override
    public UnknownEndpoint getDestinationEndpoint() {
        @Nullable UnknownEndpoint endpoint = fDestinationEndpoint;
        if (endpoint == null) {
            endpoint = new UnknownEndpoint(this, false);
        }
        fDestinationEndpoint = endpoint;
        return fDestinationEndpoint;
    }

    @Override
    public Map<String, String> getFields() {
        ImmutableMap<String, String> map = fFields;
        if (map == null) {
            @SuppressWarnings("null")
            @NonNull byte[] array = fPayload.array();

            Builder<String, String> builder = ImmutableMap.<String, String> builder()
                    .put("Binary", ConversionHelper.bytesToHex(array, true)); //$NON-NLS-1$
            try {
                String s = new String(array, "UTF-8"); //$NON-NLS-1$
                builder.put("Character", s); //$NON-NLS-1$
            } catch (UnsupportedEncodingException e) {
                // Do nothing. The string won't be added to the map anyway.
            }
            @SuppressWarnings("null")
            @NonNull ImmutableMap<String, String> newMap = builder.build();
            fFields = newMap;
            return newMap;
        }
        return map;
    }

    @Override
    public String getLocalSummaryString() {
        return "Len: " + fPayload.array().length + " bytes"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected String getSignificationString() {
        return "Data: " + fPayload.array().length + " bytes"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public Packet getMostEcapsulatedPacket() {
        Packet packet = this.getParentPacket();
        if (packet == null) {
            return this;
        }
        return packet;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        final Packet child = fChildPacket;
        if (child != null) {
            result = prime * result + ((fChildPacket == null) ? 0 : child.hashCode());
        } else {
            result = prime * result;
        }
        result = prime * result + fPayload.hashCode();
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
        UnknownPacket other = (UnknownPacket) obj;
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

        if (!fPayload.equals(other.fPayload)) {
            return false;
        }
        return true;
    }

}
