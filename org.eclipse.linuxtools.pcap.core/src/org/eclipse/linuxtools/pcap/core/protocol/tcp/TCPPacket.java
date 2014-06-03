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

package org.eclipse.linuxtools.pcap.core.protocol.tcp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
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
import com.google.common.collect.ImmutableMap.Builder;

/**
 * Class that represents a TCP packet.
 *
 * @author Vincent Perot
 */
public class TCPPacket extends Packet {

    private final @Nullable Packet fChildPacket;
    private final @Nullable ByteBuffer fPayload;

    private final int fSourcePort;
    private final int fDestinationPort;
    private final long fSequenceNumber;
    private final long fAcknowledgmentNumber;
    private final int fDataOffset; // in 4 bytes block
    private final byte fReservedField;
    private final boolean fNSFlag;
    private final boolean fCWRFlag;
    private final boolean fECEFlag;
    private final boolean fURGFlag;
    private final boolean fACKFlag;
    private final boolean fPSHFlag;
    private final boolean fRSTFlag;
    private final boolean fSYNFlag;
    private final boolean fFINFlag;
    private final int fWindowSize;
    private final int fChecksum;
    private final int fUrgentPointer;
    private final @Nullable byte[] fOptions; // TODO Interpret options.

    private @Nullable TCPEndpoint fSourceEndpoint;
    private @Nullable TCPEndpoint fDestinationEndpoint;

    private @Nullable ImmutableMap<String, String> fFields;

    /**
     * Constructor of the TCP Packet class.
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
    public TCPPacket(PcapFile file, @Nullable Packet parent, ByteBuffer packet) throws BadPacketException {
        super(file, parent, Protocol.TCP);

        // The endpoints are lazy loaded. They are defined in the get*Endpoint()
        // methods.
        fSourceEndpoint = null;
        fDestinationEndpoint = null;

        fFields = null;

        packet.order(ByteOrder.BIG_ENDIAN);
        packet.position(0);

        fSourcePort = ConversionHelper.unsignedShortToInt(packet.getShort());
        fDestinationPort = ConversionHelper.unsignedShortToInt(packet.getShort());
        fSequenceNumber = ConversionHelper.unsignedIntToLong(packet.getInt());
        fAcknowledgmentNumber = ConversionHelper.unsignedIntToLong(packet.getInt());

        byte storage = packet.get();
        fDataOffset = ((storage & 0b11110000) >>> 4) & 0x000000FF;
        fReservedField = (byte) ((storage & 0b00001110) >>> 1);
        fNSFlag = isBitSet(storage, 0);

        storage = packet.get();
        fCWRFlag = isBitSet(storage, 7);
        fECEFlag = isBitSet(storage, 6);
        fURGFlag = isBitSet(storage, 5);
        fACKFlag = isBitSet(storage, 4);
        fPSHFlag = isBitSet(storage, 3);
        fRSTFlag = isBitSet(storage, 2);
        fSYNFlag = isBitSet(storage, 1);
        fFINFlag = isBitSet(storage, 0);

        fWindowSize = ConversionHelper.unsignedShortToInt(packet.getShort());
        fChecksum = ConversionHelper.unsignedShortToInt(packet.getShort());
        fUrgentPointer = ConversionHelper.unsignedShortToInt(packet.getShort());

        // Get options if any
        if (fDataOffset > TCPValues.DEFAULT_HEADER_LENGTH) {
            fOptions = new byte[(fDataOffset - TCPValues.DEFAULT_HEADER_LENGTH) * TCPValues.BLOCK_SIZE];
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

        // find child packet
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
        final ByteBuffer payload = fPayload;
        int length = 0;
        if (payload != null) {
            length = payload.array().length;
        }

        String flagString = ""; // TODO Finish it. Im just too lazy. //$NON-NLS-1$
        String string = getProtocol().getName() + ", Source Port: " + fSourcePort + ", Destination Port: " + fDestinationPort + //$NON-NLS-1$ //$NON-NLS-2$
                "\nSequence Number: " + fSequenceNumber + ", Acknowledgment Number: " + fAcknowledgmentNumber + //$NON-NLS-1$ //$NON-NLS-2$
                "\nHeader length: " + fDataOffset * TCPValues.BLOCK_SIZE + " bytes, Data length: " + length + //$NON-NLS-1$ //$NON-NLS-2$
                "\n" + flagString + "Window size value: " + fWindowSize + ", Urgent Pointer: " + String.format("%s%04x", "0x", fUrgentPointer) + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                "\nChecksum: " + String.format("%s%04x", "0x", fChecksum) + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        final Packet child = fChildPacket;
        if (child != null) {
            return string + child.toString();
        }
        return string;
    }

    /**
     * Getter method that returns the TCP Source Port.
     *
     * @return The source Port.
     */
    public int getSourcePort() {
        return fSourcePort;
    }

