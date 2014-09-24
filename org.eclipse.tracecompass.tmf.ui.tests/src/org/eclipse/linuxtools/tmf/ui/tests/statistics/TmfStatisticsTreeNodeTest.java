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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.Messages;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;
import org.junit.Test;

/**
 * TmfStatisticsTreeNode Test Cases.
 */
public class TmfStatisticsTreeNodeTest {

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    private final String fTypeId1 = "Some type1";
    private final String fTypeId2 = "Some type2";
    private final String fTypeId3 = "Some type3";

    private final TmfStatisticsTree fStatsTree;

    private static final String fTestName = "StatisticsTreeNodeTest";

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public TmfStatisticsTreeNodeTest() {
        fStatsTree = new TmfStatisticsTree();

        /* Enter some global values */
        fStatsTree.setTotal(fTestName, true, 18);
        fStatsTree.setTypeCount(fTestName, fTypeId1, true, 5);
        fStatsTree.setTypeCount(fTestName, fTypeId2, true, 6);
        fStatsTree.setTypeCount(fTestName, fTypeId3, true, 7);

        /* Enter some time range values */
        fStatsTree.setTotal(fTestName, false, 9);
        fStatsTree.setTypeCount(fTestName, fTypeId1, false, 2);
        fStatsTree.setTypeCount(fTestName, fTypeId2, false, 3);
        fStatsTree.setTypeCount(fTestName, fTypeId3, false, 4);
    }

    /**
     * Test checking for child.
     */
    @Test
    public void testContainsChild() {
        TmfStatisticsTreeNode rootNode  = fStatsTree.getRootNode();
        TmfStatisticsTreeNode traceNode = fStatsTree.getNode(fTestName);
        // Creates a category from the key already created
        TmfStatisticsTreeNode catNode = traceNode.getChildren().iterator().next();

        assertTrue(rootNode.containsChild(fTestName));
        assertFalse(rootNode.containsChild(catNode.getName()));

        assertTrue(traceNode.containsChild(catNode.getName()));
        assertFalse(traceNode.containsChild(fTypeId1));

        assertTrue(catNode.containsChild(fTypeId1));
        assertTrue(catNode.containsChild(fTypeId2));
    }

    /**
     * Test getting of children.
     */
    @Test
    public void testGetChildren() {
        // Getting children of the ROOT
        Collection<TmfStatisticsTreeNode> childrenTreeNode = fStatsTree.getRootNode().getChildren();
        assertEquals(1, childrenTreeNode.size());
        TmfStatisticsTreeNode treeNode = childrenTreeNode.iterator().next();
        assertEquals(fTestName, treeNode.getName());

        // Getting children of the trace
        childrenTreeNode = fStatsTree.getNode(fTestName).getChildren();
        assertEquals(1, childrenTreeNode.size());
        treeNode = childrenTreeNode.iterator().next();
        assertEquals(Messages.TmfStatisticsData_EventTypes, treeNode.getName());

        Vector<String> keyExpected = new Vector<>();
        keyExpected.add(fTypeId1);
        keyExpected.add(fTypeId2);
        keyExpected.add(fTypeId3);
        // Getting children of a category
        childrenTreeNode = treeNode.getChildren();
        assertEquals(3, childrenTreeNode.size());

        Iterator<TmfStatisticsTreeNode> iterChild = childrenTreeNode.iterator();
        TmfStatisticsTreeNode temp;
        while (iterChild.hasNext()) {
            temp = iterChild.next();
            if (keyExpected.contains(temp.getName())) {
                keyExpected.removeElement(temp.getName());
            } else {
                fail();
            }
        }

        // Get children of a specific event type
        childrenTreeNode = fStatsTree.getNode(childrenTreeNode.iterator().next().getPath()).getChildren();
        assertEquals(0, childrenTreeNode.size());
    }

