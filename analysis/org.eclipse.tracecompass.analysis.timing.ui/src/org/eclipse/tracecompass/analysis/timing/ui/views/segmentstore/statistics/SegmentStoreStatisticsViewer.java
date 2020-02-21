/*******************************************************************************
 * Copyright (c) 2016, 2018 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.statistics;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.SegmentStoreStatisticsDataProvider;

/**
 * Generic viewer to show segment store statistics analysis data.
 *
 * @since 2.0
 */
public class SegmentStoreStatisticsViewer extends AbstractSegmentsStatisticsViewer {

    /**
     * Constructor
     *
     * @param parent
     *            The parent composite
     * @param analysisId
     *            The ID of the segment store provider to do statistics on
     */
    public SegmentStoreStatisticsViewer(Composite parent, String analysisId) {
        super(parent, SegmentStoreStatisticsDataProvider.ID + ':' + analysisId);
    }

}
