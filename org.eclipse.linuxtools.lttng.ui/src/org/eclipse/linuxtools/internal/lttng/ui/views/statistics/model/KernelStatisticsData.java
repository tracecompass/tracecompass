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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.eclipse.linuxtools.internal.lttng.core.LttngConstants;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.Events;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.ExecutionMode;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.ProcessStatus;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngProcessState;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngTraceState;

/**
 * <h4>Class that process the LTTng kernel events.</h4>
 */
public class KernelStatisticsData extends StatisticsData {
    /**
     * <h4>Header for the CPU categories.</h4>
     */
    public static final String HEADER_CPUS = Messages.KernelStatisticsData_CPUs;
    public static final int HEADER_CPUS_INT = 256 | LttngConstants.STATS_CATEGORY_ID;
    /**
     * <h4>Header for the event types categories.</h4>
     */
    public static final String HEADER_EVENT_TYPES = Messages.KernelStatisticsData_EventTypes;
    public static final int HEADER_EVENT_TYPES_INT = (HEADER_CPUS_INT + 1) | LttngConstants.STATS_CATEGORY_ID;
    /**
     * <h4>Header for the function categories.</h4>
     */
    public static final String HEADER_FUNCTIONS = Messages.KernelStatisticsData_Functions;
    public static final int HEADER_FUNCTIONS_INT = (HEADER_EVENT_TYPES_INT + 1) | LttngConstants.STATS_CATEGORY_ID;
    /**
     * <h4>Header for the mode categories.</h4>
     */
    public static final String HEADER_MODES = Messages.KernelStatisticsData_Modes;
    public static final int HEADER_MODES_INT = (HEADER_FUNCTIONS_INT + 1) | LttngConstants.STATS_CATEGORY_ID;
    /**
     * <h4>Header for the processes categories.</h4>
     */
    public static final String HEADER_PROCESSES = Messages.KernelStatisticsData_Processes;
    public static final int HEADER_PROCESSES_INT = (HEADER_MODES_INT + 1) | LttngConstants.STATS_CATEGORY_ID;
    /**
     * <h4>Header for the submode categories.</h4>
     */
    public static final String HEADER_SUBMODES = Messages.KernelStatisticsData_SubModes;
    public static final int HEADER_SUBMODES_INT = (HEADER_PROCESSES_INT + 1) | LttngConstants.STATS_CATEGORY_ID;

    /**
     * <h4>Class to generate unique IDs for processes.</h4>
     */
    private ProcessKeyProvider fPidKeys = new ProcessKeyProvider(LttngConstants.STATS_PROCESS_ID);

    /**
     * <h4>Class to generate unique Ids for event types.</h4>
     */
    private KeyProvider fTypeKeys = new KeyProvider(LttngConstants.STATS_TYPE_ID);
    /**
     * <h4>Class to generate unique Ids for subModes.</h4>
     */
    private KeyProvider fSubModeKeys = new KeyProvider();

    /**
     * <h4>Place Holder in path.</h4>
     */
    private static final int PLACE_HOLDER = 0;

    /**
     * For performance reason the following algorithm is applied to the paths:
     * 
     * Each array entry has to be unique to form a unique path. To generate
     * unique entries a bit mask is used, where the bit mask is applied to the
     * upper N bits of an integer value. It is assumed, that each value that
     * will be filled in the place holder below is smaller than 2 ^ (32 - N).
     */

