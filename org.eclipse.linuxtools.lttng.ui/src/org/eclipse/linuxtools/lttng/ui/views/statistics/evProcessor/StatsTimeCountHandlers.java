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
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.statistics.evProcessor;

import org.eclipse.linuxtools.lttng.state.StateStrings.Events;
import org.eclipse.linuxtools.lttng.state.evProcessor.IEventProcessing;

/**
 * Process the system call entry event
 * 
 * @author alvaro
 * 
 */
class StatsTimeCountHandlers {
	
	/**
	 * Method to handle the event: LTT_EVENT_SYSCALL_ENTRY
	 * 
	 * @return
	 */
	final IEventProcessing getSyscallEntryBeforeHandler() {
		AbstractStatsEventHandler handler = new StatsModeChangeHandler(Events.LTT_EVENT_SYSCALL_ENTRY);
		return handler;
	}
	
	/**
	 * Method to handle the event: LTT_EVENT_SYSCALL_EXIT
	 * 
	 * @return
	 */
	final IEventProcessing getsySyscallExitBeforeHandler() {
		AbstractStatsEventHandler handler = new StatsModeEndHandler(Events.LTT_EVENT_SYSCALL_EXIT);
		return handler;
	}
	
	/**
	 * Method to handle the event: LTT_EVENT_TRAP_ENTRY
	 * 
	 * @return
	 */
	final IEventProcessing getTrapEntryBeforeHandler() {
		AbstractStatsEventHandler handler = new StatsModeChangeHandler(Events.LTT_EVENT_TRAP_ENTRY);
		return handler;
	}
	
	/**
	 * Method to handle the event: LTT_EVENT_TRAP_EXIT
	 * 
	 * @return
	 */
	final IEventProcessing getTrapExitBeforeHandler() {
		AbstractStatsEventHandler handler = new StatsModeEndHandler(Events.LTT_EVENT_TRAP_EXIT);
		return handler;
	}
	
	/**
	 * Method to handle the event: LTT_EVENT_IRQ_ENTRY
	 * 
	 * @return
	 */
	final IEventProcessing getIrqEntryBeforeHandler() {
		AbstractStatsEventHandler handler = new StatsModeChangeHandler(Events.LTT_EVENT_IRQ_ENTRY);
		return handler;
	}
	
	/**
	 * Method to handle the event: LTT_EVENT_IRQ_EXIT
	 * 
	 * @return
	 */
	final IEventProcessing getIrqExitBeforeHandler() {
		AbstractStatsEventHandler handler = new StatsModeEndHandler(Events.LTT_EVENT_IRQ_EXIT);
		return handler;
	}
	
	/**
	 * Method to handle the event: LTT_EVENT_SOFT_IRQ_ENTRY
	 * 
	 * @return
	 */
	final IEventProcessing getSoftIrqEntryBeforeHandler() {
		AbstractStatsEventHandler handler = new StatsModeChangeHandler(Events.LTT_EVENT_SOFT_IRQ_ENTRY);
		return handler;
	}
	
	/**
	 * Method to handle the event: LTT_EVENT_SOFT_IRQ_EXIT
	 * 
	 * @return
	 */
	final IEventProcessing getSoftIrqExitBeforeHandler() {
		AbstractStatsEventHandler handler = new StatsModeEndHandler(Events.LTT_EVENT_SOFT_IRQ_EXIT);
		return handler;
	}

	/**
	 * <p>
	 * Handles event: LTT_EVENT_FUNCTION_ENTRY
	 * </p>
	 * <p>
	 * FIELDS: LTT_FIELD_THIS_FN, LTT_FIELD_CALL_SITE
	 * </p>
	 * 
	 * @return
	 */
	final IEventProcessing getFunctionEntryBeforeHandler() {
		AbstractStatsEventHandler handler = new StatsModeChangeHandler(Events.LTT_EVENT_FUNCTION_ENTRY);
		return handler;
	}

	/**
	 * 
	 * @return
	 */
	final IEventProcessing getFunctionExitBeforeHandler() {
		AbstractStatsEventHandler handler = new StatsModeEndHandler(Events.LTT_EVENT_FUNCTION_EXIT);
		return handler;
	}
	
	/**
	 * <p>
	 * Handles: LTT_EVENT_SCHED_SCHEDULE
	 * </p>
	 * <p>
	 * Fields: LTT_FIELD_PREV_PID, LTT_FIELD_NEXT_PID, LTT_FIELD_PREV_STATE
	 * </p>
	 * 
	 * @return
	 */
	final IEventProcessing getSchedChangeBeforeHandler() {
		AbstractStatsEventHandler handler = new StatsModeChangeHandler(Events.LTT_EVENT_SCHED_SCHEDULE);
		return handler;
	}
	
	/**
	 * <p>
	 * Handles: LTT_EVENT_SCHED_SCHEDULE
	 * </p>
	 * <p>
	 * Fields: LTT_FIELD_PREV_PID, LTT_FIELD_NEXT_PID, LTT_FIELD_PREV_STATE
	 * </p>
	 * 
	 * @return
	 */
	final IEventProcessing getSchedChangeAfterHandler() {
		AbstractStatsEventHandler handler = new StatsModeChangeHandler(Events.LTT_EVENT_SCHED_SCHEDULE);
		return handler;
	}
	
}
