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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeManager;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;
import org.junit.Before;
import org.junit.Test;

/**
 * TmfStatisticsTreeRootFactory Test Case.
 */
public class TmfStatisticsTreeManagerTest {

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    private TmfStatisticsTree fStatisticsData1;
    private TmfStatisticsTree fStatisticsData2;
    private TmfStatisticsTree fStatisticsData3;
    private String            fDataKey1 = "key1";
    private String            fDataKey2 = "key2";
    private String            fDataKey3 = "key3";


    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * Initialization
     */
    @Before
    public void setUp() {
        fStatisticsData1 = new TmfStatisticsTree();
        fStatisticsData2 = new TmfStatisticsTree();
        fStatisticsData3 = new TmfStatisticsTree();
        TmfStatisticsTreeManager.addStatsTreeRoot(fDataKey1, fStatisticsData1);
        TmfStatisticsTreeManager.addStatsTreeRoot(fDataKey2, fStatisticsData2);
        TmfStatisticsTreeManager.addStatsTreeRoot(fDataKey2, fStatisticsData3);
    }

    /**
     * Clean the statistics tree
     */
    private static void removeStatsTreeRoot() {
        TmfStatisticsTreeManager.removeAll();
    }

    /**
     * Test adding of statistics tree root. It should not throw exceptions
     */
    @Test
    public void testaddStatsTreeRoot() {
        removeStatsTreeRoot();

        try {
            assertNull(TmfStatisticsTreeManager.addStatsTreeRoot(null, null));
            assertNull(TmfStatisticsTreeManager.addStatsTreeRoot(null, fStatisticsData1));
            assertNull(TmfStatisticsTreeManager.addStatsTreeRoot(fDataKey1, null));
            assertNull(TmfStatisticsTreeManager.getStatTreeRoot(fDataKey1));

            TmfStatisticsTreeNode returnRootNode = TmfStatisticsTreeManager.addStatsTreeRoot(fDataKey1, fStatisticsData1);
            assertSame(fStatisticsData1, TmfStatisticsTreeManager.getStatTree(fDataKey1));
            assertSame(fStatisticsData1.getRootNode(), returnRootNode);

            // Overwriting the value
            returnRootNode = TmfStatisticsTreeManager.addStatsTreeRoot(fDataKey1, fStatisticsData2);
            assertSame(fStatisticsData2, TmfStatisticsTreeManager.getStatTree(fDataKey1));
            assertSame(fStatisticsData2.getRootNode(), returnRootNode);

            // Success
        } catch(Exception e) {
            fail("AddStatsTreeRoot");
        }
    }

    // ------------------------------------------------------------------------
    // get
    // ------------------------------------------------------------------------

    /**
     * Test getting of statistics tree root.
     */
    @Test
    public void testGetStatTreeRoot() {
        TmfStatisticsTreeNode value1 = TmfStatisticsTreeManager.getStatTreeRoot(fDataKey1);
        TmfStatisticsTreeNode value2 = TmfStatisticsTreeManager.getStatTreeRoot(fDataKey2);
        TmfStatisticsTreeNode value3 = TmfStatisticsTreeManager.getStatTreeRoot(fDataKey1);
        assertNotSame("getStatTreeRoot", value1, value2);
        assertNotSame("getStatTreeRoot", value2, value3);
        assertSame("getStatTreeRoot", value1, value3);
        assertNull("getStatTreeRoot", TmfStatisticsTreeManager.getStatTreeRoot(null));
    }

    /**
     * Test getting statistics tree.
     */
    @Test
    public void testGetStatTree() {
        TmfStatisticsTree value1 = TmfStatisticsTreeManager.getStatTree(fDataKey1);
        TmfStatisticsTree value2 = TmfStatisticsTreeManager.getStatTree(fDataKey2);
        TmfStatisticsTree value3 = TmfStatisticsTreeManager.getStatTree(fDataKey1);
        assertNotSame("getStatTree", value1, value2);
        assertNotSame("getStatTree", value2, value3);
        assertSame("getStatTree", value1, value3);
        assertNull("getStatTreeRoot", TmfStatisticsTreeManager.getStatTree(null));
    }

    // ------------------------------------------------------------------------
    // contains
    // ------------------------------------------------------------------------

    /**
     * Test checking for tree root existence.
     */
    @Test
    public void testContainsTreeRoot() {
        assertTrue("containsTreeRoot", TmfStatisticsTreeManager.containsTreeRoot(fDataKey1));
        assertTrue("containsTreeRoot", TmfStatisticsTreeManager.containsTreeRoot(fDataKey2));
        assertFalse("containsTreeRoot", TmfStatisticsTreeManager.containsTreeRoot(null));
    }

    // ------------------------------------------------------------------------
    // remove
    // ------------------------------------------------------------------------

    /**
     * Test removal of statistics tree node.
     */
    @Test
    public void testRemoveStatTreeRoot() {
        TmfStatisticsTreeManager.removeStatTreeRoot(fDataKey1);
        assertNull("removeStatTreeRoot", TmfStatisticsTreeManager.getStatTree(fDataKey1));

        try {
            // Trying to remove the same branch from the tree.
            TmfStatisticsTreeManager.removeStatTreeRoot(fDataKey1);

            TmfStatisticsTreeManager.removeStatTreeRoot(null);
            // Success
        } catch (Exception e) {
            fail("removeStatTreeRoot");
        }
    }

    /**
     * Test removal of all root nodes.
     */
    @Test
    public void testRemoveAll() {
        TmfStatisticsTreeManager.removeAll();
        assertNull("removeAll", TmfStatisticsTreeManager.getStatTreeRoot(fDataKey2));
        assertNull("removeAll", TmfStatisticsTreeManager.getStatTreeRoot(fDataKey3));
    }
}
