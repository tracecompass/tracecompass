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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Arrays;

import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;

/**
 * A node in the BTree. A node contains entries and pointers to other nodes.
 * The layout can be illustrated like this:
 *
 *               |
 *   ___________Node__________
 *   |  e  |  e  |  e  |  e  |
 *   p     p     p     p     p
 *   |
 *   |
 *  Node  ...
 *
 *  Where e is an entry, p is a pointer to another node.
 *
 *  A pointer on the left side of an entry points to a node containing entries
 *  that are of lesser value than the entry and a pointer on the right side of
 *  an entry points to a node containing entries that are of greater value
 *  than the entry. In this implementation, entries are ITmfCheckpoints and
 *  pointers are file offsets (long).
 *
 * @author Marc-Andre Laperle
 */
class BTreeNode {

    /**
     * Integer to represent a null child
     */
    static final int NULL_CHILD = -1;

    private final ITmfCheckpoint fEntries[];
    private final long fChildrenFileOffsets[];
    private final long fFileOffset;
    private final BTree fTree;

    private int fNumEntries = 0;
    private boolean fIsDirty = false;

    /**
     * Construct a node for the specified tree for the specified file offset
     *
     * @param tree
     *            the BTree
     * @param offset
     *            the file offset
     */
    BTreeNode(BTree tree, long offset) {
        fTree = tree;
        fFileOffset = offset;
        fEntries = new ITmfCheckpoint[fTree.getMaxNumEntries()];
        fChildrenFileOffsets = new long[fTree.getMaxNumChildren()];
        Arrays.fill(fChildrenFileOffsets, NULL_CHILD);
    }

    /**
     * Get the file offset for this node
     *
     * @return the file offset
     */
    long getOffset() {
        return fFileOffset;
    }

    /**
     * Read the node data from disk
     */
    void serializeIn() {
        try {
            fTree.getRandomAccessFile().seek(fFileOffset);

            ByteBuffer bb;
            bb = fTree.getNodeByteBuffer();
            bb.clear();
            fTree.getRandomAccessFile().read(bb.array());

            for (int i = 0; i < fTree.getMaxNumChildren(); ++i) {
                fChildrenFileOffsets[i] = bb.getLong();
            }
            fNumEntries = bb.getInt();

            for (int i = 0; i < fNumEntries; ++i) {

                ITmfLocation location = fTree.getTrace().restoreLocation(bb);
                ITmfTimestamp timeStamp = new TmfTimestamp(bb);
                TmfCheckpoint c = new TmfCheckpoint(timeStamp, location, bb);
                fEntries[i] = c;
            }

        } catch (IOException e) {
            Activator.logError(MessageFormat.format(Messages.BTreeNode_IOErrorLoading, fFileOffset, fTree.getRandomAccessFile()), e);
        }
    }

    /**
     * Write the node data to disk
     */
    void serializeOut() {
        try {
            fTree.getRandomAccessFile().seek(fFileOffset);

            ByteBuffer bb = fTree.getNodeByteBuffer();
            bb.clear();

            for (int i = 0; i < fTree.getMaxNumChildren(); ++i) {
                bb.putLong(fChildrenFileOffsets[i]);
            }
            bb.putInt(fNumEntries);

            for (int i = 0; i < fNumEntries; ++i) {
                ITmfCheckpoint key = fEntries[i];
                key.serialize(bb);
            }

            fTree.getRandomAccessFile().write(bb.array());

            fIsDirty = false;
        } catch (IOException e) {
            Activator.logError(MessageFormat.format(Messages.BTreeNode_IOErrorWriting, fFileOffset, fTree.getRandomAccessFile()), e);
        }
    }

    /**
     * Get the entry at the given index
     *
     * @param index
     *            the index where to get the entry
     * @return the entry at the index
     */
    ITmfCheckpoint getEntry(int index) {
        return fEntries[index];
    }

    long getChild(int index) {
        return fChildrenFileOffsets[index];
    }

    /**
     * Set the checkpoint entry at the given index
     *
     * @param index
     *            the index where to set the entry
     * @param checkpoint
     *            the checkpoint to set at the index
     */
    void setEntry(int index, ITmfCheckpoint checkpoint) {
        fIsDirty = true;
        // Update number of entries
        if (fEntries[index] == null && checkpoint != null) {
            ++fNumEntries;
        } else if (fEntries[index] != null && checkpoint == null) {
            fNumEntries = Math.max(0, fNumEntries - 1);
        }

        fEntries[index] = checkpoint;
    }

    /**
     * Set the child file offset at the given index
     *
     * @param index
     *            the index where to set the child offset
     * @param offset
     *            the child offset
     */
    void setChild(int index, long offset) {
        fIsDirty = true;
        fChildrenFileOffsets[index] = offset;
    }

    /**
     * Returns whether or not the node is dirty, that is, if the node has been
     * modified since it first has been loaded into memory
     *
     * @return true if the node is dirty, false otherwise
     */
    boolean isDirty() {
        return fIsDirty;
    }
}
