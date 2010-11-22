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

package org.eclipse.linuxtools.lttng.ui.views.statistics.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map;

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.state.StateStrings.ExecutionMode;
import org.eclipse.linuxtools.lttng.state.StateStrings.ProcessStatus;
import org.eclipse.linuxtools.lttng.state.model.LttngProcessState;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;

/**
 * <h4>Class that process the LTTng kernel events.</h4>
 */
public class KernelStatisticsData extends StatisticsData {
    /**
     * <h4>Header for the mode categories.</h4>
     */
    private static final String HEADER_CPUS = Messages.KernelStatisticsData_CPUs;
    /**
     * <h4>Header for the event types categories.</h4>
     */
    private static final String HEADER_EVENT_TYPES = Messages.KernelStatisticsData_EventTypes;
    /**
     * <h4>Header for the function categories.</h4>
     */
    private static final String HEADER_FUNCTIONS = Messages.KernelStatisticsData_Functions;
    /**
     * <h4>Header for the mode categories.</h4>
     */
    private static final String HEADER_MODES = Messages.KernelStatisticsData_Modes;
    /**
     * <h4>Header for the processes categories.</h4>
     */
    private static final String HEADER_PROCESSES = Messages.KernelStatisticsData_Processes;
    /**
     * <h4>Header for the submode categories.</h4>
     */
    private static final String HEADER_SUBMODES = Messages.KernelStatisticsData_SubModes;
    /**
     * <h4>Indicate that it's a value.</h4>
     * <p>
     * Used when checking the possible child node for a node.
     * </p>
     * <p>
     * It differentiate a category of a value by being appended to a value.
     * </p>
     */
    private static final String NODE = "z"; //$NON-NLS-1$
    private static final String ROOT_NODE_KEY = mergeString(ROOT.get(0), NODE);

