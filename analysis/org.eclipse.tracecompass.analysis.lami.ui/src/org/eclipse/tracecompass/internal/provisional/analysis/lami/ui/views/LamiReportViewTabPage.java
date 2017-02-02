/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.views;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiResultTable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTableEntry;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiTimeRange;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartData;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartModel;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.signal.ChartSelectionUpdateSignal;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.ui.dialog.ChartMakerDialog;
import org.eclipse.tracecompass.tmf.core.component.TmfComponent;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;

import com.google.common.collect.Iterables;

/**
 * Sub-view of a {@link LamiReportView} that shows the contents of one table of
 * the analysis report. While it is not a View object directly, its
 * responsibilities are the same.
 *
 * @author Alexandre Montplaisir
 * @author Jonathan Rajotte-Julien
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public final class LamiReportViewTabPage extends TmfComponent {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final LamiResultTable fResultTable;
    private final LamiViewerControl fTableViewerControl;
    private final Set<LamiViewerControl> fCustomGraphViewerControls = new LinkedHashSet<>();
    private final Composite fControl;

    private Set<Object> fSelection;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param parent
     *            Parent composite
     * @param table
     *            The result table to display in this tab
     */
    public LamiReportViewTabPage(Composite parent, LamiResultTable table) {
        super(table.getTableClass().getTableTitle());

        fResultTable = table;

        fControl = parent;

        /* Map the current trace selection to our lami entry */
        fSelection = getEntriesIntersectingTimerange(fResultTable, TmfTraceManager.getInstance().getCurrentTraceContext().getSelectionRange());

        /* Prepare the table viewer, which is always present */
        LamiViewerControl tableViewerControl = new LamiViewerControl(fControl, this);
        fTableViewerControl = tableViewerControl;

        /* Automatically open the table viewer initially */
        fTableViewerControl.getToggleAction().run();

        /* Simulate a new external signal to the default viewer */
        ChartSelectionUpdateSignal chartSignal = new ChartSelectionUpdateSignal(this, fResultTable, fSelection);
        TmfSignalManager.dispatchSignal(chartSignal);

        /* Dispose this class's resource */
        fControl.addDisposeListener(e -> {
            fTableViewerControl.dispose();
            clearAllCustomViewers();
            super.dispose();
        });
    }

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public void dispose() {
        /* fControl's listner will dispose other resources */
        fControl.dispose();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * This method is used for creating a chart from the result table of the
     * analyse. It uses the custom charts plugin to configure and create the
     * chart.
     */
    public void createNewCustomChart() {
        Shell shell = this.getControl().getShell();
        if (shell == null) {
            return;
        }

        /* Open the chart maker dialog */
        ChartMakerDialog dialog = new ChartMakerDialog(shell, fResultTable);
        if (dialog.open() != Window.OK) {
            return;
        }

        /* Make sure the data for making a chart was generated */
        ChartData data = dialog.getDataSeries();
        ChartModel model = dialog.getChartModel();
        if (data == null || model == null) {
            return;
        }

        /* Make a chart with the factory constructor */
        LamiViewerControl viewerControl = new LamiViewerControl(fControl, data, model);
        fCustomGraphViewerControls.add(viewerControl);
        viewerControl.getToggleAction().run();
        /* Signal the current selection to the newly created graph */
        ChartSelectionUpdateSignal signal = new ChartSelectionUpdateSignal(LamiReportViewTabPage.this,
                fResultTable, fSelection);
        TmfSignalManager.dispatchSignal(signal);


    }

    /**
     * Clear all the custom graph viewers in this tab.
     */
    public void clearAllCustomViewers() {
        fCustomGraphViewerControls.forEach(LamiViewerControl::dispose);
        fCustomGraphViewerControls.clear();
    }

    /**
     * Toggle the display of the table viewer in this tab. This shows it if it
     * is currently hidden, and vice versa.
     */
    public void toggleTableViewer() {
        fTableViewerControl.getToggleAction().run();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Get the SWT control associated with this tab page.
     *
     * @return The SWT control
     */
    public Composite getControl() {
        return fControl;
    }

    /**
     * Get the result table shown in this tab.
     *
     * @return The report result table
     */
    public LamiResultTable getResultTable() {
        return fResultTable;
    }

    // ------------------------------------------------------------------------
    // Signals
    // ------------------------------------------------------------------------

    // Custom chart signals
    /**
     * Signal handler for a chart selection update. It will try to propagate a
     * {@link TmfSelectionRangeUpdatedSignal} if possible.
     *
     * @param signal
     *            The selection update signal
     */
    @TmfSignalHandler
    public void updateSelection(ChartSelectionUpdateSignal signal) {

        /* Make sure we are not sending a signal to ourself */
        if (signal.getSource() == this) {
            return;
        }

        /* Make sure the signal comes from the data provider's scope */
        if (fResultTable.hashCode() != signal.getDataProvider().hashCode()) {
            return;
        }

        /* Find which index row has been selected */
        Set<Object> entries = signal.getSelectedObject();

        /* Update the selection */
        fSelection = entries;

        /* Only propagate to all TraceCompass if there is a single selection */
        if (entries.size() == 1) {
            LamiTableEntry entry = (LamiTableEntry) Iterables.getOnlyElement(entries);

            /* Make sure the selection represent a time range */
            LamiTimeRange timeRange = entry.getCorrespondingTimeRange();
            if (timeRange == null) {
                return;
            }

            /* Get the timestamps from the time range */
            /**
             * TODO: Consider low and high limits of timestamps here.
             */
            Number tsBeginValueNumber = timeRange.getBegin().getValue();
            Number tsEndValueNumber = timeRange.getEnd().getValue();
            if(tsBeginValueNumber == null || tsEndValueNumber == null) {
                return;
            }

            /* Send Range update to other views */
            ITmfTimestamp start = TmfTimestamp.fromNanos(tsBeginValueNumber.longValue());
            ITmfTimestamp end = TmfTimestamp.fromNanos(tsEndValueNumber.longValue());
            TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(this, start, end));
        }

    }

    /**
     * Signal handler for a trace selection range update signal. It will try to
     * map the external selection to our lami table entry.
     *
     * @param signal
     *            The received signal
     */
    @TmfSignalHandler
    public void externalUpdateSelectionCustomCharts(TmfSelectionRangeUpdatedSignal signal) {
        /* Make sure we are not sending a signal to ourself */
        if (signal.getSource() == this) {
            return;
        }

        TmfTimeRange range = new TmfTimeRange(signal.getBeginTime(), signal.getEndTime());

        // Custom chart signal
        /* Find which lami table entry intersects the signal */
        Set<Object> selection = getEntriesIntersectingTimerange(fResultTable, range);

        /* Update all LamiViewer */
        ChartSelectionUpdateSignal updateSignal = new ChartSelectionUpdateSignal(this, fResultTable, selection);
        TmfSignalManager.dispatchSignal(updateSignal);
    }

    // ------------------------------------------------------------------------
    // Util methods
    // ------------------------------------------------------------------------

    /**
     * Util method that returns {@link LamiTableEntry} that intersects a
     * {@link TmfTimeRange}.
     *
     * @param table
     *            The result table to search for entries
     * @param range
     *            The time range itself
     * @return The set of entries that intersect with the time range
     */
    private static Set<Object> getEntriesIntersectingTimerange(LamiResultTable table, TmfTimeRange range) {
        Set<Object> entries = new HashSet<>();
        for (LamiTableEntry entry : table.getEntries()) {
            LamiTimeRange lamiTimeRange = entry.getCorrespondingTimeRange();

            /* Make sure the table has time ranges */
            if (lamiTimeRange == null) {
                return entries;
            }

            /* Get the timestamps from the time range */
            /**
             * TODO: Consider low and high limits of timestamps here.
             */
            Number tsBeginValueNumber = lamiTimeRange.getBegin().getValue();
            Number tsEndValueNumber = lamiTimeRange.getEnd().getValue();
            if(tsBeginValueNumber == null || tsEndValueNumber == null) {
                return entries;
            }

            /* Convert the timestamps into TMF timestamps */
            ITmfTimestamp start = TmfTimestamp.fromNanos(tsBeginValueNumber.longValue());
            ITmfTimestamp end = TmfTimestamp.fromNanos(tsEndValueNumber.longValue());

            /* Add iff the time range intersects the the signal */
            TmfTimeRange tempTimeRange = new TmfTimeRange(start, end);
            if (tempTimeRange.getIntersection(range) != null) {
                entries.add(entry);
            }
        }

        return entries;
    }
}
