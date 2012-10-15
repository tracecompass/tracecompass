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

package org.eclipse.linuxtools.tmf.ui.viewers.statistics.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base class for the statistics storage. It allow to implement a tree structure
 * while avoiding the need to run through the tree each time you need to add a
 * node at a given place.
 *
 * @author Mathieu Denis
 * @version 2.0
 * @since 2.0
 */
public class TmfStatisticsTree {

    /**
     * Identification of the root.
     */
    public static final String[] ROOT = new String[] { "root" }; //$NON-NLS-1$

    /**
     * Header for the event type categories.
     */
    public static final String HEADER_EVENT_TYPES = Messages.TmfStatisticsData_EventTypes;

    /**
     * Indicate that it's a value.
     *
     * Used when checking the possible child node for a node.
     *
     * It differentiate a category of a value by being appended to a value.
     */
    protected static final String NODE = "z"; //$NON-NLS-1$

    /**
     * Root node key.
     */
    protected static final String ROOT_NODE_KEY = mergeString(ROOT[0], NODE);

    /**
     * Define what children a node can have. The management and usage of this map
     * is done by subclasses. HashSet are always faster than TreeSet for String keys.
     */
    protected Map<String, Set<String>> fKeys;

    /**
     * The nodes in the tree.
     */
    protected Map<List<String>, TmfStatisticsTreeNode> fNodes;

    /**
     * Default constructor. Creates base statistics tree for counting total
     * number of events and number of events per event type.
     */
    public TmfStatisticsTree() {
        fNodes = new HashMap<List<String>, TmfStatisticsTreeNode>();
        fKeys = new HashMap<String, Set<String>>();

        Map<String, Set<String>> keys = getKeys();

        // //////////// Adding category sets
        // common
        keys.put(HEADER_EVENT_TYPES, new HashSet<String>());

        // /////////// Adding value sets
        // Under a trace
        Set<String> temp = new HashSet<String>(8);
        temp.add(HEADER_EVENT_TYPES);
        keys.put(ROOT_NODE_KEY, temp);
        // Under an event type
        temp = new HashSet<String>(16);
        keys.put(mergeString(HEADER_EVENT_TYPES, NODE), temp);

        // //////////// CREATE root
        keys.put(ROOT[0], new HashSet<String>(2)); // 1 trace at the time
        getOrCreate(ROOT);
    }

    /**
     * Get a node.
     *
     * @param path
     *            Path to the node.
     * @return The node or null.
     */
    public TmfStatisticsTreeNode get(String... path) {
        List<String> pathAsList = Arrays.asList(path);
        return fNodes.get(pathAsList);
    }

    /**
     * Get the children of a node.
     *
     * @param path
     *            Path to the node.
     * @return Collection containing the children.
     */
    public List<TmfStatisticsTreeNode> getChildren(String... path) {
        List<TmfStatisticsTreeNode> result = new LinkedList<TmfStatisticsTreeNode>();

        if (path.length % 2 == 0) { // if we are at a Category
            TmfStatisticsTreeNode current = null;
            for (String value : getKeys().get(path[path.length - 1])) {
                current = get(addToArray(path, value));
                if (current != null) {
                    if (current.getValues().getTotal() > 0 || current.getValues().getPartial() > 0) {
                        result.add(current);
                    }
                }
            }
        } else if (path.length == 1) { // Special case.
            if (path.equals(ROOT)) {
                for (String value : getKeys().get(ROOT[0])) {
                    result.add(getOrCreate(value));
                }
            } else {
                // Get value under the root
                for (String value : getKeys().get(ROOT_NODE_KEY)) {
                    result.add(getOrCreate(addToArray(path, value)));
                }
            }
        } else {// If we are at a value
            for (String value : getKeys().get(mergeString(path[path.length - 2], NODE))) {
                // Search the parent name + NODE
                result.add(getOrCreate(addToArray(path, value)));
            }
        }

        return result;
    }

