/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Alvaro Sanchez-Leon - Initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.controlflow.evProcessor;

import java.util.Vector;

import org.eclipse.linuxtools.lttng.state.StateStrings.ProcessStatus;
import org.eclipse.linuxtools.lttng.state.evProcessor.ILttngEventProcessor;
import org.eclipse.linuxtools.lttng.state.model.LttngProcessState;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeComponent;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeEvent;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeEventProcess;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeEvent.Type;
import org.eclipse.linuxtools.lttng.ui.views.common.AbsTRangeUpdate;
import org.eclipse.linuxtools.lttng.ui.views.common.ParamsUpdater;
import org.eclipse.linuxtools.lttng.ui.views.controlflow.model.FlowModelFactory;
import org.eclipse.linuxtools.lttng.ui.views.controlflow.model.FlowProcessContainer;

public abstract class AbsFlowTRangeUpdate extends AbsTRangeUpdate implements ILttngEventProcessor {

	// ========================================================================
	// Data
	// =======================================================================

	protected FlowProcessContainer procContainer = FlowModelFactory.getProcContainer();
	protected ParamsUpdater params = FlowModelFactory.getParamsUpdater();
	protected static final Long ANY_CPU = 0L;


	// ========================================================================
	// Methods
	// =======================================================================
	protected TimeRangeEventProcess addLocalProcess(LttngProcessState stateProcess, long traceStartTime, long traceEndTime, String traceId) {
		// TimeRangeEventProcess localProcess = new TimeRangeEventProcess(id, name, startTime, stopTime, groupName, className)
		TimeRangeEventProcess localProcess = new TimeRangeEventProcess(
				procContainer.getUniqueId(), stateProcess.getName(),
				traceStartTime, traceEndTime, "", stateProcess.getType()
						.getInName(), stateProcess.getCpu(), stateProcess
						.getInsertion_time());
		
		
		localProcess.setCreationTime(stateProcess.getCreation_time());
		localProcess.setPid(stateProcess.getPid());
		localProcess.setTgid(stateProcess.getTgid());
		localProcess.setPpid(stateProcess.getPpid());
		localProcess.setName(stateProcess.getName());
		localProcess.setBrand(stateProcess.getBrand());
		localProcess.setTraceID(traceId);
		localProcess.setProcessType(stateProcess.getType().getInName());
		procContainer.addItem(localProcess);
		return localProcess;
	}
	
	/**
	 * Used to check if the event is visible within the current visible time
	 * window
	 * 
	 * @return
	 */
	protected boolean withinViewRange(long stime, long etime) {
		long windowStartTime = params.getStartTime();
		long windowEndTime = params.getEndTime();

		// start time is within window
		if (stime >= windowStartTime && stime <= windowEndTime) {
			// The event or part of it shall be displayed.
			return true;
		}

		// end time is within window
		if (etime >= windowStartTime && etime <= windowEndTime) {
			// The event or part of it shall be displayed.
			return true;
		}

		// check that a portion is within the window
		if (stime < windowStartTime && etime > windowEndTime) {
			// The time range is bigger than the selected time window and
			// crosses it
			return true;
		}

		return false;
	}

	/**
	 * @param traceSt
	 * @param startTime
	 * @param endTime
	 * @param localProcess
	 * @param params
	 * @param stateMode
	 * @return
	 */
	protected boolean makeDraw(LttngTraceState traceSt, long startTime,
			long endTime, TimeRangeEventProcess localProcess,
			ParamsUpdater params, String stateMode) {

		// Determine start and end times to establish duration
		Long stime = startTime;
		Long etime = endTime;

//		if (etime == 13589873239052L || etime == 13589878374675L) {
//			System.out.println("probe here, " + stime + "->" + etime);
//		}

		if (etime < stime) {
			// Validate the sequential order of events
			params.incrementEventsDiscardedWrongOrder();
			return false;
		}

		// Store the next good time to start drawing the next event
		// this is done this early to display an accurate start time of the
		// first event
		// within the display window
		// ****** moved at the end since it produces gaps among the coloured rectangles
		// localProcess.setNext_good_time(etime);
		if (!withinViewRange(stime, etime)) {
			// No use to process the event since it's outside
			// the visible time range of the window
			params.incrementEventsDiscarded(ParamsUpdater.OUT_OF_VIEWRANGE);
			return false;
		}

		// Determine if the time range event will fit it the current
		// pixel map
		double duration = etime - stime;
		double k = getPixelsPerNs(traceSt, params);
		double pixels = duration * k;

		// Visibility check
		// Display a "more information" indication by allowing non visible event
		// as long as its previous event is visible.
		boolean visible = true;
		if (pixels < 1) {
			boolean prevEventVisibility = true;
			// Get the visibility indication on previous event for
			// this process
			Vector<TimeRangeComponent> inMemEvents = localProcess
					.getTraceEvents();
			if (inMemEvents.size() != 0) {
				TimeRangeComponent prevEvent = inMemEvents.get(inMemEvents
						.size() - 1);
				prevEventVisibility = prevEvent.isVisible();

				// if previous event visibility is false and the time span
				// between events less than two pixels, there is no need to
				// load it in memory i.e. not visible and a more indicator is
				// within two pixels.
				// return i.e. event discarded to free up memory
				Long eventSpan = stime - prevEvent.getStartTime();
				if (prevEventVisibility == false
						&& ((double) eventSpan * k) < 2) {

					// discard the item
					params.incrementEventsDiscarded(ParamsUpdater.NOT_VISIBLE);
					return false;

				}
			}

			// if previous event is visible, set this one to not
			// visible and continue
			visible = false;
		}

		// Create the time-range event
		TimeRangeEvent time_window = new TimeRangeEvent(stime, etime,
				localProcess, Type.PROCESS_MODE, stateMode);

		time_window.setVisible(visible);
		localProcess.getTraceEvents().add(time_window);
		localProcess.setNext_good_time(etime);

		return false;
	}

	/**
	 * @param traceSt
	 * @param evTime
	 * @param process
	 * @param localProcess
	 * @param params
	 * @return
	 */
	protected boolean makeDraw(LttngTraceState traceSt, long evTime,
			LttngProcessState process, TimeRangeEventProcess localProcess,
			ParamsUpdater params) {

		// TmfTimestamp stime = process.getState().getChange_LttTime();
		long stime = localProcess.getNext_good_time();

		String stateMode;
		ProcessStatus procStatus = process.getState().getProc_status();
		// Use Execution mode if process state is RUN otherwise use the actual
		// process state,
		// this selection will determine the actual color selected for the event
		if (procStatus == ProcessStatus.LTTV_STATE_RUN) {
			stateMode = process.getState().getExec_mode().getInName();
		} else {
			stateMode = procStatus.getInName();
		}

		return makeDraw(traceSt, stime, evTime, localProcess, params, stateMode);

	}

}