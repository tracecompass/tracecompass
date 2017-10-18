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

import java.math.BigDecimal;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartData;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartModel;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartSeries;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.IDataChartDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.INumericalResolver;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.signal.ChartSelectionUpdateSignal;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.ui.chart.IChartViewer;
import org.eclipse.tracecompass.internal.tmf.chart.core.aggregator.IConsumerAggregator;
import org.eclipse.tracecompass.internal.tmf.chart.core.consumer.IDataConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.consumer.XYChartConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.consumer.XYSeriesConsumer;
import org.eclipse.tracecompass.internal.tmf.chart.ui.data.ChartRangeMap;
import org.eclipse.tracecompass.internal.tmf.chart.ui.data.DescriptorsInformation;
import org.eclipse.tracecompass.internal.tmf.chart.ui.format.ChartDecimalUnitFormat;
import org.eclipse.tracecompass.internal.tmf.chart.ui.format.ChartTimeStampFormat;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisTick;
import org.swtchart.ISeries;
import org.swtchart.ISeriesSet;
import org.swtchart.ITitle;

import com.google.common.collect.Iterators;

/**
 * Abstract class for XY charts. These kind of charts can take as many X and Y
 * descriptors. Bar charts and scatter charts are examples of possible XY
 * charts.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public abstract class SwtXYChartViewer extends TmfViewer implements IChartViewer {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Ellipsis character
     */
    protected static final char ELLIPSIS = '…';
    /**
     * Time stamp formatter for intervals in the days range
     */
    private static final String DAYS_FORMAT = "dd HH:mm"; //$NON-NLS-1$
    /**
     * Time stamp formatter for intervals in the hours range
     */
    private static final String HOURS_FORMAT = "HH:mm"; //$NON-NLS-1$
    /**
     * Time stamp formatter for intervals in the minutes range
     */
    private static final String MINUTES_FORMAT = "mm:ss"; //$NON-NLS-1$
    /**
     * Time stamp formatter for intervals in the seconds range
     */
    private static final String SECONDS_FORMAT = "ss"; //$NON-NLS-1$
    /**
     * Time stamp formatter for intervals in the milliseconds range
     */
    private static final String MILLISECONDS_FORMAT = "ss.SSS"; //$NON-NLS-1$

    private static final int CLOSE_BUTTON_SIZE = 25;
    private static final int CLOSE_BUTTON_MARGIN = 5;

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    /**
     * Top-right close button
     */
    private final Button fCloseButton;
    /**
     * SWT Chart object
     */
    private final Chart fChart;
    /**
     * Data to plot into the chart
     */
    private final ChartData fData;
    /**
     * Model used to make the chart
     */
    private final ChartModel fModel;
    /**
     * Information about the set of X descriptors
     */
    private final DescriptorsInformation fXInformation;
    /**
     * Information about the set of Y descriptors
     */
    private final DescriptorsInformation fYInformation;
    /**
     * Chart consumer for processing data
     */
    private @Nullable XYChartConsumer fChartConsumer;
    /**
     * Map between series and SWT series
     */
    private final Map<ChartSeries, ISeries> fSeriesMap;
    /**
     * Map between SWT series and consumed objects
     */
    private final Map<ISeries, Object[]> fObjectMap;
    /**
     * X axis title
     */
    private String fXTitle;
    /**
     * Y axis title
     */
    private String fYTitle;
    /**
     * Object that contains the selection in the chart
     */
    private final SwtChartSelection fSelectedPoints = new SwtChartSelection();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor for the chart. Any final class that derives this class must
     * call the {@link #populate()} method to create the rest of the chart.
     *
     * @param parent
     *            A parent composite
     * @param data
     *            A configured data series for the chart
     * @param model
     *            A chart model to use
     */
    public SwtXYChartViewer(Composite parent, ChartData data, ChartModel model) {
        fParent = parent;
        fData = data;
        fModel = model;
        fSeriesMap = new HashMap<>(data.getChartSeries().size());
        fObjectMap = new HashMap<>(data.getChartSeries().size());
        fXInformation = DescriptorsInformation.create(getXDescriptors());
        fYInformation = DescriptorsInformation.create(getYDescriptors());

        validateChartData();

        fChart = new Chart(parent, SWT.NONE);

        /*
         * Temporarily generate titles, they may be modified once the data has
         * been parsed (with formatting information, units, etc)
         */
        fXTitle = generateTitle(getXDescriptors(), getChart().getAxisSet().getXAxis(0));
        fYTitle = generateTitle(getYDescriptors(), getChart().getAxisSet().getYAxis(0));

        /* Set all titles and labels font color to black */
        fChart.getTitle().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
        fChart.getAxisSet().getXAxis(0).getTitle().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
        fChart.getAxisSet().getYAxis(0).getTitle().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
        fChart.getAxisSet().getXAxis(0).getTick().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
        fChart.getAxisSet().getYAxis(0).getTick().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));

        /* Set X label 90 degrees */
        fChart.getAxisSet().getXAxis(0).getTick().setTickLabelAngle(90);

        /* Set the legend position if necessary */
        if (getData().getChartSeries().size() > 1) {
            fChart.getLegend().setPosition(SWT.BOTTOM);
        } else {
            fChart.getLegend().setVisible(false);
        }

        /* Refresh the titles to fit the current chart size */
        refreshDisplayTitles();

        /* Create the close button */
        Image close = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ELCL_REMOVE);
        fCloseButton = new Button(fChart, SWT.PUSH);
        fCloseButton.setSize(CLOSE_BUTTON_SIZE, CLOSE_BUTTON_SIZE);
        fCloseButton.setLocation(fChart.getSize().x - fCloseButton.getSize().x - CLOSE_BUTTON_MARGIN, CLOSE_BUTTON_MARGIN);
        fCloseButton.setImage(close);
        fCloseButton.addSelectionListener(new CloseButtonEvent());

        /* Add listeners for the visibility of the close button and resizing */
        Listener mouseEnter = new MouseEnterEvent();
        Listener mouseExit = new MouseExitEvent();
        fChart.getDisplay().addFilter(SWT.MouseEnter, mouseEnter);
        fChart.getDisplay().addFilter(SWT.MouseExit, mouseExit);
        fChart.addDisposeListener(event -> {
            fChart.getDisplay().removeFilter(SWT.MouseEnter, mouseEnter);
            fChart.getDisplay().removeFilter(SWT.MouseExit, mouseExit);
        });
        fChart.addControlListener(new ResizeEvent());
    }

    /**
     * This method is called after the constructor by the factory constructor.
     * While everything could be put in the constructor directly, splitting the
     * constructor simply makes the code cleaner by reducing the number of null
     * checks.
     */
    protected final void populate() {
        /* Create the consumer for the data */
        XYChartConsumer chartConsumer = createChartConsumer();
        fChartConsumer = chartConsumer;

        /* Process all the objects from the stream of data */
        fData.getDataProvider().getSource().forEach(o -> {
            chartConsumer.accept(o);
        });
        chartConsumer.finish();

        /* Create the SWT series */
        createChartSeries();
        configureSeries(fObjectMap);

        /* Adjust the chart range */
        getChart().getAxisSet().adjustRange();

        /* Configure axes */
        configureAxes();
        Arrays.stream(getChart().getAxisSet().getXAxes()).forEach(a -> a.enableLogScale(getModel().isXLogscale()));
        Arrays.stream(getChart().getAxisSet().getYAxes()).forEach(a -> a.enableLogScale(getModel().isYLogscale()));

        for (IAxis yAxis : getChart().getAxisSet().getYAxes()) {
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
            yAxis.getTick().setTickLabelAngle(1);
        }

        /* Update the titles */
        fXTitle = generateTitle(getXDescriptors(), getChart().getAxisSet().getXAxis(0));
        fYTitle = generateTitle(getYDescriptors(), getChart().getAxisSet().getYAxis(0));

        /* Refresh the titles to fit the current chart size */
        refreshDisplayTitles();
        refreshDisplayLabels();
    }

    /**
     * For each series defined for the chart, create the corresponding SWT
     * series and add it to the series map
     */
    private final void createChartSeries() {
        ISeriesSet set = NonNullUtils.checkNotNull(getChart().getSeriesSet());
        Iterator<Color> colors = Iterators.cycle(COLORS);

        getData().getChartSeries().forEach(series -> {
            ISeries swtSeries = createSwtSeries(series, set, colors.next());
            fSeriesMap.put(series, swtSeries);
        });
    }

    /**
     * Create the main consumer for the chart. For each series, it create the
     * series consumer. It also needs the X and Y aggregators.
     */
    private final XYChartConsumer createChartConsumer() {
        List<XYSeriesConsumer> series = new ArrayList<>();

        getData().getChartSeries().forEach(s -> {
            IDataConsumer xConsumer = getXConsumer(s);
            IDataConsumer yConsumer = getYConsumer(s);

            /* Create consumer for this series */
            series.add(new XYSeriesConsumer(s, xConsumer, yConsumer));
        });

        /* Get the aggregators */
        IConsumerAggregator xAggregator = getXAggregator();
        IConsumerAggregator yAggregator = getYAggregator();

        /* Create the chart consumer */
        return new XYChartConsumer(series, xAggregator, yAggregator);
    }

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public void dispose() {
        if (!fChart.isDisposed()) {
            fChart.dispose();
        }
        fParent.layout();
        super.dispose();
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

    // ------------------------------------------------------------------------
    // Abstract methods
    // ------------------------------------------------------------------------

    /**
     * Get the X consumer for a series
     *
     * @param series
     *            The chart series to get the consumer for
     * @return The consumer for the X component of the series
     */
    protected abstract IDataConsumer getXConsumer(ChartSeries series);

    /**
     * Get the Y consumer for a series
     *
     * @param series
     *            The chart series to get the consumer for
     * @return The consumer for the Y component of the series
     */
    protected abstract IDataConsumer getYConsumer(ChartSeries series);

    /**
     * Get an aggregator for the X axis
     *
     * @return The aggregator for the X axis
     */
    protected abstract @Nullable IConsumerAggregator getXAggregator();

    /**
     * Get an aggregator for the Y axis
     *
     * @return The aggregator for the Y axis
     */
    protected abstract @Nullable IConsumerAggregator getYAggregator();

    /**
     * This method creates the properly configured swtSeries from a chartSeries.
     * It should set the title of the series and add it to the SWT series set.
     * Typically, an implementation of this method would contain code that looks
     * like this
     *
     * <code>
     * String title = "title"; // Get title from chartSeries for this series
     * ISeries series = swtSeriesSet.createSeries(SeriesType.LINE, title);
     * return series;
     * </code>
     *
     * @param chartSeries
     *            The chart series for which to create the SWT series
     * @param swtSeriesSet
     *            The SWT series set for this chart
     * @param color
     *            The color to associate with this series
     *
     * @return The SWT series associated with the chartSeries in parameter
     */
    protected abstract ISeries createSwtSeries(ChartSeries chartSeries, ISeriesSet swtSeriesSet, Color color);

    /**
     * This methods processes the data created from the {@link XYChartConsumer}
     * and configures the SWT series. Since SWT Chart only supports double value
     * for numbers, this method should convert the {@link Number} into double
     * and sets them into the {@link ISeries}.
     * <p>
     * In order to allow signals and selection, we need to know which object is
     * link to a point in a certain SWT series. To do this, we use a map between
     * the objects that have been consumed by a {@link XYChartConsumer} and the
     * series itself. It is the implementation's responsibility to populate the
     * mapper for each series.
     *
     * FIXME: The implementations are not ideal. Maybe the approach with
     * consumers is not ideal and it should rather be Functions that return the
     * datapoints for the data consumer and the series' ranges for the series
     * consumer. Some investigation needed. The current implementation is left
     * as is for now.
     *
     * @param mapper
     *            The mapper to put the list of object in
     */
    protected abstract void configureSeries(Map<ISeries, Object[]> mapper);

    /**
     * This method configures the axes. At this point, the data has been
     * computed, the axis know what it is supposed to show, so the formatters
     * can be set and the ticks updated.
     */
    protected abstract void configureAxes();

    /**
     * This method refreshed the display labels. For instance, it will make sure
     * that labels do not occupy to much real estate in the chart and trim it
     * and replace with ellipsis the end of the string.
     *
     * TODO: See if this method can be automatic or try to put some of its logic
     * in the parent class instead of having the implementations do all the
     * work.
     */
    protected abstract void refreshDisplayLabels();

    // ------------------------------------------------------------------------
    // Signals
    // ------------------------------------------------------------------------

    /**
     * Handler that handles a {@link ChartSelectionUpdateSignal} coming from
     * another analysis.
     *
     * @param signal
     *            The received signal
     */
    @TmfSignalHandler
    public void updateSelection(ChartSelectionUpdateSignal signal) {
        Object source = signal.getSource();
        if (equals(source) || !getData().getDataProvider().equals(signal.getDataProvider())) {
            return;
        }

        Set<Object> set = signal.getSelectedObject();
        setSelection(set);

        /* Redraw the points */
        refresh();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * This method is called when the selection is updated, before refreshing
     * the viewer. This is the place for instance to set the colors for the
     * series if anything special has to be done with selections.
     *
     * @param set
     *            The set of selected objects
     */
    protected void setSelection(Set<@NonNull Object> set) {
        /* Clear the current list of points */
        fSelectedPoints.clear();

        /* Search the each object through all the series */

        for (ISeries series : getObjectMap().keySet()) {
            Object[] objects = checkNotNull(getObjectMap().get(series));

            for (int i = 0; i < objects.length; i++) {
                if (set.contains(objects[i])) {
                    fSelectedPoints.add(new SwtChartPoint(series, i));
                }
            }
        }
    }

    /**
     * This method makes sure that the {@link ChartData}} is properly built for
     * making XY charts.
     */
    protected void validateChartData() {
        /* Make sure X logscale is disabled if data is discontinuous */
        if (!getXDescriptorsInfo().areNumerical() && getModel().isXLogscale()) {
            throw new IllegalArgumentException("Cannot have logarithmic scale on discontinuous data."); //$NON-NLS-1$
        }

        /* Make sure Y logscale is disabled if data is discontinuous */
        if (!getYDescriptorsInfo().areNumerical() && getModel().isYLogscale()) {
            throw new IllegalArgumentException("Cannot have logarithmic scale on discontinuous data."); //$NON-NLS-1$
        }
    }

    /**
     * @return The title of the chart
     */
    protected String getTitle() {
        return getModel().getTitle();
    }

    private static String generateTitle(Collection<IDataChartDescriptor<?, ?>> descriptors, @Nullable IAxis axis) {
        String title;

        if (descriptors.isEmpty()) {
            return StringUtils.EMPTY;
        }
        IDataChartDescriptor<?, ?> descriptor = descriptors.iterator().next();

        /*
         * There are multiple series in the chart, if they all share the same
         * units, display that.
         */
        long nbDiffDescriptorName = descriptors.stream()
                .map(d -> d.getName())
                .distinct()
                .count();

        long nbDiffDescriptorUnits = descriptors.stream()
                .map(d -> d.getUnit())
                .distinct()
                .count();

        title = "Value"; //$NON-NLS-1$
        if (nbDiffDescriptorName == 1) {
            title = descriptor.getName();
        }

        String units = null;
        if (nbDiffDescriptorUnits == 1) {
            units = descriptor.getUnit();
        }

        if (units != null) {
            title = title + ' ' + '(' + units + ')';
        }

        // Add the formatter time units to the title
        if (axis != null) {
            Format format = axis.getTick().getFormat();
            if (format instanceof ChartTimeStampFormat) {
                title = title + ' ' + '(' + ((ChartTimeStampFormat) format).getPattern() + ')';
            }
        }

        return title;
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
             * If the title is too long, generate a shortened version based on
             * the average character width of the current font.
             */
            if (pixels.x > maxPixelLength) {
                int charwidth = gc.getFontMetrics().getAverageCharWidth();

                int minimum = 3;

                int strLen = ((maxPixelLength / charwidth) - minimum);

                if (strLen > minimum) {
                    newTitle = canonicalTitle.substring(0, strLen) + ELLIPSIS;
                } else {
                    newTitle = String.valueOf(ELLIPSIS);
                }
            }

            title.setText(newTitle);

            /* Cleanup */
            gc.dispose();
        }
    }

    /**
     * Refresh the Chart, XAxis and YAxis titles to fit the current chart size.
     */
    private void refreshDisplayTitles() {
        Rectangle chartRect = fChart.getClientArea();
        Rectangle plotRect = fChart.getPlotArea().getClientArea();

        ITitle chartTitle = checkNotNull(fChart.getTitle());
        refreshDisplayTitle(chartTitle, getTitle(), chartRect.width);

        ITitle xTitle = checkNotNull(fChart.getAxisSet().getXAxis(0).getTitle());
        refreshDisplayTitle(xTitle, fXTitle, plotRect.width);

        ITitle yTitle = checkNotNull(fChart.getAxisSet().getYAxis(0).getTitle());
        refreshDisplayTitle(yTitle, fYTitle, plotRect.height);
    }

    // ------------------------------------------------------------------------
    // Util methods
    // ------------------------------------------------------------------------

    /**
     * Util method to clamp the input data range of a range map. It returns the
     * map passed in parameter for chaining a method.
     *
     * @param map
     *            The map to clamp the input data range
     * @return The same map passed in parameter
     */
    protected static ChartRangeMap clampInputDataRange(ChartRangeMap map) {
        map.getInputDataRange().clamp();

        return map;
    }

    /**
     * Util method to update tick mark of an axis. This step is a limitation on
     * swtchart side regarding minimal grid step hint size. When the step size
     * are smaller it get defined as the "default" value for the axis instead of
     * the smallest one.
     *
     * @param map
     *            Map of labels used to compute the minimum size required
     * @param tick
     *            Axis tick to update
     * @param availableLenghtPixel
     *            Available lenght in pixel
     */
    protected static void updateTickMark(Map<String, Integer> map, IAxisTick tick, int availableLenghtPixel) {
        int nbLabels = Math.max(1, map.size());
        int stepSizePixel = availableLenghtPixel / nbLabels;

        if (IAxisTick.MIN_GRID_STEP_HINT > stepSizePixel) {
            stepSizePixel = (int) IAxisTick.MIN_GRID_STEP_HINT;
        }
        tick.setTickMarkStepHint(stepSizePixel);
    }

    /**
     * Util method that will return a formatter based on the descriptors linked
     * to an axis.
     *
     * If all descriptors are time stamps, return a timestamp formatter tuned to
     * the interval. If all descriptors are time durations, return the
     * nanoseconds to seconds formatter. Otherwise, return the generic decimal
     * formatter.
     *
     * @param map
     *            The range map that the formatter will use
     * @param info
     *            Informations of the descriptors tied to the formatter
     * @return The formatter for the axis.
     */
    protected static Format getContinuousAxisFormatter(ChartRangeMap map, DescriptorsInformation info) {
        Format formatter;

        if (info.areTimestamp()) {
            BigDecimal max = map.getInputDataRange().getMinimum();
            BigDecimal min = map.getInputDataRange().getMaximum();

            /* Find the best formatter for our values */
            long duration = max.subtract(min).longValue();
            if (duration > TimeUnit.DAYS.toNanos(1)) {
                formatter = new ChartTimeStampFormat(DAYS_FORMAT, map);
            } else if (duration > TimeUnit.HOURS.toNanos(1)) {
                formatter = new ChartTimeStampFormat(HOURS_FORMAT, map);
            } else if (duration > TimeUnit.MINUTES.toNanos(1)) {
                formatter = new ChartTimeStampFormat(MINUTES_FORMAT, map);
            } else if (duration > TimeUnit.SECONDS.toNanos(1)) {
                formatter = new ChartTimeStampFormat(SECONDS_FORMAT, map);
            } else {
                formatter = new ChartTimeStampFormat(MILLISECONDS_FORMAT, map);
            }
        } else if (info.areDuration()) {
            /*
             * Use the time duration formatter
             *
             * FIXME: Was previously using NANO_TO_SECS_FORMATTER, but a
             * duration won't necessarily be in nanoseconds. Besides, if
             * changing the input unit of the data, we should also make sure
             * that the titles fit. So we just keep the original units/values
             * for now
             */
            formatter = new ChartDecimalUnitFormat(map);
        } else {
            /*
             * Use the default decimal formatter for other numeric descriptors
             */
            formatter = new ChartDecimalUnitFormat(map);
        }

        return formatter;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that returns the SWT chart object.
     *
     * @return The receiver's chart
     */
    protected Chart getChart() {
        return fChart;
    }

    /**
     * Accessor that returns the chart data.
     *
     * @return The data to make a chart from
     */
    protected ChartData getData() {
        return fData;
    }

    /**
     * Accessor that returns the chart model.
     *
     * @return The model to make a chart from
     */
    protected ChartModel getModel() {
        return fModel;
    }

    /**
     * Accessor that returns the information about the set of X descriptors to
     * plot.
     *
     * @return Information about the X descriptors
     */
    protected DescriptorsInformation getXDescriptorsInfo() {
        return fXInformation;
    }

    /**
     * Accessor that returns the information about the set of Y descriptors to
     * plot.
     *
     * @return Information about the Y descriptors
     */
    protected DescriptorsInformation getYDescriptorsInfo() {
        return fYInformation;
    }

    /**
     * Accessor that returns the {@link XYChartConsumer} of this chart.
     *
     * @return The consumer of this chart
     */
    protected XYChartConsumer getChartConsumer() {
        return checkNotNull(fChartConsumer);
    }

    /**
     * Accessor that returns the map between data series and SWT series objects.
     *
     * @return The map between series and SWT series
     */
    protected Map<ChartSeries, ISeries> getSeriesMap() {
        return fSeriesMap;
    }

    /**
     * Accessor that returns the map between SWT series and the objects linked
     * to each point.
     *
     * @return The map between SWT series and consumed objects
     */
    protected Map<ISeries, Object[]> getObjectMap() {
        return fObjectMap;
    }

    /**
     * Accessor that returns the selection in the chart.
     *
     * @return The selection in the chart
     */
    protected SwtChartSelection getSelection() {
        return fSelectedPoints;
    }

    /**
     * Accessor that returns the collection of the X descriptors.
     *
     * @return The stream of X descriptors
     */
    protected Collection<IDataChartDescriptor<?, ?>> getXDescriptors() {
        return checkNotNull(getData().getChartSeries().stream()
                .map(series -> series.getX())
                .collect(checkNotNull(Collectors.toList())));
    }

    /**
     * Accessor that returns the collection of the Y descriptors.
     *
     * @return The stream of Y descriptors
     */
    protected Collection<IDataChartDescriptor<?, ?>> getYDescriptors() {
        return checkNotNull(getData().getChartSeries().stream()
                .map(series -> series.getY())
                .collect(checkNotNull(Collectors.toList())));
    }

    // ------------------------------------------------------------------------
    // Anonymous Classes
    // ------------------------------------------------------------------------

    /**
     * Predicate that rejects null and negative values.
     */
    protected class LogarithmicPredicate implements Predicate<@Nullable Number> {
        private INumericalResolver<?, Number> fResolver;

        /**
         * Constructor.
         *
         * @param resolver
         *            The resolver used for getting the zero of the same type of
         *            number we are comparing
         */
        protected LogarithmicPredicate(INumericalResolver<?, Number> resolver) {
            fResolver = resolver;
        }

        @Override
        public boolean test(@Nullable Number t) {
            if (t == null) {
                return false;
            }

            return fResolver.getComparator().compare(t, fResolver.getZeroValue()) > 0;
        }
    }

    /**
     * Listener that handles resize events of the chart.
     */
    private class ResizeEvent implements ControlListener {
        @Override
        public void controlMoved(@Nullable ControlEvent e) {
        }

        @Override
        public void controlResized(@Nullable ControlEvent e) {
            /* Refresh titles */
            refreshDisplayTitles();

            /* Refresh the Axis labels to fit the current chart size */
            refreshDisplayLabels();

            /* Relocate the close button */
            fCloseButton.setLocation(fChart.getSize().x - fCloseButton.getSize().x - CLOSE_BUTTON_MARGIN, CLOSE_BUTTON_MARGIN);
        }
    }

    /**
     * Listener that handles events of the close button.
     */
    private class CloseButtonEvent implements SelectionListener {
        @Override
        public void widgetSelected(@Nullable SelectionEvent e) {
            dispose();
        }

        @Override
        public void widgetDefaultSelected(@Nullable SelectionEvent e) {
        }
    }

    /**
     * Listener that handles mouse events when the mouse enter the chart window.
     */
    private class MouseEnterEvent implements Listener {
        @Override
        public void handleEvent(@Nullable Event event) {
            if (event == null) {
                return;
            }

            Control control = (Control) event.widget;
            Point display = control.toDisplay(event.x, event.y);
            Point location = getChart().getParent().toControl(display);

            /* Only set to visible if we are inside and in the right shell. */
            boolean inside = getChart().getBounds().contains(location);
            boolean shell = control.getShell().equals(getChart().getShell());
            fCloseButton.setVisible(inside && shell);
        }
    }

    /**
     * Listener that handles mouse events when the mouse exit the chart window.
     */
    private class MouseExitEvent implements Listener {
        @Override
        public void handleEvent(@Nullable Event event) {
            fCloseButton.setVisible(false);
        }
    }

}
