/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.trace.indexer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.text.MessageFormat;

import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.trace.indexer.ITmfPersistentlyIndexable;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;

/**
 * A BTree made of BTreeNodes representing a series of ITmfCheckpoints ordered
 * by time stamps. {@link BTreeNodeCache } is used to improve performance by
 * caching some nodes in memory and the other nodes are kept on disk.
 *
 * @author Marc-Andre Laperle
 */
public class BTree extends AbstractFileCheckpointCollection {

    /**
     * Typical BTree file name
     */
    public static final String INDEX_FILE_NAME = "checkpoint_btree.idx"; //$NON-NLS-1$
    private static final int SUB_VERSION = 4;
    private static final boolean ALWAYS_CACHE_ROOT = true;

    private final int fMaxNumEntries;
    private final int fMaxNumChildren;
    private final int fMedianEntry;

    private BTreeHeader fBTreeHeader;

    // Cached values
    private int nodeSize = -1;
    private final ByteBuffer fNodeByteBuffer;
    private final BTreeNodeCache fNodeCache;

    private class BTreeHeader extends CheckpointCollectionFileHeader {
        private static final int SIZE = LONG_SIZE + INT_SIZE;
        private long fRoot;
        private final int fSubVersion;

        private BTreeHeader(int version, int subVersion) {
            super(version);
            fSubVersion = subVersion;
        }

        private BTreeHeader(RandomAccessFile randomAccessFile) throws IOException {
            super(randomAccessFile);

            fRoot = randomAccessFile.readLong();
            fSubVersion = randomAccessFile.readInt();
        }

        @Override
        public int getSubVersion() {
            return fSubVersion;
        }

        @Override
        public int getSize() {
            return SIZE + super.getSize();
        }

        @Override
        public void serialize(RandomAccessFile randomAccessFile) throws IOException {
            super.serialize(randomAccessFile);

            randomAccessFile.writeLong(fRoot);
            randomAccessFile.writeInt(fSubVersion);
        }
    }

    @Override
    protected CheckpointCollectionFileHeader createHeader() {
        fBTreeHeader = new BTreeHeader(getVersion(), SUB_VERSION);
        return fBTreeHeader;
    }

    @Override
    protected CheckpointCollectionFileHeader createHeader(RandomAccessFile randomAccessFile) throws IOException {
        fBTreeHeader = new BTreeHeader(randomAccessFile);
        return fBTreeHeader;
    }

    @Override
    protected int getSubVersion() {
        return SUB_VERSION;
    }

    /**
     * Constructs a BTree for a given trace from scratch or from an existing
     * file. The degree is used to calibrate the number of entries in each node
     * which can affect performance. When the BTree is created from scratch, it
     * is populated by subsequent calls to {@link #insert}.
     *
     * @param degree
     *            the degree to use in the tree
     * @param file
     *            the file to use as the persistent storage
     * @param trace
     *            the trace
     */
    public BTree(int degree, File file, ITmfPersistentlyIndexable trace) {
        super(file, trace);

        fMaxNumEntries = 2 * degree - 1;
        fMaxNumChildren = 2 * degree;
        fMedianEntry = degree - 1;

        fNodeByteBuffer = ByteBuffer.allocate(getNodeSize());
        fNodeByteBuffer.clear();
        fNodeCache = new BTreeNodeCache(this);
        BTreeNode rootNode = isCreatedFromScratch() ? allocateNode() : fNodeCache.getNode(fBTreeHeader.fRoot);
        setRootNode(rootNode);
    }

    /**
     * Insert a checkpoint into the file-backed BTree
     *
     * @param checkpoint
     *            the checkpoint to insert
     */
    @Override
    public void insert(ITmfCheckpoint checkpoint) {
        insert(checkpoint, fBTreeHeader.fRoot, null, 0);
    }

    private void setRootNode(BTreeNode newRootNode) {
        fBTreeHeader.fRoot = newRootNode.getOffset();
        if (ALWAYS_CACHE_ROOT) {
            fNodeCache.setRootNode(newRootNode);
        } else {
            fNodeCache.addNode(newRootNode);
        }
    }

