/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.datastore.core.historytree;

import java.io.File;
import java.io.IOException;

import org.eclipse.tracecompass.datastore.core.interval.HTInterval;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.AbstractHistoryTree;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.HTNode;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.HistoryTreeStub;

/**
 * Test the default abstract implementation of the tree, with a concrete stub.
 * It contains only children
 *
 * @author Geneviève Bastien
 */
public class HistoryTreeStubTest extends AbstractHistoryTreeTestBase<HTInterval, HTNode<HTInterval>> {

    private static final HTInterval DEFAULT_OBJECT = new HTInterval(10, 20);

    @Override
    protected HistoryTreeStub createHistoryTree(File stateHistoryFile,
            int blockSize,
            int maxChildren,
            int providerVersion,
            long treeStart) throws IOException {

        return new HistoryTreeStub(stateHistoryFile,
                blockSize,
                maxChildren,
                providerVersion,
                treeStart);
    }

    @Override
    protected HistoryTreeStub createHistoryTree(File existingStateFile, int expProviderVersion)
            throws IOException {
        return new HistoryTreeStub(existingStateFile, expProviderVersion);
    }

    @Override
    protected long fillValues(AbstractHistoryTree<HTInterval, HTNode<HTInterval>> ht, int sizeLimit, long start) {
        int objectSize = DEFAULT_OBJECT.getSizeOnDisk();
        int nbValues = sizeLimit / objectSize;
        for (int i = 0; i < nbValues; i++) {
            ht.insert(new HTInterval(start + i, start + i + 1));
        }
        return start + nbValues;
    }

    @Override
    protected HTInterval createInterval(long start, long end) {
        return new HTInterval(start, end);
    }

}
