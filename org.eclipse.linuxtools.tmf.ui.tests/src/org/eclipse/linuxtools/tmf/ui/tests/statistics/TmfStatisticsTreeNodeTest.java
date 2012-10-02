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
import org.eclipse.linuxtools.tmf.core.util.TmfFixedArray;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.ITmfExtraEventInfo;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.AbsTmfStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.Messages;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfBaseStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;

/**
 * TmfStatisticsTreeNode Test Cases.
 */
@SuppressWarnings("nls")
public class TmfStatisticsTreeNodeTest extends TestCase {

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------
    private       String   fTestName = null;


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
    private final TmfEventType fType2 = new TmfEventType(fContext, fTypeId2, TmfEventField.makeRoot(fLabels));

    private final String fReference = "Some reference";

    private final TmfEvent fEvent1;
    private final TmfEvent fEvent2;
    private final TmfEvent fEvent3;

    private final TmfEventField fContent1;
    private final TmfEventField fContent2;
    private final TmfEventField fContent3;

    private final TmfBaseStatisticsTree fStatsData;

    private final ITmfExtraEventInfo fExtraInfo;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * @param name
     *            Test name
     */
    public TmfStatisticsTreeNodeTest(final String name) {
        super(name);

        fTestName = name;

        fContent1 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, "Some content");
        fEvent1 = new TmfEvent(null, fTimestamp1, fSource, fType1, fContent1, fReference);

