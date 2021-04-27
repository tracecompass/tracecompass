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

package org.eclipse.tracecompass.internal.pcap.core.protocol.someip_service_discovery;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.pcap.core.packet.BadPacketException;
import org.eclipse.tracecompass.internal.pcap.core.packet.Packet;
import org.eclipse.tracecompass.internal.pcap.core.protocol.PcapProtocol;
import org.eclipse.tracecompass.internal.pcap.core.protocol.unknown.UnknownPacket;
import org.eclipse.tracecompass.internal.pcap.core.trace.PcapFile;
import org.eclipse.tracecompass.internal.pcap.core.util.ConversionHelper;

import com.google.common.collect.ImmutableMap;

/**
 *
 *
 * Class that represents a SOMEIP SD packet.
 *
 *
 **/
public class SOMEIP_SDPacket extends Packet {

    private final @Nullable Packet fChildPacket;
    private final @Nullable ByteBuffer fPayload;



    private @Nullable SOMEIP_SDEndpoint fSourceEndpoint;
    private @Nullable SOMEIP_SDEndpoint fDestinationEndpoint;

    private @Nullable Map<String, String> fFields;
    private int fCount;
    private byte  fType;
    private byte fIndex1;
    private byte fIndex2;
    private byte fOptions;
    private int fServiceID;
    private int fInstanceID;
    private byte fMajorVersion;
    private int fTTL;
    private long fMinorVersion;
    private long fLengthOfOptionsArray=10;
    /**
     *
     */
    public ByteBuffer fpacket;

