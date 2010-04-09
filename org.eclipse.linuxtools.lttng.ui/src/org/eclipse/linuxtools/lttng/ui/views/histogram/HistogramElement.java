/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.histogram;

/**
 * <b><u>HistogramElement</u></b>
 * <p>
 * This is used by the content to keep its data. 
 * It would be a struct if such a thing would exist in java. 
 * <p>
 * Each "element" should represent a certain time interval
 */
public class HistogramElement {
	public Integer position = 0;				// Position of the element in the table (table index, usually)
	public Long firstIntervalTimestamp = 0L;	// The first timestamp recorded for this interval 
	public Long intervalNbEvents = 0L;			// Number of events recorded in this interval
	public Integer intervalHeight = 0;			// Height (in the canvas) of this element. Should be smaller than the canvas height.
}
