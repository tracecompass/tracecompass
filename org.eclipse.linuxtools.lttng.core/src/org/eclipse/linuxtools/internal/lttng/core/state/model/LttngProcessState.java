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
package org.eclipse.linuxtools.internal.lttng.core.state.model;

import java.util.Stack;

import org.eclipse.linuxtools.internal.lttng.core.LttngConstants;
import org.eclipse.linuxtools.internal.lttng.core.TraceDebug;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.ExecutionMode;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.ExecutionSubMode;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.ProcessStatus;

/**
 * <b>LttngProcessState</b>
 * 
 * @author alvaro
 * 
 */
public class LttngProcessState implements Cloneable {
	// ========================================================================
	// Data
	// =======================================================================
	private Long cpu = null;
	private Long pid = null;
	private Long tgid = null;
	private String name = null;
	private Long creation_time = null;
	private String brand = null;
	private StateStrings.ProcessType type = null;
	private Long current_function = null;
	private Long ppid = null;
	private Long insertion_time = null;
	private String pid_time = null;
	private Long free_events = null;
	private LttngExecutionState state = null; // top of stack
	private Stack<LttngExecutionState> execution_stack = new Stack<LttngExecutionState>();
	private Stack<Long> user_stack = new Stack<Long>(); // user space
	
	private String userTrace = null; /* Associated file trace  */
	private Long target_pid = null; /* target PID of the current event. */
	private String trace_id = null;
	
	// ========================================================================
	// Constructor
	// =======================================================================
	public LttngProcessState(Long startTime, String traceId) {
		this.cpu = 0L;
		this.pid = 0L;
		this.tgid = 0L;
		this.name = StateStrings.ProcessStatus.LTTV_STATE_UNNAMED.getInName();
		this.insertion_time = startTime;
		this.trace_id = traceId;
		init();
	}

	public LttngProcessState(Long cpu, Long pid, Long tgid,
			String name, Long startTime, String traceId) {
		this.cpu = cpu;
		this.pid = pid;
		this.tgid = tgid;
		this.name = name;
		this.insertion_time = startTime;
		this.trace_id = traceId;
		init();
	}

	// ========================================================================
	// Methods
	// =======================================================================
	private void init() {
		this.brand = StateStrings.LTTV_STATE_UNBRANDED;
		this.type = StateStrings.ProcessType.LTTV_STATE_USER_THREAD;
		this.current_function = 0L;
		this.ppid = 0L;
		this.creation_time = 0L;
		this.free_events = 0L;

		// Initialise stack
		LttngExecutionState es = new LttngExecutionState();
		es.setExec_mode(ExecutionMode.LTTV_STATE_USER_MODE);
		es.setExec_submode(ExecutionSubMode.LTTV_STATE_SUBMODE_NONE.getInName());
        // Note: For statistics performance improvement a integer representation of the submode is used 
        // as well as a bit mask is applied! 
		es.setExec_submode_id(StateStrings.ExecutionSubMode.LTTV_STATE_SUBMODE_NONE.ordinal() | 
		        LttngConstants.STATS_NONE_ID);
		es.setEntry_Time(this.insertion_time);
		es.setChange_Time(this.insertion_time);
		es.setCum_cpu_time(0L);
		es.setProc_status(ProcessStatus.LTTV_STATE_RUN);
		this.execution_stack.push(es);

		//This second entry is needed when processes are created via a Fork event.
		es = new LttngExecutionState();
		es.setExec_mode(ExecutionMode.LTTV_STATE_SYSCALL);
		es.setExec_submode(ExecutionSubMode.LTTV_STATE_SUBMODE_NONE.getInName());
        // Note: For statistics performance improvement a integer representation of the submode is used 
        // as well as a bit mask is applied! 
		es.setExec_submode_id(StateStrings.ExecutionSubMode.LTTV_STATE_SUBMODE_NONE.ordinal() | 
		        LttngConstants.STATS_NONE_ID);
		es.setEntry_Time(this.insertion_time);
		es.setChange_Time(this.insertion_time);
		es.setCum_cpu_time(0L);
		es.setProc_status(ProcessStatus.LTTV_STATE_WAIT_FORK);
		this.execution_stack.push(es);
		
		// point state to the top of the stack
		this.state = es;
	}
	