    private void insert(ITmfCheckpoint checkpoint, long nodeOffset, BTreeNode pParent, int iParent) {
        BTreeNode parent = pParent;
        BTreeNode node = fNodeCache.getNode(nodeOffset);

        // If this node is full (last entry isn't null), split it
        if (node.getEntry(fMaxNumEntries - 1) != null) {

            ITmfCheckpoint median = node.getEntry(fMedianEntry);
            if (median.compareTo(checkpoint) == 0) {
                // Found it
                return;
            }

            // Split it.
            // Create the new node and move the larger entries over.
            BTreeNode newnode = allocateNode();
            fNodeCache.addNode(newnode);
            long newNodeOffset = newnode.getOffset();
            for (int i = 0; i < fMedianEntry; ++i) {
                newnode.setEntry(i, node.getEntry(fMedianEntry + 1 + i));
                node.setEntry(fMedianEntry + 1 + i, null);
                newnode.setChild(i, node.getChild(fMedianEntry + 1 + i));
                node.setChild(fMedianEntry + 1 + i, BTreeNode.NULL_CHILD);
            }
            newnode.setChild(fMedianEntry, node.getChild(fMaxNumEntries));
            node.setChild(fMaxNumEntries, BTreeNode.NULL_CHILD);

            if (parent == null) {
                parent = allocateNode();
                setRootNode(parent);
                parent.setChild(0, nodeOffset);
            } else {
                // Insert the median into the parent.
                for (int i = fMaxNumEntries - 2; i >= iParent; --i) {
                    ITmfCheckpoint r = parent.getEntry(i);
                    if (r != null) {
                        parent.setEntry(i + 1, r);
                        parent.setChild(i + 2, parent.getChild(i + 1));
                    }
                }
            }

            fNodeCache.getNode(parent.getOffset());

            parent.setEntry(iParent, median);
            parent.setChild(iParent + 1, newNodeOffset);

            node.setEntry(fMedianEntry, null);

            // Set the node to the correct one to follow.
            if (checkpoint.compareTo(median) > 0) {
                node = newnode;
            }
        }

        // Binary search to find the insert point.
        int lower = 0;
        int upper = fMaxNumEntries - 1;
        while (lower < upper && node.getEntry(upper - 1) == null) {
            upper--;
        }

        while (lower < upper) {
            int middle = (lower + upper) / 2;
            ITmfCheckpoint check = node.getEntry(middle);
            if (check == null) {
                upper = middle;
            } else {
                int compare = check.compareTo(checkpoint);
                if (compare > 0) {
                    upper = middle;
                } else if (compare < 0) {
                    lower = middle + 1;
                } else {
                    // Found it, no insert
                    return;
                }
            }
        }
        final int i = lower;
        long child = node.getChild(i);
        if (child != BTreeNode.NULL_CHILD) {
            // Visit the children.
            insert(checkpoint, child, node, i);
        } else {
            // We are at the leaf, add us in.
            // First copy everything after over one.
            for (int j = fMaxNumEntries - 2; j >= i; --j) {
                ITmfCheckpoint r = node.getEntry(j);
                if (r != null) {
                    node.setEntry(j + 1, r);
                }
            }
            node.setEntry(i, checkpoint);
            return;
        }
    }

    int getNodeSize() {
        if (nodeSize == -1) {
            nodeSize = INT_SIZE; // num entries
            nodeSize += getTrace().getCheckpointSize() * fMaxNumEntries;
            nodeSize += LONG_SIZE * fMaxNumChildren;
        }

        return nodeSize;
    }

    private BTreeNode allocateNode() {
        try {
            long offset = getRandomAccessFile().length();
            getRandomAccessFile().setLength(offset + getNodeSize());
            BTreeNode node = new BTreeNode(this, offset);
            return node;
        } catch (IOException e) {
            Activator.logError(MessageFormat.format(Messages.BTree_IOErrorAllocatingNode, getFile()), e);
        }
        return null;
    }

    @Override
    public long binarySearch(ITmfCheckpoint checkpoint) {
        BTreeCheckpointVisitor v = new BTreeCheckpointVisitor(checkpoint);
        accept(v);
        return v.getCheckpointRank();
    }

    /**
     * Accept a visitor. This visitor is used to search through the whole tree.
     *
     * @param treeVisitor
     *            the visitor to accept
     */
    public void accept(IBTreeVisitor treeVisitor) {
        accept(fBTreeHeader.fRoot, treeVisitor);
    }

    private void accept(long nodeOffset, IBTreeVisitor visitor) {

        if (nodeOffset == BTreeNode.NULL_CHILD) {
            return;
        }

        BTreeNode node = fNodeCache.getNode(nodeOffset);

        // Binary search to find first entry greater or equal.
        int lower = 0;
        int upper = fMaxNumEntries - 1;
        while (lower < upper && node.getEntry(upper - 1) == null) {
            upper--;
        }
        while (lower < upper) {
            int middle = (lower + upper) / 2;
            ITmfCheckpoint middleCheckpoint = node.getEntry(middle);
            if (middleCheckpoint == null) {
                upper = middle;
            } else {
                int compare = visitor.compare(middleCheckpoint);
                if (compare == 0) {
                    return;
                } else if (compare > 0) {
                    upper = middle;
                } else {
                    lower = middle + 1;
                }
            }
        }

        // Start with first record greater or equal, reuse comparison
        // results.
        int i = lower;
        for (; i < fMaxNumEntries; ++i) {
            ITmfCheckpoint record = node.getEntry(i);
            if (record == null) {
                break;
            }

            int compare = visitor.compare(record);
            if (compare > 0) {
                // Start point is to the left.
                accept(node.getChild(i), visitor);
                return;
            } else if (compare == 0) {
                return;
            }
        }
        accept(node.getChild(i), visitor);
        return;
    }

    /**
     * Set the index as complete. No more checkpoints will be inserted.
     */
    @Override
    public void setIndexComplete() {
        super.setIndexComplete();

        fNodeCache.serialize();
    }

    /**
     * Get the maximum number of entries in a node
     *
     * @return the maximum number of entries in a node
     */
    int getMaxNumEntries() {
        return fMaxNumEntries;
    }

    /**
     * Set the size of the BTree, expressed as a number of checkpoints
     *
     * @param size
     *            the size of the BTree
     */
    public void setSize(int size) {
        fBTreeHeader.fSize = size;
    }

    /**
     * Get the maximum number of children in a node
     *
     * @return the maximum number of children in a node
     */
    int getMaxNumChildren() {
        return fMaxNumChildren;
    }

    ByteBuffer getNodeByteBuffer() {
        return fNodeByteBuffer;
    }
}
