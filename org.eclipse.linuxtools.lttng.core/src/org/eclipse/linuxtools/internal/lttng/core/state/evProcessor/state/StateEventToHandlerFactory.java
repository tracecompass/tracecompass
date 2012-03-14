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
 * 	 Michel Dagenais (michel.dagenais@polymtl.ca) - Reference C implementation, used with permission
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.state;

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
public class StateEventToHandlerFactory extends AbsEventToHandlerResolver {
	// ========================================================================
	// Data
	// =======================================================================
	private final Map<String, ILttngEventProcessor> eventNametoStateProcessor = new HashMap<String, ILttngEventProcessor>();
	private final static StateEventToHandlerFactory instance = new StateEventToHandlerFactory();
	private StateUpdateHandlers instantiateHandler = new StateUpdateHandlers();

	// ========================================================================
	// Constructors
	// =======================================================================
	protected StateEventToHandlerFactory() {
		//create one instance of each individual event handler and add the instance to the map
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_SYSCALL_ENTRY
				.getInName(), instantiateHandler.getSyscallEntryHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_SYSCALL_EXIT
				.getInName(), instantiateHandler.getsySyscallExitHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_TRAP_ENTRY
				.getInName(), instantiateHandler.getTrapEntryHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_TRAP_EXIT
				.getInName(), instantiateHandler.getTrapExitHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_PAGE_FAULT_ENTRY
				.getInName(), instantiateHandler.getTrapEntryHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_PAGE_FAULT_EXIT
				.getInName(), instantiateHandler.getTrapExitHandler());

		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_PAGE_FAULT_NOSEM_ENTRY
				.getInName(), instantiateHandler.getTrapEntryHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_PAGE_FAULT_NOSEM_EXIT
				.getInName(), instantiateHandler.getTrapExitHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_IRQ_ENTRY
				.getInName(), instantiateHandler.getIrqEntryHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_IRQ_EXIT
				.getInName(), instantiateHandler.getIrqExitHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_SOFT_IRQ_RAISE
				.getInName(), instantiateHandler.getSoftIrqRaiseHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_SOFT_IRQ_ENTRY
				.getInName(), instantiateHandler.getSoftIrqEntryHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_SOFT_IRQ_EXIT
				.getInName(), instantiateHandler.getSoftIrqExitHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_LIST_INTERRUPT
				.getInName(), instantiateHandler.getEnumInterruptHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_REQUEST_ISSUE
				.getInName(), instantiateHandler.getBdevRequestIssueHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_REQUEST_COMPLETE
				.getInName(), instantiateHandler.getBdevRequestCompleteHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_FUNCTION_ENTRY
				.getInName(), instantiateHandler.getFunctionEntryHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_FUNCTION_EXIT
				.getInName(), instantiateHandler.getFunctionExitHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_SYS_CALL_TABLE
				.getInName(), instantiateHandler.getDumpSyscallHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_KPROBE_TABLE
				.getInName(), instantiateHandler.getDumpKprobeHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_SOFTIRQ_VEC
				.getInName(), instantiateHandler.getDumpSoftIrqHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_SCHED_SCHEDULE
				.getInName(), instantiateHandler.getSchedChangeHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_PROCESS_FORK
				.getInName(), instantiateHandler.getProcessForkHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_KTHREAD_CREATE
				.getInName(), instantiateHandler.getProcessKernelThreadHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_PROCESS_EXIT
				.getInName(), instantiateHandler.getProcessExitHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_PROCESS_FREE
				.getInName(), instantiateHandler.getProcessFreeHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_EXEC
				.getInName(), instantiateHandler.getProcessExecHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_THREAD_BRAND
				.getInName(), instantiateHandler.GetThreadBrandHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_STATEDUMP_END
				.getInName(), instantiateHandler.getStateDumpEndHandler());
		
		eventNametoStateProcessor.put(StateStrings.Events.LTT_EVENT_PROCESS_STATE
				.getInName(), instantiateHandler.getEnumProcessStateHandler());


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
		return instance;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.state.evProcessor.AbsEventProcessorFactory#getAfterProcessor(java.lang.String)
	 */
	@Override
	public ILttngEventProcessor getAfterProcessor(String eventType) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.state.evProcessor.AbsEventProcessorFactory#getBeforeProcessor(java.lang.String)
	 */
	@Override
	public ILttngEventProcessor getBeforeProcessor(String eventType) {
		return null;
	}
	
	/**
	 * This is the only event handler to update the State provider
	 * @return 
	 * 
	 */
	@Override
	public ILttngEventProcessor getStateUpdaterProcessor(String eventType) {
		return eventNametoStateProcessor.get(eventType);
	}

	@Override
	public ILttngEventProcessor getfinishProcessor() {
		// No finishing processor used
		return null;
	}

}
