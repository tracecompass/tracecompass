/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.historytree;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;

/**
 * The interval component, which will be contained in a node of the History
 * Tree.
 *
 * @author alexmont
 *
 */
final class HTInterval implements ITmfStateInterval, Comparable<HTInterval> {

    private static final String errMsg = "Invalid interval data. Maybe your file is corrupt?"; //$NON-NLS-1$

    /* 'Byte' equivalent for state values types */
    private static final byte TYPE_NULL = -1;
    private static final byte TYPE_INTEGER = 0;
    private static final byte TYPE_STRING = 1;
    private static final byte TYPE_LONG = 2;
    private static final byte TYPE_DOUBLE = 3;

    /* String entry sizes of different state values */
    private static final int NO_ENTRY_SIZE = 0;
    private static final int LONG_ENTRY_SIZE = 8;
    private static final int DOUBLE_ENTRY_SIZE = 8;
    // sizes of string values depend on the string itself

    private final long start;
    private final long end;
    private final int attribute;
    private final TmfStateValue sv;

    /*
     * Size of the strings section entry used by this interval (= 0 if not used)
     */
    private final int stringsEntrySize;

    /**
     * Standard constructor
     *
     * @param intervalStart
     * @param intervalEnd
     * @param attribute
     * @param value
     * @throws TimeRangeException
     */
    HTInterval(long intervalStart, long intervalEnd, int attribute,
            TmfStateValue value) throws TimeRangeException {
        if (intervalStart > intervalEnd) {
            throw new TimeRangeException();
        }

        this.start = intervalStart;
        this.end = intervalEnd;
        this.attribute = attribute;
        this.sv = value;
        this.stringsEntrySize = computeStringsEntrySize();
    }

    /**
     * "Faster" constructor for inner use only. When we build an interval when
     * reading it from disk (with {@link #readFrom}), we already know the size
     * of the strings entry, so there is no need to call
     * {@link #computeStringsEntrySize()} and do an extra copy.
     *
     * @param intervalStart
     * @param intervalEnd
     * @param attribute
     * @param value
     * @param size
     * @throws TimeRangeException
     */
    private HTInterval(long intervalStart, long intervalEnd, int attribute,
            TmfStateValue value, int size) throws TimeRangeException {
        if (intervalStart > intervalEnd) {
            throw new TimeRangeException();
        }

        this.start = intervalStart;
        this.end = intervalEnd;
        this.attribute = attribute;
        this.sv = value;
        this.stringsEntrySize = size;
    }

