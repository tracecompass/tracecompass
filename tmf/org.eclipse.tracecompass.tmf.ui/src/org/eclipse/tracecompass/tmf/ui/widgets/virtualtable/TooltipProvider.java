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

/**
 * An interface to get tooltips.
 * @author  Xavier Raynaud
 * @version 1.0
 */
public interface TooltipProvider {

    /**
     * get a Tooltip for a given column in a table row. (a cell if you will)
     * @param column the column
     * @param data the object being selected. (quite often a "TableItem")
     * @return the string of text to display in the tooltip.
     */
    String getTooltip(int column, Object data);

}
