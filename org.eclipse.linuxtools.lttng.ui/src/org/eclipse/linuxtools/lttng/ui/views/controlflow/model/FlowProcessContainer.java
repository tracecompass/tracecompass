/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon - Initial API and implementation
 * 	 Michel Dagenais (michel.dagenais@polymtl.ca) - Reference C implementation, used with permission
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.controlflow.model;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.linuxtools.lttng.core.TraceDebug;
import org.eclipse.linuxtools.lttng.ui.model.trange.ItemContainer;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeEventProcess;

/**
 * Contains the processes in use by the Control flow view
 * 
 * @author alvaro
 * 
 */
public class FlowProcessContainer implements ItemContainer<TimeRangeEventProcess> {
	// ========================================================================
	// Data
	// ========================================================================
	private final HashMap<ProcessKey, TimeRangeEventProcess> allProcesses = new HashMap<ProcessKey, TimeRangeEventProcess>();
	private static Integer uniqueId = 0;
	
	// ========================================================================
	// Constructor
	// ========================================================================

	/**
	 * Package level constructor
	 */
	FlowProcessContainer() {

	}

	// ========================================================================
	// Methods
	// ========================================================================
	/**
	 * Interface to add a new process.<p>
	 * 
	 * Note : Process with the same key will be overwritten, it's calling function job to make sure the new process is unique.
	 * 
	 * @param newProcess   The process to add
	 */
	@Override
	public void addItem(TimeRangeEventProcess newItem) {
		if (newItem != null) {
			allProcesses.put(new ProcessKey(newItem), newItem);
		}
	}
	
	/**
     * Request a unique ID
     * 
     * @return Integer
     */
    @Override
	public Integer getUniqueId() {
        return uniqueId++;
    }
    
    /**
     * This method is intended for read only purposes in order to keep the
     * internal data structure in synch
     * 
     * @return TimeRangeEventProcess[]
     */
	@Override
	public TimeRangeEventProcess[] readItems() {
		
	    // This allow us to return an Array of the correct type of the exact correct dimension, without looping
		return allProcesses.values().toArray(new TimeRangeEventProcess[allProcesses.size()]);
	}
	
	/**
	 * Clear the children information for processes e.g. just before refreshing
	 * data with a new time range
	 */
	@Override
	public void clearChildren() {
	    TimeRangeEventProcess process = null;
        Iterator<ProcessKey> iterator = allProcesses.keySet().iterator();
        
        while (iterator.hasNext()) {
            process = allProcesses.get(iterator.next());
			process.reset();
        }
	}
	
	/**
     * Clear all process items
     */
    @Override
	public void clearItems() {
        allProcesses.clear();
    }
	
    /**
     * Remove the process related to a specific trace e.g. during trace
     * removal
     * 
     * @param traceId   The trace unique id (trace name?) on which we want to remove process
     */
	@Override
	public void removeItems(String traceId) {
	    ProcessKey iterKey = null;

        Iterator<ProcessKey> iterator = allProcesses.keySet().iterator();
        while (iterator.hasNext()) {
            iterKey = iterator.next();
            
            if (allProcesses.get(iterKey).getTraceID().equals(traceId)) {
                allProcesses.remove(iterKey);
            }
        }
	}
	
    /**
     * Search by keys (pid, cpuId, traceId and creationTime)<p>
     * 
     * A match is returned if the four arguments received match an entry
     *  Otherwise null is returned
     *  
     * @param searchedPid       The processId (Pid) we are looking for
     * @param searchedCpuId     The cpu Id we are looking for
     * @param searchedTraceID   The traceId (trace name?) we are looking for
     * @param searchedCreationtime The creation time we are looking for
     * 
     * @return TimeRangeEventProcess
     */
    public TimeRangeEventProcess findProcess(Long searchedPid, Long searchedCpuId, String searchedTraceID, Long searchedCreationtime) {
    	// Get the TimeRangeEventProcess associated to a key we create here
        TimeRangeEventProcess foundProcess = allProcesses.get( new ProcessKey(searchedPid, searchedCpuId, searchedTraceID, searchedCreationtime) );
    	 
        return foundProcess;
    }
}


class ProcessKey {
    private TimeRangeEventProcess valueRef = null;
    
    private Long    pid = null;
    private Long    cpuId = null;
    private String  traceId = null;
    private Long    creationtime = null;
    
    @SuppressWarnings("unused")
    private ProcessKey() { }
    
    public ProcessKey(TimeRangeEventProcess newRef) {
        valueRef = newRef;
    }
    
    public ProcessKey(Long newPid, Long newCpuId, String newTraceId, Long newCreationTime) {
        pid = newPid;
        cpuId = newCpuId;
        traceId = newTraceId;
        creationtime = newCreationTime;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null)
            return false;

        boolean isSame = false;
        
        if ( obj instanceof ProcessKey ) {
        	ProcessKey procKey = (ProcessKey) obj;
        	
			if (valueRef != null) {
				if ((procKey.getPid().equals(valueRef.getPid()))
						&& (procKey.getTraceId().equals(valueRef.getTraceID()))
						&& (procKey.getCreationtime().equals(valueRef.getCreationTime()))) {
					// use the cpu value to validate pid 0
					if (valueRef.getPid().longValue() == 0L && !procKey.getCpuId().equals(valueRef.getCpu())) {
						isSame = false;
					} else {
						isSame = true;
					}
				}
			} else {
				if ((procKey.getPid().equals(this.pid)) && (procKey.getTraceId().equals(this.traceId))
						&& (procKey.getCreationtime().equals(this.creationtime))) {
					// use the cpu value to validate pid 0
					if (this.pid.longValue() == 0L && !procKey.getCpuId().equals(this.cpuId)) {
						isSame = false;
					} else {
						isSame = true;
					}
				}
			}
        }
        else {
        	TraceDebug.debug("ERROR : The given key is not of the type ProcessKey!" + obj.getClass().toString()); //$NON-NLS-1$
        }
        
        return isSame;
    }
    
    // *** WARNING : Everything in there work because the check "valueRef != null" is the same for ALL getter
    // Do NOT change this check without checking.
    public Long getPid() {
    	if ( valueRef != null ) {
            return valueRef.getPid();
        }
        else {
            return pid;
        }
    }

    public Long getCpuId() {
        if ( valueRef != null ) {
            return valueRef.getCpu();
        }
        else {
            return cpuId;
        }
    }
    
    public String getTraceId() {
        if ( valueRef != null ) {
            return valueRef.getTraceID();
        }
        else {
            return traceId;
        }
    }
    
    public Long getCreationtime() {
        if ( valueRef != null ) {
            return valueRef.getCreationTime();
        }
        else {
            return creationtime;
        }
    }
    
    @Override
    public int hashCode() {
    	return this.toString().hashCode();
    }
    
    
	@Override
    @SuppressWarnings("nls")
    public String toString() {
        if ( valueRef != null ) {
			// return (valueRef.getPid().toString() + ":" +
			// valueRef.getCpu().toString() + ":"
			// + valueRef.getTraceID().toString() + ":" +
			// valueRef.getCreationTime().toString());
			return (valueRef.getPid().toString() + ":" + valueRef.getTraceID().toString() + ":" + valueRef
					.getCreationTime().toString());
        } 
        
		// return (pid.toString() + ":" + cpuId.toString() + ":" +
		// traceId.toString() + ":" + creationtime.toString());

		return (pid.toString() + ":" + traceId.toString() + ":" + creationtime.toString());
    }
}
