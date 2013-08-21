/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Adapted for TMF Trace Model 1.0
 *   Alexandre Montplaisir - Port to JUnit4
 *   Marc-Andre Laperle - Adapted to BTree indexer from TmfCheckpointIndexTest
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace.indexer.checkpoint;

import static org.junit.Assert.assertFalse;

import org.eclipse.linuxtools.tmf.core.trace.indexer.TmfBTreeTraceIndexer;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpointIndex;
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
    protected TestIndexerInterface createTestIndexer(TestTrace trace) {
        return new TestBTreeIndexer(trace);
    }

    private static class TestBTreeIndexer extends TmfBTreeTraceIndexer implements TestIndexerInterface {
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

}