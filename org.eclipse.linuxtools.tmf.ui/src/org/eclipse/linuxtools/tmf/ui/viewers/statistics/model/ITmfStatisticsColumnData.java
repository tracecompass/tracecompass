/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial API and Implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.statistics.model;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfBaseColumnData.ITmfColumnPercentageProvider;

/**
 * Provide the basic interface to create a statistics column for the statistics
 * table tree.
 *
 * @version 2.0
 * @since 2.0
 * @author Mathieu Denis
 */
public interface ITmfStatisticsColumnData {

    /**
     * Return the column name.
     *
     * @return the name of the column.
     */
    public String getHeader();

    /**
     * Return the width of the column at the creation.
     *
     * @return the width of the column.
     */
    public int getWidth();

    /**
     * Return the alignment of the column.
     *
     * @see org.eclipse.swt.SWT
     * @return an integer representing the alignment inside the column.
     */
    public int getAlignment();

    /**
     * Provide the text to show in the tooltip when the cursor comes over the
     * column header.
     *
     * @return text to show in the tooltip
     */
    public String getTooltip();

    /**
     * Return the labelProvider which provides the information to put in column
     * cells.
     *
     * @return a ColumnLabelProvider.
     */
    public ColumnLabelProvider getLabelProvider();

    /**
     * Return a ViewerComparator used to sort viewer's contents.
     *
     * @return the comparator.
     */
    public ViewerComparator getComparator();

    /**
     * Return the provider of the percentage. Used to draw bar charts in
     * columns.
     *
     * @return the percentageProvider.
     */
    public ITmfColumnPercentageProvider getPercentageProvider();
}
