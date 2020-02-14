/******************************************************************************
 * Copyright (c) 2015, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density2;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.segmentstore.core.ISegment;

/**
 * A listener that gets notified when the viewer sees its data changed or its
 * data selection change.
 * @since 4.1
 */
public interface ISegmentStoreDensityViewerDataListener {

    /**
     * Notification that the data changed in the viewer.
     *
     * @param newData
     *            the new data
     */
    default void viewDataChanged(Iterable<? extends ISegment> newData) {
        // To be implemented by children
    }

    /**
     * Notification that the selection of the data changed in the viewer.
     *
     * @param newSelectionData
     *            the new selection of the data
     */
    default void selectedDataChanged(@Nullable Iterable<? extends ISegment> newSelectionData) {
        // To be implemented in children
    }

    /**
     * Notification that chart updated
     */
    default void chartUpdated() {
        // To be implemented in children
    }

}
