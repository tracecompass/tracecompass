/**********************************************************************
 * Copyright (c) 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Geneviève Bastien - Create and use base class for XY plots
 **********************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.ui.views.memusage;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.common.core.format.DataSizeWithUnitFormat;
import org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.memory.UstMemoryStrings;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.lttng2.ust.core.analysis.memory.UstMemoryAnalysisModule;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXLineChartViewer;
import org.swtchart.Chart;

/**
 * Memory usage view
 *
 * @author Matthew Khouzam
 */
@SuppressWarnings("restriction")
public class MemoryUsageViewer extends TmfCommonXLineChartViewer {

    private TmfStateSystemAnalysisModule fModule = null;

    private final Map<Integer, double[]> fYValues = new HashMap<>();
    private final Map<Integer, Integer> fMemoryQuarks = new HashMap<>();
    private final Map<Integer, String> fSeriesName = new HashMap<>();

    // Timeout between updates in the updateData thread
    private static final long BUILD_UPDATE_TIMEOUT = 500;

    /**
     * Constructor
     *
     * @param parent
     *            parent view
     */
    public MemoryUsageViewer(Composite parent) {
        super(parent, Messages.MemoryUsageViewer_Title, Messages.MemoryUsageViewer_XAxis, Messages.MemoryUsageViewer_YAxis);
        Chart chart = getSwtChart();
        chart.getLegend().setPosition(SWT.LEFT);
        chart.getAxisSet().getYAxis(0).getTick().setFormat(DataSizeWithUnitFormat.getInstance());
    }

    @Override
    protected void initializeDataSource() {
        ITmfTrace trace = getTrace();
        if (trace != null) {
            fModule = TmfTraceUtils.getAnalysisModuleOfClass(trace, TmfStateSystemAnalysisModule.class, UstMemoryAnalysisModule.ID);
            if (fModule == null) {
                return;
            }
            fModule.schedule();
        }
    }

    @Override
    protected void updateData(long start, long end, int nb, IProgressMonitor monitor) {
        try {
            if (getTrace() == null || fModule == null) {
                return;
            }
            if (!fModule.waitForInitialization()) {
                return;
            }
            ITmfStateSystem ss = fModule.getStateSystem();
            /* Don't wait for the module completion, when it's ready, we'll know */
            if (ss == null) {
                return;
            }

            double[] xvalues = getXAxis(start, end, nb);
            setXAxis(xvalues);

            boolean complete = false;
            long currentEnd = start;

            while (!complete && currentEnd < end) {
                if (monitor.isCanceled()) {
                    return;
                }
                complete = ss.waitUntilBuilt(BUILD_UPDATE_TIMEOUT);
                currentEnd = ss.getCurrentEndTime();
                List<Integer> tidQuarks = ss.getSubAttributes(-1, false);
                long traceStart = getStartTime();
                long traceEnd = getEndTime();
                long offset = this.getTimeOffset();

                /* Initialize quarks and series names */
                List<ITmfStateInterval> fullState = ss.queryFullState(start);
                for (int quark : tidQuarks) {
                    fYValues.put(quark, new double[xvalues.length]);
                    fMemoryQuarks.put(quark, ss.getQuarkRelative(quark, UstMemoryStrings.UST_MEMORY_MEMORY_ATTRIBUTE));
                    int procNameQuark = ss.getQuarkRelative(quark, UstMemoryStrings.UST_MEMORY_PROCNAME_ATTRIBUTE);
                    String oldSeriesName = fSeriesName.get(quark);
                    String seriesName = null;
                    try {
                        ITmfStateValue procnameValue = fullState.get(procNameQuark).getStateValue();
                        String procname = ""; //$NON-NLS-1$
                        if (!procnameValue.isNull()) {
                            procname = procnameValue.unboxStr();
                        }
                        seriesName = (procname + ' ' + '(' + ss.getAttributeName(quark) + ')').trim();
                    } catch (TimeRangeException e) {
                        seriesName = '(' + ss.getAttributeName(quark) + ')';
                    }

                    if (oldSeriesName != null && !oldSeriesName.equals(seriesName)) {
                        deleteSeries(oldSeriesName);
                    }
                    fSeriesName.put(quark, seriesName);
                }

                /*
                 * TODO: It should only show active threads in the time range.
                 * If a tid does not have any memory value (only 1 interval in
                 * the time range with value null or 0), then its series should
                 * not be displayed.
                 */
                double yvalue = 0.0;
                for (int i = 0; i < xvalues.length; i++) {
                    if (monitor.isCanceled()) {
                        return;
                    }
                    double x = xvalues[i];
                    long time = (long) x + offset;
                    // make sure that time is in the trace range after double to
                    // long conversion
                    time = time < traceStart ? traceStart : time;
                    time = time > traceEnd ? traceEnd : time;
                    try {
                        fullState = ss.queryFullState(time);
                        for (int quark : tidQuarks) {
                            double[] values = checkNotNull(fYValues.get(quark));

                            Integer memQuark = checkNotNull(fMemoryQuarks.get(quark));
                            yvalue = fullState.get(memQuark.intValue()).getStateValue().unboxLong();
                            values[i] = yvalue;
                        }
                    } catch (TimeRangeException e) {
                        for (int quark : tidQuarks) {
                            double[] values = checkNotNull(fYValues.get(quark));
                            values[i] = 0;
                        }
                    }
                }
                for (int quark : tidQuarks) {
                    setSeries(fSeriesName.get(quark), fYValues.get(quark));
                }
                updateDisplay();
            }
        } catch (AttributeNotFoundException | StateValueTypeException e) {
            Activator.logError("Error updating the data of the Memory usage view", e); //$NON-NLS-1$
        } catch (StateSystemDisposedException e) {
            /* State system is closing down, no point continuing */
        }
    }

}
