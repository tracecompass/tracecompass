/**********************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.io.diskioactivity;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.Disk;
import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.InputOutputAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.InputOutputInformationProvider;
import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.IoOperationType;
import org.eclipse.tracecompass.common.core.format.DataSpeedWithUnitFormat;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.IYSeries;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXLineChartViewer;
import org.swtchart.Chart;

/**
 * Disk IO Activity viewer, shows read and write bandwidth used over time.
 *
 * @author Houssem Daoud
 */
public class DisksIOActivityViewer extends TmfCommonXLineChartViewer {

    // Timeout between updates in the updateData thread
    private static final long BUILD_UPDATE_TIMEOUT = 500;
    private static final double RESOLUTION = 0.2;
    private static final int BYTES_PER_SECTOR = 512;
    private static final int SECOND_TO_NANOSECOND = (int) Math.pow(10, 9);

    private @Nullable InputOutputAnalysisModule fModule = null;

    /**
     * Constructor
     *
     * @param parent
     *            parent view
     */
    public DisksIOActivityViewer(@Nullable Composite parent) {
        super(parent, Messages.DiskIOActivityViewer_Title, Messages.DiskIOActivityViewer_XAxis, Messages.DiskIOActivityViewer_YAxis);
        setResolution(RESOLUTION);
        Chart chart = getSwtChart();
        chart.getAxisSet().getYAxis(0).getTick().setFormat(DataSpeedWithUnitFormat.getInstance());
        chart.getLegend().setPosition(SWT.LEFT);
    }

    @Override
    protected String getSeriesType(@NonNull String seriesName) {
        return IYSeries.AREA;
    }

    @Override
    protected void initializeDataSource() {
        ITmfTrace trace = getTrace();
        if (trace != null) {
            InputOutputAnalysisModule module = TmfTraceUtils.getAnalysisModuleOfClass(trace, InputOutputAnalysisModule.class, InputOutputAnalysisModule.ID);
            if (module == null) {
                return;
            }
            module.schedule();
            fModule = module;
        }
    }

    @Override
    protected void updateData(long start, long end, int nb, @Nullable IProgressMonitor monitor) {
        InputOutputAnalysisModule module = fModule;
        if (getTrace() == null || module == null) {
            return;
        }
        if (!module.waitForInitialization()) {
            return;
        }
        ITmfStateSystem ss = module.getStateSystem();
        if (ss == null) {
            return;
        }

        double[] xvalues = getXAxis(start, end, nb);
        setXAxis(xvalues);

        boolean complete = false;
        long currentEnd = start;

        while (!complete && currentEnd < end) {
            if (monitor != null && monitor.isCanceled()) {
                return;
            }
            complete = ss.waitUntilBuilt(BUILD_UPDATE_TIMEOUT);
            currentEnd = ss.getCurrentEndTime();
            long traceStart = getStartTime();
            long traceEnd = getEndTime();
            long offset = this.getTimeOffset();

            Collection<Disk> disks = InputOutputInformationProvider.getDisks(module);

            for (Disk disk : disks) {
                if (!disk.hasActivity()) {
                    continue;
                }

                String diskName = disk.getDiskName();

                /* Initialize quarks and series names */
                double[] yValuesWritten = new double[xvalues.length];
                double[] yValuesRead = new double[xvalues.length];
                String seriesNameWritten = new String(diskName + Messages.DisksIOActivityViewer_Write);
                String seriesNameRead = new String(diskName + Messages.DisksIOActivityViewer_Read);

                double prevX = xvalues[0];
                long prevTime = (long) prevX + offset;
                /*
                 * make sure that time is in the trace range after double to
                 * long conversion
                 */
                prevTime = Math.max(traceStart, prevTime);
                prevTime = Math.min(traceEnd, prevTime);
                long prevCountRead = disk.getSectorsAt(prevTime, IoOperationType.READ);
                long prevCountWrite = disk.getSectorsAt(prevTime, IoOperationType.WRITE);
                for (int i = 1; i < xvalues.length; i++) {
                    if (monitor != null && monitor.isCanceled()) {
                        return;
                    }
                    double x = xvalues[i];
                    long time = (long) x + offset;
                    time = Math.max(traceStart, time);
                    time = Math.min(traceEnd, time);
                    try {
                        long count = disk.getSectorsAt(time, IoOperationType.WRITE);
                        yValuesWritten[i] = (count - prevCountWrite) * BYTES_PER_SECTOR / ((double) (time - prevTime) / SECOND_TO_NANOSECOND);
                        prevCountWrite = count;
                        count = disk.getSectorsAt(time, IoOperationType.READ);
                        yValuesRead[i] = (count - prevCountRead) * BYTES_PER_SECTOR / ((double) (time - prevTime) / SECOND_TO_NANOSECOND);
                        prevCountRead = count;
                    } catch (TimeRangeException e) {
                        yValuesWritten[i] = 0;
                        yValuesRead[i] = 0;
                    }
                    prevTime = time;
                }
                setSeries(seriesNameRead, yValuesRead);
                setSeries(seriesNameWritten, yValuesWritten);
                if (monitor != null && monitor.isCanceled()) {
                    return;
                }
                updateDisplay();
            }
        }
    }
}
