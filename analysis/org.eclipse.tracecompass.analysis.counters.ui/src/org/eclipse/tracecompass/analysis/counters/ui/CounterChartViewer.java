/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.counters.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeViewerEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXLineChartViewer;
import org.swtchart.Chart;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.collect.TreeMultimap;

/**
 * XY line chart which displays the counters data.
 *
 * @author Matthew Khouzam
 * @author Mikael Ferland
 */
public final class CounterChartViewer extends TmfCommonXLineChartViewer implements ITreeViewerListener {

    private static final @NonNull Logger LOGGER = TraceCompassLog.getLogger(CounterChartViewer.class);

    private boolean fIsCumulative = false;
    private Collection<ITmfTreeViewerEntry> fEntries = Collections.emptyList();

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
        fEntries = entries;
        updateContent();
    }

    @TmfSignalHandler
    @Override
    public void traceSelected(@Nullable TmfTraceSelectedSignal signal) {
        super.traceSelected(signal);
        clearContent();
        fEntries = Collections.emptyList();
    }

    @Override
    protected void updateData(long start, long end, int nb, IProgressMonitor monitor) {
        // Set the X axis according to the new window range
        double[] xAxis = getXAxis(start, end, nb);
        if (xAxis.length == 1) {
            return;
        }
        setXAxis(xAxis);

        // Associate the counter entries to the state systems
        Iterable<@NonNull CounterTreeViewerEntry> filtered = Iterables.filter(fEntries, CounterTreeViewerEntry.class);
        ImmutableListMultimap<ITmfStateSystem, @NonNull CounterTreeViewerEntry> fStateSystems = Multimaps.index(filtered, CounterTreeViewerEntry::getStateSystem);

        /*
         * TODO: avoid redrawing series already present on chart and iterate over time
         * values first (for performance increase)
         */
        TreeMultimap<Integer, ITmfStateInterval> countersIntervals = TreeMultimap.create(Comparator.naturalOrder(), Comparator.comparingLong(ITmfStateInterval::getStartTime));
        for (Map.Entry<ITmfStateSystem, Collection<CounterTreeViewerEntry>> entry : fStateSystems.asMap().entrySet()) {
            ITmfStateSystem ss = entry.getKey();
            Collection<Long> times = retrieve2dQueryTimestamps(ss, xAxis, start);

            try (ScopeLog log = new ScopeLog(LOGGER, Level.FINE, "CounterChartViewer#querySS")) { //$NON-NLS-1$
                // Extract the quarks for 2D querying
                List<CounterTreeViewerEntry> counters = (List<CounterTreeViewerEntry>) entry.getValue();
                Collection<@NonNull Integer> quarks = Lists.transform(counters, CounterTreeViewerEntry::getQuark);

                Iterable<@NonNull ITmfStateInterval> query2d = ss.query2D(quarks, times);
                for (ITmfStateInterval interval : query2d) {
                    if (monitor.isCanceled()) {
                        return;
                    }

                    countersIntervals.put(interval.getAttribute(), interval);
                }

                for (CounterTreeViewerEntry counter : counters) {
                    if (monitor.isCanceled()) {
                        return;
                    }

                    double[] yValues = buildYValues(countersIntervals.get(counter.getQuark()), xAxis, start);
                    setSeries(counter.getFullPath(), yValues);
                }
            } catch (IndexOutOfBoundsException | TimeRangeException e) {
                LOGGER.log(Level.SEVERE, e.getMessage());
            } catch (StateSystemDisposedException e) {
                /*
                 * Ignore exception (can take place when closing the trace during update), and
                 * continue with the other state system(s)
                 */
            } finally {
                countersIntervals.clear();
            }
        }

        updateDisplay();
    }

    private static Collection<Long> retrieve2dQueryTimestamps(ITmfStateSystem ss, double[] xAxis, long start) {
        Collection<Long> times = new ArrayList<>();

        long stateSystemStartTime = ss.getStartTime();
        long stateSystemEndTime = ss.getCurrentEndTime();
        long prevTime = Math.max(stateSystemStartTime, (long) (start - (xAxis[1])));

        if (prevTime <= stateSystemEndTime) {
            times.add(prevTime);
            for (double t : xAxis) {
                long nextTime = start + (long) t - 1;
                if (nextTime > stateSystemEndTime) {
                    break;
                } else if (nextTime >= stateSystemStartTime) {
                    times.add(nextTime);
                }
            }
        }

        return times;
    }

    private double[] buildYValues(Collection<ITmfStateInterval> countersIntervals, double[] xAxis, long start) {
        double[] yValues = new double[xAxis.length];
        long prevValue = 0l;
        int to = 0;

        for (ITmfStateInterval interval : countersIntervals) {
            int from = Arrays.binarySearch(xAxis, (interval.getStartTime() - start));
            from = (from >= 0) ? from : -1 - from;
            Number value = (Number) interval.getValue();
            long l = value != null ? value.longValue() : 0l;
            if (!fIsCumulative) {
                yValues[from] = (l - prevValue);
            } else {
                /* Fill in all the time stamps that the interval overlaps */
                to = Arrays.binarySearch(xAxis, (interval.getEndTime() - start));
                to = (to >= 0) ? to + 1 : -1 - to;
                Arrays.fill(yValues, from, to, l);
            }
            prevValue = l;
        }

        /* Fill the time stamps after the state system, if any. */
        if (fIsCumulative) {
            Arrays.fill(yValues, to, yValues.length, prevValue);
        }

        return yValues;
    }

}
