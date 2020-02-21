/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests.backend.historytree;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HTInterval;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HTNode;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HistoryTreeBackend;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.IHistoryTree;
import org.eclipse.tracecompass.statesystem.core.tests.stubs.backend.HistoryTreeBackendStub;
import org.eclipse.tracecompass.statesystem.core.tests.stubs.backend.HistoryTreeBackendStub.HistoryTreeType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test the {@link HistoryTreeBackend}-specific behavior and its interactions
 * with the {@link IHistoryTree} classes.
 *
 * @author Geneviève Bastien
 */
@RunWith(Parameterized.class)
public class HistoryTreeWithBackendTest {

    /** State system ID */
    private static final String SSID = "test";
    /** Provider version */
    private static final int PROVIDER_VERSION = 0;

    /** Default maximum number of children nodes */
    private static final int MAX_CHILDREN = 3;
    /** Default block size */
    private static final int BLOCK_SIZE = 4096;

    private final HistoryTreeType fHtType;

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { HistoryTreeType.CLASSIC }
        });
    }

    /**
     * Constructor
     *
     * @param htType
     *            The type of history tree to use
     */
    public HistoryTreeWithBackendTest(HistoryTreeType htType) {
        fHtType = htType;
    }

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
            HistoryTreeBackendStub.setTreeType(fHtType);
            HistoryTreeBackendStub backend = new HistoryTreeBackendStub(SSID, historyTreeFile, PROVIDER_VERSION, startTime, BLOCK_SIZE, MAX_CHILDREN);

            int duration = nbAttr;
            int quarkTest = nbAttr;
            long time = startTime + duration;

            HTInterval interval = new HTInterval(startTime, time, quarkTest, time);
            // Insert a first interval for the test attribute
            backend.insertPastState(interval.getStartTime(), interval.getEndTime(), interval.getAttribute(), interval.getValue());

            /*
             * insert cascading intervals to fill 2 levels of history tree, so
             * that we start another branch
             */
            while (backend.getHistoryTree().getDepth() < depthToRead) {
                backend.insertPastState(
                        Math.max(startTime, time - duration),
                        time - 1,
                        (int) time % nbAttr,
                        time);
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
                        time);
                time++;
            }

            // Add an interval that does not fit in latest leaf, but starts
            // before the current branch
            backend.insertPastState(interval.getEndTime() + 1, time, quarkTest, time);

            backend.getHistoryTree().assertIntegrity();

        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

}
