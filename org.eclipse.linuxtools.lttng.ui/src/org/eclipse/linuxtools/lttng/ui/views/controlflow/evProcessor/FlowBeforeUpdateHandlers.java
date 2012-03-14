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
 *   Michel Dagenais (michel.dagenais@polymtl.ca) - Reference C implementation, used with permission
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.controlflow.evProcessor;

import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.Fields;
import org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.ILttngEventProcessor;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngProcessState;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngTraceState;
import org.eclipse.linuxtools.lttng.ui.TraceDebug;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeEventProcess;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;

/**
 * Creates instances of specific before state update handlers, per corresponding
 * event.
 * 
 * @author alvaro
 * 
 */
class FlowBeforeUpdateHandlers {
	/**
	 * <p>
	 * Handles: LTT_EVENT_SYSCALL_ENTRY
	 * </p>
	 * Replace C function named "before_execmode_hook" in eventhooks.c
	 * 
	 * @return
	 */
	final ILttngEventProcessor getStateModesHandler() {
		AbsFlowTRangeUpdate handler = new AbsFlowTRangeUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				Long cpu = trcEvent.getCpuId();
				LttngProcessState stateProcess = traceSt.getRunning_process()
						.get(cpu);
				// TraceDebug.debug("Before handler called");
				String traceId = traceSt.getTraceId();

				if (stateProcess != null) {
					// Find process within the list of registered time-range
					// related
					// processes

					// key process attributes to look for it or store it
					// are: pid, birth, trace_num, note: cpu not relevant since
					// it
					// may change
					TimeRangeEventProcess localProcess = procContainer
							.findProcess(stateProcess.getPid(), stateProcess.getCpu(), traceId, stateProcess
                                    .getCreation_time());

					// Add process to process list if not present
					if (localProcess == null) {
						TmfTimeRange timeRange = traceSt.getContext()
								.getTraceTimeWindow();
						localProcess = addLocalProcess(stateProcess, timeRange
								.getStartTime().getValue(), timeRange
								.getEndTime().getValue(), traceId);
					}

					// Do the actual drawing
					makeDraw(traceSt, trcEvent.getTimestamp().getValue(),
							stateProcess, localProcess, params);
				} else {
					TraceDebug
							.debug("Running state process is null! (getStateModesHandler)"); //$NON-NLS-1$
				}

				return false;
			}
		};
		return handler;
	}

	/**
	 * <p>
	 * Handles: LTT_EVENT_SCHED_SCHEDULE
	 * </p>
	 * Replace C function named "before_schedchange_hook" in eventhooks.c
	 * <p>
	 * Fields: LTT_FIELD_PREV_PID, LTT_FIELD_NEXT_PID, LTT_FIELD_PREV_STATE (?)
	 * </p>
	 * 
	 * @return
	 */
	final ILttngEventProcessor getBeforeSchedChangeHandler() {
		AbsFlowTRangeUpdate handler = new AbsFlowTRangeUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				Long pid_out = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_PREV_PID);
				Long pid_in = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_NEXT_PID);

				// This is useless even in Lttv !!
				// Long state_out = getAFieldLong(trcEvent, traceSt,
				// Fields.LTT_FIELD_PREV_STATE);

				// We need to process information.
				LttngProcessState process = traceSt.getRunning_process().get(
						trcEvent.getCpuId());

				if (process != null) {
					if (process.getPid().equals(pid_out) == false) {
						// To replace :
						// process = lttv_state_find_process(ts,tfs->cpu,
						// pid_out);
						process = lttv_state_find_process(traceSt, trcEvent
								.getCpuId(), pid_out);
						// Also, removed :
						// guint trace_num = ts->parent.index;
					}

					if (process != null) {
						// TODO: Implement something similar to current hash in
						// order to keep track of the current process and speed
						// up finding the local resource.

						// HashedProcessData *hashed_process_data = NULL;
						// hashed_process_data =
						// processlist_get_process_data(process_list,pid_out,process->cpu,&birth,trace_num);
						TimeRangeEventProcess localProcess = procContainer
								.findProcess(process.getPid(), process.getCpu(), traceSt
										.getTraceId(), process.getCreation_time());

						// Add process to process list if not present
						// Replace C Call :
						// processlist_add(process_list,drawing,pid_out,process->tgid,process->cpu,process->ppid,&birth,trace_num,process->name,process->brand,&pl_height,&process_info,&hashed_process_data);
						if (localProcess == null) {
							TmfTimeRange timeRange = traceSt.getContext()
									.getTraceTimeWindow();
							localProcess = addLocalProcess(process, timeRange
									.getStartTime().getValue(), timeRange
									.getEndTime().getValue(), traceSt
									.getTraceId());
						}

						// Do the actual drawing
						makeDraw(traceSt, trcEvent.getTimestamp().getValue(),
								process,
								localProcess, params);
					} else {
						// Process may be null if the process started BEFORE the
						// trace start
						// TraceDebug.debug("Process is null for pid_out! (getBeforeSchedChangeHandler)");
					}

					// PID_IN section
					process = lttv_state_find_process(traceSt, trcEvent
							.getCpuId(), pid_in);

					if (process != null) {
						// HashedProcessData *hashed_process_data = NULL;
						// hashed_process_data =
						// processlist_get_process_data(process_list, pid_in,
						// tfs->cpu, &birth, trace_num);
						TimeRangeEventProcess localProcess = procContainer
								.findProcess(process.getPid(), process.getCpu(), traceSt
										.getTraceId(), process.getCreation_time());

						// Add process to process list if not present
						// Replace C Call :
						// processlist_add(process_list, drawing, pid_in,
						// process->tgid, tfs->cpu, process->ppid, &birth,
						// trace_num, process->name, process->brand, &pl_height,
						// &process_info, &hashed_process_data);
						if (localProcess == null) {
							TmfTimeRange timeRange = traceSt.getContext()
									.getTraceTimeWindow();
							localProcess = addLocalProcess(process, timeRange
									.getStartTime().getValue(), timeRange
									.getEndTime().getValue(), traceSt
									.getTraceId());
						}

						// Do the actual drawing
						makeDraw(traceSt, trcEvent.getTimestamp().getValue(),
								process,
								localProcess, params);

					} else {
						// Process can be null if it started AFTER the trace
						// end. Do nothing...
						// TraceDebug.debug("No process found for pid_in! Something is wrong? (getBeforeSchedChangeHandler)");
					}
				} else {
					TraceDebug
							.debug("Running process is null! (getBeforeSchedChangeHandler)"); //$NON-NLS-1$
				}

				return false;
			}
		};

		return handler;
	}

	/**
	 * <p>
	 * Handles: LTT_EVENT_PROCESS_EXIT
	 * </p>
	 * Replace C function named "before_process_exit_hook" in eventhooks.c
	 * 
	 * @return
	 */
	final ILttngEventProcessor getProcessExitHandler() {
		AbsFlowTRangeUpdate handler = new AbsFlowTRangeUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				// We need to process information.
				LttngProcessState process = traceSt.getRunning_process().get(
						trcEvent.getCpuId());

				if (process != null) {
					// TODO: Implement a similar method to track the current
					// local process in order to speed up finding the local
					// resource

					// hashed_process_data =
					// processlist_get_process_data(process_list, pid,
					// process->cpu, &birth,trace_num);
					TimeRangeEventProcess localProcess = procContainer
							.findProcess(process.getPid(), process.getCpu(), traceSt
									.getTraceId(), process.getCreation_time());

					// Add process to process list if not present
					// Replace C Call :
					// processlist_add(process_list, drawing, pid,
					// process->tgid, process->cpu, process->ppid, &birth,
					// trace_num, process->name, process->brand,&pl_height,
					// &process_info, &hashed_process_data);
					if (localProcess == null) {
						TmfTimeRange timeRange = traceSt.getContext()
								.getTraceTimeWindow();
						localProcess = addLocalProcess(process, timeRange
								.getStartTime().getValue(), timeRange
								.getEndTime().getValue(), traceSt.getTraceId());
					}

					// Call the function that does the actual drawing
					makeDraw(traceSt, trcEvent.getTimestamp().getValue(),
							process, localProcess, params);

				} else {
					TraceDebug
							.debug("Running process is null! (getProcessExitHandler)"); //$NON-NLS-1$
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
	 * Replace C function named "before_process_release_hook" in eventhooks.c
	 * <p>
	 * Fields: LTT_FIELD_PID
	 * </p>
	 * 
	 * @return
	 */
	final ILttngEventProcessor getProcessFreeHandler() {
		AbsFlowTRangeUpdate handler = new AbsFlowTRangeUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {
				// PID of the process to release
				Long release_pid = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_PID);

				if ((release_pid != null)) {
					LttngProcessState process = lttv_state_find_process(
							traceSt, ANY_CPU, release_pid);
					if (process != null) {

						// Replace the C call :
						// hashed_process_data =
						// processlist_get_process_data(process_list,pid,process->cpu,&birth,trace_num);
						TimeRangeEventProcess localProcess = procContainer
								.findProcess(process.getPid(), process.getCpu(), traceSt
										.getTraceId(), process
                                        .getCreation_time());

						// This is as it was in the C ... ?
						if (localProcess == null) {
							return false;
						}

						// Perform the drawing
						makeDraw(traceSt, trcEvent.getTimestamp().getValue(),
								process,
								localProcess, params);
					}
				} else {
					TraceDebug
							.debug("Release_pid is null! (getProcessFreeHandler)"); //$NON-NLS-1$
				}

				return false;
			}
		};
		return handler;
	}

	/**
	 * <p>
	 * Handles: LTT_EVENT_STATEDUMP_END
	 * </p>
	 * Replace C function named "before_statedump_end" in eventhooks.c
	 * 
	 * @return
	 */
	final ILttngEventProcessor getStateDumpEndHandler() {
		AbsFlowTRangeUpdate handler = new AbsFlowTRangeUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				// What's below should replace the following call in C :
				// ClosureData closure_data;
				// closure_data.events_request = events_request;
				// closure_data.tss = tss;
				// closure_data.end_time = evtime;
				// convert_time_to_pixels(time_window,evtime,width,&closure_data.x_end);
				// g_hash_table_foreach(process_list->process_hash,
				// draw_closure,(void*)&closure_data);
				//
				// And the draw is always the same then...

				// The c-library loops through the local processes, search for
				// the local processes in the state provider and then draws
				// If it's present is the local processes why shuldn't they be
				// present in the state provider?
				// This seems more direct. and makes sure all processes are
				// reflected in the control flow view.
				LttngProcessState[] processes = traceSt.getProcesses();
				for (int pos=0; pos < processes.length; pos++) {
					LttngProcessState process = processes[pos];
					
					// Replace the C call :
					// hashed_process_data =
					// processlist_get_process_data(process_list,pid,process->cpu,&birth,trace_num);
					TimeRangeEventProcess localProcess = procContainer
							.findProcess(process.getPid(), process.getCpu(), traceSt
									.getTraceId(), process
                                    .getCreation_time());

					// Add process to process list if not present
					if (localProcess == null) {
						TmfTimeRange timeRange = traceSt.getContext()
								.getTraceTimeWindow();
						localProcess = addLocalProcess(process, timeRange
								.getStartTime().getValue(), timeRange
								.getEndTime().getValue(), traceSt.getTraceId());
					}

					// Call the function that will does the actual
					// drawing
					makeDraw(traceSt, trcEvent.getTimestamp().getValue(),
							process, localProcess, params);
				}

				return false;
			}
		};

		return handler;
	}

}
