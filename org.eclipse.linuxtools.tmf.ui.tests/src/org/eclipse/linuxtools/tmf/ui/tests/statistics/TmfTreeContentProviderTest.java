/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial design and implementation
 *   Bernd Hufmann - Fixed warnings
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.statistics;

import java.util.Arrays;

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.Messages;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfTreeContentProvider;

/**
 * TmfTreeContentProvider Test Cases.
 */
@SuppressWarnings("nls")
public class TmfTreeContentProviderTest extends TestCase {

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    private String fTestName = null;

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

    private final TmfEvent fEvent1;
    private final TmfEvent fEvent2;

    private final TmfEventField fContent1;
    private final TmfEventField fContent2;

    private final TmfStatisticsTree fStatsData;

    private final TmfTreeContentProvider treeProvider;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * @param name
     *            of the test
     */
    public TmfTreeContentProviderTest(final String name) {
        super(name);

        fTestName = name;

        fContent1 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, "Some content");
        fEvent1 = new TmfEvent(null, fTimestamp1, fSource, fType1, fContent1, fReference);

        fContent2 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, "Some other content");
        fEvent2 = new TmfEvent(null, fTimestamp2, fSource, fType2, fContent2, fReference);

        fStatsData = new TmfStatisticsTree();

        fStatsData.setTotal(fTestName, true, 2);
        fStatsData.setTypeCount(fTestName, fEvent1.getType().getName(), true, 1);
        fStatsData.setTypeCount(fTestName, fEvent2.getType().getName(), true, 1);

        treeProvider = new TmfTreeContentProvider();
    }

    // ------------------------------------------------------------------------
    // GetChildren
    // ------------------------------------------------------------------------

    /**
     * Test getting of children.
     * FIXME this test was quickly adapted when we removed the TmfFixedArray,
     * but it could be rewritten to be much more simple...
     */
    public void testGetChildren() {
        Object[] objectArray = treeProvider.getChildren(fStatsData.getOrCreate(fTestName, Messages.TmfStatisticsData_EventTypes));
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

    // ------------------------------------------------------------------------
    // GetParent
    // ------------------------------------------------------------------------

    /**
     * Test getting of parent.
     */
    public void testGetParent() {
        TmfStatisticsTreeNode parent = (TmfStatisticsTreeNode) treeProvider.getParent(fStatsData.get(fTestName));

        assertNotNull("getParent", parent);
        assertTrue("getParent", parent.getPath().equals(TmfStatisticsTree.ROOT));
    }

    // ------------------------------------------------------------------------
    // HasChildren
    // ------------------------------------------------------------------------
    /**
     * Test checking for children.
     */
    public void testHasChildren() {
        Boolean hasChildren = treeProvider.hasChildren(fStatsData.getOrCreate(TmfStatisticsTree.ROOT));
        assertTrue("hasChildren", hasChildren);

        hasChildren = treeProvider.hasChildren(fStatsData.getOrCreate(fTestName));
        assertTrue("hasChildren", hasChildren);

        hasChildren = treeProvider.hasChildren(fStatsData.getOrCreate(fTestName, Messages.TmfStatisticsData_EventTypes));
        assertTrue("hasChildren", hasChildren);

        hasChildren = treeProvider.hasChildren(fStatsData.getOrCreate(fTestName, Messages.TmfStatisticsData_EventTypes, fEvent1.getType().getName()));
        assertFalse("hasChildren", hasChildren);
    }

    // ------------------------------------------------------------------------
    // GetElements
    // ------------------------------------------------------------------------

    /**
     * Test getting of elements.
     */
    public void testGetElements() {
        Object[] objectElements = treeProvider.getElements(fStatsData.get(TmfStatisticsTree.ROOT));
        TmfStatisticsTreeNode[] nodeElements = Arrays.asList(objectElements).toArray(new TmfStatisticsTreeNode[0]);
        assertEquals("getElements", 1, nodeElements.length);
        assertTrue("getElements", nodeElements[0].getPath()[0].equals(fTestName));
    }
}
