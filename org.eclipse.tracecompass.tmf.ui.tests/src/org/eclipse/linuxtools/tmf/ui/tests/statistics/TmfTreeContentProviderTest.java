/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial design and implementation
 *   Bernd Hufmann - Fixed warnings
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.Messages;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfTreeContentProvider;
import org.junit.Test;

/**
 * TmfTreeContentProvider Test Cases.
 */
public class TmfTreeContentProviderTest {

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    private static final String fTestName = "TreeContentProviderTest";

    private final String fContext = "UnitTest";
    private final String fTypeId1 = "Some type1";
    private final String fTypeId2 = "Some type2";

    private final String fLabel0 = "label1";
    private final String fLabel1 = "label2";
    private final String[] fLabels = new String[] { fLabel0, fLabel1 };

    private final TmfTimestamp fTimestamp1 = new TmfTimestamp(12345, (byte) 2, 5);
    private final TmfTimestamp fTimestamp2 = new TmfTimestamp(12350, (byte) 2, 5);

    private final String fSource = "Source";

    private final TmfEventType fType1 = new TmfEventType(fContext, fTypeId1, TmfEventField.makeRoot(fLabels));
    private final TmfEventType fType2 = new TmfEventType(fContext, fTypeId2, TmfEventField.makeRoot(fLabels));

    private final String fReference = "Some reference";

    private final ITmfEvent fEvent1;
    private final ITmfEvent fEvent2;

    private final TmfEventField fContent1;
    private final TmfEventField fContent2;

    private final TmfStatisticsTree fStatsData;

    private final TmfTreeContentProvider treeProvider;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public TmfTreeContentProviderTest() {
        fContent1 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, "Some content", null);
        fEvent1 = new TmfEvent(null, fTimestamp1, fSource, fType1, fContent1, fReference);

        fContent2 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, "Some other content", null);
        fEvent2 = new TmfEvent(null, fTimestamp2, fSource, fType2, fContent2, fReference);

        fStatsData = new TmfStatisticsTree();

        fStatsData.setTotal(fTestName, true, 2);
        fStatsData.setTypeCount(fTestName, fEvent1.getType().getName(), true, 1);
        fStatsData.setTypeCount(fTestName, fEvent2.getType().getName(), true, 1);

        treeProvider = new TmfTreeContentProvider();
    }

    // ------------------------------------------------------------------------
    // Test methods
    // ------------------------------------------------------------------------

    /**
     * Test getting of children.
     * FIXME this test was quickly adapted when we removed the TmfFixedArray,
     * but it could be rewritten to be much more simple...
     */
    @Test
    public void testGetChildren() {
        Object[] objectArray = treeProvider.getChildren(fStatsData.getOrCreateNode(fTestName, Messages.TmfStatisticsData_EventTypes));
        TmfStatisticsTreeNode[] childrenNode = Arrays.asList(objectArray).toArray(new TmfStatisticsTreeNode[0]);

        String[][] childrenExpected = new String[][] {
                new String[] { fTestName, Messages.TmfStatisticsData_EventTypes, fEvent1.getType().getName() },
                new String[] { fTestName, Messages.TmfStatisticsData_EventTypes, fEvent2.getType().getName() }
        };

        assertEquals("getChildren", childrenExpected.length, childrenNode.length);
        // assertTrue("getChildren", childrenPath.equals(childrenExpected));
        for (TmfStatisticsTreeNode childNode : childrenNode) {
            if (!arrayOfArraysContains(childrenExpected, childNode.getPath())) {
                fail();
            }
        }
    }

    private static boolean arrayOfArraysContains(String[][] arrayOfArrays, String[] array) {
        for (String[] curArray : arrayOfArrays) {
            if (arraysEqual(curArray, array)) {
                return true;
            }
        }
        return false;
    }

    private static boolean arraysEqual(String[] array1, String[] array2) {
        if (array1.length != array2.length) {
            return false;
        }
        for (int i = 0; i < array1.length; i++) {
            if (!array1[i].equals(array2[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Test getting of parent.
     */
    @Test
    public void testGetParent() {
        TmfStatisticsTreeNode parent = (TmfStatisticsTreeNode) treeProvider.getParent(fStatsData.getNode(fTestName));

        assertNotNull("getParent", parent);
        assertTrue("getParent", parent.getPath().equals(fStatsData.getRootNode().getPath()));
    }

    /**
     * Test checking for children.
     */
    @Test
    public void testHasChildren() {
        boolean hasChildren = treeProvider.hasChildren(fStatsData.getRootNode());
        assertTrue("hasChildren", hasChildren);

        hasChildren = treeProvider.hasChildren(fStatsData.getOrCreateNode(fTestName));
        assertTrue("hasChildren", hasChildren);

        hasChildren = treeProvider.hasChildren(fStatsData.getOrCreateNode(fTestName, Messages.TmfStatisticsData_EventTypes));
        assertTrue("hasChildren", hasChildren);

        hasChildren = treeProvider.hasChildren(fStatsData.getOrCreateNode(fTestName, Messages.TmfStatisticsData_EventTypes, fEvent1.getType().getName()));
        assertFalse("hasChildren", hasChildren);
    }

    /**
     * Test getting of elements.
     */
    @Test
    public void testGetElements() {
        Object[] objectElements = treeProvider.getElements(fStatsData.getRootNode());
        TmfStatisticsTreeNode[] nodeElements = Arrays.asList(objectElements).toArray(new TmfStatisticsTreeNode[0]);
        assertEquals("getElements", 1, nodeElements.length);
        assertTrue("getElements", nodeElements[0].getPath()[0].equals(fTestName));
    }
}
