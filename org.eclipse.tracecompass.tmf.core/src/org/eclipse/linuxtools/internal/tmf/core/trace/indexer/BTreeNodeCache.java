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

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A simple LRU node cache. The BTree request a node from the cache and the
 * cache load it from disk if it's not already in memory.
 *
 * This cache could be improved considerably by allowing bigger caches.
 *
 * @author Marc-Andre Laperle
 */
public class BTreeNodeCache {

    /**
     * Cache size obtained by experimentation. An improved cache could set this
     * dynamically or using a user preference.
     */
    private static final int CACHE_SIZE = 15;

    private final BTree fTree;
    /**
     * The root node is always kept in memory when {@link
     * BTree#ALWAYS_CACHE_ROOT} is set to true
     */
    private BTreeNode fRootNode = null;
    /**
     * The collection keeping the nodes in memory. The most recently used is
     * kept at the front of the double-ended queue and the least recently used
     * node is kept at the back.
     */
    private final Deque<BTreeNode> fCachedNodes = new ArrayDeque<>(CACHE_SIZE);

    private int fCcheMisses = 0;

    /**
     * Construct a new node cache for the given BTree
     *
     * @param tree
     *            the BTree that will use the cache
     */
    BTreeNodeCache(BTree tree) {
        fTree = tree;
    }

    /**
     * Get the node at the offset from the cache. If the node is not found in
     * memory, it is loaded from disk.
     *
     * @param offset
     * @return
     */
    BTreeNode getNode(long offset) {
        if (fRootNode != null && fRootNode.getOffset() == offset) {
            return fRootNode;
        }

        for (BTreeNode nodeSearch : fCachedNodes) {
            if (nodeSearch.getOffset() == offset) {
                // This node is now the most recently used
                fCachedNodes.remove(nodeSearch);
                fCachedNodes.push(nodeSearch);

                return nodeSearch;
            }
        }

        ++fCcheMisses;

        BTreeNode node = new BTreeNode(fTree, offset);
        node.serializeIn();
        addNode(node);

        return node;
    }

    /**
     * Write all in-memory nodes to disk if they are dirty
     */
    void serialize() {
        if (fRootNode != null && fRootNode.isDirty()) {
            fRootNode.serializeOut();
        }
        for (BTreeNode nodeSearch : fCachedNodes) {
            if (nodeSearch.isDirty()) {
                nodeSearch.serializeOut();
            }
        }
    }

    /**
     * Add a node to the cache. If the cache has reached the size specified with
     * {@link #CACHE_SIZE}, the least recently used node is removed from memory.
     *
     * @param node
     *            the node to add to the cache
     */
    void addNode(BTreeNode node) {
        if (fCachedNodes.size() >= CACHE_SIZE) {
            BTreeNode removed = fCachedNodes.removeLast();
            if (removed.isDirty()) {
                removed.serializeOut();
            }
        }
        fCachedNodes.push(node);
    }

    /**
     * Set the root node. See {@link #fRootNode}
     *
     * @param newRootNode
     *            the new root node
     */
    void setRootNode(BTreeNode newRootNode) {
        BTreeNode oldRootNode = fRootNode;
        fRootNode = newRootNode;
        if (oldRootNode != null) {
            addNode(oldRootNode);
        }
        return;
    }

    /**
     * Useful for benchmarks. Get the number of cache misses for the whole BTree
     * instance lifetime. Cache misses occur when a node is requested and it's
     * not in memory therefore it has to be read from disk.
     *
     * @return the number of cache misses.
     */
    int getCacheMisses() {
        return fCcheMisses;
    }
}
