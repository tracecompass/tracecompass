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
 * 2010-07-16 Yuriy Vashchuk - Histogram class simplification.
 * 							   Selection Window related methods has been
 * 							   implemented in Parent Histogram.
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.histogram;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * <b><u>HistogramCanvas</u></b>
 * <p>
 * Canvas implementation aimed to draw histograms.
 * <p>
 * This canvas goal is to display certain "HistogramContent" onto an histogram.<p>
 * Several method exist to extend it so it should suit most needs. 
 */
public class HistogramCanvas extends Canvas
{
	private static HistogramView histogramView = null; 

	protected AsyncCanvasRedrawer 	canvasRedrawer 	 = null;
	protected HistogramContent 		histogramContent = null;
	
/*	
	// 2010-07-16 Yuriy: Moved to child classes.
 	protected HistogramCanvasPaintListener 		paintListener = null;
	protected HistogramCanvasMouseListener 		mouseListener = null;
	protected HistogramCanvasKeyListener 		keyListener   = null;
	protected HistogramCanvasControlListener	controlListener = null;
*/
	protected HistogramCanvasFocusListener  	focusListener = null;
	
/*	
	// 2010-07-16 Yuriy: Moved to parent histogram class.
	protected HistogramSelectedWindow currentWindow = null;
*/	
	
	/**
	 * HistogramCanvas constructor
	 * 
	 * @param parent 		Composite control which will be the parent of the new instance (cannot be null)
	 * @param 				Style the style of control to construct
	 */
	public HistogramCanvas(HistogramView histogramView, Composite parent, int style) {
		super(parent, style);
		HistogramCanvas.histogramView = histogramView;
		addNeededListeners();
		
/*
		// 2010-06-20 Yuriy: Moved to parent hitogram class.
		// New selected window, not visible by default
		createNewSelectedWindow(0L);
*/		
	}
	
	/*
	 * Create the needed "event listeners" and hook them to the Canvas.
	 */

	protected void addNeededListeners() {
		createAndAddCanvasRedrawer();
		createAndAddFocusListener();
		
/*
		// 2010-06-20 Yuriy: Moved to derived classes.
		createAndAddPaintListener();
		createAndAddMouseListener();
		createAndAddKeyListener();
		createAndAddControlListener();
*/		
	}
	
	/*
	 * Create a canvas redrawer and bind it to this canvas.<p>
	 * 
	 * Note : AsyncCanvasRedrawer is an internal class
	 * 	This is used to redraw the canvas from a different thread 
	 * 		without^H^H^H with less danger.
	 */
	protected void createAndAddCanvasRedrawer() {
		canvasRedrawer = new AsyncCanvasRedrawer(this);
	}

	/*
	 * Create a histogram paint listener and bind it to this canvas.<p>
	 * 
	 * @see org.eclipse.linuxtools.lttng.ui.views.histogram.HistogramCanvasPaintListener
	 */
/*	
	// 2010-07-16 Yuriy: Moved to derived classes.
	protected void createAndAddPaintListener() {
		paintListener = new HistogramCanvasPaintListener(this);
		this.addPaintListener( paintListener );
	}
*/	
	/*
	 * Create a histogram mouse listener and bind it to this canvas.<p>
	 * Note : this mouse listener handle the mouse, the move and the wheel at once.
	 * 
	 * @see org.eclipse.linuxtools.lttng.ui.views.histogram.HistogramCanvasMouseListener
	 */
/*
	// 2010-07-16 Yuriy: Moved to parent histogram class
	protected void createAndAddMouseListener() {
		mouseListener = new HistogramCanvasMouseListener(this);
		this.addMouseListener(mouseListener);
		this.addMouseMoveListener(mouseListener);
		this.addMouseWheelListener(mouseListener);
	}
*/	
	
	/*
	 * Create a histogram key listener and bind it to this canvas.<p>
	 * 
	 * @see org.eclipse.linuxtools.lttng.ui.views.histogram.HistogramCanvasKeyListener
	 */
/*	
	// 2010-07-16 Yuriy: Moved to parent histogram class
	protected void createAndAddKeyListener() {
		keyListener   = new HistogramCanvasKeyListener(this);
		this.addKeyListener(keyListener);
	}
*/	
	/*
	 * Create a histogram focus listener and bind it to this canvas.<p>
	 * 
	 *  @see org.eclipse.linuxtools.lttng.ui.views.histogram.HistogramCanvasFocusListener
	 */
	protected void createAndAddFocusListener() {
		focusListener = new HistogramCanvasFocusListener(this);
		this.addFocusListener(focusListener);
	}

