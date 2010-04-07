package org.eclipse.linuxtools.lttng.ui.views.histogram;

import org.eclipse.swt.widgets.Composite;

public class ChildrenTraceCanvas extends TraceCanvas {
	
	private HistogramView parentHistogramWindow = null; 
	
	public ChildrenTraceCanvas(HistogramView newParentWindow, Composite parent, int style, int widthPerColumn, int columnHeight) {
		super(parent, style, widthPerColumn, columnHeight);
		
		parentHistogramWindow = newParentWindow;
	}
	
	@Override
	public void updateParentInformation() {
		parentHistogramWindow.updateViewInformation();
	}
}
