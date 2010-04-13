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
	protected AsyncCanvasRedrawer 	canvasRedrawer 	 = null;
	protected HistogramContent 		histogramContent = null;
	
	protected HistogramCanvasPaintListener 		paintListener = null;
	protected HistogramCanvasMouseListener 		mouseListener = null;
	protected HistogramCanvasKeyListener 		keyListener   = null;
	protected HistogramCanvasFocusListener  	focusListener = null;
	
	protected HistogramSelectedWindow currentWindow = null;
	
	
	/**
	 * HistogramCanvas constructor
	 * 
	 * @param parent 		Composite control which will be the parent of the new instance (cannot be null)
	 * @param 				Style the style of control to construct
	 */
	public HistogramCanvas(Composite parent, Integer style) {
		super(parent, style);
		addNeededListeners();
	}
	
	/*
	 * Create the needed "event listeners" and hook them to the Canvas.
	 * 
	 * @see org.eclipse.linuxtools.lttng.ui.views.histogram.HistogramCanvasPaintListener
	 * @see org.eclipse.linuxtools.lttng.ui.views.histogram.HistogramCanvasMouseListener
	 * @see org.eclipse.linuxtools.lttng.ui.views.histogram.HistogramCanvasKeyListener
	 * @see org.eclipse.linuxtools.lttng.ui.views.histogram.HistogramCanvasFocusListener
	 */
	private void addNeededListeners() {
		// AsyncCanvasRedrawer is an internal class
		// This is used to redraw the canvas without danger from a different thread
		canvasRedrawer = new AsyncCanvasRedrawer(this);
		
		paintListener = new HistogramCanvasPaintListener(this);
		mouseListener = new HistogramCanvasMouseListener(this);
		keyListener   = new HistogramCanvasKeyListener(this);
		focusListener = new HistogramCanvasFocusListener(this);
		
		this.addPaintListener( paintListener );
		this.addMouseListener(mouseListener);
		this.addMouseMoveListener(mouseListener);
		this.addMouseWheelListener(mouseListener);
		this.addKeyListener(keyListener);
		this.addFocusListener(focusListener);
	}
	
	/**
	 * Create a new HistogramContent for this HistogramCanvas<p>
	 * A new <I>empty</I> canvas will then be created.
	 * 
	 * IMPORTANT NOTE : Canvas size, bar width and bar height need to be known at this point, as these dimension are used to create a content 
	 * 						of the correct size. This function should be recalled if the canvas size change.
	 * 
	 * NOTE 		  : The selection windows won't be draw until its visible field is set to true. 
	 * 
	 * @param windowSize					Size of the selection window. Set to something very small (0?) if not needed.
	 * @param widthPerBar					Width of the histogram "bars"
	 * @param barsHeight   					Height of the histogram "bars"
	 * @param maxBarsDifferenceToAverage	Factor used to "chop" bars that are too tall. Set to something big (100.0?) if not needed.
	 */
	public void createNewHistogramContent(Long windowSize, Integer widthPerBar, Integer barsHeight, Double maxBarsDifferenceToAverage) {
		histogramContent = new HistogramContent( getSize().x / widthPerBar, getSize().x, barsHeight, maxBarsDifferenceToAverage);
		
		// *** FIXME ***
		// paintlistener need to know about the new content...
		// This is nowhere near elegant, change me.
		paintListener.setHistogramContent(histogramContent);
		paintListener.setBarWidth(widthPerBar);
		
		// New selected window, not visible by default
		createNewSelectedWindow(windowSize);
	}
	
	/**
	 * Create a new selection window of the size (time width) given.<p>
	 * The window initial position is at X = 0.
	 * The window is created hidden, it won't be draw unless it is set to visible.<p> 
	 * 
	 * @param windowTimeDuration	Time width (in nanosecond) of the window.
	 */
	public void createNewSelectedWindow(Long windowTimeDuration) {
		currentWindow = new HistogramSelectedWindow(histogramContent);
		
		currentWindow.setWindowTimeWidth(windowTimeDuration);
		currentWindow.setWindowXPositionCenter(0);
		
		// Warn the paint listener about the new window
		paintListener.setSelectedWindow(currentWindow);
	}
	
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
	public HistogramSelectedWindow getCurrentWindow() {
		return currentWindow;
	}
	
	/**
	 * Getter for the selection window width<p>
	 * 
	 * @return Time width (in nanosecond) of the selection window.
	 */
	public Long getSelectedWindowSize() {
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
	public void setSelectedWindowSize(Long newSelectedWindowSize) {
		
		if ( newSelectedWindowSize < histogramContent.getIntervalTime() ) {
			newSelectedWindowSize = histogramContent.getIntervalTime();
		}
		
		else if ( newSelectedWindowSize > (2*histogramContent.getCompleteTimeInterval()) ) {
			newSelectedWindowSize = (2*histogramContent.getCompleteTimeInterval());
		}
		
		currentWindow.setWindowTimeWidth(newSelectedWindowSize);
	}
	
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
	public void notifyParentSelectionWindowChangedAsynchronously() {
		// Create a new redrawer in case it doesn't exist yet (we never know with thread!)
		if ( canvasRedrawer == null ) {
			canvasRedrawer = new AsyncCanvasRedrawer(this);
		}
		
		canvasRedrawer.asynchronousNotifyParentSelectionWindowChanged();
	}
	
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
	public void moveWindow(Integer newRelativeXPosition) {
		// Nothing : function is a place holder
	}
	
	/**
	 * Function that is called when the selection window is re-centered.<p>
	 * Note: Given position should be absolute to the window and need to be the selection window center.
	 * 
	 * <B>METHOD INTENDED TO BE EXTENDED</B>
	 * 
	 * @param newRelativeXPosition	New absolute position.
	 */
	public void centerWindow(Integer newAbsoluteXPosition) {
		// Nothing : function is a place holder
	}
	
	/**
	 * Function that is called when the selection window size (time width) changed by an absolute time.<p>
	 * Note: Given time should be in nanoseconds, positive.
	 * 
	 * <B>METHOD INTENDED TO BE EXTENDED</B>
	 * 
	 * @param newTime	 New absoulte time (in nanoseconds) to apply to the window.
	 */
	public void resizeWindowByAbsoluteTime(Long newTime) {
		// Nothing : function is a place holder
	}
	
	/**
	 * Function that is called to tell the parent that the selection window changed.<p>
	 * 
	 * <B>METHOD INTENDED TO BE EXTENDED</B>
	 * 
	 */
	public void notifyParentSelectionWindowChanged() {
		// Nothing : function is a place holder
	}
	
	/**
	 * Function that is called to tell the parent that some information changed.<p>
	 * 
	 * <B>METHOD INTENDED TO BE EXTENDED</B>
	 * 
	 */
	public void notifyParentUpdatedInformation() {
		// Nothing : function is a place holder
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
		Display display = parentCanvas.getDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				parentCanvas.redraw();
			}
		});
	}
	
	/**
	 * Function to asynchonously notify the parent of the related canvas that the window changed.<p>
	 * 
	 * Basically, it just run "notifyParentSelectionWindowChanged()" in asyncExec.
	 * 
	 */
	public void asynchronousNotifyParentSelectionWindowChanged() {
		Display display = parentCanvas.getDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				parentCanvas.notifyParentSelectionWindowChanged();
			}
		});
	}
	
	/**
	 * Function to asynchonously notify the parent of the related canvas that information changed.<p>
	 * 
	 * Basically, it just run "notifyParentUpdatedInformation()" in asyncExec.
	 * 
	 */
	public void asynchronousNotifyParentUpdatedInformation() {
		Display display = parentCanvas.getDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				parentCanvas.notifyParentUpdatedInformation();
			}
		});
	}
}
