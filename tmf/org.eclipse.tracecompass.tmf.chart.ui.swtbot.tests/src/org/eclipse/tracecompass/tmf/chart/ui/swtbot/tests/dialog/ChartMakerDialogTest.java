/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.chart.ui.swtbot.tests.dialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRootMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartType;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DataChartDurationDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DataChartNumericalDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DataChartStringDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DataChartTimestampDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.IDataChartDescriptor;
import org.eclipse.tracecompass.tmf.chart.core.model.IDataChartProvider;
import org.eclipse.tracecompass.tmf.chart.core.tests.stubs.StubChartProviderFull;
import org.eclipse.tracecompass.tmf.chart.core.tests.stubs.StubObject;
import org.eclipse.tracecompass.tmf.chart.ui.swtbot.stubs.CustomChartStubView;
import org.eclipse.tracecompass.tmf.chart.ui.swtbot.tests.shared.SWTBotCustomChartUtils;
import org.eclipse.tracecompass.tmf.chart.ui.swtbot.tests.shared.SWTBotCustomChartUtils.AxisType;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test the custom Chart Maker Dialog
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
@RunWith(SWTBotJunit4ClassRunner.class)
public class ChartMakerDialogTest {

    /** The Log4j logger instance. */
    private static final Logger fLogger = NonNullUtils.checkNotNull(Logger.getRootLogger());

    private final SWTWorkbenchBot fBot = new SWTWorkbenchBot();

    private static final IDataChartProvider<StubObject> CHART_PROVIDER = new StubChartProviderFull();

    /**
     * Things to setup
     */
    @BeforeClass
    public static void beforeClass() {

        SWTBotUtils.initialize();
        Thread.currentThread().setName("SWTBotTest");
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
        SWTBotUtils.openView(CustomChartStubView.ID);
        CustomChartStubView.setChartProvider(CHART_PROVIDER);
    }

    /**
     * Clean up
     */
    @AfterClass
    public static void afterClass() {
        fLogger.removeAllAppenders();
    }

    private void openDialog() {
        // Open the new custom chart dialog
        SWTBotView viewBot = fBot.viewById(CustomChartStubView.ID);
        SWTBotRootMenu viewMenu = viewBot.viewMenu();
        SWTBotMenu menu = viewMenu.menu(CustomChartStubView.MENU_TITLE);
        menu.click();
    }

    private List<IDataChartDescriptor<?, ?>> getDescriptors(int tblIndex) {
        SWTBotTable table = fBot.table(tblIndex);

        List<IDataChartDescriptor<?, ?>> items = UIThreadRunnable.syncExec((Result<@Nullable List<IDataChartDescriptor<?, ?>>>) () -> {
            List<IDataChartDescriptor<?, ?>> list = new ArrayList<>();
            for (TableItem item : table.widget.getItems()) {
                Object data = item.getData();
                if (data instanceof IDataChartDescriptor<?, ?>) {
                    IDataChartDescriptor<?, ?> desc = (IDataChartDescriptor<?, ?>) data;
                    list.add(desc);
                }
            }
            return list;
        });
        assertNotNull(items);
        return items;
    }

    private void assertDescriptors(String msg, Collection<@NonNull IDataChartDescriptor<@NonNull StubObject, ?>> expected, int tblIndex) {
        List<IDataChartDescriptor<?, ?>> descriptors = getDescriptors(tblIndex);
        assertEquals(msg + " descriptors count", expected.size(), descriptors.size());
        for (IDataChartDescriptor<?, ?> desc : descriptors) {
            assertTrue(msg + " contains", expected.contains(desc));
        }
    }

