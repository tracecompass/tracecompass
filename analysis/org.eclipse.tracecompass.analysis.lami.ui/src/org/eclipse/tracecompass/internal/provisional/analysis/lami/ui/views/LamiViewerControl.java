/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.views;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.analysis.lami.ui.Activator;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.viewers.LamiTableViewer;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartData;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartModel;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartType;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.ui.chart.IChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;

/**
 * Control for Lami viewers.
 *
 * Since viewers can be disposed, the "viewer control" will remain and be ready
 * to re-instantiate the viewer if required to.
 *
 * @author Alexandre Montplaisir
 */
public final class LamiViewerControl {

    private final Action fToggleAction;

    private @Nullable TmfViewer fViewer;

    /**
     * Build a new control for a Lami table viewer.
     *
     * @param parent
     *            The parent composite
     * @param page
     *            The {@link LamiReportViewTabPage} page parent
     */
    public LamiViewerControl(Composite parent, LamiReportViewTabPage page) {
        fToggleAction = new Action() {
            @Override
            public void run() {
                TmfViewer viewer = fViewer;
                if (viewer == null) {
                    fViewer = LamiTableViewer.createLamiTable(parent, page);
                } else {
                    viewer.dispose();
                    fViewer = null;
                }
                parent.layout();
            }
        };
        fToggleAction.setText(Messages.LamiReportView_ActivateTableAction_ButtonName);
        fToggleAction.setToolTipText(Messages.LamiReportView_ActivateTableAction_ButtonTooltip);
        fToggleAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath("icons/table.gif")); //$NON-NLS-1$
    }

    /**
     * Build a new control for a graph viewer.
     *
     * @param parent
     *            The parent composite
     * @param data
     *            The {@link LamiReportViewTabPage} parent page
     * @param model
     *            The graph model
     */
    public LamiViewerControl(Composite parent, ChartData data, ChartModel model) {
        fToggleAction = new Action() {
            @Override
            public void run() {
                TmfViewer viewer = fViewer;
                if (viewer == null) {
                    fViewer = (TmfViewer) IChartViewer.createChart(parent, data, model);
                } else {
                    viewer.dispose();
                    fViewer = null;
                }
                parent.layout();
            }
        };
        fToggleAction.setText(Messages.LamiReportView_ToggleAction_ButtonNamePrefix + ' ' + model.getTitle());
        fToggleAction.setToolTipText(Messages.LamiReportView_ToggleAction_ButtonTooltip);
        fToggleAction.setImageDescriptor(getIconForGraphType(model.getChartType()));
    }

    /**
     * Get the viewer of this control. Returns null if the viewer is current
     * disposed.
     *
     * @return The viewer
     */
    public @Nullable TmfViewer getViewer() {
        return fViewer;
    }

    /**
     * Get the toggle action that shows/hide this control's viewer.
     *
     * @return The toggle action
     */
    public Action getToggleAction() {
        return fToggleAction;
    }

    /**
     * Explicitly dispose this control's viewer.
     */
    public void dispose() {
        if (fViewer != null) {
            fViewer.dispose();
        }
    }

    private static @Nullable ImageDescriptor getIconForGraphType(ChartType chartType) {
        switch (chartType) {
        case BAR_CHART:
            return Activator.getDefault().getImageDescripterFromPath("icons/histogram.gif"); //$NON-NLS-1$
        case PIE_CHART:
        case SCATTER_CHART:
        default:
            // FIXME Use other icons
            return Activator.getDefault().getImageDescripterFromPath("icons/histogram.gif"); //$NON-NLS-1$
        }
    }

}
