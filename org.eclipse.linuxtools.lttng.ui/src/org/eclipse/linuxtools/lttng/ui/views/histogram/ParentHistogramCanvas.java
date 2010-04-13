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
 * <b><u>ParentHistogramCanvas</u></b>
 * <p>
 * Extended implementation of the HistogramCanvas.
 * <p>
 * This canvas goal is to display the "SelectionWindow" in details. 
 */
public class ParentHistogramCanvas extends HistogramCanvas {
	
	private HistogramView parentHistogramWindow = null; 
	
	/**
	 * ParentHistogramCanvas constructor.<p>
	 * Same as HistogramCanvas, but receive a parent HistogramView that we can call from here.
	 * 
	 * @param parent 		Composite control which will be the parent of the new instance (cannot be null)
	 * @param 				Style the style of control to construct
	 */
	public ParentHistogramCanvas(HistogramView newParentWindow, Composite parent, int style) {
		super(parent, style);
		
		parentHistogramWindow = newParentWindow;
	}
	
	/**
	 * Function that is called when the selection window is moved.<p>
	 * Note: Given position should be relative to the previous (centered) absolute position.<p>
	 * 
	 * Calculate the new position then re-center the window.<p> 
	 * It will also notify the HistogramView that the window changed. 
	 * 
	 * @param newRelativeXPosition	New position relative to the last known absolute position.
	 */
	@Override
	public void moveWindow(int newRelativeXPosition) {
		int absolutePosition = currentWindow.getWindowXPositionCenter() + newRelativeXPosition;
		
		centerWindow(absolutePosition);
		notifyParentSelectionWindowChangedAsynchronously();
	}
	
	/**
	 * Function that is called when the selection window is re-centered.<p>
	 * Note: Given position should be absolute to the window and need to be the selection window center.<p>
	 * 
	 * Recenter the window and notify the HistogramView that the window changed. 
	 * 
	 * @param newRelativeXPosition	New absolute position.
	 */
	@Override
	public void centerWindow(int newAbsoluteXPosition) {
		
		if ( newAbsoluteXPosition < 0 ) {
			newAbsoluteXPosition = 0;
		}
		else if ( newAbsoluteXPosition > getParent().getSize().x ) {
			newAbsoluteXPosition = getParent().getSize().x;
		}
		
		if ( newAbsoluteXPosition != currentWindow.getWindowXPositionCenter() ) {
			currentWindow.setWindowXPositionCenter(newAbsoluteXPosition);
			redrawAsynchronously();
		}
	}
	
	/**
	 * Function that is called when the selection window size (time width) changed by an absolute time.<p>
	 * Note: Given time should be in nanoseconds, positive.
	 * 
	 * Set the new window size and notify the HistogramView that the window changed.
	 * 
	 * @param newTime	 New absoulte time (in nanoseconds) to apply to the window.
	 */
	@Override
	public void resizeWindowByAbsoluteTime(long newTime) {
		if ( newTime != getSelectedWindowSize() ) {
			setSelectedWindowSize(newTime);
			
			notifyParentSelectionWindowChangedAsynchronously();
			redrawAsynchronously();
		}
	}
	
	/**
	 * Notify the parent HistogramView that we have updated information.<p>
	 * This is intended to be called at the end of the request when we know we have up-to-date information.
	 */
	@Override
	public void notifyParentUpdatedInformation() {
		parentHistogramWindow.updateFullTraceInformation();
	}
	
	/**
	 * Notify the parent HistogramView that the SelectionWindow changed.<p>
	 * This is intended to be called when the window move or is resized.
	 */
	@Override
	public void notifyParentSelectionWindowChanged() {
		// Notify the parent view that something changed
		parentHistogramWindow.windowChangedNotification();
	}
}
