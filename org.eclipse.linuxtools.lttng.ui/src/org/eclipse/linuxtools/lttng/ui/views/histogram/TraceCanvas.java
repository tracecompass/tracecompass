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

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class TraceCanvas extends Canvas implements MouseMoveListener, MouseListener, KeyListener ,FocusListener
{
	private HistogramContent histogramContent = null;
	private TraceCanvasPaintListener paintListener = null;
	
	private int columnWidth = 0;
	private int columnMaxHeight = 0;
	
	private boolean isWindowMoving = false;
	private HistogramSelectedWindow currentWindow = null;
	
	public TraceCanvas(Composite parent, int style, int widthPerColumn, int columnHeight) {
		super (parent, style);
		
		columnWidth = widthPerColumn;
		columnMaxHeight = columnHeight;
		
		addNeededListeners();
	}
	
	public void addNeededListeners() {
		paintListener = new TraceCanvasPaintListener(getHistogramContent(), getColumnWidth(), getColumnMaxHeight() );
		
		this.addPaintListener( paintListener );
		this.addMouseListener(this);
		this.addMouseMoveListener(this);
		this.addKeyListener(this);
		this.addFocusListener(this);
	}
	
	public void createNewHistogramContent(long windowSize, double maxBarsDifferenceToAverage) {
		histogramContent = new HistogramContent( getSize().x / columnWidth, getSize().x, columnMaxHeight, maxBarsDifferenceToAverage);
		
		// *** FIXME ***
		// paintlistener need to know about the new content...
		// This is nowhere near elegant, change me.
		paintListener.setHistogramContent(histogramContent);
		
		// New selected window, not visible by default
		currentWindow = createNewSelectedWindow(windowSize);
	}
	
	
	public HistogramSelectedWindow createNewSelectedWindow(long windowTimeDuration) {
		
		HistogramSelectedWindow returnedWindow = new HistogramSelectedWindow(histogramContent);
		returnedWindow.setWindowTimeWidth(windowTimeDuration);
		returnedWindow.setWindowCenterXPosition(0);
		
		setCurrentWindow( returnedWindow );
		
		return returnedWindow;
	}
	
	public Long getSelectedWindowSize() {
		return currentWindow.getWindowTimeWidth();
	}
	
	public void setSelectedWindowSize(Long newSelectedWindowSize) {
		currentWindow.setWindowTimeWidth(newSelectedWindowSize);
	}
	
	public HistogramSelectedWindow getCurrentWindow() {
		return currentWindow;
	}
	
	public void setCurrentWindow(HistogramSelectedWindow newCurrentWindow) {
		this.currentWindow = newCurrentWindow;
		paintListener.setSelectedWindow(newCurrentWindow);
	}

	
	public boolean positionMouseCursor(MouseEvent e) {
		
		boolean returnedValue = false;
		
		if ( histogramContent.getIntervalTime() != 0 ) {
			
			int centralPos = e.x;
			
			if ( centralPos < 0 ) {
				centralPos = 0;
			}
			else if ( centralPos > getParent().getSize().x ) {
				centralPos = getParent().getSize().x;
			}
			
			currentWindow.setWindowCenterXPosition(centralPos);
			
			returnedValue = true;
			redraw();
		}
		
		return returnedValue;
	}
	
	public void notifyTimeWindowChanged() {
		// Nothing to do yet, this function is a place holder
	}
	

	public HistogramContent getHistogramContent() {
		return histogramContent;
	}
	
	public int getColumnWidth() {
		return columnWidth;
	}
	
	public int getColumnMaxHeight() {
		return columnMaxHeight;
	}
	
	public void mouseDown(MouseEvent e) {
		isWindowMoving = true;
		
		positionMouseCursor(e);
	}
	
	public void mouseUp(MouseEvent e) {
		if ( positionMouseCursor(e) ) {
			notifyTimeWindowChanged();
		}
		
		isWindowMoving = false;
	}
	
	public void mouseMove(MouseEvent e) {
		if ( isWindowMoving == true ) {
			positionMouseCursor(e);
		}
	}
	
	public void mouseDoubleClick(MouseEvent e) {
		System.out.println("mouseDoubleClick");
	}
	
	public void focusGained(FocusEvent e) {
		System.out.println("focusGained");
	}
	
	public void focusLost(FocusEvent e) {
		System.out.println("focusLost");
	}
	
	public void keyPressed(KeyEvent e) {
		System.out.println("Key " + e.character + " (" + e.keyCode + ")" + " was pressed.");
	}
	
	public void keyReleased(KeyEvent e) {
		System.out.println("Key " + e.character + " (" + e.keyCode + ")" + " was released.");
	}
}
