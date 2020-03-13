/*******************************************************************************
 * Copyright (c) 2014, 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.cpuusage;

import java.text.Format;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.os.linux.core.cpuusage.CpuUsageEntryModel;
import org.eclipse.tracecompass.analysis.os.linux.core.model.OsStrings;
import org.eclipse.tracecompass.common.core.format.SubSecondTimeWithUnitFormat;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.cpuusage.CpuUsageDataProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
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

    private static final Format TIME_FORMATTER = SubSecondTimeWithUnitFormat.getInstance();

    /** Provides label for the CPU usage tree viewer cells */
    protected class CpuLabelProvider extends DataProviderTreeLabelProvider {

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
                    return TIME_FORMATTER.format(obj.getModel().getTime());
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
                    // Total entry, assume it is checked
                    // FIXME: Make it [un]checkable
                    fPresentationProvider.setTotalSeries(model.getId());
                    return getLegendImage(model.getId());
                }
                if (isChecked(element)) {
                    return getLegendImage(model.getId());
                }
            }
            return null;
        }
    }

    private CPUUsagePresentationProvider fPresentationProvider;

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
    protected @NonNull Map<String, Object> getParameters(long start, long end, boolean isSelection) {
        long newStart = Long.max(start, getStartTime());
        long newEnd = Long.min(end, getEndTime());

        if (isSelection || newEnd < newStart) {
            return Collections.emptyMap();
        }

        Map<@NonNull String, @NonNull Object> parameters = FetchParametersUtils.selectionTimeQueryToMap(new SelectionTimeQueryFilter(start, end, 2, Collections.emptyList()));
        parameters.put(CpuUsageDataProvider.REQUESTED_CPUS_KEY, CpuUsageView.getCpus(getTrace()));
        return parameters;
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
            columns.add(createColumn(OsStrings.tid(), tidCompare));

            TmfTreeColumnData percentColumn = createColumn(Messages.CpuUsageComposite_ColumnPercent, Comparator.comparingDouble(CpuUsageEntry::getPercent));
            percentColumn.setPercentageProvider(data -> ((CpuUsageEntry) data).getPercent());
            columns.add(percentColumn);

            Comparator<CpuUsageEntry> timeCompare = Comparator.comparingLong(c -> c.getModel().getTime());
            columns.add(createColumn(Messages.CpuUsageComposite_ColumnTime, timeCompare));

            columns.add(new TmfTreeColumnData(Messages.CpuUsageComposite_ColumnLegend));

            return columns.build();
        };
    }

    @Override
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        super.traceSelected(signal);
        fPresentationProvider = CPUUsagePresentationProvider.getForTrace(signal.getTrace());
    }

    @Override
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        super.traceOpened(signal);
        fPresentationProvider = CPUUsagePresentationProvider.getForTrace(signal.getTrace());
    }

}
