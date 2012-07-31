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
import java.util.Map;
import java.util.Set;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.util.TmfFixedArray;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.ITmfExtraEventInfo;

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
    protected static final String ROOT_NODE_KEY = mergeString(ROOT.get(0), NODE);

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
        keys.put(ROOT.get(0), new HashSet<String>(2)); // 1 trace at the time
        getOrCreate(ROOT);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.AbsTmfStatisticsTree#getChildren
     * (org.eclipse.linuxtools.tmf.core.util.TmfFixedArray)
     */
    @Override
    public Collection<TmfStatisticsTreeNode> getChildren(TmfFixedArray<String> path) {
        LinkedList<TmfStatisticsTreeNode> result = new LinkedList<TmfStatisticsTreeNode>();

        if (path.size() % 2 == 0) { // if we are at a Category
            TmfStatisticsTreeNode current = null;
            for (String value : getKeys().get(path.get(path.size() - 1))) {
                current = get(path.append(value));
                if (current != null && current.getValue().getTotal() != 0) {
                    result.add(current);
                }
            }
        } else if (path.size() == 1) { // Special case.
            if (path.equals(ROOT)) {
                for (String value : getKeys().get(ROOT.get(0))) {
                    result.add(getOrCreate(new TmfFixedArray<String>(value)));
                }
            } else {
                // Get value under the root
                for (String value : getKeys().get(ROOT_NODE_KEY)) {
                    result.add(getOrCreate(path.append(value)));
                }
            }
        } else {// If we are at a value
            for (String value : getKeys().get(mergeString(path.get(path.size() - 2), NODE))) {
                // Search the parent name + NODE
                result.add(getOrCreate(path.append(value)));
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
    public Collection<TmfStatisticsTreeNode> getAllChildren(TmfFixedArray<String> path) {
        LinkedList<TmfStatisticsTreeNode> result = new LinkedList<TmfStatisticsTreeNode>();

        if (path.size() % 2 == 0) { // if we are at a Category
            TmfStatisticsTreeNode current = null;
            for (String value : getKeys().get(path.get(path.size() - 1))) {
                current = get(path.append(value));
                if (current != null) {
                    result.add(current);
                }
            }
        } else if (path.size() == 1) { // Special case.
            if (path.equals(ROOT)) {
                for (String value : getKeys().get(ROOT.get(0))) {
                    result.add(getOrCreate(new TmfFixedArray<String>(value)));
                }
            } else {
                // Get value under the root
                for (String value : getKeys().get(ROOT_NODE_KEY)) {
                    result.add(getOrCreate(path.append(value)));
                }
            }
        } else {// If we are at a value
            for (String value : getKeys().get(mergeString(path.get(path.size() - 2), NODE))) {
                // Search the parent name + NODE
                result.add(getOrCreate(path.append(value)));
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.AbsTmfStatisticsTree#increase
     * (org.eclipse.linuxtools.tmf.core.event.ITmfEvent, org.eclipse.linuxtools.tmf.ui.viewers.statistics.ITmfExtraEventInfo, int)
     */
    @Override
    public void increase(ITmfEvent event, ITmfExtraEventInfo extraInfo, int values) {
        // Do nothing
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.AbsTmfStatisticsTree#registerEvent
     * (org.eclipse.linuxtools.tmf.core.event.ITmfEvent, org.eclipse.linuxtools.tmf.ui.viewers.statistics.ITmfExtraEventInfo)
     */
    @Override
    public void registerEvent(ITmfEvent event, ITmfExtraEventInfo extraInfo) {
        TmfFixedArray<String>[] paths = getNormalPaths(event, extraInfo);
        for (TmfFixedArray<String> path : paths) {
            getOrCreate(path).getValue().incrementTotal();
        }

        paths = getTypePaths(event, extraInfo);
        for (TmfFixedArray<String> path : paths) {
            getOrCreate(path).getValue().incrementTotal();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.AbsTmfStatisticsTree#registerEventInTimeRange
     * (org.eclipse.linuxtools.tmf.core.event.ITmfEvent, org.eclipse.linuxtools.tmf.ui.viewers.statistics.ITmfExtraEventInfo)
     */
    @Override
    public void registerEventInTimeRange(ITmfEvent event, ITmfExtraEventInfo extraInfo) {
        TmfFixedArray<String>[] paths = getNormalPaths(event, extraInfo);
        for (TmfFixedArray<String> path : paths) {
            getOrCreate(path).getValue().incrementPartial();
        }

        paths = getTypePaths(event, extraInfo);
        for (TmfFixedArray<String> path : paths) {
            getOrCreate(path).getValue().incrementPartial();
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected TmfFixedArray<String>[] getTypePaths(ITmfEvent event, ITmfExtraEventInfo extraInfo) {
        String trace = extraInfo.getTraceName();
        // String type = event.getType().getTypeId(); // Add too much
        // informations
        String type = event.getType().toString();

        TmfFixedArray[] paths = { new TmfFixedArray<String>(trace, HEADER_EVENT_TYPES, type) };

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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected TmfFixedArray<String>[] getNormalPaths(ITmfEvent event, ITmfExtraEventInfo extraInfo) {
        String trace = extraInfo.getTraceName();

        TmfFixedArray[] paths = { new TmfFixedArray<String>(trace) };
        return paths;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.AbsTmfStatisticsTree#registerName
     * (org.eclipse.linuxtools.tmf.core.util.TmfFixedArray)
     */
    @Override
    protected void registerName(TmfFixedArray<String> path) {
        if (path.size() == 1) {
            if (!path.equals(ROOT)) {
                getKeys().get(ROOT.get(0)).add(path.get(0));
            }
        } else if (path.size() % 2 != 0) {
            getKeys().get(path.get(path.size() - 2)).add(path.get(path.size() - 1));
        }
    }
}
