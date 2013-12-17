/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial API
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.statistics.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class to create and store TMF statistic trees.
 *
 * Based on a given tree node ID a TMF statistic tree is stored internally. A
 * root node is created for each tree. Using the tree node ID the statistics
 * tree can be retrieved.
 *
 * @author Mathieu Denis
 * @version 2.0
 * @since 2.0
 */
public class TmfStatisticsTreeManager {

    /**
     * Contains the experiment name as the key and the traces data
     */
    private static final Map<String, TmfStatisticsTree> fTreeInstances = new HashMap<>();

    /**
     * Provide a statisticsTree instance per trace
     *
     * @param traceUniqueId
     *            Unique ID for the trace
     * @return The root node of the corresponding trace statistics tree
     */
    public static TmfStatisticsTreeNode getStatTreeRoot(String traceUniqueId) {

        TmfStatisticsTree tree = getStatTree(traceUniqueId);
        if (tree == null) {
            return null;
        }
        return tree.getRootNode();
    }

    /**
     * Get the tree that's being used for statistics
     *
     * @param traceUniqueId
     *            Unique ID for the trace
     * @return the corresponding trace statistics tree
     */
    public static TmfStatisticsTree getStatTree(String traceUniqueId) {
        if (traceUniqueId == null) {
            return null;
        }

        TmfStatisticsTree tree = fTreeInstances.get(traceUniqueId);
        return tree;
    }

    /**
     * Add the new trace statistics data in the tree. Can be used later on if
     * the same traces is selected back.
     *
     * @param traceUniqueId
     *            The name of the trace which will be used as a key to store the
     *            data. Must be different for each traces, otherwise the traces
     *            might be overwritten which would trigger a reload of the same
     *            trace.
     * @param statsData
     *            The information about the trace
     * @return The newly created root node of the trace statistics tree, or null if something went wrong
     */
    public static TmfStatisticsTreeNode addStatsTreeRoot(String traceUniqueId, TmfStatisticsTree statsData) {
        if (traceUniqueId == null || statsData == null) {
            return null;
        }
        fTreeInstances.put(traceUniqueId, statsData);
        return statsData.getRootNode();
    }

    /**
     * Return if the given trace is currently known by the statistics manager.
     *
     * @param traceUniqueId
     *            The unique ID of the trace
     * @return true if the trace id is known
     */
    public static boolean containsTreeRoot(String traceUniqueId) {
        return fTreeInstances.containsKey(traceUniqueId);
    }

    /**
     * Remove previously registered statistics tree.
     *
     * @param traceUniqueId
     *            The unique ID of the trace
     */
    public static void removeStatTreeRoot(String traceUniqueId) {
        if (traceUniqueId != null && fTreeInstances.containsKey(traceUniqueId)) {
            fTreeInstances.remove(traceUniqueId);
        }
    }

    /**
     * Remove all tree and root instances
     */
    public static void removeAll() {
        fTreeInstances.clear();
    }
}
