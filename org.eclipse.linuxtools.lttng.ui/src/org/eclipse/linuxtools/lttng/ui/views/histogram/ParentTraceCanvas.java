package org.eclipse.linuxtools.lttng.ui.views.histogram;

import org.eclipse.swt.widgets.Composite;

public class ParentTraceCanvas extends TraceCanvas {
	
	private HistogramView parentHistogramWindow = null; 
	
	public ParentTraceCanvas(HistogramView newParentWindow, Composite parent, int style, int widthPerColumn, int columnHeight) {
		super(parent, style, widthPerColumn, columnHeight);
		
		parentHistogramWindow = newParentWindow;
	}
	
	@Override
	public void notifyTimeWindowChanged() {
		// Notify the parent view that something changed
		parentHistogramWindow.windowChangedNotification();
	}
	
}
