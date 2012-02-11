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

package org.eclipse.linuxtools.lttng.core.state.evProcessor.state;

import java.util.Map;

import org.eclipse.linuxtools.lttng.core.LttngConstants;
import org.eclipse.linuxtools.lttng.core.TraceDebug;
import org.eclipse.linuxtools.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.lttng.core.state.StateStrings;
import org.eclipse.linuxtools.lttng.core.state.StateStrings.BdevMode;
import org.eclipse.linuxtools.lttng.core.state.StateStrings.CpuMode;
import org.eclipse.linuxtools.lttng.core.state.StateStrings.Events;
import org.eclipse.linuxtools.lttng.core.state.StateStrings.ExecutionMode;
import org.eclipse.linuxtools.lttng.core.state.StateStrings.ExecutionSubMode;
import org.eclipse.linuxtools.lttng.core.state.StateStrings.Fields;
import org.eclipse.linuxtools.lttng.core.state.StateStrings.IRQMode;
import org.eclipse.linuxtools.lttng.core.state.StateStrings.ProcessStatus;
import org.eclipse.linuxtools.lttng.core.state.StateStrings.ProcessType;
import org.eclipse.linuxtools.lttng.core.state.evProcessor.ILttngEventProcessor;
import org.eclipse.linuxtools.lttng.core.state.model.LTTngCPUState;
import org.eclipse.linuxtools.lttng.core.state.model.LttngBdevState;
import org.eclipse.linuxtools.lttng.core.state.model.LttngExecutionState;
import org.eclipse.linuxtools.lttng.core.state.model.LttngIRQState;
import org.eclipse.linuxtools.lttng.core.state.model.LttngProcessState;
import org.eclipse.linuxtools.lttng.core.state.model.LttngSoftIRQState;
import org.eclipse.linuxtools.lttng.core.state.model.LttngTraceState;
import org.eclipse.linuxtools.lttng.core.state.model.LttngTrapState;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;

/**
 * Wraps the creation of individual handlers, each method creates and instance
 * of the corresponding handler
 * 
 * @author alvaro
 * 
 */
class StateUpdateHandlers {

	final ILttngEventProcessor getSyscallEntryHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				Long cpu = trcEvent.getCpuId();

				// No syscall_entry update for initialization process
				LttngProcessState process = traceSt.getRunning_process().get(
						cpu);
				if ((process != null) && (process.getPid() != null)
						&& (process.getPid().longValue() == 0L)) {
					return true;
				}

