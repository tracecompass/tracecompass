/*******************************************************************************
 * Copyright (c) 2017, 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.chart.ui.swtbot.tests.shared;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.Format;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.matchers.AbstractMatcher;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartType;
import org.eclipse.tracecompass.internal.tmf.chart.ui.data.ChartRange;
import org.eclipse.tracecompass.internal.tmf.chart.ui.data.ChartRangeMap;
import org.eclipse.tracecompass.internal.tmf.chart.ui.format.ChartDecimalUnitFormat;
import org.eclipse.tracecompass.internal.tmf.chart.ui.format.ChartTimeStampFormat;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ISeries;
import org.swtchart.ISeriesSet;

/**
 * Class that provides utility methods to test custom charts with swtbot
 *
 * @author Geneviève Bastien
 */
public final class SWTBotCustomChartUtils {

    /**
     * The index in the dialog for the table to select the chart type
     */
    public static final int CHART_TYPE_TABLE_INDEX = 0;
    /**
     * The index in the dialog for the table containing the selected series
     */
    public static final int SERIES_SELECTION_TABLE_INDEX = 1;
    /**
     * The index in the dialog for the table with the choices for the X axis
     */
    public static final int X_SERIES_TABLE_INDEX = 2;
    /**
     * The index in the dialog for the table with the choices for the X axis
     */
    public static final int Y_SERIES_TABLE_INDEX = 3;


    /**
     * An enum for the chart axis
     */
    public enum AxisType {
        /**
         * Represents the X axis
         */
        X,
        /**
         * Represents the Y axis
         */
        Y
    }

    private SWTBotCustomChartUtils() {

    }

    private static void ensureDialogFocus(SWTWorkbenchBot bot) {
        bot.shell("Custom chart creation").activate();
    }

    /**
     * Select the chart type from the dialog maker.
     *
     * The chart maker dialog must have been opened by the caller, this method
     * will put focus on the dialog.
     *
     * @param bot
     *            The SWT workbench bot to use
     * @param chartType
     *            The type of chart to select
     */
    public static void selectChartType(SWTWorkbenchBot bot, ChartType chartType) {
        ensureDialogFocus(bot);
        int index = 0;
        switch (chartType) {
        case BAR_CHART:
            index = 0;
            break;
        case SCATTER_CHART:
            index = 1;
            break;
        case PIE_CHART:
        default:
            throw new IllegalStateException("Unsupported chart type: " + chartType.name());
        }
        // The chart selection table is the first
        SWTBotTable table = bot.table(CHART_TYPE_TABLE_INDEX);
        // Click on the row corresponding to the chart type
        table.click(index, 0);
    }

    /**
     * Add series to the chart. The X and Y values are the texts of the series
     * to select. This method will fail if one of the requested series string is
     * not available.
     *
     * The chart maker dialog must have been opened by the caller, this method
     * will put focus on the dialog.
     *
     * @param bot
     *            The SWT workbench bot to use
     * @param xSerie
     *            The title of the X axis series to select
     * @param ySeries
     *            The titles of the Y axis series to select. Many Y series can
     *            be selected at the same time
     */
    public static void addSeries(SWTWorkbenchBot bot, String xSerie, Set<String> ySeries) {
        ensureDialogFocus(bot);
        // The X and Y tables are the 3rd and 4th respectively
        SWTBotTable xTable = bot.table(X_SERIES_TABLE_INDEX);
        xTable.getTableItem(xSerie).click();

        SWTBotTable yTable = bot.table(Y_SERIES_TABLE_INDEX);
        ySeries.stream().forEach(s -> yTable.getTableItem(s).check());

        bot.buttonInGroup("Series Creator", 0).click();
    }

    /**
     * Check logarithmic scale button for the axis.
     *
     * The chart maker dialog must have been opened by the caller, this method
     * will put focus on the dialog.
     *
     * @param bot
     *            The SWT workbench bot to use
     * @param axisType
     *            The axis for which to set the log scale
     */
    public static void setLogScale(SWTWorkbenchBot bot, AxisType axisType) {
        ensureDialogFocus(bot);
        SWTBotCheckBox checkbox = bot.checkBox("Logarithmic Scale " + axisType.name());
        assertTrue(checkbox.isEnabled());
        checkbox.select();
    }

    /**
     * Press the OK button of the chart maker dialog.
     *
     * The chart maker dialog must have been opened by the caller, this method
     * will put focus on the dialog.
     *
     * @param bot
     *            The SWT workbench bot to use
     */
    public static void confirmDialog(SWTWorkbenchBot bot) {
        ensureDialogFocus(bot);
        bot.button("OK").click();
    }

