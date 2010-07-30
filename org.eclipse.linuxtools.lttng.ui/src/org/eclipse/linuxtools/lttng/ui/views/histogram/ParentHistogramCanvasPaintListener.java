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
 * 2010-07-16 Yuriy Vashchuk - Base class simplification. Redraw bug correction.
 * 							   Double Buffering implementation.
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.views.histogram;

import org.eclipse.swt.events.PaintEvent;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

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
	private static ParentHistogramCanvas  parentCanvas = null;

	/**
	 * ParentHistogramCanvasPaintListener constructor
	 * 
	 * @param newCanvas Related canvas
	 */	
	public ParentHistogramCanvasPaintListener(ParentHistogramCanvas newCanvas) {
		parentCanvas = newCanvas;
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
	public synchronized void drawHistogram(GC imageGC, Image image) {
		final HistogramContent tmpContent = parentCanvas.getHistogramContent();
		final int tmpBarWidth = tmpContent.getBarsWidth();
		
		imageGC.setBackground(parentCanvas.getDisplay().getSystemColor(HistogramConstant.HISTOGRAM_BARS_COLOR));
		
		// Calculate the closest power of 2 just smaller than the canvas size
		final int closestPowerToCanvas = (int)Math.pow(2, Math.floor( Math.log( image.getBounds().width ) / Math.log(2.0) ));
		
		// Make sure the canvas didn't change size, it which case we need to recalculate our heights
		recalculateHeightIfCanvasSizeChanged();
		
		// Calculate the factor of difference between canvas and the power
		final double factor = (double)image.getBounds().width / (double)closestPowerToCanvas;
		// Calculate how many interval will need to be concatenated into one pixel
		final int intervalDifference = (tmpContent.getNbElement() / closestPowerToCanvas)*tmpBarWidth;
		
		// This keep a link between the position in "power" and the pixel we draw 
		// I.e. correlation between position in the power ("fake" pixels) and the position in the canvas ("real" pixels)
		// So if pos == 30 and factor == 1.5, we know that the pixel that draw this pos is (30 * 1.5) == 45
		int posInPower = 0;
		int widthFilled = 0;
		
		// Read from 0 up to the currently ready position
		// We advance by "intervalDifference" as the bars migth not represent 1 interval only
		int itemWidth = 0;
		int thisElementHeight = 0;
		for( int contentPos=0; contentPos < tmpContent.getReadyUpToPosition(); contentPos += intervalDifference ) {
			// Width of the current item. 
			// Vary because of the difference between the power of 2 and the canvas size
			// Ex: if power == 1024 and canvas == 1500, a bars every (1024/1500) will have a size of 2 instead of 1.
			itemWidth = (int)( Math.ceil((double)(posInPower+1)*factor) - Math.ceil((double)posInPower*factor) );
			itemWidth = itemWidth * tmpBarWidth;
			
			// Concatenate all the element in the interval
			// Ex : if power == 1024 and content == 2048, every (2048/1024)*bars_width will be concatenated 
	    	thisElementHeight = 0;
	    	for ( int concatPos=0; concatPos<intervalDifference; concatPos++) {
	    		final int updatedPos = contentPos + concatPos;
	    		// Make sure we don't cross the last element available.
	    		if ( updatedPos < tmpContent.getReadyUpToPosition() ) {
	    			thisElementHeight += tmpContent.getElementByIndex(contentPos + concatPos).intervalHeight;
	    		}
	    	}
	    	
	    	// *** NOTE *** 
			// Y Position in a canvas is REVERSED, so "0" is on top of the screen and "MAX" is on bottom.
			// Not very instinctive, isn't it?
	    	
	    	// Draw our rectangle 
    		imageGC.fillRectangle(
    				widthFilled,
    				image.getBounds().height - thisElementHeight,
    				itemWidth,
    				thisElementHeight
    				);
    		
    		// Keep in a variable how much width we filld so far
    		widthFilled += itemWidth;
    		// Keep a correlation between fake_pixel -> real_pixel, 
    		//	this is used to calculate the width of each element 
    		posInPower++;
	    }
	}
	
	/*
	 * The function will make sure that the "max difference average" factor is still the same as before;
	 * 		if not, the heigth of the events will be recalculated.<p>
	 * 
	 * The factor might change if the canvas is resized by a big factor.<p>
	 */
	protected void recalculateHeightIfCanvasSizeChanged() {
		final HistogramContent tmpContent = parentCanvas.getHistogramContent();
		// We need to ajust the "maxDifferenceToAverageFactor" as the bars we draw might be slitghly larger than the value asked
		// Each "interval" are concatenated when draw so the worst case should be : 
		// contentSize / (closest power of 2 to canvasMaxSize)
		// Ex : if canvasSize is 1500 -> (2048 / 1024) == 2  so maxDiff should be twice larger
		//
		// His is set in the create content of the canvas, but we need to recalculate it 
		//	here because the window might have been resized!
		final int exp = (int)Math.floor( Math.log( (double)tmpContent.getCanvasWindowSize() ) / Math.log(2.0) );
		final int contentSize = (int)Math.pow(2, exp);
		final double maxBarsDiffFactor = ((double)tmpContent.getNbElement() / (double)contentSize );
		
		// Floating point comparaison : 
		//	We consider it is different if the difference is greater than 10^-3
		if ( Math.abs(maxBarsDiffFactor - tmpContent.getMaxDifferenceToAverageFactor()) > 0.001 ) {
			// The factor changed! That's unfortunate because it will take a while to recalculate.
			tmpContent.setMaxDifferenceToAverageFactor(maxBarsDiffFactor);
			tmpContent.recalculateHeightFactor();
			tmpContent.recalculateEventHeight();
		}
	}
	
	/**
	 * Function called when the canvas need to redraw.<p>
	 * 
	 * @param event  The generated paint event when redraw is called.
	 */
	@Override
	public void paintControl(PaintEvent event) {
		
		if (parentCanvas.getSize().x > 0 && parentCanvas.getSize().y > 0) {
			Image image = (Image) parentCanvas.getData("double-buffer-image");
			
			// Creates new image only absolutely necessary.
			if (image == null
					|| image.getBounds().width != parentCanvas.getBounds().width
					|| image.getBounds().height != parentCanvas.getBounds().height) {

				image =	new Image(
						event.display,
						parentCanvas.getBounds().width,
						parentCanvas.getBounds().height
						);

				parentCanvas.setData("double-buffer-image", image);
			}
			
			// Initializes the graphics context of the image. 
	        GC imageGC = new GC(image);
	        
			// First clear the whole canvas to have a clean section where to draw
			clearDrawingSection(imageGC, image, parentCanvas);
	        
			// If the content is null or has rady to draw we quit the function here
			if ( (parentCanvas.getHistogramContent() != null) && (parentCanvas.getHistogramContent().getReadyUpToPosition() != 0) ) {
				// Call the function that draw the bars
				drawHistogram(imageGC, image);
				
				// If we have a selected window set to visible, call the function to draw it
				if ( (parentCanvas.getCurrentWindow() != null) && (parentCanvas.getCurrentWindow().getSelectedWindowVisible()) ) {
					drawSelectedWindow(
							imageGC,
							image
							);
				}

				// Draws the buffer image onto the canvas. 
				event.gc.drawImage(image, 0, 0);
			}

			imageGC.dispose();
		}
		
	}

	/**
	 * Draw the selection window in the canvas.<p>
	 * This draw a square around the selected section with a crosshair in the middle.
	 * The square cannot be smaller than "MINIMUM_WINDOW_WIDTH"
	 * 
	 * @param imageGC GC content.
	 * @param image Image content.
	 */
	public void drawSelectedWindow(GC imageGC, Image image) {
		// Get the window position... this would fail if the window is not initialized yet
		final int positionCenter = parentCanvas.getCurrentWindow().getWindowXPositionCenter();
		final int positionLeft = parentCanvas.getCurrentWindow().getWindowXPositionLeft();
		final int positionRight = parentCanvas.getCurrentWindow().getWindowXPositionRight();
		
		final int imageHeight = image.getBounds().height;
		
		// Draw the selection window square
		// Attributes (color and width) of the lines
		imageGC.setForeground(parentCanvas.getDisplay().getSystemColor(HistogramConstant.SELECTION_WINDOW_COLOR));
		imageGC.setLineWidth(HistogramConstant.SELECTION_LINE_WIDTH);
		imageGC.drawLine(positionLeft , 0       	 , positionLeft , imageHeight);
		imageGC.drawLine(positionLeft , imageHeight, positionRight, imageHeight);
		imageGC.drawLine(positionRight, imageHeight, positionRight, 0);
		imageGC.drawLine(positionLeft , 0       	 , positionRight, 0);	

		// Draw the crosshair section
		imageGC.setBackground(parentCanvas.getDisplay().getSystemColor(HistogramConstant.SELECTION_WINDOW_COLOR));
		imageGC.fillOval(
				positionCenter,
				imageHeight / 2,
				HistogramConstant.SELECTION_CROSSHAIR_LENGTH,
				HistogramConstant.SELECTION_CROSSHAIR_LENGTH
				);
	}
	
}
