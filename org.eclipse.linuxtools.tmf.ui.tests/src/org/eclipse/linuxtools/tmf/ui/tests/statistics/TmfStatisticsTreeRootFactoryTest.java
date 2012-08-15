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

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfBaseStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.AbsTmfStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeRootFactory;

/**
 * TmfStatisticsTreeRootFactory Test Case.
 */
@SuppressWarnings("nls")
public class TmfStatisticsTreeRootFactoryTest extends TestCase {

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    AbsTmfStatisticsTree fStatisticsData1;
    AbsTmfStatisticsTree fStatisticsData2;
    AbsTmfStatisticsTree fStatisticsData3;
    String            fDataKey1 = "key1";
    String            fDataKey2 = "key2";
    String            fDataKey3 = "key3";

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        addStatsTreeRoot();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Adding of statistics tree root.
     */
    public void addStatsTreeRoot() {
        fStatisticsData1 = new TmfBaseStatisticsTree();
        fStatisticsData2 = new TmfBaseStatisticsTree();
        fStatisticsData3 = new TmfBaseStatisticsTree();
        TmfStatisticsTreeRootFactory.addStatsTreeRoot(fDataKey1, fStatisticsData1);
        TmfStatisticsTreeRootFactory.addStatsTreeRoot(fDataKey2, fStatisticsData2);
        TmfStatisticsTreeRootFactory.addStatsTreeRoot(fDataKey2, fStatisticsData3);
    }

    /**
     * Clean the statistics tree
     */
    public void removeStatsTreeRoot() {
        TmfStatisticsTreeRootFactory.removeAll();
    }

    /**
     * Test adding of statistics tree root. It should not throw exceptions
     */
    public void testaddStatsTreeRoot() {
        removeStatsTreeRoot();

        try {
            TmfStatisticsTreeRootFactory.addStatsTreeRoot(null, null);
            TmfStatisticsTreeRootFactory.addStatsTreeRoot(null, fStatisticsData1);
            TmfStatisticsTreeRootFactory.addStatsTreeRoot(fDataKey1, null);
            assertNull(TmfStatisticsTreeRootFactory.getStatTreeRoot(fDataKey1));

            TmfStatisticsTreeRootFactory.addStatsTreeRoot(fDataKey1, fStatisticsData1);
            assertSame(fStatisticsData1, TmfStatisticsTreeRootFactory.getStatTree(fDataKey1));

            // Overwriting the value
            TmfStatisticsTreeRootFactory.addStatsTreeRoot(fDataKey1, fStatisticsData2);
            assertSame(fStatisticsData2, TmfStatisticsTreeRootFactory.getStatTree(fDataKey1));

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
    public void testGetStatTreeRoot() {
        TmfStatisticsTreeNode value1 = TmfStatisticsTreeRootFactory.getStatTreeRoot(fDataKey1);
        TmfStatisticsTreeNode value2 = TmfStatisticsTreeRootFactory.getStatTreeRoot(fDataKey2);
        TmfStatisticsTreeNode value3 = TmfStatisticsTreeRootFactory.getStatTreeRoot(fDataKey1);
        assertNotSame("getStatTreeRoot", value1, value2);
        assertNotSame("getStatTreeRoot", value2, value3);
        assertSame("getStatTreeRoot", value1, value3);
        assertNull("getStatTreeRoot", TmfStatisticsTreeRootFactory.getStatTreeRoot(null));
    }

    /**
     * Test getting statistics tree.
     */
    public void testGetStatTree() {
        AbsTmfStatisticsTree value1 = TmfStatisticsTreeRootFactory.getStatTree(fDataKey1);
        AbsTmfStatisticsTree value2 = TmfStatisticsTreeRootFactory.getStatTree(fDataKey2);
        AbsTmfStatisticsTree value3 = TmfStatisticsTreeRootFactory.getStatTree(fDataKey1);
        assertNotSame("getStatTree", value1, value2);
        assertNotSame("getStatTree", value2, value3);
        assertSame("getStatTree", value1, value3);
        assertNull("getStatTreeRoot", TmfStatisticsTreeRootFactory.getStatTree(null));
    }

    // ------------------------------------------------------------------------
    // contains
    // ------------------------------------------------------------------------

    /**
     * Test checking for tree root existence.
     */
    public void testContainsTreeRoot() {
        assertTrue("containsTreeRoot", TmfStatisticsTreeRootFactory.containsTreeRoot(fDataKey1));
        assertTrue("containsTreeRoot", TmfStatisticsTreeRootFactory.containsTreeRoot(fDataKey2));
        assertFalse("containsTreeRoot", TmfStatisticsTreeRootFactory.containsTreeRoot(null));
    }

    // ------------------------------------------------------------------------
    // remove
    // ------------------------------------------------------------------------

    /**
     * Test removal of statistics tree node.
     */
    public void testRemoveStatTreeRoot() {
        TmfStatisticsTreeRootFactory.removeStatTreeRoot(fDataKey1);
        assertNull("removeStatTreeRoot", TmfStatisticsTreeRootFactory.getStatTree(fDataKey1));

        try {
            // Trying to remove the same branch from the tree.
            TmfStatisticsTreeRootFactory.removeStatTreeRoot(fDataKey1);

            TmfStatisticsTreeRootFactory.removeStatTreeRoot(null);
            // Success
        } catch (Exception e) {
            fail("removeStatTreeRoot");
        }
    }

    /**
     * Test removal of all root nodes.
     */
    public void testRemoveAll() {
        TmfStatisticsTreeRootFactory.removeAll();
        assertNull("removeAll", TmfStatisticsTreeRootFactory.getStatTreeRoot(fDataKey2));
        assertNull("removeAll", TmfStatisticsTreeRootFactory.getStatTreeRoot(fDataKey3));
    }
}
