/*******************************************************************************
 *
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *   Prasanna Vadanan - SOMEIP Dissection and Implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.pcap.core.protocol.someip;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.pcap.core.packet.BadPacketException;
import org.eclipse.tracecompass.internal.pcap.core.packet.Packet;
import org.eclipse.tracecompass.internal.pcap.core.protocol.PcapProtocol;
import org.eclipse.tracecompass.internal.pcap.core.protocol.sd.SOMEIP_SDPacket;
import org.eclipse.tracecompass.internal.pcap.core.protocol.unknown.UnknownPacket;
import org.eclipse.tracecompass.internal.pcap.core.trace.PcapFile;
import org.eclipse.tracecompass.internal.pcap.core.util.ConversionHelper;

import com.google.common.collect.ImmutableMap;

/**
 *
 *
 * Class that represents a SOMEIP packet.
 *
 *
 **/
public class SOMEIPPacket extends Packet {

    private final @Nullable Packet fChildPacket;
    private final @Nullable ByteBuffer fPayload;

    private final long fMessageID;
    private final long fRequestID;
    private final long fTotalLength;
    private final byte fProtocolVersion;
    private final byte fInterfaceVersion;
    private final byte fMessageType;
    private final byte fReturnCode;
    private final byte fFlags;
    private final long fReserved;
    private final long fLengthOfEnteriesArray;
    private final int fLenghtOfEntryArrInInt;


    private @Nullable SOMEIPEndpoint fSourceEndpoint;
    private @Nullable SOMEIPEndpoint fDestinationEndpoint;

    private @Nullable Map<String, String> fFields;

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
    public SOMEIPPacket(PcapFile file, @Nullable Packet parent, ByteBuffer packet) throws BadPacketException {
        super(file, parent, PcapProtocol.UDP);

        // The endpoints are lazy loaded. They are defined in the get*Endpoint()
        // methods.
        fSourceEndpoint = null;
        fDestinationEndpoint = null;
        fFields = null;
        packet.order(ByteOrder.BIG_ENDIAN);
        packet.position(0);
        fMessageID = ConversionHelper.unsignedIntToLong(packet.getInt());
        fTotalLength = ConversionHelper.unsignedIntToLong(packet.getInt());
        fRequestID = ConversionHelper.unsignedIntToLong(packet.getInt());
        fProtocolVersion = packet.get();
        fInterfaceVersion = packet.get();
        //fMessageType = packet.get();
        //fReturnCode = packet.get();
        fMessageType = 0x02;
        fReturnCode = 0x00;
        fFlags = packet.get();
        byte temp_storage = packet.get();
        fReserved=ConversionHelper.unsignedShortToInt(packet.getShort()) & (temp_storage << 8);
        //fLengthOfEnteriesArray = ConversionHelper.unsignedIntToLong(packet.getInt());
        fLengthOfEnteriesArray=0x20;
        //fLenghtOfEntryArrInInt= 1;
        fLenghtOfEntryArrInInt=(int) (fLengthOfEnteriesArray/16);

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

        switch (fMessageType) {
        case 0x02:
            switch(fReturnCode) {
            case 0x00:
                 return new SOMEIP_SDPacket(getPcapFile(), this, payload,fLenghtOfEntryArrInInt);
            default:
                return new UnknownPacket(getPcapFile(), this, payload);
             }
          default:
             return new UnknownPacket(getPcapFile(), this, payload);
        }

    }

    @Override
    public String toString() {
        String string = getProtocol().getName() + ", MessageID : " + String.format("%s%04x", "0x", fMessageID)+ getMessageIDFields() + ", RequestID: " + String.format("%s%04x", "0x", fRequestID)+ getRequestIDFields() + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                ", Length: " +  String.format("%s%04x", "0x", fTotalLength) + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        final Packet child = fChildPacket;
        if (child != null) {
            return string + child.toString();
        }
        return string;
    }

    /**
     * Getter method that returns the Message ID of a SOME-IP Frame.
     *
     * @return The message ID.
     */
    public long getMessageID() {
        return fMessageID;
    }


    /**
     * @return the subfields of Message ID Field
     */
    public String getMessageIDFields() {

        String fMessageIDFields = null;

        long service_id=fMessageID;
        service_id=service_id & 0xffff0000;
        service_id=service_id>>16;

        int flag_check=(int) (fMessageID & 0x00008000);
        flag_check=flag_check>>15;

        switch(flag_check) {

        case 0:
            long method_id=fMessageID&0x00007fff;
            fMessageIDFields="Service ID :"+String.format("%s%04x", "0x", service_id)+"  Method ID :"+String.format("%s%04x", "0x", method_id); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
            break;
        default:
            long event_id=fMessageID&0x00007fff;
            fMessageIDFields="Service ID :"+String.format("%s%04x", "0x", service_id)+ "  Event ID :"+String.format("%s%04x", "0x", event_id); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
            break;
         }

        return fMessageIDFields;

   }

    /**
     * Getter method that returns the Request ID of a SOME-IP Frame.
     *
     * @return The Request ID.
     */
    public long getRequestID() {
        return fRequestID;
    }


