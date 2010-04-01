package org.eclipse.linuxtools.lttng.ui.views.histogram;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;

public class TraceCanvasPaintListener implements PaintListener {
	
	private final static int EMPTY_BACKGROUND_COLOR = SWT.COLOR_WHITE;
	private final static int HISTOGRAM_BARS_COLOR = SWT.COLOR_DARK_CYAN;
	private final static int SELECTION_WINDOW_COLOR = SWT.COLOR_RED;
	
	private final static int SELECTION_LINE_WIDTH = 2;
	private final static int SELECTION_CROSSHAIR_LENGTH = 3;
	
	private HistogramContent histogramContent = null;
	private HistogramSelectedWindow selectedWindow = null;
	
	private int columnWidth = 0;
	private int columnHeight = 0;
	
	public TraceCanvasPaintListener(HistogramContent newHistogramContent, int newBarWidth, int newBarHeight) {
		columnWidth = newBarWidth;
		columnHeight = newBarHeight;
		histogramContent = newHistogramContent;
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
		
		if ( selectedWindow != null) {
			drawSelectedWindow(e);
		}
	}
	
	public void clearDrawingSection(PaintEvent e) {
		e.gc.setBackground(e.display.getSystemColor(EMPTY_BACKGROUND_COLOR));
		Rectangle allSection = new Rectangle(0, 0, e.width, e.height);
		e.gc.fillRectangle(allSection);
		e.gc.drawRectangle(allSection);
	}
	
	// *** VERIFY ***
	// Is it good to put this synchronized?
	//
	public synchronized void drawHistogram(PaintEvent e) {
		e.gc.setBackground(e.display.getSystemColor(HISTOGRAM_BARS_COLOR));
		Rectangle allSection = new Rectangle(0, 0, histogramContent.getReadyUpToPosition()*columnWidth, e.height);
		e.gc.fillRectangle(allSection);
		e.gc.drawRectangle(allSection);
		
	    e.gc.setForeground(e.display.getSystemColor(EMPTY_BACKGROUND_COLOR));
	    e.gc.setBackground(e.display.getSystemColor(EMPTY_BACKGROUND_COLOR));
	    
	    for ( int x=0; x<histogramContent.getReadyUpToPosition(); x++) {
	    	Rectangle rect = new Rectangle(columnWidth*x, 0, columnWidth, columnHeight - histogramContent.getElementByIndex(x).intervalHeight );
			e.gc.fillRectangle(rect);
			e.gc.drawRectangle(rect);
	    }
	    
	    e.gc.setForeground(e.display.getSystemColor(EMPTY_BACKGROUND_COLOR));
	    e.gc.setBackground(e.display.getSystemColor(EMPTY_BACKGROUND_COLOR));
	    Rectangle rect = new Rectangle(columnWidth*histogramContent.getNbElement(), 0, e.width, columnHeight);
		e.gc.fillRectangle(rect);
		e.gc.drawRectangle(rect);
	}
	
	public void drawSelectedWindow(PaintEvent e) {
		
		e.gc.setForeground(e.display.getSystemColor(SELECTION_WINDOW_COLOR));
	    e.gc.setBackground(e.display.getSystemColor(SELECTION_WINDOW_COLOR));
		
		e.gc.setLineWidth(SELECTION_LINE_WIDTH);
	    
		e.gc.drawLine(selectedWindow.selectionLeft, 0, selectedWindow.selectionLeft, e.height);
	    e.gc.drawLine(selectedWindow.selectionLeft, e.height, selectedWindow.selectionRight, e.height);
	    e.gc.drawLine(selectedWindow.selectionRight, e.height, selectedWindow.selectionRight, 0);
	    e.gc.drawLine(selectedWindow.selectionLeft, 0, selectedWindow.selectionRight, 0);
	    
	    
	    e.gc.drawLine(selectedWindow.selectionCenter + SELECTION_CROSSHAIR_LENGTH, e.height/2, selectedWindow.selectionCenter - SELECTION_CROSSHAIR_LENGTH, e.height/2);
	    e.gc.drawLine(selectedWindow.selectionCenter, (e.height/2) + SELECTION_CROSSHAIR_LENGTH, selectedWindow.selectionCenter, (e.height/2) - SELECTION_CROSSHAIR_LENGTH);
	}
	
	
	public void setHistogramContent(HistogramContent newhistogramContent) {
		this.histogramContent = newhistogramContent;
	}
	
	public HistogramContent getHistogramContent() {
		return histogramContent;
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
