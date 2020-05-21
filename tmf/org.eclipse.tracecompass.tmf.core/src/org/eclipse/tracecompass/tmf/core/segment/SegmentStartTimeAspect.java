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

package org.eclipse.tracecompass.tmf.core.segment;

import java.util.Comparator;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.core.segment.Messages;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.SegmentComparators;
import org.eclipse.tracecompass.tmf.core.TmfStrings;

/**
 * An aspect used to get the start time of a segment. It can be used with most
 * segment types.
 *
 * @author David Piché
 * @since 5.2
 *
 */
public final class SegmentStartTimeAspect implements ISegmentAspect {

    /**
     * The Segment start time aspect instance
     */
    public static final ISegmentAspect SEGMENT_START_TIME_ASPECT = new SegmentStartTimeAspect();

    /**
     * Constructor
     */
    private SegmentStartTimeAspect() {
        // Do nothing
    }

    @Override
    public @NonNull String getName() {
        return TmfStrings.startTime();
    }

    @Override
    public @NonNull String getHelpText() {
        return NonNullUtils.nullToEmptyString(Messages.SegmentStartTimeAspect_startDescription);
    }

    @Override
    public @Nullable Comparator<?> getComparator() {
        return SegmentComparators.INTERVAL_START_COMPARATOR.thenComparing(Comparator.comparingLong(ISegment::getLength));
    }

    @Override
    public @Nullable Long resolve(@NonNull ISegment segment) {
        return segment.getStart();
    }

    @Override
    public @NonNull SegmentType getType() {
        return ISegmentAspect.SegmentType.CONTINUOUS;
    }
}
