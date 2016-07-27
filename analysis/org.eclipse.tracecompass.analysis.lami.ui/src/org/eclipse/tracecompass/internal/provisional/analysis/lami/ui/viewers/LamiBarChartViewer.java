/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.viewers;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.math.BigDecimal;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTableEntryAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiChartModel;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiChartModel.LamiChartType;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTableEntry;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.signals.LamiSelectionUpdateSignal;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.views.LamiReportViewTabPage;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.swtchart.IAxis;
import org.swtchart.IAxisTick;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.Range;

import com.google.common.collect.Iterators;

/**
 * Bar chart Viewer for LAMI views.
 *
 * @author Alexandre Montplaisir
 * @author Jonathan Rajotte-Julien
 * @author Mathieu Desnoyers
 */
public class LamiBarChartViewer extends LamiXYChartViewer {

    private static final double LOGSCALE_EPSILON_FACTOR = 100.0;

    private class Mapping {
        final private @Nullable Integer fInternalValue;
        final private @Nullable Integer fModelValue;

        public Mapping(@Nullable Integer internalValue, @Nullable Integer modelValue) {
            fInternalValue = internalValue;
            fModelValue = modelValue;
        }

        public @Nullable Integer getInternalValue() {
            return fInternalValue;
        }

        public @Nullable Integer getModelValue() {
            return fModelValue;
        }
    }

    private final String[] fCategories;
    private final Map<ISeries, List<Mapping>> fIndexPerSeriesMapping;
    private final Map<LamiTableEntry, Mapping> fEntryToCategoriesMap;

    private LamiGraphRange fYInternalRange = new LamiGraphRange(BigDecimal.ZERO, BigDecimal.ONE);
    private LamiGraphRange fYExternalRange;


    /**
     * Creates a bar chart Viewer instance based on SWTChart.
     *
     * @param parent
     *            The parent composite to draw in.
     * @param page
     *            The {@link LamiReportViewTabPage} parent page
     * @param chartModel
     *            The information about the chart to build
     */
    public LamiBarChartViewer(Composite parent, LamiReportViewTabPage page, LamiChartModel chartModel) {
        super(parent, page, chartModel);

        List<LamiTableEntryAspect> xAxisAspects = getXAxisAspects();
        List<LamiTableEntryAspect> yAxisAspects = getYAxisAspects();

        /* bar chart cannot deal with multiple X series */
        if (getChartModel().getChartType() != LamiChartType.BAR_CHART && xAxisAspects.size() != 1) {
            throw new IllegalArgumentException("Invalid configuration passed to a bar chart."); //$NON-NLS-1$
        }

        /* Enable categories */
        getChart().getAxisSet().getXAxis(0).enableCategory(true);

        LamiTableEntryAspect xAxisAspect = xAxisAspects.get(0);
        List<LamiTableEntry> entries = getResultTable().getEntries();
        boolean logscale = chartModel.yAxisIsLog();
        fIndexPerSeriesMapping = new HashMap<>();
        fEntryToCategoriesMap = new HashMap<>();

        /* Categories index mapping */
        Format formatter = null;
        if (xAxisAspect.isContinuous()) {
            formatter = getContinuousAxisFormatter(xAxisAspects, entries, null, null);
        }

        List<@Nullable String> xCategories = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            String string = xAxisAspect.resolveString(entries.get(i));
            if (string == null) {
                fEntryToCategoriesMap.put(entries.get(i), new Mapping(null, i));
                continue;
            }

            fEntryToCategoriesMap.put(entries.get(i), new Mapping(xCategories.size(), i));
            if (formatter != null) {
                string = formatter.format(xAxisAspect.resolveNumber(entries.get(i)));
            }

            xCategories.add(string);

        }
        fCategories = xCategories.toArray(new String[0]);

        /* The y values range */
        /* Clamp minimum to zero or negative value */
        fYExternalRange = getRange(yAxisAspects, true);

        /*
         * Log scale magic course 101:
         *
         * It uses the relative difference divided by a factor
         * (100) to get as close as it can to the actual minimum but still a
         * little bit smaller. This is used as a workaround of SWTCHART
         * limitations regarding custom scale drawing in log scale mode, bogus
         * representation of NaN double values and limited support of multiple
         * size series.
         *
         * This should be good enough for most users.
         */
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double logScaleEpsilon = ZERO_DOUBLE;
        if (logscale) {

            /* Find minimum and maximum values excluding <= 0 values */
            for (LamiTableEntryAspect aspect : yAxisAspects) {
                for (LamiTableEntry entry : entries) {
                    Number externalValue = aspect.resolveNumber(entry);
                    if (externalValue == null) {
                        continue;
                    }
                    Double value = getInternalDoubleValue(externalValue, fYInternalRange, fYExternalRange);
                    if (value <= 0) {
                        continue;
                    }
                    min = Math.min(min, value);
                    max = Math.max(max, value);
                }
            }

            if (min == Double.MAX_VALUE) {
                /* Series are empty in log scale*/
                return;
            }

            double delta = max - min;
            logScaleEpsilon = min - ((min * delta) / (LOGSCALE_EPSILON_FACTOR * max));
        }

