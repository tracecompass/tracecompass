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
package org.eclipse.linuxtools.lttng.ui.model.trange;


import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LTTngCPUState;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngIRQState;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngTraceState;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngTrapState;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeEventResource.ResourceTypes;

/**
 * Creates Resources with custom implementation to obtain its corresponding
 * state mode
 * <p>
 * The state mode resolution is needed at the end of a data request, as well as
 * in the before and after handlers
 * </p>
 * 
 * @author alvaro
 * 
 */
public class TimeRangeResourceFactory {
	// ========================================================================
	// Data
	// =======================================================================
	private static TimeRangeResourceFactory instance = null;

	// ========================================================================
	// Create instance
	// =======================================================================
	/**
	 * Factory singleton
	 * 
	 * @return
	 */
	public static TimeRangeResourceFactory getInstance() {
		if (instance == null) {
			instance = new TimeRangeResourceFactory();
		}
		return instance;
	}

	// ========================================================================
	// Public methods
	// =======================================================================
	public TimeRangeEventResource createResource(int newId, long newStartTime,
			long newStopTime, String newName, String newGroupName,
			String newClassName, ResourceTypes type, Long newResourceId,
			long insertionTime) {

		TimeRangeEventResource resource = null;
		switch (type) {
		case CPU:
			resource = createCpuResource(newId, newStartTime, newStopTime,
					newName, newGroupName, newClassName, type, newResourceId,
					insertionTime);
			break;
		case IRQ:
			resource = createIrqResource(newId, newStartTime, newStopTime,
					newName, newGroupName, newClassName, type, newResourceId,
					insertionTime);
			break;
		case SOFT_IRQ:
			resource = createSoftIrqResource(newId, newStartTime, newStopTime,
					newName, newGroupName, newClassName, type, newResourceId,
					insertionTime);
			break;
		case TRAP:
			resource = createTrapResource(newId, newStartTime, newStopTime,
					newName, newGroupName, newClassName, type, newResourceId,
					insertionTime);
			break;
		case BDEV:
			resource = createBdevResource(newId, newStartTime, newStopTime,
					newName, newGroupName, newClassName, type, newResourceId,
					insertionTime);
			break;
		default:
			break;
		}

		return resource;
	}

	// ========================================================================
	// Private methods
	// =======================================================================
	private TimeRangeEventResource createIrqResource(int newId,
			long newStartTime, long newStopTime, String newName,
			String newGroupName, String newClassName, ResourceTypes newType,
			Long newResourceId, long insertionTime) {

		TimeRangeEventResource resource = new TimeRangeEventResource(newId,
				newStartTime, newStopTime, newName, newGroupName, newClassName,
				newType, newResourceId, insertionTime) {

			@Override
			public String getStateMode(LttngTraceState traceSt) {
				LttngIRQState irqState = traceSt.getIrq_states().get(
						getResourceId());
				String statemode = ""; //$NON-NLS-1$
				if (irqState != null) {
					statemode = irqState.peekFromIrqStack().getInName();
				}

				return statemode;
			}
		};
		
		return resource;
	}

	private TimeRangeEventResource createTrapResource(int newId,
			long newStartTime, long newStopTime, String newName,
			String newGroupName, String newClassName, ResourceTypes newType,
			Long newResourceId, long insertionTime) {

		TimeRangeEventResource resource = new TimeRangeEventResource(newId,
				newStartTime, newStopTime, newName, newGroupName, newClassName,
				newType, newResourceId, insertionTime) {

			@Override
			public String getStateMode(LttngTraceState traceSt) {
				// Determine the trap state.
				String trapStateMode = ""; //$NON-NLS-1$
				LttngTrapState ts = traceSt.getTrap_states().get(getResourceId());
				
				// *** Note : 
				//	Ts might not have been created yet.
				//	This is because the state system will be updated next to this before hook
				//	It should be correct to create it here as Busy 
				//		(traps are created with running++ so it wont be idle)
				if ( ts != null ) {
					Long trapState = ts.getRunning();
					
					if (trapState == 0) {
						trapStateMode = StateStrings.TrapMode.LTTV_TRAP_IDLE.getInName();
					} else {
						trapStateMode = StateStrings.TrapMode.LTTV_TRAP_BUSY.getInName();
					}
				}
				else {
					trapStateMode = StateStrings.TrapMode.LTTV_TRAP_BUSY.getInName();
				}
				
				return trapStateMode;
			}
		};

		return resource;
	}

	private TimeRangeEventResource createSoftIrqResource(int newId,
			long newStartTime, long newStopTime, String newName,
			String newGroupName, String newClassName, ResourceTypes newType,
			Long newResourceId, long insertionTime) {

		TimeRangeEventResource resource = new TimeRangeEventResource(newId,
				newStartTime, newStopTime, newName, newGroupName, newClassName,
				newType, newResourceId, insertionTime) {

			@Override
			public String getStateMode(LttngTraceState traceSt) {
				// Get the resource id.
				Long softIrqId = getResourceId();
				// Get the resource state mode
				long running = traceSt.getSoft_irq_states().get(softIrqId)
						.getRunning().longValue();
				long pending = traceSt.getSoft_irq_states().get(softIrqId)
						.getPending().longValue();

				String softIrqStateMode;
				if (running > 0) {
					softIrqStateMode = StateStrings.SoftIRQMode.LTTV_SOFT_IRQ_BUSY
							.getInName();
				} else if (pending > 0) {
					softIrqStateMode = StateStrings.SoftIRQMode.LTTV_SOFT_IRQ_PENDING
							.getInName();
				} else {
					softIrqStateMode = StateStrings.SoftIRQMode.LTTV_SOFT_IRQ_IDLE
							.getInName();
				}

				return softIrqStateMode;
			}

		};

		return resource;
	}

	private TimeRangeEventResource createBdevResource(int newId,
			long newStartTime, long newStopTime, String newName,
			String newGroupName, String newClassName, ResourceTypes newType,
			Long newResourceId, long insertionTime) {

		TimeRangeEventResource resource = new TimeRangeEventResource(newId,
				newStartTime, newStopTime, newName, newGroupName, newClassName,
				newType, newResourceId, insertionTime) {

			@Override
			public String getStateMode(LttngTraceState traceSt) {
				// Get the resource state mode
				String bdevStateMode = traceSt.getBdev_states().get(
						getResourceId()).peekFromBdevStack().getInName();

				return bdevStateMode;
			}

		};

		return resource;
	}

	private TimeRangeEventResource createCpuResource(int newId,
			long newStartTime, long newStopTime, String newName,
			String newGroupName, String newClassName, ResourceTypes newType,
			Long newResourceId, long insertionTime) {

		TimeRangeEventResource resource = new TimeRangeEventResource(newId,
				newStartTime, newStopTime, newName, newGroupName, newClassName,
				newType, newResourceId, insertionTime) {

			@Override
			public String getStateMode(LttngTraceState traceSt) {
				// Get the resource state mode
			    LTTngCPUState cpuState = traceSt.getCpu_states().get(
                        getResourceId());
			    
				String cpuStateMode = ""; //$NON-NLS-1$
				if (cpuState != null) { 
				    cpuStateMode = traceSt.getCpu_states().get(
				            getResourceId())
				            .peekFromCpuStack().getInName();
				}

				return cpuStateMode;
			}

		};

		return resource;
	}
}