    /**
     * Reader constructor. Builds the interval using an already-allocated
     * ByteBuffer, which normally comes from a NIO FileChannel.
     *
     * @param buffer
     *            The ByteBuffer from which to read the information
     * @throws IOException
     */
    final static HTInterval readFrom(ByteBuffer buffer) throws IOException {
        HTInterval interval;
        long intervalStart, intervalEnd;
        int attribute;
        TmfStateValue value;
        int valueOrOffset, valueSize, res;
        byte valueType;
        byte array[];

        /* Read the Data Section entry */
        intervalStart = buffer.getLong();
        intervalEnd = buffer.getLong();
        attribute = buffer.getInt();

        /* Read the 'type' of the value, then react accordingly */
        valueType = buffer.get();
        valueOrOffset = buffer.getInt();
        switch (valueType) {

        case TYPE_NULL:
            value = TmfStateValue.nullValue();
            valueSize = NO_ENTRY_SIZE;
            break;

        case TYPE_INTEGER:
            /* "ValueOrOffset" is the straight value */
            value = TmfStateValue.newValueInt(valueOrOffset);
            valueSize = NO_ENTRY_SIZE;
            break;

        case TYPE_STRING:
            /* Go read the matching entry in the Strings section of the block */
            buffer.mark();
            buffer.position(valueOrOffset);

            /* the first byte = the size to read */
            valueSize = buffer.get();

            /*
             * Careful though, 'valueSize' is the total size of the entry,
             * including the 'size' byte at the start and end (0'ed) byte at the
             * end. Here we want 'array' to only contain the real payload of the
             * value.
             */
            array = new byte[valueSize - 2];
            buffer.get(array);
            value = TmfStateValue.newValueString(new String(array));

            /* Confirm the 0'ed byte at the end */
            res = buffer.get();
            if (res != 0) {
                throw new IOException(errMsg);
            }

            /*
             * Restore the file pointer's position (so we can read the next
             * interval)
             */
            buffer.reset();
            break;

        case TYPE_LONG:
            /* Go read the matching entry in the Strings section of the block */
            buffer.mark();
            buffer.position(valueOrOffset);
            value = TmfStateValue.newValueLong(buffer.getLong());
            valueSize = LONG_ENTRY_SIZE;

            /*
             * Restore the file pointer's position (so we can read the next
             * interval)
             */
            buffer.reset();
            break;

        case TYPE_DOUBLE:
            /* Go read the matching entry in the Strings section of the block */
            buffer.mark();
            buffer.position(valueOrOffset);
            value = TmfStateValue.newValueDouble(buffer.getDouble());
            valueSize = DOUBLE_ENTRY_SIZE;

            /*
             * Restore the file pointer's position (so we can read the next
             * interval)
             */
            buffer.reset();
            break;

        default:
            /* Unknown data, better to not make anything up... */
            throw new IOException(errMsg);
        }

        try {
            interval = new HTInterval(intervalStart, intervalEnd, attribute, value, valueSize);
        } catch (TimeRangeException e) {
            throw new IOException(errMsg);
        }
        return interval;
    }

    /**
     * Antagonist of the previous constructor, write the Data entry
     * corresponding to this interval in a ByteBuffer (mapped to a block in the
     * history-file, hopefully)
     *
     * @param buffer
     *            The already-allocated ByteBuffer corresponding to a SHT Node
     * @param endPosOfStringEntry
     *            The initial (before calling this function for this interval)
     *            position of the Strings Entry for this node. This will change
     *            from one call to the other if we're writing String
     *            StateValues.
     * @return The size of the Strings Entry that was written, if any.
     */
    int writeInterval(ByteBuffer buffer, int endPosOfStringEntry) {
        buffer.putLong(start);
        buffer.putLong(end);
        buffer.putInt(attribute);
        buffer.put(getByteFromType(sv.getType()));

        switch (getByteFromType(sv.getType())) {

        case TYPE_NULL:
        case TYPE_INTEGER:
            /* We write the 'valueOffset' field as a straight value. In the case
             * of a null value, it will be unboxed as -1 */
            try {
                buffer.putInt(sv.unboxInt());
            } catch (StateValueTypeException e) {
                /*
                 * This should not happen, since the value told us it was of
                 * type Null or Integer (corrupted value?)
                 */
                e.printStackTrace();
            }
            break;

        case TYPE_STRING:
            byte[] byteArrayToWrite;
            try {
                byteArrayToWrite = sv.unboxStr().getBytes();
            } catch (StateValueTypeException e1) {
                /* Should not happen, we're in a switch/case for string type */
                throw new RuntimeException();
            }

            /* we use the valueOffset as an offset. */
            buffer.putInt(endPosOfStringEntry - stringsEntrySize);
            buffer.mark();
            buffer.position(endPosOfStringEntry - stringsEntrySize);

            /*
             * write the Strings entry (1st byte = size, then the bytes, then the 0)
             */
            buffer.put((byte) stringsEntrySize);
            buffer.put(byteArrayToWrite);
            buffer.put((byte) 0);
            assert (buffer.position() == endPosOfStringEntry);
            buffer.reset();
            break;

        case TYPE_LONG:
            /* we use the valueOffset as an offset. */
            buffer.putInt(endPosOfStringEntry - stringsEntrySize);
            buffer.mark();
            buffer.position(endPosOfStringEntry - stringsEntrySize);

            /*
             * write the Long in the Strings section
             */
            try {
                buffer.putLong(sv.unboxLong());
            } catch (StateValueTypeException e) {
                /*
                 * This should not happen, since the value told us it was of
                 * type Long (corrupted value?)
                 */
                e.printStackTrace();
            }
            assert (buffer.position() == endPosOfStringEntry);
            buffer.reset();
            break;

        case TYPE_DOUBLE:
            /* we use the valueOffset as an offset. */
            buffer.putInt(endPosOfStringEntry - stringsEntrySize);
            buffer.mark();
            buffer.position(endPosOfStringEntry - stringsEntrySize);

            /* Write the Double in the Strings section */
            try {
                buffer.putDouble(sv.unboxDouble());
            } catch (StateValueTypeException e) {
                /*
                 * This should not happen, since the value told us it was of
                 * type Double (corrupted value?)
                 */
                e.printStackTrace();
            }
            if (buffer.position() != endPosOfStringEntry) {
                throw new IllegalStateException();
            }
            buffer.reset();
            break;

        default:
            break;
        }
        return stringsEntrySize;
    }

