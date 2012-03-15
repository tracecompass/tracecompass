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

import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.IRQMode;

/**
 * <b><u>LttvIRQState</u></b>
 * @author alvaro
 *
 */
public class LttngIRQState implements Cloneable {
    // ========================================================================
	// Data
    // =======================================================================
	private Stack<StateStrings.IRQMode> mode_stack = new Stack<StateStrings.IRQMode>();

    // ========================================================================
    // Constructor
    // =======================================================================
	public LttngIRQState() {
		mode_stack.push(IRQMode.LTTV_IRQ_UNKNOWN);
	}
	
	@Override
	@SuppressWarnings("unchecked")
    public LttngIRQState clone() {
	    LttngIRQState newState = null;
        
        try {
            newState = (LttngIRQState)super.clone();
            
            // Clone should work correctly for all stack object that contain basic java object (String, Long, etc...)
            newState.mode_stack = (Stack<StateStrings.IRQMode>)this.mode_stack.clone();
        }
        catch ( CloneNotSupportedException e ) {
            System.out.println("Cloning failed with : " + e.getMessage() ); //$NON-NLS-1$
        }
        
        return newState;
    }
	
    // ========================================================================
    // Methods
    // =======================================================================
	public void clearIrqStack() {
        mode_stack.clear();
    }
    
    public void clearAndSetBaseToIrqStack(StateStrings.IRQMode newState) {
        mode_stack.clear();
        // Ensure that there is always at least 1 item in the stack
        mode_stack.push(newState);
    }
    
    public void pushToIrqStack(StateStrings.IRQMode newState) {
        mode_stack.push(newState);
    }
    
    public StateStrings.IRQMode popFromIrqStack() {
        
        StateStrings.IRQMode returnedMode = mode_stack.pop();
       if (mode_stack.size() < 1) {
            mode_stack.push(IRQMode.LTTV_IRQ_UNKNOWN);
       }
       
       return returnedMode;
    }
    
    public StateStrings.IRQMode peekFromIrqStack() {
        return mode_stack.peek();
    }
}
