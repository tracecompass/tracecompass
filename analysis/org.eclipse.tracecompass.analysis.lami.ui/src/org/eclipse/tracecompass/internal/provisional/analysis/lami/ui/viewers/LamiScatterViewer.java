/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Jonathan Rajotte-Julien
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.viewers;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTableEntryAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiChartModel;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiChartModel.ChartType;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiLabelFormat;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiResultTable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTableEntry;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.signals.LamiSelectionUpdateSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.swtchart.IAxisTick;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.LineStyle;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterators;

/**
 * XY Scatter chart viewer for Lami views
 *
 * @author Jonathan Rajotte-Julien
 */
public class LamiScatterViewer extends LamiXYChartViewer {

    private static final int SELECTION_SNAP_RANGE_MULTIPLIER = 20;
    private static final int SELECTION_CROSS_SIZE_MULTIPLIER = 3;

    private final Map<ISeries, List<Integer>> fIndexMapping;

    /* The current data point for the hovering cross */
    private Point fHoveringCrossDataPoint;

    /**
     * Constructor
     *
     * @param parent
     *            parent
     * @param resultTable
     *            Result table populating this chart
     * @param graphModel
     *            Model of this chart
     */
    public LamiScatterViewer(Composite parent, LamiResultTable resultTable, LamiChartModel graphModel) {
        super(parent, resultTable, graphModel);
        if (getChartModel().getChartType() != ChartType.XY_SCATTER) {
            throw new IllegalStateException("Chart type not a Scatter Chart " + getChartModel().getChartType().toString()); //$NON-NLS-1$
        }

        /* Inspect X series */
        fIndexMapping = new HashMap<>();

        fHoveringCrossDataPoint = new Point(-1, -1);

        List<LamiTableEntryAspect> xAxisAspects = getXAxisAspects();
        if (xAxisAspects.stream().distinct().count() == 1) {
            LamiTableEntryAspect singleXAspect = xAxisAspects.get(0);
            xAxisAspects.clear();
            xAxisAspects.add(singleXAspect);
        }

        BiMap<@Nullable String, Integer> xMap = checkNotNull(HashBiMap.create());
        boolean xIsLog = graphModel.xAxisIsLog();

        boolean areXAspectsContinuous = areAspectsContinuous(xAxisAspects);
        boolean areXAspectsTimeStamp = areAspectsTimeStamp(xAxisAspects);

        /* Check all aspect are the same type */
        for (LamiTableEntryAspect aspect : xAxisAspects) {
            if (aspect.isContinuous() != areXAspectsContinuous) {
                throw new IllegalStateException("Some X aspects are continuous and some are not"); //$NON-NLS-1$
            }
            if (aspect.isTimeStamp() != areXAspectsTimeStamp) {
                throw new IllegalStateException("Some X aspects are time based and some are not"); //$NON-NLS-1$
            }
        }

        /*
         * When xAxisAspects are discrete create a map for all values of all
         * series
         */
        if (!areXAspectsContinuous) {
            generateLabelMap(xAxisAspects, checkNotNull(xMap));
        }

        /*
         * Create Y series
         */
        List<LamiTableEntryAspect> yAxisAspects = getYAxisAspects();
        BiMap<@Nullable String, Integer> yMap = checkNotNull(HashBiMap.create());
        boolean yIsLog = graphModel.yAxisIsLog();

        boolean areYAspectsContinuous = areAspectsContinuous(yAxisAspects);
        boolean areYAspectsTimeStamp = areAspectsTimeStamp(yAxisAspects);

        /* Check all aspect are the same type */
        for (LamiTableEntryAspect aspect : yAxisAspects) {
            if (aspect.isContinuous() != areYAspectsContinuous) {
                throw new IllegalStateException("Some Y aspects are continuous and some are not"); //$NON-NLS-1$
            }
            if (aspect.isTimeStamp() != areYAspectsTimeStamp) {
                throw new IllegalStateException("Some Y aspects are time based and some are not"); //$NON-NLS-1$
            }
        }

        /*
         * When yAspects are discrete create a map for all values of all series
         */
        if (!areYAspectsContinuous) {
            generateLabelMap(yAxisAspects, yMap);
        }

        /* Plot the series */
        int index = 0;
        for (LamiTableEntryAspect yAspect : getYAxisAspects()) {
            String name = ""; //$NON-NLS-1$
            LamiTableEntryAspect xAspect;
            if (xAxisAspects.size() == 1) {
                /* Always map to the same x series */
                xAspect = xAxisAspects.get(0);
                name = yAspect.getLabel();
            } else {
                xAspect = xAxisAspects.get(index);
                name = (yAspect.getName() + ' ' + Messages.LamiScatterViewer_by + ' ' + xAspect.getName());
            }

            List<@Nullable Double> xDoubleSeries = new ArrayList<>();
            List<@Nullable Double> yDoubleSeries = new ArrayList<>();

            if (xAspect.isContinuous()) {
                xDoubleSeries = getResultTable().getEntries().stream().map((entry -> xAspect.resolveDouble(entry))).collect(Collectors.toList());
            } else {
                xDoubleSeries = getResultTable().getEntries().stream().map(entry -> {
                    String string = xAspect.resolveString(entry);
                    Integer value = xMap.get(string);
                    if (value != null) {
                        return Double.valueOf(value.doubleValue());
                    }
                    return null;

                }).collect(Collectors.toList());
            }

            if (yAspect.isContinuous()) {
                yDoubleSeries = getResultTable().getEntries().stream().map((entry -> yAspect.resolveDouble(entry))).collect(Collectors.toList());
            } else {
                yDoubleSeries = getResultTable().getEntries().stream().map(entry -> {
                    String string = yAspect.resolveString(entry);
                    Integer value = yMap.get(string);
                    if (value != null) {
                        return Double.valueOf(value.doubleValue());
                    }
                    return null;

                }).collect(Collectors.toList());
            }

            List<@Nullable Double> validXDoubleSeries = new ArrayList<>();
            List<@Nullable Double> validYDoubleSeries = new ArrayList<>();
            List<Integer> indexSeriesCorrespondance = new ArrayList<>();

            if (xDoubleSeries.size() != yDoubleSeries.size()) {
                throw new IllegalStateException("Series sizes don't match!"); //$NON-NLS-1$
            }

            /* Check for invalid tuple value. Any null elements are invalid */
            for (int i = 0; i < xDoubleSeries.size(); i++) {
                Double xValue = xDoubleSeries.get(i);
                Double yValue = yDoubleSeries.get(i);
                if (xValue == null || yValue == null) {
                    /* Reject this tuple */
                    continue;
                }
                if ((xIsLog && xValue <= ZERO) || (yIsLog && yValue <= ZERO)) {
                    /*
                     * Equal or less than 0 values can't be plotted on log scale
                     */
                    continue;
                }
                validXDoubleSeries.add(xValue);
                validYDoubleSeries.add(yValue);
                indexSeriesCorrespondance.add(i);
            }

            if (validXDoubleSeries.isEmpty() || validXDoubleSeries.isEmpty()) {
                /* No need to plot an empty series */
                index++;
                continue;
            }

            ILineSeries scatterSeries = (ILineSeries) getChart().getSeriesSet().createSeries(SeriesType.LINE, name);
            scatterSeries.setLineStyle(LineStyle.NONE);

            double[] xserie = validXDoubleSeries.stream().mapToDouble(elem -> checkNotNull(elem).doubleValue()).toArray();
            double[] yserie = validYDoubleSeries.stream().mapToDouble(elem -> checkNotNull(elem).doubleValue()).toArray();
            scatterSeries.setXSeries(xserie);
            scatterSeries.setYSeries(yserie);
            fIndexMapping.put(scatterSeries, indexSeriesCorrespondance);
            index++;
        }

        /* Modify x axis related chart styling */
        IAxisTick xTick = getChart().getAxisSet().getXAxis(0).getTick();
        if (areXAspectsContinuous) {
            xTick.setFormat(getContinuousAxisFormatter(xAxisAspects, getResultTable().getEntries()));
        } else {
            xTick.setFormat(new LamiLabelFormat(checkNotNull(xMap)));
            updateTickMark(checkNotNull(xMap), xTick, getChart().getPlotArea().getSize().x);

            /* Remove vertical grid line */
            getChart().getAxisSet().getXAxis(0).getGrid().setStyle(LineStyle.NONE);
        }

        /* Modify Y axis related chart styling */
        IAxisTick yTick = getChart().getAxisSet().getYAxis(0).getTick();
        if (areYAspectsContinuous) {
            yTick.setFormat(getContinuousAxisFormatter(yAxisAspects, getResultTable().getEntries()));
        } else {
            yTick.setFormat(new LamiLabelFormat(checkNotNull(yMap)));
            updateTickMark(checkNotNull(yMap), yTick, getChart().getPlotArea().getSize().y);

            /*
             * SWTChart workaround: SWTChart fiddles with tick mark visibility
             * based on the fact that it can parse the label to double or not.
             *
             * If the label happens to be a double, it checks for the presence
             * of that value in its own tick labels to decide if it should add
             * it or not. If it happens that the parsed value is already present
             * in its map, the tick gets a visibility of false.
             *
             * The X axis does not have this problem since SWTCHART checks on
             * label angle, and if it is != 0 simply does no logic regarding
             * visibility. So simply set a label angle of 1 to the axis.
             */
            yTick.setTickLabelAngle(1);

            /* Remove horizontal grid line */
            getChart().getAxisSet().getYAxis(0).getGrid().setStyle(LineStyle.NONE);
        }

        setLineSeriesColor();

        /* Put log scale if necessary */
        if (xIsLog && areXAspectsContinuous && !areXAspectsTimeStamp) {
            Stream.of(getChart().getAxisSet().getXAxes()).forEach(axis -> axis.enableLogScale(xIsLog));
        }

        if (yIsLog && areYAspectsContinuous && !areYAspectsTimeStamp) {
            /* Set the axis as logscale */
            Stream.of(getChart().getAxisSet().getYAxes()).forEach(axis -> axis.enableLogScale(yIsLog));
        }
        getChart().getAxisSet().adjustRange();

        /*
         * Selection listener
         */
        getChart().getPlotArea().addMouseListener(new LamiScatterMouseDownListener());

        /*
         * Hovering cross listener
         */
        getChart().getPlotArea().addMouseMoveListener(new HoveringCrossListener());

        /*
         * Mouse exit listener: reset state of hovering cross on mouse exit.
         */
        getChart().getPlotArea().addListener(SWT.MouseExit, new Listener() {

            @Override
            public void handleEvent(@Nullable Event event) {
                if (event != null) {
                    fHoveringCrossDataPoint.x = -1;
                    fHoveringCrossDataPoint.y = -1;
                    redraw();
                }
            }
        });

        /*
         * Selections and hovering cross painting
         */
        getChart().getPlotArea().addPaintListener(new LamiScatterPainterListener());

        /* On resize check for axis tick updating */
        getChart().addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(@Nullable Event event) {
                if (yTick.getFormat() instanceof LamiLabelFormat) {
                    updateTickMark(checkNotNull(yMap), yTick, getChart().getPlotArea().getSize().y);
                }
                if (xTick.getFormat() instanceof LamiLabelFormat) {
                    updateTickMark(checkNotNull(xMap), xTick, getChart().getPlotArea().getSize().x);
                }
            }
        });
    }

    private void generateLabelMap(List<LamiTableEntryAspect> aspects, BiMap<@Nullable String, Integer> map) {
        TreeSet<@Nullable String> set = new TreeSet<>();
        for (LamiTableEntryAspect aspect : aspects) {
            for (LamiTableEntry entry : getResultTable().getEntries()) {
                String string = aspect.resolveString(entry);
                if (string != null) {
                    set.add(string);
                }
            }
        }
        /* Ordered label mapping to double */
        for (String string : set) {
            map.put(string, map.size());
        }
    }

    /**
     * Set the chart series colors.
     */
    private void setLineSeriesColor() {
        Iterator<Color> colorsIt;

        colorsIt = Iterators.cycle(COLORS);

        for (ISeries series : getChart().getSeriesSet().getSeries()) {
            ((ILineSeries) series).setSymbolColor((colorsIt.next()));
            /*
             * Generate initial array of Color to enable per point color change
             * on selection in the future
             */
            ArrayList<Color> colors = new ArrayList<>();
            for (int i = 0; i < series.getXSeries().length; i++) {
                Color color = ((ILineSeries) series).getSymbolColor();
                colors.add(checkNotNull(color));
            }
            ((ILineSeries) series).setSymbolColors(colors.toArray(new Color[colors.size()]));
        }
    }

    // ------------------------------------------------------------------------
    // Listeners
    // ------------------------------------------------------------------------

    private final class HoveringCrossListener implements MouseMoveListener {

        @Override
        public void mouseMove(@Nullable MouseEvent e) {
            if (e == null) {
                return;
            }
            ISeries[] series = getChart().getSeriesSet().getSeries();
            @Nullable Point closest = null;
            double closestDistance = -1.0;

            for (ISeries oneSeries : series) {
                ILineSeries lineSerie = (ILineSeries) oneSeries;
                for (int i = 0; i < lineSerie.getXSeries().length; i++) {
                    Point dataPoint = lineSerie.getPixelCoordinates(i);

                    /*
                     * Find the distance between the data point and the mouse
                     * location and compare it to the symbol size * the range
                     * multiplier, so when a user hovers the mouse near the dot
                     * the cursor cross snaps to it.
                     */
                    int snapRangeRadius = lineSerie.getSymbolSize() * SELECTION_SNAP_RANGE_MULTIPLIER;

                    /*
                     * FIXME if and only if performance of this code is an issue
                     * for large sets, this can be accelerated by getting the
                     * distance squared, and if it is smaller than
                     * snapRangeRadius squared, then check hypot.
                     */
                    double distance = Math.hypot(dataPoint.x - e.x, dataPoint.y - e.y);
                    if (distance < snapRangeRadius) {
                        if (closestDistance == -1 || distance < closestDistance) {
                            closest = dataPoint;
                            closestDistance = distance;
                        }
                    }
                }
            }
            if (closest != null) {
                fHoveringCrossDataPoint.x = closest.x;
                fHoveringCrossDataPoint.y = closest.y;
            } else {
                fHoveringCrossDataPoint.x = -1;
                fHoveringCrossDataPoint.y = -1;
            }
            refresh();
        }
    }

    private final class LamiScatterMouseDownListener extends MouseAdapter {

        @Override
        public void mouseDown(@Nullable MouseEvent event) {
            if (event == null || event.button != 1) {
                return;
            }

            int xMouseLocation = event.x;
            int yMouseLocation = event.y;

            boolean ctrlMode = false;

            ISeries[] series = getChart().getSeriesSet().getSeries();
            Set<Integer> selections = getSelection();

            /* Check for ctrl on click */
            if ((event.stateMask & SWT.CTRL) != 0) {
                selections = getSelection();
                ctrlMode = true;
            } else {
                /* Reset selection */
                unsetSelection();
                selections = new HashSet<>();
            }

            for (ISeries oneSeries : series) {
                ILineSeries lineSerie = (ILineSeries) oneSeries;

                int closest = -1;
                double closestDistance = -1;
                for (int i = 0; i < lineSerie.getXSeries().length; i++) {
                    Point dataPoint = lineSerie.getPixelCoordinates(i);

                    /*
                     * Find the distance between the data point and the mouse
                     * location, and compare it to the symbol size so when a
                     * user clicks on a symbol it selects it.
                     */
                    double distance = Math.hypot(dataPoint.x - xMouseLocation, dataPoint.y - yMouseLocation);
                    int snapRangeRadius = lineSerie.getSymbolSize() * SELECTION_SNAP_RANGE_MULTIPLIER;
                    if (distance < snapRangeRadius) {
                        if (closestDistance == -1 || distance < closestDistance) {
                            closest = i;
                            closestDistance = distance;
                        }
                    }
                }
                if (closest != -1) {
                    /* Translate to global index */
                    int tableEntryIndex = getTableEntryIndexFromGraphIndex(checkNotNull(oneSeries), closest);
                    if (tableEntryIndex < 0) {
                        continue;
                    }
                    LamiTableEntry entry = getResultTable().getEntries().get(tableEntryIndex);
                    int index = getResultTable().getEntries().indexOf(entry);

                    if (!ctrlMode || !selections.remove(index)) {
                        selections.add(index);
                    }
                    /* Do no iterate since we already found a match */
                    break;
                }
            }
            setSelection(selections);
            /* Signal all Lami viewers & views of the selection */
            LamiSelectionUpdateSignal signal = new LamiSelectionUpdateSignal(this,
                    selections, checkNotNull(getResultTable().hashCode()));
            TmfSignalManager.dispatchSignal(signal);
            refresh();
        }
    }

    private final class LamiScatterPainterListener implements PaintListener {

        @Override
        public void paintControl(@Nullable PaintEvent e) {
            if (e == null) {
                return;
            }
            GC gc = e.gc;

            /* Draw the selection */
            drawSelectedDot(checkNotNull(gc));

            /* Draw the hovering cross */
            drawHoveringCross(checkNotNull(gc));
        }

        private void drawSelectedDot(GC gc) {
            if (isSelected()) {
                Iterator<Color> colorsIt;
                colorsIt = Iterators.cycle(COLORS);
                for (ISeries series : getChart().getSeriesSet().getSeries()) {

                    /* Get series colors */
                    Color color = colorsIt.next();
                    int symbolSize = ((ILineSeries) series).getSymbolSize();

                    for (int index : getInternalSelections()) {
                        int graphIndex = getGraphIndexFromTableEntryIndex(series, index);

                        if (graphIndex < 0) {
                            continue;
                        }
                        Point point = series.getPixelCoordinates(graphIndex);

                        /* Create a colored dot for selection */
                        gc.setBackground(color);
                        gc.fillOval(point.x - symbolSize, point.y - symbolSize, symbolSize * 2, symbolSize * 2);

                        /* Draw cross */
                        gc.setLineWidth(2);
                        gc.setLineStyle(SWT.LINE_SOLID);
                        /* Vertical line */
                        int drawingDelta = SELECTION_CROSS_SIZE_MULTIPLIER * symbolSize;
                        gc.drawLine(point.x, point.y - drawingDelta, point.x, point.y + drawingDelta);
                        /* Horizontal line */
                        gc.drawLine(point.x - drawingDelta, point.y, point.x + drawingDelta, point.y);

                    }
                }
            }
        }

        private void drawHoveringCross(GC gc) {
            gc.setLineWidth(1);
            gc.setLineStyle(SWT.LINE_SOLID);
            gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
            gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            /* Vertical line */
            gc.drawLine(fHoveringCrossDataPoint.x, 0, fHoveringCrossDataPoint.x, getChart().getPlotArea().getSize().y);
            /* Horizontal line */
            gc.drawLine(0, fHoveringCrossDataPoint.y, getChart().getPlotArea().getSize().x, fHoveringCrossDataPoint.y);
        }
    }

    // ------------------------------------------------------------------------
    // Utility functions
    // ------------------------------------------------------------------------

    private int getTableEntryIndexFromGraphIndex(ISeries series, int index) {
        List<Integer> indexes = fIndexMapping.get(series);
        if (indexes == null || index > indexes.size() || index < 0) {
            return -1;
        }
        return indexes.get(index);
    }

    private int getGraphIndexFromTableEntryIndex(ISeries series, int index) {
        List<Integer> indexes = fIndexMapping.get(series);
        if (indexes == null || !indexes.contains(index)) {
            return -1;
        }
        return indexes.indexOf(index);
    }

    @Override
    protected void refreshDisplayLabels() {
    }

    /**
     * Return the current selection in internal mapping
     *
     * @return the internal selections
     */
    protected Set<Integer> getInternalSelections() {
        /* Translate to internal table location */
        Set<Integer> indexes = super.getSelection();
        Set<Integer> internalIndexes = indexes.stream()
                .mapToInt(index -> getResultTable().getEntries().indexOf((getResultTable().getEntries().get(index))))
                .boxed()
                .collect(Collectors.toSet());
        return internalIndexes;
    }

    private static void updateTickMark(BiMap<@Nullable String, Integer> map, IAxisTick tick, int availableLenghtPixel) {
        int nbLabels = Math.max(1, map.size());
        int stepSizePixel = availableLenghtPixel / nbLabels;
        /*
         * This step is a limitation on swtchart side regarding minimal grid
         * step hint size. When the step size are smaller it get defined as the
         * "default" value for the axis instead of the smallest one.
         */
        if (IAxisTick.MIN_GRID_STEP_HINT > stepSizePixel) {
            stepSizePixel = (int) IAxisTick.MIN_GRID_STEP_HINT;
        }
        tick.setTickMarkStepHint(stepSizePixel);
    }

    @Override
    protected void setSelection(@NonNull Set<@NonNull Integer> selection) {
        super.setSelection(selection);

        /* Set color of selected symbol */
        Iterator<Color> colorsIt = Iterators.cycle(COLORS);
        Iterator<Color> lightColorsIt = Iterators.cycle(LIGHT_COLORS);

        Set<Integer> currentSelections = getInternalSelections();

        for (ISeries series : getChart().getSeriesSet().getSeries()) {
            /* Series color */
            Color lightColor = lightColorsIt.next();
            Color color = colorsIt.next();
            Color[] colors = ((ILineSeries) series).getSymbolColors();

            if (currentSelections.isEmpty()) {
                /* Put all symbols to the normal colors */
                Arrays.fill(colors, color);
            } else {
                /*
                 * Fill with light colors to represent the deselected state. The
                 * paint listener is then responsible for drawing the cross and
                 * the dark colors for the selection.
                 */
                Arrays.fill(colors, lightColor);
            }
            ((ILineSeries) series).setSymbolColors(colors);
        }
    }

}
