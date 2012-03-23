/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Mathieu Denis      (mathieu.denis@polymtl.ca)  - Initial API
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.statistics.model;

import java.util.HashMap;
import java.util.Map;

public class TmfStatisticsTreeRootFactory {

    // -----------------------------------------------------------------------
    // Data
    // -----------------------------------------------------------------------
    /**
     * Contains the experiment name as the key and the traces data
     */
    private static final Map<String, AbsTmfStatisticsTree> fTreeInstances = new HashMap<String, AbsTmfStatisticsTree>();

    // -----------------------------------------------------------------------
    // Methods
    // -----------------------------------------------------------------------

    /**
     * Provide a statisticsTree instance per trace
     * 
     * @return the corresponding trace statistics tree
     */
    public static TmfStatisticsTreeNode getStatTreeRoot(String traceUniqueId) {

        AbsTmfStatisticsTree tree = getStatTree(traceUniqueId);
        if (tree == null) {
            return null;
        }
        return tree.getOrCreate(AbsTmfStatisticsTree.ROOT);
    }

    /**
     * 
     * @param traceUniqueId
     * @return the corresponding trace statistics tree
     */
    public static AbsTmfStatisticsTree getStatTree(String traceUniqueId) {
        if (traceUniqueId == null)
            return null;

        AbsTmfStatisticsTree tree = fTreeInstances.get(traceUniqueId);
        return tree;
    }

    /**
     * Add the new trace statistics data in the tree. Can be used later on if the same traces is selected back.
     * 
     * @param traceUniqueId
     *            the name of the trace which will be used as a key to store the data. Must be different for each traces, otherwise the traces might
     *            be overwritten which would trigger a reload of the same trace.
     * @param statsData
     *            the information about the trace
     */
    public static void addStatsTreeRoot(String traceUniqueId, AbsTmfStatisticsTree statsData) {
        if (traceUniqueId == null || statsData == null)
            return;

        fTreeInstances.put(traceUniqueId, statsData);
        // if called for the first time, create the root node
        statsData.getOrCreate(AbsTmfStatisticsTree.ROOT);
    }

    /**
     * 
     * @param traceUniqueId
     * @return true if the trace id is known
     */
    public static boolean containsTreeRoot(String traceUniqueId) {
        return fTreeInstances.containsKey(traceUniqueId);
    }

    /**
     * Remove previously registered statistics tree.
     * 
     * @param traceUniqueId
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
