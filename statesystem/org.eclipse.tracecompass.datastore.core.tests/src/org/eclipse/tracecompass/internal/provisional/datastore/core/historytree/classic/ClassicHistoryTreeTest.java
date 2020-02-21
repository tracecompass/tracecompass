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

package org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.classic;

import java.io.File;
import java.io.IOException;

import org.eclipse.tracecompass.datastore.core.interval.HTInterval;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.AbstractHistoryTree;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.AbstractHistoryTreeTestBase;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.classic.ClassicHistoryTreeStub;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.classic.ClassicNode;

/**
 * Test the classic history tree
 *
 * @author Geneviève Bastien
 */
public class ClassicHistoryTreeTest
        extends AbstractHistoryTreeTestBase<HTInterval, ClassicNode<HTInterval>> {

    private static final HTInterval DEFAULT_OBJECT = new HTInterval(0, 0);


    @Override
    protected ClassicHistoryTreeStub createHistoryTree(
            File stateHistoryFile,
            int blockSize,
            int maxChildren,
            int providerVersion,
            long treeStart) throws IOException {

        return new ClassicHistoryTreeStub(stateHistoryFile,
                blockSize,
                maxChildren,
                providerVersion,
                treeStart);
    }

    @Override
    protected ClassicHistoryTreeStub createHistoryTree(
            File existingStateFile, int expectedProviderVersion) throws IOException {
        return new ClassicHistoryTreeStub(existingStateFile, expectedProviderVersion);
    }

    @Override
    protected HTInterval createInterval(long start, long end) {
        return new HTInterval(start, end);
    }

    @Override
    protected long fillValues(AbstractHistoryTree<HTInterval, ClassicNode<HTInterval>> ht,
            int fillSize, long start) {

        int nbValues = fillSize / DEFAULT_OBJECT.getSizeOnDisk();
        for (int i = 0; i < nbValues; i++) {
            ht.insert(new HTInterval(start + i, start + i + 1));
        }
        return start + nbValues;
    }

}
