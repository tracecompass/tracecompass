/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Implementation and Initial API
 *   Alexandre Montplaisir - Merge TmfBaseStatisticsTree and AbsStatisticsTree
 *                           Move the tree structure logic into the nodes
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.statistics.model;


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

    /** Header for the event type categories. */
    public static final String HEADER_EVENT_TYPES = Messages.TmfStatisticsData_EventTypes;

    /** Root node of this tree */
    private final TmfStatisticsTreeNode rootNode;

    /**
     * Default constructor. Creates base statistics tree for counting total
     * number of events and number of events per event type.
     */
    public TmfStatisticsTree() {
        rootNode = new TmfStatisticsTreeNode(this, null, new String[0]);
    }

    /**
     * Retrieve the root node of this tree.
     *
     * @return The root node
     */
    public TmfStatisticsTreeNode getRootNode() {
        return rootNode;
    }

    /**
     * Get a node.
     *
     * @param path
     *            Path to the node.
     * @return The node, or null if it doesn't current exist in the tree.
     */
    public TmfStatisticsTreeNode getNode(String... path) {
        TmfStatisticsTreeNode curNode = rootNode;
        for (String pathElem : path) {
            curNode = curNode.getChild(pathElem);
            if (curNode == null) {
                /* The requested path doesn't exist, return null */
                break;
            }
        }
        return curNode;
    }

    /**
     * Get or create a node.
     *
     * @param path
     *            Path to the node.
     * @return The requested node. Will be created if it didn't exist.
     */
    public TmfStatisticsTreeNode getOrCreateNode(String... path) {
        TmfStatisticsTreeNode curNode = rootNode;
        TmfStatisticsTreeNode nextNode;
        for (String pathElem : path) {
            nextNode = curNode.getChild(pathElem);
            if (nextNode == null) {
                nextNode = curNode.addChild(pathElem);
            }
            curNode = nextNode;
        }
        return curNode;
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
            getOrCreateNode(path).getValues().setValue(isGlobal, qty);
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
            getOrCreateNode(path).getValues().setValue(isGlobal, qty);
        }
    }

    /**
     * Get the event types paths.
     *
     * @param traceName
     *            The name of the trace (will be used as a sub-tree in the view)
     * @param type
     *            The event type
     * @return Array of arrays representing the paths
     */
    protected String[][] getTypePaths(String traceName, String type) {
        String[][] paths = { new String[] {traceName, HEADER_EVENT_TYPES, type } };
        return paths;
    }

    /**
     * Get the standard paths for an event.
     *
     * @param traceName
     *            The name of the trace (will be used as a sub-tree in the view)
     * @return Array of arrays representing the paths
     */
    protected String[][] getNormalPaths(String traceName) {
        String[][] paths = { new String[] { traceName } };
        return paths;
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
}
