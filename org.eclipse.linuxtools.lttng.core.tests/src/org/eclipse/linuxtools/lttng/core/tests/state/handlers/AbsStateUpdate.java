package org.eclipse.linuxtools.lttng.core.tests.state.handlers;

import org.eclipse.linuxtools.internal.lttng.core.TraceDebug;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngEventField;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.Fields;
import org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.ILttngEventProcessor;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngExecutionState;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngTraceState;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;

@SuppressWarnings("nls")
public abstract class AbsStateUpdate implements ILttngEventProcessor {

	// ========================================================================
	// Data
	// =======================================================================
	protected static LttngExecutionState exState = null;
	protected static Long pid = null;
	
	// ========================================================================
	// push and pop from stack
	// =======================================================================

//	protected static final Long ANY_CPU = 0L;
	

//	protected void push_state(Long cpu, StateStrings.ExecutionMode execMode,
//			String submode, TmfTimestamp eventTime, LttngTraceState traceSt) {
//
//		LttngProcessState process = traceSt.getRunning_process().get(cpu);
//		LttngExecutionState exe_state = new LttngExecutionState();
//		exe_state.setExec_mode(execMode);
//		exe_state.setExec_submode(submode);
//		exe_state.setEntry_Time(eventTime);
//		exe_state.setChage_Time(eventTime);
//		exe_state.setCum_cpu_time(0L);
//		exe_state.setProc_status(process.getState().getProc_status());
//		process.setState(exe_state);
//
//		Stack<LttngExecutionState> exe_state_stack = process
//				.getExecution_stack();
//		exe_state_stack.push(exe_state);
//	}
//
//	protected void pop_state(Long cpu, StateStrings.ExecutionMode execMode,
//			LttngTraceState traceSt, TmfTimestamp eventTime) {
//
//		LttngProcessState process = traceSt.getRunning_process().get(cpu);
//
//		if (!process.getState().getExec_mode().equals(execMode)) {
//			// Different execution mode
//			TraceDebug.debug("Different Execution Mode type \n\tTime:"
//					+ eventTime.toString() + "\n\tprocess state has: \n\t"
//					+ process.getState().getExec_mode().toString()
//					+ "\n\twhen pop_int is:\n\t" + execMode.toString());
//			return;
//		}
//
//		Stack<LttngExecutionState> exe_state_stack = process
//				.getExecution_stack();
//
//		if (exe_state_stack.size() <= 1) {
//			TraceDebug
//					.debug("Removing last item from execution stack is not allowed");
//		}
//
//		exe_state_stack.pop();
//		process.setState(exe_state_stack.peek());
//		process.getState().setChage_Time(eventTime);
//	}
//
//	protected void irq_push_mode(LttngIRQState irqst, IRQMode state) {
//		irqst.getMode_stack().push(state);
//	}
//
//	protected void irq_set_base_mode(LttngIRQState irqst, IRQMode state) {
//		Stack<IRQMode> irqModeStack = irqst.getMode_stack();
//		irqModeStack.clear();
//		irqModeStack.push(state);
//	}
//
//	protected void irq_pop_mode(LttngIRQState irqst) {
//		Stack<IRQMode> irq_stack = irqst.getMode_stack();
//		if (irq_stack.size() <= 1)
//			irq_set_base_mode(irqst, IRQMode.LTTV_IRQ_UNKNOWN);
//		else
//			irq_stack.pop();
//	}
//
//	protected void cpu_push_mode(LTTngCPUState cpust, StateStrings.CpuMode state) {
//		// The initialization (init) creates a LttngCPUState instance per
//		// available cpu in the system
//		Stack<CpuMode> cpuStack = cpust.getMode_stack();
//		cpuStack.push(state);
//	}
//
//	protected void cpu_pop_mode(LTTngCPUState cpust) {
//		if (cpust.getMode_stack().size() <= 1)
//			cpu_set_base_mode(cpust, StateStrings.CpuMode.LTTV_CPU_UNKNOWN);
//		else
//			cpust.getMode_stack().pop();
//	}
//
//	/* clears the stack and sets the state passed as argument */
//	protected void cpu_set_base_mode(LTTngCPUState cpust,
//			StateStrings.CpuMode state) {
//		Stack<CpuMode> cpuStack = cpust.getMode_stack();
//		cpuStack.clear();
//		cpuStack.push(state);
//	}
//
//	protected void bdev_pop_mode(LttngBdevState bdevst) {
//		Stack<BdevMode> bdevModeStack = bdevst.getMode_stack();
//		if (bdevModeStack.size() <= 1) {
//			bdev_set_base_mode(bdevModeStack, BdevMode.LTTV_BDEV_UNKNOWN);
//		} else {
//			bdevModeStack.pop();
//		}
//
//	}
//
//	protected void bdev_set_base_mode(Stack<BdevMode> bdevModeStack,
//			BdevMode state) {
//		bdevModeStack.clear();
//		bdevModeStack.push(state);
//	}
//
//	/**
//	 * Push a new received function pointer to the user_stack
//	 * 
//	 * @param traceSt
//	 * @param funcptr
//	 * @param cpu
//	 */
//	protected void push_function(LttngTraceState traceSt, Long funcptr, Long cpu) {
//		// Get the related process
//		LttngProcessState process = traceSt.getRunning_process().get(cpu);
//
//		// get the user_stack
//		Stack<Long> user_stack = process.getUser_stack();
//
//		// update stack
//		user_stack.push(funcptr);
//
//		// update the pointer to the current function on the corresponding
//		// process
//		process.setCurrent_function(funcptr);
//	}
//
//	protected void pop_function(LttngTraceState traceSt, LttngEvent trcEvent,
//			Long funcptr) {
//		Long cpu = trcEvent.getCpuId();
//		// LttvTraceState *ts = (LttvTraceState*)tfs->parent.t_context;
//		// LttvProcessState *process = ts->running_process[cpu];
//		LttngProcessState process = traceSt.getRunning_process().get(cpu);
//		Long curr_function = process.getCurrent_function();
//		if (curr_function != null && curr_function != funcptr) {
//			TraceDebug.debug("Different functions: " + funcptr + " current: "
//					+ curr_function + " time stamp: "
//					+ trcEvent.getTimestamp().toString());
//
//			// g_info("Different functions (%lu.%09lu): ignore it\n",
//			// tfs->parent.timestamp.tv_sec, tfs->parent.timestamp.tv_nsec);
//			// g_info("process state has %" PRIu64 " when pop_function is %"
//			// PRIu64
//			// "\n",
//			// process->current_function, funcptr);
//			// g_info("{ %u, %u, %s, %s, %s }\n",
//			// process->pid,
//			// process->ppid,
//			// g_quark_to_string(process->name),
//			// g_quark_to_string(process->brand),
//			// g_quark_to_string(process->state->s));
//			return;
//		}
//
//		Stack<Long> user_stack = process.getUser_stack();
//		if (user_stack.size() == 0) {
//			TraceDebug
//					.debug("Trying to pop last function in stack. Ignored.  Time Stamp: "
//							+ trcEvent.getTimestamp());
//			return;
//		}
//		user_stack.pop();
//		process.setCurrent_function(user_stack.peek());
//	}
//
//	// ========================================================================
//	// General methods
//	// =======================================================================
//	/**
//	 * protected method used when only one Field is expected with Type "Long" if
//	 * the number of fields is greater, the first field is returned and a
//	 * tracing message is sent Null is returned if the value could not be
//	 * extracted.
//	 * 
//	 * @param trcEvent
//	 * @param traceSt
//	 * @param expectedNumFields
//	 * @return
//	 */
//	protected Long getDField(LttngEvent trcEvent, LttngTraceState traceSt,
//			Fields expectedField) {
//		Long fieldVal = null;
//		TmfEventField[] fields = trcEvent.getContent().getFields();
//		String[] fieldLabels = trcEvent.getContent().getFormat().getLabels();
//
//		// Only one field expected
//		if (fields.length != 1 || fieldLabels.length != 1) {
//			StringBuilder sb = new StringBuilder(
//					"Unexpected number of fields received: " + fields.length
//							+ " for Event: " + trcEvent.getMarkerName() + "\n\t\tFields: ");
//
//			for (TmfEventField field : fields) {
//				sb.append(((LttngEventField)field).getName() + " ");				
//			}
//
//			TraceDebug.debug(sb.toString());
//			if (fields.length == 0) {
//				return null;
//			}
//		}
//
//		LttngEventField field = (LttngEventField) fields[0];
//		String fieldname = field.getName();
//		String expectedFieldName = expectedField.getInName();
//		if (fieldname.equals(expectedFieldName)) {
//			Object fieldObj = field.getValue();
//			if (fieldObj instanceof Long) {
//				// Expected value found
//				fieldVal = (Long) field.getValue();
//			} else {
//				if (TraceDebug.isDEBUG()) {
//					TraceDebug
//							.debug("Unexpected field Type. Expected: Long, Received: "
//									+ fieldObj.getClass().getSimpleName());
//				}
//				return null;
//			}
//		} else {
//			TraceDebug.debug("Unexpected field received: " + fieldname
//					+ " Expected: " + expectedFieldName);
//			return null;
//		}
//
//		return fieldVal;
//	}
//
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

//	// Adaption from MKDEV macro
//	protected Long mkdev(Long major, Long minor) {
//		Long result = null;
//		if (major != null && minor != null) {
//			result = (major << 20) | minor;
//		}
//		return result;
//	}
//
//	/*
//	 * FIXME : this function should be called when we receive an event telling
//	 * that release_task has been called in the kernel. In happens generally
//	 * when the parent waits for its child terminaison, but may also happen in
//	 * special cases in the child's exit : when the parent ignores its children
//	 * SIGCCHLD or has the flag SA_NOCLDWAIT. It can also happen when the child
//	 * is part of a killed thread group, but isn't the leader.
//	 */
//	protected boolean exit_process(LttngTraceState ts, LttngProcessState process) {
//		/*
//		 * Wait for both schedule with exit dead and process free to happen.
//		 * They can happen in any order.
//		 */
//		process.incrementFree_events();
//		if (process.getFree_events() < 2) {
//			return false;
//		}
//
//		process.getExecution_stack().clear();
//		process.getUser_stack().clear();
//		ts.getProcesses().remove(process);
//
//		return true;
//	}
//
//	// LttvProcessState *
//	// lttv_state_create_process(LttvTraceState *tcs, LttvProcessState *parent,
//	// guint cpu, guint pid, guint tgid, GQuark name, const LttTime *timestamp)
//	// {
//	// LttvProcessState *process = g_new(LttvProcessState, 1);
//	//
//	// LttvExecutionState *es;
//	//
//	// char buffer[128];
//	//
//	// process->pid = pid;
//	// process->tgid = tgid;
//	// process->cpu = cpu;
//	// process->name = name;
//	// process->brand = LTTV_STATE_UNBRANDED;
//	// //process->last_cpu = tfs->cpu_name;
//	// //process->last_cpu_index =
//	// ltt_tracefile_num(((LttvTracefileContext*)tfs)->tf);
//	// process->type = LTTV_STATE_USER_THREAD;
//	// process->usertrace = ltt_state_usertrace_find(tcs, pid, timestamp);
//	// process->current_function = 0; //function 0x0 by default.
//	//
//	// g_info("Process %u, core %p", process->pid, process);
//	// g_hash_table_insert(tcs->processes, process, process);
//	//
//	// if(parent) {
//	// process->ppid = parent->pid;
//	// process->creation_time = *timestamp;
//	// }
//	//
//	// /* No parent. This process exists but we are missing all information
//	// about
//	// its creation. The birth time is set to zero but we remember the time of
//	// insertion */
//	//
//	// else {
//	// process->ppid = 0;
//	// process->creation_time = ltt_time_zero;
//	// }
//	//
//	// process->insertion_time = *timestamp;
//	// sprintf(buffer,"%d-%lu.%lu",pid, process->creation_time.tv_sec,
//	// process->creation_time.tv_nsec);
//	// process->pid_time = g_quark_from_string(buffer);
//	// process->cpu = cpu;
//	// process->free_events = 0;
//	// //process->last_cpu = tfs->cpu_name;
//	// //process->last_cpu_index =
//	// ltt_tracefile_num(((LttvTracefileContext*)tfs)->tf);
//	// process->execution_stack = g_array_sized_new(FALSE, FALSE,
//	// sizeof(LttvExecutionState), PREALLOCATED_EXECUTION_STACK);
//	// process->execution_stack = g_array_set_size(process->execution_stack, 2);
//	// es = process->state = &g_array_index(process->execution_stack,
//	// LttvExecutionState, 0);
//	// es->t = LTTV_STATE_USER_MODE;
//	// es->n = LTTV_STATE_SUBMODE_NONE;
//	// es->entry = *timestamp;
//	// //g_assert(timestamp->tv_sec != 0);
//	// es->change = *timestamp;
//	// es->cum_cpu_time = ltt_time_zero;
//	// es->s = LTTV_STATE_RUN;
//	//
//	// es = process->state = &g_array_index(process->execution_stack,
//	// LttvExecutionState, 1);
//	// es->t = LTTV_STATE_SYSCALL;
//	// es->n = LTTV_STATE_SUBMODE_NONE;
//	// es->entry = *timestamp;
//	// //g_assert(timestamp->tv_sec != 0);
//	// es->change = *timestamp;
//	// es->cum_cpu_time = ltt_time_zero;
//	// es->s = LTTV_STATE_WAIT_FORK;
//	//	  
//	// /* Allocate an empty function call stack. If it's empty, use 0x0. */
//	// process->user_stack = g_array_sized_new(FALSE, FALSE,
//	// sizeof(guint64), 0);
//	//	  
//	// return process;
//	// }
//
//	/**
//	 * Find the process matching the given pid and cpu
//	 * 
//	 * If cpu is 0, the cpu value is not matched and the selection is based on
//	 * pid value only
//	 * 
//	 * @param ts
//	 * @param cpu
//	 * @param pid
//	 * @return
//	 */
//	protected LttngProcessState lttv_state_find_process(LttngTraceState ts,
//			Long cpu, Long pid) {
//		// Define the return value
//		LttngProcessState process = null;
//
//		// Obtain the list of available processes
//		List<LttngProcessState> processList = ts.getProcesses();
//
//		// find the process matching pid and cpu,
//		// TODO: This may need to be improved since the pid may be re-used and
//		// the creation time may need to be considered
//		for (LttngProcessState dprocess : processList) {
//			if (dprocess.getPid() == pid) {
//				if (dprocess.getCpu() == cpu || cpu == 0) {
//					return process;
//				}
//			}
//		}
//
//		return process;
//	}
//
//	/**
//	 * @param ts
//	 * @param cpu
//	 * @param pid
//	 * @param timestamp
//	 *            , Used when a new process is needed
//	 * @return
//	 */
//	protected LttngProcessState lttv_state_find_process_or_create(
//			LttngTraceState ts, Long cpu, Long pid, final TmfTimestamp timestamp) {
//
//		LttngProcessState process = lttv_state_find_process(ts, cpu, pid);
//		/* Put ltt_time_zero creation time for unexisting processes */
//		if (process == null) {
//			process = create_process(ts, cpu, pid, 0L, timestamp);
//		}
//		return process;
//	}
//
//	/**
//	 * @param traceSt
//	 * @param cpu
//	 * @param pid
//	 * @param tgid
//	 * @param timestamp
//	 * @return
//	 */
//	protected LttngProcessState create_process(LttngTraceState traceSt,
//			Long cpu, Long pid, Long tgid, final TmfTimestamp timestamp) {
//		LttngProcessState process = create_process(traceSt, cpu, pid, tgid,
//				ProcessStatus.LTTV_STATE_UNNAMED.getInName(), timestamp);
//		return process;
//	}
//
//	/**
//	 * @param traceSt
//	 * @param cpu
//	 * @param pid
//	 * @param tgid
//	 * @param name
//	 * @param timestamp
//	 * @return
//	 */
//	protected LttngProcessState create_process(LttngTraceState traceSt,
//			Long cpu, Long pid, Long tgid, String name,
//			final TmfTimestamp timestamp) {
//		LttngProcessState process;
//		process = new LttngProcessState(cpu, pid, tgid, name, timestamp);
//		traceSt.getProcesses().add(process);
//		return process;
//	}

}