    /**
     * Get every children of a node, even if it doesn't have any registered
     * events, as opposed to getChildren
     *
     * @param path
     *            Path to the node.
     * @return Collection containing all the children.
     */
    public List<TmfStatisticsTreeNode> getAllChildren(String... path) {
        LinkedList<TmfStatisticsTreeNode> result = new LinkedList<TmfStatisticsTreeNode>();

        if (path.length % 2 == 0) { // if we are at a Category
            TmfStatisticsTreeNode current = null;
            for (String value : getKeys().get(path[path.length - 1])) {
                current = get(addToArray(path, value));
                if (current != null) {
                    result.add(current);
                }
            }
        } else if (path.length == 1) { // Special case.
            if (path.equals(ROOT)) {
                for (String value : getKeys().get(ROOT[0])) {
                    result.add(getOrCreate(value));
                }
            } else {
                // Get value under the root
                for (String value : getKeys().get(ROOT_NODE_KEY)) {
                    result.add(getOrCreate(addToArray(path, value)));
                }
            }
        } else {// If we are at a value
            for (String value : getKeys().get(mergeString(path[path.length - 2], NODE))) {
                // Search the parent name + NODE
                result.add(getOrCreate(addToArray(path, value)));
            }
        }
        return result;
    }

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
    public TmfStatisticsTreeNode getOrCreate(String... path) {
        List<String> pathAsList = Arrays.asList(path);
        TmfStatisticsTreeNode current = fNodes.get(pathAsList);

        if (current == null) {
            registerName(path);
            current = new TmfStatisticsTreeNode(this, path);
            fNodes.put(pathAsList, current);
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
    public TmfStatisticsTreeNode getParent(final String... path) {
        if (path.length == 1) {
            if (path.equals(ROOT)) {
                return null;
            }
            return get(ROOT);
        }

        String[] parentPath = new String[path.length - 1];
        System.arraycopy(path, 0, parentPath, 0, parentPath.length);
        return get(parentPath);
    }

    /**
     * Set the value to display in the "total" cells. This means the row
     * indicating the total count of events for a trace.
     *
     * @param traceName
     *            The name of the trace (will be used as a sub-tree in the view)
     * @param isGlobal
     *            Is this a for a global or a time range request? Determines if
     *            this goes in the Global column or the Selected Time Range one.
     * @param qty
     *            The value to display
     */
    public void setTotal(String traceName, boolean isGlobal, long qty) {
        String[][] paths = getNormalPaths(traceName);
        for (String path[] : paths) {
            getOrCreate(path).getValues().setValue(isGlobal, qty);
        }
    }

    /**
     * Set the value to display in the "Type count" cells. These are the counts
     * for each event types.
     *
     * @param traceName
     *            The name of the trace (will be used as a sub-tree in the view)
     * @param type
     *            The event type
     * @param isGlobal
     *            Is this a for a global or a time range request? Determines if
     *            this goes in the Global column or the Selected Time Range one.
     * @param qty
     *            The value to display
     */
    public void setTypeCount(String traceName, String type, boolean isGlobal,  long qty) {
        String[][] paths = getTypePaths(traceName, type);
        for (String[] path : paths) {
            getOrCreate(path).getValues().setValue(isGlobal, qty);
        }
    }

    /**
     * Get the event types paths.
     *
     * @param event
     *            Event to get the path for.
     * @param extraInfo
     *            Extra information to pass along with the event
     * @return Array of FixedArray representing the paths.
     */
    protected String[][] getTypePaths(String traceName, String type) {
        String[][] paths = { new String[] {traceName, HEADER_EVENT_TYPES, type } };
        return paths;
    }

    /**
     * Get the standard paths for an event.
     *
     * @param event
     *            Event to get the path for.
     * @param extraInfo
     *            Extra information to pass along with the event
     * @return Array of FixedArray representing the paths.
     */
    protected String[][] getNormalPaths(String traceName) {
        String[][] paths = { new String[] { traceName } };
        return paths;
    }

    /**
     * Register that a new node was created.
     *
     * Must make sure the {@link #getChildren(TmfFixedArray)} on the parent node
     * will return the newly created node.
     *
     * @param path
     *            Path of the new node.
     */
    protected void registerName(String... path) {
        if (path.length == 1) {
            if (!path.equals(ROOT)) {
                getKeys().get(ROOT[0]).add(path[0]);
            }
        } else if (path.length % 2 != 0) {
            getKeys().get(path[path.length - 2]).add(path[path.length - 1]);
        }
    }

    /**
     * Resets a node.
     *
     * Works recursively.
     *
     * @param path
     *            Path to the node.
     */
    public void reset(final String... path) {
        for (TmfStatisticsTreeNode node : getAllChildren(path)) {
            reset(node.getPath());
            List<String> nodePathList = Arrays.asList(node.getPath());
            fNodes.remove(nodePathList);
        }
    }

    /**
     * Reset the global value of a node.
     *
     * Works recursively.
     *
     * @param path
     *            Path to the node.
     * @since 2.0
     */
    public void resetGlobalValue(final String... path) {
        for (TmfStatisticsTreeNode node : getChildren(path)) {
            node.resetGlobalValue();
        }
    }

    /**
     * Reset the time range value of a node.
     *
     * Works recursively.
     *
     * @param path
     *            Path to the node.
     * @since 2.0
     */
    public void resetTimeRangeValue(final String... path) {
        for (TmfStatisticsTreeNode node : getChildren(path)) {
            node.resetTimeRangeValue();
        }
    }

    /**
     * Function to merge many string more efficiently.
     *
     * @param strings
     *            Strings to merge.
     * @return A new string containing all the strings.
     */
    protected static String mergeString(String... strings) {
        StringBuilder builder = new StringBuilder();
        for (String s : strings) {
            builder.append(s);
        }
        return builder.toString();
    }

    /**
     * Return a new array that's a copy of the old one, plus 'newElem' added at
     * the end.
     */
    private static String[] addToArray(String[] array, String newElem) {
        String[] newArray = new String[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = newElem;
        return newArray;
    }
}
