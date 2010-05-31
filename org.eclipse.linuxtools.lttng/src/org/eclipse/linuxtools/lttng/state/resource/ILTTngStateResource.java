/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.state.resource;

import org.eclipse.linuxtools.lttng.model.ILTTngTreeNode;

/**
 * @author alvaro
 *
 */
public interface ILTTngStateResource<E extends ILTTngStateResource<E>> extends
		ILTTngTreeNode<E> {
	
	// ========================================================================
	// Interface methods
	// =======================================================================
	/**
	 * @return
	 */
	public GlobalStateMode getStateMode();

	/**
	 * @return
	 */
	public ILttngStateContext getContext();

	// ========================================================================
	// Interface Enums
	// =======================================================================
	/**
	 * <p>
	 * Represents the specific type of resources known by the application
	 * </p>
	 * 
	 */
	public enum ResourceType {
		LTT_RESOURCE_PROCESS("process"), /* */
		LTT_RESOURCE_CPU("cpu"), /* */
		LTT_RESOURCE_BDEV("bdev"), /* */
		LTT_RESOURCE_IRQ("irq"), /* */
		LTT_RESOURCE_SOFTIRQ("softIrq"), /* */
		LTT_RESOURCE_TRAP("trap"), /* */
		LTT_RESOURCE_RUNNING_PROCESS("running"); /* */
		
		String inName;

		private ResourceType(String name) {
			this.inName = name;
		}

		public String getInName() {
			return this.inName;
		}
	}
	
	/**
	 * <p>unifies the possible states of the state resources known by the application</p>
	 *
	 */
	public enum GlobalStateMode {
		LTT_STATEMODE_UNNAMED("unnamed"), /* */
		LTT_STATEMODE_UNKNOWN("unknown"), /* */
		LTT_STATEMODE_IDLE("idle"), /* */
		LTT_STATEMODE_BUSY("busy"), /* */
		LTT_STATEMODE_PENDING("pending"), /* */
		LTT_STATEMODE_READING("reading"), /* */
		LTT_STATEMODE_WRITING("writing"), /* */
		LTT_STATEMODE_IRQ("irq"), /* */
		LTT_STATEMODE_SOFTIRQ("softirq"), /* */
		LTT_STATEMODE_TRAP("trap"), /* */
		LTT_STATEMODE_WAIT_FORK("waitfork"), /* */
		LTT_STATEMODE_WAIT_CPU("waitcpu"), /* */
		LTT_STATEMODE_EXIT("exit"), /* */
		LTT_STATEMODE_ZOMBIE("zombie"), /* */
		LTT_STATEMODE_WAIT_IO("waitio"), /* */
		LTT_STATEMODE_RUN("run"), /* */
		LTT_STATEMODE_DEAD("dead"), /* */
		LTT_STATEMODE_USER_MODE("usermode"), /* */
		LTT_STATEMODE_SYSCALL("syscall"); /* */
		
		
		String inName;

		private GlobalStateMode(String name) {
			this.inName = name;
		}

		public String getInName() {
			return this.inName;
		}
	}
	
}
