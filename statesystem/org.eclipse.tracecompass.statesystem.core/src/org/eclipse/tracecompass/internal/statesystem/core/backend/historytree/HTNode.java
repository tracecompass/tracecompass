/*******************************************************************************
 * Copyright (c) 2010, 2019 Ericsson, École Polytechnique de Montréal, and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Florian Wininger - Add Extension and Leaf Node
 *   Patrick Tasse - Keep interval list sorted on insert
 *******************************************************************************/

package org.eclipse.tracecompass.internal.statesystem.core.backend.historytree;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.IntegerRangeCondition;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.TimeRangeCondition;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;

/**
 * The base class for all the types of nodes that go in the History Tree.
 *
 * @author Alexandre Montplaisir
 */
public abstract class HTNode {

    private static final @NonNull Logger LOGGER = TraceCompassLog.getLogger(HTNode.class);

    // ------------------------------------------------------------------------
    // Class fields
    // ------------------------------------------------------------------------

    /**
     * The type of node
     */
    public enum NodeType {
        /**
         * Core node, which is a "front" node, at any level of the tree except
         * the bottom-most one. It has children, and may have extensions.
         */
        CORE,
        /**
         * Leaf node, which is a node at the last bottom level of the tree. It
         * cannot have any children or extensions.
         */
        LEAF;

        /**
         * Determine a node type by reading a serialized byte.
         *
         * @param rep
         *            The byte representation of the node type
         * @return The corresponding NodeType
         * @throws IOException
         *             If the NodeType is unrecognized
         */
        public static NodeType fromByte(byte rep) throws IOException {
            switch (rep) {
            case 1:
                return CORE;
            case 2:
                return LEAF;
            default:
                throw new IOException();
            }
        }

        /**
         * Get the byte representation of this node type. It can then be read
         * with {@link #fromByte}.
         *
         * @return The byte matching this node type
         */
        public byte toByte() {
            switch (this) {
            case CORE:
                return 1;
            case LEAF:
                return 2;
            default:
                throw new IllegalStateException();
            }
        }
    }

    /**
     * <pre>
     *  1 - byte (type)
     * 16 - 2x long (start time, end time)
     * 16 - 3x int (seq number, parent seq number, intervalcount)
     * 16 - 2x int (minimum quark and maximum quark)
     * </pre>
     */
    private static final int COMMON_HEADER_SIZE = Byte.BYTES
            + 2 * Long.BYTES
            + 3 * Integer.BYTES
            + 2 * Integer.BYTES;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /* Configuration of the History Tree to which belongs this node */
    private final HTConfig fConfig;

    /* Time range of this node */
    private final long fNodeStart;
    private long fNodeEnd;
    private int fMinQuark = Integer.MAX_VALUE;
    private int fMaxQuark = Integer.MIN_VALUE;

    /* Sequence number = position in the node section of the file */
    private final int fSequenceNumber;
    private int fParentSequenceNumber; /* = -1 if this node is the root node */

    /* Sum of bytes of all intervals in the node */
    private int fSizeOfIntervalSection;

    /*
     * True if this node was read from disk (meaning its end time is now fixed)
     */
    private volatile boolean fIsOnDisk;

    /* Vector containing all the intervals contained in this node */
    private final List<HTInterval> fIntervals;

    /* Lock used to protect the accesses to intervals, nodeEnd and such */
    private final ReentrantReadWriteLock fRwl = new ReentrantReadWriteLock(false);

    /**
     * Order of intervals in a HTNode: sorted by end times, then by start times.
     */
    private static final Comparator<ITmfStateInterval> NODE_ORDER = Comparator
            .comparingLong(ITmfStateInterval::getEndTime)
            .thenComparingLong(ITmfStateInterval::getStartTime)
            .thenComparingInt(ITmfStateInterval::getAttribute);

    /**
     * Constructor
     *
     * @param config
     *            Configuration of the History Tree
     * @param seqNumber
     *            The (unique) sequence number assigned to this particular node
     * @param parentSeqNumber
     *            The sequence number of this node's parent node
     * @param start
     *            The earliest timestamp stored in this node
     */
    protected HTNode(HTConfig config, int seqNumber, int parentSeqNumber, long start) {
        fConfig = config;
        fNodeStart = start;
        fNodeEnd = start;
        fSequenceNumber = seqNumber;
        fParentSequenceNumber = parentSeqNumber;

        fSizeOfIntervalSection = 0;
        fIsOnDisk = false;
        fIntervals = new ArrayList<>();
    }

