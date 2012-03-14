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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngSyntheticEvent;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngSyntheticEvent.SequenceInd;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings;
import org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.AbsEventToHandlerResolver;
import org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.ILttngEventProcessor;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngTraceState;

/**
 * Builds a Map from string event name to a processing handler object, the
 * processors implement the same interface to facilitate transparent methods
 * call,
 * 
 * The map key String is the entry point of the raw events. Using a hash speeds
 * up the resolution of the appropriate processor
 * 
 * @author alvaro
 * 
 */
public class ResourcesEventToHandlerFactory extends AbsEventToHandlerResolver {
	// ========================================================================
	// Data
	// =======================================================================
	private final Map<String, ILttngEventProcessor> eventNametoBeforeProcessor = new HashMap<String, ILttngEventProcessor>();
	private final Map<String, ILttngEventProcessor> eventNametoAfterProcessor = new HashMap<String, ILttngEventProcessor>();
	private ResourcesFinishUpdateHandler finishProcessor = null;
	private static ResourcesEventToHandlerFactory instance = null;
	private ResourcesBeforeUpdateHandlers instantiateBeforeHandler = new ResourcesBeforeUpdateHandlers();
	private ResourcesAfterUpdateHandlers instantiateAfterHandler = new ResourcesAfterUpdateHandlers();

	private ResourcesEventToHandlerFactory() {
		super();
		// Create one instance of each individual event handler and add the
		// instance to the map

		// *** BEFORE HOOKS ***
		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_SCHED_SCHEDULE.getInName(),
				instantiateBeforeHandler.getBeforeSchedChangeHandler());

		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_TRAP_ENTRY
				.getInName(), instantiateBeforeHandler
				.getBeforeExecutionModeTrap());
		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_TRAP_EXIT
				.getInName(), instantiateBeforeHandler
				.getBeforeExecutionModeTrap());
		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_PAGE_FAULT_ENTRY.getInName(),
				instantiateBeforeHandler.getBeforeExecutionModeTrap());
		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_PAGE_FAULT_EXIT.getInName(),
				instantiateBeforeHandler.getBeforeExecutionModeTrap());
		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_PAGE_FAULT_NOSEM_ENTRY
						.getInName(), instantiateBeforeHandler
						.getBeforeExecutionModeTrap());
		eventNametoBeforeProcessor
				.put(StateStrings.Events.LTT_EVENT_PAGE_FAULT_NOSEM_EXIT
						.getInName(), instantiateBeforeHandler
						.getBeforeExecutionModeTrap());

		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_IRQ_ENTRY
				.getInName(), instantiateBeforeHandler
				.getBeforeExecutionModeIrq());
		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_IRQ_EXIT
				.getInName(), instantiateBeforeHandler
				.getBeforeExecutionModeIrq());

		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_SOFT_IRQ_RAISE.getInName(),
				instantiateBeforeHandler.getBeforeExecutionModeSoftIrq());
		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_SOFT_IRQ_ENTRY.getInName(),
				instantiateBeforeHandler.getBeforeExecutionModeSoftIrq());
		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_SOFT_IRQ_EXIT.getInName(),
				instantiateBeforeHandler.getBeforeExecutionModeSoftIrq());

		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_REQUEST_ISSUE.getInName(),
				instantiateBeforeHandler.getBeforeBdevEvent());
		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_REQUEST_COMPLETE.getInName(),
				instantiateBeforeHandler.getBeforeBdevEvent());

		// *** AFTER HOOKS ***
		eventNametoAfterProcessor.put(
				StateStrings.Events.LTT_EVENT_SCHED_SCHEDULE.getInName(),
				instantiateAfterHandler.getAfterSchedChangeHandler());

		finishProcessor = new ResourcesFinishUpdateHandler();
	}

	/**
     * 
     */
	public static AbsEventToHandlerResolver getInstance() {
		if (instance == null) {
			instance = new ResourcesEventToHandlerFactory();
		}
		return instance;
	}

	@Override
	public ILttngEventProcessor getAfterProcessor(String eventType) {
		return eventNametoAfterProcessor.get(eventType);
	}

	@Override
	public ILttngEventProcessor getBeforeProcessor(String eventType) {
		return eventNametoBeforeProcessor.get(eventType);
	}

	@Override
	public ILttngEventProcessor getfinishProcessor() {
		return finishProcessor;
	}

	@Override
	public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {
		if (trcEvent instanceof LttngSyntheticEvent) {

			LttngSyntheticEvent synEvent = (LttngSyntheticEvent) trcEvent;
			String eventType = synEvent.getMarkerName();
			ILttngEventProcessor processor = null;
			if (synEvent.getSynType() == SequenceInd.BEFORE) {
				processor = getBeforeProcessor(eventType);
			}

			if (synEvent.getSynType() == SequenceInd.AFTER) {
				processor = getAfterProcessor(eventType);
			}

			if (synEvent.getSynType() == SequenceInd.ENDREQ) {
				processor = getfinishProcessor();
			}

			if (processor != null) {
				processor.process(trcEvent, traceSt);
			}
		}
		return false;
	}

	@Override
	public ILttngEventProcessor getStateUpdaterProcessor(String eventType) {
		return null;
	}
}
