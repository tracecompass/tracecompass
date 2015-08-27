/*******************************************************************************
 * Copyright (c) 2011, 2015 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.trace;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.tracecompass.ctf.core.CTFStrings;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.FloatDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.SimpleDatatypeDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StringDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;
import org.eclipse.tracecompass.ctf.core.trace.ICTFPacketDescriptor;

/**
 * <b><u>StreamInputPacketIndexEntry</u></b>
 * <p>
 * Represents an entry in the index of event packets.
 */
public class StreamInputPacketIndexEntry implements ICTFPacketDescriptor {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\D*(\\d+)"); //$NON-NLS-1$

    private static final int UNKNOWN = -1;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Position of the start of the packet header in the file, in bits
     */
    private final long fOffsetBits;

    /**
     * Position of the start of the packet header in the file, in bytes
     */
    private final long fOffsetBytes;

    /**
     * Packet size, in bits
     */
    private final long fPacketSizeBits;

    /**
     * Content size, in bits
     */
    private final long fContentSizeBits;

    /**
     * Begin timestamp
     */
    private final long fTimestampBegin;

    /**
     * End timestamp
     */
    private final long fTimestampEnd;

    /**
     * How many lost events are there?
     */
    private final long fLostEvents;

    /**
     * Which target is being traced
     */
    private final String fTarget;
    private final long fTargetID;

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
     * @param dataOffsetBits
     *            offset in the file for the start of data in bits
     * @param fileSizeBytes
     *            number of bytes in a file
     */

    public StreamInputPacketIndexEntry(long dataOffsetBits, long fileSizeBytes) {
        fContentSizeBits = (fileSizeBytes * Byte.SIZE);
        fPacketSizeBits = (fileSizeBytes * Byte.SIZE);
        fOffsetBits = dataOffsetBits;
        fOffsetBytes = dataOffsetBits / Byte.SIZE;
        fLostEvents = 0;
        fTarget = ""; //$NON-NLS-1$
        fTargetID = 0;
        fTimestampBegin = 0;
        fTimestampEnd = Long.MAX_VALUE;
    }

    /**
     * full Constructor
     *
     * @param dataOffsetBits
     *            offset in the file for the start of data in bits
     * @param streamPacketContextDef
     *            packet context
     * @param fileSizeBytes
     *            number of bytes in a file
     * @param lostSoFar
     *            number of lost events so far
     */
    public StreamInputPacketIndexEntry(long dataOffsetBits, StructDefinition streamPacketContextDef, long fileSizeBytes, long lostSoFar) {
        for (String field : streamPacketContextDef.getDeclaration().getFieldsList()) {
            IDefinition id = streamPacketContextDef.lookupDefinition(field);
            if (id instanceof IntegerDefinition) {
                fAttributes.put(field, ((IntegerDefinition) id).getValue());
            } else if (id instanceof FloatDefinition) {
                fAttributes.put(field, ((FloatDefinition) id).getValue());
            } else if (id instanceof EnumDefinition) {
                fAttributes.put(field, ((EnumDefinition) id).getValue());
            } else if (id instanceof StringDefinition) {
                fAttributes.put(field, ((StringDefinition) id).getValue());
            }
        }

        Long contentSize = (Long) fAttributes.get(CTFStrings.CONTENT_SIZE);
        Long packetSize = (Long) fAttributes.get(CTFStrings.PACKET_SIZE);
        Long tsBegin = (Long) fAttributes.get(CTFStrings.TIMESTAMP_BEGIN);
        Long tsEnd = (Long) fAttributes.get(CTFStrings.TIMESTAMP_END);
        boolean hasDevice = fAttributes.containsKey(CTFStrings.DEVICE);
        // LTTng Specific
        Long cpuId = (Long) fAttributes.get(CTFStrings.CPU_ID);
        Long lostEvents = (Long) fAttributes.get(CTFStrings.EVENTS_DISCARDED);

        /* Read the content size in bits */
        if (contentSize != null) {
            fContentSizeBits = (contentSize.longValue());
        } else if (packetSize != null) {
            fContentSizeBits = (packetSize.longValue());
        } else {
            fContentSizeBits = (fileSizeBytes * Byte.SIZE);
        }

        /* Read the packet size in bits */
        if (packetSize != null) {
            fPacketSizeBits = (packetSize.longValue());
        } else if (this.getContentSizeBits() != 0) {
            fPacketSizeBits = fContentSizeBits;
        } else {
            fPacketSizeBits = (fileSizeBytes * Byte.SIZE);
        }

        /* Read the begin timestamp */
        if (tsBegin != null) {
            fTimestampBegin = (tsBegin.longValue());
        } else {
            fTimestampBegin = 0;
        }

        /* Read the end timestamp */
        if (tsEnd != null) {
            // check if tsEnd == unsigned long max value
            if (tsEnd == -1) {
                tsEnd = Long.MAX_VALUE;
            }
            fTimestampEnd = (tsEnd.longValue());
        } else {
            fTimestampEnd = Long.MAX_VALUE;
        }

        Target target = lookupTarget(streamPacketContextDef, hasDevice, cpuId);
        fTarget = target.string;
        fTargetID = target.number;

        if (lostEvents != null) {
            fLostEvents = (lostEvents - lostSoFar);
        } else {
            fLostEvents = 0;
        }

        fOffsetBits = dataOffsetBits;
        fOffsetBytes = dataOffsetBits / Byte.SIZE;
    }

