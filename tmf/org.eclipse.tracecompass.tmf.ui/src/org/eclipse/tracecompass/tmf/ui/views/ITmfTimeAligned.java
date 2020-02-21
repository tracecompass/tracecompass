/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views;

import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentInfo;

/**
 * An interface that adds the time-axis alignment feature to a view. This
 * interface provides information about the current state of alignment of the
 * view as well as performs alignment operations in case the views become
 * misaligned (resize, moved, etc).
 *
 * @since 1.0
 */
public interface ITmfTimeAligned {

    /**
     * Get the time alignment information. The view provides information about
     * where the time-axis is in addition to information necessary to decide
     * whether or not views should be time-aligned with each other (Shell,
     * location).
     *
     * @return the time alignment information
     */
    TmfTimeViewAlignmentInfo getTimeViewAlignmentInfo();

    /**
     * Get the available width for the specified time-axis offset. The
     * implementation should return the width that would be available if the
     * time-axis was to be at that offset. When about to perform a re-alignment,
     * the alignment algorithm will choose the narrowest width to accommodate
     * all views.
     *
     * @param requestedOffset
     *            the requested time-axis offset. Greater or equal to zero.
     * @return the available width. Should be greater or equal to zero.
     */
    int getAvailableWidth(int requestedOffset);

    /**
     * Perform the alignment by moving the time-axis to the specified offset and
     * the resizing it to the specified width. Implementations should handle
     * cases were the requested width is greater than the width of the view. For
     * example, Integer.MAX_VALUE can be requested in order to obtain the
     * largest width possible.
     *
     * @param offset
     *            the time-axis offset. Greater or equal to zero.
     * @param width
     *            the time-axis width. Greater or equal to zero. Can be
     *            Integer.MAX_VALUE.
     */
    void performAlign(int offset, int width);
}
