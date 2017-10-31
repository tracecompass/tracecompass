/**********************************************************************
 * Copyright (c) 2016, 2017 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.memory;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.os.linux.core.memory.MemoryUsageTreeModel;
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

/**
 * Memory usage view
 *
 * @since 2.2
 * @author Samuel Gagnon
 * @author Mahdi Zolnouri
 * @author Wassim Nasrallah
 */
public class MemoryUsageView extends TmfChartView {
   private final String fProviderId;
   private final TmfXYChartSettings fSettings;

    /**
     * Constructor
     *
     * @param title
     *            the Memory view's name.
     * @param providerId
     *            the ID of the provider to use for this view.
     * @param settings
     *            See {@link TmfXYChartSettings} to know what it contains
     */
    public MemoryUsageView(String title, String providerId, TmfXYChartSettings settings) {
        super(title);
        fProviderId = providerId;
        fSettings = settings;
    }

    @Override
    protected TmfXYChartViewer createChartViewer(Composite parent) {
        TmfFilteredXYChartViewer viewer = new TmfFilteredXYChartViewer(parent, fSettings, fProviderId) {
            @Override
            public @NonNull IYAppearance getSeriesAppearance(String seriesName) {
                int width = seriesName.endsWith(MemoryUsageTreeModel.TOTAL_SUFFIX) ? 2 : 1;
                return getPresentationProvider().getAppearance(seriesName, IYAppearance.Type.LINE, width);
            }
        };
        viewer.getSwtChart().getAxisSet().getYAxis(0).getTick().setFormat(DataSizeWithUnitFormat.getInstance());
        return viewer;
    }

    @Override
    protected @NonNull TmfViewer createLeftChildViewer(Composite parent) {
        MemoryUsageTreeViewer fTreeViewer = new MemoryUsageTreeViewer(parent, fProviderId);

        /* Initialize the viewers with the currently selected trace */
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            TmfTraceSelectedSignal signal = new TmfTraceSelectedSignal(this, trace);
            fTreeViewer.traceSelected(signal);
        }

        return fTreeViewer;
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        TmfViewer tree = getLeftChildViewer();
        TmfXYChartViewer chart = getChartViewer();
        if (tree instanceof MemoryUsageTreeViewer && chart instanceof TmfFilteredXYChartViewer) {
            ILegendImageProvider legendImageProvider = new XYChartLegendImageProvider((TmfCommonXAxisChartViewer) chart);
            MemoryUsageTreeViewer memoryTree = (MemoryUsageTreeViewer) tree;
            memoryTree.setTreeListener((TmfFilteredXYChartViewer) chart);
            memoryTree.setLegendImageProvider(legendImageProvider);
        }
    }
}
