/**********************************************************************
 * Copyright (c) 2016, 2017 Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.memory;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.os.linux.core.memory.MemoryUsageTreeModel;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractSelectTreeViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeColumnDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfGenericTreeEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeColumnData;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.TriStateFilteredCheckboxTree;

/**
 * Tree viewer to select which process to display in the kernel memory usage
 * chart.
 *
 * @since 2.2
 * @author Mahdi Zolnouri
 * @author Wassim Nasrallah
 * @author Najib Arbaoui
 */
public class MemoryUsageTreeViewer extends AbstractSelectTreeViewer {

    /** Provides label for the Kernel memory usage tree viewer cells */
    private class MemoryLabelProvider extends TreeLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (!(element instanceof TmfGenericTreeEntry)) {
                return null;
            }
            TmfGenericTreeEntry<MemoryUsageTreeModel> obj = (TmfGenericTreeEntry<MemoryUsageTreeModel>) element;
            if (columnIndex == 0) {
                return obj.getName();
            } else if (columnIndex == 1) {
                int tid = obj.getModel().getTid();
                if (tid < 0) {
                    return Messages.MemoryUsageTree_Total;
                }
                return Integer.toString(tid);
            }
            return null;
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 2 && element instanceof TmfGenericTreeEntry) {
                TmfGenericTreeEntry<MemoryUsageTreeModel> entry = (TmfGenericTreeEntry<MemoryUsageTreeModel>) element;
                int tid = entry.getModel().getTid();
                if (entry.getParent() instanceof TmfGenericTreeEntry && isChecked(element)) {
                    return getLegendImage(entry.getParent().getName() + ':' + tid);
                } else if (tid < 0) {
                    return getLegendImage(entry.getName() + MemoryUsageTreeModel.TOTAL_SUFFIX);
                }
            }
            return null;
        }
    }

    /**
     * Constructor
     *
     * @param parent
     *            The parent composite that holds this viewer
     * @param checkboxTree
     *            <code>TriStateFilteredTree</code> wrapping a
     *            <code>CheckboxTreeViewer</code>
     * @param id
     *            The memory usage data provider ID.
     */
    public MemoryUsageTreeViewer(Composite parent, TriStateFilteredCheckboxTree checkboxTree, String id) {
        super(parent, checkboxTree, 2, id);
        setLabelProvider(new MemoryLabelProvider());
    }

    @Override
    protected ITmfTreeColumnDataProvider getColumnDataProvider() {
        return () -> {
            List<TmfTreeColumnData> columns = new ArrayList<>(3);
            TmfTreeColumnData column = new TmfTreeColumnData(Messages.MemoryUsageTree_ColumnProcess);
            column.setComparator(new ViewerComparator() {
                @Override
                public int compare(Viewer viewer, Object e1, Object e2) {
                    TmfGenericTreeEntry<MemoryUsageTreeModel> n1 = (TmfGenericTreeEntry<MemoryUsageTreeModel>) e1;
                    TmfGenericTreeEntry<MemoryUsageTreeModel> n2 = (TmfGenericTreeEntry<MemoryUsageTreeModel>) e2;

                    return n1.getName().compareTo(n2.getName());
                }
            });
            columns.add(column);
            column = new TmfTreeColumnData(Messages.MemoryUsageTree_ColumnTID);
            column.setComparator(new ViewerComparator() {
                @Override
                public int compare(Viewer viewer, Object e1, Object e2) {
                    TmfGenericTreeEntry<MemoryUsageTreeModel> n1 = (TmfGenericTreeEntry<MemoryUsageTreeModel>) e1;
                    TmfGenericTreeEntry<MemoryUsageTreeModel> n2 = (TmfGenericTreeEntry<MemoryUsageTreeModel>) e2;

                    return Integer.compare(n1.getModel().getTid(), n2.getModel().getTid());
                }
            });
            columns.add(column);
            column = new TmfTreeColumnData(Messages.MemoryUsageTree_Legend);
            columns.add(column);
            return columns;
        };
    }
}