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
package org.eclipse.linuxtools.lttng.ui.views.resources.evProcessor;

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.state.StateStrings.Events;
import org.eclipse.linuxtools.lttng.state.evProcessor.IEventProcessing;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;
import org.eclipse.linuxtools.lttng.ui.TraceDebug;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeEventResource;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

/**
 * Creates specific finish state data request
 * 
 * @author alvaro
 * 
 */
public class ResourcesTRangeFinishUpdateHandler extends
 AbsResourcesTRangeUpdate
		implements IEventProcessing {

	public Events getEventHandleType() {
		// No specific event
		return null;
	}

	public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {
		// Draw a last known state to the end of the trace
		TmfTimestamp endReqTime = traceSt.getInputDataRef()
				.getTraceTimeWindow().getEndTime();

		TraceDebug.debug("Number of localResources: "
				+ resContainer.readResources().length);

		// for each existing resource
		for (TimeRangeEventResource localResource : resContainer
				.readResources()) {

			// get the start time
			long stime = localResource.getNext_good_time();

			// Get the resource state mode
			String stateMode = localResource.getStateMode(traceSt);

			// Insert an instance from previous time to end request time with
			// the current state
			makeDraw(traceSt, stime, endReqTime.getValue(),
					localResource, params, stateMode);
		}

		return false;
	}

}
