/******************************************************************************
 * Copyright (c) 2015, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.segmentstore.core.ISegment;

/**
 * A listener that gets notified when the viewer sees its data changed or its
 * data selection change.
 */
public interface ISegmentStoreDensityViewerDataListener {

    /**
     * Notification that the data changed in the viewer.
     *
     * @param newData
     *            the new data
     * @since 2.0
     */
    default void viewDataChanged(Iterable<? extends ISegment> newData) {
        // To be implemented by children
    }

    /**
     * Notification that the selection of the data changed in the viewer.
     *
     * @param newSelectionData
     *            the new selection of the data
     * @since 2.0
     */
    default void selectedDataChanged(@Nullable Iterable<? extends ISegment> newSelectionData) {
        // To be implemented in children
    }

    /**
     * Notification that chart updated
     * @since 2.2
     */
    default void chartUpdated() {
        // To be implemented in children
    }

}
