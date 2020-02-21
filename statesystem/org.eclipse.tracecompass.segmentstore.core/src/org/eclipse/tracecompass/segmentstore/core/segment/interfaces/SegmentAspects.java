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

package org.eclipse.tracecompass.segmentstore.core.segment.interfaces;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.segmentstore.core.ISegment;

/**
 * Interface to return some fields of a segment depending on the interfaces it
 * implements
 *
 * @since 2.0
 */
public final class SegmentAspects {

    private SegmentAspects() {

    }

    /**
     * Get the name aspect of a segment. If the segment is a
     * {@link INamedSegment}, the aspect will return its name. Otherwise, it is
     * <code>null</code>
     *
     * @param segment
     *            The segment to get the aspect of
     * @return The name of a {@link INamedSegment}, <code>null</code> otherwise
     */
    public static @Nullable String getName(ISegment segment) {
        if (segment instanceof INamedSegment) {
            return ((INamedSegment) segment).getName();
        }
        return null;
    }

}
