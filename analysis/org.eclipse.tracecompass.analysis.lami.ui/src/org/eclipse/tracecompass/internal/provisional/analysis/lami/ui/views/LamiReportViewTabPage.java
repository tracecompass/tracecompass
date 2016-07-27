/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.views;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiEmptyAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTableEntryAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiChartModel;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiChartModel.LamiChartType;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiResultTable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTableEntry;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiXYSeriesDescription;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiTimeRange;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.signals.LamiSelectionUpdateSignal;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartData;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartModel;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.signal.ChartSelectionUpdateSignal;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.ui.chart.IChartViewer;
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

    private Set<Integer> fSelectionIndexes;

    private @Nullable IChartViewer fChart;
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
        fSelectionIndexes = new HashSet<>();
        fSelectionIndexes = getIndexOfEntriesIntersectingTimerange(checkNotNull(fResultTable), TmfTraceManager.getInstance().getCurrentTraceContext().getSelectionRange());

        fControl = parent;

        /* Map the current trace selection to our lami entry */
        fSelection = getEntriesIntersectingTimerange(fResultTable, TmfTraceManager.getInstance().getCurrentTraceContext().getSelectionRange());

        /* Prepare the table viewer, which is always present */
        LamiViewerControl tableViewerControl = new LamiViewerControl(fControl, this);
        fTableViewerControl = tableViewerControl;

        /* Automatically open the table viewer initially */
        fTableViewerControl.getToggleAction().run();

        /* Simulate a new external signal to the default viewer */
        LamiSelectionUpdateSignal signal = new LamiSelectionUpdateSignal(LamiReportViewTabPage.this, fSelectionIndexes, this);
        TmfSignalManager.dispatchSignal(signal);
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
        fChart = IChartViewer.createChart(fControl, data, model);
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

    /**
     * Add a new chart viewer to this tab.
     *
     * The method only needs a chart type (currently selected via separate
     * actions), all other information will be found in the result table or in
     * dialogs shown to the user as part of the execution of this method.
     *
     * @param chartType
     *            The type of chart to create
     */
    public void createNewCustomChart(LamiChartType chartType) {
        int xLogScaleOptionIndex = -1;
        int yLogScaleOptionIndex = -1;

        List<LamiTableEntryAspect> xStringColumn = fResultTable.getTableClass().getAspects().stream()
                .filter(aspect -> !(aspect instanceof LamiEmptyAspect))
                .collect(Collectors.toList());

        /* Get the flattened aspects for Y since mapping an aggregate aspect to y series make no sense so far */
        List<LamiTableEntryAspect> yStringColumn = fResultTable.getTableClass().getAspects().stream()
                .filter(aspect -> !(aspect instanceof LamiEmptyAspect))
                .collect(Collectors.toList());

        switch (chartType) {
        case BAR_CHART:
            /* Y value must strictly continous and non timestamp */
            yStringColumn = yStringColumn.stream()
                .filter(aspect -> !aspect.isTimeStamp() && aspect.isContinuous())
                .collect(Collectors.toList());
            break;
        case PIE_CHART:
            break;
        case XY_SCATTER:
            break;
        default:
            break;
        }

        IStructuredContentProvider contentProvider = checkNotNull(ArrayContentProvider.getInstance());

        LamiSeriesDialog dialog = new LamiSeriesDialog(getControl().getShell(),
                chartType,
                xStringColumn,
                yStringColumn,
                contentProvider,
                new LabelProvider() {
                    @Override
                    public String getText(@Nullable Object element) {
                        return ((LamiTableEntryAspect) checkNotNull(element)).getLabel();
                    }
                },
                contentProvider,
                new LabelProvider() {
                    @Override
                    public String getText(@Nullable Object element) {
                        return ((LamiTableEntryAspect) checkNotNull(element)).getLabel();
                    }
                });
        dialog.setTitle(chartType.toString() + ' ' + Messages.LamiSeriesDialog_creation);

        /* X options per chart type */
        switch (chartType) {
        case XY_SCATTER:
            xLogScaleOptionIndex = dialog.addXCheckBoxOption(
                    Messages.LamiSeriesDialog_x_axis + ' ' + Messages.LamiReportView_LogScale,
                    false, new Predicate<LamiTableEntryAspect>() {
                @Override
                public boolean test(@NonNull LamiTableEntryAspect t) {
                    return t.isContinuous() && !t.isTimeStamp();
                }
            });
            break;
        case BAR_CHART:
        case PIE_CHART:
        default:
            break;
        }

        /* Y options per chart type */
        switch (chartType) {
        case BAR_CHART:
        case XY_SCATTER:
            yLogScaleOptionIndex = dialog.addYCheckBoxOption(
                    Messages.LamiSeriesDialog_y_axis + ' ' + Messages.LamiReportView_LogScale,
                    false, new Predicate<LamiTableEntryAspect>() {
                @Override
                public boolean test(@NonNull LamiTableEntryAspect t) {
                    return t.isContinuous() && !t.isTimeStamp();
                }
            });
            break;

        case PIE_CHART:
        default:
            break;
        }

        if (dialog.open() != Window.OK) {
            return;
        }

        List<LamiXYSeriesDescription> results = Arrays.stream(dialog.getResult())
                .map(serie -> (LamiXYSeriesDescription) serie)
                .collect(Collectors.toList());

        boolean[] xCheckBoxOptionsResults = dialog.getXCheckBoxOptionValues();
        boolean[] yCheckBoxOptionsResults = dialog.getYCheckBoxOptionValues();

        boolean isXLogScale = false;
        boolean isYLogScale = false;

        /* Get X log scale option */
        if (xLogScaleOptionIndex > -1 && xLogScaleOptionIndex < xCheckBoxOptionsResults.length) {
            isXLogScale = xCheckBoxOptionsResults[xLogScaleOptionIndex];
        }
        /* Get Y log scale option */
        if (yLogScaleOptionIndex > -1 && yLogScaleOptionIndex < yCheckBoxOptionsResults.length) {
            isYLogScale = yCheckBoxOptionsResults[yLogScaleOptionIndex];
        }

        List<String> xAxisColString = new ArrayList<>();
        List<String> yAxisColString = new ArrayList<>();

        /* Specific chart type result fetching */
        switch (chartType) {
        case PIE_CHART:
        case BAR_CHART:
            /* Validate that we only have 1 X aspect */
            if (results.stream()
                    .map(element -> element.getXAspect().getLabel())
                    .distinct()
                    .count() != 1) {
                throw new IllegalStateException();
            }
            xAxisColString = results.stream()
                    .map(element -> element.getXAspect().getLabel())
                    .distinct()
                    .collect(Collectors.toList());
            break;
        case XY_SCATTER:
            xAxisColString = results.stream()
                    .map(element -> element.getXAspect().getLabel())
                    .collect(Collectors.toList());
            break;
        default:
            break;
        }

        yAxisColString = results.stream()
                .map(element -> element.getYAspect().getLabel())
                .collect(Collectors.toList());

        LamiChartModel model = new LamiChartModel(chartType,
                nullToEmptyString(Messages.LamiReportView_Custom),
                xAxisColString,
                yAxisColString,
                isXLogScale,
                isYLogScale);

        LamiViewerControl viewerControl = new LamiViewerControl(fControl, this, model);
        fCustomGraphViewerControls.add(viewerControl);
        viewerControl.getToggleAction().run();

        /* Signal the current selection to the newly created graph */
        LamiSelectionUpdateSignal signal = new LamiSelectionUpdateSignal(LamiReportViewTabPage.this,
                fSelectionIndexes, this);
        TmfSignalManager.dispatchSignal(signal);
    }

    // ------------------------------------------------------------------------
    // Signals
    // ------------------------------------------------------------------------

    // Lami signals
    /**
     * Signal handler for selection update.
     * Propagate a TmfSelectionRangeUpdatedSignal if possible.
     *
     * @param signal
     *          The selection update signal
     */
    @TmfSignalHandler
    public void updateSelection(LamiSelectionUpdateSignal signal) {
        LamiResultTable table = fResultTable;
        Object source = signal.getSource();

        /*
         * Don't forward signals from other tab pages, especially those
         * from other views/tab page.
         */
        if (this != signal.getSignalKey() ||
                this == source ||
                source instanceof LamiReportViewTabPage) {
            /* The signal is not for us */
            return;
        }

        Set<Integer> entryIndex = signal.getEntryIndex();

        /*
         * Since most of the external viewer deal only with continuous timerange and do not allow multi time range
         * selection simply signal only when only one selection is present.
         */

        if (entryIndex.isEmpty()) {
            /*
             * In an ideal world we would send a null signal to reset all view
             * and simply show no selection. But since this is Tracecompass
             * there is no notion of "unselected state" in most of the viewers so
             * we do not update/clear the last timerange and show false information to the user.
             */
            ChartSelectionUpdateSignal customSignal = new ChartSelectionUpdateSignal(LamiReportViewTabPage.this, fResultTable, Collections.EMPTY_SET);
            TmfSignalManager.dispatchSignal(customSignal);
            return;
        }

        if (entryIndex.size() == 1) {
            int index = Iterables.getOnlyElement(entryIndex).intValue();
            LamiTimeRange timeRange = table.getEntries().get(index).getCorrespondingTimeRange();
            if (timeRange != null) {
                /* Send Range update to other views */
                // TODO: Consider low and high limits of timestamps here.
                Number tsBeginValueNumber = timeRange.getBegin().getValue();
                Number tsEndValueNumber = timeRange.getEnd().getValue();

                if (tsBeginValueNumber != null && tsEndValueNumber != null) {
                    ITmfTimestamp start = TmfTimestamp.fromNanos(tsBeginValueNumber.longValue());
                    ITmfTimestamp end = TmfTimestamp.fromNanos(tsEndValueNumber.longValue());
                    TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(LamiReportViewTabPage.this, start, end));
                }
            }
        }

        fSelectionIndexes = entryIndex;

        // Create the signal for the custom chart
        List<LamiTableEntry> entries = fResultTable.getEntries();
        Set<Object> selectionSet = new HashSet<>();
        for (Integer selectionIndex : entryIndex) {
            selectionSet.add(entries.get(selectionIndex));
        }
        fSelection = selectionSet;
        ChartSelectionUpdateSignal customSignal = new ChartSelectionUpdateSignal(LamiReportViewTabPage.this, fResultTable, selectionSet);
        TmfSignalManager.dispatchSignal(customSignal);

