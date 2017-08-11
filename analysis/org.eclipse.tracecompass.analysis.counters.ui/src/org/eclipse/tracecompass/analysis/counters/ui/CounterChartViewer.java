/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.counters.ui;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.ScopeLog;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeViewerEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXLineChartViewer;
import org.swtchart.Chart;

import com.google.common.collect.Iterables;

/**
 * XY line chart which displays the counters data.
 *
 * @author Matthew Khouzam
 * @author Mikael Ferland
 */
public final class CounterChartViewer extends TmfCommonXLineChartViewer implements ITreeViewerListener {

    private static final @NonNull Logger LOGGER = TraceCompassLog.getLogger(CounterChartViewer.class);

    private Iterable<ITmfTreeViewerEntry> fEntries = Collections.emptyList();

    /**
     * Constructor
     *
     * @param parent
     *            Parent composite
     */
    public CounterChartViewer(Composite parent) {
        // Avoid displaying chart title and axis titles (to reduce wasted space)
        super(parent, null, null, null);
        Chart chart = getSwtChart();
        chart.getLegend().setPosition(SWT.BOTTOM);
        chart.getLegend().setVisible(true);
        chart.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
    }

    /**
     * Update the chart depending on the selected entries.
     *
     * @param entries
     *            Counters to display on the chart
     */
    @Override
    public void handleCheckStateChangedEvent(Iterable<ITmfTreeViewerEntry> entries) {
        cancelUpdate();
        clearContent();
        fEntries = entries;
        updateContent();
    }

    @Override
    protected void updateData(long start, long end, int nb, IProgressMonitor monitor) {
        // Set the X axis according to the new window range
        double[] xAxis = getXAxis(start, end, nb);
        if (xAxis.length == 1) {
            return;
        }
        setXAxis(xAxis);

        /*
         * TODO: avoid redrawing series already present on chart and iterate over time
         * values first (for performance increase)
         */
        try (ScopeLog log = new ScopeLog(LOGGER, Level.FINE, "CounterChartViewer#updateData")) { //$NON-NLS-1$
            for (CounterTreeViewerEntry counterEntry : Iterables.filter(fEntries, CounterTreeViewerEntry.class)) {
                if (monitor.isCanceled()) {
                    return;
                }

                // Create the array of values for the series
                double[] steps = new double[xAxis.length];

                ITmfStateSystem ss = counterEntry.getStateSystem();
                Integer quark = counterEntry.getQuark();

                long stateSystemStartTime = ss.getStartTime();
                long stateSystemEndTime = ss.getCurrentEndTime();
                long prevTime = Math.max(stateSystemStartTime, (long) (start - (xAxis[1])));
                if (prevTime <= stateSystemEndTime) {

                    Object prev = ss.querySingleState(prevTime, quark).getValue();
                    long prevValue = prev instanceof Long ? (long) prev : 0;

                    for (int i = 0; i < xAxis.length; i++) {
                        long nextTime = start + (long) xAxis[i] - 1;
                        if (nextTime >= stateSystemStartTime && nextTime <= stateSystemEndTime) {
                            Object next = ss.querySingleState(nextTime, quark).getValue();
                            long nextValue = next instanceof Long ? (long) next : 0;
                            steps[i] = (next == null) ? 0 : nextValue - prevValue;
                            prevValue = nextValue;
                        }
                    }
                }
                setSeries(counterEntry.getFullPath(), steps);
            }
        } catch (StateSystemDisposedException e) {
            /*
             * Ignore exception (can take place when closing the trace during update), and
             * exit method.
             */
            return;
        }

        updateDisplay();
    }

    @TmfSignalHandler
    @Override
    public void traceSelected(@Nullable TmfTraceSelectedSignal signal) {
        super.traceSelected(signal);
        fEntries = Collections.emptyList();
    }

}
