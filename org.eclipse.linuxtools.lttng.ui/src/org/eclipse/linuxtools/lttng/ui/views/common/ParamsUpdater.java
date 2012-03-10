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
package org.eclipse.linuxtools.lttng.ui.views.common;

import org.eclipse.linuxtools.lttng.core.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.ui.TraceDebug;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.TmfTimeScaleSelectionEvent;

/**
 * 
 * Preserve the time and space width parameters applicable to a particular view
 * in order to facilitate filtering of events and request handling.
 * 
 * @author alvaro
 * 
 */
public class ParamsUpdater {
	// ========================================================================
	// Data
	// ========================================================================

    private long startTime = 0;
    private long endTime = Long.MAX_VALUE;
    private static Long selectedTime = null;
    private final int DEFAULT_WIDTH = 2000; // number of estimated pixels that
                                            // can hold the time range space
    private int width = DEFAULT_WIDTH; // width in pixels used to represent the
                                       // time interval
    private double pixelsPerNs = 0;
    private int eventsDiscarded = 0;
    private int eventsDiscardedOutOfView = 0;
    private int eventsDiscardedNotVisible = 0;
    private int eventsDiscardedWrongOrder = 0;
    private TmfTimeRange trange = null;

    public static final int OUT_OF_VIEWRANGE = 0;
    public static final int NOT_VISIBLE = 1;

	// ========================================================================
	// Methods
	// ========================================================================

	/**
	 * @param event
	 * @return
	 */
	public synchronized boolean processTimeScaleEvent(
			TmfTimeScaleSelectionEvent event) {

		boolean updated = false;
		if (event != null) {
			long time0 = event.getTime0();
			long time1 = event.getTime1();
			int dwidth = event.getWidth();

			updated = update(time0, time1, dwidth);

			// initialization only, otherwise wait for the actual selection
			// event to update its value. Note that the time must be different
			// upon selection of a new time in order to trigger an update to all
			if (selectedTime == null) {
				setSelectedTime(event.getSelectedTime());
			}

		}

		return updated;

	}

	/**
	 * Save the selected time
	 * @param selTime
	 */
	public void setSelectedTime(long selTime) {
		TraceDebug.debug("Selected time changed from: \n\t" + selectedTime //$NON-NLS-1$
				+ " to: \n\t" + selTime); //$NON-NLS-1$
		selectedTime = selTime;
	}

	/**
	 * May return null, if the selected time is invalid
	 * 
	 * @return
	 */
	public Long getSelectedTime() {
		return selectedTime;
	}

	/**
	 * Update time range but keep width as is
	 * 
	 * @param time0
	 * @param time1
	 * @return
	 */
	public boolean update(long time0, long time1) {
		return update(time0, time1, width);
	}

	/**
	 * Only positive attributes are expected
	 * 
	 * @param time0
	 * @param time1
	 * @param dwidth
	 * @return
	 */
	public boolean update(long time0, long time1, int dwidth) {
		boolean updated = false;

		if (time0 == startTime && time1 == endTime && dwidth == width) {
			// No updated needed
			return updated;
		}

		// Negatives are invalid
		time0 = time0 > 0 ? time0 : 0;
		time1 = time1 > 0 ? time1 : 0;
		dwidth = dwidth > 0 ? dwidth : 0;

		if (time1 > time0) {
			// Store the new values as long as they are within range
			startTime = time0;
			endTime = time1;
			width = dwidth;

			pixelsPerNs = (double) width / (double) (endTime - startTime);

			TmfTimestamp fTimeStart = new LttngTimestamp(startTime);
			TmfTimestamp fTimeEnd = new LttngTimestamp(endTime);
			trange = new TmfTimeRange(fTimeStart, fTimeEnd);

			// make sure the selected time is within the new range or else set
			// mark it as invalid
			if (selectedTime != null) {
				setSelectedTime(selectedTime);
			}

			// update succeeded
			updated = true;

			TraceDebug.debug("Configuration updated to: StartTime: " /* */ //$NON-NLS-1$
					+ fTimeStart /* */
					+ "-" /* */ //$NON-NLS-1$
					+ fTimeEnd /* */
					+ " width: " /* */ //$NON-NLS-1$
					+ width + " k: " + pixelsPerNs); /* */ //$NON-NLS-1$
		} else {
			TraceDebug
					.debug("End time is not greater than start time, start time: " //$NON-NLS-1$
							+ time0 + " end time: " + time1); //$NON-NLS-1$
		}

		return updated;
	}

	/**
	 * @return
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * @return
	 */
	public long getEndTime() {
		return endTime;
	}

	/**
	 * @return
	 */
	public int getWidth() {
		if (width == 0) {
			TraceDebug
					.debug("Unexpected width value of 0 pixels, returning default"); //$NON-NLS-1$
			return DEFAULT_WIDTH;
		}

		return width;
	}

	/**
	 * Return the current constant "K" of pixels per nano second used for the
	 * widest time space widget registered in this instance.
	 * 
	 * @return
	 */
	public double getPixelsPerNs() {
		return pixelsPerNs;
	}

	/**
	 * Set the value of pixels per nano second as long as the value is grater
	 * positive
	 * 
	 * @return
	 */
	public void setPixelsPerNs(double pixperNsec) {
		if (pixperNsec > 0) {
			pixelsPerNs = pixperNsec;
		}
	}

	/**
	 * @param value
	 */
	public void setEventsDiscarded(int value) {
		eventsDiscarded = value;
		if (value == 0) {
			eventsDiscardedWrongOrder = 0;
			eventsDiscardedNotVisible = 0;
			eventsDiscardedOutOfView = 0;
		}
	}

	/**
	 * 
	 */
	public void incrementEventsDiscarded(int reason) {
		if (reason == OUT_OF_VIEWRANGE) {
			this.eventsDiscardedOutOfView++;
		}

		if (reason == NOT_VISIBLE) {
			this.eventsDiscardedNotVisible++;
		}

		this.eventsDiscarded++;
	}

	/**
	 * @return
	 */
	public int getEventsDiscarded() {
		return eventsDiscarded;
	}

	/**
	 * increase the number of events discarder since they were not received in a
	 * later time than previous events
	 */
	public void incrementEventsDiscardedWrongOrder() {
		this.eventsDiscarded++;
		this.eventsDiscardedWrongOrder++;
	}

	/**
	 * @return
	 */
	public int getEventsDiscardedWrongOrder() {
		return eventsDiscardedWrongOrder;

	}

	/**
	 * @return
	 */
	public int getEventsDiscardedNotVisible() {
		return eventsDiscardedNotVisible;

	}

	/**
	 * @return
	 */
	public int getEventsDiscardedOutOfViewRange() {
		return eventsDiscardedOutOfView;

	}

	/**
	 * @return
	 */
	public TmfTimeRange getTrange() {
		return trange;
	}

}
