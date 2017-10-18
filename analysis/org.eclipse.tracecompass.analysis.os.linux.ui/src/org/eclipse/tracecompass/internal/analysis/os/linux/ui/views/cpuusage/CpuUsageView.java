/*******************************************************************************
 * Copyright (c) 2014, 2017 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.cpuusage;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.os.linux.core.signals.TmfCpuSelectedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.ILegendImageProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.XYChartLegendImageProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXAxisChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfXYChartSettings;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.TriStateFilteredCheckboxTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * CPU usage view. It contains 2 viewers: one tree viewer showing all the
 * threads who were on the CPU in the time range, and one XY chart viewer
 * plotting the total time spent on CPU and the time of the threads selected in
 * the tree viewer.
 *
 * @author Geneviève Bastien
 */
public class CpuUsageView extends TmfChartView {

    /** ID string */
    public static final @NonNull String ID = "org.eclipse.tracecompass.analysis.os.linux.views.cpuusage"; //$NON-NLS-1$

    /** ID of the followed CPU in the map data in {@link TmfTraceContext} */
    public static final @NonNull String CPU_USAGE_FOLLOW_CPU = ID + ".FOLLOW_CPU"; //$NON-NLS-1$

    private @Nullable CpuUsageTreeViewer fTreeViewer = null;
    private @Nullable CpuUsageXYViewer fXYViewer = null;

    /*
     * To avoid up and downs CPU usage when process is in and out of CPU frequently,
     * use a smaller resolution to get better averages.
     */
    private static final double RESOLUTION = 0.4;

    /**
     * Constructor
     */
    public CpuUsageView() {
        super(Messages.CpuUsageView_Title);
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        TmfViewer tree = getLeftChildViewer();
        TmfXYChartViewer chart = getChartViewer();
        if (tree instanceof CpuUsageTreeViewer && chart instanceof CpuUsageXYViewer) {
            ILegendImageProvider legendImageProvider = new XYChartLegendImageProvider((TmfCommonXAxisChartViewer) chart);
            CpuUsageTreeViewer cpuTree = (CpuUsageTreeViewer) tree;
            cpuTree.setTreeListener((CpuUsageXYViewer) chart);
            cpuTree.setLegendImageProvider(legendImageProvider);
        }

        /* Initialize the viewers with the currently selected trace */
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            TmfTraceSelectedSignal signal = new TmfTraceSelectedSignal(this, trace);
            if (fTreeViewer != null) {
                fTreeViewer.traceSelected(signal);
            }
            if (fXYViewer != null) {
                fXYViewer.traceSelected(signal);
            }
        }
    }

    @Override
    protected TmfXYChartViewer createChartViewer(Composite parent) {
        TmfXYChartSettings settings = new TmfXYChartSettings(Messages.CpuUsageXYViewer_Title, Messages.CpuUsageXYViewer_TimeXAxis, Messages.CpuUsageXYViewer_CpuYAxis, RESOLUTION);
        CpuUsageXYViewer viewer = new CpuUsageXYViewer(parent, settings);
        viewer.setSendTimeAlignSignals(true);
        fXYViewer = viewer;
        return viewer;
    }

    @Override
    public TmfViewer createLeftChildViewer(Composite parent) {
        // Create the tree viewer with a filtered checkbox
        int treeStyle = SWT.MULTI | SWT.H_SCROLL | SWT.FULL_SELECTION;
        TriStateFilteredCheckboxTree checkboxTree = new TriStateFilteredCheckboxTree(parent, treeStyle, new PatternFilter(), true);
        final CpuUsageTreeViewer viewer = new CpuUsageTreeViewer(parent, checkboxTree);

        viewer.getControl().addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                super.controlResized(e);
            }
        });

        fTreeViewer = viewer;
        return fTreeViewer;
    }

    /**
     * Save a data in the data map of {@link TmfTraceContext}
     */
    private static void saveData(@NonNull ITmfTrace trace, @NonNull String key, @NonNull Object data) {
        TmfTraceManager.getInstance().updateTraceContext(trace,
                builder -> builder.setData(key, data));
    }

    private static Object getData(@NonNull ITmfTrace trace, @NonNull String key) {
        TmfTraceContext ctx = TmfTraceManager.getInstance().getTraceContext(trace);
        return ctx.getData(key);
    }

    protected static @NonNull Set<@NonNull Integer> getCpus(@NonNull ITmfTrace trace) {
        Set<@NonNull Integer> data = (Set<@NonNull Integer>) getData(trace, CpuUsageView.CPU_USAGE_FOLLOW_CPU);
        return data != null ? data : Collections.emptySet();
    }

    @Override
    public void setFocus() {
        if (fXYViewer != null) {
            fXYViewer.getControl().setFocus();
        }
    }

    /**
     * Signal handler for when a cpu is selected
     *
     * @param signal
     *            the cpu being selected
     * @since 2.0
     */
    @TmfSignalHandler
    public void cpuSelect(TmfCpuSelectedSignal signal) {
        final @Nullable CpuUsageXYViewer xyViewer = fXYViewer;
        final @Nullable CpuUsageTreeViewer treeViewer = fTreeViewer;
        ITmfTrace trace = signal.getTrace();
        if (xyViewer != null && treeViewer != null) {
            Set<Integer> data = (Set<Integer>) getData(trace, CPU_USAGE_FOLLOW_CPU);
            if (data == null) {
                data = new TreeSet<>();
                saveData(trace, CPU_USAGE_FOLLOW_CPU, data);
            }
            int core = signal.getCore();
            if (core >= 0) {
                data.add(core);
            } else {
                data.clear();
            }
            xyViewer.refresh();
            xyViewer.setTitle();
            treeViewer.updateContent(treeViewer.getWindowStartTime(), treeViewer.getWindowEndTime(), false);
        }
    }

}
