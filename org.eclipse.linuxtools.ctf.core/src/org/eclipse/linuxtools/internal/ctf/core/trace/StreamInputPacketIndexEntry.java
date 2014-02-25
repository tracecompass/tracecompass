/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.ctf.core.trace;

import java.util.HashMap;
import java.util.Map;

/**
 * <b><u>StreamInputPacketIndexEntry</u></b>
 * <p>
 * Represents an entry in the index of event packets.
 */
public class StreamInputPacketIndexEntry {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Offset of the packet in the file, in bytes
     */
    final private long fOffsetBytes;

    /**
     * Offset of the data in the packet, in bits
     */
    private long fDataOffsetBits = 0;

    /**
     * Packet size, in bits
     */
    private long fPacketSizeBits = 0;

    /**
     * Content size, in bits
     */
    private long fContentSizeBits = 0;

    /**
     * Begin timestamp
     */
    private long fTimestampBegin = 0;

    /**
     * End timestamp
     */
    private long fTimestampEnd = 0;

    /**
     * How many lost events are there?
     */
    private long fLostEvents = 0;

    /**
     * Which target is being traced
     */
    private String fTarget ;
    private long fTargetID;

    /**
     * Attributes of this index entry
     */
    private final Map<String, Object> fAttributes = new HashMap<>();


    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs an index entry.
     *
     * @param offset
     *            The offset of the packet in the file, in bytes.
     */

    public StreamInputPacketIndexEntry(long offset) {
        fOffsetBytes = offset;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Returns whether the packet includes (inclusively) the given timestamp in
     * the begin-end timestamp range.
     *
     * @param ts
     *            The timestamp to check.
     * @return True if the packet includes the timestamp.
     */
    boolean includes(long ts) {
        return (ts >= fTimestampBegin) && (ts <= fTimestampEnd);
    }

    @Override
    public String toString() {
        return "StreamInputPacketIndexEntry [offsetBytes=" + fOffsetBytes //$NON-NLS-1$
                + ", timestampBegin=" + fTimestampBegin + ", timestampEnd=" //$NON-NLS-1$ //$NON-NLS-2$
                + fTimestampEnd + "]"; //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Getters and Setters
    // ------------------------------------------------------------------------

    /**
     * @return the offsetBytes
     */
    public long getOffsetBytes() {
        return fOffsetBytes;
    }

    /**
     * @return the dataOffsetBits
     */
    public long getDataOffsetBits() {
        return fDataOffsetBits;
    }

    /**
     * @param dataOffsetBits
     *            the dataOffsetBits to set
     */
    public void setDataOffsetBits(long dataOffsetBits) {
        fDataOffsetBits = dataOffsetBits;
    }

    /**
     * @return the packetSizeBits
     */
    public long getPacketSizeBits() {
        return fPacketSizeBits;
    }

    /**
     * @param packetSizeBits
     *            the packetSizeBits to set
     */
    public void setPacketSizeBits(long packetSizeBits) {
        fPacketSizeBits = packetSizeBits;
    }

    /**
     * @return the contentSizeBits
     */
    public long getContentSizeBits() {
        return fContentSizeBits;
    }

    /**
     * @param contentSizeBits
     *            the contentSizeBits to set
     */
    public void setContentSizeBits(long contentSizeBits) {
        fContentSizeBits = contentSizeBits;
    }

    /**
     * @return the timestampBegin
     */
    public long getTimestampBegin() {
        return fTimestampBegin;
    }

    /**
     * @param timestampBegin
     *            the timestampBegin to set
     */
    public void setTimestampBegin(long timestampBegin) {
        fTimestampBegin = timestampBegin;
    }

    /**
     * @return the timestampEnd
     */
    public long getTimestampEnd() {
        return fTimestampEnd;
    }

    /**
     * @param timestampEnd
     *            the timestampEnd to set
     */
    public void setTimestampEnd(long timestampEnd) {
        fTimestampEnd = timestampEnd;
    }

    /**
     * @return the lostEvents in this packet
     */
    public long getLostEvents() {
        return fLostEvents;
    }

    /**
     * @param lostEvents the lostEvents to set
     */
    public void setLostEvents(long lostEvents) {
        fLostEvents = lostEvents;
    }

    /**
     * Add an attribute to this index entry
     *
     * @param field
     *            The name of the attribute
     * @param value
     *            The value to insert
     */
    public void addAttribute(String field, Object value) {
        fAttributes.put(field, value);
    }

    /**
     * Retrieve the value of an existing attribute
     *
     * @param field
     *            The name of the attribute
     * @return The value that was stored, or null if it wasn't found
     */
    public Object lookupAttribute(String field){
        return fAttributes.get(field);
    }

    /**
     * @return The target that is being traced
     */
    public String getTarget() {
        return fTarget;
    }

    /**
     * Assign a target to this index entry
     *
     * @param target
     *            The target to assign
     */
    public void setTarget(String target) {
        fTarget = target;
        fTargetID = Integer.parseInt(target.replaceAll("[\\D]", "")); //$NON-NLS-1$ //$NON-NLS-2$ // slow
    }

    /**
     * @return The ID of the target
     */
    public long getTargetId(){
        return fTargetID;
    }
}
