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
 * 	 Michel Dagenais (michel.dagenais@polymtl.ca) - Reference C implementation, used with permission
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.views.resources.evProcessor;

import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.Channels;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.Events;
import org.eclipse.linuxtools.internal.lttng.core.state.StateStrings.Fields;
import org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.ILttngEventProcessor;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngTraceState;
import org.eclipse.linuxtools.internal.lttng.ui.model.trange.TimeRangeEventResource;
import org.eclipse.linuxtools.internal.lttng.ui.model.trange.TimeRangeEventResource.ResourceTypes;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;

/**
 * Creates instances of specific before state update handlers, per corresponding
 * event.
 * 
 * @author alvaro
 * 
 */
public class ResourcesBeforeUpdateHandlers {

	/**
	 * <p>
	 * Handles: LTT_EVENT_SCHED_SCHEDULE
	 * </p>
	 * Replace C function named "before_schedchange_hook" in eventhooks.c
	 * <p>
	 * Fields: LTT_FIELD_PREV_PID, LTT_FIELD_NEXT_PID(?), LTT_FIELD_PREV_STATE
	 * (?)
	 * </p>
	 * 
	 * @return
	 */
	final ILttngEventProcessor getBeforeSchedChangeHandler() {
		AbsResourcesTRangeUpdate handler = new AbsResourcesTRangeUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {
				// Create a time range for the cpu.
				globalProcessBeforeExecmode(trcEvent, traceSt);

				return false;
			}
		};

