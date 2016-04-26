/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiEmptyAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTableEntryAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiChartModel;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiChartModel.ChartType;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiResultTable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTableEntry;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiXYSeriesDescription;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiTimeRange;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.signals.LamiSelectionUpdateSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

import com.google.common.collect.Iterables;

/**
 * Base view showing output of Babeltrace scripts.
 *
 * Implementations can specify which analysis modules to use, which will define
 * the scripts and parameters to use accordingly.
 *
 * @author Alexandre Montplaisir
 */
public final class LamiReportView extends TmfView {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** View ID */
    public static final String VIEW_ID = "org.eclipse.tracecompass.analysis.lami.views.reportview"; //$NON-NLS-1$

    private final @Nullable LamiResultTable fResultTable;

    private @Nullable LamiViewerControl fTableViewerControl;
    private final Set<LamiViewerControl> fPredefGraphViewerControls = new LinkedHashSet<>();
    private final Set<LamiViewerControl> fCustomGraphViewerControls = new LinkedHashSet<>();
    private @Nullable SashForm fSashForm;
    private Set<Integer> fSelectionIndexes;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public LamiReportView() {
        super(VIEW_ID);
        fResultTable = LamiReportViewFactory.getCurrentResultTable();
        fSelectionIndexes = new HashSet<>();
        if (fResultTable != null) {
            fSelectionIndexes = getIndexOfEntriesIntersectingTimerange(checkNotNull(fResultTable), TmfTraceManager.getInstance().getCurrentTraceContext().getSelectionRange());
        }
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

    @Override
    public void createPartControl(@Nullable Composite parent) {
        LamiResultTable resultTable = fResultTable;
        if (resultTable == null || parent == null) {
            return;
        }

        SashForm sf = new SashForm(parent, SWT.NONE);
        fSashForm = sf;
        setPartName(resultTable.getTableClass().getTableTitle());

        /* Prepare the table viewer, which is always present */
        LamiViewerControl tableViewerControl = new LamiViewerControl(sf, resultTable);
        fTableViewerControl = tableViewerControl;

        /* Prepare the predefined graph viewers, if any */
        resultTable.getTableClass().getPredefinedViews()
            .forEach(graphModel -> fPredefGraphViewerControls.add(new LamiViewerControl(sf, resultTable, graphModel)));

        /* Automatically open the table viewer initially */
        tableViewerControl.getToggleAction().run();

        /* Add toolbar buttons */
        IToolBarManager toolbarMgr = getViewSite().getActionBars().getToolBarManager();
        toolbarMgr.add(tableViewerControl.getToggleAction());
        fPredefGraphViewerControls.stream()
            .map(LamiViewerControl::getToggleAction)
            .forEach(toolbarMgr::add);

        IMenuManager menuMgr = getViewSite().getActionBars().getMenuManager();
        IAction newBarChartAction = new NewChartAction(checkNotNull(parent.getShell()), sf, resultTable, ChartType.BAR_CHART);
        IAction newXYScatterAction = new NewChartAction(checkNotNull(parent.getShell()), sf, resultTable, ChartType.XY_SCATTER);

        newBarChartAction.setText(Messages.LamiReportView_NewCustomBarChart);
        newXYScatterAction.setText(Messages.LamiReportView_NewCustomScatterChart);


        IAction clearCustomViewsAction = new Action() {
            @Override
            public void run() {
                fCustomGraphViewerControls.forEach(LamiViewerControl::dispose);
                fCustomGraphViewerControls.clear();
                sf.layout();

            }
        };
        clearCustomViewsAction.setText(Messages.LamiReportView_ClearAllCustomViews);

        menuMgr.add(newBarChartAction);
        menuMgr.add(newXYScatterAction);
        menuMgr.add(new Separator());
        menuMgr.add(clearCustomViewsAction);

        /* Simulate a new external signal to the default viewer */
        LamiSelectionUpdateSignal signal = new LamiSelectionUpdateSignal(LamiReportView.this, fSelectionIndexes, checkNotNull(fResultTable).hashCode());
        TmfSignalManager.dispatchSignal(signal);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void setFocus() {
    }

    @Override
    public void dispose() {
        super.dispose();
        if (fSashForm != null) {
            fSashForm.dispose();
        }
        if (fTableViewerControl != null) {
            fTableViewerControl.dispose();
        }
        fPredefGraphViewerControls.forEach(LamiViewerControl::dispose);
        fCustomGraphViewerControls.forEach(LamiViewerControl::dispose);
    }

    private class NewChartAction extends Action {

        private final Shell icfDialogParentShell;
        private final Composite icfChartViewerParent;
        private final LamiResultTable icfResultTable;
        private boolean icfXLogScale;
        private boolean icfYLogScale;
        private final ChartType icfChartType;

        public NewChartAction(Shell parentShell, Composite chartViewerParent,
                LamiResultTable resultTable, ChartType chartType) {
            icfDialogParentShell = parentShell;
            icfChartViewerParent = chartViewerParent;
            icfResultTable = resultTable;
            icfXLogScale = false;
            icfYLogScale = false;
            icfChartType = chartType;
        }

        @Override
        public void run() {
            int xLogScaleOptionIndex = -1;
            int yLogScaleOptionIndex = -1;

            List<LamiTableEntryAspect> xStringColumn = icfResultTable.getTableClass().getAspects().stream()
                    .filter(aspect -> !(aspect instanceof LamiEmptyAspect))
                    .collect(Collectors.toList());

            /* Get the flattened aspects for Y since mapping an aggregate aspect to y series make no sense so far */
            List<LamiTableEntryAspect> yStringColumn = icfResultTable.getTableClass().getAspects().stream()
                    .filter(aspect -> !(aspect instanceof LamiEmptyAspect))
                    .collect(Collectors.toList());

            switch (icfChartType) {
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

            LamiSeriesDialog dialog = new LamiSeriesDialog(icfDialogParentShell,
                    icfChartType,
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
            dialog.setTitle(icfChartType.toString() + ' ' + Messages.LamiSeriesDialog_creation);

            /* X options per chart type */
            switch (icfChartType) {
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
            switch (icfChartType) {
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

            /* Get X log scale option */
            if (xLogScaleOptionIndex > -1 && xLogScaleOptionIndex < xCheckBoxOptionsResults.length) {
                icfXLogScale = xCheckBoxOptionsResults[xLogScaleOptionIndex];
            }
            /* Get Y log scale option */
            if (yLogScaleOptionIndex > -1 && yLogScaleOptionIndex < yCheckBoxOptionsResults.length) {
                icfYLogScale = yCheckBoxOptionsResults[yLogScaleOptionIndex];
            }

            List<String> xAxisColString = new ArrayList<>();
            List<String> yAxisColString = new ArrayList<>();

            /* Specific chart type result fetching */
            switch (icfChartType) {
            case PIE_CHART:
            case BAR_CHART:
                /* Validate that we only have 1 X aspect */
                if (results.stream()
                        .map(element -> element.getXAspect().getLabel())
                        .distinct()
                        .count() != 1) {
                    throw new IllegalStateException("No unique X axis label for results"); //$NON-NLS-1$
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

            LamiChartModel model = new LamiChartModel(icfChartType,
                    nullToEmptyString(Messages.LamiReportView_Custom),
                    xAxisColString,
                    yAxisColString,
                    icfXLogScale,
                    icfYLogScale);

            LamiViewerControl viewerControl = new LamiViewerControl(icfChartViewerParent, icfResultTable, model);
            fCustomGraphViewerControls.add(viewerControl);
            viewerControl.getToggleAction().run();

            /* Signal the current selection to the newly created graph */
            LamiSelectionUpdateSignal signal = new LamiSelectionUpdateSignal(LamiReportView.this, fSelectionIndexes, checkNotNull(fResultTable).hashCode());
            TmfSignalManager.dispatchSignal(signal);
        }
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
        if (table == null) {
            return;
        }

        if (table.hashCode() != signal.getSignalHash() || equals(signal.getSource())) {
            /* The signal is not for us */
            return;
        }

        Set<Integer> entryIndex = signal.getEntryIndex();

        /*
         * Since most of the external viewers deal only with continuous time
         * ranges and do not allow multi-time range selection, simply signal
         * only when one selection is present.
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
                ITmfTimestamp start = TmfTimestamp.fromNanos(timeRange.getStart());
                ITmfTimestamp end = TmfTimestamp.fromNanos(timeRange.getEnd());
                TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(LamiReportView.this, start, end));
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
        if (table == null) {
            return;
        }

        if (signal.getSource() == this) {
            /* We are the source */
            return;
        }
        TmfTimeRange range = new TmfTimeRange(signal.getBeginTime(), signal.getEndTime());

        Set<Integer> selections = getIndexOfEntriesIntersectingTimerange(table, range);

        /* Update all LamiViewer */
        LamiSelectionUpdateSignal signal1 = new LamiSelectionUpdateSignal(LamiReportView.this, selections, table.hashCode());
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

            TmfTimeRange tempTimeRange = new TmfTimeRange(TmfTimestamp.fromNanos(timerange.getStart()), TmfTimestamp.fromNanos(timerange.getEnd()));
            if (tempTimeRange.getIntersection(range) != null) {
                selections.add(table.getEntries().indexOf(entry));
            }
        }
        return selections;
    }
}
