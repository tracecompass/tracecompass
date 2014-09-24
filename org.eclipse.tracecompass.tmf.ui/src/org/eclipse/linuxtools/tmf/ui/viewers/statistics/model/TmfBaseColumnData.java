/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial Implementation
 *   Bernd Hufmann - Added Annotations
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.statistics.model;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * Contains all the information necessary to build a column of the table.
 *
 * @author Mathieu Denis
 * @since 2.0
 */
public class TmfBaseColumnData {

    /**
     * Name of the column.
     */
    private final String fHeader;

    /**
     * Width of the column.
     */
    private final int fWidth;

    /**
     * Alignment of the column.
     */
    private final int fAlignment;

    /**
     * Tooltip of the column.
     */
    private final String fTooltip;

    /**
     * Adapts a StatisticsTreeNode into the content of it's corresponding cell
     * for that column.
     */
    private final ColumnLabelProvider fLabelProvider;

    /**
     * Used to sort elements of this column. Can be null.
     */
    private final @Nullable ViewerComparator fComparator;

    /**
     * Used to draw bar charts in this column. Can be null.
     */
    private final @Nullable ITmfColumnPercentageProvider fPercentageProvider;

    /**
     * Used to draw bar charts in columns.
     */
    public interface ITmfColumnPercentageProvider {

        /**
         * Percentage provider
         *
         * @param node
         *            The statistics tree node
         * @return The value as a percentage
         */
        public double getPercentage(TmfStatisticsTreeNode node);
    }

    /**
     * Constructor with parameters
     *
     * @param h
     *            header of the column. The name will be shown at the top of the
     *            column.
     * @param w
     *            width of the column.
     * @param a
     *            alignment of the text
     * @param t
     *            text to shown as a tooltip when the cursor comes over the
     *            header
     * @param l
     *            provide all the column element
     * @param c
     *            used to compare element between them to be able to classify
     *            the content of the columns
     * @param p
     *            provide the percentage of a specific element
     */
    public TmfBaseColumnData(String h, int w, int a, String t,
            ColumnLabelProvider l, ViewerComparator c,
            ITmfColumnPercentageProvider p) {
        fHeader = h;
        fWidth = w;
        fAlignment = a;
        fTooltip = t;
        fLabelProvider = l;
        fComparator = c;
        fPercentageProvider = p;
    }

    /**
     * Return the column name.
     *
     * @return the name of the column.
     */
    public String getHeader() {
        return fHeader;
    }

    /**
     * Return the width of the column at the creation.
     *
     * @return the width of the column.
     */
    public int getWidth() {
        return fWidth;
    }

    /**
     * Return the alignment of the column.
     *
     * @see org.eclipse.swt.SWT
     * @return an integer representing the alignment inside the column.
     */
    public int getAlignment() {
        return fAlignment;
    }

    /**
     * Provide the text to show in the tooltip when the cursor comes over the
     * column header.
     *
     * @return text to show in the tooltip
     */
    public String getTooltip() {
        return fTooltip;
    }

    /**
     * Return the labelProvider which provides the information to put in column
     * cells.
     *
     * @return a ColumnLabelProvider.
     */
    public ColumnLabelProvider getLabelProvider() {
        return fLabelProvider;
    }

    /**
     * Return a ViewerComparator used to sort viewer's contents.
     *
     * @return the comparator.
     */
    public ViewerComparator getComparator() {
        return fComparator;
    }

    /**
     * Return the provider of the percentage. Used to draw bar charts in
     * columns.
     *
     * @return the percentageProvider.
     */
    public ITmfColumnPercentageProvider getPercentageProvider() {
        return fPercentageProvider;
    }
}