				// Get the expected event field
				Long syscall = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_SYSCALL_ID);

				String submode = null;
				int submodeId = 0;
				if (syscall == null) {
					TraceDebug
							.debug("Syscall Field not found, traceVent time: " //$NON-NLS-1$
									+ trcEvent.getTimestamp());
				} else {
					submode = traceSt.getSyscall_names().get(syscall);
					// Note: For statistics performance improvement only the integer value of syscall is used 
					// as well as a bit mask is applied! 
					submodeId = syscall.intValue() | LttngConstants.STATS_SYS_CALL_NAME_ID;
				}

				if (submode == null) {
					submode = ExecutionSubMode.LTTV_STATE_SUBMODE_UNKNOWN.getInName();
					submodeId = ExecutionSubMode.LTTV_STATE_SUBMODE_UNKNOWN.ordinal() | LttngConstants.STATS_NONE_ID; 
				}

				push_state(cpu, StateStrings.ExecutionMode.LTTV_STATE_SYSCALL,
						submode, submodeId, trcEvent.getTimestamp(), traceSt);
				return false;
			}
		};
		return handler;
	}

	final ILttngEventProcessor getsySyscallExitHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				Long cpu = trcEvent.getCpuId();
				LttngProcessState process = traceSt.getRunning_process().get(
						cpu);

				// No syscall_entry update for initialization process
				if ((process != null) && (process.getPid() != null)
						&& (process.getPid().longValue() == 0L)) {
					return true;
				}

				pop_state(cpu, StateStrings.ExecutionMode.LTTV_STATE_SYSCALL,
						traceSt, trcEvent.getTimestamp());
				return false;

			}
		};
		return handler;
	}

	/**
	 * Update stacks related to the parsing of an LttngEvent
	 * 
	 * @return
	 */
	final ILttngEventProcessor getTrapEntryHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {
				Long cpu = trcEvent.getCpuId();

				Long trap = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_TRAP_ID);
				if (trap == null) {
					TraceDebug
							.debug("Trap field could not be found, event time: " //$NON-NLS-1$
									+ trcEvent.getTimestamp());
					return true;
				}

				// ready the trap submode name
				String submode = traceSt.getTrap_names().get(trap);
				// Note: For statistics performance improvement only the integer value of trap is used 
                // as well as a bit mask is applied! 

				int submodeId = trap.intValue() | LttngConstants.STATS_TRAP_NAME_ID; 

				if (submode == null) {
					submode = ExecutionSubMode.LTTV_STATE_SUBMODE_UNKNOWN.getInName();
					submodeId = ExecutionSubMode.LTTV_STATE_SUBMODE_UNKNOWN.ordinal() | LttngConstants.STATS_NONE_ID;
				}

				/* update process state */
				push_state(cpu, StateStrings.ExecutionMode.LTTV_STATE_TRAP,
						submode, submodeId, trcEvent.getTimestamp(), traceSt);

				/* update cpu status */
				LTTngCPUState cpust = traceSt.getCpu_states().get(cpu);
				cpu_push_mode(cpust, StateStrings.CpuMode.LTTV_CPU_TRAP);
				cpust.pushToTrapStack(trap); /* update trap status */
				
				// update Trap State
				LttngTrapState trap_state = null;
				trap_state = traceSt.getTrap_states().get(trap);
				
				// If the trape_state exists, just increment it's counter, 
				//	otherwise, create it
				if ( trap_state == null ) {
					trap_state = new LttngTrapState();
					trap_state.incrementRunning();
					traceSt.getTrap_states().put(trap, trap_state);
				}
				else {
					trap_state.incrementRunning();
				}
				
				return false;

			}
		};
		return handler;
	}

	/**
	 * 
	 * @return
	 */
	final ILttngEventProcessor getTrapExitHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				Long cpu = trcEvent.getCpuId();
				LTTngCPUState cpust = traceSt.getCpu_states().get(cpu);
				Long trap = cpust.popFromTrapStack();

				/* update process state */
				pop_state(cpu, ExecutionMode.LTTV_STATE_TRAP, traceSt, trcEvent
						.getTimestamp());

				/* update cpu status */
				cpu_pop_mode(cpust);

				if (trap != -1L) {
					traceSt.getTrap_states().get(trap).decrementRunning();
				}
				// else {
				// TraceDebug.debug("remove this line");
				// }

				return false;

			}
		};
		return handler;
	}

	/**
	 * 
	 * @return
	 */
	final ILttngEventProcessor getIrqEntryHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				Long cpu = trcEvent.getCpuId();

				Long irq = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_IRQ_ID);
				if (irq == null || traceSt.getIrq_states().get(irq) == null) {
					if (irq != null) {
						TraceDebug.debug("Invalid irq (" + irq + "), ts = " + trcEvent.getTimestamp()); //$NON-NLS-1$ //$NON-NLS-2$
					}
					return true;
				}

				String submode;
				submode = traceSt.getIrq_names().get(irq);
                // Note: For statistics performance improvement only the integer value of irq is used 
                // as well as a bit mask is applied! 
				int submodeId = irq.intValue() | LttngConstants.STATS_IRQ_NAME_ID;
                
				if (submode == null) {
					submode = ExecutionSubMode.LTTV_STATE_SUBMODE_UNKNOWN.getInName();
					submodeId = ExecutionSubMode.LTTV_STATE_SUBMODE_UNKNOWN.ordinal() | LttngConstants.STATS_NONE_ID;
				}

				/*
				 * Do something with the info about being in user or system mode
				 * when int?
				 */
				push_state(cpu, ExecutionMode.LTTV_STATE_IRQ, submode, submodeId, trcEvent
						.getTimestamp(), traceSt);

				/* update cpu state */
				LTTngCPUState cpust = traceSt.getCpu_states().get(cpu);
				cpu_push_mode(cpust, CpuMode.LTTV_CPU_IRQ); /* mode stack */
				cpust.pushToIrqStack(irq); /* last irq */

				/* udpate irq state */
				irq_push_mode(traceSt.getIrq_states().get(irq),
						IRQMode.LTTV_IRQ_BUSY);
				return false;

			}
		};
		return handler;
	}

	/**
	 * 
	 * @return
	 */
	final ILttngEventProcessor getSoftIrqExitHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				Long cpu = trcEvent.getCpuId();
				LTTngCPUState cpust = traceSt.getCpu_states().get(cpu);
				Long softirq = cpust.popFromSoftIrqStack();

				// Update process status
				pop_state(cpu, ExecutionMode.LTTV_STATE_SOFT_IRQ, traceSt,
						trcEvent.getTimestamp());

				/* update softirq status */
				if (softirq != -1) {
					LttngSoftIRQState softIrqstate = traceSt
							.getSoft_irq_states().get(softirq);
					if (softIrqstate != null) {
						softIrqstate.decrementRunning();
					}
				}

				/* update cpu status */
				cpu_pop_mode(cpust);

				return false;
			}
		};
		return handler;
	}

	/**
	 * 
	 * @return
	 */
	final ILttngEventProcessor getIrqExitHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				Long cpu = trcEvent.getCpuId();

				/* update process state */
				pop_state(cpu, ExecutionMode.LTTV_STATE_IRQ, traceSt, trcEvent
						.getTimestamp());

				/* update cpu status */
				LTTngCPUState cpust = traceSt.getCpu_states().get(cpu);
				cpu_pop_mode(cpust);

				/* update irq status */
				Long last_irq = cpust.popFromIrqStack();
				if (last_irq != -1L) {
					LttngIRQState irq_state = traceSt.getIrq_states().get(
							last_irq);
					irq_pop_mode(irq_state);
				}

				return false;

			}
		};
		return handler;
	}

	/**
	 * 
	 * @return
	 */
	final ILttngEventProcessor getSoftIrqRaiseHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			private Events eventType = Events.LTT_EVENT_SOFT_IRQ_RAISE;

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				// Long cpu = trcEvent.getCpuId();

				// get event field
				Long softirq = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_SOFT_IRQ_ID);

				if (softirq == null) {
					TraceDebug.debug("Soft_irq_id not found in " //$NON-NLS-1$
							+ eventType.getInName() + " time: " //$NON-NLS-1$
							+ trcEvent.getTimestamp());
					return true;
				}

				// String submode;
				// String[] softIrqNames = traceSt.getSoft_irq_names();
				// if (softirq < softIrqNames.length) {
				// submode = softIrqNames[softirq];
				// } else {
				// submode = "softirq " + softirq;
				// }

				/* update softirq status */
				/* a soft irq raises are not cumulative */
				LttngSoftIRQState irqState = traceSt.getSoft_irq_states().get(
						softirq);
				if (irqState != null) {
					irqState.setPending(1L);
				} else {
					TraceDebug
							.debug("unexpected soft irq id value: " + softirq); //$NON-NLS-1$
				}

				return false;

			}
		};
		return handler;
	}

	/**
	 * 
	 * @return
	 */
	final ILttngEventProcessor getSoftIrqEntryHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				// obtrain cpu
				Long cpu = trcEvent.getCpuId();

				// get event field
				Long softirq = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_SOFT_IRQ_ID);

				if (softirq == null) {
					TraceDebug.debug("Soft IRQ ID not found, eventTime: " //$NON-NLS-1$
							+ trcEvent.getTimestamp());
					return true;
				}

				// obtain submode
				Map<Long, String> softIrqNames = traceSt.getSoft_irq_names();
				String submode = softIrqNames.get(softirq);
				if (submode == null) {
					submode = "softirq " + softirq; //$NON-NLS-1$
					softIrqNames.put(softirq, submode);
				}

                // Note: For statistics performance improvement only the integer value of softirq is used 
                // as well as a bit mask is applied! 
				int submodeId = softirq.intValue() | LttngConstants.STATS_SOFT_IRQ_NAME_ID;

				/* update softirq status */
				LttngSoftIRQState irqState = traceSt.getSoft_irq_states().get(
						softirq);
				if (irqState != null) {
					irqState.decrementPending();
					irqState.incrementRunning();
				} else {
					TraceDebug
							.debug("unexpected soft irq id value: " + softirq); //$NON-NLS-1$
				}

				/* update cpu state */
				LTTngCPUState cpu_state = traceSt.getCpu_states().get(cpu);
				cpu_state.pushToSoftIrqStack(softirq);
				cpu_push_mode(cpu_state, CpuMode.LTTV_CPU_SOFT_IRQ);

				/* update process execution mode state stack */
				push_state(cpu, ExecutionMode.LTTV_STATE_SOFT_IRQ, submode, submodeId,
						trcEvent.getTimestamp(), traceSt);

				return false;

			}
		};
		return handler;
	}

	/**
	 * Method to handle the event: LTT_EVENT_LIST_INTERRRUPT
	 * 
	 * @return
	 */
	final ILttngEventProcessor getEnumInterruptHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			private Events eventType = Events.LTT_EVENT_LIST_INTERRUPT;

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {
				String action = getAFieldString(trcEvent, traceSt,
						Fields.LTT_FIELD_ACTION);
				Long irq = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_IRQ_ID);

				if (action == null) {
					TraceDebug.debug("Field Action not found in event " //$NON-NLS-1$
							+ eventType.getInName() + " time: " //$NON-NLS-1$
							+ trcEvent.getTimestamp());
					return true;
				}

				if (irq == null) {
					TraceDebug.debug("Field irq_id not found in event " //$NON-NLS-1$
							+ eventType.getInName() + " time: " //$NON-NLS-1$
							+ trcEvent.getTimestamp());
					return true;
				}

				Map<Long, String> irq_names = traceSt.getIrq_names();

				irq_names.put(irq, action);
				return false;

			}
		};
		return handler;
	}

	/**
	 * Handle the event LTT_EVENT_REQUEST_ISSUE
	 * 
	 * @return
	 */
	final ILttngEventProcessor getBdevRequestIssueHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				// Get Fields
				Long major = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_MAJOR);
				Long minor = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_MINOR);
				Long operation = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_OPERATION);

				// calculate bdevcode
				Long devcode = mkdev(major, minor);

				if (devcode == null) {
					TraceDebug
							.debug("incorrect calcualtion of bdevcode input( major: " //$NON-NLS-1$
									+ major
									+ " minor: " //$NON-NLS-1$
									+ minor
									+ " operation: " + operation); //$NON-NLS-1$
					return true;
				}

				Map<Long, LttngBdevState> bdev_states = traceSt
						.getBdev_states();
				// Get the instance
				LttngBdevState bdevState = bdev_states.get(devcode);
				if (bdevState == null) {
					bdevState = new LttngBdevState();
				}

				// update the mode in the stack
				if (operation == 0L) {
					bdevState.pushToBdevStack(BdevMode.LTTV_BDEV_BUSY_READING);
				} else {
					bdevState.pushToBdevStack(BdevMode.LTTV_BDEV_BUSY_WRITING);
				}

				// make sure it is included in the set
				bdev_states.put(devcode, bdevState);
				return false;

			}
		};
		return handler;
	}

	/**
	 * <p>
	 * Handling event: LTT_EVENT_REQUEST_COMPLETE
	 * </p>
	 * <p>
	 * FIELDS(LTT_FIELD_MAJOR, LTT_FIELD_MINOR, LTT_FIELD_OPERATION
	 * </p>
	 * 
	 * @return
	 */
	final ILttngEventProcessor getBdevRequestCompleteHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				// Get Fields
				Long major = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_MAJOR);
				Long minor = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_MINOR);
				Long operation = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_OPERATION);

				// calculate bdevcode
				Long devcode = mkdev(major, minor);

				if (devcode == null) {
					TraceDebug
							.debug("incorrect calcualtion of bdevcode input( major: " //$NON-NLS-1$
									+ major
									+ " minor: " //$NON-NLS-1$
									+ minor
									+ " operation: " + operation); //$NON-NLS-1$
					return true;
				}

				Map<Long, LttngBdevState> bdev_states = traceSt
						.getBdev_states();
				// Get the instance
				LttngBdevState bdevState = bdev_states.get(devcode);
				if (bdevState == null) {
					bdevState = new LttngBdevState();
				}

				/* update block device */
				bdev_pop_mode(bdevState);

				return false;

			}
		};
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
	final ILttngEventProcessor getFunctionEntryHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {
				Long cpu = trcEvent.getCpuId();
				Long funcptr = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_THIS_FN);

				push_function(traceSt, funcptr, cpu);
				return false;

			}
		};
		return handler;
	}

	/**
	 * 
	 * @return
	 */
	final ILttngEventProcessor getFunctionExitHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				Long funcptr = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_THIS_FN);

				pop_function(traceSt, trcEvent, funcptr);
				return false;

			}
		};
		return handler;
	}

	/**
	 * <p>
	 * process event: LTT_EVENT_SYS_CALL_TABLE
	 * </p>
	 * <p>
	 * fields: LTT_FIELD_ID, LTT_FIELD_ADDRESS, LTT_FIELD_SYMBOL
	 * </p>
	 * 
	 * @return
	 */
	final ILttngEventProcessor getDumpSyscallHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {
				// obtain the syscall id
				Long id = getAFieldLong(trcEvent, traceSt, Fields.LTT_FIELD_ID);

				// Long address = getAFieldLong(trcEvent, traceSt,
				// Fields.LTT_FIELD_ADDRESS);

				// Obtain the symbol
				String symbol = getAFieldString(trcEvent, traceSt,
						Fields.LTT_FIELD_SYMBOL);

				// fill the symbol to the sycall_names collection
				traceSt.getSyscall_names().put(id, symbol);

				return false;
			}
		};
		return handler;
	}

	/**
	 * <p>
	 * Handles event: LTT_EVENT_KPROBE_TABLE
	 * </p>
	 * <p>
	 * Fields: LTT_FIELD_IP, LTT_FIELD_SYMBOL
	 * </p>
	 * 
	 * @return
	 */
	final ILttngEventProcessor getDumpKprobeHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				Long ip = getAFieldLong(trcEvent, traceSt, Fields.LTT_FIELD_IP);
				String symbol = getAFieldString(trcEvent, traceSt,
						Fields.LTT_FIELD_SYMBOL);

				traceSt.getKprobe_table().put(ip, symbol);

				return false;

			}
		};
		return handler;
	}

	/**
	 * <p>
	 * Handles: LTT_EVENT_SOFTIRQ_VEC
	 * </p>
	 * <p>
	 * Fields: LTT_FIELD_ID, LTT_FIELD_ADDRESS, LTT_FIELD_SYMBOL
	 * </p>
	 * 
	 * @return
	 */
	final ILttngEventProcessor getDumpSoftIrqHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				// Get id
				Long id = getAFieldLong(trcEvent, traceSt, Fields.LTT_FIELD_ID);

				// Address not needed
				// Long address = ltt_event_get_long_unsigned(e,
				// lttv_trace_get_hook_field(th,
				// 1));

				// Get symbol
				String symbol = getAFieldString(trcEvent, traceSt,
						Fields.LTT_FIELD_SYMBOL);

				// Register the soft irq name
				traceSt.getSoft_irq_names().put(id, symbol);
				return false;

			}
		};
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
	final ILttngEventProcessor getSchedChangeHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				Long cpu = trcEvent.getCpuId();
				ITmfTimestamp eventTime = trcEvent.getTimestamp();

				LttngProcessState process = traceSt.getRunning_process().get(
						cpu);

				Long pid_out = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_PREV_PID);
				Long pid_in = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_NEXT_PID);
				Long state_out = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_PREV_STATE);

				if (process != null) {

					/*
					 * We could not know but it was not the idle process
					 * executing. This should only happen at the beginning,
					 * before the first schedule event, and when the initial
					 * information (current process for each CPU) is missing. It
					 * is not obvious how we could, after the fact, compensate
					 * the wrongly attributed statistics.
					 */

					// This test only makes sense once the state is known and if
					// there
					// is no
					// missing events. We need to silently ignore schedchange
					// coming
					// after a
					// process_free, or it causes glitches. (FIXME)
					// if(unlikely(process->pid != pid_out)) {
					// g_assert(process->pid == 0);
					// }
					if ((process.getPid().longValue() == 0L)
							&& (process.getState().getExec_mode() == ExecutionMode.LTTV_STATE_MODE_UNKNOWN)) {
						if ((pid_out != null) && (pid_out.longValue() == 0L)) {
							/*
							 * Scheduling out of pid 0 at beginning of the trace
							 * : we know for sure it is in syscall mode at this
							 * point.
							 */

							process.getState().setExec_mode(
									ExecutionMode.LTTV_STATE_SYSCALL);
							process.getState().setProc_status(
									ProcessStatus.LTTV_STATE_WAIT);
							process.getState().setChange_Time(
									trcEvent.getTimestamp().getValue());
							process.getState().setEntry_Time(
									trcEvent.getTimestamp().getValue());
						}
					} else {
						if (process.getState().getProc_status() == ProcessStatus.LTTV_STATE_EXIT) {
							process.getState().setProc_status(
									ProcessStatus.LTTV_STATE_ZOMBIE);
							process.getState().setChange_Time(
									trcEvent.getTimestamp().getValue());
						} else {
							if ((state_out != null)
									&& (state_out.longValue() == 0L)) {
								process.getState().setProc_status(
										ProcessStatus.LTTV_STATE_WAIT_CPU);
							} else {
								process.getState().setProc_status(
										ProcessStatus.LTTV_STATE_WAIT);
							}

							process.getState().setChange_Time(
									trcEvent.getTimestamp().getValue());
						}

						if ((state_out != null)
								&& (state_out == 32L || state_out == 64L)) { /*
																			 * EXIT_DEAD
																			 * ||
																			 * TASK_DEAD
																			 */
							/* see sched.h for states */
							if (!exit_process(traceSt, process)) {
								process.getState().setProc_status(
										ProcessStatus.LTTV_STATE_DEAD);
								process.getState().setChange_Time(
										trcEvent.getTimestamp().getValue());
							}
						}
					}
				}
				process = lttv_state_find_process_or_create(traceSt, cpu,
						pid_in, eventTime);

				traceSt.getRunning_process().put(cpu, process);

				process.getState().setProc_status(ProcessStatus.LTTV_STATE_RUN);
				process.getState().setChange_Time(eventTime.getValue());
				process.setCpu(cpu);
				// process->state->s = LTTV_STATE_RUN;
				// if(process->usertrace)
				// process->usertrace->cpu = cpu;
				// process->last_cpu_index =
				// ltt_tracefile_num(((LttvTracefileContext*)s)->tf);

				// process->state->change = s->parent.timestamp;

				LTTngCPUState cpu_state = traceSt.getCpu_states().get(cpu);
				/* update cpu status */
				if ((pid_in != null) && (pid_in.longValue() == 0L)) {

					/* going to idle task */
					cpu_set_base_mode(cpu_state, CpuMode.LTTV_CPU_IDLE);
				} else {
					/*
					 * scheduling a real task. we must be careful here: if we
					 * just schedule()'ed to a process that is in a trap, we
					 * must put the cpu in trap mode
					 */
					cpu_set_base_mode(cpu_state, CpuMode.LTTV_CPU_BUSY);
					if (process.getState().getExec_mode() == ExecutionMode.LTTV_STATE_TRAP) {
						cpu_push_mode(cpu_state, CpuMode.LTTV_CPU_TRAP);
					}
				}
				return false;

			}
		};
		return handler;
	}

	/**
	 * <p>
	 * Handles: LTT_EVENT_PROCESS_FORK
	 * </p>
	 * <p>
	 * Fields: FIELD_ARRAY(LTT_FIELD_PARENT_PID, LTT_FIELD_CHILD_PID,
	 * LTT_FIELD_CHILD_TGID)
	 * </p>
	 * 
	 * @return
	 */
	final ILttngEventProcessor getProcessForkHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				Long cpu = trcEvent.getCpuId();
				LttngProcessState process = traceSt.getRunning_process().get(
						cpu);
				ITmfTimestamp timeStamp = trcEvent.getTimestamp();

				// /* Parent PID */
				// Long parent_pid = getAFieldLong(trcEvent, traceSt,
				// Fields.LTT_FIELD_PARENT_PID);

				/* Child PID */
				/* In the Linux Kernel, there is one PID per thread. */
				Long child_pid = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_CHILD_PID);

				/* Child TGID */
				/* tgid in the Linux kernel is the "real" POSIX PID. */
				Long child_tgid = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_CHILD_TGID);
				if (child_tgid == null) {
					child_tgid = 0L;
				}

				/*
				 * Mathieu : it seems like the process might have been scheduled
				 * in before the fork, and, in a rare case, might be the current
				 * process. This might happen in a SMP case where we don't have
				 * enough precision on the clocks.
				 * 
				 * Test reenabled after precision fixes on time. (Mathieu)
				 */
				// #if 0
				// zombie_process = lttv_state_find_process(ts, ANY_CPU,
				// child_pid);
				//	
				// if(unlikely(zombie_process != NULL)) {
				// /* Reutilisation of PID. Only now we are sure that the old
				// PID
				// * has been released. FIXME : should know when release_task
				// happens
				// instead.
				// */
				// guint num_cpus = ltt_trace_get_num_cpu(ts->parent.t);
				// guint i;
				// for(i=0; i< num_cpus; i++) {
				// g_assert(zombie_process != ts->running_process[i]);
				// }
				//	
				// exit_process(s, zombie_process);
				// }
				// #endif //0

				if (process.getPid().equals(child_pid)) {
					TraceDebug
							.debug("Unexpected, process pid equal to child pid: " //$NON-NLS-1$
									+ child_pid
									+ " Event Time: " //$NON-NLS-1$
									+ trcEvent.getTimestamp());
				}

				// g_assert(process->pid != child_pid);
				// FIXME : Add this test in the "known state" section
				// g_assert(process->pid == parent_pid);
				LttngProcessState child_process = lttv_state_find_process(
						traceSt, ANY_CPU, child_pid);
				if (child_process == null) {
					child_process = create_process(traceSt, cpu, child_pid,
							child_tgid, timeStamp);
					child_process.setPpid(process.getPid(), timeStamp.getValue());
				} else {
					/*
					 * The process has already been created : due to time
					 * imprecision between multiple CPUs : it has been scheduled
					 * in before creation. Note that we shouldn't have this kind
					 * of imprecision.
					 * 
					 * Simply put a correct parent.
					 */
					StringBuilder sb = new StringBuilder("Process " + child_pid); //$NON-NLS-1$
					sb.append(" has been created at [" //$NON-NLS-1$
							+ child_process.getCreation_time() + "] "); //$NON-NLS-1$
					sb.append("and inserted at [" //$NON-NLS-1$
							+ child_process.getInsertion_time() + "] "); //$NON-NLS-1$
					sb.append("before \nfork on cpu " + cpu + " Event time: [" //$NON-NLS-1$ //$NON-NLS-2$
							+ trcEvent + "]\n."); //$NON-NLS-1$
					sb
							.append("Probably an unsynchronized TSD problem on the traced machine."); //$NON-NLS-1$
					TraceDebug.debug(sb.toString());

					// g_assert(0); /* This is a problematic case : the process
					// has
					// beencreated
					// before the fork event */
					child_process.setPpid(process.getPid());
					child_process.setTgid(child_tgid);
				}

				if (!child_process.getName().equals(
						ProcessStatus.LTTV_STATE_UNNAMED.getInName())) {
					TraceDebug.debug("Unexpected child process status: " //$NON-NLS-1$
							+ child_process.getName());
				}

				child_process.setName(process.getName());
				child_process.setBrand(process.getBrand());

				return false;

			}
		};
		return handler;
	}

	/**
	 * <p>
	 * Handles: LTT_EVENT_KTHREAD_CREATE
	 * </p>
	 * <p>
	 * Fields: LTT_FIELD_PID
	 * </p>
	 * 
	 * @return
	 */
	final ILttngEventProcessor getProcessKernelThreadHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {
				/*
				 * We stamp a newly created process as kernel_thread. The thread
				 * should not be running yet.
				 */

				LttngExecutionState exState;
				Long pid;
				LttngProcessState process;

				/* PID */
				pid = getAFieldLong(trcEvent, traceSt, Fields.LTT_FIELD_PID);
				// s->parent.target_pid = pid;

				process = lttv_state_find_process_or_create(traceSt, ANY_CPU,
						pid, new TmfTimestamp());

				if (!process.getState().getProc_status().equals(
						ProcessStatus.LTTV_STATE_DEAD)) {
					// Leave only the first element in the stack with execution
					// mode to
					// syscall
					exState = process.getFirstElementFromExecutionStack();
					exState.setExec_mode(ExecutionMode.LTTV_STATE_SYSCALL);
					process.clearExecutionStack();
					process.pushToExecutionStack(exState);
				}

				process.setType(ProcessType.LTTV_STATE_KERNEL_THREAD);

				return false;

			}
		};
		return handler;
	}

	/**
	 * <p>
	 * Handles: LTT_EVENT_PROCESS_EXIT
	 * </p>
	 * <p>
	 * LTT_FIELD_PID
	 * </p>
	 * 
	 * @return
	 */
	final ILttngEventProcessor getProcessExitHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				Long pid;
				LttngProcessState process;

				pid = getAFieldLong(trcEvent, traceSt, Fields.LTT_FIELD_PID);
				// s->parent.target_pid = pid;

				// FIXME : Add this test in the "known state" section
				// g_assert(process->pid == pid);

				process = lttv_state_find_process(traceSt, ANY_CPU, pid);
				if (process != null) {
					process.getState().setProc_status(
							ProcessStatus.LTTV_STATE_EXIT);
				}
				return false;

			}
		};
		return handler;
	}

	/**
	 * <p>
	 * Handles: LTT_EVENT_PROCESS_FREE
	 * </p>
	 * <p>
	 * Fields: LTT_FIELD_PID
	 * </p>
	 * 
	 * @return
	 */
	final ILttngEventProcessor getProcessFreeHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				Long release_pid;
				LttngProcessState process;

				/* PID of the process to release */
				release_pid = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_PID);
				// s->parent.target_pid = release_pid;

				if ((release_pid != null) && (release_pid.longValue() == 0L)) {
					TraceDebug.debug("Unexpected release_pid: 0, Event time: " //$NON-NLS-1$
							+ trcEvent.getTimestamp());
				}

				process = lttv_state_find_process(traceSt, ANY_CPU, release_pid);
				if (process != null) {
					exit_process(traceSt, process);
				}

				return false;
				// DISABLED
				// if(process != null) {
				/*
				 * release_task is happening at kernel level : we can now safely
				 * release the data structure of the process
				 */
				// This test is fun, though, as it may happen that
				// at time t : CPU 0 : process_free
				// at time t+150ns : CPU 1 : schedule out
				// Clearly due to time imprecision, we disable it. (Mathieu)
				// If this weird case happen, we have no choice but to put the
				// Currently running process on the cpu to 0.
				// I re-enable it following time precision fixes. (Mathieu)
				// Well, in the case where an process is freed by a process on
				// another
				// CPU
				// and still scheduled, it happens that this is the schedchange
				// that
				// will
				// drop the last reference count. Do not free it here!

				// int num_cpus = ltt_trace_get_num_cpu(ts->parent.t);
				// guint i;
				// for(i=0; i< num_cpus; i++) {
				// //g_assert(process != ts->running_process[i]);
				// if(process == ts->running_process[i]) {
				// //ts->running_process[i] = lttv_state_find_process(ts, i, 0);
				// break;
				// }
				// }
				// if(i == num_cpus) /* process is not scheduled */
				// exit_process(s, process);
				// }
				//	
				// return false;

			}
		};
		return handler;
	}

	/**
	 * <p>
	 * Handles: LTT_EVENT_EXEC
	 * </p>
	 * <p>
	 * FIELDS: LTT_FIELD_FILENAME
	 * </p>
	 * 
	 * @return
	 */
	final ILttngEventProcessor getProcessExecHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				Long cpu = trcEvent.getCpuId();
				LttngProcessState process = traceSt.getRunning_process().get(
						cpu);

				// #if 0//how to use a sequence that must be transformed in a
				// string
				// /* PID of the process to release */
				// guint64 name_len = ltt_event_field_element_number(e,
				// lttv_trace_get_hook_field(th, 0));
				// //name = ltt_event_get_string(e,
				// lttv_trace_get_hook_field(th, 0));
				// LttField *child = ltt_event_field_element_select(e,
				// lttv_trace_get_hook_field(th, 0), 0);
				// gchar *name_begin =
				// (gchar*)(ltt_event_data(e)+ltt_event_field_offset(e, child));
				// gchar *null_term_name = g_new(gchar, name_len+1);
				// memcpy(null_term_name, name_begin, name_len);
				// null_term_name[name_len] = '\0';
				// process->name = g_quark_from_string(null_term_name);
				// #endif //0

				process.setName(getAFieldString(trcEvent, traceSt,
						Fields.LTT_FIELD_FILENAME));
				process.setBrand(StateStrings.LTTV_STATE_UNBRANDED);
				return false;

			}
		};
		return handler;
	}

	/**
	 * <p>
	 * LTT_EVENT_THREAD_BRAND
	 * </p>
	 * <p>
	 * FIELDS: LTT_FIELD_NAME
	 * </p>
	 * 
	 * @return
	 */
	final ILttngEventProcessor GetThreadBrandHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				String name;
				Long cpu = trcEvent.getCpuId();
				LttngProcessState process = traceSt.getRunning_process().get(
						cpu);

				name = getAFieldString(trcEvent, traceSt, Fields.LTT_FIELD_NAME);
				process.setBrand(name);
				return false;

			}
		};
		return handler;
	}

	/**
	 * @return
	 */
	final ILttngEventProcessor getStateDumpEndHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				/* For all processes */
				/*
				 * if kernel thread, if stack[0] is unknown, set to syscall
				 * mode, wait
				 */
				/* else, if stack[0] is unknown, set to user mode, running */
				LttngProcessState[] processes = traceSt.getProcesses();
				ITmfTimestamp time = trcEvent.getTimestamp();
				
				for (int pos = 0; pos < processes.length; pos++) {
					fix_process(processes[pos], time);
				}
				
				// Set the current process to be running
				// TODO Should we do it for all process running on a cpu?
				LttngProcessState process = traceSt.getRunning_process().get(trcEvent.getCpuId());
				process.getState().setProc_status(ProcessStatus.LTTV_STATE_RUN);
				
				return false;

			}

			/**
			 * Private method used to establish the first execution state in the
			 * stack for a given process
			 * 
			 * @param process
			 * @param timestamp
			 */
			private void fix_process(LttngProcessState process,
					ITmfTimestamp timestamp) {

				LttngExecutionState es;

				if (process.getType() == ProcessType.LTTV_STATE_KERNEL_THREAD) {
					es = process.getFirstElementFromExecutionStack();

					if (es.getExec_mode() == ExecutionMode.LTTV_STATE_MODE_UNKNOWN) {
						es.setExec_mode(ExecutionMode.LTTV_STATE_SYSCALL);
						es.setExec_submode(ExecutionSubMode.LTTV_STATE_SUBMODE_NONE.getInName());
                        // Note: For statistics performance improvement a integer representation of the submode is used 
                        // as well as a bit mask is applied! 
						es.setExec_submode_id(StateStrings.ExecutionSubMode.LTTV_STATE_SUBMODE_NONE.ordinal() | LttngConstants.STATS_NONE_ID);
						es.setEntry_Time(timestamp.getValue());
						es.setChange_Time(timestamp.getValue());
						es.setCum_cpu_time(0L);
						if (es.getProc_status() == ProcessStatus.LTTV_STATE_UNNAMED) {
							es.setProc_status(ProcessStatus.LTTV_STATE_WAIT);
						}
					}
				} else {
					es = process.getFirstElementFromExecutionStack();
					if (es.getExec_mode() == ExecutionMode.LTTV_STATE_MODE_UNKNOWN) {
						es.setExec_mode(ExecutionMode.LTTV_STATE_USER_MODE);
						es.setExec_submode(ExecutionSubMode.LTTV_STATE_SUBMODE_NONE.getInName());
                        // Note: For statistics performance improvement a integer representation of the submode is used 
                        // as well as a bit mask is applied! 
						es.setExec_submode_id(StateStrings.ExecutionSubMode.LTTV_STATE_SUBMODE_NONE.ordinal() | LttngConstants.STATS_NONE_ID);
						es.setEntry_Time(timestamp.getValue());
						es.setChange_Time(timestamp.getValue());
						es.setCum_cpu_time(0L);
						if (es.getProc_status() == ProcessStatus.LTTV_STATE_UNNAMED) {
							es.setProc_status(ProcessStatus.LTTV_STATE_RUN);
						}

						// If the first element is also the one on top... mean
						// we have ONE element on the stack
						if (process.getFirstElementFromExecutionStack() == process
								.peekFromExecutionStack()) {
							/*
							 * Still in bottom unknown mode, means never did a
							 * system call May be either in user mode, syscall
							 * mode, running or waiting.
							 */
							/*
							 * FIXME : we may be tagging syscall mode when being
							 * user mode
							 */
							// Get a new execution State
							es = new LttngExecutionState();

							// initialize values
							es.setExec_mode(ExecutionMode.LTTV_STATE_SYSCALL);
							es.setExec_submode(ExecutionSubMode.LTTV_STATE_SUBMODE_NONE.getInName());
	                        // Note: For statistics performance improvement a integer representation of the submode is used 
	                        // as well as a bit mask is applied! 
							es.setExec_submode_id(StateStrings.ExecutionSubMode.LTTV_STATE_SUBMODE_NONE.ordinal() | LttngConstants.STATS_NONE_ID);
							es.setEntry_Time(timestamp.getValue());
							es.setChange_Time(timestamp.getValue());
							es.setCum_cpu_time(0L);
							es.setProc_status(ProcessStatus.LTTV_STATE_WAIT);

							// Push the new state to the stack
							process.pushToExecutionStack(es);
						}
					}
				}
			}
		};
		return handler;
	}

	/**
	 * <p>
	 * Handles: LTT_EVENT_PROCESS_STATE
	 * </p>
	 * <p>
	 * FIELDS: LTT_FIELD_PID, LTT_FIELD_PARENT_PID, LTT_FIELD_NAME,
	 * LTT_FIELD_TYPE, LTT_FIELD_MODE, LTT_FIELD_SUBMODE, LTT_FIELD_STATUS,
	 * LTT_FIELD_TGID
	 * </p>
	 * 
	 * @return
	 */
	final ILttngEventProcessor getEnumProcessStateHandler() {
		AbsStateUpdate handler = new AbsStateUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				Long parent_pid;
				Long pid;
				Long tgid;
				String command;
				Long cpu = trcEvent.getCpuId();
				
				LttngProcessState process = traceSt.getRunning_process().get(
						cpu);
				LttngProcessState parent_process;
				String type;
				// String mode, submode, status;
				LttngExecutionState es;
				
				/* PID */
				pid = getAFieldLong(trcEvent, traceSt, Fields.LTT_FIELD_PID);

				/* Parent PID */
				parent_pid = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_PARENT_PID);

				/* Command name */
				command = getAFieldString(trcEvent, traceSt,
						Fields.LTT_FIELD_NAME);

				Long typeVal = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_TYPE);

				type = ProcessType.LTTV_STATE_KERNEL_THREAD.getInName();
				if ((typeVal != null) && (typeVal.longValue() == 0L)) {
					type = ProcessType.LTTV_STATE_USER_THREAD.getInName();
				}

				// /* mode */
				// mode = getAFieldString(trcEvent, traceSt,
				// Fields.LTT_FIELD_MODE);
				//
				// /* submode */
				// submode = getAFieldString(trcEvent, traceSt,
				// Fields.LTT_FIELD_SUBMODE);
				//
				// /* status */
				// status = getAFieldString(trcEvent, traceSt,
				// Fields.LTT_FIELD_STATUS);

				/* TGID */
				tgid = getAFieldLong(trcEvent, traceSt, Fields.LTT_FIELD_TGID);
				if (tgid == null) {
					tgid = 0L;
				}

				if ((pid != null) && (pid.longValue() == 0L)) {
					for (Long acpu : traceSt.getCpu_states().keySet()) {
						process = lttv_state_find_process(traceSt, acpu, pid);
						if (process != null) {
							process.setPpid(parent_pid);
							process.setTgid(tgid);
							process.setName(command);
							process
									.setType(ProcessType.LTTV_STATE_KERNEL_THREAD);
						} else {
							StringBuilder sb = new StringBuilder(
									"Unexpected, null process read from the TraceState list of processes, event time: " //$NON-NLS-1$
											+ trcEvent.getTimestamp());
							TraceDebug.debug(sb.toString());
						}
					}
				} else {
					/*
					 * The process might exist if a process was forked while
					 * performing the state dump.
					 */
					process = lttv_state_find_process(traceSt, ANY_CPU, pid);
					if (process == null) {
						parent_process = lttv_state_find_process(traceSt,
								ANY_CPU, parent_pid);
						ITmfTimestamp eventTime = trcEvent.getTimestamp();
						process = create_process(traceSt, cpu, pid, tgid,
								command, eventTime);
						if (parent_process != null) {
							process.setPpid(parent_process.getPid(), eventTime.getValue());
						}

						/* Keep the stack bottom : a running user mode */
						/*
						 * Disabled because of inconsistencies in the current
						 * statedump states.
						 */
						if (type.equals(ProcessType.LTTV_STATE_KERNEL_THREAD
								.getInName())) {
							/*
							 * FIXME Kernel thread : can be in syscall or
							 * interrupt or trap.
							 */
							/*
							 * Will cause expected trap when in fact being
							 * syscall (even after end of statedump event) Will
							 * cause expected interrupt when being syscall.
							 * (only before end of statedump event)
							 */
							// process type is USER_THREAD by default.
							process
									.setType(ProcessType.LTTV_STATE_KERNEL_THREAD);

						}
						
						//Only one entry needed in the execution stack
						process.popFromExecutionStack();
						es = process.getState();
						es.setExec_mode(ExecutionMode.LTTV_STATE_MODE_UNKNOWN);
						es.setProc_status(ProcessStatus.LTTV_STATE_UNNAMED);
						es.setExec_submode(ExecutionSubMode.LTTV_STATE_SUBMODE_UNKNOWN
										.getInName());
                        // Note: For statistics performance improvement a integer representation of the submode is used 
                        // as well as a bit mask is applied! 
						es.setExec_submode_id(StateStrings.ExecutionSubMode.LTTV_STATE_SUBMODE_UNKNOWN.ordinal() | LttngConstants.STATS_NONE_ID);
						
						// #if 0
						// /* UNKNOWN STATE */
						// {
						// es = process->state =
						// &g_array_index(process->execution_stack,
						// LttvExecutionState, 1);
						// es->t = LTTV_STATE_MODE_UNKNOWN;
						// es->s = LTTV_STATE_UNNAMED;
						// es->n = LTTV_STATE_SUBMODE_UNKNOWN;
						// }
						// #endif //0
					} else {
						/*
						 * The process has already been created : Probably was
						 * forked while dumping the process state or was simply
						 * scheduled in prior to get the state dump event.
						 */
						process.setPpid(parent_pid);
						process.setTgid(tgid);
						process.setName(command);
						if (type.equals(ProcessType.LTTV_STATE_KERNEL_THREAD
								.getInName())) {
							process
									.setType(ProcessType.LTTV_STATE_KERNEL_THREAD);
						} else {
							process.setType(ProcessType.LTTV_STATE_USER_THREAD);
						}

						// es =
						// &g_array_index(process->execution_stack,
						// LttvExecutionState,
						// 0);
						// #if 0
						// if(es->t == LTTV_STATE_MODE_UNKNOWN) {
						// if(type == LTTV_STATE_KERNEL_THREAD)
						// es->t = LTTV_STATE_SYSCALL;
						// else
						// es->t = LTTV_STATE_USER_MODE;
						// }
						// #endif //0
						/*
						 * Don't mess around with the stack, it will eventually
						 * become ok after the end of state dump.
						 */
					}
				}

				return false;

			}
		};
		return handler;
	}

}
