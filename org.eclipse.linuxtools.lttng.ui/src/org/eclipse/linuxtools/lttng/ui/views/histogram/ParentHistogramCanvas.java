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
 * 2010-07-16 Yuriy Vashchuk - Base Histogram class simplification.
 * 							   Selection Window related methods has been
 * 							   implemented here (Parent Histogram).
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.histogram;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * <b><u>ParentHistogramCanvas</u></b>
 * <p>
 * Extended implementation of the HistogramCanvas.
 * <p>
 * This canvas goal is to display the "Full experiment" histogram. 
 */
public class ParentHistogramCanvas extends HistogramCanvas {
	
	private ParentHistogramCanvasPaintListener 		paintListener = null;
	private HistogramCanvasMouseListener 			mouseListener = null;
	private HistogramCanvasKeyListener 				keyListener   = null;
	private ParentHistogramCanvasControlListener	controlListener = null;
	
	private HistogramSelectedWindow currentWindow = null;
	
	/**
	 * ParentHistogramCanvas constructor.<p>
	 * Same as HistogramCanvas, but receive a parent HistogramView that we can call from here.
	 * 
	 * @param parent 		Composite control which will be the parent of the new instance (cannot be null)
	 * @param 				Style the style of control to construct
	 */
	public ParentHistogramCanvas(HistogramView histogramView, Composite parent, int style) {
		super(histogramView, parent, style);
		
		// New selected window, not visible by default
		if (histogramView !=null && HistogramView.getFullExperimentCanvas() != null) {
			createNewSelectedWindow(
					HistogramView.getFullExperimentCanvas().getHistogramContent().getStartTime() + HistogramView.getDEFAULT_WINDOW_SIZE() / 2,
					HistogramView.getDEFAULT_WINDOW_SIZE()
					);
		}
		
		// 2010-06-20 Yuriy: Moved from parent class
		createAndAddPaintListener();
		createAndAddMouseListener();
		createAndAddKeyListener();
		createAndAddControlListener();
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
	private void createAndAddPaintListener() {
		paintListener = new ParentHistogramCanvasPaintListener(this);
		this.addPaintListener( paintListener );
	}

	/*
	 * Create a histogram mouse listener and bind it to this canvas.<p>
	 * Note : this mouse listener handle the mouse, the move and the wheel at once.
	 * 
	 * @see org.eclipse.linuxtools.lttng.ui.views.histogram.HistogramCanvasMouseListener
	 */
	private void createAndAddMouseListener() {
		mouseListener = new HistogramCanvasMouseListener(this);
		this.addMouseListener(mouseListener);
		this.addMouseMoveListener(mouseListener);
		this.addMouseWheelListener(mouseListener);
	}
	
	/*
	 * Create a histogram key listener and bind it to this canvas.<p>
	 * 
	 * @see org.eclipse.linuxtools.lttng.ui.views.histogram.HistogramCanvasKeyListener
	 */
	private void createAndAddKeyListener() {
		keyListener   = new HistogramCanvasKeyListener(this);
		this.addKeyListener(keyListener);
	}	
	
	/*
	 * Create a histogram control listener and bind it to this canvas.<p>
	 * 
	 *  @see org.eclipse.linuxtools.lttng.ui.views.histogram.HistogramCanvasControlListener
	 */
	private void createAndAddControlListener() {
		controlListener = new ParentHistogramCanvasControlListener(this);
		this.addControlListener(controlListener);
	}

	/**
	 * Create a new selection window of the size (time width) given.<p>
	 * The window initial position is at X = 0.
	 * The window is created hidden, it won't be draw unless it is set to visible.<p> 
	 * 
	 * @param windowTimeDuration	Time width (in nanosecond) of the window.
	 */
	public void createNewSelectedWindow(long timestampOfLeftPosition, long windowTimeDuration) {
		currentWindow = new HistogramSelectedWindow(histogramContent, timestampOfLeftPosition, windowTimeDuration);
	}
	
	/**
	 * Getter for the selection window<p>
	 * 
	 * @return the current selection window
	 * 
	 * @see org.eclipse.linuxtools.lttng.ui.views.histogram.HistogramSelectedWindow
	 */
	public HistogramSelectedWindow getCurrentWindow() {
		return currentWindow;
	}
	
	/**
	 * Getter for the selection window width<p>
	 * 
	 * @return Time width (in nanosecond) of the selection window.
	 */
	public long getSelectedWindowSize() {
		return currentWindow.getWindowTimeWidth();
	}
	
	/**
	 * Setter for the selection window width<p>
	 * The window size will be ajusted if it does not respect one of these constraints :
	 * - The window size cannot be smaller than a single histogram content interval.<p>
	 * - The window size cannot be larger than twice the histogram content complete time interval.<p>
	 * 
	 * @param newSelectedWindowSize	New time width (in nanosecond) of the selection window.
	 */
	public void setSelectedWindowSize(long newSelectedWindowSize) {
		
		if ( newSelectedWindowSize <= 0 ) {
			newSelectedWindowSize = 1L;
		}
		else if ( newSelectedWindowSize > (2*histogramContent.getCompleteTimeInterval()) ) {
			newSelectedWindowSize = (2*histogramContent.getCompleteTimeInterval());
		}
		
		currentWindow.setWindowTimeWidth(newSelectedWindowSize);
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
	public void setWindowCenterPosition(int newAbsoluteXPosition) {
		
		// We will check if the coordinate the same
		if ( newAbsoluteXPosition != currentWindow.getWindowXPositionCenter() ) {

			long timestampOfLeftPosition = this.getHistogramContent().getClosestElementFromXPosition( newAbsoluteXPosition ).firstIntervalTimestamp - currentWindow.getWindowTimeWidth() / 2;
			long timestampOfCenterPosition = 0;
			long timestampOfRightPosition = 0;
			
			// Let's do the border verifications
			if ( timestampOfLeftPosition < histogramContent.getStartTime() ) {

				timestampOfLeftPosition = histogramContent.getStartTime();
				timestampOfCenterPosition = timestampOfLeftPosition + currentWindow.getWindowTimeWidth() / 2;
				timestampOfRightPosition = timestampOfLeftPosition + currentWindow.getWindowTimeWidth();

			} else {
			
				timestampOfRightPosition = this.getHistogramContent().getClosestElementFromXPosition( newAbsoluteXPosition ).firstIntervalTimestamp + currentWindow.getWindowTimeWidth() / 2;
				
				if ( timestampOfRightPosition > histogramContent.getEndTime() ) {
					
					timestampOfRightPosition = histogramContent.getEndTime();
					timestampOfCenterPosition = timestampOfRightPosition - currentWindow.getWindowTimeWidth() / 2;
					timestampOfLeftPosition = timestampOfRightPosition - currentWindow.getWindowTimeWidth();
					
				} else {
					
					timestampOfCenterPosition = this.getHistogramContent().getClosestElementFromXPosition( newAbsoluteXPosition ).firstIntervalTimestamp;
					
				}
				
			}
		
			// We will do the update in case of different center timestamp
			if( timestampOfCenterPosition != currentWindow.getTimestampOfCenterPosition() ) {
				// Firstly we will setup new left, right and center timestamps
				currentWindow.setTimestampOfLeftPosition( timestampOfLeftPosition );
				currentWindow.setTimestampOfCenterPosition( timestampOfCenterPosition );
				currentWindow.setTimestampOfRightPosition( timestampOfRightPosition );
	
				// After we will update coordonates using timestamps already recalculated
				currentWindow.setWindowXPositionLeft( histogramContent.getClosestXPositionFromTimestamp(timestampOfLeftPosition) );
				currentWindow.setWindowXPositionCenter( histogramContent.getClosestXPositionFromTimestamp(timestampOfCenterPosition) );
				currentWindow.setWindowXPositionRight( histogramContent.getClosestXPositionFromTimestamp(timestampOfRightPosition) );
				
				redrawAsynchronously();
			}
		}
	}

	/**
	 * Function that is called when the selection window is re-centered.<p>
	 * Note: Given position should be timestamp in the experiment timerange<p>
	 * 
	 * Recenter the window and notify the HistogramView that the window changed. 
	 * 
	 * @param timestampOfCenterPosition	New timestamp of center position.
	 */
	public void setWindowCenterPosition(long timestampOfCenterPosition) {
		
		// We will check if the coordinate the same
		if ( timestampOfCenterPosition != currentWindow.getTimestampOfCenterPosition() ) {

			long timestampOfLeft = timestampOfCenterPosition - currentWindow.getWindowTimeWidth() / 2;
			long timestampOfCenter = 0;
			long timestampOfRight = 0;

			int windowXPositionLeft = histogramContent.getClosestXPositionFromTimestamp(timestampOfLeft);
			int windowXPositionCenter = 0;
			int windowXPositionRight = 0;
			
			// Let's do the border verifications
			if ( timestampOfLeft < histogramContent.getStartTime() ) {
				
				timestampOfLeft = histogramContent.getStartTime();
				timestampOfCenter = timestampOfLeft + currentWindow.getWindowTimeWidth() / 2;
				timestampOfRight = timestampOfLeft + currentWindow.getWindowTimeWidth();

				windowXPositionLeft = histogramContent.getClosestXPositionFromTimestamp(timestampOfLeft);
				windowXPositionCenter = histogramContent.getClosestXPositionFromTimestamp(timestampOfCenter); 
				windowXPositionRight = histogramContent.getClosestXPositionFromTimestamp(timestampOfRight);

			} else {
			
				timestampOfRight = timestampOfCenterPosition + currentWindow.getWindowTimeWidth() / 2;
				windowXPositionRight = histogramContent.getClosestXPositionFromTimestamp(timestampOfRight);
				
				if ( windowXPositionRight > histogramContent.getEndTime() ) {

					timestampOfRight = histogramContent.getEndTime();
					timestampOfCenter = timestampOfRight - currentWindow.getWindowTimeWidth() / 2;
					timestampOfLeft = timestampOfRight - currentWindow.getWindowTimeWidth();
					
					windowXPositionLeft = histogramContent.getClosestXPositionFromTimestamp(timestampOfLeft);
					windowXPositionCenter = histogramContent.getClosestXPositionFromTimestamp(timestampOfCenter); 
					windowXPositionRight = histogramContent.getClosestXPositionFromTimestamp(timestampOfRight);
					
				} else {
					
					timestampOfCenter = timestampOfCenterPosition;
					windowXPositionCenter = histogramContent.getClosestXPositionFromTimestamp(timestampOfCenter);
					
				}
				
			}

			// Firstly we will setup new left, right and center timestamps
			currentWindow.setTimestampOfLeftPosition( timestampOfLeft );
			currentWindow.setTimestampOfCenterPosition( timestampOfCenter );
			currentWindow.setTimestampOfRightPosition( timestampOfRight );
			
			// We will do the update in case of different center timestamp
			if( windowXPositionCenter != currentWindow.getWindowXPositionCenter() ) {

				// After we will update coordonates using timestamps already recalculated
				currentWindow.setWindowXPositionLeft(windowXPositionLeft);
				currentWindow.setWindowXPositionCenter(windowXPositionCenter);
				currentWindow.setWindowXPositionRight(windowXPositionRight);
				
				redrawAsynchronously();
			}
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
	public void resizeWindowByAbsoluteTime(long newTime) {
		if ( newTime != getSelectedWindowSize() ) {
			
			resizeWindowByAbsoluteTimeWithoutNotification(newTime);
			
			notifyParentSelectionWindowChangedAsynchronously();
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
	public void resizeWindowByAbsoluteTimeWithoutNotification(long newTime) {
		
		// We will change the size in case of delta (newTime) != 0
		if (newTime != 0 ) { 
			
			if(newTime > getHistogramContent().getEndTime() - getHistogramContent().getStartTime()) {
				newTime = getHistogramContent().getEndTime() - getHistogramContent().getStartTime();
			}
	
			setSelectedWindowSize(newTime);
	
	/*			
			// Yuriy: we can't use this function because we change the left and right coordinates.
			setWindowCenterPosition(currentWindow.getWindowXPositionCenter());
	*/			
	
			long timestampOfLeftPosition = currentWindow.getTimestampOfCenterPosition() - currentWindow.getWindowTimeWidth() / 2;
			long timestampOfCenterPosition = currentWindow.getTimestampOfCenterPosition();
			long timestampOfRightPosition = 0;
				
			// Let's do the border verifications
			if ( timestampOfLeftPosition < histogramContent.getStartTime() ) {
	
				timestampOfLeftPosition = histogramContent.getStartTime();
				timestampOfCenterPosition = timestampOfLeftPosition + currentWindow.getWindowTimeWidth() / 2;
				timestampOfRightPosition = timestampOfLeftPosition + currentWindow.getWindowTimeWidth();
	
			} else {
				
				timestampOfRightPosition = currentWindow.getTimestampOfCenterPosition() + currentWindow.getWindowTimeWidth() / 2;
					
				if ( timestampOfRightPosition > histogramContent.getEndTime() ) {
						
					timestampOfRightPosition = histogramContent.getEndTime();
					timestampOfCenterPosition = timestampOfRightPosition - currentWindow.getWindowTimeWidth() / 2;
					timestampOfLeftPosition = timestampOfRightPosition - currentWindow.getWindowTimeWidth();
						
				}
				
			}
			
			// Firstly we will setup new left, right and center timestamps
			currentWindow.setTimestampOfLeftPosition( timestampOfLeftPosition );
			currentWindow.setTimestampOfCenterPosition( timestampOfCenterPosition );
			currentWindow.setTimestampOfRightPosition( timestampOfRightPosition );
		
			// After we will update coordonates using timestamps already recalculated
			currentWindow.setWindowXPositionLeft( histogramContent.getClosestXPositionFromTimestamp(timestampOfLeftPosition) );
			currentWindow.setWindowXPositionCenter( histogramContent.getClosestXPositionFromTimestamp(timestampOfCenterPosition) );
			currentWindow.setWindowXPositionRight( histogramContent.getClosestXPositionFromTimestamp(timestampOfRightPosition) );
			
			redrawAsynchronously();
		}
	}
	
	/**
	 * Notify the parent HistogramView that we have updated information.<p>
	 * This is intended to be called at the end of the request when we know we have up-to-date information.
	 */
	@Override
	public void notifyParentUpdatedInformation() {
		getHistogramView().updateFullExperimentInformation();
	}
	
	/**
	 * Notify the parent HistogramView that the SelectionWindow changed.<p>
	 * This is intended to be called when the window move or is resized.
	 */
	public void notifyParentSelectionWindowChanged() {
		// Notify the parent view that something changed
		getHistogramView().windowChangedNotification();
		// Send a broadcast to the framework about the window change
		getHistogramView().sendTmfRangeSynchSignalBroadcast();
	}

	/**
	 * Method to call the "Asynchronous NotifyParentSelectionWindowChanged" for this canvas<p>
	 * This allow safe update UI objects from different threads.
	 * 
	 */
	public void notifyParentSelectionWindowChangedAsynchronously() {
		// Create a new redrawer in case it doesn't exist yet (we never know with thread!)
		if ( canvasRedrawer == null ) {
			canvasRedrawer = new AsyncCanvasRedrawer(this);
		}
		
		asynchronousNotifyParentSelectionWindowChanged();
	}	
	
	/**
	 * Function to asynchonously notify the parent of the related canvas that the window changed.<p>
	 * 
	 * Basically, it just run "notifyParentSelectionWindowChanged()" in asyncExec.
	 * 
	 */
	public void asynchronousNotifyParentSelectionWindowChanged() {
		// Ignore update if widget is disposed
		if (this.isDisposed()) return;

		Display display = this.getDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				if(!ParentHistogramCanvas.this.isDisposed())
					notifyParentSelectionWindowChanged();
			}
		});
	}	
}