    /**
     * Test with an actual trace, this is more of an integration test than a
     * unit test.
     */
    @Test
    public void testBarChartSeries() {
        openDialog();

        SWTBotCustomChartUtils.selectChartType(fBot, ChartType.BAR_CHART);

        /* Test initial descriptors */
        // Only string descriptors should be in X
        List<IDataChartDescriptor<StubObject, ?>> expectedXDescriptors = CHART_PROVIDER.getDataDescriptors().stream().filter(d -> d instanceof DataChartStringDescriptor<?>).collect(Collectors.toList());
        assertDescriptors("Initial X", expectedXDescriptors, SWTBotCustomChartUtils.X_SERIES_TABLE_INDEX);

        // Only numerical descriptors should be in Y
        List<IDataChartDescriptor<StubObject, ?>> expectedYDescriptors = CHART_PROVIDER.getDataDescriptors().stream().filter(d -> d instanceof DataChartNumericalDescriptor<?, ?>).collect(Collectors.toList());
        assertDescriptors("Initial Y", expectedYDescriptors, SWTBotCustomChartUtils.Y_SERIES_TABLE_INDEX);

        SWTBotTable yTable = fBot.table(SWTBotCustomChartUtils.Y_SERIES_TABLE_INDEX);
        SWTBotCheckBox chkLogX = fBot.checkBox("Logarithmic Scale " + AxisType.X);
        SWTBotCheckBox chkLogY = fBot.checkBox("Logarithmic Scale " + AxisType.Y);

        /* Test selection and filter of Y descriptors */
        /*
         * In Y, check one of each type, make sure the other types are filtered
         * out, and uncheck
         */
        /* Check a numerical descriptor */
        String toCheck = expectedYDescriptors.stream()
                .filter(d -> d.getClass() == DataChartNumericalDescriptor.class)
                .map(d -> d.getLabel())
                .findFirst().get();
        yTable.getTableItem(toCheck).check();
        List<IDataChartDescriptor<StubObject, ?>> subset = expectedYDescriptors.stream()
                .filter(d -> d.getClass() == DataChartNumericalDescriptor.class)
                .collect(Collectors.toList());
        assertDescriptors("Checked numerical Y descriptors", subset, SWTBotCustomChartUtils.Y_SERIES_TABLE_INDEX);
        yTable.getTableItem(toCheck).uncheck();
        assertDescriptors("Unchecked numerical Y descriptors", expectedYDescriptors, SWTBotCustomChartUtils.Y_SERIES_TABLE_INDEX);
        assertFalse(chkLogX.isEnabled());
        assertTrue(chkLogY.isEnabled());

        /* Check a duration descriptor */
        toCheck = expectedYDescriptors.stream()
                .filter(d -> d.getClass() == DataChartDurationDescriptor.class)
                .map(d -> d.getLabel())
                .findFirst().get();
        yTable.getTableItem(toCheck).check();
        subset = expectedYDescriptors.stream()
                .filter(d -> d.getClass() == DataChartDurationDescriptor.class)
                .collect(Collectors.toList());
        assertDescriptors("Checked duration Y descriptors", subset, SWTBotCustomChartUtils.Y_SERIES_TABLE_INDEX);
        yTable.getTableItem(toCheck).uncheck();
        assertDescriptors("Unchecked duration Y descriptors", expectedYDescriptors, SWTBotCustomChartUtils.Y_SERIES_TABLE_INDEX);
        assertFalse(chkLogX.isEnabled());
        assertTrue(chkLogY.isEnabled());

        /* Check a timestamp descriptor */
        toCheck = expectedYDescriptors.stream()
                .filter(d -> d.getClass() == DataChartTimestampDescriptor.class)
                .map(d -> d.getLabel())
                .findFirst().get();
        yTable.getTableItem(toCheck).check();
        subset = expectedYDescriptors.stream()
                .filter(d -> d.getClass() == DataChartTimestampDescriptor.class)
                .collect(Collectors.toList());
        assertDescriptors("Checked timestamp Y descriptors", subset, SWTBotCustomChartUtils.Y_SERIES_TABLE_INDEX);
        yTable.getTableItem(toCheck).uncheck();
        assertDescriptors("Unchecked timestamp Y descriptors", expectedYDescriptors, SWTBotCustomChartUtils.Y_SERIES_TABLE_INDEX);
        assertFalse(chkLogX.isEnabled());
        assertTrue(chkLogY.isEnabled());

        /* Actually add series */

        // Add a series with a timestamp descriptor, log scale in Y is not available for timestamps
        SWTBotCustomChartUtils.addSeries(fBot, expectedXDescriptors.get(0).getLabel(), Collections.singleton(toCheck));

        // Only one descriptor is allowed in X, the other one should be filtered
        assertDescriptors("After add X", Collections.singleton(expectedXDescriptors.get(0)), SWTBotCustomChartUtils.X_SERIES_TABLE_INDEX);

        // Only timestamps should be available in Y
        assertDescriptors("After add Y", subset, SWTBotCustomChartUtils.Y_SERIES_TABLE_INDEX);
        assertFalse(chkLogX.isEnabled());
        assertFalse(chkLogY.isEnabled());

        // The selected descriptor should be checked, all others not
        for (IDataChartDescriptor<StubObject, ?> descriptor : subset) {
            String label = descriptor.getLabel();
            if (label.equals(toCheck)) {
                assertTrue(yTable.getTableItem(label).isChecked());
            } else {
                assertFalse(yTable.getTableItem(label).isChecked());
            }
        }
        yTable.getTableItem(toCheck).check();

        // Check that one series is selected
        SWTBotTable selectedTable = fBot.table(SWTBotCustomChartUtils.SERIES_SELECTION_TABLE_INDEX);
        assertEquals("Selected rows after one addition", 1, selectedTable.rowCount());

        // Add all timestamp series
        SWTBotCustomChartUtils.addSeries(fBot, expectedXDescriptors.get(0).getLabel(),
                subset.stream().map(d -> d.getLabel()).collect(Collectors.toSet()));
        assertEquals("Selected rows after all added", subset.size(), selectedTable.rowCount());
        assertFalse(chkLogX.isEnabled());
        assertFalse(chkLogY.isEnabled());

    }

}
