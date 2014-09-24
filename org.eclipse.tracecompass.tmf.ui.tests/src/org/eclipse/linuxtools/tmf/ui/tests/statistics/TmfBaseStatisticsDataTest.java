/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.Messages;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;
import org.junit.Test;

/**
 * TmfBaseStatistics Test Cases.
 */
public class TmfBaseStatisticsDataTest {

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    private static final String fTestName = "StatisticsDataTest";

    private final String fContext = "UnitTest";
    private final String fTypeId1 = "Some type1";
    private final String fTypeId2 = "Some type2";

    private final String   fLabel0 = "label1";
    private final String   fLabel1 = "label2";
    private final String   fLabel2 = "label3";
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

    private final TmfStatisticsTree fStatsTree;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public TmfBaseStatisticsDataTest() {
        fContent1 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, "Some content", null);
        fEvent1 = new TmfEvent(null, fTimestamp1, fSource, fType1, fContent1, fReference);

        fContent2 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, "Some other content", null);
        fEvent2 = new TmfEvent(null, fTimestamp2, fSource, fType2, fContent2, fReference);

        fContent3 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, "Some other different content", null);
        fEvent3 = new TmfEvent(null, fTimestamp3, fSource, fType3, fContent3, fReference);

        fStatsTree = new TmfStatisticsTree();

        fStatsTree.setTotal(fTestName, true, 3);
        fStatsTree.setTypeCount(fTestName, fEvent1.getType().getName(), true, 1);
        fStatsTree.setTypeCount(fTestName, fEvent2.getType().getName(), true, 1);
        fStatsTree.setTypeCount(fTestName, fEvent3.getType().getName(), true, 1);
    }

    // ------------------------------------------------------------------------
    // Test methods
    // ------------------------------------------------------------------------

    /**
     * Test getting of children.
     */
    @Test
    public void testGetChildren() {
        // Getting children of the ROOT
        Collection<TmfStatisticsTreeNode> childrenTreeNode = fStatsTree.getRootNode().getChildren();
        assertEquals("getChildren", 1, childrenTreeNode.size());
        TmfStatisticsTreeNode treeNode = childrenTreeNode.iterator().next();
        assertEquals("getChildren", fTestName, treeNode.getName());

        // Getting children of the trace
        childrenTreeNode = fStatsTree.getNode(fTestName).getChildren();
        assertEquals("getChildren", 1, childrenTreeNode.size());
        treeNode = childrenTreeNode.iterator().next();
        assertEquals("getChildren", Messages.TmfStatisticsData_EventTypes, treeNode.getName());

        Vector<String> keyExpected = new Vector<>();
        keyExpected.add(fEvent1.getType().getName());
        keyExpected.add(fEvent3.getType().getName());
        // Getting children of a category
        childrenTreeNode = treeNode.getChildren();
        assertEquals("getChildren", 2, childrenTreeNode.size());

        Iterator<TmfStatisticsTreeNode> iterChild = childrenTreeNode.iterator();
        TmfStatisticsTreeNode temp;
        while (iterChild.hasNext()) {
            temp = iterChild.next();
            assertEquals(0, temp.getChildren().size());
            if (keyExpected.contains(temp.getName())) {
                keyExpected.removeElement(temp.getName());
            } else {
                fail();
            }
        }

        // Get children of a specific event type
        childrenTreeNode = childrenTreeNode.iterator().next().getChildren();
        assertEquals("getChildren", 0, childrenTreeNode.size());
    }

    /**
     * Test registering of events.
     */
    @Test
    public void testRegisterEvent() {
        TmfStatisticsTreeNode trace = fStatsTree.getNode(fTestName);
        assertEquals("registerEvent", 3, trace.getValues().getTotal());

        Collection<TmfStatisticsTreeNode> childrenTreeNode = fStatsTree.getNode(fTestName, Messages.TmfStatisticsData_EventTypes).getChildren();
        for (TmfStatisticsTreeNode child : childrenTreeNode) {
            if (child.getName().compareTo(fEvent1.getType().getName()) == 0) {
                assertEquals("registerEvent", 1, child.getValues().getTotal());
            } else if (child.getName().compareTo(fEvent3.getType().getName()) == 0) {
                assertEquals("registerEvent", 1, child.getValues().getTotal());
            }
        }
    }

    /**
     * Test getter.
     */
    @Test
    public void testGet() {
        TmfStatisticsTreeNode traceRoot = fStatsTree.getNode(fTestName);
        assertNotNull("get", traceRoot);
        assertEquals("get", 0, traceRoot.getPath()[0].compareTo(fTestName));
        assertEquals("get", 3, traceRoot.getValues().getTotal());
        assertEquals("get", 1, traceRoot.getNbChildren());
    }

    /**
     * Test getting or creating of node entries.
     */
    @Test
    public void testGetOrCreate() {
        String[] newEventType = new String[] { fTestName, Messages.TmfStatisticsData_EventTypes, "Fancy Type" };
        TmfStatisticsTreeNode newEventTypeNode;

        // newEventType is not in the tree
        newEventTypeNode = fStatsTree.getNode(newEventType);
        assertNull(newEventTypeNode);

        newEventTypeNode = fStatsTree.getOrCreateNode(newEventType);
        assertNotNull(newEventTypeNode);
        assertTrue(Arrays.equals(newEventType, newEventTypeNode.getPath()));

        // newEventType is in the tree
        newEventTypeNode.reset();
        newEventTypeNode = fStatsTree.getNode(newEventType);
        assertNotNull(newEventTypeNode);

        newEventTypeNode = fStatsTree.getOrCreateNode(newEventType);
        assertNotNull(newEventTypeNode);
        assertTrue(Arrays.equals(newEventType, newEventTypeNode.getPath()));
    }

    /**
     * Test getting of parent node.
     */
    @Test
    public void testGetParent() {
        TmfStatisticsTreeNode parentNode = fStatsTree.getRootNode().getParent();
        assertNull(parentNode);

        parentNode = fStatsTree.getNode(fTestName).getParent();
        assertNotNull(parentNode);
        assertEquals(parentNode.getPath().toString(), fStatsTree.getRootNode().getPath().toString());

        parentNode = fStatsTree.getNode(fTestName, Messages.TmfStatisticsData_EventTypes).getParent();
        assertNotNull(parentNode);
        assertEquals(parentNode.getPath().toString(), fStatsTree.getNode(fTestName).getPath().toString());
    }

    /**
     * Test reset method
     */
    @Test
    public void testReset() {
        fStatsTree.getNode(fTestName, Messages.TmfStatisticsData_EventTypes).reset();

        assertEquals(0, fStatsTree.getNode(fTestName, Messages.TmfStatisticsData_EventTypes).getChildren().size());
        assertNull(fStatsTree.getNode(fTestName, Messages.TmfStatisticsData_EventTypes, fType1.getName()));
        assertNull(fStatsTree.getNode(fTestName, Messages.TmfStatisticsData_EventTypes, fType3.getName()));

        fStatsTree.getNode(fTestName).reset();
        assertEquals(0, fStatsTree.getNode(fTestName).getChildren().size());
    }
}