    /**
     * <h4>Pre-created paths for type statistics, which will be filled for each
     * relevant event.</h4>
     */
    private final FixedArray[] fTypedPaths = { new FixedArray(PLACE_HOLDER, HEADER_EVENT_TYPES_INT, PLACE_HOLDER), new FixedArray(PLACE_HOLDER, HEADER_MODES_INT, PLACE_HOLDER, HEADER_EVENT_TYPES_INT, PLACE_HOLDER),
            new FixedArray(PLACE_HOLDER, HEADER_MODES_INT, PLACE_HOLDER, HEADER_SUBMODES_INT, PLACE_HOLDER, HEADER_EVENT_TYPES_INT, PLACE_HOLDER),
            new FixedArray(PLACE_HOLDER, HEADER_PROCESSES_INT, PLACE_HOLDER, HEADER_EVENT_TYPES_INT, PLACE_HOLDER),
            new FixedArray(PLACE_HOLDER, HEADER_PROCESSES_INT, PLACE_HOLDER, HEADER_CPUS_INT, PLACE_HOLDER, HEADER_EVENT_TYPES_INT, PLACE_HOLDER),
            new FixedArray(PLACE_HOLDER, HEADER_PROCESSES_INT, PLACE_HOLDER, HEADER_CPUS_INT, PLACE_HOLDER, HEADER_FUNCTIONS_INT, PLACE_HOLDER, HEADER_EVENT_TYPES_INT, PLACE_HOLDER),
            new FixedArray(PLACE_HOLDER, HEADER_PROCESSES_INT, PLACE_HOLDER, HEADER_CPUS_INT, PLACE_HOLDER, HEADER_FUNCTIONS_INT, PLACE_HOLDER, HEADER_MODES_INT, PLACE_HOLDER, HEADER_EVENT_TYPES_INT, PLACE_HOLDER),
            new FixedArray(PLACE_HOLDER, HEADER_PROCESSES_INT, PLACE_HOLDER, HEADER_CPUS_INT, PLACE_HOLDER, HEADER_FUNCTIONS_INT, PLACE_HOLDER, HEADER_MODES_INT, PLACE_HOLDER, HEADER_SUBMODES_INT, PLACE_HOLDER, HEADER_EVENT_TYPES_INT, PLACE_HOLDER),
            new FixedArray(PLACE_HOLDER, HEADER_PROCESSES_INT, PLACE_HOLDER, HEADER_CPUS_INT, PLACE_HOLDER, HEADER_MODES_INT, PLACE_HOLDER, HEADER_EVENT_TYPES_INT, PLACE_HOLDER),
            new FixedArray(PLACE_HOLDER, HEADER_PROCESSES_INT, PLACE_HOLDER, HEADER_CPUS_INT, PLACE_HOLDER, HEADER_MODES_INT, PLACE_HOLDER, HEADER_SUBMODES_INT, PLACE_HOLDER, HEADER_EVENT_TYPES_INT, PLACE_HOLDER),
            new FixedArray(PLACE_HOLDER, HEADER_PROCESSES_INT, PLACE_HOLDER, HEADER_MODES_INT, PLACE_HOLDER, HEADER_EVENT_TYPES_INT, PLACE_HOLDER),
            new FixedArray(PLACE_HOLDER, HEADER_PROCESSES_INT, PLACE_HOLDER, HEADER_MODES_INT, PLACE_HOLDER, HEADER_SUBMODES_INT, PLACE_HOLDER, HEADER_EVENT_TYPES_INT, PLACE_HOLDER),
            new FixedArray(PLACE_HOLDER, HEADER_CPUS_INT, PLACE_HOLDER, HEADER_EVENT_TYPES_INT, PLACE_HOLDER), new FixedArray(PLACE_HOLDER, HEADER_CPUS_INT, PLACE_HOLDER, HEADER_MODES_INT, PLACE_HOLDER, HEADER_EVENT_TYPES_INT, PLACE_HOLDER),
            new FixedArray(PLACE_HOLDER, HEADER_CPUS_INT, PLACE_HOLDER, HEADER_MODES_INT, PLACE_HOLDER, HEADER_SUBMODES_INT, PLACE_HOLDER, HEADER_EVENT_TYPES_INT, PLACE_HOLDER), };
    /**
     * <h4>Pre-created paths for other statistics, which will be filled for each
     * relevant event.</h4>
     */
    final FixedArray[] fNormalPaths = { new FixedArray(PLACE_HOLDER), new FixedArray(PLACE_HOLDER, HEADER_MODES_INT, PLACE_HOLDER), new FixedArray(PLACE_HOLDER, HEADER_MODES_INT, PLACE_HOLDER, HEADER_SUBMODES_INT, PLACE_HOLDER),
            new FixedArray(PLACE_HOLDER, HEADER_PROCESSES_INT, PLACE_HOLDER), new FixedArray(PLACE_HOLDER, HEADER_PROCESSES_INT, PLACE_HOLDER, HEADER_CPUS_INT, PLACE_HOLDER),
            new FixedArray(PLACE_HOLDER, HEADER_PROCESSES_INT, PLACE_HOLDER, HEADER_CPUS_INT, PLACE_HOLDER, HEADER_FUNCTIONS_INT, PLACE_HOLDER),
            new FixedArray(PLACE_HOLDER, HEADER_PROCESSES_INT, PLACE_HOLDER, HEADER_CPUS_INT, PLACE_HOLDER, HEADER_FUNCTIONS_INT, PLACE_HOLDER, HEADER_MODES_INT, PLACE_HOLDER),
            new FixedArray(PLACE_HOLDER, HEADER_PROCESSES_INT, PLACE_HOLDER, HEADER_CPUS_INT, PLACE_HOLDER, HEADER_FUNCTIONS_INT, PLACE_HOLDER, HEADER_MODES_INT, PLACE_HOLDER, HEADER_SUBMODES_INT, PLACE_HOLDER),
            new FixedArray(PLACE_HOLDER, HEADER_PROCESSES_INT, PLACE_HOLDER, HEADER_CPUS_INT, PLACE_HOLDER, HEADER_MODES_INT, PLACE_HOLDER),
            new FixedArray(PLACE_HOLDER, HEADER_PROCESSES_INT, PLACE_HOLDER, HEADER_CPUS_INT, PLACE_HOLDER, HEADER_MODES_INT, PLACE_HOLDER, HEADER_SUBMODES_INT, PLACE_HOLDER),
            new FixedArray(PLACE_HOLDER, HEADER_PROCESSES_INT, PLACE_HOLDER, HEADER_MODES_INT, PLACE_HOLDER), new FixedArray(PLACE_HOLDER, HEADER_PROCESSES_INT, PLACE_HOLDER, HEADER_MODES_INT, PLACE_HOLDER, HEADER_SUBMODES_INT, PLACE_HOLDER),
            new FixedArray(PLACE_HOLDER, HEADER_CPUS_INT, PLACE_HOLDER), new FixedArray(PLACE_HOLDER, HEADER_CPUS_INT, PLACE_HOLDER, HEADER_MODES_INT, PLACE_HOLDER),
            new FixedArray(PLACE_HOLDER, HEADER_CPUS_INT, PLACE_HOLDER, HEADER_MODES_INT, PLACE_HOLDER, HEADER_SUBMODES_INT, PLACE_HOLDER), };

    /**
     * <h4>Indicate that it's a value.</h4>
     * <p>
     * Used when checking the possible child node for a node.
     * </p>
     * <p>
     * It differentiate a category of a value by being appended to a value.
     * </p>
     */
    private static final Integer NODE = -1;
    private static final Integer ROOT_NODE_KEY = -2;

