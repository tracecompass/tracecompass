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
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiEmptyAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTableEntryAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiChartModel;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiChartModel.ChartType;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiResultTable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTableEntry;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiXYSeriesDescription;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiTimeRange;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.signals.LamiSelectionUpdateSignal;
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

        /* Prepare the table viewer, which is always present */
        LamiViewerControl tableViewerControl = new LamiViewerControl(fControl, fResultTable);
        fTableViewerControl = tableViewerControl;

        /* Automatically open the table viewer initially */
        tableViewerControl.getToggleAction().run();

        /* Simulate a new external signal to the default viewer */
        LamiSelectionUpdateSignal signal = new LamiSelectionUpdateSignal(LamiReportViewTabPage.this, fSelectionIndexes, checkNotNull(fResultTable).hashCode());
        TmfSignalManager.dispatchSignal(signal);

        fControl.addDisposeListener(e -> {
            /* Dispose this class's resource */
            fTableViewerControl.dispose();
            clearAllCustomViewers();
            super.dispose();
        });
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void dispose() {
        fControl.dispose();
        /* fControl's disposeListener will dispose the class's resources */
    }

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
    public void createNewCustomChart(ChartType chartType) {
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

        LamiViewerControl viewerControl = new LamiViewerControl(fControl, fResultTable, model);
        fCustomGraphViewerControls.add(viewerControl);
        viewerControl.getToggleAction().run();

        /* Signal the current selection to the newly created graph */
        LamiSelectionUpdateSignal signal = new LamiSelectionUpdateSignal(LamiReportViewTabPage.this,
                fSelectionIndexes, checkNotNull(fResultTable).hashCode());
        TmfSignalManager.dispatchSignal(signal);
    }

    // ------------------------------------------------------------------------
    // Signals
    // ------------------------------------------------------------------------

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

        if (table.hashCode() != signal.getSignalHash() ||
                source == this ||
                /*
                 * Don't forward signals from other tab pages, especially those
                 * from other views.
                 */
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
    }

    /**
     * Signal handler for time range selections
     *
     * @param signal
     *            The received signal
     */
    @TmfSignalHandler
    public void externalUpdateSelection(TmfSelectionRangeUpdatedSignal signal) {
        LamiResultTable table = fResultTable;

        if (signal.getSource() == this) {
            /* We are the source */
            return;
        }
        TmfTimeRange range = new TmfTimeRange(signal.getBeginTime(), signal.getEndTime());

        Set<Integer> selections = getIndexOfEntriesIntersectingTimerange(table, range);

        /* Update all LamiViewer */
        LamiSelectionUpdateSignal signal1 = new LamiSelectionUpdateSignal(LamiReportViewTabPage.this, selections, table.hashCode());
        TmfSignalManager.dispatchSignal(signal1);
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
}