    /**
     * Test getting of number of children.
     */
    @Test
    public void testGetNbChildren() {
        TmfStatisticsTreeNode rootNode    = fStatsTree.getRootNode();
        TmfStatisticsTreeNode traceNode   = fStatsTree.getNode(fTestName);
        TmfStatisticsTreeNode catNode     = traceNode.getChildren().iterator().next();
        TmfStatisticsTreeNode elementNode = fStatsTree.getNode(fTestName, Messages.TmfStatisticsData_EventTypes, fTypeId1);

        assertEquals(1, rootNode.getNbChildren());
        assertEquals(1, traceNode.getNbChildren());
        assertEquals(3, catNode.getNbChildren());
        assertEquals(0, elementNode.getNbChildren());
    }

    /**
     * Test checking for children.
     */
    @Test
    public void testHasChildren() {
        TmfStatisticsTreeNode rootNode    = fStatsTree.getRootNode();
        TmfStatisticsTreeNode traceNode   = fStatsTree.getNode(fTestName);
        TmfStatisticsTreeNode catNode     = traceNode.getChildren().iterator().next();
        TmfStatisticsTreeNode elementNode = fStatsTree.getNode(fTestName, Messages.TmfStatisticsData_EventTypes, fTypeId1);

        assertTrue(rootNode.hasChildren());
        assertTrue(traceNode.hasChildren());
        assertTrue(catNode.hasChildren());
        assertFalse(elementNode.hasChildren());
    }

    /**
     * Test getting of parent.
     */
    @Test
    public void testGetParent() {
        final TmfStatisticsTreeNode rootNode = fStatsTree.getRootNode();
        TmfStatisticsTreeNode parentNode = rootNode.getParent();
        assertNull(parentNode);

        TmfStatisticsTreeNode newTraceNode = new TmfStatisticsTreeNode(fStatsTree, rootNode, "newly created trace node");
        parentNode = newTraceNode.getParent();
        assertNotNull(parentNode);
        assertTrue(fStatsTree.getRootNode() == parentNode);

        TmfStatisticsTreeNode traceNode = fStatsTree.getNode(fTestName);
        parentNode = traceNode.getParent();
        assertNotNull(parentNode);
        assertTrue(rootNode == parentNode);

        TmfStatisticsTreeNode elementNode = fStatsTree.getNode(fTestName, Messages.TmfStatisticsData_EventTypes, fTypeId1);
        parentNode = elementNode.getParent();
        assertTrue(parentNode == fStatsTree.getNode(fTestName, Messages.TmfStatisticsData_EventTypes));

        TmfStatisticsTreeNode catNode = traceNode.getChildren().iterator().next();
        parentNode = catNode.getParent();
        assertNotNull(parentNode);
        assertTrue(parentNode == fStatsTree.getNode(fTestName));

        parentNode = elementNode.getParent();
        assertNotNull(parentNode);
        assertTrue(arraysEqual(parentNode.getPath(), fTestName, Messages.TmfStatisticsData_EventTypes));
    }

    /**
     * Test getting of key.
     */
    @Test
    public void testgetName() {
        TmfStatisticsTreeNode rootNode    = fStatsTree.getRootNode();
        TmfStatisticsTreeNode traceNode   = fStatsTree.getNode(fTestName);
        TmfStatisticsTreeNode catNode     = traceNode.getChildren().iterator().next();
        TmfStatisticsTreeNode elementNode = fStatsTree.getNode(fTestName, Messages.TmfStatisticsData_EventTypes, fTypeId1);

        assertEquals(0, rootNode.getName().compareTo("root"));
        assertEquals(0, traceNode.getName().compareTo(fTestName));
        assertEquals(0, catNode.getName().compareTo(Messages.TmfStatisticsData_EventTypes));
        assertEquals(0, elementNode.getName().compareTo(fTypeId1));
    }

