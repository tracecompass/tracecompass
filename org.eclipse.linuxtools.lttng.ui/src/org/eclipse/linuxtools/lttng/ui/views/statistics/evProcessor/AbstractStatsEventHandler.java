/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Yann N. Dauphin     (dhaemon@gmail.com)  - Implementation for stats
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.statistics.evProcessor;

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.state.StateStrings.Events;
import org.eclipse.linuxtools.lttng.state.StateStrings.ExecutionMode;
import org.eclipse.linuxtools.lttng.state.StateStrings.ProcessStatus;
import org.eclipse.linuxtools.lttng.state.evProcessor.IEventProcessing;
import org.eclipse.linuxtools.lttng.state.model.LttngProcessState;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;
import org.eclipse.linuxtools.lttng.ui.views.statistics.model.StatisticsTreeFactory;
import org.eclipse.linuxtools.lttng.ui.views.statistics.model.StatisticsTreeNode;

abstract class AbstractStatsEventHandler implements IEventProcessing {
	private Events eventType;
	
	public AbstractStatsEventHandler(Events eventType) {
		super();
		this.eventType = eventType;
	}

	/**
	 * @return root of of the tree for this experiment.
	 */
	protected StatisticsTreeNode getStatisticsTree(LttngEvent trcEvent) {
		StatisticsTreeNode tree = StatisticsTreeFactory.getStatisticsTree("Experiment");
		return tree;
	}
	
	/**
	 * @return list of paths that should be updated for this event.
	 */
	protected String[][] getRelevantPaths(LttngEvent event,
			LttngTraceState traceState) {
		String trace = traceState.getInputDataRef().getTraceId();
		
		Long cpu = event.getCpuId();
		
		LttngProcessState process = traceState.getRunning_process().get(
				cpu);
		
		String processName = getPocessName(process);
				
		String mode = process.getState().getExec_mode().getInName();
		
		String submode = process.getState().getExec_submode();
		
		Long function = process.getCurrent_function();
		
		// String type = event.getType().getTypeId();

		String[][] paths = {
				{trace},
				{trace, "Modes", mode},
				{trace, "Modes", mode, "Submodes", submode},
				{trace, "Processes", processName},
				{trace, "Processes", processName, "CPUs", cpu.toString()},
				{trace, "Processes", processName, "CPUs", cpu.toString(), "Functions", function.toString()},
				{trace, "Processes", processName, "CPUs", cpu.toString(), "Modes", mode},
				{trace, "Processes", processName, "CPUs", cpu.toString(), "Modes", mode, "Submodes", submode},
				{trace, "Processes", processName, "Modes", mode},
				{trace, "Processes", processName, "Modes", mode, "Submodes", submode},
				{trace, "CPUs", cpu.toString()},
				{trace, "CPUs", cpu.toString(), "Modes", mode},
				{trace, "CPUs", cpu.toString(), "Modes", mode, "Submodes", submode},
		};
		return paths;
	}
	
	/**
	 * @return list of event types paths that should be updated for this event.
	 */
	protected String[][] getRelevantEventTypesPaths(LttngEvent event,
			LttngTraceState traceState) {
		String trace = traceState.getInputDataRef().getTraceId();
		
		Long cpu = event.getCpuId();
		
		LttngProcessState process = traceState.getRunning_process().get(
				cpu);
		
		String processName = getPocessName(process);
				
		String mode = process.getState().getExec_mode().getInName();
		
		String submode = process.getState().getExec_submode();
		
		Long function = process.getCurrent_function();
		
		String type = event.getType().getTypeId();

		String[][] paths = {
				{trace, "Event Types", type},
				{trace, "Modes", mode, "Event Types", type},
				{trace, "Modes", mode, "Submodes", submode, "Event Types", type},
				{trace, "Processes", processName, "Event Types", type},
				{trace, "Processes", processName, "CPUs", cpu.toString(), "Event Types", type},
				{trace, "Processes", processName, "CPUs", cpu.toString(), "Functions", function.toString(), "Event Types", type},
				{trace, "Processes", processName, "CPUs", cpu.toString(), "Modes", mode, "Event Types", type},
				{trace, "Processes", processName, "CPUs", cpu.toString(), "Modes", mode, "Submodes", submode, "Event Types", type},
				{trace, "Processes", processName, "Modes", mode, "Event Types", type},
				{trace, "Processes", processName, "Modes", mode, "Submodes", submode, "Event Types", type},
				{trace, "CPUs", cpu.toString(), "Event Types", type},
				{trace, "CPUs", cpu.toString(), "Modes", mode, "Event Types", type},
				{trace, "CPUs", cpu.toString(), "Modes", mode, "Submodes", submode, "Event Types", type},
		};
		return paths;
	}
	
