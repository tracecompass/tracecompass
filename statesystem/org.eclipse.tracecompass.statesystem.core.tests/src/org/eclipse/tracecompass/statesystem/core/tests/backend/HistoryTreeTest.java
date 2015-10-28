/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HTConfig;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HTInterval;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HTNode;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HistoryTree;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompasss.statesystem.core.tests.stubs.backend.HistoryTreeStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the history tree
 *
 * @author Geneviève Bastien
 */
public class HistoryTreeTest {

    /* Minimal allowed blocksize */
    private static final int BLOCK_SIZE = HistoryTree.TREE_HEADER_SIZE;
    /* The extra size used by long and double values */
    private static final int LONG_DOUBLE_SIZE = 8;
    /* The number of extra characters to store a string interval */
    private static final int STRING_PADDING = 2;

    /* String with 23 characters, interval in file will be 25 bytes long */
    private static final String TEST_STRING = "abcdefghifklmnopqrstuvw";
    private static final TmfStateValue STRING_VALUE = TmfStateValue.newValueString(TEST_STRING);
    private static final TmfStateValue LONG_VALUE = TmfStateValue.newValueLong(10L);
    private static final TmfStateValue INT_VALUE = TmfStateValue.newValueInt(1);

    private File fTempFile;

    /**
     * Create the temporary file for this history tree
     */
    @Before
    public void setupTest() {
        try {
            fTempFile = File.createTempFile("tmpStateSystem", null);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Delete the temporary history tree file after the test
     */
    @After
    public void cleanup() {
        fTempFile.delete();
    }

    private HistoryTreeStub setupSmallTree() {
        HistoryTreeStub ht = null;
        try {
            File newFile = fTempFile;
            assertNotNull(newFile);
            HTConfig config = new HTConfig(newFile,
                    BLOCK_SIZE,
                    3, /* Number of children */
                    1, /* Provider version */
                    1); /* Start time */
            ht = new HistoryTreeStub(config);

        } catch (IOException e) {
            fail(e.getMessage());
        }

        assertNotNull(ht);
        return ht;
    }

    private static long fillValues(HistoryTree ht, TmfStateValue value, int nbValues, long start) {
        for (int i = 0; i < nbValues; i++) {
            ht.insertInterval(new HTInterval(start + i, start + i + 1, 1, value));
        }
        return start + nbValues;
    }

    /**
     * Test that nodes are filled
     *
     * It fills nodes with sequential intervals from one attribute only, so that
     * leafs should be filled.
     */
    @Test
    public void testSequentialFill() {
        HistoryTreeStub ht = setupSmallTree();

        HTNode node = ht.getLatestLeaf();
        assertEquals(0, node.getNodeUsagePercent());

        /* Add null intervals up to ~10% */
        int nodeFreeSpace = node.getNodeFreeSpace();
        int nbIntervals = nodeFreeSpace / 10 / HTInterval.DATA_ENTRY_SIZE;
        long start = fillValues(ht, TmfStateValue.nullValue(), nbIntervals, 1);
        assertEquals(nodeFreeSpace - nbIntervals * HTInterval.DATA_ENTRY_SIZE, node.getNodeFreeSpace());

        /* Add integer intervals up to ~20% */
        nodeFreeSpace = node.getNodeFreeSpace();
        nbIntervals = nodeFreeSpace / 10 / HTInterval.DATA_ENTRY_SIZE;
        start = fillValues(ht, INT_VALUE, nbIntervals, start);
        assertEquals(nodeFreeSpace - nbIntervals * HTInterval.DATA_ENTRY_SIZE, node.getNodeFreeSpace());

        /* Add long intervals up to ~30% */
        nodeFreeSpace = node.getNodeFreeSpace();
        nbIntervals = nodeFreeSpace / 10 / (HTInterval.DATA_ENTRY_SIZE + LONG_DOUBLE_SIZE);
        start = fillValues(ht, LONG_VALUE, nbIntervals, start);
        assertEquals(nodeFreeSpace - nbIntervals * (HTInterval.DATA_ENTRY_SIZE + LONG_DOUBLE_SIZE), node.getNodeFreeSpace());

        /* Add string intervals up to ~40% */
        nodeFreeSpace = node.getNodeFreeSpace();
        nbIntervals = nodeFreeSpace / 10 / (HTInterval.DATA_ENTRY_SIZE + TEST_STRING.length() + STRING_PADDING);
        start = fillValues(ht, STRING_VALUE, nbIntervals, start);
        assertEquals(nodeFreeSpace - nbIntervals * (HTInterval.DATA_ENTRY_SIZE + TEST_STRING.length() + STRING_PADDING), node.getNodeFreeSpace());

    }

}
