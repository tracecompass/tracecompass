/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.segment;

import static org.junit.Assert.assertEquals;

import java.util.Comparator;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.tmf.core.segment.ISegmentAspect;
import org.eclipse.tracecompass.tmf.core.segment.SegmentEndTimeAspect;
import org.eclipse.tracecompass.tmf.core.segment.SegmentDurationAspect;
import org.eclipse.tracecompass.tmf.core.segment.SegmentStartTimeAspect;
import org.junit.Test;

/**
 * Tests for basic segment aspects (SegmentEndTimeAspect,
 * SegmentStartTimeAspect, SegmentLengthAspect)
 *
 * @author David Piché
 *
 */
public class SegmentAspectTest {

    private static final ISegmentAspect START_ASPECT = SegmentStartTimeAspect.SEGMENT_START_TIME_ASPECT;
    private static final ISegmentAspect END_ASPECT = SegmentEndTimeAspect.SEGMENT_END_TIME_ASPECT;
    private static final ISegmentAspect DURATION_ASPECT = SegmentDurationAspect.SEGMENT_DURATION_ASPECT;
    private static final ISegmentAspect ASPECT_STUB = SegmentAspectStub.SEGMENT_ASPECT_STUB;

    /**
     * Tests the aspect type
     */
    @Test
    public void aspectTypeTest() {
        // By default, SegmentAspectStub is of type categorical
        assertEquals(ISegmentAspect.SegmentType.CATEGORICAL, ASPECT_STUB.getType());
        assertEquals(ISegmentAspect.SegmentType.CONTINUOUS, START_ASPECT.getType());
        assertEquals(ISegmentAspect.SegmentType.CONTINUOUS, END_ASPECT.getType());
        assertEquals(ISegmentAspect.SegmentType.CONTINUOUS, DURATION_ASPECT.getType());
    }

    /**
     * Tests the resolve method
     */
    @Test
    public void aspectResolveTest() {
        long start = 1;
        long end = 3;

        BasicSegment segment = new BasicSegment(start, end);

        assertEquals((long)0, ASPECT_STUB.resolve(segment));
        assertEquals(start, START_ASPECT.resolve(segment));
        assertEquals(end, END_ASPECT.resolve(segment));
        assertEquals(end - start, DURATION_ASPECT.resolve(segment));
    }

    private static class SegmentAspectStub implements ISegmentAspect {

        /**
         * The Segment start time aspect instance
         */
        public static final ISegmentAspect SEGMENT_ASPECT_STUB = new SegmentAspectStub();

        /**
         * Constructor
         */
        private SegmentAspectStub() {
            // Do nothing
        }

        @Override
        public @NonNull String getName() {
            return "Stub Aspect";
        }

        @Override
        public @NonNull String getHelpText() {
            return "Stub Aspect text"; //$NON-NLS-1$
        }

        @Override
        public @Nullable Comparator<?> getComparator() {
            return null;
        }

        @Override
        public @Nullable Long resolve(@NonNull ISegment segment) {
            return (long) 0;
        }

    }
}
