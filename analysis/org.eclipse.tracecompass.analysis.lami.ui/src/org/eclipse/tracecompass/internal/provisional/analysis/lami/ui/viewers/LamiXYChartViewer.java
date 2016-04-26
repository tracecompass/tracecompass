/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Michael Jeanson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.viewers;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.text.Format;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.tracecompass.common.core.format.DecimalUnitFormat;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTableEntryAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiChartModel;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiResultTable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTableEntry;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTimeStampFormat;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.signals.LamiSelectionUpdateSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.swtchart.Chart;
import org.swtchart.ITitle;

import com.google.common.collect.ImmutableList;

/**
 * Abstract XYChart Viewer for LAMI views.
 *
 * @author Michael Jeanson
 *
 */
public abstract class LamiXYChartViewer extends TmfViewer implements ILamiViewer {

    /** Ellipsis character */
    protected static final String ELLIPSIS = "…"; //$NON-NLS-1$

    /**
     * String representing unknown values. Can be present even in numerical
     * aspects!
     */
    protected static final String UNKNOWN = "?"; //$NON-NLS-1$

    /** Zero value */
    protected static final double ZERO = 0.0;

    /** Symbol for seconds (used in the custom ns -> s conversion) */
    private static final String SECONDS_SYMBOL = "s"; //$NON-NLS-1$

    /** Symbol for nanoseconds (used in the custom ns -> s conversion) */
    private static final String NANOSECONDS_SYMBOL = "ns"; //$NON-NLS-1$

    /**
     * Function to use to map Strings read from the data table to doubles for
     * use in SWTChart series.
     */
    protected static final ToDoubleFunction<@Nullable String> DOUBLE_MAPPER = str -> {
        if (str == null || str.equals(UNKNOWN)) {
            return ZERO;
        }
        return Double.parseDouble(str);
    };

    /**
     * List of standard colors
     */
    protected static final List<@NonNull Color> COLORS = ImmutableList.of(
                new Color(Display.getDefault(),  72, 120, 207),
                new Color(Display.getDefault(), 106, 204, 101),
                new Color(Display.getDefault(), 214,  95,  95),
                new Color(Display.getDefault(), 180, 124, 199),
                new Color(Display.getDefault(), 196, 173, 102),
                new Color(Display.getDefault(), 119, 190, 219)
                );

    /**
     * List of "light" colors (when unselected)
     */
    protected static final List<@NonNull Color> LIGHT_COLORS = ImmutableList.of(
                new Color(Display.getDefault(), 173, 195, 233),
                new Color(Display.getDefault(), 199, 236, 197),
                new Color(Display.getDefault(), 240, 196, 196),
                new Color(Display.getDefault(), 231, 213, 237),
                new Color(Display.getDefault(), 231, 222, 194),
                new Color(Display.getDefault(), 220, 238, 246)
                );

    /**
     * Time stamp formatter for intervals in the days range.
     */
    protected static final LamiTimeStampFormat DAYS_FORMATTER = new LamiTimeStampFormat("dd HH:mm"); //$NON-NLS-1$

    /**
     * Time stamp formatter for intervals in the hours range.
     */
    protected static final LamiTimeStampFormat HOURS_FORMATTER = new LamiTimeStampFormat("HH:mm"); //$NON-NLS-1$

    /**
     * Time stamp formatter for intervals in the minutes range.
     */
    protected static final LamiTimeStampFormat MINUTES_FORMATTER = new LamiTimeStampFormat("mm:ss"); //$NON-NLS-1$

    /**
     * Time stamp formatter for intervals in the seconds range.
     */
    protected static final LamiTimeStampFormat SECONDS_FORMATTER = new LamiTimeStampFormat("ss"); //$NON-NLS-1$

    /**
     * Time stamp formatter for intervals in the milliseconds range.
     */
    protected static final LamiTimeStampFormat MILLISECONDS_FORMATTER = new LamiTimeStampFormat("ss.SSS"); //$NON-NLS-1$

    /**
     * Decimal formatter to display nanoseconds as seconds.
     */
    protected static final DecimalUnitFormat NANO_TO_SECS_FORMATTER = new DecimalUnitFormat(0.000000001);

    /**
     * Default decimal formatter.
     */
    protected static final DecimalUnitFormat DECIMAL_FORMATTER = new DecimalUnitFormat();

    private final Listener fResizeListener = event -> {
        /* Refresh the titles to fit the current chart size */
        refreshDisplayTitles();

        /* Refresh the Axis labels to fit the current chart size */
        refreshDisplayLabels();
    };

