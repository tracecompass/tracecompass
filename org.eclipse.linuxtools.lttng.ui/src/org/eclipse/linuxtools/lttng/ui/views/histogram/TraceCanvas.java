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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class TraceCanvas extends Canvas implements MouseMoveListener, MouseListener, FocusListener
//public class TraceCanvas extends Canvas implements FocusListener, KeyListener, MouseMoveListener, MouseListener, MouseWheelListener, ControlListener, SelectionListener, MouseTrackListener, TraverseListener
{
	
	public Long BIDON_VARIABLE_WINDOW_SIZE = (long)(0.1 * 1000000000L); // 0.1 seconds
	
	
	final static int DEFAULT_WIDTH_PER_COL = 2;
	final static int DEFAULT_MAX_HEIGHT_PER_COL = 50;
	
	private HistogramContent histogramContent = null;
	
	private TraceCanvasPaintListener paintListener = null;
	
	private int columnWidth = DEFAULT_WIDTH_PER_COL;
	private int columnMaxHeight = DEFAULT_MAX_HEIGHT_PER_COL;
	
	public TraceCanvas(Composite parent, int style) {
		this(parent, style, DEFAULT_WIDTH_PER_COL, DEFAULT_MAX_HEIGHT_PER_COL);
	}
	
	public TraceCanvas(Composite parent, int style, int widthPerColumn, int columnHeight) {
		super (parent, style);
		
		columnWidth = widthPerColumn;
		columnMaxHeight = columnHeight;
		
		setSize(parent.getDisplay().getBounds().width, columnHeight);
		
		//int viewWidth = getParent().computeSize(SWT.DEFAULT,SWT.DEFAULT).x;
		//System.out.println("*************** X " + viewWidth);
		
		int viewWidth = parent.getDisplay().getBounds().width;
		
		histogramContent = new HistogramContent( viewWidth / widthPerColumn, viewWidth, columnMaxHeight);
		
		addNeededListeners();
	}

	public void addNeededListeners() {
		paintListener = new TraceCanvasPaintListener(getHistogramContent(), getColumnWidth(), getColumnMaxHeight() );
		
		this.addPaintListener( paintListener );
		this.addMouseListener(this);
		this.addMouseMoveListener(this);
		this.addFocusListener(this);
		
		/*
		this.addControlListener(this);
		this.addKeyListener(this);
		this.addMouseWheelListener(this);
		this.addMouseTrackListener(this);
		this.addTraverseListener(this);
		*/
	}
	
	public void resetSelectedWindow() {
		paintListener.setSelectedWindow(null);
	}
	
	public boolean positionMouseCursor(MouseEvent e) {
		
		boolean returnedValue = false;
		
		if ( histogramContent.getIntervalTime() != 0 ) {
			int centralPos = e.x;
			HistogramElement centralElement = histogramContent.getElementFromXPosition(centralPos);
			HistogramElement leftElement  = histogramContent.getClosestElementByTimeInterval(centralElement, -(BIDON_VARIABLE_WINDOW_SIZE / 2) );
			HistogramElement rightElement = histogramContent.getClosestElementByTimeInterval(centralElement, +(BIDON_VARIABLE_WINDOW_SIZE / 2) );
			
			/*
			System.out.println("X 		: " + e.x);
			System.out.println("Y 		: " + e.y);
			System.out.println("CENTRAL : " + centralElement.firstIntervalTimestamp);
			System.out.println("LEFT 	: " + leftElement.firstIntervalTimestamp);
			System.out.println("RIGHT 	: " + rightElement.firstIntervalTimestamp);
			*/
			
			HistogramSelectedWindow tmpSelection = new HistogramSelectedWindow();
			tmpSelection.selectionCenter = histogramContent.getXPositionFromElement(centralElement);
			tmpSelection.selectionLeft = histogramContent.getXPositionFromElement(leftElement);
			tmpSelection.selectionRight = histogramContent.getXPositionFromElement(rightElement);
			
			paintListener.setSelectedWindow(tmpSelection);
			
			returnedValue = true;
			
			redraw();
		}
		
		return returnedValue;
	}
	
	public void notifyTimeWindowChanged() {
		// *** TODO ***
		// Notify framework that the windows changed!
		
		System.out.println("NOTIFICATION");
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
	
	public boolean isWindowMoving = false;
	
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
}
