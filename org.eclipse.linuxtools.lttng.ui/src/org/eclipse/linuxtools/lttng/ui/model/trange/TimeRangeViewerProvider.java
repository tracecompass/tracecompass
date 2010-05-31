/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.model.trange;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.lttng.state.StateStrings.BdevMode;
import org.eclipse.linuxtools.lttng.state.StateStrings.CpuMode;
import org.eclipse.linuxtools.lttng.state.StateStrings.ExecutionMode;
import org.eclipse.linuxtools.lttng.state.StateStrings.IRQMode;
import org.eclipse.linuxtools.lttng.state.StateStrings.ProcessStatus;
import org.eclipse.linuxtools.lttng.state.StateStrings.SoftIRQMode;
import org.eclipse.linuxtools.lttng.state.StateStrings.TrapMode;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.TmfTimeAnalysisProvider;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITmfTimeAnalysisEntry;

public class TimeRangeViewerProvider extends TmfTimeAnalysisProvider {
	// ========================================================================
	// Data
	// =======================================================================
	Map<String, StateColor> procStateToColor = new HashMap<String, StateColor>(
			16);
	Map<String, StateColor> bdevStateToColor = new HashMap<String, StateColor>(
			4);
	Map<String, StateColor> softIrqStateToColor = new HashMap<String, StateColor>(
			4);
	Map<String, StateColor> trapStateToColor = new HashMap<String, StateColor>(
			4);
	Map<String, StateColor> irqStateToColor = new HashMap<String, StateColor>(4);
	Map<String, StateColor> cpuStateToColor = new HashMap<String, StateColor>(8);

	// ========================================================================
	// Constructors
	// =======================================================================
	public TimeRangeViewerProvider() {
		// Fill the statemode to color maps
		fillProcessStateToColor();
		fillBdevStateToColor();
		fillSoftIRQStateToColor();
		fillTrapStateToColor();
		fillIrqStateToColor();
		fillCpuStateToColor();
	}

