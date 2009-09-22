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
package org.eclipse.linuxtools.lttng.state.evProcessor.state;

import java.util.List;

import org.eclipse.linuxtools.lttng.TraceDebug;
import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.event.LttngEventField;
import org.eclipse.linuxtools.lttng.state.StateStrings;
import org.eclipse.linuxtools.lttng.state.StateStrings.Fields;
import org.eclipse.linuxtools.lttng.state.StateStrings.IRQMode;
import org.eclipse.linuxtools.lttng.state.StateStrings.ProcessStatus;
import org.eclipse.linuxtools.lttng.state.evProcessor.IEventProcessing;
import org.eclipse.linuxtools.lttng.state.model.LTTngCPUState;
import org.eclipse.linuxtools.lttng.state.model.LttngBdevState;
import org.eclipse.linuxtools.lttng.state.model.LttngExecutionState;
import org.eclipse.linuxtools.lttng.state.model.LttngIRQState;
import org.eclipse.linuxtools.lttng.state.model.LttngProcessState;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;
import org.eclipse.linuxtools.tmf.event.TmfEventField;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

public abstract class AbsStateUpdate implements IEventProcessing {

	// ========================================================================
	// Data
	// =======================================================================
	protected static final Long ANY_CPU = 0L;

	// ========================================================================
	// push and pop from stack
	// =======================================================================
	protected void push_state(Long cpu, StateStrings.ExecutionMode execMode,
			String submode, TmfTimestamp eventTime, LttngTraceState traceSt) {

		LttngProcessState process = traceSt.getRunning_process().get(cpu);
		LttngExecutionState exe_state = new LttngExecutionState();
		exe_state.setExec_mode(execMode);
		exe_state.setExec_submode(submode);
		exe_state.setEntry_Time(eventTime);
		exe_state.setChange_Time(eventTime);
		exe_state.setCum_cpu_time(0L);
		exe_state.setProc_status(process.getState().getProc_status());
		process.setState(exe_state);
		process.pushToExecutionStack(exe_state);
	}

