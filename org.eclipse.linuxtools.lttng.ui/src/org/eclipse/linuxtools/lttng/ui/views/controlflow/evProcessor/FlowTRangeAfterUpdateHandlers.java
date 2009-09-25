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
package org.eclipse.linuxtools.lttng.ui.views.controlflow.evProcessor;

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.state.StateStrings.Events;
import org.eclipse.linuxtools.lttng.state.StateStrings.Fields;
import org.eclipse.linuxtools.lttng.state.evProcessor.IEventProcessing;
import org.eclipse.linuxtools.lttng.state.model.LttngProcessState;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;
import org.eclipse.linuxtools.lttng.ui.TraceDebug;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeEventProcess;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;

/**
 * Creates instances of specific after state update handlers, per corresponding
 * event.
 * 
 * @author alvaro
 * 
 */
class FlowTRangeAfterUpdateHandlers {
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
	final IEventProcessing getSchedChangeHandler() {
		AbsFlowTRangeUpdate handler = new AbsFlowTRangeUpdate() {

			private Events eventType = Events.LTT_EVENT_SCHED_SCHEDULE;

			// @Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {
				
				// get event time, cpu, trace_number, process, pid
				LttngProcessState process_in = traceSt.getRunning_process().get(trcEvent.getCpuId());
				
				// pid_out is never used, even in LTTv!
				//Long pid_out = getAFieldLong(trcEvent, traceSt, Fields.LTT_FIELD_PREV_PID);
				Long pid_in = getAFieldLong(trcEvent, traceSt, Fields.LTT_FIELD_NEXT_PID);
				
				// *** VERIFY ***
				// LTTV modify tracefile context with pid_in... should we do something with that?
				// tfc->target_pid = pid_in;
				
				if ( !(pid_in.equals(process_in.getPid())) ) {
				    TraceDebug.debug("pid_in != PID!  (getSchedChangeHandler)");
                }
				
				//hashed_process_data = processlist_get_process_data(process_list,pid_out,process->cpu,&birth,trace_num);
				TimeRangeEventProcess localProcess = procContainer.findProcess(process_in.getPid(), process_in.getCreation_time().getValue(), traceSt.getTraceId() );
				
				if ( localProcess == null ) {
					if ( (pid_in == 0) || (pid_in != process_in.getPpid()) ) {
					    TmfTimeRange timeRange = traceSt.getInputDataRef().getTraceTimeWindow();
	                    localProcess = addLocalProcess(process_in, timeRange.getStartTime().getValue(), timeRange.getEndTime().getValue(), traceSt.getTraceId());
					}
					else {
					    TraceDebug.debug("pid_in is 0 or pid_in != PPID!  (getSchedChangeHandler)");
					}
				}
				
				// *** VERIFY ***
				// We doesn't seem to be doing anything about this C call... should we?
				//process_list->current_hash_data[trace_num][process_in->cpu] = hashed_process_data_in;
				
				// *** VERIFY ***
				// There doesn't seem to be any drawing done by the below C code ??
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

			// @Override
			public Events getEventHandleType() {
				return eventType;
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
	final IEventProcessing getProcessForkHandler() {
	    AbsFlowTRangeUpdate handler = new AbsFlowTRangeUpdate() {

			private Events eventType = Events.LTT_EVENT_PROCESS_FORK;

			// @Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {  
			    
                Long child_pid = getAFieldLong(trcEvent, traceSt, Fields.LTT_FIELD_CHILD_PID);
                LttngProcessState process_child = lttv_state_find_process(traceSt, trcEvent.getCpuId(), child_pid );
			    
			    if ( process_child != null ) {
			        TimeRangeEventProcess localProcess = procContainer.findProcess(process_child.getPid(), process_child.getCreation_time().getValue(), traceSt.getTraceId() );
			        
			        if ( localProcess == null ) {
			            if ( (child_pid == 0) || (child_pid != process_child.getPpid()) ) {            
			                // *** VERIFY ***
			                // What am I supposed to do with that?
			                //   Drawing_t *drawing = control_flow_data->drawing;
			                //   ProcessInfo *process_info;
			                //   gtk_widget_set_size_request(drawing->drawing_area, -1, pl_height);
			                //   gtk_widget_queue_draw(drawing->drawing_area);
			                TmfTimeRange timeRange = traceSt.getInputDataRef().getTraceTimeWindow();
                            localProcess = addLocalProcess(process_child, timeRange.getStartTime().getValue(), timeRange.getEndTime().getValue(), traceSt.getTraceId());
			            }
			            else {
			                TraceDebug.debug("localProcess is null with child_pid not 0 or child_pid equals PPID (getProcessForkHandler)");
			            }
			        }
			        else {
			            // If we found the process, the Ppid and the Tgid might be missing, let's add them
			            localProcess.setPpid(process_child.getPpid());
			            localProcess.setTgid(process_child.getTgid());
			            
			        }
			    }
			    else {
			        TraceDebug.debug("process_child is null! (getProcessForkHandler)");
			    }
                
			    // *** VERIFY ***
			    // We don't need any of those, do we?
			    //
			    //if(likely(ltt_time_compare(hashed_process_data_child->next_good_time,evtime) <= 0))
	            //  {
	            //    TimeWindow time_window = lttvwindow_get_time_window(control_flow_data->tab);
			    //
	            //    #ifdef EXTRA_CHECK
	            //    if(ltt_time_compare(evtime, time_window.start_time) == -1 || ltt_time_compare(evtime, time_window.end_time) == 1)
	            //       return FALSE;
	            //    #endif //EXTRA_CHECK
	            //    
	            //    Drawing_t *drawing = control_flow_data->drawing;
	            //    guint width = drawing->width;
	            //    guint new_x;
	            //    convert_time_to_pixels(time_window,evtime,width,&new_x);
			    //
	            //    if(likely(hashed_process_data_child->x.over != new_x)) {
	            //      hashed_process_data_child->x.over = new_x;
	            //      hashed_process_data_child->x.over_used = FALSE;
	            //      hashed_process_data_child->x.over_marked = FALSE;
	            //    }
	            //    if(likely(hashed_process_data_child->x.middle != new_x)) {
	            //      hashed_process_data_child->x.middle = new_x;
	            //      hashed_process_data_child->x.middle_used = FALSE;
	            //      hashed_process_data_child->x.middle_marked = FALSE;
	            //    }
	            //    if(likely(hashed_process_data_child->x.under != new_x)) {
	            //      hashed_process_data_child->x.under = new_x;
	            //      hashed_process_data_child->x.under_used = FALSE;
	            //      hashed_process_data_child->x.under_marked = FALSE;
	            //    }
	              
				return false;
			}

			// @Override
			public Events getEventHandleType() {
				return eventType;
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
	final IEventProcessing getProcessExitHandler() {
	    AbsFlowTRangeUpdate handler = new AbsFlowTRangeUpdate() {

			private Events eventType = Events.LTT_EVENT_PROCESS_EXIT;

			// @Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {
			    
			    LttngProcessState process = traceSt.getRunning_process().get(trcEvent.getCpuId());

			    if ( process != null ) {
			        
					// *** TODO: ***
					// We shall look into a way to find the current process
					// (current_hash) in order to speed up the find. see c-code
			        //   if(likely(process_list->current_hash_data[trace_num][cpu] != NULL) ){
		            //        hashed_process_data = process_list->current_hash_data[trace_num][cpu];
		            //   }
			        TimeRangeEventProcess localProcess = procContainer.findProcess(process.getPid(), process.getCreation_time().getValue(), traceSt.getTraceId());
			        
			        if ( localProcess == null ) {
			            if ( (process.getPid() == 0) || (process.getPid() != process.getPpid()) ) {			                
			                TmfTimeRange timeRange = traceSt.getInputDataRef().getTraceTimeWindow();
		                    localProcess = addLocalProcess(process, timeRange.getStartTime().getValue(), timeRange.getEndTime().getValue(), traceSt.getTraceId());
			            }
			            else {
			                TraceDebug.debug("process pid is not 0 or pid equals ppid! (getProcessExitHandler)");
			            }
			        }
			        else {
			            // *** FIXME ***
                        // I feel like we are missing something here... what are we suppose to do with that?
                        //   process_list->current_hash_data[trace_num][process->cpu] = hashed_process_data;
			        }
                        
			    }
			    else {
			        TraceDebug.debug("process is null! (getProcessExitHandler)");
			    }			    
			    
			    // *** VERIFY ***
                // We don't need any of those, do we?
                //
                //if(likely(ltt_time_compare(hashed_process_data_child->next_good_time,evtime) <= 0))
                //  {
                //    TimeWindow time_window = lttvwindow_get_time_window(control_flow_data->tab);
                //
                //    #ifdef EXTRA_CHECK
                //    if(ltt_time_compare(evtime, time_window.start_time) == -1 || ltt_time_compare(evtime, time_window.end_time) == 1)
                //       return FALSE;
                //    #endif //EXTRA_CHECK
                //    
                //    Drawing_t *drawing = control_flow_data->drawing;
                //    guint width = drawing->width;
                //    guint new_x;
                //    convert_time_to_pixels(time_window,evtime,width,&new_x);
                //
                //    if(likely(hashed_process_data_child->x.over != new_x)) {
                //      hashed_process_data_child->x.over = new_x;
                //      hashed_process_data_child->x.over_used = FALSE;
                //      hashed_process_data_child->x.over_marked = FALSE;
                //    }
                //    if(likely(hashed_process_data_child->x.middle != new_x)) {
                //      hashed_process_data_child->x.middle = new_x;
                //      hashed_process_data_child->x.middle_used = FALSE;
                //      hashed_process_data_child->x.middle_marked = FALSE;
                //    }
                //    if(likely(hashed_process_data_child->x.under != new_x)) {
                //      hashed_process_data_child->x.under = new_x;
                //      hashed_process_data_child->x.under_used = FALSE;
                //      hashed_process_data_child->x.under_marked = FALSE;
                //    }
			    
				return false;
			}

			// @Override
			public Events getEventHandleType() {
				return eventType;
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
	final IEventProcessing getProcessExecHandler() {
	    AbsFlowTRangeUpdate handler = new AbsFlowTRangeUpdate() {

			private Events eventType = Events.LTT_EVENT_EXEC;

			// @Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {
			    
			    LttngProcessState process = traceSt.getRunning_process().get(trcEvent.getCpuId());

                if ( process != null ) {
                    
                    // *** VERIFY ***
                    // This make no sense in our java implementation and should be ignored, right?
                    //
                    //   if(likely(process_list->current_hash_data[trace_num][cpu] != NULL) ){
                    //        hashed_process_data = process_list->current_hash_data[trace_num][cpu];
                    //   }
                    TimeRangeEventProcess localProcess = procContainer.findProcess(process.getPid(), process.getCreation_time().getValue(), traceSt.getTraceId());
                    
                    if ( localProcess == null ) {
                        if ( (process.getPid() == 0) || (process.getPid() != process.getPpid()) ) {
                            // *** VERIFY ***
                            // What am I supposed to do with that?
                            //   Drawing_t *drawing = control_flow_data->drawing;
                            //   ProcessInfo *process_info;
                            //   gtk_widget_set_size_request(drawing->drawing_area, -1, pl_height);
                            //   gtk_widget_queue_draw(drawing->drawing_area);
                            TmfTimeRange timeRange = traceSt.getInputDataRef().getTraceTimeWindow();
                            localProcess = addLocalProcess(process, timeRange.getStartTime().getValue(), timeRange.getEndTime().getValue(), traceSt.getTraceId());
                        }
                        else {
                            TraceDebug.debug("process pid is not 0 or pid equals ppid! (getProcessExecHandler)");
                        }
                    }
                    else {
                        // If we found the process, the name might be missing. Let's add it here.
                        localProcess.setName(process.getName());
                        
                        // *** FIXME ***
                        // I feel like we are missing something here... what are we suppose to do with that?
                        //   process_list->current_hash_data[trace_num][process->cpu] = hashed_process_data;
                    }
                }
                else {
                    TraceDebug.debug("process is null! (getProcessExecHandler)");
                }
                
				return false;
			}

			// @Override
			public Events getEventHandleType() {
				return eventType;
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
	final IEventProcessing GetThreadBrandHandler() {
	    AbsFlowTRangeUpdate handler = new AbsFlowTRangeUpdate() {

			private Events eventType = Events.LTT_EVENT_THREAD_BRAND;

			// @Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {
			    
			    LttngProcessState process = traceSt.getRunning_process().get(trcEvent.getCpuId());

                if ( process != null ) {
                    
                    // *** VERIFY ***
                    // This make no sense in our java implementation and should be ignored, right?
                    //
                    //   if(likely(process_list->current_hash_data[trace_num][cpu] != NULL) ){
                    //        hashed_process_data = process_list->current_hash_data[trace_num][cpu];
                    //   }
                    TimeRangeEventProcess localProcess = procContainer.findProcess(process.getPid(), process.getCreation_time().getValue(), traceSt.getTraceId());
                    
                    if ( localProcess == null ) {
                        if ( (process.getPid() == 0) || (process.getPid() != process.getPpid()) ) {                         
                            // *** VERIFY ***
                            // What am I supposed to do with that?
                            //   Drawing_t *drawing = control_flow_data->drawing;
                            //   ProcessInfo *process_info;
                            //   gtk_widget_set_size_request(drawing->drawing_area, -1, pl_height);
                            //   gtk_widget_queue_draw(drawing->drawing_area);
                            TmfTimeRange timeRange = traceSt.getInputDataRef().getTraceTimeWindow();
                            localProcess = addLocalProcess(process, timeRange.getStartTime().getValue(), timeRange.getEndTime().getValue(), traceSt.getTraceId());
                        }
                        else {
                            TraceDebug.debug("process pid is not 0 or pid equals ppid! (GetThreadBrandHandler)");
                        }
                    }
                    else {
                        // If we foubd the process, the brand might be missing on it, add it.
                        localProcess.setBrand(process.getBrand());
                        
                        // *** FIXME ***
                        // I feel like we are missing something here... what are we suppose to do with that?
                        //   process_list->current_hash_data[trace_num][process->cpu] = hashed_process_data;
                    }
                }
                else {
                    TraceDebug.debug("process is null! (GetThreadBrandHandler)");
                }
                
                return false;
			    
			}

			// @Override
			public Events getEventHandleType() {
				return eventType;
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
	 * * <p>
     * Fields: LTT_FIELD_NEXT_PID
     * </p>
     * 
	 * @return
	 */
	final IEventProcessing getEnumProcessStateHandler() {
	    AbsFlowTRangeUpdate handler = new AbsFlowTRangeUpdate() {

			private Events eventType = Events.LTT_EVENT_PROCESS_STATE;

			// @Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {
			    
				int first_cpu;
				int nb_cpus;
			    
			    // *** VERIFY ***
			    // We want the pid_in... we assume the pid_in is the next pid, as we get on the CPU, right?
			    Long pid_in = getAFieldLong(trcEvent, traceSt, Fields.LTT_FIELD_NEXT_PID);
			    
                // Lttv assume that pid_in will NEVER be null or incoherent
                // What if ... ?    (let's add some debug)
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
    			        LttngProcessState process_in = lttv_state_find_process(traceSt, trcEvent.getCpuId(), pid_in );
    			        
    			        if ( process_in != null ) {
    			            TimeRangeEventProcess localProcess = procContainer.findProcess(process_in.getPid(), process_in.getCreation_time().getValue(), traceSt.getTraceId());
    	                    
    			            if (localProcess == null) {
        			            if ( (process_in.getPid() == 0) || (process_in.getPid() != process_in.getPpid()) ) {
                                    // *** VERIFY ***
                                    // What am I supposed to do with that?
            		                //    Drawing_t *drawing = control_flow_data->drawing;
            		                //    gtk_widget_set_size_request(drawing->drawing_area,-1,pl_height);
            		                //    gtk_widget_queue_draw(drawing->drawing_area);
                                    TmfTimeRange timeRange = traceSt.getInputDataRef().getTraceTimeWindow();
                                    localProcess = addLocalProcess(process_in, timeRange.getStartTime().getValue(), timeRange.getEndTime().getValue(), traceSt.getTraceId());
                                }
                                else {
                                    TraceDebug.debug("process pid is not 0 or pid equals ppid! (getEnumProcessStateHandler)");
                                }
    			            }
    			            else {
    			                // If the process was found, it might be missing informations, add it here
    			                localProcess.setName(process_in.getName());
    			                localProcess.setPpid(process_in.getPpid());
    			                localProcess.setTgid(process_in.getTgid());
    			            }
    			        }
    			        else {
    			            TraceDebug.debug("process_in is null! This should never happen. (getEnumProcessStateHandler)");
    			        }
    			    }
			    }
			    else {
			        TraceDebug.debug("pid_in is null! This should never happen, really... (getEnumProcessStateHandler)");
			    }
			    
				return false;
			}

			// @Override
			public Events getEventHandleType() {
				return eventType;
			}
		};
		return handler;
	}

}
