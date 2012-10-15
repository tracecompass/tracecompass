/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Yann N. Dauphin <dhaemon@gmail.com> - Implementation for stats
 *   Francois Godin <copelnug@gmail.com> - Re-design for new stats structure
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Re-design for new stats structure (2)
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.statistics.model;

import java.util.Collection;

/**
 * A tree where nodes can be accessed efficiently using paths.
 *
 * It works like file systems. Each node is identified by a key. A path is an
 * array of String. The elements of the array represent the path from the root
 * to this node.
 *
 * @version 2.0
 * @since 2.0
 * @author Mathieu Denis
 */
public class TmfStatisticsTreeNode {

    /**
     * Value of the node.
     */
    protected TmfStatisticsValues fValues;

    /**
     * Path of the node.
     */
    protected String[] fPath;

    /**
     * Corresponding StatisticsData.
     */
    protected AbsTmfStatisticsTree fNodes;

    /**
     * Constructor.
     *
     * @param path
     *            Path to the node.
     * @param nodes
     *            Corresponding StatisticsData.
     */
    public TmfStatisticsTreeNode(AbsTmfStatisticsTree nodes, final String... path) {
        fPath = path;
        fNodes = nodes;
        fValues = new TmfStatisticsValues();
    }

    /**
     * Test if a node contain the specified child.
     *
     * @param key
     *            Name of the child.
     * @return true: if child with given key is present, false: if no child
     *         exists with given key name
     */
    public boolean containsChild(String key) {
        if (AbsTmfStatisticsTree.ROOT.equals(fPath)) {
            return fNodes.get(key) != null;
        }

        String[] childPath = new String[fPath.length + 1];
        System.arraycopy(fPath, 0, childPath, 0, fPath.length);
        childPath[fPath.length] = key;
        return (fNodes.get(childPath) != null);
    }

    /**
     * Get the children of this node.
     *
     * @return Direct children of this node.
     */
    public Collection<TmfStatisticsTreeNode> getChildren() {
        return fNodes.getChildren(fPath);
    }

    /**
     * Gets every children of this node even if no event has been registered for a node.
     *
     * @return Direct children of this node.
     */
    public Collection<TmfStatisticsTreeNode> getAllChildren() {
        return fNodes.getAllChildren(fPath);
    }

    /**
     * Get the key for this node.
     *
     * @return Key associated with this node.
     */
    public String getKey() {
        return fPath[fPath.length - 1];
    }

    /**
     * Get the number of children this node have.
     *
     * @return Number of direct children of this node.
     */
    public int getNbChildren() {
        return fNodes.getChildren(fPath).size();
    }

    /**
     * Return the parent node.
     *
     * @return Parent node.
     */
    public TmfStatisticsTreeNode getParent() {
        return fNodes.getParent(fPath);
    }

    /**
     * Get the path of the node.
     *
     * @return The path of the node.
     */
    public String[] getPath() {
        return fPath;
    }

    /**
     * Get the value of this node.
     *
     * @return Value associated with this node.
     */
    public TmfStatisticsValues getValues() {
        return fValues;
    }

    /**
     * Indicate if the node have children.
     *
     * @return True if the node has children.
     */
    public boolean hasChildren() {
        return !fNodes.getChildren(fPath).isEmpty();
    }

    /**
     * Start from creation time i.e. keep key and parent but new statistics and
     * no children.
     */
    public void reset() {
        fValues = new TmfStatisticsValues();
        fNodes.reset(fPath);
    }

    /**
     * Resets the global number of events. It doesn't remove any node
     * and doesn't modify the partial event count.
     *
     * Works recursively.
     *
     * @since 2.0
     */
    public void resetGlobalValue() {
        getValues().resetTotalCount();
        fNodes.resetGlobalValue(fPath);
    }

    /**
     * Resets the number of events in the time range. It doesn't remove any node
     * and doesn't modify the global event count.
     *
     * Works recursively.
     *
     * @since 2.0
     */
    public void resetTimeRangeValue() {
        getValues().resetPartialCount();
        fNodes.resetTimeRangeValue(fPath);
    }
}