	@Override
	@SuppressWarnings("unchecked")
    public LttngProcessState clone() {
	    LttngProcessState newState = null;
        
        try {
            newState = (LttngProcessState)super.clone();
            
            // *** IMPORTANT ***
            // Basic type in java are immutable! 
			// Thus, using assignment ("=") on basic type is CORRECT,
            //  but we should ALWAYS use "new" or "clone()" on "non basic" type
            newState.cpu = this.cpu;
            newState.pid = this.pid;
            newState.tgid = this.tgid;
            newState.name = this.name;
            newState.brand = this.brand;
            newState.type = this.type;
            newState.current_function = this.current_function;
            newState.ppid = this.ppid;
            newState.pid_time= this.pid_time;
            newState.free_events = this.free_events;
            newState.userTrace = this.userTrace;
            newState.target_pid = this.target_pid;
            newState.trace_id = this.trace_id;
            newState.creation_time = this.creation_time;
            newState.insertion_time = this.insertion_time;
            
            // Call clone on our own object is safe as Long it implements Clonable
            newState.state = (LttngExecutionState)this.state.clone();
            
            // Clone should work correctly for all stack object that contain basic java object (String, Long, etc...)
            newState.user_stack = (Stack<Long>)this.user_stack.clone();
            
            
            // This is worst case : Stack that contain user defined object. We have to unstack it and clone every object in a new stack!
            // Why does java does not call clone() for every object in the stack it clone? It would probably be too useful...
            newState.execution_stack = new Stack<LttngExecutionState>();
            
            // Work stack we will use to "pop" item
            Stack<LttngExecutionState> tmpStack = new Stack<LttngExecutionState>();
            
            // First, we pop every ExecutionState, and insert a CLONED copy into our new cloned stack
            while ( this.execution_stack.empty() == false ) {
                // Save a copy of the original reference
                tmpStack.push(this.execution_stack.peek());
                // Push a CLONED copy into the new stack while poping it from the original stack
                newState.execution_stack.push( this.execution_stack.pop().clone() );
            }
            
            // Second, we reinsert back our content into the original stack
            while ( tmpStack.empty() == false ) {
                // Pop the cloned copy and push it back into the original stack
                this.execution_stack.push( tmpStack.pop() );
            }
        }
        catch ( CloneNotSupportedException e ) {
            System.out.println("Cloning failed with : " + e.getMessage() ); //$NON-NLS-1$
        }
        
        return newState;
    }
	
	
	// ========================================================================
	// Methods
	// =======================================================================
	/**
	 * @return the pid
	 */
	public Long getPid() {
		return pid;
	}

	/**
	 * @param pid
	 *            the pid to set
	 */
	public void setPid(Long pid) {
		this.pid = pid;
	}

	/**
	 * @return the tgid
	 */
	public Long getTgid() {
		return tgid;
	}

	/**
	 * @param tgid
	 *            the tgid to set
	 */
	public void setTgid(Long tgid) {
		this.tgid = tgid;
	}

	/**
	 * @return the ppid
	 */
	public Long getPpid() {
		return ppid;
	}

	/**
	 * @param ppid
	 *            the ppid to set
	 */
	public void setPpid(Long ppid) {
		this.ppid = ppid;
	}

	/**
	 * <p>
	 * When the parent pid is known, the creation time is also known and
	 * requires update
	 * </p>
	 * 
	 * @param ppid
	 *            the ppid to set
	 */
	public void setPpid(Long ppid, Long creationTime) {
		if (ppid != null) {
			this.ppid = ppid;
		}
		
		if (creationTime != null) {
			setCreation_time(creationTime);
		}
	}

	/**
	 * @return the creation_time
	 */
	public Long getCreation_time() {
		return creation_time;
	}

	/**
	 * @param creationTime
	 *            the creation_time to set
	 */
	public void setCreation_time(Long creationTime) {
		if ( (creationTime != null) && (pid != null) ) {
			creation_time = creationTime;
			StringBuilder sb = new StringBuilder(this.pid.toString() + "-" //$NON-NLS-1$
					+ creationTime.toString());
			this.pid_time = sb.toString();
		}
	}

	/**
	 * @return the insertion_time
	 */
	public Long getInsertion_time() {
		return insertion_time;
	}