	/*
	 * Create a histogram control listener and bind it to this canvas.<p>
	 * 
	 *  @see org.eclipse.linuxtools.lttng.ui.views.histogram.HistogramCanvasControlListener
	 */
/*
	// 2010-07-16 Yuriy: Moved to derived classes.
	protected void createAndAddControlListener() {
		controlListener = new HistogramCanvasControlListener(this);
		this.addControlListener(controlListener);
	}
*/	
	/**
	 * Create a new HistogramContent for this HistogramCanvas<p>
	 * A new <I>empty</I> content will then be created.
	 * 
	 * IMPORTANT NOTE : Canvas size, bar width and bar height need to be known at this point, as these dimension are used to create a content 
	 * 						of the correct size.
	 * 
	 * @param canvasSize					Size of the parent canvas.
	 * @param widthPerBar					Width of the histogram "bars"
	 * @param barsHeight   					Height of the histogram "bars"
	 * @param maxBarsDifferenceToAverage	Factor used to "chop" bars that are too tall. Set to something big (100.0?) if not needed.
	 */
	public void createNewHistogramContent(int canvasSize, int widthPerBar, int barsHeight, double maxBarsDifferenceToAverage) {
		histogramContent = new HistogramContent( canvasSize / widthPerBar, canvasSize, widthPerBar, barsHeight, maxBarsDifferenceToAverage);
	}
	
	/**
	 * Create a new selection window of the size (time width) given.<p>
	 * The window initial position is at X = 0.
	 * The window is created hidden, it won't be draw unless it is set to visible.<p> 
	 * 
	 * @param windowTimeDuration	Time width (in nanosecond) of the window.
	 */
/*	
	// 2010-07-16 Yuriy: Moved to parent histogram class.
	public void createNewSelectedWindow(long windowTimeDuration) {
		currentWindow = new HistogramSelectedWindow(histogramContent);
		
		currentWindow.setWindowTimeWidth(windowTimeDuration);
		currentWindow.setWindowXPositionCenter(0);
	}
*/	
	public HistogramContent getHistogramContent() {
		return histogramContent;
	}
	
	/**
	 * Getter for the selection window<p>
	 * 
	 * @return the current selection window
	 * 
	 * @see org.eclipse.linuxtools.lttng.ui.views.histogram.HistogramSelectedWindow
	 */
/*	
	// 2010-07-16 Yuriy: Moved to parent histogram class.
	public HistogramSelectedWindow getCurrentWindow() {
		return currentWindow;
	}
*/
	
	/**
	 * Getter for the selection window width<p>
	 * 
	 * @return Time width (in nanosecond) of the selection window.
	 */
/*	
	// 2010-07-16 Yuriy: Moved to parent histogram class.
	public long getSelectedWindowSize() {
		return currentWindow.getWindowTimeWidth();
	}
*/
	
	/**
	 * Setter for the selection window width<p>
	 * The window size will be ajusted if it does not respect one of these constraints :
	 * - The window size cannot be smaller than a single histogram content interval.<p>
	 * - The window size cannot be larger than twice the histogram content complete time interval.<p>
	 * 
	 * @param newSelectedWindowSize	New time width (in nanosecond) of the selection window.
	 */
/*	
	// 2010-07-16 Yuriy: Moved to parent histogram class.
	public void setSelectedWindowSize(long newSelectedWindowSize) {
		
		if ( newSelectedWindowSize <= 0 ) {
			newSelectedWindowSize = 1L;
		}
		else if ( newSelectedWindowSize > (2*histogramContent.getCompleteTimeInterval()) ) {
			newSelectedWindowSize = (2*histogramContent.getCompleteTimeInterval());
		}
		
		currentWindow.setWindowTimeWidth(newSelectedWindowSize);
	}
*/	
	/**
	 * Method to call the "Asynchronous redrawer" for this canvas<p>
	 * This allow safe redraw from different threads.
	 * 
	 */
	public void redrawAsynchronously() {
		// Create a new redrawer in case it doesn't exist yet (we never know with thread!)
		if ( canvasRedrawer == null ) {
			canvasRedrawer = new AsyncCanvasRedrawer(this);
		}
		
		canvasRedrawer.asynchronousRedraw();
	}
	
	/**
	 * Method to call the "Asynchronous NotifyParentSelectionWindowChanged" for this canvas<p>
	 * This allow safe update UI objects from different threads.
	 * 
	 */
/*
	// 2010-07-16 Yuriy: Moved to parent histogram class.
	public void notifyParentSelectionWindowChangedAsynchronously() {
		// Create a new redrawer in case it doesn't exist yet (we never know with thread!)
		if ( canvasRedrawer == null ) {
			canvasRedrawer = new AsyncCanvasRedrawer(this);
		}
		
		canvasRedrawer.asynchronousNotifyParentSelectionWindowChanged();
	}
*/	
	
	/**
	 * Method to call the "Asynchronous NotifyParentUpdatedInformation" for this canvas<p>
	 * This allow safe redraw from different threads.
	 * 
	 */
	public void notifyParentUpdatedInformationAsynchronously() {
		// Create a new redrawer in case it doesn't exist yet (we never know with thread!)
		if ( canvasRedrawer == null ) {
			canvasRedrawer = new AsyncCanvasRedrawer(this);
		}
		
		canvasRedrawer.asynchronousNotifyParentUpdatedInformation();
	}
	
