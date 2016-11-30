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

import java.math.BigDecimal;
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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.tracecompass.common.core.format.DecimalUnitFormat;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTableEntryAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiChartModel;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiResultTable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTableEntry;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.format.LamiDecimalUnitFormat;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.format.LamiTimeStampFormat;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.signals.LamiSelectionUpdateSignal;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.views.LamiReportViewTabPage;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
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

    /** Zero long value */
    protected static final long ZERO_LONG = 0L;
    /** Zero double value */
    protected static final double ZERO_DOUBLE = 0.0;

    /**
     * Function to use to map Strings read from the data table to doubles for
     * use in SWTChart series.
     */
    protected static final ToDoubleFunction<@Nullable String> DOUBLE_MAPPER = str -> {
        if (str == null || str.equals(UNKNOWN)) {
            return ZERO_LONG;
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
    protected static final DecimalUnitFormat NANO_TO_SECS_FORMATTER = new LamiDecimalUnitFormat(0.000000001);

    /**
     * Default decimal formatter.
     */
    protected static final DecimalUnitFormat DECIMAL_FORMATTER = new LamiDecimalUnitFormat();

    /** Symbol for seconds (used in the custom ns -> s conversion) */
    private static final String SECONDS_SYMBOL = "s"; //$NON-NLS-1$

    /** Symbol for nanoseconds (used in the custom ns -> s conversion) */
    private static final String NANOSECONDS_SYMBOL = "ns"; //$NON-NLS-1$

    /** Maximum amount of digits that can be represented into a double */
    private static final int BIG_DECIMAL_DIVISION_SCALE = 22;

    private final Listener fResizeListener = event -> {
        /* Refresh the titles to fit the current chart size */
        refreshDisplayTitles();

        /* Refresh the Axis labels to fit the current chart size */
        refreshDisplayLabels();
    };

    private final LamiChartModel fChartModel;
    private final LamiReportViewTabPage fPage;

    private final Chart fChart;

    private final String fChartTitle;

    private String fXLabel;
    private @Nullable String fXUnits;

    private String fYLabel;
    private @Nullable String fYUnits;

    private boolean fSelected;
    private Set<Integer> fSelection;

    private final ToolBar fToolBar;

    /**
     * Creates a Viewer instance based on SWTChart.
     *
     * @param parent
     *            The parent composite to draw in.
     * @param page
     *            The {@link LamiReportViewTabPage} parent page
     * @param chartModel
     *            The information about the chart to build
     */
    public LamiXYChartViewer(Composite parent, LamiReportViewTabPage page, LamiChartModel chartModel) {
        super(parent);

        fParent = parent;
        fPage = page;
        fChartModel = chartModel;
        fSelection = new HashSet<>();

        fXLabel = ""; //$NON-NLS-1$
        fYLabel = ""; //$NON-NLS-1$

        fChart = new Chart(parent, SWT.NONE);
        fChart.addListener(SWT.Resize, fResizeListener);

        /* Set Chart title */
        fChartTitle = fPage.getResultTable().getTableClass().getTableTitle();

        /* Set X axis title */
        if (fChartModel.getXSeriesColumns().size() == 1) {
            /*
             * There is only 1 series in the chart, we will use its name as the
             * X axis.
             */
            innerSetXTitle(getXAxisAspects().get(0).getName(), getXAxisAspects().get(0).getUnits());
        } else {
            /*
             * There are multiple series in the chart, if they all share the same
             * units, display that.
             */
            long nbDiffAspectsUnits = getXAxisAspects().stream()
                .map(aspect -> aspect.getUnits())
                .distinct()
                .count();

            long nbDiffAspectName = getXAxisAspects().stream()
                    .map(aspect -> aspect.getName())
                    .distinct()
                    .count();

            String xBaseTitle = Messages.LamiViewer_DefaultValueName;
            if (nbDiffAspectName == 1) {
                xBaseTitle = getXAxisAspects().get(0).getName();
            }

            String units = null;
            if (nbDiffAspectsUnits == 1) {
                /* All aspects use the same unit type */
                units = getXAxisAspects().get(0).getUnits();
            }

            innerSetXTitle(xBaseTitle, units);
        }

        /* Set Y axis title */
        if (fChartModel.getYSeriesColumns().size() == 1) {
            /*
             * There is only 1 series in the chart, we will use its name as the
             * Y axis (and hide the legend).
             */
            innerSetYTitle(getYAxisAspects().get(0).getName(), getYAxisAspects().get(0).getUnits());

            /* Hide the legend */
            fChart.getLegend().setVisible(false);
        } else {
            /*
             * There are multiple series in the chart, if they all share the same
             * units, display that.
             */
            long nbDiffAspectsUnits = getYAxisAspects().stream()
                .map(aspect -> aspect.getUnits())
                .distinct()
                .count();

            long nbDiffAspectName = getYAxisAspects().stream()
                    .map(aspect -> aspect.getName())
                    .distinct()
                    .count();

            String yBaseTitle = Messages.LamiViewer_DefaultValueName;
            if (nbDiffAspectName == 1) {
                yBaseTitle = getYAxisAspects().get(0).getName();
            }

            String units = null;
            if (nbDiffAspectsUnits == 1) {
                /* All aspects use the same unit type */
                units = getYAxisAspects().get(0).getUnits();
            }

            innerSetYTitle(yBaseTitle, units);

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

        fToolBar = createChartToolBar();

        fChart.addDisposeListener(e -> {
                /* Dispose resources of this class */
                LamiXYChartViewer.super.dispose();
        });
    }

    /**
     * Set the Y axis title and refresh the chart.
     *
     * @param label
     *            the label string.
     * @param units
     *            the units string.
     */
    protected void setYTitle(@Nullable String label, @Nullable String units) {
        innerSetYTitle(label, units);
    }

    private void innerSetYTitle(@Nullable String label, @Nullable String units) {
        fYLabel = nullToEmptyString(label);
        innerSetYUnits(units);
        refreshDisplayTitles();
    }

    /**
     * Set the units on the Y Axis title and refresh the chart.
     *
     * @param units
     *            the units string.
     */
    protected void setYUnits(@Nullable String units) {
        innerSetYUnits(units);
    }

    private void innerSetYUnits(@Nullable String units) {
        /*
         * All time durations in the Lami protocol are nanoseconds, on the
         * charts we use an axis formater that converts back to seconds as a
         * base unit and then uses prefixes like nano and milli depending on the
         * range.
         *
         * So set the units to seconds in the title to match the base unit of
         * the formater.
         */
        if (NANOSECONDS_SYMBOL.equals(units)) {
            fYUnits = SECONDS_SYMBOL;
        } else {
            fYUnits = units;
        }
        refreshDisplayTitles();
    }

    /**
     * Get the Y axis title string.
     *
     * If the units is non-null, the title will be: "label (units)"
     *
     * If the units is null, the title will be: "label"
     *
     * @return the title of the Y axis.
     */
    protected String getYTitle() {
        if (fYUnits == null) {
            return fYLabel;
        }
        return fYLabel + " (" + fYUnits + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Set the X axis title and refresh the chart.
     *
     * @param label
     *            the label string.
     * @param units
     *            the units string.
     */
    protected void setXTitle(@Nullable String label, @Nullable String units) {
        innerSetXTitle(label, units);
    }

    private void innerSetXTitle(@Nullable String label, @Nullable String units) {
        fXLabel = nullToEmptyString(label);
        innerSetXUnits(units);
        refreshDisplayTitles();
    }

    /**
     * Set the units on the X Axis title.
     *
     * @param units
     *            the units string
     */
    protected void setXUnits(@Nullable String units) {
        innerSetXUnits(units);
    }

    private void innerSetXUnits(@Nullable String units) {
        /* The time duration formatter converts ns to s on the axis */
        if (NANOSECONDS_SYMBOL.equals(units)) {
            fXUnits = SECONDS_SYMBOL;
        } else {
            fXUnits = units;
        }
        refreshDisplayTitles();
    }

    /**
     * Get the X axis title string.
     *
     * If the units is non-null, the title will be: "label (units)"
     *
     * If the units is null, the title will be: "label"
     *
     * @return the title of the Y axis.
     */
    protected String getXTitle() {
        if (fXUnits == null) {
            return fXLabel;
        }
        return fXLabel + " (" + fXUnits + ")"; //$NON-NLS-1$ //$NON-NLS-2$
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
     * @param internalRange
     *            The internal range for value transformation
     * @param externalRange
     *            The external range for value transformation
     * @return a formatter for the axis.
     */
    protected static Format getContinuousAxisFormatter(List<LamiTableEntryAspect> axisAspects, List<LamiTableEntry> entries , @Nullable LamiGraphRange internalRange, @Nullable LamiGraphRange externalRange) {

        Format formatter = DECIMAL_FORMATTER;

        if (areAspectsTimeStamp(axisAspects)) {
            /* Set a TimeStamp formatter depending on the duration between the first and last value */
            BigDecimal max = new BigDecimal(Long.MIN_VALUE);
            BigDecimal min = new BigDecimal(Long.MAX_VALUE);

            for (LamiTableEntry entry : entries) {
                for (LamiTableEntryAspect aspect : axisAspects) {
                    @Nullable Number number = aspect.resolveNumber(entry);
                    if (number != null) {
                        BigDecimal current = new BigDecimal(number.toString());
                        max = current.max(max);
                        min = current.min(min);
                    }
                }
            }

            long duration = max.subtract(min).longValue();
            if (duration > TimeUnit.DAYS.toNanos(1)) {
                formatter = DAYS_FORMATTER;
            } else if (duration > TimeUnit.HOURS.toNanos(1)) {
                formatter = HOURS_FORMATTER;
            } else if (duration > TimeUnit.MINUTES.toNanos(1)) {
                formatter = MINUTES_FORMATTER;
            } else if (duration > TimeUnit.SECONDS.toNanos(15)) {
                formatter = SECONDS_FORMATTER;
            } else {
                formatter = MILLISECONDS_FORMATTER;
            }
            ((LamiTimeStampFormat) formatter).setInternalRange(internalRange);
            ((LamiTimeStampFormat) formatter).setExternalRange(externalRange);

        } else if (areAspectsTimeDuration(axisAspects)) {
            /* Set the time duration formatter. */
            formatter = NANO_TO_SECS_FORMATTER;
            ((LamiDecimalUnitFormat) formatter).setInternalRange(internalRange);
            ((LamiDecimalUnitFormat) formatter).setExternalRange(externalRange);

        } else {
            /*
             * For other numeric aspects, use the default lami decimal unit
             * formatter.
             */
            formatter = DECIMAL_FORMATTER;
            ((LamiDecimalUnitFormat) formatter).setInternalRange(internalRange);
            ((LamiDecimalUnitFormat) formatter).setExternalRange(externalRange);
        }

        return formatter;
    }

    /**
     * Get the chart result table.
     *
     * @return The chart result table.
     */
    protected LamiResultTable getResultTable() {
        return fPage.getResultTable();
    }

    /**
     * Get the chart {@code LamiReportViewTabPage} parent.
     *
     * @return The {@code LamiReportViewTabPage} parent.
     */
    protected LamiReportViewTabPage getPage() {
        return fPage;
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
     * @return the toolBar
     */
    public ToolBar getToolBar() {
        return fToolBar;
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
        refreshDisplayTitle(xTitle, getXTitle(), plotRect.width);

        ITitle yTitle = checkNotNull(fChart.getAxisSet().getYAxis(0).getTitle());
        refreshDisplayTitle(yTitle, getYTitle(), plotRect.height);
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
        if (getPage() != signal.getSignalKey() || equals(signal.getSource())) {
            /* The signal is not for us */
            return;
        }
        setSelection(signal.getEntryIndex());

        redraw();
    }

    /**
     * Create a tool bar on top right of the chart. Contained actions:
     * <ul>
     * <li>Dispose the current viewer, also known as "Close the chart"</li>
     * </ul>
     *
     * This tool bar should only appear when the mouse enters the composite.
     *
     * @return the tool bar
     */
    protected ToolBar createChartToolBar() {
        Image removeImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ELCL_REMOVE);
        ToolBar toolBar = new ToolBar(getChart(), SWT.HORIZONTAL);

        /* Default state */
        toolBar.moveAbove(null);
        toolBar.setVisible(false);

        /*
         * Close chart button
         */
        ToolItem closeButton = new ToolItem(toolBar, SWT.PUSH);
        closeButton.setImage(removeImage);
        closeButton.setToolTipText(Messages.LamiXYChartViewer_CloseChartToolTip);
        closeButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(@Nullable SelectionEvent e) {
                Composite parent = getParent();
                dispose();
                parent.layout();
            }

            @Override
            public void widgetDefaultSelected(@Nullable SelectionEvent e) {
            }
        });

        toolBar.pack();
        toolBar.setLocation(new Point(getChart().getSize().x - toolBar.getSize().x, 0));

        /* Visibility toggle filter */
        Listener toolBarVisibilityToggleListener = e -> {
            if (e.widget instanceof Control) {
                Control control = (Control) e.widget;
                Point display = control.toDisplay(e.x, e.y);
                Point location = getChart().getParent().toControl(display);

                /*
                 * Only set to visible if we are at the right location, in the
                 * right shell.
                 */
                boolean visible = getChart().getBounds().contains(location) &&
                        control.getShell().equals(getChart().getShell());
                getToolBar().setVisible(visible);
            }
        };

        /* Filter to make sure we hide the toolbar if we exit the window */
        Listener hideToolBarListener = (e -> getToolBar().setVisible(false));

        /*
         * Add the filters to the main Display, and remove them when we dispose
         * the chart.
         */
        Display display = getChart().getDisplay();
        display.addFilter(SWT.MouseEnter, toolBarVisibilityToggleListener);
        display.addFilter(SWT.MouseExit, hideToolBarListener);

        getChart().addDisposeListener(e -> {
            display.removeFilter(SWT.MouseEnter, toolBarVisibilityToggleListener);
            display.removeFilter(SWT.MouseExit, hideToolBarListener);
        });

        /* Reposition the tool bar on resize */
        getChart().addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(@Nullable Event event) {
                toolBar.setLocation(new Point(getChart().getSize().x - toolBar.getSize().x, 0));
            }
        });

        return toolBar;
    }

    /**
     * Get a {@link LamiGraphRange} that covers all data points in the result
     * table.
     * <p>
     * The returned range will be the minimum and maximum of the resolved values
     * of the passed aspects for all result entries. If <code>clampToZero</code>
     * is true, a positive minimum value will be clamped down to zero.
     *
     * @param aspects
     *            The aspects that the range will represent
     * @param clampToZero
     *            If true, a positive minimum value will be clamped down to zero
     * @return the range
     */
    protected LamiGraphRange getRange(List<LamiTableEntryAspect> aspects, boolean clampToZero) {
        /* Find the minimum and maximum values */
        BigDecimal min = new BigDecimal(Long.MAX_VALUE);
        BigDecimal max = new BigDecimal(Long.MIN_VALUE);
        for (LamiTableEntryAspect lamiTableEntryAspect : aspects) {
            for (LamiTableEntry entry : getResultTable().getEntries()) {
                @Nullable Number number = lamiTableEntryAspect.resolveNumber(entry);
                if (number != null) {
                    BigDecimal current = new BigDecimal(number.toString());
                    min = current.min(min);
                    max = current.max(max);
                }
            }
        }

        if (clampToZero) {
            min = min.min(BigDecimal.ZERO);
        }

        /* Do not allow a range with a zero delta default to 1 */
        if (max.equals(min)) {
            max = min.add(BigDecimal.ONE);
        }

        return new LamiGraphRange(checkNotNull(min), checkNotNull(max));
    }

    /**
     * Transform an external value into an internal value. Since SWTChart only
     * support Double and Lami can pass Long values, loss of precision might
     * happen. To minimize this, transform the raw values to an internal
     * representation based on a linear transformation.
     *
     * The internal value =
     *
     * ((rawValue - rawMinimum) * (internalRangeDelta/rawRangeDelta)) +
     * internalMinimum
     *
     * @param number
     *            The number to transform
     * @param internalRange
     *            The internal range definition to be used
     * @param externalRange
     *            The external range definition to be used
     * @return the transformed value in Double comprised inside the internal
     *         range
     */
    protected static double getInternalDoubleValue(Number number, LamiGraphRange internalRange, LamiGraphRange externalRange) {
        BigDecimal value = new BigDecimal(number.toString());

        if (externalRange.getDelta().compareTo(BigDecimal.ZERO) == 0) {
            return internalRange.getMinimum().doubleValue();
        }

        BigDecimal internalValue = value
                .subtract(externalRange.getMinimum())
                .multiply(internalRange.getDelta())
                .divide(externalRange.getDelta(), BIG_DECIMAL_DIVISION_SCALE, BigDecimal.ROUND_DOWN)
                .add(internalRange.getMinimum());

        return internalValue.doubleValue();
    }
}
