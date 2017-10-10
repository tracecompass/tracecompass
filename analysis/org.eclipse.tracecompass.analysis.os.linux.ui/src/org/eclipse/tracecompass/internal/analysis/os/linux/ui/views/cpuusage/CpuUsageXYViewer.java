/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
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

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.os.linux.core.cpuusage.CpuUsageDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectedCpuQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.presentation.IYAppearance;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXAxisChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfXYChartSettings;

import com.google.common.base.Joiner;

/**
 * CPU usage viewer with XY line chart. It displays the total CPU usage and that
 * of the threads selected in the CPU usage tree viewer.
 *
 * @author Geneviève Bastien
 */
@SuppressWarnings("restriction")
public class CpuUsageXYViewer extends TmfCommonXAxisChartViewer {

    private static final @NonNull String NOT_SELECTED = "-1"; //$NON-NLS-1$
    private static final int DEFAULT_SERIES_WIDTH = 1;

    private final @NonNull Set<@NonNull Integer> fCpus = new TreeSet<>();
    private @NonNull String fSelectedThread = NOT_SELECTED;

    /**
     * Constructor
     *
     * @param parent
     *            parent composite
     * @param settings
     *            See {@link TmfXYChartSettings} to know what it contains
     */
    public CpuUsageXYViewer(Composite parent, TmfXYChartSettings settings) {
        super(parent, settings);
        getSwtChart().getTitle().setVisible(true);
        getSwtChart().getLegend().setVisible(false);
    }

    @Override
    protected void initializeDataProvider() {
        CpuUsageDataProvider provider = DataProviderManager.getInstance().getDataProvider(getTrace(), CpuUsageDataProvider.ID, CpuUsageDataProvider.class);
        setDataProvider(provider);
    }

    @Override
    protected TimeQueryFilter createQueryFilter(long start, long end, int nb) {
        return new SelectedCpuQueryFilter(start, end, nb, fSelectedThread, fCpus);
    }

    @Override
    public IYAppearance getSeriesAppearance(@NonNull String seriesName) {
        if (seriesName.equals(Messages.CpuUsageXYViewer_Total)) {
            return getPresentationProvider().getAppearance(seriesName, IYAppearance.Type.LINE, DEFAULT_SERIES_WIDTH);
        }
        return getPresentationProvider().getAppearance(seriesName, IYAppearance.Type.AREA, DEFAULT_SERIES_WIDTH);
    }

    /**
     * Set the selected thread ID, which will be graphed in this viewer
     *
     * @param tid
     *            The selected thread ID
     */
    public void setSelectedThread(@NonNull String tid) {
        fSelectedThread = tid;
        clearContent();
        getPresentationProvider().clear();
        updateContent();
    }

    /**
     * Add a core
     *
     * @param core
     *            the core to add
     * @since 2.0
     */
    public void addCpu(int core) {
        fCpus.add(core);
        updateContent();
        getSwtChart().getTitle().setText(Messages.CpuUsageView_Title + ' ' + getCpuList());
    }

    /**
     * Remove a core
     *
     * @param core
     *            the core to remove
     * @since 2.0
     */
    public void removeCpu(int core) {
        fCpus.remove(core);
        updateContent();
        getSwtChart().getTitle().setText(Messages.CpuUsageView_Title + ' ' + getCpuList());
    }

    private String getCpuList() {
        return Joiner.on(", ").join(fCpus); //$NON-NLS-1$
    }

    /**
     * Clears the cores
     *
     * @since 2.0
     */
    public void clearCpu() {
        fCpus.clear();
        updateContent();
        getSwtChart().getTitle().setText(Messages.CpuUsageView_Title);
    }

    @Override
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        super.traceSelected(signal);
        initSelection();
        initCPU();
    }

    @Override
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        super.traceOpened(signal);
        initSelection();
        initCPU();
    }

    private void initSelection() {
        TmfTraceContext ctx = TmfTraceManager.getInstance().getCurrentTraceContext();
        String data = (String) ctx.getData(CpuUsageView.CPU_USAGE_SELECTED_THREAD);
        String thread = data != null ? data : NOT_SELECTED;
        setSelectedThread(thread);
    }

    private void initCPU() {
        clearCpu();
        TmfTraceContext ctx = TmfTraceManager.getInstance().getCurrentTraceContext();
        Object data = ctx.getData(CpuUsageView.CPU_USAGE_FOLLOW_CPU);
        if (data instanceof Set<?>) {
            Set<?> set = (Set<?>) data;
            for (Object coreObject : set) {
                Integer core = (Integer) coreObject;
                if (core != null && core >= 0) {
                    addCpu(core);
                }
            }
        }
    }
}
