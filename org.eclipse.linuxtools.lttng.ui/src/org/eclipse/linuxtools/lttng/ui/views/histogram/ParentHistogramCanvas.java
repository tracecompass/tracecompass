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
 * This canvas goal is to display the "Full experiment" histogram. 
 */
public class ParentHistogramCanvas extends HistogramCanvas {
	
	protected HistogramView parentHistogramWindow = null; 
	
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
	 * Create a new HistogramContent for this HistogramCanvas<p>
	 * A new <I>empty</I> canvas will then be created.
	 * 
	 * IMPORTANT NOTE : This implementaton use the next power of 2 to the full screen resolution as the content size.
	 * 					This allow us to resize the canvas at low cost (i.e. : no need to reissue a full request)
	 * 					We need a "particular" paint listener that know about this.
	 * 
	 * @param canvasSize					Size of the parent canvas.
	 * @param widthPerBar					Width of the histogram "bars"
	 * @param barsHeight   					Height of the histogram "bars"
	 * @param maxBarsDifferenceToAverage	Factor used to "chop" bars that are too tall. Set to something big (100.0?) if not needed.
	 */
	@Override
	public void createNewHistogramContent(int canvasSize, int widthPerBar, int barsHeight, double maxBarsDifferenceToAverage) {
		
		// *** FIXME ***
		// Note there MIGHT be some unhandled case, like if the resolution of the screen change 
		//		or if a new screen is plugged.
		// Let's ignore them for now.
		//
		// The maximum size the canvas could ever had
		int canvasMaxSize = getParent().getDisplay().getBounds().width;
		
		// Calculate the power of two superior to the max size
		int exp = (int)Math.ceil( Math.log( (double)canvasMaxSize ) / Math.log(2.0) );
		int contentSize = (int)Math.pow(2, exp);
		
		// Create the content
		histogramContent = new HistogramContent( contentSize, canvasSize, widthPerBar, barsHeight, maxBarsDifferenceToAverage);
		
		// We need to ajust the "maxDifferenceToAverageFactor" as the bars we draw might be slitghly larger than the value asked
		// Each "interval" are concatenated when draw so the worst case should be : 
		// contentSize / (closest power of 2 to canvasMaxSize)
		// Ex : if canvasSize is 1500 -> (2048 / 1024) == 2  so maxDiff should be twice larger
		//
		// Note : this is not perfect, if the screen is resized after we calculate this, the resulting output can be quite ugly
		// For this reason, this will be recalculated in the paintListener as well. 
		double maxBarsDiffFactor = ((double)contentSize / Math.pow(2, exp-1));
		histogramContent.setMaxDifferenceToAverageFactor(maxBarsDiffFactor);
	}
	
	/*
	 * Create a histogram paint listener and bind it to this canvas.<p>
	 * 
	 * Note : This one is a bit particular, as it is made to draw content that is of a power of 2.
	 * 			The default one draw content that is relative to the real pixels size.
	 */
	@Override
	protected void createAndAddPaintListener() {
		paintListener = new ParentHistogramCanvasPaintListener(this);;
		this.addPaintListener( paintListener );
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
		
		setWindowCenterPosition(absolutePosition);
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
	public void setWindowCenterPosition(int newAbsoluteXPosition) {
		
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
		parentHistogramWindow.updateFullExperimentInformation();
	}
	
	/**
	 * Notify the parent HistogramView that the SelectionWindow changed.<p>
	 * This is intended to be called when the window move or is resized.
	 */
	@Override
	public void notifyParentSelectionWindowChanged() {
		// Notify the parent view that something changed
		parentHistogramWindow.windowChangedNotification();
		// Send a broadcast to the framework about the window change
		parentHistogramWindow.sendTmfRangeSynchSignalBroadcast();
	}
}