	/**
	 * @return name of the process. Returns special string if the name is "".
	 */
	private String getPocessName(LttngProcessState process) {
		if (process.getName() == null) {
			return "Unknown process";
		}
		if (process.getName() == "") {
			return process.getPid().toString();
		}
		else {
			return process.getName();
		}
	}
	
	/**
	 * Increase the NbEvents counter of this node.
	 */
	protected void increaseNbEvents(StatisticsTreeNode node) {
		node.getValue().nbEvents++;
	}
	
	/**
	 * Increase the CPU Time according to the trace state.
	 */
	protected void increaseCPUTime(StatisticsTreeNode node, LttngEvent event,
			LttngTraceState traceState) {
		Long cpu = event.getCpuId();
		
		LttngProcessState process = traceState.getRunning_process().get(
				cpu);
		
		if (process.getState().getProc_status().equals(ProcessStatus.LTTV_STATE_RUN) &&
				!process.getState().getExec_mode().equals(ExecutionMode.LTTV_STATE_MODE_UNKNOWN)) {
			node.getValue().cpuTime += event.getTimestamp().getValue()
				- process.getState().getChange_LttTime();
		}
	}
	
	/**
	 * Increase the Elapsed Time according to the trace state.
	 */
	protected void increaseElapsedTime(StatisticsTreeNode node, LttngEvent event,
			LttngTraceState traceState) {
		Long cpu = event.getCpuId();
		
		LttngProcessState process = traceState.getRunning_process().get(
				cpu);
		
		if (!process.getState().getExec_mode().equals(ExecutionMode.LTTV_STATE_MODE_UNKNOWN)) {
			node.getValue().elapsedTime += event.getTimestamp().getValue()
				- process.getState().getEntry_LttTime();
		}
	}
	
	/**
	 * Increase the Cumulative CPU Time according to the trace state.
	 */
	protected void increaseCumulativeCPUTime(StatisticsTreeNode node, LttngEvent event,
			LttngTraceState traceState) {
		Long cpu = event.getCpuId();
		
		LttngProcessState process = traceState.getRunning_process().get(
				cpu);
		
		if (!process.getState().getExec_mode().equals(ExecutionMode.LTTV_STATE_MODE_UNKNOWN)) {
			long cumulativeCpuTime = process.getState().getCum_cpu_time();
			long delta = event.getTimestamp().getValue() - process.getState().getEntry_LttTime();
			process.getState().setCum_cpu_time(cumulativeCpuTime + delta);
			node.getValue().cumulativeCpuTime += process.getState().getCum_cpu_time();
		}
		else if (process.getState().getProc_status().equals(ProcessStatus.LTTV_STATE_RUN) &&
				!process.getState().getExec_mode().equals(ExecutionMode.LTTV_STATE_MODE_UNKNOWN)) {
			long cumulativeCpuTime = process.getState().getCum_cpu_time();
			long delta = event.getTimestamp().getValue() - process.getState().getChange_LttTime();
			process.getState().setCum_cpu_time(cumulativeCpuTime + delta);
			node.getValue().cumulativeCpuTime += process.getState().getCum_cpu_time();
		}
	}
	
	/**
	 * Increase the State-bound Cumulative CPU Time according to the trace state.
	 */
	protected void increaseStateCumulativeCPUTime(LttngEvent event,
			LttngTraceState traceState) {
		Long cpu = event.getCpuId();
		
		LttngProcessState process = traceState.getRunning_process().get(cpu);
		
		if (process.getState().getProc_status().equals(ProcessStatus.LTTV_STATE_RUN) &&
				!process.getState().getExec_mode().equals(ExecutionMode.LTTV_STATE_MODE_UNKNOWN)) {
			long cumulativeCpuTime = process.getState().getCum_cpu_time();
			long delta = event.getTimestamp().getValue() - process.getState().getChange_LttTime();
			process.getState().setCum_cpu_time(cumulativeCpuTime + delta);
		}
	}
	
//	@Override
	public Events getEventHandleType() {
		return eventType;
	}

}