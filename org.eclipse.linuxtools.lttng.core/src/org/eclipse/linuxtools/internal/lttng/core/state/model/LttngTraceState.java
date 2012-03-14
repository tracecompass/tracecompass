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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.linuxtools.internal.lttng.core.LttngConstants;
import org.eclipse.linuxtools.internal.lttng.core.TraceDebug;
import org.eclipse.linuxtools.internal.lttng.core.state.LttngStateException;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.ExecutionMode;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.ExecutionSubMode;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.IRQMode;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.ProcessStatus;
import org.eclipse.linuxtools.internal.lttng.core.state.resource.ILttngStateContext;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;

/**
 * <b><u>LttngTraceState</u></b>
 * <p>
 * 
 */
/**
 * @author alvaro
 * 
 */
public class LttngTraceState implements Cloneable {
	// ========================================================================
	// Data
	// =======================================================================
    
	private Long save_interval = null;

	private Long max_time_state_recomputed_in_seek = null;
	private boolean has_precomputed_states = false;

	private HashMap<ProcessStateKey, LttngProcessState> processes = new HashMap<ProcessStateKey, LttngProcessState>();

	// by cpu
	private Map<Long, LttngProcessState> running_process = new HashMap<Long, LttngProcessState>();

	// Get state tables
	private Map<Long, LTTngCPUState> cpu_states = new HashMap<Long, LTTngCPUState>();
	private Map<Long, LttngIRQState> irq_states = new HashMap<Long, LttngIRQState>();
	private Map<Long, LttngSoftIRQState> soft_irq_states = new HashMap<Long, LttngSoftIRQState>();
	private Map<Long, LttngTrapState> trap_states = new HashMap<Long, LttngTrapState>();
	private Map<Long, LttngBdevState> bdev_states = new HashMap<Long, LttngBdevState>();

	// Get name tables
	private Map<Long, String> syscall_names = new HashMap<Long, String>();
	private Map<Long, String> kprobe_table = new HashMap<Long, String>();
	private Map<Long, String> soft_irq_names = new HashMap<Long, String>();
	private Map<Long, String> trap_names = new HashMap<Long, String>();
	private Map<Long, String> irq_names = new HashMap<Long, String>();

	private int nb_events = 0;

	// reference to input data provider
	ILttngStateContext fContext = null;
	String traceId = ""; //$NON-NLS-1$

	// ========================================================================
	// Constructor
	// =======================================================================
	LttngTraceState() {
		// Get name tables
		StateStrings strings = StateStrings.getInstance();

		// initialize sycall_names
		String[] ref_name_table = strings.getSyscallNames();
		for (Long i = 0L; i < ref_name_table.length; i++) {
			syscall_names.put(i, ref_name_table[i.intValue()]);
		}

		// trap names
		ref_name_table = strings.getTrapNames();
		for (Long i = 0L; i < ref_name_table.length; i++) {
			trap_names.put(i, ref_name_table[i.intValue()]);
		}

		// irq names
		ref_name_table = strings.getIrqNames();
		for (Long i = 0L; i < ref_name_table.length; i++) {
			irq_names.put(i, ref_name_table[i.intValue()]);
		}

		// softirq names
		ref_name_table = strings.getSoftIrqNames();
		for (Long i = 0L; i < ref_name_table.length; i++) {
			soft_irq_names.put(i, ref_name_table[i.intValue()]);
		}
	}

