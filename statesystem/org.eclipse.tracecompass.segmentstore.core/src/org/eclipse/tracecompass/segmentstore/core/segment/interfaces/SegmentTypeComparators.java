/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core.segment.interfaces;

import java.util.Comparator;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.segmentstore.core.ISegment;

/**
 * Segments comparators for specific segment interfaces
 *
 * @author Geneviève Bastien
 * @noimplement This interface only contains static definitions.
 * @since 2.0
 */
public interface SegmentTypeComparators {

    /**
     * Compare named segments. If segments that are not named are mixed with
     * named segments, their order will be undefined by they will be greater
     * than all the named ones.
     */
    Comparator<ISegment> NAMED_SEGMENT_COMPARATOR = new Comparator<ISegment>() {
        @Override
        public int compare(@Nullable ISegment o1, @Nullable ISegment o2) {
            if (o1 == null || o2 == null) {
                throw new IllegalArgumentException("One of the segment to compare is null, that should never happen"); //$NON-NLS-1$
            }
            String name1 = SegmentAspects.getName(o1);
            String name2 = SegmentAspects.getName(o2);
            if (Objects.equals(name1, name2)) {
                return 0;
            }
            // Segments that are not named are greater than named ones
            if (name2 == null) {
                return -1;
            }
            if (name1 == null) {
                return 1;
            }
            return name1.compareTo(name2);
        }
    };
}
