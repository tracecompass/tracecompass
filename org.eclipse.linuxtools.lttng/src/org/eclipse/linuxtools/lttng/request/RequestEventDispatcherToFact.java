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
package org.eclipse.linuxtools.lttng.request;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.eclipse.linuxtools.lttng.TraceDebug;
import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.state.evProcessor.AbsEventToHandlerResolver;
import org.eclipse.linuxtools.lttng.state.evProcessor.EventProcessorProxy;
import org.eclipse.linuxtools.lttng.state.evProcessor.IBaseEventProcessor;
import org.eclipse.linuxtools.lttng.state.evProcessor.IEventToHandlerResolver;
import org.eclipse.linuxtools.lttng.state.evProcessor.ILttngEventProcessor;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;
import org.eclipse.linuxtools.tmf.event.TmfEvent;

/**
 * @author Alvaro
 * 
 */
public class RequestEventDispatcherToFact implements IBaseEventProcessor {
	// ========================================================================
	// Table data
	// =======================================================================
	protected Set<String> eventsNotHandled = null;
	protected Vector<ILttngEventProcessor> listeners = new Vector<ILttngEventProcessor>();

	// ========================================================================
	// Constructors
	// ========================================================================
	public RequestEventDispatcherToFact(IEventToHandlerResolver handlerRegistry) {

	}

	// ========================================================================
	// Methods
	// =======================================================================


	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.state.trace.IEventDispatcher#processEvent(org.eclipse.linuxtools.tmf.event.TmfEvent)
	 */
	public void process(TmfEvent tmfEvent, LttngTraceState traceStateModel) {
		if (tmfEvent == null) {
			return;
		}

		if (!(tmfEvent instanceof LttngEvent)) {
			TraceDebug
					.debug("The event received is not an instance of LttngEvent and can not be processed");
			return;
		}

//		System.out.println("Event: " + nbEvents++ + ", timestamp: " + tmfEvent.getTimestamp());
		
		LttngEvent trcEvent = (LttngEvent) tmfEvent;
//		LttngEventField[] fields = ((LttngEventContent)trcEvent.getContent()).getFields();

		if (trcEvent != null) {
			String inEventName = trcEvent.getMarkerName();
			// String inChannel = trcEvent.getChannelName();
			// TraceDebug.debug("Event: " + inEventName);

			// Check if the received event is a transition state event
			// TODO: Remove temporarily to allow other events to go to the
			// statistics view.
			// Needs restructuring.
			// Events eventStruct = StateStrings.getInstance()
			// .getStateTransEventMap().get(inEventName);
			// if (eventStruct != null) {
			// String expectedChannel = eventStruct.getParent().getInName();
			// check that received channel is the expected channel in the
			// structure
			// if (inChannel.equals(expectedChannel)) {
			// Notify the before Handlers
			Set<AbsEventToHandlerResolver> handlerRegister = EventProcessorProxy
					.getInstance().getProcessingFactories();
			// Notify the state BEFORE update handlers
			for (Iterator<AbsEventToHandlerResolver> iterator = handlerRegister
					.iterator(); iterator.hasNext();) {
				IEventToHandlerResolver handlerRegistry = (IEventToHandlerResolver) iterator
						.next();
				ILttngEventProcessor handler = handlerRegistry
						.getBeforeProcessor(inEventName);
				if (handler != null) {
					// process State Update
					handler.process(trcEvent, traceStateModel);
				}

			}
			
			// Notify the STATE UPDATE handlers
			// Only one state update expected
			for (Iterator<AbsEventToHandlerResolver> iterator = handlerRegister
					.iterator(); iterator.hasNext();) {
				IEventToHandlerResolver handlerRegistry = (IEventToHandlerResolver) iterator
						.next();
				ILttngEventProcessor handler = handlerRegistry
						.getStateUpdaterProcessor(inEventName);
				if (handler != null) {
					// process State Update
					handler.process(trcEvent, traceStateModel);
				}

			}

			// Notify the AFTER update handlers
			for (Iterator<AbsEventToHandlerResolver> iterator = handlerRegister
					.iterator(); iterator.hasNext();) {
				IEventToHandlerResolver handlerRegistry = (IEventToHandlerResolver) iterator
						.next();
				ILttngEventProcessor handler = handlerRegistry
						.getAfterProcessor(inEventName);
				if (handler != null) {
					// process State Update
					handler.process(trcEvent, traceStateModel);
				}
			}

			// } else {
			// StringBuilder sb = new StringBuilder(
			// "Unexpected channel received for: " + inEventName
			// + ", channel rec: " + inChannel
			// + " chanel expected: " + expectedChannel);
			// TraceDebug.debug(sb.toString());
			// }
			// }
		}
	}

	/**
	 * Used for troubleshooting when debug mode is on
	 * 
	 * @return
	 */
	public Set<String> getEventsNotHandled() {
		return eventsNotHandled;
	}
}
