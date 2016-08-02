/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.ui.swtchart;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
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
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartData;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartModel;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartSeries;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.INumericalResolver;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.IStringResolver;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.signal.ChartSelectionUpdateSignal;
import org.eclipse.tracecompass.internal.tmf.chart.core.aggregator.IConsumerAggregator;
import org.eclipse.tracecompass.internal.tmf.chart.core.consumer.IDataConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.core.consumer.NumericalConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.aggregator.NumericalConsumerAggregator;
import org.eclipse.tracecompass.internal.tmf.chart.ui.consumer.BarStringConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.consumer.XYChartConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.consumer.XYSeriesConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.data.ChartRangeMap;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.swtchart.IAxis;
import org.swtchart.IAxisTick;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;

import com.google.common.collect.Iterators;

/**
 * Class for a bar chart.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public final class SwtBarChart extends SwtXYChartViewer {

    private static final int BAR_PADDING = 20;

    /* Maximum percentage of chart for label */
    private static final double LENGTH_LIMIT = 0.4;

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    /**
     * Range map for the Y axis since it must be numerical
     */
    private ChartRangeMap fYRanges = new ChartRangeMap();
    /**
     * Map reprensenting categories on the X axis
     */
    private String @Nullable [] fCategories;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param parent
     *            parent composite
     * @param data
     *            configured data series for the chart
     * @param model
     *            chart model to use
     */
    public SwtBarChart(Composite parent, ChartData data, ChartModel model) {
        super(parent, data, model);

        /* Add the mouse click listener */
        getChart().getPlotArea().addMouseListener(new MouseDownListener());

        /* Add the paint listener */
        getChart().getPlotArea().addPaintListener(new BarPainterListener());

        populate();
    }

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public void validateChartData() {
        super.validateChartData();

        /* Make sure the X axis is not continuous */
        if (getXDescriptorsInfo().areNumerical()) {
            throw new IllegalArgumentException("Bar chart X axis cannot be numerical."); //$NON-NLS-1$
        }

        /**
         * TODO: allow Y discontinuous by mapping each string to a number
         */

        /* Make sure the Y axis is continuous */
        if (!getYDescriptorsInfo().areNumerical()) {
            throw new IllegalArgumentException("Bar chart Y axis must be numerical."); //$NON-NLS-1$
        }

        /**
         * TODO: allow multiple X axes
         */

        /* Make sure there is only one X axis */
        if (getXDescriptors().stream().distinct().count() > 1) {
            throw new IllegalArgumentException("Bar chart can only have one X axis."); //$NON-NLS-1$
        }
    }

    @Override
    protected IDataConsumer getXConsumer(@NonNull ChartSeries series) {
        IStringResolver<Object> xResolver = IStringResolver.class.cast(series.getX().getResolver());
        return new BarStringConsumer(xResolver);
    }

    @Override
    protected IDataConsumer getYConsumer(@NonNull ChartSeries series) {
        INumericalResolver<Object, Number> yResolver = INumericalResolver.class.cast(series.getY().getResolver());
        Predicate<@Nullable Number> yPredicate;
        if (getModel().isYLogscale()) {
            yPredicate = new LogarithmicPredicate(yResolver);
        } else {
            yPredicate = o -> true;
        }

        return new NumericalConsumer(yResolver, yPredicate);
    }

    @Override
    protected @Nullable IConsumerAggregator getXAggregator() {
        return null;
    }

    @Override
    protected @Nullable IConsumerAggregator getYAggregator() {
        return new NumericalConsumerAggregator();
    }

    @Override
    protected ISeries createSwtSeries(ChartSeries chartSeries, ISeriesSet swtSeriesSet, @NonNull Color color) {
        String title = chartSeries.getY().getLabel();
        IBarSeries swtSeries = (IBarSeries) swtSeriesSet.createSeries(SeriesType.BAR, title);
        swtSeries.setBarPadding(BAR_PADDING);
        swtSeries.setBarColor(color);

        return swtSeries;
    }

    @Override
    protected void configureSeries(Map<@NonNull ISeries, Object[]> mapper) {
        XYChartConsumer chartConsumer = getChartConsumer();
        NumericalConsumerAggregator aggregator = (NumericalConsumerAggregator) checkNotNull(chartConsumer.getYAggregator());

        /* Clamp the Y ranges */
        fYRanges = clampInputDataRange(checkNotNull(aggregator.getChartRanges()));

        /* Generate data for each SWT series */
        for (XYSeriesConsumer seriesConsumer : chartConsumer.getSeries()) {
            BarStringConsumer xconsumer = (BarStringConsumer) seriesConsumer.getXConsumer();
            NumericalConsumer yConsumer = (NumericalConsumer) seriesConsumer.getYConsumer();
            Object[] object = seriesConsumer.getConsumedElements().toArray();

            /* Generate categories for the X axis */
            Collection<@Nullable String> list = xconsumer.getList();
            /*
             * The categories are nullable, but swtchart does not support null
             * values, so we'll update the null values to an empty string
             */
            String @Nullable [] categories = list.toArray(new String[list.size()]);
            for (int i = 0; i < list.size(); i++) {
                if (categories[i] == null) {
                    categories[i] = "?"; //$NON-NLS-1$
                }
            }
            fCategories = categories;

            /* Generate numerical data for the Y axis */
            double[] yData = new double[yConsumer.getData().size()];
            for (int i = 0; i < yData.length; i++) {
                Number number = checkNotNull(yConsumer.getData().get(i));
                yData[i] = fYRanges.getInternalValue(number).doubleValue();
            }

            /* Set the data for the SWT series */
            ISeries series = checkNotNull(getSeriesMap().get(seriesConsumer.getSeries()));
            series.setYSeries(yData);

            /* Create a series mapper */
            mapper.put(series, checkNotNull(object));
        }
    }

    @Override
    protected void configureAxes() {
        /* Format X axes */
        Stream.of(getChart().getAxisSet().getXAxes()).forEach(a -> {
            a.enableCategory(true);
            a.setCategorySeries(fCategories);
        });

        /* Format Y axes */
        Stream.of(getChart().getAxisSet().getYAxes()).forEach(a -> {
            IAxisTick tick = a.getTick();
            tick.setFormat(getContinuousAxisFormatter(fYRanges, getYDescriptorsInfo()));
        });
    }

    @Override
    protected void setSelection(@NonNull Set<@NonNull Object> set) {
        super.setSelection(set);

        /* Set color of selected symbol */
        Iterator<Color> colorsIt = Iterators.cycle(COLORS);
        Iterator<Color> lightColorsIt = Iterators.cycle(COLORS_LIGHT);

        for (ISeries series : getChart().getSeriesSet().getSeries()) {
            /* Series color */
            Color lightColor = NonNullUtils.checkNotNull(lightColorsIt.next());
            Color color = NonNullUtils.checkNotNull(colorsIt.next());

            if (set.isEmpty()) {
                /* Put all symbols to the normal colors */
                ((IBarSeries) series).setBarColor(color);
            } else {
                /*
                 * Fill with light colors to represent the deselected state. The
                 * paint listener is then responsible for drawing the cross and
                 * the dark colors for the selection.
                 */
                ((IBarSeries) series).setBarColor(lightColor);
            }
        }
    }

    @Override
    protected void refreshDisplayLabels() {
        String @Nullable [] categories = fCategories;
        /* Only if we have at least 1 category */
        if (categories == null || categories.length == 0) {
            return;
        }

        /* Only refresh if labels are visible */
        IAxis xAxis = getChart().getAxisSet().getXAxis(0);
        if (!xAxis.getTick().isVisible() || !xAxis.isCategoryEnabled()) {
            return;
        }

        /*
         * Shorten all the labels to 5 characters plus "…" when the longest
         * label length is more than a percentage of the chart height.
         */
        Rectangle rect = getChart().getClientArea();
        int lengthLimit = (int) (rect.height * LENGTH_LIMIT);

        GC gc = new GC(getParent());
        gc.setFont(xAxis.getTick().getFont());

        /* Find the longest category string */
        String longestString = Arrays.stream(categories).max(Comparator.comparingInt(String::length)).orElse(categories[0]);

        /* Get the length and height of the longest label in pixels */
        Point pixels = gc.stringExtent(longestString);

        /* Completely arbitrary */
        int cutLen = 5;

        String[] displayCategories = new String[categories.length];
        if (pixels.x > lengthLimit) {
            /* We have to cut down some strings */
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].length() > cutLen) {
                    displayCategories[i] = categories[i].substring(0, cutLen) + ELLIPSIS;
                } else {
                    displayCategories[i] = categories[i];
                }
            }
        } else {
            /* All strings should fit */
            displayCategories = Arrays.copyOf(categories, categories.length);
        }
        xAxis.setCategorySeries(displayCategories);

        /* Cleanup */
        gc.dispose();
    }

    // ------------------------------------------------------------------------
    // Listeners
    // ------------------------------------------------------------------------

    private final class MouseDownListener extends MouseAdapter {
        @Override
        public void mouseDown(@Nullable MouseEvent event) {
            if (event == null || event.button != 1) {
                return;
            }

            /* Get the click location */
            int xClick = event.x;
            int yClick = event.y;

            /* Check if CTRL is pressed */
            boolean ctrl = (event.stateMask & SWT.CTRL) != 0;

            /* Find which series contains the click */
            boolean found = false;
            for (ISeries swtSeries : getChart().getSeriesSet().getSeries()) {
                IBarSeries series = (IBarSeries) swtSeries;

                /* Look through each rectangle */
                Rectangle[] rectangles = series.getBounds();
                for (int i = 0; i < rectangles.length; i++) {
                    if (rectangles[i].contains(xClick, yClick)) {
                        getSelection().touch(new SwtChartPoint(series, i), ctrl);
                        found = true;
                    }
                }
            }

            /* Check if a selection was found */
            if (!found) {
                getSelection().clear();
            }

            /* Redraw the selected points */
            refresh();

            /* Find these points map to which objects */
            Set<Object> set = new HashSet<>();
            for (SwtChartPoint point : getSelection().getPoints()) {
                Object[] objects = checkNotNull(getObjectMap().get(point.getSeries()));

                /* Add objects to the set */
                Object obj = objects[point.getIndex()];
                if (obj != null) {
                    set.add(obj);
                }
            }

            /* Send the update signal */
            setSelection(set);
            ChartSelectionUpdateSignal signal = new ChartSelectionUpdateSignal(SwtBarChart.this, getData().getDataProvider(), set);
            TmfSignalManager.dispatchSignal(signal);
        }
    }

    private final class BarPainterListener implements PaintListener {
        @Override
        public void paintControl(@Nullable PaintEvent event) {
            if (event == null) {
                return;
            }

            /* Don't draw if there's no selection */
            if (getSelection().getPoints().size() == 0) {
                return;
            }

            /* Create iterators for the colors */
            Iterator<Color> colors = Iterators.cycle(COLORS);
            Iterator<Color> lights = Iterators.cycle(COLORS_LIGHT);

            GC gc = event.gc;

            /* Redraw all the series */
            for (ISeries swtSeries : getChart().getSeriesSet().getSeries()) {
                IBarSeries series = (IBarSeries) swtSeries;
                Color color = checkNotNull(colors.next());
                Color light = checkNotNull(lights.next());

                /* Redraw all the rectangles */
                for (int i = 0; i < series.getBounds().length; i++) {
                    gc.setBackground(light);

                    /* Check if the rectangle is selected */
                    for (SwtChartPoint point : getSelection().getPoints()) {
                        if (point.getSeries() == series && point.getIndex() == i) {
                            gc.setBackground(color);
                            break;
                        }
                    }

                    gc.fillRectangle(series.getBounds()[i]);
                }
            }
        }
    }

}
