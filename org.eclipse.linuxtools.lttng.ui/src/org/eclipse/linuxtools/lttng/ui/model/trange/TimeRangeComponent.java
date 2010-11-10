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


/**
 * @author alvaro
 * 
 */
public abstract class TimeRangeComponent implements ITimeRangeComponent {

	// ========================================================================
	// Data
	// =======================================================================
	protected Long startTime = 0L;
	protected Long stopTime = Long.MAX_VALUE;
	protected TimeRangeComposite eventParent = null;
	private boolean visible = true;
	


	// ========================================================================
	// Constructor
	// =======================================================================
	public TimeRangeComponent(Long stime, Long etime,
			TimeRangeComposite eventParent) {
		this.startTime = stime;
		this.stopTime = etime;
		this.eventParent = eventParent;
	}

	// ========================================================================
	// Methods
	// =======================================================================
	/**
	 * This method shall not be used to estimate the starting drawing point of
	 * the time range-event. see interface method getTime(). However this method
	 * can be used to retrieve the tool tip information where we need to reflect
	 * that the actual start of this event is unknown
	 * 
	 * @param time
	 */
	@Override
	public long getStartTime() {
		return startTime.longValue();
	}

	@Override
	public void setStartTime(long time) {
		if (time > -1) {
			startTime = time;
		}
	}

	@Override
	public long getStopTime() {
		return stopTime.longValue();
	}

	@Override
	public void setStopTime(long stopTime) {
		if (stopTime > -1) {
			this.stopTime = stopTime;
		}
	}

	@Override
	public ITimeRangeComponent getEventParent() {
		return eventParent;
	}

	public void setEventParent(TimeRangeComposite eventParent) {
		this.eventParent = eventParent;
	}

	@Override
	public abstract String getName();

	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.ui.model.trange.ITimeRangeComponent#isVisible()
	 */
	@Override
	public boolean isVisible() {
		return visible;
	}

    @Override
    @SuppressWarnings("nls")
    public String toString() {
		return "[TimeRangeComponent:" + "startTime=" + startTime + ",stopTime=" + stopTime +
		",parent=" + (eventParent != null ? eventParent.id : "null") + "]";
    }

}
