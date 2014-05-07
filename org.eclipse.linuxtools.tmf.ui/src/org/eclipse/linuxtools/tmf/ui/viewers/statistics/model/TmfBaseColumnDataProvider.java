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
 *   Vincent Perot - Add percentages to the label provider
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
 * @since 3.0
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

    /**
     * Level for which statistics should not be displayed.
     *
     * @since 3.0
     */
    public static final Set<String> HIDDEN_FOLDER_LEVELS = ImmutableSet.of("Event Types"); //$NON-NLS-1$

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Column index (Ideally, this should not be hardcoded).
    // ------------------------------------------------------------------------

    /**
     * Possible columns in the view
     *
     * @since 3.0
     */
    public static enum StatsColumn {
        /**
         * Column index for the event type column.
         */
        EVENT(0),
        /**
         * Column index for the event total count column.
         */
        TOTAL(1),
        /**
         * Column index for the event partial count column.
         */
        PARTIAL(2),
        /**
         * Column index for the dummy column.
         */
        DUMMY(3);

        private final int colIndex;

        private StatsColumn(int index) {
            colIndex = index;
        }

        /**
         * Getter method for the column index.
         *
         * @return the index of the column
         */
        public int getIndex() {
            return colIndex;
        }

        /**
         * Method to get the column at a certain index.
         *
         * @param index the index of the column
         *
         * @return the column at the specified index
         */
        public static StatsColumn getColumn(int index) {
            switch(index) {
            case 0:
                return EVENT;

            case 1:
                return TOTAL;

            case 2:
                return PARTIAL;

            case 3:
                return DUMMY;

            // Other values are illegal.
            default:
                throw new IllegalArgumentException();
            }

        }
    }

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
                SWT.RIGHT,
                EVENTS_COUNT_COLUMN_TIP,
                new ColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        TmfStatisticsTreeNode node = (TmfStatisticsTreeNode) element;
                        if (!HIDDEN_FOLDER_LEVELS.contains(node.getName())) {
                            return TmfStatisticsFormatter.toColumnData(node, StatsColumn.TOTAL);
                        }
                        return EMPTY_STRING;
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
                        TmfStatisticsTreeNode top = node.getTop();
                        return (top == null || top.getValues().getTotal() == 0) ?
                                0 : (double) (node.getValues().getTotal()) / top.getValues().getTotal();
                    }
                }));

        /* Column showing the number of events within the selected time range */
        builder.add(new TmfBaseColumnData(
                PARTIAL_EVENTS_COUNT_COLUMN,
                140,
                SWT.RIGHT,
                PARTIAL_COUNT_COLUMN_TIP,
                new ColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        TmfStatisticsTreeNode node = (TmfStatisticsTreeNode) element;
                        if (!HIDDEN_FOLDER_LEVELS.contains(node.getName())) {
                            return TmfStatisticsFormatter.toColumnData(node, StatsColumn.PARTIAL);
                        }
                        return EMPTY_STRING;
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
                        TmfStatisticsTreeNode top = node.getTop();
                        return (top == null || top.getValues().getPartial() == 0) ?
                                0 : (double) (node.getValues().getPartial()) / top.getValues().getPartial();
                    }
                }));

        /* Dummy column used to "fix" the display on Linux (using GTK) */
        builder.add(new TmfBaseColumnData(EMPTY_STRING, 1, SWT.RIGHT, EMPTY_STRING,
                new ColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        return EMPTY_STRING;
                    }
                },
                new ViewerComparator(),
                new ITmfColumnPercentageProvider() {
                    @Override
                    public double getPercentage(TmfStatisticsTreeNode node) {
                        return 0;
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