	// =======================================================================
	// Methods
	// =======================================================================
	@Override
	public LttngTraceState clone() {
		LttngTraceState newState = null;

		try {
			newState = (LttngTraceState) super.clone();

			// *** IMPORTANT ***
			// Basic type in java are immutable!
			// Thus, using assignment ("=") on basic type is CORRECT,
			// but we should ALWAYS use "new" or "clone()" on "non basic" type
			newState.save_interval = this.save_interval;
			newState.traceId = this.traceId;

			// Basic value only need to be assigned while cloning
			newState.has_precomputed_states = this.has_precomputed_states;
			newState.nb_events = this.nb_events;
			newState.max_time_state_recomputed_in_seek = this.max_time_state_recomputed_in_seek;

			// Clone should work correctly for all stack object that contain
			// basic java object (String, Long, etc...)
			newState.syscall_names = this.syscall_names;
			newState.kprobe_table = this.kprobe_table;
			newState.soft_irq_names = this.soft_irq_names;
			newState.trap_names = this.trap_names;
			newState.irq_names = this.irq_names;

			// This reference should never need to be updated, should it?
			newState.fContext = this.fContext;

			// *** We need loop on each ArrayList and HashMap, as java implement
			// nothing that's remotely near deep copying.
			// *** TODO ***
			// In the future, implement something better here... serialization
			// perhaps? Or copy the array chunk of memory in C?
			
			Iterator<Long> iteratorL = null;
			Iterator<ProcessStateKey> iteratorP = null;
			Long mapKey = null;
			ProcessStateKey processKey = null;
			
			newState.processes = new HashMap<ProcessStateKey, LttngProcessState>();
			iteratorP = this.processes.keySet().iterator();
			while (iteratorP.hasNext()) {
				processKey = iteratorP.next();
				newState.processes.put(processKey, this.processes.get(processKey).clone());
			}

			newState.running_process = new HashMap<Long, LttngProcessState>();
			iteratorL = this.running_process.keySet().iterator();
			while (iteratorL.hasNext()) {
				mapKey = iteratorL.next();
				newState.running_process.put(mapKey, this.running_process.get(mapKey).clone());
			}

			newState.cpu_states = new HashMap<Long, LTTngCPUState>();
			iteratorL = this.cpu_states.keySet().iterator();
			while (iteratorL.hasNext()) {
				mapKey = iteratorL.next();
				newState.cpu_states.put(mapKey, this.cpu_states.get(mapKey)
						.clone());
			}

			newState.irq_states = new HashMap<Long, LttngIRQState>();
			iteratorL = this.irq_states.keySet().iterator();
			while (iteratorL.hasNext()) {
				mapKey = iteratorL.next();
				newState.irq_states.put(mapKey, this.irq_states.get(mapKey)
						.clone());
			}

			newState.soft_irq_states = new HashMap<Long, LttngSoftIRQState>();
			iteratorL = this.soft_irq_states.keySet().iterator();
			while (iteratorL.hasNext()) {
				mapKey = iteratorL.next();
				newState.soft_irq_states.put(mapKey, this.soft_irq_states.get(
						mapKey).clone());
			}

			newState.trap_states = new HashMap<Long, LttngTrapState>();
			iteratorL = this.trap_states.keySet().iterator();
			while (iteratorL.hasNext()) {
				mapKey = iteratorL.next();
				newState.trap_states.put(mapKey, this.trap_states.get(mapKey)
						.clone());
			}

			newState.bdev_states = new HashMap<Long, LttngBdevState>();
			iteratorL = this.bdev_states.keySet().iterator();
			while (iteratorL.hasNext()) {
				mapKey = iteratorL.next();
				newState.bdev_states.put(mapKey, this.bdev_states.get(mapKey)
						.clone());
			}

		} catch (CloneNotSupportedException e) {
			System.out.println("Cloning failed with : " + e.getMessage()); //$NON-NLS-1$
		}

		return newState;
	}