        fContent2 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, "Some other content");
        fEvent2 = new TmfEvent(null, fTimestamp2, fSource, fType1, fContent2, fReference);

        fContent3 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, "Some other different content");
        fEvent3 = new TmfEvent(null, fTimestamp3, fSource, fType2, fContent3, fReference);

        fStatsData = new TmfBaseStatisticsTree();
        fExtraInfo = new ITmfExtraEventInfo() {
            @Override
            public String getTraceName() {
                return fTestName;
            }
        };
        fStatsData.registerEvent(fEvent1, fExtraInfo);
        fStatsData.registerEvent(fEvent1, fExtraInfo, 2);
        fStatsData.registerEvent(fEvent2, fExtraInfo);
        fStatsData.registerEvent(fEvent2, fExtraInfo, 3);
        fStatsData.registerEvent(fEvent3, fExtraInfo);
        fStatsData.registerEvent(fEvent3, fExtraInfo, 4);

        // Registers some events in time range
        fStatsData.registerEventInTimeRange(fEvent1, fExtraInfo);
        fStatsData.registerEventInTimeRange(fEvent1, fExtraInfo, 3);
        fStatsData.registerEventInTimeRange(fEvent2, fExtraInfo);
        fStatsData.registerEventInTimeRange(fEvent2, fExtraInfo, 4);
        fStatsData.registerEventInTimeRange(fEvent3, fExtraInfo);
        fStatsData.registerEventInTimeRange(fEvent3, fExtraInfo, 5);
    }

    // ------------------------------------------------------------------------
    // ContainsChild
    // ------------------------------------------------------------------------

    /**
     * Test checking for child.
     */
    public void testContainsChild() {
        TmfStatisticsTreeNode rootNode  = fStatsData.get(AbsTmfStatisticsTree.ROOT);
        TmfStatisticsTreeNode traceNode = fStatsData.get(new TmfFixedArray<String>(fTestName));
        // Creates a category from the key already created
        TmfStatisticsTreeNode catNode = traceNode.getChildren().iterator().next();

        assertTrue("containsChild", rootNode.containsChild(fTestName));
        assertFalse("containsChild", rootNode.containsChild(catNode.getKey()));
        assertFalse("containsChild", rootNode.containsChild(null));

        assertTrue("containsChild", traceNode.containsChild(catNode.getKey()));
        assertFalse("containsChild", traceNode.containsChild(fType1.getName()));
        assertFalse("containsChild", traceNode.containsChild(null));

        assertTrue("containsChild", catNode.containsChild(fType1.getName()));
        assertTrue("containsChild", catNode.containsChild(fType2.getName()));
        assertFalse("containsChild", catNode.containsChild(null));
    }

    // ------------------------------------------------------------------------
    // GetChildren
    // ------------------------------------------------------------------------

    /**
     * Test getting of children.
     */
    public void testGetChildren() {
        // Getting children of the ROOT
        Collection<TmfStatisticsTreeNode> childrenTreeNode = fStatsData.get(AbsTmfStatisticsTree.ROOT).getChildren();
        assertEquals("getChildren", 1, childrenTreeNode.size());
        TmfStatisticsTreeNode treeNode = childrenTreeNode.iterator().next();
        assertEquals("getChildren", fTestName, treeNode.getKey());

        // Getting children of the trace
        childrenTreeNode = fStatsData.get(new TmfFixedArray<String>(fTestName)).getChildren();
        assertEquals("getChildren", 1, childrenTreeNode.size());
        treeNode = childrenTreeNode.iterator().next();
        assertEquals("getChildren", Messages.TmfStatisticsData_EventTypes, treeNode.getKey());

        Vector<String> keyExpected = new Vector<String>();
        keyExpected.add(fType1.getName());
        keyExpected.add(fType2.getName());
        // Getting children of a category
        childrenTreeNode = treeNode.getChildren();
        assertEquals("getChildren", 2, childrenTreeNode.size());

        Iterator<TmfStatisticsTreeNode> iterChild = childrenTreeNode.iterator();
        TmfStatisticsTreeNode temp;
        while (iterChild.hasNext()) {
            temp = iterChild.next();
            if (keyExpected.contains(temp.getKey())) {
                keyExpected.removeElement(temp.getKey());
            } else {
                fail();
            }
        }

        // Get children of a specific event type
        childrenTreeNode = fStatsData.get(childrenTreeNode.iterator().next().getPath()).getChildren();
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
        Collection<TmfStatisticsTreeNode> childrenTreeNode = fStatsData.get(AbsTmfStatisticsTree.ROOT).getAllChildren();
        assertEquals("getChildren", 1, childrenTreeNode.size());
        TmfStatisticsTreeNode treeNode = childrenTreeNode.iterator().next();
        assertEquals("getChildren", fTestName, treeNode.getKey());

        // Getting children of the trace
        childrenTreeNode = fStatsData.get(new TmfFixedArray<String>(fTestName)).getAllChildren();
        assertEquals("getChildren", 1, childrenTreeNode.size());
        treeNode = childrenTreeNode.iterator().next();
        assertEquals("getChildren", Messages.TmfStatisticsData_EventTypes, treeNode.getKey());

        Vector<String> keyExpected = new Vector<String>();
        keyExpected.add(fType1.getName());
        keyExpected.add(fType2.getName());
        /*
         * It should return the eventType even though the number of events
         * equals 0
         */
        fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fType1.getName())).reset();
        // Getting children of a category
        childrenTreeNode = treeNode.getAllChildren();
        assertEquals("getChildren", 2, childrenTreeNode.size());

        Iterator<TmfStatisticsTreeNode> iterChild = childrenTreeNode.iterator();
        TmfStatisticsTreeNode temp;
        while (iterChild.hasNext()) {
            temp = iterChild.next();
            if (keyExpected.contains(temp.getKey())) {
                keyExpected.removeElement(temp.getKey());
            } else {
                fail();
            }
        }

        // Get children of a specific event type
        childrenTreeNode = fStatsData.get(childrenTreeNode.iterator().next().getPath()).getAllChildren();
        assertEquals("getChildren", 0, childrenTreeNode.size());
    }

    // ------------------------------------------------------------------------
    // GetNbChildren
    // ------------------------------------------------------------------------

    /**
     * Test getting of number of children.
     */
    public void testGetNbChildren() {
        TmfStatisticsTreeNode rootNode    = fStatsData.get(AbsTmfStatisticsTree.ROOT);
        TmfStatisticsTreeNode traceNode   = fStatsData.get(new TmfFixedArray<String>(fTestName));
        TmfStatisticsTreeNode catNode     = traceNode.getChildren().iterator().next();
        TmfStatisticsTreeNode elementNode = fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fType1.getName()));

        assertEquals("getNbChildren", 1, rootNode.getNbChildren());
        assertEquals("getNbChildren", 1, traceNode.getNbChildren());
        assertEquals("getNbChildren", 2, catNode.getNbChildren());
        assertEquals("getNbChildren", 0, elementNode.getNbChildren());
    }

    // ------------------------------------------------------------------------
    // HasChildren
    // ------------------------------------------------------------------------

    /**
     * Test checking for children.
     */
    public void testHasChildren() {
        TmfStatisticsTreeNode rootNode    = fStatsData.get(AbsTmfStatisticsTree.ROOT);
        TmfStatisticsTreeNode traceNode   = fStatsData.get(new TmfFixedArray<String>(fTestName));
        TmfStatisticsTreeNode catNode     = traceNode.getChildren().iterator().next();
        TmfStatisticsTreeNode elementNode = fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fType1.getName()));

        assertTrue("hasChildren", rootNode.hasChildren());
        assertTrue("hasChildren", traceNode.hasChildren());
        assertTrue("hasChildren", catNode.hasChildren());
        assertFalse("hasChildren", elementNode.hasChildren());
    }

    // ------------------------------------------------------------------------
    // GetParent
    // ------------------------------------------------------------------------

    /**
     * Test getting of parent.
     */
    public void testGetParent() {
        TmfStatisticsTreeNode rootNode = fStatsData.get(AbsTmfStatisticsTree.ROOT);
        TmfStatisticsTreeNode parentNode = rootNode.getParent();
        assertNull("getParent", parentNode);

        TmfStatisticsTreeNode newTraceNode = new TmfStatisticsTreeNode(new TmfFixedArray<String>("newly created trace node"), fStatsData);
        parentNode = newTraceNode.getParent();
        assertNotNull("getParent", parentNode);
        assertEquals("getParent", 0, parentNode.getKey().compareTo(fStatsData.get(AbsTmfStatisticsTree.ROOT).getKey().toString()));

        TmfStatisticsTreeNode traceNode = fStatsData.get(new TmfFixedArray<String>(fTestName));
        parentNode = traceNode.getParent();
        assertNotNull("getParent", parentNode);
        assertEquals("getParent", 0, parentNode.getPath().toString().compareTo(AbsTmfStatisticsTree.ROOT.toString()));

        TmfStatisticsTreeNode newNode = new TmfStatisticsTreeNode(new TmfFixedArray<String>("TreeNode", Messages.TmfStatisticsData_EventTypes, "TreeNode that should not exist"), fStatsData);
        parentNode = newNode.getParent();
        assertNull("getParent", parentNode);

        TmfStatisticsTreeNode elementNode = fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fType1.getName()));
        parentNode = elementNode.getParent();
        assertNull("getParent", parentNode);

        TmfStatisticsTreeNode catNode = traceNode.getChildren().iterator().next();
        parentNode = catNode.getParent();
        assertNotNull("getParent", parentNode);
        assertEquals("getParent", 0, parentNode.getPath().toString().compareTo(fStatsData.get(new TmfFixedArray<String>(fTestName)).getPath().toString()));

        parentNode = elementNode.getParent();
        assertNotNull("getParent", parentNode);
        assertTrue("getParent", parentNode.getPath().equals(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes)));
    }

    // ------------------------------------------------------------------------
    // GetKey
    // ------------------------------------------------------------------------

    /**
     * Test getting of key.
     */
    public void testGetKey() {
        TmfStatisticsTreeNode rootNode    = fStatsData.get(AbsTmfStatisticsTree.ROOT);
        TmfStatisticsTreeNode traceNode   = fStatsData.get(new TmfFixedArray<String>(fTestName));
        TmfStatisticsTreeNode catNode     = traceNode.getChildren().iterator().next();
        TmfStatisticsTreeNode elementNode = fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fType1.getName()));

        assertEquals("getKey", 0, rootNode.getKey().compareTo(AbsTmfStatisticsTree.ROOT.get(0)));
        assertEquals("getKey", 0, traceNode.getKey().compareTo(fTestName));
        assertEquals("getKey", 0, catNode.getKey().compareTo(Messages.TmfStatisticsData_EventTypes));
        assertEquals("getKey", 0, elementNode.getKey().compareTo(fType1.getName()));
    }

    // ------------------------------------------------------------------------
    // GetPath
    // ------------------------------------------------------------------------

    /**
     * Test getting of path to node.
     */
    public void testGetPath() {
        TmfStatisticsTreeNode rootNode    = fStatsData.get(AbsTmfStatisticsTree.ROOT);
        TmfStatisticsTreeNode traceNode   = fStatsData.get(new TmfFixedArray<String>(fTestName));
        TmfStatisticsTreeNode catNode     = traceNode.getChildren().iterator().next();
        TmfStatisticsTreeNode elementNode = fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fType1.getName()));

        assertTrue("getPath", rootNode.getPath().equals(AbsTmfStatisticsTree.ROOT));
        assertTrue("getPath", traceNode.getPath().equals(new TmfFixedArray<String>(fTestName)));
        assertTrue("getPath", catNode.getPath().equals(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes)));
        assertTrue("getPath", elementNode.getPath().equals(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fType1.getName())));
    }

    // ------------------------------------------------------------------------
    // GetValue
    // ------------------------------------------------------------------------

    /**
     * Test getting statistic value.
     */
    public void testGetValue() {
        TmfStatisticsTreeNode rootNode     = fStatsData.get(AbsTmfStatisticsTree.ROOT);
        TmfStatisticsTreeNode traceNode    = fStatsData.get(new TmfFixedArray<String>(fTestName));
        TmfStatisticsTreeNode catNode      = traceNode.getChildren().iterator().next();
        TmfStatisticsTreeNode elementNode1 = fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fType1.getName()));
        TmfStatisticsTreeNode elementNode3 = fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fType2.getName()));

        assertEquals("getValue", 0, rootNode.getValue().getTotal());
        assertEquals("getValue", 12, traceNode.getValue().getTotal());
        assertEquals("getValue", 0, catNode.getValue().getTotal());
        assertEquals("getValue", 7, elementNode1.getValue().getTotal());
        assertEquals("getValue", 5, elementNode3.getValue().getTotal());

        assertEquals("getValue", 0, rootNode.getValue().getPartial());
        assertEquals("getValue", 15, traceNode.getValue().getPartial());
        assertEquals("getValue", 0, catNode.getValue().getPartial());
        assertEquals("getValue", 9, elementNode1.getValue().getPartial());
        assertEquals("getValue", 6, elementNode3.getValue().getPartial());
    }

    // ------------------------------------------------------------------------
    // Reset
    // ------------------------------------------------------------------------

    /**
     * Test reset of tree.
     */
    public void testReset() {
        TmfStatisticsTreeNode rootNode    = fStatsData.get(AbsTmfStatisticsTree.ROOT);
        TmfStatisticsTreeNode traceNode   = fStatsData.get(new TmfFixedArray<String>(fTestName));
        TmfStatisticsTreeNode catNode     = traceNode.getChildren().iterator().next();
        TmfStatisticsTreeNode elementNode = fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fType1.getName()));

        elementNode.reset();
        assertEquals("reset", 0, elementNode.getValue().getTotal());
        assertEquals("reset", 0, elementNode.getValue().getPartial());

        catNode.reset();
        assertEquals("reset", 0, catNode.getValue().getTotal());
        assertEquals("reset", 0, catNode.getValue().getPartial());
        assertEquals("reset", 0, catNode.getNbChildren());
        assertNull("reset", fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fType1.getName())));

        traceNode.reset();
        assertEquals("reset", 0, traceNode.getValue().getTotal());
        assertEquals("reset", 0, traceNode.getValue().getPartial());
        // A trace always have at least one child that is eventType
        assertEquals("reset", 1, traceNode.getNbChildren());

        rootNode.reset();
        assertEquals("reset", 0, rootNode.getValue().getTotal());
        assertEquals("reset", 0, rootNode.getValue().getPartial());
        assertEquals("reset", 1, rootNode.getNbChildren());
    }

    /**
     * Test reset global value of the node in the tree. It should only clear
     * the global value without removing any node from the tree.
     */
    public void testResetGlobalValue() {
        TmfStatisticsTreeNode rootNode    = fStatsData.get(AbsTmfStatisticsTree.ROOT);
        TmfStatisticsTreeNode traceNode   = fStatsData.get(new TmfFixedArray<String>(fTestName));
        TmfStatisticsTreeNode catNode     = traceNode.getChildren().iterator().next();
        TmfStatisticsTreeNode eventTypeNode1 = fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fType1.getName()));
        TmfStatisticsTreeNode eventTypeNode2 = fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fType2.getName()));

        rootNode.resetGlobalValue();

        assertEquals(0, rootNode.getValue().getTotal());
        assertEquals(0, traceNode.getValue().getTotal());
        assertEquals(0, catNode.getValue().getTotal());
        assertEquals(0, eventTypeNode1.getValue().getTotal());
        assertEquals(0, eventTypeNode2.getValue().getTotal());

        // Checks the state of the statistics tree
        Collection<TmfStatisticsTreeNode> rootChildren =  rootNode.getAllChildren();
        assertEquals(1, rootChildren.size());
        assertTrue(rootChildren.contains(traceNode));

        Collection<TmfStatisticsTreeNode> traceChildren =  traceNode.getAllChildren();
        assertEquals(1, traceChildren.size());
        assertTrue(traceChildren.contains(catNode));

        Collection<TmfStatisticsTreeNode> catChildren =  catNode.getAllChildren();
        assertEquals(2, catChildren.size());
        assertTrue(catChildren.contains(eventTypeNode1));
        assertTrue(catChildren.contains(eventTypeNode2));
    }

    /**
     * Test reset time range value of the node in the tree. It should only clear
     * the time range value without removing any node from the tree.
     */
    public void testResetTimeRangeValue() {
        TmfStatisticsTreeNode rootNode    = fStatsData.get(AbsTmfStatisticsTree.ROOT);
        TmfStatisticsTreeNode traceNode   = fStatsData.get(new TmfFixedArray<String>(fTestName));
        TmfStatisticsTreeNode catNode     = traceNode.getChildren().iterator().next();
        TmfStatisticsTreeNode eventTypeNode1 = fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fType1.getName()));
        TmfStatisticsTreeNode eventTypeNode2 = fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fType2.getName()));

        rootNode.resetTimeRangeValue();

        assertEquals(0, rootNode.getValue().getPartial());
        assertEquals(0, traceNode.getValue().getPartial());
        assertEquals(0, catNode.getValue().getPartial());
        assertEquals(0, eventTypeNode1.getValue().getPartial());
        assertEquals(0, eventTypeNode2.getValue().getPartial());

        // Checks the state of the statistics tree
        Collection<TmfStatisticsTreeNode> rootChildren =  rootNode.getAllChildren();
        assertEquals(1, rootChildren.size());
        assertTrue(rootChildren.contains(traceNode));

        Collection<TmfStatisticsTreeNode> traceChildren =  traceNode.getAllChildren();
        assertEquals(1, traceChildren.size());
        assertTrue(traceChildren.contains(catNode));

        Collection<TmfStatisticsTreeNode> catChildren =  catNode.getAllChildren();
        assertEquals(2, catChildren.size());
        assertTrue(catChildren.contains(eventTypeNode1));
        assertTrue(catChildren.contains(eventTypeNode2));
    }
}
