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
package org.eclipse.linuxtools.lttng.state.model;

import java.util.Stack;

import org.eclipse.linuxtools.lttng.TraceDebug;
import org.eclipse.linuxtools.lttng.state.StateStrings;
import org.eclipse.linuxtools.lttng.state.StateStrings.ExecutionMode;
import org.eclipse.linuxtools.lttng.state.StateStrings.ExecutionSubMode;
import org.eclipse.linuxtools.lttng.state.StateStrings.ProcessStatus;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

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
	private TmfTimestamp creation_time = null;
	private String brand = null;
	private StateStrings.ProcessType type = null;
	private Long current_function = null;
	private Long ppid = null;
	private TmfTimestamp insertion_time = null;
	private String pid_time = null;
	private Long free_events = null;
	private LttngExecutionState state = null; // top of stack
	private Stack<LttngExecutionState> execution_stack = new Stack<LttngExecutionState>();
	private Stack<Long> user_stack = new Stack<Long>(); // user space
	
	private String userTrace = null; /* Associated file trace  */
	private Long target_pid = null; /* target PID of the current event. */

	// ========================================================================
	// Constructor
	// =======================================================================
	public LttngProcessState(TmfTimestamp startTime) {
		this.cpu = 0L;
		this.pid = 0L;
		this.tgid = 0L;
		this.name = StateStrings.ProcessStatus.LTTV_STATE_UNNAMED.getInName();
		this.insertion_time = startTime;
		init();
	}

	public LttngProcessState(Long cpu, Long pid, Long tgid,
			String name, TmfTimestamp startTime) {
		this.cpu = cpu;
		this.pid = pid;
		this.tgid = tgid;
		this.name = name;
		this.insertion_time = startTime;
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
		// creation time defined when parent pid is known
		// calling the setCreation_time method adjust the pid_time string
		setCreation_time(new TmfTimestamp());
		this.free_events = 0L;

		// Initialise stack
		LttngExecutionState es = new LttngExecutionState();
		es.setExec_mode(ExecutionMode.LTTV_STATE_USER_MODE);
		es.setExec_submode(ExecutionSubMode.LTTV_STATE_SUBMODE_NONE.getInName());
		es.setEntry_Time(this.insertion_time);
		es.setChange_Time(this.insertion_time);
		es.setCum_cpu_time(0L);
		es.setProc_status(ProcessStatus.LTTV_STATE_RUN);
		this.execution_stack.push(es);

		//This second entry is needed when processes are created via a Fork event.
		es = new LttngExecutionState();
		es.setExec_mode(ExecutionMode.LTTV_STATE_SYSCALL);
		es
				.setExec_submode(ExecutionSubMode.LTTV_STATE_SUBMODE_NONE
						.getInName());
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
            
			// No clonable implemented in TMF, we will use copy constructor
            // NOTE : we GOT to check for null to avoid crashing on null pointer here!
            if ( this.creation_time != null ) {
                newState.creation_time = new TmfTimestamp(this.creation_time);
            }
            
            if ( this.creation_time != null ) {
                newState.insertion_time = new TmfTimestamp(this.insertion_time);
            }
            
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
            System.out.println("Cloning failed with : " + e.getMessage() );
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
	public void setPpid(Long ppid, TmfTimestamp creationTime) {
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
	public TmfTimestamp getCreation_time() {
		return creation_time;
	}

	/**
	 * @param creationTime
	 *            the creation_time to set
	 */
	public void setCreation_time(TmfTimestamp creationTime) {
		if ( (creationTime != null) && (pid != null) ) {
			creation_time = creationTime;
			StringBuilder sb = new StringBuilder(this.pid.toString() + "-"
					+ creationTime.toString());
			this.pid_time = sb.toString();
		}
	}

	/**
	 * @return the insertion_time
	 */
	public TmfTimestamp getInsertion_time() {
		return insertion_time;
	}

	/**
	 * @param insertionTime
	 *            the insertion_time to set
	 */
	public void setInsertion_time(TmfTimestamp insertionTime) {
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
            TraceDebug.debug("Removing last item from user stack is not allowed! (popFromUserStack)");
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
            TraceDebug.debug("Removing last item from execution stack is not allowed! (popFromExecutionStack)");
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
	
	/* 
     *  MAIN : For testing only!
     */
     public static void main(String[] args) {
         
         // !!! TESTING CLONE HERE !!!
         
         // *** New object with some args set to "123"
         LttngProcessState joie = new LttngProcessState(new TmfTimestamp(123L, (byte) -9));
         
         // Stack not empty by default??
         System.out.println("Emptying stack... Trashing empty instance of : " + joie.popFromExecutionStack() );
         
         joie.setCpu(123L);
         joie.setName("123");
         
         LttngExecutionState testEx1 = new LttngExecutionState();
         testEx1.setCum_cpu_time(123L);
         testEx1.setChange_Time(new TmfTimestamp(123L, (byte) -9));
         testEx1.setEntry_Time(new TmfTimestamp(123L, (byte) -9));
         
         // Print testEx1 reference
         System.out.println("testEx1 reference : " + testEx1);
         
         joie.pushToExecutionStack(testEx1);
         joie.pushToUserStack(123L);
         
         
         
         // *** New object cloned from the first one
         LttngProcessState joie2 = (LttngProcessState)joie.clone();
         
         
         // *** Modification of the FIRST object : Everything to "456"
         joie.setCpu(456L);
         joie.setName("456");
         testEx1.setCum_cpu_time(456L);
         testEx1.setChange_Time(new TmfTimestamp(456L, (byte) -9));
         testEx1.setEntry_Time(new TmfTimestamp(456L, (byte) -9));
         
         // Push new object on stack of the FIRST object
         LttngExecutionState testEx2 = new LttngExecutionState();
         testEx2.setCum_cpu_time(456L);
         joie.pushToExecutionStack(testEx2);
         joie.pushToUserStack(456L);
         
         
         // *** TEST : Everything should be "123L" stil
         System.out.println("123 == " + joie2.getCpu() );
         System.out.println("123 == " + joie2.getName() );
         
         LttngExecutionState newtestEx1 = joie2.popFromExecutionStack();
         // Print newtestEx1 reference
         System.out.println("testEx1 reference : " + newtestEx1);
         
         System.out.println("123 == " + newtestEx1.getCum_cpu_time() );
         System.out.println("123 == " + joie2.popFromUserStack() );
         
         // *** LAST TEST : The joie2 stack should be empty, only joie1 stack contains more than 1 object  
         try {
             System.out.println("123 == " + joie2.popFromExecutionStack().getCum_cpu_time() );
         }
         catch ( Exception e) {
             System.out.println("All fine");
         }
     }
     
}
