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
     */
    void dataChanged(List<ISegment> newData);

    /**
     * Notification that the selection of the data changed in the viewer.
     *
     * @param newSelectionData
     *            the new selection of the data
     */
    void dataSelectionChanged(List<ISegment> newSelectionData);
}
