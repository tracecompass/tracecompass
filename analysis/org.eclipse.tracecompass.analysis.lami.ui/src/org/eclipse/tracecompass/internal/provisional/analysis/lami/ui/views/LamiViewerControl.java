/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.views;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.analysis.lami.ui.Activator;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiChartModel;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiResultTable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.viewers.ILamiViewer;

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

    private @Nullable ILamiViewer fViewer;

    /**
     * Build a new control for a Lami table viewer.
     *
     * @param parent
     *            The parent composite
     * @param table
     *            The results table populating the table viewer
     */
    public LamiViewerControl(Composite parent, LamiResultTable table) {
        fToggleAction = new Action() {
            @Override
            public void run() {
                ILamiViewer viewer = fViewer;
                if (viewer == null) {
                    fViewer = ILamiViewer.createLamiTable(parent, table);
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
     * @param table
     *            The table containing the source data
     * @param graphModel
     *            The graph model
     */
    public LamiViewerControl(Composite parent, LamiResultTable table, LamiChartModel graphModel) {
        fToggleAction = new Action() {
            @Override
            public void run() {
                ILamiViewer viewer = fViewer;
                if (viewer == null) {
                    fViewer = ILamiViewer.createLamiChart(parent, table, graphModel);
                } else {
                    viewer.dispose();
                    fViewer = null;
                }
                parent.layout();
            }
        };
        fToggleAction.setText(Messages.LamiReportView_ToggleAction_ButtonNamePrefix + ' ' + graphModel.getName());
        fToggleAction.setToolTipText(Messages.LamiReportView_ToggleAction_ButtonTooltip);
        fToggleAction.setImageDescriptor(getIconForGraphType(graphModel.getChartType()));
    }

    /**
     * Get the viewer of this control. Returns null if the viewer is current
     * disposed.
     *
     * @return The viewer
     */
    public @Nullable ILamiViewer getViewer() {
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

    private static @Nullable ImageDescriptor getIconForGraphType(LamiChartModel.ChartType graphType) {
        switch (graphType) {
        case BAR_CHART:
            return Activator.getDefault().getImageDescripterFromPath("icons/histogram.gif"); //$NON-NLS-1$
        case PIE_CHART:
        case XY_SCATTER:
        default:
            // FIXME Use other icons
            return Activator.getDefault().getImageDescripterFromPath("icons/histogram.gif"); //$NON-NLS-1$
        }
    }

}
