/*******************************************************************************
 * Copyright (c) 2011, 2012 Kalray.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud - Initial API and implementation
 ******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.widgets.virtualtable;

import org.eclipse.swt.widgets.TableItem;
/**
 * Double click listener interface
 * @author Xavier Raynaud
 * @version 1.0
 */
public interface IDoubleClickListener {

	/**
	 * Handle a double click event
	 * @param table the table that was double clicked
	 * @param item the item that was double clicked in the table
	 * @param column the column that was double clicked in the item in the table.
	 */
	public void handleDoubleClick(TmfVirtualTable table, TableItem item, int column);

}
