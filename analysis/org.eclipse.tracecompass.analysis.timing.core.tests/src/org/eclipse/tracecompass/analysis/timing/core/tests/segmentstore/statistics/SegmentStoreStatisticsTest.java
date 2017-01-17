/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.core.tests.segmentstore.statistics;

import java.util.Collection;
import java.util.Random;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.timing.core.tests.statistics.AbstractStatisticsTest;
import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;

/**
 * Test statistics for segment length
 *
 * @author Matthew Khouzam
 */
public class SegmentStoreStatisticsTest extends AbstractStatisticsTest<@NonNull ISegment> {

    private final Random fRandom = new Random(10);

    /**
     * Constructor
     */
    public SegmentStoreStatisticsTest() {
        super(s -> s.getLength());
    }

    @Override
    protected Collection<@NonNull ISegment> createElementsWithValues(Collection<@NonNull Long> longFixture) {
        return longFixture.stream()
                .map(l -> {
                    long nextStart = fRandom.nextInt(10000000);
                    long nextEnd = nextStart + l;
                    // Check the boundaries, it is random after all, so if
                    // there's an overflow, just take the max value as end time
                    if (nextEnd < nextStart) {
                        nextEnd = Long.MAX_VALUE;
                        nextStart = nextEnd - l;
                    }
                    return new BasicSegment(nextStart, nextEnd);
                })
                .collect(Collectors.toList());

    }

    @Override
    public void testLimitDataset2() {
        /* ISegments do not support negative values */
    }

    @Override
    public void testLargeDatasetNegative() {
        /* ISegments do not support negative values */
    }

}
