/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon - Initial implementation
 * 	 Michel Dagenais (michel.dagenais@polymtl.ca) - Reference C implementation, used with permission
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.controlflow.evProcessor;

import java.util.Vector;

import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.Events;
import org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.ILttngEventProcessor;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngProcessState;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngTraceState;
import org.eclipse.linuxtools.lttng.ui.TraceDebug;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeComponent;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeEvent;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeEventProcess;

/**
 * Creates specific finish state data request
 * 
 * @author alvaro
 * 
 */
public class FlowFinishUpdateHandler extends AbsFlowTRangeUpdate
		implements ILttngEventProcessor {

	public Events getEventHandleType() {
		// No specific event
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {
		// The end of the last state is unknown since it's beyond the requested time range window. Create this last
		// event to half page after the visible window but not beyond the end of trace
		long endOfTrace = traceSt.getContext().getTraceTimeWindow().getEndTime().getValue();
		long halfWindow = (params.getEndTime() - params.getStartTime()) / 2;

		// End of event common to all processes within the trace for this specific request
		long endOfEvent = params.getEndTime() + halfWindow;
		if (endOfEvent > endOfTrace) {
			endOfEvent = endOfTrace;
		}

		TraceDebug.debug("Number of localProcesses: " + procContainer.readItems().length); //$NON-NLS-1$
		// to identify the process relevant to the traceState
		String traceId = traceSt.getTraceId();
		int numLocalFound = 0;
		int numLocalNotFound = 0;
		int numWithNoChildren = 0;
		for (TimeRangeEventProcess localProcess : procContainer.readItems()) {
			LttngProcessState stateProcess = lttv_state_find_process(traceSt,
					localProcess.getCpu(), localProcess.getPid());

			// Drawing the last state for processes related to the current trace
			// id.
			if (!localProcess.getTraceID().equals(traceId)) {
				continue;
			}

			// Check if the process is in the state provider, it is the case
			// when the requested time frame did not include any events for a
			// process
			if (stateProcess == null) {
				// Get Start time from the end time of previous event
				Vector<TimeRangeComponent> childrenEvents = localProcess
						.getTraceEvents();
				long nextGoodTime;
				String stateMode;
				if (childrenEvents.size() > 0) {
					TimeRangeComponent prevEvent = childrenEvents
							.get(childrenEvents.size() - 1);
					if (prevEvent instanceof TimeRangeEvent) {
						TimeRangeEvent prevTimeRange = (TimeRangeEvent) prevEvent;
						// calculate the next good time to draw the event
						// nextGoodTime = prevTimeRange.getStopTime() + 1;
						nextGoodTime = localProcess.getNext_good_time();
						stateMode = prevTimeRange.getStateMode();

						// Draw with the Local information since the current
						// request did
						// not contain events related to this process
						makeDraw(traceSt, nextGoodTime, endOfEvent, localProcess, params, stateMode);
					} else {
						TraceDebug.debug("previous event not instance of TimeRangeEvent?: " //$NON-NLS-1$
								+ prevEvent.getClass().getSimpleName());
					}
				} else {
					numWithNoChildren++;
				}

				numLocalNotFound++;
				continue;
			}

			numLocalFound++;
			// Draw the last state for this process
			makeDraw(traceSt, endOfEvent, stateProcess, localProcess, params);
		}

		TraceDebug.debug("Print Last Event: NumLocalFound " + numLocalFound	 //$NON-NLS-1$
				+ "; NumLocalNotFound: " + numLocalNotFound //$NON-NLS-1$
				+ "; NumWithNoChildren: " + numWithNoChildren); //$NON-NLS-1$

		return false;
	}

}
