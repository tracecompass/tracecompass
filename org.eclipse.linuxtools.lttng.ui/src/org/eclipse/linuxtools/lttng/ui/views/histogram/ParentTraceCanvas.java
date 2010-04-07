package org.eclipse.linuxtools.lttng.ui.views.histogram;

import org.eclipse.swt.widgets.Composite;

public class ParentTraceCanvas extends TraceCanvas {
	
	private HistogramView parentHistogramWindow = null; 
	
	public ParentTraceCanvas(HistogramView newParentWindow, Composite parent, int style, int widthPerColumn, int columnHeight) {
		super(parent, style, widthPerColumn, columnHeight);
		
		parentHistogramWindow = newParentWindow;
	}
	
	@Override
	public void slideWindow(int newRelativeXPosition) {
		int absolutePosition = currentWindow.getWindowCenterXPosition() + newRelativeXPosition;
		
		positionWindow(absolutePosition);
		notifyTimeWindowChanged();
	}
	
	@Override
	public void positionWindow(int newAbsoluteXPosition) {
		
		if ( newAbsoluteXPosition < 0 ) {
			newAbsoluteXPosition = 0;
		}
		else if ( newAbsoluteXPosition > getParent().getSize().x ) {
			newAbsoluteXPosition = getParent().getSize().x;
		}
		
		if ( checkIfTimeWindowChanged(newAbsoluteXPosition) == true ) {
			currentWindow.setWindowCenterXPosition(newAbsoluteXPosition);
			redrawAsynchronously();
		}
	}
	
	@Override
	public void resizeWindowByFactor(int newFactor) {
		
		long ajustedTime = (long)((double)getSelectedWindowSize() * ZOOM_FACTOR);
		ajustedTime = ajustedTime * Math.abs(newFactor);
		
		if ( newFactor < 0 ) {
			ajustedTime = getSelectedWindowSize() + ajustedTime;
		}
		else {
			ajustedTime = getSelectedWindowSize() - ajustedTime;
		}
		
		if ( ajustedTime != currentWindow.getWindowTimeWidth() ) {
			currentWindow.setWindowTimeWidth(ajustedTime);
			notifyTimeWindowChanged();
			redrawAsynchronously();
		}
	}
	
	@Override
	public boolean checkIfTimeWindowChanged(int newXPosition) {
		boolean returnedValue = false;
		
		if ( newXPosition != currentWindow.getWindowCenterXPosition() ) {
			returnedValue = true;
		}
		return returnedValue;
	}
	
	@Override
	public void notifyTimeWindowChanged() {
		// Notify the parent view that something changed
		parentHistogramWindow.windowChangedNotification();
	}
}
