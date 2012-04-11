/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng.ui.viewers.timeAnalysis;

import java.util.EventObject;

public class TmfTimeScaleSelectionEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4177428788761351379L;

	
	long time0 = 0;
	long time1 = 0;
	long selectedTime = 0;
	int width = 0;

	/**
	 * 
	 * @param arg0
	 *            source of event
	 * @param time0
	 *            time0 the start time
	 * @param time1
	 * @param width
	 *            pixels used to draw the width of the time space
	 * @param selTime
	 *            carries the selected time if available otherwise is 0
	 */
	public TmfTimeScaleSelectionEvent(Object arg0, long time0, long time1,
			int width, long selTime) {
		super(arg0);
		this.time0 = time0;
		this.time1 = time1;
		this.width = width;
		this.selectedTime = selTime;
	}
	
	/**
	 * @return the start time
	 */
	public long getTime0() {
		return time0;
	}
	
	/**
	 * @return the end time
	 */
	public long getTime1() {
		return time1;
	}

	/**
	 * @return the selection width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return the selected time
	 */
	public long getSelectedTime() {
		return selectedTime;
	}

}
