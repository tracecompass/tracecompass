/*******************************************************************************
 * Copyright (c) 2011, 2014 Kalray, Ericsson.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Xavier Raynaud - Initial API and implementation
 ******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.widgets.virtualtable;

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
    void handleDoubleClick(TmfVirtualTable table, TableItem item, int column);

}