        for (LamiTableEntryAspect yAxisAspect : yAxisAspects) {
            if (!yAxisAspect.isContinuous() || yAxisAspect.isTimeStamp()) {
                /* Only plot continuous aspects */
                continue;
            }

            List<Double> validXValues = new ArrayList<>();
            List<Double> validYValues = new ArrayList<>();
            List<Mapping> indexMapping = new ArrayList<>();

            for (int i = 0; i < entries.size(); i++) {
                Integer categoryIndex = checkNotNull(fEntryToCategoriesMap.get(checkNotNull(entries.get(i)))).fInternalValue;

                if (categoryIndex == null) {
                    /* Invalid value do not show */
                    continue;
                }

                Double yValue = ZERO_DOUBLE;
                @Nullable Number number = yAxisAspect.resolveNumber(entries.get(i));

                if (number == null) {
                    /*
                     * Null value for y is the same as zero since this is a bar
                     * chart
                     */
                    yValue = ZERO_DOUBLE;
                } else {
                    yValue = getInternalDoubleValue(number, fYInternalRange, fYExternalRange);
                }

                if (logscale && yValue <= ZERO_DOUBLE) {
                    /*
                     * Less or equal to 0 values can't be plotted on a log
                     * scale. We map them to the mean of the >=0 minimal value
                     * and the calculated log scale magic epsilon.
                     */
                    yValue = (min + logScaleEpsilon) / 2.0;
                }

                validXValues.add(checkNotNull(categoryIndex).doubleValue());
                validYValues.add(yValue.doubleValue());
                indexMapping.add(new Mapping(categoryIndex, checkNotNull(fEntryToCategoriesMap.get(checkNotNull(entries.get(i)))).fModelValue));
            }

            String name = yAxisAspect.getLabel();

            if (validXValues.isEmpty() || validYValues.isEmpty()) {
                /* No need to plot an empty series */
                continue;
            }

            IBarSeries barSeries = (IBarSeries) getChart().getSeriesSet().createSeries(SeriesType.BAR, name);
            barSeries.setXSeries(validXValues.stream().mapToDouble(Double::doubleValue).toArray());
            barSeries.setYSeries(validYValues.stream().mapToDouble(Double::doubleValue).toArray());
            fIndexPerSeriesMapping.put(barSeries, indexMapping);
        }

        setBarSeriesColors();

        /* Set all y axis logscale mode */
        Stream.of(getChart().getAxisSet().getYAxes()).forEach(axis -> axis.enableLogScale(logscale));

        /* Set the formatter on the Y axis */
        IAxisTick yTick = getChart().getAxisSet().getYAxis(0).getTick();
        yTick.setFormat(getContinuousAxisFormatter(yAxisAspects, entries, fYInternalRange, fYExternalRange));

        /*
         * SWTChart workaround: SWTChart fiddles with tick mark visibility based
         * on the fact that it can parse the label to double or not.
         *
         * If the label happens to be a double, it checks for the presence of
         * that value in its own tick labels to decide if it should add it or
         * not. If it happens that the parsed value is already present in its
         * map, the tick gets a visibility of false.
         *
         * The X axis does not have this problem since SWTCHART checks on label
         * angle, and if it is != 0 simply does no logic regarding visibility.
         * So simply set a label angle of 1 to the axis.
         */
        yTick.setTickLabelAngle(1);

        /* Adjust the chart range */
        getChart().getAxisSet().adjustRange();

        if (logscale && logScaleEpsilon != max) {
            getChart().getAxisSet().getYAxis(0).setRange(new Range(logScaleEpsilon, max));
        }

        /* Once the chart is filled, refresh the axis labels */
        refreshDisplayLabels();

        /* Add mouse listener */
        getChart().getPlotArea().addMouseListener(new LamiBarChartMouseDownListener());