//        /* Update all LamiViewer */
//        LamiSelectionUpdateSignal signal1 = new LamiSelectionUpdateSignal(LamiReportViewTabPage.this, selections, this);
//        TmfSignalManager.dispatchSignal(signal1);
    }

    private static Set<Integer> getIndexOfEntriesIntersectingTimerange(LamiResultTable table, TmfTimeRange range) {
        Set<Integer> selections = new HashSet<>();
        for (LamiTableEntry entry : table.getEntries()) {
            LamiTimeRange timerange = entry.getCorrespondingTimeRange();
            if (timerange == null) {
                /* Return since the table have no timerange */
                return selections;
            }

            // TODO: Consider low and high limits of timestamps here.
            Number tsBeginValueNumber = timerange.getBegin().getValue();
            Number tsEndValueNumber = timerange.getEnd().getValue();

            if (tsBeginValueNumber != null && tsEndValueNumber != null) {
                ITmfTimestamp start = TmfTimestamp.fromNanos(tsBeginValueNumber.longValue());
                ITmfTimestamp end = TmfTimestamp.fromNanos(tsEndValueNumber.longValue());

                TmfTimeRange tempTimeRange = new TmfTimeRange(start, end);
                if (tempTimeRange.getIntersection(range) != null) {
                    selections.add(table.getEntries().indexOf(entry));
                }
            }
        }
        return selections;
    }

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
        IChartViewer chart = fChart;
        if (chart == null) {
            return;
        }

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

        /*
         * Since most of the external viewer deal only with continuous timerange
         * and do not allow multi time range selection simply signal only when
         * only one selection is present.
         */
        if (entries.isEmpty()) {
            /*
             * In an ideal world we would send a null signal to reset all view
             * and simply show no selection. But since this is Tracecompass
             * there is no notion of "unselected state" in most of the viewers
             * so we do not update/clear the last timerange and show false
             * information to the user.
             */
            /* Signal all Lami viewers & views of the selection */
            LamiSelectionUpdateSignal lamiSignal = new LamiSelectionUpdateSignal(LamiReportViewTabPage.this, Collections.EMPTY_SET, this);
            TmfSignalManager.dispatchSignal(lamiSignal);
            return;
        }

        /* Update the selection */
        fSelection = entries;

        // Create the signal for the LAMI selection
        Set<Integer> selectionIndexes = new HashSet<>();
        for (Object entry : entries ) {
            selectionIndexes.add(fResultTable.getEntries().indexOf(entry));
        }

        fSelectionIndexes = selectionIndexes;

        /* Signal all Lami viewers & views of the selection */
        LamiSelectionUpdateSignal lamiSignal = new LamiSelectionUpdateSignal(LamiReportViewTabPage.this, selectionIndexes, this);
        TmfSignalManager.dispatchSignal(lamiSignal);

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

        // Lami signal
        Set<Integer> selections = getIndexOfEntriesIntersectingTimerange(fResultTable, range);

        /* Update all LamiViewer */
        LamiSelectionUpdateSignal signal1 = new LamiSelectionUpdateSignal(LamiReportViewTabPage.this, selections, this);
        TmfSignalManager.dispatchSignal(signal1);

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
