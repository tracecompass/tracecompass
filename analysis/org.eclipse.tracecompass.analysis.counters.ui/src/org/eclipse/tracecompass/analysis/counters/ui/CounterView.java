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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;

/**
 * Main implementation for the counter analysis view.
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

    /**
     * Title of the chart viewer
     */
    public static final String VIEW_TITLE = "Counters Chart"; //$NON-NLS-1$

    private CounterTreeViewer fTreeViewer;
    private CounterChartViewer fChartViewer;

    /**
     * Constructor
     */
    public CounterView() {
        super(VIEW_TITLE);
    }

    @Override
    protected TmfXYChartViewer createChartViewer(Composite parent) {
        fChartViewer = new CounterChartViewer(parent);
        return fChartViewer;
    }

    @Override
    protected @NonNull TmfViewer createLeftChildViewer(Composite parent) {
        CounterTreeViewer treeViewer = new CounterTreeViewer(parent);
        treeViewer.addSelectionChangeListener(new SelectionChangeListener());

        // Initialize the viewers with the currently selected trace
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            TmfTraceSelectedSignal signal = new TmfTraceSelectedSignal(this, trace);
            treeViewer.traceSelected(signal);
        }
        fTreeViewer = treeViewer;
        return treeViewer;
    }

    private final class SelectionChangeListener implements ISelectionChangedListener {

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            CounterChartViewer chartViewer = fChartViewer;
            if (chartViewer == null) {
                return;
            }

            if (fTreeViewer.getTrace() == null) {
                return;
            }

            ISelection selection = event.getSelection();
            if (selection instanceof IStructuredSelection) {
                Object entry = ((IStructuredSelection) selection).getFirstElement();
                if (entry instanceof CounterTreeViewerEntry) {
                    chartViewer.updateChart(((CounterTreeViewerEntry) entry).getQuark());
                }
            }
        }

    }

}
