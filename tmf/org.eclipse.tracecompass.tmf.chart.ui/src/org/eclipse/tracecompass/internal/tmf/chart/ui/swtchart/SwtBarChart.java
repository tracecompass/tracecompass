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
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartData;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartModel;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartSeries;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.INumericalResolver;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.IStringResolver;
import org.eclipse.tracecompass.internal.tmf.chart.core.aggregator.IConsumerAggregator;
import org.eclipse.tracecompass.internal.tmf.chart.core.consumer.IDataConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.core.consumer.NumericalConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.aggregator.NumericalConsumerAggregator;
import org.eclipse.tracecompass.internal.tmf.chart.ui.consumer.BarStringConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.consumer.XYChartConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.consumer.XYSeriesConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.data.ChartRangeMap;
import org.swtchart.IAxis;
import org.swtchart.IAxisTick;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;

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

}
