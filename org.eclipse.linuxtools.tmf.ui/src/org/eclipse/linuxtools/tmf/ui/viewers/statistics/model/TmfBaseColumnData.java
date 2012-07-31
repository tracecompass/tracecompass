/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
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

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * Contains all the information necessary to build a column of the table.
 *
 * @version 2.0
 * @author Mathieu Denis
 * @since 2.0
 */
public class TmfBaseColumnData implements ITmfStatisticsColumnData {

    /**
     * Name of the column.
     */
    protected final String fHeader;

    /**
     * Width of the column.
     */
    protected final int fWidth;

    /**
     * Alignment of the column.
     */
    protected final int fAlignment;

    /**
     * Tooltip of the column.
     */
    protected final String fTooltip;

    /**
     * Adapts a StatisticsTreeNode into the content of it's corresponding cell
     * for that column.
     */
    protected final ColumnLabelProvider fLabelProvider;

    /**
     * Used to sort elements of this column. Can be null.
     */
    protected final ViewerComparator fComparator;

    /**
     * Used to draw bar charts in this column. Can be null.
     */
    protected final ITmfColumnPercentageProvider fPercentageProvider;

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

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.ITmfStatisticsColumnData#getHeader()
     */
    @Override
    public String getHeader() {
        return fHeader;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.ITmfStatisticsColumnData#getWidth()
     */
    @Override
    public int getWidth() {
        return fWidth;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.ITmfStatisticsColumnData#getAlignment()
     */
    @Override
    public int getAlignment() {
        return fAlignment;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.ITmfStatisticsColumnData#getTooltip()
     */
    @Override
    public String getTooltip() {
        return fTooltip;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.ITmfStatisticsColumnData#getLabelProvider()
     */
    @Override
    public ColumnLabelProvider getLabelProvider() {
        return fLabelProvider;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.ITmfStatisticsColumnData#getComparator()
     */
    @Override
    public ViewerComparator getComparator() {
        return fComparator;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.ITmfStatisticsColumnData#getPercentageProvider()
     */
    @Override
    public ITmfColumnPercentageProvider getPercentageProvider() {
        return fPercentageProvider;
    }
}
