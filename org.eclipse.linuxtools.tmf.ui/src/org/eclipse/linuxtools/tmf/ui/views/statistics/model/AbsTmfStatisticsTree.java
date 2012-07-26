/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Implementation and Initial API
 *
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.statistics.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.util.TmfFixedArray;
import org.eclipse.linuxtools.tmf.ui.views.statistics.ITmfExtraEventInfo;

/**
 * Base class for the statistics storage. It allow to implement a tree structure
 * while avoiding the need to run through the tree each time you need to add a
 * node at a given place.
 *
 * @version 1.0
 * @author Mathieu Denis
 */
public abstract class AbsTmfStatisticsTree {

    /**
     * String builder used to merge string with more efficiency.
     */
    protected static final StringBuilder fBuilder = new StringBuilder();

    /**
     * Identification of the root.
     */
    public static final TmfFixedArray<String> ROOT = new TmfFixedArray<String>("root"); //$NON-NLS-1$

    /**
     * Function to merge many string with more efficiency.
     *
     * @param strings
     *            Strings to merge.
     * @return A new string containing all the strings.
     */
    public synchronized static String mergeString(String... strings) {
        fBuilder.setLength(0);
        for (String s : strings) {
            fBuilder.append(s);
        }
        return fBuilder.toString();
    }

    /**
     * Define what children a node can have. The management and usage of this map
     * is done by subclasses. HashSet are always faster than TreeSet for String keys.
     */
    protected Map<String, Set<String>> fKeys;

    /**
     * The nodes in the tree.
     */
    protected HashMap<TmfFixedArray<String>, TmfStatisticsTreeNode> fNodes;

    /**
     * Constructor.
     */
    public AbsTmfStatisticsTree() {
        fNodes = new HashMap<TmfFixedArray<String>, TmfStatisticsTreeNode>();
        fKeys = new HashMap<String, Set<String>>();
    }

    /**
     * Get a node.
     *
     * @param path
     *            Path to the node.
     * @return The node or null.
     */
    public TmfStatisticsTreeNode get(final TmfFixedArray<String> path) {
        return fNodes.get(path);
    }

    /**
     * Get the children of a node.
     *
     * @param path
     *            Path to the node.
     * @return Collection containing the children.
     */
    public abstract Collection<TmfStatisticsTreeNode> getChildren(final TmfFixedArray<String> path);

    /**
     * Get every children of a node, even if it doesn't have any registered
     * events, as opposed to getChildren
     *
     * @param path
     *            Path to the node.
     * @return Collection containing all the children.
     */
    public abstract Collection<TmfStatisticsTreeNode> getAllChildren(final TmfFixedArray<String> path);

    /**
     * Get the map of existing elements of path classified by parent.
     *
     * @return The map.
     */
    public Map<String, Set<String>> getKeys() {
        return fKeys;
    }

    /**
     * Get or create a node.
     *
     * @param path
     *            Path to the node.
     * @return The node.
     */
    public TmfStatisticsTreeNode getOrCreate(final TmfFixedArray<String> path) {
        TmfStatisticsTreeNode current = fNodes.get(path);
        if (current == null) {
            registerName(path);
            current = new TmfStatisticsTreeNode(path, this);
            fNodes.put(path, current);
        }
        return current;
    }

    /**
     * Get the parent of a node.
     *
     * @param path
     *            Path to the node.
     * @return Parent node or null.
     */
    public TmfStatisticsTreeNode getParent(final TmfFixedArray<String> path) {
        if (path.size() == 1) {
            if (path.equals(ROOT)) {
                return null;
            }
            return get(ROOT);
        }
        return get(path.subArray(0, path.size() - 1));
    }

    /**
     * Increase any kind of counter.
     *
     * This method must be implemented by subclasses.
     *
     * @param event
     *            Current event.
     * @param extraInfo
     *            Extra information to pass along with the event.
     * @param values
     *            Values desired.
     */
    public abstract void increase(ITmfEvent event, ITmfExtraEventInfo extraInfo, int values);

    /**
     * Register an event.
     *
     * This method must be implemented by subclasses.
     *
     * @param event
     *            Current event.
     * @param extraInfo
     *            Extra information to pass along with the event.
     */
    public abstract void registerEvent(ITmfEvent event, ITmfExtraEventInfo extraInfo);

    /**
     * Register that a new node was created.
     *
     * Must make sure the {@link #getChildren(TmfFixedArray)} on the parent node
     * will return the newly created node.
     *
     * @param path
     *            Path of the new node.
     */
    protected abstract void registerName(final TmfFixedArray<String> path);

    /**
     * Reset a node.
     *
     * Work recursively.
     *
     * @param path
     *            Path to the node.
     */
    public void reset(final TmfFixedArray<String> path) {
        for (TmfStatisticsTreeNode node : getAllChildren(path)) {
            reset(node.getPath());
            fNodes.remove(node.getPath());
        }
    }
}
