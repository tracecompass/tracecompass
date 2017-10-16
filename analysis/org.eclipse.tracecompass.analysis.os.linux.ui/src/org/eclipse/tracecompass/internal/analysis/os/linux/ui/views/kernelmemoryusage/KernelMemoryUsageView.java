/**********************************************************************
 * Copyright (c) 2016, 2017 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.kernelmemoryusage;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelmemoryusage.KernelMemoryUsageDataProvider;
import org.eclipse.tracecompass.common.core.format.DataSizeWithUnitFormat;
import org.eclipse.tracecompass.internal.provisional.tmf.core.presentation.IYAppearance;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.ILegendImageProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.XYChartLegendImageProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXAxisChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfFilteredXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfXYChartSettings;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.TreePatternFilter;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.TriStateFilteredCheckboxTree;

/**
 * Memory usage view
 *
 * @author Samuel Gagnon
 * @author Mahdi Zolnouri
 * @author Wassim Nasrallah
 */
public class KernelMemoryUsageView extends TmfChartView {

    /** ID string */
    public static final @NonNull String ID = "org.eclipse.tracecompass.analysis.os.linux.ui.kernelmemoryusageview"; //$NON-NLS-1$
    private static final int DEFAULT_SERIES_WIDTH = 1;
    /**
     * Constructor
     */
    public KernelMemoryUsageView() {
        super(Messages.MemoryUsageView_title);
    }

    @Override
    protected TmfXYChartViewer createChartViewer(Composite parent) {
        TmfXYChartSettings settings = new TmfXYChartSettings(Messages.MemoryUsageViewer_title, Messages.MemoryUsageViewer_xAxis, Messages.MemoryUsageViewer_yAxis, 1);
        TmfFilteredXYChartViewer viewer = new TmfFilteredXYChartViewer(parent, settings, KernelMemoryUsageDataProvider.ID) {
            @Override
            public IYAppearance getSeriesAppearance(@NonNull String seriesName) {
                int thickness = seriesName.endsWith(KernelMemoryUsageDataProvider.TOTAL_SUFFIX) ? 2 * DEFAULT_SERIES_WIDTH : DEFAULT_SERIES_WIDTH;
                return getPresentationProvider().getAppearance(seriesName, IYAppearance.Type.LINE, thickness);
            }
        };
        viewer.getSwtChart().getAxisSet().getYAxis(0).getTick().setFormat(DataSizeWithUnitFormat.getInstance());
        return viewer;
    }

    @Override
    protected @NonNull TmfViewer createLeftChildViewer(Composite parent) {
        // Create the tree viewer with a filtered checkbox
        int treeStyle = SWT.MULTI | SWT.H_SCROLL | SWT.FULL_SELECTION;
        TriStateFilteredCheckboxTree triStateFilteredCheckboxTree = new TriStateFilteredCheckboxTree(parent, treeStyle, new TreePatternFilter(), true);
        KernelMemoryUsageTreeViewer fTreeViewer = new KernelMemoryUsageTreeViewer(parent, triStateFilteredCheckboxTree);

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
        if (tree instanceof KernelMemoryUsageTreeViewer && chart instanceof TmfFilteredXYChartViewer) {
            ILegendImageProvider legendImageProvider = new XYChartLegendImageProvider((TmfCommonXAxisChartViewer) chart);
            KernelMemoryUsageTreeViewer kernelMemoryTree = (KernelMemoryUsageTreeViewer) tree;
            kernelMemoryTree.setTreeListener((TmfFilteredXYChartViewer) chart);
            kernelMemoryTree.setLegendImageProvider(legendImageProvider);
        }
    }
}
