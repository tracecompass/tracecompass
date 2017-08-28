/**********************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.kernelmemoryusage;

import java.util.Collections;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelmemoryusage.KernelMemoryUsageDataProvider;
import org.eclipse.tracecompass.common.core.format.DataSizeWithUnitFormat;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXAxisChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfXYChartSettings;
import org.swtchart.Chart;

/**
 * Memory usage view
 *
 * @author Samuel Gagnon
 * @author Wassim Nasrallah
 */
@SuppressWarnings("restriction")
public class KernelMemoryUsageViewer extends TmfCommonXAxisChartViewer {

    private Long fSelectedEntry = null;

    /**
     * Constructor
     *
     * @param parent
     *            parent view
     * @param settings
     *            See {@link TmfXYChartSettings} to know what it contains
     */
    public KernelMemoryUsageViewer(Composite parent, TmfXYChartSettings settings) {
        super(parent, settings);
        Chart chart = getSwtChart();
        chart.getAxisSet().getYAxis(0).getTick().setFormat(DataSizeWithUnitFormat.getInstance());
        chart.getLegend().setPosition(SWT.BOTTOM);
        chart.getLegend().setVisible(false);
    }

    @Override
    protected void initializeDataProvider() {
        ITmfTrace trace = getTrace();
        KernelMemoryUsageDataProvider provider = DataProviderManager.getInstance().getDataProvider(trace,
                KernelMemoryUsageDataProvider.ID, KernelMemoryUsageDataProvider.class);
        setDataProvider(provider);
    }

    @Override
    protected TimeQueryFilter createQueryFilter(long start, long end, int nb) {
        return fSelectedEntry != null ? new SelectionTimeQueryFilter(start, end, nb, Collections.singleton(fSelectedEntry))
                : new TimeQueryFilter(start, end, nb);
    }

    /**
     * Set the selected entry's ID, which will be graphed in this viewer
     *
     * @param id
     *            The selected entry's ID
     */
    public void setSelectedEntry(Long id) {
        fSelectedEntry = id;
        clearContent();
        getPresentationProvider().clear();
        updateContent();
    }

    @Override
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        super.traceSelected(signal);
        initSelection();
    }

    @Override
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        super.traceOpened(signal);
        initSelection();
    }

    private void initSelection() {
        TmfTraceContext ctx = TmfTraceManager.getInstance().getCurrentTraceContext();
        Object data = ctx.getData(KernelMemoryUsageView.KERNEL_MEMORY);
        Long id = data instanceof Long ? (Long) data : null;
        setSelectedEntry(id);
    }
}
