package org.eclipse.linuxtools.lttng.ui.views.histogram;

import org.eclipse.swt.widgets.Composite;

public class ChildrenTraceCanvas extends TraceCanvas {
		
	public ChildrenTraceCanvas(HistogramView newParentWindow, Composite parent, int style, int widthPerColumn, int columnHeight) {
		super(parent, style, widthPerColumn, columnHeight);
	}
}