    /**
     * Getter method that returns the TCP Destination Port.
     *
     * @return The destination Port.
     */
    public int getDestinationPort() {
        return fDestinationPort;
    }

    /**
     * Getter method that returns the Sequence Number. The sequence number has a
     * dual role:
     * <ul>
     * <li>If the SYN flag is set (1), then this is the initial sequence number.
     * The sequence number of the actual first data byte and the acknowledged
     * number in the corresponding ACK are then this sequence number plus 1.</li>
     * <li>If the SYN flag is clear (0), then this is the accumulated sequence
     * number of the first data byte of this segment for the current session.</li>
     * </ul>
     *
     * Source: http://en.wikipedia.org/wiki/Transmission_Control_Protocol
     *
     * @return The Sequence Number.
     */
    public long getSequenceNumber() {
        return fSequenceNumber;
    }

    /**
     * Getter method that returns the Acknowledgment Number.
     *
     * If the ACK flag is set then the value of this field is the next sequence
     * number that the receiver is expecting. This acknowledges receipt of all
     * prior bytes (if any). The first ACK sent by each end acknowledges the
     * other end's initial sequence number itself, but no data.
     *
     * Source: http://en.wikipedia.org/wiki/Transmission_Control_Protocol
     *
     * @return The Acknowledgment Number.
     */
    public long getAcknowledgmentNumber() {
        return fAcknowledgmentNumber;
    }

    /**
     * Getter method that returns the size of the TCP header in 4 bytes data
     * block. The minimum size is 5 words and the maximum is 15 words.
     *
     * @return The Data Offset.
     */
    public int getDataOffset() {
        return fDataOffset;
    }

    /**
     * Getter method that returns the Reserved field. This field is for future
     * use and should always be zero. In this library, it is used as a mean to
     * verify the validity of a TCP packet.
     *
     * @return The Reserved Field.
     */
    public byte getReservedField() {
        return fReservedField;
    }

    /**
     * Getter method that returns the state of the NS flag.
     *
     * @return The state of the NS flag.
     */
    public boolean isNSFlagSet() {
        return fNSFlag;
    }

    /**
     * Getter method that returns the state of the CWR flag.
     *
     * @return The state of the CWR flag.
     */
    public boolean isCongestionWindowReducedFlagSet() {
        return fCWRFlag;
    }

    /**
     * Getter method that returns the state of the ECE flag.
     *
     * @return The state of the ECE flag.
     */
    public boolean isECNEchoFlagSet() {
        return fECEFlag;
    }

    /**
     * Getter method that returns the state of the URG flag.
     *
     * @return The state of the URG flag.
     */
    public boolean isUrgentFlagSet() {
        return fURGFlag;
    }

    /**
     * Getter method that returns the state of the ACK flag.
     *
     * @return The state of the ACK flag.
     */
    public boolean isAcknowledgeFlagSet() {
        return fACKFlag;
    }

    /**
     * Getter method that returns the state of the PSH flag.
     *
     * @return The state of the PSH flag.
     */
    public boolean isPushFlagSet() {
        return fPSHFlag;
    }

    /**
     * Getter method that returns the state of the RST flag.
     *
     * @return The state of the RST flag.
     */
    public boolean isResetFlagSet() {
        return fRSTFlag;
    }

    /**
     * Getter method that returns the state of the SYN flag.
     *
     * @return The state of the SYN flag.
     */
    public boolean isSynchronizationFlagSet() {
        return fSYNFlag;
    }

    /**
     * Getter method that returns the state of the FIN flag.
     *
     * @return The state of the FIN flag.
     */
    public boolean isFinalFlagSet() {
        return fFINFlag;
    }

    /**
     * Getter method that returns the size of the windows, in windows size unit
     * (by default, bytes), that the sender of this packet is willing to
     * receive.
     *
     * @return The Window Size.
     */
    public int getWindowSize() {
        return fWindowSize;
    }

    /**
     * Getter method that returns the checksum of this packet. This checksum may
     * be wrong if the packet is erroneous.
     *
     * @return The data and header checksum.
     */
    public int getChecksum() {
        return fChecksum;
    }

    /**
     * Getter method that returns the Urgent Pointer. If the URG flag is set,
     * this field is an offset from the sequence number indicating the last
     * urgent data byte.
     *
     * @return The Urgent Pointer.
     */
    public int getUrgentPointer() {
        return fUrgentPointer;
    }

