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

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;

/**
 * <b><u>HistogramCanvasPaintListener</u></b>
 * <p>
 * Implementation of a PaintListener for the need of the HistogramCanvas
 * <p> 
 */
public class HistogramCanvasPaintListener implements PaintListener 
{
	private HistogramCanvas  parentCanvas = null;
	private HistogramContent histogramContent = null;
	private HistogramSelectedWindow selectedWindow = null;
	
	private int barsWidth = 0;
	
	/**
	 * HistogramCanvasPaintListener constructor
	 * 
	 * @param parentCanvas Related canvas
	 */
	public HistogramCanvasPaintListener(HistogramCanvas newParentCanvas) {
		parentCanvas = newParentCanvas;
		histogramContent = parentCanvas.getHistogramContent();
	}
	
	/**
	 * Function called when the canvas need to redraw.<p>
	 * 
	 * @param event  The generated paint event when redraw is called.
	 */
	public void paintControl(PaintEvent event) {
		
		// First clear the whole canvas to have a clean section where to draw
		clearDrawingSection(event);
		
		// If the content is null or has rady to draw we quit the function here
		if ( (histogramContent == null) || (histogramContent.getReadyUpToPosition() == 0) ) {
			return;
		}
		
		// Call the function that draw the bars
		drawHistogram(event);
		
		// Pinpoint a position if set
		if (histogramContent.getSelectedEventTimeInWindow() > 0 ) {
			drawSelectedEventInWindow(event);
		}
		
		// If we have a selected window set to visible, call the function to draw it
		if ( (selectedWindow != null) && (selectedWindow.getSelectedWindowVisible() == true) ) {
			drawSelectedWindow(event);
		}
	}
	
	/**
	 * Clear the drawing section of the canvas<p>
	 * This paint the whole background in EMPTY_BACKGROUND_COLOR, so we have something clean to draw on.
	 * 
	 * @param event The generated paint event when redraw is called.
	 */
	public void clearDrawingSection(PaintEvent event) {
		event.gc.setForeground(event.display.getSystemColor(HistogramConstant.EMPTY_BACKGROUND_COLOR));
		event.gc.setBackground(event.display.getSystemColor(HistogramConstant.EMPTY_BACKGROUND_COLOR));
		Rectangle allSection = new Rectangle(0, 0, event.width, event.height);
		event.gc.fillRectangle(allSection);
		event.gc.drawRectangle(allSection);
	}
	
	
	// *** VERIFY ***
	// Is it good to put this synchronized?
	//
	/**
	 * Draw the histogram bars in the canvas.<p>
	 * Use existing elements in HistogramContent to draw bars on the cancas; 
	 * 	the element table in content need to be populated and have consistent value.  
	 * 
	 * @param event The generated paint event when redraw is called.
	 */
	public synchronized void drawHistogram(PaintEvent event) {
		// This will be the color for all the bars that wil be draw below.
		event.gc.setBackground(event.display.getSystemColor(HistogramConstant.HISTOGRAM_BARS_COLOR));
		
		// *** NOTE *** 
		// Y Position in a canvas is REVERSED, so "0" is on top of the screen and "MAX" is on bottom.
		// Not very instinctive, isn't it?
		
		// Draw a bar from the left (pos X=0) until the pos=(NbBars*barWidth). If space is left, it will be blanked after.
	    for ( int x=0; x<histogramContent.getReadyUpToPosition(); x++) {
    		Rectangle rect = new Rectangle(barsWidth*x, event.height - histogramContent.getElementByIndex(x).intervalHeight, barsWidth, histogramContent.getElementByIndex(x).intervalHeight);
    		event.gc.fillRectangle(rect);
	    }
	    
	    // Clear the remaining space in the canvas (if any) so it appears clean.
	    event.gc.setBackground(event.display.getSystemColor(HistogramConstant.EMPTY_BACKGROUND_COLOR));
	    Rectangle rect = new Rectangle(barsWidth*histogramContent.getNbElement(), 0, event.width, event.height);
	    event.gc.fillRectangle(rect);
	}
	
