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
 */
public class HistogramElement {
	public Integer position = 0;
	public Long firstIntervalTimestamp = 0L;
	public Long intervalNbEvents = 0L;
	public Integer intervalHeight = 0;
}
