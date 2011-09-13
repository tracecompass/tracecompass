package org.eclipse.linuxtools.tmf.ui.widgets;

import org.eclipse.swt.widgets.TableItem;

public interface IDoubleClickListener {

	
	public void handleDoubleClick(TmfVirtualTable table, TableItem item, int column);
	
}