    @Override
    public long getStartTime() {
        return start;
    }

    @Override
    public long getEndTime() {
        return end;
    }

    @Override
    public long getViewerEndTime() {
        return end + 1;
    }

    @Override
    public int getAttribute() {
        return attribute;
    }

    @Override
    public ITmfStateValue getStateValue() {
        return sv;
    }

    @Override
    public boolean intersects(long timestamp) {
        if (start <= timestamp) {
            if (end >= timestamp) {
                return true;
            }
        }
        return false;
    }

    int getStringsEntrySize() {
        return stringsEntrySize;
    }

    /**
     * Total serialized size of this interval
     *
     * @return
     */
    int getIntervalSize() {
        return stringsEntrySize + HTNode.DATA_ENTRY_SIZE;
    }

    private int computeStringsEntrySize() {
        switch(sv.getType()) {
        case NULL:
        case INTEGER:
            /* Those don't use the strings section at all */
            return NO_ENTRY_SIZE;
        case LONG:
            /* The value's bytes are written directly into the strings section */
            return LONG_ENTRY_SIZE;
        case DOUBLE:
            /* The value is also written directly into the strings section */
            return DOUBLE_ENTRY_SIZE;
        case STRING:
            try {
                /* String's length + 2 (1 byte for size, 1 byte for \0 at the end */
                return sv.unboxStr().getBytes().length + 2;
            } catch (StateValueTypeException e) {
                /* We're inside a switch/case for the string type, can't happen */
                throw new IllegalStateException(e);
            }
        default:
            /* It's very important that we know how to write the state value in
             * the file!! */
            throw new IllegalStateException();
        }
    }

    /**
     * Compare the END TIMES of different intervals. This is used to sort the
     * intervals when we close down a node.
     */
    @Override
    public int compareTo(HTInterval other) {
        if (this.end < other.end) {
            return -1;
        } else if (this.end > other.end) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof HTInterval &&
                this.compareTo((HTInterval) other) == 0) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        /* Only for debug, should not be externalized */
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(start);
        sb.append(", "); //$NON-NLS-1$
        sb.append(end);
        sb.append(']');

        sb.append(", attribute = "); //$NON-NLS-1$
        sb.append(attribute);

        sb.append(", value = "); //$NON-NLS-1$
        sb.append(sv.toString());

        return sb.toString();
    }

    /**
     * Here we determine how state values "types" are written in the 8-bit
     * field that indicates the value type in the file.
     */
    private static byte getByteFromType(ITmfStateValue.Type type) {
        switch(type) {
        case NULL:
            return TYPE_NULL;
        case INTEGER:
            return TYPE_INTEGER;
        case STRING:
            return TYPE_STRING;
        case LONG:
            return TYPE_LONG;
        case DOUBLE:
            return TYPE_DOUBLE;
        default:
            /* Should not happen if the switch is fully covered */
            throw new IllegalStateException();
        }
    }
}
