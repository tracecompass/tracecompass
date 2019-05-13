/*******************************************************************************
 * Copyright (c) 2012, 2019 Ericsson, École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexandre Montplaisir - Initial API and implementation
 *    Florian Wininger - Allow to change the size of a interval
 *    Patrick Tasse - Add message to exceptions
 *******************************************************************************/

package org.eclipse.tracecompass.internal.statesystem.core.backend.historytree;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.datastore.core.serialization.ISafeByteBufferReader;
import org.eclipse.tracecompass.datastore.core.serialization.ISafeByteBufferWriter;
import org.eclipse.tracecompass.datastore.core.serialization.SafeByteBufferFactory;
import org.eclipse.tracecompass.internal.provisional.statesystem.core.statevalue.CustomStateValue;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;

/**
 * The interval component, which will be contained in a node of the History
 * Tree.
 *
 * @author Alexandre Montplaisir
 */
public final class HTInterval implements ITmfStateInterval {

    private static final Charset CHARSET = Charset.forName("UTF-8"); //$NON-NLS-1$

    private static final String errMsg = "Invalid interval data. Maybe your file is corrupt?"; //$NON-NLS-1$

    /* 'Byte' equivalent for state values types */
    private static final byte TYPE_NULL = -1;
    private static final byte TYPE_INTEGER = 0;
    private static final byte TYPE_STRING = 1;
    private static final byte TYPE_LONG = 2;
    private static final byte TYPE_DOUBLE = 3;
    private static final byte TYPE_CUSTOM = 20;

    private final long fStart;
    private final long fDuration;
    private final int fAttribute;
    private final @Nullable Object fStateValue;

    /** Number of bytes used by this interval when it is written to disk */
    private final int fSizeOnDisk;

    /**
     * Standard constructor
     *
     * @param intervalStart
     *            Start time of the interval
     * @param intervalEnd
     *            End time of the interval
     * @param attribute
     *            Attribute (quark) to which the state represented by this
     *            interval belongs
     * @param value
     *            State value represented by this interval
     * @throws TimeRangeException
     *             If the start time or end time are invalid
     */
    public HTInterval(long intervalStart, long intervalEnd, int attribute,
            Object value) throws TimeRangeException {
        if (intervalStart > intervalEnd) {
            throw new TimeRangeException("Start:" + intervalStart + ", End:" + intervalEnd); //$NON-NLS-1$ //$NON-NLS-2$
        }

        fStart = intervalStart;
        fDuration = intervalEnd - intervalStart;
        fAttribute = attribute;
        fStateValue = (value instanceof TmfStateValue) ? ((ITmfStateValue) value).unboxValue() : value;
        fSizeOnDisk = computeSizeOnDisk(fStateValue);
    }

    /**
     * Compute how much space (in bytes) an interval will take in its serialized
     * form on disk. This is dependent on its state value.
     */
    private int computeSizeOnDisk(Object stateValue) {
        /*
         * Minimum size is 1x long (start), a value determined by HTVarInt (duration), 1x int (attribute) and 1x
         * byte (value type).
         */
        int minSize = Long.BYTES + HTVarInt.getEncodedLengthLong(fDuration) + Integer.BYTES + Byte.BYTES;

        if (stateValue == null) {
            return minSize;
        } else if (stateValue instanceof Integer) {
            return (minSize + Integer.BYTES);
        } else if (stateValue instanceof Long) {
            return (minSize + Long.BYTES);
        } else if (stateValue instanceof Double) {
            return (minSize + Double.BYTES);
        } else if (stateValue instanceof String) {
            String str = (String) stateValue;
            int strLength = str.getBytes(CHARSET).length;

            if (strLength > Short.MAX_VALUE) {
                throw new IllegalArgumentException("String is too long to be stored in state system: " + str); //$NON-NLS-1$
            }

            /*
             * String's length + 3 (2 bytes for size, 1 byte for \0 at the end)
             */
            return (minSize + strLength + 3);
        } else if (stateValue instanceof CustomStateValue) {
            /* Length of serialized value (short) + state value */
            return (minSize + Short.BYTES + ((CustomStateValue) stateValue).getSerializedSize());
        }
        /*
         * It's very important that we know how to write the state value in the
         * file!!
         */
        throw new IllegalStateException();
    }

    /**
     * "Faster" constructor for inner use only. When we build an interval when
     * reading it from disk (with {@link #readFrom}), we already know the size
     * of the strings entry, so there is no need to call
     * {@link #computeStringsEntrySize()} and do an extra copy.
     */
    private HTInterval(long intervalStart, long intervalEnd, int attribute,
            Object value, int size) throws TimeRangeException {
        if (intervalStart > intervalEnd) {
            throw new TimeRangeException("Start:" + intervalStart + ", End:" + intervalEnd); //$NON-NLS-1$ //$NON-NLS-2$
        }

        fStart = intervalStart;
        fDuration = intervalEnd - intervalStart;
        fAttribute = attribute;
        fStateValue = value;
        fSizeOnDisk = size;
    }