	protected void pop_state(Long cpu, StateStrings.ExecutionMode execMode,
			LttngTraceState traceSt, TmfTimestamp eventTime) {

		LttngProcessState process = traceSt.getRunning_process().get(cpu);

		if (!process.getState().getExec_mode().equals(execMode)) {
			// Different execution mode
			TraceDebug.debug("Different Execution Mode type \n\tTime:"
					+ eventTime.toString() + "\n\tprocess state has: \n\t"
					+ process.getState().getExec_mode().toString()
					+ "\n\twhen pop_int is:\n\t" + execMode.toString());
			return;
		}

		process.popFromExecutionStack();
		process.setState(process.peekFromExecutionStack());
		process.getState().setChange_Time(eventTime);
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

		if (curr_function != null && curr_function != funcptr) {
			TraceDebug.debug("Different functions: " + funcptr + " current: "
					+ curr_function + " time stamp: "
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
	/**
	 * protected method used when only one Field is expected with Type "Long" if
	 * the number of fields is greater, the first field is returned and a
	 * tracing message is sent Null is returned if the value could not be
	 * extracted.
	 * 
	 * @param trcEvent
	 * @param traceSt
	 * @param expectedNumFields
	 * @return
	 */
	protected Long getDField(LttngEvent trcEvent, LttngTraceState traceSt,
			Fields expectedField) {
		Long fieldVal = null;
		TmfEventField[] fields = trcEvent.getContent().getFields();
		String[] fieldLabels = trcEvent.getContent().getFormat().getLabels();

		// Only one field expected
		if (fields.length != 1 || fieldLabels.length != 1) {
			StringBuilder sb = new StringBuilder(
					"Unexpected number of fields received: " + fields.length
							+ " for Event: " + trcEvent.getMarkerName() + "\n\t\tFields: ");

			for (TmfEventField field : fields) {
				sb.append(((LttngEventField)field).getName() + " ");				
			}

			TraceDebug.debug(sb.toString());
			if (fields.length == 0) {
				return null;
			}
		}

		LttngEventField field = (LttngEventField) fields[0];
		String fieldname = field.getName();
		String expectedFieldName = expectedField.getInName();
		if (fieldname.equals(expectedFieldName)) {
			Object fieldObj = field.getValue();
			if (fieldObj instanceof Long) {
				// Expected value found
				fieldVal = (Long) field.getValue();
			} else {
				if (TraceDebug.isDEBUG()) {
					TraceDebug
							.debug("Unexpected field Type. Expected: Long, Received: "
									+ fieldObj.getClass().getSimpleName());
				}
				return null;
			}
		} else {
			TraceDebug.debug("Unexpected field received: " + fieldname
					+ " Expected: " + expectedFieldName);
			return null;
		}

		return fieldVal;
	}

	/**
	 * protected method used when a Field is requested among several available
	 * fields and the expected type is Long
	 * 
	 * @param trcEvent
	 * @param traceSt
	 * @param expectedNumFields
	 * @return
	 */
	protected Long getAFieldLong(LttngEvent trcEvent, LttngTraceState traceSt,
			Fields expectedField) {
		Long fieldVal = null;
		TmfEventField[] fields = trcEvent.getContent().getFields();

		// At least one field expected
		if (fields.length == 0) {
			TraceDebug.debug("Unexpected number of fields received: "
					+ fields.length);
			return null;
		}

		LttngEventField field;
		String fieldname;
		String expectedFieldName = expectedField.getInName();
		for (int i = 0; i < fields.length; i++) {
			field = (LttngEventField) fields[i];
			fieldname = field.getName();
			if (fieldname.equals(expectedFieldName)) {
				Object fieldObj = field.getValue();
				if (fieldObj instanceof Long) {
					// Expected value found
					fieldVal = (Long) field.getValue();
					// if (expectedField == Fields.LTT_FIELD_TYPE) {
					// TraceDebug.debug("Field Type value is: " + fieldVal);
					// }
					break;
				} else {
					if (TraceDebug.isDEBUG()) {
						TraceDebug
								.debug("Unexpected field Type. Expected: Long, Received: "
										+ fieldObj.getClass().getSimpleName());
					}
					return null;
				}
			}
		}

		if (fieldVal == null) {
			if (TraceDebug.isDEBUG()) {
				sendNoFieldFoundMsg(fields, expectedFieldName);
			}
		}
		return fieldVal;
	}

	/**
	 * protected method used when a Field is requested among several available
	 * fields and the expected type is String
	 * 
	 * @param trcEvent
	 * @param traceSt
	 * @param expectedNumFields
	 * @return
	 */
	protected String getAFieldString(LttngEvent trcEvent,
			LttngTraceState traceSt, Fields expectedField) {
		String fieldVal = null;
		TmfEventField[] fields = trcEvent.getContent().getFields();

		// Only one field expected
		if (fields.length == 0) {
			TraceDebug.debug("Unexpected number of fields received: "
					+ fields.length);
			return null;
		}

		LttngEventField field;
		String fieldname;
		String expectedFieldName = expectedField.getInName();
		for (int i = 0; i < fields.length; i++) {
			field = (LttngEventField) fields[i];
			fieldname = field.getName();
			if (fieldname.equals(expectedFieldName)) {
				Object fieldObj = field.getValue();
				if (fieldObj instanceof String) {
					// Expected value found
					fieldVal = (String) field.getValue();
					break;
				} else {
					if (TraceDebug.isDEBUG()) {
						TraceDebug
								.debug("Unexpected field Type. Expected: String, Received: "
										+ fieldObj.getClass().getSimpleName());
					}
					return null;
				}
			}
		}

		if (fieldVal == null) {
			if (TraceDebug.isDEBUG()) {
				sendNoFieldFoundMsg(fields, expectedFieldName);
			}
		}
		return fieldVal;
	}

	protected void sendNoFieldFoundMsg(TmfEventField[] fields,
			String expectedFieldName) {
		LttngEventField field;
		StringBuilder sb = new StringBuilder("Field not found, requested: "
				+ expectedFieldName);
		sb.append(" number of fields: " + fields.length + "Fields: ");
		for (int i = 0; i < fields.length; i++) {
			field = (LttngEventField) fields[i];
			sb.append(field.getName() + " ");
		}

		TraceDebug.debug(sb.toString(), 5);
	}

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
	 * when the parent waits for its child terminaison, but may also happen in
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
		ts.getProcesses().remove(process);

		return true;
	}

	/**
	 * Find the process matching the given pid and cpu
	 * 
	 * If cpu is 0, the cpu value is not matched and the selection is based on
	 * pid value only
	 * 
	 * @param ts
	 * @param cpu
	 * @param pid
	 * @return
	 */
	protected LttngProcessState lttv_state_find_process(LttngTraceState ts,
			Long cpu, Long pid) {
		// Define the return value
		LttngProcessState process = null;

		// Obtain the list of available processes
		List<LttngProcessState> processList = ts.getProcesses();

		// find the process matching pid and cpu,
		// TODO: This may need to be improved since the pid may be re-used and
		// the creation time may need to be considered.
		// NOTE: A hash search shall be used
		for (LttngProcessState dprocess : processList) {
			if (dprocess.getPid().equals(pid)) {
				if (dprocess.getCpu().equals(cpu) || cpu.longValue() == 0L) {
					return dprocess;
				}
			}
		}

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
			LttngTraceState ts, Long cpu, Long pid, final TmfTimestamp timestamp) {

		LttngProcessState process = lttv_state_find_process(ts, cpu, pid);
		/* Put ltt_time_zero creation time for unexisting processes */
		if (process == null) {
			process = create_process(ts, cpu, pid, 0L, timestamp);
		}
		return process;
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
			Long cpu, Long pid, Long tgid, final TmfTimestamp timestamp) {
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
			final TmfTimestamp timestamp) {
		LttngProcessState process;
		process = new LttngProcessState(cpu, pid, tgid, name, timestamp);
		traceSt.getProcesses().add(process);
		return process;
	}

}