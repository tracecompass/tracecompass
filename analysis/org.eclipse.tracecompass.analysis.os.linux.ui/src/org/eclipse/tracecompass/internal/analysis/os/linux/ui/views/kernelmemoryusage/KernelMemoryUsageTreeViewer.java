/**********************************************************************
 * Copyright (c) 2016, 2017 Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.kernelmemoryusage;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelmemoryusage.KernelMemoryUsageDataProvider;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelmemoryusage.KernelMemoryUsageTreeModel;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractSelectTreeViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeColumnDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfGenericTreeEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeColumnData;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.TriStateFilteredCheckboxTree;

/**
 * Tree viewer to select which process to display in the kernel memory usage
 * chart.
 *
 * @author Mahdi Zolnouri
 * @author Wassim Nasrallah
 * @author Najib Arbaoui
 */
public class KernelMemoryUsageTreeViewer extends AbstractSelectTreeViewer {

    /** Provides label for the Kernel memory usage tree viewer cells */
    protected class KernelMemoryLabelProvider extends TreeLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (!(element instanceof TmfGenericTreeEntry)) {
                return null;
            }
            TmfGenericTreeEntry<KernelMemoryUsageTreeModel> obj = (TmfGenericTreeEntry<KernelMemoryUsageTreeModel>) element;
            int tid = obj.getModel().getTid();
            if (columnIndex == 0) {
                if (tid == KernelMemoryUsageDataProvider.TOTAL_TID) {
                    // this is the entry for the total, put the trace name instead.
                    return obj.getName();
                }
                return Integer.toString(tid);
            } else if (columnIndex == 1) {
                if (tid == KernelMemoryUsageDataProvider.TOTAL_TID) {
                    // this is the entry for the total, say this is the total.
                    return Messages.KernelMemoryUsageComposite_Total;
                }
                return obj.getName();
            }
            return null;
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 2 && element instanceof TmfGenericTreeEntry) {
                TmfGenericTreeEntry<KernelMemoryUsageTreeModel> entry = (TmfGenericTreeEntry<KernelMemoryUsageTreeModel>) element;
                int tid = entry.getModel().getTid();
                if (entry.getParent() instanceof TmfGenericTreeEntry && isChecked(element)) {
                    return getLegendImage(entry.getParent().getName() + ':' + tid);
                } else if (tid == KernelMemoryUsageDataProvider.TOTAL_TID) {
                    return getLegendImage(entry.getName() + KernelMemoryUsageDataProvider.TOTAL_SUFFIX);
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
     */
    public KernelMemoryUsageTreeViewer(Composite parent, TriStateFilteredCheckboxTree checkboxTree) {
        super(parent, checkboxTree, 2, KernelMemoryUsageDataProvider.ID);
        setLabelProvider(new KernelMemoryLabelProvider());
    }

    @Override
    protected ITmfTreeColumnDataProvider getColumnDataProvider() {
        return () -> {
            List<TmfTreeColumnData> columns = new ArrayList<>(3);
            TmfTreeColumnData column = new TmfTreeColumnData(Messages.KernelMemoryUsageComposite_ColumnTID);
            column.setComparator(new ViewerComparator() {
                @Override
                public int compare(Viewer viewer, Object e1, Object e2) {
                    TmfGenericTreeEntry<KernelMemoryUsageTreeModel> n1 = (TmfGenericTreeEntry<KernelMemoryUsageTreeModel>) e1;
                    TmfGenericTreeEntry<KernelMemoryUsageTreeModel> n2 = (TmfGenericTreeEntry<KernelMemoryUsageTreeModel>) e2;

                    return Integer.compare(n1.getModel().getTid(), n2.getModel().getTid());
                }
            });
            columns.add(column);
            column = new TmfTreeColumnData(Messages.KernelMemoryUsageComposite_ColumnProcess);
            column.setComparator(new ViewerComparator() {
                @Override
                public int compare(Viewer viewer, Object e1, Object e2) {
                    TmfGenericTreeEntry<KernelMemoryUsageTreeModel> n1 = (TmfGenericTreeEntry<KernelMemoryUsageTreeModel>) e1;
                    TmfGenericTreeEntry<KernelMemoryUsageTreeModel> n2 = (TmfGenericTreeEntry<KernelMemoryUsageTreeModel>) e2;

                    return n1.getName().compareTo(n2.getName());
                }
            });
            columns.add(column);
            column = new TmfTreeColumnData(Messages.KernelMemoryUsageComposite_Legend);
            columns.add(column);
            return columns;
        };
    }
}