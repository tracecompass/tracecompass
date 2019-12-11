/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.segmentstore.core.segmentHistoryTree;

import java.io.File;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.datastore.core.interval.IHTIntervalReader;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.TimeRangeCondition;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.HTNode;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.IHTNode;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.overlapping.AbstractOverlappingHistoryTree;
import org.eclipse.tracecompass.internal.segmentstore.core.Activator;
import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;

import com.google.common.annotations.VisibleForTesting;

/**
 * Specific implementation of the history tree to save a segment store. It adds
 * specific methods to get the elements intersecting a given time range.
 *
 * @author Loic Prieur-Drevon
 * @author Geneviève Bastien
 * @param <E>
 *            type of {@link ISegment}
 */
public class SegmentHistoryTree<E extends ISegment> extends AbstractOverlappingHistoryTree<E, SegmentTreeNode<E>> {

    private static final int HISTORY_MAGIC_NUMBER = 0x05FFC600;

    /** File format version. Increment when breaking compatibility. */
    private static final int FILE_VERSION = 1;

    private static final int ITERATOR_QUEUE_SIZE = 2000;

    // ------------------------------------------------------------------------
    // Constructors/"Destructors"
    // ------------------------------------------------------------------------

    /**
     * Create a new State History from scratch, specifying all configuration
     * parameters.
     *
     * @param stateHistoryFile
     *            The name of the history file
     * @param blockSize
     *            The size of each "block" on disk in bytes. One node will
     *            always fit in one block. It should be at least 4096.
     * @param maxChildren
     *            The maximum number of children allowed per core (non-leaf)
     *            node.
     * @param providerVersion
     *            The version of the state provider. If a file already exists,
     *            and their versions match, the history file will not be rebuilt
     *            uselessly.
     * @param treeStart
     *            The start time of the history
     * @param intervalReader
     *            typed ISegment to allow access to the readSegment methods
     * @throws IOException
     *             If an error happens trying to open/write to the file
     *             specified in the config
     */
    public SegmentHistoryTree(File stateHistoryFile,
            int blockSize,
            int maxChildren,
            int providerVersion,
            long treeStart,
            IHTIntervalReader<E> intervalReader) throws IOException {
        super(stateHistoryFile,
                blockSize,
                maxChildren,
                providerVersion,
                treeStart,
                intervalReader);

    }

    /**
     * "Reader" constructor : instantiate a SHTree from an existing tree file on
     * disk
     *
     * @param existingStateFile
     *            Path/filename of the history-file we are to open
     * @param expProviderVersion
     *            The expected version of the state provider
     * @param intervalReader
     *            typed ISegment to allow access to the readSegment methods
     * @throws IOException
     *             If an error happens reading the file
     */
    public SegmentHistoryTree(File existingStateFile, int expProviderVersion, IHTIntervalReader<E> intervalReader) throws IOException {
        super(existingStateFile, expProviderVersion, intervalReader);
    }

    @Override
    protected int getMagicNumber() {
        return HISTORY_MAGIC_NUMBER;
    }

    @Override
    protected int getFileVersion() {
        return FILE_VERSION;
    }

    @Override
    protected @NonNull IHTNodeFactory<E, SegmentTreeNode<E>> getNodeFactory() {
        // This cannot be defined statically because of the generic and because
        // this method is called from the constructor of the abstract class,
        // assigning it in a final field in the constructor generates a NPE. So
        // we return the method directly here.
        return (t, b, m, seq, p, start) -> new SegmentTreeNode<>(t, b, m, seq, p, start);
    }

    // ------------------------------------------
    // Segment store specific methods
    // ------------------------------------------

    /**
     * Get the number of elements in this history tree
     *
     * @return The number of elements in this tree
     */
    public int size() {
        SegmentTreeNode<E> node;
        long total = 0;

        try {
            // Add the number of intervals of each node
            for (int seq = 0; seq < getNodeCount(); seq++) {
                node = readNode(seq);
                total += node.getNumIntervals();
            }
        } catch (ClosedChannelException e) {
            Activator.instance().logError(e.getMessage(), e);
            return 0;
        }
        return (int) total;
    }

    /**
     * Return whether the tree is empty or not
     *
     * @return <code>true</code> if the tree is empty
     */
    public boolean isEmpty() {
        Iterator<E> it = iterator();
        if (it == null) {
            return true;
        }
        return !it.hasNext();
    }

    // ------------------------------------------
    // Iterators
    // ------------------------------------------

    /**
     * Get an iterator for the elements of this tree. It uses
     * {@link #getIntersectingElements(long, long)} for the full duration of the
     * tree.
     *
     * @return An iterator for this tree
     */
    public @Nullable Iterator<E> iterator() {
        return getIntersectingElements(getTreeStart(), getTreeEnd()).iterator();
    }