    /**
     * Close the chart received in parameter. It clicks on the close button of
     * the chart.
     *
     * @param bot
     *            The SWT workbench bot to use
     * @param customChart
     *            The custom chart to close
     */
    public static void closeChart(SWTWorkbenchBot bot, Chart customChart) {
        Button closeButton = bot.widget(closeButtonForChart(customChart), 0);
        SWTBotButton button = new SWTBotButton(closeButton);
        button.click();
    }

    private static Matcher<Button> closeButtonForChart(final Chart chart) {
        return new AbstractMatcher<Button>() {

            @Override
            protected boolean doMatch(Object item) {
                if (!(item instanceof Button)) {
                    return false;
                }
                Button button = (Button) item;
                return (button.getParent() == chart);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("delete button for custom chart");
            }
        };
    }

    /**
     * Verify the titles of the chart and its axis
     *
     * @param chart
     *            The chart to verify
     * @param chartTitle
     *            The title of the chart
     * @param xTitle
     *            The title of the X axis
     * @param yTitle
     *            The title of the Y axis
     */
    public static void assertTitles(Chart chart, String chartTitle, String xTitle, String yTitle) {
        String title = UIThreadRunnable.syncExec((Result<String>) () -> {
            return chart.getTitle().getText();
        });
        assertEquals("Title", chartTitle, title);
        title = UIThreadRunnable.syncExec((Result<String>) () -> {
            return chart.getAxisSet().getXAxis(0).getTitle().getText();
        });
        assertEquals("X axis title", xTitle, title);
        title = UIThreadRunnable.syncExec((Result<String>) () -> {
            return chart.getAxisSet().getYAxis(0).getTitle().getText();
        });
        assertEquals("Y axis title", yTitle, title);
    }

    /**
     * Verify that the internal range of the axis corresponds to the expected
     * values. This method should be called on axis using numerical or time
     * stamp formatters
     *
     * @param chart
     *            The chart to verify
     * @param axisType
     *            The axis to test for
     * @param min
     *            The minimum range value
     * @param max
     *            The maximum range value
     */
    public static void assertAxisRange(Chart chart, AxisType axisType, Number min, Number max) {
        IAxis axis = (axisType == AxisType.X ? chart.getAxisSet().getXAxes()[0] : chart.getAxisSet().getYAxes()[0]);
        Format format = axis.getTick().getFormat();

        // Get the range map for the format, it has to have one
        ChartRangeMap map = null;
        if (format instanceof ChartTimeStampFormat) {
            map = ((ChartTimeStampFormat) format).getRangeMap();
        } else if (format instanceof ChartDecimalUnitFormat) {
            map = ((ChartDecimalUnitFormat) format).getRangeMap();
        }
        assertNotNull(map);

        ChartRange inputDataRange = map.getInputDataRange();
        assertEquals(min.doubleValue(), inputDataRange.getMinimum().doubleValue(), 1);
        assertEquals(max.doubleValue(), inputDataRange.getMaximum().doubleValue(), 1);

    }

    /**
     * Verify the categories of an axis correspond to the excpected values. This
     * method should be called on axis using discrete string values.
     *
     * @param chart
     *            The chart to verify
     * @param axisType
     *            The axis to test for
     * @param categories
     *            The expected categories
     */
    public static void assertCategoriesAxis(Chart chart, AxisType axisType, String[] categories) {
        IAxis axis = (axisType == AxisType.X ? chart.getAxisSet().getXAxes()[0] : chart.getAxisSet().getYAxes()[0]);
        assertTrue(axis.isCategoryEnabled());
        String[] categorySeries = axis.getCategorySeries();
        assertArrayEquals(categories, categorySeries);
    }

    /**
     * Verify the title of the series. These titles are shown in the legend if
     * there is more than one serie
     *
     * @param customChart
     *            The chart to verify
     * @param titles
     *            The list of series titles
     */
    public static void assertSeriesTitle(Chart customChart, List<String> titles) {
        ISeriesSet seriesSet = customChart.getSeriesSet();
        assertNotNull(seriesSet);
        ISeries[] series = seriesSet.getSeries();

        assertEquals(titles.size(), series.length);
        for (int i = 0; i < series.length; i++) {
            assertEquals("Series title " + i, titles.get(i), series[i].getId());
        }
    }

    /**
     * Verify an axis log scale status
     *
     * @param chart
     *            The chart to verify
     * @param axisType
     *            The axis to test for
     * @param logscale
     *            <code>true</code> if this axis should be logscale,
     *            <code>false</code> otherwise
     */
    public static void assertAxisLogscale(Chart chart, AxisType axisType, boolean logscale) {
        IAxis axis = (axisType == AxisType.X ? chart.getAxisSet().getXAxes()[0] : chart.getAxisSet().getYAxes()[0]);
        assertEquals("Log scale", logscale, axis.isLogScaleEnabled());
    }

}