    /**
     * Constructor of the UDP Packet class.
     *
     * @param file
     *            The file that contains this packet.
     * @param parent
     *            The parent packet of this packet (the encapsulating packet).
     * @param packet
     *            The entire packet (header and payload).
     * @param LenghtOfEntryArrayInInt
     * @throws BadPacketException
     *             Thrown when the packet is erroneous.
     */
    @SuppressWarnings("javadoc")
    public SOMEIP_SDPacket(PcapFile file, @Nullable Packet parent, ByteBuffer packet, int LenghtOfEntryArrayInInt) throws BadPacketException {
        super(file, parent, PcapProtocol.SD);

        // The endpoints are lazy loaded. They are defined in the get*Endpoint()
        // methods.
        fSourceEndpoint = null;
        fDestinationEndpoint = null;

        fCount=LenghtOfEntryArrayInInt;


        packet.order(ByteOrder.BIG_ENDIAN);
        packet.position(0);
        setFpacket(packet);
        fpacket=getFpacket();
        fpacket.order(ByteOrder.BIG_ENDIAN);
        fpacket.position(0);

        // Code Snippet below is used to find out if there are any options array data.
        // if any data is available  it stores in the payload field which is nothing but the raw data of options array
        if (fpacket.array().length - fpacket.position() > (28+(fCount*16)))
                {
            byte[] array = new byte[fpacket.array().length - fpacket.position()];
            fpacket.get(array);

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

    /**
     * @return fpacket
     */
    public ByteBuffer getFpacket() {
        return fpacket;
    }

    /**
     * @param fpacket stores the entire bytebuffer received from SOMEIP parent packet
     */
    public void setFpacket(ByteBuffer fpacket) {
        this.fpacket = fpacket;
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


    /**
     *
     * @return String
     */
    @Override
    public String toString() {
        String string = getProtocol().getName()+"\n"; //$NON-NLS-1$
        final Packet child = fChildPacket;
        if (child != null) {
            return string + child.toString();
        }
        return string;
    }

    /**
     *
     * @return the type
     */
    public byte getType() {
        fType=fpacket.get();
        //fType=0;
        return fType;
    }

    /**
     * @return the Type of SD entry in string format.
     */
    public String getTypeInString() {


        switch(getType()) {
        case 0x00:
            return "0x00 [FIND SERVICE]"; //$NON-NLS-1$
        case 0x01:
            return "0x01 [OFFER SERVICE]"; //$NON-NLS-1$
        case 0x06:
            return "0x06 [SUBSCRIBE]"; //$NON-NLS-1$
        case 0x07:
            return "0x07 [SUBSCRIBE ACK]"; //$NON-NLS-1$
        default:
            return "0x01 [OFFER SERVICE]"; //$NON-NLS-1$
        }


    }

    /**
     * @return the index1
     */
    public byte getIndex1() {
        fIndex1=fpacket.get();
        //fIndex1=0;
        return fIndex1;
    }

    /**
     * @return the index2
     */
    public byte getIndex2() {
        fIndex2=fpacket.get();
        //fIndex2=0;
        return fIndex2;
    }

    /**
     * @return the options
     */
    public byte getOptions() {
        fOptions=fpacket.get();
        //fOptions=0;
        return fOptions;
    }

    /**
     * @return get options parameters in string
     */
    public String getOptionsInString() {

        byte opt=getOptions();
        byte opt1=(byte) ((opt & 0xf0)>>4);
        byte opt2=(byte) (opt & 0x0f);
        return "# of Opt 1 : "+String.format("%s%04x", "0x",opt1 )+"      # of Opt 2 : "+String.format("%s%04x", "0x",opt2 ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$



    }

    /**
     * @return the serviceID
     */
    public int getServiceID() {
        fServiceID=ConversionHelper.unsignedShortToInt(fpacket.getShort());
        //fServiceID=0;
        return fServiceID;
    }

    /**
     * @return the instanceID
     */
    public int getInstanceID() {
        fInstanceID=ConversionHelper.unsignedShortToInt(fpacket.getShort());
        //fInstanceID=0;
        return fInstanceID;
    }

    /**
     * @return the majorVersion
     */
    public byte getMajorVersion() {
        fMajorVersion=fpacket.get();
        //fMajorVersion=0;
        return fMajorVersion;
    }

    /**
     * @return the tTL
     */
    public int getTTL() {
        int temp_store = fpacket.get();
        fTTL=ConversionHelper.unsignedShortToInt(fpacket.getShort());
        fTTL=(((temp_store << 8) & 0b111100000000) & fTTL);
        //fTTL=0;
        return fTTL;
    }

    /**
     * @return the minorVersion
     */
    public long getMinorVersion() {
        //fMinorVersion=ConversionHelper.unsignedIntToLong(fpacket.getInt());
        fMinorVersion=0;
        return fMinorVersion;
    }

    /**
     * Getter method that returns the Protocol Version Field of a SOME-IP Frame.
     *
     * @return The Protocol Version Field.
     */
    public long getLengthOfOptionsArray() {
        //fLengthOfOptionsArray = ConversionHelper.unsignedIntToLong(fpacket.getInt());
        fLengthOfOptionsArray = 0x00;
        return fLengthOfOptionsArray;
    }

    /**
     * @return the len
     */
    public int getCount() {

        return fCount;
    }

    /**
     * Getter method that returns the Interface Version Field of a SOME-IP Frame.
     *
     * @return The Interface Version Field.
     */

    @Override
    public boolean validate() {
        // Not yet implemented. ATM, we consider that all packets are valid.
        // This is the case for all packets.
        // TODO Implement it.
        return true;
    }

    @Override
    public SOMEIP_SDEndpoint getSourceEndpoint() {
        @Nullable
        SOMEIP_SDEndpoint endpoint = fSourceEndpoint;
        if (endpoint == null) {
            endpoint = new SOMEIP_SDEndpoint(this, true);
        }
        fSourceEndpoint = endpoint;
        return fSourceEndpoint;
    }

    @Override
    public SOMEIP_SDEndpoint getDestinationEndpoint() {
        @Nullable SOMEIP_SDEndpoint endpoint = fDestinationEndpoint;
        if (endpoint == null) {
            endpoint = new SOMEIP_SDEndpoint(this, false);
        }
        fDestinationEndpoint = endpoint;
        return fDestinationEndpoint;
    }

    @Override
    public Map<String, String> getFields() {
        Map<String, String> map = fFields;
        if (map == null) {
            ImmutableMap.Builder<String, String> builder = ImmutableMap.<@NonNull String, @NonNull String> builder();
                for(int i=1;i<=fCount;i++) {
                     builder.put("--- Start of Array "+i+" ---",""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                     builder.put("SD Entry "+i+ " Type :", getTypeInString()); //$NON-NLS-1$ //$NON-NLS-2$
                     builder.put("SD Entry "+i+ " Index 1st Options :",String.format("%s%04x", "0x",getIndex1() )); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                     builder.put("SD Entry "+i+ " Index 2nd Options :", String.format("%s%04x", "0x",getIndex2() )); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                     builder.put("SD Entry "+i+ " Options :",getOptionsInString()); //$NON-NLS-1$ //$NON-NLS-2$
                     builder.put("SD Entry "+i+ " Service ID :",String.format("%s%04x", "0x", getServiceID())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                     builder.put("SD Entry "+i+ " Instance ID :",String.format("%s%04x", "0x", getInstanceID())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                     builder.put("SD Entry "+i+ " Major Version :", String.format("%s%04x", "0x", getMajorVersion())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                     builder.put("SD Entry "+i+ " TTL :", String.format("%s%04x", "0x", getTTL())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                     builder.put("SD Entry "+i+ " Minor Version :", String.format("%s%04x", "0x", getMinorVersion())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                     builder.put("--- End of Array "+i+" ---",""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }

                builder.put("Length of Options Array :", String.format("%s%04x", "0x", getLengthOfOptionsArray()));//$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                builder.put("Options Array Raw Data", String.format("%s%04x", "0x", fPayload));  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
            fFields = builder.build();
            return fFields;
        }
        return map;
    }

    {

    }
     @Override
    public String getLocalSummaryString() {
        return "Length of Enteries Array: " + fCount +" SD-Entry packet(s)"+" Length of Options Array :"+ fLengthOfOptionsArray/16 +" Options array packet(s)"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    @Override
    protected String getSignificationString() {
        return "Length of Enteries Array: " + fCount +" SD-Entry packet(s)"+" Length of Options Array :"+ fLengthOfOptionsArray/16 +" Options array packet(s)"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
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
        SOMEIP_SDPacket other = (SOMEIP_SDPacket) obj;
        if (fLengthOfOptionsArray != other.fLengthOfOptionsArray) {
            return false;
        }
        if(!Objects.equals(fChildPacket, other.fChildPacket)){
            return false;
        }
        if (fType != other.fType) {
            return false;
        }
        if(!Objects.equals(fPayload, other.fPayload)){
            return false;
        }
        if ( fIndex1 != other.fIndex1 ) {
            return false;
        }
        if (fIndex2 != other.fIndex2) {
            return false;
        }
        return true;
    }
}
