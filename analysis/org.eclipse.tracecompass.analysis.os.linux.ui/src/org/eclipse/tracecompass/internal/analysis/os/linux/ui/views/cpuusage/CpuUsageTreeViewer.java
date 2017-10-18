/*******************************************************************************
 * Copyright (c) 2014, 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.cpuusage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.os.linux.core.cpuusage.CpuUsageDataProvider;
import org.eclipse.tracecompass.analysis.os.linux.core.cpuusage.CpuUsageEntryModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectedCpuQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractSelectTreeViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeColumnDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeViewerEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeColumnData;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeViewerEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.TriStateFilteredCheckboxTree;

/**
 * Tree viewer to display CPU usage information in a specified time range. It
 * shows the process's TID, its name, the time spent on the CPU during that
 * range, in % and absolute value.
 *
 * @author Geneviève Bastien
 */
public class CpuUsageTreeViewer extends AbstractSelectTreeViewer {

    /** Provides label for the CPU usage tree viewer cells */
    protected class CpuLabelProvider extends TreeLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            CpuUsageEntry obj = (CpuUsageEntry) element;
            if (columnIndex == 0) {
                int tid = obj.getModel().getTid();
                if (tid == CpuUsageDataProvider.TOTAL_SERIES_TID) {
                    // Get the trace name
                    return obj.getName();
                }
                return Integer.toString(tid);
            } else if (columnIndex == 1) {
                int tid = obj.getModel().getTid();
                if (tid == CpuUsageDataProvider.TOTAL_SERIES_TID) {
                    return Messages.CpuUsageXYViewer_Total;
                }
                return obj.getModel().getName();
            } else if (columnIndex == 2) {
                return String.format(Messages.CpuUsageComposite_TextPercent, 100 * obj.getPercent());
            } else if (columnIndex == 3) {
                return NLS.bind(Messages.CpuUsageComposite_TextTime, obj.getModel().getTime());
            }

            return null;
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 4 && element instanceof CpuUsageEntry) {
                CpuUsageEntry cpuUsageEntry = (CpuUsageEntry) element;
                CpuUsageEntryModel model = cpuUsageEntry.getModel();
                int tid = model.getTid();
                if (tid < 0) {
                    return getLegendImage(CpuUsageDataProvider.TOTAL + model.getName());
                }
                if (isChecked(element)) {
                    CpuUsageEntry parent = (CpuUsageEntry) cpuUsageEntry.getParent();
                    return getLegendImage(parent.getModel().getName() + ':' + model.getTid());
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
    public CpuUsageTreeViewer(Composite parent, TriStateFilteredCheckboxTree checkboxTree) {
        super(parent, checkboxTree, 4, CpuUsageDataProvider.ID);
        setLabelProvider(new CpuLabelProvider());
    }

    @Override
    protected ITmfTreeColumnDataProvider getColumnDataProvider() {
        return () -> {
            /* All columns are sortable */
            List<TmfTreeColumnData> columns = new ArrayList<>();
            TmfTreeColumnData column = new TmfTreeColumnData(Messages.CpuUsageComposite_ColumnTID);
            column.setComparator(new ViewerComparator() {
                @Override
                public int compare(Viewer viewer, Object e1, Object e2) {
                    CpuUsageEntry n1 = (CpuUsageEntry) e1;
                    CpuUsageEntry n2 = (CpuUsageEntry) e2;

                    return n1.getName().compareTo(n2.getName());

                }
            });
            columns.add(column);
            column = new TmfTreeColumnData(Messages.CpuUsageComposite_ColumnProcess);
            column.setComparator(new ViewerComparator() {
                @Override
                public int compare(Viewer viewer, Object e1, Object e2) {
                    CpuUsageEntry n1 = (CpuUsageEntry) e1;
                    CpuUsageEntry n2 = (CpuUsageEntry) e2;

                    return n1.getName().compareTo(n2.getModel().getName());

                }
            });
            columns.add(column);
            column = new TmfTreeColumnData(Messages.CpuUsageComposite_ColumnPercent);
            column.setComparator(new ViewerComparator() {
                @Override
                public int compare(Viewer viewer, Object e1, Object e2) {
                    CpuUsageEntry n1 = (CpuUsageEntry) e1;
                    CpuUsageEntry n2 = (CpuUsageEntry) e2;

                    return Double.compare(n1.getPercent(), n2.getPercent());

                }
            });
            column.setPercentageProvider(data -> ((CpuUsageEntry) data).getPercent());
            columns.add(column);
            column = new TmfTreeColumnData(Messages.CpuUsageComposite_ColumnTime);
            column.setComparator(new ViewerComparator() {
                @Override
                public int compare(Viewer viewer, Object e1, Object e2) {
                    CpuUsageEntry n1 = (CpuUsageEntry) e1;
                    CpuUsageEntry n2 = (CpuUsageEntry) e2;

                    return Long.compare(n1.getModel().getTime(), n2.getModel().getTime());

                }
            });
            columns.add(column);
            column = new TmfTreeColumnData(Messages.CpuUsageComposite_ColumnLegend);
            columns.add(column);

            return columns;
        };
    }

    /**
     * Expose the {@link AbstractSelectTreeViewer#updateContent} method to the
     * {@link CpuUsageView}.
     */
    @Override
    protected void updateContent(long start, long end, boolean isSelection) {
        super.updateContent(start, end, isSelection);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected ITmfTreeViewerEntry updateElements(long start, long end, boolean isSelection) {
        if (isSelection || start >= end) {
            return null;
        }

        ITmfTrace trace = getTrace();
        long newStart = Long.max(start, getStartTime());
        long newEnd = Long.min(end, getEndTime());
        if (trace == null || newEnd <= newStart) {
            return null;
        }

        ITmfTreeXYDataProvider<@NonNull CpuUsageEntryModel> provider = DataProviderManager.getInstance().getDataProvider(trace,
                CpuUsageDataProvider.ID, ITmfTreeXYDataProvider.class);
        if (provider == null) {
            return null;
        }

        TimeQueryFilter filter = new SelectedCpuQueryFilter(newStart, newEnd, 2, Collections.emptyList(), CpuUsageView.getCpus(trace));
        TmfModelResponse<List<@NonNull CpuUsageEntryModel>> response = provider.fetchTree(filter, null);
        List<@NonNull CpuUsageEntryModel> model = response.getModel();
        if (model == null) {
            return null;
        }

        double time = end - start;
        Map<Long, TmfTreeViewerEntry> map = new HashMap<>();
        TmfTreeViewerEntry root = new TmfTreeViewerEntry(""); //$NON-NLS-1$
        map.put(-1L, root);
        for (CpuUsageEntryModel entryModel : model) {
            CpuUsageEntry cpuUsageEntry = new CpuUsageEntry(entryModel, entryModel.getTime() / time);
            map.put(entryModel.getId(), cpuUsageEntry);
            TmfTreeViewerEntry parent = map.get(entryModel.getParentId());
            if (parent != null) {
                parent.addChild(cpuUsageEntry);
            }
        }

        return root;
    }
}
