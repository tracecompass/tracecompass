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
 * 2010-07-16 Yuriy Vashchuk - Heritage corrections. Redraw bug correction.
 * 							   Double Buffering implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.histogram;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

/**
 * <b><u>HistogramCanvasPaintListener</u></b>
 * <p>
 * Implementation of a PaintListener for the need of the HistogramCanvas
 * <p> 
 */
public class HistogramCanvasPaintListener implements PaintListener 
{
	private static ChildrenHistogramCanvas childrenCanvas = null;
	protected boolean isFinished = false;
	
	/**
	 * HistogramCanvasPaintListener default constructor
	 */	
	public HistogramCanvasPaintListener() {
	}
	
	/**
	 * HistogramCanvasPaintListener constructor
	 * 
	 * @param parentCanvas Related canvas
	 */
	public HistogramCanvasPaintListener(ChildrenHistogramCanvas newCanvas) {
		childrenCanvas = newCanvas;
	}
	
	/**
	 * Function called when the canvas need to redraw.<p>
	 * 
	 * @param event  The generated paint event when redraw is called.
	 */
	private final String DATA_KEY = "double-buffer-image"; //$NON-NLS-1$
	@Override
	public void paintControl(PaintEvent event) {

		if (childrenCanvas.getSize().x > 0 && childrenCanvas.getSize().y > 0) {
			Image image = (Image) childrenCanvas.getData(DATA_KEY);
			
			// Creates new image only absolutely necessary.
			if (image == null
					|| image.getBounds().width != childrenCanvas.getBounds().width
					|| image.getBounds().height != childrenCanvas.getBounds().height) {

				image =	new Image(
						event.display,
						childrenCanvas.getBounds().width,
						childrenCanvas.getBounds().height
						);

				childrenCanvas.setData(DATA_KEY, image);
			}
			
			// Initializes the graphics context of the image. 
	        GC imageGC = new GC(image);
	        
			// First clear the whole canvas to have a clean section where to draw
			clearDrawingSection(imageGC, image, childrenCanvas);
	        
			// If the content is null or has rady to draw we quit the function here
			if ( (childrenCanvas.getHistogramContent() != null)
					&& (childrenCanvas.getHistogramContent().getReadyUpToPosition() != 0) ) {
				
				// Call the function that draw the bars
//				if (!isFinished) {
					drawHistogram(imageGC, image);
//				}
				
				// Pinpoint a position if set
				if (childrenCanvas.getHistogramContent().getSelectedEventTimeInWindow() > 0 ) {
					drawSelectedEventInWindow(imageGC, image);
				}

				// Draws the buffer image onto the canvas. 
				event.gc.drawImage(image, 0, 0);
			}

			imageGC.dispose();
		}
	}
	
	/**
	 * Clear the drawing section of the canvas<p>
	 * This paint the whole background in EMPTY_BACKGROUND_COLOR, so we have something clean to draw on.
	 * 
	 * @param imageGC GC content.
	 * @param image Image content.
	 * @param ourCanvas Canvas to clean.
	 */
	public void clearDrawingSection(GC imageGC, Image image, HistogramCanvas ourCanvas) {
		// Fills background. 
		imageGC.setBackground(ourCanvas.getDisplay().getSystemColor(HistogramConstant.EMPTY_BACKGROUND_COLOR));
        imageGC.fillRectangle(0, 0, image.getBounds().width + 1, image.getBounds().height + 1);		
	}
	
	// *** VERIFY ***
	// Is it good to put this synchronized?
	//
	/**
	 * Draw the histogram bars in the canvas.<p>
	 * Use existing elements in HistogramContent to draw bars on the cancas; 
	 * 	the element table in content need to be populated and have consistent value.  
	 * 
	 * @param imageGC GC content.
	 * @param image image content.
	 */
	public synchronized void drawHistogram(GC imageGC, Image image) {
		
		// This will be the bottom color for all the bars that wil be draw below.
		imageGC.setBackground( new Color( imageGC.getDevice(), 74, 112, 139) );
		
		// *** NOTE *** 
		// Y Position in a canvas is REVERSED, so "0" is on top of the screen and "MAX" is on bottom.
		// Not very instinctive, isn't it?

		// Draw a bar from the left (pos X=0) until the pos=(NbBars*barWidth). If space is left, it will be blanked after.
	    for ( int x = 0; x < childrenCanvas.getHistogramContent().getReadyUpToPosition(); x++) {
	    	imageGC.fillRectangle(
	    			childrenCanvas.getHistogramContent().getBarsWidth() * x,
	    			image.getBounds().height - childrenCanvas.getHistogramContent().getElementByIndex(x).intervalHeight,
    				childrenCanvas.getHistogramContent().getBarsWidth(),
    				childrenCanvas.getHistogramContent().getElementByIndex(x).intervalHeight
    				);
	    }
		
	}
	
	/**
	 * Draw a certain event selected in the window.<p>
	 * 
	 * @param imageGC GC content.
	 * @param image image content.
	 */
	public synchronized void drawSelectedEventInWindow(GC imageGC, Image image) {
		
		final HistogramContent tmpContent = childrenCanvas.getHistogramContent();
		final int tmpBarWidth = tmpContent.getBarsWidth();
		final int position = tmpContent.getClosestXPositionFromTimestamp(tmpContent.getSelectedEventTimeInWindow());
		
		// This will be the color for all the bars that will be draw below.
		imageGC.setForeground(childrenCanvas.getDisplay().getSystemColor(HistogramConstant.SELECTED_EVENT_COLOR));
		imageGC.setLineWidth(HistogramConstant.SELECTION_LINE_WIDTH);
		imageGC.drawLine(
				tmpBarWidth * position,
				0,
				tmpBarWidth * position,
				image.getBounds().height
				);
		}
	
	/**
	 * @param isFinished the flag value
	 */
	public void setIsFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}		
}
