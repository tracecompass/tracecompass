/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.ui.swtbot.tests.latency;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.latency.PatternLatencyTableView;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.junit.Test;

/**
 * Test the pattern latency table
 *
 * @author Jean-Christian Kouame
 */
public class PatternLatencyTableViewTest extends PatternLatencyViewTestBase {

    private static final String COLUMN_HEADER = "Name";
    private static final String SYSTEM_CALL_PREFIX = "sys_";
    private static final String VIEW_ID = PatternLatencyTableView.ID;
    private static final String VIEW_TITLE = "Latency Table";
    private PatternLatencyTableView fLatencyView;
    private AbstractSegmentStoreTableViewer fTable;

    /**
     * Test the latency view data
     */
    @Test
    public void testData() {
        getTable();
        WaitUtils.waitForJobs();
        assertNotNull(fTable);

        SWTBotTable tableBot = new SWTBotTable(fTable.getTableViewer().getTable());
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, SYSTEM_CALL_PREFIX, 0, 3));
        tableBot.header(COLUMN_HEADER).click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, SYSTEM_CALL_PREFIX, 0, 3));
        tableBot.header(COLUMN_HEADER).click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, SYSTEM_CALL_PREFIX, 0, 3));
    }

    /**
     * Get the table associated to the view
     */
    private void getTable() {
        SWTBotView viewBot = fBot.viewById(VIEW_ID);
        final IViewReference viewReference = viewBot.getViewReference();
        IViewPart viewPart = UIThreadRunnable.syncExec(new Result<IViewPart>() {
            @Override
            public IViewPart run() {
                return viewReference.getView(true);
            }
        });
        assertNotNull(viewPart);
        if (!(viewPart instanceof PatternLatencyTableView)) {
            fail("Could not instanciate view");
        }
        fLatencyView = (PatternLatencyTableView) viewPart;
        fTable = fLatencyView.getSegmentStoreViewer();
        assertNotNull(fTable);
    }

    @Override
    protected String getViewId() {
        return VIEW_ID;
    }

    @Override
    protected String getViewTitle() {
        return VIEW_TITLE;
    }
}