    /**
     * <h4>Constructor.</h4>
     * 
     * @param traceName
     */
    public KernelStatisticsData(String traceName) {
	super();
	Map<String, Set<String>> keys = getKeys();

	// //////////// Adding category sets
	keys.put(HEADER_PROCESSES, new HashSet<String>());
	keys.put(HEADER_MODES, new HashSet<String>());
	keys.put(HEADER_CPUS, new HashSet<String>(4)); // Over 4 CPUs is not
						       // common
	keys.put(HEADER_SUBMODES, new HashSet<String>());
	keys.put(HEADER_EVENT_TYPES, new HashSet<String>());
	keys.put(HEADER_FUNCTIONS, new HashSet<String>(4)); // Seems to be
							    // always one.

	// /////////// Adding value sets
	// Under a trace
	Set<String> temp = new HashSet<String>(8);
	temp.add(HEADER_PROCESSES);
	temp.add(HEADER_MODES);
	temp.add(HEADER_CPUS);
	temp.add(HEADER_EVENT_TYPES);
	keys.put(ROOT_NODE_KEY, temp);
	// Under a process
	temp = new HashSet<String>(4);
	temp.add(HEADER_MODES);
	temp.add(HEADER_CPUS);
	temp.add(HEADER_EVENT_TYPES);
	keys.put(mergeString(HEADER_PROCESSES, NODE), temp);
	// Under a CPUs : Functions is a special case
	temp = new HashSet<String>(4);
	temp.add(HEADER_MODES);
	temp.add(HEADER_EVENT_TYPES);
	keys.put(mergeString(HEADER_CPUS, NODE), temp);
	// Under a functions
	temp = new HashSet<String>(4);
	temp.add(HEADER_MODES);
	temp.add(HEADER_EVENT_TYPES);
	keys.put(mergeString(HEADER_FUNCTIONS, NODE), temp);
	// Under a mode
	temp = new HashSet<String>(4);
	temp.add(HEADER_SUBMODES);
	temp.add(HEADER_EVENT_TYPES);
	keys.put(mergeString(HEADER_MODES, NODE), temp);
	// Under a submodes
	temp = new HashSet<String>(2);
	temp.add(HEADER_EVENT_TYPES);
	keys.put(mergeString(HEADER_SUBMODES, NODE), temp);
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
    public Collection<StatisticsTreeNode> getChildren(final FixedArray<String> path) {
	LinkedList<StatisticsTreeNode> result = new LinkedList<StatisticsTreeNode>();

	if (path.size() % 2 == 0) { // if we are at a Category
	    StatisticsTreeNode current = null;
	    for (String value : getKeys().get(path.get(path.size() - 1))) {
		current = get(path.append(value));
		if (current != null && current.getValue().nbEvents != 0)
		    result.add(current);
	    }
	} else if (path.size() == 1) { // Special case.
	    if (path.equals(ROOT)) // Asking for the root.
		for (String value : getKeys().get(ROOT.get(0)))
		    result.add(getOrCreate(new FixedArray<String>(value)));
	    else
		// Get value under the root
		for (String value : getKeys().get(ROOT_NODE_KEY))
		    result.add(getOrCreate(path.append(value)));
	} else {// If we are at a value
	    for (String value : getKeys().get(mergeString(path.get(path.size() - 2), NODE)))
		// Search the parent name + NODE
		result.add(getOrCreate(path.append(value)));

	    if (path.size() == 5 && path.get(3).equals(HEADER_CPUS)) // Special
								     // the
								     // Functions
								     // is just
								     // there.
								     // We want
								     // the
								     // fourth
								     // element
								     // but it
								     // start at
								     // 0. So #3
		result.add(getOrCreate(path.append(HEADER_FUNCTIONS)));
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private FixedArray<String>[] getNormalPaths(LttngEvent event, LttngTraceState traceState) {
	String trace = traceState.getContext().getTraceId();
	Long cpu = event.getCpuId();
	String cpus = String.valueOf(cpu);
	LttngProcessState process = traceState.getRunning_process().get(cpu);
	String processName = getProcessName(process);
	String mode = process.getState().getExec_mode().getInName();
	String submode = process.getState().getExec_submode();
	String function = process.getCurrent_function().toString();

	FixedArray[] paths = { new FixedArray<String>(trace), new FixedArray<String>(trace, HEADER_MODES, mode), new FixedArray<String>(trace, HEADER_MODES, mode, HEADER_SUBMODES, submode),
	        new FixedArray<String>(trace, HEADER_PROCESSES, processName), new FixedArray<String>(trace, HEADER_PROCESSES, processName, HEADER_CPUS, cpus),
	        new FixedArray<String>(trace, HEADER_PROCESSES, processName, HEADER_CPUS, cpus, HEADER_FUNCTIONS, function),
	        new FixedArray<String>(trace, HEADER_PROCESSES, processName, HEADER_CPUS, cpus, HEADER_FUNCTIONS, function, HEADER_MODES, mode),
	        new FixedArray<String>(trace, HEADER_PROCESSES, processName, HEADER_CPUS, cpus, HEADER_FUNCTIONS, function, HEADER_MODES, mode, HEADER_SUBMODES, submode),
	        new FixedArray<String>(trace, HEADER_PROCESSES, processName, HEADER_CPUS, cpus, HEADER_MODES, mode),
	        new FixedArray<String>(trace, HEADER_PROCESSES, processName, HEADER_CPUS, cpus, HEADER_MODES, mode, HEADER_SUBMODES, submode),
	        new FixedArray<String>(trace, HEADER_PROCESSES, processName, HEADER_MODES, mode),
	        new FixedArray<String>(trace, HEADER_PROCESSES, processName, HEADER_MODES, mode, HEADER_SUBMODES, submode), new FixedArray<String>(trace, HEADER_CPUS, cpus),
	        new FixedArray<String>(trace, HEADER_CPUS, cpus, HEADER_MODES, mode), new FixedArray<String>(trace, HEADER_CPUS, cpus, HEADER_MODES, mode, HEADER_SUBMODES, submode), };

	return paths;
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private FixedArray<String>[] getTypePaths(LttngEvent event, LttngTraceState traceState) {
	String trace = traceState.getContext().getTraceId();
	Long cpu = event.getCpuId();
	String cpus = String.valueOf(cpu);
	LttngProcessState process = traceState.getRunning_process().get(cpu);
	String processName = getProcessName(process);
	String mode = process.getState().getExec_mode().getInName();
	String submode = process.getState().getExec_submode();
	String function = process.getCurrent_function().toString();
	// String type = event.getType().getTypeId(); // Add too much
	// informations
	String type = event.getMarkerName();

	FixedArray[] paths = { new FixedArray<String>(trace, HEADER_EVENT_TYPES, type), new FixedArray<String>(trace, HEADER_MODES, mode, HEADER_EVENT_TYPES, type),
	        new FixedArray<String>(trace, HEADER_MODES, mode, HEADER_SUBMODES, submode, HEADER_EVENT_TYPES, type),
	        new FixedArray<String>(trace, HEADER_PROCESSES, processName, HEADER_EVENT_TYPES, type),
	        new FixedArray<String>(trace, HEADER_PROCESSES, processName, HEADER_CPUS, cpus, HEADER_EVENT_TYPES, type),
	        new FixedArray<String>(trace, HEADER_PROCESSES, processName, HEADER_CPUS, cpus, HEADER_FUNCTIONS, function, HEADER_EVENT_TYPES, type),
	        new FixedArray<String>(trace, HEADER_PROCESSES, processName, HEADER_CPUS, cpus, HEADER_FUNCTIONS, function, HEADER_MODES, mode, HEADER_EVENT_TYPES, type),
	        new FixedArray<String>(trace, HEADER_PROCESSES, processName, HEADER_CPUS, cpus, HEADER_FUNCTIONS, function, HEADER_MODES, mode, HEADER_SUBMODES, submode, HEADER_EVENT_TYPES, type),
	        new FixedArray<String>(trace, HEADER_PROCESSES, processName, HEADER_CPUS, cpus, HEADER_MODES, mode, HEADER_EVENT_TYPES, type),
	        new FixedArray<String>(trace, HEADER_PROCESSES, processName, HEADER_CPUS, cpus, HEADER_MODES, mode, HEADER_SUBMODES, submode, HEADER_EVENT_TYPES, type),
	        new FixedArray<String>(trace, HEADER_PROCESSES, processName, HEADER_MODES, mode, HEADER_EVENT_TYPES, type),
	        new FixedArray<String>(trace, HEADER_PROCESSES, processName, HEADER_MODES, mode, HEADER_SUBMODES, submode, HEADER_EVENT_TYPES, type),
	        new FixedArray<String>(trace, HEADER_CPUS, cpus, HEADER_EVENT_TYPES, type), new FixedArray<String>(trace, HEADER_CPUS, cpus, HEADER_MODES, mode, HEADER_EVENT_TYPES, type),
	        new FixedArray<String>(trace, HEADER_CPUS, cpus, HEADER_MODES, mode, HEADER_SUBMODES, submode, HEADER_EVENT_TYPES, type), };

	return paths;
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
	FixedArray<String>[] paths = getNormalPaths(event, traceState);
	Long cpu = event.getCpuId();
	LttngProcessState process = traceState.getRunning_process().get(cpu);

	for (FixedArray<String> path : paths) {
	    StatisticsTreeNode node = getOrCreate(path);

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
	FixedArray<String>[] paths = getNormalPaths(event, traceState);
	Long cpu = event.getCpuId();
	LttngProcessState process = traceState.getRunning_process().get(cpu);

	for (FixedArray<String> path : paths) {
	    StatisticsTreeNode node = getOrCreate(path);

	    if ((values & Values.CPU_TIME) != 0) {
		// TODO Uncomment if the event after process_exit need to be
		// count.
		if ((process.getState().getProc_status().equals(ProcessStatus.LTTV_STATE_RUN) /*
						                                               * ||
						                                               * process
						                                               * .
						                                               * getState
						                                               * (
						                                               * )
						                                               * .
						                                               * getProc_status
						                                               * (
						                                               * )
						                                               * .
						                                               * equals
						                                               * (
						                                               * ProcessStatus
						                                               * .
						                                               * LTTV_STATE_EXIT
						                                               * )
						                                               */) && !process.getState().getExec_mode().equals(ExecutionMode.LTTV_STATE_MODE_UNKNOWN)) {
		    node.getValue().cpuTime += event.getTimestamp().getValue() - process.getState().getChange_LttTime();
		}
	    }
	    if ((values & Values.CUMULATIVE_CPU_TIME) != 0) {
		if (!process.getState().getExec_mode().equals(ExecutionMode.LTTV_STATE_MODE_UNKNOWN)) {
		    long cumulativeCpuTime = process.getState().getCum_cpu_time();
		    long delta = event.getTimestamp().getValue() - process.getState().getEntry_LttTime();
		    process.getState().setCum_cpu_time(cumulativeCpuTime + delta);
		    node.getValue().cumulativeCpuTime += process.getState().getCum_cpu_time();
		} else if (process.getState().getProc_status().equals(ProcessStatus.LTTV_STATE_RUN) && !process.getState().getExec_mode().equals(ExecutionMode.LTTV_STATE_MODE_UNKNOWN)) {
		    long cumulativeCpuTime = process.getState().getCum_cpu_time();
		    long delta = event.getTimestamp().getValue() - process.getState().getChange_LttTime();
		    process.getState().setCum_cpu_time(cumulativeCpuTime + delta);
		    node.getValue().cumulativeCpuTime += process.getState().getCum_cpu_time();
		}
	    }
	    if ((values & Values.ELAPSED_TIME) != 0) {
		if (!process.getState().getExec_mode().equals(ExecutionMode.LTTV_STATE_MODE_UNKNOWN)) {
		    node.getValue().elapsedTime += event.getTimestamp().getValue() - process.getState().getEntry_LttTime();
		}
	    }
	    if ((values & Values.STATE_CUMULATIVE_CPU_TIME) != 0) {
		if (process.getState().getProc_status().equals(ProcessStatus.LTTV_STATE_RUN) && !process.getState().getExec_mode().equals(ExecutionMode.LTTV_STATE_MODE_UNKNOWN)) {
		    long cumulativeCpuTime = process.getState().getCum_cpu_time();
		    long delta = event.getTimestamp().getValue() - process.getState().getChange_LttTime();
		    process.getState().setCum_cpu_time(cumulativeCpuTime + delta);
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
	FixedArray<String>[] paths = getNormalPaths(event, traceState);
	for (FixedArray<String> path : paths)
	    ++(getOrCreate(path).getValue().nbEvents);

	paths = getTypePaths(event, traceState);
	for (FixedArray<String> path : paths)
	    ++(getOrCreate(path).getValue().nbEvents);

	// last_ = event; // TODO Used by endTraceset
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
    protected void registerName(final FixedArray<String> path) {
	if (path.size() == 1) {
	    if (!path.equals(ROOT))
		getKeys().get(ROOT.get(0)).add(path.get(0));
	} else if (path.size() % 2 != 0)
	    getKeys().get(path.get(path.size() - 2)).add(path.get(path.size() - 1));
    }

}
