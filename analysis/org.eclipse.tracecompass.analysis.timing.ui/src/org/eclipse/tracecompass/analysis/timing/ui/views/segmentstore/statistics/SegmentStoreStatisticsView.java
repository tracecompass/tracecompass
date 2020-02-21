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

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.statistics;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * Generic view for showing segment store statistics analysis data
 *
 * @since 2.0
 */
public class SegmentStoreStatisticsView extends AbstractSegmentsStatisticsView {

    /**
     * ID of this view
     */
    public static final String ID = "org.eclipse.tracecompass.analysis.timing.ui.segstore.statistics"; //$NON-NLS-1$

    @Override
    public void createPartControl(@Nullable Composite parent) {
        super.createPartControl(parent);
        // Set the title of the view from the actual view ID
        IViewDescriptor desc = PlatformUI.getWorkbench().getViewRegistry().find(getViewId());
        if (desc != null) {
            setPartName(desc.getLabel());
        }
    }

    @Override
    protected @NonNull AbstractSegmentsStatisticsViewer createSegmentStoreStatisticsViewer(@NonNull Composite parent) {
        // The analysis ID is the secondary ID of the view
        String analysisId = NonNullUtils.nullToEmptyString(getViewSite().getSecondaryId());
        return new SegmentStoreStatisticsViewer(parent, analysisId);
    }

}
