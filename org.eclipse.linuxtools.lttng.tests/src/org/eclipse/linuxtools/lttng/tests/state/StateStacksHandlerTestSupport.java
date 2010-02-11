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
package org.eclipse.linuxtools.lttng.tests.state;

import org.eclipse.linuxtools.lttng.TraceDebug;
import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.state.StateStacksHandler;
import org.eclipse.linuxtools.lttng.state.StateStrings;
import org.eclipse.linuxtools.lttng.state.StateStrings.Events;
import org.eclipse.linuxtools.lttng.state.evProcessor.IEventProcessing;
import org.eclipse.linuxtools.lttng.state.evProcessor.state.StateUpdateFactory;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;
import org.eclipse.linuxtools.lttng.tests.state.handlers.after.StateAfterUpdateFactory;
import org.eclipse.linuxtools.lttng.tests.state.handlers.before.StateBeforeUpdateFactory;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfEventField;

/**
 * @author Alvaro
 * 
 */
public class StateStacksHandlerTestSupport extends StateStacksHandler {

	// ========================================================================
	// Table data
	// =======================================================================

	// private static final Long LTTNG_STATE_SAVE_INTERVAL = 50000L;
	// private JniTrace trace = null;

	// ========================================================================
	// Constructors
	// ========================================================================
	StateStacksHandlerTestSupport(LttngTraceState model) {
		// It's assumed to have one instance of this class per "TraceSet"
		super(model);
	}

	// ========================================================================
	// Methods
	// =======================================================================

	@Override
	protected void processEvent(TmfEvent tmfEvent) /* throws LttngStateException */{
		if (tmfEvent == null) {
			return;
		}

		if (!(tmfEvent instanceof LttngEvent)) {
			TraceDebug.debug("The event received is not an instance of LttngEvent and can not be processed");
		}

		LttngEvent trcEvent = (LttngEvent) tmfEvent;
		TmfEventField[] fields = trcEvent.getContent().getFields();

		if (fields != null) {
			String inChannel = trcEvent.getChannelName();
			String inEventName = trcEvent.getMarkerName();
			// TraceDebug.debug("Event: " + inEventName);

			// Check if the received event is a transition state event
			Events eventStruct = StateStrings.getInstance()
					.getStateTransEventMap().get(inEventName);
			if (eventStruct != null) {
				String expectedChannel = eventStruct.getParent().getInName();
				// check that received channel is the expected channel in the
				// structure
				if (inChannel.equals(expectedChannel)) {
					IEventProcessing handlerBefore = StateBeforeUpdateFactory.getInstance()
					.getEventNametoProcessor(inEventName);

					IEventProcessing handler = StateUpdateFactory.getInstance()
					.getStateUpdaterProcessor(inEventName);
					
					IEventProcessing handlerAfter = StateAfterUpdateFactory.getInstance()
					.getEventNametoProcessor(inEventName);
					
					
					//Establish test reference calling the before handler
					if(handlerBefore != null) {
						handlerBefore.process(trcEvent, traceStateModel);
					} 
					
					//Execute the actual test action
					if (handler != null) {
						// process State Update
						handler.process(trcEvent, traceStateModel);
					} else {
						if (TraceDebug.isDEBUG()) {
							eventsNotHandled.add(inEventName);
						}
					}
					
					//After processing verify the effects over the base state.
					if(handlerAfter != null) {
						handlerAfter.process(trcEvent, traceStateModel);
					}

				} else {
					StringBuilder sb = new StringBuilder(
							"Unexpected channel received for: " + inEventName
									+ ", channel rec: " + inChannel
									+ " chanel expected: " + expectedChannel);
					TraceDebug.debug(sb.toString());
				}
			}
		}
	}


}
