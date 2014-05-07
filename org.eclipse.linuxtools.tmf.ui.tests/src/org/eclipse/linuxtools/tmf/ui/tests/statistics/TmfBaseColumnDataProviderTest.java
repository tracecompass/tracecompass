/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial API and Implementation
 *   Bernd Hufmann - Fixed header and warnings
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.Messages;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfBaseColumnData;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfBaseColumnData.ITmfColumnPercentageProvider;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfBaseColumnDataProvider;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;
import org.junit.Test;

/**
 * TmfBaseColumnDataProvider test cases.
 *
 */
public class TmfBaseColumnDataProviderTest {

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    private static final double DELTA = 1e-15;

    private final static String LEVEL_COLUMN = Messages.TmfStatisticsView_LevelColumn;
    private final static String EVENTS_COUNT_COLUMN = Messages.TmfStatisticsView_NbEventsColumn;

    private TmfBaseColumnDataProvider provider;

    private static final String fTestName = "ColumnDataProviderTest";

    private final String fContext = "UnitTest";

    private final String fTypeId1 = "Some type1";
    private final String fTypeId2 = "Some type2";

    private final String fLabel0 = "label1";
    private final String fLabel1 = "label2";
    private final String fLabel2 = "label3";
    private final String[] fLabels = new String[] { fLabel0, fLabel1, fLabel2 };

    private final TmfTimestamp fTimestamp1 = new TmfTimestamp(12345, (byte) 2, 5);
    private final TmfTimestamp fTimestamp2 = new TmfTimestamp(12350, (byte) 2, 5);
    private final TmfTimestamp fTimestamp3 = new TmfTimestamp(12355, (byte) 2, 5);

    private final String fSource = "Source";

    private final TmfEventType fType1 = new TmfEventType(fContext, fTypeId1, TmfEventField.makeRoot(fLabels));
    private final TmfEventType fType2 = new TmfEventType(fContext, fTypeId1, TmfEventField.makeRoot(fLabels));
    private final TmfEventType fType3 = new TmfEventType(fContext, fTypeId2, TmfEventField.makeRoot(fLabels));

    private final String fReference = "Some reference";

    private final ITmfEvent fEvent1;
    private final ITmfEvent fEvent2;
    private final ITmfEvent fEvent3;

    private final TmfEventField fContent1;
    private final TmfEventField fContent2;
    private final TmfEventField fContent3;

    private final TmfStatisticsTree fStatsData;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public TmfBaseColumnDataProviderTest() {
        fContent1 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, "Some content", null);
        fEvent1 = new TmfEvent(null, fTimestamp1, fSource, fType1, fContent1, fReference);

        fContent2 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, "Some other content", null);
        fEvent2 = new TmfEvent(null, fTimestamp2, fSource, fType2, fContent2, fReference);

        fContent3 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, "Some other different content", null);
        fEvent3 = new TmfEvent(null, fTimestamp3, fSource, fType3, fContent3, fReference);

        fStatsData = new TmfStatisticsTree();

        fStatsData.getOrCreateNode(fTestName, Messages.TmfStatisticsData_EventTypes);

        fStatsData.setTotal(fTestName, true, 3);
        fStatsData.setTypeCount(fTestName, fEvent1.getType().getName(), true, 1);
        fStatsData.setTypeCount(fTestName, fEvent2.getType().getName(), true, 1);
        fStatsData.setTypeCount(fTestName, fEvent3.getType().getName(), true, 1);

        provider = new TmfBaseColumnDataProvider();
    }

    // ------------------------------------------------------------------------
    // Get Column Data
    // ------------------------------------------------------------------------

    /**
     * Method with test cases.
     */
    @Test
    public void testGetColumnData() {
        List<TmfBaseColumnData> columnsData = provider.getColumnData();
        assertNotNull("getColumnData", columnsData);
        assertEquals("getColumnData", 4, columnsData.size());

        TmfStatisticsTreeNode parentNode = fStatsData.getNode(fTestName);
        TmfStatisticsTreeNode treeNode1 = fStatsData.getNode(fTestName, Messages.TmfStatisticsData_EventTypes, fEvent1.getType().getName());
        TmfStatisticsTreeNode treeNode2 = fStatsData.getNode(fTestName, Messages.TmfStatisticsData_EventTypes, fEvent3.getType().getName());
        ViewerComparator vComp = null;
        for (TmfBaseColumnData columnData : columnsData) {
            assertNotNull("getColumnData", columnData);
            assertNotNull("getColumnData", columnData.getHeader());
            assertNotNull("getColumnData", columnData.getTooltip());

            // Testing labelProvider
            ColumnLabelProvider labelProvider = columnData.getLabelProvider();
            if (columnData.getHeader().compareTo(LEVEL_COLUMN) == 0) {
                assertEquals("getColumnData", 0, labelProvider.getText(treeNode1).compareTo(treeNode1.getName()));
            } else if (columnData.getHeader().compareTo(EVENTS_COUNT_COLUMN) == 0) {
                // might not work because of machine local number format
                assertEquals("getColumnData", "1", labelProvider.getText(treeNode1));
            }

            // Testing comparator
            vComp = columnData.getComparator();
            if (columnData.getHeader().compareTo(LEVEL_COLUMN) == 0) {
                assertTrue("getColumnData", vComp.compare(null, treeNode1, treeNode2) < 0);
                assertTrue("getColumnData", vComp.compare(null, treeNode2, treeNode1) > 0);
                assertTrue("getColumnData", vComp.compare(null, treeNode1, treeNode1) == 0);
            } else if (columnData.getHeader().compareTo(EVENTS_COUNT_COLUMN) == 0) {
                assertTrue("getColumnData", vComp.compare(null, treeNode1, treeNode2) == 0);
                assertTrue("getColumnData", vComp.compare(null, treeNode2, treeNode1) == 0);
                assertTrue("getColumnData", vComp.compare(null, treeNode1, treeNode1) == 0);
            }

            // Testing percentage provider
            ITmfColumnPercentageProvider percentProvider = columnData.getPercentageProvider();
            if (columnData.getHeader().compareTo(LEVEL_COLUMN) == 0) {
                assertNull("getColumnData", percentProvider);
            } else if (columnData.getHeader().compareTo(EVENTS_COUNT_COLUMN) == 0) {
                double percentage = (double) treeNode1.getValues().getTotal() / parentNode.getValues().getTotal();
                assertEquals("getColumnData", percentage, percentProvider.getPercentage(treeNode1), DELTA);
            }
        }
    }
}
