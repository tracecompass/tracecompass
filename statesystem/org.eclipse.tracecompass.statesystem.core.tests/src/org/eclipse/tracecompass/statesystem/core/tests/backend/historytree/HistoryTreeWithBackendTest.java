/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests.backend.historytree;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HTInterval;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HTNode;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HistoryTree;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HistoryTreeBackend;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.statesystem.core.tests.stubs.backend.HistoryTreeBackendStub;
import org.junit.Test;

/**
 * Test the {@link HistoryTreeBackend}-specific behavior and its interactions
 * with the {@link HistoryTree} class
 *
 * @author Geneviève Bastien
 */
public class HistoryTreeWithBackendTest {

    /** State system ID */
    private static final String SSID = "test";
    /** Provider version */
    private static final int PROVIDER_VERSION = 0;

    /** Default maximum number of children nodes */
    private static final int MAX_CHILDREN = 3;
    /** Default block size */
    private static final int BLOCK_SIZE = 4096;

    /**
     * Test the behavior of the history tree after at least a depth of 3
     */
    @Test
    public void testFillNodes() {
        try {
            // Test case parameters
            final int nbAttr = 5;
            final int depthToRead = 3;

            long startTime = 1;
            File historyTreeFile = NonNullUtils.checkNotNull(File.createTempFile("HistoryTreeBackendTest", ".ht"));
            HistoryTreeBackendStub backend = new HistoryTreeBackendStub(SSID, historyTreeFile, PROVIDER_VERSION, startTime, BLOCK_SIZE, MAX_CHILDREN);

            int duration = nbAttr;
            int quarkTest = nbAttr;
            long time = startTime + duration;

            HTInterval interval = new HTInterval(startTime, time, quarkTest, TmfStateValue.newValueLong(time));
            // Insert a first interval for the test attribute
            backend.insertPastState(interval.getStartTime(), interval.getEndTime(), interval.getAttribute(), interval.getStateValue());

            /*
             * insert cascading intervals to fill 2 levels of history
             * tree, so that we start another branch
             */
            while (backend.getHistoryTree().getDepth() < depthToRead) {
                backend.insertPastState(
                        Math.max(startTime, time - duration),
                        time - 1,
                        (int) time % nbAttr,
                        TmfStateValue.newValueLong(time));
                time++;
            }

            // entirely fill the latest leaf with cascading intervals
            HTNode latestLeaf = backend.getHistoryTree().getLatestLeaf();
            /*
             * Add an interval while there is still room for it or make sure the
             * node does not get written on disk in the meantime.
             */
            while (interval.getSizeOnDisk() <= latestLeaf.getNodeFreeSpace() || latestLeaf.isOnDisk()) {
                backend.insertPastState(
                        Math.max(startTime, time - duration),
                        time - 1,
                        (int) time % nbAttr,
                        TmfStateValue.newValueLong(time));
                time++;
            }

            // Add an interval that does not fit in latest leaf, but starts
            // before the current branch
            backend.insertPastState(interval.getEndTime() + 1, time, quarkTest, TmfStateValue.newValueLong(time));

            backend.getHistoryTree().assertIntegrity();

        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

}
