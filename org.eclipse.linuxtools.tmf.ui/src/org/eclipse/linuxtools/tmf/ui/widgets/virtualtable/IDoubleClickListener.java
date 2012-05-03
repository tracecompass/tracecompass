/*******************************************************************************
 * Copyright (c) 2011 Kalray.
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

public interface IDoubleClickListener {

	
	public void handleDoubleClick(TmfVirtualTable table, TableItem item, int column);
	
}
