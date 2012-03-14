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

/**
 * <b>LttvSoftIRQState</b>
 * 
 * @author alvaro
 * 
 */
public class LttngSoftIRQState implements Cloneable {

	// ========================================================================
	// Data
	// =======================================================================
	private Long pending = null;
	private Long running = null;

	// ========================================================================
	// Constructor
	// =======================================================================
	public LttngSoftIRQState() {
		pending = 0L;
		running = 0L;
	}
	
    @Override
	public LttngSoftIRQState clone() {
        LttngSoftIRQState newState = null;
        
        try {
            newState = (LttngSoftIRQState)super.clone();
            
            // *** IMPORTANT ***
            // Basic type in java are immutable! 
			// Thus, using assignment ("=") on basic type is CORRECT,
            //  but we should ALWAYS use "new" or "clone()" on "non basic" type
            newState.pending = this.pending;
            newState.running = this.running;
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
	 * @param pending
	 *            the pending to set
	 */
	public void setPending(Long pending) {
		this.pending = pending;
	}

	/**
	 * @return the pending
	 */
	public Long getPending() {
		return pending;
	}

	/**
	 * @param running
	 *            the running to set
	 */
	public void setRunning(Long running) {
		this.running = running;
	}

	/**
	 * @return the running
	 */
	public Long getRunning() {
		return running;
	}

	public void reset() {
		pending = 0L;
		running = 0L;
	}
	
	public void incrementRunning() {
		running ++;
	}
	
	public void incrementPending() {
		pending ++;
	}
	
	public void decrementRunning() {
		if (running > 0L) {
			running--;
		}
	}
	
	public void decrementPending() {
		if (pending > 0L) {
			pending--;
		}
	}
}