    /**
     * Return an iterator for a range. It will iterate through the nodes
     * top-down and visit only the nodes that contain segments within this range
     * and for each of those nodes, it gets the segments that intersect the
     * range
     *
     * @param start
     *            The start of the range
     * @param end
     *            The end of the range
     * @return The iterable
     */
    public Iterable<@NonNull E> getIntersectingElements(final long start, final long end) {
        final TimeRangeCondition rc = TimeRangeCondition.forContinuousRange(start, end);
        return () -> new Iterator<E>() {

            private boolean started = false;
            private Deque<Integer> queue = new LinkedList<>();
            private Deque<E> intersecting = new LinkedList<>();

            @Override
            public @NonNull E next() {
                if (hasNext()) {
                    return NonNullUtils.checkNotNull(intersecting.removeFirst());
                }
                throw new NoSuchElementException();
            }

            @Override
            public boolean hasNext() {
                /* Iteration has not started yet */
                if (!started) {
                    queue.add(getRootNode().getSequenceNumber());
                    started = true;
                }

                /*
                 * Need to read nodes until either we find more segments
                 * or iterate over all segments
                 */
                while (intersecting.isEmpty() && !queue.isEmpty()) {
                    SegmentTreeNode<E> currentNode;
                    try {
                        currentNode = readNode(queue.pop());
                    } catch (ClosedChannelException e) {
                        Activator.instance().logError(e.getMessage(), e);
                        return false;
                    }
                    if (currentNode.getNodeType() == IHTNode.NodeType.CORE) {
                        queue.addAll(currentNode.selectNextChildren(rc));
                    }
                    intersecting.addAll(currentNode.getMatchingIntervals(rc, interval -> true));
                }

                /* Return if we have found segments */
                return !intersecting.isEmpty();
            }
        };
    }

    /**
     * Return an iterator for a range where segments are sorted
     *
     * @param start
     *            The start of the range
     * @param end
     *            The end of the range
     * @param order
     *            The comparator to sort elements with
     * @return The iterable
     */
    public Iterable<@NonNull E> getIntersectingElements(long start, long end, Comparator<@NonNull E> order) {
        final TimeRangeCondition rc = TimeRangeCondition.forContinuousRange(start, end);
        return () -> new Iterator<E>() {

            private boolean started = false;
            private PriorityQueue<SegmentTreeNode.Tuple<E>> queue = new PriorityQueue<>(getNodeCount(),
                    Comparator.comparing(SegmentTreeNode.Tuple<E>::getSegment, order));

            private PriorityQueue<E> intersecting = new PriorityQueue<>(ITERATOR_QUEUE_SIZE, order);

            @Override
            public @NonNull E next() {
                if (hasNext()) {
                    return NonNullUtils.checkNotNull(intersecting.remove());
                }
                throw new NoSuchElementException();
            }

            @Override
            public boolean hasNext() {
                /* Iteration has not started yet */
                if (!started) {
                    SegmentTreeNode<E> rootNode = getRootNode();

                    /*
                     * Add the root node with any segment for the tuple,
                     * it will always be read.
                     */
                    queue.add(new SegmentTreeNode.Tuple(new BasicSegment(0,0), rootNode.getSequenceNumber()));

                    started = true;
                }

                /*
                 * Need to read nodes until either we find more segments
                 * or iterate over all nodes
                 */
                while (!queue.isEmpty() && (intersecting.isEmpty()
                        || order.compare(intersecting.element(), queue.peek().getSegment()) > 0)) {
                    SegmentTreeNode<E> currentNode;
                    try {
                        currentNode = readNode(queue.poll().getSequenceNumber());
                    } catch (ClosedChannelException e) {
                        Activator.instance().logError(e.getMessage(), e);
                        return false;
                    }
                    if (currentNode.getNodeType() == HTNode.NodeType.CORE) {
                        queue.addAll(((SegmentTreeNode) currentNode).selectNextChildren(rc, order));
                    }
                    intersecting.addAll(currentNode.getMatchingIntervals(rc, interval -> true));
                }

                /* Return if we have found segments */
                return !intersecting.isEmpty();
            }
        };
    }

    // ------------------------------------------------------------------------
    // Test-specific methods
    // ------------------------------------------------------------------------

    @Override
    @VisibleForTesting
    protected boolean verifyChildrenSpecific(SegmentTreeNode<E> parent, int index, SegmentTreeNode<E> child) {
        return (parent.getMaxStart(index) >= child.getMaxStart()
                && parent.getMinEnd(index) <= child.getMinEnd()
                && parent.getLongest(index) >= child.getLongest()
                && parent.getShortest(index) <= child.getShortest());
    }

    @Override
    @VisibleForTesting
    protected boolean verifyIntersectingChildren(SegmentTreeNode<E> parent, SegmentTreeNode<E> child) {
        int childSequence = child.getSequenceNumber();
        for (long t = parent.getNodeStart(); t < parent.getNodeEnd(); t++) {
            TimeRangeCondition rc = TimeRangeCondition.singleton(t);
            boolean shouldBeInCollection = (rc.intersects(child.getNodeStart(), child.getNodeEnd()));
            Collection<Integer> nextChildren = parent.selectNextChildren(rc);
            if (shouldBeInCollection != nextChildren.contains(childSequence)) {
                return false;
            }
        }
        return true;
    }

}
