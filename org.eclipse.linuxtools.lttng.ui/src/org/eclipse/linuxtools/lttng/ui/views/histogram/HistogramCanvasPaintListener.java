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

public class HistogramCanvasPaintListener implements PaintListener {
	private HistogramContent histogramContent = null;
	private HistogramSelectedWindow selectedWindow = null;
	
	private int columnWidth = 0;
	private int columnHeight = 0;
	
	public HistogramCanvasPaintListener(HistogramCanvas parentCanvas) {
		histogramContent = parentCanvas.getHistogramContent();
	}
	
	public void paintControl(PaintEvent e) {
		
		clearDrawingSection(e);
		
		if ( (histogramContent == null) || (histogramContent.getReadyUpToPosition() == 0) ) {
			return;
		}
		
		if ( (e.height != columnHeight) && (columnHeight != 0) ) {
			columnHeight = e.height;
		}
		
		drawHistogram(e);
		
		if ( (selectedWindow != null) && (selectedWindow.getSelectedWindowVisible() == true) ) {
			drawSelectedWindow(e);
		}
	}
	
	public void clearDrawingSection(PaintEvent e) {
		e.gc.setForeground(e.display.getSystemColor(HistogramConstant.EMPTY_BACKGROUND_COLOR));
		e.gc.setBackground(e.display.getSystemColor(HistogramConstant.EMPTY_BACKGROUND_COLOR));
		Rectangle allSection = new Rectangle(0, 0, e.width, e.height);
		e.gc.fillRectangle(allSection);
		e.gc.drawRectangle(allSection);
	}
	
	// *** VERIFY ***
	// Is it good to put this synchronized?
	//
	public synchronized void drawHistogram(PaintEvent e) {
	    e.gc.setForeground(e.display.getSystemColor(HistogramConstant.EMPTY_BACKGROUND_COLOR));
		Rectangle allSection = new Rectangle(0, 0, histogramContent.getReadyUpToPosition()*columnWidth, e.height);
		e.gc.fillRectangle(allSection);
		e.gc.drawRectangle(allSection);
		
	    e.gc.setBackground(e.display.getSystemColor(HistogramConstant.HISTOGRAM_BARS_COLOR));
	    for ( int x=0; x<histogramContent.getReadyUpToPosition(); x++) {
	    	Rectangle rect = new Rectangle(columnWidth*x, columnHeight - histogramContent.getElementByIndex(x).intervalHeight, columnWidth, histogramContent.getElementByIndex(x).intervalHeight);
			
	    	e.gc.fillRectangle(rect);
	    }
	    
	    e.gc.setBackground(e.display.getSystemColor(HistogramConstant.EMPTY_BACKGROUND_COLOR));
	    Rectangle rect = new Rectangle(columnWidth*histogramContent.getNbElement(), 0, e.width, columnHeight);
		e.gc.fillRectangle(rect);
	}
	
	public void drawSelectedWindow(PaintEvent e) {
		
		e.gc.setForeground(e.display.getSystemColor(HistogramConstant.SELECTION_WINDOW_COLOR));
	    e.gc.setBackground(e.display.getSystemColor(HistogramConstant.SELECTION_WINDOW_COLOR));
		
		e.gc.setLineWidth(HistogramConstant.SELECTION_LINE_WIDTH);
	    
		int positionCenter = selectedWindow.getWindowCenterXPosition();
		int positionLeft = selectedWindow.getWindowPositionLeft();
		int positionRight = selectedWindow.getWindowPositionRight();
		
		if ( (positionRight - positionLeft) < HistogramConstant.MINIMUM_WINDOW_WIDTH ) {
			positionLeft  = positionCenter - (HistogramConstant.MINIMUM_WINDOW_WIDTH/2);
			positionRight = positionCenter + (HistogramConstant.MINIMUM_WINDOW_WIDTH/2);
		}
		
		e.gc.drawLine(positionLeft , 0       , positionLeft , e.height);
	    e.gc.drawLine(positionLeft , e.height, positionRight, e.height);
	    e.gc.drawLine(positionRight, e.height, positionRight, 0);
	    e.gc.drawLine(positionLeft , 0       , positionRight, 0);
	    
	    
	    e.gc.drawLine(positionCenter + HistogramConstant.SELECTION_CROSSHAIR_LENGTH, e.height/2, positionCenter - HistogramConstant.SELECTION_CROSSHAIR_LENGTH, e.height/2);
	    e.gc.drawLine(positionCenter, (e.height/2) + HistogramConstant.SELECTION_CROSSHAIR_LENGTH, positionCenter, (e.height/2) - HistogramConstant.SELECTION_CROSSHAIR_LENGTH);
	}
	
	
	public HistogramContent getHistogramContent() {
		return histogramContent;
	}
	
	public void setHistogramContent(HistogramContent newhistogramContent) {
		this.histogramContent = newhistogramContent;
	}
	
	public int getColumnWidth() {
		return columnWidth;
	}
	
	public void setColumnWidth(int newcolumnWidth) {
		this.columnWidth = newcolumnWidth;
	}
	
	public int getColumnHeight() {
		return columnHeight;
	}
	
	public void setColumnHeight(int newcolumnHeight) {
		this.columnHeight = newcolumnHeight;
	}

	public void setSelectedWindow(HistogramSelectedWindow newSelectedWindow) {
		this.selectedWindow = newSelectedWindow;
	}

	public HistogramSelectedWindow getSelectedWindow() {
		return selectedWindow;
	}
	
}
