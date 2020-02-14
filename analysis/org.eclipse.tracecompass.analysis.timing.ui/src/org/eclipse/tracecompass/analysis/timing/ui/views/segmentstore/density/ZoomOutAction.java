/******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.AnalysisTimingImageConstants;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.timing.ui.Activator;
import org.swtchart.Range;

/**
 * Zoom action for the density view
 */
@Deprecated
class ZoomOutAction extends Action {

    private final AbstractSegmentStoreDensityView fView;

    /**
     * Constructors a ZoomOutAction.
     *
     * @param densityViewer
     *            The parent density viewer
     */
    public ZoomOutAction(AbstractSegmentStoreDensityView densityViewer) {
        fView = densityViewer;
    }

    @Override
    public void run() {
        final AbstractSegmentStoreDensityViewer chart = fView.getDensityViewer();
        if (chart != null) {
            chart.zoom(new Range(AbstractSegmentStoreDensityView.DEFAULT_RANGE.getFirst(), AbstractSegmentStoreDensityView.DEFAULT_RANGE.getSecond()));
        }
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return NonNullUtils.checkNotNull(Activator.getDefault().getImageDescripterFromPath(AnalysisTimingImageConstants.IMG_UI_ZOOM_OUT_MENU));
    }

    @Override
    public String getToolTipText() {
        return NonNullUtils.checkNotNull(Messages.AbstractSegmentStoreDensityViewer_ZoomOutActionToolTipText);
    }
}