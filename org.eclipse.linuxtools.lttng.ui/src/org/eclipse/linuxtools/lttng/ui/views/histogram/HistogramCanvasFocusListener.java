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

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;

/**
 * <b><u>HistogramCanvasFocusListener</u></b>
 * <p>
 * Implementation of a FocusListener for the need of the HistogramCanvas
 * <p> 
 */
public class HistogramCanvasFocusListener implements FocusListener {
	
	@SuppressWarnings("unused")
	private HistogramCanvas parentCanvas = null;
	
	/**
	 * HistogramCanvasFocusListener constructor
	 * 
	 * @param newCanvas Related canvas
	 */
	public HistogramCanvasFocusListener(HistogramCanvas newCanvas) {
		parentCanvas = newCanvas;
	}
	
	/**
	 * Function that is called when the canvas get focus.<p>
	 * 
	 * Doesn't do anything yet... 
	 * 
	 * @param event  The focus event generated.
	 */
	public void focusGained(FocusEvent event) {
		System.out.println("focusGained");
	}
	
	/**
	 * Function that is called when the canvas get focus.<p>
	 * 
	 * Doesn't do anything yet... 
	 * 
	 * @param event  The focus event generated.
	 */
	public void focusLost(FocusEvent event) {
		System.out.println("focusLost");
	}

}
