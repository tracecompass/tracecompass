/*******************************************************************************
 * Copyright (c) 2014, 2018 École Polytechnique de Montréal
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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.os.linux.core.cpuusage.CpuUsageEntryModel;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.cpuusage.CpuUsageDataProvider;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectedCpuQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractSelectTreeViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeColumnDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeViewerEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeColumnData;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeViewerEntry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

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
            if (element instanceof CpuUsageEntry) {
                CpuUsageEntry obj = (CpuUsageEntry) element;
                if (columnIndex == 0) {
                    return obj.getName();
                } else if (columnIndex == 1) {
                    int tid = obj.getModel().getTid();
                    if (tid == CpuUsageDataProvider.TOTAL_SERIES_TID) {
                        return Messages.CpuUsageXYViewer_Total;
                    }
                    return Integer.toString(tid);
                } else if (columnIndex == 2) {
                    return String.format(Messages.CpuUsageComposite_TextPercent, 100 * obj.getPercent());
                } else if (columnIndex == 3) {
                    return NLS.bind(Messages.CpuUsageComposite_TextTime, obj.getModel().getTime());
                }
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
     */
    public CpuUsageTreeViewer(Composite parent) {
        super(parent, 4, CpuUsageDataProvider.ID);
        setLabelProvider(new CpuLabelProvider());
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
    protected @Nullable TimeQueryFilter getFilter(long start, long end, boolean isSelection) {
        long newStart = Long.max(start, getStartTime());
        long newEnd = Long.min(end, getEndTime());

        if (isSelection || newEnd < newStart) {
            return null;
        }
        return new SelectedCpuQueryFilter(newStart, newEnd, 2, Collections.emptyList(), CpuUsageView.getCpus(getTrace()));
    }

    @Override
    protected ITmfTreeViewerEntry modelToTree(long start, long end, List<ITmfTreeDataModel> model) {
        double time = end - start;

        Map<Long, TmfTreeViewerEntry> map = new HashMap<>();
        TmfTreeViewerEntry root = new TmfTreeViewerEntry(""); //$NON-NLS-1$
        map.put(-1L, root);

        for (CpuUsageEntryModel entryModel : Iterables.filter(model, CpuUsageEntryModel.class)) {
            CpuUsageEntry cpuUsageEntry = new CpuUsageEntry(entryModel, entryModel.getTime() / time);
            map.put(entryModel.getId(), cpuUsageEntry);

            TmfTreeViewerEntry parent = map.get(entryModel.getParentId());
            if (parent != null) {
                parent.addChild(cpuUsageEntry);
            }
        }
        return root;
    }

    @Override
    protected ITmfTreeColumnDataProvider getColumnDataProvider() {
        return () -> {
            ImmutableList.Builder<TmfTreeColumnData> columns = ImmutableList.builder();

            columns.add(createColumn(Messages.CpuUsageComposite_ColumnProcess, Comparator.comparing(CpuUsageEntry::getName)));

            Comparator<CpuUsageEntry> tidCompare = Comparator.comparingInt(c -> c.getModel().getTid());
            columns.add(createColumn(Messages.CpuUsageComposite_ColumnTID, tidCompare));

            TmfTreeColumnData percentColumn = createColumn(Messages.CpuUsageComposite_ColumnPercent, Comparator.comparingDouble(CpuUsageEntry::getPercent));
            percentColumn.setPercentageProvider(data -> ((CpuUsageEntry) data).getPercent());
            columns.add(percentColumn);

            Comparator<CpuUsageEntry> timeCompare = Comparator.comparingLong(c -> c.getModel().getTime());
            columns.add(createColumn(Messages.CpuUsageComposite_ColumnTime, timeCompare));

            columns.add(new TmfTreeColumnData(Messages.CpuUsageComposite_ColumnLegend));

            return columns.build();
        };
    }
}
