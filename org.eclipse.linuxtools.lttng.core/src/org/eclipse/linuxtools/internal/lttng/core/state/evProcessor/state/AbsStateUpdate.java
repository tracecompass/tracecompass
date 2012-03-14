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
package org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.state;

import org.eclipse.linuxtools.internal.lttng.core.TraceDebug;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.ExecutionMode;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.IRQMode;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.ProcessStatus;
import org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.ILttngEventProcessor;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LTTngCPUState;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngBdevState;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngExecutionState;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngIRQState;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngProcessState;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngTraceState;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;

public abstract class AbsStateUpdate extends AbsStateProcessing implements
		ILttngEventProcessor {

	// ========================================================================
	// Data
	// =======================================================================
	protected static final Long ANY_CPU = 0L;

	// ========================================================================
	// push and pop from stack
	// =======================================================================
	protected void push_state(Long cpu, StateStrings.ExecutionMode execMode,
	        String submode, int subModeId, ITmfTimestamp eventTime, LttngTraceState traceSt) {

		LttngProcessState process = traceSt.getRunning_process().get(cpu);
		LttngExecutionState exe_state = new LttngExecutionState();
		exe_state.setExec_mode(execMode);
		exe_state.setExec_submode(submode);
		exe_state.setExec_submode_id(subModeId);
		exe_state.setEntry_Time(eventTime.getValue());
		exe_state.setChange_Time(eventTime.getValue());
		exe_state.setCum_cpu_time(0L);
//		if (process != null)
			exe_state.setProc_status(process.getState().getProc_status());
		process.pushToExecutionStack(exe_state);
	}

	protected void pop_state(Long cpu, StateStrings.ExecutionMode execMode,
			LttngTraceState traceSt, ITmfTimestamp eventTime) {

		LttngProcessState process = traceSt.getRunning_process().get(cpu);

		if (!process.getState().getExec_mode().equals(execMode)) {
			// Different execution mode
			TraceDebug.debug("Different Execution Mode type \n\tTime:" //$NON-NLS-1$
					+ eventTime.toString() + "\n\tprocess state has: \n\t" //$NON-NLS-1$
					+ process.getState().getExec_mode().toString()
					+ "\n\twhen pop_int is:\n\t" + execMode.toString()); //$NON-NLS-1$
			return;
		}

		//The process state is updated within the pop method
		process.popFromExecutionStack();
		process.getState().setChange_Time(eventTime.getValue());
	}

	protected void irq_push_mode(LttngIRQState irqst, IRQMode state) {
		irqst.pushToIrqStack(state);
	}

	protected void irq_set_base_mode(LttngIRQState irqst, IRQMode state) {
	    irqst.clearAndSetBaseToIrqStack(state);
	}

	protected void irq_pop_mode(LttngIRQState irqst) {
	    irqst.popFromIrqStack();
	}

	protected void cpu_push_mode(LTTngCPUState cpust, StateStrings.CpuMode state) {
		// The initialization (init) creates a LttngCPUState instance per
		// available cpu in the system
		cpust.pushToCpuStack(state);
	}

	protected void cpu_pop_mode(LTTngCPUState cpust) {
		cpust.popFromCpuStack();
	}

	/* clears the stack and sets the state passed as argument */
	protected void cpu_set_base_mode(LTTngCPUState cpust,
			StateStrings.CpuMode state) {
		cpust.clearAndSetBaseToCpuStack(state);
	}

	protected void bdev_pop_mode(LttngBdevState bdevst) {
	    bdevst.popFromBdevStack();
	}

	/**
	 * Push a new received function pointer to the user_stack
	 * 
	 * @param traceSt
	 * @param funcptr
	 * @param cpu
	 */
	protected void push_function(LttngTraceState traceSt, Long funcptr, Long cpu) {
		// Get the related process
		LttngProcessState process = traceSt.getRunning_process().get(cpu);

		// update stack
		process.pushToUserStack(funcptr);

		// update the pointer to the current function on the corresponding
		// process
		process.setCurrent_function(funcptr);
	}

	protected void pop_function(LttngTraceState traceSt, LttngEvent trcEvent,
			Long funcptr) {
		Long cpu = trcEvent.getCpuId();
		LttngProcessState process = traceSt.getRunning_process().get(cpu);
		Long curr_function = process.getCurrent_function();

		if (curr_function != null && !curr_function.equals(funcptr)) {
			TraceDebug.debug("Different functions: " + funcptr + " current: " //$NON-NLS-1$ //$NON-NLS-2$
					+ curr_function + " time stamp: " //$NON-NLS-1$
					+ trcEvent.getTimestamp().toString());

			// g_info("Different functions (%lu.%09lu): ignore it\n",
			// tfs->parent.timestamp.tv_sec, tfs->parent.timestamp.tv_nsec);
			// g_info("process state has %" PRIu64 " when pop_function is %"
			// PRIu64
			// "\n",
			// process->current_function, funcptr);
			// g_info("{ %u, %u, %s, %s, %s }\n",
			// process->pid,
			// process->ppid,
			// g_quark_to_string(process->name),
			// g_quark_to_string(process->brand),
			// g_quark_to_string(process->state->s));
			return;
		}
		
		process.popFromUserStack();
		process.setCurrent_function(process.peekFromUserStack());
	}

	// ========================================================================
	// General methods
	// =======================================================================
	// Adaption from MKDEV macro
	protected Long mkdev(Long major, Long minor) {
		Long result = null;
		if (major != null && minor != null) {
			result = (major << 20) | minor;
		}
		return result;
	}

	/*
	 * FIXME : this function should be called when we receive an event telling
	 * that release_task has been called in the kernel. In happens generally
	 * when the parent waits for its child termination, but may also happen in
	 * special cases in the child's exit : when the parent ignores its children
	 * SIGCCHLD or has the flag SA_NOCLDWAIT. It can also happen when the child
	 * is part of a killed thread group, but isn't the leader.
	 */
	protected boolean exit_process(LttngTraceState ts, LttngProcessState process) {
		/*
		 * Wait for both schedule with exit dead and process free to happen.
		 * They can happen in any order.
		 */
		process.incrementFree_events();
		if (process.getFree_events() < 2) {
			return false;
		}

		process.clearExecutionStack();
		process.clearUserStack();
		ts.removeProcessState(process);

		return true;
	}

	/**
	 * @param traceSt
	 * @param cpu
	 * @param pid
	 * @param tgid
	 * @param timestamp
	 * @return
	 */
	protected LttngProcessState create_process(LttngTraceState traceSt,
			Long cpu, Long pid, Long tgid, final ITmfTimestamp timestamp) {
		LttngProcessState process = create_process(traceSt, cpu, pid, tgid,
				ProcessStatus.LTTV_STATE_UNNAMED.getInName(), timestamp);
		return process;
	}

	/**
	 * @param traceSt
	 * @param cpu
	 * @param pid
	 * @param tgid
	 * @param name
	 * @param timestamp
	 * @return
	 */
	protected LttngProcessState create_process(LttngTraceState traceSt,
			Long cpu, Long pid, Long tgid, String name,
			final ITmfTimestamp timestamp) {
		LttngProcessState process;
		process = new LttngProcessState(cpu, pid, tgid, name, timestamp.getValue(), traceSt.getTraceId());
		traceSt.addProcessState(process);
		return process;
	}

	/**
	 * @param ts
	 * @param cpu
	 * @param pid
	 * @param timestamp
	 *            , Used when a new process is needed
	 * @return
	 */
	protected LttngProcessState lttv_state_find_process_or_create(
			LttngTraceState ts, Long cpu, Long pid, final ITmfTimestamp timestamp) {
	
		LttngProcessState process = lttv_state_find_process(ts, cpu, pid);
		/* Put ltt_time_zero creation time for non existing processes */
		if (process == null) {
			process = create_process(ts, cpu, pid, 0L, timestamp);
			// leave only one entry in the execution stack
			process.popFromExecutionStack();
			LttngExecutionState es = process.getState();
			es.setExec_mode(ExecutionMode.LTTV_STATE_MODE_UNKNOWN);
			es.setProc_status(ProcessStatus.LTTV_STATE_UNNAMED);
		}
	
		return process;
	}

}