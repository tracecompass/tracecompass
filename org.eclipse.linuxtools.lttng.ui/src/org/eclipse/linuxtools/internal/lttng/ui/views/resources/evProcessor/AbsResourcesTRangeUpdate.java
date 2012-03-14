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

import java.util.Vector;

import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.internal.lttng.core.state.evProcessor.ILttngEventProcessor;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngTraceState;
import org.eclipse.linuxtools.internal.lttng.ui.TraceDebug;
import org.eclipse.linuxtools.internal.lttng.ui.model.trange.TimeRangeComponent;
import org.eclipse.linuxtools.internal.lttng.ui.model.trange.TimeRangeEvent;
import org.eclipse.linuxtools.internal.lttng.ui.model.trange.TimeRangeEventResource;
import org.eclipse.linuxtools.internal.lttng.ui.model.trange.TimeRangeResourceFactory;
import org.eclipse.linuxtools.internal.lttng.ui.model.trange.TimeRangeEvent.Type;
import org.eclipse.linuxtools.internal.lttng.ui.model.trange.TimeRangeEventResource.ResourceTypes;
import org.eclipse.linuxtools.internal.lttng.ui.views.common.AbsTRangeUpdate;
import org.eclipse.linuxtools.internal.lttng.ui.views.common.ParamsUpdater;
import org.eclipse.linuxtools.internal.lttng.ui.views.resources.model.ResourceContainer;
import org.eclipse.linuxtools.internal.lttng.ui.views.resources.model.ResourceModelFactory;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;

