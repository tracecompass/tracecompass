/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Mathieu Denis      (mathieu.denis@polymtl.ca) - Implementation and Initial API
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
 * <h4>Base class for the statistics storage.</h4>
 * <p>
 * It allow to implement a tree structure while avoiding the need to run through
 * the tree each time you need to add a node at a given place.
 * </p>
 * 
 *  @version 1.0
 *  @author Mathieu Denis
 */
public abstract class AbsTmfStatisticsTree {

    /**
     * <h4>String builder used to merge string with more efficacy.</h4>
     */
    protected static final StringBuilder fBuilder = new StringBuilder();
    /**
     * <h4>Identification of the root.</h4>
     */
    public static final TmfFixedArray<String> ROOT = new TmfFixedArray<String>("root"); //$NON-NLS-1$

    /**
     * <h4>Function to merge many string with more efficacy.</h4>
     * 
     * @param strings
     *            Strings to merge.
     * @return A new string containing all the strings.
     */
    public synchronized static String mergeString(String... strings) {
        fBuilder.setLength(0);
        for (String s : strings)
            fBuilder.append(s);
                return fBuilder.toString();
    }

    /**
     * <h4>Define what child a node can have.</h4>
     * <p>
     * The management and usage of this map is done by subclass.
     * </p>
     * <p>
     * HashSet are always faster than TreeSet.
     * </p>
     */
    protected Map<String, Set<String>> fKeys;
    /**
     * <h4>The nodes in the tree.</f4>
     */
    protected HashMap<TmfFixedArray<String>, TmfStatisticsTreeNode> fNodes;

    /**
     * <h4>Constructor.</h4>
     */
    public AbsTmfStatisticsTree() {
        fNodes = new HashMap<TmfFixedArray<String>, TmfStatisticsTreeNode>();
        fKeys = new HashMap<String, Set<String>>();
    }

    /**
     * <h4>Get a node.</h4>
     * 
     * @param path
     *            Path to the node.
     * @return The node or null.
     */
    public TmfStatisticsTreeNode get(final TmfFixedArray<String> path) {
        return fNodes.get(path);
    }

    /**
     * <h4>Get the children of a node.</h4>
     * 
     * @param path
     *            Path to the node.
     * @return Collection containing the children.
     */
    public abstract Collection<TmfStatisticsTreeNode> getChildren(final TmfFixedArray<String> path);

    /**
     * <h4>Get every children of a node, even if it doesn't have any registered events, as opposed to getChildren</h4>
     * 
     * @param path
     *            Path to the node.
     * @return Collection containing all the children.
     */
    public abstract Collection<TmfStatisticsTreeNode> getAllChildren(final TmfFixedArray<String> path);
    
    /**
     * <h4>Get the map of existing elements of path classified by parent.</h4>
     * 
     * @return The map.
     */
    public Map<String, Set<String>> getKeys() {
        return fKeys;
    }

    /**
     * <h4>Get or create a node.</h4>
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
     * <h4>Get the parent of a node.</h4>
     * 
     * @param path
     *            Path to the node.
     * @return Parent node or null.
     */
    public TmfStatisticsTreeNode getParent(final TmfFixedArray<String> path) {
        if (path.size() == 1) {
            if (path.equals(ROOT))
                return null;
            else
                return get(ROOT);
        }
        return get(path.subArray(0, path.size() - 1));
    }

    /**
     * <h4>Increase any kind of counter.</h4>
     * <p>
     * This method must be implemented by subclass.
     * </p>
     * @param event
     *            Current event.
     * @param extraInfo
     *            Extra information to pass along with the event.
     * @param values
     *            Values desired.
     */
    public abstract void increase(ITmfEvent event, ITmfExtraEventInfo extraInfo, int values);

    /**
     * <h4>Register an event.</h4>
     * <p>
     * This method must be implemented by subclass.
     * </p>
     * @param event
     *            Current event.
     * @param extraInfo
     *            Extra information to pass along with the event.
     */
    public abstract void registerEvent(ITmfEvent event, ITmfExtraEventInfo extraInfo);

    /**
     * <h4>Register that a new node was created.</h4>
     * <p>
     * Must make sure the {@link #getChildren(TmfFixedArray)} on the parent node
     * will return the newly created node.
     * </p>
     * 
     * @param path
     *            Path of the new node.
     */
    protected abstract void registerName(final TmfFixedArray<String> path);

    /**
     * <h4>Reset a node.</h4>
     * <p>
     * Work recursively.
     * </p>
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
