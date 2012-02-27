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

import org.eclipse.linuxtools.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.lttng.core.state.StateStrings.Fields;
import org.eclipse.linuxtools.lttng.core.state.evProcessor.ILttngEventProcessor;
import org.eclipse.linuxtools.lttng.core.state.model.LttngProcessState;
import org.eclipse.linuxtools.lttng.core.state.model.LttngTraceState;
import org.eclipse.linuxtools.lttng.ui.TraceDebug;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeEventProcess;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;

/**
 * Creates instances of specific after state update handlers, per corresponding
 * event.
 * 
 * @author alvaro
 * 
 */
class FlowAfterUpdateHandlers {
	/**
	 * <p>
	 * Handles: LTT_EVENT_SCHED_SCHEDULE
	 * </p>
	 * Replace C function "after_schedchange_hook" in eventhooks.c
	 * <p>
	 * Fields: LTT_FIELD_PREV_PID, LTT_FIELD_NEXT_PID
	 * </p>
	 * 
	 * @return
	 */
	final ILttngEventProcessor getSchedChangeHandler() {
		AbsFlowTRangeUpdate handler = new AbsFlowTRangeUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {
				
				// get event time, cpu, trace_number, process, pid
				LttngProcessState process_in = traceSt.getRunning_process().get(trcEvent.getCpuId());
				
				// pid_out is never used, even in LTTv!
				//Long pid_out = getAFieldLong(trcEvent, traceSt, Fields.LTT_FIELD_PREV_PID);
				Long pid_in = getAFieldLong(trcEvent, traceSt, Fields.LTT_FIELD_NEXT_PID);
				
				if ( !(pid_in.equals(process_in.getPid())) ) {
				    TraceDebug.debug("pid_in != PID!  (getSchedChangeHandler)"); //$NON-NLS-1$
                }
				
				//hashed_process_data = processlist_get_process_data(process_list,pid_out,process->cpu,&birth,trace_num);
				TimeRangeEventProcess localProcess = procContainer.findProcess(pid_in, process_in.getCpu(), traceSt
						.getTraceId(), process_in.getCreation_time());
				
				if ( localProcess == null ) {
					if ( (pid_in == 0) || (pid_in != process_in.getPpid()) ) {
					    TmfTimeRange timeRange = traceSt.getContext().getTraceTimeWindow();
	                    localProcess = addLocalProcess(process_in, timeRange.getStartTime().getValue(), timeRange.getEndTime().getValue(), traceSt.getTraceId());
					}
					else {
						TraceDebug
								.debug("pid_in is not 0 or pid_in == PPID!  (getSchedChangeHandler)"); //$NON-NLS-1$
					}
				}

				// There is no drawing done by the C code below, only refreshing
				// the references to the current hash data to make it ready for
				// next event

				// This current implementation does not support the use of
				// current hashed data
				// although an equivalent would be good in order to improve the
				// time to find the currently running process per cpu.
				/*
				if(ltt_time_compare(hashed_process_data_in->next_good_time, evtime) <= 0)
				{
				    TimeWindow time_window = lttvwindow_get_time_window(control_flow_data->tab);
				  
					#ifdef EXTRA_CHECK
				    if(ltt_time_compare(evtime, time_window.start_time) == -1 || ltt_time_compare(evtime, time_window.end_time) == 1)
				       return FALSE;
					#endif //EXTRA_CHECK
				
				    Drawing_t *drawing = control_flow_data->drawing;
				    guint width = drawing->width;
				    guint new_x;
				      
				    convert_time_to_pixels(time_window,evtime,width,&new_x);
				  	
				    if(hashed_process_data_in->x.middle != new_x) {
				        hashed_process_data_in->x.middle = new_x;
				        hashed_process_data_in->x.middle_used = FALSE;
				        hashed_process_data_in->x.middle_marked = FALSE;
				    }
				}*/
				
				return false;

			}
		};
		return handler;
	}

	/**
	 * <p>
	 * Handles: LTT_EVENT_PROCESS_FORK
	 * </p>
	 * Replace C function "after_process_fork_hook" in eventhooks.c
	 * <p>
	 * Fields: LTT_FIELD_CHILD_PID
	 * </p>
	 * 
	 * @return
	 */
	final ILttngEventProcessor getProcessForkHandler() {
	    AbsFlowTRangeUpdate handler = new AbsFlowTRangeUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {  
			    
                Long child_pid = getAFieldLong(trcEvent, traceSt, Fields.LTT_FIELD_CHILD_PID);
                LttngProcessState process_child = lttv_state_find_process(traceSt, trcEvent.getCpuId(), child_pid );
			    
			    if (process_child != null) {
			        TimeRangeEventProcess localProcess = procContainer.findProcess(process_child.getPid(), process_child.getCpu(), traceSt.getTraceId(), process_child.getCreation_time() );
			        
			        if (localProcess == null) {
			            if (child_pid == 0 || !child_pid.equals(process_child.getPpid())) {            
			                TmfTimeRange timeRange = traceSt.getContext().getTraceTimeWindow();
                            localProcess = addLocalProcess(process_child, timeRange.getStartTime().getValue(), timeRange.getEndTime().getValue(), traceSt.getTraceId());
			            }
			            else {
			                TraceDebug.debug("localProcess is null with child_pid not 0 or child_pid equals PPID (getProcessForkHandler)"); //$NON-NLS-1$
			            }
					} else {
						// If we found the process, the Ppid and the Tgid might
						// be missing, let's add them
						localProcess.setPpid(process_child.getPpid());
						localProcess.setTgid(process_child.getTgid());
			        }
			    }
			    else {
			        TraceDebug.debug("process_child is null! (getProcessForkHandler)"); //$NON-NLS-1$
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
	 * Replace C function "after_process_exit_hook" in eventhooks.c
	 * 
	 * @return
	 */
	final ILttngEventProcessor getProcessExitHandler() {
	    AbsFlowTRangeUpdate handler = new AbsFlowTRangeUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {
			    
			    LttngProcessState process = traceSt.getRunning_process().get(trcEvent.getCpuId());

			    if (process != null) {
			        
					// *** TODO: ***
					// We shall look into a way to find the current process
					// faster, see the c library
					// (current_hash) in order to speed up the find. see c-code
			        //   if(likely(process_list->current_hash_data[trace_num][cpu] != NULL) ){
		            //        hashed_process_data = process_list->current_hash_data[trace_num][cpu];
		            //   }
			        TimeRangeEventProcess localProcess = procContainer.findProcess(process.getPid(), process.getCpu(), traceSt.getTraceId(),  process.getCreation_time());
			        
			        if (localProcess == null) {
			            if (process.getPid() == 0 || !process.getPid().equals(process.getPpid())) {			                
			                TmfTimeRange timeRange = traceSt.getContext().getTraceTimeWindow();
		                    localProcess = addLocalProcess(process, timeRange.getStartTime().getValue(), timeRange.getEndTime().getValue(), traceSt.getTraceId());
			            }
			            else {
			                TraceDebug.debug("process pid is not 0 or pid equals ppid! (getProcessExitHandler)"); //$NON-NLS-1$
			            }
					}
			    }
			    else {
			        TraceDebug.debug("process is null! (getProcessExitHandler)"); //$NON-NLS-1$
			    }			    
			    
				return false;
			}
		};
		return handler;
	}

	
	/**
	 * <p>
	 * Handles: LTT_EVENT_EXEC
	 * </p>
	 * Replace C function "after_fs_exec_hook" in eventhooks.c
	 * 
	 * @return
	 */
	final ILttngEventProcessor getProcessExecHandler() {
	    AbsFlowTRangeUpdate handler = new AbsFlowTRangeUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {
			    
			    LttngProcessState process = traceSt.getRunning_process().get(trcEvent.getCpuId());

                if (process != null) {
                    
                    TimeRangeEventProcess localProcess = procContainer.findProcess(process.getPid(),  process.getCpu(), traceSt.getTraceId(), process.getCreation_time());
                    
                    if (localProcess == null) {
                        if (process.getPid() == 0 || !process.getPid().equals(process.getPpid())) {
                            TmfTimeRange timeRange = traceSt.getContext().getTraceTimeWindow();
                            localProcess = addLocalProcess(process, timeRange.getStartTime().getValue(), timeRange.getEndTime().getValue(), traceSt.getTraceId());
                        }
                        else {
                            TraceDebug.debug("process pid is not 0 or pid equals ppid! (getProcessExecHandler)"); //$NON-NLS-1$
                        }
                    }
                    else {
                        // If we found the process, the name might be missing. Let's add it here.
                        localProcess.setName(process.getName());
                    }
                }
                else {
                    TraceDebug.debug("process is null! (getProcessExecHandler)"); //$NON-NLS-1$
                }
                
				return false;
			}
		};
		return handler;
	}
	
	/**
	 * <p>
	 * LTT_EVENT_THREAD_BRAND
	 * </p>
	 * Replace C function "after_user_generic_thread_brand_hook" in eventhooks.c
	 * 
	 * @return
	 */
	final ILttngEventProcessor GetThreadBrandHandler() {
	    AbsFlowTRangeUpdate handler = new AbsFlowTRangeUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {
			    
			    LttngProcessState process = traceSt.getRunning_process().get(trcEvent.getCpuId());

                if (process != null) {
                    
					// Similar to above comments, implement a faster way to find
					// the local process
                    //   if(likely(process_list->current_hash_data[trace_num][cpu] != NULL) ){
                    //        hashed_process_data = process_list->current_hash_data[trace_num][cpu];
                    //   }
                    TimeRangeEventProcess localProcess = procContainer.findProcess(process.getPid(), process.getCpu(), traceSt.getTraceId(), process.getCreation_time());
                    
                    if (localProcess == null) {
                        if (process.getPid() == 0 || !process.getPid().equals(process.getPpid())) {                         
                            TmfTimeRange timeRange = traceSt.getContext().getTraceTimeWindow();
                            localProcess = addLocalProcess(process, timeRange.getStartTime().getValue(), timeRange.getEndTime().getValue(), traceSt.getTraceId());
                        }
                        else {
                            TraceDebug.debug("process pid is not 0 or pid equals ppid! (GetThreadBrandHandler)"); //$NON-NLS-1$
                        }
                    }
                    else {
						// If we found the process, the brand might be missing
						// on it, add it.
                        localProcess.setBrand(process.getBrand());
                    }
                }
                else {
                    TraceDebug.debug("process is null! (GetThreadBrandHandler)"); //$NON-NLS-1$
                }
                
                return false;
			    
			}
		};
		return handler;
	}

	/**
	 * <p>
	 * LTT_EVENT_PROCESS_STATE
	 * </p>
	 * Replace C function "after_event_enum_process_hook" in eventhooks.c
	 * <p>
	 * <p>
	 * Creates the processlist entry for the child process. Put the last
	 * position in x at the current time value.
	 * </p>
	 * 
	 * <p>
	 * Fields: LTT_FIELD_PID
	 * </p>
	 * 
	 * @return
	 */
	final ILttngEventProcessor getEnumProcessStateHandler() {
	    AbsFlowTRangeUpdate handler = new AbsFlowTRangeUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {
			    
				int first_cpu;
				int nb_cpus;
			    
				Long pid_in = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_PID);
			    
			    if ( pid_in != null  ) {
    			    if(pid_in == 0L) {
						first_cpu = 0;
						nb_cpus = traceSt.getNumberOfCPUs();
    			    } 
    			    else {
						first_cpu = ANY_CPU.intValue();
						nb_cpus = ANY_CPU.intValue() + 1;
    			    }
    			    
					for (int cpu = first_cpu; cpu < nb_cpus; cpu++) {
						LttngProcessState process_in = lttv_state_find_process(traceSt, Long.valueOf(cpu), pid_in);
    			        
    			        if ( process_in != null ) {
    			            TimeRangeEventProcess localProcess = procContainer.findProcess(process_in.getPid(), process_in.getCpu(), traceSt.getTraceId(), process_in.getCreation_time());
    	                    
    			            if (localProcess == null) {
        			            if (process_in.getPid() == 0 || !process_in.getPid().equals(process_in.getPpid())) {
                                    TmfTimeRange timeRange = traceSt.getContext().getTraceTimeWindow();
                                    localProcess = addLocalProcess(process_in, timeRange.getStartTime().getValue(), timeRange.getEndTime().getValue(), traceSt.getTraceId());
                                }
                                else {
                                    TraceDebug.debug("process pid is not 0 or pid equals ppid! (getEnumProcessStateHandler)"); //$NON-NLS-1$
                                    return false;
                                }
    			            }

							// If the process was found, it might be missing
							// informations, add it here
							localProcess.setName(process_in.getName());
							localProcess.setPpid(process_in.getPpid());
							localProcess.setTgid(process_in.getTgid());
    			        }
    			        else {
    			            TraceDebug.debug("process_in is null! This should never happen. (getEnumProcessStateHandler)"); //$NON-NLS-1$
    			        }
    			    }
			    }
			    else {
			        TraceDebug.debug("pid_in is null! This should never happen, really... (getEnumProcessStateHandler)"); //$NON-NLS-1$
			    }
			    
				return false;
			}
		};
		return handler;
	}

}