	// ========================================================================
	// Methods
	// =======================================================================
	@Override
	public StateColor getEventColor(ITimeEvent event) {
		StateColor retColor = null;
		
		if (event instanceof TimeRangeEvent) {
			TimeRangeEvent devent = (TimeRangeEvent) event;
			String stateMode = devent.getStateMode();
			switch (devent.getEventType()) {
			case PROCESS_MODE:
				retColor = procStateToColor.get(stateMode);
				break;
			case BDEV_MODE:
				retColor = bdevStateToColor.get(stateMode);
				break;
			case IRQ_MODE:
				retColor = irqStateToColor.get(stateMode);
				break;
			case SOFT_IRQ_MODE:
				retColor = softIrqStateToColor.get(stateMode);
				break;
			case CPU_MODE:
				retColor = cpuStateToColor.get(stateMode);
				break;
			case TRAP_MODE:
				retColor = trapStateToColor.get(stateMode);
				break;
			}
		}
		
		if (retColor == null) {
			return StateColor.MAGENTA3;
		}
		return retColor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.ui.widgets.timeAnalysis.api.TsfTmAbstractImplProvider#getStateName(org.eclipse.linuxtools.tmf.ui.widgets.timeAnalysis.api.TsfTmAbstractImplProvider.StateColor)
	 */
	@Override
	public String getStateName(StateColor color) {
		//Override to multiple instances of the widget, the same color can have multiple meanings
		return "Not mapped";
	}

	@Override
	public Map<String, String> getEventHoverToolTipInfo(ITimeEvent revent) {
		Map<String, String> toolTipEventMsgs = new HashMap<String, String>();
		//if the selected resource is a Process, add the Process type to the tool tip
		if (revent instanceof TimeRangeComponent) {
			ITimeRangeComponent parent = ((TimeRangeComponent) revent)
					.getEventParent();
			if (parent != null && parent instanceof TimeRangeEventProcess) {
				TimeRangeEventProcess localProcess = (TimeRangeEventProcess) parent;
				toolTipEventMsgs.put("Process Type", localProcess
						.getProcessType());
			}
		}

		return toolTipEventMsgs;
	}

	@Override
	public String getEventName(ITimeEvent event, boolean upper, boolean extInfo) {
		String name = null;
		//The relevant event name for the time range is the actual state mode
		if (event instanceof TimeRangeEvent) {
			TimeRangeEvent devent = (TimeRangeEvent) event;
			StringBuilder sb = new StringBuilder(devent.getStateMode());
			name = sb.toString();
		}
		
		if (name == null) {
			return "Unknown";
		}
		return name;
	}

	@Override
	public String getTraceClassName(ITmfTimeAnalysisEntry trace) {
		String name = "";
		if (trace instanceof TimeRangeComposite) {
			TimeRangeComposite dTrace = (TimeRangeComposite) trace;
			name = dTrace.getClassName();
		}
		return name;
	}

	private void fillCpuStateToColor() {
		cpuStateToColor.put(CpuMode.LTTV_CPU_UNKNOWN.getInName(),
				StateColor.BLACK);
		cpuStateToColor.put(CpuMode.LTTV_CPU_IDLE.getInName(), StateColor.GRAY);
		cpuStateToColor.put(CpuMode.LTTV_CPU_BUSY.getInName(),
				StateColor.LIGHT_BLUE);
		cpuStateToColor
				.put(CpuMode.LTTV_CPU_IRQ.getInName(), StateColor.ORANGE);
		cpuStateToColor.put(CpuMode.LTTV_CPU_SOFT_IRQ.getInName(),
				StateColor.PURPLE1);
		cpuStateToColor.put(CpuMode.LTTV_CPU_TRAP.getInName(), StateColor.GOLD);
	}

	private void fillIrqStateToColor() {
		irqStateToColor.put(IRQMode.LTTV_IRQ_UNKNOWN.getInName(),
				StateColor.BLACK);
		irqStateToColor.put(IRQMode.LTTV_IRQ_IDLE.getInName(), StateColor.GRAY);
		irqStateToColor.put(IRQMode.LTTV_IRQ_BUSY.getInName(),
				StateColor.ORANGE);
	}

	private void fillTrapStateToColor() {
		trapStateToColor.put(TrapMode.LTTV_TRAP_UNKNOWN.getInName(),
				StateColor.BLACK);
		trapStateToColor.put(TrapMode.LTTV_TRAP_IDLE.getInName(),
				StateColor.BLACK);
		trapStateToColor.put(TrapMode.LTTV_TRAP_BUSY.getInName(),
				StateColor.GOLD);
	}

	private void fillSoftIRQStateToColor() {
		softIrqStateToColor.put(SoftIRQMode.LTTV_SOFT_IRQ_UNKNOWN.getInName(),
				StateColor.BLACK);
		softIrqStateToColor.put(SoftIRQMode.LTTV_SOFT_IRQ_IDLE.getInName(),
				StateColor.BLACK);
		softIrqStateToColor.put(SoftIRQMode.LTTV_SOFT_IRQ_PENDING.getInName(),
				StateColor.PINK1);
		softIrqStateToColor.put(SoftIRQMode.LTTV_SOFT_IRQ_BUSY.getInName(),
				StateColor.PURPLE1);
	}

	private void fillBdevStateToColor() {
		softIrqStateToColor.put(BdevMode.LTTV_BDEV_UNKNOWN.getInName(),
				StateColor.BLACK);
		softIrqStateToColor.put(BdevMode.LTTV_BDEV_IDLE.getInName(),
				StateColor.BLACK);
		softIrqStateToColor.put(BdevMode.LTTV_BDEV_BUSY_READING.getInName(),
				StateColor.DARK_BLUE);
		softIrqStateToColor.put(BdevMode.LTTV_BDEV_BUSY_WRITING.getInName(),
				StateColor.RED);
	}

	private void fillProcessStateToColor() {
		// Process Status
		procStateToColor.put(ProcessStatus.LTTV_STATE_UNNAMED.getInName(),
				StateColor.GRAY);
		procStateToColor.put(ProcessStatus.LTTV_STATE_DEAD.getInName(),
				StateColor.BLACK);
		procStateToColor.put(ProcessStatus.LTTV_STATE_WAIT_FORK.getInName(),
				StateColor.DARK_GREEN);
		procStateToColor.put(ProcessStatus.LTTV_STATE_WAIT_CPU.getInName(),
				StateColor.DARK_YELLOW);
		procStateToColor.put(ProcessStatus.LTTV_STATE_EXIT.getInName(),
				StateColor.MAGENTA3);
		procStateToColor.put(ProcessStatus.LTTV_STATE_ZOMBIE.getInName(),
				StateColor.PURPLE1);
		procStateToColor.put(ProcessStatus.LTTV_STATE_WAIT.getInName(),
				StateColor.RED);

		// Execution Mode
		procStateToColor.put(ExecutionMode.LTTV_STATE_MODE_UNKNOWN.getInName(),
				StateColor.BLACK);
		procStateToColor.put(ExecutionMode.LTTV_STATE_USER_MODE.getInName(),
				StateColor.GREEN);
		procStateToColor.put(ExecutionMode.LTTV_STATE_SYSCALL.getInName(),
				StateColor.DARK_BLUE);
		procStateToColor.put(ExecutionMode.LTTV_STATE_TRAP.getInName(),
				StateColor.GOLD);
		procStateToColor.put(ExecutionMode.LTTV_STATE_IRQ.getInName(),
				StateColor.ORANGE);
		procStateToColor.put(ExecutionMode.LTTV_STATE_SOFT_IRQ.getInName(),
				StateColor.PINK1);
	}
}