    /**
     * @return the subfields of Request ID Field
     */
    public String getRequestIDFields() {

        String fRequestIDFields = null;

        long client_id=(fRequestID&0xffff0000)>>16;
        long session_id=(fRequestID&0x0000ffff);
        fRequestIDFields="Client ID :"+String.format("%s%04x", "0x", client_id)+"  Session ID :"+String.format("%s%04x", "0x", session_id); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        return fRequestIDFields;


    }

    /**
     * Getter method that returns the total length of the packet in bytes. The
     * values it can take go from 8 to 65,515.
     *
     * @return The total length of the packet in bytes.
     */
    public long getTotalLength() {
        return fTotalLength;
    }

    /**
     * Getter method that returns the Protocol Version Field of a SOME-IP Frame.
     *
     * @return The Protocol Version Field.
     */
    public byte getProtocolVersion() {
        return fProtocolVersion;
    }

    /**
     * Getter method that returns the Interface Version Field of a SOME-IP Frame.
     *
     * @return The Interface Version Field.
     */
    public byte getInterfaceVersion() {
        return fInterfaceVersion;
    }

    /**
     * Getter method that returns the Message Type Field of a SOME-IP Frame.
     *
     * @return The Message Type Field.
     */
    public byte getMessageType() {
        return fMessageType;
    }

    /**
     * @return the message type in string format
     */
    public String getMessageTypeToString() {

        String fMessageTypeToString=null;

        switch(fMessageType) {

        case 0x00:
              fMessageTypeToString= String.format("%s%04x", "0x", fMessageType) + "  [REQUEST]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
              return fMessageTypeToString;
        case 0x01:
            fMessageTypeToString= String.format("%s%04x", "0x", fMessageType) + "  [REQUEST_NO_RETURN]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return fMessageTypeToString;
        case 0x02:
            fMessageTypeToString= String.format("%s%04x", "0x", fMessageType) + "  [NOTIFICATION]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return fMessageTypeToString;
        case 0x40:
            fMessageTypeToString= String.format("%s%04x", "0x", fMessageType) + "  [REQUEST_ACK]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return fMessageTypeToString;
        case 0x41:
            fMessageTypeToString= String.format("%s%04x", "0x", fMessageType) + "  [REQUEST_NO_RETURN_ACK]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return fMessageTypeToString;
        case 0x42:
            fMessageTypeToString= String.format("%s%04x", "0x", fMessageType) + "  [NOTIFICATION_ACK]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return fMessageTypeToString;
        case (byte)0x80:
            fMessageTypeToString= String.format("%s%04x", "0x", fMessageType) + "  [RESPONSE]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return fMessageTypeToString;
        case (byte)0x81:
            fMessageTypeToString= String.format("%s%04x", "0x", fMessageType) + "  [ERROR]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return fMessageTypeToString;
        case (byte) 0xC0:
            fMessageTypeToString= String.format("%s%04x", "0x", fMessageType) + "  [RESPONSE_ACK]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return fMessageTypeToString;
        case (byte) 0xC1:
            fMessageTypeToString= String.format("%s%04x", "0x", fMessageType) + "  [ERROR_ACK]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return fMessageTypeToString;
        default:
            fMessageTypeToString= String.format("%s%04x", "0x", fMessageType) + "  [ERR]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return fMessageTypeToString;
        }


    }

    /**
     * Getter method that returns the Return Type Field of a SOME-IP Frame.
     *
     * @return The Return Type Field.
     */
    public byte getReturnCode() {
        return fReturnCode;
    }
    /**
     * @return the Return Code in string format
     */
    public String getReturnCodeToString() {

        String fReturnCodeToString=null;

        switch(fReturnCode) {

        case 0x00:
              fReturnCodeToString= String.format("%s%04x", "0x", fReturnCode) + "  [E_OK]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
              return fReturnCodeToString;
        case 0x01:
            fReturnCodeToString= String.format("%s%04x", "0x", fReturnCode) + "  [E_NOT_OK]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return fReturnCodeToString;
        case 0x02:
            fReturnCodeToString= String.format("%s%04x", "0x", fReturnCode) + "  [E_UNKNOWN_SERVICE]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return fReturnCodeToString;
        case 0x03:
            fReturnCodeToString= String.format("%s%04x", "0x", fReturnCode) + "  [E_UNKNOWN_METHOD]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return fReturnCodeToString;
        case 0x04:
            fReturnCodeToString= String.format("%s%04x", "0x", fReturnCode) + "  [E_NOT_READY]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return fReturnCodeToString;
        case 0x05:
            fReturnCodeToString= String.format("%s%04x", "0x", fReturnCode) + "  [E_NOT_REACHACABLE]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return fReturnCodeToString;
        case 0x06:
            fReturnCodeToString= String.format("%s%04x", "0x", fReturnCode) + "  [E_TIMEOUT]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return fReturnCodeToString;
        case 0x07:
            fReturnCodeToString= String.format("%s%04x", "0x", fReturnCode) + "  [E_WRONG_PROTOCOL_VERSION]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return fReturnCodeToString;
        case 0x08:
            fReturnCodeToString= String.format("%s%04x", "0x", fReturnCode) + "  [E_WRONG_INTERFACE_VERSION]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return fReturnCodeToString;
        case 0x09:
            fReturnCodeToString= String.format("%s%04x", "0x", fReturnCode) + "  [E_MALFORMED_MESSAGE]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return fReturnCodeToString;
        case 0x0A:
            fReturnCodeToString= String.format("%s%04x", "0x", fReturnCode) + "  [E_WRONG_MESSAGE_TYPE]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return fReturnCodeToString;
        default:
            fReturnCodeToString= String.format("%s%04x", "0x", fReturnCode) + "  [RESERVED]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return fReturnCodeToString;
        }


    }


