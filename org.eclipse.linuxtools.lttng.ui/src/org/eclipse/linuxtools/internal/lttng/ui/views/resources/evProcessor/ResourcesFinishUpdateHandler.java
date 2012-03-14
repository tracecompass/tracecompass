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
 * 	 Michel Dagenais (michel.dagenais@polymtl.ca) - Reference C implementation, used with permission
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.views.resources.evProcessor;

import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.Events;
import org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.ILttngEventProcessor;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngTraceState;
import org.eclipse.linuxtools.internal.lttng.ui.TraceDebug;
import org.eclipse.linuxtools.internal.lttng.ui.model.trange.TimeRangeEventResource;

/**
 * Creates specific finish state data request
 * 
 * @author alvaro
 * 
 */
public class ResourcesFinishUpdateHandler extends
 AbsResourcesTRangeUpdate
		implements ILttngEventProcessor {

	public Events getEventHandleType() {
		// No specific event
		return null;
	}

	@Override
	public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {
		// The end of the last state is unknown since it's beyond the requested time range window. Create this last
		// event to half page after the visible window but not beyond the end of trace
		long endOfTrace = traceSt.getContext().getTraceTimeWindow().getEndTime().getValue();
		long halfWindow = (params.getEndTime() - params.getStartTime()) / 2;

		// End of event common to all resources within the trace for this specific request
		long endOfEvent = params.getEndTime() + halfWindow;
		if (endOfEvent > endOfTrace) {
			endOfEvent = endOfTrace;
		}
		
		TraceDebug.debug("Number of localResources: " //$NON-NLS-1$
				+ resContainer.readItems().length);

		// for each existing resource
		for (TimeRangeEventResource localResource : resContainer
				.readItems()) {

			// get the start time
			long stime = localResource.getNext_good_time();

			// Get the resource state mode
			String stateMode = localResource.getStateMode(traceSt);

			// Insert an instance from previous time to end request time with
			// the current state
			makeDraw(traceSt, stime, endOfEvent, localResource, params, stateMode);
		}

		return false;
	}

}
