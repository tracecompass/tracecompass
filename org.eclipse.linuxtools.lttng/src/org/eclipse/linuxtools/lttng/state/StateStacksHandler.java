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
package org.eclipse.linuxtools.lttng.state;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.eclipse.linuxtools.lttng.TraceDebug;
import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.state.evProcessor.AbsEventProcessorFactory;
import org.eclipse.linuxtools.lttng.state.evProcessor.EventProcessorProxy;
import org.eclipse.linuxtools.lttng.state.evProcessor.IEventProcessing;
import org.eclipse.linuxtools.lttng.state.model.ILttngStateInputRef;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.trace.TmfTrace;

/**
 * @author Alvaro
 * 
 */
public class StateStacksHandler {
	// ========================================================================
	// Table data
	// =======================================================================
	protected LttngTraceState traceStateModel = null;
	protected Set<String> eventsNotHandled = null;
	protected Vector<IEventProcessing> listeners = new Vector<IEventProcessing>();

	// ========================================================================
	// Constructors
	// ========================================================================
	public StateStacksHandler(LttngTraceState model) {
		// It's assumed to have one instance of this class per Trace
		this.traceStateModel = model;
	}

	// ========================================================================
	// Methods
	// =======================================================================
	/**
	 * Initialised by manager, any time a JniTrace selection is updated
	 * 
	 * @param trace
	 * @param log
	 * 
	 */
	void init(JniTrace trace, TmfTrace log) throws LttngStateException {
		if (trace == null || log == null) {
			StringBuilder sb = new StringBuilder(
					"No JniTrace object available, trace must be set via method setTrace(JniTrace trace)");
			throw new LttngStateException(sb.toString());
		}

		// this.trace = trace;
		ILttngStateInputRef ref = new LttngStateInputRef(trace, log);
		this.traceStateModel.init(ref);
	}


	void processEvent(TmfEvent tmfEvent) /* throws LttngStateException */{
		if (tmfEvent == null) {
			return;
		}

		if (!(tmfEvent instanceof LttngEvent)) {
			TraceDebug
					.debug("The event received is not an instance of LttngEvent and can not be processed");
		}

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
			Set<AbsEventProcessorFactory> handlerRegister = EventProcessorProxy
					.getInstance().getProcessingFactories();

			// Notify the state BEFORE update handlers
			for (Iterator<AbsEventProcessorFactory> iterator = handlerRegister
					.iterator(); iterator.hasNext();) {
				AbsEventProcessorFactory handlerRegistry = (AbsEventProcessorFactory) iterator
						.next();
				IEventProcessing handler = handlerRegistry
						.getBeforeProcessor(inEventName);
				if (handler != null) {
					// process State Update
					handler.process(trcEvent, traceStateModel);
				}

			}

			// Notify the STATE UPDATE handlers
			// Only one state update expected
			for (Iterator<AbsEventProcessorFactory> iterator = handlerRegister
					.iterator(); iterator.hasNext();) {
				AbsEventProcessorFactory handlerRegistry = (AbsEventProcessorFactory) iterator
						.next();
				IEventProcessing handler = handlerRegistry
						.getStateUpdaterProcessor(inEventName);
				if (handler != null) {
					// process State Update
					handler.process(trcEvent, traceStateModel);
				}

			}

			// Notify the AFTER update handlers
			for (Iterator<AbsEventProcessorFactory> iterator = handlerRegister
					.iterator(); iterator.hasNext();) {
				AbsEventProcessorFactory handlerRegistry = (AbsEventProcessorFactory) iterator
						.next();
				IEventProcessing handler = handlerRegistry
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
	Set<String> getEventsNotHandled() {
		return eventsNotHandled;
	}
	
	/**
     * Needed for checkpoint
     * 
     * @param LttngTraceState
     */
    public LttngTraceState getTraceStateModel() {
        return traceStateModel;
    }
    
    /**
     * Needed for checkpoint
     * 
     * @param LttngTraceState
     */
    public void setTraceStateModel(LttngTraceState newTraceState) {
        traceStateModel = newTraceState;
    }
	
	/**
	 * Needed for verification purposes
	 * 
	 * @param listener
	 */
	void registerListener(IEventProcessing listener) {
		this.listeners.add(listener);
	}

	/**
	 * Needed for verification purposes
	 * 
	 * @param listener
	 */
	void deregisterListener(IEventProcessing listener) {
		this.listeners.remove(listener);
	}
}
