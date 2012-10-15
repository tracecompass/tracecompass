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

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.Messages;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;

/**
 * TmfBaseStatistics Test Cases.
 */
@SuppressWarnings("nls")
public class TmfBaseStatisticsDataTest extends TestCase {

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------
    private       String fTestName = null;

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

    private final TmfEvent fEvent1;
    private final TmfEvent fEvent2;
    private final TmfEvent fEvent3;

    private final TmfEventField fContent1;
    private final TmfEventField fContent2;
    private final TmfEventField fContent3;

    private final TmfStatisticsTree fStatsData;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * @param name
     *            Test name
     */
    public TmfBaseStatisticsDataTest(final String name) {
        super(name);

        fTestName = name;

        fContent1 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, "Some content");
        fEvent1 = new TmfEvent(null, fTimestamp1, fSource, fType1, fContent1, fReference);

        fContent2 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, "Some other content");
        fEvent2 = new TmfEvent(null, fTimestamp2, fSource, fType2, fContent2, fReference);

        fContent3 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, "Some other different content");
        fEvent3 = new TmfEvent(null, fTimestamp3, fSource, fType3, fContent3, fReference);

        fStatsData = new TmfStatisticsTree();

        fStatsData.setTotal(fTestName, true, 3);
        fStatsData.setTypeCount(fTestName, fEvent1.getType().getName(), true, 1);
        fStatsData.setTypeCount(fTestName, fEvent2.getType().getName(), true, 1);
        fStatsData.setTypeCount(fTestName, fEvent3.getType().getName(), true, 1);
    }

    // ------------------------------------------------------------------------
    // GetChildren
    // ------------------------------------------------------------------------

    /**
     * Test getting of children.
     */
    public void testGetChildren() {
        // Getting children of the ROOT
        Collection<TmfStatisticsTreeNode> childrenTreeNode = fStatsData.getChildren(TmfStatisticsTree.ROOT);
        assertEquals("getChildren", 1, childrenTreeNode.size());
        TmfStatisticsTreeNode treeNode = childrenTreeNode.iterator().next();
        assertEquals("getChildren", fTestName, treeNode.getKey());

        // Getting children of the trace
        childrenTreeNode = fStatsData.getChildren(fTestName);
        assertEquals("getChildren", 1, childrenTreeNode.size());
        treeNode = childrenTreeNode.iterator().next();
        assertEquals("getChildren", Messages.TmfStatisticsData_EventTypes, treeNode.getKey());

        Vector<String> keyExpected = new Vector<String>();
        keyExpected.add(fEvent1.getType().getName());
        keyExpected.add(fEvent3.getType().getName());
        // Getting children of a category
        childrenTreeNode = fStatsData.getChildren(treeNode.getPath());
        assertEquals("getChildren", 2, childrenTreeNode.size());

        Iterator<TmfStatisticsTreeNode> iterChild = childrenTreeNode.iterator();
        TmfStatisticsTreeNode temp;
        while (iterChild.hasNext()) {
            temp = iterChild.next();
            assertEquals(0, fStatsData.getChildren(temp.getPath()).size());
            if (keyExpected.contains(temp.getKey())) {
                keyExpected.removeElement(temp.getKey());
            } else {
                fail();
            }
        }

        // Get children of a specific event type
        childrenTreeNode = fStatsData.getChildren(childrenTreeNode.iterator().next().getPath());
        assertEquals("getChildren", 0, childrenTreeNode.size());
    }

    // ------------------------------------------------------------------------
    // GetAllChildren
    // ------------------------------------------------------------------------

    /**
     * Test getting of all children.
     */
    public void testGetAllChildren() {
        // Getting children of the ROOT
        Collection<TmfStatisticsTreeNode> childrenTreeNode = fStatsData.getAllChildren(TmfStatisticsTree.ROOT);
        assertEquals("getChildren", 1, childrenTreeNode.size());
        TmfStatisticsTreeNode treeNode = childrenTreeNode.iterator().next();
        assertEquals("getChildren", fTestName, treeNode.getKey());

        // Getting children of the trace
        childrenTreeNode = fStatsData.getAllChildren(fTestName);
        assertEquals("getChildren", 1, childrenTreeNode.size());
        treeNode = childrenTreeNode.iterator().next();
        assertEquals("getChildren", Messages.TmfStatisticsData_EventTypes, treeNode.getKey());

        Vector<String> keyExpected = new Vector<String>();
        keyExpected.add(fEvent1.getType().getName());
        keyExpected.add(fEvent3.getType().getName());
        /*
         * It should return the eventType even though the number of events
         * equals 0
         */
        fStatsData.get(fTestName, Messages.TmfStatisticsData_EventTypes, fEvent1.getType().getName()).reset();
        // Getting children of a category
        childrenTreeNode = fStatsData.get(treeNode.getPath()).getAllChildren();
        assertEquals("getChildren", 2, childrenTreeNode.size());

        Iterator<TmfStatisticsTreeNode> iterChild = childrenTreeNode.iterator();
        TmfStatisticsTreeNode temp;
        while (iterChild.hasNext()) {
            temp = iterChild.next();
            assertEquals(0, fStatsData.getAllChildren(temp.getPath()).size());
            if (keyExpected.contains(temp.getKey())) {
                keyExpected.removeElement(temp.getKey());
            } else {
                fail();
            }
        }

        // Get children of a specific event type
        childrenTreeNode = fStatsData.getAllChildren(childrenTreeNode.iterator().next().getPath());
        assertEquals("getChildren", 0, childrenTreeNode.size());
    }

    // ------------------------------------------------------------------------
    // RegisterEvent
    // ------------------------------------------------------------------------

    /**
     * Test registering of events.
     */
    public void testRegisterEvent() {
        TmfStatisticsTreeNode trace = fStatsData.get(fTestName);
        assertEquals("registerEvent", 3, trace.getValues().getTotal());

        Collection<TmfStatisticsTreeNode> childrenTreeNode = fStatsData.getChildren(fTestName, Messages.TmfStatisticsData_EventTypes);
        for (TmfStatisticsTreeNode child : childrenTreeNode) {
            if (child.getKey().compareTo(fEvent1.getType().getName()) == 0) {
                assertEquals("registerEvent", 1, child.getValues().getTotal());
            } else if (child.getKey().compareTo(fEvent3.getType().getName()) == 0) {
                assertEquals("registerEvent", 1, child.getValues().getTotal());
            }
        }
    }

    // ------------------------------------------------------------------------
    // Get a node
    // ------------------------------------------------------------------------

    /**
     * Test getter.
     */
    public void testGet() {
        TmfStatisticsTreeNode traceRoot = fStatsData.get(fTestName);
        assertNotNull("get", traceRoot);
        assertEquals("get", 0, traceRoot.getPath()[0].compareTo(fTestName));
        assertEquals("get", 3, traceRoot.getValues().getTotal());
        assertEquals("get", 1, traceRoot.getNbChildren());
    }

    // ------------------------------------------------------------------------
    // GetOrCreate
    // ------------------------------------------------------------------------

    /**
     * Test getting or creating of node entries.
     */
    public void testGetOrCreate() {
        String[] newEventType = new String[] { fTestName, Messages.TmfStatisticsData_EventTypes, "Fancy Type" };
        TmfStatisticsTreeNode newEventTypeNode;

        // newEventType is not in the tree
        newEventTypeNode = fStatsData.get(newEventType);
        assertNull("getOrCreate", newEventTypeNode);

        newEventTypeNode = fStatsData.getOrCreate(newEventType);
        assertNotNull("getOrCreate", newEventTypeNode);
        assertTrue("getOrCreate", newEventTypeNode.getPath().equals(newEventType));

        // newEventType is in the tree
        newEventTypeNode.reset();
        newEventTypeNode = fStatsData.get(newEventType);
        assertNotNull("getOrCreate", newEventTypeNode);

        newEventTypeNode = fStatsData.getOrCreate(newEventType);
        assertNotNull("getOrCreate", newEventTypeNode);
        assertTrue("getOrCreate", newEventTypeNode.getPath().equals(newEventType));
    }

    // ------------------------------------------------------------------------
    // GetParent
    // ------------------------------------------------------------------------

    /**
     * Test getting of parent node.
     */
    public void testGetParent() {
        TmfStatisticsTreeNode parentNode = fStatsData.getParent(TmfStatisticsTree.ROOT);
        assertNull("getParent", parentNode);

        parentNode = fStatsData.getParent("TreeRootNode that should not exist");
        assertNotNull("getParent", parentNode);
        assertEquals("getParent", 0, parentNode.getKey().compareTo(fStatsData.get(TmfStatisticsTree.ROOT).getKey().toString()));

        parentNode = fStatsData.getParent("TreeNode", Messages.TmfStatisticsData_EventTypes, "TreeNode that should not exist");
        assertNull("getParent", parentNode);
        parentNode = fStatsData.getParent(fTestName, Messages.TmfStatisticsData_EventTypes, fEvent1.getType().getName());
        assertNull("getParent", parentNode);

        parentNode = fStatsData.getParent(fTestName);
        assertNotNull("getParent", parentNode);
        assertEquals("getParent", 0, parentNode.getPath().toString().compareTo(TmfStatisticsTree.ROOT.toString()));

        parentNode = fStatsData.getParent(fTestName, Messages.TmfStatisticsData_EventTypes);
        assertNotNull("getParent", parentNode);
        assertEquals("getParent", 0, parentNode.getPath().toString().compareTo(fStatsData.get(fTestName).getPath().toString()));
    }

    // ------------------------------------------------------------------------
    // Reset
    // ------------------------------------------------------------------------

    /**
     * Test reset method
     */
    public void testReset() {
        fStatsData.reset(fTestName, Messages.TmfStatisticsData_EventTypes);

        assertEquals("reset", 0, fStatsData.getChildren(fTestName, Messages.TmfStatisticsData_EventTypes).size());
        assertNull("reset", fStatsData.get(fTestName, Messages.TmfStatisticsData_EventTypes, fType1.getName()));
        assertNull("reset", fStatsData.get(fTestName, Messages.TmfStatisticsData_EventTypes, fType3.getName()));

        fStatsData.reset(fTestName);

        // A root should always have at least one child that is eventType
        assertEquals("reset", 1, fStatsData.getChildren(fTestName).size());
    }
}
