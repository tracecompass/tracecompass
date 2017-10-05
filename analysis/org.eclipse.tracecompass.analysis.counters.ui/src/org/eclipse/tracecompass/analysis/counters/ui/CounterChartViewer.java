/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.counters.ui;

import java.util.Collection;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.analysis.counters.core.CompositeCounterDataProvider;
import org.eclipse.tracecompass.internal.analysis.counters.ui.CounterTreeViewerEntry;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectedCounterQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfXYDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.presentation.IYAppearance;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ICheckboxTreeViewerListener;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeViewerEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXAxisChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfXYChartSettings;
import org.swtchart.Chart;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * XY line chart which displays the counters data.
 *
 * @author Matthew Khouzam
 * @author Mikael Ferland
 */
public final class CounterChartViewer extends TmfCommonXAxisChartViewer implements ICheckboxTreeViewerListener {

    private static final int DEFAULT_SERIES_WIDTH = 2;

    private boolean fIsCumulative = false;
    private Multimap<UUID, Integer> fSelectedQuarks = HashMultimap.create();

    /**
     * Constructor
     *
     * @param parent
     *            Parent composite
     */
    public CounterChartViewer(Composite parent, TmfXYChartSettings settings) {
        // Avoid displaying chart title and axis titles (to reduce wasted space)
        super(parent, settings);
        Chart chart = getSwtChart();
        chart.getLegend().setVisible(false);
        chart.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
    }

    /**
     * Display the counters data cumulatively or not.
     */
    public void toggleCumulative() {
        cancelUpdate();
        fIsCumulative ^= true;
        updateContent();
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
        clearContent();

        Multimap<UUID, Integer> selected = HashMultimap.create();
        Iterable<CounterTreeViewerEntry> counterEntries = Iterables.filter(entries, CounterTreeViewerEntry.class);
        counterEntries.forEach(c -> selected.put(c.getTraceID(), c.getQuark()));
        fSelectedQuarks = selected;

        updateContent();
    }

    @TmfSignalHandler
    @Override
    public void traceSelected(@Nullable TmfTraceSelectedSignal signal) {
        super.traceSelected(signal);
        clearContent();
        fSelectedQuarks.clear();
    }

    @TmfSignalHandler
    @Override
    public void traceClosed(@Nullable TmfTraceClosedSignal signal) {
        if (signal != null && signal.getTrace().equals(getTrace())) {
            fSelectedQuarks.clear();
        }
        super.traceClosed(signal);
    }

    @Override
    protected TimeQueryFilter createQueryFilter(long start, long end, int nb) {
        return new SelectedCounterQueryFilter(start, end, nb, fSelectedQuarks, fIsCumulative);
    }

    @Override
    protected void initializeDataProvider() {
        ITmfTrace trace = getTrace();
        ITmfXYDataProvider provider = CompositeCounterDataProvider.create(trace);
        setDataProvider(provider);
    }

    @Override
    public @NonNull IYAppearance getSeriesAppearance(@NonNull String seriesName) {
        return getPresentationProvider().getAppearance(seriesName, IYAppearance.Type.LINE, DEFAULT_SERIES_WIDTH);
    }
}
