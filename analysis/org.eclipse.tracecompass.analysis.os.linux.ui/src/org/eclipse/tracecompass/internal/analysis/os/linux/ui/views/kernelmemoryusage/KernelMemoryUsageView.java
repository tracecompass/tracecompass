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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;

import com.google.common.annotations.VisibleForTesting;

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
    /** ID of the Kernel Memory Usage view data in the data map of {@link TmfTraceContext} */
    public static final @NonNull String KERNEL_MEMORY = ID + ".KERNEL_MEMORY"; //$NON-NLS-1$

    /*
     * We need this reference to update the viewer when there is a new selection
     */
    private KernelMemoryUsageTreeViewer fTreeViewerReference = null;

    /**
     * Constructor
     */
    public KernelMemoryUsageView() {
        super(Messages.MemoryUsageView_title);
    }

    @Override
    protected TmfXYChartViewer createChartViewer(Composite parent) {
        return new KernelMemoryUsageViewer(parent);
    }

    private final class SelectionChangeListener implements ISelectionChangedListener {
        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            KernelMemoryUsageTreeViewer treeViewer = fTreeViewerReference;
            KernelMemoryUsageViewer chartViewer = (KernelMemoryUsageViewer) getChartViewer();
            if (treeViewer == null || chartViewer == null) {
                return;
            }
            ITmfTrace trace = treeViewer.getTrace();
            if (trace == null) {
                return;
            }
            ISelection selection = event.getSelection();
            if (selection instanceof IStructuredSelection) {
                Object structSelection = ((IStructuredSelection) selection).getFirstElement();
                if (structSelection instanceof KernelMemoryUsageEntry) {
                    KernelMemoryUsageEntry entry = (KernelMemoryUsageEntry) structSelection;
                    String tid = entry.getTid();
                    if (tid == null) {
                        return;
                    }
                    treeViewer.setSelectedThread(tid);
                    chartViewer.setSelectedThread(tid);
                    TmfTraceManager.getInstance().updateTraceContext(trace,
                            builder -> builder.setData(KERNEL_MEMORY, tid));
                }
            }
        }
    }

    @Override
    protected @NonNull TmfViewer createLeftChildViewer(Composite parent) {
        @NonNull
        KernelMemoryUsageTreeViewer fTreeViewer = new KernelMemoryUsageTreeViewer(parent);
        fTreeViewerReference = fTreeViewer;

        fTreeViewer.addSelectionChangeListener(new SelectionChangeListener());

        /* Initialize the viewers with the currently selected trace */
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            TmfTraceSelectedSignal signal = new TmfTraceSelectedSignal(this, trace);
            fTreeViewer.traceSelected(signal);
            fTreeViewerReference.traceSelected(signal);
        }

        fTreeViewer.getControl().addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                super.controlResized(e);
            }
        });
        return fTreeViewer;
    }

    @VisibleForTesting
    @Override
    public TmfXYChartViewer getChartViewer() {
        // change visibility
        return super.getChartViewer();
    }
}