    private final LamiResultTable fResultTable;
    private final LamiChartModel fChartModel;

    private final Chart fChart;

    private final String fChartTitle;
    private final String fXTitle;
    private final String fYTitle;

    private boolean fSelected;
    private Set<Integer> fSelection;

    /**
     * Creates a Viewer instance based on SWTChart.
     *
     * @param parent
     *            The parent composite to draw in.
     * @param resultTable
     *            The result table containing the data from which to build the
     *            chart
     * @param chartModel
     *            The information about the chart to build
     */
    public LamiXYChartViewer(Composite parent, LamiResultTable resultTable, LamiChartModel chartModel) {
        super(parent);

        fParent = parent;
        fResultTable = resultTable;
        fChartModel = chartModel;
        fSelection = new HashSet<>();

        fChart = new Chart(parent, SWT.NONE);
        fChart.addListener(SWT.Resize, fResizeListener);

        /* Set Chart title */
        fChartTitle = fResultTable.getTableClass().getTableTitle();

        /* Set X axis title */
        if (fChartModel.getXSeriesColumns().size() == 1) {
            /*
             * There is only 1 series in the chart, we will use its name as the
             * Y axis (and hide the legend).
             */
            String seriesName = getChartModel().getXSeriesColumns().get(0);
            // The time duration formatter converts ns to s on the axis
            if (NANOSECONDS_SYMBOL.equals(getXAxisAspects().get(0).getUnits())) {
                seriesName = getXAxisAspects().get(0).getName() + " (" + SECONDS_SYMBOL + ')'; //$NON-NLS-1$
            }
            fXTitle = seriesName;
        } else {
            /*
             * There are multiple series in the chart, if they all share the same
             * units, display that.
             */
            long nbDiffAspects = getXAxisAspects().stream()
                .map(aspect -> aspect.getUnits())
                .distinct()
                .count();

            String units = getXAxisAspects().get(0).getUnits();
            if (nbDiffAspects == 1 && units != null) {
                /* All aspects use the same unit type */

                // The time duration formatter converts ns to s on the axis
                if (NANOSECONDS_SYMBOL.equals(units)) {
                    units = SECONDS_SYMBOL;
                }
                fXTitle = Messages.LamiViewer_DefaultValueName + " (" + units + ')'; //$NON-NLS-1$
            } else {
                /* Various unit types, just say "Value" */
                fXTitle = nullToEmptyString(Messages.LamiViewer_DefaultValueName);
            }
        }

        /* Set Y axis title */
        if (fChartModel.getYSeriesColumns().size() == 1) {
            /*
             * There is only 1 series in the chart, we will use its name as the
             * Y axis (and hide the legend).
             */
            String seriesName = getChartModel().getYSeriesColumns().get(0);
            // The time duration formatter converts ns to s on the axis
            if (NANOSECONDS_SYMBOL.equals(getYAxisAspects().get(0).getUnits())) {
                seriesName = getYAxisAspects().get(0).getName() + " (" + SECONDS_SYMBOL + ')'; //$NON-NLS-1$
            }
            fYTitle = seriesName;
            fChart.getLegend().setVisible(false);
        } else {
            /*
             * There are multiple series in the chart, if they all share the same
             * units, display that.
             */
            long nbDiffAspects = getYAxisAspects().stream()
                .map(aspect -> aspect.getUnits())
                .distinct()
                .count();

            String units = getYAxisAspects().get(0).getUnits();
            if (nbDiffAspects == 1 && units != null) {
                /* All aspects use the same unit type */

                // The time duration formatter converts ns to s on the axis
                if (NANOSECONDS_SYMBOL.equals(units)) {
                    units = SECONDS_SYMBOL;
                }
                fYTitle = Messages.LamiViewer_DefaultValueName + " (" + units + ')'; //$NON-NLS-1$
            } else {
                /* Various unit types, just say "Value" */
                fYTitle = nullToEmptyString(Messages.LamiViewer_DefaultValueName);
            }

            /* Put legend at the bottom */
            fChart.getLegend().setPosition(SWT.BOTTOM);
        }

        /* Set all titles and labels font color to black */
        fChart.getTitle().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
        fChart.getAxisSet().getXAxis(0).getTitle().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
        fChart.getAxisSet().getYAxis(0).getTitle().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
        fChart.getAxisSet().getXAxis(0).getTick().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
        fChart.getAxisSet().getYAxis(0).getTick().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));

        /* Set X label 90 degrees */
        fChart.getAxisSet().getXAxis(0).getTick().setTickLabelAngle(90);

        /* Refresh the titles to fit the current chart size */
        refreshDisplayTitles();

        fChart.addDisposeListener(e -> {
                /* Dispose resources of this class */
                LamiXYChartViewer.super.dispose();
        });
    }

    /**
     * Util method to check if a list of aspects are all continuous.
     *
     * @param axisAspects
     *            The list of aspects to check.
     * @return true is all aspects are continuous, otherwise false.
     */
    protected static boolean areAspectsContinuous(List<LamiTableEntryAspect> axisAspects) {
        return axisAspects.stream().allMatch(aspect -> aspect.isContinuous());
    }

    /**
     * Util method to check if a list of aspects are all time stamps.
     *
     * @param axisAspects
     *            The list of aspects to check.
     * @return true is all aspects are time stamps, otherwise false.
     */
    protected static boolean areAspectsTimeStamp(List<LamiTableEntryAspect> axisAspects) {
        return axisAspects.stream().allMatch(aspect -> aspect.isTimeStamp());
    }

    /**
     * Util method to check if a list of aspects are all time durations.
     *
     * @param axisAspects
     *            The list of aspects to check.
     * @return true is all aspects are time durations, otherwise false.
     */
    protected static boolean areAspectsTimeDuration(List<LamiTableEntryAspect> axisAspects) {
        return axisAspects.stream().allMatch(aspect -> aspect.isTimeDuration());
    }

    /**
     * Util method that will return a formatter based on the aspects linked to an axis
     *
     * If all aspects are time stamps, return a timestamp formatter tuned to the interval.
     * If all aspects are time durations, return the nanoseconds to seconds formatter.
     * Otherwise, return the generic decimal formatter.
     *
     * @param axisAspects
     *            The list of aspects of the axis.
     * @param entries
     *            The list of entries of the chart.
     * @return a formatter for the axis.
     */
    protected static Format getContinuousAxisFormatter(List<LamiTableEntryAspect> axisAspects, List<LamiTableEntry> entries) {

        if (areAspectsTimeStamp(axisAspects)) {
            /* Set a TimeStamp formatter depending on the duration between the first and last value */
            double max = Double.MIN_VALUE;
            double min = Double.MAX_VALUE;

            for (LamiTableEntry entry : entries) {
                for (LamiTableEntryAspect aspect : axisAspects) {
                    Double current = aspect.resolveDouble(entry);
                    if (current != null) {
                        max = Math.max(max, current);
                        min = Math.min(min, current);
                    }
                }
            }
            long duration = (long) max - (long) min;

            if (duration > TimeUnit.DAYS.toNanos(1)) {
                return DAYS_FORMATTER;
            } else if (duration > TimeUnit.HOURS.toNanos(1)) {
                return HOURS_FORMATTER;
            } else if (duration > TimeUnit.MINUTES.toNanos(1)) {
                return MINUTES_FORMATTER;
            } else if (duration > TimeUnit.SECONDS.toNanos(15)) {
                return SECONDS_FORMATTER;
            } else {
                return MILLISECONDS_FORMATTER;
            }
        } else if (areAspectsTimeDuration(axisAspects)) {
            /* Set the time duration formatter */
            return NANO_TO_SECS_FORMATTER;

        } else {
            /* For other numeric aspects, use the default decimal unit formatter */
            return DECIMAL_FORMATTER;
        }
    }

    /**
     * Get the chart result table.
     *
     * @return The chart result table.
     */
    protected LamiResultTable getResultTable() {
        return fResultTable;
    }

    /**
     * Get the chart model.
     *
     * @return The chart model.
     */
    protected LamiChartModel getChartModel() {
        return fChartModel;
    }

    /**
     * Get the chart object.
     * @return The chart object.
     */
    protected Chart getChart() {
        return fChart;
    }

    /**
     * Is a selection made in the chart.
     *
     * @return true if there is a selection.
     */
    protected boolean isSelected() {
        return fSelected;
    }

    /**
     * Set the selection index.
     *
     * @param selection the index to select.
     */
    protected void setSelection(Set<Integer> selection) {
        fSelection = selection;
        fSelected = !selection.isEmpty();
    }

    /**
     * Unset the chart selection.
     */
    protected void unsetSelection() {
        fSelection.clear();
        fSelected = false;
    }

    /**
     * Get the current selection index.
     *
     * @return the current selection index.
     */
    protected Set<Integer> getSelection() {
        return fSelection;
    }

    @Override
    public @Nullable Control getControl() {
        return fChart.getParent();
    }

    @Override
    public void refresh() {
        Display.getDefault().asyncExec(() -> {
            if (!fChart.isDisposed()) {
                fChart.redraw();
            }
        });
    }

    @Override
    public void dispose() {
        fChart.dispose();
        /* The control's DisposeListener will call super.dispose() */
    }

    /**
     * Get a list of all the aspect of the Y axis.
     *
     * @return The aspects for the Y axis
     */
    protected List<LamiTableEntryAspect> getYAxisAspects() {

        List<LamiTableEntryAspect> yAxisAspects = new ArrayList<>();

        for (String colName : getChartModel().getYSeriesColumns()) {
            yAxisAspects.add(checkNotNull(getAspectFromName(getResultTable().getTableClass().getAspects(), colName)));
        }

        return yAxisAspects;
    }

    /**
     * Get a list of all the aspect of the X axis.
     *
     * @return The aspects for the X axis
     */
    protected List<LamiTableEntryAspect> getXAxisAspects() {

        List<LamiTableEntryAspect> xAxisAspects = new ArrayList<>();

        for (String colName : getChartModel().getXSeriesColumns()) {
            xAxisAspects.add(checkNotNull(getAspectFromName(getResultTable().getTableClass().getAspects(), colName)));
        }

        return xAxisAspects;
    }

    /**
     * Set the ITitle object text to a substring of canonicalTitle that when
     * rendered in the chart will fit maxPixelLength.
     */
    private void refreshDisplayTitle(ITitle title, String canonicalTitle, int maxPixelLength) {
        if (title.isVisible()) {

            String newTitle = canonicalTitle;

            /* Get the title font */
            Font font = title.getFont();

            GC gc = new GC(fParent);
            gc.setFont(font);

            /* Get the length and height of the canonical title in pixels */
            Point pixels = gc.stringExtent(canonicalTitle);

            /*
             * If the title is too long, generate a shortened version based on the
             * average character width of the current font.
             */
            if (pixels.x > maxPixelLength) {
                int charwidth = gc.getFontMetrics().getAverageCharWidth();

                int minimum = 3;

                int strLen = ((maxPixelLength / charwidth) - minimum);

                if (strLen > minimum) {
                    newTitle = canonicalTitle.substring(0, strLen) + ELLIPSIS;
                } else {
                    newTitle = ELLIPSIS;
                }
            }

            title.setText(newTitle);

            // Cleanup
            gc.dispose();
        }
    }

    /**
     * Refresh the Chart, XAxis and YAxis titles to fit the current
     * chart size.
     */
    private void refreshDisplayTitles() {
        Rectangle chartRect = fChart.getClientArea();
        Rectangle plotRect = fChart.getPlotArea().getClientArea();

        ITitle chartTitle = checkNotNull(fChart.getTitle());
        refreshDisplayTitle(chartTitle, fChartTitle, chartRect.width);

        ITitle xTitle = checkNotNull(fChart.getAxisSet().getXAxis(0).getTitle());
        refreshDisplayTitle(xTitle, fXTitle, plotRect.width);

        ITitle yTitle = checkNotNull(fChart.getAxisSet().getYAxis(0).getTitle());
        refreshDisplayTitle(yTitle, fYTitle, plotRect.height);
    }

    /**
     * Get the aspect with the given name
     *
     * @param aspects
     *            The list of aspects to search into
     * @param aspectName
     *            The name of the aspect we are looking for
     * @return The corresponding aspect
     */
    protected static @Nullable LamiTableEntryAspect getAspectFromName(List<LamiTableEntryAspect> aspects, String aspectName) {
        for (LamiTableEntryAspect lamiTableEntryAspect : aspects) {

            if (lamiTableEntryAspect.getLabel().equals(aspectName)) {
                return lamiTableEntryAspect;
            }
        }

        return null;
    }

    /**
     * Refresh the axis labels to fit the current chart size.
     */
    protected abstract void refreshDisplayLabels();

    /**
     * Redraw the chart.
     */
    protected void redraw() {
        refresh();
    }

    /**
     * Signal handler for selection update.
     *
     * @param signal
     *          The selection update signal
     */
    @TmfSignalHandler
    public void updateSelection(LamiSelectionUpdateSignal signal) {
        if (getResultTable().hashCode() != signal.getSignalHash() || equals(signal.getSource())) {
            /* The signal is not for us */
            return;
        }
        setSelection(signal.getEntryIndex());

        redraw();
    }
}
