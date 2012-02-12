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

import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventContent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.util.TmfFixedArray;
import org.eclipse.linuxtools.tmf.ui.views.statistics.ITmfExtraEventInfo;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.AbsTmfStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.Messages;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.TmfBaseStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.TmfStatisticsTreeNode;

@SuppressWarnings("nls")
public class TmfBaseStatisticsDataTest extends TestCase {
    
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
    
    private final TmfEventType fType1 = new TmfEventType(fContext, fTypeId1, fLabels);
    private final TmfEventType fType2 = new TmfEventType(fContext, fTypeId1, fLabels);
    private final TmfEventType fType3 = new TmfEventType(fContext, fTypeId2, fLabels);
    
    private final String fReference = "Some reference";

    private final TmfEvent fEvent1;
    private final TmfEvent fEvent2;
    private final TmfEvent fEvent3;

    private final TmfEventContent fContent1;
    private final TmfEventContent fContent2;
    private final TmfEventContent fContent3;
    
    private final TmfBaseStatisticsTree fStatsData;
    
    private final ITmfExtraEventInfo fExtraInfo;
    
    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------
    
    /**
     * @param name of the test
     */
    public TmfBaseStatisticsDataTest(final String name) {
        super(name);
        
        fTestName = name;
        
        fEvent1 = new TmfEvent(fTimestamp1, fSource, fType1, fReference);
        fContent1 = new TmfEventContent(fEvent1, "Some content");
        fEvent1.setContent(fContent1);

        fEvent2 = new TmfEvent(fTimestamp2, fSource, fType2, fReference);
        fContent2 = new TmfEventContent(fEvent2, "Some other content");
        fEvent2.setContent(fContent2);
        
        fEvent3 = new TmfEvent(fTimestamp3, fSource, fType3, fReference);
        fContent3 = new TmfEventContent(fEvent3, "Some other different content");
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
    // GetChildren
    // ------------------------------------------------------------------------

    public void testGetChildren() {
        // Getting children of the ROOT
        Collection<TmfStatisticsTreeNode> childrenTreeNode = fStatsData.getChildren(AbsTmfStatisticsTree.ROOT);
        assertEquals("getChildren", 1, childrenTreeNode.size());
        TmfStatisticsTreeNode treeNode = childrenTreeNode.iterator().next();
        assertEquals("getChildren", fTestName, treeNode.getKey());
        
        // Getting children of the trace
        childrenTreeNode = fStatsData.getChildren(new TmfFixedArray<String>(fTestName));
        assertEquals("getChildren", 1, childrenTreeNode.size());
        treeNode = childrenTreeNode.iterator().next();
        assertEquals("getChildren", Messages.TmfStatisticsData_EventTypes, treeNode.getKey());
        
        Vector<String> keyExpected = new Vector<String>(); 
        keyExpected.add(fEvent1.getType().toString());
        keyExpected.add(fEvent3.getType().toString());
        // Getting children of a category
        childrenTreeNode = fStatsData.getChildren(treeNode.getPath());
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
        childrenTreeNode = fStatsData.getChildren(childrenTreeNode.iterator().next().getPath());
        assertEquals("getChildren", 0, childrenTreeNode.size());
    }
    
    // ------------------------------------------------------------------------
    // GetAllChildren
    // ------------------------------------------------------------------------

    public void testGetAllChildren() {
        // Getting children of the ROOT
        Collection<TmfStatisticsTreeNode> childrenTreeNode = fStatsData.getAllChildren(AbsTmfStatisticsTree.ROOT);
        assertEquals("getChildren", 1, childrenTreeNode.size());
        TmfStatisticsTreeNode treeNode = childrenTreeNode.iterator().next();
        assertEquals("getChildren", fTestName, treeNode.getKey());
        
        // Getting children of the trace
        childrenTreeNode = fStatsData.getAllChildren(new TmfFixedArray<String>(fTestName));
        assertEquals("getChildren", 1, childrenTreeNode.size());
        treeNode = childrenTreeNode.iterator().next();
        assertEquals("getChildren", Messages.TmfStatisticsData_EventTypes, treeNode.getKey());
        
        Vector<String> keyExpected = new Vector<String>(); 
        keyExpected.add(fEvent1.getType().toString());
        keyExpected.add(fEvent3.getType().toString());
        // It should return the eventType even though the number of events equals 0
        fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fEvent1.getType().toString())).reset();
        // Getting children of a category
        childrenTreeNode = fStatsData.get(treeNode.getPath()).getAllChildren();
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
        childrenTreeNode = fStatsData.getAllChildren(childrenTreeNode.iterator().next().getPath());
        assertEquals("getChildren", 0, childrenTreeNode.size());
    }

    // ------------------------------------------------------------------------
    // RegisterEvent
    // ------------------------------------------------------------------------
    
    public void testRegisterEvent() {
        TmfStatisticsTreeNode trace = fStatsData.get(new TmfFixedArray<String>(fTestName));
        assertEquals("registerEvent", 3, trace.getValue().nbEvents);
        
        Collection<TmfStatisticsTreeNode> childrenTreeNode = fStatsData.getChildren(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes));
        for (TmfStatisticsTreeNode child : childrenTreeNode) {
            if (child.getKey().compareTo(fEvent1.getType().toString()) == 0) {
                assertEquals("registerEvent", 2, child.getValue().nbEvents);                
            }
            else if (child.getKey().compareTo(fEvent3.getType().toString()) == 0) {
                assertEquals("registerEvent", 1, child.getValue().nbEvents);
            }
        }
    }
    
    // ------------------------------------------------------------------------
    // Get a node
    // ------------------------------------------------------------------------
    
    public void testGet() {
        TmfStatisticsTreeNode traceRoot = fStatsData.get(new TmfFixedArray<String>(fTestName));
        assertNotNull("get", traceRoot);
        assertEquals("get", 0, traceRoot.getPath().toString().compareTo("[" + fTestName + "]"));
        assertEquals("get", 3, traceRoot.getValue().nbEvents);
        assertEquals("get", 1, traceRoot.getNbChildren());
    }
    
    // ------------------------------------------------------------------------
    // GetOrCreate
    // ------------------------------------------------------------------------
    
    public void testGetOrCreate() {
        TmfFixedArray<String> newEventType = new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, "Fancy Type");
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
    
    public void testGetParent() {
        TmfStatisticsTreeNode parentNode = fStatsData.getParent(AbsTmfStatisticsTree.ROOT);
        assertNull("getParent", parentNode);
        
        parentNode = fStatsData.getParent(new TmfFixedArray<String>("TreeRootNode that should not exist"));
        assertNotNull("getParent", parentNode);
        assertEquals("getParent", 0, parentNode.getKey().compareTo(fStatsData.get(AbsTmfStatisticsTree.ROOT).getKey().toString()));
        
        parentNode = fStatsData.getParent(new TmfFixedArray<String>("TreeNode", Messages.TmfStatisticsData_EventTypes, "TreeNode that should not exist"));
        assertNull("getParent", parentNode);
        parentNode = fStatsData.getParent(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fEvent1.getType().toString()));
        assertNull("getParent", parentNode);
        
        parentNode = fStatsData.getParent(new TmfFixedArray<String>(fTestName));
        assertNotNull("getParent", parentNode);
        assertEquals("getParent", 0, parentNode.getPath().toString().compareTo(AbsTmfStatisticsTree.ROOT.toString()));
        
        parentNode = fStatsData.getParent(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes));
        assertNotNull("getParent", parentNode);
        assertEquals("getParent", 0, parentNode.getPath().toString().compareTo(fStatsData.get(new TmfFixedArray<String>(fTestName)).getPath().toString()));
    }

    // ------------------------------------------------------------------------
    // Reset
    // ------------------------------------------------------------------------

    public void testReset() {
        fStatsData.reset(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes));
        
        assertEquals("reset", 0, fStatsData.getChildren(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes)).size());
        assertNull("reset", fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fType1.getId())));
        assertNull("reset", fStatsData.get(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fType3.getId())));
        
        fStatsData.reset(new TmfFixedArray<String>(fTestName));
        
        // A rootz should always have at least one child that is eventType
        assertEquals("reset", 1, fStatsData.getChildren(new TmfFixedArray<String>(fTestName)).size());
    }
}