	/**
	 * Draw a certain event selected in the window.<p>
	 * 
	 * @param event The generated paint event when redraw is called.
	 */
	public synchronized void drawSelectedEventInWindow(PaintEvent event) {
		// This will be the color for all the bars that wil be draw below.
		event.gc.setBackground(event.display.getSystemColor(HistogramConstant.SELECTED_EVENT_COLOR));
		
		int position = histogramContent.getClosestXPositionFromTimestamp(histogramContent.getSelectedEventTimeInWindow());
		
		Rectangle rect = new Rectangle(barsWidth*position, 0, barsWidth, event.height);
		event.gc.fillRectangle(rect);
	}
	
	/**
	 * Draw the selection window in the canvas.<p>
	 * This draw a square ober the selected section with a crosshair in the middle.
	 * The square cannot be smaller than "MINIMUM_WINDOW_WIDTH"
	 * 
	 * @param event The generated paint event when redraw is called.
	 */
	public void drawSelectedWindow(PaintEvent event) {
		// Attributes (color and width) of the lines
		event.gc.setForeground(event.display.getSystemColor(HistogramConstant.SELECTION_WINDOW_COLOR));
		event.gc.setLineWidth(HistogramConstant.SELECTION_LINE_WIDTH);
	    
		// Get the window position... this would fail if the window is not initialized yet
		int positionCenter = selectedWindow.getWindowXPositionCenter();
		int positionLeft = selectedWindow.getWindowXPositionLeft();
		int positionRight = selectedWindow.getWindowXPositionRight();
		
		// Minimal size verification.
		if ( (positionRight - positionLeft) < HistogramConstant.MINIMUM_WINDOW_WIDTH ) {
			positionLeft  = positionCenter - (HistogramConstant.MINIMUM_WINDOW_WIDTH/2);
			positionRight = positionCenter + (HistogramConstant.MINIMUM_WINDOW_WIDTH/2);
		}
		
		// Draw the selection window square
		event.gc.drawLine(positionLeft , 0       	 , positionLeft , event.height);
		event.gc.drawLine(positionLeft , event.height, positionRight, event.height);
		event.gc.drawLine(positionRight, event.height, positionRight, 0);
		event.gc.drawLine(positionLeft , 0       	 , positionRight, 0);
	    
		// Draw the crosshair section
		event.gc.drawLine(positionCenter + HistogramConstant.SELECTION_CROSSHAIR_LENGTH, event.height/2, positionCenter - HistogramConstant.SELECTION_CROSSHAIR_LENGTH, event.height/2);
		event.gc.drawLine(positionCenter, (event.height/2) + HistogramConstant.SELECTION_CROSSHAIR_LENGTH, positionCenter, (event.height/2) - HistogramConstant.SELECTION_CROSSHAIR_LENGTH);
	}
	
	/**
	 * Getter for the histogram content used by this paint listener.
	 * 
	 * @return Histogram content currently tied to this paint listener
	 */
	public HistogramContent getHistogramContent() {
		return histogramContent;
	}
	
	/**
	 * Setter for the histogram content used by this paint listener.<p>
	 * The new content will be displayed upon the next redraw (assuming the content is populated).
	 * 
	 * @return Histogram content currently tied to this paint listener
	 */
	public void setHistogramContent(HistogramContent newhistogramContent) {
		this.histogramContent = newhistogramContent;
	}
	
	/**
	 * Getter for the bars witdh of this paint listener.
	 * 
	 * @return Bars width we will use during draw. 
	 */
	public int getBarWidth() {
		return barsWidth;
	}
	
	/**
	 * Getter for the bars witdh of this paint listener.<p>
	 * NOTE : This MUST be set before any draw is done.
	 * 
	 * @param newBarsWidth	The new width to use for the bars
	 */
	public void setBarWidth(int newBarsWidth) {
		this.barsWidth = newBarsWidth;
	}
	
	/**
	 * Getter for the selection window used by this paint listener
	 * 
	 * @return The selection window tied to this paint listener
	 */
	public HistogramSelectedWindow getSelectedWindow() {
		return selectedWindow;
	}
	
	/**
	 * Setter for the selection window used by this paint listener.<p>
	 * NOTE : a null selectedWindow or a selectedWindow with visible == false will be ignored (i.e. : not draw).
	 * 
	 * @param newSelectedWindow	The new selection window
	 */
	public void setSelectedWindow(HistogramSelectedWindow newSelectedWindow) {
		this.selectedWindow = newSelectedWindow;
	}
	
}
