/**********************************************************************
 * Copyright (c) 2016, 2018 Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.memory;

import java.util.Comparator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.os.linux.core.memory.MemoryUsageTreeModel;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.model.filters.FilterTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractSelectTreeViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeColumnDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfGenericTreeEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeColumnData;

import com.google.common.collect.ImmutableList;

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
    private class MemoryLabelProvider extends DataProviderTreeLabelProvider {

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
                if (obj.getModel().getParentId() == -1) {
                    // FIXME: Series total have different style than others. This should come from the data provider
                    fPresentationProvider.setTotalSeries(obj.getModel().getId());
                    return Messages.MemoryUsageTree_Total;
                }
                return Integer.toString(tid);
            }
            return null;
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex != 2 || !(element instanceof TmfGenericTreeEntry)) {
                return null;
            }
            TmfGenericTreeEntry<MemoryUsageTreeModel> obj = (TmfGenericTreeEntry<MemoryUsageTreeModel>) element;
            if (isChecked(element) || obj.getModel().getParentId() == -1) {
                TmfGenericTreeEntry<MemoryUsageTreeModel> entry = (TmfGenericTreeEntry<MemoryUsageTreeModel>) element;
                return getLegendImage(entry.getModel().getId());
            }
            return null;
        }
    }

    // view is filtered by default
    private boolean fFiltered = true;
    private MemoryPresentationProvider fPresentationProvider;

    /**
     * Constructor
     *
     * @param parent
     *            The parent composite that holds this viewer
     * @param id
     *            The memory usage data provider ID.
     */
    public MemoryUsageTreeViewer(Composite parent, String id) {
        super(parent, 2, id);
        setLabelProvider(new MemoryLabelProvider());
    }

    @Override
    protected @NonNull Map<String, Object> getParameters(long start, long end, boolean isSelection) {
        return FetchParametersUtils.filteredTimeQueryToMap(new FilterTimeQueryFilter(Long.min(start, end), Long.max(start, end), 2, fFiltered));
    }

    @Override
    protected ITmfTreeColumnDataProvider getColumnDataProvider() {
        return () -> {
            Comparator<TmfGenericTreeEntry<MemoryUsageTreeModel>> compareTid = Comparator.comparingInt(c -> c.getModel().getTid());
            return ImmutableList.of(
                    createColumn(Messages.MemoryUsageTree_ColumnProcess, Comparator.comparing(TmfGenericTreeEntry::getName)),
                    createColumn(Messages.MemoryUsageTree_ColumnTID, compareTid),
                    new TmfTreeColumnData(Messages.MemoryUsageTree_Legend));
        };
    }

    /**
     * Set the view to filter active threads or not.
     *
     * @param isFiltered
     *            if we filter the active threads or not.
     */
    public void setFiltered(boolean isFiltered) {
        fFiltered = isFiltered;
        updateContent(getWindowStartTime(), getWindowEndTime(), false);
    }

    @Override
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        super.traceSelected(signal);
        fPresentationProvider = MemoryPresentationProvider.getForTrace(signal.getTrace());
    }

    @Override
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        super.traceOpened(signal);
        fPresentationProvider = MemoryPresentationProvider.getForTrace(signal.getTrace());
    }
}