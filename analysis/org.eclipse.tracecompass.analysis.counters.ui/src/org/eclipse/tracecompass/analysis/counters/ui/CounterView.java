/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.counters.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.analysis.counters.ui.TriStateFilteredCheckboxTree;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;

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

    /**
     * Title of the chart viewer
     */
    public static final String VIEW_TITLE = "Counters"; //$NON-NLS-1$

    private TriStateFilteredCheckboxTree fTriStateFilteredCheckboxTree;

    /**
     * Constructor
     */
    public CounterView() {
        super(VIEW_TITLE);
    }

    @Override
    protected TmfXYChartViewer createChartViewer(Composite parent) {
        return new CounterChartViewer(parent);
    }

    @Override
    protected @NonNull TmfViewer createLeftChildViewer(Composite parent) {
        // Create the tree viewer with a filtered checkbox
        fTriStateFilteredCheckboxTree = new TriStateFilteredCheckboxTree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.FULL_SELECTION, new CounterTreePatternFilter(), true);
        fTriStateFilteredCheckboxTree.addCheckStateListener(new CheckStateChangedListener());
        CounterTreeViewer treeViewer = new CounterTreeViewer(parent, fTriStateFilteredCheckboxTree);

        // Initialize the tree viewer with the currently selected trace
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            TmfTraceSelectedSignal signal = new TmfTraceSelectedSignal(this, trace);
            treeViewer.traceSelected(signal);
        }

        return treeViewer;
    }

    private final class CheckStateChangedListener implements ICheckStateListener {

        @Override
        public void checkStateChanged(CheckStateChangedEvent event) {
            TmfXYChartViewer chartViewer = getChartViewer();
            if (chartViewer == null) {
                return;
            }

            ICheckable checkboxTree = event.getCheckable();
            if (checkboxTree instanceof CheckboxTreeViewer) {
                List<CounterTreeViewerEntry> entries = new ArrayList<>();
                for (Object checkedElement : fTriStateFilteredCheckboxTree.getCheckedElements()) {
                    if (checkedElement instanceof CounterTreeViewerEntry) {
                        entries.add((CounterTreeViewerEntry) checkedElement);
                    }
                }

                // The chart uses the quarks to display data for each counters
                if (chartViewer instanceof CounterChartViewer) {
                    ((CounterChartViewer) chartViewer).updateChart(entries);
                }
            }
        }

    }

}
