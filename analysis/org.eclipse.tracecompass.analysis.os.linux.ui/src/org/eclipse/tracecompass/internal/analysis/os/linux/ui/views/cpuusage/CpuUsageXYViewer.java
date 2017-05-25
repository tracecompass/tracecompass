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

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.os.linux.core.cpuusage.KernelCpuUsageAnalysis;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.IYSeries;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXLineChartViewer;

import com.google.common.base.Joiner;

/**
 * CPU usage viewer with XY line chart. It displays the total CPU usage and that
 * of the threads selected in the CPU usage tree viewer.
 *
 * @author Geneviève Bastien
 */
public class CpuUsageXYViewer extends TmfCommonXLineChartViewer {

    private static final int NOT_SELECTED = -1;

    private KernelCpuUsageAnalysis fModule = null;

    /* Maps a thread ID to a list of y values */
    private final Map<String, double[]> fYValues = new LinkedHashMap<>();
    /*
     * To avoid up and downs CPU usage when process is in and out of CPU
     * frequently, use a smaller resolution to get better averages.
     */
    private static final double RESOLUTION = 0.4;

    // Timeout between updates in the updateData thread
    private static final long BUILD_UPDATE_TIMEOUT = 500;

    private long fSelectedThread = NOT_SELECTED;

    private final @NonNull Set<@NonNull Integer> fCpus = new TreeSet<>();

    /**
     * Constructor
     *
     * @param parent
     *            parent composite
     */
    public CpuUsageXYViewer(Composite parent) {
        super(parent, Messages.CpuUsageXYViewer_Title, Messages.CpuUsageXYViewer_TimeXAxis, Messages.CpuUsageXYViewer_CpuYAxis);
        setResolution(RESOLUTION);
        getSwtChart().getTitle().setVisible(true);
        getSwtChart().getLegend().setVisible(false);
    }

    @Override
    protected void initializeDataSource() {
        ITmfTrace trace = getTrace();
        if (trace != null) {
            fModule = TmfTraceUtils.getAnalysisModuleOfClass(trace, KernelCpuUsageAnalysis.class, KernelCpuUsageAnalysis.ID);
            if (fModule == null) {
                return;
            }
            fModule.schedule();
        }
    }

    private static double[] zeroFill(int nb) {
        double[] arr = new double[nb];
        Arrays.fill(arr, 0.0);
        return arr;
    }

