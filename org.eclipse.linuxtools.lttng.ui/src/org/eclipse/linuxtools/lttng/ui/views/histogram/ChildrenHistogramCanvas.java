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

import org.eclipse.swt.widgets.Composite;

/**
 * <b><u>ChildrenHistogramCanvas</u></b>
 * <p>
 * Extended implementation of the HistogramCanvas.
 * <p>
 * This canvas goal is to display the "SelectionWindow" in details. 
 */
public class ChildrenHistogramCanvas extends HistogramCanvas {
	
	protected HistogramView parentHistogramWindow = null; 
	
	/**
	 * ChildrenHistogramCanvas constructor.<p>
	 * Same as HistogramCanvas, but receive a parent HistogramView that we can call from here.
	 * 
	 * @param parent 		Composite control which will be the parent of the new instance (cannot be null)
	 * @param 				Style the style of control to construct
	 */
	public ChildrenHistogramCanvas(HistogramView newParentWindow, Composite parent, int style) {
		super(parent, style);
		
		parentHistogramWindow = newParentWindow;
	}
	
	/**
	 * Notify the parent HistogramView that we have updated information.<p>
	 * This is intended to be called at the end of the request when we know we have up-to-date information.
	 */
	@Override
	public void notifyParentUpdatedInformation() {
		parentHistogramWindow.updateSelectedWindowInformation();
	}
}
