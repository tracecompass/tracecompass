/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.counters.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.analysis.counters.core.CounterAnalysis;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXLineChartViewer;
import org.swtchart.Chart;
import org.swtchart.ISeries;

/**
 * XY line chart which displays the counters data.
 *
 * @author Matthew Khouzam
 * @author Mikael Ferland
 */
public final class CounterChartViewer extends TmfCommonXLineChartViewer {

    private Integer fQuark;
    private CounterAnalysis fModule;

    /**
     * Constructor
     *
     * @param parent
     *            Parent composite
     */
    public CounterChartViewer(Composite parent) {
        /*
         * Avoid displaying chart title (redundant with the view name) and axis titles
         * (to reduce wasted space).
         */
        super(parent, null, null, null);
        Chart chart = getSwtChart();
        chart.getLegend().setPosition(SWT.BOTTOM);
        chart.getLegend().setVisible(true);
        chart.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
    }

    @Override
    protected void initializeDataSource() {
        ITmfTrace trace = getTrace();
        if (trace != null) {
            fModule = TmfTraceUtils.getAnalysisModuleOfClass(trace, CounterAnalysis.class, CounterAnalysis.ID);
        }

        if (fModule != null) {
            fModule.schedule();
            fModule.waitForInitialization();
        }
    }

    @Override
    protected void updateData(long start, long end, int nb, IProgressMonitor monitor) {
        if (fModule == null) {
            return;
        }

        ITmfStateSystem ss = fModule.getStateSystem();
        if (ss == null || fQuark == null) {
            return;
        }

        double[] xAxis = getXAxis(start, end, nb);
        if (xAxis.length == 1) {
            return;
        }
        setXAxis(xAxis);

        try {
            double[] steps = new double[xAxis.length];
            Object prev = ss.querySingleState(Math.max(ss.getStartTime(), (long) (start - (xAxis[1]))), fQuark).getValue();

            for (int i = 0; i < xAxis.length; i++) {
                Object next = ss.querySingleState(start + (long) xAxis[i] - 1, fQuark).getValue();
                long prevValue = prev instanceof Long ? (long) prev : 0;
                long nextValue = next instanceof Long ? (long) next : 0;
                long stateValue = (prev == null) ? nextValue : nextValue - prevValue;
                steps[i] = (next == null) ? 0 : stateValue;
                prev = next;
            }

            setSeries(ss.getFullAttributePath(fQuark), steps);
        } catch (StateSystemDisposedException e) {
            /*
             * Ignore exception (can take place when closing the trace during update), and
             * exit method.
             */
            return;
        }

        updateDisplay();
    }

    /**
     * Update the chart depending on the selected entries.
     *
     * @param quark
     *            ID of the selected tree element
     */
    public void updateChart(Integer quark) {
        cancelUpdate();
        resetData();
        fQuark = quark;
        updateContent();
    }

    /**
     * Clear the chart.
     */
    private void resetData() {
        for (ISeries serie : getSwtChart().getSeriesSet().getSeries()) {
            deleteSeries(serie.getId());
        }
        fQuark = null;
    }

    @TmfSignalHandler
    @Override
    public void traceSelected(@Nullable TmfTraceSelectedSignal signal) {
        super.traceSelected(signal);
        resetData();
    }
}