    /**
     * Reader factory method. Builds the interval using an already-allocated
     * ByteBuffer, which normally comes from a NIO FileChannel.
     *
     * The interval is just a start, end, attribute and value, this is the
     * layout of the HTInterval on disk
     * <ul>
     * <li>start (8 bytes)</li>
     * <li>end (8 bytes)</li>
     * <li>attribute (4 bytes)</li>
     * <li>sv type (1 byte)</li>
     * <li>sv ( 0 bytes for null, 4 for int , 8 for long and double, and the
     * length of the string +2 for strings (it's variable))</li>
     * </ul>
     *
     * @param buffer
     *            The ByteBuffer from which to read the information
     * @return The interval object
     * @throws IOException
     *             If there was an error reading from the buffer
     */
    public static final HTInterval readFrom(ByteBuffer buffer) throws IOException {
        Object value;

        int posStart = buffer.position();
        /* Read the Data Section entry */
        long intervalStart = buffer.getLong();
        long intervalEnd = HTVarInt.readLong(buffer) + intervalStart;
        int attribute = buffer.getInt();

        /* Read the 'type' of the value, then react accordingly */
        byte valueType = buffer.get();
        switch (valueType) {

        case TYPE_NULL:
            value = null;
            break;

        case TYPE_INTEGER:
            value = buffer.getInt();
            break;

        case TYPE_STRING: {
            /* the first short = the size to read */
            int valueSize = buffer.getShort();

            byte[] array = new byte[valueSize];
            buffer.get(array);
            value = new String(array, CHARSET);

            /* Confirm the 0'ed byte at the end */
            byte res = buffer.get();
            if (res != 0) {
                throw new IOException(errMsg);
            }
            break;
        }

        case TYPE_LONG:
            /* Go read the matching entry in the Strings section of the block */
            value = buffer.getLong();
            break;

        case TYPE_DOUBLE:
            /* Go read the matching entry in the Strings section of the block */
            value = buffer.getDouble();
            break;

        case TYPE_CUSTOM: {
            short valueSize = buffer.getShort();
            ISafeByteBufferReader safeBuffer = SafeByteBufferFactory.wrapReader(buffer, valueSize);
            value = CustomStateValue.readSerializedValue(safeBuffer);
            break;
        }
        default:
            /* Unknown data, better to not make anything up... */
            throw new IOException(errMsg);
        }

        try {
            return new HTInterval(intervalStart, intervalEnd, attribute, value, buffer.position() - posStart);
        } catch (TimeRangeException e) {
            throw new IOException(errMsg);
        }
    }

    /**
     * Antagonist of the previous constructor, write the Data entry
     * corresponding to this interval in a ByteBuffer (mapped to a block in the
     * history-file, hopefully)
     *
     * The interval is just a start, end, attribute and value, this is the
     * layout of the HTInterval on disk
     * <ul>
     * <li>start (8 bytes)</li>
     * <li>end (8 bytes)</li>
     * <li>attribute (4 bytes)</li>
     * <li>sv type (1 byte)</li>
     * <li>sv ( 0 bytes for null, 4 for int , 8 for long and double, and the
     * length of the string +2 for strings (it's variable))</li>
     * </ul>
     *
     * @param buffer
     *            The already-allocated ByteBuffer corresponding to a SHT Node
     */
    public void writeInterval(ByteBuffer buffer) {
        buffer.putLong(fStart);
        HTVarInt.writeLong(buffer, fDuration);
        buffer.putInt(fAttribute);

        if (fStateValue != null) {
            @NonNull Object value = fStateValue;
            if (value instanceof Integer) {
                buffer.put(TYPE_INTEGER);
                buffer.putInt((int) value);
            } else if (value instanceof Long) {
                buffer.put(TYPE_LONG);
                buffer.putLong((long) value);
            } else if (value instanceof Double) {
                buffer.put(TYPE_DOUBLE);
                buffer.putDouble((double) value);
            } else if (value instanceof String) {
                buffer.put(TYPE_STRING);
                String string = (String) value;
                byte[] strArray = string.getBytes(CHARSET);

                /*
                 * Write the Strings entry (1st byte = size, then the bytes, then the 0). We
                 * have checked the string length at the constructor.
                 */
                buffer.putShort((short) strArray.length);
                buffer.put(strArray);
                buffer.put((byte) 0);
            } else if (value instanceof CustomStateValue) {
                buffer.put(TYPE_CUSTOM);
                int size = ((CustomStateValue) value).getSerializedSize();
                buffer.putShort((short) size);
                ISafeByteBufferWriter safeBuffer = SafeByteBufferFactory.wrapWriter(buffer, size);
                ((CustomStateValue) value).serialize(safeBuffer);
            } else {
                throw new IllegalStateException("Type: " + value.getClass() + " is not implemented in the state system"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else {
            buffer.put(TYPE_NULL);
        }
    }

    @Override
    public long getStartTime() {
        return fStart;
    }

    @Override
    public long getEndTime() {
        return fStart + fDuration;
    }

    @Override
    public int getAttribute() {
        return fAttribute;
    }

    @Override
    public ITmfStateValue getStateValue() {
        return TmfStateValue.newValue(fStateValue);
    }

    @Override
    public Object getValue() {
        return fStateValue;
    }

    @Override
    public boolean intersects(long timestamp) {
        return (fStart <= timestamp && (fStart + fDuration) >= timestamp);
    }

    /**
     * Total serialized size of this interval
     *
     * @return The interval size
     */
    public int getSizeOnDisk() {
        return fSizeOnDisk;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HTInterval other = (HTInterval) obj;
        return (fStart == other.fStart &&
                fDuration == other.fDuration &&
                fAttribute == other.fAttribute &&
                Objects.equals(fStateValue, other.fStateValue));
    }

    @Override
    public int hashCode() {
        return Objects.hash(fStart, fDuration, fAttribute, fStateValue);
    }

    @Override
    public String toString() {
        /* Only for debug, should not be externalized */
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(fStart);
        sb.append(", "); //$NON-NLS-1$
        sb.append(fStart + fDuration);
        sb.append(']');

        sb.append(", attribute = "); //$NON-NLS-1$
        sb.append(fAttribute);

        sb.append(", value = "); //$NON-NLS-1$
        sb.append(String.valueOf(fStateValue));

        return sb.toString();
    }
}
