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

import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.Events;
import org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.ILttngEventProcessor;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngTraceState;

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
	final ILttngEventProcessor getSyscallEntryBeforeHandler() {
		AbstractStatsEventHandler handler = new StatsModeChangeHandler(Events.LTT_EVENT_SYSCALL_ENTRY);
		return handler;
	}
	
	/**
	 * Method to handle the event: LTT_EVENT_SYSCALL_EXIT
	 * 
	 * @return
	 */
	final ILttngEventProcessor getsySyscallExitBeforeHandler() {
		AbstractStatsEventHandler handler = new StatsModeEndHandler(Events.LTT_EVENT_SYSCALL_EXIT);
		return handler;
	}
	
	/**
	 * Method to handle the event: LTT_EVENT_TRAP_ENTRY
	 * 
	 * @return
	 */
	final ILttngEventProcessor getTrapEntryBeforeHandler() {
		AbstractStatsEventHandler handler = new StatsModeChangeHandler(Events.LTT_EVENT_TRAP_ENTRY);
		return handler;
	}
	
	/**
	 * Method to handle the event: LTT_EVENT_TRAP_EXIT
	 * 
	 * @return
	 */
	final ILttngEventProcessor getTrapExitBeforeHandler() {
		AbstractStatsEventHandler handler = new StatsModeEndHandler(Events.LTT_EVENT_TRAP_EXIT);
		return handler;
	}
	
	/**
	 * Method to handle the event: LTT_EVENT_IRQ_ENTRY
	 * 
	 * @return
	 */
	final ILttngEventProcessor getIrqEntryBeforeHandler() {
		AbstractStatsEventHandler handler = new StatsModeChangeHandler(Events.LTT_EVENT_IRQ_ENTRY);
		return handler;
	}
	
	/**
	 * Method to handle the event: LTT_EVENT_IRQ_EXIT
	 * 
	 * @return
	 */
	final ILttngEventProcessor getIrqExitBeforeHandler() {
		AbstractStatsEventHandler handler = new StatsModeEndHandler(Events.LTT_EVENT_IRQ_EXIT);
		return handler;
	}
	
	/**
	 * Method to handle the event: LTT_EVENT_SOFT_IRQ_ENTRY
	 * 
	 * @return
	 */
	final ILttngEventProcessor getSoftIrqEntryBeforeHandler() {
		AbstractStatsEventHandler handler = new StatsModeChangeHandler(Events.LTT_EVENT_SOFT_IRQ_ENTRY);
		return handler;
	}
	
	/**
	 * Method to handle the event: LTT_EVENT_SOFT_IRQ_EXIT
	 * 
	 * @return
	 */
	final ILttngEventProcessor getSoftIrqExitBeforeHandler() {
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
	final ILttngEventProcessor getFunctionEntryBeforeHandler() {
		AbstractStatsEventHandler handler = new StatsModeChangeHandler(Events.LTT_EVENT_FUNCTION_ENTRY);
		return handler;
	}

	/**
	 * 
	 * @return
	 */
	final ILttngEventProcessor getFunctionExitBeforeHandler() {
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
	final ILttngEventProcessor getSchedChangeBeforeHandler() {
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
	final ILttngEventProcessor getAfterHandler() {
		AbstractStatsEventHandler handler = new StatsModeChangeHandler(null) {
			int sched_hash = StateStrings.Events.LTT_EVENT_SCHED_SCHEDULE.getInName().hashCode();
			@Override
			public boolean process(LttngEvent event, LttngTraceState traceState) {
				// Step the event counter for any after event
				stepCount(event, traceState);

				int eventNameHash = event.getMarkerName().hashCode();
				// specific processing for after sched schedule
				if (sched_hash == eventNameHash
						&& event.getMarkerName().equals(StateStrings.Events.LTT_EVENT_SCHED_SCHEDULE.getInName())) {
					return super.process(event, traceState);
				}

				return false;
			}
		};

		return handler;
	}
	/**
	 * <h4>Get the trace end handler</h4>
	 * <p>Allow to do some calculations when the trace is finished.</p>
	 * @return The handler.
	 */
	ILttngEventProcessor getTracesetEndHandler() {
		return new StatsTracesetEndHandler();
	}
	/**
	 * <h4>Get the process exit handler</h4>
	 * <p> Handles: {@link org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.Events#LTT_EVENT_PROCESS_EXIT}.</p>
	 * @return The handler.
	 */
	ILttngEventProcessor getProcessExitHandler() {
		return new StatsProcessExitHandler();
	}
}
