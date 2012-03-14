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
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.CpuMode;

/**
 * <b><u>LTTngCPUState</u></b>
 * <p>
 * 
 */
public class LTTngCPUState implements Cloneable {
	// ========================================================================
	// Data
	// =======================================================================
	private Stack<StateStrings.CpuMode> mode_stack = new Stack<StateStrings.CpuMode>();
	private Stack<Long> irq_stack = new Stack<Long>();
	private Stack<Long> softirq_stack = new Stack<Long>();
	private Stack<Long> trap_stack = new Stack<Long>();

	// ========================================================================
	// Constructor
	// =======================================================================
	public LTTngCPUState() {
		mode_stack.push(CpuMode.LTTV_CPU_UNKNOWN);
		irq_stack.push(-1L);
		softirq_stack.push(-1L);
		trap_stack.push(-1L);
	}

	@Override
	@SuppressWarnings("unchecked")
	public LTTngCPUState clone() {
		LTTngCPUState newState = null;

		try {
			newState = (LTTngCPUState) super.clone();

			// // *** IMPORTANT ***
			// // Basic type in java are immutable!
			// // Thus, using assignation ("=") on basic type is CORRECT,
			// // but we should ALWAYS use "new" or "clone()" on "non basic"
			// type

			// Clone should work correctly for all stack object that contain
			// basic java object (String, Long, etc...)
			newState.mode_stack = (Stack<StateStrings.CpuMode>) this.mode_stack
					.clone();
			newState.irq_stack = (Stack<Long>) this.irq_stack.clone();
			newState.softirq_stack = (Stack<Long>) this.softirq_stack.clone();
			newState.trap_stack = (Stack<Long>) this.trap_stack.clone();
		} catch (CloneNotSupportedException e) {
			System.out.println("Cloning failed with : " + e.getMessage()); //$NON-NLS-1$
		}

		return newState;
	}

	// ========================================================================
	// Methods
	public void clearAndSetBaseToCpuStack(StateStrings.CpuMode newCpuMode) {
		mode_stack.clear();
		irq_stack.clear();
		softirq_stack.clear();
		trap_stack.clear();

		// Ensure that there is always at least 1 item in the stack
		mode_stack.push(newCpuMode);
		irq_stack.push(-1L);
		softirq_stack.push(-1L);
		trap_stack.push(-1L);
	}

	// Push to stacks
	public void pushToCpuStack(StateStrings.CpuMode newCpuMode) {
		mode_stack.push(newCpuMode);
	}

	public void pushToIrqStack(Long irqID) {
		irq_stack.push(irqID);
	}

	public void pushToSoftIrqStack(Long softIrqID) {
		softirq_stack.push(softIrqID);
	}

	public void pushToTrapStack(Long trapID) {
		trap_stack.push(trapID);
	}

	// Pop from stacks
	public StateStrings.CpuMode popFromCpuStack() {

		StateStrings.CpuMode returnedMode = mode_stack.pop();

		if (mode_stack.size() < 1) {
			// Ensure that there is always at least 1 item in the stack
			mode_stack.push(StateStrings.CpuMode.LTTV_CPU_UNKNOWN);
		}

		return returnedMode;

	}

	public Long popFromIrqStack() {
		Long irq = irq_stack.pop();

		if (irq_stack.size() < 1) {
			// make sure the stack is not empty
			irq_stack.push(-1L);
		}
		return irq;
	}

	public Long popFromSoftIrqStack() {
		Long softirq = softirq_stack.pop();

		if (softirq_stack.size() < 1) {
			// make sure the stack is not empty
			softirq_stack.push(-1L);
		}
		return softirq;
	}

	public Long popFromTrapStack() {
		Long trap = trap_stack.pop();

		if (trap_stack.size() < 1) {
			// make sure the stack is not empty
			trap_stack.push(-1L);
		}
		return trap;
	}

	// Peek from stacks
	public StateStrings.CpuMode peekFromCpuStack() {
		return mode_stack.peek();
	}

	public Long peekFromIrqStack() {
		return irq_stack.peek();
	}

	public Long peekFromSoftIrqStack() {
		return softirq_stack.peek();
	}

	public Long peekFromTrapStack() {
		return trap_stack.peek();
	}
	

	public void reset() {
		mode_stack.clear();
		irq_stack.clear();
		softirq_stack.clear();
		trap_stack.clear();

		// Ensure that there is always at least 1 item in the stack
		mode_stack.push(CpuMode.LTTV_CPU_UNKNOWN);
		irq_stack.push(-1L);
		softirq_stack.push(-1L);
		trap_stack.push(-1L);
	}

}
