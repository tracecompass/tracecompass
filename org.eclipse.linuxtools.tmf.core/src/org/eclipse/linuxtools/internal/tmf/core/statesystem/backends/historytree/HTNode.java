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
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;

/**
 * The base class for all the types of nodes that go in the History Tree.
 *
 * @author alexmont
 *
 */
abstract class HTNode {

    /* Reference to the History Tree to whom this node belongs */
    protected final HistoryTree ownerTree;

    /* Time range of this node */
    private final long nodeStart;
    private long nodeEnd;

    /* Sequence number = position in the node section of the file */
    private final int sequenceNumber;
    private int parentSequenceNumber; /* = -1 if this node is the root node */

    /* Where the Strings section begins (from the start of the node */
    private int stringSectionOffset;

    /* True if this node is closed (and to be committed to disk) */
    private boolean isDone;

    /* Vector containing all the intervals contained in this node */
    private final ArrayList<HTInterval> intervals;

    HTNode(HistoryTree tree, int seqNumber, int parentSeqNumber, long start) {
        this.ownerTree = tree;
        this.nodeStart = start;
        this.sequenceNumber = seqNumber;
        this.parentSequenceNumber = parentSeqNumber;

        this.stringSectionOffset = ownerTree.config.blockSize;
        this.isDone = false;
        this.intervals = new ArrayList<HTInterval>();
    }

