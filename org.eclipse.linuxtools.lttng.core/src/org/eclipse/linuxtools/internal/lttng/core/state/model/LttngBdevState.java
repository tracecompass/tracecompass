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

import org.eclipse.linuxtools.internal.lttng.core.TraceDebug;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.BdevMode;

/**
 * <b><u>LttvBdevState</u></b>
 * <p>
 *
 */
public class LttngBdevState implements Cloneable {
    // ========================================================================
	// Data
    // =======================================================================
	private Stack<BdevMode> mode_stack = new Stack<BdevMode>() ;

	
    // ========================================================================
	// Constructor
    // =======================================================================
	public LttngBdevState() {
		mode_stack.push(BdevMode.LTTV_BDEV_UNKNOWN);
	}
	
	// ========================================================================
	// Methods
	// =======================================================================
	public void clearBdevStack() {
        mode_stack.clear();
    }
	
	public void clearAndSetBaseToBdevStack(BdevMode newState) {
        mode_stack.clear();
        // Ensure that there is always at least 1 item in the stack
        mode_stack.push(newState);
    }
    
    public void pushToBdevStack(BdevMode newState) {
        mode_stack.push(newState);
    }
    
    public BdevMode popFromBdevStack() {
        
       BdevMode returnedMode = mode_stack.pop();
        
       if (mode_stack.size() < 1) {
            TraceDebug.debug("Removing last item from mode stack is not allowed! (popFromModeStack)"); //$NON-NLS-1$
            mode_stack.push(BdevMode.LTTV_BDEV_UNKNOWN);
       }
       
       return returnedMode;
    }
    
    public BdevMode peekFromBdevStack() {
        return mode_stack.peek();
    }
	
    
	@Override
	@SuppressWarnings("unchecked")
    public LttngBdevState clone() {
	    LttngBdevState newState = null;
	    
	    try {
	        newState = (LttngBdevState)super.clone();
	        // Clone should work correctly for all stack object that contain basic java object (String, Long, etc...)
	        newState.mode_stack = (Stack<BdevMode>)this.mode_stack.clone();
	    }
	    catch ( CloneNotSupportedException e ) {
	        System.out.println("Cloning failed with : " + e.getMessage() ); //$NON-NLS-1$
	    }
	    
	    return newState;
	}
	
}