    /**
     * Test getting of path to node.
     */
    @Test
    public void testGetPath() {
        TmfStatisticsTreeNode rootNode    = fStatsTree.getRootNode();
        TmfStatisticsTreeNode traceNode   = fStatsTree.getNode(fTestName);
        TmfStatisticsTreeNode catNode     = traceNode.getChildren().iterator().next();
        TmfStatisticsTreeNode elementNode = fStatsTree.getNode(fTestName, Messages.TmfStatisticsData_EventTypes, fTypeId1);

        assertEquals(0, rootNode.getPath().length); /* Root node has an empty path */
        assertTrue(arraysEqual(traceNode.getPath(), fTestName));
        assertTrue(arraysEqual(catNode.getPath(),
                fTestName, Messages.TmfStatisticsData_EventTypes));
        assertTrue(arraysEqual(elementNode.getPath(),
                fTestName, Messages.TmfStatisticsData_EventTypes, fTypeId1));
    }

    /**
     * Test getting statistic value.
     */
    @Test
    public void testGetValue() {
        TmfStatisticsTreeNode rootNode, traceNode, catNode, elementNode1, elementNode2, elementNode3;
        rootNode     = fStatsTree.getRootNode();
        traceNode    = fStatsTree.getNode(fTestName);
        catNode      = traceNode.getChildren().iterator().next();
        elementNode1 = fStatsTree.getNode(fTestName, Messages.TmfStatisticsData_EventTypes, fTypeId1);
        elementNode2 = fStatsTree.getNode(fTestName, Messages.TmfStatisticsData_EventTypes, fTypeId2);
        elementNode3 = fStatsTree.getNode(fTestName, Messages.TmfStatisticsData_EventTypes, fTypeId3);

        assertEquals(0, rootNode.getValues().getTotal());
        assertEquals(18, traceNode.getValues().getTotal());
        assertEquals(0, catNode.getValues().getTotal());
        assertEquals(5, elementNode1.getValues().getTotal());
        assertEquals(6, elementNode2.getValues().getTotal());
        assertEquals(7, elementNode3.getValues().getTotal());

        assertEquals(0, rootNode.getValues().getPartial());
        assertEquals(9, traceNode.getValues().getPartial());
        assertEquals(0, catNode.getValues().getPartial());
        assertEquals(2, elementNode1.getValues().getPartial());
        assertEquals(3, elementNode2.getValues().getPartial());
        assertEquals(4, elementNode3.getValues().getPartial());
    }

    /**
     * Test reset of tree.
     */
    @Test
    public void testReset() {
        TmfStatisticsTreeNode rootNode, traceNode, catNode, elementNode;
        rootNode    = fStatsTree.getRootNode();
        traceNode   = fStatsTree.getNode(fTestName);
        catNode     = traceNode.getChildren().iterator().next();
        elementNode = fStatsTree.getNode(fTestName, Messages.TmfStatisticsData_EventTypes, fTypeId1);

        elementNode.reset();
        assertEquals(0, elementNode.getValues().getTotal());
        assertEquals(0, elementNode.getValues().getPartial());

        catNode.reset();
        assertEquals(0, catNode.getValues().getTotal());
        assertEquals(0, catNode.getValues().getPartial());
        assertEquals(0, catNode.getNbChildren());
        assertNull(fStatsTree.getNode(fTestName, Messages.TmfStatisticsData_EventTypes, fTypeId1));

        traceNode.reset();
        assertEquals(0, traceNode.getValues().getTotal());
        assertEquals(0, traceNode.getValues().getPartial());
        assertEquals(0, traceNode.getNbChildren());

        rootNode.reset();
        assertEquals(0, rootNode.getValues().getTotal());
        assertEquals(0, rootNode.getValues().getPartial());
        assertEquals(0, rootNode.getNbChildren());
    }

