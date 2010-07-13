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
 *   
 * Modifications:
 * 2010-06-20 Yuriy Vashchuk - Histogram optimisations.   
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
		
		// 2010-06-20 Yuriy: Moved from parent class
		createAndAddCanvasRedrawer();
		createAndAddPaintListener();
		createAndAddControlListener();
	}
			
	/*
	 * Create a histogram paint listener and bind it to this canvas.<p>
	 * 
	 * Note : This one is a bit particular, as it is made to draw content that is of a power of 2.
	 * 			The default one draw content that is relative to the real pixels size.
	 */
	@Override
	protected void createAndAddPaintListener() {
		paintListener = new HistogramCanvasPaintListener(this);
		this.addPaintListener( paintListener );
	}
	
	/*
	 * Create a histogram control listener and bind it to this canvas.<p>
	 * 
	 *  @see org.eclipse.linuxtools.lttng.ui.views.histogram.HistogramCanvasControlListener
	 */
	@Override
	protected void createAndAddControlListener() {
		controlListener = new HistogramCanvasControlListener(this);
		this.addControlListener(controlListener);
	}
	
	/**
	 * Notify the parent HistogramView that we have updated information.<p>
	 * This is intended to be called at the end of the request when we know we have up-to-date information.
	 */
	@Override
	public void notifyParentUpdatedInformation() {
		if(parentHistogramWindow != null) {
			parentHistogramWindow.updateSelectedWindowInformation();
		}
	}
}
