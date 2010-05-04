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
import org.eclipse.swt.graphics.Rectangle;

/**
 * <b><u>HistogramCanvasPaintListener</u></b>
 * <p>
 * Implementation of a PaintListener for the specific need of the ParentHistogramCanvas.
 * <p> 
 * The difference with the default one is that this one take a content that is the power of 2 higher 
 * 		than the display size.<p>
 * 
 * When it is time to draw, it takes the closest power of 2 smaller than the canvas size; it is then easy to 
 * 	  	concatenate the interval as they are both power of 2.<p>
 * The difference between the power of 2 and the not-power-of-2 canvas size is then filled by drawing bar that are
 * 		slightly larger every (power/canvasSize) interval.<p> 
 */
public class ParentHistogramCanvasPaintListener extends HistogramCanvasPaintListener 
{
	public ParentHistogramCanvasPaintListener(HistogramCanvas newParentCanvas) {
		super(newParentCanvas);
	}
	
	
	// *** VERIFY ***
	// Is it good to put this synchronized?
	//
	/**
	 * Draw the histogram bars in the canvas.<p>
	 * This drawing function expect the content to be the power of 2 higher than the canvas size.
	 * The bars size will be slightly dynamic to fill the gap between the power and the canvas size.<p>
	 * 
	 * Note : This draw function is somewhat heavier than the default one.
	 * 
	 * @param event The generated paint event when redraw is called.
	 */
	@Override
	public synchronized void drawHistogram(PaintEvent event) {
		HistogramContent tmpContent = parentCanvas.getHistogramContent();
		int tmpBarWidth = tmpContent.getBarsWidth();
		int canvasSize = event.width;
		
		event.gc.setBackground(event.display.getSystemColor(HistogramConstant.HISTOGRAM_BARS_COLOR));
		
		// Calculate the closest power of 2 just smaller than the canvas size
		int closestPowerToCanvas = (int)Math.pow(2, Math.floor( Math.log( canvasSize ) / Math.log(2.0) ));
		
		// Make sure the canvas didn't change size, it which case we need to recalculate our heights
		recalculateHeightIfCanvasSizeChanged();
		
		// Calculate the factor of difference between canvas and the power
		double factor = (double)canvasSize / (double)closestPowerToCanvas;
		// Calculate how many interval will need to be concatenated into one pixel
		int intervalDifference = (tmpContent.getNbElement() / closestPowerToCanvas)*tmpBarWidth;
		
		// This keep a link between the position in "power" and the pixel we draw 
		// I.e. correlation between position in the power ("fake" pixels) and the position in the canvas ("real" pixels)
		// So if pos == 30 and factor == 1.5, we know that the pixel that draw this pos is (30 * 1.5) == 45
		int posInPower = 0;
		int widthFilled = 0;
		
		// Read from 0 up to the currently ready position
		// We advance by "intervalDifference" as the bars migth not represent 1 interval only
		for( int contentPos=0; contentPos < tmpContent.getReadyUpToPosition(); contentPos += intervalDifference ) {
			// Width of the current item. 
			// Vary because of the difference between the power of 2 and the canvas size
			// Ex: if power == 1024 and canvas == 1500, a bars every (1024/1500) will have a size of 2 instead of 1.
			int itemWidth = (int)( Math.ceil((double)(posInPower+1)*factor) - Math.ceil((double)posInPower*factor) );
			itemWidth = itemWidth*tmpBarWidth;
			
			// Concatenate all the element in the interval
			// Ex : if power == 1024 and content == 2048, every (2048/1024)*bars_width will be concatenated 
	    	int thisElementHeight = 0;
	    	for ( int concatPos=0; concatPos<intervalDifference; concatPos++) {
	    		int updatedPos = contentPos + concatPos;
	    		// Make sure we don't cross the last element available.
	    		if ( updatedPos < tmpContent.getReadyUpToPosition() ) {
	    			thisElementHeight += tmpContent.getElementByIndex(contentPos + concatPos).intervalHeight;
	    		}
	    	}
	    	
	    	// *** NOTE *** 
			// Y Position in a canvas is REVERSED, so "0" is on top of the screen and "MAX" is on bottom.
			// Not very instinctive, isn't it?
	    	
	    	// Draw our rectangle 
    		Rectangle rect = new Rectangle(widthFilled, event.height - thisElementHeight, itemWidth, thisElementHeight);
    		event.gc.fillRectangle(rect);
    		
    		// Keep in a variable how much width we filld so far
    		widthFilled += itemWidth;
    		// Keep a correlation between fake_pixel -> real_pixel, 
    		//	this is used to calculate the width of each element 
    		posInPower++;
	    }
	    
		// Clear the remaining space in the canvas (there should not be any) so it appears clean.
	    event.gc.setBackground(event.display.getSystemColor(HistogramConstant.EMPTY_BACKGROUND_COLOR));
	    Rectangle rect = new Rectangle(widthFilled, 0, event.width, event.height);
	    event.gc.fillRectangle(rect);
	}
	
	/*
	 * The function will make sure that the "max difference average" factor is still the same as before;
	 * 		if not, the heigth of the events will be recalculated.<p>
	 * 
	 * The factor might change if the canvas is resized by a big factor.<p>
	 */
	protected void recalculateHeightIfCanvasSizeChanged() {
		HistogramContent tmpContent = parentCanvas.getHistogramContent();
		// We need to ajust the "maxDifferenceToAverageFactor" as the bars we draw might be slitghly larger than the value asked
		// Each "interval" are concatenated when draw so the worst case should be : 
		// contentSize / (closest power of 2 to canvasMaxSize)
		// Ex : if canvasSize is 1500 -> (2048 / 1024) == 2  so maxDiff should be twice larger
		//
		// His is set in the create content of the canvas, but we need to recalculate it 
		//	here because the window might have been resized!
		int exp = (int)Math.floor( Math.log( (double)tmpContent.getCanvasWindowSize() ) / Math.log(2.0) );
		int contentSize = (int)Math.pow(2, exp);
		double maxBarsDiffFactor = ((double)tmpContent.getNbElement() / (double)contentSize );
		
		if ( maxBarsDiffFactor != tmpContent.getMaxDifferenceToAverageFactor() ) {
			// The factor changed! That's unfortunate because it will take a while to recalculate.
			tmpContent.setMaxDifferenceToAverageFactor(maxBarsDiffFactor);
			tmpContent.recalculateHeightFactor();
			tmpContent.recalculateEventHeight();
		}
	}
	
}