    /**
     * Getter method that returns the options. This method returns null if no
     * options are present.
     *
     * @return The options of the packet.
     */
    public @Nullable byte[] getOptions() {
        byte[] options = fOptions;
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
    public TCPEndpoint getSourceEndpoint() {
        @Nullable
        TCPEndpoint endpoint = fSourceEndpoint;
        if (endpoint == null) {
            endpoint = new TCPEndpoint(this, true);
        }
        fSourceEndpoint = endpoint;
        return fSourceEndpoint;
    }

    @Override
    public TCPEndpoint getDestinationEndpoint() {
        @Nullable
        TCPEndpoint endpoint = fDestinationEndpoint;

        if (endpoint == null) {
            endpoint = new TCPEndpoint(this, false);
        }
        fDestinationEndpoint = endpoint;
        return fDestinationEndpoint;
    }

    @Override
    public Map<String, String> getFields() {
        ImmutableMap<String, String> map = fFields;
        if (map == null) {
            Builder<String, String> builder = ImmutableMap.<String, String> builder()
                    .put("Source Port", String.valueOf(fSourcePort)) //$NON-NLS-1$
                    .put("Destination Port", String.valueOf(fDestinationPort)) //$NON-NLS-1$
                    .put("Sequence Number", String.valueOf(fSequenceNumber)) //$NON-NLS-1$
                    .put("Acknowledgement Number", String.valueOf(fAcknowledgmentNumber)) //$NON-NLS-1$
                    .put("Length", String.valueOf(fDataOffset * TCPValues.BLOCK_SIZE) + " bytes") //$NON-NLS-1$ //$NON-NLS-2$
                    .put("ECN-Nonce Flag", String.valueOf(fNSFlag)) //$NON-NLS-1$
                    .put("Congestion Window Reduced Flag", String.valueOf(fCWRFlag)) //$NON-NLS-1$
                    .put("ECN-Echo Flag", String.valueOf(fECEFlag)) //$NON-NLS-1$
                    .put("Urgent Flag", String.valueOf(fURGFlag)) //$NON-NLS-1$
                    .put("ACK Flag", String.valueOf(fACKFlag)) //$NON-NLS-1$
                    .put("PSH Flag", String.valueOf(fPSHFlag)) //$NON-NLS-1$
                    .put("RST Flag", String.valueOf(fRSTFlag)) //$NON-NLS-1$
                    .put("SYN Flag", String.valueOf(fSYNFlag)) //$NON-NLS-1$
                    .put("FIN Flag", String.valueOf(fFINFlag)) //$NON-NLS-1$
                    .put("Window Size Value", String.valueOf(fWindowSize)) //$NON-NLS-1$
                    .put("Checksum", String.format("%s%04x", "0x", fChecksum)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    .put("Urgent Pointer", String.format("%s%04x", "0x", fUrgentPointer)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            byte[] options = fOptions;
            if (options == null) {
                builder.put("Options", EMPTY_STRING); //$NON-NLS-1$
            } else {
                builder.put("Options", ConversionHelper.bytesToHex(options, true)); //$NON-NLS-1$

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
        return "Src Port: " + fSourcePort + ", Dst Port: " + fDestinationPort + //$NON-NLS-1$ //$NON-NLS-2$
                ", Seq: " + fSequenceNumber + ", Ack: " + fAcknowledgmentNumber + //$NON-NLS-1$ //$NON-NLS-2$
                ", Len: " + (fDataOffset * TCPValues.BLOCK_SIZE); //$NON-NLS-1$    }
    }

    @Override
    protected String getSignificationString() {
        StringBuilder sb = new StringBuilder();
        sb.append(fSourcePort)
                .append(" > ") //$NON-NLS-1$
                .append(fDestinationPort);

        if (!(generateFlagString().equals(EMPTY_STRING))) {
            sb.append(' ')
                    .append('[')
                    .append(generateFlagString())
                    .append(']');
        }
        sb.append(" Seq=") //$NON-NLS-1$
        .append(fSequenceNumber);

        if (fACKFlag) {
            sb.append(" Ack=") //$NON-NLS-1$
            .append(fAcknowledgmentNumber);
        }

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

        if (fSYNFlag) {
            if (!start) {
                sb.append(", "); //$NON-NLS-1$
            }
            sb.append("SYN"); //$NON-NLS-1$
            start = false;
        }
        if (fACKFlag) {
            if (!start) {
                sb.append(", "); //$NON-NLS-1$
            }
            sb.append("ACK"); //$NON-NLS-1$
            start = false;
        }
        if (fFINFlag) {
            if (!start) {
                sb.append(", "); //$NON-NLS-1$
            }
            sb.append("FIN"); //$NON-NLS-1$
            start = false;
        }
        if (fRSTFlag) {
            if (!start) {
                sb.append(", "); //$NON-NLS-1$
            }
            sb.append("RST"); //$NON-NLS-1$
            start = false;
        }
        if (fPSHFlag) {
            if (!start) {
                sb.append(", "); //$NON-NLS-1$
            }
            sb.append("PSH"); //$NON-NLS-1$
            start = false;
        }
        if (fURGFlag) {
            if (!start) {
                sb.append(", "); //$NON-NLS-1$
            }
            sb.append("URG"); //$NON-NLS-1$
            start = false;
        }
        if (fNSFlag) {
            if (!start) {
                sb.append(", "); //$NON-NLS-1$
            }
            sb.append("NS"); //$NON-NLS-1$
            start = false;
        }
        if (fCWRFlag) {
            if (!start) {
                sb.append(", "); //$NON-NLS-1$
            }
            sb.append("CWR"); //$NON-NLS-1$
            start = false;
        }
        if (fECEFlag) {
            if (!start) {
                sb.append(", "); //$NON-NLS-1$
            }
            sb.append("ECE"); //$NON-NLS-1$
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
        result = prime * result + (fACKFlag ? 1231 : 1237);
        result = prime * result + (int) (fAcknowledgmentNumber ^ (fAcknowledgmentNumber >>> 32));
        result = prime * result + (fCWRFlag ? 1231 : 1237);
        result = prime * result + fChecksum;
        final Packet child = fChildPacket;
        if (child != null) {
            result = prime * result + child.hashCode();
        } else {
            result = prime * result;
        }
        result = prime * result + fDataOffset;
        result = prime * result + fDestinationPort;
        result = prime * result + (fECEFlag ? 1231 : 1237);
        result = prime * result + (fFINFlag ? 1231 : 1237);
        result = prime * result + (fNSFlag ? 1231 : 1237);
        result = prime * result + Arrays.hashCode(fOptions);
        result = prime * result + (fPSHFlag ? 1231 : 1237);
        final ByteBuffer payload = fPayload;
        if (payload != null) {
            result = prime * result + payload.hashCode();
        } else {
            result = prime * result;
        }
        result = prime * result + (fRSTFlag ? 1231 : 1237);
        result = prime * result + fReservedField;
        result = prime * result + (fSYNFlag ? 1231 : 1237);
        result = prime * result + (int) (fSequenceNumber ^ (fSequenceNumber >>> 32));
        result = prime * result + fSourcePort;
        result = prime * result + (fURGFlag ? 1231 : 1237);
        result = prime * result + fUrgentPointer;
        result = prime * result + fWindowSize;
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
        TCPPacket other = (TCPPacket) obj;
        if (fACKFlag != other.fACKFlag) {
            return false;
        }
        if (fAcknowledgmentNumber != other.fAcknowledgmentNumber) {
            return false;
        }
        if (fCWRFlag != other.fCWRFlag) {
            return false;
        }
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

        if (fDataOffset != other.fDataOffset) {
            return false;
        }
        if (fDestinationPort != other.fDestinationPort) {
            return false;
        }
        if (fECEFlag != other.fECEFlag) {
            return false;
        }
        if (fFINFlag != other.fFINFlag) {
            return false;
        }
        if (fNSFlag != other.fNSFlag) {
            return false;
        }
        if (!Arrays.equals(fOptions, other.fOptions)) {
            return false;
        }
        if (fPSHFlag != other.fPSHFlag) {
            return false;
        }
        final ByteBuffer fPayload2 = fPayload;
        if (fPayload2 != null) {
            if (!fPayload2.equals(other.fPayload)) {
                return false;
            }
        } else {
            if (other.fPayload != null) {
                return false;
            }
        }
        if (fRSTFlag != other.fRSTFlag) {
            return false;
        }
        if (fReservedField != other.fReservedField) {
            return false;
        }
        if (fSYNFlag != other.fSYNFlag) {
            return false;
        }
        if (fSequenceNumber != other.fSequenceNumber) {
            return false;
        }
        if (fSourcePort != other.fSourcePort) {
            return false;
        }
        if (fURGFlag != other.fURGFlag) {
            return false;
        }
        if (fUrgentPointer != other.fUrgentPointer) {
            return false;
        }
        if (fWindowSize != other.fWindowSize) {
            return false;
        }
        return true;
    }

}
