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

/**
 * The common component of the Composite related to time-range events its
 * components or containers
 * 
 * @author alvaro
 * 
 */
public interface ITimeRangeComponent {

	public long getStartTime();

	public void setStartTime(long startTime);

	public long getStopTime();

	public void setStopTime(long endTime);

	public ITimeRangeComponent getEventParent();

	public String getName();

	/**
	 * Flag to indicate if this Time Range is visible within the GUI, May not be
	 * visible if the duration can not be represented in one pixel
	 * 
	 * @return true if visible i.e. represented at least in one pixel
	 */
	public boolean isVisible();

}