    @Override
    protected void updateData(long start, long end, int nb, IProgressMonitor monitor) {
        try {
            if (getTrace() == null || fModule == null) {
                return;
            }
            fModule.waitForInitialization();
            ITmfStateSystem ss = fModule.getStateSystem();
            if (ss == null) {
                return;
            }
            double[] xvalues = getXAxis(start, end, nb);
            if (xvalues.length == 0) {
                return;
            }
            setXAxis(xvalues);

            boolean complete = false;
            long currentEnd = Math.max(ss.getStartTime(), start);

            while (!complete && currentEnd < end) {

                if (monitor.isCanceled()) {
                    return;
                }

                long traceStart = Math.max(getStartTime(), ss.getStartTime());
                long traceEnd = getEndTime();
                long offset = getTimeOffset();
                long selectedThread = fSelectedThread;

                complete = ss.waitUntilBuilt(BUILD_UPDATE_TIMEOUT);
                currentEnd = ss.getCurrentEndTime();

                /* Initialize the data */
                Map<String, Long> cpuUsageMap = fModule.getCpuUsageInRange(fCpus, Math.max(start, traceStart), Math.min(end, traceEnd));
                Map<String, String> totalEntries = new HashMap<>();
                fYValues.clear();
                fYValues.put(Messages.CpuUsageXYViewer_Total, zeroFill(xvalues.length));
                String stringSelectedThread = Long.toString(selectedThread);
                if (selectedThread != NOT_SELECTED) {
                    fYValues.put(stringSelectedThread, zeroFill(xvalues.length));
                }

                for (Entry<String, Long> entry : cpuUsageMap.entrySet()) {
                    /*
                     * Process only entries representing the total of all CPUs
                     * and that have time on CPU
                     */
                    if (entry.getValue() == 0) {
                        continue;
                    }
                    if (!entry.getKey().startsWith(KernelCpuUsageAnalysis.TOTAL)) {
                        continue;
                    }
                    String[] strings = entry.getKey().split(KernelCpuUsageAnalysis.SPLIT_STRING, 2);

                    if ((strings.length > 1) && !(strings[1].equals(KernelCpuUsageAnalysis.TID_ZERO))) {
                        /* This is the total cpu usage for a thread */
                        totalEntries.put(strings[1], entry.getKey());
                    }
                }

                double prevX = xvalues[0] - 1;
                long prevTime = (long) prevX + offset;
                /*
                 * make sure that time is in the trace range after double to
                 * long conversion
                 */
                prevTime = Math.max(traceStart, prevTime);
                prevTime = Math.min(traceEnd, prevTime);
                /* Get CPU usage statistics for each x value */
                for (int i = 0; i < xvalues.length; i++) {
                    if (monitor.isCanceled()) {
                        return;
                    }
                    long totalCpu = 0;
                    double x = xvalues[i];
                    long time = (long) x + offset;
                    time = Math.max(traceStart, time);
                    time = Math.min(traceEnd, time);
                    if (time == prevTime) {
                        /*
                         * we need at least 1 time unit to be able to get cpu
                         * usage when zoomed in
                         */
                        prevTime = time - 1;
                    }

                    cpuUsageMap = fModule.getCpuUsageInRange(fCpus, prevTime, time);

                    /*
                     * Calculate the sum of all total entries, and add a data
                     * point to the selected one
                     */
                    for (Entry<String, String> entry : totalEntries.entrySet()) {
                        Long cpuEntry = cpuUsageMap.get(entry.getValue());
                        cpuEntry = cpuEntry != null ? cpuEntry : 0L;

                        totalCpu += cpuEntry;

                        if (entry.getKey().equals(stringSelectedThread)) {
                            /* This is the total cpu usage for a thread */
                            double[] key = checkNotNull(fYValues.get(entry.getKey()));
                            key[i] = (double) cpuEntry / (double) (time - prevTime) * 100;
                        }

                    }
                    double[] key = checkNotNull(fYValues.get(Messages.CpuUsageXYViewer_Total));
                    key[i] = (double) totalCpu / (double) (time - prevTime) * 100;
                    prevTime = time;
                }
                for (Entry<String, double[]> entry : fYValues.entrySet()) {
                    setSeries(entry.getKey(), entry.getValue());
                }
                if (monitor.isCanceled()) {
                    return;
                }
                updateDisplay();
            }
        } catch (StateValueTypeException e) {
            Activator.getDefault().logError("Error updating the data of the CPU usage view", e); //$NON-NLS-1$
        }

    }

    @Override
    protected String getSeriesType(String seriesName) {
        if (seriesName.equals(Messages.CpuUsageXYViewer_Total)) {
            return IYSeries.LINE;
        }
        return IYSeries.AREA;
    }

    /**
     * Set the selected thread ID, which will be graphed in this viewer
     *
     * @param tid
     *            The selected thread ID
     */
    public void setSelectedThread(long tid) {
        cancelUpdate();
        deleteSeries(Long.toString(fSelectedThread));
        fSelectedThread = tid;
        updateContent();
    }

    /**
     * Gets the analysis module
     *
     * @return the {@link KernelCpuUsageAnalysis}
     *
     * @since 2.0
     */
    public KernelCpuUsageAnalysis getModule() {
        return fModule;
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
        cancelUpdate();
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
        cancelUpdate();
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
        cancelUpdate();
        updateContent();
        getSwtChart().getTitle().setText(Messages.CpuUsageView_Title);
    }

    @Override
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        initSelection();
        initCPU();
        super.traceSelected(signal);
    }

    @Override
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        initSelection();
        initCPU();
        super.traceOpened(signal);
    }

    private void initSelection() {
        TmfTraceContext ctx = TmfTraceManager.getInstance().getCurrentTraceContext();
        String data = (String) ctx.getData(CpuUsageView.CPU_USAGE_SELECTED_THREAD);
        long thread = data != null ? Long.valueOf(data) : NOT_SELECTED;
        setSelectedThread(thread);
    }

    private void initCPU() {
        clearCpu();
        TmfTraceContext ctx = TmfTraceManager.getInstance().getCurrentTraceContext();
        Object data =  ctx.getData(CpuUsageView.CPU_USAGE_FOLLOW_CPU);
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
