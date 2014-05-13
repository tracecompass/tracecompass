/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Implementation and Initial API
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.statistics.model;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfBaseColumnData.ITmfColumnPercentageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Create a basic list of columns with providers.
 *
 * @author Mathieu Denis
 * @since 2.0
 */
public class TmfBaseColumnDataProvider {

    // ------------------------------------------------------------------------
    // Localized strings
    // ------------------------------------------------------------------------

    /** Level column names */
    private static final String LEVEL_COLUMN = Messages.TmfStatisticsView_LevelColumn;

    /** Number of events column names */
    private static final String EVENTS_COUNT_COLUMN = Messages.TmfStatisticsView_NbEventsColumn;

    /** Number of events in time range column names */
    private static final String PARTIAL_EVENTS_COUNT_COLUMN = Messages.TmfStatisticsView_NbEventsTimeRangeColumn;

    /** Level column tooltips */
    private static final String LEVEL_COLUMN_TIP = Messages.TmfStatisticsView_LevelColumnTip;

    /** Number of events column tooltips */
    private static final String EVENTS_COUNT_COLUMN_TIP = Messages.TmfStatisticsView_NbEventsTip;

    /** Number of events in time range column tooltips */
    private static final String PARTIAL_COUNT_COLUMN_TIP = Messages.TmfStatisticsView_NbEventsTimeRangeTip;

    // ------------------------------------------------------------------------
    // Class attributes
    // ------------------------------------------------------------------------

    /** Level for which statistics should not be displayed. */
    private static final Set<String> HIDDEN_FOLDER_LEVELS = ImmutableSet.of("Event Types"); //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Instance fields
    // ------------------------------------------------------------------------

    /**
     * Contains the list of the columns
     */
    private final List<TmfBaseColumnData> fColumnData;

    /**
     * Create basic columns to represent the statistics data
     */
    public TmfBaseColumnDataProvider() {
        /* List that will be used to create the table. */
        ImmutableList.Builder<TmfBaseColumnData> builder = new ImmutableList.Builder<>();
        /* Column showing the name of the events and its level in the tree */
        builder.add(new TmfBaseColumnData(
                LEVEL_COLUMN,
                200,
                SWT.LEFT,
                LEVEL_COLUMN_TIP,
                new ColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        return ((TmfStatisticsTreeNode) element).getName();
                    }

                    @Override
                    public Image getImage(Object element) {
                        TmfStatisticsTreeNode node = (TmfStatisticsTreeNode) element;
                        if (HIDDEN_FOLDER_LEVELS.contains(node.getName())) {
                            return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
                        }
                        return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
                    }
                },
                new ViewerComparator() {
                    @Override
                    public int compare(Viewer viewer, Object e1, Object e2) {
                        TmfStatisticsTreeNode n1 = (TmfStatisticsTreeNode) e1;
                        TmfStatisticsTreeNode n2 = (TmfStatisticsTreeNode) e2;

                        return n1.getName().compareTo(n2.getName());
                    }
                },
                null));

        /* Column showing the total number of events */
        builder.add(new TmfBaseColumnData(
                EVENTS_COUNT_COLUMN,
                140,
                SWT.LEFT,
                EVENTS_COUNT_COLUMN_TIP,
                new ColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        TmfStatisticsTreeNode node = (TmfStatisticsTreeNode) element;
                        if (!HIDDEN_FOLDER_LEVELS.contains(node.getName())) {
                            return Long.toString(node.getValues().getTotal());
                        }
                        return ""; //$NON-NLS-1$
                    }
                },
                new ViewerComparator() {
                    @Override
                    public int compare(Viewer viewer, Object e1, Object e2) {
                        TmfStatisticsTreeNode n1 = (TmfStatisticsTreeNode) e1;
                        TmfStatisticsTreeNode n2 = (TmfStatisticsTreeNode) e2;

                        return (int) (n1.getValues().getTotal() - n2.getValues().getTotal());
                    }
                },
                new ITmfColumnPercentageProvider() {
                    @Override
                    public double getPercentage(TmfStatisticsTreeNode node) {
                        TmfStatisticsTreeNode parent = node;
                        do {
                            parent = parent.getParent();
                        } while (parent != null && parent.getValues().getTotal() == 0);

                        if (parent == null) {
                            return 0;
                        }
                        return (double) node.getValues().getTotal() / parent.getValues().getTotal();
                    }
                }));

        /* Column showing the number of events within the selected time range */
        builder.add(new TmfBaseColumnData(
                PARTIAL_EVENTS_COUNT_COLUMN,
                140,
                SWT.LEFT,
                PARTIAL_COUNT_COLUMN_TIP,
                new ColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        TmfStatisticsTreeNode node = (TmfStatisticsTreeNode) element;
                        if (!HIDDEN_FOLDER_LEVELS.contains(node.getName())) {
                            return Long.toString(node.getValues().getPartial());
                        }
                        return ""; //$NON-NLS-1$
                    }
                },
                new ViewerComparator() {
                    @Override
                    public int compare(Viewer viewer, Object e1, Object e2) {
                        TmfStatisticsTreeNode n1 = (TmfStatisticsTreeNode) e1;
                        TmfStatisticsTreeNode n2 = (TmfStatisticsTreeNode) e2;

                        return (int) (n1.getValues().getPartial() - n2.getValues().getPartial());
                    }
                },
                new ITmfColumnPercentageProvider() {
                    @Override
                    public double getPercentage(TmfStatisticsTreeNode node) {
                        TmfStatisticsTreeNode parent = node;
                        do {
                            parent = parent.getParent();
                        } while (parent != null && parent.getValues().getPartial() == 0);

                        if (parent == null) {
                            return 0;
                        }
                        return (double) node.getValues().getPartial() / parent.getValues().getPartial();
                    }
                }));

        fColumnData = builder.build();
    }

    /**
     * Return a list of the column created for the view
     *
     * @return columns list
     */
    public List<TmfBaseColumnData> getColumnData() {
        return fColumnData;
    }

}
