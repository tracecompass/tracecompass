/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Yann N. Dauphin     (dhaemon@gmail.com)  - Implementation for stats
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng.ui.views.statistics.evProcessor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings;
import org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.AbsEventToHandlerResolver;
import org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.ILttngEventProcessor;

/**
 * Provide the handlers that will count the CPU Time, Cumulative CPU Time and
 * Elapsed Time and update the appropriate tree.
 * 
 * Builds a Map from string event name to a processing handler object, the
 * processors implement the same interface to facilitate transparent methods
 * call,
 * 
 * The map key STring is the entry point of the raw events, using a hash speeds
 * up the resolution of the appropriate processor
 * 
 * @author alvaro
 * 
 */
public class StatsTimeCountHandlerFactory extends AbsEventToHandlerResolver {

	// -----------------------------------------------------------------------
	// Data
	// -----------------------------------------------------------------------

	private final Map<String, ILttngEventProcessor> eventNametoBeforeProcessor = new HashMap<String, ILttngEventProcessor>();
	ILttngEventProcessor afterhandler;
	private static StatsTimeCountHandlerFactory instance = null;
	private StatsTimeCountHandlers instantiateHandler = new StatsTimeCountHandlers();

	// -----------------------------------------------------------------------
	// Constructors
	// -----------------------------------------------------------------------

	private StatsTimeCountHandlerFactory() {
		super();
		//create one instance of each individual event handler and add the instance to the map
		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_SYSCALL_ENTRY
				.getInName(), instantiateHandler.getSyscallEntryBeforeHandler());
		
		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_SYSCALL_EXIT
				.getInName(), instantiateHandler.getsySyscallExitBeforeHandler());
		
		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_TRAP_ENTRY
				.getInName(), instantiateHandler.getTrapEntryBeforeHandler());
		
		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_TRAP_EXIT
				.getInName(), instantiateHandler.getTrapExitBeforeHandler());
		
		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_PAGE_FAULT_ENTRY
				.getInName(), instantiateHandler.getTrapEntryBeforeHandler());
		
		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_PAGE_FAULT_EXIT
				.getInName(), instantiateHandler.getTrapExitBeforeHandler());

		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_PAGE_FAULT_NOSEM_ENTRY
				.getInName(), instantiateHandler.getTrapEntryBeforeHandler());
		
		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_PAGE_FAULT_NOSEM_EXIT
				.getInName(), instantiateHandler.getTrapExitBeforeHandler());
		
		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_IRQ_ENTRY
				.getInName(), instantiateHandler.getIrqEntryBeforeHandler());
		
		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_IRQ_EXIT
				.getInName(), instantiateHandler.getIrqExitBeforeHandler());
		
		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_SOFT_IRQ_ENTRY
				.getInName(), instantiateHandler.getSoftIrqEntryBeforeHandler());
		
		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_SOFT_IRQ_EXIT
				.getInName(), instantiateHandler.getSoftIrqExitBeforeHandler());
		
		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_FUNCTION_ENTRY
				.getInName(), instantiateHandler.getFunctionEntryBeforeHandler());
		
		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_FUNCTION_EXIT
				.getInName(), instantiateHandler.getFunctionExitBeforeHandler());
		
		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_SCHED_SCHEDULE
				.getInName(), instantiateHandler.getSchedChangeBeforeHandler());
		
		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_PROCESS_EXIT
				.getInName(), instantiateHandler.getProcessExitHandler());
		
		afterhandler = instantiateHandler.getAfterHandler();

	}

	// -----------------------------------------------------------------------
	// Public methods
	// -----------------------------------------------------------------------
	/**
	 * The event processors are common to all traces an multiple instances will
	 * use more memory unnecessarily
	 * 
	 * @return
	 */
	public static AbsEventToHandlerResolver getInstance() {
		if (instance == null) {
			instance = new StatsTimeCountHandlerFactory();
		}
		return instance;
	}


	@Override
	public ILttngEventProcessor getAfterProcessor(String eventType) {
		return afterhandler;
	}

	@Override
	public ILttngEventProcessor getBeforeProcessor(String eventType) {
		return eventNametoBeforeProcessor.get(eventType);
	}

	@Override
	public ILttngEventProcessor getfinishProcessor() {
		return instantiateHandler.getTracesetEndHandler();
	}

	@Override
	public ILttngEventProcessor getStateUpdaterProcessor(String eventType) {
		return null;
	}
}