    /**
     * Reader factory method. Build a Node object (of the right type) by reading
     * a block in the file.
     *
     * @param config
     *            Configuration of the History Tree
     * @param fc
     *            FileChannel to the history file, ALREADY SEEKED at the start
     *            of the node.
     * @param nodeFactory
     *            The factory to create the nodes for this tree
     * @return The node object
     * @throws IOException
     *             If there was an error reading from the file channel
     */
    public static final @NonNull HTNode readNode(HTConfig config, FileChannel fc, IHistoryTree.IHTNodeFactory nodeFactory)
            throws IOException {
        HTNode newNode = null;

        ByteBuffer buffer = ByteBuffer.allocate(config.getBlockSize());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.clear();
        int res = fc.read(buffer);
        if (res != config.getBlockSize()) {
            throw new IOException("Expected " + config.getBlockSize() + " block size, but got " + res); //$NON-NLS-1$//$NON-NLS-2$
        }
        buffer.flip();

        /* Read the common header part */
        byte typeByte = buffer.get();
        NodeType type = NodeType.fromByte(typeByte);
        long start = buffer.getLong();
        long end = buffer.getLong();
        int min = buffer.getInt();
        int max = buffer.getInt();
        int seqNb = buffer.getInt();
        int parentSeqNb = buffer.getInt();
        int intervalCount = buffer.getInt();

        /* Now the rest of the header depends on the node type */
        switch (type) {
        case CORE:
            /* Core nodes */
            newNode = nodeFactory.createCoreNode(config, seqNb, parentSeqNb, start);
            newNode.readSpecificHeader(buffer);
            break;

        case LEAF:
            /* Leaf nodes */
            newNode = nodeFactory.createLeafNode(config, seqNb, parentSeqNb, start);
            newNode.readSpecificHeader(buffer);
            break;

        default:
            /* Unrecognized node type */
            throw new IOException();
        }

        /*
         * At this point, we should be done reading the header and 'buffer'
         * should only have the intervals left
         */
        for (int i = 0; i < intervalCount; i++) {
            HTInterval interval = HTInterval.readFrom(buffer, start);
            newNode.fIntervals.add(interval);
            newNode.fSizeOfIntervalSection += interval.getSizeOnDisk();
        }

        /* Assign the node's other information we have read previously */
        newNode.fNodeEnd = end;
        newNode.fMinQuark = min;
        newNode.fMaxQuark = max;
        newNode.fIsOnDisk = true;

        return newNode;
    }

