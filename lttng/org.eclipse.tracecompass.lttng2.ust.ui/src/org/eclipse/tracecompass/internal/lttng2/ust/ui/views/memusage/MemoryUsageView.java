/**********************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 **********************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.ui.views.memusage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.widgets.Composite;
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
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.TreePatternFilter;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.TriStateFilteredCheckboxTree;

/**
 * Memory Usage View
 *
 * @author Matthew Khouzam
 */
public class MemoryUsageView extends TmfChartView {

    /** ID string */
    public static final String ID = "org.eclipse.linuxtools.lttng2.ust.memoryusage"; //$NON-NLS-1$

    /**
     * Constructor
     */
    public MemoryUsageView() {
        super(Messages.MemoryUsageView_Title);
    }

    @Override
    protected TmfXYChartViewer createChartViewer(Composite parent) {
        TmfXYChartSettings settings = new TmfXYChartSettings(Messages.MemoryUsageViewer_Title, Messages.MemoryUsageViewer_XAxis, Messages.MemoryUsageViewer_YAxis, 1);
        return new MemoryUsageViewer(parent, settings);
    }

    @Override
    protected TmfViewer createLeftChildViewer(Composite parent) {
        // Create the tree viewer with a filtered checkbox
        int treeStyle = SWT.MULTI | SWT.H_SCROLL | SWT.FULL_SELECTION;
        TriStateFilteredCheckboxTree triStateFilteredCheckboxTree = new TriStateFilteredCheckboxTree(parent, treeStyle, new TreePatternFilter(), true);
        MemoryUsageTreeViewer fTreeViewer = new MemoryUsageTreeViewer(parent, triStateFilteredCheckboxTree);

        /* Initialize the viewers with the currently selected trace */
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            TmfTraceSelectedSignal signal = new TmfTraceSelectedSignal(this, trace);
            fTreeViewer.traceSelected(signal);
        }

        fTreeViewer.getControl().addControlListener(new ControlAdapter() {});
        return fTreeViewer;
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        TmfViewer tree = getLeftChildViewer();
        TmfXYChartViewer chart = getChartViewer();
        if (tree instanceof MemoryUsageTreeViewer && chart instanceof MemoryUsageViewer) {
            ILegendImageProvider legendImageProvider = new XYChartLegendImageProvider((TmfCommonXAxisChartViewer) chart);
            MemoryUsageTreeViewer kernelMemoryTree = (MemoryUsageTreeViewer) tree;
            kernelMemoryTree.setTreeListener((MemoryUsageViewer) chart);
            kernelMemoryTree.setLegendImageProvider(legendImageProvider);
        }
    }
}