	public void init(ILttngStateContext context)
			throws LttngStateException {
		if (context == null) {
			StringBuilder sb = new StringBuilder(
					"The input provider reference must not be null"); //$NON-NLS-1$
			throw new LttngStateException(sb.toString());
		}

		// Save the input data reference
		fContext = context;

		// Save traceid
		traceId = fContext.getTraceId();

		// max time
		max_time_state_recomputed_in_seek = 0L;

		// reset cpu_states
		cpu_states.clear();
		
		// Obtain the total num of available CPUs and initialize the map
		// to the corresponding size
		int numCpus = fContext.getNumberOfCpus();
		for (Long i = 0L; i < numCpus; i++) {
			cpu_states.put(i, new LTTngCPUState());
		}

		// irq states
		irq_states.clear();
		for (Long i = 0L; i < irq_names.size(); i++) {
			irq_states.put(i, new LttngIRQState());
		}

		// soft irqs
		soft_irq_states.clear();
		for (Long i = 0L; i < soft_irq_names.size(); i++) {
			soft_irq_states.put(i, new LttngSoftIRQState());
		}

		// traps
		trap_states.clear();
		for (Long i = 0L; i < trap_names.size(); i++) {
			trap_states.put(i, new LttngTrapState(0L));
		}

		// bdev states
		bdev_states.clear();
		
		processes.clear();

		nb_events = 0;
		TmfTimeRange timeWin = fContext.getTraceTimeWindow();

		/* Put the per cpu running_process to beginning state : process 0. */
		for (Long i = 0L; i < numCpus; i++) {
			LttngProcessState process = new LttngProcessState(timeWin.getStartTime().getValue(), traceId );

			/*
			 * We are not sure is it's a kernel thread or normal thread, put the
			 * bottom stack state to unknown
			 */
			LttngExecutionState es = process.getFirstElementFromExecutionStack();
			process.setState(es);
			es.setExec_mode(ExecutionMode.LTTV_STATE_MODE_UNKNOWN);
			es.setExec_submode(ExecutionSubMode.LTTV_STATE_SUBMODE_NONE.getInName());
            // Note: For statistics performance improvement a integer representation of the submode is used 
            // as well as a bit mask is applied! 
			es.setExec_submode_id(StateStrings.ExecutionSubMode.LTTV_STATE_SUBMODE_NONE.ordinal() | LttngConstants.STATS_NONE_ID);
			es.setProc_status(ProcessStatus.LTTV_STATE_UNNAMED);

			// Reduce from default to only one execution state in the stack
			process.popFromExecutionStack();

			process.setCpu(i);
			// no associated user trace yet
			process.setUserTrace(""); //$NON-NLS-1$
			// processes.put(i, process);
			running_process.put(i, process);
			// reset cpu states
			LTTngCPUState cpuState = cpu_states.get(i);
			cpuState.reset();
			// Add the new process to the list
			processes.put(new ProcessStateKey(process), process);
		}

		// reset irq_states
		for (Long key : irq_states.keySet()) {
			LttngIRQState irqState = irq_states.get(key);
			irqState.clearAndSetBaseToIrqStack(IRQMode.LTTV_IRQ_UNKNOWN);
		}

		// reset soft_irq_states
		for (Long key : soft_irq_states.keySet()) {
			LttngSoftIRQState softIrqState = soft_irq_states.get(key);
			softIrqState.reset();
		}

		// reset trap_states
		for (Long key : trap_states.keySet()) {
			LttngTrapState trapState = trap_states.get(key);
			trapState.setRunning(0L);
		}

		// reset bdev_states
		for (Long key : bdev_states.keySet()) {
			LttngBdevState bdevState = bdev_states.get(key);
			bdevState.clearBdevStack();
		}

	}

	public Long getSave_interval() {
		return save_interval;
	}

	public void setSave_interval(Long saveInterval) {
		save_interval = saveInterval;
	}

	/**
	 * @return total number of CPUs registered as read from the Trace
	 */
	public int getNumberOfCPUs() {
		return fContext.getNumberOfCpus();
	}

	/**
	 * Provide access to input data not necessarily at Trace level
	 * 
	 * @return
	 */
	public ILttngStateContext getContext() {
		return fContext;
	}

	public Long getMax_time_state_recomputed_in_seek() {
		return max_time_state_recomputed_in_seek;
	}

	public void setMax_time_state_recomputed_in_seek(
			Long maxTimeStateRecomputedInSeek) {
		max_time_state_recomputed_in_seek = maxTimeStateRecomputedInSeek;
	}

	public boolean isHas_precomputed_states() {
		return has_precomputed_states;
	}

	public void setHas_precomputed_states(boolean hasPrecomputedStates) {
		has_precomputed_states = hasPrecomputedStates;
	}

	public Map<Long, LttngProcessState> getRunning_process() {
		return running_process;
	}

	public Map<Long, String> getSyscall_names() {
		return syscall_names;
	}

	public Map<Long, String> getTrap_names() {
		return trap_names;
	}