	/**
	 * @param insertionTime
	 *            the insertion_time to set
	 */
	public void setInsertion_time(Long insertionTime) {
		insertion_time = insertionTime;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the brand
	 */
	public String getBrand() {
		return brand;
	}

	/**
	 * @param brand
	 *            the brand to set
	 */
	public void setBrand(String brand) {
		this.brand = brand;
	}

	/**
	 * @return the prid_time
	 */
	public String getPid_time() {
		return pid_time;
	}

	/**
	 * @return the cpu
	 */
	public Long getCpu() {
		return cpu;
	}

	/**
	 * @param cpu
	 *            the cpu to set
	 */
	public void setCpu(Long cpu) {
		this.cpu = cpu;
	}

	/**
	 * @return the current_function
	 */
	public Long getCurrent_function() {
		return current_function;
	}

	/**
	 * @param currentFunction
	 *            the current_function to set
	 */
	public void setCurrent_function(Long currentFunction) {
		current_function = currentFunction;
	}

	/**
	 * @return the target_pid
	 */
	public Long getTarget_pid() {
		return target_pid;
	}

	/**
	 * @param targetPid
	 *            the target_pid to set
	 */
	public void setTarget_pid(Long targetPid) {
		target_pid = targetPid;
	}
	
	public String getTrace_id() {
		return trace_id;
	}

	public void setTrace_id(String traceId) {
		trace_id = traceId;
	}
	
	/**
	 * @return the free_events
	 */
	public Long getFree_events() {
		return free_events;
	}

	/**
	 * @param freeEvents
	 *            the free_events to set
	 */
	public void setFree_events(Long freeEvents) {
		free_events = freeEvents;
	}
	
	/**
	 * increment the nuber of free events
	 */
	public void incrementFree_events() {
		++free_events;
	}

	/**
	 * @return the state
	 */
	public LttngExecutionState getState() {
		return state;
	}

	/**
	 * @param state
	 *            the state to set
	 */
	public void setState(LttngExecutionState state) {
		this.state = state;
	}

	/**
	 * @return the type
	 */
	public StateStrings.ProcessType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(StateStrings.ProcessType type) {
		this.type = type;
	}

	/**
	 * @return the userTrace
	 */
	public String getUserTrace() {
		return userTrace;
	}

	/**
	 * @param userTrace
	 *            the userTrace to set
	 */
	public void setUserTrace(String userTrace) {
		this.userTrace = userTrace;
	}
	
	
	public void clearUserStack() {
	    user_stack.clear();
	}
	
	public void pushToUserStack(Long newState) {
	    user_stack.push(newState);
	}
	
	public Long popFromUserStack() {
       if (user_stack.size() <= 1) {
            TraceDebug.debug("Removing last item from user stack is not allowed! (popFromUserStack)"); //$NON-NLS-1$
            return null;
        }
        else {
           return user_stack.pop();
       }
    }
	
	public Long peekFromUserStack() {
        return user_stack.peek();
    }
	
	
	
	public void clearExecutionStack() {
        execution_stack.clear();
    }
    
    public void pushToExecutionStack(LttngExecutionState newState) {
        execution_stack.push(newState);
		setState(newState);
    }
    
    public LttngExecutionState popFromExecutionStack() {
       if (execution_stack.size() <= 1) {
    	   TraceDebug.debug("Removing last item from execution stack is not allowed! (popFromExecutionStack)"); //$NON-NLS-1$
            return null;
        }
        else {
			LttngExecutionState popedState = execution_stack.pop();
			// adjust current state to the new top
			setState(peekFromExecutionStack());
			return popedState;
       }
    }
    
    public LttngExecutionState peekFromExecutionStack() {
        return execution_stack.peek();
    }
	
    public LttngExecutionState getFirstElementFromExecutionStack() {
        return execution_stack.firstElement();
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
    	String stateSt  = state.toString();
    	String eStackSt = execution_stack.toString();
    	
		return "[LttngProcessState: " + "cpu=" + cpu + ",pid=" + pid + ",tgid=" + tgid + ",name=" + name + ",ctime=" + creation_time +
		",brand=" + brand + ",type=" + type + ",cfunc=" + current_function + ",ppid=" + ppid + ",itime=" + insertion_time + ",ptime=" + pid_time +
		",fevents=" + free_events + ",state=" + stateSt + ",estack=" + eStackSt + ",ustack=" + user_stack + ",utrace=" + userTrace +
		",tpid=" + target_pid + ",trace=" + trace_id + "]";
    }
}
