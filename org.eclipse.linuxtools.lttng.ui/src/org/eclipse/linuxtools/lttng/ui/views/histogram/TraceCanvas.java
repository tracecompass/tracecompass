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
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class TraceCanvas extends Canvas implements FocusListener
{
	protected static Long	MINIMUM_WINDOW_SIZE = 1L;
	protected static Double ZOOM_FACTOR = 0.1;
	
	protected asyncCanvasRedrawer canvasRedrawer = null;
	
	protected HistogramContent histogramContent = null;
	protected TraceCanvasPaintListener 	paintListener = null;
	protected TraceCanvasMouseListener 	mouseListener = null;
	protected TraceCanvasKeyListener 	keyListener = null;
	
	protected Integer columnWidth = 0;
	protected Integer columnMaxHeight = 0;
	
	protected HistogramSelectedWindow currentWindow = null;
	
	public TraceCanvas(Composite parent, int style, int widthPerColumn, int columnHeight) {
		super (parent, style);
		
		columnWidth = widthPerColumn;
		columnMaxHeight = columnHeight;
		
		addNeededListeners();
	}
	
	public void addNeededListeners() {
		canvasRedrawer = new asyncCanvasRedrawer(this);
		
		paintListener = new TraceCanvasPaintListener(getHistogramContent(), getColumnWidth(), getColumnMaxHeight() );
		mouseListener = new TraceCanvasMouseListener(this);
		keyListener = new TraceCanvasKeyListener(this);
		
		this.addPaintListener( paintListener );
		this.addMouseListener(mouseListener);
		this.addMouseMoveListener(mouseListener);
		this.addMouseWheelListener(mouseListener);
		this.addKeyListener(keyListener);
		this.addFocusListener(this);
	}
	
	public void createNewHistogramContent(long windowSize, double maxBarsDifferenceToAverage) {
		histogramContent = new HistogramContent( getSize().x / columnWidth, getSize().x, columnMaxHeight, maxBarsDifferenceToAverage);
		
		// *** FIXME ***
		// paintlistener need to know about the new content...
		// This is nowhere near elegant, change me.
		paintListener.setHistogramContent(histogramContent);
		
		// New selected window, not visible by default
		createNewSelectedWindow(windowSize);
	}
	
	
	public HistogramSelectedWindow createNewSelectedWindow(Long windowTimeDuration) {
		HistogramSelectedWindow returnedWindow = new HistogramSelectedWindow(histogramContent);
		setCurrentWindow( returnedWindow );
		
		currentWindow.setWindowTimeWidth(windowTimeDuration);
		currentWindow.setWindowCenterXPosition(0);
		
		return returnedWindow;
	}
	
	public Long getSelectedWindowSize() {
		return currentWindow.getWindowTimeWidth();
	}
	
	public void setSelectedWindowSize(Long newSelectedWindowSize) {
		
		if ( newSelectedWindowSize < MINIMUM_WINDOW_SIZE ) {
			newSelectedWindowSize = MINIMUM_WINDOW_SIZE;
		}
		else if ( newSelectedWindowSize > histogramContent.getFullTraceInterval() ) {
			newSelectedWindowSize = histogramContent.getFullTraceInterval();
		}
		
		currentWindow.setWindowTimeWidth(newSelectedWindowSize);
	}
	
	public HistogramSelectedWindow getCurrentWindow() {
		return currentWindow;
	}
	
	public void setCurrentWindow(HistogramSelectedWindow newCurrentWindow) {
		this.currentWindow = newCurrentWindow;
		paintListener.setSelectedWindow(newCurrentWindow);
	}
	
	public void slideWindow(int newRelativeXPosition) {
		// Nothing : function is a place holder
	}
	
	public void positionWindow(int newAbsoluteXPosition) {
		// Nothing : function is a place holder
	}
	
	public void resizeWindowByFactor(int newFactor) {
		// Nothing : function is a place holder
	}
	
	public boolean checkIfTimeWindowChanged(int newPosition) {
		// Nothing : function is a place holder
		return false;
	}
	
	public void notifyTimeWindowChanged() {
		// Nothing : function is a place holder
	}
	
	public void updateParentInformation() {
		// Nothing : function is a place holder
	}
	
	public void redrawAsynchronously() {
		
		if ( canvasRedrawer == null ) {
			canvasRedrawer = new asyncCanvasRedrawer(this);
		}
		
		canvasRedrawer.asynchronousRedraw();
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
	
	public void focusGained(FocusEvent e) {
		System.out.println("focusGained");
	}
	
	public void focusLost(FocusEvent e) {
		System.out.println("focusLost");
	}
}

class asyncCanvasRedrawer {
	
	private TraceCanvas parentCanvas = null; 
	
	public asyncCanvasRedrawer(TraceCanvas newCanvas) {
		parentCanvas = newCanvas;
	}
	
	public void asynchronousRedraw() {
		Display display = parentCanvas.getDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				parentCanvas.updateParentInformation();
				parentCanvas.redraw();
			}
		});
	}
}