    private static class Target {
        public String string;
        public long number;

        public Target() {
            string = null;
            number = UNKNOWN;
        }
    }

    private static Target lookupTarget(StructDefinition streamPacketContextDef, boolean hasDevice, Long cpuId) {
        Target ret = new Target();
        if (hasDevice) {
            IDefinition def = streamPacketContextDef.lookupDefinition(CTFStrings.DEVICE);
            if (def instanceof SimpleDatatypeDefinition) {
                SimpleDatatypeDefinition simpleDefinition = (SimpleDatatypeDefinition) def;
                ret.string = simpleDefinition.getStringValue();
                ret.number = simpleDefinition.getIntegerValue();
            } else if (def instanceof StringDefinition) {
                StringDefinition stringDefinition = (StringDefinition) def;
                ret.string = stringDefinition.getValue();
                final Matcher matcher = NUMBER_PATTERN.matcher(ret.string);
                if (matcher.matches()) {
                    String number = matcher.group(1);
                    ret.number = Integer.parseInt(number);
                }
            }
        } else if (cpuId != null) {
            ret.string = ("CPU" + cpuId.toString()); //$NON-NLS-1$
            ret.number = cpuId;
        }
        return ret;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public boolean includes(long ts) {
        return (ts >= fTimestampBegin) && (ts <= fTimestampEnd);
    }

    @Override
    public String toString() {
        return "StreamInputPacketIndexEntry [offsetBits=" + fOffsetBits //$NON-NLS-1$
                + ", timestampBegin=" + fTimestampBegin + ", timestampEnd=" //$NON-NLS-1$ //$NON-NLS-2$
                + fTimestampEnd + "]"; //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Getters and Setters
    // ------------------------------------------------------------------------

    @Override
    public long getOffsetBits() {
        return fOffsetBits;
    }

    @Override
    public long getPacketSizeBits() {
        return fPacketSizeBits;
    }

    @Override
    public long getContentSizeBits() {
        return fContentSizeBits;
    }

    @Override
    public long getTimestampBegin() {
        return fTimestampBegin;
    }

    @Override
    public long getTimestampEnd() {
        return fTimestampEnd;
    }

    @Override
    public long getLostEvents() {
        return fLostEvents;
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

    @Override
    public Object lookupAttribute(String field) {
        return fAttributes.get(field);
    }

    @Override
    public String getTarget() {
        return fTarget;
    }

    @Override
    public long getTargetId() {
        return fTargetID;
    }

    @Override
    public long getOffsetBytes() {
        return fOffsetBytes;
    }
}
