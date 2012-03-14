/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Godin (copelnug@gmail.com)  - Initial design and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng.ui.views.statistics.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngTraceState;

/**
 * <h4>Base class for the statistics storage.</h4>
 * <p>
 * It allow to implement a tree structure while avoiding the need to run through
 * the tree each time you need to add a node at a given place.
 * </p>
 */
public abstract class StatisticsData {
    /**
     * <h4>Define values that can be used like a C++ enumeration.</h4>
     * <p>
     * The values can be used with binary "or" and "and" to mix them.
     * </p>
     */
    public static class Values {
        /**
         * <h4>Indicate the cpu time</h4>
         * <p>
         * The actual time the cpu as passed in this state making calculations.
         * </p>
         */
        public static final int CPU_TIME = 1;
        /**
         * <h4>Indicate the cumulative cpu time</h4>
         * <p>
         * Include the time the cpu as passed in this state and substate.
         * </p>
         * <p>
         * Example:
         * <ul>
         * <li>PID:1, Mode:USER_MODE</li>
         * <ul>
         * <li>PID:1, Mode:SYSCALL</li>
         * </ul>
         * <li>PID:2, Mode:USER_MODE</li>
         * </ul>
         * </p>
         * <p>
         * In this example, the cumulative cpu time for "PID:1, Mode:USER_MODE"
         * would be equal to its cpu time plus the cpu time of
         * "PID:1, Mode:SYSCALL".
         * </p>
         * TODO Validate values. Not tested in LTTv. TODO Validate description.
         */
        public static final int CUMULATIVE_CPU_TIME = 2;
        /**
         * <h4>Elapsed time</h4>
         * <p>
         * Description...
         * </p>
         * TODO Give a correct description.
         */
        public static final int ELAPSED_TIME = 4;
        /**
         * <h4>State cumulative cpu time</h4>
         * <p>
         * Description...
         * </p>
         * TODO Give a correct description.
         */
        public static final int STATE_CUMULATIVE_CPU_TIME = 8;
    }

    /**
     * <h4>String builder used to merge string with more efficacy.</h4>
     */
    protected static final StringBuilder fBuilder = new StringBuilder();
    /**
     * <h4>Identification of the root.</h4>
     */
    public static final FixedArray ROOT = new FixedArray(-1);

    /**
     * <h4>Function to merge many string with more efficacy.</h4>
     * 
     * @param strings
     *            Strings to merge.
     * @return A new string containing all the strings.
     */
    protected synchronized static String mergeString(String... strings) {
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
    private Map<Integer, Set<Integer>> fKeys;
    /**
     * <h4>The nodes in the tree.</f4>
     */
    private HashMap<FixedArray, StatisticsTreeNode> fNodes;

    /**
     * <h4>Constructor.</h4>
     */
    public StatisticsData() {
        fNodes = new HashMap<FixedArray, StatisticsTreeNode>();
        fKeys = new HashMap<Integer, Set<Integer>>();
    }

    /**
     * <h4>Indicate the end of the traceset</4>
     * <p>
     * Can be used to trigger necessary calculations.
     * </p>
     * 
     * @param event
     *            Event receive (May have timestamp of 0).
     * @param traceState
     *            State of the trace at that moment.
     */
    public abstract void endTraceset(LttngEvent event, LttngTraceState traceState);

    /**
     * <h4>Get a node.</h4>
     * 
     * @param path
     *            Path to the node.
     * @return The node or null.
     */
    public StatisticsTreeNode get(final FixedArray path) {
        return fNodes.get(path);
    }

    /**
     * <h4>Put a node.</h4>
     * 
     * @param path
     *            Path to the node.
     * @param node
     *            Node to put.
     * @return node if replaced.
     */
    public StatisticsTreeNode put(final FixedArray path, StatisticsTreeNode node) {
        return fNodes.put(path, node);
    }

    /**
     * <h4>Get the children of a node.</h4>
     * 
     * @param path
     *            Path to the node.
     * @return Collection containing the children.
     */
    public abstract Collection<StatisticsTreeNode> getChildren(final FixedArray path);

    /**
     * <h4>Get the map of existing elements of path classified by parent.</h4>
     * 
     * @return The map.
     */
    protected Map<Integer, Set<Integer>> getKeys() {
        return fKeys;
    }

    /**
     * <h4>Get or create a node.</h4>
     * 
     * @param path
     *            Path to the node.
     * @return The node.
     */
    public StatisticsTreeNode getOrCreate(final FixedArray path) {
        StatisticsTreeNode current = fNodes.get(path);
        if (current == null) {
            registerName(path);
            current = new StatisticsTreeNode(path, this);
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
    public StatisticsTreeNode getParent(final FixedArray path) {
        if (path.size() == 1) {
            if (path.equals(ROOT))
                return null;
            else
                return get(ROOT);
        }
        // TODO Get or GetOrCreate?
        return get(path.subArray(0, path.size() - 1));
    }

    /**
     * <h4>Increase some values.</h4>
     * <p>
     * Values is an binary or operation on the desired values between
     * {@link Values#CPU_TIME}, {@link Values#CUMULATIVE_CPU_TIME},
     * {@link Values#ELAPSED_TIME} and {@link Values#STATE_CUMULATIVE_CPU_TIME}
     * .
     * 
     * @param event
     *            Current event.
     * @param traceState
     *            State of the trace at that moment.
     * @param values
     *            Values desired.
     */
    public abstract void increase(LttngEvent event, LttngTraceState traceState, int values);

    /**
     * <h4>Register an event.</h4>
     * <p>
     * This method must be implemented by subclass.
     * </p>
     * 
     * @param event
     *            Event to process.
     * @param traceState
     *            State of the trace at the moment of the event.
     */
    public abstract void registerEvent(LttngEvent event, LttngTraceState traceState);

    /**
     * <h4>Register that a new node was created.</h4>
     * <p>
     * Must make sure the {@link #getChildren(FixedArray)} on the parent node
     * will return the newly created node.
     * </p>
     * 
     * @param path
     *            Path of the new node.
     */
    protected abstract void registerName(final FixedArray path);

    /**
     * <h4>Reset a node.</h4>
     * <p>
     * Work recursively.
     * </p>
     * 
     * @param path
     *            Path to the node.
     */
    public void reset(final FixedArray path) {
        for (StatisticsTreeNode node : getChildren(path)) {
            reset(node.getPath());
            fNodes.remove(node.getPath());
        }
    }

    /**
     * Indicate that the process is finishing.
     * 
     * @param event
     *            The event indicating the end of the process.
     * @param traceState
     *            State of the trace at that moment.
     */
    public abstract void process_exit(LttngEvent event, LttngTraceState traceState);
}