		return handler;
	}

	/**
	 * <p>
	 * Handles: LTT_EVENT_IRQ_ENTRY, LTT_EVENT_IRQ_EXIT
	 * </p>
	 * Replace C function named "before_execmode_hook_irq" in eventhooks.c
	 * 
	 * @return
	 */
	final ILttngEventProcessor getBeforeExecutionModeIrq() {
		AbsResourcesTRangeUpdate handler = new AbsResourcesTRangeUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				Long irqId = null;

				// According to Ltt, we should not draw anything if the channel
				// is the kernel one
				if (trcEvent.getChannelName().equals(
						Channels.LTT_CHANNEL_KERNEL.name())) {
					return false;
				} else {

					if (trcEvent.getMarkerName().equals(
							Events.LTT_EVENT_IRQ_ENTRY.getInName())) {
						irqId = getAFieldLong(trcEvent, traceSt,
								Fields.LTT_FIELD_IRQ_ID);
					} else if (trcEvent.getMarkerName().equals(
							Events.LTT_EVENT_IRQ_EXIT.getInName())) {
						long cpu = trcEvent.getCpuId();
						irqId = traceSt.getCpu_states().get(cpu)
								.peekFromIrqStack();
						if (irqId.equals(-1L)) {
							// nothing to update
							return false;
						}
					}


					// softIrqId is the resource id here
					TimeRangeEventResource localResource = resourcelist_obtain_irq(
							traceSt, irqId);

					// If the resource is missing in the list, add it
					if (localResource == null) {
						TmfTimeRange timeRange = traceSt.getContext()
								.getTraceTimeWindow();
						localResource = addLocalResource(timeRange
								.getStartTime().getValue(), timeRange
								.getEndTime().getValue(), traceSt.getTraceId(),
								ResourceTypes.IRQ, irqId, trcEvent
										.getTimestamp().getValue());
					}

					// get the start time
					long stime = localResource.getNext_good_time();

					// Get the resource state mode
					String irqStateMode = localResource.getStateMode(traceSt);

					// Call the makeDraw function
					makeDraw(traceSt, stime,
							trcEvent.getTimestamp().getValue(), localResource,
							params, irqStateMode);

					// Call the globalProcessBeforeExecmode() after, as
					// it is needed by all
					// getBeforeExecmode*SOMETHING*()
					globalProcessBeforeExecmode(trcEvent, traceSt);
				}
				return false;
			}
		};

		return handler;
	}

	/**
	 * <p>
	 * Handles: LTT_EVENT_SOFT_IRQ_RAISE, LTT_EVENT_SOFT_IRQ_ENTRY,
	 * LTT_EVENT_SOFT_IRQ_EXIT,
	 * </p>
	 * Replace C function named "before_execmode_hook_soft_irq" in eventhooks.c
	 * <p>
	 * Fields: LTT_FIELD_SOFT_IRQ_ID
	 * </p>
	 * 
	 * @return
	 */
	final ILttngEventProcessor getBeforeExecutionModeSoftIrq() {
		AbsResourcesTRangeUpdate handler = new AbsResourcesTRangeUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				Long softIrqId = null;

				// According to Ltt, we should not draw anything if the channel
				// is the kernel one
				if (trcEvent.getChannelName().equals(
						Channels.LTT_CHANNEL_KERNEL.name())) {
					return false;
				} else {

					if ((trcEvent.getMarkerName()
							.equals(Events.LTT_EVENT_SOFT_IRQ_RAISE.getInName()))
							|| (trcEvent.getMarkerName()
									.equals(Events.LTT_EVENT_SOFT_IRQ_ENTRY
											.getInName()))) {
						softIrqId = getAFieldLong(trcEvent, traceSt,
								Fields.LTT_FIELD_SOFT_IRQ_ID);
					} else if (trcEvent.getMarkerName().equals(
							Events.LTT_EVENT_SOFT_IRQ_EXIT.getInName())) {
						long cpu = trcEvent.getCpuId();
						softIrqId = traceSt.getCpu_states().get(cpu)
								.peekFromSoftIrqStack();
						if (softIrqId < 0) {
							// nothing to update
							return false;
						}
					}

					// Add the resource to the resource list
					// softIrqId is the resource id here
					TimeRangeEventResource localResource = resourcelist_obtain_soft_irq(
							traceSt, softIrqId);

					// If the resource is missing in the list, add it
					if (localResource == null) {
						TmfTimeRange timeRange = traceSt.getContext()
								.getTraceTimeWindow();
						localResource = addLocalResource(timeRange
								.getStartTime().getValue(), timeRange
								.getEndTime().getValue(), traceSt.getTraceId(),
								ResourceTypes.SOFT_IRQ, softIrqId, trcEvent
										.getTimestamp().getValue());
					}

					// get the start time
					long stime = localResource.getNext_good_time();

					// Get the resource state mode
					String softIrqStateMode = localResource
							.getStateMode(traceSt);

					// Call the makeDraw function
					makeDraw(traceSt, stime,
							trcEvent.getTimestamp().getValue(), localResource,
							params, softIrqStateMode);

					// Call the globalProcessBeforeExecmode() after, as
					// it is needed by all
					// getBeforeExecmode*SOMETHING*()
					globalProcessBeforeExecmode(trcEvent, traceSt);

				}

				return false;
			}
		};

		return handler;
	}

	/**
	 * <p>
	 * Handles: LTT_EVENT_TRAP_ENTRY, LTT_EVENT_TRAP_EXIT,
	 * LTT_EVENT_PAGE_FAULT_ENTRY, LTT_EVENT_PAGE_FAULT_EXIT,
	 * LTT_EVENT_PAGE_FAULT_NOSEM_ENTRY, LTT_EVENT_PAGE_FAULT_NOSEM_EXIT
	 * </p>
	 * Replace C function named "before_execmode_hook_trap" in eventhooks.c
	 * <p>
	 * Fields: LTT_FIELD_TRAP_ID
	 * </p>
	 * 
	 * @return
	 */
	final ILttngEventProcessor getBeforeExecutionModeTrap() {
		AbsResourcesTRangeUpdate handler = new AbsResourcesTRangeUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				Long trapId = null;

				// According to Ltt, we should not draw anything if the channel
				// is the kernel one
				if (trcEvent.getChannelName().equals(
						Channels.LTT_CHANNEL_KERNEL.name())) {
					return false;
				} else {

					if ((trcEvent.getMarkerName()
							.equals(Events.LTT_EVENT_TRAP_ENTRY.getInName()))
							|| (trcEvent.getMarkerName()
									.equals(Events.LTT_EVENT_PAGE_FAULT_ENTRY
											.getInName()))
							|| (trcEvent.getMarkerName()
									.equals(Events.LTT_EVENT_PAGE_FAULT_NOSEM_ENTRY
											.getInName()))) {
						trapId = getAFieldLong(trcEvent, traceSt,
								Fields.LTT_FIELD_TRAP_ID);
					} else if ((trcEvent.getMarkerName()
							.equals(Events.LTT_EVENT_TRAP_EXIT.getInName()))
							|| (trcEvent.getMarkerName()
									.equals(Events.LTT_EVENT_PAGE_FAULT_EXIT
											.getInName()))
							|| (trcEvent.getMarkerName()
									.equals(Events.LTT_EVENT_PAGE_FAULT_NOSEM_EXIT
											.getInName()))) {
						long cpu = trcEvent.getCpuId();
						trapId = traceSt.getCpu_states().get(cpu)
								.peekFromTrapStack();

						if (trapId.equals(-1L)) {
							// Nothing to update
							return false;
						}
					} else {
						return false;
					}

					// Add the resource to the resource list
					// trapId is the resource id here
					TimeRangeEventResource localResource = resourcelist_obtain_trap(
							traceSt, trapId);

					// If the resource is missing in the list, add it
					if (localResource == null) {
						TmfTimeRange timeRange = traceSt.getContext()
								.getTraceTimeWindow();
						localResource = addLocalResource(timeRange
								.getStartTime().getValue(), timeRange
								.getEndTime().getValue(), traceSt.getTraceId(),
								ResourceTypes.TRAP, trapId, trcEvent
										.getTimestamp().getValue());
					}

					// Determine the trap state.
					String trapStateMode = localResource.getStateMode(traceSt);

					long stime = localResource.getNext_good_time();
					makeDraw(traceSt, stime,
							trcEvent.getTimestamp().getValue(), localResource,
							params, trapStateMode);

					// Call the globalProcessBeforeExecmode() after, as
					// it is needed by all
					// getBeforeExecmode*SOMETHING*()
					globalProcessBeforeExecmode(trcEvent, traceSt);

				}

				return false;
			}
		};

		return handler;
	}

	/**
	 * <p>
	 * Handles: LTT_EVENT_REQUEST_ISSUE, LTT_EVENT_REQUEST_COMPLETE
	 * </p>
	 * Replace C function named "before_bdev_event_hook" in eventhooks.c
	 * <p>
	 * Fields: LTT_FIELD_MAJOR, LTT_FIELD_MINOR, LTT_FIELD_OPERATION (?)
	 * </p>
	 * 
	 * @return
	 */
	final ILttngEventProcessor getBeforeBdevEvent() {
		AbsResourcesTRangeUpdate handler = new AbsResourcesTRangeUpdate() {

			// @Override
			@Override
			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {
				Long major = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_MAJOR);
				Long minor = getAFieldLong(trcEvent, traceSt,
						Fields.LTT_FIELD_MINOR);
				// This is useless even in LTTv!
				// Long oper = getAFieldLong(trcEvent, traceSt,
				// Fields.LTT_FIELD_OPERATION);

				Long bdevId = getMkdevId(major, minor);

				// According to Lttv, bdevId (obtain from MKDEV macro) is
				// the id here
				TimeRangeEventResource localResource = resourcelist_obtain_bdev(
						traceSt, bdevId);

				if (localResource == null) {
					TmfTimeRange timeRange = traceSt.getContext()
							.getTraceTimeWindow();
					localResource = addLocalResource(timeRange.getStartTime()
							.getValue(), timeRange.getEndTime().getValue(),
							traceSt.getTraceId(), ResourceTypes.BDEV, bdevId,
							trcEvent.getTimestamp().getValue());
				}

				// get the start time
				long stime = localResource.getNext_good_time();
				// Get the resource state mode
				String bdevStateMode = localResource.getStateMode(traceSt);
				// Call the makeDraw function
				makeDraw(traceSt, stime, trcEvent.getTimestamp().getValue(),
						localResource, params, bdevStateMode);

				return false;
			}
		};

		return handler;
	}
}
