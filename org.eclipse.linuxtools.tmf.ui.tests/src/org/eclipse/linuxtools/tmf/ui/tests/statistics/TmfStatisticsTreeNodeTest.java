/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Mathieu Denis (mathieu.denis@polymtl.ca)  - Initial design and implementation
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
import org.eclipse.linuxtools.tmf.ui.views.statistics.ITmfExtraEventInfo;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.AbsTmfStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.Messages;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.TmfBaseStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.TmfStatisticsTreeNode;

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
    private final TmfEventType fType2 = new TmfEventType(fContext, fTypeId1, TmfEventField.makeRoot(fLabels));
    private final TmfEventType fType3 = new TmfEventType(fContext, fTypeId2, TmfEventField.makeRoot(fLabels));
    
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
     * @param name of the test
     */
    public TmfStatisticsTreeNodeTest(final String name) {
        super(name);
        
        fTestName = name;
        
        fEvent1 = new TmfEvent(fTimestamp1, fSource, fType1, fReference);
        fContent1 = new TmfEventField(ITmfEventField.ROOT_ID, "Some content");
        fEvent1.setContent(fContent1);

        fEvent2 = new TmfEvent(fTimestamp2, fSource, fType2, fReference);
        fContent2 = new TmfEventField(ITmfEventField.ROOT_ID, "Some other content");
        fEvent2.setContent(fContent2);
        
        fEvent3 = new TmfEvent(fTimestamp3, fSource, fType3, fReference);
        fContent3 = new TmfEventField(ITmfEventField.ROOT_ID, "Some other different content");
        fEvent3.setContent(fContent3);
        
        fStatsData = new TmfBaseStatisticsTree();
        fExtraInfo = new ITmfExtraEventInfo() {
            @Override
            public String getTraceName() {
                return name;
            }
        };
        fStatsData.registerEvent(fEvent1, fExtraInfo);
        fStatsData.registerEvent(fEvent2, fExtraInfo);
        fStatsData.registerEvent(fEvent3, fExtraInfo);
    }
    
    // ------------------------------------------------------------------------
    // ContainsChild
    // ------------------------------------------------------------------------

    public void testContainsChild() {
        TmfStatisticsTreeNode rootNode  = fStatsData.get(AbsTmfStatisticsTree.ROOT);
        TmfStatisticsTreeNode traceNode = fStatsData.get(new TmfFixedArray<String>(fTestName));
        // Creates a category from the key already created
        TmfStatisticsTreeNode catNode = traceNode.getChildren().iterator().next();
        
        assertTrue("containsChild",  rootNode.containsChild(fTestName));
        assertFalse("containsChild", rootNode.containsChild(catNode.getKey()));
        assertFalse("containsChild", rootNode.containsChild(null));
        
        assertTrue("containsChild",  traceNode.containsChild(catNode.getKey()));
        assertFalse("containsChild", traceNode.containsChild(fEvent1.getType().toString()));
        assertFalse("containsChild", traceNode.containsChild(null));
        
        assertTrue("containsChild",  catNode.containsChild(fEvent1.getType().toString()));
        assertTrue("containsChild",  catNode.containsChild(fEvent3.getType().toString()));
        assertFalse("containsChild", catNode.containsChild(null));
    }
    
    // ------------------------------------------------------------------------
    // GetChildren
    // ------------------------------------------------------------------------

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
        keyExpected.add(fEvent1.getType().toString());
        keyExpected.add(fEvent3.getType().toString());
        // Getting children of a category
        childrenTreeNode = treeNode.getChildren();
        assertEquals("getChildren", 2, childrenTreeNode.size());
        
        Iterator<TmfStatisticsTreeNode> iterChild = childrenTreeNode.iterator();
        TmfStatisticsTreeNode temp;
        while (iterChild.hasNext()) {
            temp = iterChild.next();
            if (keyExpected.contains(temp.getKey())) {
                keyExpected.removeElement(temp.getKey());
            }
            else {
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
        keyExpected.add(fEvent1.getType().toString());
        keyExpected.add(fEvent3.getType().toString());
        // It should return the eventType even though the number of events equals 0
        fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fEvent1.getType().toString())).reset();
        // Getting children of a category
        childrenTreeNode = treeNode.getAllChildren();
        assertEquals("getChildren", 2, childrenTreeNode.size());
        
        Iterator<TmfStatisticsTreeNode> iterChild = childrenTreeNode.iterator();
        TmfStatisticsTreeNode temp;
        while (iterChild.hasNext()) {
            temp = iterChild.next();
            if (keyExpected.contains(temp.getKey())) {
                keyExpected.removeElement(temp.getKey());
            }
            else {
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
    
    public void testGetNbChildren() {
        TmfStatisticsTreeNode rootNode    = fStatsData.get(AbsTmfStatisticsTree.ROOT);
        TmfStatisticsTreeNode traceNode   = fStatsData.get(new TmfFixedArray<String>(fTestName));
        TmfStatisticsTreeNode catNode     = traceNode.getChildren().iterator().next();
        TmfStatisticsTreeNode elementNode = fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fEvent1.getType().toString()));
        
        assertEquals("getNbChildren", 1,    rootNode.getNbChildren());
        assertEquals("getNbChildren", 1,   traceNode.getNbChildren());
        assertEquals("getNbChildren", 2,     catNode.getNbChildren());
        assertEquals("getNbChildren", 0, elementNode.getNbChildren());
    }
    
    // ------------------------------------------------------------------------
    // HasChildren
    // ------------------------------------------------------------------------
    
    public void testHasChildren() {
        TmfStatisticsTreeNode rootNode    = fStatsData.get(AbsTmfStatisticsTree.ROOT);
        TmfStatisticsTreeNode traceNode   = fStatsData.get(new TmfFixedArray<String>(fTestName));
        TmfStatisticsTreeNode catNode     = traceNode.getChildren().iterator().next();
        TmfStatisticsTreeNode elementNode = fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fEvent1.getType().toString()));
        
        assertTrue ("hasChildren",    rootNode.hasChildren());
        assertTrue ("hasChildren",   traceNode.hasChildren());
        assertTrue ("hasChildren",     catNode.hasChildren());
        assertFalse("hasChildren", elementNode.hasChildren());
    }
    
    // ------------------------------------------------------------------------
    // GetParent
    // ------------------------------------------------------------------------
    
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
        
        TmfStatisticsTreeNode elementNode = fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fEvent1.getType().toString()));
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
    
    public void testGetKey() {
        TmfStatisticsTreeNode rootNode    = fStatsData.get(AbsTmfStatisticsTree.ROOT);
        TmfStatisticsTreeNode traceNode   = fStatsData.get(new TmfFixedArray<String>(fTestName));
        TmfStatisticsTreeNode catNode     = traceNode.getChildren().iterator().next();
        TmfStatisticsTreeNode elementNode = fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fEvent1.getType().toString()));
        
        assertEquals("getKey", 0,    rootNode.getKey().compareTo(AbsTmfStatisticsTree.ROOT.get(0)));
        assertEquals("getKey", 0,   traceNode.getKey().compareTo(fTestName));
        assertEquals("getKey", 0,     catNode.getKey().compareTo(Messages.TmfStatisticsData_EventTypes));
        assertEquals("getKey", 0, elementNode.getKey().compareTo(fEvent1.getType().toString()));
    }
    
    // ------------------------------------------------------------------------
    // GetPath
    // ------------------------------------------------------------------------
    
    public void testGetPath() {
        TmfStatisticsTreeNode rootNode    = fStatsData.get(AbsTmfStatisticsTree.ROOT);
        TmfStatisticsTreeNode traceNode   = fStatsData.get(new TmfFixedArray<String>(fTestName));
        TmfStatisticsTreeNode catNode     = traceNode.getChildren().iterator().next();
        TmfStatisticsTreeNode elementNode = fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fEvent1.getType().toString()));
        
        assertTrue("getPath",    rootNode.getPath().equals(AbsTmfStatisticsTree.ROOT));
        assertTrue("getPath",   traceNode.getPath().equals(new TmfFixedArray<String>(fTestName)));
        assertTrue("getPath",     catNode.getPath().equals(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes)));
        assertTrue("getPath", elementNode.getPath().equals(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fEvent1.getType().toString())));
    }
    
    // ------------------------------------------------------------------------
    // GetValue
    // ------------------------------------------------------------------------
    
    public void testGetValue() {
        TmfStatisticsTreeNode rootNode     = fStatsData.get(AbsTmfStatisticsTree.ROOT);
        TmfStatisticsTreeNode traceNode    = fStatsData.get(new TmfFixedArray<String>(fTestName));
        TmfStatisticsTreeNode catNode      = traceNode.getChildren().iterator().next();
        TmfStatisticsTreeNode elementNode1 = fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fEvent1.getType().toString()));
        TmfStatisticsTreeNode elementNode2 = fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fEvent3.getType().toString()));
        
        assertEquals("getValue", 0,     rootNode.getValue().nbEvents);
        assertEquals("getValue", 3,    traceNode.getValue().nbEvents);
        assertEquals("getValue", 0,      catNode.getValue().nbEvents);
        assertEquals("getValue", 2, elementNode1.getValue().nbEvents);
        assertEquals("getValue", 1, elementNode2.getValue().nbEvents);
    }
    
    // ------------------------------------------------------------------------
    // Reset
    // ------------------------------------------------------------------------
    
    public void testReset() {
        TmfStatisticsTreeNode rootNode    = fStatsData.get(AbsTmfStatisticsTree.ROOT);
        TmfStatisticsTreeNode traceNode   = fStatsData.get(new TmfFixedArray<String>(fTestName));
        TmfStatisticsTreeNode catNode     = traceNode.getChildren().iterator().next();
        TmfStatisticsTreeNode elementNode = fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fEvent1.getType().toString()));
        
        elementNode.reset();
        assertEquals("reset", 0, elementNode.getValue().nbEvents);
        
        catNode.reset();
        assertEquals("reset", 0, catNode.getValue().nbEvents);
        assertEquals("reset", 0, catNode.getNbChildren());
        assertNull("reset", fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fEvent1.getType().toString())));
        
        traceNode.reset();
        assertEquals("reset", 0, traceNode.getValue().nbEvents);
        // A trace always have at least one child that is eventType
        assertEquals("reset", 1, traceNode.getNbChildren());

        rootNode.reset();
        assertEquals("reset", 0, rootNode.getValue().nbEvents);
        assertEquals("reset", 1, rootNode.getNbChildren());
    }
}
