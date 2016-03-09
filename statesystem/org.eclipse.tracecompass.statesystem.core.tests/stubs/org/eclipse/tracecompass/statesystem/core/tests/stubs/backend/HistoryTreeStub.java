/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests.stubs.backend;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.IOException;
import java.util.List;

import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HTConfig;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HTNode;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HistoryTree;

import com.google.common.collect.Iterables;

/**
 * Stub class to unit test the history tree. You can set the size of the
 * interval section before using the tree, in order to fine-tune the test.
 *
 * Note to developers: This tree is not meant to be used with a backend. It just
 * exposes some info from the history tree.
 *
 * @author Geneviève Bastien
 */
public class HistoryTreeStub extends HistoryTree {

    /**
     * Constructor for this history tree stub
     *
     * @param conf
     *            The config to use for this History Tree.
     * @throws IOException
     *             If an error happens trying to open/write to the file
     *             specified in the config
     */
    public HistoryTreeStub(HTConfig conf) throws IOException {
        super(conf);
    }

    @Override
    public List<HTNode> getLatestBranch() {
        return checkNotNull(super.getLatestBranch());
    }

    /**
     * Get the latest leaf of the tree
     *
     * @return The current leaf node of the tree
     */
    public HTNode getLatestLeaf() {
        List<HTNode> latest = getLatestBranch();
        return Iterables.getLast(latest);
    }

    /**
     * Get the node from the latest branch at a given position, 0 being the root
     * and <size of latest branch - 1> being a leaf node.
     *
     * @param pos
     *            The position at which to return the node
     * @return The node at position pos
     */
    public HTNode getNodeAt(int pos) {
        List<HTNode> latest = getLatestBranch();
        return latest.get(pos);
    }

    /**
     * Get the depth of the tree
     *
     * @return The depth of the tree
     */
    public int getDepth() {
        return getLatestBranch().size();
    }

}
