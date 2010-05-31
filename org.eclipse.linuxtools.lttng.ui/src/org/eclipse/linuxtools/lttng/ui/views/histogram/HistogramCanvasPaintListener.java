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
	protected HistogramCanvas  parentCanvas = null;
	
	/**
	 * HistogramCanvasPaintListener constructor
	 * 
	 * @param parentCanvas Related canvas
	 */
	public HistogramCanvasPaintListener(HistogramCanvas newParentCanvas) {
		parentCanvas = newParentCanvas;
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
		if ( (parentCanvas.getHistogramContent() == null) || (parentCanvas.getHistogramContent().getReadyUpToPosition() == 0) ) {
			return;
		}
		
		// Call the function that draw the bars
		drawHistogram(event);
		
		// Pinpoint a position if set
		if (parentCanvas.getHistogramContent().getSelectedEventTimeInWindow() > 0 ) {
			drawSelectedEventInWindow(event);
		}
		
		// If we have a selected window set to visible, call the function to draw it
		if ( (parentCanvas.getCurrentWindow()  != null) && (parentCanvas.getCurrentWindow().getSelectedWindowVisible() == true) ) {
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
		HistogramContent tmpContent = parentCanvas.getHistogramContent();
		int tmpBarWidth = tmpContent.getBarsWidth();
		
		// This will be the color for all the bars that wil be draw below.
		event.gc.setBackground(event.display.getSystemColor(HistogramConstant.HISTOGRAM_BARS_COLOR));
		
		// *** NOTE *** 
		// Y Position in a canvas is REVERSED, so "0" is on top of the screen and "MAX" is on bottom.
		// Not very instinctive, isn't it?
		
		// Draw a bar from the left (pos X=0) until the pos=(NbBars*barWidth). If space is left, it will be blanked after.
	    for ( int x=0; x<tmpContent.getReadyUpToPosition(); x++) {
    		Rectangle rect = new Rectangle(tmpBarWidth*x, event.height - tmpContent.getElementByIndex(x).intervalHeight, tmpBarWidth, tmpContent.getElementByIndex(x).intervalHeight);
    		event.gc.fillRectangle(rect);
	    }
	    
	    // Clear the remaining space in the canvas (if any) so it appears clean.
	    event.gc.setBackground(event.display.getSystemColor(HistogramConstant.EMPTY_BACKGROUND_COLOR));
	    Rectangle rect = new Rectangle(tmpBarWidth*tmpContent.getNbElement(), 0, event.width, event.height);
	    event.gc.fillRectangle(rect);
	}
	
	/**
	 * Draw a certain event selected in the window.<p>
	 * 
	 * @param event The generated paint event when redraw is called.
	 */
	public synchronized void drawSelectedEventInWindow(PaintEvent event) {
		HistogramContent tmpContent = parentCanvas.getHistogramContent();
		int tmpBarWidth = tmpContent.getBarsWidth();
		
		// This will be the color for all the bars that wil be draw below.
		event.gc.setBackground(event.display.getSystemColor(HistogramConstant.SELECTED_EVENT_COLOR));
		
		int position = tmpContent.getClosestXPositionFromTimestamp(tmpContent.getSelectedEventTimeInWindow());
		
		Rectangle rect = new Rectangle(tmpBarWidth*position, 0, tmpBarWidth, event.height);
		event.gc.fillRectangle(rect);
	}
	
	/**
	 * Draw the selection window in the canvas.<p>
	 * This draw a square around the selected section with a crosshair in the middle.
	 * The square cannot be smaller than "MINIMUM_WINDOW_WIDTH"
	 * 
	 * @param event The generated paint event when redraw is called.
	 */
	public void drawSelectedWindow(PaintEvent event) {
		HistogramSelectedWindow tmpWindow = parentCanvas.getCurrentWindow();
		
		// Attributes (color and width) of the lines
		event.gc.setForeground(event.display.getSystemColor(HistogramConstant.SELECTION_WINDOW_COLOR));
		event.gc.setLineWidth(HistogramConstant.SELECTION_LINE_WIDTH);
	    
		// Get the window position... this would fail if the window is not initialized yet
		int positionCenter = tmpWindow.getWindowXPositionCenter();
		int positionLeft = tmpWindow.getWindowXPositionLeft();
		int positionRight = tmpWindow.getWindowXPositionRight();
		
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
	
}
