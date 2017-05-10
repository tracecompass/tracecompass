/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core.tests.historytree;

import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.tracecompass.datastore.core.interval.IHTIntervalReader;
import org.eclipse.tracecompass.internal.provisional.segmentstore.core.ISegment2;
import org.eclipse.tracecompass.internal.segmentstore.core.segmentHistoryTree.HistoryTreeSegmentStore;

/**
 * Base stub class, that resolves the node type of the tree to the stub node
 *
 * @author Geneviève Bastien
 *
 * @param <E>
 *            The type of segments accepted in this store
 */
public class HistoryTreeSegmentStoreStub<E extends ISegment2> extends HistoryTreeSegmentStore<E> {

    /**
     * Constructor for new history files. Use this when creating a new history
     * from scratch.
     *
     * @param newStateFile
     *            The filename/location where to store the state history (Should
     *            end in .ht)
     * @param startTime
     *            The earliest time stamp that will be stored in the history
     * @param factory
     *            Factory to read history tree objects from the backend
     * @throws IOException
     *             Thrown if we can't create the file for some reason
     */
    public HistoryTreeSegmentStoreStub(Path newStateFile,
            long startTime,
            IHTIntervalReader<E> factory) throws IOException {
        super(newStateFile, factory);
    }

}
