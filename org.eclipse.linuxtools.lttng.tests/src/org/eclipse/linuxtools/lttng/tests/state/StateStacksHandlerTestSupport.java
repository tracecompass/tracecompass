/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
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
import org.eclipse.linuxtools.lttng.request.RequestEventDispatcher;
import org.eclipse.linuxtools.lttng.state.StateStrings;
import org.eclipse.linuxtools.lttng.state.StateStrings.Events;
import org.eclipse.linuxtools.lttng.state.evProcessor.IEventToHandlerResolver;
import org.eclipse.linuxtools.lttng.state.evProcessor.ILttngEventProcessor;
import org.eclipse.linuxtools.lttng.state.evProcessor.state.StateEventToHandlerFactory;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;
import org.eclipse.linuxtools.lttng.tests.state.handlers.after.StateAfterUpdateFactory;
import org.eclipse.linuxtools.lttng.tests.state.handlers.before.StateBeforeUpdateFactory;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfEventField;

/**
 * @author Alvaro
 * 
 */
public class StateStacksHandlerTestSupport extends RequestEventDispatcher {

	// ========================================================================
	// Table data
	// =======================================================================

	// private static final Long LTTNG_STATE_SAVE_INTERVAL = 50000L;
	// private JniTrace trace = null;

	// ========================================================================
	// Constructors
	// ========================================================================
	public StateStacksHandlerTestSupport(IEventToHandlerResolver handlerRegistry) {
		super(handlerRegistry);
	}

	// ========================================================================
	// Methods
	// =======================================================================

	@Override
	public void process(TmfEvent tmfEvent, LttngTraceState traceStateModel) {
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
					ILttngEventProcessor handlerBefore = StateBeforeUpdateFactory.getInstance()
					.getEventNametoProcessor(inEventName);

					ILttngEventProcessor handler = StateEventToHandlerFactory.getInstance()
					.getStateUpdaterProcessor(inEventName);
					
					ILttngEventProcessor handlerAfter = StateAfterUpdateFactory.getInstance()
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