	/**
	 * Function that is called when the selection window is moved.<p>
	 * Note: Given position should be relative to the previous (centered) absolute position.
	 * 
	 * <B>METHOD INTENDED TO BE EXTENDED</B>
	 * 
	 * @param newRelativeXPosition	New position relative to the last known absolute position.
	 */
/*
 	// 2010-07-16 Yuriy: Moved to parent histogram class.
	public void moveWindow(int newRelativeXPosition) {
		// Nothing : function is a place holder
	}
*/

	/**
	 * Function that is called when the selection window is re-centered.<p>
	 * Note: Given position should be absolute to the window and need to be the selection window center.
	 * 
	 * <B>METHOD INTENDED TO BE EXTENDED</B>
	 * 
	 * @param newRelativeXPosition	New absolute position.
	 */
/*
 	// 2010-07-16 Yuriy: Moved to parent histogram class.
	public void setWindowCenterPosition(int newAbsoluteXPosition) {
		// Nothing : function is a place holder
	}
*/
	
	/**
	 * Function that is called when the selection window size (time width) changed by an absolute time.<p>
	 * Note: Given time should be in nanoseconds, positive.
	 * 
	 * <B>METHOD INTENDED TO BE EXTENDED</B>
	 * 
	 * @param newTime	 New absoulte time (in nanoseconds) to apply to the window.
	 */
/*
/*
 	// 2010-07-16 Yuriy: Moved to parent histogram class.
	public void resizeWindowByAbsoluteTime(long newTime) {
		// Nothing : function is a place holder
	}
*/	
	/**
	 * Function that is called to tell the parent that the selection window changed.<p>
	 * 
	 * <B>METHOD INTENDED TO BE EXTENDED</B>
	 * 
	 */
/*
 	// 2010-07-16 Yuriy: Moved to parent histogram class.
	public void notifyParentSelectionWindowChanged() {
		// Nothing : function is a place holder
	}
*/	
	/**
	 * Function that is called to tell the parent that some information changed.<p>
	 * 
	 * <B>METHOD INTENDED TO BE EXTENDED</B>
	 * 
	 */
	public void notifyParentUpdatedInformation() {
		// Nothing : function is a place holder
	}
	
	/**
	 * Getter for View
	 * 
	 * @return view instance
	 * 
	 */
	public static HistogramView getHistogramView() {
		return histogramView;
	}

	/**
	 * Setter for View
	 * 
	 * @param histogramView reference to object
	 */
	public static void setHistogramView(HistogramView histogramView) {
		HistogramCanvas.histogramView = histogramView;
	}
}


/**
 * <b><u>AsyncCanvasRedrawer Inner Class</u></b>
 * <p>
 * Asynchronous redrawer for the HistogramCanvas
 * <p>
 * This class role is to call method that update the UI on asynchronously. 
 * This should prevent any "invalid thread access" exception when trying to update UI from a different thread.
 */
class AsyncCanvasRedrawer {
	
	private HistogramCanvas parentCanvas = null; 
	
	/**
	 * AsyncCanvasRedrawer constructor.
	 * 
	 * @param newCanvas 	Related histogram canvas.
	 */
	public AsyncCanvasRedrawer(HistogramCanvas newCanvas) {
		parentCanvas = newCanvas;
	}
	
	/**
	 * Function to redraw the related canvas asynchonously.<p>
	 * 
	 * Basically, it just run "canvas.redraw()" in asyncExec.
	 * 
	 */
	public void asynchronousRedraw() {
		if (parentCanvas != null) {
			Display display = parentCanvas.getDisplay();
			display.asyncExec(new Runnable() {
				public void run() {
					parentCanvas.redraw();
				}
			});
		}
	}
	
	/**
	 * Function to asynchonously notify the parent of the related canvas that the window changed.<p>
	 * 
	 * Basically, it just run "notifyParentSelectionWindowChanged()" in asyncExec.
	 * 
	 */
/*	
	// 2010-07-16 Yuriy: Moved to parent histogram class.
	public void asynchronousNotifyParentSelectionWindowChanged() {
		if(parentCanvas != null) {
			Display display = parentCanvas.getDisplay();
			display.asyncExec(new Runnable() {
				public void run() {
					parentCanvas.notifyParentSelectionWindowChanged();
				}
			});
		}
	}
*/	
	
	/**
	 * Function to asynchonously notify the parent of the related canvas that information changed.<p>
	 * 
	 * Basically, it just run "notifyParentUpdatedInformation()" in asyncExec.
	 * 
	 */
	public void asynchronousNotifyParentUpdatedInformation() {
		if(parentCanvas != null) {
			Display display = parentCanvas.getDisplay();
			display.asyncExec(new Runnable() {
				public void run() {
					parentCanvas.notifyParentUpdatedInformation();
				}
			});
		}
	}
}
