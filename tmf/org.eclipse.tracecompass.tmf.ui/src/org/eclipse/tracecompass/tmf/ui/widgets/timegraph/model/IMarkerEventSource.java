/*******************************************************************************
 * Copyright (c) 2015, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Interface to an object which is capable of providing marker events.
 *
 * @since 2.0
 */
public interface IMarkerEventSource {

    /**
     * Gets the list of marker categories that this object provides.
     *
     * @return The list of marker categories
     */
    @NonNull List<@NonNull String> getMarkerCategories();

    /**
     * Gets the list of marker events of a specific category that intersect the
     * given time range (inclusively).
     * <p>
     * The list should also include the nearest previous and next markers that
     * do not intersect the time range.
     *
     * @param category
     *            The marker category
     * @param startTime
     *            Start of the time range
     * @param endTime
     *            End of the time range
     * @param resolution
     *            The resolution
     * @param monitor
     *            The progress monitor object
     * @return The list of marker events
     */
    @NonNull List<@NonNull IMarkerEvent> getMarkerList(@NonNull String category, long startTime, long endTime, long resolution, @NonNull IProgressMonitor monitor);

    /**
     * Gets the list of marker events of all categories that intersect the given
     * time range (inclusively).
     * <p>
     * The list should include, for each category, the nearest previous and next
     * markers that do not intersect the time range.
     *
     * @param startTime
     *            Start of the time range
     * @param endTime
     *            End of the time range
     * @param resolution
     *            The resolution
     * @param monitor
     *            The progress monitor object
     * @return The list of marker events
     * @since 3.0
     */
    default @NonNull List<@NonNull IMarkerEvent> getMarkerList(long startTime, long endTime, long resolution, @NonNull IProgressMonitor monitor) {
        List<@NonNull IMarkerEvent> markers = new ArrayList<>();
        for (String category : getMarkerCategories()) {
            if (monitor.isCanceled()) {
                break;
            }
            markers.addAll(getMarkerList(category, startTime, endTime, resolution, monitor));
        }
        return markers;
    }
}