public abstract class AbsResourcesTRangeUpdate extends AbsTRangeUpdate
		implements ILttngEventProcessor {

	// ========================================================================
	// Data
	// =======================================================================
	protected ResourceContainer resContainer = ResourceModelFactory
			.getResourceContainer();
	protected ParamsUpdater params = ResourceModelFactory.getParamsUpdater();
	protected static final Long ANY_CPU = 0L;

	// ========================================================================
	// Methods
	// =======================================================================
	protected TimeRangeEventResource addLocalResource(long traceStartTime,
			long traceEndTime, String traceId, ResourceTypes type, Long resId,
			long insertionTime) {

		String resourceName = type.toString() + " " + resId.toString(); //$NON-NLS-1$
		// Note : the "traceid" here is assigned to the "groupname" as we group
		// by trace in the UI
		TimeRangeEventResource localRessource = TimeRangeResourceFactory
				.getInstance().createResource(resContainer.getUniqueId(),
						traceStartTime, traceEndTime, resourceName, traceId,
						"", type, resId, insertionTime); //$NON-NLS-1$
		resContainer.addItem(localRessource);

		if (TraceDebug.isRV()) {
			TraceDebug.traceRV("addLocalResource():" + localRessource); //$NON-NLS-1$
		}

		return localRessource;
	}

	/**
	 * Used to check if the event is visible within the current visible time
	 * window
	 * 
	 * @return
	 */
	protected boolean withinViewRange(long stime, long etime) {
		long windowStartTime = params.getStartTime();
		long windowEndTime = params.getEndTime();

		// Start of event is already out of range
		if (stime > windowEndTime) {
			return false;
		}

		// End time within or beyond start of window as long as the start time
		// is before the end of the window (condition above)
		if (etime >= windowStartTime) {
			return true;
		}

		// // start time is within window
		// if (stime >= windowStartTime && stime <= windowEndTime) {
		// // The event or part of it shall be displayed.
		// return true;
		// }
		//
		// // end time is within window
		// if (etime >= windowStartTime && etime <= windowEndTime) {
		// // The event or part of it shall be displayed.
		// return true;
		// }

		// crosses the window
		if (stime <= windowStartTime && etime >= windowEndTime) {
			// The time range is bigger than the selected time window and
			// crosses it
			return true;
		}

		return false;
	}

	public TimeRangeEventResource resourcelist_obtain_bdev(
			LttngTraceState traceState, Long resourceId) {
		return resourcelist_obtain_generic(resourceId, ResourceTypes.BDEV,
				traceState.getTraceId());
	}

	public TimeRangeEventResource resourcelist_obtain_trap(
			LttngTraceState traceState, Long resourceId) {
		return resourcelist_obtain_generic(resourceId, ResourceTypes.TRAP,
				traceState.getTraceId());
	}

	public TimeRangeEventResource resourcelist_obtain_irq(
			LttngTraceState traceState, Long resourceId) {
		return resourcelist_obtain_generic(resourceId, ResourceTypes.IRQ,
				traceState.getTraceId());
	}

	public TimeRangeEventResource resourcelist_obtain_soft_irq(
			LttngTraceState traceState, Long resourceId) {
		return resourcelist_obtain_generic(resourceId, ResourceTypes.SOFT_IRQ,
				traceState.getTraceId());
	}

	public TimeRangeEventResource resourcelist_obtain_cpu(
			LttngTraceState traceState, Long resourceId) {
		return resourcelist_obtain_generic(resourceId, ResourceTypes.CPU,
				traceState.getTraceId());
	}

	public TimeRangeEventResource resourcelist_obtain_machine(
			LttngTraceState traceState, Long resourceId) {
		// *** VERIFY ***
		// Does "UNKNOWN" make sense for "obtain_machine" ?
		// It seems to be the only choice, thought...
		return resourcelist_obtain_generic(resourceId, ResourceTypes.UNKNOWN,
				traceState.getTraceId());
	}

	public TimeRangeEventResource resourcelist_obtain_generic(Long resourceId,
			ResourceTypes resourceType, String traceId) {
		return resContainer.findItem(resourceId, resourceType, traceId);
	}

	protected boolean globalProcessBeforeExecmode(LttngEvent trcEvent,
			LttngTraceState traceSt) {

		// TODO: Implement the tracking of current resource in order ot speed up
		// searching for the relevant resource similar to current_hash_data in
		// the C implementation
		// e.g.
		// hashed_process_data =
		// process_list->current_hash_data[trace_num][cpu];

		TimeRangeEventResource localResource = resourcelist_obtain_cpu(traceSt,
				trcEvent.getCpuId());
		Long cpu = trcEvent.getCpuId();
		if (localResource == null) {
			TmfTimeRange timeRange = traceSt.getContext().getTraceTimeWindow();
			localResource = addLocalResource(timeRange.getStartTime()
					.getValue(), timeRange.getEndTime().getValue(),
					traceSt.getTraceId(), ResourceTypes.CPU, cpu, trcEvent
					.getTimestamp().getValue());
		}

		// get the start time
		long stime = localResource.getNext_good_time();
		// Get the resource state mode
		String cpuStateMode = localResource.getStateMode(traceSt);
		// Call the makeDraw function
		makeDraw(traceSt, stime, trcEvent.getTimestamp().getValue(),
				localResource, params, cpuStateMode);

		return false;
	}

	/**
	 * @param traceSt
	 * @param startTime
	 * @param endTime
	 * @param localResource
	 * @param params
	 * @param stateMode
	 * @return
	 */
	@SuppressWarnings("deprecation")
	protected boolean makeDraw(LttngTraceState traceSt, long stime, long etime,
			TimeRangeEventResource localResource, ParamsUpdater params,
			String stateMode) {

		if (TraceDebug.isRV()) {
			TraceDebug.traceRV("makeDraw():[" + localResource + ",candidate=[stime=" + stime + ",etime=" + etime + ",state=" + stateMode + "]]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}

		// Check if the event is out of range
		if (!withinViewRange(stime, etime)) {
			params.incrementEventsDiscarded(ParamsUpdater.OUT_OF_VIEWRANGE);
			return false;
		}

		// Check if the time range is consistent.
		if (etime < stime) {
			params.incrementEventsDiscardedWrongOrder();
			return false;
		}

		// Store the next good time to start drawing the next event
		// this is done this early to display an accurate start time of the
		// first event
		// within the display window
		// Moved at the end since it produces space gaps among events
		// localResource.setNext_good_time(etime);

		// If First event of a resource, initialise start time half page before to enable pagination to the left
		if (stime < params.getStartTime()) {
			// event start time is before the visible time window
			long insertion = localResource.getInsertionTime();
			if (stime == insertion) {
				// if start time is equal to insertion this is the first event to be drawn for this resource
				long halfPage = (params.getEndTime() - params.getStartTime()) / 2;
				long initTime = params.getStartTime() - halfPage;
				if (initTime > insertion) {
					// start time of this event is unknown, place it half page before visible window to allow left side
					// pagination when selecting previous event
					stime = initTime;
				}
			}
		}

		// Determine if the time range event will fit it the current
		// pixel map
		double duration = etime - stime;
		double k = getPixelsPerNs(traceSt, params);
		double pixels = duration * k;

		// Visibility check
		// Display a "more information" indication by allowing non visible event
		// as long as its previous event is visible.
		boolean visible = true;
		if (pixels < 1.0) {
			boolean prevEventVisibility = true;
			// Get the visibility indication on previous event for
			// this process
			Vector<TimeRangeComponent> inMemEvents = localResource
					.getTraceEvents();
			if (inMemEvents.size() != 0) {
				TimeRangeComponent prevEvent = inMemEvents.get(inMemEvents
						.size() - 1);
				prevEventVisibility = prevEvent.isVisible();

				// if previous event visibility is false and the time span
				// between events less than two pixels, there is no need to
				// load it in memory i.e. not visible and a more indicator is
				// within two pixels.
				// return i.e. event discarded to free up memory
				Long eventSpan = stime - prevEvent.getStartTime();
				if (prevEventVisibility == false
						&& ((double) eventSpan * k) < 2.0) {
					params.incrementEventsDiscarded(ParamsUpdater.NOT_VISIBLE);
					return false;
				}
			}

			// if previous event is visible, set this one to not
			// visible and continue
			visible = false;
		}

		Type eventType = getEventType(localResource);
		if (eventType != null) {
			TimeRangeEvent time_window = new TimeRangeEvent(stime, etime,
					localResource, eventType, stateMode);

			time_window.setVisible(visible);
			localResource.addChildren(time_window);

			localResource.setNext_good_time(etime);
		}

		return false;
	}

	/**
	 * Convert between resource type and timeRange event type
	 * 
	 * @param resource
	 * @return
	 */
	private Type getEventType(TimeRangeEventResource resource) {
		// TODO: Can we merge into one type
		ResourceTypes resType = resource.getType();
		Type eventType = null;

		switch (resType) {
		case CPU:
			eventType = Type.CPU_MODE;
			break;
		case IRQ:
			eventType = Type.IRQ_MODE;
			break;
		case SOFT_IRQ:
			eventType = Type.SOFT_IRQ_MODE;
			break;
		case TRAP:
			eventType = Type.TRAP_MODE;
			break;
		case BDEV:
			eventType = Type.BDEV_MODE;
			break;
		default:
			eventType = Type.PROCESS_MODE;
			break;
		}

		return eventType;
	}

}