    /**
     * Test reset global value of the node in the tree. It should only clear
     * the global value without removing any node from the tree.
     */
    @Test
    public void testResetGlobalValue() {
        TmfStatisticsTreeNode rootNode, traceNode, catNode, eventTypeNode1, eventTypeNode2, eventTypeNode3;
        rootNode       = fStatsTree.getRootNode();
        traceNode      = fStatsTree.getNode(fTestName);
        catNode        = traceNode.getChildren().iterator().next();
        eventTypeNode1 = fStatsTree.getNode(fTestName, Messages.TmfStatisticsData_EventTypes, fTypeId1);
        eventTypeNode2 = fStatsTree.getNode(fTestName, Messages.TmfStatisticsData_EventTypes, fTypeId2);
        eventTypeNode3 = fStatsTree.getNode(fTestName, Messages.TmfStatisticsData_EventTypes, fTypeId3);

        rootNode.resetGlobalValue();

        assertEquals(0, rootNode.getValues().getTotal());
        assertEquals(0, traceNode.getValues().getTotal());
        assertEquals(0, catNode.getValues().getTotal());
        assertEquals(0, eventTypeNode1.getValues().getTotal());
        assertEquals(0, eventTypeNode2.getValues().getTotal());
        assertEquals(0, eventTypeNode3.getValues().getTotal());

        // Checks the state of the statistics tree
        Collection<TmfStatisticsTreeNode> rootChildren =  rootNode.getChildren();
        assertEquals(1, rootChildren.size());
        assertTrue(rootChildren.contains(traceNode));

        Collection<TmfStatisticsTreeNode> traceChildren =  traceNode.getChildren();
        assertEquals(1, traceChildren.size());
        assertTrue(traceChildren.contains(catNode));

        Collection<TmfStatisticsTreeNode> catChildren =  catNode.getChildren();
        assertEquals(3, catChildren.size());
        assertTrue(catChildren.contains(eventTypeNode1));
        assertTrue(catChildren.contains(eventTypeNode2));
        assertTrue(catChildren.contains(eventTypeNode3));
    }

    /**
     * Test reset time range value of the node in the tree. It should only clear
     * the time range value without removing any node from the tree.
     */
    @Test
    public void testResetTimeRangeValue() {
        TmfStatisticsTreeNode rootNode, traceNode, catNode, eventTypeNode1, eventTypeNode2, eventTypeNode3;
        rootNode       = fStatsTree.getRootNode();
        traceNode      = fStatsTree.getNode(fTestName);
        catNode        = traceNode.getChildren().iterator().next();
        eventTypeNode1 = fStatsTree.getNode(fTestName, Messages.TmfStatisticsData_EventTypes, fTypeId1);
        eventTypeNode2 = fStatsTree.getNode(fTestName, Messages.TmfStatisticsData_EventTypes, fTypeId2);
        eventTypeNode3 = fStatsTree.getNode(fTestName, Messages.TmfStatisticsData_EventTypes, fTypeId3);

        rootNode.resetTimeRangeValue();

        assertEquals(0, rootNode.getValues().getPartial());
        assertEquals(0, traceNode.getValues().getPartial());
        assertEquals(0, catNode.getValues().getPartial());
        assertEquals(0, eventTypeNode1.getValues().getPartial());
        assertEquals(0, eventTypeNode2.getValues().getPartial());

        // Checks the state of the statistics tree
        Collection<TmfStatisticsTreeNode> rootChildren =  rootNode.getChildren();
        assertEquals(1, rootChildren.size());
        assertTrue(rootChildren.contains(traceNode));

        Collection<TmfStatisticsTreeNode> traceChildren =  traceNode.getChildren();
        assertEquals(1, traceChildren.size());
        assertTrue(traceChildren.contains(catNode));

        Collection<TmfStatisticsTreeNode> catChildren =  catNode.getChildren();
        assertEquals(3, catChildren.size());
        assertTrue(catChildren.contains(eventTypeNode1));
        assertTrue(catChildren.contains(eventTypeNode2));
        assertTrue(catChildren.contains(eventTypeNode3));
    }

    /**
     * Check if two String arrays are equals, by comparing their contents.
     * Unlike Arrays.equals(), we can use varargs for the second argument.
     */
    private static boolean arraysEqual(String[] array1, String... array2) {
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
}
