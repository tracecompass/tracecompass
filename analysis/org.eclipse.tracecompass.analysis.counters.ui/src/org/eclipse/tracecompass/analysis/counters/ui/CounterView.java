/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.counters.ui;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.analysis.counters.ui.Messages;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.ILegendImageProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.XYChartLegendImageProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXAxisChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfXYChartSettings;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;
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
 */
public class CounterView extends TmfChartView {

    /** View ID. */
    public static final String ID = "org.eclipse.tracecompass.analysis.counters.ui.view.counters"; //$NON-NLS-1$

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

        TmfViewer tree = getLeftChildViewer();
        TmfXYChartViewer chart = getChartViewer();
        if (tree instanceof CounterTreeViewer && chart instanceof CounterChartViewer) {
            ILegendImageProvider legendImageProvider = new XYChartLegendImageProvider((TmfCommonXAxisChartViewer) chart);
            CounterTreeViewer counterTree = (CounterTreeViewer) tree;
            counterTree.setTreeListener((CounterChartViewer) chart);
            counterTree.setLegendImageProvider(legendImageProvider);
        }

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
        CounterTreeViewer treeViewer = new CounterTreeViewer(parent);

        // Initialize the tree viewer with the currently selected trace
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            TmfTraceSelectedSignal signal = new TmfTraceSelectedSignal(this, trace);
            treeViewer.traceSelected(signal);
        }

        return treeViewer;
    }

    private Action getCumulativeAction() {
        Action action = new Action(Messages.CounterView_CumulativeAction_Text, IAction.AS_CHECK_BOX) {
            private boolean isCumulative = false;

            @Override
            public void run() {
                isCumulative ^= true;
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