    /**
     * <h4>Constructor.</h4>
     * 
     * @param traceName
     */
    public KernelStatisticsData(String traceName) {
        super();
        Map<Integer, Set<Integer>> keys = getKeys();

        // //////////// Adding category sets
        keys.put(HEADER_PROCESSES_INT, new HashSet<Integer>());
        keys.put(HEADER_MODES_INT, new HashSet<Integer>());
        keys.put(HEADER_CPUS_INT, new HashSet<Integer>(4)); // Over 4 CPUs is
                                                            // not common
        keys.put(HEADER_SUBMODES_INT, new HashSet<Integer>());
        keys.put(HEADER_EVENT_TYPES_INT, new HashSet<Integer>());
        keys.put(HEADER_FUNCTIONS_INT, new HashSet<Integer>(4)); // Seems to be
                                                                 // always one.

        // /////////// Adding value sets
        // Under a trace
        Set<Integer> temp = new HashSet<Integer>(8);
        temp.add(HEADER_PROCESSES_INT);
        temp.add(HEADER_MODES_INT);
        temp.add(HEADER_CPUS_INT);
        temp.add(HEADER_EVENT_TYPES_INT);
        keys.put(ROOT_NODE_KEY, temp);
        // Under a process
        temp = new HashSet<Integer>(4);
        temp.add(HEADER_MODES_INT);
        temp.add(HEADER_CPUS_INT);
        temp.add(HEADER_EVENT_TYPES_INT);
        keys.put(HEADER_PROCESSES_INT * NODE, temp);
        // Under a CPUs : Functions is a special case
        temp = new HashSet<Integer>(4);
        temp.add(HEADER_MODES_INT);
        temp.add(HEADER_EVENT_TYPES_INT);
        keys.put(HEADER_CPUS_INT * NODE, temp);
        // Under a functions
        temp = new HashSet<Integer>(4);
        temp.add(HEADER_MODES_INT);
        temp.add(HEADER_EVENT_TYPES_INT);
        keys.put(HEADER_FUNCTIONS_INT * NODE, temp);
        // Under a mode
        temp = new HashSet<Integer>(4);
        temp.add(HEADER_SUBMODES_INT);
        temp.add(HEADER_EVENT_TYPES_INT);
        keys.put(HEADER_MODES_INT * NODE, temp);
        // Under a submodes
        temp = new HashSet<Integer>(2);
        temp.add(HEADER_EVENT_TYPES_INT);
        keys.put(HEADER_SUBMODES_INT * NODE, temp);
        // Under an event type
        temp = new HashSet<Integer>(16);
        keys.put(HEADER_EVENT_TYPES_INT * NODE, temp);

        // //////////// CREATE root
        keys.put(ROOT.get(0), new HashSet<Integer>(2)); // 1 trace at the time
        StatisticsTreeNode node = getOrCreate(ROOT);
        node.setName("root"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.linuxtools.lttng.ui.views.statistics.model.StatisticsData
     * #endTraceset(org.eclipse.linuxtools.lttng.event.LttngEvent,
     * org.eclipse.linuxtools.lttng.state.model.LttngTraceState)
     */
    @Override
    public void endTraceset(LttngEvent event, LttngTraceState traceState) {
        // TODO Should we uncomment the rest?
        // It include in the cpu time the time between the last event of each
        // cpu and the time of the last global event.
        // Because we know that there won't be a change of mode or process
        // between those time.
        /*
         * if(last_ == null) return;
         * 
         * LttngProcessState process = traceState.getRunning_process().get(0L);
         * System.out.println(process.getState().getChange_LttTime()); for(long
         * cpu : traceState.getRunning_process().keySet()) { LttngEventType
         * newType = new LttngEventType(last_.getType().getTracefileName(), cpu,
         * last_.getType().getMarkerName(), last_.getType().getLabels());
         * last_.setType(newType); increase(last_, traceState, Values.CPU_TIME |
         * Values.CUMULATIVE_CPU_TIME | Values.ELAPSED_TIME |
         * Values.STATE_CUMULATIVE_CPU_TIME); // TODO Are all those values
         * required? }
         */
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.linuxtools.lttng.ui.views.statistics.model.StatisticsData
     * #getChildren
     * (org.eclipse.linuxtools.lttng.ui.views.statistics.model.FixedArray)
     */
    @Override
    public Collection<StatisticsTreeNode> getChildren(final FixedArray path) {
        LinkedList<StatisticsTreeNode> result = new LinkedList<StatisticsTreeNode>();

        if (path.size() % 2 == 0) { // if we are at a Category
            StatisticsTreeNode current = null;
            for (int value : getKeys().get(path.get(path.size() - 1))) {
                current = get(path.append(value));
                if (current != null && current.getValue().nbEvents != 0)
                    result.add(current);
            }
        } else if (path.size() == 1) { // Special case.
            if (path.equals(ROOT)) // Asking for the root.
                for (int value : getKeys().get(ROOT.get(0)))
                    result.add(getOrCreate(new FixedArray(value)));
            else
                // Get value under the root
                for (int value : getKeys().get(ROOT_NODE_KEY)) {
                    StatisticsTreeNode node = getOrCreate(path.append(value));
                    node.setName(getCategoryFromId(value));
                    result.add(node);
                }
        } else {// If we are at a value
            for (int value : getKeys().get((path.get(path.size() - 2) * NODE))) { // Search
                                                                                  // the
                                                                                  // parent
                                                                                  // name
                                                                                  // +
                                                                                  // NODE
                StatisticsTreeNode node = getOrCreate(path.append(value));
                node.setName(getCategoryFromId(value));
                result.add(node);
            }

            if (path.size() == 5 && path.get(3) == HEADER_CPUS_INT) { // Special
                                                                      // the
                                                                      // Functions
                                                                      // is just
                                                                      // there.
                                                                      // We want
                                                                      // the
                                                                      // fourth
                                                                      // element
                                                                      // but it
                                                                      // start
                                                                      // at 0.
                                                                      // So #3
                StatisticsTreeNode node = getOrCreate(path.append(HEADER_FUNCTIONS_INT));
                node.setName(getCategoryFromId(HEADER_FUNCTIONS_INT));
                result.add(node);
            }
        }

        return result;
    }

    /**
     * <h4>Get the standard paths for an event.</h4>
     * 
     * @param event
     *            Event to get the path for.
     * @param traceState
     *            State of the trace for this event.
     * @return Array of FixedArray representing the paths.
     */
    private FixedArray[] getNormalPaths(LttngEvent event, LttngTraceState traceState) {
        int trace = (int) traceState.getContext().getIdentifier(); // No need
                                                                   // for the
                                                                   // identifier
                                                                   // (already
                                                                   // applied)
        Long cpu = event.getCpuId();
        int cpus = cpu.intValue() | LttngConstants.STATS_CPU_ID;
        LttngProcessState process = traceState.getRunning_process().get(cpu);
        int processName = fPidKeys.getUniqueId(process.getPid().intValue(), process.getCpu().intValue(), process.getCreation_time());
        int mode = process.getState().getExec_mode().ordinal() | LttngConstants.STATS_MODE_ID;
        int submode = fSubModeKeys.getUniqueId(process.getState().getExec_submode_id(), process.getState().getExec_submode());
        int function = process.getCurrent_function().intValue() | LttngConstants.STATS_FUNCTION_ID;

        /*
         * Note that it's faster to re-use the path object, set the relevant
         * fields and clone the path later when it's time to add to the map
         */

        // FixedArray(trace)
        fNormalPaths[0].set(0, trace);

        // FixedArray(trace,HEADER_MODES_INT,mode)
        fNormalPaths[1].set(0, trace);
        fNormalPaths[1].set(2, mode);

        // FixedArray(trace,HEADER_MODES_INT,mode,HEADER_SUBMODES_INT,submode)
        fNormalPaths[2].set(0, trace);
        fNormalPaths[2].set(2, mode);
        fNormalPaths[2].set(4, submode);

        // FixedArray(trace,HEADER_PROCESSES_INT,processName)
        fNormalPaths[3].set(0, trace);
        fNormalPaths[3].set(2, processName);

        // FixedArray(trace,HEADER_PROCESSES_INT,processName,HEADER_CPUS_INT,cpus)
        fNormalPaths[4].set(0, trace);
        fNormalPaths[4].set(2, processName);
        fNormalPaths[4].set(4, cpus);

        // FixedArray(trace,HEADER_PROCESSES_INT,processName,HEADER_CPUS_INT,cpus,HEADER_FUNCTIONS_INT,function)
        fNormalPaths[5].set(0, trace);
        fNormalPaths[5].set(2, processName);
        fNormalPaths[5].set(4, cpus);
        fNormalPaths[5].set(6, function);

        // FixedArray(trace,HEADER_PROCESSES_INT,processName,HEADER_CPUS_INT,cpus,HEADER_FUNCTIONS_INT,function,
        // HEADER_MODES_INT, mode)
        fNormalPaths[6].set(0, trace);
        fNormalPaths[6].set(2, processName);
        fNormalPaths[6].set(4, cpus);
        fNormalPaths[6].set(6, function);
        fNormalPaths[6].set(8, mode);

        // FixedArray(trace,HEADER_PROCESSES_INT,processName,HEADER_CPUS_INT,cpus,HEADER_FUNCTIONS_INT,function,
        // HEADER_MODES_INT, mode, HEADER_SUBMODES_INT, submode)
        fNormalPaths[7].set(0, trace);
        fNormalPaths[7].set(2, processName);
        fNormalPaths[7].set(4, cpus);
        fNormalPaths[7].set(6, function);
        fNormalPaths[7].set(8, mode);
        fNormalPaths[7].set(10, submode);

        // FixedArray(trace,HEADER_PROCESSES_INT,processName,HEADER_CPUS_INT,cpus,HEADER_MODES_INT,mode)
        fNormalPaths[8].set(0, trace);
        fNormalPaths[8].set(2, processName);
        fNormalPaths[8].set(4, cpus);
        fNormalPaths[8].set(6, mode);

        // FixedArray(trace,HEADER_PROCESSES_INT,processName,HEADER_CPUS_INT,cpus,HEADER_MODES_INT,mode,HEADER_SUBMODES_INT,submode)
        fNormalPaths[9].set(0, trace);
        fNormalPaths[9].set(2, processName);
        fNormalPaths[9].set(4, cpus);
        fNormalPaths[9].set(6, mode);
        fNormalPaths[9].set(8, submode);

        // FixedArray(trace,HEADER_PROCESSES_INT,processName,HEADER_MODES_INT,mode)
        fNormalPaths[10].set(0, trace);
        fNormalPaths[10].set(2, processName);
        fNormalPaths[10].set(4, mode);

        // FixedArray(trace,HEADER_PROCESSES_INT,processName,HEADER_MODES_INT,mode,HEADER_SUBMODES_INT,submode)
        fNormalPaths[11].set(0, trace);
        fNormalPaths[11].set(2, processName);
        fNormalPaths[11].set(4, mode);
        fNormalPaths[11].set(6, submode);

        // FixedArray(trace,HEADER_CPUS_INT,cpus)
        fNormalPaths[12].set(0, trace);
        fNormalPaths[12].set(2, cpus);

        // FixedArray(trace,HEADER_CPUS_INT,cpus,HEADER_MODES_INT,mode)
        fNormalPaths[13].set(0, trace);
        fNormalPaths[13].set(2, cpus);
        fNormalPaths[13].set(4, mode);

        // FixedArray(trace,HEADER_CPUS_INT,cpus,HEADER_MODES_INT,mode,HEADER_SUBMODES_INT,submode)
        fNormalPaths[14].set(0, trace);
        fNormalPaths[14].set(2, cpus);
        fNormalPaths[14].set(4, mode);
        fNormalPaths[14].set(6, submode);

        return fNormalPaths;

    }

    /**
     * <h4>Get the event types paths.</h4>
     * 
     * @param event
     *            Event to get the path for.
     * @param traceState
     *            State of the trace for this event.
     * @return Array of FixedArray representing the paths.
     */
    private FixedArray[] getTypePaths(LttngEvent event, LttngTraceState traceState) {
        int trace = (int) traceState.getContext().getIdentifier(); // No need
                                                                   // for the
                                                                   // identifier
                                                                   // (already
                                                                   // applied)
        Long cpu = event.getCpuId();
        int cpus = cpu.intValue() | LttngConstants.STATS_CPU_ID;
        LttngProcessState process = traceState.getRunning_process().get(cpu);
        int processName = fPidKeys.getUniqueId(process.getPid().intValue(), process.getCpu().intValue(), process.getCreation_time());
        int mode = process.getState().getExec_mode().ordinal() | LttngConstants.STATS_MODE_ID;
        int submode = fSubModeKeys.getUniqueId(process.getState().getExec_submode_id(), process.getState().getExec_submode());
        int function = process.getCurrent_function().intValue() | LttngConstants.STATS_FUNCTION_ID;
        int type = fTypeKeys.getUniqueId(event.getMarkerId(), event.getMarkerName());

        /*
         * Note that it's faster to re-use the path object, set the relevant
         * fields and clone the path later when it's time to add to the map
         */

        // FixedArray(trace,HEADER_EVENT_TYPES_INT,type)
        fTypedPaths[0].set(0, trace);
        fTypedPaths[0].set(2, type);

        // FixedArray(trace,HEADER_MODES_INT,mode,HEADER_EVENT_TYPES_INT,type)
        fTypedPaths[1].set(0, trace);
        fTypedPaths[1].set(2, mode);
        fTypedPaths[1].set(4, type);

        // FixedArray(trace,HEADER_MODES_INT,mode,HEADER_SUBMODES_INT,submode,HEADER_EVENT_TYPES_INT,type)
        fTypedPaths[2].set(0, trace);
        fTypedPaths[2].set(2, mode);
        fTypedPaths[2].set(4, submode);
        fTypedPaths[2].set(6, type);

        // FixedArray(trace,HEADER_PROCESSES_INT,processName,HEADER_EVENT_TYPES_INT,type)
        fTypedPaths[3].set(0, trace);
        fTypedPaths[3].set(2, processName);
        fTypedPaths[3].set(4, type);

        // FixedArray(trace,HEADER_PROCESSES_INT,processName,HEADER_CPUS_INT,cpus,HEADER_EVENT_TYPES_INT,type)
        fTypedPaths[4].set(0, trace);
        fTypedPaths[4].set(2, processName);
        fTypedPaths[4].set(4, cpus);
        fTypedPaths[4].set(6, type);

        // FixedArray(trace,HEADER_PROCESSES_INT,processName,HEADER_CPUS_INT,cpus,HEADER_FUNCTIONS_INT,function,HEADER_EVENT_TYPES_INT,type)
        fTypedPaths[5].set(0, trace);
        fTypedPaths[5].set(2, processName);
        fTypedPaths[5].set(4, cpus);
        fTypedPaths[5].set(6, function);
        fTypedPaths[5].set(8, type);

        // FixedArray(trace,HEADER_PROCESSES_INT,processName,HEADER_CPUS_INT,cpus,HEADER_FUNCTIONS_INT,function,HEADER_MODES_INT,mode,HEADER_EVENT_TYPES_INT,type)
        fTypedPaths[6].set(0, trace);
        fTypedPaths[6].set(2, processName);
        fTypedPaths[6].set(4, cpus);
        fTypedPaths[6].set(6, function);
        fTypedPaths[6].set(8, mode);
        fTypedPaths[6].set(10, type);

        // FixedArray(trace,HEADER_PROCESSES_INT,processName,HEADER_CPUS_INT,cpus,HEADER_FUNCTIONS_INT,function,
        // HEADER_MODES_INT,mode,HEADER_SUBMODES_INT,submode,HEADER_EVENT_TYPES_INT,type)
        fTypedPaths[7].set(0, trace);
        fTypedPaths[7].set(2, processName);
        fTypedPaths[7].set(4, cpus);
        fTypedPaths[7].set(6, function);
        fTypedPaths[7].set(8, mode);
        fTypedPaths[7].set(10, submode);
        fTypedPaths[7].set(12, type);

        // FixedArray(trace,HEADER_PROCESSES_INT,processName,HEADER_CPUS_INT,cpus,HEADER_MODES_INT,mode,HEADER_EVENT_TYPES_INT,type)
        fTypedPaths[8].set(0, trace);
        fTypedPaths[8].set(2, processName);
        fTypedPaths[8].set(4, cpus);
        fTypedPaths[8].set(6, mode);
        fTypedPaths[8].set(8, type);

        // FixedArray(trace,HEADER_PROCESSES_INT,processName,HEADER_CPUS_INT,cpus,HEADER_MODES_INT,mode,HEADER_SUBMODES_INT,submode,HEADER_EVENT_TYPES_INT,type)
        fTypedPaths[9].set(0, trace);
        fTypedPaths[9].set(2, processName);
        fTypedPaths[9].set(4, cpus);
        fTypedPaths[9].set(6, mode);
        fTypedPaths[9].set(8, submode);
        fTypedPaths[9].set(10, type);

        // FixedArray(trace,HEADER_PROCESSES_INT,processName,HEADER_MODES_INT,mode,HEADER_EVENT_TYPES_INT,type)
        fTypedPaths[10].set(0, trace);
        fTypedPaths[10].set(2, processName);
        fTypedPaths[10].set(4, mode);
        fTypedPaths[10].set(6, type);

        // FixedArray(trace,HEADER_PROCESSES_INT,processName,HEADER_MODES_INT,mode,HEADER_SUBMODES_INT,submode,HEADER_EVENT_TYPES_INT,type)
        fTypedPaths[11].set(0, trace);
        fTypedPaths[11].set(2, processName);
        fTypedPaths[11].set(4, mode);
        fTypedPaths[11].set(6, submode);
        fTypedPaths[11].set(8, type);

        // FixedArray(trace,HEADER_CPUS_INT,cpus,HEADER_EVENT_TYPES_INT,type)
        fTypedPaths[12].set(0, trace);
        fTypedPaths[12].set(2, cpus);
        fTypedPaths[12].set(4, type);

        // FixedArray(trace,HEADER_CPUS_INT,cpus,HEADER_MODES_INT,mode,HEADER_EVENT_TYPES_INT,type)
        fTypedPaths[13].set(0, trace);
        fTypedPaths[13].set(2, cpus);
        fTypedPaths[13].set(4, mode);
        fTypedPaths[13].set(6, type);

        // FixedArray(trace,HEADER_CPUS_INT,cpus,HEADER_MODES_INT,mode,HEADER_SUBMODES_INT,submode,HEADER_EVENT_TYPES_INT,type)
        fTypedPaths[14].set(0, trace);
        fTypedPaths[14].set(2, cpus);
        fTypedPaths[14].set(4, mode);
        fTypedPaths[14].set(6, submode);
        fTypedPaths[14].set(8, type);

        return fTypedPaths;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.linuxtools.lttng.ui.views.statistics.model.StatisticsData
     * #process_exit(org.eclipse.linuxtools.lttng.event.LttngEvent,
     * org.eclipse.linuxtools.lttng.state.model.LttngTraceState)
     */
    @Override
    public void process_exit(LttngEvent event, LttngTraceState traceState) {
        FixedArray[] paths = getNormalPaths(event, traceState);
        Long cpu = event.getCpuId();
        LttngProcessState process = traceState.getRunning_process().get(cpu);

        for (int j = 0; j < paths.length; ++j) {
            StatisticsTreeNode node = getOrCreate(paths[j], event, traceState, j, false);

            if (!process.getState().getExec_mode().equals(ExecutionMode.LTTV_STATE_MODE_UNKNOWN)) {
                node.getValue().cpuTime += event.getTimestamp().getValue() - process.getState().getChange_LttTime();
            }
        }
        // TODO Unstacks cumulative CPU time
        // TODO Elapsed time?
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.linuxtools.lttng.ui.views.statistics.model.StatisticsData
     * #increase(org.eclipse.linuxtools.lttng.event.LttngEvent,
     * org.eclipse.linuxtools.lttng.state.model.LttngTraceState, int)
     */
    @Override
    public void increase(LttngEvent event, LttngTraceState traceState, int values) {
        FixedArray[] paths = getNormalPaths(event, traceState);
        Long cpu = event.getCpuId();
		LttngProcessState process = traceState.getRunning_process().get(cpu);

        // Updating the cumulative CPU time
        if ((values & Values.STATE_CUMULATIVE_CPU_TIME) != 0) {
            if (process.getState().getProc_status().equals(ProcessStatus.LTTV_STATE_RUN) && !process.getState().getExec_mode().equals(ExecutionMode.LTTV_STATE_MODE_UNKNOWN)) {
                long cumulativeCpuTime = process.getState().getCum_cpu_time();
                long delta = event.getTimestamp().getValue() - process.getState().getChange_LttTime();
                process.getState().setCum_cpu_time(cumulativeCpuTime + delta);
            }
        }
        if ((values & Values.CUMULATIVE_CPU_TIME) != 0) {
            if (!process.getState().getExec_mode().equals(ExecutionMode.LTTV_STATE_MODE_UNKNOWN)) {
                long cumulativeCpuTime = process.getState().getCum_cpu_time();
                long delta = event.getTimestamp().getValue() - process.getState().getEntry_LttTime();
                long newCumulativeCpuTime = cumulativeCpuTime + delta;
                process.getState().setCum_cpu_time(newCumulativeCpuTime);
            } else if (process.getState().getProc_status().equals(ProcessStatus.LTTV_STATE_RUN) && !process.getState().getExec_mode().equals(ExecutionMode.LTTV_STATE_MODE_UNKNOWN)) {
                long cumulativeCpuTime = process.getState().getCum_cpu_time();
                long delta = event.getTimestamp().getValue() - process.getState().getChange_LttTime();
                long newCumulativeCpuTime = cumulativeCpuTime + delta;
                process.getState().setCum_cpu_time(newCumulativeCpuTime);
            }
        }

		for (int j = 0; j < paths.length; ++j) {
			StatisticsTreeNode node = getOrCreate(paths[j], event, traceState, j, false);

            if ((values & Values.CPU_TIME) != 0) {
                // TODO Uncomment if the event after process_exit need to be
                // count.
                if ((process.getState().getProc_status().equals(ProcessStatus.LTTV_STATE_RUN) /*
                                                                                               * || process.getState().
                                                                                               * getProc_status
                                                                                               * ().equals(ProcessStatus
                                                                                               * .LTTV_STATE_EXIT)
                                                                                               */) && !process.getState().getExec_mode().equals(ExecutionMode.LTTV_STATE_MODE_UNKNOWN)) {
                    node.getValue().cpuTime += event.getTimestamp().getValue() - process.getState().getChange_LttTime();
                }
            }
            if ((values & Values.CUMULATIVE_CPU_TIME) != 0) {
                if (!process.getState().getExec_mode().equals(ExecutionMode.LTTV_STATE_MODE_UNKNOWN)) {
                    node.getValue().cumulativeCpuTime += process.getState().getCum_cpu_time();
                } else if (process.getState().getProc_status().equals(ProcessStatus.LTTV_STATE_RUN) && !process.getState().getExec_mode().equals(ExecutionMode.LTTV_STATE_MODE_UNKNOWN)) {
                     node.getValue().cumulativeCpuTime += process.getState().getCum_cpu_time();
                }
            }
            if ((values & Values.ELAPSED_TIME) != 0) {
                if (!process.getState().getExec_mode().equals(ExecutionMode.LTTV_STATE_MODE_UNKNOWN)) {
                    node.getValue().elapsedTime += event.getTimestamp().getValue() - process.getState().getEntry_LttTime();
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.linuxtools.lttng.ui.views.statistics.model.StatisticsData
     * #registerEvent(org.eclipse.linuxtools.lttng.event.LttngEvent,
     * org.eclipse.linuxtools.lttng.state.model.LttngTraceState)
     */
    @Override
    public void registerEvent(LttngEvent event, LttngTraceState traceState) {
        FixedArray[] paths = getNormalPaths(event, traceState);
        for (int i = 0; i < paths.length; ++i)
            ++(getOrCreate(paths[i], event, traceState, i, false).getValue().nbEvents);

        paths = getTypePaths(event, traceState);
        for (int i = 0; i < paths.length; ++i)
            ++(getOrCreate(paths[i], event, traceState, i, true).getValue().nbEvents);

        // last_ = event; // TODO Used by endTraceset
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.linuxtools.lttng.ui.views.statistics.model.StatisticsData
     * #registerName
     * (org.eclipse.linuxtools.lttng.ui.views.statistics.model.FixedArray)
     */
    @Override
    protected void registerName(final FixedArray path) {
        if (path.size() == 1) {
            if (!path.equals(ROOT))
                getKeys().get(ROOT.get(0)).add(path.get(0));
        } else if (path.size() % 2 != 0)
            getKeys().get(path.get(path.size() - 2)).add(path.get(path.size() - 1));
    }

    /**
     * <h4>Get or create a node.</h4>
     * 
     * @param path
     *            Path to the node.
     * @param event
     *            The current event
     * @param traceState
     *            The current trace state
     * @param index
     *            The corresponding index of the statistic
     * @param isType
     *            The type of statistic (type or other)
     * @return The node.
     */
    public StatisticsTreeNode getOrCreate(final FixedArray path, LttngEvent event, LttngTraceState traceState, int index, boolean isType) {
        StatisticsTreeNode current = get(path);
        if (current == null) {
            // We have to clone the path since the object for the path is
            // re-used for performance reasons!
            FixedArray newPath = (FixedArray) path.clone();

            // Note that setting of the name is done here only when the node is
            // created (for performance reasons).
            String name = (isType) ? getTypeStatsName(event, traceState, index) : getOtherStatsName(event, traceState, index);
            registerName(path);
            current = new StatisticsTreeNode(newPath, this, name);
            put(newPath, current);
        }
        else {
            // Special case: Update name if event is of type "exec". This is necessary because the 
            // process name can change at this point (See Bug333114))
            if ((index == 3) && !isType && Events.LTT_EVENT_EXEC.getInName().equals(event.getMarkerName())) {
                String name = getOtherStatsName(event, traceState, index);
                current.setName(name);
            }
        }
        return current;
    }

    /**
     * <h4>Get the name to be displayed for other statistics than type
     * statistics</h4>
     * 
     * @param event
     *            The current event
     * @param traceState
     *            The current trace state
     * @param The
     *            corresponding index of the statistic
     * @return The name
     */
    private String getOtherStatsName(LttngEvent event, LttngTraceState traceState, int index) {
        Long cpu = event.getCpuId();
        LttngProcessState process = traceState.getRunning_process().get(cpu);

        switch (index) {
        case 0:
            return traceState.getContext().getTraceId();
        case 1:
            return process.getState().getExec_mode().getInName();
        case 2:
            return process.getState().getExec_submode();
        case 3:
            return getProcessName(process);
        case 4:
            return String.valueOf(cpu);
        case 5:
            return process.getCurrent_function().toString();
        case 6:
            return process.getState().getExec_mode().getInName();
        case 7:
            return process.getState().getExec_submode();
        case 8:
            return process.getState().getExec_mode().getInName();
        case 9:
            return process.getState().getExec_submode();
        case 10:
            return process.getState().getExec_mode().getInName();
        case 11:
            return process.getState().getExec_submode();
        case 12:
            return String.valueOf(cpu);
        case 13:
            return process.getState().getExec_mode().getInName();
        case 14:
            return process.getState().getExec_submode();
        default:
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * <h4>Get the name to be displayed for type statistics</h4>
     * 
     * @param event
     *            The current event
     * @param traceState
     *            The current state
     * @param index
     *            The corresponding index of the statistic
     * @return The strings in a array
     */
    private String getTypeStatsName(LttngEvent event, LttngTraceState traceState, int index) {
        return event.getMarkerName();
    }

    /**
     * <h4>Get the name of a process.</h4>
     * 
     * @param process
     *            The process.
     * @return The name of the process. //TODO Adding the creation time of the
     *         process may be needed to differentiate two process.
     */
    private String getProcessName(LttngProcessState process) {
        if (process.getPid() == -1)
            return Messages.StatisticsData_UnknowProcess;
        if (process.getName() == null)
            return mergeString(Messages.StatisticsData_UnknowProcess + " - ", String.valueOf(process.getPid())); //$NON-NLS-1$
        if (process.getName().equals("")) //$NON-NLS-1$
            return process.getPid().toString();
        else
            return mergeString(process.getName(), " - ", String.valueOf(process.getPid())); //$NON-NLS-1$
    }

    /**
     * <h4>Converts the integer representation of the category to string.</h4>
     * 
     * @param value
     *            Integer representation of the category.
     * @return Category as string.
     */
    public static String getCategoryFromId(int value) {
        switch (value) {
        case KernelStatisticsData.HEADER_CPUS_INT:
            return KernelStatisticsData.HEADER_CPUS;
        case KernelStatisticsData.HEADER_EVENT_TYPES_INT:
            return KernelStatisticsData.HEADER_EVENT_TYPES;
        case KernelStatisticsData.HEADER_FUNCTIONS_INT:
            return KernelStatisticsData.HEADER_FUNCTIONS;
        case KernelStatisticsData.HEADER_MODES_INT:
            return KernelStatisticsData.HEADER_MODES;
        case KernelStatisticsData.HEADER_PROCESSES_INT:
            return KernelStatisticsData.HEADER_PROCESSES;
        case KernelStatisticsData.HEADER_SUBMODES_INT:
            return KernelStatisticsData.HEADER_SUBMODES;
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * <h4>Provides unique keys for String - Integer pairs.</h4>
     * 
     * @author bhufmann
     * 
     */
    private static final class KeyProvider {

        /**
         * <h4>Instance counter for unique ID generation.</h4>
         */
        private int fCount = 0;

        /**
         * <h4>Attributes to generate unique IDs for processes.</h4>
         */
        private HashMap<KeyHelper, Integer> fKeyMap = new HashMap<KeyHelper, Integer>(65535);
        private final KeyHelper fHelper = new KeyHelper();

        /**
         * <h4>Bit mask to apply for the key.</h4>
         */
        private int fBitMask = 0;

        /**
         * Constructor
         * 
         * @param bitMask
         *            <h4>Bit mask to apply for the key.</h4>
         */
        KeyProvider(int bitMask) {
            this.fBitMask = bitMask;
        }

        /**
         * <h4>Standard Constructor</h4>
         */
        KeyProvider() {
            this(0);
        }

        /**
         * <h4>Creates unique id for the given input data.</h4>
         * 
         * @param value
         *            Integer value of the data the key is for
         * @param name
         *            Name of the data the key is for
         * @return Unique id
         */
        public int getUniqueId(int value, String name) {
            fHelper.setName(name);
            fHelper.setValue(value);

            Integer returnKey = fKeyMap.get(fHelper);
            if (returnKey == null) {
                returnKey = Integer.valueOf((++fCount) | fBitMask);
                KeyHelper newHelper = fHelper.clone();
                fKeyMap.put(newHelper, returnKey);
            }
            return returnKey.intValue();
        }
    }

    /**
     <h4>Helper class that provides keys for HashMaps depending on an integer 
     * - string -pair. It provides better performance than using a string as key
     * only. However, for optimal performance the integer values should be mostly
     * unique.</h4>
     * 
     * @author bhufmann
     * 
     */
    private static final class KeyHelper implements Cloneable {

        // Short pre-fix
        private final static String UNKNOWN_PREFIX = "P"; //$NON-NLS-1$

        private String fName = UNKNOWN_PREFIX;
        private int fValue = -1;

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return fValue;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (o == null)
                return false;
            if (!(o instanceof KeyHelper))
                return false;
            KeyHelper kh = (KeyHelper) o;
            if (fValue == kh.fValue && fName.equals(kh.fName)) {
                return true;
            }
            return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#clone()
         */
        @Override
        public KeyHelper clone() {
            KeyHelper clone = null;
            try {
                clone = (KeyHelper) super.clone();
                clone.fName = fName;
                clone.fValue = fValue;
            } catch (CloneNotSupportedException e) {
            }
            return clone;
        }

        /**
         * <h4>Set the name of the key.</h4>
         * 
         * @param name
         *            The name to set.
         */
        public void setName(String name) {
            if (fName != null) {
                this.fName = name;
            } else {
                this.fName = UNKNOWN_PREFIX;
            }
        }

        /**
         * <h4>Set the value of the key.</h4>
         * 
         * @param value
         */
        public void setValue(int value) {
            this.fValue = value;
        }
    }

    /**
     * <h4>Provides unique keys for given process information. For optimal performance the integer 
     * PIDs need to be mostly unique</h4>
     * 
     * @author bhufmann
     * 
     */
    private static final class ProcessKeyProvider {
        /**
         * <h4>Instance counter for unique ID generation.</h4>
         */
        private int fCount = 0;

        /**
         * <h4>Attributes to generate unique IDs for processes.</h4>
         */
        private HashMap<ProcessKey, Integer> fKeyMap = new HashMap<ProcessKey, Integer>(65535);
        private ProcessKey fHelper = new ProcessKey();

        /**
         * <h4>Bit mask to apply for the key.</h4>
         */
        private int fBitMask = 0;

        /**
         * Constructor
         * 
         * @param bitMask
         *            <h4>Bit mask to apply for the key.</h4>
         */
        public ProcessKeyProvider(int bitMask) {
            fBitMask = bitMask;
        }

        /**
         * <h4>Creates unique id for the given input data.</h4>
         * 
         * @param value
         *            Integer value of the data the key is for
         * @param cpuId
         *            The cpuId for the processKey Helper
         * @param creationTime
         *            The creation Time for the processKey Helper
         * @return Unique id
         */
        public int getUniqueId(int value, int cpuId, long creationTime) {
            fHelper.setPid(value);
            fHelper.setCpuId(cpuId);
            fHelper.setCreationTime(creationTime);

            Integer returnKey = fKeyMap.get(fHelper);
            if (returnKey == null) {
                returnKey = Integer.valueOf((++fCount) | fBitMask);
                ProcessKey newHelper = fHelper.clone();
                fKeyMap.put(newHelper, returnKey);
            }
            return returnKey.intValue();
        }
    }
    
    /**
    <h4>Helper class that provides keys for HashMaps depending on process information.</h4>
    * 
    * @author bhufmann
    * 
    */
    private static final class ProcessKey implements Cloneable {
        private int fPid = 0;
        private int fCpuId = 0;
        private long fCreationTime = 0;

        /**
         * <h4>Set the PID of the key.</h4>
         * 
         * @param pid
         */
        public void setPid(int pid) {
            this.fPid = pid;
        }
        
        /**
         * <h4>Set the cpuTime of the process key.</h4>
         * 
         * @param cpuTime
         *            The name to set.
         */
        public void setCpuId(int cpuId) {
            this.fCpuId = cpuId;
        }

        /**
         * <h4>Set the creationTime of the process key.</h4>
         * 
         * @param creationTime
         *            The name to set.
         */
        public void setCreationTime(long creationTime) {
            this.fCreationTime = creationTime;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof ProcessKey))
                return false;

            ProcessKey procKey = (ProcessKey) obj;

            if (procKey.fPid != this.fPid) {
                return false;
            }

            if (procKey.fCreationTime != this.fCreationTime) {
                return false;
            }
            
            // use the cpu value to validate pid 0
            if (((procKey.fPid == 0L) && (procKey.fCpuId != this.fCpuId))) {
                return false;
            }
            return true;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return this.fPid;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#clone()
         */
        @Override
        public ProcessKey clone() {
            ProcessKey clone = null;
            try {
                clone = (ProcessKey) super.clone();
                clone.fPid = fPid;
                clone.fCpuId = fCpuId;
                clone.fCreationTime = fCreationTime;
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return clone;
        }
    }
}
