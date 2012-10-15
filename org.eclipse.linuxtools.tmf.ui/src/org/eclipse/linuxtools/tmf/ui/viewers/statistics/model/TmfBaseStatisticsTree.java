/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial API and Implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.statistics.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Store information about base statistics data.
 *
 * This class provides a way to represent statistics data that is compatible
 * with every type of trace.
 *
 * @version 2.0
 * @author Mathieu Denis
 * @since 2.0
 */
public class TmfBaseStatisticsTree extends AbsTmfStatisticsTree {

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
     * Default constructor. Creates base statistics tree for counting total
     * number of events and number of events per event type.
     */
    public TmfBaseStatisticsTree() {
        super();
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

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.AbsTmfStatisticsTree#getChildren
     * (org.eclipse.linuxtools.tmf.core.util.TmfFixedArray)
     */
    @Override
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

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.AbsTmfStatisticsTree#getAllChildren
     * (org.eclipse.linuxtools.tmf.core.util.TmfFixedArray)
     */
    @Override
    public Collection<TmfStatisticsTreeNode> getAllChildren(String... path) {
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

    @Override
    public void setTotal(String traceName, boolean isGlobal, long qty) {
        String[][] paths = getNormalPaths(traceName);
        for (String path[] : paths) {
            getOrCreate(path).getValues().setValue(isGlobal, qty);
        }
    }

    @Override
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

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.AbsTmfStatisticsTree#registerName
     * (org.eclipse.linuxtools.tmf.core.util.TmfFixedArray)
     */
    @Override
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
