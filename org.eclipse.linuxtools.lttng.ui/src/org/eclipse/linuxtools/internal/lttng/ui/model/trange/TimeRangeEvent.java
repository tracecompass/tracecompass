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

package org.eclipse.linuxtools.internal.lttng.ui.model.trange;

import org.eclipse.linuxtools.internal.lttng.ui.viewers.timeAnalysis.model.ITimeEvent;
import org.eclipse.linuxtools.internal.lttng.ui.viewers.timeAnalysis.model.ITmfTimeAnalysisEntry;

/**
 * @author alvaro
 * 
 */
public class TimeRangeEvent extends TimeRangeComponent implements ITimeEvent {
	// =======================================================================
	// Data
	// =======================================================================
	TimeRangeComposite parent = null;

	public static enum Type {
		UNKNOWN, PROPERTY, PROCESS_MODE, BDEV_MODE, TRAP_MODE, SOFT_IRQ_MODE, IRQ_MODE, CPU_MODE
	}

	protected Type eventType = Type.UNKNOWN;
	protected String stateMode = ""; //$NON-NLS-1$

	// =======================================================================
	// Constructors
	// =======================================================================
	/**
	 * @param stime
	 *            Event Start Time (may be unknown)
	 * @param etime
	 *            Event EndTime (may be unknown)
	 * @param eventParent
	 * @param type
	 * @param duration
	 */
	public TimeRangeEvent(Long stime, Long etime,
			TimeRangeComposite eventParent, Type type, String stateMode) {
		super(stime, etime, eventParent);
		parent = eventParent;
		this.eventType = type;
		this.stateMode = stateMode;
	}

	// =======================================================================
	// Methods
	// =======================================================================
	
	// @Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITimeEvent#getTime
	 * ()
	 */
	@Override
	public long getTime() {
		// The value provided by this method is used to start drawing the
		// time-range,
		// so a null value shall not be provided.
		// If the actual start time is unknown then use the start of the Trace
		// as the
		// starting reference point.
		if (startTime == null) {
			return eventParent.getStartTime();
		}
		return startTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITimeEvent#getEntry
	 * ()
	 */
	@Override
	public ITmfTimeAnalysisEntry getEntry() {
		return parent;
	}

	/**
	 * return the duration between end and start time , if the start time or end
	 * time are unknown, use the Trace start and End times to estimate it, this
	 * value will be used to draw the time range and need to provide a valid
	 * time width.
	 * 
	 * @return the duration
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITimeEvent#
	 * getDuration()
	 */
	@Override
	public long getDuration() {
		long duration = -1;
		long endT = (stopTime == null) ? parent.getStopTime() : stopTime;
		long startT = (startTime == null) ? parent.getStartTime() : startTime;

		if (endT > startT) {
			return stopTime - startTime;
		}
		return duration;
	}

	/**
	 * @return
	 */
	public String getStateMode() {
		return stateMode;
	}

	/**
	 * @param stateMode
	 */
	public void setStateMode(String stateMode) {
		if (stateMode != null) {
			this.stateMode = stateMode;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.lttng.ui.model.ITimeRangeComponent#getName()
	 */
	@Override
	public String getName() {
		return stateMode;
	}

	/**
	 * @return
	 */
	public Type getEventType() {
		return eventType;
	}

	/**
	 * @param eventType
	 */
	public void setEventType(Type eventType) {
		this.eventType = eventType;
	}

}
