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
package org.eclipse.linuxtools.internal.lttng.core.state.resource;

import org.eclipse.linuxtools.internal.lttng.core.model.ILTTngTreeNode;

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
		LTT_RESOURCE_PROCESS("process"), /* */ //$NON-NLS-1$
		LTT_RESOURCE_CPU("cpu"), /* */ //$NON-NLS-1$
		LTT_RESOURCE_BDEV("bdev"), /* */ //$NON-NLS-1$
		LTT_RESOURCE_IRQ("irq"), /* */ //$NON-NLS-1$
		LTT_RESOURCE_SOFTIRQ("softIrq"), /* */ //$NON-NLS-1$
		LTT_RESOURCE_TRAP("trap"), /* */ //$NON-NLS-1$
		LTT_RESOURCE_RUNNING_PROCESS("running"); /* */ //$NON-NLS-1$
		
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
		LTT_STATEMODE_UNNAMED("unnamed"), /* */ //$NON-NLS-1$
		LTT_STATEMODE_UNKNOWN("unknown"), /* */ //$NON-NLS-1$
		LTT_STATEMODE_IDLE("idle"), /* */ //$NON-NLS-1$
		LTT_STATEMODE_BUSY("busy"), /* */ //$NON-NLS-1$
		LTT_STATEMODE_PENDING("pending"), /* */ //$NON-NLS-1$
		LTT_STATEMODE_READING("reading"), /* */ //$NON-NLS-1$
		LTT_STATEMODE_WRITING("writing"), /* */ //$NON-NLS-1$
		LTT_STATEMODE_IRQ("irq"), /* */ //$NON-NLS-1$
		LTT_STATEMODE_SOFTIRQ("softirq"), /* */ //$NON-NLS-1$
		LTT_STATEMODE_TRAP("trap"), /* */ //$NON-NLS-1$
		LTT_STATEMODE_WAIT_FORK("waitfork"), /* */ //$NON-NLS-1$
		LTT_STATEMODE_WAIT_CPU("waitcpu"), /* */ //$NON-NLS-1$
		LTT_STATEMODE_EXIT("exit"), /* */ //$NON-NLS-1$
		LTT_STATEMODE_ZOMBIE("zombie"), /* */ //$NON-NLS-1$
		LTT_STATEMODE_WAIT_IO("waitio"), /* */ //$NON-NLS-1$
		LTT_STATEMODE_RUN("run"), /* */ //$NON-NLS-1$
		LTT_STATEMODE_DEAD("dead"), /* */ //$NON-NLS-1$
		LTT_STATEMODE_USER_MODE("usermode"), /* */ //$NON-NLS-1$
		LTT_STATEMODE_SYSCALL("syscall"); /* */ //$NON-NLS-1$
		
		
		String inName;

		private GlobalStateMode(String name) {
			this.inName = name;
		}

		public String getInName() {
			return this.inName;
		}
	}
	
}
