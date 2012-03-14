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
package org.eclipse.linuxtools.internal.lttng.ui.views.controlflow.evProcessor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings;
import org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.AbsEventToHandlerResolver;
import org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.ILttngEventProcessor;

/**
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
public class FlowEventToHandlerFactory extends AbsEventToHandlerResolver {
	// ========================================================================
	// Data
	// =======================================================================
	private final Map<String, ILttngEventProcessor> eventNametoBeforeProcessor = new HashMap<String, ILttngEventProcessor>();
	private final Map<String, ILttngEventProcessor> eventNametoAfterProcessor = new HashMap<String, ILttngEventProcessor>();
	private ILttngEventProcessor finishProcesor = null;
	private static FlowEventToHandlerFactory instance = null;
	private FlowBeforeUpdateHandlers instantiateBeforeHandler = new FlowBeforeUpdateHandlers();
	private FlowAfterUpdateHandlers instantiateAfterHandler = new FlowAfterUpdateHandlers();

	// ========================================================================
	// Constructors
	// =======================================================================
	private FlowEventToHandlerFactory() {
		super();
		// Create one instance of each individual event handler and add the
		// instance to the map

		// BEFORE HOOKS
		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_SYSCALL_ENTRY.getInName(),
				instantiateBeforeHandler.getStateModesHandler());

		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_SYSCALL_EXIT.getInName(),
				instantiateBeforeHandler.getStateModesHandler());

		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_TRAP_ENTRY
				.getInName(), instantiateBeforeHandler.getStateModesHandler());

		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_TRAP_EXIT
				.getInName(), instantiateBeforeHandler.getStateModesHandler());

		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_PAGE_FAULT_ENTRY.getInName(),
				instantiateBeforeHandler.getStateModesHandler());

		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_PAGE_FAULT_EXIT.getInName(),
				instantiateBeforeHandler.getStateModesHandler());

		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_PAGE_FAULT_NOSEM_ENTRY
						.getInName(), instantiateBeforeHandler
						.getStateModesHandler());

		eventNametoBeforeProcessor
				.put(StateStrings.Events.LTT_EVENT_PAGE_FAULT_NOSEM_EXIT
						.getInName(), instantiateBeforeHandler
						.getStateModesHandler());

		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_IRQ_ENTRY
				.getInName(), instantiateBeforeHandler.getStateModesHandler());

		eventNametoBeforeProcessor.put(StateStrings.Events.LTT_EVENT_IRQ_EXIT
				.getInName(), instantiateBeforeHandler.getStateModesHandler());

		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_SOFT_IRQ_ENTRY.getInName(),
				instantiateBeforeHandler.getStateModesHandler());

		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_SOFT_IRQ_EXIT.getInName(),
				instantiateBeforeHandler.getStateModesHandler());

		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_SCHED_SCHEDULE.getInName(),
				instantiateBeforeHandler.getBeforeSchedChangeHandler());

		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_PROCESS_EXIT.getInName(),
				instantiateBeforeHandler.getProcessExitHandler());

		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_PROCESS_FREE.getInName(),
				instantiateBeforeHandler.getProcessFreeHandler());

		eventNametoBeforeProcessor.put(
				StateStrings.Events.LTT_EVENT_STATEDUMP_END.getInName(),
				instantiateBeforeHandler.getStateDumpEndHandler());


		// AFTER HOOKS
		eventNametoAfterProcessor.put(
				StateStrings.Events.LTT_EVENT_SCHED_SCHEDULE.getInName(),
				instantiateAfterHandler.getSchedChangeHandler());

		eventNametoAfterProcessor.put(
				StateStrings.Events.LTT_EVENT_PROCESS_FORK.getInName(),
				instantiateAfterHandler.getProcessForkHandler());

		eventNametoAfterProcessor.put(
				StateStrings.Events.LTT_EVENT_PROCESS_EXIT.getInName(),
				instantiateAfterHandler.getProcessExitHandler());

		eventNametoAfterProcessor.put(StateStrings.Events.LTT_EVENT_EXEC
				.getInName(), instantiateAfterHandler.getProcessExecHandler());

		eventNametoAfterProcessor.put(
				StateStrings.Events.LTT_EVENT_THREAD_BRAND.getInName(),
				instantiateAfterHandler.GetThreadBrandHandler());

		eventNametoAfterProcessor.put(
				StateStrings.Events.LTT_EVENT_PROCESS_STATE.getInName(),
				instantiateAfterHandler.getEnumProcessStateHandler());

		finishProcesor = new FlowFinishUpdateHandler();
	}

	// ========================================================================
	// Public methods
	// =======================================================================
	/**
	 * The event processors are common to all traces an multiple instances will
	 * use more memory unnecessarily
	 * 
	 * @return
	 */
	public static AbsEventToHandlerResolver getInstance() {
		if (instance == null) {
			instance = new FlowEventToHandlerFactory();
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
		return finishProcesor;
	}

	@Override
	public ILttngEventProcessor getStateUpdaterProcessor(String eventType) {
		return null;
	}
}
