/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Adapted for TMF Trace Model 1.0
 *   Alexandre Montplaisir - Port to JUnit4
 *   Marc-Andre Laperle - Adapted to BTree indexer from TmfCheckpointIndexTest
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.trace.indexer.checkpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.tracecompass.internal.tmf.core.trace.indexer.BTree;
import org.eclipse.tracecompass.internal.tmf.core.trace.indexer.FlatArray;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.indexer.TmfBTreeTraceIndexer;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.ITmfCheckpointIndex;
import org.junit.Test;

/**
 * Test suite for the TmfBTreeTraceIndexer class.
 *
 * @author Marc-Andre Laperle
 */
public class TmfBTreeIndexTest extends AbstractIndexTest {

    /**
     * Create the indexer for testing
     *
     * @param trace
     *            the trace
     * @return the indexer for testing
     */
    @Override
    protected ITestIndexer createTestIndexer(TestTrace trace) {
        return new TestBTreeIndexer(trace);
    }

    private static class TestBTreeIndexer extends TmfBTreeTraceIndexer implements ITestIndexer {
        public TestBTreeIndexer(TestTrace testTrace) {
            super(testTrace, BLOCK_SIZE);
        }

        @Override
        public ITmfCheckpointIndex getCheckpoints() {
            return getTraceIndex();
        }
    }

    /**
     * Test that a fully built index has the same content when reloaded from disk
     *
     * @throws Exception when error occurs
     */
    @Test
    public void testReopenIndex() throws Exception {
        fTrace.dispose();
        fTrace = createTrace(getTracePath());
        assertFalse(fTrace.getIndexer().getCheckpoints().isCreatedFromScratch());
        fTrace.indexTrace(true);

        verifyIndexContent();
    }

    /**
     * Test that the indexer can resume from a partially built index reloaded
     * from disk
     *
     * @throws Exception
     *             when error occurs
     */
    @Test
    public void testInsertAfterReopenIndex() throws Exception {
        // Make sure we start from a completely non-existing index
        fTrace.dispose();
        String directory = TmfTraceManager.getSupplementaryFileDir(fTrace);
        new File(directory + BTree.INDEX_FILE_NAME).delete();
        new File(directory + FlatArray.INDEX_FILE_NAME).delete();

        // Index half of the trace
        fNbEventsLimit = NB_EVENTS / 2;
        fTrace = createTrace(getTracePath());
        assertTrue(fTrace.getIndexer().getCheckpoints().isCreatedFromScratch());
        // The trace should not have been indexed completely
        assertEquals(fNbEventsLimit, fTrace.getNbEvents());

        // Finish indexing the trace
        fNbEventsLimit = Long.MAX_VALUE;
        fTrace = createTrace(getTracePath());
        assertFalse(fTrace.getIndexer().getCheckpoints().isCreatedFromScratch());

        verifyIndexContent();
    }

}