        /* Custom Painter listener to highlight the current selection */
        getChart().getPlotArea().addPaintListener(new LamiBarChartPainterListener());
    }

    private final class LamiBarChartMouseDownListener extends MouseAdapter {

        @Override
        public void mouseDown(@Nullable MouseEvent event) {
            if (event == null || event.button != 1) {
                return;
            }

            boolean ctrlMode = false;
            int xMouseLocation = event.x;
            int yMouseLocation = event.y;

            Set<Integer> selections;
            if ((event.stateMask & SWT.CTRL) != 0) {
                ctrlMode = true;
                selections = getSelection();
            } else {
                /* Reset selection state */
                unsetSelection();
                selections = new HashSet<>();
            }

            ISeries[] series = getChart().getSeriesSet().getSeries();

            /*
             * Iterate over all series, get the rectangle bounds for each
             * category, and find the category index under the mouse.
             *
             * Since categories map directly to the index of the fResultTable
             * and that this table is immutable the index of the entry
             * corresponds to the categories index. Signal to all LamiViewer and
             * LamiView the update of selection.
             */
            for (ISeries oneSeries : series) {
                IBarSeries barSerie = ((IBarSeries) oneSeries);
                Rectangle[] recs = barSerie.getBounds();

                for (int j = 0; j < recs.length; j++) {
                    Rectangle rectangle = recs[j];
                    if (rectangle.contains(xMouseLocation, yMouseLocation)) {
                        int index = getTableEntryIndexFromGraphIndex(checkNotNull(oneSeries), j);
                        if (!ctrlMode || (index >= 0 && !selections.remove(index))) {
                            selections.add(index);
                        }
                    }
                }
            }

            /* Save the current selection internally */
            setSelection(selections);
            /* Signal all Lami viewers & views of the selection */
            LamiSelectionUpdateSignal signal = new LamiSelectionUpdateSignal(this,
                    selections, getPage());
            TmfSignalManager.dispatchSignal(signal);
            redraw();
        }
    }

    @Override
    protected void redraw() {
        setBarSeriesColors();
        super.redraw();
    }

    /**
     * Set the chart series colors according to the selection state. Use light
     * colors when a selection is present.
     */
    private void setBarSeriesColors() {
        Iterator<Color> colorsIt;

        if (isSelected()) {
            colorsIt = Iterators.cycle(LIGHT_COLORS);
        } else {
            colorsIt = Iterators.cycle(COLORS);
        }

        for (ISeries series : getChart().getSeriesSet().getSeries()) {
            ((IBarSeries) series).setBarColor(colorsIt.next());
        }
    }

    private final class LamiBarChartPainterListener implements PaintListener {
        @Override
        public void paintControl(@Nullable PaintEvent e) {
            if (e == null || !isSelected()) {
                return;
            }

            Iterator<Color> colorsIt = Iterators.cycle(COLORS);
            GC gc = e.gc;

            for (ISeries series : getChart().getSeriesSet().getSeries()) {
                Color color = colorsIt.next();
                for (int index : getSelection()) {
                    int graphIndex = getGraphIndexFromTableEntryIndex(series, index);
                    if (graphIndex < 0) {
                        /* Invalid index */
                        continue;
                    }

                    Rectangle[] bounds = ((IBarSeries) series).getBounds();
                    if (bounds.length != fCategories.length) {
                        /*
                         * The plot is too cramped and SWTChart currently does
                         * its best on rectangle drawing and returns the
                         * rectangle that it is able to draw.
                         *
                         * For now we simply do not draw since it is really hard
                         * to see anyway. A better way to visualize the value
                         * would be a full cross for each selection based on
                         * their coordinates.
                         */
                        continue;
                    }
                    Rectangle rectangle = bounds[graphIndex];
                    gc.setBackground(color);
                    gc.fillRectangle(rectangle);
                }
            }
        }
    }

    @Override
    protected void refreshDisplayLabels() {
        /* Only if we have at least 1 category */
        if (fCategories.length == 0) {
            return;
        }

        /* Only refresh if labels are visible */
        IAxis xAxis = getChart().getAxisSet().getXAxis(0);
        if (!xAxis.getTick().isVisible() || !xAxis.isCategoryEnabled()) {
            return;
        }

        /*
         * Shorten all the labels to 5 characters plus "…" when the longest
         * label length is more than 50% of the chart height.
         */

        Rectangle rect = getChart().getClientArea();
        int lengthLimit = (int) (rect.height * 0.40);

        GC gc = new GC(fParent);
        gc.setFont(xAxis.getTick().getFont());

        /* Find the longest category string */
        String longestString = Arrays.stream(fCategories).max(Comparator.comparingInt(String::length)).orElse(fCategories[0]);

        /* Get the length and height of the longest label in pixels */
        Point pixels = gc.stringExtent(longestString);

        // Completely arbitrary
        int cutLen = 5;

        String[] displayCategories = new String[fCategories.length];
        if (pixels.x > lengthLimit) {
            /* We have to cut down some strings */
            for (int i = 0; i < fCategories.length; i++) {
                if (fCategories[i].length() > cutLen) {
                    displayCategories[i] = fCategories[i].substring(0, cutLen) + ELLIPSIS;
                } else {
                    displayCategories[i] = fCategories[i];
                }
            }
        } else {
            /* All strings should fit */
            displayCategories = Arrays.copyOf(fCategories, fCategories.length);
        }
        xAxis.setCategorySeries(displayCategories);

        /* Cleanup */
        gc.dispose();
    }

    private int getTableEntryIndexFromGraphIndex(ISeries series, int index) {
        List<Mapping> indexes = fIndexPerSeriesMapping.get(series);
        if (indexes == null || index > indexes.size() || index < 0) {
            return -1;
        }

        Mapping mapping = indexes.get(index);
        Integer modelValue = mapping.getModelValue();
        if (modelValue != null) {
            return modelValue.intValue();
        }
        return -1;
    }

    private int getGraphIndexFromTableEntryIndex(ISeries series, int index) {
        List<Mapping> indexes = fIndexPerSeriesMapping.get(series);
        if (indexes == null || index < 0) {
            return -1;
        }

        int internalIndex = -1;
        for (Mapping mapping : indexes) {
            if (mapping.getModelValue() == index) {
                Integer internalValue = mapping.getInternalValue();
                if (internalValue != null) {
                    internalIndex = internalValue.intValue();
                    break;
                }
            }
        }
        return internalIndex;
    }
}
