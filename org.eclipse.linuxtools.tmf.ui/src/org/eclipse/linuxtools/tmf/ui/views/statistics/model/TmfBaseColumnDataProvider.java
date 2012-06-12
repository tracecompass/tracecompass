/*******************************************************************************
 * Copyright (c) 2011, 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Mathieu Denis      (mathieu.denis@polymtl.ca)  - Implementation and Initial API
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.statistics.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.tmf.ui.views.statistics.Messages;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.TmfBaseColumnData.ITmfColumnPercentageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Create a basic list of columns with providers.
 * 
 * @version 1.0
 * @author Mathieu Denis
 */
public class TmfBaseColumnDataProvider implements ITmfColumnDataProvider {

    /**
     * Contains the list of the columns
     */
    protected List<TmfBaseColumnData> fColumnData = null;
    /**
     * Level column names
     */
    protected final static String LEVEL_COLUMN = Messages.TmfStatisticsView_LevelColumn;
    /**
     * Number of events column names
     */
    protected final static String EVENTS_COUNT_COLUMN = Messages.TmfStatisticsView_NbEventsColumn;
    /**
     * Level column tooltips
     */
    protected final static String LEVEL_COLUMN_TIP = Messages.TmfStatisticsView_LevelColumnTip;
    /**
     * Number of events column tooltips
     */
    protected final static String EVENTS_COUNT_COLUMN_TIP = Messages.TmfStatisticsView_NbEventsTip;
    /**
     * Level for which statistics should not be displayed.
     */
    protected Set<String> fFolderLevels = new HashSet<String>(Arrays.asList(new String[] { "Event Types" })); //$NON-NLS-1$
    /**
     * Create basic columns to represent the statistics data
     */
    public TmfBaseColumnDataProvider() {
        // List that will be used to create the table.
        fColumnData = new Vector<TmfBaseColumnData>();
        fColumnData.add(new TmfBaseColumnData(LEVEL_COLUMN, 200, SWT.LEFT, LEVEL_COLUMN_TIP, new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((TmfStatisticsTreeNode) element).getKey();
            }

            @Override
            public Image getImage(Object element) {
                TmfStatisticsTreeNode node = (TmfStatisticsTreeNode) element;
                if (fFolderLevels.contains(node.getKey())) {
                    return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
                } else {
                    return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
                }
            }
        }, new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                TmfStatisticsTreeNode n1 = (TmfStatisticsTreeNode) e1;
                TmfStatisticsTreeNode n2 = (TmfStatisticsTreeNode) e2;

                return n1.getKey().compareTo(n2.getKey());
            }
        }, null));

        fColumnData.add(new TmfBaseColumnData(EVENTS_COUNT_COLUMN, 125, SWT.LEFT, EVENTS_COUNT_COLUMN_TIP, new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                TmfStatisticsTreeNode node = (TmfStatisticsTreeNode) element;
                if (!fFolderLevels.contains(node.getKey())) {
                    return Long.toString(node.getValue().nbEvents);
                } else {
                    return ""; //$NON-NLS-1$
                }
            }
        }, new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                TmfStatisticsTreeNode n1 = (TmfStatisticsTreeNode) e1;
                TmfStatisticsTreeNode n2 = (TmfStatisticsTreeNode) e2;

                return (int) (n1.getValue().nbEvents - n2.getValue().nbEvents);
            }
        }, new ITmfColumnPercentageProvider() {

            @Override
            public double getPercentage(TmfStatisticsTreeNode node) {
                TmfStatisticsTreeNode parent = node;
                do {
                    parent = parent.getParent();
                } while (parent != null && parent.getValue().nbEvents == 0);

                if (parent == null) {
                    return 0;
                } else {
                    return (double) node.getValue().nbEvents / parent.getValue().nbEvents;
                }
            }
        }));
    }

    /**
     * Provide the columns to represent statistics data
     */
    @Override
    public List<TmfBaseColumnData> getColumnData() {
        return fColumnData;
    }

}
