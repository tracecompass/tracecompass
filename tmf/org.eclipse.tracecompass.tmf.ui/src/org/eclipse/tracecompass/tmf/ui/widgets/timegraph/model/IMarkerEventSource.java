/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model;

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
    @NonNull List<String> getMarkerCategories();

    /**
     * Gets the list of marker events of a specific category that intersect the
     * given time range (inclusively).
     * <p>
     * The list should include the nearest previous marker that starts before
     * the time range (it might already be included if it intersects the time
     * range), and the nearest next marker that starts after the time range.
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
    @NonNull List<IMarkerEvent> getMarkerList(@NonNull String category, long startTime, long endTime, long resolution, @NonNull IProgressMonitor monitor);

}
