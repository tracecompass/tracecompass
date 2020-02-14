/*******************************************************************************
 * Copyright (c) 2017, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.counters.ui.views;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.analysis.counters.ui.Messages;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.linechart.TmfXYChartSettings;
import org.eclipse.tracecompass.tmf.ui.views.xychart.TmfChartView;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Main implementation for the counters view.
 *
 * <p>
 * The view is composed of two parts:
 * <ol>
 * <li>CounterTreeViewer (left-hand side)</li>
 * <li>CounterChartViewer (right-hand side)</li>
 * </ol>
 * </p>
 *
 * @author Matthew Khouzam
 * @author Mikael Ferland
 * @since 1.0
 */
public class CounterView extends TmfChartView {

    /** View ID. */
    public static final String ID = "org.eclipse.tracecompass.analysis.counters.ui.views.countersview"; //$NON-NLS-1$

    /**
     * Title of the chart viewer
     */
    public static final String VIEW_TITLE = "Counters"; //$NON-NLS-1$

    /**
     * Constructor
     */
    public CounterView() {
        super(VIEW_TITLE);
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        // Add a tool bar button to display counters data cumulatively
        IActionBars bars = getViewSite().getActionBars();
        bars.getToolBarManager().appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, getCumulativeAction());
    }

    @Override
    protected TmfXYChartViewer createChartViewer(Composite parent) {
        return new CounterChartViewer(parent, new TmfXYChartSettings(null, null, null, 1));
    }

    @Override
    protected @NonNull TmfViewer createLeftChildViewer(Composite parent) {
        return new CounterTreeViewer(parent);
    }

    private Action getCumulativeAction() {
        Action action = new Action(Messages.CounterView_CumulativeAction_Text, IAction.AS_CHECK_BOX) {

            @Override
            public void run() {
                boolean isCumulative = isChecked();
                setToolTipText(isCumulative ? Messages.CounterView_CumulativeAction_DifferentialTooltipText : Messages.CounterView_CumulativeAction_CumulativeTooltipText);
                TmfXYChartViewer chart = getChartViewer();
                if (chart instanceof CounterChartViewer) {
                    ((CounterChartViewer) chart).toggleCumulative();
                }
            }
        };

        action.setToolTipText(Messages.CounterView_CumulativeAction_CumulativeTooltipText);
        action.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.tracecompass.tmf.ui", "icons/elcl16/sigma.gif")); //$NON-NLS-1$ //$NON-NLS-2$
        return action;
    }

}
