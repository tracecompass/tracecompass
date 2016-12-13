/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core.tests.perf;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.internal.provisional.segmentstore.core.BasicSegment2;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.tests.historytree.HistoryTreeSegmentStoreStub;
import org.junit.runners.Parameterized.Parameters;

/**
 * Benchmark the segment store with datasets that are too big to fit in memory
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public class SegmentStoreBigBenchmark extends SegmentStoreBenchmark {

    /**
     * Constructor
     *
     * @param name
     *            name of the benchmark
     * @param segStore
     *            The segment store to use
     */
    public SegmentStoreBigBenchmark(String name, ISegmentStore<@NonNull BasicSegment2> segStore) {
        super(name, segStore);
    }

    /**
     * @return The arrays of parameters
     * @throws IOException
     *             Exceptions thrown when setting the on-disk backends
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() throws IOException {
        return Arrays.asList(new Object[][] {
                { "HT store", new HistoryTreeSegmentStoreStub<>(Files.createTempFile("tmpSegStore", null), 1, BasicSegment2.BASIC_SEGMENT_READ_FACTORY) },
        });
    }

    @Override
    protected long getSegmentStoreSize() {
        return 100000000;
    }
}
