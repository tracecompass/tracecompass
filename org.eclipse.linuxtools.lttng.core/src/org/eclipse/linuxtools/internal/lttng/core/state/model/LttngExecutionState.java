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

import org.eclipse.linuxtools.internal.lttng.core.LttngConstants;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings;

/**
 * <b><u>LttngExecutionState</u></b>
 * <p>
 * 
 */
public class LttngExecutionState implements Cloneable {
	// ========================================================================
	// Data
	// =======================================================================

    private Long entry_LttTime = null;
	private Long change_LttTime = null;
	private Long cum_cpu_time_Timens = null;
	
	private StateStrings.ProcessStatus proc_status = StateStrings.ProcessStatus.LTTV_STATE_UNNAMED;
	private StateStrings.ExecutionMode exec_mode = StateStrings.ExecutionMode.LTTV_STATE_MODE_UNKNOWN;
	private String exec_submode = StateStrings.ExecutionSubMode.LTTV_STATE_SUBMODE_UNKNOWN.getInName();
    // Note: For statistics performance improvement a integer representation of the submode is used 
    // as well as a bit mask is applied! 
	private int exec_submode_id = Integer.valueOf(StateStrings.ExecutionSubMode.LTTV_STATE_SUBMODE_UNKNOWN.ordinal() | LttngConstants.STATS_NONE_ID);

    @Override
	public LttngExecutionState clone() {
	    LttngExecutionState newState = null;
        
        try {
            newState = (LttngExecutionState)super.clone();
            
            // *** IMPORTANT ***
            // Basic type in java are immutable! 
            // Thus, using assignation ("=") on basic type is CORRECT, 
            //  but we should ALWAYS use "new" or "clone()" on "non basic" type
            newState.cum_cpu_time_Timens = this.cum_cpu_time_Timens;
            newState.exec_submode = this.exec_submode;
            newState.exec_submode_id = this.exec_submode_id;
            
            // ProcessStatus and ExecutionMode are enum, and so shouldn't be a problem to use their reference
            newState.proc_status = this.proc_status;
            newState.exec_mode = this.exec_mode;
            newState.entry_LttTime = this.entry_LttTime;
            newState.change_LttTime = this.change_LttTime;
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
	 * @return the entry_LttTime
	 */
	public Long getEntry_LttTime() {
		return entry_LttTime;
	}

	/**
	 * @param entryLttTime
	 *            the entry_LttTime to set
	 */
	public void setEntry_Time(Long entryLttTime) {
		entry_LttTime = entryLttTime;
	}

	/**
	 * @return the change_LttTime
	 */
	public Long getChange_LttTime() {
		return change_LttTime;
	}

	/**
	 * @param changeLttTime
	 *            the change_LttTime to set
	 */
	public void setChange_Time(Long changeLttTime) {
		change_LttTime = changeLttTime;
	}

	/**
	 * @return the cum_cpu_time_LttTime
	 */
	public Long getCum_cpu_time() {
		return cum_cpu_time_Timens;
	}

	/**
	 * @param cumCpuTimeLttTime
	 *            the cum_cpu_time_LttTime to set
	 */
	public void setCum_cpu_time(Long cumCpuTime) {
		cum_cpu_time_Timens = cumCpuTime;
	}

	/**
	 * @return the proc_status
	 */
	public StateStrings.ProcessStatus getProc_status() {
		return proc_status;
	}

	/**
	 * @param procStatus
	 *            the proc_status to set
	 */
	public void setProc_status(StateStrings.ProcessStatus procStatus) {
		proc_status = procStatus;
	}

	/**
	 * @return the exec_mode
	 */
	public StateStrings.ExecutionMode getExec_mode() {
		return exec_mode;
	}

	/**
	 * @param execMode
	 *            the exec_mode to set
	 */
	public void setExec_mode(StateStrings.ExecutionMode execMode) {
		exec_mode = execMode;
	}

	/**
	 * @return the exec_submode
	 */
	public String getExec_submode() {
		return exec_submode;
	}

	/**
	 * @param execSubmode
	 *            the exec_submode to set
	 */
    public void setExec_submode(String execSubmode) {
        exec_submode = execSubmode;
    }
	
    /**
     * @return the exec_submode
     */
    public int getExec_submode_id() {
        return exec_submode_id;
    }

    /**
     * @param execSubmode
     *            the exec_submode id to set
     */
    public void setExec_submode_id(int execSubmodeId) {
        exec_submode_id = execSubmodeId;
    }

    
    @Override
    @SuppressWarnings("nls")
    public String toString() {
		return "[LttngExecutionState: " + "entry=" + entry_LttTime + ",change=" + change_LttTime + ",cum_cpu=" + cum_cpu_time_Timens +
		",pstatus=" + proc_status + ",emode=" + exec_mode + ",esubmode=" + exec_submode +"]";
    }
}