    /**
     * @return the flags
     */
    public byte getFlags() {
        return fFlags;
    }

    /**
     * @return the reserved
     */
    public long getReserved() {
        return fReserved;
    }

    /**
     * @return the lengthOfEnteriesArray
     */
    public long getLengthOfEnteriesArray() {
        return fLengthOfEnteriesArray;
    }

    /**
     * @return the lenghtofentryarrinint
     */
    public int getLenghtofentryarrinint() {
        return fLenghtOfEntryArrInInt;
    }

    @Override
    public boolean validate() {
        // Not yet implemented. ATM, we consider that all packets are valid.
        // This is the case for all packets.
        // TODO Implement it.
        return true;
    }

    @Override
    public SOMEIPEndpoint getSourceEndpoint() {
        @Nullable
        SOMEIPEndpoint endpoint = fSourceEndpoint;
        if (endpoint == null) {
            endpoint = new SOMEIPEndpoint(this, true);
        }
        fSourceEndpoint = endpoint;
        return fSourceEndpoint;
    }

    @Override
    public SOMEIPEndpoint getDestinationEndpoint() {
        @Nullable SOMEIPEndpoint endpoint = fDestinationEndpoint;
        if (endpoint == null) {
            endpoint = new SOMEIPEndpoint(this, false);
        }
        fDestinationEndpoint = endpoint;
        return fDestinationEndpoint;
    }

    @Override
    public Map<String, String> getFields() {
        Map<String, String> map = fFields;
        if (map == null) {
            ImmutableMap.Builder<String, String> builder = ImmutableMap.<@NonNull String, @NonNull String> builder()
                    .put("Message ID ", getMessageIDFields()) //$NON-NLS-1$
                    .put("Request ID ", getRequestIDFields()) //$NON-NLS-1$
                    .put("Length", String.format("%s%04x", "0x", fTotalLength)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    .put("Protcol Version", String.format("%s%04x", "0x", fProtocolVersion))  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                    .put("Interface Version", String.format("%s%04x", "0x", fInterfaceVersion)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    .put("Message Type", getMessageTypeToString() ) //$NON-NLS-1$
                    .put("Return Code", getReturnCodeToString()) //$NON-NLS-1$
                    .put("Flags", String.format("%s%04x", "0x", fFlags)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    .put("Reserved", String.format("%s%04x", "0x", fReserved)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    .put("Length Of Entries Array", String.format("%s%04x", "0x", fLengthOfEnteriesArray)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            fFields = builder.build();
            return fFields;
        }
        return map;
    }

     @Override
    public String getLocalSummaryString() {
        return "Msg ID: " + String.format("%s%04x", "0x", fMessageID) + ", Req ID: " + String.format("%s%04x", "0x", fRequestID) +", Len: " + fTotalLength + ", ProtoVer: " + String.format("%s%04x", "0x", fProtocolVersion)+", IfaceVer: " + String.format("%s%04x", "0x", fInterfaceVersion)+", MsgType: " + String.format("%s%04x", "0x", fMessageType)+", RetCode: " + String.format("%s%04x", "0x", fReturnCode); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$ //$NON-NLS-16$ //$NON-NLS-17$ //$NON-NLS-18$ //$NON-NLS-19$
    }

    @Override
    protected String getSignificationString() {
        return "Message ID: " + String.format("%s%04x", "0x", fMessageID) + ", Request ID: " + String.format("%s%04x", "0x", fRequestID)+ ", Length: " + fTotalLength; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
    }

    @Override
    public int hashCode() {
        //final int prime = 31;
        int result = 1;
        /*
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
        result = prime * result + fMessageID;
        result = prime * result + fTotalLength;
        */
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
        SOMEIPPacket other = (SOMEIPPacket) obj;
        if (fProtocolVersion != other.fProtocolVersion) {
            return false;
        }
        if(!Objects.equals(fChildPacket, other.fChildPacket)){
            return false;
        }
        if (fRequestID != other.fRequestID) {
            return false;
        }
        if(!Objects.equals(fPayload, other.fPayload)){
            return false;
        }
        if ( fMessageID != other.fMessageID) {
            return false;
        }
        if (fTotalLength != other.fTotalLength) {
            return false;
        }
        return true;
    }

}
