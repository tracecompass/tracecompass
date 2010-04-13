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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;

/**
 * <b><u>HistogramCanvasKeyListener</u></b>
 * <p>
 * Implementation of a KeyListener for the need of the HistogramCanvas
 * <p> 
 */
public class HistogramCanvasKeyListener implements KeyListener 
{
	private HistogramCanvas parentCanvas = null;
	private boolean isShiftPressed = false;
	
	/**
	 * HistogramCanvasKeyListener constructor
	 * 
	 * @param newCanvas Related canvas
	 */
	public HistogramCanvasKeyListener(HistogramCanvas newCanvas) {
		parentCanvas = newCanvas;
	}
	
	/**
	 * Function that is called when a key is pressed.<p>
	 * Possible actions : 
	 * - Left arrow  : move the selection window left.<p>
	 * - Right arrow : move the selection window right.<p>
	 * - Shift       : turn on "fast move" mode.<p>
	 * 
	 * @param event  The KeyEvent generated when the key was pressed.
	 */
	public void keyPressed(KeyEvent event) {
		switch (event.keyCode) {
			case SWT.SHIFT:
				isShiftPressed = true;
				break;
			case SWT.ARROW_LEFT:
				moveWindowPosition(HistogramConstant.BASIC_DISPLACEMENT_FACTOR * -1);
				break;
			case SWT.ARROW_RIGHT:
				moveWindowPosition(HistogramConstant.BASIC_DISPLACEMENT_FACTOR);
				break;
			default:
				break;
		}
	}
	
	/**
	 * Function that is called when a key is released.<p>
	 * Possible actions : 
	 * - Shift  : turn off "fast move" mode.
	 * 
	 * @param event  The KeyEvent generated when the key was pressed.
	 */
	public void keyReleased(KeyEvent event) {
		switch (event.keyCode) {
			case SWT.SHIFT:
				isShiftPressed = false;
				break;
			default:
				break;
		}
	}
	
	/**
	 * Function to move the window position of a given displacemnt.<p>
	 * 
	 * @param displacementFactor	The basic displacement to perform (positive or negative value)
	 */
	public void moveWindowPosition(Integer displacementFactor) {
		
		// If we are in "fast move mode", multiply the basic displacement by a factor
		if ( isShiftPressed == true ) {
			displacementFactor = (int)((double)displacementFactor * HistogramConstant.FAST_DISPLACEMENT_MULTIPLE);
		}
		
		parentCanvas.moveWindow(displacementFactor);
	}
	
}
