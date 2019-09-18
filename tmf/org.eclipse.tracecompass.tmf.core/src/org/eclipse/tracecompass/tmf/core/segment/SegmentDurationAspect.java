/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * An aspect used to get the duration of a segment. It can be used with all
 * segment types.
 *
 * @author David Piché
 * @since 5.2
 *
 */
public final class SegmentDurationAspect implements ISegmentAspect {

    /**
     * The Segment duration aspect instance
     */
    public static final ISegmentAspect SEGMENT_DURATION_ASPECT = new SegmentDurationAspect();

    /**
     * Constructor
     */
    private SegmentDurationAspect() {
        // Do nothing
    }

    @Override
    public @NonNull String getName() {
        return TmfStrings.duration();
    }

    @Override
    public @NonNull String getHelpText() {
        return NonNullUtils.nullToEmptyString(Messages.SegmentDurationAspect_durationDescription);
    }

    @Override
    public @Nullable Comparator<?> getComparator() {
        return SegmentComparators.INTERVAL_LENGTH_COMPARATOR;
    }

    @Override
    public @Nullable Long resolve(@NonNull ISegment segment) {
        return segment.getLength();
    }

    @Override
    public @NonNull SegmentType getType() {
        return ISegmentAspect.SegmentType.CONTINUOUS;
    }

}