    /**
     * Reader factory constructor. Build a Node object (of the right type) by
     * reading a block in the file.
     *
     * @param tree
     *            Reference to the HT which will own this node
     * @param fc
     *            FileChannel to the history file, ALREADY SEEKED at the start
     *            of the node.
     * @throws IOException
     */
    final static HTNode readNode(HistoryTree tree, FileChannel fc)
            throws IOException {
        HTNode newNode = null;
        int res, i;

        ByteBuffer buffer = ByteBuffer.allocate(tree.config.blockSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.clear();
        res = fc.read(buffer);
        assert (res == tree.config.blockSize);
        // This often breaks, so might as well keep this code not too far...
        // if ( res != tree.config.blockSize ) {
        // tree.debugPrintFullTree(new PrintWriter(System.out, true), null,
        // false);
        // assert ( false );
        // }
        buffer.flip();

        /* Read the common header part */
        byte type = buffer.get();
        long start = buffer.getLong();
        long end = buffer.getLong();
        int seqNb = buffer.getInt();
        int parentSeqNb = buffer.getInt();
        int intervalCount = buffer.getInt();
        int stringSectionOffset = buffer.getInt();
        boolean done = byteToBool(buffer.get());

        /* Now the rest of the header depends on the node type */
        switch (type) {
        case 1:
            /* Core nodes */
            newNode = new CoreNode(tree, seqNb, parentSeqNb, start);
            newNode.readSpecificHeader(buffer);
            break;

        // TODO implement other node types
        // case 2:
        // /* Leaf nodes */
        //
        // break;
        //
        //
        // case 3:
        // /* "Claudette" (extended) nodes */
        //
        // break;

        default:
            /* Unrecognized node type */
            throw new IOException();
        }

        /*
         * At this point, we should be done reading the header and 'buffer'
         * should only have the intervals left
         */
        for (i = 0; i < intervalCount; i++) {
            newNode.intervals.add(HTInterval.readFrom(buffer));
        }

        /* Assign the node's other information we have read previously */
        newNode.nodeEnd = end;
        newNode.stringSectionOffset = stringSectionOffset;
        newNode.isDone = done;

        return newNode;
    }

    final void writeSelf(FileChannel fc) throws IOException {
        int res, size;
        int curStringsEntryEndPos = ownerTree.config.blockSize;

        ByteBuffer buffer = ByteBuffer.allocate(ownerTree.config.blockSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.clear();

        /* Write the common header part */
        buffer.put(this.getNodeType());
        buffer.putLong(nodeStart);
        buffer.putLong(nodeEnd);
        buffer.putInt(sequenceNumber);
        buffer.putInt(parentSequenceNumber);
        buffer.putInt(intervals.size());
        buffer.putInt(stringSectionOffset);
        buffer.put(boolToByte(isDone));

        /* Now call the inner method to write the specific header part */
        this.writeSpecificHeader(buffer);

        /* Back to us, we write the intervals */
        for (HTInterval interval : intervals) {
            size = interval.writeInterval(buffer, curStringsEntryEndPos);
            curStringsEntryEndPos -= size;
        }

        /*
         * Write padding between the end of the Data section and the start of
         * the Strings section (needed to fill the node in case there is no
         * Strings section)
         */
        while (buffer.position() < stringSectionOffset) {
            buffer.put((byte) 0);
        }

        /*
         * If the offsets were right, the size of the Strings section should be
         * == to the expected size
         */
        assert (curStringsEntryEndPos == stringSectionOffset);

        /* Finally, write everything in the Buffer to disk */

        // if we don't do this, flip() will lose what's after.
        buffer.position(ownerTree.config.blockSize);

        buffer.flip();
        res = fc.write(buffer);
        assert (res == ownerTree.config.blockSize);
    }

    /**
     * Accessors
     */
    long getNodeStart() {
        return nodeStart;
    }

    long getNodeEnd() {
        if (this.isDone) {
            return nodeEnd;
        }
        return 0;
    }

    int getSequenceNumber() {
        return sequenceNumber;
    }

    int getParentSequenceNumber() {
        return parentSequenceNumber;
    }

    /**
     * Change this node's parent. Used when we create a new root node for
     * example.
     */
    void setParentSequenceNumber(int newParent) {
        parentSequenceNumber = newParent;
    }

    boolean isDone() {
        return isDone;
    }

    /**
     * Add an interval to this node
     *
     * @param newInterval
     */
    void addInterval(HTInterval newInterval) {
        /* Just in case, but should be checked before even calling this function */
        assert (newInterval.getIntervalSize() <= this.getNodeFreeSpace());

        intervals.add(newInterval);

        /* Update the in-node offset "pointer" */
        stringSectionOffset -= (newInterval.getStringsEntrySize());
    }

    /**
     * We've received word from the containerTree that newest nodes now exist to
     * our right. (Puts isDone = true and sets the endtime)
     *
     * @param endtime
     *            The nodeEnd time that the node will have
     * @throws TimeRangeException
     */
    void closeThisNode(long endtime) {
        assert (endtime >= this.nodeStart);
        // /* This also breaks often too */
        // if ( endtime.getValue() <= this.nodeStart.getValue() ) {
        // ownerTree.debugPrintFullTree(new PrintWriter(System.out, true), null,
        // false);
        // assert ( false );
        // }

        if (intervals.size() > 0) {
            /*
             * Sort the intervals by ascending order of their end time. This
             * speeds up lookups a bit
             */
            Collections.sort(intervals);

            /*
             * Make sure there are no intervals in this node with their EndTime
             * > the one requested. Only need to check the last one since they
             * are now sorted
             */
            assert (endtime >= intervals.get(intervals.size() - 1).getEndTime());
        }

        this.isDone = true;
        this.nodeEnd = endtime;
        return;
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
     */
    void writeInfoFromNode(List<ITmfStateInterval> stateInfo, long t)
            throws TimeRangeException {
        assert (this.isDone); // not sure this will always be the case...
        int startIndex;

        if (intervals.size() == 0) {
            return;
        }
        startIndex = getStartIndexFor(t);

        for (int i = startIndex; i < intervals.size(); i++) {
            /*
             * Now we only have to compare the Start times, since we now the End
             * times necessarily fit
             */
            if (intervals.get(i).getStartTime() <= t) {
                stateInfo.set(intervals.get(i).getAttribute(), intervals.get(i));
            }
        }
        return;
    }

    /**
     * Get a single Interval from the information in this node If the
     * key/timestamp pair cannot be found, we return null.
     *
     * @param key
     * @param t
     * @return The Interval containing the information we want, or null if it
     *         wasn't found
     * @throws TimeRangeException
     */
    HTInterval getRelevantInterval(int key, long t) throws TimeRangeException {
        assert (this.isDone);
        int startIndex;
        HTInterval curInterval;

        if (intervals.size() == 0) {
            return null;
        }

        startIndex = getStartIndexFor(t);

        for (int i = startIndex; i < intervals.size(); i++) {
            curInterval = intervals.get(i);
            if (curInterval.getAttribute() == key
                    && curInterval.getStartTime() <= t
                    && curInterval.getEndTime() >= t) {
                return curInterval;
            }
        }
        /* We didn't find the relevant information in this node */
        return null;
    }

    private int getStartIndexFor(long t) throws TimeRangeException {
        HTInterval dummy;
        int index;

        /*
         * Since the intervals are sorted by end time, we can skip all the ones
         * at the beginning whose end times are smaller than 't'. Java does
         * provides a .binarySearch method, but its API is quite weird...
         */
        dummy = new HTInterval(0, t, 0, TmfStateValue.nullValue());
        index = Collections.binarySearch(intervals, dummy);

        if (index < 0) {
            /*
             * .binarySearch returns a negative number if the exact value was
             * not found. Here we just want to know where to start searching, we
             * don't care if the value is exact or not.
             */
            index = -index - 1;

        }

        /* Sometimes binarySearch yields weird stuff... */
        if (index < 0) {
            index = 0;
        }
        if (index >= intervals.size()) {
            index = intervals.size() - 1;
        }

        /*
         * Another API quirkiness, the returned index is the one of the *last*
         * element of a series of equal endtimes, which happens sometimes. We
         * want the *first* element of such a series, to read through them
         * again.
         */
        while (index > 0
                && intervals.get(index - 1).compareTo(intervals.get(index)) == 0) {
            index--;
        }
        // FIXME F*ck all this, just do our own binary search in a saner way...

        // //checks to make sure startIndex works how I think it does
        // if ( startIndex > 0 ) { assert ( intervals.get(startIndex-1).getEnd()
        // < t ); }
        // assert ( intervals.get(startIndex).getEnd() >= t );
        // if ( startIndex < intervals.size()-1 ) { assert (
        // intervals.get(startIndex+1).getEnd() >= t ); }

        return index;
    }

    /**
     * @return The offset, within the node, where the Data section ends
     */
    private int getDataSectionEndOffset() {
        return this.getTotalHeaderSize() + HTNode.getDataEntrySize()
                * intervals.size();
    }

    /**
     * Returns the free space in the node, which is simply put, the
     * stringSectionOffset - dataSectionOffset
     */
    int getNodeFreeSpace() {
        return stringSectionOffset - this.getDataSectionEndOffset();
    }

    /**
     * Returns the current space utilisation of this node, as a percentage.
     * (used space / total usable space, which excludes the header)
     */
    long getNodeUsagePRC() {
        float freePercent = (float) this.getNodeFreeSpace()
                / (float) (ownerTree.config.blockSize - this.getTotalHeaderSize())
                * 100f;
        return (long) (100L - freePercent);
    }

    protected final static int getDataEntrySize() {
        return 16 /* 2 x Timevalue/long (interval start + end) */
        + 4 /* int (key) */
        + 1 /* byte (type) */
        + 4; /* int (valueOffset) */
        /* = 25 */
    }

    protected final static byte boolToByte(boolean thebool) {
        if (thebool) {
            return (byte) 1;
        }
        return (byte) 0;
    }

    final static boolean byteToBool(byte thebyte) {
        return (thebyte == (byte) 1);
    }

    /**
     * @name Debugging functions
     */

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        /* Only used for debugging, shouldn't be externalized */
        StringBuffer buf = new StringBuffer("Node #" + sequenceNumber + ", ");
        buf.append(this.toStringSpecific());
        buf.append(intervals.size() + " intervals (" + this.getNodeUsagePRC()
                + "% used), ");

        buf.append("[" + this.nodeStart + " - ");
        if (this.isDone) {
            buf = buf.append("" + this.nodeEnd + "]");
        } else {
            buf = buf.append("...]");
        }
        return buf.toString();
    }

    /**
     * Debugging function that prints out the contents of this node
     *
     * @param writer
     *            PrintWriter in which we will print the debug output
     */
    @SuppressWarnings("nls")
    void debugPrintIntervals(PrintWriter writer) {
        /* Only used for debugging, shouldn't be externalized */
        writer.println("Node #" + sequenceNumber + ":");

        /* Array of children */
        if (this.getNodeType() == 1) { /* Only Core Nodes can have children */
            CoreNode thisNode = (CoreNode) this;
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
        for (int i = 0; i < intervals.size(); i++) {
            writer.println(intervals.get(i).toString());
        }
        writer.println('\n');
    }

    final static int getCommonHeaderSize() {
        /*
         * 1 - byte (type)
         *
         * 16 - 2x long (start time, end time)
         *
         * 16 - 4x int (seq number, parent seq number, intervalcount, strings
         * section pos.)
         *
         * 1 - byte (done or not)
         */
        return 34;
    }

    // ------------------------------------------------------------------------
    // Abstract methods
    // ------------------------------------------------------------------------

    protected abstract byte getNodeType();

    protected abstract int getTotalHeaderSize();

    protected abstract void readSpecificHeader(ByteBuffer buffer);

    protected abstract void writeSpecificHeader(ByteBuffer buffer);

    protected abstract String toStringSpecific();
}