	public Map<Long, String> getIrq_names() {
		return irq_names;
	}

	public Map<Long, String> getSoft_irq_names() {
		return soft_irq_names;
	}

	public Map<Long, LTTngCPUState> getCpu_states() {
		return cpu_states;
	}

	public Map<Long, LttngIRQState> getIrq_states() {
		return irq_states;
	}

	public Map<Long, LttngSoftIRQState> getSoft_irq_states() {
		return soft_irq_states;
	}

	public Map<Long, LttngTrapState> getTrap_states() {
		return trap_states;
	}

	public Map<Long, LttngBdevState> getBdev_states() {
		return bdev_states;
	}

	public Map<Long, String> getKprobe_table() {
		return kprobe_table;
	}
	
	/**
	 * @return the traceId
	 */
	public String getTraceId() {
		return traceId;
	}
	
	/**
	 *  Return an array of Processes
	 * 
	 * @return LttngProcessState
	 */
	public LttngProcessState[] getProcesses() {
		return processes.values().toArray(new LttngProcessState[processes.size()]);
	}
	
	/**
	 * Clear all process state items e.g. when a new experiment is selected
	 */
	public void clearProcessState() {
		processes.clear();
	}
	
	/**
	 * Interface to add process state.
	 * 
	 * @param newProcessState
	 */
	public void addProcessState(LttngProcessState newProcessState) {
		if (newProcessState != null) {
			processes.put( new ProcessStateKey(newProcessState), newProcessState);
		}
	}
	
	/**
	 * Interface to remove process state.
	 * 
	 * @param oldProcessState
	 */
	public void removeProcessState(LttngProcessState oldProcessState) {
		if (oldProcessState != null) {
			processes.remove(new ProcessStateKey(oldProcessState));
		}
	}
	
	/**
     * Search by keys (pid, cpuId and traceId)<p>
     * 
     * A match is returned if the three arguments received match an entry
     *  Otherwise null is returned
     *  
     * @param searchedPid       The processId (Pid) we are looking for
     * @param searchedCpuId     The cpu Id we are looking for
     * @param searchedTraceID   The traceId (trace name?) we are looking for
     * 
     * @return LttngProcessState
     */
    public LttngProcessState findProcessState(Long searchedPid, Long searchedCpuId, String searchedTraceID) {
        // Get the TimeRangeEventProcess associated to a key we create here
    	LttngProcessState foundProcess = processes.get( new ProcessStateKey(searchedPid, searchedCpuId, searchedTraceID) );
        
        return foundProcess;
    }
}

final class ProcessStateKey {

    private long pid;
    private long cpuId;
    private String traceId;
    
    
    @SuppressWarnings("unused")
    private ProcessStateKey() { }
    
    public ProcessStateKey(LttngProcessState newRef) {
        pid = newRef.getPid().longValue();
        cpuId = newRef.getCpu().longValue();
        traceId = newRef.getTrace_id();
    }
    
    public ProcessStateKey(Long newPid, Long newCpuId, String newTraceId) {
        pid = newPid;
        cpuId = newCpuId;
        traceId = newTraceId;
    }
    
    @Override
    public boolean equals(Object obj) {

    	if (obj == null) return false;
		boolean isSame = true;
		
        if ( obj instanceof ProcessStateKey ) {
        	ProcessStateKey procKey = (ProcessStateKey) obj;
        	
        	if (procKey.pid != this.pid) {
        	    return false;
        	}

        	if (!(procKey.traceId.equals(this.traceId))) {
        	    return false;
        	}

        	if (((procKey.pid == 0L) && (procKey.cpuId != this.cpuId))) {
        	    return false;
        	}

		}
        else {
			TraceDebug
					.debug("ERROR : The received Key is not of the type ProcessStateKey! but " //$NON-NLS-1$
							+ obj.getClass().toString());
			return false;
        }
        
        return isSame;
    }
    
    @Override
    public int hashCode() {
        return (1 ^ (int)(pid ^ (pid >>> 32)) ^ traceId.hashCode());
    }
    
    
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return (String.valueOf(pid) + ":" + traceId);
    }
}
