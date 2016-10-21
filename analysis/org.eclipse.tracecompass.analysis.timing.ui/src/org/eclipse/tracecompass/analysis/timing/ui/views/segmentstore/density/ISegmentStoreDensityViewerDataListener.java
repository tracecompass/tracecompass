/******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density;

import java.util.List;

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
     * @deprecated Use {@link #viewDataChanged(Iterable)} instead
     */
    @Deprecated
    void dataChanged(List<ISegment> newData);

    /**
     * Notification that the selection of the data changed in the viewer.
     *
     * @param newSelectionData
     *            the new selection of the data
     * @deprecated Use {@link #selectedDataChanged(Iterable)} instead
     */
    @Deprecated
    void dataSelectionChanged(@Nullable List<ISegment> newSelectionData);

    /**
     * Notification that the data changed in the viewer.
     *
     * @param newData
     *            the new data
     * @since 1.4
     */
    default void viewDataChanged(Iterable<? extends ISegment> newData) {
        // To be implemented by children
    }

    /**
     * Notification that the selection of the data changed in the viewer.
     *
     * @param newSelectionData
     *            the new selection of the data
     * @since 1.4
     */
    default void selectedDataChanged(@Nullable Iterable<? extends ISegment> newSelectionData) {
        // To be implemented in children
    }
}