    /**
     * Write this node to the given file channel.
     *
     * @param fc
     *            The file channel to write to (should be sought to be correct
     *            position)
     * @throws IOException
     *             If there was an error writing
     */
    public final void writeSelf(FileChannel fc) throws IOException {
        /*
         * Yes, we are taking the *read* lock here, because we are reading the
         * information in the node to write it to disk.
         */
        fRwl.readLock().lock();
        try {
            final int blockSize = fConfig.getBlockSize();

            ByteBuffer buffer = ByteBuffer.allocate(blockSize);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.clear();

            /* Write the common header part */
            buffer.put(getNodeType().toByte());
            buffer.putLong(fNodeStart);
            buffer.putLong(fNodeEnd);
            buffer.putInt(fMinQuark);
            buffer.putInt(fMaxQuark);
            buffer.putInt(fSequenceNumber);
            buffer.putInt(fParentSequenceNumber);
            buffer.putInt(fIntervals.size());

            /* Now call the inner method to write the specific header part */
            writeSpecificHeader(buffer);

            /* Back to us, we write the intervals */
            for (HTInterval interval : fIntervals) {
                interval.writeInterval(buffer, fNodeStart);
            }
            if (blockSize - buffer.position() != getNodeFreeSpace()) {
                throw new IllegalStateException("Wrong free space: Actual: " + (blockSize - buffer.position()) + ", Expected: " + getNodeFreeSpace()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            /*
             * Fill the rest with zeros
             */
            while (buffer.position() < blockSize) {
                buffer.put((byte) 0);
            }

            /* Finally, write everything in the Buffer to disk */
            buffer.flip();
            int res = fc.write(buffer);
            if (res != blockSize) {
                throw new IllegalStateException("Wrong size of block written: Actual: " + res + ", Expected: " + blockSize); //$NON-NLS-1$ //$NON-NLS-2$
            }

        } finally {
            fRwl.readLock().unlock();
        }
        fIsOnDisk = true;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Retrieve the history tree configuration used for this node.
     *
     * @return The history tree config
     */
    protected HTConfig getConfig() {
        return fConfig;
    }

    /**
     * Get the start time of this node.
     *
     * @return The start time of this node
     */
    public long getNodeStart() {
        return fNodeStart;
    }

    /**
     * Get the end time of this node.
     *
     * @return The end time of this node
     */
    public long getNodeEnd() {
        fRwl.readLock().lock();
        try {
            return fNodeEnd;
        } finally {
            fRwl.readLock().unlock();
        }
    }

    /**
     * Get the sequence number of this node.
     *
     * @return The sequence number of this node
     */
    public int getSequenceNumber() {
        return fSequenceNumber;
    }

    /**
     * Get the sequence number of this node's parent.
     *
     * @return The parent sequence number
     */
    public int getParentSequenceNumber() {
        return fParentSequenceNumber;
    }

    /**
     * Change this node's parent. Used when we create a new root node for
     * example.
     *
     * @param newParent
     *            The sequence number of the node that is the new parent
     */
    public void setParentSequenceNumber(int newParent) {
        fParentSequenceNumber = newParent;
    }

    /**
     * Return if this node is "done" (full and written to disk).
     *
     * @return If this node is done or not
     */
    public boolean isOnDisk() {
        return fIsOnDisk;
    }

    /**
     * Add an interval to this node
     *
     * @param newInterval
     *            Interval to add to this node
     */
    public void addInterval(HTInterval newInterval) {
        fRwl.writeLock().lock();
        try {
            /*
             * Just in case, should be checked before even calling this function
             */
            int newSizeOnDisk = newInterval.getSizeOnDisk(fNodeStart);
            if (newSizeOnDisk > getNodeFreeSpace()) {
                // Could be an IO exception, but that would change the API
                throw new IllegalStateException("Insufficient disk space."); //$NON-NLS-1$
            }

            /* Find the insert position to keep the list sorted */
            int index = 0;
            if (fIntervals.isEmpty()) {
                index = 0;
            } else if (NODE_ORDER.compare(fIntervals.get(fIntervals.size() - 1), newInterval) <= 0) {
                index = fIntervals.size();
            } else {
                index = Collections.binarySearch(fIntervals, newInterval, NODE_ORDER);
                /*
                 * Interval should not already be in the node, binarySearch will
                 * return (-insertionPoint - 1).
                 */
                index = -index - 1;
            }
            newInterval.setSizeOnDisk(newSizeOnDisk);
            fIntervals.add(index, newInterval);
            fNodeEnd = Long.max(fNodeEnd, newInterval.getEndTime());
            fMinQuark = Integer.min(fMinQuark, newInterval.getAttribute());
            fMaxQuark = Integer.max(fMaxQuark, newInterval.getAttribute());
            fSizeOfIntervalSection += newInterval.getSizeOnDisk();

        } finally {
            fRwl.writeLock().unlock();
        }
    }

    /**
     * We've received word from the containerTree that newest nodes now exist to
     * our right. (Puts isDone = true and sets the endtime)
     *
     * @param endtime
     *            The nodeEnd time that the node will have
     */
    public void closeThisNode(long endtime) {
        fRwl.writeLock().lock();
        try {
            /*
             * Make sure there are no intervals in this node with their EndTime
             * greater than (>) the one requested.
             */
            if (fNodeEnd > endtime) {
                throw new IllegalArgumentException("Closing end time should be greater than or equal to the end time of the intervals of this node"); //$NON-NLS-1$
            }

            fNodeEnd = endtime;
        } finally {
            fRwl.writeLock().unlock();
        }
    }

    /**
     * The method to fill up the stateInfo (passed on from the Current State
     * Tree when it does a query on the SHT). We'll replace the data in that
     * vector with whatever relevant we can find from this node
     *
     * @param stateInfo
     *            The same stateInfo that comes from SHT's doQuery()
     * @param t
     *            The timestamp for which the query is for. Only return
     *            intervals that intersect t.
     * @throws TimeRangeException
     *             If 't' is invalid
     */
    public void writeInfoFromNode(List<ITmfStateInterval> stateInfo, long t)
            throws TimeRangeException {
        /* This is from a state system query, we are "reading" this node */
        fRwl.readLock().lock();
        try {
            for (int i = getStartIndexFor(t); i < fIntervals.size(); i++) {
                /*
                 * Now we only have to compare the Start times, since we now the
                 * End times necessarily fit.
                 *
                 * Second condition is to ignore new attributes that might have
                 * been created after stateInfo was instantiated (they would be
                 * null anyway).
                 */
                ITmfStateInterval interval = fIntervals.get(i);
                if (t >= interval.getStartTime() &&
                        interval.getAttribute() < stateInfo.size()) {
                    stateInfo.set(interval.getAttribute(), interval);
                }
            }
        } finally {
            fRwl.readLock().unlock();
        }
    }

    /**
     * Get a single Interval from the information in this node If the
     * key/timestamp pair cannot be found, we return null.
     *
     * @param key
     *            The attribute quark to look for
     * @param t
     *            The timestamp
     * @return The Interval containing the information we want, or null if it
     *         wasn't found
     * @throws TimeRangeException
     *             If 't' is invalid
     */
    public HTInterval getRelevantInterval(int key, long t) throws TimeRangeException {
        fRwl.readLock().lock();
        try (TraceCompassLogUtils.ScopeLog log = new TraceCompassLogUtils.ScopeLog(LOGGER, Level.FINEST, "HTNode:singleQuery", //$NON-NLS-1$
                "time", t, //$NON-NLS-1$
                "attribute", key)) { //$NON-NLS-1$
            for (int i = getStartIndexFor(t); i < fIntervals.size(); i++) {
                HTInterval curInterval = fIntervals.get(i);
                if (curInterval.getAttribute() == key
                        && curInterval.getStartTime() <= t) {
                    return curInterval;
                }
            }

            /* We didn't find the relevant information in this node */
            return null;

        } finally {
            fRwl.readLock().unlock();
        }
    }

    /**
     * 2D query method, returns an iterable over the intervals for the desired
     * quarks and times.
     *
     * @param quarks
     *            NumCondition on the quarks on which we want information
     * @param times
     *            NumCondition on the times on which we want information
     * @return an Iterable over intervals that match conditions.
     */
    public Iterable<@NonNull HTInterval> iterable2D(IntegerRangeCondition quarks, TimeRangeCondition times) {
        fRwl.readLock().lock();
        try (TraceCompassLogUtils.ScopeLog log = new TraceCompassLogUtils.ScopeLog(LOGGER, Level.FINEST, "HTNode:query2D", //$NON-NLS-1$
                "quarks", quarks, //$NON-NLS-1$
                "times", times)) { //$NON-NLS-1$
            List<@NonNull HTInterval> intervals = new ArrayList<>();
            for (HTInterval interval : fIntervals.subList(getStartIndexFor(times.min()), fIntervals.size())) {
                if (quarks.test(interval.getAttribute())
                        && times.intersects(interval.getStartTime(), interval.getEndTime())) {
                    intervals.add(interval);
                }
            }
            return intervals;
        } finally {
            fRwl.readLock().unlock();
        }
    }

    private int getStartIndexFor(long t) throws TimeRangeException {
        /* Should only be called by methods with the readLock taken */

        if (fIntervals.isEmpty()) {
            return 0;
        }

        /*
         * Since the intervals are sorted by end time then by start time, we can
         * skip all the ones at the beginning whose end times are smaller than
         * 't'. We search for a dummy interval from [Long.MIN_VALUE, t], which
         * will return the first interval that ends with a time >= t.
         */
        HTInterval dummy = new HTInterval(Long.MIN_VALUE, t, 0, null);
        int index = Collections.binarySearch(fIntervals, dummy, NODE_ORDER);

        /* Handle negative binarySearch return */
        return (index >= 0 ? index : -index - 1);
    }

    /**
     * Return the total header size of this node (will depend on the node type).
     *
     * @return The total header size
     */
    public final int getTotalHeaderSize() {
        return COMMON_HEADER_SIZE + getSpecificHeaderSize();
    }

    /**
     * @return The offset, within the node, where the Data section ends
     */
    private int getDataSectionEndOffset() {
        return getTotalHeaderSize() + fSizeOfIntervalSection;
    }

    /**
     * Returns the free space in the node, which is simply put, the
     * stringSectionOffset - dataSectionOffset
     *
     * @return The amount of free space in the node (in bytes)
     */
    public int getNodeFreeSpace() {
        fRwl.readLock().lock();
        try {
            return fConfig.getBlockSize() - getDataSectionEndOffset();
        } finally {
            fRwl.readLock().unlock();
        }
    }

    /**
     * Returns the current space utilization of this node, as a percentage.
     * (used space / total usable space, which excludes the header)
     *
     * @return The percentage (value between 0 and 100) of space utilization in
     *         in this node.
     */
    public long getNodeUsagePercent() {
        fRwl.readLock().lock();
        try {
            final int blockSize = fConfig.getBlockSize();
            float freePercent = (float) getNodeFreeSpace()
                    / (float) (blockSize - getTotalHeaderSize())
                    * 100F;
            return (long) (100L - freePercent);

        } finally {
            fRwl.readLock().unlock();
        }
    }

    /**
     * Getter for the minimum quark value for this node
     *
     * @return the minimum quark for the intervals stored in this node
     */
    public int getMinQuark() {
        fRwl.readLock().lock();
        try {
            return fMinQuark;
        } finally {
            fRwl.readLock().unlock();
        }
    }

    /**
     * Getter for the maximum quark value for this node
     *
     * @return the maximum quark for the intervals stored in this node
     */
    public int getMaxQuark() {
        fRwl.readLock().lock();
        try {
            return fMaxQuark;
        } finally {
            fRwl.readLock().unlock();
        }
    }

    /**
     * @name Debugging functions
     */

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        /* Only used for debugging, shouldn't be externalized */
        return String.format("Node #%d, %s, %s, %d intervals (%d%% used), [%d - %s]",
                fSequenceNumber,
                (fParentSequenceNumber == -1) ? "Root" : "Parent #" + fParentSequenceNumber,
                toStringSpecific(),
                fIntervals.size(),
                getNodeUsagePercent(),
                fNodeStart,
                (fIsOnDisk || fNodeEnd != 0) ? fNodeEnd : "...");
    }

    /**
     * Debugging function that prints out the contents of this node
     *
     * @param writer
     *            PrintWriter in which we will print the debug output
     */
    @SuppressWarnings("nls")
    public void debugPrintIntervals(PrintWriter writer) {
        /* Only used for debugging, shouldn't be externalized */
        writer.println("Intervals for node #" + fSequenceNumber + ":");

        /* Leaf Nodes don't have children */
        if (getNodeType() != NodeType.LEAF) {
            ParentNode thisNode = (ParentNode) this;
            writer.print("  " + thisNode.getNbChildren() + " children");
            if (thisNode.getNbChildren() >= 1) {
                writer.print(": [ " + thisNode.getChild(0));
                for (int i = 1; i < thisNode.getNbChildren(); i++) {
                    writer.print(", " + thisNode.getChild(i));
                }
                writer.print(']');
            }
            writer.print('\n');
        }

        /* List of intervals in the node */
        writer.println("  Intervals contained:");
        for (int i = 0; i < fIntervals.size(); i++) {
            writer.println(fIntervals.get(i).toString());
        }
        writer.println('\n');
    }

    // ------------------------------------------------------------------------
    // Abstract methods
    // ------------------------------------------------------------------------

    /**
     * Get the byte value representing the node type.
     *
     * @return The node type
     */
    public abstract NodeType getNodeType();

    /**
     * Return the specific header size of this node. This means the size
     * occupied by the type-specific section of the header (not counting the
     * common part).
     *
     * @return The specific header size
     */
    protected abstract int getSpecificHeaderSize();

    /**
     * Read the type-specific part of the node header from a byte buffer.
     *
     * @param buffer
     *            The byte buffer to read from. It should be already positioned
     *            correctly.
     */
    protected abstract void readSpecificHeader(ByteBuffer buffer);

    /**
     * Write the type-specific part of the header in a byte buffer.
     *
     * @param buffer
     *            The buffer to write to. It should already be at the correct
     *            position.
     */
    protected abstract void writeSpecificHeader(ByteBuffer buffer);

    /**
     * Node-type-specific toString method. Used for debugging.
     *
     * @return A string representing the node
     */
    protected abstract String toStringSpecific();

}
