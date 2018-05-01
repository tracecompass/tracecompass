/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfXYDataProvider;
import org.eclipse.tracecompass.tmf.core.presentation.IYAppearance;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ICheckboxTreeViewerListener;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeViewerEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfGenericTreeEntry;
import org.swtchart.Chart;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * XY chart viewer which queries and displays selected entries.
 *
 * @author Loic Prieur-Drevon
 * @since 3.2
 */
public class TmfFilteredXYChartViewer extends TmfCommonXAxisChartViewer implements ICheckboxTreeViewerListener {

    private static final int DEFAULT_SERIES_WIDTH = 2;

    private @NonNull Collection<@NonNull Long> fSelectedIds = Collections.emptyList();

    private final String fId;

    /**
     * Constructor
     *
     * @param parent
     *            The parent composite
     * @param settings
     *            See {@link TmfXYChartSettings} to know what it contains
     * @param id
     *            The ITmfXYDataProvider ID.
     */
    public TmfFilteredXYChartViewer(Composite parent, TmfXYChartSettings settings, String id) {
        super(parent, settings);
        Chart chart = getSwtChart();
        // Avoid displaying chart title and axis titles (to reduce wasted space)
        chart.getLegend().setVisible(false);
        fId = id;
    }

    /**
     * Update the chart depending on the selected entries.
     *
     * @param entries
     *            Counters to display on the chart
     */
    @Override
    public void handleCheckStateChangedEvent(Collection<ITmfTreeViewerEntry> entries) {
        cancelUpdate();

        Iterable<TmfGenericTreeEntry> counterEntries = Iterables.filter(entries, TmfGenericTreeEntry.class);
        Collection<@NonNull Long> selectedIds = Sets.newHashSet(Iterables.transform(counterEntries, e -> e.getModel().getId()));
        if (!selectedIds.containsAll(fSelectedIds)) {
            clearContent();
        }
        fSelectedIds = selectedIds;
        updateContent();
    }

    @TmfSignalHandler
    @Override
    public void traceSelected(@Nullable TmfTraceSelectedSignal signal) {
        super.traceSelected(signal);
        clearContent();
        fSelectedIds.clear();
    }

    @TmfSignalHandler
    @Override
    public void traceClosed(@Nullable TmfTraceClosedSignal signal) {
        if (signal != null && signal.getTrace().equals(getTrace())) {
            fSelectedIds.clear();
        }
        super.traceClosed(signal);
    }

    @Override
    protected TimeQueryFilter createQueryFilter(long start, long end, int nb) {
        return new SelectionTimeQueryFilter(start, end, nb, fSelectedIds);
    }

    /**
     * Get the IDs of the selected entries
     *
     * @return a collection of the IDs of the selected entries
     */
    public @NonNull Collection<@NonNull Long> getSelected() {
        return fSelectedIds;
    }

    @Override
    public @NonNull IYAppearance getSeriesAppearance(@NonNull String seriesName) {
        return getPresentationProvider().getAppearance(seriesName, IYAppearance.Type.LINE, DEFAULT_SERIES_WIDTH);
    }

    @Override
    protected ITmfXYDataProvider initializeDataProvider(ITmfTrace trace) {
        return DataProviderManager.getInstance().getDataProvider(trace, fId, ITmfTreeXYDataProvider.class);
    }

}
